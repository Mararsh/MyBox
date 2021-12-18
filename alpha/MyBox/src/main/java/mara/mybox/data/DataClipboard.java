package mara.mybox.data;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.controller.DataClipboardController;
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

    public static DataClipboard create(SingletonTask task, List<Data2DColumn> cols, List<List<String>> data) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        DataClipboard d = new DataClipboard();
        d.setTask(task);
        List<String> names = new ArrayList<>();
        for (Data2DColumn c : cols) {
            names.add(c.getName());
        }
        File tmpFile = d.tmpFile(names, data);
        if (tmpFile == null) {
            return null;
        }
        File dFile = newFile();
        if (FileTools.rename(tmpFile, dFile, false)) {
            return create(task, cols, dFile, data.size(), data.get(0).size());
        } else {
            MyBoxLog.error("Failed");
            return null;
        }
    }

    public static DataClipboard create(SingletonTask task, List<Data2DColumn> cols, File dFile,
            int rowsNumber, int colsNumber) {
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
            d.setDataName(rowsNumber + "x" + colsNumber);
            d.setColsNumber(colsNumber);
            d.setRowsNumber(rowsNumber);
            if (Data2D.save(d, cols)) {
                DataClipboardController.update();
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
