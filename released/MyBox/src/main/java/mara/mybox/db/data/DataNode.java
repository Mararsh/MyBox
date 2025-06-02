package mara.mybox.db.data;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import static mara.mybox.db.table.BaseNodeTable.RootID;
import static mara.mybox.value.AppValues.InvalidDouble;
import static mara.mybox.value.AppValues.InvalidInteger;
import static mara.mybox.value.AppValues.InvalidLong;
import static mara.mybox.value.AppValues.InvalidShort;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class DataNode extends BaseData {

    public static final String TitleSeparater = " > ";
    public static final String TagsSeparater = ";;;";

    public static enum SelectionType {
        None, Multiple, Single
    }

    protected long nodeid, parentid, index, childrenSize;
    protected String title, hierarchyNumber, chainName;
    protected float orderNumber;
    protected Date updateTime;
    protected Map<String, Object> values;
    protected final BooleanProperty selected = new SimpleBooleanProperty(false);
    protected List<DataNode> chainNodes;
    protected DataNode parentNode;
    protected List<DataNodeTag> nodeTags;

    private void init() {
        nodeid = -1;
        parentid = RootID;
        title = null;
        updateTime = new Date();
        orderNumber = 0f;
        chainNodes = null;
        parentNode = null;
        hierarchyNumber = null;
        chainName = null;
        index = -1;
        childrenSize = -1;
        selected.set(false);
    }

    public DataNode() {
        init();
    }

    public DataNode cloneAll() {
        try {
            DataNode node = create()
                    .setNodeid(nodeid)
                    .setParentid(parentid)
                    .setTitle(title)
                    .setOrderNumber(orderNumber)
                    .setUpdateTime(updateTime)
                    .setHierarchyNumber(hierarchyNumber)
                    .setChainNodes(chainNodes)
                    .setParentNode(parentNode)
                    .setChainName(chainName)
                    .setIndex(index)
                    .setChildrenSize(childrenSize);
            if (values != null) {
                for (String key : values.keySet()) {
                    node.setValue(key, values.get(key));
                }
            }
            node.getSelected().set(selected.get());
            return node;
        } catch (Exception e) {
            return null;
        }
    }

    public String info() {
        String info = message("ID") + ": " + nodeid + "\n";
        info += message("Title") + ": " + title + "\n";
        info += message("ParentID") + ": " + parentid + "\n";
        info += message("HierarchyNumber") + ": " + hierarchyNumber + "\n";
        info += message("ChainName") + ": " + chainName + "\n";
        info += message("OrderNumber") + ": " + orderNumber + "\n";
        info += message("Values") + ": " + values + "\n";
        info += "Ancestors: " + (chainNodes != null ? chainNodes.size() : null) + "\n";
        info += "ParentNode: " + (parentNode != null ? parentNode.getTitle() : null) + "\n";
        info += "Index: " + index + "\n";
        info += message("ChildrenSize") + ": " + childrenSize + "\n";
        return info;
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

    public String getTagNames() {
        if (nodeTags == null || nodeTags.isEmpty()) {
            return null;
        }
        String names = null, name;
        for (DataNodeTag nodeTag : nodeTags) {
            name = nodeTag.getTag().getTag();
            if (names == null) {
                names = name;
            } else {
                names += "," + name;
            }
        }
        return names;
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

    public DataNode setHierarchyNumber(String hierarchyNumber) {
        this.hierarchyNumber = hierarchyNumber;
        return this;
    }

    public BooleanProperty getSelected() {
        return selected;
    }

    public String getChainName() {
        return chainName;
    }

    public DataNode setChainName(String chainName) {
        this.chainName = chainName;
        return this;
    }

    public List<DataNode> getChainNodes() {
        return chainNodes;
    }

    public DataNode setChainNodes(List<DataNode> nodes) {
        this.chainNodes = nodes;
        return this;
    }

    public DataNode getParentNode() {
        return parentNode;
    }

    public DataNode setParentNode(DataNode parentNode) {
        this.parentNode = parentNode;
        return this;
    }

    public long getIndex() {
        return index;
    }

    public DataNode setIndex(long index) {
        this.index = index;
        return this;
    }

    public long getChildrenSize() {
        return childrenSize;
    }

    public DataNode setChildrenSize(long childrenSize) {
        this.childrenSize = childrenSize;
        return this;
    }

    public List<DataNodeTag> getNodeTags() {
        return nodeTags;
    }

    public DataNode setNodeTags(List<DataNodeTag> nodeTags) {
        this.nodeTags = nodeTags;
        return this;
    }

}
