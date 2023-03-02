package mara.mybox.db.table;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.FileBackup;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import static mara.mybox.fxml.WindowTools.recordError;
import static mara.mybox.fxml.WindowTools.recordInfo;
import mara.mybox.tools.FileDeleteTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-2-26
 * @License Apache License Version 2.0
 */
public class TableFileBackup extends BaseTable<FileBackup> {

    public static final int Default_Max_Backups = 10;

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
        int max = UserConfig.getInt("MaxFileBackups", Default_Max_Backups);
        if (max <= 0) {
            max = Default_Max_Backups;
            UserConfig.setInt("MaxFileBackups", Default_Max_Backups);
        }
        List<FileBackup> invalid = new ArrayList<>();
        try (PreparedStatement statement = conn.prepareStatement(FileQuery)) {
            conn.setAutoCommit(true);
            statement.setString(1, filename);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    FileBackup data = readData(results);
                    File backup = data.getBackup();
                    if (backup == null || !backup.exists() || records.size() >= max) {
                        invalid.add(data);
                    } else {
                        records.add(data);
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e, filename);
        }
        deleteData(conn, invalid);
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
        recordInfo(task, FileQuery);
        try (PreparedStatement statement = conn.prepareStatement(FileQuery)) {
            conn.setAutoCommit(true);
            for (String file : files) {
                recordInfo(task, message("Check") + ": " + file);
                statement.setString(1, file);
                try (ResultSet results = statement.executeQuery()) {
                    while (results.next()) {
                        FileBackup data = readData(results);
                        recordInfo(task, message("Delete") + ": " + data.getBackup());
                        FileDeleteTools.delete(data.getBackup());
                    }
                } catch (Exception e) {
                    recordError(task, e.toString() + "\n" + tableName);
                }
            }
        } catch (Exception e) {
            recordError(task, e.toString() + "\n" + tableName);
        }
        recordInfo(task, DeleteFile);
        try (PreparedStatement statement = conn.prepareStatement(DeleteFile)) {
            for (String file : files) {
                recordInfo(task, message("Clear") + ": " + file);
                statement.setString(1, file);
                statement.executeUpdate();
            }
        } catch (Exception e) {
            recordError(task, e.toString() + "\n" + tableName);
        }
    }

    public int clearInvalid(SingletonTask task, Connection conn) {
        int count = 0;
        try {
            recordInfo(task, message("Check") + ": " + tableName);
            conn.setAutoCommit(true);
            List<FileBackup> invalid = new ArrayList<>();
            List<String> clear = new ArrayList<>();
            try (PreparedStatement query = conn.prepareStatement(queryAllStatement());
                    ResultSet results = query.executeQuery()) {
                while (results.next()) {
                    FileBackup data = readData(results);
                    File file = data.getFile();
                    if (file == null) {
                        invalid.add(data);
                    } else if (!file.exists()) {
                        clear.add(file.getAbsolutePath());
                        recordInfo(task, message("NotFound") + ": " + file.getAbsolutePath());
                    } else {
                        File backup = data.getBackup();
                        if (backup == null || !backup.exists()) {
                            invalid.add(data);
                            if (backup != null) {
                                recordInfo(task, message("NotFound") + ": " + backup.getAbsolutePath());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                recordError(task, e.toString() + "\n" + tableName);
            }
            count = clear.size() + invalid.size();
            if (count > 0) {
                recordInfo(task, message("Invalid") + ": " + clear.size() + " + " + invalid.size());
                clearBackups(task, conn, clear);
                deleteData(conn, invalid);
            }
            conn.setAutoCommit(true);
        } catch (Exception e) {
            recordError(task, e.toString() + "\n" + tableName);
        }
        return count;
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
        FileDeleteTools.delete(new File(backup));
    }

}
