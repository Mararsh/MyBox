package mara.mybox.db.table;

import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.ShapeDescription;

/**
 * @Author Mara
 * @CreateDate 2022-4-24
 * @License Apache License Version 2.0
 */
public class TableShapeDescription extends BaseTable<ShapeDescription> {

    public TableShapeDescription() {
        tableName = "Shape_Description";
        defineColumns();
    }

    public TableShapeDescription(boolean defineColumns) {
        tableName = "Shape_Description";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableShapeDescription defineColumns() {
        addColumn(new ColumnDefinition("sdid", ColumnType.Long, true, true).setAuto(true));
        addColumn(new ColumnDefinition("shape", ColumnType.String, true).setLength(128));
        addColumn(new ColumnDefinition("title", ColumnType.String, true).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("descpriton", ColumnType.String, true).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("more", ColumnType.String).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("update_time", ColumnType.Datetime));
        orderColumns = "update_time DESC";
        return this;
    }

}
