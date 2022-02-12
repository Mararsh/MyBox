package mara.mybox.db.data;

import java.util.HashMap;
import java.util.Map;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-10-17
 * @License Apache License Version 2.0
 */
public class Data2DRow extends BaseData {

    protected Map<String, Object> values;
    protected int index;

    private void init() {
        values = new HashMap<>();
    }

    public Data2DRow() {
        init();
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

    /*
        static methods
     */
    public static Data2DRow create() {
        return new Data2DRow();
    }

    public static boolean valid(Data2DRow data) {
        return data != null;
    }

    public static boolean setValue(Data2DRow data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        return data.setValue(column, value);
    }

    public static Object getValue(Data2DRow data, String column) {
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

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

}
