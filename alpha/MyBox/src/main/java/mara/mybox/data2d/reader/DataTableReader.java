package mara.mybox.data2d.reader;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import mara.mybox.data2d.DataTable;
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
    public void scanFile() {
        operate.handleData();
    }

    @Override
    public void readColumnNames() {
        try {
            names = readerTable.columnNames();
        } catch (Exception e) {
            showError(e.toString());
        }
    }

    @Override
    public void readTotal() {
        try {
            sourceIndex = readerTableData2D.size(conn());
        } catch (Exception e) {
            showError(e.toString());
            setFailed();
        }
    }

    @Override
    public void readPage() {
        sourceIndex = sourceData.startRowOfCurrentPage;
        String sql = readerTable.pageQuery();
        showInfo(sql);
        try (PreparedStatement statement = conn().prepareStatement(sql);
                ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                makeRecord(readerTableData2D.readData(results));
                sourceIndex++;
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
        long tableIndex = 0;
        long startIndex = sourceData.startRowOfCurrentPage;
        long endIndex = sourceData.endRowOfCurrentPage;
        String sql = "SELECT * FROM " + readerTable.getSheet();
        showInfo(sql);
        try (PreparedStatement statement = conn().prepareStatement(sql);
                ResultSet results = statement.executeQuery()) {
            while (results.next() && !isStopped()) {
                try {
                    if (tableIndex < startIndex || tableIndex >= endIndex) {
                        makeRecord(readerTableData2D.readData(results));
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

    public void makeRecord(Data2DRow row) {
        try {
            sourceRow = row.toStrings(readerTable.getColumns());
        } catch (Exception e) {
            showError(e.toString());
            setFailed();
        }
    }

}
