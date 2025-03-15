package mara.mybox.db.table;

import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.MatrixCell;

/**
 * @Author Mara
 * @CreateDate 2021-10-17
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
        addColumn(new ColumnDefinition("mcdid", ColumnType.Long, true, true)
                .setReferName("Matrix_Cell_fk").setReferTable("Data2D_Definition").setReferColumn("d2did")
                .setOnDelete(ColumnDefinition.OnDelete.Cascade));
        addColumn(new ColumnDefinition("row", ColumnType.Long, true, true));
        addColumn(new ColumnDefinition("col", ColumnType.Long, true, true));
        addColumn(new ColumnDefinition("value", ColumnType.Double, true));
        orderColumns = "mcdid,row,col";
        return this;
    }

    public static final String QueryData
            = "SELECT * FROM Matrix_Cell WHERE mcdid=?";

    public static final String QueryRow
            = "SELECT * FROM Matrix_Cell WHERE mcdid=? AND row=?";

    public static final String ClearData
            = "DELETE FROM Matrix_Cell WHERE mcdid=?";

    public static final String DeleteRow
            = "DELETE FROM Matrix_Cell WHERE mcdid=? AND row=?";

    public static final String DeleteCell
            = "DELETE FROM Matrix_Cell WHERE mcdid=? AND row=? AND col=?";

    @Override
    public boolean setValue(MatrixCell data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        return data.setValue(column, value);
    }

    @Override
    public Object getValue(MatrixCell data, String column) {
        if (data == null || column == null) {
            return null;
        }
        return data.getValue(column);
    }

    @Override
    public boolean valid(MatrixCell data) {
        if (data == null) {
            return false;
        }
        return data.valid();
    }

}
