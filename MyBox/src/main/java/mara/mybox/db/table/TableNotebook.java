package mara.mybox.db.table;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.List;
import static mara.mybox.db.DerbyBase.dbHome;
import static mara.mybox.db.DerbyBase.login;
import static mara.mybox.db.DerbyBase.protocol;
import mara.mybox.db.data.Notebook;
import mara.mybox.db.table.ColumnDefinition.ColumnType;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.AppVariables.message;

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
        addColumn(new ColumnDefinition("name", ColumnType.String, true).setLength(256));
        addColumn(new ColumnDefinition("description", ColumnType.String).setLength(4096));
        addColumn(new ColumnDefinition("owner", ColumnType.Long)
                .setForeignName("Notebook_owner_fk").setForeignTable("Notebook").setForeignColumn("nbid")
                .setOnDelete(ColumnDefinition.OnDelete.Cascade)
        );
        return this;
    }

    public static final String QueryID
            = "SELECT * FROM Notebook WHERE nbid=?";

    public static final String QueryName
            = "SELECT * FROM Notebook WHERE name=?";

    public static final String QueryChildren
            = "SELECT * FROM Notebook WHERE owner=? AND nbid > 1";

    public static final String QueryChild
            = "SELECT * FROM Notebook WHERE owner=? AND name=?";

    public static final String DeleteBook
            = "DELETE FROM Notebook WHERE nbid=?";

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
        if (id < 1) {
            return null;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            return children(conn, id);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<Notebook> children(Connection conn, long id) {
        if (conn == null || id < 1) {
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

    public Notebook find(long owner, String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
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
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
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
            String[] nodes = ownerChain.split(Notebook.NotebooksSeparater);
            long ownerid = 1;
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
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
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
                String sql = "INSERT INTO Notebook(nbid, name,owner) VALUES(1, '" + message("Notebook") + "', 1)";
                update(conn, sql);
                conn.commit();
                sql = "ALTER TABLE Notebook ALTER COLUMN nbid RESTART WITH 2";
                update(conn, sql);
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
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setAutoCommit(true);
//            String sql = "DROP TABLE Note_Tag";
//            update(conn, sql);
            String sql = "DROP TABLE Note";
            update(conn, sql);
            sql = "DROP TABLE Notebook";
            update(conn, sql);

            createTable(conn);
            new TableNote().createTable(conn);

            return checkRoot(conn);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
