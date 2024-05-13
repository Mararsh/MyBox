package mara.mybox.data2d.modify;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import mara.mybox.data2d.Data2D;
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
    protected int columnsNumber;
    protected Data2DRow sourceTableRow;
    protected PreparedStatement update;
    protected List<Data2DColumn> columns;

    public boolean updateTable() {
        try {
            if (sourceTable == null || conn == null) {
                return false;
            }
            String sql = "SELECT count(*) FROM " + sourceTable.getSheet();
            showInfo(sql);
            try (ResultSet query = conn.prepareStatement(sql).executeQuery()) {
                if (query.next()) {
                    rowsNumber = query.getLong(1);
                }
            }
            sourceData.setRowsNumber(rowsNumber);
            Data2D.saveAttributes(conn, sourceData, sourceData.getColumns());
            showInfo(message("DataTable") + ": " + sourceData.getSheet() + "  "
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
