package mara.mybox.data2d.reader;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import mara.mybox.data2d.DataMatrix;
import static mara.mybox.data2d.DataMatrix.toDouble;
import mara.mybox.db.data.Data2DCell;
import org.apache.commons.math3.linear.AbstractRealMatrix;

/**
 * @Author Mara
 * @CreateDate 2022-1-29
 * @License Apache License Version 2.0
 */
public class DataMatrixReader extends Data2DReader {

    protected DataMatrix sourceMatrix;

    public DataMatrixReader(DataMatrix data) {
        sourceMatrix = data;
        sourceData = data;
    }

    @Override
    public void readColumnNames() {
        names = sourceMatrix.columnNames();
    }

    @Override
    public void readTotal() {
        sourceIndex = sourceMatrix.pagination.rowsNumber;
    }

    @Override
    public void readPage() {
        int rowsNumber = (int) sourceMatrix.pagination.pageSize;
        int colsNumber = (int) sourceMatrix.colsNumber;
        int endRow = -1;
        sourceIndex = sourceMatrix.pagination.startRowOfCurrentPage;
        String sql = "SELECT * FROM Data2D_Cell WHERE dcdid=" + sourceMatrix.dataID
                + " AND row >" + (sourceIndex - 1)
                + " AND row < " + (sourceIndex + rowsNumber);
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
                    if (cellRow > endRow) {
                        endRow = cellRow;
                    }
                }
            }

        } catch (Exception e) {
            showError(e.toString());
            setFailed();
        }
        for (int r = (int) sourceIndex; r < endRow; r++) {
            sourceRow = new ArrayList<>();
            for (int c = 0; c < colsNumber; c++) {
                sourceRow.add(values.getEntry(r, c) + "");
            }
            sourceIndex++;
            makePageRow();
        }
    }

    @Override
    public void readRows() {
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
        try {
            sourceIndex = 0;
            long startIndex = sourceMatrix.pagination.startRowOfCurrentPage;
            long endIndex = sourceMatrix.pagination.endRowOfCurrentPage;
            int tableIndex = 0;
            while (tableIndex < rowsNumber && !isStopped()) {
                try {
                    if (tableIndex < startIndex || tableIndex >= endIndex) {
                        sourceRow = new ArrayList<>();
                        for (int c = 0; c < colsNumber; c++) {
                            sourceRow.add(values.getEntry(tableIndex, c) + "");
                        }
                        ++sourceIndex;
                        handleRow();

                    } else if (tableIndex == startIndex) {
                        scanPage();
                    }

                    tableIndex++;
                } catch (Exception e) {  // skip  bad lines
//                    showError(e.toString());
//                    setFailed();
                }
            }
        } catch (Exception e) {
            showError(e.toString());
            setFailed();
        }
    }

}
