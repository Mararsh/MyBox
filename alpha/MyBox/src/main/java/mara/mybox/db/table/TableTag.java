package mara.mybox.db.table;

import java.util.List;
import mara.mybox.db.data.Tag;
import mara.mybox.db.table.ColumnDefinition.ColumnType;

/**
 * @Author Mara
 * @CreateDate 2021-3-3
 * @License Apache License Version 2.0
 */
public class TableTag extends BaseTable<Tag> {

    public TableTag() {
        tableName = "Tag";
        defineColumns();
    }

    public TableTag(boolean defineColumns) {
        tableName = "Tag";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableTag defineColumns() {
        addColumn(new ColumnDefinition("tgid", ColumnType.Long, true, true).setIsID(true));
        addColumn(new ColumnDefinition("tag", ColumnType.String, true).setLength(2048));
        return this;
    }

    public static final String Create_Unique_Index
            = "CREATE UNIQUE INDEX Tag_unique_index on Tag (  tag )";

    public static final String QueryID
            = "SELECT * FROM Tag WHERE tgid=?";

    public static final String QueryTag
            = "SELECT * FROM Tag WHERE tag=?";

    @Override
    public List<Tag> readAll() {
        return readData("SELECT * FROM " + tableName + " ORDER BY tag");
    }

}
