package mara.mybox.db.data;

import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-2-28
 * @License Apache License Version 2.0
 */
public class NoteTag extends BaseData {

    protected long ngid;
    protected String tag;

    private void init() {
        ngid = -1;
        tag = null;
    }

    public NoteTag() {
        init();
    }

    public NoteTag(String tag) {
        init();
        this.tag = tag;
    }

    /*
        Static methods
     */
    public static NoteTag create() {
        return new NoteTag();
    }

    public static boolean setValue(NoteTag data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "ngid":
                    data.setNgid(value == null ? -1 : (long) value);
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

    public static Object getValue(NoteTag data, String column) {
        if (data == null || column == null) {
            return null;
        }
        switch (column) {
            case "ngid":
                return data.getNgid();
            case "tag":
                return data.getTag();
        }
        return null;
    }

    public static boolean valid(NoteTag data) {
        return data != null
                && data.getTag() != null && !data.getTag().isBlank();
    }

    /*
        get/set
     */
    public long getNgid() {
        return ngid;
    }

    public void setNgid(long ngid) {
        this.ngid = ngid;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

}
