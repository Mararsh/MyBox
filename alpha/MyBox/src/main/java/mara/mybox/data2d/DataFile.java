package mara.mybox.data2d;

import java.sql.Connection;
import java.util.List;
import mara.mybox.data2d.scan.Data2DReader;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.tools.FileTools;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public abstract class DataFile extends Data2D {

    @Override
    public Data2DDefinition queryDefinition(Connection conn) {
        if (conn == null || type == null || file == null || !file.exists()) {
            return null;
        }
        return tableData2DDefinition.queryFile(conn, type, file);
    }

    @Override
    public List<String> readColumnNames() {
        if (!FileTools.hasData(file)) {
            return null;
        }
        Data2DReader reader = Data2DReader.create(this)
                .setReaderTask(task).start(Data2DReader.Operation.ReadColumnNames);
        if (reader == null) {
            hasHeader = false;
            return null;
        }
        return reader.getNames();
    }

}
