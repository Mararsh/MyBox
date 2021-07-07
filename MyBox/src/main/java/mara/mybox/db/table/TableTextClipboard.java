package mara.mybox.db.table;

import mara.mybox.db.data.TextClipboard;
import mara.mybox.db.table.ColumnDefinition.ColumnType;

/**
 * @Author Mara
 * @CreateDate 2021-7-3
 * @License Apache License Version 2.0
 */
public class TableTextClipboard extends BaseTable<TextClipboard> {

    public TableTextClipboard() {
        tableName = "Text_Clipboard";
        defineColumns();
    }

    public TableTextClipboard(boolean defineColumns) {
        tableName = "Text_Clipboard";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableTextClipboard defineColumns() {
        addColumn(new ColumnDefinition("tcid", ColumnType.Long, true, true).setIsID(true));
        addColumn(new ColumnDefinition("text", ColumnType.String, true).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("create_time", ColumnType.Datetime));
        orderColumns = "create_time DESC";
        return this;
    }

}
