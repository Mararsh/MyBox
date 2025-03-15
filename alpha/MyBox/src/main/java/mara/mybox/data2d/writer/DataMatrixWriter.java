package mara.mybox.data2d.writer;

import java.sql.PreparedStatement;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataMatrix;
import mara.mybox.db.Database;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.MatrixCell;
import mara.mybox.db.table.TableMatrixCell;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-1-29
 * @License Apache License Version 2.0
 */
public class DataMatrixWriter extends Data2DWriter {

    protected DataMatrix targetMatrix;
    protected TableMatrixCell tableData2DCell;
    protected long dataID, rowsNumber, colsNumber;
    protected Data2DColumn column;
    protected PreparedStatement insert;

    @Override
    public boolean openWriter() {
        try {
            if (!super.openWriter()) {
                return false;
            }
            conn = conn();
            if (conn == null) {
                return false;
            }
            if (targetMatrix == null) {
                targetMatrix = new DataMatrix();
                targetMatrix.setTask(task()).setDataName(dataName);
            }
            if (dataID < 0) {
                if (!Data2D.saveAttributes(conn, targetMatrix, columns)) {
                    return false;
                }
            } else {
                columns = targetMatrix.getColumns();
            }
            dataID = targetMatrix.dataID;
            if (dataID < 0) {
                return false;
            }
            tableData2DCell = targetMatrix.tableMatrixCell;
            rowsNumber = targetMatrix.pagination.rowsNumber;
            colsNumber = columns.size();

            conn.setAutoCommit(false);
            targetRowIndex = 0;
            String sql = tableData2DCell.insertStatement();
            showInfo(sql);
            insert = conn.prepareStatement(sql);
            targetData = targetMatrix;
            validateValue = true;
            showInfo(message("Writing") + " " + targetMatrix.getName());
            return true;
        } catch (Exception e) {
            showError(e.toString());
            return false;
        }
    }

    @Override
    public void printTargetRow() {
        try {
            if (printRow == null || conn == null) {
                return;
            }
            for (int c = 0; c < colsNumber; ++c) {
                column = columns.get(c);
                Object v = column.fromString(printRow.get(c), invalidAs);
                if (v == null || (double) v == 0d) {
                    continue;
                }
                MatrixCell cell = MatrixCell.create()
                        .setDataID(dataID)
                        .setRowID(targetRowIndex)
                        .setColumnID(c)
                        .setValue((double) v);
                if (tableData2DCell.setInsertStatement(conn, insert, cell)) {
                    insert.addBatch();
                    if (++targetRowIndex % Database.BatchSize == 0) {
                        insert.executeBatch();
                        conn.commit();
                        showInfo(message("Inserted") + ": " + targetRowIndex);
                    }
                }
            }
        } catch (Exception e) {
            showError(e.toString());
        }
    }

    @Override
    public void closeWriter() {
        try {
            created = false;
            if (conn == null || targetMatrix == null) {
                showInfo(message("Failed"));
                return;
            }
            insert.executeBatch();
            conn.commit();
            insert.close();
            targetMatrix.setRowsNumber(targetRowIndex);
            Data2D.saveAttributes(conn, targetMatrix, targetMatrix.getColumns());
            targetData = targetMatrix;
            showInfo(message("Generated") + ": " + targetMatrix.getName() + "  "
                    + message("RowsNumber") + ": " + targetRowIndex);
            created = true;
        } catch (Exception e) {
            showError(e.toString());
        }
    }

    /*
        set/get
     */
    public DataMatrix getTargetMatrix() {
        return targetMatrix;
    }

    public DataMatrixWriter setMatrix(DataMatrix matrix) {
        this.targetMatrix = matrix;
        return this;
    }

}
