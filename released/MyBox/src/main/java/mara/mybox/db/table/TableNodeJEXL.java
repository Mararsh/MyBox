package mara.mybox.db.table;

import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.DataNode;
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
        majorColumnName = "script";
        nodeExecutable = true;
        defineColumns();
    }

    public final TableNodeJEXL defineColumns() {
        defineNodeColumns();
        addColumn(new ColumnDefinition("script", ColumnType.Clob)
                .setLabel(message("JexlScript")));
        addColumn(new ColumnDefinition("context", ColumnType.Clob)
                .setLabel(message("JexlContext")));
        addColumn(new ColumnDefinition("parameters", ColumnType.String)
                .setLength(FilenameMaxLength)
                .setLabel(message("JexlParamters")));
        return this;
    }

    @Override
    public boolean isNodeExecutable(DataNode node) {
        if (node == null) {
            return false;
        }
        String script = node.getStringValue("script");
        return script != null && !script.isBlank();
    }

}
