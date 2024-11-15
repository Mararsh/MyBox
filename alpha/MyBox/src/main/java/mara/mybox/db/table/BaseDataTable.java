package mara.mybox.db.table;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mara.mybox.controller.BaseController;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.data.DataNodeTag;
import mara.mybox.db.data.DataValues;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.fxml.FxFileTools.getInternalFile;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.JsonTools;
import mara.mybox.tools.XmlTools;
import static mara.mybox.value.AppValues.Indent;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2024-8-12
 * @License Apache License Version 2.0
 */
public abstract class BaseDataTable<D> extends BaseTable<D> {

    protected String fxml;
    protected File examplesFile;

    /*
        methods
     */
    public boolean initTreeTables(Connection conn) {
        if (conn == null || tableName == null) {
            return false;
        }
        try {
            TableDataNode treeTable = new TableDataNode(this);
            treeTable.createTable(conn);
            treeTable.createIndices(conn);

            new TableDataTag(this).createTable(conn);

            TableDataNodeTag nodeTagTable = new TableDataNodeTag(this);
            nodeTagTable.createTable(conn);

            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    public File exampleFile() {
        if (tableName == null || columns == null) {
            return null;
        }
        String lang = Languages.embedFileLang();
        return getInternalFile("/data/examples/" + tableName + "_Examples_" + lang + ".txt",
                "data", tableName + "_Examples_" + lang + ".txt", true);
    }

    public String toXML(String prefix, String indent, DataNode node, boolean withID, List<DataNodeTag> tags) {
        if (node == null || columns == null) {
            return null;
        }
        String xml = prefix + "<Attributes>\n";
        if (withID) {
            if (node.getNodeid() >= 0) {
                xml += prefix + indent + "<nodeid>" + node.getNodeid() + "</nodeid>\n";
            }
            if (node.getParentid() >= 0) {
                xml += prefix + indent + "<parentid>" + node.getParentid() + "</parentid>\n";
            }
        }
        if (node.getTitle() != null) {
            xml += prefix + indent + "<title>\n"
                    + prefix + indent + "<![CDATA[" + node.getTitle() + "]]>\n"
                    + prefix + indent + "</title>\n";
        }
        if (node.getUpdateTime() != null) {
            xml += prefix + "<updateTime>" + DateTools.datetimeToString(node.getUpdateTime()) + "</updateTime>\n";
        }
        xml += prefix + "</Attributes>\n";
        xml += prefix + "<Data>\n";
        for (ColumnDefinition column : columns) {
            String name = column.getColumnName();
            Object value = node.getValue(name);
            if (value == null) {
                continue;
            }
            String tname = XmlTools.xmlTag(name);
            xml += prefix + indent + "<" + tname + ">\n";
            xml += prefix + indent + indent + "<![CDATA[" + column.toString(value) + "]]>\n";
            xml += prefix + indent + "</" + tname + ">\n";
        }
        xml += prefix + "</Data>\n";
        if (tags != null && !tags.isEmpty()) {
            xml += prefix + "<Tags>\n";
            for (DataNodeTag tag : tags) {
                xml += prefix + indent + "<Tag>\n";
                xml += prefix + indent + indent + "<![CDATA[" + tag.getTag().getTag() + "]]>\n";
                xml += prefix + indent + "</Tag>\n";
            }
            xml += prefix + "</Tags>\n";
        }
        return xml;
    }

    public String valuesHtml(FxTask task, Connection conn,
            BaseController controller, DataNode node) {
        if (conn == null || node == null) {
            return null;
        }
        DataValues values = node.dataValues(conn, this);
        if (values == null || values.isEmpty()) {
            return null;
        }
        StringTable table = new StringTable();
        for (ColumnDefinition column : columns) {
            String name = column.getColumnName();
            Object value = values.getValue(name);
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
        DataValues values = node.dataValues(conn, this);
        if (values == null || values.isEmpty()) {
            return null;
        }
        String prefix2 = prefix + Indent;
        String prefix3 = prefix2 + Indent;
        String xml = prefix + "<Data>\n";
        for (ColumnDefinition column : columns) {
            String name = column.getColumnName();
            Object value = values.getValue(name);
            if (value == null) {
                continue;
            }
            xml += prefix2 + "<" + name + ">\n";
            xml += prefix3 + "<![CDATA[" + column.toString(value) + "]]>\n";
            xml += prefix2 + "</" + name + ">\n";
        }
        xml += prefix + "</Data>\n";
        return xml;
    }

    public String valuesJson(FxTask task, Connection conn,
            BaseController controller, String prefix, DataNode node) {
        if (conn == null || node == null) {
            return null;
        }
        DataValues values = node.dataValues(conn, this);
        if (values == null || values.isEmpty()) {
            return null;
        }
        String json = "";
        for (ColumnDefinition column : columns) {
            String name = column.getColumnName();
            Object value = values.getValue(name);
            if (value == null) {
                continue;
            }
            json += prefix + ",\n"
                    + prefix + "\"" + message(name) + "\": "
                    + JsonTools.encode(column.toString(value));
        }
        return json;
    }

    /*
        get/set
     */
    public String getFxml() {
        return fxml;
    }

    public void setFxml(String fxml) {
        this.fxml = fxml;
    }

    public File getExamplesFile() {
        return examplesFile;
    }

    public void setExamplesFile(File examplesFile) {
        this.examplesFile = examplesFile;
    }

}
