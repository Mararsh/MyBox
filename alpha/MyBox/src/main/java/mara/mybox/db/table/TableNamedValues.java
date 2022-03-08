package mara.mybox.db.table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.NamedValues;
import static mara.mybox.db.table.BaseTable.StringMaxLength;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-3-8
 * @License Apache License Version 2.0
 */
public class TableNamedValues extends BaseTable<NamedValues> {

    public TableNamedValues() {
        tableName = "Named_Values";
        defineColumns();
    }

    public TableNamedValues(boolean defineColumns) {
        tableName = "Named_Values";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableNamedValues defineColumns() {
        addColumn(new ColumnDefinition("key_name", ColumnDefinition.ColumnType.String, true, true).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("value", ColumnDefinition.ColumnType.String, true, true).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("value_name", ColumnDefinition.ColumnType.String).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("update_time", ColumnDefinition.ColumnType.Datetime, true));
        orderColumns = "update_time DESC";
        return this;
    }

    public static final String QueryKey
            = "SELECT * FROM Named_Values WHERE key_name=?";

    public List<NamedValues> read(String key) {
        if (key == null || key.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            conn.setReadOnly(true);
            return read(conn, key);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return new ArrayList<>();
        }
    }

    public List<NamedValues> read(Connection conn, String key) {
        if (conn == null || key == null || key.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try ( PreparedStatement statement = conn.prepareStatement(QueryKey)) {
            statement.setString(1, key);
            return query(statement);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return new ArrayList<>();
        }
    }

}
