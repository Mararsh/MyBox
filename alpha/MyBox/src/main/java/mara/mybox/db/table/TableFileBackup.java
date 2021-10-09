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
import mara.mybox.tools.FileDeleteTools;
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
        addColumn(new ColumnDefinition("fbid", ColumnType.Long, true, true).setIsID(true));
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
        if (file == null || file.trim().isBlank()) {
            return dataList;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
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
        try ( PreparedStatement statement = conn.prepareStatement(FileQuery)) {
            statement.setString(1, filename);
            try ( ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    FileBackup data = (FileBackup) readData(results);
                    records.add(data);
                }
            }
            List<FileBackup> valid = new ArrayList<>();
            for (int i = 0; i < records.size(); ++i) {
                FileBackup record = records.get(i);
                File file = record.getFile();
                if (file == null || !file.exists()) {
                    clearBackups(conn, filename);
                    continue;
                }
                File backup = record.getBackup();
                if (backup == null || !backup.exists()) {
                    deleteBackup(conn, record);
                    continue;
                }
                valid.add(records.get(i));
            }
            if (valid.size() > max) {
                for (int i = max; i < valid.size(); ++i) {
                    deleteBackup(conn, valid.get(i));
                }
                records = valid.subList(0, max);
            } else {
                records = valid;
            }
        } catch (Exception e) {
            MyBoxLog.error(e, filename);
        }
        return records;
    }

    public void clearBackups(String filename) {
        if (filename == null) {
            return;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            clearBackups(conn, filename);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void clearBackups(Connection conn, String filename) {
        if (conn == null || filename == null) {
            return;
        }
        try ( PreparedStatement statement = conn.prepareStatement(FileQuery)) {
            statement.setString(1, filename);
            try ( ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    FileBackup data = (FileBackup) readData(results);
                    FileDeleteTools.delete(data.getBackup());
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        try ( PreparedStatement statement = conn.prepareStatement(DeleteFile)) {
            statement.setString(1, filename);
            statement.executeUpdate();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public int clearInvalid(Connection conn) {
        int count = 0;
        try {
            conn.setAutoCommit(true);
            List<FileBackup> invalid = new ArrayList<>();
            try ( PreparedStatement query = conn.prepareStatement(queryAllStatement());
                     ResultSet results = query.executeQuery()) {
                while (results.next()) {
                    FileBackup data = readData(results);
                    if (data.getBackup() == null || !data.getBackup().exists()
                            || data.getFile() == null || !data.getFile().exists()) {
                        invalid.add(data);
                    }
                }

            } catch (Exception e) {
                MyBoxLog.debug(e, tableName);
            }
            count = invalid.size();
            deleteData(conn, invalid);
            conn.setAutoCommit(true);
        } catch (Exception e) {
            MyBoxLog.error(e, tableName);
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
        try ( Connection conn = DerbyBase.getConnection()) {
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
        try ( PreparedStatement statement = conn.prepareStatement(DeleteBackup)) {
            statement.setString(1, filename);
            statement.setString(2, backup);
            statement.executeUpdate();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        FileDeleteTools.delete(new File(backup));
    }

}
