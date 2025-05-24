package mara.mybox.db.table;

import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class TableNodeMathFunction extends BaseNodeTable {

    public TableNodeMathFunction() {
        tableName = "Node_Math_Function";
        treeName = message("MathFunction");
        dataName = message("MathFunction");
        tableTitle = message("MathFunction");
        dataFxml = Fxmls.ControlDataMathFunctionFxml;
        examplesFileName = "MathFunction";
        majorColumnName = "expression";
        defineColumns();
    }

    public final TableNodeMathFunction defineColumns() {
        defineNodeColumns();
        addColumn(new ColumnDefinition("variables", ColumnType.String)
                .setLabel(message("Variables")));
        addColumn(new ColumnDefinition("expression", ColumnType.Clob)
                .setLabel(message("Expression")));
        addColumn(new ColumnDefinition("domain", ColumnType.Clob)
                .setLabel(message("FunctionDomain")));
        return this;
    }

}
