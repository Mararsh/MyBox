package mara.mybox.db.table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.Tag;
import mara.mybox.db.data.TreeNode;
import mara.mybox.db.data.TreeNodeTag;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-3-3
 * @License Apache License Version 2.0
 */
public class TableTreeNodeTag extends BaseTable<TreeNodeTag> {

    protected TableTreeNode tableTreeNode;
    protected TableTag tableTag;

    public TableTreeNodeTag() {
        tableName = "Tree_Node_Tag";
        defineColumns();
    }

    public TableTreeNodeTag(boolean defineColumns) {
        tableName = "Tree_Node_Tag";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableTreeNodeTag defineColumns() {
        addColumn(new ColumnDefinition("ttid", ColumnType.Long, true, true).setAuto(true));
        addColumn(new ColumnDefinition("tnodeid", ColumnType.Long)
                .setReferName("Tree_Node_Tag_Node_fk").setReferTable("Tree_Node").setReferColumn("nodeid")
                .setOnDelete(ColumnDefinition.OnDelete.Cascade)
        );
        addColumn(new ColumnDefinition("tagid", ColumnType.Long)
                .setReferName("Tree_Node_Tag_Tag_fk").setReferTable("Tag").setReferColumn("tgid")
                .setOnDelete(ColumnDefinition.OnDelete.Cascade)
        );
        return this;
    }

    public static final String Create_Unique_Index
            = "CREATE UNIQUE INDEX Tree_Node_Tag_unique_index on Tree_Node_Tag ( tnodeid , tagid  )";

    public static final String QueryNodeTags
            = "SELECT * FROM Tree_Node_Tag, Tag WHERE tnodeid=? AND tagid=tgid";

    public static final String DeleteNodeTags
            = "DELETE FROM Tree_Node_Tag WHERE tnodeid=?";

    public static final String DeleteNodeTag
            = "DELETE FROM Tree_Node_Tag WHERE tnodeid=? AND tagid=?";

    @Override
    public Object readForeignValue(ResultSet results, String column) {
        if (results == null || column == null) {
            return null;
        }
        try {
            if ("tnodeid".equals(column) && results.findColumn("nodeid") > 0) {
                return getTableTreeNode().readData(results);
            }
            if ("tagid".equals(column) && results.findColumn("tgid") > 0) {
                return getTableTag().readData(results);
            }
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public boolean setForeignValue(TreeNodeTag data, String column, Object value) {
        if (data == null || column == null || value == null) {
            return true;
        }
        if ("tnodeid".equals(column) && value instanceof TreeNode) {
            data.setNode((TreeNode) value);
        }
        if ("tagid".equals(column) && value instanceof Tag) {
            data.setTag((Tag) value);
        }
        return true;
    }

    public List<TreeNodeTag> nodeTags(long nodeid) {
        List<TreeNodeTag> tags = new ArrayList<>();
        if (nodeid < 0) {
            return tags;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            tags = nodeTags(conn, nodeid);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return tags;
    }

    public List<TreeNodeTag> nodeTags(Connection conn, long nodeid) {
        List<TreeNodeTag> tags = new ArrayList<>();
        if (conn == null || nodeid < 0) {
            return tags;
        }
        try ( PreparedStatement statement = conn.prepareStatement(QueryNodeTags)) {
            statement.setLong(1, nodeid);
            ResultSet results = statement.executeQuery();
            while (results.next()) {
                TreeNodeTag tag = readData(results);
                if (tag != null) {
                    tags.add(tag);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return tags;
    }

    public List<String> nodeTagNames(long nodeid) {
        List<String> tags = new ArrayList<>();
        if (nodeid < 0) {
            return tags;
        }
        try ( Connection conn = DerbyBase.getConnection();
                 PreparedStatement statement = conn.prepareStatement(QueryNodeTags)) {
            statement.setLong(1, nodeid);
            ResultSet results = statement.executeQuery();
            while (results.next()) {
                TreeNodeTag nodeTag = readData(results);
                if (nodeTag != null) {
                    tags.add(nodeTag.getTag().getTag());
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return tags;
    }

    public int addTags(Connection conn, long nodeid, String category, List<String> tags) {
        if (conn == null || nodeid < 0 || category == null || category.isBlank()
                || tags == null || tags.isEmpty()) {
            return -1;
        }
        String sql = "INSERT INTO Tree_Node_Tag (tnodeid, tagid) "
                + "SELECT " + nodeid + ", tgid FROM Tag "
                + "WHERE category='" + category + "' AND tag IN (";
        for (int i = 0; i < tags.size(); i++) {
            if (i > 0) {
                sql += ",";
            }
            sql += "'" + tags.get(i) + "'";
        }
        sql += ")";
        try ( PreparedStatement statement = conn.prepareStatement(sql)) {
            return statement.executeUpdate();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return -1;
        }
    }

    public int removeTags(Connection conn, long nodeid, String category, List<String> tags) {
        if (conn == null || nodeid < 0 || category == null || category.isBlank()
                || tags == null || tags.isEmpty()) {
            return -1;
        }
        String sql = "DELETE FROM Tree_Node_Tag WHERE tnodeid=" + nodeid + " AND "
                + "tagid IN (SELECT tgid FROM Tag "
                + "WHERE category='" + category + "' AND tag IN (";
        for (int i = 0; i < tags.size(); i++) {
            if (i > 0) {
                sql += ",";
            }
            sql += "'" + tags.get(i) + "'";
        }
        sql += " ) )";
        try ( PreparedStatement statement = conn.prepareStatement(sql)) {
            return statement.executeUpdate();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return -1;
        }
    }

    public int removeTags(Connection conn, long nodeid) {
        if (conn == null || nodeid < 0) {
            return -1;
        }
        try ( PreparedStatement statement = conn.prepareStatement(DeleteNodeTags)) {
            statement.setLong(1, nodeid);
            return statement.executeUpdate();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return -1;
        }
    }

    /*
        get/set
     */
    public TableTreeNode getTableTreeNode() {
        if (tableTreeNode == null) {
            tableTreeNode = new TableTreeNode();
        }
        return tableTreeNode;
    }

    public void setTableTreeNode(TableTreeNode tableTreeNode) {
        this.tableTreeNode = tableTreeNode;
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
