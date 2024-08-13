package mara.mybox.db.table;

import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.WebFavorite;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class TableWebFavorite extends BaseTable<WebFavorite> {

    public TableWebFavorite() {
        tableName = "Web_Favorite";
        defineColumns();
    }

    public TableWebFavorite(boolean defineColumns) {
        tableName = "Web_Favorite";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableWebFavorite defineColumns() {
        addColumn(new ColumnDefinition("addrid", ColumnType.Long, true, true).setAuto(true));
        addColumn(new ColumnDefinition("address", ColumnType.File, true).setLength(FilenameMaxLength));
        addColumn(new ColumnDefinition("icon", ColumnType.File).setLength(FilenameMaxLength));
        return this;
    }

    @Override
    public boolean setValue(WebFavorite data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        return data.setValue(column, value);
    }

    @Override
    public Object getValue(WebFavorite data, String column) {
        if (data == null || column == null) {
            return null;
        }
        return data.getValue(column);
    }

    @Override
    public boolean valid(WebFavorite data) {
        if (data == null) {
            return false;
        }
        return data.valid();
    }

}
