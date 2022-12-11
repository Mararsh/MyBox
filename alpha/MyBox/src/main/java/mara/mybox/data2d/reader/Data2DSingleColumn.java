package mara.mybox.data2d.reader;

import java.sql.Connection;
import mara.mybox.data2d.Data2D_Attributes.InvalidAs;
import mara.mybox.data2d.Data2D_Edit;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.Database;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DRow;
import mara.mybox.db.table.TableData2D;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public class Data2DSingleColumn extends Data2DOperator {

    protected Connection conn;
    protected DataTable writerTable;
    protected TableData2D writerTableData2D;
    protected long count;

    public static Data2DSingleColumn create(Data2D_Edit data) {
        Data2DSingleColumn op = new Data2DSingleColumn();
        return op.setData(data) ? op : null;
    }

    @Override
    public boolean checkParameters() {
        if (cols == null || cols.isEmpty() || conn == null || writerTable == null) {
            return false;
        }
        writerTableData2D = writerTable.getTableData2D();
        count = 0;
        return true;
    }

    @Override
    public void handleRow() {
        try {
            int len = sourceRow.size();
            for (int col : cols) {
                if (col >= 0 && col < len) {
                    Data2DRow data2DRow = writerTableData2D.newRow();
                    Data2DColumn targetColumn = writerTable.columnByName("data");
                    String value = sourceRow.get(col);
                    if (targetColumn != null) {
                        data2DRow.setColumnValue("data", targetColumn.fromString(value, InvalidAs.Blank));
                        writerTableData2D.insertData(conn, data2DRow);
                        if (++count % Database.BatchSize == 0) {
                            conn.commit();
                        }
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        set
     */
    public Data2DSingleColumn setConn(Connection conn) {
        this.conn = conn;
        return this;
    }

    public Data2DSingleColumn setWriterTable(DataTable writerTable) {
        this.writerTable = writerTable;
        return this;
    }

}
