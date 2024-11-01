package mara.mybox.db.table;

import java.sql.Connection;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.DataValues;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class TableNote extends BaseDataTable<DataValues> {

    public TableNote() {
        tableName = "Note";
        tableTitle = message("Notes");
        idColumnName = "noteid";
        defineColumns();
    }

    public final TableNote defineColumns() {
        addColumn(new ColumnDefinition("noteid", ColumnType.Long, true, true).setAuto(true));
        addColumn(new ColumnDefinition("title", ColumnType.String).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("note", ColumnType.Clob));
        return this;
    }

    @Override
    public boolean setValue(DataValues data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        return data.setValue(column, value);
    }

    @Override
    public Object getValue(DataValues data, String column) {
        if (data == null || column == null) {
            return null;
        }
        return data.getValue(column);
    }

    @Override
    public boolean valid(DataValues data) {
        if (data == null) {
            return false;
        }
        return data.valid();
    }

    @Override
    public long insertData(Connection conn, String title, String note) {
        try {
            DataValues node = new DataValues();
            node.setValue("note", note);
            node.setValue("title", title);
            node = insertData(conn, node);
            return (long) node.getValue(idColumnName);
        } catch (Exception e) {
            MyBoxLog.console(e);
            return -1;
        }
    }

}
