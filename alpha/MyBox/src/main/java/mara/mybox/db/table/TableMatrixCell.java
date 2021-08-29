package mara.mybox.db.table;

import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.MatrixCell;
import mara.mybox.db.data.ColumnDefinition.ColumnType;

/**
 * @Author Mara
 * @CreateDate 2020-12-15
 * @License Apache License Version 2.0
 */
public class TableMatrixCell extends BaseTable<MatrixCell> {

    public TableMatrixCell() {
        tableName = "Matrix_Cell";
        defineColumns();
    }

    public TableMatrixCell(boolean defineColumns) {
        tableName = "Matrix_Cell";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableMatrixCell defineColumns() {
        addColumn(new ColumnDefinition("mcid", ColumnType.Long, true, true).setIsID(true));
        addColumn(new ColumnDefinition("mcxid", ColumnType.Long, true)
                .setForeignName("Matrix_Cell_fk").setForeignTable("Matrix").setForeignColumn("mxid")
                .setOnDelete(ColumnDefinition.OnDelete.Cascade));
        addColumn(new ColumnDefinition("row", ColumnType.Integer, true).setMinValue(0));
        addColumn(new ColumnDefinition("col", ColumnType.Integer, true).setMinValue(0));
        addColumn(new ColumnDefinition("value", ColumnType.Double, true));
        return this;
    }

}
