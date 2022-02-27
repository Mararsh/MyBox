package mara.mybox.db.table;

import mara.mybox.db.data.Data2DRow;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-2-12
 * @License Apache License Version 2.0
 */
public class TableData2D extends BaseTable<Data2DRow> {

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
