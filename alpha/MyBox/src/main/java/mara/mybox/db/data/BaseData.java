package mara.mybox.db.data;

import java.util.HashMap;
import java.util.Map;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2020-7-19
 * @License Apache License Version 2.0
 */
public abstract class BaseData implements Cloneable {

    protected Map<String, Object> mapValues;
    protected int rowIndex;

    /*
        abstract
     */
    public abstract boolean valid();

    public abstract boolean setValue(String column, Object value);

    public abstract Object getValue(String column);

    /*
        Map
     */
    public boolean setMapValue(String column, Object value) {
        try {
            if (mapValues == null) {
                mapValues = new HashMap<>();
            }
            mapValues.put(column, value);
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    public Object getMapValue(String column) {
        try {
            return mapValues.get(column);
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public boolean isMapEmpty() {
        try {
            return mapValues == null || mapValues.keySet().isEmpty();
        } catch (Exception e) {
//            MyBoxLog.debug(e);
            return true;
        }
    }

    /*
        others
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        try {
            BaseData newData = (BaseData) super.clone();
            return newData;
        } catch (Exception e) {
            return null;
        }
    }

    public String label(String columnName) {
        return columnName;
    }

    /*
        static methods
     */
    public static boolean setColumnValue(BaseData data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        return data.setMapValue(column, value);
    }

    public static Object getColumnValue(BaseData data, String column) {
        if (data == null || column == null) {
            return null;
        }
        return data.getMapValue(column);
    }

    /*
        get/set
     */
    public Map<String, Object> getMapValues() {
        return mapValues;
    }

    public void setColumValues(Map<String, Object> values) {
        this.mapValues = values;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

}
