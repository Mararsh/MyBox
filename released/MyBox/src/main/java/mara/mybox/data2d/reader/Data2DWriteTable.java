package mara.mybox.data2d.reader;

import java.sql.Connection;
import mara.mybox.data2d.Data2D_Attributes.InvalidAs;
import mara.mybox.data2d.Data2D_Edit;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.DerbyBase;
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
public class Data2DWriteTable extends Data2DOperator {

    protected Connection conn;
    protected DataTable writerTable;
    protected TableData2D writerTableData2D;
    protected long count;

    public static Data2DWriteTable create(Data2D_Edit data) {
        Data2DWriteTable op = new Data2DWriteTable();
        return op.setData(data) ? op : null;
    }

    @Override
    public boolean checkParameters() {
        if (cols == null || cols.isEmpty() || conn == null || writerTable == null || invalidAs == null) {
            return false;
        }
        if (invalidAs == InvalidAs.Skip) {
            invalidAs = InvalidAs.Blank;
        }
        writerTableData2D = writerTable.getTableData2D();
        count = 0;
        return true;
    }

    @Override
    public void handleRow() {
        try {
            Data2DRow data2DRow = writerTableData2D.newRow();
            int len = sourceRow.size();
            for (int col : cols) {
                if (col >= 0 && col < len) {
                    Data2DColumn sourceColumn = data2D.getColumns().get(col);
                    String colName = writerTable.mappedColumnName(sourceColumn.getColumnName());
                    Data2DColumn targetColumn = writerTable.columnByName(colName);
                    data2DRow.setColumnValue(colName, targetColumn.fromString(sourceRow.get(col), invalidAs));
                }
            }
            if (data2DRow.isEmpty()) {
                return;
            }
            if (includeRowNumber) {
                data2DRow.setColumnValue(writerTable.mappedColumnName(message("SourceRowNumber")), rowIndex);
            }
            writerTableData2D.insertData(conn, data2DRow);
            if (++count % DerbyBase.BatchSize == 0) {
                conn.commit();
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

    public Data2DWriteTable setIncludeRowNumber(boolean includeRowNumber) {
        this.includeRowNumber = includeRowNumber;
        return this;
    }

    /*
        get
     */
    public long getCount() {
        return count;
    }

}
