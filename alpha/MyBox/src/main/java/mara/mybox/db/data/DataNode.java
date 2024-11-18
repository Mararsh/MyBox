package mara.mybox.db.data;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import static mara.mybox.db.table.BaseNodeTable.RootID;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class DataNode extends BaseData {

    public static final String TitleSeparater = " > ";
    public static final String NodeTag = "Node";

    protected long nodeid, parentid;
    protected String title, hierarchyNumber;
    protected float orderNumber;
    protected Date updateTime;
    protected Map<String, Object> values;

    private void init() {
        nodeid = -1;
        parentid = RootID;
        title = null;
        updateTime = new Date();
        orderNumber = updateTime.getTime();
    }

    public DataNode() {
        init();
    }

    @Override
    public boolean setValue(String column, Object value) {
        try {
            switch (column) {
                case "nodeid":
                    setNodeid(value == null ? -1 : (long) value);
                    return true;
                case "parentid":
                    setParentid(value == null ? RootID : (long) value);
                    return true;
                case "title":
                    setTitle(value == null ? null : (String) value);
                    return true;
                case "order_number":
                    setOrderNumber(value == null ? new Date().getTime() : (float) value);
                    return true;
                case "update_time":
                    setUpdateTime(value == null ? new Date() : (Date) value);
                    return true;
            }
            if (values == null) {
                values = new HashMap<>();
            }
            values.put(column, value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Object getValue(String column) {
        try {
            if (column == null) {
                return null;
            }
            switch (column) {
                case "nodeid":
                    return getNodeid();
                case "parentid":
                    return getParentid();
                case "title":
                    return getTitle();
                case "order_number":
                    return getOrderNumber();
                case "update_time":
                    return getUpdateTime();
            }
            return values.get(column);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean valid() {
        return true;
    }

    public boolean isRoot() {
        return nodeid == RootID;
    }

    public DataNode copy() {
        try {
            DataNode node = create()
                    .setParentid(parentid)
                    .setTitle(title + " " + message("Copy"));
            if (values != null) {
                for (String key : values.keySet()) {
                    node.setValue(key, values.get(key));
                }
            }
            return node;
        } catch (Exception e) {
            return null;
        }
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

    public static boolean valid(DataNode node) {
        return node != null;
    }

    public static DataNode createChild(DataNode parent) {
        return create().setParentid(parent.getNodeid());
    }

    public static DataNode createChild(DataNode parent, String title) {
        return createChild(parent).setTitle(title);
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

    public Map<String, Object> getValues() {
        return values;
    }

    public DataNode setValues(Map<String, Object> values) {
        this.values = values;
        return this;
    }

    public String getHierarchyNumber() {
        return hierarchyNumber;
    }

    public void setHierarchyNumber(String hierarchyNumber) {
        this.hierarchyNumber = hierarchyNumber;
    }

}
