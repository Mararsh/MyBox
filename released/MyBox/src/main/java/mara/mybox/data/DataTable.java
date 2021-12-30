package mara.mybox.data;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.controller.ControlDataConvert;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.table.TableData2DCell;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public class DataTable extends Data2D {

    protected TableData2DCell tableData2DCell;

    public DataTable() {
        type = Type.Table;
        tableData2DCell = new TableData2DCell();
    }

    public int type() {
        return type(Type.Table);
    }

    @Override
    public Data2DDefinition queryDefinition(Connection conn) {
        return tableData2DDefinition.queryID(conn, d2did);
    }

    @Override
    public void applyOptions() {
    }

    @Override
    public List<String> readColumns() {
        checkForLoad();
        if (matrix != null) {
            colsNumber = matrix != null ? matrix[0].length : 0;
        }
        List<String> names = new ArrayList<>();
        for (int i = 1; i <= colsNumber; i++) {
            names.add(colPrefix() + i);
        }
        return names;
    }

    @Override
    public long readTotal() {
        return dataSize;
    }

    @Override
    public List<List<String>> readPageData() {
        return null;
    }

    @Override
    public boolean savePageData(Data2D targetData) {
        return false;
    }

    @Override
    public boolean export(ControlDataConvert convertController, List<Integer> colIndices) {
        return false;
    }

}
