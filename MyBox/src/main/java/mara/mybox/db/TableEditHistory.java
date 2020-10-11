package mara.mybox.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.data.EditHistory;
import mara.mybox.db.ColumnDefinition.ColumnType;
import static mara.mybox.db.DerbyBase.dbHome;
import static mara.mybox.db.DerbyBase.failed;
import static mara.mybox.db.DerbyBase.login;
import static mara.mybox.db.DerbyBase.protocol;
import static mara.mybox.value.AppVariables.logger;

/**
 * @Author Mara
 * @CreateDate 2020-9-7
 * @License Apache License Version 2.0
 */
public class TableEditHistory extends TableBase<EditHistory> {

    public TableEditHistory() {
        tableName = "Edit_History";
        defineColumns();
    }

    public TableEditHistory(boolean defineColumns) {
        tableName = "Edit_History";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableEditHistory defineColumns() {
        addColumn(new ColumnDefinition("ehid", ColumnType.Long, true, true).setIsID(true));
        addColumn(new ColumnDefinition("file", ColumnType.File, true));
        addColumn(new ColumnDefinition("history", ColumnType.File, true));
        addColumn(new ColumnDefinition("create_time", ColumnType.Datetime, true));
        return this;
    }

    public static final String Create_Index_unique
            = "CREATE UNIQUE INDEX Edit_History_unique_index on Edit_History (  file, history, create_time )";

    public static final String AllQuery
            = " SELECT * FROM Edit_History  ORDER BY create_time  DESC  ";

    public static final String FileQuery
            = " SELECT * FROM Edit_History  WHERE file=? ORDER BY create_time DESC  ";

    @Override
    public EditHistory newData() {
        return new EditHistory();
    }

    @Override
    public boolean setValue(EditHistory data, String column, Object value) {
        if (data == null) {
            return false;
        }
        return data.setValue(column, value);
    }

    @Override
    public Object getValue(EditHistory data, String column) {
        if (data == null) {
            return null;
        }
        return data.getValue(column);
    }

    @Override
    public void setId(EditHistory source, EditHistory target) {
        try {
            if (source == null || target == null) {
                return;
            }
            target.setEhid(source.getEhid());
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @Override
    public boolean valid(EditHistory data) {
        if (data == null) {
            return false;
        }
        return data.valid();
    }

    public List<EditHistory> histories(String file) {
        List<EditHistory> dataList = new ArrayList<>();
        if (file == null || file.trim().isBlank()) {
            return dataList;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setReadOnly(true);
            return histories(conn, file);
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
        }
        return dataList;
    }

    public List<EditHistory> histories(Connection conn, String file) {
        List<EditHistory> dataList = new ArrayList<>();
        if (conn == null || file == null || file.trim().isBlank()) {
            return dataList;
        }
        try ( PreparedStatement statement = conn.prepareStatement(FileQuery)) {
            statement.setString(1, file);
            try ( ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    EditHistory data = (EditHistory) readData(results);
                    dataList.add(data);
                }
            }
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
        }
        return dataList;
    }

}
