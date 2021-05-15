package mara.mybox.db.table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Note;
import mara.mybox.db.data.Notebook;
import mara.mybox.db.data.Tag;
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
        orderColumns = "update_time DESC";
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

    public static final String Times
            = "SELECT DISTINCT update_time FROM Note ORDER BY update_time DESC";

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

    public List<Note> withSub(Connection conn, TableNotebook tableNotebook, long bookid) {
        List<Note> notes = new ArrayList<>();
        List<Note> bookNotes = notes(conn, bookid);
        if (bookNotes != null && !bookNotes.isEmpty()) {
            notes.addAll(notes);
        }
        if (tableNotebook == null) {
            tableNotebook = new TableNotebook();
        }
        List<Notebook> children = tableNotebook.children(conn, bookid);
        if (children != null) {
            for (Notebook child : children) {
                bookNotes = withSub(conn, tableNotebook, child.getNbid());
                if (bookNotes != null && !bookNotes.isEmpty()) {
                    notes.addAll(notes);
                }
            }
        }
        return notes;
    }

    public List<Note> withSub(TableNotebook tableNotebook, long bookid, int start, int size) {
        List<Note> notes = new ArrayList<>();
        try ( Connection conn = DerbyBase.getConnection();
                 PreparedStatement qNotes = conn.prepareStatement(QueryNotebook)) {
            if (tableNotebook == null) {
                tableNotebook = new TableNotebook();
            }
            withSub(conn, qNotes, tableNotebook, bookid, start, size, notes, 0);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return notes;
    }

    public int withSub(Connection conn, PreparedStatement qNotes,
            TableNotebook tableNotebook, long bookid, int start, int size, List<Note> notes, int index) {
        if (conn == null || bookid < 1 || notes == null || tableNotebook == null
                || qNotes == null || start < 0 || size <= 0 || notes.size() >= size) {
            return index;
        }
        int thisIndex = index;
        try {
            int thisSize = notes.size();
            boolean ok = false;
            qNotes.setLong(1, bookid);
            try ( ResultSet nresults = qNotes.executeQuery()) {
                while (nresults.next()) {
                    Note data = readData(nresults);
                    if (data != null) {
                        if (thisIndex >= start) {
                            notes.add(data);
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
                List<Notebook> children = tableNotebook.children(conn, bookid);
                if (children != null) {
                    for (Notebook child : children) {
                        thisIndex = withSub(conn, qNotes, tableNotebook, child.getNbid(), start, size, notes, thisIndex);
                        if (notes.size() >= size) {
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
    public static int bookSize(long notebook) {
        try ( Connection conn = DerbyBase.getConnection()) {
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

    public static int withSubSize(TableNotebook tableNotebook, long bookid) {
        int count = 0;
        try ( Connection conn = DerbyBase.getConnection()) {
            count = withSubSize(conn, tableNotebook, bookid);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return count;
    }

    public static int withSubSize(Connection conn, TableNotebook tableNotebook, long bookid) {
        int count = bookSize(conn, bookid);
        if (tableNotebook == null) {
            tableNotebook = new TableNotebook();
        }
        List<Notebook> children = tableNotebook.children(conn, bookid);
        if (children != null) {
            for (Notebook child : children) {
                count += withSubSize(conn, tableNotebook, child.getNbid());
            }
        }
        return count;
    }

    public static String tagsCondition(List<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        String condition = "ntid IN ( SELECT noteid FROM Note_Tag WHERE tagid IN ( " + tags.get(0).getTgid();
        for (int i = 1; i < tags.size(); ++i) {
            condition += ", " + tags.get(i).getTgid();
        }
        condition += " ) )";
        return condition;
    }

    public static List<Date> times() {
        try ( Connection conn = DerbyBase.getConnection()) {
            conn.setReadOnly(true);
            return times(conn);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static List<Date> times(Connection conn) {
        List<Date> times = new ArrayList();
        if (conn == null) {
            return times;
        }
        try ( PreparedStatement statement = conn.prepareStatement(Times);
                 ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                Date time = results.getTimestamp("update_time");
                if (time != null) {
                    times.add(time);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return times;
    }

}
