package mara.mybox.db.data;

import java.util.Date;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-2-28
 * @License Apache License Version 2.0
 */
public class Note extends BaseData {

    protected long ntid, notebookid;
    protected String title, html;
    protected Date updateTime;

    private void init() {
        ntid = notebookid = -1;
        title = null;
        html = null;
        updateTime = null;
    }

    public Note() {
        init();
    }

    public Note(long notebook, String title, String html, Date time) {
        init();
        this.notebookid = notebook;
        this.title = title;
        this.html = html;
        updateTime = time == null ? new Date() : time;
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
                case "ntid":
                    data.setNtid(value == null ? -1 : (long) value);
                    return true;
                case "notebook":
                    data.setNotebook(value == null ? -1 : (long) value);
                    return true;
                case "title":
                    data.setTitle(value == null ? null : (String) value);
                    return true;
                case "html":
                    data.setHtml(value == null ? null : (String) value);
                    return true;
                case "update_time":
                    data.setUpdateTime(value == null ? null : (Date) value);
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    public static Object getValue(Note data, String column) {
        if (data == null || column == null) {
            return null;
        }
        switch (column) {
            case "ntid":
                return data.getNtid();
            case "notebook":
                return data.getNotebookid();
            case "title":
                return data.getTitle();
            case "html":
                return data.getHtml();
            case "update_time":
                return data.getUpdateTime();
        }
        return null;
    }

    public static boolean valid(Note data) {
        return data != null
                && data.getTitle() != null && !data.getTitle().isBlank()
                && data.getUpdateTime() != null;
    }

    /*
        get/set
     */
    public long getNtid() {
        return ntid;
    }

    public Note setNtid(long ntid) {
        this.ntid = ntid;
        return this;
    }

    public long getNotebookid() {
        return notebookid;
    }

    public Note setNotebook(long notebook) {
        this.notebookid = notebook;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Note setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getHtml() {
        return html;
    }

    public Note setHtml(String html) {
        this.html = html;
        return this;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public Note setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
        return this;
    }

}
