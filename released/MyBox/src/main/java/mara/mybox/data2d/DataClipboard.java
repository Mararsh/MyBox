package mara.mybox.data2d;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import mara.mybox.controller.DataInMyBoxClipboardController;
import mara.mybox.data2d.writer.Data2DWriter;
import mara.mybox.data2d.writer.DataFileCSVWriter;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppPaths;

/**
 * @Author Mara
 * @CreateDate 2021-8-25
 * @License Apache License Version 2.0
 */
public class DataClipboard extends DataFileCSV {

    public DataClipboard() {
        dataType = DataType.MyBoxClipboard;
    }

    public int type() {
        return type(DataType.MyBoxClipboard);
    }

    @Override
    public boolean checkForSave() {
        if (dataName == null || dataName.isBlank()) {
            dataName = rowsNumber + "x" + colsNumber;
        }
        return true;
    }

    @Override
    public Data2DWriter selfWriter() {
        DataFileCSVWriter writer = new DataFileCSVWriter();
        writer.setCharset(charset)
                .setDelimiter(delimiter)
                .setWriteHeader(hasHeader)
                .setPrintFile(file)
                .setTargetData(this)
                .setRecordTargetFile(false)
                .setRecordTargetData(true);
        return writer;
    }

    public static File newFile() {
        return new File(AppPaths.getDataClipboardPath() + File.separator + DateTools.nowFileString() + ".csv");
    }

    public static DataClipboard create(FxTask task, String clipName,
            List<Data2DColumn> cols, List<List<String>> data) {
        if (cols == null || data == null || data.isEmpty()) {
            return null;
        }
        DataFileCSV csvData = DataFileCSV.save(task, null, clipName, ",", cols, data);
        if (csvData == null) {
            return null;
        }
        File dFile = newFile();
        if (FileTools.override(csvData.getFile(), dFile, true)) {
            return create(task, csvData, clipName, dFile);
        } else {
            MyBoxLog.error("Failed");
            return null;
        }
    }

    public static DataClipboard create(FxTask task, Data2D sourceData,
            String clipName, File dFile) {
        if (dFile == null || sourceData == null) {
            return null;
        }
        try {
            DataClipboard d = new DataClipboard();
            d.setTask(task);
            d.setFile(dFile);
            d.setCharset(Charset.forName("UTF-8"));
            d.setDelimiter(",");
            List<Data2DColumn> cols = sourceData.getColumns();
            long rowsNumber = sourceData.getRowsNumber();
            long colsNumber = sourceData.getColsNumber();
            d.setHasHeader(cols != null && !cols.isEmpty());
            if (rowsNumber > 0 && colsNumber > 0) {
                d.setColsNumber(colsNumber);
                d.setRowsNumber(rowsNumber);
            }
            String srcName = sourceData.dataName();
            if (clipName != null && !clipName.isBlank()) {
                d.setDataName(clipName);
            } else if (srcName != null && !srcName.isBlank()) {
                d.setDataName(srcName);
            } else if (rowsNumber > 0 && colsNumber > 0) {
                d.setDataName(rowsNumber + "x" + colsNumber);
            } else {
                d.setDataName(dFile.getName());
            }
            d.setComments(sourceData.getComments());
            if (Data2D.saveAttributes(d, cols)) {
                DataInMyBoxClipboardController.update();
                return d;
            } else {
                return null;
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
    }

}
