package mara.mybox.db.data;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author Mara
 * @CreateDate 2021-10-17
 * @License Apache License Version 2.0
 */
public class Data2DRow extends BaseData {

    public List<String> toStrings(List<Data2DColumn> columns) {
        List<String> row = new ArrayList<>();
        for (Data2DColumn column : columns) {
            Object value = getColumnValue(column.getColumnName());
            row.add(column.toString(value));
        }
        return row;
    }

}
