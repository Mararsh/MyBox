package mara.mybox.data2d.reader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DRow;
import mara.mybox.db.table.TableData2D;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-1-29
 * @License Apache License Version 2.0
 */
public class DataTableReader extends Data2DReader {

    protected DataTable readerTable;
    protected TableData2D readerTableData2D;

    public DataTableReader(DataTable data) {
        init(data);
        readerTable = data;
        readerTableData2D = readerTable.getTableData2D();
        readerTableData2D.setTableName(readerTable.getSheet());
    }

    @Override
    public void scanData() {
        if (conn == null) {
            try ( Connection dconn = DerbyBase.getConnection()) {
                conn = dconn;
                operator.handleData();
                conn.close();
            } catch (Exception e) {
                MyBoxLog.error(e);
                if (task != null) {
                    task.setError(e.toString());
                }
            }
        } else {
            operator.handleData();
        }
    }

    @Override
    public void readColumnNames() {
        try {
            names = readerTable.columnNames();
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
        }
    }

    @Override
    public void readTotal() {
        try {
            rowIndex = readerTableData2D.size(conn);
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
            failed = true;
        }
    }

    @Override
    public void readPage() {
        rowIndex = data2D.startRowOfCurrentPage;
        try ( PreparedStatement statement = conn.prepareStatement(readerTable.pageQuery());
                 ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                makeRecord(readerTableData2D.readData(results));
                rowIndex++;
                handlePageRow();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
            failed = true;
        }
    }

    @Override
    public void readRows() {
        rowIndex = 0;
        String sql = "SELECT * FROM " + readerTable.getSheet();
        try ( PreparedStatement statement = conn.prepareStatement(sql);
                 ResultSet results = statement.executeQuery()) {
            while (results.next() && !readerStopped()) {
                makeRecord(readerTableData2D.readData(results));
                rowIndex++;
                handleRow();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
            failed = true;
        }
    }

    public void makeRecord(Data2DRow row) {
        try {
            sourceRow = new ArrayList<>();
            for (int i = 0; i < columnsNumber; ++i) {
                Data2DColumn column = readerTable.getColumns().get(i);
                Object value = row.getColumnValue(column.getColumnName());
                sourceRow.add(column.toString(value));
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
            failed = true;
        }
    }

}
