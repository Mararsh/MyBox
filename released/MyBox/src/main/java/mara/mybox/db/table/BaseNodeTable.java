package mara.mybox.db.table;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.DataTreeNodeEditorController;
import mara.mybox.controller.HtmlTableController;
import mara.mybox.controller.WebBrowserController;
import mara.mybox.data.StringTable;
import mara.mybox.data2d.DataInternalTable;
import mara.mybox.db.Database;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.DataNode;
import static mara.mybox.db.data.DataNode.TitleSeparater;
import mara.mybox.db.data.DataNodeTools;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.fxml.FxFileTools.getInternalFile;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.HtmlWriteTools;
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

    public String treeName, dataName, dataFxml, examplesFileName, majorColumnName;
    public boolean nodeExecutable;

    public BaseNodeTable() {
        idColumnName = "nodeid";
        orderColumns = "order_number ASC,nodeid ASC";
        nodeExecutable = false;
    }

    public final BaseNodeTable defineNodeColumns() {
        addColumn(new ColumnDefinition("nodeid", ColumnType.Long, true, true)
                .setAuto(true)
                .setLabel(message("NodeID")));
        addColumn(new ColumnDefinition("title", ColumnType.String, true)
                .setLength(StringMaxLength)
                .setLabel(message("Title")));
        addColumn(new ColumnDefinition("order_number", ColumnType.Float)
                .setLabel(message("OrderNumber")));
        addColumn(new ColumnDefinition("update_time", ColumnType.Datetime)
                .setLabel(message("UpdateTime")));
        addColumn(new ColumnDefinition("parentid", ColumnType.Long, true)
                .setLabel(message("ParentID"))
                .setReferName(tableName + "_parentid_fk")
                .setReferTable(tableName).setReferColumn(idColumnName)
                .setOnDelete(ColumnDefinition.OnDelete.Cascade)
        );
        return this;
    }

    @Override
    public boolean setValue(DataNode node, String column, Object value) {
        if (node == null || column == null) {
            return false;
        }
        return node.setValue(column, value);
    }

    @Override
    public Object getValue(DataNode node, String column) {
        if (node == null || column == null) {
            return null;
        }
        return node.getValue(column);
    }

    @Override
    public boolean valid(DataNode node) {
        if (node == null) {
            return false;
        }
        return node.valid();
    }

    @Override
    public boolean createTable(Connection conn) {
        try {
            if (!super.createTable(conn)) {
                return false;
            }

            conn.setAutoCommit(true);

            createIndices(conn);

            new TableDataTag(this).createTable(conn);

            new TableDataNodeTag(this).createTable(conn);

            return createRoot(conn) != null;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
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

    public boolean isNodeExecutable(DataNode node) {
        if (node == null) {
            return false;
        }
        return nodeExecutable;
    }

    public DataInternalTable dataTable() {
        DataInternalTable dataTable = new DataInternalTable();
        dataTable.setDataName(treeName).setSheet(tableName);
        return dataTable;
    }

    /*
        static
     */
    public static BaseNodeTable create(String name) {
        if (name == null) {
            return null;
        }
        if (Languages.match("TextTree", name)) {
            return new TableNodeText();
        } else if (Languages.match("HtmlTree", name)) {
            return new TableNodeHtml();
        } else if (Languages.match("MathFunction", name)) {
            return new TableNodeMathFunction();
        } else if (Languages.match("WebFavorite", name)) {
            return new TableNodeWebFavorite();
        } else if (Languages.match("DatabaseSQL", name)) {
            return new TableNodeSQL();
        } else if (Languages.match("ImageScope", name)) {
            return new TableNodeImageScope();
        } else if (Languages.match("JShell", name)) {
            return new TableNodeJShell();
        } else if (Languages.match("JEXL", name)) {
            return new TableNodeJEXL();
        } else if (Languages.match("JavaScript", name)) {
            return new TableNodeJavaScript();
        } else if (Languages.match("RowExpression", name)) {
            return new TableNodeRowExpression();
        } else if (Languages.match("DataColumn", name)) {
            return new TableNodeDataColumn();
        } else if (Languages.match("GeographyCode", name)) {
            return new TableNodeGeographyCode();
        } else if (Languages.match("MacroCommands", name)) {
            return new TableNodeMacro();
        }
        return null;
    }

    /*
        rows
     */
    @Override
    public DataNode newData() {
        return DataNode.create();
    }

    public DataNode createRoot() {
        try (Connection conn = DerbyBase.getConnection()) {
            return createRoot(conn);
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    private DataNode createRoot(Connection conn) {
        try {
            if (clearData(conn) < 0) {
                return null;
            }

            String sql = "INSERT INTO " + tableName + " ( nodeid, title, parentid ) "
                    + " VALUES ( " + RootID + ", '" + treeName + "', " + RootID + " )";
            if (update(conn, sql) < 0) {
                return null;
            }

            sql = "ALTER TABLE " + tableName + " ALTER COLUMN nodeid RESTART WITH 2";
            if (update(conn, sql) < 0) {
                return null;
            }

            return query(conn, RootID);
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
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
        try {
            if (conn == null || tableName == null) {
                return null;
            }
            DataNode root = query(conn, RootID);
            if (root == null) {
                root = createRoot(conn);
            }
            if (treeName != null && !treeName.equals(root.getTitle())) {
                root.setTitle(treeName);
            }
            root.setHierarchyNumber("").setChainName(root.getTitle());
            root = updateData(conn, root);
            return root;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public DataNode find(long id) {
        if (id < 0) {
            return null;
        }
        try (Connection conn = DerbyBase.getConnection()) {
            return find(conn, id);
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public DataNode find(Connection conn, long id) {
        if (conn == null || id < 0) {
            return null;
        }
        String sql = "SELECT * FROM " + tableName + " WHERE nodeid=?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setLong(1, id);
            return query(conn, statement);
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

    // its decentants will be deleted automatically
    public long deleteNode(long nodeid) {
        if (nodeid < 0) {
            return -1;
        }
        if (nodeid == RootID) {
            return createRoot() != null ? 1 : -3;
        }
        String sql = "DELETE FROM " + tableName + " WHERE nodeid=?";
        try (Connection conn = DerbyBase.getConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setLong(1, nodeid);
            return statement.executeUpdate();
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return -2;
        }
    }

    public long deleteDecentants(long nodeid) {
        if (nodeid < 0) {
            return -1;
        }
        String sql = "DELETE FROM " + tableName
                + " WHERE parentid=? AND parentid<>nodeid";
        try (Connection conn = DerbyBase.getConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setLong(1, nodeid);
            return statement.executeUpdate();
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return -2;
        }
    }

    // this will clear all data and start from 1
    public long truncate() {
        try (Connection conn = DerbyBase.getConnection()) {
            return truncate(conn);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return -3;
        }
    }

    public long truncate(Connection conn) {
        if (conn == null) {
            return -1;
        }
        String sql = "DELETE FROM " + tableName + " WHERE nodeid=" + RootID;
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            return statement.executeUpdate();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return -2;
        }
    }

    public boolean hasChildren(Connection conn, DataNode node) {
        if (node == null || node.getChildrenSize() == 0) {
            return false;
        }
        if (node.getChildrenSize() > 0) {
            return true;
        }
        return hasChildren(conn, node.getNodeid());
    }

    public boolean hasChildren(Connection conn, long nodeid) {
        if (conn == null || nodeid < 0) {
            return false;
        }
        boolean hasChildren = false;
        String sql = "SELECT nodeid FROM " + tableName
                + " WHERE parentid=? AND parentid<>nodeid FETCH FIRST ROW ONLY";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setLong(1, nodeid);
            try (ResultSet results = statement.executeQuery()) {
                hasChildren = results != null && results.next();
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return hasChildren;
    }

    public long childrenSize(Connection conn, long nodeid) {
        String sql = "SELECT count(nodeid) FROM " + tableName
                + " WHERE parentid=? AND parentid<>nodeid";
        long size = -1;
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setLong(1, nodeid);
            conn.setAutoCommit(true);
            ResultSet results = statement.executeQuery();
            if (results != null && results.next()) {
                size = results.getInt(1);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return size;
    }

    public DataNode copyNode(Connection conn, DataNode sourceNode, DataNode targetNode) {
        if (conn == null || sourceNode == null || targetNode == null) {
            return null;
        }
        try {
            DataNode newNode = sourceNode.cloneAll()
                    .setNodeid(-1).setParentid(targetNode.getNodeid());
            newNode = insertData(conn, newNode);
            if (newNode == null) {
                return null;
            }
            conn.commit();
            return newNode;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public int copyDescendants(FxTask task, Connection conn,
            DataNode sourceNode, DataNode targetNode, boolean allDescendants, int inCount) {
        int count = inCount;
        try {
            if (conn == null || sourceNode == null || targetNode == null) {
                return -count;
            }
            long sourceid = sourceNode.getNodeid();
            long targetid = targetNode.getNodeid();
            if (sourceid < 0 || targetid < 0 || (task != null && !task.isWorking())) {
                return -count;
            }
            conn.setAutoCommit(false);
            String sql = "SELECT * FROM " + tableName
                    + " WHERE parentid=? AND parentid<>nodeid  ORDER BY " + orderColumns;
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setLong(1, sourceid);
                try (ResultSet results = statement.executeQuery()) {
                    while (results != null && results.next()) {
                        if (task != null && !task.isWorking()) {
                            return -count;
                        }
                        DataNode childNode = readData(results);
                        DataNode newNode = childNode.cloneAll()
                                .setNodeid(-1).setParentid(targetid);
                        newNode = insertData(conn, newNode);
                        if (newNode == null) {
                            return -count;
                        }
                        if (++count % Database.BatchSize == 0) {
                            conn.commit();
                        }
                        if (allDescendants) {
                            copyDescendants(task, conn, childNode, newNode,
                                    allDescendants, count);
                        }
                    }
                } catch (Exception e) {
                    if (task != null) {
                        task.setError(e.toString());
                    } else {
                        MyBoxLog.error(e.toString());
                    }
                    return -count;
                }
            } catch (Exception e) {
                if (task != null) {
                    task.setError(e.toString());
                } else {
                    MyBoxLog.error(e.toString());
                }
                return -count;
            }
            conn.commit();
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
            return -count;
        }
        return count;
    }

    public int copyNodeAndDescendants(FxTask task, Connection conn,
            DataNode sourceNode, DataNode targetNode, boolean allDescendants) {
        DataNode copiedNode = copyNode(conn, sourceNode, targetNode);
        if (copiedNode == null) {
            return 0;
        }
        return copyDescendants(task, conn, sourceNode, copiedNode, allDescendants, 1);
    }

    public boolean equalOrDescendant(FxTask<Void> task, Connection conn,
            DataNode targetNode, DataNode sourceNode) {
        if (sourceNode == null) {
            return false;
        }
        return equalOrDescendant(task, conn, targetNode, sourceNode.getNodeid());
    }

    public boolean equalOrDescendant(FxTask<Void> task, Connection conn,
            DataNode targetNode, Long sourceID) {
        if (conn == null || targetNode == null
                || (task != null && !task.isWorking())) {
            return false;
        }
        long targetID = targetNode.getNodeid();
        if (targetID == sourceID) {
            return true;
        }
        DataNode parent = query(conn, targetNode.getParentid());
        if (parent == null || targetID == parent.getNodeid()) {
            return false;
        }
        return equalOrDescendant(task, conn, parent, sourceID);
    }

    public String chainName(FxTask<Void> task, Connection conn, DataNode node) {
        if (conn == null || node == null) {
            return null;
        }
        String chainName = "";
        DataNode cnode = node;
        while (cnode.getNodeid() != cnode.getParentid()) {
            if (conn == null || (task != null && !task.isWorking())) {
                return null;
            }
            cnode = query(conn, cnode.getParentid());
            if (cnode == null) {
                return null;
            }
            chainName = cnode.getTitle() + TitleSeparater + chainName;
        }
        chainName += node.getTitle();
        return chainName;
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

    public int trimDescedentsOrders(FxTask task, Connection conn,
            DataNode node, boolean allDescendants, int inCount) {
        if (node == null) {
            return -inCount;
        }
        long nodeid = node.getNodeid();
        if (nodeid < 0 || (task != null && !task.isWorking())) {
            return -inCount;
        }
        int count = inCount;
        String sql = "SELECT * FROM " + tableName
                + " WHERE parentid=? AND parentid<>nodeid  ORDER BY " + orderColumns;
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            int num = 0;
            statement.setLong(1, nodeid);
            try (ResultSet results = statement.executeQuery()) {
                while (results != null && results.next()) {
                    if (task != null && !task.isWorking()) {
                        return -count;
                    }
                    DataNode childNode = readData(results);
                    if (childNode == null) {
                        continue;
                    }
                    childNode.setOrderNumber(++num);
                    childNode = updateData(conn, childNode);
                    if (childNode == null) {
                        return -count;
                    }
                    if (++count % Database.BatchSize == 0) {
                        conn.commit();
                    }
                    if (allDescendants) {
                        count = trimDescedentsOrders(task, conn, childNode,
                                allDescendants, count);
                    }
                }
            } catch (Exception e) {
                MyBoxLog.console(e.toString());
                return -count;
            }
            conn.commit();
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
            return -count;
        }
        return count;
    }

    public DataNode readChain(FxTask<Void> task, Connection conn, DataNode node) {
        return readChain(task, conn, node == null ? RootID : node.getNodeid());
    }

    public DataNode readChain(FxTask<Void> task, Connection conn, long id) {
        if (conn == null || id < 0) {
            return null;
        }
        String chainName = "";
        List<DataNode> chainNodes = new ArrayList<>();
        DataNode node = query(conn, id);
        if (node == null) {
            return node;
        }
        chainNodes.add(node);
        DataNode child = node, parent;
        long parentid, childid;
        String h = "";
        while (true) {
            if (conn == null || (task != null && !task.isWorking())) {
                return null;
            }
            childid = child.getNodeid();
            parentid = child.getParentid();
            if (parentid == childid || childid == RootID) {
                break;
            }
            parent = query(conn, parentid);
            if (parent == null) {
                break;
            }
            chainName = parent.getTitle() + TitleSeparater + chainName;
            chainNodes.add(0, parent);
            child.setParentNode(parent);
            String sql = "SELECT nodeid FROM " + tableName
                    + " WHERE parentid=? AND parentid<>nodeid  ORDER BY " + orderColumns;
            int index = -1;
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setLong(1, parentid);
                try (ResultSet results = statement.executeQuery()) {
                    while (results != null && results.next()) {
                        index++;
                        long itemid = results.getLong("nodeid");
                        if (itemid == childid) {
                            break;
                        }
                    }
                } catch (Exception e) {
                    MyBoxLog.console(e);
                    return null;
                }
            } catch (Exception e) {
                MyBoxLog.console(e);
                return null;
            }
            if (index < 0) {
                return null;
            }
            child.setIndex(index);
            h = "." + (index + 1) + h;
            child = parent;
        }
        node.setChainNodes(chainNodes);
        node.setChainName(chainName + node.getTitle());
        if (h.startsWith(".")) {
            h = h.substring(1, h.length());
        }
        node.setHierarchyNumber(h);
        return node;
    }

    public String title(Connection conn, long id) {
        if (conn == null || id < 0) {
            return null;
        }
        String sql = "SELECT title FROM " + tableName + " WHERE nodeid=?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    return results.getString("title");
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    /*
        values
     */
    public File exampleFile() {
        return exampleFile(null);
    }

    public File exampleFile(String name) {
        return exampleFile(name, null);
    }

    public File exampleFileLang(String lang) {
        return exampleFile(null, lang);
    }

    public File exampleFile(String name, String lang) {
        if (name == null) {
            name = examplesFileName;
        }
        if (lang == null) {
            lang = Languages.embedFileLang();
        }
        return getInternalFile("/data/examples/" + name + "_Examples_" + lang + ".xml",
                "data", name + "_Examples_" + lang + ".xml", true);
    }

    public List<String> dataColumnNames() {
        List<String> names = new ArrayList<>();
        for (int i = 5; i < columns.size(); ++i) {
            ColumnDefinition column = columns.get(i);
            names.add(column.getColumnName());
        }
        return names;
    }

    // Node should be queried with all fields
    public String nodeHtml(FxTask task, Connection conn,
            BaseController controller, DataNode node) {
        return DataNodeTools.nodeHtml(task, conn, controller, this, node);
    }

    public String valuesHtml(FxTask task, Connection conn,
            BaseController controller, DataNode node) {
        if (node == null) {
            return null;
        }
        Map<String, Object> values = node.getValues();
        if (values == null || values.isEmpty()) {
            return null;
        }
        StringTable table = new StringTable();
        for (String name : dataColumnNames()) {
            ColumnDefinition column = column(name);
            String value = displayValue(column, values.get(name));
            if (value == null || value.isBlank()) {
                continue;
            }
            List<String> row = new ArrayList<>();
            row.add(column.getLabel());
            row.add(HtmlWriteTools.codeToHtml(value));
            table.add(row);
        }
        if (table.isEmpty()) {
            return null;
        } else {
            return table.div();
        }
    }

    public String valuesXml(String prefix, DataNode node, boolean format) {
        if (node == null) {
            return null;
        }
        Map<String, Object> values = node.getValues();
        if (values == null || values.isEmpty()) {
            return null;
        }
        String prefix2 = prefix + Indent;
        String xml = "";
        for (String name : dataColumnNames()) {
            ColumnDefinition column = column(name);
            Object value = values.get(name);
            if (value == null) {
                continue;
            }
            String sValue = exportValue(column, value, format);
            if (sValue == null || sValue.isBlank()) {
                continue;
            }
            if (column.isDBStringType()) {
                xml += prefix + "<" + name + ">\n";
                xml += prefix2 + "<![CDATA[" + sValue + "]]>\n";
                xml += prefix + "</" + name + ">\n";
            } else {
                xml += prefix + "<" + name + ">"
                        + sValue + "</" + name + ">\n";
            }
        }
        return xml;
    }

    public String valuesJson(String prefix, DataNode node, boolean format) {
        if (node == null) {
            return null;
        }
        Map<String, Object> values = node.getValues();
        if (values == null || values.isEmpty()) {
            return null;
        }
        String json = "";
        for (String name : dataColumnNames()) {
            ColumnDefinition column = column(name);
            Object value = values.get(name);
            if (value == null) {
                continue;
            }
            String sValue = exportValue(column, value, format);
            if (sValue == null || sValue.isBlank()) {
                continue;
            }
            if (!json.isBlank()) {
                json += ",\n";
            }
            json += prefix + "\"" + column.getLabel() + "\": "
                    + JsonTools.encode(sValue);
        }
        return json;
    }

    public String valuesString(DataNode node) {
        return valuesJson("", node, true);
    }

    // Node should be queried with all fields
    public String valuesText(DataNode node) {
        if (node == null) {
            return null;
        }
        String s = message("Title") + ": " + node.getTitle();
        Map<String, Object> values = node.getValues();
        if (values == null || values.isEmpty()) {
            return s;
        }
        for (String name : dataColumnNames()) {
            ColumnDefinition column = column(name);
            String value = displayValue(column, values.get(name));
            if (value == null || value.isBlank()) {
                continue;
            }
            s += "\n" + column.getLabel() + ": " + value;

        }
        return s;
    }

    public Object majorValue(DataNode node) {
        return getValue(node, majorColumnName);
    }

    /*
        tools
     */
    public void popNode(BaseController controller, DataNode node) {
        if (node == null) {
            return;
        }
        FxTask popTask = new FxSingletonTask<Void>(controller) {
            private String html;
            private DataNode savedNode;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    savedNode = readChain(this, conn, node.getNodeid());
                    if (savedNode == null) {
                        return false;
                    }
                    html = nodeHtml(this, conn, controller, savedNode);
                    return html != null && !html.isBlank();
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                HtmlTableController.open(null, html);
            }

        };
        controller.start(popTask, false);
    }

    public void executeNode(BaseController controller, DataNode node) {
        if (node == null) {
            controller.popError(message("SelectToHandle"));
            return;
        }
        if (this instanceof TableNodeWebFavorite) {
            FxTask exTask = new FxTask<Void>(controller) {
                private String address;

                @Override
                protected boolean handle() {
                    try (Connection conn = DerbyBase.getConnection()) {
                        DataNode savedNode = query(conn, node.getNodeid());
                        if (savedNode == null) {
                            return false;
                        }
                        address = savedNode.getStringValue("address");
                        return address != null;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    WebBrowserController.openAddress(address, true);
                }
            };
            controller.start(exTask, false, message("Handling..."));
        } else {
            DataTreeNodeEditorController.loadNode(controller, this, node, true);
        }
    }

    /*
        get/set
     */
    public String getDataName() {
        return dataName;
    }

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

    public boolean isNodeExecutable() {
        return nodeExecutable;
    }

    public void setNodeExecutable(boolean nodeExecutable) {
        this.nodeExecutable = nodeExecutable;
    }

}
