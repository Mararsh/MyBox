package mara.mybox.data2d.modify;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import mara.mybox.data.SetValue;
import mara.mybox.data2d.DataMatrix;
import static mara.mybox.data2d.DataMatrix.toDouble;
import mara.mybox.db.Database;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2DCell;
import static mara.mybox.value.Languages.message;

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
        String sql = "SELECT * FROM Data2D_Cell WHERE dcdid=" + sourceMatrix.dataID
                + " AND row=?";
        showInfo(sql);
        try (Connection rconn = DerbyBase.getConnection();
                PreparedStatement query = rconn.prepareStatement(sql)) {
            conn = rconn;
            conn.setAutoCommit(false);

            long cellCol;
            for (sourceRowIndex = 0; sourceRowIndex < rowsNum; sourceRowIndex++) {
                if (stopped) {
                    break;
                }
                sourceRow = new ArrayList<>();
                for (long c = 0; c < colsNum; c++) {
                    sourceRow.add("0");
                }
                query.setLong(1, sourceRowIndex);
                try (ResultSet results = query.executeQuery()) {
                    while (results.next()) {
                        if (stopped) {
                            break;
                        }
                        Data2DCell cell = sourceMatrix.tableData2DCell.readData(results);
                        cellCol = cell.getColumnID();
                        if (cellCol > -1 && cellCol < colsNum) {
                            sourceRow.set((int) cellCol, toDouble(cell.getValue()) + "");
                        }
                    }
                } catch (Exception e) {
                    showError(e.toString());
                    setFailed();
                }
                if (stopped) {
                    break;
                }
                setValue(sourceRow, sourceRowIndex);
            }

            if (!stopped) {
                conn.commit();
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
            for (int c = 0; c < columnsNumber; ++c) {
                column = columns.get(c);
                columnName = column.getColumnName();
                Object v = column.fromString(targetRow.get(c), invalidAs);
                if (v == null || (double) v == 0d) {
                    continue;
                }
                Data2DCell cell = Data2DCell.create()
                        .setDataID(dataID)
                        .setRowID(sourceRowIndex)
                        .setColumnID(c)
                        .setValue(v + "");
                if (tableData2DCell.writeData(conn, cell) != null) {
                    if (handledCount % Database.BatchSize == 0) {
                        conn.commit();
                        showInfo(message("Updated") + ": " + handledCount);
                    }
                }
            }
        } catch (Exception e) {
            showError(e.toString());
        }
    }

}
