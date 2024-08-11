package mara.mybox.db.table;

import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.Note;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class TableNote extends BaseTable<Note> {

    public TableNote() {
        tableName = "Note";
        tableTitle = message("Notes");
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
        addColumn(new ColumnDefinition("title", ColumnType.String).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("note", ColumnType.Clob));
        return this;
    }

    @Override
    public boolean setValue(Note data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        return data.setValue(column, value);
    }

    @Override
    public Object getValue(Note data, String column) {
        if (data == null || column == null) {
            return null;
        }
        return data.getValue(column);
    }

}
