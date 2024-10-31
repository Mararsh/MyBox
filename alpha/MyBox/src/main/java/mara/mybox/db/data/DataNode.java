package mara.mybox.db.data;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import mara.mybox.db.table.BaseTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.JsonTools;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class DataNode extends BaseData {

    public static final String RootIdentify = "MyBoxTreeRoot;;;";
    public static final String TitleSeparater = " > ";
    public static final String IDPrefix = "ID:";
    public static final String TimePrefix = "Time:";
    public static final String TagsPrefix = "Tags:";
    public static final String TagSeparater = ";;;";
    public static final String ValueSeparater = "_:;MyBoxNodeValue;:_";
    public static final String MoreSeparater = "MyBoxTreeNodeMore:";
    public static final String Root = "Root";
    public static final int RootID = -9;

    protected BaseTable dataTable;
    protected Map<String, Object> values;

    private void init() {
        dataTable = null;
        values = null;
    }

    public DataNode() {
        init();
    }

    @Override
    public boolean setValue(String column, Object value) {
        try {
            if (column == null) {
                return false;
            }
            if (values == null) {
                values = new HashMap<>();
            }
            values.put(column, value);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public Object getValue(String column) {
        if (column == null || values == null) {
            return null;
        }
        return values.get(column);
    }

    public String getTitle() {
        return getNodeTitle();
    }

    public String getValue() {
        return getNodeTitle();
    }

    @Override
    public boolean valid() {
        return true;
    }

    public boolean isRoot() {
        long id = getNodeid();
        return id >= 0 && getParentid() == id;
    }

    public String toText() {
        return getNodeTitle();
    }

    public String toXml(String prefix) {
        String xml = prefix + "<node>\n";
        String title = getNodeTitle();
        if (title != null && !title.isBlank()) {
            xml += prefix + prefix + "<title>\n"
                    + prefix + prefix + prefix + "<![CDATA[" + title.trim() + "]]>\n"
                    + prefix + prefix + "</title>\n";
        }
        xml += prefix + "</node>\n";
        return xml;
    }

    public String toHtml() {
        String html = "";
        String title = getNodeTitle();
        if (title != null && !title.isBlank()) {
            html += "<H2>" + title.trim() + "</H2>\n";
        }
        return html;
    }

    public String toJson(String prefix) {
        String title = getNodeTitle();
        String json = "";
        if (title != null && !title.isBlank()) {
            json += prefix + ",\n"
                    + prefix + "\"title\": " + JsonTools.encode(title.trim());
        }
        return json;
    }

    /*
        Static methods
     */
    public static DataNode create() {
        return new DataNode();
    }

    public static DataNode createRoot(BaseTable dataTable) {
        if (dataTable == null) {
            return null;
        }
        DataNode root = new DataNode()
                .setDataTable(dataTable)
                .setParentid(RootID)
                .setNodeid(RootID)
                .setNodeTitle(dataTable.getTableTitle());
        return root;
    }

    public static DataNode createChild(DataNode parent) {
        return create().
                setDataTable(parent.getDataTable())
                .setParentNode(parent)
                .setParentid(parent.getNodeid());
    }

    public static DataNode createChild(DataNode parent, String title) {
        return create().
                setDataTable(parent.getDataTable())
                .setParentNode(parent)
                .setParentid(parent.getNodeid())
                .setNodeTitle(title);
    }

    public static boolean valid(Note data) {
        return data != null;
    }

    /*
        get/set
     */
    public BaseTable getDataTable() {
        return dataTable;
    }

    public DataNode setDataTable(BaseTable dataTable) {
        this.dataTable = dataTable;
        return this;
    }

    public long getNodeid() {
        try {
            Object v = getValue("nodeid");
            return v == null ? -1 : (long) v;
        } catch (Exception e) {
            return -1;
        }
    }

    public DataNode setNodeid(long nodeid) {
        setValue("nodeid", nodeid);
        return this;
    }

    public long getParentid() {
        try {
            Object v = getValue("parentid");
            return v == null ? -1 : (long) v;
        } catch (Exception e) {
            return -1;
        }
    }

    public DataNode setParentid(long parentid) {
        setValue("parentid", parentid);
        return this;
    }

    public String getNodeTitle() {
        try {
            Object v = getValue("node_title");
            return v == null ? null : (String) v;
        } catch (Exception e) {
            return null;
        }
    }

    public DataNode setNodeTitle(String title) {
        setValue("node_title", title);
        return this;
    }

    public Date getUpdateTime() {
        try {
            Object v = getValue("update_time");
            return v == null ? null : (Date) v;
        } catch (Exception e) {
            return null;
        }
    }

    public DataNode setUpdateTime(Date updateTime) {
        setValue("update_time", updateTime);
        return this;
    }

    public DataNode getParentNode() {
        try {
            Object v = getValue("parentNode");
            return v == null ? null : (DataNode) v;
        } catch (Exception e) {
            return null;
        }
    }

    public DataNode setParentNode(DataNode parent) {
        setValue("parentNode", parent);
        return this;
    }

    public String getDataTitle() {
        try {
            Object v = getValue("title");
            return v == null ? null : (String) v;
        } catch (Exception e) {
            return null;
        }
    }

    public DataNode setDataTitle(String title) {
        setValue("title", title);
        return this;
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public void setValues(Map<String, Object> values) {
        this.values = values;
    }

}
