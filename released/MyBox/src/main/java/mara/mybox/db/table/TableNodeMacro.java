package mara.mybox.db.table;

import java.sql.Connection;
import mara.mybox.controller.BaseController;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.DataNode;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2025-9-1
 * @License Apache License Version 2.0
 */
public class TableNodeMacro extends BaseNodeTable {

    public TableNodeMacro() {
        tableName = "Node_Macro";
        treeName = message("MacroCommands");
        dataName = message("MacroCommands");
        dataFxml = Fxmls.ControlDataMacroFxml;
        examplesFileName = "Macro";
        majorColumnName = "script";
        nodeExecutable = true;
        defineColumns();
    }

    public final TableNodeMacro defineColumns() {
        defineNodeColumns();
        addColumn(new ColumnDefinition("script", ColumnType.Clob)
                .setLabel(message("MacroCommands")));
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

    @Override
    public String valuesHtml(FxTask task, Connection conn, BaseController controller, DataNode node) {
        try {
            String text = node.getStringValue("script");
            return text == null || text.isBlank() ? null : HtmlWriteTools.codeToHtml(text);
        } catch (Exception e) {
            return null;
        }
    }
}
