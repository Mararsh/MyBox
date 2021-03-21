package mara.mybox.db.table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Note;
import mara.mybox.db.data.NoteTag;
import mara.mybox.db.data.Tag;
import mara.mybox.db.table.ColumnDefinition.ColumnType;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-3-3
 * @License Apache License Version 2.0
 */
public class TableNoteTag extends BaseTable<NoteTag> {

    protected TableNote tableNote;
    protected TableTag tableTag;

    public TableNoteTag() {
        tableName = "Note_Tag";
        defineColumns();
    }

    public TableNoteTag(boolean defineColumns) {
        tableName = "Note_Tag";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableNoteTag defineColumns() {
        addColumn(new ColumnDefinition("ngid", ColumnType.Long, true, true).setIsID(true));
        addColumn(new ColumnDefinition("noteid", ColumnType.Long)
                .setForeignName("Note_Tag_note_fk").setForeignTable("Note").setForeignColumn("ntid")
                .setOnDelete(ColumnDefinition.OnDelete.Cascade)
        );
        addColumn(new ColumnDefinition("tagid", ColumnType.Long)
                .setForeignName("Note_Tag_tag_fk").setForeignTable("Tag").setForeignColumn("tgid")
                .setOnDelete(ColumnDefinition.OnDelete.Cascade)
        );
        return this;
    }

    public static final String Create_Unique_Index
            = "CREATE UNIQUE INDEX Note_Tag_unique_index on Note_Tag (  noteid , tagid  )";

    public static final String QueryNoteTags
            = "SELECT * FROM Note_Tag WHERE noteid=?";

    public static final String DeleteNoteTags
            = "DELETE FROM Note_Tag WHERE noteid=?";

    @Override
    public Object readForeignValue(ResultSet results, String column) {
        if (results == null || column == null) {
            return null;
        }
        try {
            if ("noteid".equals(column) && results.findColumn("ntid") > 0) {
                return getTableNote().readData(results);
            }
            if ("tagid".equals(column) && results.findColumn("tgid") > 0) {
                return getTableTag().readData(results);
            }
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public boolean setForeignValue(NoteTag data, String column, Object value) {
        if (data == null || column == null || value == null) {
            return true;
        }
        if ("noteid".equals(column) && value instanceof Note) {
            data.setNote((Note) value);
        }
        if ("tagid".equals(column) && value instanceof Tag) {
            data.setTag((Tag) value);
        }
        return true;
    }

    public List<Note> readNotes(List<Tag> tags) {
        String sql = "SELECT ntid, notebook,title, update_time, html FROM Note WHERE "
                + "ntid IN ( SELECT noteid FROM Note_Tag WHERE tagid IN ( " + tags.get(0).getTgid();
        for (int i = 1; i < tags.size(); ++i) {
            sql += ", " + tags.get(i).getTgid();
        }
        sql += " ) )";
        return getTableNote().readData(sql);
    }

    public List<Long> readTags(long noteid) {
        List<Long> tags = new ArrayList();
        try ( Connection conn = DerbyBase.getConnection();
                 PreparedStatement statement = conn.prepareStatement(QueryNoteTags)) {
            statement.setLong(1, noteid);
            try ( ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    NoteTag data = readData(results);
                    tags.add(data.getTagid());
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return tags;
    }

    public List<Long> deleteTags(long noteid) {
        List<Long> tags = new ArrayList();
        try ( Connection conn = DerbyBase.getConnection();
                 PreparedStatement statement = conn.prepareStatement(QueryNoteTags)) {
            statement.setLong(1, noteid);
            try ( ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    NoteTag data = readData(results);
                    tags.add(data.getTagid());
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return tags;
    }


    /*
        get/set
     */
    public TableNote getTableNote() {
        if (tableNote == null) {
            tableNote = new TableNote();
        }
        return tableNote;
    }

    public void setTableNote(TableNote tableNote) {
        this.tableNote = tableNote;
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
