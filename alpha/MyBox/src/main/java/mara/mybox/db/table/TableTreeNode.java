package mara.mybox.db.table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.Tag;
import mara.mybox.db.data.TreeNode;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class TableTreeNode extends BaseTable<TreeNode> {

    public TableTreeNode() {
        tableName = "Tree_Node";
        defineColumns();
    }

    public TableTreeNode(boolean defineColumns) {
        tableName = "Tree_Node";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableTreeNode defineColumns() {
        addColumn(new ColumnDefinition("nodeid", ColumnType.Long, true, true).setAuto(true));
        addColumn(new ColumnDefinition("category", ColumnType.String, true).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("title", ColumnType.String, true).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("value", ColumnType.String).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("more", ColumnType.String).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("update_time", ColumnType.Datetime));
        addColumn(new ColumnDefinition("parentid", ColumnType.Long)
                .setReferName("Tree_Node_parent_fk").setReferTable("Tree_Node").setReferColumn("nodeid")
                .setOnDelete(ColumnDefinition.OnDelete.Cascade)
        );
        return this;
    }

    public static final String Create_Parent_Index
            = "CREATE INDEX Tree_Node_parent_index on Tree_Node ( parentid )";

    public static final String Create_Title_Index
            = "CREATE INDEX Tree_Node_title_index on Tree_Node ( title )";

    public static final String QueryID
            = "SELECT * FROM Tree_Node WHERE nodeid=?";

    public static final String QueryRoot
            = "SELECT * FROM Tree_Node WHERE category=? ORDER BY nodeid ASC";

    public static final String QueryChildren
            = "SELECT * FROM Tree_Node WHERE parentid=? AND nodeid<>parentid";

    public static final String QueryTitle
            = "SELECT * FROM Tree_Node WHERE parentid=? AND title=?";

    public static final String DeleteID
            = "DELETE FROM Tree_Node WHERE nodeid=?";

    public static final String ChildrenCount
            = "SELECT count(nodeid) FROM Tree_Node WHERE parentid=? AND nodeid<>parentid";

    public static final String ChildrenEmpty
            = "SELECT nodeid FROM Tree_Node WHERE parentid=? AND nodeid<>parentid FETCH FIRST ROW ONLY";

    public static final String CategoryCount
            = "SELECT count(nodeid) FROM Tree_Node WHERE category=? AND nodeid<>parentid";

    public static final String CategoryEmpty
            = "SELECT nodeid FROM Tree_Node WHERE category=? AND nodeid<>parentid FETCH FIRST ROW ONLY";

    public static final String DeleteParent
            = "DELETE FROM Tree_Node WHERE parentid=?";

    public static final String DeleteChildren
            = "DELETE FROM Tree_Node WHERE parentid=? AND nodeid<>parentid";

    public static final String Times
            = "SELECT DISTINCT update_time FROM Tree_Node WHERE category=? ORDER BY update_time DESC";

    public TreeNode find(long id) {
        if (id < 0) {
            return null;
        }
        try (Connection conn = DerbyBase.getConnection()) {
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
        try (PreparedStatement statement = conn.prepareStatement(QueryID)) {
            statement.setLong(1, id);
            return query(conn, statement);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<TreeNode> findRoots(String category) {
        try (Connection conn = DerbyBase.getConnection()) {
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
        try (PreparedStatement statement = conn.prepareStatement(QueryRoot)) {
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
        try (Connection conn = DerbyBase.getConnection()) {
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
        try (PreparedStatement statement = conn.prepareStatement(QueryChildren)) {
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
        try (Connection conn = DerbyBase.getConnection()) {
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
        long parentid = node.getParentid();
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
        try (Connection conn = DerbyBase.getConnection()) {
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
        try (PreparedStatement statement = conn.prepareStatement(QueryTitle)) {
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
        try (Connection conn = DerbyBase.getConnection()) {
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
                TreeNode parentNode = find(conn, parent);
                node = new TreeNode(parentNode, title);
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
                String sql = "INSERT INTO Tree_Node (nodeid,title,parentid,category) VALUES(1,'base',1, 'Root')";
                update(conn, sql);
                conn.commit();
                sql = "ALTER TABLE Tree_Node ALTER COLUMN nodeid RESTART WITH 2";
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
        try (Connection conn = DerbyBase.getConnection()) {
            return findAndCreateRoot(conn, category);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public TreeNode findAndCreateRoot(Connection conn, String category) {
        List<TreeNode> roots = findAndCreateRoots(conn, category);
        if (roots != null && !roots.isEmpty()) {
            TreeNode root = roots.get(0);
            root.setTitle(message(category));
            return root;
        } else {
            return null;
        }
    }

    public List<TreeNode> findAndCreateRoots(String category) {
        try (Connection conn = DerbyBase.getConnection()) {
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
                TreeNode root = TreeNode.create().setCategory(category)
                        .setTitle(message(category)).setParentid(base.getNodeid());
                insertData(conn, root);
                conn.commit();
                root.setParentid(root.getNodeid());
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

    public TreeNode findAndCreateChain(Connection conn, TreeNode categoryRoot, String ownerChain) {
        if (conn == null || categoryRoot == null || ownerChain == null || ownerChain.isBlank()) {
            return null;
        }
        try {
            long parentid = categoryRoot.getNodeid();
            String chain = ownerChain;
            if (chain.startsWith(categoryRoot.getTitle() + TreeNode.NodeSeparater)) {
                chain = chain.substring((categoryRoot.getTitle() + TreeNode.NodeSeparater).length());
            } else if (chain.startsWith(message(categoryRoot.getTitle()) + TreeNode.NodeSeparater)) {
                chain = chain.substring((message(categoryRoot.getTitle()) + TreeNode.NodeSeparater).length());
            }
            String[] nodes = chain.split(TreeNode.NodeSeparater);
            TreeNode owner = null;
            for (String node : nodes) {
                owner = findAndCreate(conn, parentid, node);
                if (owner == null) {
                    return null;
                }
                parentid = owner.getNodeid();
            }
            return owner;
        } catch (Exception e) {
            MyBoxLog.console(e);
            return null;
        }
    }

    public List<TreeNode> decentants(Connection conn, long parentid) {
        List<TreeNode> allChildren = new ArrayList<>();
        List<TreeNode> children = children(conn, parentid);
        if (children != null && !children.isEmpty()) {
            allChildren.addAll(allChildren);
            for (TreeNode child : children) {
                children = decentants(conn, child.getNodeid());
                if (children != null && !children.isEmpty()) {
                    allChildren.addAll(allChildren);
                }
            }
        }
        return allChildren;
    }

    public List<TreeNode> decentants(Connection conn, long parentid, long start, long size) {
        List<TreeNode> children = new ArrayList<>();
        try (PreparedStatement query = conn.prepareStatement(QueryChildren)) {
            decentants(conn, query, parentid, start, size, children, 0);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return children;
    }

    public long decentants(Connection conn, PreparedStatement query,
            long parentid, long start, long size, List<TreeNode> nodes, long index) {
        if (conn == null || parentid < 1 || nodes == null
                || query == null || start < 0 || size <= 0 || nodes.size() >= size) {
            return index;
        }
        long thisIndex = index;
        try {
            int thisSize = nodes.size();
            boolean ok = false;
            query.setLong(1, parentid);
            conn.setAutoCommit(true);
            try (ResultSet nresults = query.executeQuery()) {
                while (nresults.next()) {
                    TreeNode data = readData(nresults);
                    if (data != null) {
                        if (thisIndex >= start) {
                            nodes.add(data);
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
                List<TreeNode> children = children(conn, parentid);
                if (children != null) {
                    for (TreeNode child : children) {
                        thisIndex = decentants(conn, query, child.getNodeid(), start, size, nodes, thisIndex);
                        if (nodes.size() >= size) {
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

    public int decentantsSize(Connection conn, long parentid) {
        if (conn == null || parentid < 0) {
            return 0;
        }
        int count = 0;
        try (PreparedStatement sizeQuery = conn.prepareStatement(ChildrenCount)) {
            count = decentantsSize(conn, sizeQuery, parentid);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return count;
    }

    public int decentantsSize(Connection conn, PreparedStatement sizeQuery, long parentid) {
        if (conn == null || sizeQuery == null || parentid < 0) {
            return 0;
        }
        int count = 0;
        try {
            count = childrenSize(sizeQuery, parentid);
            List<TreeNode> children = children(conn, parentid);
            if (children != null) {
                for (TreeNode child : children) {
                    count += decentantsSize(conn, sizeQuery, child.getNodeid());
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return count;
    }

    public int childrenSize(PreparedStatement sizeQuery, long parent) {
        if (sizeQuery == null || parent < 0) {
            return 0;
        }
        int size = 0;
        try {
            sizeQuery.setLong(1, parent);
            sizeQuery.getConnection().setAutoCommit(true);
            try (ResultSet results = sizeQuery.executeQuery()) {
                if (results != null && results.next()) {
                    size = results.getInt(1);
                }
            } catch (Exception e) {
                MyBoxLog.debug(e);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return size;
    }

    public int childrenSize(long parent) {
        if (parent < 0) {
            return 0;
        }
        try (Connection conn = DerbyBase.getConnection()) {
            return childrenSize(conn, parent);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return -1;
        }
    }

    public int childrenSize(Connection conn, long parent) {
        if (conn == null || parent < 0) {
            return 0;
        }
        int size = 0;
        try (PreparedStatement sizeQuery = conn.prepareStatement(ChildrenCount)) {
            size = childrenSize(sizeQuery, parent);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return size;
    }

    public boolean childrenEmpty(Connection conn, long parent) {
        boolean isEmpty = true;
        try (PreparedStatement statement = conn.prepareStatement(ChildrenEmpty)) {
            statement.setLong(1, parent);
            conn.setAutoCommit(true);
            try (ResultSet results = statement.executeQuery()) {
                isEmpty = results == null || !results.next();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return isEmpty;
    }

    public int categorySize(String category) {
        try (Connection conn = DerbyBase.getConnection()) {
            return categorySize(conn, category);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return -1;
        }
    }

    public int categorySize(Connection conn, String category) {
        if (conn == null) {
            return 0;
        }
        int size = 0;
        try (PreparedStatement statement = conn.prepareStatement(CategoryCount)) {
            statement.setString(1, category);
            conn.setAutoCommit(true);
            try (ResultSet results = statement.executeQuery()) {
                if (results != null && results.next()) {
                    size = results.getInt(1);
                }
            } catch (Exception e) {
                MyBoxLog.error(e);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return size;
    }

    public boolean categoryEmpty(String category) {
        boolean isEmpty = true;
        try (Connection conn = DerbyBase.getConnection();
                PreparedStatement statement = conn.prepareStatement(CategoryEmpty)) {
            statement.setString(1, category);
            conn.setAutoCommit(true);
            try (ResultSet results = statement.executeQuery()) {
                isEmpty = results == null || !results.next();
            } catch (Exception e) {
                MyBoxLog.error(e);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return isEmpty;
    }

    public int deleteChildren(long parent) {
        try (Connection conn = DerbyBase.getConnection()) {
            return deleteChildren(conn, parent);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return -1;
        }
    }

    public int deleteChildren(Connection conn, long parent) {
        try (PreparedStatement statement = conn.prepareStatement(DeleteChildren)) {
            statement.setLong(1, parent);
            return statement.executeUpdate();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return -1;
        }
    }

    public String tagsCondition(List<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        String condition = " nodeid IN ( SELECT tnodeid FROM Tree_Node_Tag WHERE tagid IN ( " + tags.get(0).getTgid();
        for (int i = 1; i < tags.size(); ++i) {
            condition += ", " + tags.get(i).getTgid();
        }
        condition += " ) ) ";
        return condition;
    }

    public List<Date> times(String category) {
        try (Connection conn = DerbyBase.getConnection()) {
            conn.setReadOnly(true);
            return times(conn, category);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<Date> times(Connection conn, String category) {
        List<Date> times = new ArrayList();
        if (conn == null) {
            return times;
        }
        try (PreparedStatement statement = conn.prepareStatement(Times)) {
            statement.setString(1, category);
            conn.setAutoCommit(true);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    Date time = results.getTimestamp("update_time");
                    if (time != null) {
                        times.add(time);
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return times;
    }

}
