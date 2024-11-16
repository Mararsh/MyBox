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
public class TableInfo extends BaseNodeTable {

    public TableInfo() {
        tableName = "Info_In_Tree";
        tableTitle = message("InformationInTree");
        fxml = Fxmls.ControlDataInfoFxml;
        examplesFile = exampleFile();
        defineColumns();
    }

    public final TableInfo defineColumns() {
        defineNodeColumns();
        addColumn(new ColumnDefinition("info", ColumnType.Clob));
        return this;
    }

    @Override
    public String valuesHtml(FxTask task, Connection conn, BaseController controller, DataNode node) {
        try {
            Map<String, Object> values = node.getValues();
            String info = (String) values.get("info");
            return info == null || info.isBlank() ? null
                    : ("<PRE><CODE>" + info + "</CODE></PRE>");
        } catch (Exception e) {
            return null;
        }
    }

}
