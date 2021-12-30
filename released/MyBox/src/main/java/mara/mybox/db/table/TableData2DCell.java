package mara.mybox.db.table;

import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.Data2DCell;

/**
 * @Author Mara
 * @CreateDate 2021-10-17
 * @License Apache License Version 2.0
 */
public class TableData2DCell extends BaseTable<Data2DCell> {

    public TableData2DCell() {
        tableName = "Data2D_Cell";
        defineColumns();
    }

    public TableData2DCell(boolean defineColumns) {
        tableName = "Data2D_Cell";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableData2DCell defineColumns() {
        addColumn(new ColumnDefinition("dceid", ColumnType.Long, true, true).setIsID(true));
        addColumn(new ColumnDefinition("dcdid", ColumnType.Long, true)
                .setForeignName("Data2D_Cell_fk").setForeignTable("Data2D_Definition").setForeignColumn("d2did")
                .setOnDelete(ColumnDefinition.OnDelete.Cascade));
        addColumn(new ColumnDefinition("row", ColumnType.Long, true).setMinValue(0));
        addColumn(new ColumnDefinition("col", ColumnType.Long, true).setMinValue(0));
        addColumn(new ColumnDefinition("value", ColumnType.String, true).setLength(StringMaxLength));
        return this;
    }

    public static final String QueryData
            = "SELECT * FROM Data2D_Cell WHERE dcdid=?";

    public static final String ClearData
            = "DELETE FROM Data2D_Cell WHERE dcdid=?";

}
