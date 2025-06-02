package mara.mybox.db.table;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import mara.mybox.data2d.Data2D;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import static mara.mybox.db.table.BaseTable.StringMaxLength;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-1-23
 * @License Apache License Version 2.0
 */
public class TableData2DColumn extends BaseTable<Data2DColumn> {

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
        addColumn(new Data2DColumn("d2cid", ColumnType.Long, true, true).setAuto(true));
        addColumn(new Data2DColumn("d2id", ColumnType.Long, true)
                .setReferName("Data2D_Column_d2id_fk").setReferTable("Data2D_Definition").setReferColumn("d2did")
                .setOnDelete(Data2DColumn.OnDelete.Cascade));
        addColumn(new Data2DColumn("column_type", ColumnType.Short, true));
        addColumn(new Data2DColumn("column_name", ColumnType.String, true).setLength(StringMaxLength));
        addColumn(new Data2DColumn("label", ColumnType.String).setLength(StringMaxLength));
        addColumn(new Data2DColumn("index", ColumnType.Integer));
        addColumn(new Data2DColumn("length", ColumnType.Integer));
        addColumn(new Data2DColumn("width", ColumnType.Integer));
        addColumn(new Data2DColumn("scale", ColumnType.Integer));
        addColumn(new Data2DColumn("color", ColumnType.Color).setLength(16));
        addColumn(new Data2DColumn("is_primary", ColumnType.Boolean));
        addColumn(new Data2DColumn("is_auto", ColumnType.Boolean));
        addColumn(new Data2DColumn("not_null", ColumnType.Boolean));
        addColumn(new Data2DColumn("editable", ColumnType.Boolean));
        addColumn(new Data2DColumn("on_delete", ColumnType.Short));
        addColumn(new Data2DColumn("on_update", ColumnType.Short));
        addColumn(new Data2DColumn("fix_year", ColumnType.Boolean));
        addColumn(new Data2DColumn("century", ColumnType.Integer));
        addColumn(new Data2DColumn("format", ColumnType.String).setLength(StringMaxLength));
        addColumn(new Data2DColumn("default_value", ColumnType.String).setLength(StringMaxLength));
        addColumn(new Data2DColumn("max_value", ColumnType.String).setLength(StringMaxLength));
        addColumn(new Data2DColumn("min_value", ColumnType.String).setLength(StringMaxLength));
        addColumn(new Data2DColumn("foreign_name", ColumnType.String).setLength(StringMaxLength));
        addColumn(new Data2DColumn("foreign_table", ColumnType.String).setLength(StringMaxLength));
        addColumn(new Data2DColumn("foreign_column", ColumnType.String).setLength(StringMaxLength));
        addColumn(new Data2DColumn("description", ColumnType.String).setLength(StringMaxLength));
        return this;
    }

    /*
        View
     */
    public static final String CreateView
            = " CREATE VIEW Data2D_Column_View AS "
            + " SELECT Data2D_Column.*, Data2D_Definition.* "
            + " FROM Data2D_Column JOIN Data2D_Definition ON Data2D_Column.d2id=Data2D_Definition.d2did";

    public static final String ClearData
            = "DELETE FROM Data2D_Column WHERE d2id=?";

    public static final String QueryData
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
    public boolean setForeignValue(Data2DColumn data, String column, Object value) {
        if (data == null || column == null || value == null) {
            return true;
        }
        if ("d2id".equals(column) && value instanceof Data2DDefinition) {
            data.setData2DDefinition((Data2DDefinition) value);
        }
        return true;
    }

    @Override
    public boolean setValue(Data2DColumn data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        return data.setValue(column, value);
    }

    @Override
    public Object getValue(Data2DColumn data, String column) {
        if (data == null || column == null) {
            return null;
        }
        return data.getValue(column);
    }

    @Override
    public boolean valid(Data2DColumn data) {
        if (data == null) {
            return false;
        }
        return data.valid();
    }

    public List<Data2DColumn> read(long d2id) {
        if (d2id < 0) {
            return null;
        }
        try (Connection conn = DerbyBase.getConnection();) {
            return read(conn, d2id);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<Data2DColumn> read(Connection conn, long d2id) {
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

    public List<Data2DColumn> queryFile(Connection conn, Data2DDefinition.DataType type, File file) {
        if (file == null) {
            return null;
        }
        try {
            Data2DDefinition dataDefinition = getTableData2DDefinition().queryFile(conn, type, file);
            if (dataDefinition == null) {
                return null;
            }
            return read(conn, dataDefinition.getDataID());
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public boolean clear(Data2D data) {
        if (data == null) {
            return false;
        }
        try (Connection conn = DerbyBase.getConnection()) {
            if (data.getDataID() >= 0) {
                return clear(conn, data.getDataID());
            } else {
                return clearFile(conn, data.getType(), data.getFile());
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean clearFile(Data2DDefinition.DataType type, File file) {
        if (file == null) {
            return false;
        }
        try (Connection conn = DerbyBase.getConnection();) {
            return clearFile(conn, type, file);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean clearFile(Connection conn, Data2DDefinition.DataType type, File file) {
        if (conn == null || file == null) {
            return false;
        }
        Data2DDefinition dataDefinition = getTableData2DDefinition().queryFile(conn, type, file);
        if (dataDefinition == null) {
            return false;
        }
        return clear(conn, dataDefinition.getDataID());
    }

    public boolean clear(Connection conn, long d2id) {
        if (conn == null || d2id < 0) {
            return false;
        }
        try (PreparedStatement statement = conn.prepareStatement(ClearData)) {
            statement.setLong(1, d2id);
            statement.executeUpdate();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean save(Connection conn, long dataid, List<Data2DColumn> columns) {
        if (dataid < 0 || columns == null) {
            return false;
        }
        try {
            boolean ac = conn.getAutoCommit();
            conn.setAutoCommit(false);
            List<Data2DColumn> existed = read(conn, dataid);
            conn.setAutoCommit(true);
            if (existed != null && !existed.isEmpty()) {
                for (Data2DColumn ecolumn : existed) {
                    boolean keep = false;
                    for (Data2DColumn icolumn : columns) {
                        if (ecolumn.getColumnID() == icolumn.getColumnID()) {
                            keep = true;
                            break;
                        }
                    }
                    if (!keep) {
                        deleteData(conn, ecolumn);
//                        MyBoxLog.console("delete:" + ecolumn.getColumnName() + " " + ecolumn.getType());
                    }
                }
                conn.commit();
            }
            for (int i = 0; i < columns.size(); i++) {
                Data2DColumn column = columns.get(i);
                column.setDataID(dataid);
                column.setIndex(i);
                if (column.getColumnID() >= 0) {
                    column = updateData(conn, column);
                } else {
                    column = insertData(conn, column);
                }
                if (column == null) {
                    MyBoxLog.error("Failed");
                    return false;
                }
                columns.set(i, column);
//                MyBoxLog.console(i + "   " + column.getColumnName() + " " + column.getType());
            }
            conn.commit();
            conn.setAutoCommit(ac);
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
