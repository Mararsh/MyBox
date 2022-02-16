package mara.mybox.db.table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.Notebook;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-2-28
 * @License Apache License Version 2.0
 */
public class TableNotebook extends BaseTable<Notebook> {

    public TableNotebook() {
        tableName = "Notebook";
        defineColumns();
    }

    public TableNotebook(boolean defineColumns) {
        tableName = "Notebook";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableNotebook defineColumns() {
        addColumn(new ColumnDefinition("nbid", ColumnType.Long, true, true).setIsID(true));
        addColumn(new ColumnDefinition("name", ColumnType.String, true).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("description", ColumnType.String).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("owner", ColumnType.Long)
                .setReferName("Notebook_owner_fk").setReferTable("Notebook").setReferColumn("nbid")
                .setOnDelete(ColumnDefinition.OnDelete.Cascade)
        );
        return this;
    }

    public static final String Create_Owner_Index
            = "CREATE INDEX Notebook_owner_index on Notebook (  owner )";

    public static final String QueryID
            = "SELECT * FROM Notebook WHERE nbid=?";

    public static final String QueryName
            = "SELECT * FROM Notebook WHERE name=?";

    public static final String QueryChildren
            = "SELECT * FROM Notebook WHERE owner=? AND nbid>" + Notebook.RootID;

    public static final String QueryChild
            = "SELECT * FROM Notebook WHERE owner=? AND name=?";

    public static final String DeleteBook
            = "DELETE FROM Notebook WHERE nbid=?";

    public Notebook find(long id) {
        if (id < Notebook.RootID) {
            return null;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            return find(conn, id);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public Notebook find(Connection conn, long id) {
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

    public List<Notebook> children(long id) {
        if (id < Notebook.RootID) {
            return null;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            return children(conn, id);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<Notebook> children(Connection conn, long id) {
        if (conn == null || id < Notebook.RootID) {
            return null;
        }
        try ( PreparedStatement statement = conn.prepareStatement(QueryChildren)) {
            statement.setLong(1, id);
            return query(statement);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<Notebook> ancestor(long id) {
        if (id <= Notebook.RootID) {
            return null;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            return ancestor(conn, id);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<Notebook> ancestor(Connection conn, long id) {
        if (conn == null || id <= Notebook.RootID) {
            return null;
        }
        List<Notebook> ancestor = null;
        Notebook book = find(conn, id);
        if (book == null) {
            return ancestor;
        }
        long parentid = book.getOwner();
        Notebook parent = find(conn, parentid);
        if (parent != null) {
            ancestor = ancestor(conn, parentid);
            if (ancestor == null) {
                ancestor = new ArrayList<>();
            }
            ancestor.add(parent);
        }
        return ancestor;
    }

    public Notebook find(long owner, String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            return find(conn, owner, name);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public Notebook find(Connection conn, long owner, String name) {
        if (conn == null || name == null || name.isBlank()) {
            return null;
        }
        try ( PreparedStatement statement = conn.prepareStatement(QueryChild)) {
            statement.setLong(1, owner);
            statement.setString(2, name);
            return query(conn, statement);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public Notebook findAndCreate(long owner, String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            return findAndCreate(conn, owner, name);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public Notebook findAndCreate(Connection conn, long owner, String name) {
        if (conn == null || name == null || name.isBlank()) {
            return null;
        }
        try {
            Notebook notebook = find(conn, owner, name);
            if (notebook == null) {
                notebook = new Notebook(owner, name);
                notebook = insertData(conn, notebook);
                conn.commit();
            }
            return notebook;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public Notebook findAndCreateChain(Connection conn, String ownerChain) {
        if (conn == null || ownerChain == null || ownerChain.isBlank()) {
            return null;
        }
        try {
            String[] nodes = ownerChain.split(Notebook.NotebookNameSeparater);
            long ownerid = Notebook.RootID;
            Notebook owner = null;
            for (String node : nodes) {
                owner = findAndCreate(conn, ownerid, node);
                if (owner == null) {
                    return null;
                }
                ownerid = owner.getNbid();
            }
            return owner;
        } catch (Exception e) {
            MyBoxLog.console(e);
            return null;
        }
    }

    public Notebook checkRoot() {
        try ( Connection conn = DerbyBase.getConnection()) {
            return checkRoot(conn);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public Notebook checkRoot(Connection conn) {
        if (conn == null) {
            return null;
        }
        Notebook root = find(conn, 1);
        if (root == null) {
            try {
                String sql = "INSERT INTO Notebook(nbid, name,owner) VALUES("
                        + Notebook.RootID + ", '" + Languages.message("Notebook") + "', " + Notebook.RootID + ")";
                update(conn, sql);
                conn.commit();
                sql = "ALTER TABLE Notebook ALTER COLUMN nbid RESTART WITH " + (Notebook.RootID + 1);
                update(conn, sql);
                conn.commit();
                return find(conn, 1);
            } catch (Exception e) {
                MyBoxLog.error(e);
                return null;
            }
        } else {
            return root;
        }
    }

    public Notebook clear() {
        try ( Connection conn = DerbyBase.getConnection()) {
            return clear(conn);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public Notebook clear(Connection conn) {
        try ( Statement statement = conn.createStatement()) {
            conn.setAutoCommit(true);
            String sql = "DROP TABLE Note_Tag";
            update(conn, sql);
            sql = "DROP TABLE Note";
            update(conn, sql);
            sql = "DROP TABLE Notebook";
            update(conn, sql);

            createTable(conn);
            new TableNote().createTable(conn);
            new TableNoteTag().createTable(conn);

            statement.executeUpdate(TableNote.Create_Time_Index);
            statement.executeUpdate(TableNoteTag.Create_Unique_Index);

            return checkRoot(conn);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
