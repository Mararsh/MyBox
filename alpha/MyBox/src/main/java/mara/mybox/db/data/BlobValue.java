package mara.mybox.db.data;

import java.sql.Blob;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-1-17
 * @License Apache License Version 2.0
 */
public class BlobValue extends BaseData {

    protected String key;
    protected Blob value;

    private void init() {
        key = null;
        value = null;
    }

    public BlobValue() {
        init();
    }

    public BlobValue(String key, Blob value) {
        init();
        this.key = key;
        this.value = value;
    }


    /*
        Static methods
     */
    public static BlobValue create() {
        return new BlobValue();
    }

    public static boolean setValue(BlobValue data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "key_name":
                    data.setKey(value == null ? null : (String) value);
                    return true;
                case "blob_value":
                    data.setValue(value == null ? null : (Blob) value);
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return false;
    }

    public static Object getValue(BlobValue data, String column) {
        if (data == null || column == null) {
            return null;
        }
        switch (column) {
            case "key_name":
                return data.getKey();
            case "blob_value":
                return data.getValue();
        }
        return null;
    }

    public static boolean valid(BlobValue data) {
        return data != null
                && data.getKey() != null;
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

    public Blob getValue() {
        return value;
    }

    public void setValue(Blob value) {
        this.value = value;
    }

}
