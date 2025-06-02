package mara.mybox.db.table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.data.DataNodeTag;
import mara.mybox.db.data.DataTag;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-3-3
 * @License Apache License Version 2.0
 */
public class TableDataNodeTag extends BaseTable<DataNodeTag> {

    public static final String TableNameSuffix = "_Node_Tag";

    protected BaseNodeTable nodeTable;
    protected TableDataTag tagTable;

    public TableDataNodeTag(BaseNodeTable table) {
        if (table == null) {
            return;
        }
        nodeTable = table;
        tagTable = new TableDataTag(nodeTable);
        init();
    }

    public TableDataNodeTag(BaseNodeTable data, TableDataTag tag) {
        nodeTable = data;
        tagTable = tag;
        init();
    }

    public final void init() {
        if (nodeTable == null || tagTable == null) {
            return;
        }
        tableName = nodeTable.tableName + TableNameSuffix;
        defineColumns();
    }

    public final TableDataNodeTag defineColumns() {
        addColumn(new ColumnDefinition("tnodeid", ColumnType.Long, true, true)
                .setReferName(tableName + "_nodeid_fk")
                .setReferTable(nodeTable.tableName).setReferColumn(nodeTable.idColumnName)
                .setOnDelete(ColumnDefinition.OnDelete.Cascade)
        );
        addColumn(new ColumnDefinition("ttagid", ColumnType.Long, true, true)
                .setReferName(tableName + "_tagid_fk")
                .setReferTable(tagTable.tableName).setReferColumn(tagTable.idColumnName)
                .setOnDelete(ColumnDefinition.OnDelete.Cascade)
        );
        return this;
    }

    @Override
    public boolean setValue(DataNodeTag data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        return data.setValue(column, value);
    }

    @Override
    public Object getValue(DataNodeTag data, String column) {
        if (data == null || column == null) {
            return null;
        }
        return data.getValue(column);
    }

    @Override
    public boolean valid(DataNodeTag data) {
        if (data == null) {
            return false;
        }
        return data.valid();
    }

    @Override
    public Object readForeignValue(ResultSet results, String column) {
        if (results == null || column == null || nodeTable == null || tagTable == null) {
            return null;
        }
        try {
            if ("tnodeid".equals(column) && results.findColumn("nodeid") > 0) {
                return nodeTable.readData(results);
            }
            if ("ttagid".equals(column) && results.findColumn("tagid") > 0) {
                return tagTable.readData(results);
            }
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public boolean setForeignValue(DataNodeTag data, String column, Object value) {
        if (data == null || column == null || value == null) {
            return true;
        }
        if ("tnodeid".equals(column) && value instanceof DataNode) {
            data.setNode((DataNode) value);
        }
        if ("ttagid".equals(column) && value instanceof DataTag) {
            data.setTag((DataTag) value);
        }
        return true;
    }

    public List<DataNodeTag> nodeTags(long nodeid) {
        List<DataNodeTag> tags = new ArrayList<>();
        if (nodeid < 0) {
            return tags;
        }
        try (Connection conn = DerbyBase.getConnection()) {
            tags = nodeTags(conn, nodeid);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return tags;
    }

    public List<DataNodeTag> nodeTags(Connection conn, long nodeid) {
        List<DataNodeTag> tags = new ArrayList<>();
        if (conn == null || nodeid < 0) {
            return tags;
        }
        String sql = "SELECT * FROM " + tableName + ", " + nodeTable.tableName + "_Tag"
                + " WHERE tnodeid=? AND ttagid=tagid";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setLong(1, nodeid);
            conn.setAutoCommit(true);
            ResultSet results = statement.executeQuery();
            while (results.next()) {
                DataNodeTag tag = readData(results);
                if (tag != null) {
                    tags.add(tag);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return tags;
    }

    public int setAll(Connection conn, long nodeid, List<DataTag> tags) {
        if (nodeTable == null || conn == null || nodeid < 0) {
            return -1;
        }
        removeTags(conn, nodeid);
        if (tags == null || tags.isEmpty()) {
            return 0;
        }
        int count = 0;
        try {
            for (DataTag dataTag : tags) {
                DataNodeTag nodeTag = DataNodeTag.create()
                        .setTnodeid(nodeid).setTtagid(dataTag.getTagid());
                count += insertData(conn, nodeTag) == null ? 0 : 1;
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return -1;
        }
        return count;
    }

    public int removeTag(Connection conn, long nodeid, long tagid) {
        if (conn == null || nodeid < 0 || tagid < 0) {
            return -1;
        }
        String sql = "DELETE FROM " + tableName + " WHERE "
                + "tnodeid=" + nodeid + " AND tagid=" + tagid;
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            return statement.executeUpdate();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return -1;
        }
    }

    public int removeTags(Connection conn, long nodeid) {
        if (conn == null || nodeid < 0) {
            return -1;
        }
        String sql = "DELETE FROM " + tableName + " WHERE tnodeid=?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setLong(1, nodeid);
            return statement.executeUpdate();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return -1;
        }
    }

}
