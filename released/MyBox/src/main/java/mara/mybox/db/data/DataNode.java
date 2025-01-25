package mara.mybox.db.data;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import static mara.mybox.db.table.BaseNodeTable.RootID;
import static mara.mybox.value.AppValues.InvalidDouble;
import static mara.mybox.value.AppValues.InvalidInteger;
import static mara.mybox.value.AppValues.InvalidLong;
import static mara.mybox.value.AppValues.InvalidShort;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class DataNode extends BaseData {

    public static final String TitleSeparater = " > ";
    public static final String TagsSeparater = ";;;";

    protected long nodeid, parentid;
    protected String title, hierarchyNumber;
    protected float orderNumber;
    protected Date updateTime;
    protected Map<String, Object> values;
    protected final BooleanProperty selected = new SimpleBooleanProperty(false);

    private void init() {
        nodeid = -1;
        parentid = RootID;
        title = null;
        updateTime = new Date();
        orderNumber = 0f;
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
                    setOrderNumber(value == null ? 0f : (float) value);
                    return true;
                case "update_time":
                    setUpdateTime(value == null ? new Date() : (Date) value);
                    return true;
            }
            if (values == null) {
                values = new HashMap<>();
            }
            if (value == null) {
                values.remove(column);
            } else {
                values.put(column, value);
            }
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

    public String getStringValue(String column) {
        try {
            Object o = getValue(column);
            if (o == null) {
                return null;
            }
            return (String) getValue(column);
        } catch (Exception e) {
            return null;
        }
    }

    public int getIntValue(String column) {
        try {
            Object o = getValue(column);
            if (o == null) {
                return InvalidInteger;
            }
            return (Integer) getValue(column);
        } catch (Exception e) {
            return InvalidInteger;
        }
    }

    public long getLongValue(String column) {
        try {
            Object o = getValue(column);
            if (o == null) {
                return InvalidLong;
            }
            return (Long) getValue(column);
        } catch (Exception e) {
            return InvalidLong;
        }
    }

    public short getShortValue(String column) {
        try {
            Object o = getValue(column);
            if (o == null) {
                return InvalidShort;
            }
            return (Short) getValue(column);
        } catch (Exception e) {
            return InvalidShort;
        }
    }

    public double getDoubleValue(String column) {
        try {
            Object o = getValue(column);
            if (o == null) {
                return InvalidDouble;
            }
            return (Double) getValue(column);
        } catch (Exception e) {
            return InvalidDouble;
        }
    }

    public boolean getBooleanValue(String column) {
        try {
            Object o = getValue(column);
            if (o == null) {
                return false;
            }
            return (Boolean) getValue(column);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean valid() {
        return true;
    }

    public boolean isRoot() {
        return nodeid == RootID;
    }

    @Override
    public boolean equals(Object obj) {
        try {
            return ((DataNode) obj).getNodeid() == nodeid;
        } catch (Exception e) {
            return false;
        }
    }

    public DataNode copy() {
        try {
            DataNode node = create()
                    .setParentid(parentid)
                    .setTitle(title)
                    .setOrderNumber(orderNumber);
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

    public String shortDescription() {
        return shortDescription(title);
    }

    public String shortDescription(String name) {
        String s = "";
        if (hierarchyNumber != null && !hierarchyNumber.isBlank()) {
            s += hierarchyNumber + " - ";
        }
        if (nodeid > 0) {
            s += nodeid + " - ";
        }
        s += name;
        return s;
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

    public BooleanProperty getSelected() {
        return selected;
    }

}
