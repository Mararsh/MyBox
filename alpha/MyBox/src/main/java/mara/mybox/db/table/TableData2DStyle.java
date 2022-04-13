package mara.mybox.db.table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.Data2DStyle;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-4-7
 * @License Apache License Version 2.0
 */
public class TableData2DStyle extends BaseTable<Data2DStyle> {

    protected TableData2DDefinition tableData2DDefinition;

    public TableData2DStyle() {
        tableName = "Data2D_Style";
        defineColumns();
    }

    public TableData2DStyle(boolean defineColumns) {
        tableName = "Data2D_Style";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableData2DStyle defineColumns() {
        addColumn(new ColumnDefinition("d2sid", ColumnType.Long, true, true).setAuto(true));
        addColumn(new ColumnDefinition("d2id", ColumnType.Long, true)
                .setReferName("Data2D_Style_d2id_fk").setReferTable("Data2D_Definition").setReferColumn("d2did")
                .setOnDelete(ColumnDefinition.OnDelete.Cascade));
        addColumn(new ColumnDefinition("row", ColumnType.Long, true));
        addColumn(new ColumnDefinition("colName", ColumnType.String, true));
        addColumn(new ColumnDefinition("style", ColumnType.String, true).setLength(StringMaxLength));
        return this;
    }

    public static final String Create_Unique_Index
            = "CREATE UNIQUE INDEX Data2D_Style_unique_index on Data2D_Style ( d2id , row, colName  )";

    public static final String QueryStyle
            = "SELECT * FROM Data2D_Style WHERE d2id=? AND row=? AND colName=? FETCH FIRST ROW ONLY";

    public static final String QueryPageStyles
            = "SELECT * FROM Data2D_Style WHERE d2id=? AND row>=? AND row<?";

    public static final String ClearStyle
            = "DELETE FROM Data2D_Style WHERE d2id=?";

    public static final String CheckColumns
            = "DELETE FROM Data2D_Style WHERE d2id=? AND colName NOT IN ( ? )";

    public static final String CheckRows
            = "DELETE FROM Data2D_Style WHERE d2id=? AND (row < 0 OR row >= ?)";

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
    public boolean setForeignValue(Data2DStyle data, String column, Object value) {
        if (data == null || column == null || value == null) {
            return true;
        }
        if ("d2id".equals(column) && value instanceof Data2DDefinition) {
            data.setData2DDefinition((Data2DDefinition) value);
        }
        return true;
    }

    public Data2DStyle query(Connection conn, Data2DStyle d2Style) {
        if (conn == null || d2Style == null) {
            return null;
        }
        try ( PreparedStatement statement = conn.prepareStatement(QueryStyle)) {
            statement.setLong(1, d2Style.getD2id());
            statement.setLong(2, d2Style.getRow());
            statement.setString(3, d2Style.getColName());
            ResultSet results = statement.executeQuery();
            if (results.next()) {
                return readData(results);
            } else {
                return null;
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public Data2DStyle write(Connection conn, Data2DStyle d2Style) {
        if (conn == null || d2Style == null) {
            return null;
        }
        try {
            Data2DStyle exist = query(conn, d2Style);
            if (exist == null) {
                return insertData(conn, d2Style);
            } else {
                d2Style.setD2sid(exist.getD2sid());
                return updateData(conn, d2Style);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public boolean clear(Connection conn, long d2id) {
        if (conn == null || d2id < 0) {
            return false;
        }
        try ( PreparedStatement statement = conn.prepareStatement(ClearStyle)) {
            statement.setLong(1, d2id);
            statement.executeUpdate();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean checkColumns(Connection conn, long d2id, List<String> colNames) {
        if (conn == null || d2id < 0 || colNames == null || colNames.isEmpty()) {
            return false;
        }
        String in = null;
        for (String col : colNames) {
            if (in == null) {
                in = "'" + col + "'";
            } else {
                in += ", '" + col + "'";
            }
        }
        String sql = "DELETE FROM Data2D_Style WHERE d2id=" + d2id
                + "  AND colName NOT IN ( " + in + " )";
        try ( Statement statement = conn.createStatement()) {
            statement.executeUpdate(sql);
            conn.commit();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean checkRows(Connection conn, long d2id, long maxRow) {
        if (conn == null || d2id < 0 || maxRow < 0) {
            return false;
        }
        try ( PreparedStatement statement = conn.prepareStatement(CheckRows)) {
            statement.setLong(1, d2id);
            statement.setLong(2, maxRow);
            statement.executeUpdate();
            conn.commit();
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
