package mara.mybox.db.data;

import java.sql.Connection;
import java.util.Date;
import mara.mybox.db.table.BaseDataTable;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class DataNode extends BaseData {

    public static final int RootID = 0;
    public static final String TitleSeparater = " > ";
    public static final String TagSeparater = ";;;";

    protected long nodeid, parentid;
    protected String title;
    protected float orderNumber;
    protected Date updateTime;

    private void init() {
        nodeid = -1;
        parentid = -2;
        title = null;
        orderNumber = 0f;
        updateTime = new Date();
    }

    public DataNode() {
        init();
    }

    @Override
    public boolean setValue(String column, Object value) {
        return setValue(this, column, value);
    }

    @Override
    public Object getValue(String column) {
        return getValue(this, column);
    }

    @Override
    public boolean valid() {
        return true;
    }

    public DataValues dataValues(Connection conn, BaseDataTable dataTable) {
        return dataValues(conn, dataTable, this);
    }

    public boolean isRoot() {
        return parentid == nodeid;
    }

    public DataNode copy() {
        return copy(this);
    }

    public String texts() {
        return title;
    }

    public String toText() {
        return title;
    }

    public String toHtml() {
        return title;
    }

    public String getValue() {
        return title;
    }

    /*
        Static methods
     */
    public static DataNode create() {
        return new DataNode();
    }

    public static boolean setValue(DataNode node, String column, Object value) {
        if (node == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "nodeid":
                    node.setNodeid(value == null ? -1 : (long) value);
                    return true;
                case "parentid":
                    node.setParentid(value == null ? -1 : (long) value);
                    return true;
                case "title":
                    node.setTitle(value == null ? null : (String) value);
                    return true;
                case "order_number":
                    node.setOrderNumber(value == null ? 0f : (float) value);
                    return true;
                case "update_time":
                    node.setUpdateTime(value == null ? null : (Date) value);
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return false;
    }

    public static Object getValue(DataNode node, String column) {
        if (node == null || column == null) {
            return null;
        }
        switch (column) {
            case "nodeid":
                return node.getNodeid();
            case "parentid":
                return node.getParentid();
            case "title":
                return node.getTitle();
            case "order_number":
                return node.getOrderNumber();
            case "update_time":
                return node.getUpdateTime();
        }
        return null;
    }

    public static boolean valid(DataNode node) {
        return node != null;
    }

    public static DataNode createRoot(BaseDataTable dataTable) {
        if (dataTable == null) {
            return null;
        }
        DataNode root = new DataNode()
                .setParentid(RootID)
                .setNodeid(RootID)
                .setTitle(dataTable.getTableTitle());
        return root;
    }

    public static DataNode createChild(DataNode parent) {
        return create().setParentid(parent.getNodeid());
    }

    public static DataNode createChild(DataNode parent, String title) {
        return createChild(parent).setTitle(title);
    }

    public static DataNode copy(DataNode node) {
        return create()
                .setParentid(node.getParentid())
                .setTitle(node.getTitle() + " " + message("Copy"));
    }

    public static DataValues dataValues(Connection conn, BaseDataTable dataTable, DataNode node) {
        try {
            return (DataValues) dataTable.query(conn, node.getNodeid());
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    /*
        get/set
     */
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

    public float getOrderNumber() {
        return orderNumber;
    }

    public DataNode setOrderNumber(float orderNumber) {
        this.orderNumber = orderNumber;
        return this;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public DataNode setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
        return this;
    }

}
