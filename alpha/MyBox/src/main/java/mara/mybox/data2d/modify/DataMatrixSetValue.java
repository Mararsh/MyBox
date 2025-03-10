package mara.mybox.data2d.modify;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import mara.mybox.data.SetValue;
import mara.mybox.data2d.DataMatrix;
import static mara.mybox.data2d.DataMatrix.toDouble;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2DCell;
import static mara.mybox.value.Languages.message;
import org.apache.commons.math3.linear.AbstractRealMatrix;

/**
 * @Author Mara
 * @CreateDate 2022-1-29
 * @License Apache License Version 2.0
 */
public class DataMatrixSetValue extends DataMatrixModify {

    public DataMatrixSetValue(DataMatrix data, SetValue value) {
        if (!setSourceMatrix(data)) {
            return;
        }
        initSetValue(value);
        sourceMatrix = data;
    }

    @Override
    public boolean go() {
        handledCount = 0;
        int rowsNumber = (int) sourceMatrix.pagination.rowsNumber;
        int colsNumber = (int) sourceMatrix.colsNumber;
        String sql = "SELECT * FROM Data2D_Cell WHERE dcdid=" + sourceMatrix.dataID;
        showInfo(sql);
        AbstractRealMatrix values = sourceMatrix.realMatrix(conn());
        try (PreparedStatement statement = conn().prepareStatement(sql);
                ResultSet results = statement.executeQuery()) {
            int cellCol, cellRow;
            while (results.next() && !isStopped()) {
                Data2DCell cell = sourceMatrix.tableData2DCell.readData(results);
                cellRow = (int) cell.getRowID();
                cellCol = (int) cell.getColumnID();
                if (cellCol > -1 && cellCol < colsNumber) {
                    values.addToEntry(cellRow, cellCol, toDouble(cell.getValue()));
                }
            }
        } catch (Exception e) {
            showError(e.toString());
            setFailed();
        }
        try (Connection dconn = DerbyBase.getConnection();
                PreparedStatement dUpdate = dconn.prepareStatement(sourceMatrix.tableData2DCell.updateStatement())) {
            conn = dconn;
            conn.setAutoCommit(false);
            update = dUpdate;
            for (int r = 0; r < rowsNumber; r++) {
                sourceRow = new ArrayList<>();
                for (int c = 0; c < colsNumber; c++) {
                    sourceRow.add(values.getEntry(r, c) + "");
                }
                sourceRowIndex++;
                setValue(sourceRow, sourceRowIndex);
            }
            if (!stopped) {
                update.executeBatch();
                conn.commit();
                updateMatrix();
            }
            showInfo(message("Updated") + ": " + handledCount);
            conn.close();
            conn = null;
            return true;
        } catch (Exception e) {
            failStop(e.toString());
            return false;
        }
    }

    @Override
    public void writeRow() {
        try {
            if (stopped || !rowChanged
                    || targetRow == null || targetRow.isEmpty()) {
                return;
            }
//            Data2DRow data2DRow = tableData2D.newRow();
//            for (int i = 0; i < columnsNumber; ++i) {
//                column = columns.get(i);
//                columnName = column.getColumnName();
//                data2DRow.setValue(columnName, column.fromString(targetRow.get(i), invalidAs));
//            }
//            if (tableData2D.setUpdateStatement(conn, update, data2DRow)) {
//                update.addBatch();
//                if (handledCount % Database.BatchSize == 0) {
//                    update.executeBatch();
//                    conn.commit();
//                    showInfo(message("Updated") + ": " + handledCount);
//                }
//            }
        } catch (Exception e) {
            showError(e.toString());
        }
    }

}
