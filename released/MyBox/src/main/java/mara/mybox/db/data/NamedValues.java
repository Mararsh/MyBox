package mara.mybox.db.data;

import java.util.Date;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-3-8
 * @License Apache License Version 2.0
 */
public class NamedValues extends BaseData {

    protected String key, name, value;
    protected Date time;

    public NamedValues() {
        init();
    }

    private void init() {
        key = null;
        name = null;
        value = null;
        time = new Date();
    }

    public NamedValues(String key, String name, String value, Date time) {
        this.key = key;
        this.name = name;
        this.value = value;
        this.time = time;
    }

    /*
        Static methods
     */
    public static NamedValues create() {
        return new NamedValues();
    }

    public static boolean setValue(NamedValues data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "key_name":
                    data.setKey(value == null ? null : (String) value);
                    return true;
                case "value":
                    data.setValue(value == null ? null : (String) value);
                    return true;
                case "value_name":
                    data.setName(value == null ? null : (String) value);
                    return true;
                case "update_time":
                    data.setTime(value == null ? null : (Date) value);
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return false;
    }

    public static Object getValue(NamedValues data, String column) {
        if (data == null || column == null) {
            return null;
        }
        switch (column) {
            case "key_name":
                return data.getKey();
            case "value":
                return data.getValue();
            case "value_name":
                return data.getName();
            case "update_time":
                return data.getTime();
        }
        return null;
    }

    public static boolean valid(NamedValues data) {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
