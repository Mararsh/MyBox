package mara.mybox.db.table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.DataTag;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-3-3
 * @License Apache License Version 2.0
 */
public class TableDataTag extends BaseTable<DataTag> {

    protected BaseNodeTable nodeTable;

    public TableDataTag(BaseNodeTable table) {
        if (table == null) {
            return;
        }
        nodeTable = table;
        tableName = nodeTable.tableName + "_Tag";
        idColumnName = "tagid";
        defineColumns();
    }

    public final TableDataTag defineColumns() {
        addColumn(new ColumnDefinition("tagid", ColumnType.Long, true, true).setAuto(true));
        addColumn(new ColumnDefinition("tag", ColumnType.String, true).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("color", ColumnType.Color, true));
        orderColumns = "tagid ASC";
        return this;
    }

    @Override
    public boolean setValue(DataTag data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        return data.setValue(column, value);
    }

    @Override
    public Object getValue(DataTag data, String column) {
        if (data == null || column == null) {
            return null;
        }
        return data.getValue(column);
    }

    @Override
    public boolean valid(DataTag data) {
        if (data == null) {
            return false;
        }
        return data.valid();
    }

    public DataTag queryTag(Connection conn, String tag) {
        if (conn == null || tag == null || tag.isBlank()) {
            return null;
        }
        DataTag dataTag = null;
        String sql = "SELECT * FROM " + tableName + " WHERE tag=?  FETCH FIRST ROW ONLY";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, tag);
            ResultSet results = statement.executeQuery();
            if (results.next()) {
                dataTag = readData(results);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return dataTag;
    }

}
