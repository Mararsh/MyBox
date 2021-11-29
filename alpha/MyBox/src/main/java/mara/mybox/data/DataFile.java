package mara.mybox.data;

import java.sql.Connection;
import mara.mybox.db.data.Data2DDefinition;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public abstract class DataFile extends Data2D {

    @Override
    public Data2DDefinition queryDefinition(Connection conn) {
        return tableData2DDefinition.queryFile(conn, type, file);
    }

}
