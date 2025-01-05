package mara.mybox.db.table;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.scene.paint.Color;
import mara.mybox.controller.BaseController;
import mara.mybox.data.StringTable;
import mara.mybox.db.Database;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.DataNode;
import static mara.mybox.db.data.DataNode.TitleSeparater;
import mara.mybox.db.data.DataNodeTag;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.fxml.FxFileTools.getInternalFile;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.image.FxColorTools;
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
    protected boolean nodeExecutable;

    public BaseNodeTable() {
        idColumnName = "nodeid";
        orderColumns = "order_number,nodeid ASC";
        nodeExecutable = false;
    }

    public final BaseNodeTable defineNodeColumns() {
        addColumn(new ColumnDefinition("nodeid", ColumnType.Long, true, true).setAuto(true));
        addColumn(new ColumnDefinition("title", ColumnType.String, true).setLength(StringMaxLength));
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
    public String label(String name) {
        if (name == null || name.isBlank()) {
            return name;
        }
        switch (name.toLowerCase()) {
            case "nodeid":
                return message("NodeID");
            case "title":
                return message("Title");
            case "order_number":
                return message("OrderNumber");
            case "parentid":
                return message("ParentID");
            case "update_time":
                return message("UpdateTime");
        }
        return name;
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

    /*
        static
     */
    public static BaseNodeTable create(String name) {
        if (name == null) {
            return null;
        }
        switch (name) {
            case "Text":
                return new TableNodeText();
            case "Html":
                return new TableNodeHtml();
            case "MathFunction":
                return new TableNodeMathFunction();
            case "WebFavorite":
                return new TableNodeWebFavorite();
            case "SQL":
                return new TableNodeSQL();
            case "ImageScope":
                return new TableNodeImageScope();
            case "JShell":
                return new TableNodeJShell();
            case "JEXL":
                return new TableNodeJEXL();
            case "JavaScript":
                return new TableNodeJavaScript();
            case "RowExpression":
                return new TableNodeRowExpression();
            case "DataColumn":
                return new TableNodeDataColumn();
            case "GeographyCode":
                return new TableNodeGeographyCode();
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
            } else {
                if (treeName != null && !treeName.equals(root.getTitle())) {
                    root.setTitle(treeName);
                    root = updateData(conn, root);
                }
            }
            return root;
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
    public long deleteNode(Connection conn, long nodeid) {
        if (conn == null || nodeid < 0) {
            return -1;
        }
        if (nodeid == RootID) {
            return createRoot(conn) != null ? 1 : -3;
        }
        String sql = "DELETE FROM " + tableName + " WHERE nodeid=?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setLong(1, nodeid);
            return statement.executeUpdate();
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return -2;
        }
    }

    public long deleteDecentants(Connection conn, long nodeid) {
        if (conn == null || nodeid < 0) {
            return -1;
        }
        String sql = "DELETE FROM " + tableName
                + " WHERE parentid=? AND parentid<>nodeid";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
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

    public boolean hasChildren(Connection conn, long nodeid) {
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

    public DataNode copyNode(Connection conn, DataNode sourceNode, DataNode targetNode) {
        if (conn == null || sourceNode == null || targetNode == null) {
            return null;
        }
        try {
            DataNode newNode = sourceNode.copy()
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
                        DataNode newNode = childNode.copy()
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
        if (conn == null || targetNode == null || sourceNode == null
                || (task != null && !task.isWorking())) {
            return false;
        }
        long targetID = targetNode.getNodeid();
        long sourceID = sourceNode.getNodeid();
        if (targetID == sourceID) {
            return true;
        }
        DataNode parent = query(conn, targetNode.getParentid());
        if (parent == null || targetID == parent.getNodeid()) {
            return false;
        }
        return equalOrDescendant(task, conn, parent, sourceNode);
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

    public List<DataNode> ancestors(long id) {
        if (id < 0) {
            return null;
        }
        try (Connection conn = DerbyBase.getConnection()) {
            return ancestors(conn, id);
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public List<DataNode> ancestors(Connection conn, long id) {
        if (conn == null || id < 0) {
            return null;
        }
        List<DataNode> ancestors = null;
        DataNode node = query(conn, id);
        if (node == null || node.isRoot()) {
            return ancestors;
        }
        long parentid = node.getParentid();
        DataNode parent = query(conn, parentid);
        if (parent != null) {
            ancestors = ancestors(conn, parentid);
            if (ancestors == null) {
                ancestors = new ArrayList<>();
            }
            ancestors.add(parent);
        }
        return ancestors;
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

    public String valuesHtml(DataNode node) {
        if (node == null) {
            return null;
        }
        String title = "<P align=\"center\">" + node.getTitle() + "</p>";
        String html = valuesHtml(null, null, null, node);
        if (html == null) {
            return title;
        } else {
            return title + "<BR>" + html;
        }
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
            row.add(label(name));
            row.add("<CODE>" + value + "</CODE>");
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
            json += prefix + "\"" + label(name) + "\": "
                    + JsonTools.encode(sValue);
        }
        return json;
    }

    public String valuesString(DataNode node) {
        return valuesJson("", node, true);
    }

    // Node should be queried with all fields
    public String valuesHtml(FxTask task, Connection conn, BaseController controller,
            DataNode node, String hierarchyNumber, int indent) {
        try {
            if (conn == null || node == null) {
                return null;
            }
            long nodeid = node.getNodeid();
            String indentNode = " ".repeat(indent);
            String spaceNode = "&nbsp;".repeat(indent);
            String nodeName = node.getTitle();
            String displayName = "<SPAN class=\"SerialNumber\">"
                    + (hierarchyNumber != null ? hierarchyNumber : "")
                    + "&nbsp;&nbsp;</SPAN>" + nodeName;

            String html = indentNode + "<DIV style=\"padding: 2px;\">" + spaceNode + displayName + "\n";
            List<DataNodeTag> tags = new TableDataNodeTag(this).nodeTags(conn, nodeid);
            if (tags != null && !tags.isEmpty()) {
                String indentTag = " ".repeat(indent + 8);
                String spaceTag = "&nbsp;".repeat(2);
                html += indentTag + "<SPAN class=\"NodeTag\">\n";
                for (DataNodeTag nodeTag : tags) {
                    if (task != null && !task.isWorking()) {
                        return null;
                    }
                    Color color = nodeTag.getTag().getColor();
                    if (color == null) {
                        color = FxColorTools.randomColor();
                    }
                    html += indentTag + spaceTag
                            + "<SPAN style=\"border-radius:4px; padding: 2px; font-size:0.8em;  background-color: "
                            + FxColorTools.color2rgb(color)
                            + "; color: " + FxColorTools.color2rgb(FxColorTools.foreColor(color))
                            + ";\">" + nodeTag.getTag().getTag() + "</SPAN>\n";
                }
                html += indentTag + "</SPAN>\n";
            }
            html += indentNode + "</DIV>\n";

            String dataHtml = valuesHtml(task, conn, controller, node);
            if (dataHtml != null && !dataHtml.isBlank()) {
                html += indentNode + "<DIV class=\"nodeValue\">"
                        + "<DIV style=\"padding: 0 0 0 " + (indent + 4) * 6 + "px;\">"
                        + "<DIV class=\"valueBox\">\n";
                html += indentNode + dataHtml + "\n";
                html += indentNode + "</DIV></DIV></DIV>\n";
            }
            return html;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
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
            s += "\n" + label(name) + ": " + value;

        }
        return s;
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
