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
public class TableNodeHtml extends BaseNodeTable {

    public TableNodeHtml() {
        tableName = "Node_Html";
        treeName = message("HtmlTree");
        dataName = message("Html");
        dataFxml = Fxmls.ControlDataHtmlFxml;
        examplesFileName = "HtmlTree";
        majorColumnName = "html";
        defineColumns();
    }

    public final TableNodeHtml defineColumns() {
        defineNodeColumns();
        addColumn(new ColumnDefinition("html", ColumnType.Clob)
                .setLabel(message("Html")));
        return this;
    }

    @Override
    public String valuesHtml(FxTask task, Connection conn, BaseController controller, DataNode node) {
        try {
            return node.getStringValue("html");
        } catch (Exception e) {
            return null;
        }
    }

}
