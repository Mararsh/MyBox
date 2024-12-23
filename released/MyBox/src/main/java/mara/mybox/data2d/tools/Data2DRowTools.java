package mara.mybox.data2d.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DRow;

/**
 * @Author Mara
 * @CreateDate 2023-9-12
 * @License Apache License Version 2.0
 */
public class Data2DRowTools {

    public static List<String> toStrings(Data2DRow drow, List<Data2DColumn> columns) {
        List<String> row = new ArrayList<>();
        for (Data2DColumn column : columns) {
            Object value = drow.getValue(column.getColumnName());
            row.add(column.toString(value));
        }
        return row;
    }

    public static Map<String, String> toNameValues(Data2DRow drow, List<Data2DColumn> columns) {
        Map<String, String> values = new HashMap<>();
        for (Data2DColumn column : columns) {
            Object value = drow.getValue(column.getColumnName());
            values.put(column.getColumnName(), column.toString(value));
        }
        return values;
    }

}
