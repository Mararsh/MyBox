package mara.mybox.db.table;

import java.sql.Connection;
import mara.mybox.controller.BaseController;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.data.DataValues;
import mara.mybox.fxml.FxTask;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class TableInfo extends BaseDataTable<DataValues> {

    public TableInfo() {
        tableName = "Info_In_Tree";
        tableTitle = message("InformationInTree");
        idColumnName = "infoid";
        fxml = Fxmls.ControlDataInfoFxml;
        defineColumns();
    }

    public final TableInfo defineColumns() {
        addColumn(new ColumnDefinition("infoid", ColumnType.Long, true, true).setAuto(true));
        addColumn(new ColumnDefinition("info", ColumnType.Clob));
        return this;
    }

    @Override
    public boolean setValue(DataValues data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        return data.setValue(column, value);
    }

    @Override
    public Object getValue(DataValues data, String column) {
        if (data == null || column == null) {
            return null;
        }
        return data.getValue(column);
    }

    @Override
    public boolean valid(DataValues data) {
        if (data == null) {
            return false;
        }
        return data.valid();
    }

    @Override
    public String valuesHtml(FxTask task, Connection conn, BaseController controller, DataNode node) {
        try {
            DataValues values = node.dataValues(conn, this);
            String info = (String) values.getValue("info");
            return info == null || info.isBlank() ? null
                    : ("<PRE><CODE>" + info + "</CODE></PRE>");
        } catch (Exception e) {
            return null;
        }
    }

}
