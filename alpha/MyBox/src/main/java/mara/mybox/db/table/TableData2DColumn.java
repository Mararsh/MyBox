package mara.mybox.db.table;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.Data2Column;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-1-23
 * @License Apache License Version 2.0
 */
public class TableData2DColumn extends BaseTable<Data2Column> {

    protected TableData2DDefinition tableData2DDefinition;

    public TableData2DColumn() {
        tableName = "Data2D_Column";
        defineColumns();
    }

    public TableData2DColumn(boolean defineColumns) {
        tableName = "Data2D_Column";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableData2DColumn defineColumns() {
        addColumn(new Data2Column("d2cid", ColumnType.Long, true, true).setIsID(true));
        addColumn(new Data2Column("d2id", ColumnType.Long, true)
                .setForeignName("Data2D_Column_d2id_fk").setForeignTable("Data2D_Definition").setForeignColumn("d2did")
                .setOnDelete(Data2Column.OnDelete.Cascade));
        addColumn(new Data2Column("column_type", ColumnType.Short, true));
        addColumn(new Data2Column("column_name", ColumnType.String, true).setLength(1024));
        addColumn(new Data2Column("index", ColumnType.Integer));
        addColumn(new Data2Column("length", ColumnType.Integer));
        addColumn(new Data2Column("width", ColumnType.Integer));
        addColumn(new Data2Column("is_primary", ColumnType.Boolean));
        addColumn(new Data2Column("not_null", ColumnType.Boolean));
        addColumn(new Data2Column("is_id", ColumnType.Boolean));
        addColumn(new Data2Column("editable", ColumnType.Boolean));
        addColumn(new Data2Column("on_delete", ColumnType.Short));
        addColumn(new Data2Column("on_update", ColumnType.Short));
        addColumn(new Data2Column("default_value", ColumnType.String).setLength(4096));
        addColumn(new Data2Column("max_value", ColumnType.String).setLength(128));
        addColumn(new Data2Column("min_value", ColumnType.String).setLength(128));
        addColumn(new Data2Column("time_format", ColumnType.Short));
        addColumn(new Data2Column("label", ColumnType.String).setLength(1024));
        addColumn(new Data2Column("foreign_name", ColumnType.String).setLength(1024));
        addColumn(new Data2Column("foreign_table", ColumnType.String).setLength(1024));
        addColumn(new Data2Column("foreign_column", ColumnType.String).setLength(1024));
        addColumn(new Data2Column("values_list", ColumnType.Text).setLength(32672));
        return this;
    }

    public static final String Create_Index_unique
            = "CREATE UNIQUE INDEX Data2D_Column_unique_index on Data2D_Column (d2id, column_name)";

    /*
        View
     */
    public static final String CreateView
            = " CREATE VIEW Data2D_Column_View AS "
            + " SELECT Data2D_Column.*, Data2D_Definition.* "
            + " FROM Data2D_Column JOIN Data2D_Definition ON Data2D_Column.d2id=Data2D_Definition.d2did";

    public static final String ClearData
            = "DELETE FROM Data2D_Column WHERE d2id=?";

    public static final String QeuryData
            = "SELECT * FROM Data2D_Column WHERE d2id=? ORDER BY index";

    /*
        local methods
     */
    @Override
    public Object readForeignValue(ResultSet results, String column) {
        if (results == null || column == null) {
            return null;
        }
        try {
            if ("d2id".equals(column) && results.findColumn("d2did") > 0) {
                return getTableData2DDefinition().readData(results);
            }
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public boolean setForeignValue(Data2Column data, String column, Object value) {
        if (data == null || column == null || value == null) {
            return true;
        }
        if ("d2id".equals(column) && value instanceof Data2DDefinition) {
            data.setData2DDefinition((Data2DDefinition) value);
        }
        return true;
    }

    public List<Data2Column> read(long d2id) {
        if (d2id < 0) {
            return null;
        }
        try ( Connection conn = DerbyBase.getConnection();) {
            return read(conn, d2id);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<Data2Column> read(Connection conn, long d2id) {
        if (conn == null || d2id < 0) {
            return null;
        }
        try {
            String sql = "SELECT * FROM Data2D_Column WHERE d2id=" + d2id + " ORDER BY index";
            return query(conn, sql);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<Data2Column> queryFile(Connection conn, Data2DDefinition.Type type, File file) {
        if (file == null) {
            return null;
        }
        try {
            Data2DDefinition dataDefinition = getTableData2DDefinition().queryFile(conn, type, file);
            if (dataDefinition == null) {
                return null;
            }
            return read(conn, dataDefinition.getD2did());
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public boolean clearFile(Data2DDefinition.Type type, File file) {
        if (file == null) {
            return false;
        }
        try ( Connection conn = DerbyBase.getConnection();) {
            return clearFile(conn, type, file);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean clearFile(Connection conn, Data2DDefinition.Type type, File file) {
        if (conn == null || file == null) {
            return false;
        }
        Data2DDefinition dataDefinition = getTableData2DDefinition().queryFile(conn, type, file);
        if (dataDefinition == null) {
            return false;
        }
        return clear(conn, dataDefinition.getD2did());
    }

    public boolean clear(Connection conn, long d2id) {
        if (conn == null || d2id < 0) {
            return false;
        }
        try ( PreparedStatement statement = conn.prepareStatement(ClearData)) {
            statement.setLong(1, d2id);
            statement.executeUpdate();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean save(long d2id, List<Data2Column> columns) {
        if (d2id < 0 || columns.isEmpty()) {
            return false;
        }
        try ( Connection conn = DerbyBase.getConnection();) {
            save(conn, d2id, columns);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
        return true;
    }

    public boolean save(Connection conn, long d2id, List<Data2Column> columns) {
        if (d2id < 0 || columns == null || columns.isEmpty()) {
            return false;
        }
        try {
            clear(conn, d2id);
            conn.commit();
            int index = 0;
            for (Data2Column column : columns) {
                column.setD2id(d2id);
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

    public boolean save(Data2DDefinition.Type type, File file, String dataName, List<Data2Column> columns) {
        if (file == null || dataName == null || columns == null || columns.isEmpty()) {
            return false;
        }
        try ( Connection conn = DerbyBase.getConnection();) {
            return save(conn, type, file, dataName, columns);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean save(Connection conn, Data2DDefinition.Type type, File file, String dataName, List<Data2Column> columns) {
        if (file == null || dataName == null || columns == null || columns.isEmpty()) {
            return false;
        }
        try {
            Data2DDefinition dataDefinition = getTableData2DDefinition().queryFileName(conn, type, file, dataName);
            if (dataDefinition == null) {
                dataDefinition = Data2DDefinition.create().setType(type)
                        .setFile(file).setDataName(dataName);
                tableData2DDefinition.insertData(conn, dataDefinition);
            }
            long d2id = dataDefinition.getD2did();
            clear(conn, d2id);
            conn.commit();
            int index = 0;
            for (Data2Column column : columns) {
                column.setD2id(d2id);
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
    public TableData2DDefinition getTableData2DDefinition() {
        if (tableData2DDefinition == null) {
            tableData2DDefinition = new TableData2DDefinition();
        }
        return tableData2DDefinition;
    }

    public void setTableData2DDefinition(TableData2DDefinition tableData2DDefinition) {
        this.tableData2DDefinition = tableData2DDefinition;
    }

}
