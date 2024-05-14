package mara.mybox.data2d.modify;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DRow;
import mara.mybox.db.table.TableData2D;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-8-16
 * @License Apache License Version 2.0
 */
public abstract class DataTableModify extends Data2DModify {

    protected DataTable sourceTable;
    protected TableData2D tableData2D;
    protected String tableName;
    protected int columnsNumber;
    protected Data2DRow sourceTableRow;
    protected PreparedStatement update;
    protected List<Data2DColumn> columns;

    public boolean setSourceTable(DataTable data) {
        if (!setSourceData(data)) {
            return false;
        }
        sourceTable = data;
        tableName = sourceTable.getSheet();
        tableData2D = sourceTable.getTableData2D();
        tableData2D.setTableName(tableName);
        columns = sourceTable.getColumns();
        columnsNumber = columns.size();
        return true;
    }

    public boolean updateTable() {
        try {
            if (stopped || sourceTable == null || conn == null) {
                return false;
            }
            String sql = "SELECT count(*) FROM " + tableName;
            showInfo(sql);
            try (ResultSet query = conn.prepareStatement(sql).executeQuery()) {
                if (query.next()) {
                    rowsNumber = query.getLong(1);
                }
            }
            sourceData.setRowsNumber(rowsNumber);
            if (stopped) {
                return false;
            }
            sourceData.saveAttributes(conn);
            showInfo(message("DataTable") + ": " + tableName + "  "
                    + message("RowsNumber") + ": " + rowsNumber);
            return true;
        } catch (Exception e) {
            showError(e.toString());
            return false;
        }
    }

    @Override
    public boolean end() {
        return true;
    }

}
