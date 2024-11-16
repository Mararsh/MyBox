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
public class TableMathFunction extends BaseNodeTable {

    public TableMathFunction() {
        tableName = "Math_Function";
        tableTitle = message("Notes");
        fxml = Fxmls.ControlDataNoteFxml;
        examplesFile = exampleFile();
        defineColumns();
    }

    public final TableMathFunction defineColumns() {
        defineNodeColumns();
        addColumn(new ColumnDefinition("variables", ColumnType.String));
        addColumn(new ColumnDefinition("expression", ColumnType.Clob));
        addColumn(new ColumnDefinition("domain", ColumnType.Clob));
        return this;
    }

}
