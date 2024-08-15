package mara.mybox.db.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author Mara
 * @CreateDate 2021-10-17
 * @License Apache License Version 2.0
 */
public class Data2DRow extends BaseData {

    @Override
    public boolean valid() {
        return true;
    }

    @Override
    public boolean setValue(String column, Object value) {
        return setMapValue(column, value);
    }

    @Override
    public Object getValue(String column) {
        return getMapValue(column);
    }

    public List<String> toStrings(List<Data2DColumn> columns) {
        List<String> row = new ArrayList<>();
        for (Data2DColumn column : columns) {
            Object value = getMapValue(column.getColumnName());
            row.add(column.toString(value));
        }
        return row;
    }

    public Map<String, String> toNameValues(List<Data2DColumn> columns) {
        Map<String, String> values = new HashMap<>();
        for (Data2DColumn column : columns) {
            Object value = getMapValue(column.getColumnName());
            values.put(column.getColumnName(), column.toString(value));
        }
        return values;
    }

}
