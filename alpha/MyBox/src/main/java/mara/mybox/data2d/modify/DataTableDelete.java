package mara.mybox.data2d.modify;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.Database;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DRow;
import mara.mybox.db.table.TableData2D;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-1-29
 * @License Apache License Version 2.0
 */
public class DataTableDelete extends Data2DDelete {

    protected DataTable sourceTable;
    protected TableData2D tableData2D;
    protected int columnsNumber;
    protected Data2DRow sourceTableRow;
    protected Connection conn;
    protected PreparedStatement delete;
    protected List<Data2DColumn> columns;

    public DataTableDelete(DataTable data) {
        setSourceData(data);
        sourceTable = data;
    }

    @Override
    public boolean go() {
        handledCount = 0;
        tableData2D = sourceTable.getTableData2D();
        tableData2D.setTableName(sourceTable.getSheet());
        String sql = "SELECT * FROM " + sourceTable.getSheet();
        showInfo(sql);
        columns = sourceTable.getColumns();
        columnsNumber = columns.size();
        try (Connection dconn = DerbyBase.getConnection();
                PreparedStatement statement = dconn.prepareStatement(sql);
                ResultSet results = statement.executeQuery();
                PreparedStatement dDelete = conn.prepareStatement(tableData2D.deleteStatement())) {
            conn = dconn;
            conn.setAutoCommit(false);
            delete = dDelete;
            while (results.next() && !stopped && !reachMax) {
                sourceTableRow = tableData2D.readData(results);
                sourceRow = sourceTableRow.toStrings(columns);
                sourceRowIndex++;
                handleRow(sourceRow, sourceRowIndex);
            }
            delete.executeBatch();
            conn.commit();
            showInfo(message("Deleted") + ": " + handledCount);
            conn.close();
            conn = null;
            return true;
        } catch (Exception e) {
            failStop(e.toString());
            return false;
        }
    }

    @Override
    public void deleteRow(boolean needDelete) {
        try {
            if (!needDelete) {
                return;
            }
            if (tableData2D.setDeleteStatement(conn, delete, sourceTableRow)) {
                delete.addBatch();
                if (++handledCount % Database.BatchSize == 0) {
                    delete.executeBatch();
                    conn.commit();
                    showInfo(message("Deleted") + ": " + handledCount);
                }
            }
        } catch (Exception e) {
            showError(e.toString());
        }
    }

}
