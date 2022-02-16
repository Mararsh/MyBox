package mara.mybox.data;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import mara.mybox.controller.ControlDataConvert;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.Data2DRow;
import mara.mybox.db.table.TableData2D;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public class DataTable extends Data2D {

    protected TableData2D tableData2D;

    public DataTable() {
        type = Type.DatabaseTable;
        tableData2D = new TableData2D();
    }

    public int type() {
        return type(Type.DatabaseTable);
    }

    public void recordTable(Connection conn, String tableName) {
        try {
            resetData();
            sheet = tableName != null ? tableName.toLowerCase() : null;
            readDataDefinition(conn);
            readColumns(conn);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean checkForLoad() {
        if (dataName == null) {
            dataName = sheet;
        }
        return super.checkForLoad();
    }

    @Override
    public Data2DDefinition queryDefinition(Connection conn) {
        return tableData2DDefinition.queryInternalTable(conn, sheet);
    }

    @Override
    public void applyOptions() {
    }

    @Override
    public boolean readColumns(Connection conn) {
        try {
            columns = null;
            if (d2did < 0) {
                return false;
            }
            tableData2D.readDefinitionFromDB(conn, sheet);
            List<ColumnDefinition> dbColumns = tableData2D.getColumns();
            if (dbColumns == null) {
                return false;
            }
            columns = new ArrayList<>();
            Random random = new Random();
            for (int i = 0; i < dbColumns.size(); i++) {
                ColumnDefinition dbColumn = dbColumns.get(i);
                Data2DColumn column = new Data2DColumn();
                column.cloneFrom(dbColumn);
                if (savedColumns != null && i < savedColumns.size()) {
                    Data2DColumn scolumn = savedColumns.get(i);
                    column.setColor(scolumn.getColor());
                    column.setWidth(scolumn.getWidth());
                }
                column.setD2id(d2did);
                column.setIndex(i);
                if (column.getColor() == null) {
                    column.setColor(FxColorTools.randomColor(random));
                }
                if (column.isIsID()) {
                    column.setEditable(false);
                }
                columns.add(column);
            }
            colsNumber = columns.size();
            tableData2DColumn.save(conn, d2did, columns);
            tableData2DDefinition.updateData(conn, this);
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public long readTotal() {
        dataSize = 0;
        if (tableData2D != null) {
            dataSize = tableData2D.size();
        }
        rowsNumber = dataSize;
        tableData2DDefinition.updateData(this);
        return dataSize;
    }

    @Override
    public List<List<String>> readPageData() {
        if (d2did < 0 || sheet == null || !isColumnsValid()) {
            startRowOfCurrentPage = endRowOfCurrentPage = 0;
            return null;
        }
        if (startRowOfCurrentPage < 0) {
            startRowOfCurrentPage = 0;
        }
        endRowOfCurrentPage = startRowOfCurrentPage;
        List<List<String>> rows = new ArrayList<>();
        List<Data2DRow> trows = tableData2D.query(startRowOfCurrentPage, pageSize);
        if (trows != null) {
            long rowIndex = startRowOfCurrentPage;
            for (Data2DRow trow : trows) {
                List<String> row = new ArrayList<>();
                for (int i = 0; i < columns.size(); ++i) {
                    ColumnDefinition column = columns.get(i);
                    Object value = trow.getValue(column.getColumnName());
                    row.add(column.toString(value));
                }
                row.add(0, ++rowIndex + "");
                rows.add(row);
            }
        }
        endRowOfCurrentPage = startRowOfCurrentPage + rows.size();
        return rows;
    }

    public List<List<String>> pageRows() {
        return loadController == null ? null : loadController.getTableData();
    }

    @Override
    public boolean savePageData(Data2D targetData) {
        try ( Connection conn = DerbyBase.getConnection()) {
            List<Data2DRow> dbRows = tableData2D.queryConditions(conn, null, null, startRowOfCurrentPage, pageSize);
            List<Data2DRow> pageRows = new ArrayList<>();
            List<List<String>> pageData = tableData();
            conn.setAutoCommit(false);
            if (pageData != null) {
                for (int i = 0; i < pageData.size(); i++) {
                    Data2DRow row = tableData2D.from(pageData.get(i));
                    if (row != null) {
                        pageRows.add(row);
                        tableData2D.writeData(conn, row);
                    }
                }
            }
            if (dbRows != null) {
                for (Data2DRow drow : dbRows) {
                    boolean exist = false;
                    for (Data2DRow prow : pageRows) {
                        if (tableData2D.sameRow(drow, prow)) {
                            exist = true;
                            break;
                        }
                    }
                    if (!exist) {
                        tableData2D.deleteData(conn, drow);
                    }
                }
            }
            conn.commit();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return false;
    }

    @Override
    public boolean export(ControlDataConvert convertController, List<Integer> colIndices) {
        return false;
    }

    @Override
    public long clearData() {
        return tableData2D.clearData();
    }

    /*
        static
     */
    public static List<String> userTables() {
        List<String> userTables = new ArrayList<>();
        try ( Connection conn = DerbyBase.getConnection()) {
            List<String> allTables = DerbyBase.allTables(conn);
            for (String name : allTables) {
                if (!DataInternalTable.InternalTables.contains(name)) {
                    userTables.add(name);
                }
            }
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
        return userTables;
    }

    public static String tableDefinition(String tableName) {
        try {
            TableData2D table = new TableData2D();
            table.readDefinitionFromDB(tableName);
            return table.html();
        } catch (Exception e) {
            MyBoxLog.console(e);
            return null;
        }
    }

}
