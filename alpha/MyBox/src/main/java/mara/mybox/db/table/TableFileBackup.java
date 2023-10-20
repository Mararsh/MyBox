package mara.mybox.db.table;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
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
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileCopyTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.value.AppPaths;
import static mara.mybox.value.AppPaths.getBackupsPath;
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

    public static final String QueryPath
            = "SELECT * FROM File_Backup  WHERE file=? ";

    public static final String FileQuery
            = "SELECT * FROM File_Backup  WHERE file=? ORDER BY record_time DESC";

    public static final String DeleteFile
            = "DELETE FROM File_Backup  WHERE file=?";

    public static final String DeleteBackup
            = "DELETE FROM File_Backup  WHERE file=? AND backup=?";

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
            FileBackup backup = query(conn, query);
            if (backup != null) {
                File backFile = backup.getBackup();
                if (backFile != null && backFile.exists()) {
                    return backFile.getParentFile();
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return null;
    }

    public List<FileBackup> read(File file) {
        List<FileBackup> records = new ArrayList<>();
        if (file == null || !file.exists()) {
            return records;
        }
        try (Connection conn = DerbyBase.getConnection()) {
            return read(conn, file);
        } catch (Exception e) {
            MyBoxLog.debug(e, file.getAbsolutePath());
            return records;
        }
    }

    public List<FileBackup> read(Connection conn, File file) {
        List<FileBackup> records = new ArrayList<>();
        if (conn == null || file == null || !file.exists()) {
            return records;
        }
        try (PreparedStatement statement = conn.prepareStatement(FileQuery)) {
            int max = UserConfig.getInt(conn, "MaxFileBackups", FileBackup.Default_Max_Backups);
            if (max <= 0) {
                max = FileBackup.Default_Max_Backups;
                UserConfig.setInt(conn, "MaxFileBackups", FileBackup.Default_Max_Backups);
            }
            conn.setAutoCommit(true);
            statement.setString(1, file.getAbsolutePath());
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    FileBackup data = readData(results);
                    if (data == null) {
                        continue;
                    }
                    if (!data.valid() || records.size() >= max) {
                        deleteData(conn, data);
                    } else {
                        records.add(data);
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e, file.getAbsolutePath());
        }
        return records;
    }

    public FileBackup addBackup(Connection conn, File file) {
        if (conn == null || file == null) {
            return null;
        }
        try {
            File backupPath = path(conn, file);
            if (backupPath == null) {
                String fname = file.getName();
                String ext = FileNameTools.suffix(fname);
                backupPath = new File(getBackupsPath() + File.separator
                        + (ext == null || ext.isBlank() ? "x" : ext) + File.separator
                        + FileNameTools.prefix(fname) + new Date().getTime() + File.separator);
            }
            backupPath.mkdirs();
            File backupFile = new File(backupPath,
                    FileNameTools.append(file.getName(), "-" + DateTools.nowFileString()));
            while (backupFile.exists()) {
                backupFile = new File(backupPath,
                        FileNameTools.append(file.getName(), "-" + DateTools.nowFileString()));
                Thread.sleep(100);
            }
            if (!FileCopyTools.copyFile(file, backupFile, false, false)) {
                return null;
            }
            FileBackup data = new FileBackup(file, backupFile);
            data = insertData(conn, data);
            return data;
        } catch (Exception e) {
            MyBoxLog.debug(e, file.getAbsolutePath());
            return null;
        }
    }

    public int count(Connection conn, File file) {
        List<FileBackup> records = read(conn, file);
        if (records == null) {
            return -1;
        } else {
            return records.size();
        }
    }

    @Override
    public int deleteData(Connection conn, FileBackup data) {
        if (data == null) {
            return 0;
        }
        int count = super.deleteData(conn, data);
        if (count > 0) {
            FileDeleteTools.delete(data.getBackup());
        }
        return count;
    }

    public long clearBackups(SingletonTask task, String filename) {
        long count = 0;
        if (filename == null) {
            return count;
        }
        try (Connection conn = DerbyBase.getConnection()) {
            List<String> files = new ArrayList<>();
            files.add(filename);
            return clearBackups(task, conn, files);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return count;
        }
    }

    public long clearBackups(SingletonTask task, Connection conn, List<String> files) {
        long count = 0;
        if (conn == null || files == null || files.isEmpty()) {
            return count;
        }
//        taskInfo(task, FileQuery);
        try (PreparedStatement query = conn.prepareStatement(FileQuery)) {
            conn.setAutoCommit(true);
            for (String file : files) {
                if (task != null && task.isCancelled()) {
                    return count;
                }
//                taskInfo(task, message("Check") + ": " + file);
                query.setString(1, file);
                try (ResultSet results = query.executeQuery()) {
                    while (results.next()) {
                        if (task != null && task.isCancelled()) {
                            return -3;
                        }
                        FileBackup data = readData(results);
//                        taskInfo(task, message("Delete") + ": " + data.getBackup());
                        FileDeleteTools.delete(data.getBackup());
                    }
                } catch (Exception e) {
                    taskError(task, e.toString() + "\n" + tableName);
                }
            }
        } catch (Exception e) {
            taskError(task, e.toString() + "\n" + tableName);
        }
//        taskInfo(task, DeleteFile);
        if (task != null && task.isCancelled()) {
            return count;
        }
        try (PreparedStatement statement = conn.prepareStatement(DeleteFile)) {
            conn.setAutoCommit(true);
            for (String file : files) {
                if (task != null && task.isCancelled()) {
                    return count;
                }
//                taskInfo(task, message("Clear") + ": " + file);
                statement.setString(1, file);
                statement.executeUpdate();
                count++;
            }
        } catch (Exception e) {
            taskError(task, e.toString() + "\n" + tableName);
        }
        return count;
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
                for (String fbpath : fbPaths) {
                    if (taskController != null && taskController.getTask() != null
                            && taskController.getTask().isCancelled()) {
                        return invalidCount;
                    }
                    File level1path = new File(fbRootpath + File.separator + fbpath);
                    String[] level1names = level1path.list();
                    if (level1names == null || level1names.length == 0) {
                        try {
                            level1path.delete();
                            recordInfo(taskController, message("Delete") + ": " + level1path);
                        } catch (Exception ex) {
                        }
                        continue;
                    }
                    for (String level1name : level1names) {
                        rowCount++;
                        if (taskController != null && taskController.getTask() != null
                                && taskController.getTask().isCancelled()) {
                            return invalidCount;
                        }
                        File level2file = new File(level1path, level1name);
                        if (level2file.isDirectory()) {
                            String[] level2names = level2file.list();
                            if (level2names == null || level2names.length == 0) {
                                try {
                                    level2file.delete();
                                    recordInfo(taskController, message("Delete") + ": " + level2file);
                                } catch (Exception ex) {
                                }
                            } else {
                                for (String level2name : level2names) {
                                    rowCount++;
                                    if (taskController != null && taskController.getTask() != null
                                            && taskController.getTask().isCancelled()) {
                                        return invalidCount;
                                    }
                                    File level3file = new File(level2file, level2name);
                                    query.setString(1, level3file.getAbsolutePath());
                                    try (ResultSet results = query.executeQuery()) {
                                        if (!results.next()) {
                                            invalidCount++;
                                            if (FileDeleteTools.delete(level3file)) {
                                                recordInfo(taskController, message("Delete") + ": " + level3file);
                                            }
                                        }
                                    } catch (Exception e) {
                                        recordError(taskController, e.toString() + "\n" + level3file);
                                    }
                                }
                                level2names = level2file.list();
                                if (level2names == null || level2names.length == 0) {
                                    try {
                                        level2file.delete();
                                        recordInfo(taskController, message("Delete") + ": " + level2file);
                                    } catch (Exception ex) {
                                    }
                                }
                            }
                        } else {
                            query.setString(1, level2file.getAbsolutePath());
                            try (ResultSet results = query.executeQuery()) {
                                if (!results.next()) {
                                    invalidCount++;
                                    if (FileDeleteTools.delete(level2file)) {
                                        recordInfo(taskController, message("Delete") + ": " + level2file);
                                    }
                                }
                            } catch (Exception e) {
                                recordError(taskController, e.toString() + "\n" + level2file);
                            }
                        }
                    }
                    level1names = level1path.list();
                    if (level1names == null || level1names.length == 0) {
                        try {
                            level1path.delete();
                            recordInfo(taskController, message("Delete") + ": " + level1path);
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
