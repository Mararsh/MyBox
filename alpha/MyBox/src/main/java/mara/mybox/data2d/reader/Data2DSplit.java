package mara.mybox.data2d.reader;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.data2d.Data2D_Edit;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.CsvTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TmpFileTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public class Data2DSplit extends Data2DOperator {

    protected long splitSize, startIndex, currentSize;
    protected String prefix;
    protected List<Data2DColumn> targetColumns;
    protected List<String> names;
    protected File currentFile;
    protected List<DataFileCSV> files;

    public static Data2DSplit create(Data2D_Edit data) {
        Data2DSplit op = new Data2DSplit();
        return op.setData(data) ? op : null;
    }

    @Override
    public boolean checkParameters() {
        try {
            if (cols == null || cols.isEmpty() || splitSize <= 0) {
                return false;
            }
            startIndex = 1;
            currentSize = 0;
            prefix = data2D.dataName();
            names = new ArrayList<>();
            targetColumns = new ArrayList<>();
            for (int c : cols) {
                Data2DColumn column = data2D.column(c);
                names.add(column.getColumnName());
                targetColumns.add(column.cloneAll().setD2cid(-1).setD2id(-1));
            }
            if (includeRowNumber) {
                targetColumns.add(0, new Data2DColumn(message("SourceRowNumber"), ColumnDefinition.ColumnType.Long));
            }
            files = new ArrayList<>();
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return false;
        }
    }

    @Override
    public void handleRow() {
        try {
            List<String> row = new ArrayList<>();
            for (int col : cols) {
                if (col >= 0 && col < sourceRow.size()) {
                    row.add(sourceRow.get(col));
                } else {
                    row.add(null);
                }
            }
            if (row.isEmpty()) {
                return;
            }
            if (currentFile == null) {
                currentFile = TmpFileTools.getTempFile(".csv");
                csvPrinter = CsvTools.csvPrinter(currentFile);
                csvPrinter.printRecord(names);
                startIndex = rowIndex;
            }
            if (includeRowNumber) {
                row.add(0, rowIndex + "");
            }
            csvPrinter.printRecord(row);
            currentSize++;
            if (currentSize % splitSize == 0) {
                closeFile();
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
        }
    }

    public void closeFile() {
        try {
            if (csvPrinter == null) {
                return;
            }
            csvPrinter.flush();
            csvPrinter.close();
            csvPrinter = null;
            File file = data2D.tmpFile(prefix + "_" + startIndex + "-" + rowIndex, null, ".csv");
            if (FileTools.rename(currentFile, file) && file.exists()) {
                DataFileCSV dataFileCSV = new DataFileCSV();
                dataFileCSV.setTask(task);
                dataFileCSV.setColumns(targetColumns)
                        .setFile(file)
                        .setCharset(Charset.forName("UTF-8"))
                        .setDelimiter(",")
                        .setHasHeader(true)
                        .setColsNumber(targetColumns.size())
                        .setRowsNumber(currentSize);
                dataFileCSV.saveAttributes();
                dataFileCSV.stopTask();
                files.add(dataFileCSV);
            }
            currentFile = null;
            currentSize = 0;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
        }
    }

    @Override
    public boolean end() {
        closeFile();
        return true;
    }

    /*
        set
     */
    public Data2DSplit setSplitSize(int splitSize) {
        this.splitSize = splitSize;
        return this;
    }

    /*
        get
     */
    public List<DataFileCSV> getFiles() {
        return files;
    }

}
