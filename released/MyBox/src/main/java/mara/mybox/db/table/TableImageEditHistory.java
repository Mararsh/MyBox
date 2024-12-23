package mara.mybox.db.table;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.controller.BaseTaskController;
import mara.mybox.db.Database;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.ImageEditHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import static mara.mybox.fxml.WindowTools.recordError;
import static mara.mybox.fxml.WindowTools.recordInfo;
import static mara.mybox.fxml.WindowTools.taskError;
import static mara.mybox.fxml.WindowTools.taskInfo;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.value.AppPaths;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-10-15 9:31:28
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class TableImageEditHistory extends BaseTable<ImageEditHistory> {

    public TableImageEditHistory() {
        tableName = "Image_Edit_History";
        defineColumns();
    }

    public TableImageEditHistory(boolean defineColumns) {
        tableName = "Image_Edit_History";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableImageEditHistory defineColumns() {
        addColumn(new ColumnDefinition("iehid", ColumnType.Long, true, true).setAuto(true));
        addColumn(new ColumnDefinition("image_location", ColumnType.File, true).setLength(FilenameMaxLength));
        addColumn(new ColumnDefinition("history_location", ColumnType.File, true).setLength(FilenameMaxLength));
        addColumn(new ColumnDefinition("thumbnail_file", ColumnType.File).setLength(FilenameMaxLength));
        addColumn(new ColumnDefinition("operation_time", ColumnType.Datetime, true));
        addColumn(new ColumnDefinition("update_type", ColumnType.String).setLength(128));
        addColumn(new ColumnDefinition("object_type", ColumnType.String).setLength(128));
        addColumn(new ColumnDefinition("op_type", ColumnType.String).setLength(128));
        addColumn(new ColumnDefinition("scope_type", ColumnType.String).setLength(128));
        addColumn(new ColumnDefinition("scope_name", ColumnType.String).setLength(StringMaxLength));
        orderColumns = "operation_time DESC";
        return this;
    }

    public static final String QueryPath
            = "SELECT * FROM Image_Edit_History  WHERE image_location=? ";

    public static final String QueryHistories
            = "SELECT * FROM Image_Edit_History  WHERE image_location=? ORDER BY operation_time DESC";

    public static final String DeleteHistories
            = "DELETE FROM Image_Edit_History  WHERE image_location=?";

    public static final String QueryFile
            = "SELECT * FROM Image_Edit_History  WHERE history_location=? OR thumbnail_file=?";

    @Override
    public boolean setValue(ImageEditHistory data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        return data.setValue(column, value);
    }

    @Override
    public Object getValue(ImageEditHistory data, String column) {
        if (data == null || column == null) {
            return null;
        }
        return data.getValue(column);
    }

    @Override
    public boolean valid(ImageEditHistory data) {
        if (data == null) {
            return false;
        }
        return data.valid();
    }

    public File path(File file) {
        if (file == null || !file.exists()) {
            return null;
        }
        try (Connection conn = DerbyBase.getConnection()) {
            return path(conn, file);
        } catch (Exception e) {
            return null;
        }
    }

    public File path(Connection conn, File file) {
        if (conn == null || file == null || !file.exists()) {
            return null;
        }
        try (PreparedStatement query = conn.prepareStatement(QueryPath)) {
            query.setString(1, file.getAbsolutePath());
            ImageEditHistory his = query(conn, query);
            if (his != null) {
                File hisFile = his.getHistoryFile();
                if (hisFile != null && hisFile.exists()) {
                    return hisFile.getParentFile();
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return null;
    }

    public List<ImageEditHistory> read(File srcFile) {
        if (srcFile == null || !srcFile.exists()) {
            return null;
        }
        try (Connection conn = DerbyBase.getConnection()) {
            return read(conn, srcFile);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return null;
    }

    public List<ImageEditHistory> read(Connection conn, File srcFile) {
        List<ImageEditHistory> records = new ArrayList<>();
        if (conn == null || srcFile == null || !srcFile.exists()) {
            return records;
        }
        try (PreparedStatement statement = conn.prepareStatement(QueryHistories)) {
            int max = UserConfig.getInt(conn, "MaxImageHistories", ImageEditHistory.Default_Max_Histories);
            if (max <= 0) {
                max = ImageEditHistory.Default_Max_Histories;
                UserConfig.setInt(conn, "MaxImageHistories", ImageEditHistory.Default_Max_Histories);
            }
            conn.setAutoCommit(true);
            statement.setMaxRows(max);
            statement.setString(1, srcFile.getAbsolutePath());
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    ImageEditHistory his = readData(results);
                    if (his == null) {
                        continue;
                    }
                    if (!his.valid() || records.size() >= max) {
                        deleteData(conn, his);
                    } else {
                        records.add(his);
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return records;
    }

    public ImageEditHistory index(Connection conn, File srcFile, int index) {
        List<ImageEditHistory> records = read(conn, srcFile);
        if (records == null || srcFile == null || !srcFile.exists()) {
            return null;
        }
        int size = records.size();
        if (index >= 0 && index < size) {
            return records.get(index);
        } else {
            return null;
        }
    }

    public int count(Connection conn, File file) {
        List<ImageEditHistory> records = read(conn, file);
        if (records == null) {
            return -1;
        } else {
            return records.size();
        }
    }

    @Override
    public int deleteData(ImageEditHistory data) {
        if (data == null) {
            return 0;
        }
        try (Connection conn = DerbyBase.getConnection()) {
            return deleteData(conn, data);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return 0;
        }
    }

    @Override
    public int deleteData(Connection conn, ImageEditHistory data) {
        if (data == null) {
            return 0;
        }
        int count = super.deleteData(conn, data);
        if (count > 0) {
            FileDeleteTools.delete(data.getHistoryFile());
            FileDeleteTools.delete(data.getThumbnailFile());
        }
        return count;
    }

    public long clearHistories(FxTask task, File srcFile) {
        long count = 0;
        if (srcFile == null) {
            return count;
        }
        try (Connection conn = DerbyBase.getConnection()) {
            List<String> files = new ArrayList<>();
            files.add(srcFile.getAbsolutePath());
            return clearHistories(task, conn, files);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return count;
        }
    }

    public long clearHistories(FxTask task, Connection conn, List<String> files) {
        long count = 0;
        if (conn == null || files == null || files.isEmpty()) {
            return count;
        }
        taskInfo(task, QueryHistories);
        try (PreparedStatement statement = conn.prepareStatement(QueryHistories)) {
            conn.setAutoCommit(true);
            for (String file : files) {
                if (task != null && task.isCancelled()) {
                    return count;
                }
                taskInfo(task, message("Check") + ": " + file);
                statement.setString(1, file);
                try (ResultSet results = statement.executeQuery()) {
                    while (results.next()) {
                        if (task != null && task.isCancelled()) {
                            return count;
                        }
                        ImageEditHistory data = readData(results);
                        File hisFile = data.getHistoryFile();
                        if (hisFile != null) {
                            taskInfo(task, message("Delete") + ": " + hisFile);
                            FileDeleteTools.delete(hisFile);
                        }
                        File thumbFile = data.getThumbnailFile();
                        if (thumbFile != null) {
                            taskInfo(task, message("Delete") + ": " + thumbFile);
                            FileDeleteTools.delete(thumbFile);
                        }
                    }
                } catch (Exception e) {
                    taskError(task, e.toString() + "\n" + tableName);
                }
            }
        } catch (Exception e) {
            taskError(task, e.toString() + "\n" + tableName);
        }
        taskInfo(task, DeleteHistories);
        if (task != null && task.isCancelled()) {
            return count;
        }
        try (PreparedStatement statement = conn.prepareStatement(DeleteHistories)) {
            conn.setAutoCommit(true);
            for (String file : files) {
                if (task != null && task.isCancelled()) {
                    return count;
                }
                taskInfo(task, message("Clear") + ": " + file);
                statement.setString(1, file);
                statement.executeUpdate();
            }
        } catch (Exception e) {
            taskError(task, e.toString() + "\n" + tableName);
        }
        return count;
    }

    public int clearAll() {
        try (Connection conn = DerbyBase.getConnection(); Statement statement = conn.createStatement()) {
            String sql = " SELECT history_location FROM Image_Edit_History";
            try (ResultSet results = statement.executeQuery(sql)) {
                while (results.next()) {
                    FileDeleteTools.delete(results.getString("history_location"));
                }
            }
            String imageHistoriesPath = AppPaths.getImageHisPath();
            File path = new File(imageHistoriesPath);
            if (path.exists()) {
                File[] files = path.listFiles();
                if (files != null) {
                    for (File f : files) {
                        FileDeleteTools.delete(f);
                    }
                }
            }
            sql = "DELETE FROM Image_Edit_History";
            return statement.executeUpdate(sql);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return -1;
        }
    }

    public int clearInvalid(BaseTaskController taskController, Connection conn) {
        int count = clearInvalidRows(taskController, conn);
        return count + clearInvalidFiles(taskController, conn);
    }

    public int clearInvalidRows(BaseTaskController taskController, Connection conn) {
        int rowCount = 0, invalidCount = 0;
        try {
            recordInfo(taskController, message("Check") + ": " + tableName);
            try (PreparedStatement query = conn.prepareStatement(queryAllStatement()); PreparedStatement delete = conn.prepareStatement(deleteStatement())) {
                conn.setAutoCommit(true);
                try (ResultSet results = query.executeQuery()) {
                    conn.setAutoCommit(false);
                    while (results.next()) {
                        rowCount++;
                        if (taskController != null && taskController.getTask() != null
                                && taskController.getTask().isCancelled()) {
                            return invalidCount;
                        }
                        ImageEditHistory data = readData(results);
                        if (!ImageEditHistory.valid(data)) {
                            File hisFile = data.getHistoryFile();
                            if (hisFile != null) {
                                recordInfo(taskController, message("Delete") + ": " + hisFile);
                                FileDeleteTools.delete(hisFile);
                            }
                            File thumbFile = data.getThumbnailFile();
                            if (thumbFile != null) {
                                recordInfo(taskController, message("Delete") + ": " + thumbFile);
                                FileDeleteTools.delete(thumbFile);
                            }
                            if (setDeleteStatement(conn, delete, data)) {
                                delete.addBatch();
                                if (invalidCount > 0 && (invalidCount % Database.BatchSize == 0)) {
                                    int[] res = delete.executeBatch();
                                    for (int r : res) {
                                        if (r > 0) {
                                            invalidCount += r;
                                        }
                                    }
                                    conn.commit();
                                    delete.clearBatch();
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    recordError(taskController, e.toString() + "\n" + tableName);
                }
                int[] res = delete.executeBatch();
                for (int r : res) {
                    if (r > 0) {
                        invalidCount += r;
                    }
                }
                conn.commit();
            } catch (Exception e) {
                recordError(taskController, e.toString() + "\n" + tableName);
            }
            conn.setAutoCommit(true);
        } catch (Exception e) {
            recordError(taskController, e.toString() + "\n" + tableName);
        }
        recordInfo(taskController, message("Checked") + ": " + rowCount + " "
                + message("Expired") + ": " + invalidCount);
        return invalidCount;
    }

    public int clearInvalidFiles(BaseTaskController taskController, Connection conn) {
        int rowCount = 0, invalidCount = 0;
        try {
            String ihRootpath = AppPaths.getImageHisPath();
            recordInfo(taskController, message("Check") + ": " + ihRootpath);
            String[] ihPaths = new File(ihRootpath).list();
            if (ihPaths == null || ihPaths.length == 0) {
                return invalidCount;
            }
            try (PreparedStatement query = conn.prepareStatement(QueryFile)) {
                conn.setAutoCommit(true);
                for (String pathname : ihPaths) {
                    if (taskController != null && taskController.getTask() != null
                            && taskController.getTask().isCancelled()) {
                        return invalidCount;
                    }
                    String path = ihRootpath + File.separator + pathname;
                    String[] names = new File(path).list();
                    if (names == null || names.length == 0) {
                        try {
                            new File(path).delete();
                            recordInfo(taskController, message("Delete") + ": " + path);
                        } catch (Exception ex) {
                        }
                        continue;
                    }
                    for (String name : names) {
                        rowCount++;
                        if (taskController != null && taskController.getTask() != null
                                && taskController.getTask().isCancelled()) {
                            return invalidCount;
                        }
                        String file = path + File.separator + name;
                        query.setString(1, file);
                        query.setString(2, file);
                        try (ResultSet results = query.executeQuery()) {
                            if (!results.next()) {
                                invalidCount++;
                                if (FileDeleteTools.delete(file)) {
                                    recordInfo(taskController, message("Delete") + ": " + file);
                                }
                            }
                        } catch (Exception e) {
                            recordError(taskController, e.toString() + "\n" + file);
                        }
                    }
                    names = new File(path).list();
                    if (names == null || names.length == 0) {
                        try {
                            new File(path).delete();
                            recordInfo(taskController, message("Delete") + ": " + path);
                        } catch (Exception ex) {
                        }
                    }
                }
            } catch (Exception ex) {
                recordError(taskController, ex.toString() + "\n" + tableName);
            }
        } catch (Exception exx) {
            recordError(taskController, exx.toString() + "\n" + tableName);
        }
        recordInfo(taskController, message("Checked") + ": " + rowCount + " "
                + message("Expired") + ": " + invalidCount);
        return invalidCount;
    }

}
