package mara.mybox.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
import static mara.mybox.value.Languages.message;

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

    public void cloneAll(DataTable d) {
        try {
            if (d == null) {
                return;
            }
            super.cloneAll(d);
            tableData2D = d.tableData2D;
            if (tableData2D == null) {
                tableData2D = new TableData2D();
            }
            tableData2D.setTableName(sheet);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void resetData() {
        super.resetData();
        tableData2D.reset();
    }

    public boolean readDefinitionFromDB(Connection conn, String tableName) {
        try {
            if (conn == null || tableName == null) {
                return false;
            }
            resetData();
            tableData2D.setTableName(tableName);
            tableData2D.readDefinitionFromDB(conn, tableName);
            List<ColumnDefinition> dbColumns = tableData2D.getColumns();
            List<Data2DColumn> dataColumns = new ArrayList<>();
            if (dbColumns != null) {
                for (ColumnDefinition dbColumn : dbColumns) {
                    Data2DColumn dataColumn = new Data2DColumn();
                    dataColumn.cloneFrom(dbColumn);
                    dataColumns.add(dataColumn);
                }
            }
            return recordTable(conn, tableName, dataColumns);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean recordTable(Connection conn, String tableName, List<Data2DColumn> dataColumns) {
        try {
            sheet = tableName.toLowerCase();
            dataName = tableName;
            colsNumber = dataColumns.size();
            tableData2DDefinition.insertData(conn, this);
            conn.commit();

            for (Data2DColumn column : dataColumns) {
                column.setD2id(d2did);
            }
            columns = dataColumns;
            tableData2DColumn.save(conn, d2did, columns);
            conn.commit();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean createTable(String name) {
        try ( Connection conn = DerbyBase.getConnection()) {
            String tableName = DerbyBase.fixedIdentifier(name);
            if (tableData2D.exist(conn, tableName)) {
                loadController.popError(message("AlreadyExisted"));
                return false;
            }
            tableData2D.reset();
            tableData2D.setTableName(tableName);
            List<Data2DColumn> savingColumns = new ArrayList<>();
            savingColumns.addAll(columns);
            for (Data2DColumn column : savingColumns) {
                column.setColumnName(DerbyBase.fixedIdentifier(column.getColumnName()));
                ColumnDefinition c = new ColumnDefinition();
                c.cloneFrom(column);
                tableData2D.addColumn(column);
            }
            if (conn.createStatement().executeUpdate(tableData2D.createTableStatement()) < 0) {
                loadController.popError(message("Failed"));
                return false;
            }
            conn.commit();
            return recordTable(conn, tableName, savingColumns);
        } catch (Exception e) {
            loadController.popError(e.toString());
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public boolean checkForLoad() {
        if (dataName == null) {
            dataName = sheet;
        }
        if (tableData2D == null) {
            tableData2D = new TableData2D();
        }
        tableData2D.setTableName(sheet);
        return super.checkForLoad();
    }

    @Override
    public Data2DDefinition queryDefinition(Connection conn) {
        return tableData2DDefinition.queryTable(conn, sheet, type);
    }

    @Override
    public void applyOptions() {
    }

    @Override
    public boolean readColumns(Connection conn) {
        try {
            columns = null;
            if (d2did < 0 || sheet == null) {
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
                if (column.isAuto()) {
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
        if (targetData == null || !(targetData instanceof DataTable)) {
            return false;
        }
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
    public boolean writeTable(Connection conn, TableData2D targetTable, List<Integer> cols) {
        try {
            if (conn == null || tableData2D == null || sheet == null
                    || targetTable == null || cols == null || cols.isEmpty()) {
                return false;
            }
            String sql = "SELECT ";
            for (int i = 0; i < cols.size(); i++) {
                int col = cols.get(i);
                if (i > 0) {
                    sql += ", ";
                }
                sql += columns.get(col).getColumnName();
            }
            sql += " FROM " + sheet;
            try ( PreparedStatement statement = conn.prepareStatement(sql);
                     ResultSet results = statement.executeQuery()) {
                conn.setAutoCommit(false);
                while (results.next()) {
                    Data2DRow row = tableData2D.readData(results);
                    targetTable.writeData(conn, row);
                }
                conn.commit();
            } catch (Exception e) {
                MyBoxLog.error(e, sql);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
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

    /*
        get/set
     */
    public TableData2D getTableData2D() {
        return tableData2D;
    }

    public void setTableData2D(TableData2D tableData2D) {
        this.tableData2D = tableData2D;
    }

}
