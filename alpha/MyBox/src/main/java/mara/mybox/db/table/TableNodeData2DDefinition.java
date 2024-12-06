package mara.mybox.db.table;

import java.sql.Connection;
import mara.mybox.controller.BaseController;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.tools.Data2DDefinitionTools;
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
public class TableNodeData2DDefinition extends BaseNodeTable {

    public TableNodeData2DDefinition() {
        tableName = "Node_Data2D_Definition";
        treeName = message("Data2DDefinition");
        dataName = message("DataDefinition");
        dataFxml = Fxmls.ControlDataData2DDefinitionFxml;
        examplesFileName = "DataDefinition";
        defineColumns();
    }

    public final TableNodeData2DDefinition defineColumns() {
        defineNodeColumns();
        addColumn(new ColumnDefinition("data2d_definition", ColumnType.Clob));
        return this;
    }

    @Override
    public String label(String name) {
        if (name == null || name.isBlank()) {
            return name;
        }
        switch (name) {
            case "data2d_definition":
                return message("Data2DDdefinition");
        }
        return super.label(name);
    }

    @Override
    public String escapeXML(String column, String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        if ("data2d_definition".equals(column)) {
            return Data2DDefinitionTools.escapeXML(value);
        }
        return value;
    }

    @Override
    public String valuesHtml(FxTask task, Connection conn, BaseController controller, DataNode node) {
        try {
            DataFileCSV def = Data2DDefinitionTools.fromDataNode(node);
            return Data2DDefinitionTools.toHtml(def);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String valuesString(DataNode node) {
        try {
            return node.getStringValue("html");
        } catch (Exception e) {
            return null;
        }
    }

}
