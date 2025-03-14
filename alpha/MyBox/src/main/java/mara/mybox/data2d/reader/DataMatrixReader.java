package mara.mybox.data2d.reader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import mara.mybox.data2d.DataMatrix;
import static mara.mybox.data2d.DataMatrix.toDouble;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2DCell;
import mara.mybox.db.table.TableData2DCell;

/**
 * @Author Mara
 * @CreateDate 2022-1-29
 * @License Apache License Version 2.0
 */
public class DataMatrixReader extends Data2DReader {

    protected DataMatrix sourceMatrix;
    protected TableData2DCell tableData2DCell;
    protected long dataID, rowsNumber, colsNumber;

    public DataMatrixReader(DataMatrix data) {
        sourceMatrix = data;
        sourceData = data;
        tableData2DCell = sourceMatrix.tableData2DCell;
        dataID = sourceMatrix.dataID;
        rowsNumber = sourceMatrix.pagination.rowsNumber;
        colsNumber = sourceMatrix.colsNumber;
    }

    @Override
    public void readColumnNames() {
        names = sourceMatrix.columnNames();
    }

    @Override
    public void readTotal() {
        sourceIndex = rowsNumber;
    }

    @Override
    public void readPage() {
        try (Connection rconn = DerbyBase.getConnection();
                PreparedStatement query = rconn.prepareStatement(TableData2DCell.QueryRow)) {
            rconn.setAutoCommit(false);
            long cellCol;
            long startIndex = sourceMatrix.pagination.startRowOfCurrentPage;
            long endIndex = startIndex + sourceMatrix.pagination.pageSize;
            if (endIndex > rowsNumber) {
                endIndex = rowsNumber;
            }
            for (sourceIndex = startIndex; sourceIndex < endIndex; sourceIndex++) {
                if (isStopped()) {
                    return;
                }
                sourceRow = new ArrayList<>();
                for (long c = 0; c < colsNumber; c++) {
                    sourceRow.add("0");
                }
                showInfo(TableData2DCell.QueryRow + "\ndata ID:" + dataID
                        + "\nrow:" + sourceIndex);
                query.setLong(1, dataID);
                query.setLong(2, sourceIndex);
                try (ResultSet results = query.executeQuery()) {
                    while (results.next()) {
                        if (isStopped()) {
                            return;
                        }
                        Data2DCell cell = tableData2DCell.readData(results);
                        cellCol = cell.getColumnID();
                        if (cellCol > -1 && cellCol < colsNumber) {
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
        String sql = "SELECT * FROM Data2D_Cell WHERE dcdid=" + sourceMatrix.dataID
                + " AND row=?";
        showInfo(sql);
        try (Connection rconn = DerbyBase.getConnection();
                PreparedStatement query = rconn.prepareStatement(TableData2DCell.QueryRow)) {
            rconn.setAutoCommit(false);
            long cellCol;
            for (long rowIndex = 0; rowIndex < rowsNumber; rowIndex++) {
                if (isStopped()) {
                    return;
                }

                if (rowIndex < startIndex || rowIndex >= endIndex) {
                    sourceRow = new ArrayList<>();
                    for (long c = 0; c < colsNumber; c++) {
                        sourceRow.add("0");
                    }
                    showInfo(TableData2DCell.QueryRow + "\ndata ID:" + dataID
                            + "\nrow:" + rowIndex);
                    query.setLong(1, dataID);
                    query.setLong(2, rowIndex);
                    try (ResultSet results = query.executeQuery()) {
                        while (results.next()) {
                            if (isStopped()) {
                                return;
                            }
                            Data2DCell cell = sourceMatrix.tableData2DCell.readData(results);
                            cellCol = cell.getColumnID();
                            if (cellCol > -1 && cellCol < colsNumber) {
                                sourceRow.set((int) cellCol, toDouble(cell.getValue()) + "");
                            }
                        }
                    } catch (Exception e) {
                        showError(e.toString());
                        setFailed();
                    }
                    ++sourceIndex;
                    handleRow();

                } else if (rowIndex == startIndex) {
                    scanPage();
                }

            }
        } catch (Exception e) {
            showError(e.toString());
            setFailed();
        }
    }

}
