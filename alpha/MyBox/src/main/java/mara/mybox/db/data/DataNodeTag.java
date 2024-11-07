package mara.mybox.db.data;

import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-3-11
 * @License Apache License Version 2.0
 */
public class DataNodeTag extends BaseData {

    protected long tnodeid, ttagid;
    protected DataNode node;
    protected DataTag tag;

    private void init() {
        tnodeid = ttagid = -1;
        node = null;
        tag = null;
    }

    public DataNodeTag() {
        init();
    }

    public DataNodeTag(long tnodeid, long tagid) {
        init();
        this.tnodeid = tnodeid;
        this.ttagid = tagid;
    }

    public DataNodeTag(DataNode node, DataTag tag) {
        init();
        this.node = node;
        this.tag = tag;
        this.tnodeid = node == null ? -1 : node.getNodeid();
        this.ttagid = tag == null ? -1 : tag.getTagid();
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
        return valid(this);
    }

    /*
        Static methods
     */
    public static DataNodeTag create() {
        return new DataNodeTag();
    }

    public static boolean setValue(DataNodeTag data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "tnodeid":
                    data.setTnodeid(value == null ? -1 : (long) value);
                    return true;
                case "ttagid":
                    data.setTtagid(value == null ? -1 : (long) value);
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return false;
    }

    public static Object getValue(DataNodeTag data, String column) {
        if (data == null || column == null) {
            return null;
        }
        switch (column) {
            case "tnodeid":
                return data.getTnodeid();
            case "ttagid":
                return data.getTtagid();
        }
        return null;
    }

    public static boolean valid(DataNodeTag data) {
        return data != null
                && data.getTnodeid() > 0 && data.getTtagid() > 0;
    }

    /*
        get/set
     */
    public long getTnodeid() {
        return tnodeid;
    }

    public DataNodeTag setTnodeid(long tnodeid) {
        this.tnodeid = tnodeid;
        return this;
    }

    public long getTtagid() {
        return ttagid;
    }

    public DataNodeTag setTtagid(long ttagid) {
        this.ttagid = ttagid;
        return this;
    }

    public DataNode getNode() {
        return node;
    }

    public DataNodeTag setNode(DataNode node) {
        this.node = node;
        return this;
    }

    public DataTag getTag() {
        return tag;
    }

    public DataNodeTag setTag(DataTag tag) {
        this.tag = tag;
        return this;
    }

}
