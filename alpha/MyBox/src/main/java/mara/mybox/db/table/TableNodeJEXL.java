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
public class TableNodeJEXL extends BaseNodeTable {

    public TableNodeJEXL() {
        tableName = "Node_JEXL";
        treeName = message("JEXL");
        dataName = message("JexlScript");
        dataFxml = Fxmls.ControlDataJEXLFxml;
        examplesFileName = "JEXL";
        defineColumns();
    }

    public final TableNodeJEXL defineColumns() {
        defineNodeColumns();
        addColumn(new ColumnDefinition("script", ColumnType.Clob));
        addColumn(new ColumnDefinition("context", ColumnType.Clob));
        addColumn(new ColumnDefinition("parameters", ColumnType.String).setLength(FilenameMaxLength));
        return this;
    }

    @Override
    public String label(String name) {
        if (name == null || name.isBlank()) {
            return name;
        }
        switch (name) {
            case "script":
                return message("JexlScript");
            case "context":
                return message("JexlContext");
            case "parameters":
                return message("JexlParamters");
        }
        return name;
    }

}
