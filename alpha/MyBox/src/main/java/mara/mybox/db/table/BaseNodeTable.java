package mara.mybox.db.table;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import mara.mybox.controller.BaseController;
import mara.mybox.data.StringTable;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.DataNode;
import static mara.mybox.db.data.DataNode.TitleSeparater;
import mara.mybox.db.data.DataTag;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.fxml.FxFileTools.getInternalFile;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.JsonTools;
import static mara.mybox.value.AppValues.Indent;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class BaseNodeTable extends BaseTable<DataNode> {

    public static final long RootID = 1l;
    public static final String NodeFields = "nodeid,title,order_number,update_time,parentid";

    protected String treeName, dataName, dataFxml, examplesFileName;

    public BaseNodeTable() {
        idColumnName = "nodeid";
        orderColumns = "order_number, nodeid ASC";
    }

    public final BaseNodeTable defineNodeColumns() {
        addColumn(new ColumnDefinition("nodeid", ColumnType.Long, true, true).setAuto(true));
        addColumn(new ColumnDefinition("title", ColumnType.String, true).setLength(256));
        addColumn(new ColumnDefinition("order_number", ColumnType.Float));
        addColumn(new ColumnDefinition("update_time", ColumnType.Datetime));
        addColumn(new ColumnDefinition("parentid", ColumnType.Long, true)
                .setReferName(tableName + "_parentid_fk")
                .setReferTable(tableName).setReferColumn(idColumnName)
                .setOnDelete(ColumnDefinition.OnDelete.Cascade)
        );
        return this;
    }

    @Override
    public boolean setValue(DataNode data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        return data.setValue(column, value);
    }

    @Override
    public Object getValue(DataNode data, String column) {
        if (data == null || column == null) {
            return null;
        }
        return data.getValue(column);
    }

    @Override
    public boolean valid(DataNode data) {
        if (data == null) {
            return false;
        }
        return data.valid();
    }

    @Override
    public boolean createTable(Connection conn) {
        try {
            if (!super.createTable(conn)) {
                return false;
            }

            conn.setAutoCommit(true);

            createIndices(conn);

            if (createRoot(conn) == null) {
                return false;
            }

            new TableDataTag(this).createTable(conn);

            new TableDataNodeTag(this).createTable(conn);

            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    private DataNode createRoot(Connection conn) {
        try {
            String sql = "INSERT INTO " + tableName + " ( nodeid, title, parentid ) "
                    + " VALUES ( " + RootID + ", '" + treeName + "', " + RootID + " )";
            update(conn, sql);

            sql = "ALTER TABLE " + tableName + " ALTER COLUMN nodeid RESTART WITH 2";
            update(conn, sql);

            return query(conn, RootID);
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    private boolean createIndices(Connection conn) {
        if (conn == null || tableName == null) {
            return false;
        }
        String sql = "CREATE INDEX " + tableName + "_parent_index on " + tableName + " ( parentid )";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (Exception e) {
//            MyBoxLog.debug(e);
//            return false;
        }
        sql = "CREATE INDEX " + tableName + "_title_index on " + tableName + " ( parentid, title )";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (Exception e) {
//            MyBoxLog.debug(e);
//            return false;
        }
        sql = "CREATE INDEX " + tableName + "_order_index on " + tableName + " ( parentid, order_number )";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (Exception e) {
//            MyBoxLog.debug(e);
//            return false;
        }
        return true;
    }


    /*
        rows
     */
    @Override
    public DataNode newData() {
        return DataNode.create();
    }

    public DataNode readNode(ResultSet results) {
        if (results == null) {
            return null;
        }
        try {
            DataNode data = DataNode.create();
            for (int i = 0; i < 5; ++i) {
                ColumnDefinition column = column("nodeid");
                Object value = readColumnValue(results, column);
                setValue(data, column.getColumnName(), value);
            }
            return data;
        } catch (Exception e) {
            MyBoxLog.debug(e, tableName);
        }
        return null;
    }

    public DataNode getRoot() {
        try (Connection conn = DerbyBase.getConnection()) {
            return getRoot(conn);
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public DataNode getRoot(Connection conn) {
        if (conn == null || tableName == null) {
            return null;
        }
        DataNode root = query(conn, RootID);
        if (root == null) {
            root = createRoot(conn);
        }
        return root;
//        String sql = "SELECT * FROM " + tableName
//                + " WHERE nodeid=" + RootID + " FETCH FIRST ROW ONLY";
//        try (PreparedStatement statement = conn.prepareStatement(sql);
//                ResultSet results = statement.executeQuery()) {
//            DataNode node = null;
//            if (results.next()) {
//                node = readData(results);
//            }
//            if (node == null) {
//                //                clearData(conn);
//                node = createRoot(conn);
//            }
//            return node;
//        } catch (Exception e) {
//            MyBoxLog.error(e);
//            return null;
//        }
    }

    public DataNode queryNode(Connection conn, long nodeid) {
        if (conn == null || nodeid < 0) {
            return null;
        }
        String sql = "SELECT " + NodeFields + " FROM " + tableName
                + " WHERE nodeid=" + nodeid + " FETCH FIRST ROW ONLY";
        try (PreparedStatement statement = conn.prepareStatement(sql);
                ResultSet results = statement.executeQuery()) {
            DataNode node = null;
            if (results.next()) {
                node = readNode(results);
            }
            return node;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public List<DataNode> children(long parent) {
        if (parent < 0) {
            return null;
        }
        try (Connection conn = DerbyBase.getConnection()) {
            return children(conn, parent);
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public List<DataNode> children(Connection conn, long parent) {
        if (conn == null || parent < 0) {
            return null;
        }
        String sql = "SELECT " + NodeFields + " FROM " + tableName
                + " WHERE parentid=? AND parentid<>nodeid  ORDER BY " + orderColumns;
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setLong(1, parent);
            return query(statement);
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public List<DataNode> ancestor(long id) {
        if (id <= 0) {
            return null;
        }
        try (Connection conn = DerbyBase.getConnection()) {
            return ancestor(conn, id);
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public List<DataNode> ancestor(Connection conn, long id) {
        if (conn == null || id <= 0) {
            return null;
        }
        List<DataNode> ancestor = null;
        DataNode node = query(conn, id);
        if (node == null || node.isRoot()) {
            return ancestor;
        }
        long parentid = node.getParentid();
        DataNode parent = query(conn, parentid);
        if (parent != null) {
            ancestor = ancestor(conn, parentid);
            if (ancestor == null) {
                ancestor = new ArrayList<>();
            }
            ancestor.add(parent);
        }
        return ancestor;
    }

    public DataNode find(long parent, String title) {
        if (title == null || title.isBlank()) {
            return null;
        }
        try (Connection conn = DerbyBase.getConnection()) {
            return find(conn, parent, title);
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public DataNode find(Connection conn, long parent, String title) {
        if (conn == null || title == null || title.isBlank()) {
            return null;
        }
        String sql = "SELECT * FROM " + tableName
                + " WHERE parentid=? AND parentid<> nodeid AND title=? "
                + "ORDER BY nodeid DESC FETCH FIRST ROW ONLY";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setLong(1, parent);
            statement.setString(2, title);
            return query(conn, statement);
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public DataNode findAndCreate(long parent, String title) {
        if (title == null || title.isBlank()) {
            return null;
        }
        try (Connection conn = DerbyBase.getConnection()) {
            return findAndCreate(conn, parent, title);
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public DataNode findAndCreate(Connection conn, long parent, String title) {
        if (conn == null || title == null || title.isBlank()) {
            return null;
        }
        try {
            DataNode node = find(conn, parent, title);
            if (node == null) {
                DataNode parentNode = query(conn, parent);
                node = DataNode.createChild(parentNode, title);
                node = insertData(conn, node);
                conn.commit();
            }
            return node;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public DataNode findAndCreateChain(Connection conn, DataNode root, String ownerChain) {
        if (conn == null || root == null || ownerChain == null || ownerChain.isBlank()) {
            return null;
        }
        try {
            long parentid = root.getNodeid();
            String chain = ownerChain;
            String title = root.getTitle();
            if (chain.startsWith(title + TitleSeparater)) {
                chain = chain.substring((title + TitleSeparater).length());
            } else if (chain.startsWith(message(title) + TitleSeparater)) {
                chain = chain.substring((message(title) + TitleSeparater).length());
            }
            String[] nodes = chain.split(TitleSeparater);
            DataNode owner = null;
            for (String node : nodes) {
                owner = findAndCreate(conn, parentid, node);
                if (owner == null) {
                    return null;
                }
                parentid = owner.getNodeid();
            }
            return owner;
        } catch (Exception e) {
            MyBoxLog.console(e);
            return null;
        }
    }

    public List<DataNode> decentants(Connection conn, long parentid) {
        List<DataNode> allChildren = new ArrayList<>();
        List<DataNode> children = children(conn, parentid);
        if (children != null && !children.isEmpty()) {
            allChildren.addAll(allChildren);
            for (DataNode child : children) {
                children = decentants(conn, child.getNodeid());
                if (children != null && !children.isEmpty()) {
                    allChildren.addAll(allChildren);
                }
            }
        }
        return allChildren;
    }

    public List<DataNode> decentants(Connection conn, long parentid, long start, long size) {
        List<DataNode> children = new ArrayList<>();
        String sql = "SELECT * FROM " + tableName
                + " WHERE parentid=? AND parentid<>nodeid  ORDER BY " + orderColumns;
        try (PreparedStatement query = conn.prepareStatement(sql)) {
            decentants(conn, query, parentid, start, size, children, 0);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return children;
    }

    public long decentants(Connection conn, PreparedStatement query,
            long parentid, long start, long size, List<DataNode> nodes, long index) {
        if (conn == null || parentid < 1 || nodes == null
                || query == null || start < 0 || size <= 0 || nodes.size() >= size) {
            return index;
        }
        long thisIndex = index;
        try {
            int thisSize = nodes.size();
            boolean ok = false;
            query.setLong(1, parentid);
            conn.setAutoCommit(true);
            try (ResultSet nresults = query.executeQuery()) {
                while (nresults.next()) {
                    DataNode data = readData(nresults);
                    if (data != null) {
                        if (thisIndex >= start) {
                            nodes.add(data);
                            thisSize++;
                        }
                        thisIndex++;
                        if (thisSize >= size) {
                            ok = true;
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                MyBoxLog.debug(e, tableName);
            }
            if (!ok) {
                List<DataNode> children = children(conn, parentid);
                if (children != null) {
                    for (DataNode child : children) {
                        thisIndex = decentants(conn, query, child.getNodeid(), start, size, nodes, thisIndex);
                        if (nodes.size() >= size) {
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return thisIndex;
    }

    public int decentantsSize(Connection conn, long parentid) {
        if (conn == null || parentid < 0) {
            return 0;
        }
        int count = 0;
        String sql = "SELECT count(nodeid) FROM " + tableName
                + " WHERE parentid=? AND parentid<>nodeid";
        try (PreparedStatement sizeQuery = conn.prepareStatement(sql)) {
            count = decentantsSize(conn, sizeQuery, parentid);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return count;
    }

    public int decentantsSize(Connection conn, PreparedStatement sizeQuery, long parentid) {
        if (conn == null || sizeQuery == null || parentid < 0) {
            return 0;
        }
        int count = 0;
        try {
            count = childrenSize(sizeQuery, parentid);
            List<DataNode> children = children(conn, parentid);
            if (children != null) {
                for (DataNode child : children) {
                    count += decentantsSize(conn, sizeQuery, child.getNodeid());
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return count;
    }

    public int childrenSize(PreparedStatement sizeQuery, long parent) {
        if (sizeQuery == null || parent < 0) {
            return 0;
        }
        int size = 0;
        try {
            sizeQuery.setLong(1, parent);
            sizeQuery.getConnection().setAutoCommit(true);
            try (ResultSet results = sizeQuery.executeQuery()) {
                if (results != null && results.next()) {
                    size = results.getInt(1);
                }
            } catch (Exception e) {
                MyBoxLog.debug(e);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return size;
    }

    public int childrenSize(long parent) {
        if (parent < 0) {
            return 0;
        }
        try (Connection conn = DerbyBase.getConnection()) {
            return childrenSize(conn, parent);
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return -1;
        }
    }

    public int childrenSize(Connection conn, long parent) {
        if (conn == null || parent < 0) {
            return 0;
        }
        int size = 0;
        String sql = "SELECT count(nodeid) FROM " + tableName
                + " WHERE parentid=? AND parentid<>nodeid";
        try (PreparedStatement sizeQuery = conn.prepareStatement(sql)) {
            size = childrenSize(sizeQuery, parent);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return size;
    }

    public boolean childrenEmpty(Connection conn, long parent) {
        boolean isEmpty = true;
        String sql = "SELECT nodeid FROM " + tableName
                + " WHERE parentid=? AND parentid<>nodeid FETCH FIRST ROW ONLY";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setLong(1, parent);
            conn.setAutoCommit(true);
            try (ResultSet results = statement.executeQuery()) {
                isEmpty = results == null || !results.next();
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return isEmpty;
    }

    public boolean equalOrDescendant(FxTask<Void> task, Connection conn, DataNode node1, DataNode node2) {
        if (conn == null || node1 == null || node2 == null) {
            if (task != null) {
                task.setError(message("InvalidData"));
            }
            return false;
        }
        long id1 = node1.getNodeid();
        long id2 = node2.getNodeid();
        if (id1 == id2) {
            return true;
        }
        DataNode parent = parent(conn, node1);
        if (parent == null || id1 == parent.getNodeid()) {
            return false;
        }
        return equalOrDescendant(task, conn, parent(conn, node1), node2);
    }

    public DataNode parent(Connection conn, DataNode node) {
        if (conn == null || node == null) {
            return null;
        }
        return query(conn, node.getParentid());
    }

    public int deleteChildren(long parent) {
        try (Connection conn = DerbyBase.getConnection()) {
            return deleteChildren(conn, parent);
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return -1;
        }
    }

    public int deleteChildren(Connection conn, long parent) {
        String sql = "DELETE FROM " + tableName
                + " WHERE parentid=? AND parentid<>nodeid";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setLong(1, parent);
            return statement.executeUpdate();
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return -1;
        }
    }

    public String tagsCondition(List<DataTag> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        String condition = " nodeid IN ( SELECT tnodeid FROM Tree_Node_Tag "
                + " WHERE tagid IN ( " + tags.get(0).getTagid();
        for (int i = 1; i < tags.size(); ++i) {
            condition += ", " + tags.get(i).getTagid();
        }
        condition += " ) ) ";
        return condition;
    }

    /*
        values
     */
    public File exampleFile() {
        return exampleFile(examplesFileName);
    }

    public File exampleFile(String name) {
        if (name == null || columns == null) {
            return null;
        }
        String lang = Languages.embedFileLang();
        return getInternalFile("/data/examples/" + name + "_Examples_" + lang + ".txt",
                "data", name + "_Examples_" + lang + ".txt", true);
    }

    public String valuesHtml(FxTask task, Connection conn,
            BaseController controller, DataNode node) {
        if (conn == null || node == null) {
            return null;
        }
        Map<String, Object> values = node.getValues();
        if (values == null || values.isEmpty()) {
            return null;
        }
        StringTable table = new StringTable();
        for (ColumnDefinition column : columns) {
            String name = column.getColumnName();
            Object value = values.get(name);
            if (value == null) {
                continue;
            }

            List<String> row = new ArrayList<>();
            row.addAll(Arrays.asList(message(name),
                    "<PRE><CODE>" + column.toString(value) + "</CODE></PRE>"));
            table.add(row);
        }
        return table.div();
    }

    public String valuesXml(FxTask task, Connection conn,
            BaseController controller, String prefix, DataNode node) {
        if (conn == null || node == null) {
            return null;
        }
        Map<String, Object> values = node.getValues();
        if (values == null || values.isEmpty()) {
            return null;
        }
        String prefix2 = prefix + Indent;
        String xml = "";
        for (ColumnDefinition column : columns) {
            String name = column.getColumnName();
            Object value = values.get(name);
            if (value == null) {
                continue;
            }
            xml += prefix + "<" + name + ">\n";
            xml += prefix2 + "<![CDATA[" + column.toString(value) + "]]>\n";
            xml += prefix + "</" + name + ">\n";
        }
        return xml;
    }

    public String valuesJson(FxTask task, Connection conn,
            BaseController controller, String prefix, DataNode node) {
        if (conn == null || node == null) {
            return null;
        }
        Map<String, Object> values = node.getValues();
        if (values == null || values.isEmpty()) {
            return null;
        }
        String json = "";
        for (ColumnDefinition column : columns) {
            String name = column.getColumnName();
            Object value = values.get(name);
            if (value == null) {
                continue;
            }
            json += prefix + ",\n"
                    + prefix + "\"" + message(name) + "\": "
                    + JsonTools.encode(column.toString(value));
        }
        return json;
    }

    public String getDataName() {
        return dataName;
    }

    /*
    get/set
     */
    public void setDataName(String dataName) {
        this.dataName = dataName;
    }

    public String getDataFxml() {
        return dataFxml;
    }

    public BaseNodeTable setDataFxml(String dataFxml) {
        this.dataFxml = dataFxml;
        return this;
    }

    public String getTreeName() {
        return treeName;
    }

    public BaseNodeTable setTreeName(String treeName) {
        this.treeName = treeName;
        return this;
    }

    public String getExamplesFileName() {
        return examplesFileName;
    }

    public BaseNodeTable setExamplesFileName(String examplesFileName) {
        this.examplesFileName = examplesFileName;
        return this;
    }

}