package mara.mybox.db.table;

import java.sql.Connection;
import mara.mybox.controller.BaseController;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.DataNode;
import mara.mybox.fxml.FxTask;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class TableNodeJavaScript extends BaseNodeTable {

    public TableNodeJavaScript() {
        tableName = "Node_JavaScript";
        treeName = message("JavaScript");
        dataName = message("JavaScript");
        dataFxml = Fxmls.ControlDataJavaScriptFxml;
        examplesFileName = "JavaScript";
        majorColumnName = "script";
        nodeExecutable = true;
        defineColumns();
    }

    public final TableNodeJavaScript defineColumns() {
        defineNodeColumns();
        addColumn(new ColumnDefinition("script", ColumnType.Clob)
                .setLabel(message("JavaScript")));
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
            return text == null || text.isBlank() ? null
                    : ("<PRE><CODE>" + text + "</CODE></PRE>");
        } catch (Exception e) {
            return null;
        }
    }
}
