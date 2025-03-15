package mara.mybox.data2d.modify;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.data2d.DataMatrix;
import mara.mybox.db.Database;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.MatrixCell;
import mara.mybox.db.table.TableMatrixCell;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2025-3-14
 * @License Apache License Version 2.0
 */
public class DataMatrixDelete extends DataMatrixModify {

    public DataMatrixDelete(DataMatrix data) {
        setSourceMatrix(data);
    }

    @Override
    public boolean go() {
        handledCount = 0;
        try (Connection dconn = DerbyBase.getConnection();
                PreparedStatement query = dconn.prepareStatement(TableMatrixCell.QueryRow);
                PreparedStatement delete = dconn.prepareStatement(TableMatrixCell.DeleteRow);
                Statement statement = dconn.createStatement()) {
            conn = dconn;
            conn.setAutoCommit(false);
            deleteRowStatement = delete;
            dbStatement = statement;

            showInfo(TableMatrixCell.QueryRow);

            long cellCol;
            for (sourceRowIndex = 0; sourceRowIndex < rowsNumber; sourceRowIndex++) {
                if (stopped || reachMaxFiltered) {
                    break;
                }
                sourceRow = new ArrayList<>();
                for (long c = 0; c < colsNumber; c++) {
                    sourceRow.add("0");
                }
                query.setLong(1, dataID);
                query.setLong(2, sourceRowIndex);
                try (ResultSet results = query.executeQuery()) {
                    while (results.next()) {
                        if (stopped || reachMaxFiltered) {
                            break;
                        }
                        MatrixCell cell = tableMatrixCell.readData(results);
                        cellCol = cell.getColumnID();
                        if (cellCol > -1 && cellCol < colsNumber) {
                            sourceRow.set((int) cellCol, cell.getValue() + "");
                        }
                    }
                } catch (Exception e) {
                    showError(e.toString());
                    setFailed();
                }
                if (stopped || reachMaxFiltered) {
                    break;
                }
                handleRow(sourceRow, sourceRowIndex);
            }

            if (!stopped) {
                deleteRowStatement.executeBatch();
                conn.commit();
                sourceData.setRowsNumber(rowsNumber - handledCount);
                sourceData.saveAttributes(conn);
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
            rowPassFilter = sourceData.filterDataRow(sourceRow, sourceRowIndex);
            reachMaxFiltered = sourceData.filterReachMaxPassed();
            if (rowPassFilter && !reachMaxFiltered) {
                deleteRowStatement.setLong(1, dataID);
                deleteRowStatement.setLong(2, sourceRowIndex);
                deleteRowStatement.executeUpdate();
                dbStatement.executeUpdate("UPDATE Matrix_Cell SET row=row-1 WHERE "
                        + " mcdid=" + dataID + " AND row>" + sourceRowIndex);
                if (++handledCount % Database.BatchSize == 0) {
                    conn.commit();
                    showInfo(message("Deleted") + ": " + handledCount);
                }
            }
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
    }

}
