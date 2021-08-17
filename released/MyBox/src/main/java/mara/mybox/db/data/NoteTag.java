package mara.mybox.db.data;

import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-2-28
 * @License Apache License Version 2.0
 */
public class NoteTag extends BaseData {

    protected long ngid, noteid, tagid;
    protected Note note;
    protected Tag tag;

    private void init() {
        ngid = noteid = tagid = -1;
        note = null;
        tag = null;
    }

    public NoteTag() {
        init();
    }

    public NoteTag(long noteid, long tagid) {
        init();
        this.noteid = noteid;
        this.tagid = tagid;
    }

    public NoteTag(Note note, Tag tag) {
        init();
        this.note = note;
        this.tag = tag;
        this.noteid = note == null ? -1 : note.getNtid();
        this.tagid = tag == null ? -1 : tag.getTgid();
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
                case "noteid":
                    data.setNoteid(value == null ? -1 : (long) value);
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

    public static Object getValue(NoteTag data, String column) {
        if (data == null || column == null) {
            return null;
        }
        switch (column) {
            case "ngid":
                return data.getNgid();
            case "noteid":
                return data.getNoteid();
            case "tagid":
                return data.getTagid();
        }
        return null;
    }

    public static boolean valid(NoteTag data) {
        return data != null
                && data.getNoteid() > 0 && data.getTagid() > 0;
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

    public long getNoteid() {
        return noteid;
    }

    public void setNoteid(long noteid) {
        this.noteid = noteid;
    }

    public long getTagid() {
        return tagid;
    }

    public void setTagid(long tagid) {
        this.tagid = tagid;
    }

    public Note getNote() {
        return note;
    }

    public void setNote(Note note) {
        this.note = note;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

}
