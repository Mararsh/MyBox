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
public class TableNodeSQL extends BaseNodeTable {

    public TableNodeSQL() {
        tableName = "Node_SQL";
        treeName = message("DatabaseSQL");
        dataName = message("SQL");
        dataFxml = Fxmls.ControlDataSQLFxml;
        examplesFileName = "SQL";
        defineColumns();
    }

    public final TableNodeSQL defineColumns() {
        defineNodeColumns();
        addColumn(new ColumnDefinition("statement", ColumnType.String).setLength(FilenameMaxLength));
        return this;
    }

    @Override
    public String valuesHtml(FxTask task, Connection conn, BaseController controller, DataNode node) {
        try {
            String sql = node.getStringValue("statement");
            return sql == null || sql.isBlank() ? null
                    : ("<CODE>" + sql + "</CODE>");
        } catch (Exception e) {
            return null;
        }
    }

}