package mara.mybox.db.table;

import java.sql.Connection;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.InfoInTree;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class TableInfo extends BaseTableTreeData<InfoInTree> {

    public TableInfo() {
        tableName = "Info_In_Tree";
        tableTitle = message("InformationInTree");
        idColumnName = "infoid";
        defineColumns();
    }

    public final TableInfo defineColumns() {
        addColumn(new ColumnDefinition("infoid", ColumnType.Long, true, true).setAuto(true));
        addColumn(new ColumnDefinition("title", ColumnType.String).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("info", ColumnType.Clob));
        return this;
    }

    @Override
    public boolean setValue(InfoInTree data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        return data.setValue(column, value);
    }

    @Override
    public Object getValue(InfoInTree data, String column) {
        if (data == null || column == null) {
            return null;
        }
        return data.getValue(column);
    }

    @Override
    public boolean valid(InfoInTree data) {
        if (data == null) {
            return false;
        }
        return data.valid();
    }

    @Override
    public long insertData(Connection conn, String title, String info) {
        try {
            InfoInTree node = new InfoInTree().setInfo(info);
            node.setNodeTitle(title).setDataTitle(title);
            node = insertData(conn, node);
            return node.getInfoid();
        } catch (Exception e) {
            MyBoxLog.console(e);
            return -1;
        }
    }

}
