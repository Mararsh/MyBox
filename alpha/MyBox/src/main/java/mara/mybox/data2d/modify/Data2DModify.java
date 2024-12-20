package mara.mybox.data2d.modify;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import mara.mybox.data.SetValue;
import mara.mybox.data2d.DataTable;
import mara.mybox.data2d.operate.Data2DOperate;
import mara.mybox.data2d.writer.Data2DWriter;
import mara.mybox.db.data.ColumnDefinition.InvalidAs;
import mara.mybox.db.data.Data2DColumn;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-8-16
 * @License Apache License Version 2.0
 */
public abstract class Data2DModify extends Data2DOperate {

    protected Data2DWriter writer;
    protected long rowsNumber;

    protected int dataIndex, digit;
    protected SetValue setValue;
    protected String dataValue;
    protected Random random = new Random();

    public void initWriter() {
        writer = sourceData.selfWriter();
        addWriter(writer);
    }

    public boolean initSetValue(SetValue v) {
        if (sourceData == null || v == null) {
            return false;
        }
        setValue = v;
        digit = setValue.countFinalDigit(sourceData.getRowsNumber());
        dataIndex = setValue.getStart();
        dataValue = setValue.getValue();
        random = new Random();
        return true;
    }

    @Override
    public boolean checkParameters() {
        rowsNumber = 0;
        return super.checkParameters();
    }

    public void setValue(List<String> row, long index) {
        try {
            sourceRow = row;
            sourceRowIndex = index;
            targetRow = null;
            if (sourceRow == null) {
                return;
            }
            passFilter = sourceData.filterDataRow(sourceRow, sourceRowIndex);
            reachMax = sourceData.filterReachMaxPassed();
            boolean handle = passFilter && !reachMax;
            if (!handle && (sourceData instanceof DataTable)) {
                return;
            }
            targetRow = new ArrayList<>();
            int rowSize = sourceRow.size();
            String currentValue;
            for (int i = 0; i < sourceData.columns.size(); i++) {
                if (i < rowSize) {
                    currentValue = sourceRow.get(i);
                } else {
                    currentValue = null;
                }
                String v;
                if (handle && cols.contains(i)) {
                    Data2DColumn column = sourceData.columns.get(i);
                    v = setValue.makeValue(sourceData, column,
                            currentValue, sourceRow, sourceRowIndex, dataIndex, digit, random);
                    if (setValue.error != null || !column.validValue(v)) {
                        if (invalidAs == InvalidAs.Skip) {
                            v = currentValue;
                        } else if (invalidAs == InvalidAs.Fail) {
                            failStop(message("InvalidData") + ". "
                                    + message("Column") + ":" + column.getColumnName() + "  "
                                    + message("Value") + ": " + v);
                            return;
                        }
                    }
                    dataIndex++;
                    handledCount++;
                } else {
                    v = currentValue;
                }
                targetRow.add(v);
            }
            writeRow();
        } catch (Exception e) {
            showError(e.toString());
        }
    }

    @Override
    public boolean end() {
        try {
            if (!super.end()
                    || writer == null
                    || writer.getTargetData() == null) {
                return false;
            }
            rowsNumber = writer.getTargetRowIndex();
            sourceData.setRowsNumber(rowsNumber);
            sourceData.setTableChanged(false);
            return true;
        } catch (Exception e) {
            showError(e.toString());
            return false;
        }
    }

    public long rowsCount() {
        if (failed) {
            return -1;
        } else {
            return rowsNumber;
        }
    }

}
