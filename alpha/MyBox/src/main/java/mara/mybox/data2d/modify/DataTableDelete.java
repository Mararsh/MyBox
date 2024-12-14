package mara.mybox.data2d.modify;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import mara.mybox.data2d.DataTable;
import mara.mybox.data2d.tools.Data2DRowTools;
import mara.mybox.db.Database;
import mara.mybox.db.DerbyBase;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-1-29
 * @License Apache License Version 2.0
 */
public class DataTableDelete extends DataTableModify {

    protected PreparedStatement delete;

    public DataTableDelete(DataTable data) {
        setSourceTable(data);
    }

    @Override
    public boolean go() {
        handledCount = 0;
        String sql = "SELECT * FROM " + tableName;
        showInfo(sql);
        try (Connection dconn = DerbyBase.getConnection();
                PreparedStatement statement = dconn.prepareStatement(sql);
                ResultSet results = statement.executeQuery();
                PreparedStatement dDelete = dconn.prepareStatement(tableData2D.deleteStatement())) {
            conn = dconn;
            conn.setAutoCommit(false);
            delete = dDelete;
            while (results.next() && !stopped && !reachMax) {
                sourceTableRow = tableData2D.readData(results);
                sourceRow = Data2DRowTools.toStrings(sourceTableRow, columns);
                sourceRowIndex++;
                handleRow(sourceRow, sourceRowIndex);
            }
            if (!stopped) {
                delete.executeBatch();
                conn.commit();
                updateTable();
            }
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
    public void handleRow(List<String> row, long index) {
        try {
            sourceRow = row;
            sourceRowIndex = index;
            targetRow = null;
            passFilter = sourceData.filterDataRow(sourceRow, sourceRowIndex);
            reachMax = sourceData.filterReachMaxPassed();
            if (passFilter && !reachMax) {
                if (tableData2D.setDeleteStatement(conn, delete, sourceTableRow)) {
                    delete.addBatch();
                    if (++handledCount % Database.BatchSize == 0) {
                        delete.executeBatch();
                        conn.commit();
                        showInfo(message("Deleted") + ": " + handledCount);
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
    }

}
