package mara.mybox.db.table;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.data.FileHistory;
import static mara.mybox.db.DerbyBase.dbHome;
import static mara.mybox.db.DerbyBase.login;
import static mara.mybox.db.DerbyBase.protocol;
import mara.mybox.db.table.ColumnDefinition.ColumnType;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2020-9-7
 * @License Apache License Version 2.0
 */
public class TableFileHistory extends BaseTable<FileHistory> {

    public TableFileHistory() {
        tableName = "File_History";
        defineColumns();
    }

    public TableFileHistory(boolean defineColumns) {
        tableName = "File_History";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableFileHistory defineColumns() {
        addColumn(new ColumnDefinition("fhid", ColumnType.Long, true, true).setIsID(true));
        addColumn(new ColumnDefinition("category", ColumnType.String, true).setLength(128));
        addColumn(new ColumnDefinition("file", ColumnType.File, true));
        addColumn(new ColumnDefinition("history", ColumnType.File, true));
        addColumn(new ColumnDefinition("record_time", ColumnType.Datetime, true));
        return this;
    }

    public static final String Create_Index_unique
            = "CREATE UNIQUE INDEX File_History_unique_index on Edit_History (  category, file, history, record_time )";

    public static final String AllQuery
            = " SELECT * FROM File_History  ORDER BY record_time  DESC  ";

    public static final String FileQuery
            = " SELECT * FROM File_History  WHERE category=? AND file=? ORDER BY record_time DESC  ";

    public List<FileHistory> histories(String category, String file) {
        List<FileHistory> dataList = new ArrayList<>();
        if (file == null || file.trim().isBlank()) {
            return dataList;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setReadOnly(true);
            return histories(conn, category, file);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return dataList;
    }

    public List<FileHistory> histories(Connection conn, String category, String file) {
        List<FileHistory> dataList = new ArrayList<>();
        if (conn == null || file == null || file.trim().isBlank()) {
            return dataList;
        }
        try ( PreparedStatement statement = conn.prepareStatement(FileQuery)) {
            statement.setString(1, category);
            statement.setString(2, file);
            try ( ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    FileHistory data = (FileHistory) readData(results);
                    dataList.add(data);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return dataList;
    }

}
