package mara.mybox.data2d.operate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import mara.mybox.data2d.Data2D_Edit;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.Database;
import mara.mybox.db.data.ColumnDefinition.InvalidAs;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DRow;
import mara.mybox.db.table.TableData2D;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public class Data2DWriteTable extends Data2DOperate {

    protected Connection conn;
    protected DataTable writerTable;
    protected TableData2D writerTableData2D;
    protected PreparedStatement insert;

    public static Data2DWriteTable create(Data2D_Edit data) {
        Data2DWriteTable op = new Data2DWriteTable();
        return op.setSourceData(data) ? op : null;
    }

    @Override
    public boolean checkParameters() {
        try {
            if (!super.checkParameters()
                    || cols == null || cols.isEmpty() || conn == null
                    || writerTable == null || invalidAs == null) {
                return false;
            }
            if (invalidAs == InvalidAs.Skip) {
                invalidAs = InvalidAs.Blank;
            }
            writerTableData2D = writerTable.getTableData2D();
            handledCount = 0;
            String sql = writerTableData2D.insertStatement();
            if (task != null) {
                task.setInfo(sql);
            }
            insert = conn.prepareStatement(sql);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public void handleData() {
        try {
            reader.readRows();
            insert.executeBatch();
            conn.commit();
            insert.close();
            if (task != null) {
                task.setInfo(message("Inserted") + ": " + handledCount);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean handleRow() {
        try {
            Data2DRow data2DRow = writerTableData2D.newRow();
            makeTableRow(data2DRow);
            if (data2DRow.isEmpty()) {
                return true;
            }
            if (includeRowNumber) {
                data2DRow.setColumnValue(writerTable.column(1).getColumnName(), sourceRowIndex);
            }
            if (writerTableData2D.setInsertStatement(conn, insert, data2DRow)) {
                insert.addBatch();
                if (++handledCount % Database.BatchSize == 0) {
                    insert.executeBatch();
                    conn.commit();
                    if (task != null) {
                        task.setInfo(message("Inserted") + ": " + handledCount);
                    }
                }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public void makeTableRow(Data2DRow data2DRow) {
        try {
            int len = sourceRow.size();
            int offset = includeRowNumber ? 2 : 1;
            for (int i = 0; i < cols.size(); i++) {
                int col = cols.get(i);
                if (col < 0 || col >= len) {
                    continue;
                }
                Data2DColumn targetColumn = writerTable.column(i + offset);
                data2DRow.setColumnValue(targetColumn.getColumnName(),
                        targetColumn.fromString(sourceRow.get(col), invalidAs));
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        set
     */
    public Data2DWriteTable setConn(Connection conn) {
        this.conn = conn;
        return this;
    }

    public Data2DWriteTable setWriterTable(DataTable writerTable) {
        this.writerTable = writerTable;
        return this;
    }

    /*
        get
     */
    public long getHandledCount() {
        return handledCount;
    }

}
