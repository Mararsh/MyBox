package mara.mybox.db.data;

import java.util.Date;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2020-10-4
 * @License Apache License Version 2.0
 */
public class StringValues extends BaseData {

    protected String key, value;
    protected Date time;

    public StringValues() {
        init();
    }

    private void init() {
        key = null;
        value = null;
        time = new Date();
    }

    public StringValues(String key, String value, Date time) {
        this.key = key;
        this.value = value;
        this.time = time;
    }

    /*
        Static methods
     */
    public static StringValues create() {
        return new StringValues();
    }

    public static boolean setValue(StringValues data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "key_name":
                    data.setKey(value == null ? null : (String) value);
                    return true;
                case "string_value":
                    data.setValue(value == null ? null : (String) value);
                    return true;
                case "create_time":
                    data.setTime(value == null ? null : (Date) value);
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return false;
    }

    public static Object getValue(StringValues data, String column) {
        if (data == null || column == null) {
            return null;
        }
        switch (column) {
            case "key_name":
                return data.getKey();
            case "string_value":
                return data.getValue();
            case "create_time":
                return data.getTime();
        }
        return null;
    }

    public static boolean valid(StringValues data) {
        return data != null
                && data.getKey() != null && data.getValue() != null;
    }

    /*
        get/set
     */
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

}
