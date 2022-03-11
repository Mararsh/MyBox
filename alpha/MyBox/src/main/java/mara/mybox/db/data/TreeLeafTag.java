package mara.mybox.db.data;

import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-3-11
 * @License Apache License Version 2.0
 */
public class TreeLeafTag extends BaseData {

    protected long ttid, leaffid, tagid;
    protected TreeLeaf leaf;
    protected Tag tag;

    private void init() {
        ttid = leaffid = tagid = -1;
        leaf = null;
        tag = null;
    }

    public TreeLeafTag() {
        init();
    }

    public TreeLeafTag(long leaffid, long tagid) {
        init();
        this.leaffid = leaffid;
        this.tagid = tagid;
    }

    public TreeLeafTag(TreeLeaf leaf, Tag tag) {
        init();
        this.leaf = leaf;
        this.tag = tag;
        this.leaffid = leaf == null ? -1 : leaf.getLeafid();
        this.tagid = tag == null ? -1 : tag.getTgid();
    }

    /*
        Static methods
     */
    public static TreeLeafTag create() {
        return new TreeLeafTag();
    }

    public static boolean setValue(TreeLeafTag data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "ttid":
                    data.setTtid(value == null ? -1 : (long) value);
                    return true;
                case "leaffid":
                    data.setLeaffid(value == null ? -1 : (long) value);
                    return true;
                case "tagid":
                    data.setTagid(value == null ? -1 : (long) value);
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    public static Object getValue(TreeLeafTag data, String column) {
        if (data == null || column == null) {
            return null;
        }
        switch (column) {
            case "ttid":
                return data.getTtid();
            case "leaffid":
                return data.getLeaffid();
            case "tagid":
                return data.getTagid();
        }
        return null;
    }

    public static boolean valid(TreeLeafTag data) {
        return data != null
                && data.getLeaffid() > 0 && data.getTagid() > 0;
    }

    /*
        get/set
     */
    public long getTtid() {
        return ttid;
    }

    public void setTtid(long ttid) {
        this.ttid = ttid;
    }

    public long getLeaffid() {
        return leaffid;
    }

    public void setLeaffid(long leaffid) {
        this.leaffid = leaffid;
    }

    public long getTagid() {
        return tagid;
    }

    public void setTagid(long tagid) {
        this.tagid = tagid;
    }

    public TreeLeaf getLeaf() {
        return leaf;
    }

    public void setLeaf(TreeLeaf leaf) {
        this.leaf = leaf;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

}
