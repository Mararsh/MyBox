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

    protected Map<String, Object> columnValues;
    protected int rowIndex;

    public BaseData() {
        initValues();
    }

    private void initValues() {
        columnValues = new HashMap<>();
    }

    public boolean setColumnValue(String column, Object value) {
        try {
            columnValues.put(column, value);
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    public Object getColumnValue(String column) {
        try {
            return columnValues.get(column);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public boolean isEmpty() {
        try {
            return columnValues.keySet().isEmpty();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return true;
        }
    }

    public String values() {
        try {
            return columnValues.toString();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
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

    public String label(String columnName) {
        return columnName;
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
        return data.setColumnValue(column, value);
    }

    public static Object getValue(BaseData data, String column) {
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
