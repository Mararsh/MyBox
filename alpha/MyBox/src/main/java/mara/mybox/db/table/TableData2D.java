package mara.mybox.db.table;

import java.util.List;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DRow;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-2-12
 * @License Apache License Version 2.0
 */
public class TableData2D extends BaseTable<Data2DRow> {

    public Data2DRow from(List<String> values) {
        try {
            if (columns == null || values == null || values.isEmpty()) {
                return null;
            }
            Data2DRow row = new Data2DRow();
            row.setIndex(Integer.valueOf(values.get(0)));
            for (int i = 0; i < Math.min(columns.size(), values.size() - 1); i++) {
                ColumnDefinition column = columns.get(i);
                String name = column.getColumnName();
                String value = values.get(i + 1);
                if (value != null) {
                    row.setValue(name, column.fromString(value));
                }
            }
            return row;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    /*
        static
     */
    public static String tableDefinition(String tableName) {
        try {
            TableData2D table = new TableData2D();
            table.readDefinitionFromDB(tableName);
            return table.html();
        } catch (Exception e) {
            MyBoxLog.console(e);
            return null;
        }
    }

}
