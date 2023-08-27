package mara.mybox.db.table;

import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.TreeCategory;

/**
 * @Author Mara
 * @CreateDate 2023-8-27
 * @License Apache License Version 2.0
 */
public class TableTreeCategory extends BaseTable<TreeCategory> {

    public TableTreeCategory() {
        tableName = "Tree_Category";
        defineColumns();
    }

    public TableTreeCategory(boolean defineColumns) {
        tableName = "Tree_Category";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableTreeCategory defineColumns() {
        addColumn(new ColumnDefinition("category", ColumnType.String, true, true).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("title", ColumnType.String, true).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("table_name", ColumnType.String, true).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("is_internal", ColumnType.Boolean));
        return this;
    }

    public static final String QueryCategory
            = "SELECT * FROM Tree_Category WHERE category=?";

}
