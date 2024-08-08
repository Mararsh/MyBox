package mara.mybox.db.table;

import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.Note;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class TableNote extends BaseTable<Note> {

    public TableNote() {
        tableName = "Note";
        defineColumns();
    }

    public TableNote(boolean defineColumns) {
        tableName = "Note";
        idColumnName = "noteid";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableNote defineColumns() {
        addColumn(new ColumnDefinition("noteid", ColumnType.Long, true, true).setAuto(true));
        addColumn(new ColumnDefinition("note", ColumnType.Clob));
        return this;
    }

}
