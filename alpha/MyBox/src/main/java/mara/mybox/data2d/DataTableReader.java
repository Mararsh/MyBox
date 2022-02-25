package mara.mybox.data2d;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DRow;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-1-29
 * @License Apache License Version 2.0
 */
public class DataTableReader extends DataFileReader {

    protected DataTable readerTable;

    public DataTableReader(DataTable data) {
        init(data);
        this.readerTable = data;
        tableData2D = readerTable.getTableData2D();
        tableData2D.setTableName(readerTable.getSheet());
    }

    @Override
    public void scanData() {
        try {
            conn = DerbyBase.getConnection();
            if (conn != null) {
                handleData();
                conn.close();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (readerTask != null) {
                readerTask.setError(e.toString());
            }
        }
    }

    @Override
    public void readColumnNames() {
        try {
            names = readerTable.columnNames();
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (readerTask != null) {
                readerTask.setError(e.toString());
            }
        }
    }

    @Override
    public void readTotal() {
        try {
            rowIndex = tableData2D.size(conn);
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (readerTask != null) {
                readerTask.setError(e.toString());
            }
            failed = true;
        }
    }

    @Override
    public void readPage() {
        rowIndex = 0;
        String sql = "SELECT * FROM " + readerTable.getSheet()
                + " OFFSET " + rowsStart + " ROWS FETCH NEXT " + (rowsEnd - rowsStart) + " ROWS ONLY";
        try ( PreparedStatement statement = conn.prepareStatement(sql);
                 ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                makeRecord(tableData2D.readData(results));
                handlePageRow();
                rowIndex++;
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (readerTask != null) {
                readerTask.setError(e.toString());
            }
            failed = true;
        }
    }

    @Override
    public void readRecords() {
        rowIndex = 0;
        String sql = "SELECT * FROM " + readerTable.getSheet();
        try ( PreparedStatement statement = conn.prepareStatement(sql);
                 ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                makeRecord(tableData2D.readData(results));
                handleRecord();
                rowIndex++;
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (readerTask != null) {
                readerTask.setError(e.toString());
            }
            failed = true;
        }
    }

    public void makeRecord(Data2DRow row) {
        try {
            record = new ArrayList<>();
            for (int i = 0; i < columnsNumber; ++i) {
                Data2DColumn column = readerTable.getColumns().get(i);
                Object value = row.getValue(column.getColumnName());
                record.add(column.toString(value));
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (readerTask != null) {
                readerTask.setError(e.toString());
            }
            failed = true;
        }
    }

}
