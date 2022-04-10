package mara.mybox.db.table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.Data2DStyle;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-4-7
 * @License Apache License Version 2.0
 */
public class TableData2DStyle extends BaseTable<Data2DStyle> {

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
        addColumn(new ColumnDefinition("did", ColumnType.Long, true)
                .setReferName("Data2D_Style_def_fk").setReferTable("Data2D_Definition").setReferColumn("d2did")
                .setOnDelete(ColumnDefinition.OnDelete.Cascade));
        addColumn(new ColumnDefinition("rowFrom", ColumnType.Long, true));
        addColumn(new ColumnDefinition("rowTo", ColumnType.Long, true));
        addColumn(new ColumnDefinition("colName", ColumnType.String, true));
        addColumn(new ColumnDefinition("style", ColumnType.String, true).setLength(StringMaxLength));
        return this;
    }

    public static final String QueryDataStyle
            = "SELECT * FROM Data2D_Style WHERE d2id=?";

    public static final String DeleteStyle
            = "DELETE FROM Data2D_Style WHERE d2id=? AND col=? AND row=?";

    public List<Data2DStyle> queryDataStyles(long d2id) {
        try ( Connection conn = DerbyBase.getConnection()) {
            return queryDataStyles(conn, d2id);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<Data2DStyle> queryDataStyles(Connection conn, long d2id) {
        List<Data2DStyle> styles = new ArrayList<>();
        try ( PreparedStatement statement = conn.prepareStatement(QueryDataStyle)) {
            statement.setLong(1, d2id);
            ResultSet results = statement.executeQuery();
            while (results.next()) {
                Data2DStyle s = readData(results);
                if (s != null) {
                    styles.add(s);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return styles;
    }

    public int deleteStyle(Connection conn, long d2id, long row, long col) {
        int ret;
        try ( PreparedStatement statement = conn.prepareStatement(DeleteStyle)) {
            statement.setLong(1, d2id);
            statement.setLong(2, row);
            statement.setLong(3, col);
            ret = statement.executeUpdate();
        } catch (Exception e) {
            MyBoxLog.error(e);
            ret = -1;
        }
        return ret;
    }

//    public Data2DStyle setStyle(Connection conn, long d2id, long row, long col, String style) {
//        return writeData(conn, new Data2DStyle(d2id, row, col));
//    }
}
