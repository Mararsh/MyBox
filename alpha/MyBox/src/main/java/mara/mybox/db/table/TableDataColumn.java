package mara.mybox.db.table;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import mara.mybox.db.DerbyBase;
import static mara.mybox.db.DerbyBase.login;
import static mara.mybox.db.DerbyBase.protocol;
import mara.mybox.db.data.DataDefinition;
import mara.mybox.db.data.DataDefinition.DataType;
import mara.mybox.db.table.ColumnDefinition.ColumnType;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-1-23
 * @License Apache License Version 2.0
 */
public class TableDataColumn extends BaseTable<ColumnDefinition> {

    protected TableDataDefinition tableDataDefinition;

    public TableDataColumn() {
        tableName = "Data_Column";
        defineColumns();
    }

    public TableDataColumn(boolean defineColumns) {
        tableName = "Data_Column";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableDataColumn defineColumns() {
        addColumn(new ColumnDefinition("dcid", ColumnType.Long, true, true).setIsID(true));
        addColumn(new ColumnDefinition("dataid", ColumnType.Long, true)
                .setForeignName("Data_Column_dataid_fk").setForeignTable("Data_Definition").setForeignColumn("dfid"));
        addColumn(new ColumnDefinition("column_type", ColumnType.Short, true));
        addColumn(new ColumnDefinition("column_name", ColumnType.String, true).setLength(1024));
        addColumn(new ColumnDefinition("index", ColumnType.Integer));
        addColumn(new ColumnDefinition("length", ColumnType.Integer));
        addColumn(new ColumnDefinition("width", ColumnType.Integer));
        addColumn(new ColumnDefinition("is_primary", ColumnType.Boolean));
        addColumn(new ColumnDefinition("not_null", ColumnType.Boolean));
        addColumn(new ColumnDefinition("is_id", ColumnType.Boolean));
        addColumn(new ColumnDefinition("editable", ColumnType.Boolean));
        addColumn(new ColumnDefinition("on_delete", ColumnType.Short));
        addColumn(new ColumnDefinition("on_update", ColumnType.Short));
        addColumn(new ColumnDefinition("default_value", ColumnType.String).setLength(4096));
        addColumn(new ColumnDefinition("max_value", ColumnType.String).setLength(128));
        addColumn(new ColumnDefinition("min_value", ColumnType.String).setLength(128));
        addColumn(new ColumnDefinition("time_format", ColumnType.Short));
        addColumn(new ColumnDefinition("label", ColumnType.String).setLength(1024));
        addColumn(new ColumnDefinition("foreign_name", ColumnType.String).setLength(1024));
        addColumn(new ColumnDefinition("foreign_table", ColumnType.String).setLength(1024));
        addColumn(new ColumnDefinition("foreign_column", ColumnType.String).setLength(1024));
        addColumn(new ColumnDefinition("values_list", ColumnType.Text).setLength(32672));
        return this;
    }

    public static final String Create_Index_unique
            = "CREATE UNIQUE INDEX Data_Column_unique_index on Data_Column (dataid, column_name)";

    /*
        View
     */
    public static final String CreateView
            = " CREATE VIEW Data_Column_View AS "
            + " SELECT Data_Column.*, Data_Definition.* "
            + " FROM Data_Column JOIN Data_Definition ON Data_Column.dataid=Data_Definition.dfid";

    public static final String ClearData
            = "DELETE FROM Data_Column WHERE dataid=?";

    public static final String QeuryData
            = "SELECT * FROM Data_Column WHERE dataid=? ORDER BY index";

    /*
        local methods
     */
    @Override
    public Object readForeignValue(ResultSet results, String column) {
        if (results == null || column == null) {
            return null;
        }
        try {
            if ("dataid".equals(column) && results.findColumn("dfid") > 0) {
                return getTableDataDefinition().readData(results);
            }
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public boolean setForeignValue(ColumnDefinition data, String column, Object value) {
        if (data == null || column == null || value == null) {
            return true;
        }
        if ("dataid".equals(column) && value instanceof DataDefinition) {
            data.setDataDefinition((DataDefinition) value);
        }
        return true;
    }

    public List<ColumnDefinition> read(long dataid) {
        if (dataid < 0) {
            return null;
        }
        try ( Connection conn = DerbyBase.getConnection();) {
            return read(conn, dataid);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<ColumnDefinition> read(Connection conn, long dataid) {
        if (conn == null || dataid < 0) {
            return null;
        }
        try {
            String sql = "SELECT * FROM Data_Column WHERE dataid=" + dataid + " ORDER BY index";
            return query(conn, sql);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<ColumnDefinition> read(Connection conn, DataType dataType, String dataName) {
        if (dataType == null || dataName == null) {
            return null;
        }
        try {
            DataDefinition dataDefinition = getTableDataDefinition().read(conn, dataType, dataName);
            if (dataDefinition == null) {
                return null;
            }
            return read(conn, dataDefinition.getDfid());
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public boolean clear(DataType type, String dataName) {
        if (type == null || dataName == null) {
            return false;
        }
        try ( Connection conn = DerbyBase.getConnection();) {
            return clear(conn, type, dataName);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean clear(Connection conn, DataType type, String dataName) {
        if (conn == null || type == null || dataName == null) {
            return false;
        }
        DataDefinition dataDefinition = getTableDataDefinition().read(type, dataName);
        if (dataDefinition == null) {
            return false;
        }
        return clear(conn, dataDefinition.getDfid());
    }

    public boolean clear(Connection conn, long dataid) {
        if (conn == null || dataid < 0) {
            return false;
        }
        try ( PreparedStatement statement = conn.prepareStatement(ClearData)) {
            statement.setLong(1, dataid);
            statement.executeUpdate();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean save(long dataid, List<ColumnDefinition> columns) {
        if (dataid < 0 || columns.isEmpty()) {
            return false;
        }
        try ( Connection conn = DerbyBase.getConnection();) {
            save(conn, dataid, columns);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
        return true;
    }

    public boolean save(Connection conn, long dataid, List<ColumnDefinition> columns) {
        if (dataid < 0 || columns == null || columns.isEmpty()) {
            return false;
        }
        try {
            clear(conn, dataid);
            conn.commit();
            int index = 0;
            for (ColumnDefinition column : columns) {
                column.setDataid(dataid);
                column.setIndex(index++);
            }
            conn.setAutoCommit(false);
            insertList(conn, columns);
            conn.commit();
            conn.setAutoCommit(true);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean save(DataType type, String dataName, List<ColumnDefinition> columns) {
        if (type == null || dataName == null || columns == null || columns.isEmpty()) {
            return false;
        }
        try ( Connection conn = DerbyBase.getConnection();) {
            return save(conn, type, dataName, columns);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean save(Connection conn, DataType type, String dataName, List<ColumnDefinition> columns) {
        if (type == null || dataName == null || columns == null || columns.isEmpty()) {
            return false;
        }
        try {
            DataDefinition dataDefinition = getTableDataDefinition().read(type, dataName);
            if (dataDefinition == null) {
                dataDefinition = DataDefinition.create().setDataType(type).setDataName(dataName);
                tableDataDefinition.insertData(conn, dataDefinition);
            }
            long dataid = dataDefinition.getDfid();
            clear(conn, dataid);
            conn.commit();
            int index = 0;
            for (ColumnDefinition column : columns) {
                column.setDataid(dataid);
                column.setIndex(index++);
            }
            conn.setAutoCommit(false);
            insertList(conn, columns);
            conn.commit();
            conn.setAutoCommit(true);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    /*
        get/set
     */
    public TableDataDefinition getTableDataDefinition() {
        if (tableDataDefinition == null) {
            tableDataDefinition = new TableDataDefinition();
        }
        return tableDataDefinition;
    }

    public void setTableDataDefinition(TableDataDefinition tableDataDefinition) {
        this.tableDataDefinition = tableDataDefinition;
    }

}
