package mara.mybox.db.data;

import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-3-20
 * @License Apache License Version 2.0
 */
public class Tag extends BaseData {

    protected long tgid;
    protected String tag;

    private void init() {
        tgid = -1;
        tag = null;
    }

    public Tag() {
        init();
    }

    public Tag(String tag) {
        init();
        this.tag = tag;
    }

    /*
        Static methods
     */
    public static Tag create() {
        return new Tag();
    }

    public static boolean setValue(Tag data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "tgid":
                    data.setTgid(value == null ? -1 : (long) value);
                    return true;
                case "tag":
                    data.setTag(value == null ? null : (String) value);
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    public static Object getValue(Tag data, String column) {
        if (data == null || column == null) {
            return null;
        }
        switch (column) {
            case "tgid":
                return data.getTgid();
            case "tag":
                return data.getTag();
        }
        return null;
    }

    public static boolean valid(Tag data) {
        return data != null
                && data.getTag() != null && !data.getTag().isBlank();
    }

    /*
        get/set
     */
    public long getTgid() {
        return tgid;
    }

    public Tag setTgid(long tgid) {
        this.tgid = tgid;
        return this;
    }

    public String getTag() {
        return tag;
    }

    public Tag setTag(String tag) {
        this.tag = tag;
        return this;
    }

}
