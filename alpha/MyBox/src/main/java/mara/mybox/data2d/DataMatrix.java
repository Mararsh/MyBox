package mara.mybox.data2d;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.data2d.writer.Data2DWriter;
import mara.mybox.data2d.writer.DataMatrixWriter;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.MatrixCell;
import mara.mybox.db.table.TableMatrixCell;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.NumberTools;
import static mara.mybox.value.AppVariables.blockMatrixThreshold;
import static mara.mybox.value.AppVariables.sparseMatrixThreshold;
import static mara.mybox.value.Languages.message;
import org.apache.commons.math3.linear.AbstractRealMatrix;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.OpenMapRealMatrix;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public class DataMatrix extends Data2D {

    public TableMatrixCell tableMatrixCell;

    public DataMatrix() {
        dataType = DataType.Matrix;
        tableMatrixCell = new TableMatrixCell();
    }

    public int type() {
        return type(DataType.Matrix);
    }

    public boolean isBig() {
        return colsNumber > blockMatrixThreshold;
    }

    public boolean isSparse() {
        return tableMatrixCell.size()
                < pagination.rowsNumber * colsNumber * sparseMatrixThreshold;
    }

    public AbstractRealMatrix realMatrix(Connection conn) {
        int rows = (int) pagination.rowsNumber;
        int cols = (int) colsNumber;
        if (cols > blockMatrixThreshold) {
            return new BlockRealMatrix(rows, cols);
        } else if (tableMatrixCell.size(conn) < rows * cols * sparseMatrixThreshold) {
            return new OpenMapRealMatrix(rows, cols);
        } else {
            return new Array2DRowRealMatrix(rows, cols);
        }
    }

    public void cloneAll(DataMatrix d) {
        try {
            if (d == null) {
                return;
            }
            super.cloneData(d);
            tableMatrixCell = d.tableMatrixCell;
            if (tableMatrixCell == null) {
                tableMatrixCell = new TableMatrixCell();
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public boolean checkForLoad() {
        hasHeader = false;
        return true;
    }

    @Override
    public boolean checkForSave() {
        if (dataName == null || dataName.isBlank()) {
            dataName = message("Matrix") + " "
                    + pagination.rowsNumber + "x" + colsNumber;
        }
        return true;
    }

    @Override
    public Data2DDefinition queryDefinition(Connection conn) {
        return tableData2DDefinition.queryID(conn, dataID);
    }

    @Override
    public long readTotal() {
        return pagination.rowsNumber;
    }

    @Override
    public List<String> readColumnNames() {
        checkForLoad();
        List<String> names = new ArrayList<>();
        for (int i = 1; i <= colsNumber; i++) {
            names.add(colPrefix() + i);
        }
        return names;
    }

    public String pageQuery() {
        String sql = "SELECT * FROM Matrix_Cell WHERE mcdid=" + dataID
                + " AND row >" + (pagination.startRowOfCurrentPage + 1)
                + " AND row < " + (pagination.startRowOfCurrentPage + pagination.pageSize);
        return sql;
    }

    @Override
    public long savePageData(FxTask task) {
        pagination.rowsNumber = save(null, this, columns, pageData());
        return pagination.rowsNumber;
    }

    public boolean isSquare() {
        return isValidDefinition() && tableColsNumber() == tableRowsNumber();
    }

    public String toString(double d) {
        if (DoubleTools.invalidDouble(d)) {
            return Double.NaN + "";
        } else {
            return NumberTools.format(d, scale);
        }
    }

    public static double toDouble(String d) {
        try {
            return Double.parseDouble(d.replaceAll(",", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    public double[][] toMatrix() {
        pagination.rowsNumber = tableRowsNumber();
        colsNumber = tableColsNumber();
        if (pagination.rowsNumber <= 0 || colsNumber <= 0) {
            return null;
        }
        double[][] data = new double[(int) pagination.rowsNumber][(int) colsNumber];
        for (int r = 0; r < pagination.rowsNumber; r++) {
            List<String> row = dataRow(r);
            for (int c = 0; c < row.size(); c++) {
                data[r][c] = toDouble(row.get(c));
            }
        }
        return data;
    }

    public List<List<String>> toTableData(double[][] data) {
        if (data == null) {
            return null;
        }
        List<List<String>> rows = new ArrayList<>();
        for (int r = 0; r < data.length; r++) {
            List<String> row = new ArrayList<>();
            row.add(("" + (r + 1)));
            for (int c = 0; c < data[r].length; c++) {
                row.add(toString(data[r][c]));
            }
            rows.add(row);
        }
        return rows;
    }

    @Override
    public Data2DWriter selfWriter() {
        DataMatrixWriter writer = new DataMatrixWriter();
        writer.setMatrix(this)
                .setTargetData(this)
                .setRecordTargetFile(false)
                .setRecordTargetData(true);
        return writer;
    }

    public static long save(FxTask task, DataMatrix matrix,
            List<Data2DColumn> cols, List<List<String>> rows) {
        if (matrix == null || cols == null || rows == null) {
            return -1;
        }
        try (Connection conn = DerbyBase.getConnection()) {
            return save(task, conn, matrix, cols, rows);
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return -2;
        }
    }

    public static long save(FxTask task, Connection conn, DataMatrix matrix,
            List<Data2DColumn> cols, List<List<String>> rows) {
        if (conn == null || matrix == null || cols == null || rows == null) {
            return -1;
        }
        TableMatrixCell tableData2DCell = matrix.tableMatrixCell;
        try {
            matrix.setColsNumber(cols.size());
            matrix.setRowsNumber(rows.size());
            Data2D.saveAttributes(conn, matrix, cols);
            long dataid = matrix.getDataID();
            if (dataid < 0) {
                return -2;
            }
            try (PreparedStatement clear = conn.prepareStatement(TableMatrixCell.ClearData)) {
                clear.setLong(1, dataid);
                clear.executeUpdate();
            } catch (Exception e) {
                if (task != null) {
                    task.setError(e.toString());
                }
                MyBoxLog.debug(e);
            }
            conn.commit();
            conn.setAutoCommit(false);
            long num = 0;
            for (int r = 0; r < rows.size(); r++) {
                List<String> row = rows.get(r);
                for (int c = 0; c < row.size(); c++) {
                    double d = toDouble(row.get(c));
                    if (d == 0 || DoubleTools.invalidDouble(d)) {
                        continue;
                    }
                    MatrixCell cell = MatrixCell.create().setDataID(dataid)
                            .setRowID(r).setColumnID(c).setValue(d);
                    tableData2DCell.insertData(conn, cell);
                }
                num++;
            }
            conn.commit();
            return num;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return -3;
        }
    }

    public TableMatrixCell getTableMatrixCell() {
        return tableMatrixCell;
    }

}
