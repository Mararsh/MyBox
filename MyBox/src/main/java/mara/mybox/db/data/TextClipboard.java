/*
 * Apache License Version 2.0
 */
package mara.mybox.db.data;

import java.util.Date;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-7-3
 * @License Apache License Version 2.0
 */
public class TextClipboard extends BaseData {

    protected long tcid, length;
    protected String text;
    protected Date createTime;

    private void init() {
        createTime = new Date();
    }

    public TextClipboard() {
        init();
    }

    public TextClipboard(String text) {
        init();
        this.text = text;
    }

    /*
        Static methods
     */
    public static TextClipboard create() {
        return new TextClipboard();
    }

    public static boolean setValue(TextClipboard data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "tcid":
                    data.setTcid(value == null ? -1 : (long) value);
                    return true;
                case "text":
                    data.setText(value == null ? null : (String) value);
                    return true;
                case "create_time":
                    data.setCreateTime(value == null ? null : (Date) value);
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    public static Object getValue(TextClipboard data, String column) {
        if (data == null || column == null) {
            return null;
        }
        switch (column) {
            case "tcid":
                return data.getTcid();
            case "text":
                return data.getText();
            case "create_time":
                return data.getCreateTime();
        }
        return null;
    }

    public static boolean valid(TextClipboard data) {
        return data != null && data.getText() != null;
    }

    /*
        get/set
     */
    public long getTcid() {
        return tcid;
    }

    public void setTcid(long tcid) {
        this.tcid = tcid;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public long getLength() {
        if (text == null) {
            length = 0;
        } else {
            length = text.length();
        }
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

}
