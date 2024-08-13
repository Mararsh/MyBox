package mara.mybox.db.data;

import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2024-8-2
 * @License Apache License Version 2.0
 */
public class Note extends BaseData {

    protected long noteid;
    protected String title, note;

    @Override
    public boolean valid() {
        return valid(this);
    }

    @Override
    public boolean setValue(String column, Object value) {
        return setValue(this, column, value);
    }

    @Override
    public Object getValue(String column) {
        return getValue(this, column);
    }

    /*
        Static methods
     */
    public static Note create() {
        return new Note();
    }

    public static boolean setValue(Note data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "noteid":
                    data.setNoteid(value == null ? -1 : (long) value);
                    return true;
                case "title":
                    data.setTitle(value == null ? null : (String) value);
                    return true;
                case "note":
                    data.setNote(value == null ? null : (String) value);
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return false;
    }

    public static Object getValue(Note data, String column) {
        if (data == null || column == null) {
            return null;
        }
        switch (column) {
            case "noteid":
                return data.getNoteid();
            case "title":
                return data.getTitle();
            case "note":
                return data.getNote();
        }
        return null;
    }

    public static boolean valid(Note data) {
        return data != null;
    }

    /*
        get/set
     */
    public long getNoteid() {
        return noteid;
    }

    public Note setNoteid(long noteid) {
        this.noteid = noteid;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Note setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getNote() {
        return note;
    }

    public Note setNote(String note) {
        this.note = note;
        return this;
    }

}
