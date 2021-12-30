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
import mara.mybox.db.data.WebFavorite;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class TableWebFavorite extends BaseTable<WebFavorite> {

    public TableWebFavorite() {
        tableName = "Web_Favorite";
        defineColumns();
    }

    public TableWebFavorite(boolean defineColumns) {
        tableName = "Web_Favorite";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableWebFavorite defineColumns() {
        addColumn(new ColumnDefinition("faid", ColumnType.Long, true, true).setIsID(true));
        addColumn(new ColumnDefinition("title", ColumnType.String, true).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("address", ColumnType.String, true).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("icon", ColumnType.String).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("owner", ColumnType.Long)
                .setForeignName("Web_Favorite_owner_fk").setForeignTable("Tree").setForeignColumn("nodeid")
                .setOnDelete(ColumnDefinition.OnDelete.Cascade)
        );
        return this;
    }

    public static final String Create_Owner_Index
            = "CREATE INDEX Web_Favorite_owner_index on Web_Favorite ( owner )";

    public static final String QueryID
            = "SELECT * FROM Web_Favorite WHERE faid=?";

    public static final String QueryTitle
            = "SELECT * FROM Web_Favorite WHERE owner=? AND title=?";

    public static final String QueryOwner
            = "SELECT * FROM Web_Favorite WHERE owner=?";

    public static final String DeleteOwner
            = "DELETE FROM Web_Favorite WHERE owner=?";

    public static final String OwnerSize
            = "SELECT COUNT(faid) FROM Web_Favorite WHERE owner=?";

    public WebFavorite find(long id) {
        try ( Connection conn = DerbyBase.getConnection()) {
            return find(conn, id);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public WebFavorite find(Connection conn, long id) {
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

    public WebFavorite find(Connection conn, long owner, String title) {
        if (conn == null) {
            return null;
        }
        try ( PreparedStatement statement = conn.prepareStatement(QueryTitle)) {
            statement.setLong(1, owner);
            statement.setString(2, title);
            return query(conn, statement);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<WebFavorite> addresses(Connection conn, long owner) {
        if (conn == null || owner < 1) {
            return null;
        }
        try ( PreparedStatement statement = conn.prepareStatement(QueryOwner)) {
            statement.setLong(1, owner);
            return query(statement);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<WebFavorite> withSub(Connection conn, TableTree tableTree, long parent) {
        List<WebFavorite> addresses = new ArrayList<>();
        List<WebFavorite> bookFavoriteAddresss = addresses(conn, parent);
        if (bookFavoriteAddresss != null && !bookFavoriteAddresss.isEmpty()) {
            addresses.addAll(addresses);
        }
        if (tableTree == null) {
            tableTree = new TableTree();
        }
        List<TreeNode> children = tableTree.children(conn, parent);
        if (children != null) {
            for (TreeNode child : children) {
                bookFavoriteAddresss = withSub(conn, tableTree, child.getNodeid());
                if (bookFavoriteAddresss != null && !bookFavoriteAddresss.isEmpty()) {
                    addresses.addAll(addresses);
                }
            }
        }
        return addresses;
    }

    public List<WebFavorite> withSub(TableTree tableTree, long parent, long start, long size) {
        List<WebFavorite> notes = new ArrayList<>();
        try ( Connection conn = DerbyBase.getConnection();
                 PreparedStatement qFavoriteAddresss = conn.prepareStatement(QueryOwner)) {
            if (tableTree == null) {
                tableTree = new TableTree();
            }
            withSub(conn, qFavoriteAddresss, tableTree, parent, start, size, notes, 0);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return notes;
    }

    public long withSub(Connection conn, PreparedStatement qFavoriteAddresss,
            TableTree tableTree, long parent, long start, long size, List<WebFavorite> addresses, long index) {
        if (conn == null || parent < 1 || addresses == null || tableTree == null
                || qFavoriteAddresss == null || start < 0 || size <= 0 || addresses.size() >= size) {
            return index;
        }
        long thisIndex = index;
        try {
            int thisSize = addresses.size();
            boolean ok = false;
            qFavoriteAddresss.setLong(1, parent);
            try ( ResultSet nresults = qFavoriteAddresss.executeQuery()) {
                while (nresults.next()) {
                    WebFavorite data = readData(nresults);
                    if (data != null) {
                        if (thisIndex >= start) {
                            addresses.add(data);
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
                List<TreeNode> children = tableTree.children(conn, parent);
                if (children != null) {
                    for (TreeNode child : children) {
                        thisIndex = withSub(conn, qFavoriteAddresss, tableTree, child.getNodeid(), start, size, addresses, thisIndex);
                        if (addresses.size() >= size) {
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
    public static int ownerSize(long owner) {
        try ( Connection conn = DerbyBase.getConnection()) {
            return ownerSize(conn, owner);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return 0;
        }
    }

    public static int ownerSize(Connection conn, long owner) {
        int size = 0;
        try ( PreparedStatement statement = conn.prepareStatement(OwnerSize)) {
            statement.setLong(1, owner);
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

    public static int withSubSize(TableTree tableTree, long owner) {
        int count = 0;
        try ( Connection conn = DerbyBase.getConnection()) {
            count = withSubSize(conn, tableTree, owner);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return count;
    }

    public static int withSubSize(Connection conn, TableTree tableTree, long owner) {
        int count = ownerSize(conn, owner);
        if (tableTree == null) {
            tableTree = new TableTree();
        }
        List<TreeNode> children = tableTree.children(conn, owner);
        if (children != null) {
            for (TreeNode child : children) {
                count += withSubSize(conn, tableTree, child.getNodeid());
            }
        }
        return count;
    }

}
