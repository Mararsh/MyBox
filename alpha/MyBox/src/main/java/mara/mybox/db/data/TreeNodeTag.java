package mara.mybox.db.data;

import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-3-11
 * @License Apache License Version 2.0
 */
public class TreeNodeTag extends BaseData {

    protected long tntid, tnodeid, ttagid;
    protected TreeNode node;
    protected Tag tag;

    private void init() {
        tntid = tnodeid = ttagid = -1;
        node = null;
        tag = null;
    }

    public TreeNodeTag() {
        init();
    }

    public TreeNodeTag(long tnodeid, long tagid) {
        init();
        this.tnodeid = tnodeid;
        this.ttagid = tagid;
    }

    public TreeNodeTag(TreeNode node, Tag tag) {
        init();
        this.node = node;
        this.tag = tag;
        this.tnodeid = node == null ? -1 : node.getNodeid();
        this.ttagid = tag == null ? -1 : tag.getTgid();
    }

    public boolean setValue(String column, Object value) {
        return setValue(this, column, value);
    }

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
    public static TreeNodeTag create() {
        return new TreeNodeTag();
    }

    public static boolean setValue(TreeNodeTag data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "tntid":
                    data.setTntid(value == null ? -1 : (long) value);
                    return true;
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

    public static Object getValue(TreeNodeTag data, String column) {
        if (data == null || column == null) {
            return null;
        }
        switch (column) {
            case "tntid":
                return data.getTntid();
            case "tnodeid":
                return data.getTnodeid();
            case "ttagid":
                return data.getTtagid();
        }
        return null;
    }

    public static boolean valid(TreeNodeTag data) {
        return data != null
                && data.getTnodeid() > 0 && data.getTtagid() > 0;
    }

    /*
        get/set
     */
    public long getTntid() {
        return tntid;
    }

    public TreeNodeTag setTntid(long tntid) {
        this.tntid = tntid;
        return this;
    }

    public long getTnodeid() {
        return tnodeid;
    }

    public TreeNodeTag setTnodeid(long tnodeid) {
        this.tnodeid = tnodeid;
        return this;
    }

    public long getTtagid() {
        return ttagid;
    }

    public TreeNodeTag setTtagid(long ttagid) {
        this.ttagid = ttagid;
        return this;
    }

    public TreeNode getNode() {
        return node;
    }

    public TreeNodeTag setNode(TreeNode node) {
        this.node = node;
        return this;
    }

    public Tag getTag() {
        return tag;
    }

    public TreeNodeTag setTag(Tag tag) {
        this.tag = tag;
        return this;
    }

}
