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
import mara.mybox.db.data.Tag;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-3-3
 * @License Apache License Version 2.0
 */
public class TableDataNodeTag extends BaseTable<DataNodeTag> {

    protected BaseTreeData dataTable;
    protected TableDataTag tagTable;

    public TableDataNodeTag(BaseTreeData data) {
        if (data == null) {
            return;
        }
        dataTable = data;
        tagTable = new TableDataTag(dataTable);
        init();
    }

    public TableDataNodeTag(BaseTreeData data, TableDataTag tag) {
        dataTable = data;
        tagTable = tag;
        init();
    }

    public final void init() {
        if (dataTable == null || tagTable == null) {
            return;
        }
        tableName = dataTable.tableName + "_Node_Tag";
        idColumnName = "tntid";
        defineColumns();
    }

    public final TableDataNodeTag defineColumns() {
        addColumn(new ColumnDefinition("tntid", ColumnType.Long, true, true).setAuto(true));
        addColumn(new ColumnDefinition("tnodeid", ColumnType.Long)
                .setReferName(tableName + "_nodeid_fk")
                .setReferTable(dataTable.tableName).setReferColumn(dataTable.idColumnName)
                .setOnDelete(ColumnDefinition.OnDelete.Cascade)
        );
        addColumn(new ColumnDefinition("ttagid", ColumnType.Long)
                .setReferName(tableName + "_tagid_fk")
                .setReferTable(tagTable.tableName).setReferColumn(tagTable.idColumnName)
                .setOnDelete(ColumnDefinition.OnDelete.Cascade)
        );
        return this;
    }

    public final boolean createIndices(Connection conn) {
        if (conn == null || tableName == null) {
            return false;
        }
        String sql = "CREATE UNIQUE INDEX  " + tableName + "_unique_index on " + tableName + " ( tnodeid , ttagid )";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (Exception e) {
            MyBoxLog.debug(e);
//            return false;
        }
        return true;
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
        if (results == null || column == null || dataTable == null || tagTable == null) {
            return null;
        }
        try {
            if ("tnodeid".equals(column) && results.findColumn("nodeid") > 0) {
                return dataTable.readData(results);
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
        if ("ttagid".equals(column) && value instanceof Tag) {
            data.setTag((Tag) value);
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
        String sql = "SELECT * FROM " + tableName + ", Tag WHERE tnodeid=? AND tagid=tgid";
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

    public DataNodeTag query(Connection conn, long nodeid, long tagid) {
        if (conn == null || nodeid < 0 || tagid < 0) {
            return null;
        }
        DataNodeTag tag = null;
        String sql = "SELECT * FROM " + tableName + ", Tag WHERE tnodeid=? AND tagid=?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setLong(1, nodeid);
            statement.setLong(2, tagid);
            conn.setAutoCommit(true);
            ResultSet results = statement.executeQuery();
            if (results.next()) {
                tag = readData(results);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return tag;
    }

    public List<String> nodeTagNames(long nodeid) {
        List<String> tags = new ArrayList<>();
        if (nodeid < 0) {
            return tags;
        }
        try (Connection conn = DerbyBase.getConnection()) {
            tags = nodeTagNames(conn, nodeid);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return tags;
    }

    public List<String> nodeTagNames(Connection conn, long nodeid) {
        List<String> tags = new ArrayList<>();
        if (nodeid < 0) {
            return tags;
        }
        String sql = "SELECT * FROM " + tableName + ", Tag WHERE tnodeid=? AND tagid=tgid";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setLong(1, nodeid);
            ResultSet results = statement.executeQuery();
            conn.setAutoCommit(true);
            while (results.next()) {
                DataNodeTag nodeTag = readData(results);
                if (nodeTag != null) {
                    tags.add(nodeTag.getTag().getTag());
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return tags;
    }

    public int addTags(Connection conn, long nodeid, String category, List<String> tags) {
        if (conn == null || nodeid < 0 || category == null || category.isBlank()
                || tags == null || tags.isEmpty()) {
            return -1;
        }
        String sql = "INSERT INTO " + tableName + " (tnodeid, tagid) "
                + "SELECT " + nodeid + ", tgid FROM Tag "
                + "WHERE category='" + category + "' AND tag IN (";
        for (int i = 0; i < tags.size(); i++) {
            if (i > 0) {
                sql += ",";
            }
            sql += "'" + tags.get(i) + "'";
        }
        sql += ")";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            return statement.executeUpdate();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return -1;
        }
    }

    public int writeTags(Connection conn, long nodeid, String category, List<String> tags) {
        if (tagTable == null || conn == null || nodeid < 0 || category == null || category.isBlank()
                || tags == null || tags.isEmpty()) {
            return -1;
        }
        int count = 0;
        try {
            for (String name : tags) {
//                Tag tag = tagTable.findAndCreate(conn, category, name);
//                if (tag == null) {
//                    continue;
//                }
//                if (query(conn, nodeid, tag.getTgid()) == null) {
//                    TreeNodeTag nodeTag = new TreeNodeTag(nodeid, tag.getTgid());
//                    count += insertData(conn, nodeTag) == null ? 0 : 1;
//                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return -1;
        }
        return count;
    }

    public int removeTags(Connection conn, long nodeid, String category, List<String> tags) {
        if (conn == null || nodeid < 0 || category == null || category.isBlank()
                || tags == null || tags.isEmpty()) {
            return -1;
        }
        String sql = "DELETE FROM " + tableName + " WHERE tnodeid=" + nodeid + " AND "
                + "tagid IN (SELECT tgid FROM Tag "
                + "WHERE category='" + category + "' AND tag IN (";
        for (int i = 0; i < tags.size(); i++) {
            if (i > 0) {
                sql += ",";
            }
            sql += "'" + tags.get(i) + "'";
        }
        sql += " ) )";
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
