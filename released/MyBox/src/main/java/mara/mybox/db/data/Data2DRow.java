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

    @Override
    public boolean valid() {
        return true;
    }

    @Override
    public boolean setValue(String column, Object value) {
        try {
            if (values == null) {
                values = new HashMap<>();
            }
            values.put(column, value);
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    @Override
    public Object getValue(String column) {
        try {
            return values.get(column);
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public boolean isEmpty() {
        try {
            return values == null || values.keySet().isEmpty();
        } catch (Exception e) {
//            MyBoxLog.debug(e);
            return true;
        }
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

}
