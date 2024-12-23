package mara.mybox.data2d.operate;

import java.sql.Connection;
import mara.mybox.data2d.Data2D_Edit;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.Database;
import mara.mybox.db.data.ColumnDefinition.InvalidAs;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DRow;
import mara.mybox.db.table.TableData2D;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public class Data2DSingleColumn extends Data2DOperate {

    protected DataTable writerTable;
    protected TableData2D writerTableData2D;
    protected long count;

    public static Data2DSingleColumn create(Data2D_Edit data) {
        Data2DSingleColumn op = new Data2DSingleColumn();
        return op.setSourceData(data) ? op : null;
    }

    @Override
    public boolean checkParameters() {
        if (!super.checkParameters()
                || cols == null || cols.isEmpty()
                || conn == null || writerTable == null) {
            return false;
        }
        writerTableData2D = writerTable.getTableData2D();
        count = 0;
        return true;
    }

    @Override
    public boolean handleRow() {
        try {
            if (sourceRow == null) {
                return false;
            }
            int len = sourceRow.size();
            for (int col : cols) {
                if (col >= 0 && col < len) {
                    Data2DRow data2DRow = writerTableData2D.newRow();
                    Data2DColumn targetColumn = writerTable.columnByName("data");
                    String value = sourceRow.get(col);
                    if (targetColumn != null) {
                        data2DRow.setValue("data", targetColumn.fromString(value, InvalidAs.Empty));
                        writerTableData2D.insertData(conn, data2DRow);
                        if (++count % Database.BatchSize == 0) {
                            conn.commit();
                        }
                    }
                }
            }
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return false;
        }
    }

    /*
        set
     */
    public Data2DSingleColumn setConn(Connection conn) {
        this.conn = conn;
        closeConn = false;
        return this;
    }

    public Data2DSingleColumn setWriterTable(DataTable writerTable) {
        this.writerTable = writerTable;
        return this;
    }

}
