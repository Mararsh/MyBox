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

    protected Map<String, Object> columnValues;
    protected int rowIndex;

    public abstract boolean valid();

    public abstract boolean setValue(String column, Object value);

    public abstract Object getValue(String column);

    public boolean setColumnValue(String column, Object value) {
        try {
            if (columnValues == null) {
                columnValues = new HashMap<>();
            }
            columnValues.put(column, value);
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    public Object getColumnValue(String column) {
        try {
            return columnValues.get(column);
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public boolean isNoColumn() {
        try {
            return columnValues == null || columnValues.keySet().isEmpty();
        } catch (Exception e) {
//            MyBoxLog.debug(e);
            return true;
        }
    }

    public String values() {
        try {
            return columnValues.toString();
        } catch (Exception e) {
//            MyBoxLog.debug(e);
            return null;
        }
    }

    public Map<String, String> nameValues() {
        Map<String, String> values = new HashMap<>();
        for (String name : columnValues.keySet()) {
            Object value = getColumnValue(name);
            values.put(name, value != null ? value.toString() : null);
        }
        return values;
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
        return data.setColumnValue(column, value);
    }

    public static Object getColumnValue(BaseData data, String column) {
        if (data == null || column == null) {
            return null;
        }
        return data.getColumnValue(column);
    }

    /*
        get/set
     */
    public Map<String, Object> getColumnValues() {
        return columnValues;
    }

    public void setColumValues(Map<String, Object> values) {
        this.columnValues = values;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

}
