package mara.mybox.data2d.writer;

import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataMatrix;
import static mara.mybox.data2d.DataMatrix.toDouble;
import mara.mybox.db.Database;
import mara.mybox.db.data.Data2DCell;
import mara.mybox.db.table.TableData2DCell;
import mara.mybox.tools.DoubleTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-1-29
 * @License Apache License Version 2.0
 */
public class MatrixWriter extends Data2DWriter {

    protected DataMatrix matrix;
    protected long did, dwCount;
    protected TableData2DCell tableData2DCell;

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
            if (matrix == null) {
                matrix = new DataMatrix();
                matrix.setTask(task()).setDataName(dataName);
                if (!Data2D.saveAttributes(conn, matrix, columns)) {
                    return false;
                }
            }
            tableData2DCell = matrix.getTableData2DCell();
            did = matrix.getD2did();
            conn.setAutoCommit(false);
            dwCount = 0;
            showInfo(message("Writing") + " " + matrix.dataName());
            return true;
        } catch (Exception e) {
            showError(e.toString());
            return false;
        }
    }

    @Override
    public void printTargetRow() {
        try {
            if (targetRow == null || conn == null) {
                return;
            }
            for (int c = 0; c < targetRow.size(); c++) {
                double d = toDouble(targetRow.get(c));
                if (d == 0 || DoubleTools.invalidDouble(d)) {
                    continue;
                }
                Data2DCell cell = Data2DCell.create().setD2did(did)
                        .setRow(targetRowIndex).setCol(c).setValue(d + "");
                tableData2DCell.insertData(conn, cell);
                if (++dwCount % Database.BatchSize == 0) {
                    conn.commit();
                    showInfo(message("Inserted") + ": " + dwCount);
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
            if (conn == null || matrix == null) {
                showInfo(message("Failed") + ": " + matrix.dataName());
                return;
            }
            conn.commit();
            matrix.setRowsNumber(targetRowIndex);
            Data2D.saveAttributes(conn, matrix, columns);
            targetData = matrix;
            showInfo(message("Generated") + ": " + matrix.dataName());
            showInfo(message("RowsNumber") + ": " + targetRowIndex);
            created = true;
        } catch (Exception e) {
            showError(e.toString());
        }
    }

    /*
        set/get
     */
    public DataMatrix getMatrix() {
        return matrix;
    }

    public MatrixWriter setMatrix(DataMatrix matrix) {
        this.matrix = matrix;
        return this;
    }

}
