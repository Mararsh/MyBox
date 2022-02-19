package mara.mybox.db.table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.TreeNode;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class TableTree extends BaseTable<TreeNode> {

    public TableTree() {
        tableName = "Tree";
        defineColumns();
    }

    public TableTree(boolean defineColumns) {
        tableName = "Tree";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableTree defineColumns() {
        addColumn(new ColumnDefinition("nodeid", ColumnType.Long, true, true).setAuto(true));
        addColumn(new ColumnDefinition("title", ColumnType.String, true).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("attribute", ColumnType.String).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("parent", ColumnType.Long)
                .setReferName("Tree_parent_fk").setReferTable("Tree").setReferColumn("nodeid")
                .setOnDelete(ColumnDefinition.OnDelete.Cascade)
        );
        return this;
    }

    public static final String Create_Parent_Index
            = "CREATE INDEX Tree_parent_index on Tree ( parent )";

    public static final String Create_Title_Index
            = "CREATE INDEX Tree_title_index on Tree ( title )";

    public static final String QueryID
            = "SELECT * FROM Tree WHERE nodeid=?";

    public static final String QueryRoot
            = "SELECT * FROM Tree WHERE title=? AND nodeid=parent";

    public static final String QueryChildren
            = "SELECT * FROM Tree WHERE parent=? AND nodeid<>parent";

    public static final String QueryAttribute
            = "SELECT * FROM Tree WHERE parent=? AND title=?";

    public static final String DeleteID
            = "DELETE FROM Tree WHERE nodeid=?";

    public static final String ParentSize
            = "SELECT count(nodeid) FROM Tree WHERE parent=? AND nodeid<>parent";

    public static final String DeleteParent
            = "DELETE FROM Tree WHERE parent=?";

    public static final String DeleteChildren
            = "DELETE FROM Tree WHERE parent=? AND nodeid<>parent";

    public TreeNode find(long id) {
        if (id < 0) {
            return null;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            return find(conn, id);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public TreeNode find(Connection conn, long id) {
        if (conn == null || id < 0) {
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

    public List<TreeNode> findRoots(String category) {
        try ( Connection conn = DerbyBase.getConnection()) {
            return findRoots(conn, category);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<TreeNode> findRoots(Connection conn, String category) {
        if (conn == null || category == null) {
            return null;
        }
        try ( PreparedStatement statement = conn.prepareStatement(QueryRoot)) {
            statement.setString(1, category);
            return query(statement);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<TreeNode> children(long parent) {
        if (parent < 0) {
            return null;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            return children(conn, parent);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<TreeNode> children(Connection conn, long parent) {
        if (conn == null || parent < 0) {
            return null;
        }
        try ( PreparedStatement statement = conn.prepareStatement(QueryChildren)) {
            statement.setLong(1, parent);
            return query(statement);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<TreeNode> ancestor(long id) {
        if (id <= 0) {
            return null;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            return ancestor(conn, id);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<TreeNode> ancestor(Connection conn, long id) {
        if (conn == null || id <= 0) {
            return null;
        }
        List<TreeNode> ancestor = null;
        TreeNode node = find(conn, id);
        if (node == null || node.isRoot()) {
            return ancestor;
        }
        long parentid = node.getParent();
        TreeNode parent = find(conn, parentid);
        if (parent != null) {
            ancestor = ancestor(conn, parentid);
            if (ancestor == null) {
                ancestor = new ArrayList<>();
            }
            ancestor.add(parent);
        }
        return ancestor;
    }

    public TreeNode find(long parent, String title) {
        if (title == null || title.isBlank()) {
            return null;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            return find(conn, parent, title);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public TreeNode find(Connection conn, long parent, String title) {
        if (conn == null || title == null || title.isBlank()) {
            return null;
        }
        try ( PreparedStatement statement = conn.prepareStatement(QueryAttribute)) {
            statement.setLong(1, parent);
            statement.setString(2, title);
            return query(conn, statement);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public TreeNode findAndCreate(long parent, String title) {
        if (title == null || title.isBlank()) {
            return null;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            return findAndCreate(conn, parent, title);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public TreeNode findAndCreate(Connection conn, long parent, String title) {
        if (conn == null || title == null || title.isBlank()) {
            return null;
        }
        try {
            TreeNode node = find(conn, parent, title);
            if (node == null) {
                node = new TreeNode(parent, title);
                node = insertData(conn, node);
                conn.commit();
            }
            return node;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public TreeNode checkBase(Connection conn) {
        TreeNode base = find(conn, 1);
        if (base == null) {
            try {
                String sql = "INSERT INTO Tree(nodeid,title,parent) VALUES(1,'base',1)";
                update(conn, sql);
                conn.commit();
                sql = "ALTER TABLE Tree ALTER COLUMN nodeid RESTART WITH 2";
                update(conn, sql);
                conn.commit();
                base = find(conn, 1);
            } catch (Exception e) {
                MyBoxLog.error(e);
            }
        }
        return base;
    }

    public TreeNode findAndCreateRoot(String category) {
        try ( Connection conn = DerbyBase.getConnection()) {
            return findAndCreateRoot(conn, category);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public TreeNode findAndCreateRoot(Connection conn, String category) {
        List<TreeNode> roots = findAndCreateRoots(conn, category);
        if (roots != null && !roots.isEmpty()) {
            return roots.get(0);
        } else {
            return null;
        }
    }

    public List<TreeNode> findAndCreateRoots(String category) {
        try ( Connection conn = DerbyBase.getConnection()) {
            return findAndCreateRoots(conn, category);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<TreeNode> findAndCreateRoots(Connection conn, String category) {
        if (conn == null) {
            return null;
        }
        List<TreeNode> roots = findRoots(conn, category);
        if (roots == null || roots.isEmpty()) {
            try {
                TreeNode base = checkBase(conn);
                TreeNode root = TreeNode.create().setTitle(category).setParent(base.getNodeid());
                insertData(conn, root);
                conn.commit();
                root.setParent(root.getNodeid());
                updateData(conn, root);
                conn.commit();
                roots = new ArrayList<>();
                roots.add(root);
            } catch (Exception e) {
                MyBoxLog.error(e);
            }
        }
        return roots;
    }

    public TreeNode findAndCreateChain(Connection conn, long rootid, String ownerChain) {
        if (conn == null || ownerChain == null || ownerChain.isBlank()) {
            return null;
        }
        try {
            String[] nodes = ownerChain.split(TreeNode.NodeSeparater);
            long ownerid = rootid;
            TreeNode owner = null;
            for (String node : nodes) {
                owner = findAndCreate(conn, ownerid, node);
                if (owner == null) {
                    return null;
                }
                ownerid = owner.getNodeid();
            }
            return owner;
        } catch (Exception e) {
            MyBoxLog.console(e);
            return null;
        }
    }

    /*
        static methods
     */
    public static int size(long parent) {
        try ( Connection conn = DerbyBase.getConnection()) {
            return size(conn, parent);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return -1;
        }
    }

    public static int size(Connection conn, long parent) {
        if (conn == null) {
            return 0;
        }
        int size = 0;
        try ( PreparedStatement statement = conn.prepareStatement(ParentSize)) {
            statement.setLong(1, parent);
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

    public static int deleteChildren(long parent) {
        try ( Connection conn = DerbyBase.getConnection()) {
            return deleteChildren(conn, parent);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return -1;
        }
    }

    public static int deleteChildren(Connection conn, long parent) {
        try ( PreparedStatement statement = conn.prepareStatement(DeleteChildren)) {
            statement.setLong(1, parent);
            return statement.executeUpdate();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return -1;
        }
    }

}
