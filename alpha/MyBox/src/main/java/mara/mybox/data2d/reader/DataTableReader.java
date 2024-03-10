package mara.mybox.data2d.reader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2DRow;
import mara.mybox.db.table.TableData2D;

/**
 * @Author Mara
 * @CreateDate 2022-1-29
 * @License Apache License Version 2.0
 */
public class DataTableReader extends Data2DReader {

    protected DataTable readerTable;
    protected TableData2D readerTableData2D;

    public DataTableReader(DataTable data) {
        readerTable = data;
        readerTableData2D = readerTable.getTableData2D();
        readerTableData2D.setTableName(readerTable.getSheet());
        sourceData = data;
    }

    @Override
    public void scanData() {
        if (conn == null) {
            try (Connection dconn = DerbyBase.getConnection()) {
                conn = dconn;
                operate.handleData();
                conn.commit();
                conn.close();
                conn = null;
            } catch (Exception e) {
                handleError(e.toString());
            }
        } else {
            operate.handleData();
        }
    }

    @Override
    public void readColumnNames() {
        try {
            names = readerTable.columnNames();
        } catch (Exception e) {
            handleError(e.toString());
        }
    }

    @Override
    public void readTotal() {
        try {
            rowIndex = readerTableData2D.size(conn);
        } catch (Exception e) {
            handleError(e.toString());
            setFailed();
        }
    }

    @Override
    public void readPage() {
        rowIndex = sourceData.startRowOfCurrentPage;
        String sql = readerTable.pageQuery();
        showInfo(sql);
        try (PreparedStatement statement = conn.prepareStatement(sql);
                ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                makeRecord(readerTableData2D.readData(results));
                rowIndex++;
                handlePageRow();
            }
        } catch (Exception e) {
            handleError(e.toString());
            setFailed();
        }
    }

    @Override
    public void readRows() {
        rowIndex = 0;
        String sql = "SELECT * FROM " + readerTable.getSheet();
        showInfo(sql);
        try (PreparedStatement statement = conn.prepareStatement(sql);
                ResultSet results = statement.executeQuery()) {
            while (results.next() && !isStopped()) {
                makeRecord(readerTableData2D.readData(results));
                rowIndex++;
                handleRow();
            }
        } catch (Exception e) {
            handleError(e.toString());
            setFailed();
        }
    }

    public void makeRecord(Data2DRow row) {
        try {
            sourceRow = row.toStrings(readerTable.getColumns());
        } catch (Exception e) {
            handleError(e.toString());
            setFailed();
        }
    }

}
