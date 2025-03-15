package mara.mybox.data2d.modify;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import mara.mybox.data.SetValue;
import mara.mybox.data2d.DataMatrix;
import mara.mybox.db.Database;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.MatrixCell;
import mara.mybox.db.table.TableMatrixCell;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2025-3-12
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
        try (Connection rconn = DerbyBase.getConnection();
                PreparedStatement query = rconn.prepareStatement(TableMatrixCell.QueryRow);
                PreparedStatement delete = rconn.prepareStatement(TableMatrixCell.DeleteRow);
                PreparedStatement insert = rconn.prepareStatement(tableMatrixCell.insertStatement())) {
            conn = rconn;
            conn.setAutoCommit(false);
            deleteRowStatement = delete;
            insertCellStatement = insert;
            showInfo(TableMatrixCell.QueryRow);
            long cellCol;
            for (sourceRowIndex = 0; sourceRowIndex < rowsNumber; sourceRowIndex++) {
                if (stopped) {
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
                        if (stopped) {
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
            deleteRowStatement.setLong(1, dataID);
            deleteRowStatement.setLong(2, sourceRowIndex);
            deleteRowStatement.addBatch();
            for (int c = 0; c < columnsNumber; ++c) {
                column = columns.get(c);
                Object v = column.fromString(targetRow.get(c), invalidAs);
                if (v == null || (double) v == 0d) {
                    continue;
                }
                MatrixCell cell = MatrixCell.create()
                        .setDataID(dataID)
                        .setRowID(sourceRowIndex)
                        .setColumnID(c)
                        .setValue((double) v);
                if (tableMatrixCell.setInsertStatement(conn, insertCellStatement, cell)) {
                    insertCellStatement.addBatch();
                }
            }
            if (handledCount % Database.BatchSize == 0) {
                deleteRowStatement.executeBatch();
                insertCellStatement.executeBatch();
                conn.commit();
                showInfo(message("Updated") + ": " + handledCount);
            }
        } catch (Exception e) {
            showError(e.toString());
        }
    }

}
