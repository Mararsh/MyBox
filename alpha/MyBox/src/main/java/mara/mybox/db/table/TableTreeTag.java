package mara.mybox.db.table;

import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.TreeTag;

/**
 * @Author Mara
 * @CreateDate 2021-3-3
 * @License Apache License Version 2.0
 */
public class TableTreeTag extends BaseTable<TreeTag> {

    protected BaseTable dataTable;

    public TableTreeTag(BaseTable data) {
        dataTable = data;
        if (dataTable == null) {
            return;
        }
        tableName = dataTable.tableName + "_Tree_Tag";
        idColumnName = "tagid";
        defineColumns();
    }

    public final TableTreeTag defineColumns() {
        addColumn(new ColumnDefinition("tagid", ColumnType.Long, true, true).setAuto(true));
        addColumn(new ColumnDefinition("tag", ColumnType.String, true).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("color", ColumnType.Color, true));
        orderColumns = "tagid ASC";
        return this;
    }

    @Override
    public boolean setValue(TreeTag data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        return data.setValue(column, value);
    }

    @Override
    public Object getValue(TreeTag data, String column) {
        if (data == null || column == null) {
            return null;
        }
        return data.getValue(column);
    }

}
