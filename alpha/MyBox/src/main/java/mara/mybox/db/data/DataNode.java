package mara.mybox.db.data;

import java.util.Date;
import mara.mybox.db.table.BaseTable;
import mara.mybox.dev.MyBoxLog;

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
    protected long nodeid, parentid;
    protected BaseTreeData node, parent;
    protected String title;
    protected Date updateTime;

    private void init() {
        dataTable = null;
        nodeid = -1;
        parentid = -2;
        title = null;
        updateTime = new Date();
        node = null;
        parent = null;
    }

    public DataNode() {
        init();
    }

    public DataNode(DataNode parent, String title) {
        init();
        this.parentid = parent.getNodeid();
        this.dataTable = parent.getDataTable();
        this.title = title;
    }

    @Override
    public boolean setValue(String column, Object value) {
        return setValue(this, column, value);
    }

    @Override
    public Object getValue(String column) {
        return getValue(this, column);
    }

    public Object getDataValue(String column) {
        if (node == null) {
            return null;
        }
        return node.getValue(column);
    }

    @Override
    public boolean valid() {
        return true;
    }

    public DataNode copyIn(DataNode parent) {
        DataNode nnode = new DataNode();
        nnode.setParentid(parent.getNodeid());
        nnode.setDataTable(parent.getDataTable());
        nnode.setTitle(title);
        return nnode;
    }

    public boolean isRoot() {
        return parentid == nodeid;
    }

    public String texts() {
        return title;
    }

    public String html() {
        return title;
    }

    public String getValue() {
        return html();
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
                .setTitle(dataTable.getTableTitle());
        return root;
    }

    public static boolean setValue(DataNode data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "nodeid":
                    data.setNodeid(value == null ? -1 : (long) value);
                    return true;
                case "title":
                    data.setTitle(value == null ? null : (String) value);
                    return true;
                case "parentid":
                    data.setParentid(value == null ? -1 : (long) value);
                    return true;
                case "update_time":
                    data.setUpdateTime(value == null ? null : (Date) value);
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return false;
    }

    public static Object getValue(DataNode data, String column) {
        if (data == null || column == null) {
            return null;
        }
        switch (column) {
            case "nodeid":
                return data.getNodeid();
            case "title":
                return data.getTitle();
            case "parentid":
                return data.getParentid();
            case "update_time":
                return data.getUpdateTime();
        }
        return null;
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
        return nodeid;
    }

    public DataNode setNodeid(long nodeid) {
        this.nodeid = nodeid;
        return this;
    }

    public long getParentid() {
        return parentid;
    }

    public DataNode setParentid(long parentid) {
        this.parentid = parentid;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public DataNode setTitle(String title) {
        this.title = title;
        return this;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public DataNode setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    public BaseTreeData getNode() {
        return node;
    }

    public DataNode setNode(BaseTreeData node) {
        this.node = node;
        return this;
    }

    public BaseTreeData getParent() {
        return parent;
    }

    public DataNode setParent(BaseTreeData parent) {
        this.parent = parent;
        return this;
    }

}
