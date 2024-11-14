package mara.mybox.db.data;

import java.util.HashMap;
import java.util.Map;
import mara.mybox.db.table.BaseDataTable;

/**
 * @Author Mara
 * @CreateDate 2024-11-1
 * @License Apache License Version 2.0
 */
public class DataValues extends BaseData {

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
            return false;
        }
    }

    @Override
    public Object getValue(String column) {
        try {
            return values.get(column);
        } catch (Exception e) {
            return null;
        }
    }

    public long getId(BaseDataTable dataTable) {
        try {
            return (long) getValue(dataTable.getIdColumnName());
        } catch (Exception e) {
            return -1;
        }
    }

    public boolean setId(BaseDataTable dataTable, long id) {
        try {
            return setValue(dataTable.getIdColumnName(), id);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isEmpty() {
        try {
            return values == null || values.keySet().isEmpty();
        } catch (Exception e) {
            return true;
        }
    }

    public DataValues copy() {
        try {
            DataValues data = new DataValues();
            for (String key : values.keySet()) {
                data.setValue(key, values.get(key));
            }
            return data;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String toString() {
        try {
            String s = "";
            for (String key : values.keySet()) {
                s += key + ": " + values.get(key) + "\n";
            }
            return s;
        } catch (Exception e) {
            return null;
        }
    }

    /*
        get/set
     */
    public Map<String, Object> getValues() {
        return values;
    }

    public DataValues setValues(Map<String, Object> values) {
        this.values = values;
        return this;
    }

}
