package mara.mybox.db.table;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import static mara.mybox.db.DerbyBase.dbHome;
import static mara.mybox.db.DerbyBase.login;
import static mara.mybox.db.DerbyBase.protocol;
import mara.mybox.db.data.Note;
import mara.mybox.db.table.ColumnDefinition.ColumnType;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-2-28
 * @License Apache License Version 2.0
 */
public class TableNote extends BaseTable<Note> {

    public TableNote() {
        tableName = "Note";
        defineColumns();
    }

    public TableNote(boolean defineColumns) {
        tableName = "Note";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableNote defineColumns() {
        addColumn(new ColumnDefinition("ntid", ColumnType.Long, true, true).setIsID(true));
        addColumn(new ColumnDefinition("notebook", ColumnType.Long)
                .setForeignName("Notes_notebook_fk").setForeignTable("Notebook").setForeignColumn("nbid")
                .setOnDelete(ColumnDefinition.OnDelete.Cascade)
        );
        addColumn(new ColumnDefinition("title", ColumnType.String, true).setLength(256));
        addColumn(new ColumnDefinition("update_time", ColumnType.Datetime, true));
        addColumn(new ColumnDefinition("html", ColumnType.String).setLength(32672));
        return this;
    }

    public static final String Create_Time_Index
            = "CREATE INDEX Note_time_index on Note (  update_time )";

    public static final String QueryID
            = "SELECT * FROM Note WHERE ntid=?";

    public static final String QueryNotebook
            = "SELECT * FROM Note WHERE notebook=?";

    public static final String QueryTitle
            = "SELECT * FROM Note WHERE notebook=? AND title=?";

    public static final String QueryBookSize
            = "SELECT COUNT(ntid) FROM Note WHERE notebook=?";

    public static final String DeleteBookNotes
            = "DELETE FROM Note WHERE notebook=?";

    public static final String DeleteNote
            = "DELETE FROM Note WHERE ntid=?";

    public Note find(Connection conn, long id) {
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

    public Note find(Connection conn, long notebook, String title) {
        if (conn == null) {
            return null;
        }
        try ( PreparedStatement statement = conn.prepareStatement(QueryTitle)) {
            statement.setLong(1, notebook);
            statement.setString(2, title);
            return query(conn, statement);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<Note> notes(long notebook) {
        if (notebook < 1) {
            return null;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            return notes(conn, notebook);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<Note> notes(Connection conn, long notebook) {
        if (conn == null || notebook < 1) {
            return null;
        }
        try ( PreparedStatement statement = conn.prepareStatement(QueryNotebook)) {
            statement.setLong(1, notebook);
            return query(statement);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<Note> queryBook(long notebook, int start, int size) {
        if (start < 0 || size <= 0) {
            return new ArrayList<>();
        }
        String sql = "SELECT * FROM " + tableName
                + " WHERE notebook=" + notebook
                + " OFFSET " + start + " ROWS FETCH NEXT " + size + " ROWS ONLY";
        return readData(sql);
    }

    public int clearBook(long notebook) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            return clearBook(conn, notebook);
        } catch (Exception e) {
            MyBoxLog.error(e, tableName);
            return -1;
        }
    }

    public int clearBook(Connection conn, long notebook) {
        if (conn == null) {
            return -1;
        }
        try ( PreparedStatement statement = conn.prepareStatement(DeleteBookNotes)) {
            statement.setLong(1, notebook);
            return statement.executeUpdate();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return -1;
        }
    }

    public static int bookSize(long notebook) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            return bookSize(conn, notebook);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return 0;
        }
    }

    public static int bookSize(Connection conn, long notebook) {
        int size = 0;
        try ( PreparedStatement statement = conn.prepareStatement(QueryBookSize)) {
            statement.setLong(1, notebook);
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

}
