package mara.mybox.db.table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import mara.mybox.db.DerbyBase;
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
        addColumn(new ColumnDefinition("title", ColumnType.String).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("rowStart", ColumnType.Long)); // 0-based
        addColumn(new ColumnDefinition("rowEnd", ColumnType.Long));  // Exclude
        addColumn(new ColumnDefinition("columns", ColumnType.String).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("rowFilter", ColumnType.String).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("columnFilter", ColumnType.String).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("fontColor", ColumnType.String).setLength(64));
        addColumn(new ColumnDefinition("fontSize", ColumnType.String).setLength(64));
        addColumn(new ColumnDefinition("bgColor", ColumnType.String).setLength(64));
        addColumn(new ColumnDefinition("bold", ColumnType.Boolean));
        addColumn(new ColumnDefinition("moreStyle", ColumnType.String).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("sequence", ColumnType.Float));
        addColumn(new ColumnDefinition("abnoramlValues", ColumnType.Boolean));
        orderColumns = "d2id, sequence, d2sid";
        return this;
    }

    public static final String QueryStyles
            = "SELECT * FROM Data2D_Style WHERE d2id=? ORDER BY sequence,d2sid";

    public static final String ClearStyles
            = "DELETE FROM Data2D_Style WHERE d2id=?";

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

    public boolean clear(Connection conn, long d2id) {
        if (conn == null || d2id < 0) {
            return false;
        }
        try ( PreparedStatement statement = conn.prepareStatement(ClearStyles)) {
            statement.setLong(1, d2id);
            statement.executeUpdate();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public int copyStyles(Connection conn, long sourceid, long targetid) {
        if (conn == null || sourceid < 0 || targetid < 0 || sourceid == targetid) {
            return -1;
        }
        clear(conn, targetid);
        int count = 0;
        try ( PreparedStatement statement = conn.prepareStatement(QueryStyles)) {
            conn.setAutoCommit(false);
            statement.setLong(1, sourceid);
            ResultSet results = statement.executeQuery();
            while (results.next()) {
                Data2DStyle s = readData(results);
                s.setD2sid(-1);
                s.setD2id(targetid);
                if (insertData(conn, s) != null) {
                    count++;
                    if (count % DerbyBase.BatchSize == 0) {
                        conn.commit();
                    }
                }
            }
            conn.commit();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return -1;
        }
        return count;
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
