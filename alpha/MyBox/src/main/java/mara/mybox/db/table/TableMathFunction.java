package mara.mybox.db.table;

import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.MathFunction;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class TableMathFunction extends BaseTable<MathFunction> {

    public TableMathFunction() {
        tableName = "Math_Function";
        defineColumns();
    }

    public TableMathFunction(boolean defineColumns) {
        tableName = "Math_Function";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableMathFunction defineColumns() {
        addColumn(new ColumnDefinition("funcid", ColumnType.Long, true, true).setAuto(true));
        addColumn(new ColumnDefinition("name", ColumnType.String, true));
        addColumn(new ColumnDefinition("variables", ColumnType.String));
        addColumn(new ColumnDefinition("expression", ColumnType.Clob));
        addColumn(new ColumnDefinition("domain", ColumnType.Clob));
        return this;
    }

}
