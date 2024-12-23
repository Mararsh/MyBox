package mara.mybox.db.table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.PathConnection;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2023-3-17
 * @License Apache License Version 2.0
 */
public class TablePathConnection extends BaseTable<PathConnection> {

    public TablePathConnection() {
        tableName = "Path_Connection";
        defineColumns();
    }

    public TablePathConnection(boolean defineColumns) {
        tableName = "Path_Connection";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TablePathConnection defineColumns() {
        addColumn(new ColumnDefinition("pcnid", ColumnType.Long, true, true).setAuto(true));
        addColumn(new ColumnDefinition("type", ColumnType.String, true));
        addColumn(new ColumnDefinition("title", ColumnType.String));
        addColumn(new ColumnDefinition("host", ColumnType.String));
        addColumn(new ColumnDefinition("username", ColumnType.String));
        addColumn(new ColumnDefinition("password", ColumnType.String));
        addColumn(new ColumnDefinition("path", ColumnType.String));
        addColumn(new ColumnDefinition("rootpath", ColumnType.String));
        addColumn(new ColumnDefinition("port", ColumnType.Integer));
        addColumn(new ColumnDefinition("timeout", ColumnType.Integer));
        addColumn(new ColumnDefinition("retry", ColumnType.Integer));
        addColumn(new ColumnDefinition("host_key_check", ColumnType.Boolean));
        addColumn(new ColumnDefinition("modify_time", ColumnType.Datetime));
        addColumn(new ColumnDefinition("comments", ColumnType.String).setLength(StringMaxLength));
        orderColumns = "modify_time DESC";
        return this;
    }

    public static final String Query_Type
            = "SELECT * FROM Path_Connection WHERE type=?";

    public static final String Clear_Type
            = "DELETE * FROM Path_Connection WHERE type=?";

    @Override
    public boolean setValue(PathConnection data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        return data.setValue(column, value);
    }

    @Override
    public Object getValue(PathConnection data, String column) {
        if (data == null || column == null) {
            return null;
        }
        return data.getValue(column);
    }

    @Override
    public boolean valid(PathConnection data) {
        if (data == null) {
            return false;
        }
        return data.valid();
    }

    /*
        local methods
     */
    public List<PathConnection> read(PathConnection.Type type, int max) {
        if (type == null) {
            return null;
        }
        try (Connection conn = DerbyBase.getConnection();) {
            return read(conn, type, max);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<PathConnection> read(Connection conn, PathConnection.Type type, int max) {
        if (conn == null || type == null) {
            return null;
        }
        try (PreparedStatement statement = conn.prepareStatement(Query_Type)) {
            if (max > 0) {
                statement.setMaxRows(max);
            }
            statement.setString(1, type.name());
            return query(statement);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public int clear(PathConnection.Type type) {
        if (type == null) {
            return -1;
        }
        try (Connection conn = DerbyBase.getConnection(); PreparedStatement statement = conn.prepareStatement(Clear_Type)) {
            statement.setString(1, type.name());
            return statement.executeUpdate();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return -2;
        }
    }

}
