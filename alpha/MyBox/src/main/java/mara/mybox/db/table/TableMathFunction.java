package mara.mybox.db.table;

import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.MathFunction;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class TableMathFunction extends BaseDataTable<MathFunction> {

    public TableMathFunction() {
        tableName = "Math_Function";
        idColumnName = "funcid";
        defineColumns();
    }

    public final TableMathFunction defineColumns() {
        addColumn(new ColumnDefinition("funcid", ColumnType.Long, true, true).setAuto(true));
        addColumn(new ColumnDefinition("name", ColumnType.String, true));
        addColumn(new ColumnDefinition("variables", ColumnType.String));
        addColumn(new ColumnDefinition("expression", ColumnType.Clob));
        addColumn(new ColumnDefinition("domain", ColumnType.Clob));
        return this;
    }

    @Override
    public boolean setValue(MathFunction data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        return data.setValue(column, value);
    }

    @Override
    public Object getValue(MathFunction data, String column) {
        if (data == null || column == null) {
            return null;
        }
        return data.getValue(column);
    }

    @Override
    public boolean valid(MathFunction data) {
        if (data == null) {
            return false;
        }
        return data.valid();
    }
}
