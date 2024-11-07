package mara.mybox.db.table;

import java.io.File;
import java.sql.Connection;
import java.util.List;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.data.DataNodeTag;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.fxml.FxFileTools.getInternalFile;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.XmlTools;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2024-8-12
 * @License Apache License Version 2.0
 */
public abstract class BaseDataTable<D> extends BaseTable<D> {

    /*
        abstract
     */
    public abstract long insertData(Connection conn, String title, String info);

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

}
