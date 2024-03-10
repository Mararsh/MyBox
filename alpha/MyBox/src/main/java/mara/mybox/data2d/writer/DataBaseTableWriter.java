package mara.mybox.data2d.writer;

import java.sql.PreparedStatement;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.Database;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DRow;
import mara.mybox.db.table.TableData2D;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-1-29
 * @License Apache License Version 2.0
 */
public class DataBaseTableWriter extends Data2DWriter {

    protected DataTable table;
    protected long dwCount;
    protected TableData2D tableData2D;
    protected PreparedStatement insert;

    public DataBaseTableWriter() {
        fileSuffix = "pdf";
    }

    @Override
    public boolean openWriter() {
        try {
            table = new DataTable();
            table.setTask(task()).setDataName(dataName);
            if (!Data2D.saveAttributes(conn, table, columns)) {
                return false;
            }
            tableData2D = table.getTableData2D();
            if (conn == null) {
                conn = DerbyBase.getConnection();
            }
            conn.setAutoCommit(false);
            dwCount = 0;
            String sql = tableData2D.insertStatement();
            showInfo(sql);
            insert = conn.prepareStatement(sql);
            targetData = table;
            return true;
        } catch (Exception e) {
            handleError(e.toString());
            return false;
        }
    }

    @Override
    public void printTargetRow() {
        try {
            if (targetRow == null || conn == null || table == null) {
                return;
            }
            Data2DRow data2DRow = tableData2D.newRow();
            int offset = writeRowNumber ? 2 : 1;
            for (int i = 0; i < targetRow.size(); i++) {
                Data2DColumn targetColumn = table.column(i + offset);
                data2DRow.setColumnValue(targetColumn.getColumnName(),
                        targetColumn.fromString(targetRow.get(i), invalidAs()));
            }
            if (data2DRow.isEmpty()) {
                return;
            }
            if (writeRowNumber) {
                data2DRow.setColumnValue(table.column(1).getColumnName(), sourceRowIndex());
            }
            if (tableData2D.setInsertStatement(conn, insert, data2DRow)) {
                insert.addBatch();
                if (++dwCount % Database.BatchSize == 0) {
                    insert.executeBatch();
                    conn.commit();
                    showInfo(message("Inserted") + ": " + dwCount);
                }
            }
        } catch (Exception e) {
            handleError(e.toString());
        }
    }

    @Override
    public void closeWriter() {
        try {
            created = false;
            if (conn == null || table == null) {
                return;
            }
            insert.executeBatch();
            conn.commit();
            insert.close();
            table.setRowsNumber(fileRowIndex);
            Data2D.saveAttributes(conn, table, columns);
            created = true;
        } catch (Exception e) {
            handleError(e.toString());
        }
    }

}
