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
public class TableNodeText extends BaseNodeTable {

    public TableNodeText() {
        tableName = "Node_Text";
        treeName = message("TextTree");
        dataName = message("Texts");
        dataFxml = Fxmls.ControlDataTextFxml;
        examplesFileName = "TextTree";
        defineColumns();
    }

    public final TableNodeText defineColumns() {
        defineNodeColumns();
        addColumn(new ColumnDefinition("text", ColumnType.Clob));
        return this;
    }

    @Override
    public String valuesHtml(FxTask task, Connection conn, BaseController controller, DataNode node) {
        try {
            Map<String, Object> values = node.getValues();
            String text = (String) values.get("text");
            return text == null || text.isBlank() ? null
                    : ("<PRE><CODE>" + text + "</CODE></PRE>");
        } catch (Exception e) {
            return null;
        }
    }

}
