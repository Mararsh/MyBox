package mara.mybox.db.table;

import mara.mybox.db.data.Matrix;
import mara.mybox.db.table.ColumnDefinition.ColumnType;

/**
 * @Author Mara
 * @CreateDate 2020-12-23
 * @License Apache License Version 2.0
 */
public class TableUserTable extends BaseTable<Matrix> {

    public TableUserTable() {
        tableName = "User_Table";
        defineColumns();
    }

    public TableUserTable(boolean defineColumns) {
        tableName = "User_Table";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableUserTable defineColumns() {
        addColumn(new ColumnDefinition("table_name", ColumnType.String, true, true).setLength(1024));
        addColumn(new ColumnDefinition("column_name", ColumnType.String, true, true).setLength(1024));
        addColumn(new ColumnDefinition("is_primary_key", ColumnType.Boolean, true));
        addColumn(new ColumnDefinition("not_null", ColumnType.Boolean, true));
        addColumn(new ColumnDefinition("is_id", ColumnType.Boolean, true));
        addColumn(new ColumnDefinition("column_type", ColumnType.Short, true));
        addColumn(new ColumnDefinition("time_format", ColumnType.Short));
        addColumn(new ColumnDefinition("length", ColumnType.Integer));
        addColumn(new ColumnDefinition("default_value", ColumnType.String).setLength(1024));
        addColumn(new ColumnDefinition("max_value", ColumnType.Double));
        addColumn(new ColumnDefinition("min_value", ColumnType.Double));
        addColumn(new ColumnDefinition("values", ColumnType.String).setLength(32672));
        return this;
    }

}
