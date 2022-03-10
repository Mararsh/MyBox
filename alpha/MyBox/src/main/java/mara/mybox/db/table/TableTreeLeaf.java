package mara.mybox.db.table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.TreeLeaf;
import mara.mybox.db.data.TreeNode;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-3-9
 * @License Apache License Version 2.0
 */
public class TableTreeLeaf extends BaseTable<TreeLeaf> {

    public TableTreeLeaf() {
        tableName = "Tree_leaf";
        defineColumns();
    }

    public TableTreeLeaf(boolean defineColumns) {
        tableName = "Tree_leaf";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableTreeLeaf defineColumns() {
        addColumn(new ColumnDefinition("leafid", ColumnType.Long, true, true).setAuto(true));
        addColumn(new ColumnDefinition("name", ColumnType.String, true).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("value", ColumnType.String).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("more", ColumnType.String).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("parentid", ColumnType.Long)
                .setReferName("Tree_leaf_parent_fk").setReferTable("Tree").setReferColumn("nodeid")
                .setOnDelete(ColumnDefinition.OnDelete.Cascade)
        );
        addColumn(new ColumnDefinition("update_time", ColumnType.Datetime));
        return this;
    }

    public static final String Create_Parent_Index
            = "CREATE INDEX Tree_leaf_parent_index on Tree_leaf ( parentid )";

    public static final String QueryID
            = "SELECT * FROM Tree_leaf WHERE leafid=?";

    public static final String QueryName
            = "SELECT * FROM Tree_leaf WHERE parentid=? AND name=?";

    public static final String QueryChildren
            = "SELECT * FROM Tree_leaf WHERE parentid=?";

    public static final String DeleteChildren
            = "DELETE FROM Tree_leaf WHERE parentid=?";

    public static final String ChildrenCount
            = "SELECT COUNT(leafid) FROM Tree_leaf WHERE parentid=?";

    public TreeLeaf find(long id) {
        try ( Connection conn = DerbyBase.getConnection()) {
            return find(conn, id);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public TreeLeaf find(Connection conn, long id) {
        if (conn == null) {
            return null;
        }
        try ( PreparedStatement statement = conn.prepareStatement(QueryID)) {
            statement.setLong(1, id);
            return query(conn, statement);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public TreeLeaf find(Connection conn, long parentid, String name) {
        if (conn == null) {
            return null;
        }
        try ( PreparedStatement statement = conn.prepareStatement(QueryName)) {
            statement.setLong(1, parentid);
            statement.setString(2, name);
            return query(conn, statement);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<TreeLeaf> leaves(Connection conn, long parentid) {
        if (conn == null || parentid < 1) {
            return null;
        }
        try ( PreparedStatement statement = conn.prepareStatement(QueryChildren)) {
            statement.setLong(1, parentid);
            return query(statement);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<TreeLeaf> withSub(Connection conn, TableTree tableTree, long parentid) {
        List<TreeLeaf> allLeaves = new ArrayList<>();
        List<TreeLeaf> leaves = leaves(conn, parentid);
        if (leaves != null && !leaves.isEmpty()) {
            allLeaves.addAll(allLeaves);
        }
        if (tableTree == null) {
            tableTree = new TableTree();
        }
        List<TreeNode> children = tableTree.children(conn, parentid);
        if (children != null) {
            for (TreeNode child : children) {
                leaves = withSub(conn, tableTree, child.getNodeid());
                if (leaves != null && !leaves.isEmpty()) {
                    allLeaves.addAll(allLeaves);
                }
            }
        }
        return allLeaves;
    }

    public List<TreeLeaf> withSub(TableTree tableTree, long parentid, long start, long size) {
        List<TreeLeaf> leaves = new ArrayList<>();
        try ( Connection conn = DerbyBase.getConnection();
                 PreparedStatement query = conn.prepareStatement(QueryChildren)) {
            if (tableTree == null) {
                tableTree = new TableTree();
            }
            withSub(conn, query, tableTree, parentid, start, size, leaves, 0);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return leaves;
    }

    public long withSub(Connection conn, PreparedStatement query,
            TableTree tableTree, long parentid, long start, long size, List<TreeLeaf> leaves, long index) {
        if (conn == null || parentid < 1 || leaves == null || tableTree == null
                || query == null || start < 0 || size <= 0 || leaves.size() >= size) {
            return index;
        }
        long thisIndex = index;
        try {
            int thisSize = leaves.size();
            boolean ok = false;
            query.setLong(1, parentid);
            try ( ResultSet nresults = query.executeQuery()) {
                while (nresults.next()) {
                    TreeLeaf data = readData(nresults);
                    if (data != null) {
                        if (thisIndex >= start) {
                            leaves.add(data);
                            thisSize++;
                        }
                        thisIndex++;
                        if (thisSize >= size) {
                            ok = true;
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                MyBoxLog.error(e, tableName);
            }
            if (!ok) {
                List<TreeNode> children = tableTree.children(conn, parentid);
                if (children != null) {
                    for (TreeNode child : children) {
                        thisIndex = withSub(conn, query, tableTree, child.getNodeid(), start, size, leaves, thisIndex);
                        if (leaves.size() >= size) {
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return thisIndex;
    }

    /*
        static methods
     */
    public static int parentidSize(long parentid) {
        try ( Connection conn = DerbyBase.getConnection()) {
            return parentidSize(conn, parentid);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return 0;
        }
    }

    public static int parentidSize(Connection conn, long parentid) {
        int size = 0;
        try ( PreparedStatement statement = conn.prepareStatement(ChildrenCount)) {
            statement.setLong(1, parentid);
            try ( ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    size = results.getInt(1);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return size;
    }

    public static int withSubSize(TableTree tableTree, long parentid) {
        int count = 0;
        try ( Connection conn = DerbyBase.getConnection()) {
            count = withSubSize(conn, tableTree, parentid);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return count;
    }

    public static int withSubSize(Connection conn, TableTree tableTree, long parentid) {
        int count = parentidSize(conn, parentid);
        if (tableTree == null) {
            tableTree = new TableTree();
        }
        List<TreeNode> children = tableTree.children(conn, parentid);
        if (children != null) {
            for (TreeNode child : children) {
                count += withSubSize(conn, tableTree, child.getNodeid());
            }
        }
        return count;
    }

}
