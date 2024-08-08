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
public class TableTreeTag extends BaseTable<TreeNodeTag> {

    protected BaseTable dataTable;
    protected TableTree tableTree;
    protected TableTag tableTag;

    public TableTreeTag(TableTree table) {
        tableTree = table;
        if (tableTree == null) {
            return;
        }
        dataTable = tableTree.dataTable;
        tableName = dataTable.tableName + "_Tag";
        idColumnName = "ttid";
        defineColumns();
        try (Connection conn = DerbyBase.getConnection()) {
            createTable(conn, false);
            createIndices(conn);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public final TableTreeTag defineColumns() {
        addColumn(new ColumnDefinition("ttid", ColumnType.Long, true, true).setAuto(true));
        addColumn(new ColumnDefinition("tnodeid", ColumnType.Long)
                .setReferName(tableName + "_nodeid_fk")
                .setReferTable(dataTable.tableName).setReferColumn(dataTable.idColumnName)
                .setOnDelete(ColumnDefinition.OnDelete.Cascade)
        );
        addColumn(new ColumnDefinition("tagid", ColumnType.Long)
                .setReferName(tableName + "_tagid_fk")
                .setReferTable("Tag").setReferColumn("tgid")
                .setOnDelete(ColumnDefinition.OnDelete.Cascade)
        );
        return this;
    }

    public final boolean createIndices(Connection conn) {
        if (conn == null || tableName == null) {
            return false;
        }
        String sql = "CREATE INDEX " + tableName + "_unique_index on " + tableName + " ( tnodeid , tagid )";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (Exception e) {
//            MyBoxLog.debug(e);
//            return false;
        }
        sql = "CREATE INDEX " + tableName + "_title_index on " + tableName + " ( parentid, title )";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (Exception e) {
//            MyBoxLog.debug(e);
//            return false;
        }
        return true;
    }

    public static final String Create_Unique_Index
            = "CREATE UNIQUE INDEX Tree_Node_Tag_unique_index on Tree_Node_Tag ( tnodeid , tagid  )";

    public static final String QueryNodeTags
            = "SELECT * FROM Tree_Node_Tag, Tag WHERE tnodeid=? AND tagid=tgid";

    public static final String QueryNodeTag
            = "SELECT * FROM Tree_Node_Tag, Tag WHERE tnodeid=? AND tagid=?";

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
        try (Connection conn = DerbyBase.getConnection()) {
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
        try (PreparedStatement statement = conn.prepareStatement(QueryNodeTags)) {
            statement.setLong(1, nodeid);
            conn.setAutoCommit(true);
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

    public TreeNodeTag query(Connection conn, long nodeid, long tagid) {
        if (conn == null || nodeid < 0 || tagid < 0) {
            return null;
        }
        TreeNodeTag tag = null;
        try (PreparedStatement statement = conn.prepareStatement(QueryNodeTag)) {
            statement.setLong(1, nodeid);
            statement.setLong(2, tagid);
            conn.setAutoCommit(true);
            ResultSet results = statement.executeQuery();
            if (results.next()) {
                tag = readData(results);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return tag;
    }

    public List<String> nodeTagNames(long nodeid) {
        List<String> tags = new ArrayList<>();
        if (nodeid < 0) {
            return tags;
        }
        try (Connection conn = DerbyBase.getConnection()) {
            tags = nodeTagNames(conn, nodeid);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return tags;
    }

    public List<String> nodeTagNames(Connection conn, long nodeid) {
        List<String> tags = new ArrayList<>();
        if (nodeid < 0) {
            return tags;
        }
        try (PreparedStatement statement = conn.prepareStatement(QueryNodeTags)) {
            statement.setLong(1, nodeid);
            ResultSet results = statement.executeQuery();
            conn.setAutoCommit(true);
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
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            return statement.executeUpdate();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return -1;
        }
    }

    public int writeTags(Connection conn, long nodeid, String category, List<String> tags) {
        if (conn == null || nodeid < 0 || category == null || category.isBlank()
                || tags == null || tags.isEmpty()) {
            return -1;
        }
        int count = 0;
        try {
            for (String name : tags) {
                Tag tag = getTableTag().findAndCreate(conn, category, name);
                if (tag == null) {
                    continue;
                }
                if (query(conn, nodeid, tag.getTgid()) == null) {
                    TreeNodeTag nodeTag = new TreeNodeTag(nodeid, tag.getTgid());
                    count += insertData(conn, nodeTag) == null ? 0 : 1;
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return -1;
        }
        return count;
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
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
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
        try (PreparedStatement statement = conn.prepareStatement(DeleteNodeTags)) {
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
    public TableTree getTableTreeNode() {
        return tableTree;
    }

    public void setTableTree(TableTree tableTree) {
        this.tableTree = tableTree;
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
