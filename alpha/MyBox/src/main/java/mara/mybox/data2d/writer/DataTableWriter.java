package mara.mybox.data2d.writer;

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
public class DataTableWriter extends Data2DWriter {

    protected DataTable sourceTable;
    protected TableData2D tableData2D;
    protected int writeCount;
    protected Data2DRow sourceTableRow;
    protected Connection conn;

    public DataTableWriter(DataTable data) {
        init(data);
        sourceTable = data;
        tableData2D = sourceTable.getTableData2D();
        tableData2D.setTableName(sourceTable.getSheet());
    }

    @Override
    public Data2DWriter start(Operation operation) {
        this.operation = operation;
        if (isClearData()) {
            count = tableData2D.clearData();
            return this;
        }
        return super.start(operation);
    }

    @Override
    public void scanData() {
        rowIndex = 0;
        count = 0;
        writeCount = 0;
        String sql = "SELECT * FROM " + sourceTable.getSheet();
        try ( Connection dconn = DerbyBase.getConnection();
                 PreparedStatement statement = dconn.prepareStatement(sql);
                 ResultSet results = statement.executeQuery()) {
            conn = dconn;
            conn.setAutoCommit(false);
            while (results.next() && !writerStopped() && !data2D.filterReachMaxPassed()) {
                sourceTableRow = tableData2D.readData(results);
                makeRecord();
                if (sourceRow == null || sourceRow.isEmpty()) {
                    continue;
                }
                rowIndex++;
                handleRow();
            }
            conn.commit();
            conn.close();
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
            failed = true;
        }
        if (failed) {
            writerStopped = true;
        }
    }

    public void makeRecord() {
        try {
            sourceRow = null;
            if (sourceTableRow == null) {
                return;
            }
            sourceRow = new ArrayList<>();
            for (int i = 0; i < columnsNumber; ++i) {
                Data2DColumn column = sourceTable.getColumns().get(i);
                Object value = sourceTableRow.getColumnValue(column.getColumnName());
                sourceRow.add(column.toString(value));
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
        }
    }

    @Override
    public void writeRow() {
        try {
            if (writerStopped() || targetRow == null || targetRow.isEmpty()) {
                return;
            }
            for (int i = 0; i < columnsNumber; ++i) {
                Data2DColumn column = sourceTable.getColumns().get(i);
                String name = column.getColumnName();
                sourceTableRow.setColumnValue(name, column.fromString(targetRow.get(i)));
            }
            tableData2D.updateData(conn, sourceTableRow);
            if (writeCount++ % DerbyBase.BatchSize == 0) {
                conn.commit();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
        }
    }

    @Override
    public void deleteRow(boolean needDelete) {
        try {
            if (needDelete) {
                tableData2D.deleteData(conn, sourceTableRow);
                if (++writeCount % DerbyBase.BatchSize == 0) {
                    conn.commit();
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
        }
    }

}
