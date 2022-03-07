package mara.mybox.db.data;

import java.util.HashMap;
import java.util.Map;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2020-7-19
 * @License Apache License Version 2.0
 */
public class BaseData implements Cloneable {

    protected Map<String, Object> values;
    protected int rowIndex;

    public BaseData() {
        initValues();
    }

    private void initValues() {
        values = new HashMap<>();
    }

    public boolean setValue(String column, Object value) {
        values.put(column, value);
        return true;
    }

    public Object getValue(String column) {
        try {
            return values.get(column);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public boolean isEmpty() {
        try {
            return values.keySet().isEmpty();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return true;
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        try {
            BaseData newData = (BaseData) super.clone();
            return newData;
        } catch (Exception e) {
            return null;
        }
    }

    /*
        static methods
     */
    public static boolean valid(BaseData data) {
        return data != null;
    }

    public static boolean setValue(BaseData data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        return data.setValue(column, value);
    }

    public static Object getValue(BaseData data, String column) {
        if (data == null || column == null) {
            return null;
        }
        return data.getValue(column);
    }

    /*
        get/set
     */
    public Map<String, Object> getValues() {
        return values;
    }

    public void setValues(Map<String, Object> values) {
        this.values = values;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

}
