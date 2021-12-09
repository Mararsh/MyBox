package mara.mybox.data;

import java.io.File;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
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
        type = Type.Clipboard;
    }

    public int type() {
        return type(Type.Clipboard);
    }

    public File newFile() {
        return new File(AppPaths.getDataClipboardPath() + File.separator + DateTools.nowFileString() + ".csv");
    }

    public static DataClipboard create(SingletonTask task, List<String> cols, List<List<String>> data) {
        DataClipboard d = new DataClipboard();
        d.setTask(task);
        File tmpFile = d.tmpFile(cols, data);
        if (tmpFile == null) {
            return null;
        }
        File dFile = d.newFile();
        if (FileTools.rename(tmpFile, dFile, false)) {
            d.setFile(dFile);
            d.setCharset(Charset.forName("UTF-8"));
            d.setDelimiter(",");
            d.setHasHeader(cols != null && !cols.isEmpty());
            d.setDataName(dFile.getName());
            try ( Connection conn = DerbyBase.getConnection()) {
                Data2DDefinition def = d.getTableData2DDefinition().insertData(conn, d);
                d.cloneAll(def);
                long did = d.getD2did();
                if (did < 0) {
                    return null;
                }
                if (cols != null && !cols.isEmpty()) {
                    try {
                        List<Data2DColumn> dCols = new ArrayList<>();
                        for (String name : cols) {
                            dCols.add(new Data2DColumn(name, d.defaultColumnType()));
                        }
                        d.setColumns(dCols);
                        d.getTableData2DColumn().save(conn, did, dCols);
                    } catch (Exception e) {
                        if (task != null) {
                            task.setError(e.toString());
                        }
                        MyBoxLog.error(e);
                    }
                }
            } catch (Exception e) {
                if (task != null) {
                    task.setError(e.toString());
                }
                MyBoxLog.error(e);
                return null;
            }
            return d;
        } else {
            MyBoxLog.error("Failed");
            return null;
        }
    }

}
