package mara.mybox.db.data;

import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-3-11
 * @License Apache License Version 2.0
 */
public class InfoNodeTag extends BaseData {

    protected long ttid, tnodeid, tagid;
    protected InfoNode node;
    protected Tag tag;

    private void init() {
        ttid = tnodeid = tagid = -1;
        node = null;
        tag = null;
    }

    public InfoNodeTag() {
        init();
    }

    public InfoNodeTag(long tnodeid, long tagid) {
        init();
        this.tnodeid = tnodeid;
        this.tagid = tagid;
    }

    public InfoNodeTag(InfoNode node, Tag tag) {
        init();
        this.node = node;
        this.tag = tag;
        this.tnodeid = node == null ? -1 : node.getNodeid();
        this.tagid = tag == null ? -1 : tag.getTgid();
    }

    /*
        Static methods
     */
    public static InfoNodeTag create() {
        return new InfoNodeTag();
    }

    public static boolean setValue(InfoNodeTag data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "ttid":
                    data.setTtid(value == null ? -1 : (long) value);
                    return true;
                case "tnodeid":
                    data.setTnodeid(value == null ? -1 : (long) value);
                    return true;
                case "tagid":
                    data.setTagid(value == null ? -1 : (long) value);
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return false;
    }

    public static Object getValue(InfoNodeTag data, String column) {
        if (data == null || column == null) {
            return null;
        }
        switch (column) {
            case "ttid":
                return data.getTtid();
            case "tnodeid":
                return data.getTnodeid();
            case "tagid":
                return data.getTagid();
        }
        return null;
    }

    public static boolean valid(InfoNodeTag data) {
        return data != null
                && data.getTnodeid() > 0 && data.getTagid() > 0;
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

    public long getTnodeid() {
        return tnodeid;
    }

    public void setTnodeid(long tnodeid) {
        this.tnodeid = tnodeid;
    }

    public long getTagid() {
        return tagid;
    }

    public void setTagid(long tagid) {
        this.tagid = tagid;
    }

    public InfoNode getNode() {
        return node;
    }

    public void setNode(InfoNode node) {
        this.node = node;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

}
