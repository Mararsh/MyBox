package mara.mybox.db.table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.Note;
import mara.mybox.db.data.Tag;
import mara.mybox.db.data.TreeLeaf;
import mara.mybox.db.data.TreeLeafTag;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-3-3
 * @License Apache License Version 2.0
 */
public class TableTreeLeafTag extends BaseTable<TreeLeafTag> {

    protected TableTreeLeaf tableTreeLeaf;
    protected TableTag tableTag;

    public TableTreeLeafTag() {
        tableName = "Tree_Leaf_Tag";
        defineColumns();
    }

    public TableTreeLeafTag(boolean defineColumns) {
        tableName = "Tree_Leaf_Tag";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableTreeLeafTag defineColumns() {
        addColumn(new ColumnDefinition("ttid", ColumnType.Long, true, true).setAuto(true));
        addColumn(new ColumnDefinition("leaffid", ColumnType.Long)
                .setReferName("Tree_Leaf_Tag_Leaf_fk").setReferTable("Tree_leaf").setReferColumn("leafid")
                .setOnDelete(ColumnDefinition.OnDelete.Cascade)
        );
        addColumn(new ColumnDefinition("tagid", ColumnType.Long)
                .setReferName("Tree_Leaf_Tag_Tag_fk").setReferTable("Tag").setReferColumn("tgid")
                .setOnDelete(ColumnDefinition.OnDelete.Cascade)
        );
        return this;
    }

    public static final String Create_Unique_Index
            = "CREATE UNIQUE INDEX Tree_Leaf_Tag_unique_index on Tree_Leaf_Tag ( leaffid , tagid  )";

    public static final String QueryLeafTags
            = "SELECT * FROM Tree_Leaf_Tag WHERE leaffid=?";

    public static final String DeleteLeafTags
            = "DELETE FROM Tree_Leaf_Tag WHERE leaffid=?";

    @Override
    public Object readForeignValue(ResultSet results, String column) {
        if (results == null || column == null) {
            return null;
        }
        try {
            if ("leaffid".equals(column) && results.findColumn("leafid") > 0) {
                return getTableTreeLeaf().readData(results);
            }
            if ("tagid".equals(column) && results.findColumn("tgid") > 0) {
                return getTableTag().readData(results);
            }
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public boolean setForeignValue(TreeLeafTag data, String column, Object value) {
        if (data == null || column == null || value == null) {
            return true;
        }
        if ("leaffid".equals(column) && value instanceof Note) {
            data.setLeaf((TreeLeaf) value);
        }
        if ("tagid".equals(column) && value instanceof Tag) {
            data.setTag((Tag) value);
        }
        return true;
    }

    public List<Long> readTags(long leafid) {
        try ( Connection conn = DerbyBase.getConnection()) {
            return readTags(conn, leafid);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<Long> readTags(Connection conn, long leafid) {
        List<Long> tags = new ArrayList();
        try ( PreparedStatement statement = conn.prepareStatement(QueryLeafTags)) {
            statement.setLong(1, leafid);
            try ( ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    TreeLeafTag data = readData(results);
                    tags.add(data.getTagid());
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return tags;
    }

    /*
        get/set
     */
    public TableTreeLeaf getTableTreeLeaf() {
        if (tableTreeLeaf == null) {
            tableTreeLeaf = new TableTreeLeaf();
        }
        return tableTreeLeaf;
    }

    public void setTableTreeLeaf(TableTreeLeaf tableTreeLeaf) {
        this.tableTreeLeaf = tableTreeLeaf;
    }

    public TableTag getTableTag() {
        if (tableTag == null) {
            tableTag = new TableTag();
        }
        return tableTag;
    }

    public void setTableTag(TableTag tableTag) {
        this.tableTag = tableTag;
    }

}
