package mara.mybox.db.data;

import java.util.HashMap;
import java.util.Map;
import mara.mybox.db.table.BaseTable;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2024-11-1
 * @License Apache License Version 2.0
 */
public class DataValues extends BaseData {

    protected BaseTable table;
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
    public BaseTable getTable() {
        return table;
    }

    public void setTable(BaseTable table) {
        this.table = table;
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public void setValues(Map<String, Object> values) {
        this.values = values;
    }

}
