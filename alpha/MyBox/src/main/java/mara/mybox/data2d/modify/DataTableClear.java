package mara.mybox.data2d.modify;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import mara.mybox.data2d.DataTable;
import mara.mybox.data2d.operate.Data2DOperate;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DRow;
import mara.mybox.db.table.TableData2D;

/**
 * @Author Mara
 * @CreateDate 2022-1-29
 * @License Apache License Version 2.0
 */
public class DataTableClear extends Data2DOperate {

    protected DataTable sourceTable;
    protected int columnsNumber;
    protected Data2DRow sourceTableRow;
    protected Connection conn;
    protected PreparedStatement delete;
    protected List<Data2DColumn> columns;

    public DataTableClear(DataTable data) {
        setSourceData(data);
        sourceTable = data;
    }

    @Override
    public boolean go() {
        try {
            String sql = "DELETE FROM " + sourceTable.getSheet();
            showInfo(sql);
            TableData2D tableData2D = sourceTable.getTableData2D();
            tableData2D.setTableName(sourceTable.getSheet());
            handledCount = tableData2D.clearData();
            return handledCount >= 0;
        } catch (Exception e) {
            showError(e.toString());
            return false;
        }
    }

}
