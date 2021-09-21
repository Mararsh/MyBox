package mara.mybox.db.table;

import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Matrix;
import mara.mybox.db.data.ColumnDefinition.ColumnType;

/**
 * @Author Mara
 * @CreateDate 2020-12-15
 * @License Apache License Version 2.0
 */
public class TableMatrix extends BaseTable<Matrix> {

    public TableMatrix() {
        tableName = "Matrix";
        defineColumns();
    }

    public TableMatrix(boolean defineColumns) {
        tableName = "Matrix";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableMatrix defineColumns() {
        addColumn(new ColumnDefinition("mxid", ColumnType.Long, true, true).setIsID(true));
        addColumn(new ColumnDefinition("name", ColumnType.String, true).setLength(1024));
        addColumn(new ColumnDefinition("columns_number", ColumnType.Integer, true).setMinValue(1));
        addColumn(new ColumnDefinition("rows_number", ColumnType.Integer, true).setMinValue(1));
        addColumn(new ColumnDefinition("scale", ColumnType.Short, true).setMinValue(0));
        addColumn(new ColumnDefinition("modify_time", ColumnType.Datetime));
        addColumn(new ColumnDefinition("comments", ColumnType.String).setLength(32672));
        return this;
    }

}
