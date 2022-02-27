package mara.mybox.data2d;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
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
                MyBoxLog.debug(column.getColumnName() + " " + column.isNotNull());
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

    public Data2DRow from(List<String> values) {
        try {
            if (columns == null || values == null || values.isEmpty()) {
                return null;
            }
            Data2DRow row = new Data2DRow();
            row.setIndex(Integer.valueOf(values.get(0)));
            for (int i = 0; i < Math.min(columns.size(), values.size() - 1); i++) {
                Data2DColumn column = columns.get(i);
                String name = column.getColumnName();
                String value = values.get(i + 1);
                if (value != null) {
                    row.setValue(name, column.fromString(value));
                    MyBoxLog.debug(name + " " + column.getType() + " " + value + " " + column.fromString(value));
                }
            }
            return row;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
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
                column.setD2id(d2did);
                column.setIndex(i);
                if (savedColumns != null) {
                    for (Data2DColumn scolumn : savedColumns) {
                        if (column.getColumnName().equalsIgnoreCase(scolumn.getColumnName())) {
                            column.setColor(scolumn.getColor());
                            column.setWidth(scolumn.getWidth());
                            column.setIndex(scolumn.getIndex());
                            break;
                        }
                    }
                }
                if (column.getColor() == null) {
                    column.setColor(FxColorTools.randomColor(random));
                }
                if (column.isAuto()) {
                    column.setEditable(false);
                }
                columns.add(column);
            }
            Collections.sort(columns, new Comparator<Data2DColumn>() {
                @Override
                public int compare(Data2DColumn v1, Data2DColumn v2) {
                    int diff = v1.getIndex() - v2.getIndex();
                    if (diff == 0) {
                        return 0;
                    } else if (diff > 0) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            });
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
    public List<String> readColumnNames() {
        return null;
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
                    Data2DRow row = from(pageData.get(i));
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
    public boolean saveDefinition(Connection conn) {
        try {
            if (!super.saveDefinition(conn)) {
                return false;
            }
            List<String> dbColumnNames = tableData2D.columnNames();
            MyBoxLog.debug(dbColumnNames);
            List<String> dataColumnNames = new ArrayList<>();
            for (Data2DColumn column : columns) {
                String name = DerbyBase.fixedIdentifier(column.getColumnName());
                dataColumnNames.add(name);
                if (!dbColumnNames.contains(name)) {
                    tableData2D.addColumn(conn, column);
                    conn.commit();
                }
            }
            MyBoxLog.debug(dbColumnNames);
            for (String name : dbColumnNames) {
                if (!dataColumnNames.contains(name)) {
                    tableData2D.dropColumn(conn, name);
                    conn.commit();
                }
            }
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
    public boolean setValue(List<Integer> cols, String value) {
        if (cols == null || cols.isEmpty()) {
            return false;
        }
        boolean isRandom = "MyBox##random".equals(value);
        boolean isRandomNn = "MyBox##randomNn".equals(value);
        if (!isRandom && !isRandomNn) {
            try ( Connection conn = DerbyBase.getConnection();
                     Statement update = conn.createStatement()) {
                String sql = null;
                for (int col : cols) {
                    if (sql == null) {
                        sql = "";
                    } else {
                        sql += ", ";
                    }
                    Data2DColumn column = columns.get(col);
                    String quote = column.valueQuoted() ? "'" : "";
                    sql += column.getColumnName() + "=" + quote + column.fromString(value) + quote;
                }
                sql = "UPDATE " + sheet + " SET " + sql;
                update.executeUpdate(sql);
                conn.commit();
            } catch (Exception e) {
                if (task != null) {
                    task.setError(e.toString());
                }
                MyBoxLog.error(e);
                return false;
            }
        } else {
            try ( Connection conn = DerbyBase.getConnection();
                     PreparedStatement query = conn.prepareStatement("SELECT * FROM " + sheet);
                     ResultSet results = query.executeQuery()) {
                Random random = new Random();
                conn.setAutoCommit(false);
                int count = 0;
                while (results.next()) {
                    Data2DRow row = tableData2D.readData(results);
                    for (int col : cols) {
                        Data2DColumn column = columns.get(col);
                        String name = column.getColumnName();
                        String v = value;
                        if (isRandom) {
                            v = random(random, col, false);
                        } else if (isRandomNn) {
                            v = random(random, col, true);
                        }
                        row.setValue(name, column.fromString(v));
                    }
                    tableData2D.updateData(conn, row);
                    if (++count % DerbyBase.BatchSize == 0) {
                        conn.commit();
                    }
                }
                conn.commit();
            } catch (Exception e) {
                if (task != null) {
                    task.setError(e.toString());
                }
                MyBoxLog.error(e);
                return false;
            }
        }
        return true;
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
