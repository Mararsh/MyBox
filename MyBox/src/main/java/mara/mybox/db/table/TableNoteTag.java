package mara.mybox.db.table;

import mara.mybox.db.data.NoteTag;
import mara.mybox.db.table.ColumnDefinition.ColumnType;

/**
 * @Author Mara
 * @CreateDate 2021-3-3
 * @License Apache License Version 2.0
 */
public class TableNoteTag extends BaseTable<NoteTag> {

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

    public static final String QueryID
            = "SELECT * FROM Note_Tag WHERE ngid=?";

    public static final String QueryNote
            = "SELECT * FROM Note_Tag WHERE noteid=?";

    public static final String QueryTag
            = "SELECT * FROM Note_Tag WHERE tagid=?";

    public static final String QueryNoteTag
            = "SELECT * FROM Note_Tag WHERE noteid=? AND tagid=?";

}
