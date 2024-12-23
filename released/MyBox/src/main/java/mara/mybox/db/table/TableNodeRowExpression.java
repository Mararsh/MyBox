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
public class TableNodeRowExpression extends BaseNodeTable {

    public TableNodeRowExpression() {
        tableName = "Node_Row_Expression";
        treeName = message("RowExpression");
        dataName = message("RowExpression");
        dataFxml = Fxmls.ControlDataRowExpressionFxml;
        examplesFileName = "RowExpression";
        defineColumns();
    }

    public final TableNodeRowExpression defineColumns() {
        defineNodeColumns();
        addColumn(new ColumnDefinition("script", ColumnType.Clob));
        return this;
    }

    @Override
    public String label(String name) {
        if (name == null || name.isBlank()) {
            return name;
        }
        switch (name) {
            case "script":
                return message("RowExpression");
        }
        return super.label(name);
    }

}
