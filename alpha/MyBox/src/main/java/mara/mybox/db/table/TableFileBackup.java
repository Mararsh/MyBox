package mara.mybox.db.table;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.controller.BaseTaskController;
import mara.mybox.db.Database;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.FileBackup;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
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
 * @CreateDate 2021-2-26
 * @License Apache License Version 2.0
 */
public class TableFileBackup extends BaseTable<FileBackup> {

    public static final String BackupQuery
            = "SELECT * FROM File_Backup WHERE backup=?";

    public TableFileBackup() {
        tableName = "File_Backup";
        defineColumns();
    }

    public TableFileBackup(boolean defineColumns) {
        tableName = "File_Backup";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableFileBackup defineColumns() {
        addColumn(new ColumnDefinition("fbid", ColumnType.Long, true, true).setAuto(true));
        addColumn(new ColumnDefinition("file", ColumnType.String, true).setLength(FilenameMaxLength));
        addColumn(new ColumnDefinition("backup", ColumnType.String, true).setLength(FilenameMaxLength));
        addColumn(new ColumnDefinition("record_time", ColumnType.Datetime, true));
        return this;
    }

    public static final String Create_Index
            = "CREATE INDEX File_Backup_index on File_Backup (  file, backup, record_time )";

    public static final String FileQuery
            = "SELECT * FROM File_Backup  WHERE file=? ORDER BY record_time DESC";

    public static final String DeleteFile
            = "DELETE FROM File_Backup  WHERE file=?";

    public static final String DeleteBackup
            = "DELETE FROM File_Backup  WHERE file=? AND backup=?";

    public List<FileBackup> read(String file) {
        List<FileBackup> dataList = new ArrayList<>();
        if (file == null || file.isBlank()) {
            return dataList;
        }
        try (Connection conn = DerbyBase.getConnection()) {
            return read(conn, file);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return dataList;
    }

    public List<FileBackup> read(Connection conn, String filename) {
        List<FileBackup> records = new ArrayList<>();
        if (filename == null || filename.isBlank()) {
            return records;
        }
        int max = UserConfig.getInt(conn, "MaxFileBackups", FileBackup.Default_Max_Backups);
        if (max <= 0) {
            max = FileBackup.Default_Max_Backups;
            UserConfig.setInt(conn, "MaxFileBackups", FileBackup.Default_Max_Backups);
        }
        try (PreparedStatement statement = conn.prepareStatement(FileQuery)) {
            conn.setAutoCommit(true);
            statement.setString(1, filename);
            try (ResultSet results = statement.executeQuery();
                    PreparedStatement delete = conn.prepareStatement(deleteStatement())) {
                while (results.next()) {
                    FileBackup data = readData(results);
                    File backup = data.getBackup();
                    if (backup == null || !backup.exists() || records.size() >= max) {
                        if (setDeleteStatement(conn, delete, data)) {
                            delete.executeUpdate();
                        }
                    } else {
                        records.add(data);
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e, filename);
        }
        return records;
    }

    public void clearBackups(SingletonTask task, String filename) {
        if (filename == null) {
            return;
        }
        try (Connection conn = DerbyBase.getConnection()) {
            List<String> files = new ArrayList<>();
            files.add(filename);
            clearBackups(task, conn, files);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void clearBackups(SingletonTask task, Connection conn, List<String> files) {
        if (conn == null || files == null || files.isEmpty()) {
            return;
        }
        taskInfo(task, FileQuery);
        try (PreparedStatement statement = conn.prepareStatement(FileQuery)) {
            conn.setAutoCommit(true);
            for (String file : files) {
                if (task != null && task.isCancelled()) {
                    return;
                }
                taskInfo(task, message("Check") + ": " + file);
                statement.setString(1, file);
                try (ResultSet results = statement.executeQuery()) {
                    while (results.next()) {
                        if (task != null && task.isCancelled()) {
                            return;
                        }
                        FileBackup data = readData(results);
                        taskInfo(task, message("Delete") + ": " + data.getBackup());
                        FileDeleteTools.delete(data.getBackup());
                    }
                } catch (Exception e) {
                    taskError(task, e.toString() + "\n" + tableName);
                }
            }
        } catch (Exception e) {
            taskError(task, e.toString() + "\n" + tableName);
        }
        taskInfo(task, DeleteFile);
        if (task != null && task.isCancelled()) {
            return;
        }
        try (PreparedStatement statement = conn.prepareStatement(DeleteFile)) {
            for (String file : files) {
                if (task != null && task.isCancelled()) {
                    return;
                }
                taskInfo(task, message("Clear") + ": " + file);
                statement.setString(1, file);
                statement.executeUpdate();
            }
        } catch (Exception e) {
            taskError(task, e.toString() + "\n" + tableName);
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
            try (PreparedStatement query = conn.prepareStatement(queryAllStatement());
                    PreparedStatement delete = conn.prepareStatement(deleteStatement())) {
                conn.setAutoCommit(true);
                try (ResultSet results = query.executeQuery()) {
                    conn.setAutoCommit(false);
                    while (results.next()) {
                        rowCount++;
                        if (taskController != null && taskController.getTask() != null
                                && taskController.getTask().isCancelled()) {
                            return invalidCount;
                        }
                        FileBackup data = readData(results);
                        File file = data.getFile();
                        File backup = data.getBackup();
                        if (file == null || !file.exists()
                                || backup == null || !backup.exists()) {
                            if (backup != null && FileDeleteTools.delete(backup)) {
                                recordInfo(taskController, message("Delete") + ": " + backup);
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
            String fbRootpath = AppPaths.getBackupsPath();
            recordInfo(taskController, message("Check") + ": " + fbRootpath);
            String[] fbPaths = new File(fbRootpath).list();
            if (fbPaths == null || fbPaths.length == 0) {
                return invalidCount;
            }
            try (PreparedStatement query = conn.prepareStatement(BackupQuery)) {
                conn.setAutoCommit(true);
                for (String pathname : fbPaths) {
                    if (taskController != null && taskController.getTask() != null
                            && taskController.getTask().isCancelled()) {
                        return invalidCount;
                    }
                    String path = fbRootpath + File.separator + pathname;
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

    /*
        static
     */
    public static void deleteBackup(FileBackup record) {
        if (record == null) {
            return;
        }
        try (Connection conn = DerbyBase.getConnection()) {
            deleteBackup(conn, record);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public static void deleteBackup(Connection conn, FileBackup record) {
        if (conn == null || record == null || record.getFile() == null || record.getBackup() == null) {
            return;
        }
        deleteBackup(conn, record.getFile().getAbsolutePath(), record.getBackup().getAbsolutePath());
    }

    public static void deleteBackup(Connection conn, String filename, String backup) {
        if (conn == null || filename == null || backup == null) {
            return;
        }
        try (PreparedStatement statement = conn.prepareStatement(DeleteBackup)) {
            conn.setAutoCommit(true);
            statement.setString(1, filename);
            statement.setString(2, backup);
            statement.executeUpdate();
        } catch (Exception e) {
            MyBoxLog.error(e + "  " + filename + "  " + backup);
        }
        FileDeleteTools.delete(backup);
    }

}
