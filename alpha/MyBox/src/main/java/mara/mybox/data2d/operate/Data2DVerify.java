package mara.mybox.data2d.operate;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.Data2D_Edit;
import mara.mybox.data2d.tools.Data2DColumnTools;
import mara.mybox.data2d.writer.DataFileCSVWriter;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public class Data2DVerify extends Data2DOperate {

    private DataFileCSVWriter writer;

    public static Data2DVerify create(Data2D_Edit data) {
        Data2DVerify op = new Data2DVerify();
        return op.setSourceData(data) ? op : null;
    }

    @Override
    public boolean checkParameters() {
        File csvFile = sourceData.tmpFile(sourceData.getName(), "Verify", "csv");
        writer = new DataFileCSVWriter();
        writer.setPrintFile(csvFile);
        List<Data2DColumn> columns = columns();
        writer.setColumns(columns)
                .setHeaderNames(Data2DColumnTools.toNames(columns))
                .setWriteHeader(true);
        addWriter(writer);
        return super.checkParameters();
    }

    @Override
    public void handleRow(List<String> row, long index) {
        sourceRow = row;
        sourceRowIndex = index;
        if (sourceRow == null) {
            return;
        }
        List<List<String>> invalids = verify(sourceData, sourceRowIndex, sourceRow);
        if (invalids != null) {
            for (List<String> invalid : invalids) {
                writer.writeRow(invalid);
            }
        }
    }

    @Override
    public boolean end() {
        try {
            if (writer == null || writer.getTargetRowIndex() <= 0) {
                writer.recordTargetData = false;
            }
            return super.end();
        } catch (Exception e) {
            showError(e.toString());
            return false;
        }
    }

    @Override
    public boolean openResults() {
        if (writer == null) {
            return false;
        }
        if (writer.getTargetRowIndex() <= 0) {
            controller.popInformation(message("RowsNumber") + ": " + sourceRowIndex + "\n"
                    + message("AllValuesValid"), 5000);
            return true;
        }
        return super.openResults();
    }

    /*
        static
     */
    public static List<Data2DColumn> columns() {
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(message("Row"), ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(message("Column"), ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(message("Invalid"), ColumnDefinition.ColumnType.String));
        return columns;
    }

    public static List<String> columnNames() {
        return Data2DColumnTools.toNames(columns());
    }

    public static List<List<String>> verify(Data2D data, long rowIndex, List<String> row) {
        try {
            List< List<String>> invalids = new ArrayList<>();
            int rowSize = row.size();
            for (int c = 0; c < data.columnsNumber(); c++) {
                Data2DColumn column = data.column(c);
                if (column.isAuto()) {
                    continue;
                }
                String value = c < rowSize ? row.get(c) : null;
                String item = null;
                if (column.isNotNull() && (value == null || value.isBlank())) {
                    item = message("Null");
                } else if (!column.validValue(value)) {
                    item = message(column.getType().name());
                } else if (!data.validValue(value)) {
                    item = message("TextDataComments");
                }
                if (item == null) {
                    continue;
                }
                List<String> invalid = new ArrayList<>();
                invalid.addAll(Arrays.asList((rowIndex + 1) + "", column.getColumnName(), item));
                invalids.add(invalid);
            }
            return invalids;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
