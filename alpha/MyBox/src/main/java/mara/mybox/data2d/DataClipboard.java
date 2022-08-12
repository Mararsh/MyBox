package mara.mybox.data2d;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.controller.DataInMyBoxClipboardController;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
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
        type = Type.MyBoxClipboard;
    }

    public int type() {
        return type(Type.MyBoxClipboard);
    }

    @Override
    public boolean checkForSave() {
        if (dataName == null || dataName.isBlank()) {
            dataName = rowsNumber + "x" + colsNumber;
        }
        return true;
    }

    public static File newFile() {
        return new File(AppPaths.getDataClipboardPath() + File.separator + DateTools.nowFileString() + ".csv");
    }

    public static DataClipboard create(SingletonTask task, String dname,
            List<Data2DColumn> cols, List<List<String>> data) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        DataClipboard d = new DataClipboard();
        d.setTask(task);
        List<String> names = new ArrayList<>();
        for (Data2DColumn c : cols) {
            names.add(c.getColumnName());
        }
        File tmpFile = d.tmpFile(dname, names, data);
        if (tmpFile == null) {
            return null;
        }
        File dFile = newFile();
        if (FileTools.rename(tmpFile, dFile, true)) {
            return create(task, dname, cols, dFile, data.size(), data.get(0).size());
        } else {
            MyBoxLog.error("Failed");
            return null;
        }
    }

    public static DataClipboard create(SingletonTask task, String name,
            List<Data2DColumn> cols, File dFile, long rowsNumber, long colsNumber) {
        if (dFile == null || rowsNumber <= 0) {
            return null;
        }
        try {
            DataClipboard d = new DataClipboard();
            d.setTask(task);
            d.setFile(dFile);
            d.setCharset(Charset.forName("UTF-8"));
            d.setDelimiter(",");
            d.setHasHeader(cols != null && !cols.isEmpty());
            if (rowsNumber > 0 && colsNumber > 0) {
                d.setColsNumber(colsNumber);
                d.setRowsNumber(rowsNumber);
            }
            if (name != null && !name.isBlank()) {
                d.setDataName(name);
            } else if (rowsNumber > 0 && colsNumber > 0) {
                d.setDataName(rowsNumber + "x" + colsNumber);
            } else {
                d.setDataName(dFile.getName());
            }
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

    public static DataClipboard create(SingletonTask task, List<Data2DColumn> cols, Data2D data) {
        if (data == null || data.getFile() == null) {
            return null;
        }
        File dFile = new File(AppPaths.getDataClipboardPath() + File.separator + data.getFile().getName());
        if (FileTools.rename(data.getFile(), dFile, true)) {
            DataClipboard d = new DataClipboard();
            d.cloneAll(data);
            d.setType(Type.MyBoxClipboard).setFile(dFile).setDataName(data.getFile().getName());
            if (Data2D.saveAttributes(d, cols)) {
                DataInMyBoxClipboardController.update();
                return d;
            } else {
                return null;
            }
        } else {
            MyBoxLog.error("Failed");
            return null;
        }
    }

}
