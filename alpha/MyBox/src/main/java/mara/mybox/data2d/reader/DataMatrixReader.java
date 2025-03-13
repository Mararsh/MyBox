package mara.mybox.data2d.reader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import mara.mybox.data2d.DataMatrix;
import static mara.mybox.data2d.DataMatrix.toDouble;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2DCell;

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
        String sql = "SELECT * FROM Data2D_Cell WHERE dcdid=" + sourceMatrix.dataID
                + " AND row=?";
        showInfo(sql);
        try (Connection rconn = DerbyBase.getConnection();
                PreparedStatement statement = rconn.prepareStatement(sql)) {
            rconn.setAutoCommit(false);
            long cellCol;
            long colsNum = sourceMatrix.colsNumber;
            long startIndex = sourceMatrix.pagination.startRowOfCurrentPage;
            long endIndex = startIndex + sourceMatrix.pagination.pageSize;
            if (endIndex > sourceMatrix.pagination.rowsNumber) {
                endIndex = sourceMatrix.pagination.rowsNumber;
            }
            for (sourceIndex = startIndex; sourceIndex < endIndex; sourceIndex++) {
                if (isStopped()) {
                    return;
                }
                sourceRow = new ArrayList<>();
                for (long c = 0; c < colsNum; c++) {
                    sourceRow.add("0");
                }
                statement.setLong(1, sourceIndex);
                try (ResultSet results = statement.executeQuery()) {
                    while (results.next()) {
                        if (isStopped()) {
                            return;
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
                makePageRow();
            }
        } catch (Exception e) {
            showError(e.toString());
            setFailed();
        }
    }

    @Override
    public void readRows() {
        sourceIndex = 0;
        long startIndex = sourceMatrix.pagination.startRowOfCurrentPage;
        long endIndex = sourceMatrix.pagination.endRowOfCurrentPage;
        long rowsNum = sourceMatrix.pagination.rowsNumber;
        long colsNum = sourceMatrix.colsNumber;
        String sql = "SELECT * FROM Data2D_Cell WHERE dcdid=" + sourceMatrix.dataID
                + " AND row=?";
        showInfo(sql);
        try (Connection rconn = DerbyBase.getConnection();
                PreparedStatement statement = rconn.prepareStatement(sql)) {
            rconn.setAutoCommit(false);
            long cellCol;
            for (long tableIndex = 0; tableIndex < rowsNum; tableIndex++) {
                if (isStopped()) {
                    return;
                }

                if (tableIndex < startIndex || tableIndex >= endIndex) {
                    sourceRow = new ArrayList<>();
                    for (long c = 0; c < colsNum; c++) {
                        sourceRow.add("0");
                    }
                    statement.setLong(1, tableIndex);
                    try (ResultSet results = statement.executeQuery()) {
                        while (results.next()) {
                            if (isStopped()) {
                                return;
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
                    ++sourceIndex;
                    handleRow();

                } else if (tableIndex == startIndex) {
                    scanPage();
                }

            }
        } catch (Exception e) {
            showError(e.toString());
            setFailed();
        }
    }

}
