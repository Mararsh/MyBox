package mara.mybox.db.table;

import java.sql.Connection;
import java.util.Map;
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
        defineColumns();
    }

    public final TableNodeHtml defineColumns() {
        defineNodeColumns();
        addColumn(new ColumnDefinition("html", ColumnType.Clob));
        return this;
    }

    @Override
    public String valuesHtml(FxTask task, Connection conn, BaseController controller, DataNode node) {
        try {
            Map<String, Object> values = node.getValues();
            return (String) values.get("html");
        } catch (Exception e) {
            return null;
        }
    }

}