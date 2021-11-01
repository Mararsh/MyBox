package mara.mybox.db.table;

import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.DataCell;

/**
 * @Author Mara
 * @CreateDate 2021-10-17
 * @License Apache License Version 2.0
 */
public class TableDataCell extends BaseTable<DataCell> {

    public TableDataCell() {
        tableName = "Data_Cell";
        defineColumns();
    }

    public TableDataCell(boolean defineColumns) {
        tableName = "Data_Cell";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableDataCell defineColumns() {
        addColumn(new ColumnDefinition("dceid", ColumnType.Long, true, true).setIsID(true));
        addColumn(new ColumnDefinition("dcdid", ColumnType.Long, true)
                .setForeignName("Data_Cell_fk").setForeignTable("Data_Definition").setForeignColumn("dfid")
                .setOnDelete(ColumnDefinition.OnDelete.Cascade));
        addColumn(new ColumnDefinition("row", ColumnType.Long, true).setMinValue(0));
        addColumn(new ColumnDefinition("col", ColumnType.Long, true).setMinValue(0));
        addColumn(new ColumnDefinition("value", ColumnType.String, true).setLength(32672));
        return this;
    }

    public static final String QeuryData
            = "SELECT * FROM Data_Cell WHERE dcdid=?";

}
