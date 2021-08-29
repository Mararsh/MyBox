package mara.mybox.db.data;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.table.TableDataDefinition;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-8-25
 * @License Apache License Version 2.0
 */
public class DataClipboard extends DataDefinition {

    /*
        static methods
     */
    public static int checkValid(TableDataDefinition tableDataDefinition) {
        String sql = "SELECT * FROM Data_Definition WHERE data_type="
                + DataDefinition.dataType(DataDefinition.DataType.DataClipboard);
        int count = 0;
        try ( Connection conn = DerbyBase.getConnection()) {
            List<DataDefinition> invalid = new ArrayList<>();
            try ( PreparedStatement statement = conn.prepareStatement(sql);
                     ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    DataDefinition data = tableDataDefinition.readData(results);
                    try {
                        File file = new File(data.getDataName());
                        if (file.exists()) {
                            count++;
                        } else {
                            invalid.add(data);
                        }
                    } catch (Exception e) {
                        invalid.add(data);
                    }
                }
            }
            tableDataDefinition.deleteData(conn, invalid);
            conn.commit();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return count;
    }

    public static int size(TableDataDefinition tableDataDefinition) {
        String condition = "data_type=" + DataDefinition.dataType(DataDefinition.DataType.DataClipboard);
        return tableDataDefinition.conditionSize(condition);
    }

    public static List<DataDefinition> queryPage(TableDataDefinition tableDataDefinition, int start, int size) {
        String condition = "data_type=" + DataDefinition.dataType(DataDefinition.DataType.DataClipboard);
        return tableDataDefinition.queryConditions(condition, start, size);
    }

}
