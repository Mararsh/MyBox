package mara.mybox.data2d;

import java.io.File;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.Data2DRow;
import mara.mybox.db.table.BaseTable;
import mara.mybox.db.table.TableData2D;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.tools.CsvTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TmpFileTools;
import static mara.mybox.value.Languages.message;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.math3.stat.Frequency;

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

    public boolean readDefinitionFromDB(Connection conn, String referredName) {
        try {
            if (conn == null || referredName == null) {
                return false;
            }
            resetData();
            tableData2D.setTableName(referredName);
            tableData2D.readDefinitionFromDB(conn, referredName);
            List<ColumnDefinition> dbColumns = tableData2D.getColumns();
            List<Data2DColumn> dataColumns = new ArrayList<>();
            if (dbColumns != null) {
                for (ColumnDefinition dbColumn : dbColumns) {
                    Data2DColumn dataColumn = new Data2DColumn();
                    dataColumn.cloneFrom(dbColumn);
                    dataColumns.add(dataColumn);
                }
            }
            return recordTable(conn, referredName, dataColumns);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean recordTable(Connection conn, String tableName, List<Data2DColumn> dataColumns) {
        try {
            sheet = BaseTable.savedName(tableName);
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
                dbColumn.setIndex(i);
                if (savedColumns != null) {
                    for (Data2DColumn scolumn : savedColumns) {
                        if (dbColumn.getColumnName().equalsIgnoreCase(scolumn.getColumnName())) {
                            dbColumn.setIndex(scolumn.getIndex());
                            dbColumn.setColor(scolumn.getColor());
                            dbColumn.setWidth(scolumn.getWidth());
                            dbColumn.setEditable(scolumn.isEditable());
                            if (dbColumn.getDefaultValue() == null) {
                                dbColumn.setDefaultValue(scolumn.getDefaultValue());
                            }
                            break;
                        }
                    }
                }
                if (dbColumn.getColor() == null) {
                    dbColumn.setColor(FxColorTools.randomColor(random));
                }
                if (dbColumn.isAuto()) {
                    dbColumn.setEditable(false);
                }
            }
            Collections.sort(dbColumns, new Comparator<ColumnDefinition>() {
                @Override
                public int compare(ColumnDefinition v1, ColumnDefinition v2) {
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
            for (int i = 0; i < dbColumns.size(); i++) {
                ColumnDefinition column = dbColumns.get(i);
                column.setIndex(i);
            }
            for (ColumnDefinition dbColumn : dbColumns) {
                Data2DColumn column = new Data2DColumn();
                column.cloneFrom(dbColumn);
                column.setD2id(d2did);
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
    public List<String> readColumnNames() {
        return null;
    }

    public Data2DRow from(List<String> values) {
        try {
            if (columns == null || values == null || values.isEmpty()) {
                return null;
            }
            Data2DRow data2DRow = tableData2D.newRow();
            data2DRow.setRowIndex(Integer.valueOf(values.get(0)));
            for (int i = 0; i < Math.min(columns.size(), values.size() - 1); i++) {
                Data2DColumn column = columns.get(i);
                String name = column.getColumnName();
                Object value = column.fromString(values.get(i + 1));
                if (value != null) {
                    data2DRow.setColumnValue(name, value);
                }
            }
            return data2DRow;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public boolean updateTable(Connection conn) {
        try {
            List<String> dbColumnNames = tableData2D.columnNames();
            List<String> dataColumnNames = new ArrayList<>();
            for (Data2DColumn column : columns) {
                String name = DerbyBase.fixedIdentifier(column.getColumnName());
                dataColumnNames.add(name);
                if (dbColumnNames.contains(name) && column.getIndex() < 0) {
                    tableData2D.dropColumn(conn, name);
                    conn.commit();
                    dbColumnNames.remove(name);
                }
            }
            for (String name : dbColumnNames) {
                if (!dataColumnNames.contains(name)) {
                    tableData2D.dropColumn(conn, name);
                    conn.commit();
                }
            }
            for (Data2DColumn column : columns) {
                String name = DerbyBase.fixedIdentifier(column.getColumnName());
                if (!dbColumnNames.contains(name)) {
                    tableData2D.addColumn(conn, column);
                    conn.commit();
                }
            }
            List<ColumnDefinition> dbColumns = new ArrayList<>();
            for (Data2DColumn column : columns) {
                ColumnDefinition dbColumn = new ColumnDefinition();
                dbColumn.cloneFrom(column);
                dbColumns.add(dbColumn);
            }
            tableData2D.setColumns(dbColumns);
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return false;
        }
    }

    public String pageQuery() {
        String sql = "SELECT * FROM " + sheet;
        String orderby = null;
        for (ColumnDefinition column : tableData2D.getPrimaryColumns()) {
            if (orderby != null) {
                orderby += "," + column.getColumnName();
            } else {
                orderby = column.getColumnName();
            }
        }
        if (orderby != null && !orderby.isBlank()) {
            sql += " ORDER BY " + orderby;
        }
        sql += " OFFSET " + startRowOfCurrentPage + " ROWS FETCH NEXT " + pageSize + " ROWS ONLY";
        return sql;
    }

    @Override
    public boolean savePageData(Data2D targetData) {
        try ( Connection conn = DerbyBase.getConnection()) {
            updateTable(conn);
            List<Data2DRow> dbRows = tableData2D.query(conn, pageQuery());
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
    public boolean setValue(List<Integer> cols, String value, boolean errorContinue) {
        if (cols == null || cols.isEmpty()) {
            return false;
        }
        boolean isRandom = false, isRandomNn = false, isBlank = false;
        String script = null;
        if (value != null) {
            if ("MyBox##blank".equals(value)) {
                isBlank = true;
            } else if ("MyBox##random".equals(value)) {
                isRandom = true;
            } else if ("MyBox##randomNn".equals(value)) {
                isRandomNn = true;
            } else if (value.startsWith("MyBox##Expression##")) {
                script = value.substring("MyBox##Expression##".length());
            }
        }
        if (!isRandom && !isRandomNn && script == null && !needFilter()) {
            try ( Connection conn = DerbyBase.getConnection();
                     Statement update = conn.createStatement()) {
                String sql = null;
                for (int col : cols) {
                    Data2DColumn column = columns.get(col);
                    Object ovalue = column.fromString(value);
                    if (ovalue == null) {
                        continue;
                    }
                    String quote = column.valueQuoted() ? "'" : "";
                    if (sql == null) {
                        sql = "";
                    } else {
                        sql += ", ";
                    }
                    sql += column.getColumnName() + "=" + quote + ovalue + quote;
                }
                if (sql == null) {
                    return false;
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
                rowIndex = 0;
                int count = 0;
                startFilter();
                while (results.next() && task != null && !task.isCancelled()) {
                    Data2DRow row = tableData2D.readData(results);
                    List<String> rowValues = new ArrayList<>();
                    for (int c = 0; c < columns.size(); c++) {
                        Data2DColumn column = columns.get(c);
                        Object v = row.getColumnValue(columns.get(c).getColumnName());
                        rowValues.add(column.toString(v));
                    }
                    filterDataRow(rowValues, ++rowIndex);
                    if (!filterPassed()) {
                        continue;
                    }
                    if (script != null) {
                        calculateDataRowExpression(script, rowValues, rowIndex);
                        if (error != null) {
                            if (errorContinue) {
                                continue;
                            } else {
                                task.setError(error);
                                return false;
                            }
                        }
                    }
                    for (int c = 0; c < columns.size(); c++) {
                        Data2DColumn column = columns.get(c);
                        String name = column.getColumnName();
                        if (cols.contains(c)) {
                            String v;
                            if (isBlank) {
                                v = "";
                            } else if (isRandom) {
                                v = random(random, c, false);
                            } else if (isRandomNn) {
                                v = random(random, c, true);
                            } else if (script != null) {
                                v = getExpressionResult();
                            } else {
                                v = value;
                            }
                            row.setColumnValue(name, column.fromString(v));
                        }
                    }
                    tableData2D.updateData(conn, row);
                    if (++count % DerbyBase.BatchSize == 0) {
                        conn.commit();
                    }
                    if (filterReachMaxFilterPassed()) {
                        break;
                    }
                }
                conn.commit();
            } catch (Exception e) {
                if (task != null) {
                    task.setError(e.toString());
                }
                MyBoxLog.debug(e);
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean delete(boolean errorContinue) {
        if (!needFilter()) {
            return clearData() >= 0;
        } else {
            try ( Connection conn = DerbyBase.getConnection();
                     PreparedStatement query = conn.prepareStatement("SELECT * FROM " + sheet);
                     ResultSet results = query.executeQuery()) {
                conn.setAutoCommit(false);
                rowIndex = 0;
                int count = 0;
                startFilter();
                while (results.next() && task != null && !task.isCancelled()) {
                    Data2DRow row = tableData2D.readData(results);
                    List<String> rowValues = new ArrayList<>();
                    for (int c = 0; c < columns.size(); c++) {
                        Data2DColumn column = columns.get(c);
                        Object v = row.getColumnValue(columns.get(c).getColumnName());
                        rowValues.add(column.toString(v));
                    }
                    filterDataRow(rowValues, ++rowIndex);
                    if (error != null) {
                        if (errorContinue) {
                            continue;
                        } else {
                            task.setError(error);
                            return false;
                        }
                    }
                    if (!filterPassed()) {
                        continue;
                    }
                    tableData2D.deleteData(conn, row);
                    if (++count % DerbyBase.BatchSize == 0) {
                        conn.commit();
                    }
                    if (filterReachMaxFilterPassed()) {
                        break;
                    }
                }
                conn.commit();
            } catch (Exception e) {
                if (task != null) {
                    task.setError(e.toString());
                }
                MyBoxLog.debug(e);
                return false;
            }
        }
        return true;
    }

    @Override
    public long clearData() {
        return tableData2D.clearData();
    }

    public DataFileCSV sort(List<Integer> cols, String orderName, boolean desc, boolean showRowNumber) {
        try {
            if (cols == null || cols.isEmpty() || orderName == null || orderName.isBlank()) {
                return null;
            }
            String sql = null;
            for (int col : cols) {
                if (sql != null) {
                    sql += ",";
                } else {
                    sql = "";
                }
                sql += columns.get(col).getColumnName();
            }
            sql = "SELECT " + sql + " FROM " + sheet + " ORDER BY " + orderName
                    + (desc ? " DESC" : "");
            File csvFile = tmpCSV("sort");
            rowIndex = 0;
            int colsSize;
            try ( CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile);
                     Connection conn = DerbyBase.getConnection();
                     PreparedStatement statement = conn.prepareStatement(sql);
                     ResultSet results = statement.executeQuery()) {
                List<String> names = new ArrayList<>();
                if (showRowNumber) {
                    names.add(message("SourceRowNumber"));
                }
                for (int col : cols) {
                    names.add(columns.get(col).getColumnName());
                }
                csvPrinter.printRecord(names);
                colsSize = names.size();
                List<String> fileRow = new ArrayList<>();
                startFilter();
                while (results.next() && task != null && !task.isCancelled()) {
                    Data2DRow dataRow = tableData2D.readData(results);
                    List<String> rowValues = new ArrayList<>();
                    for (int c = 0; c < columns.size(); c++) {
                        Data2DColumn column = columns.get(c);
                        Object v = dataRow.getColumnValue(columns.get(c).getColumnName());
                        rowValues.add(column.toString(v));
                    }
                    filterDataRow(rowValues, ++rowIndex);
                    if (!filterPassed()) {
                        continue;
                    }
                    if (showRowNumber) {
                        fileRow.add(rowIndex + "");
                    }
                    for (int col : cols) {
                        Data2DColumn column = columns.get(col);
                        fileRow.add(column.toString(rowValues.get(col)));
                    }
                    csvPrinter.printRecord(fileRow);
                    fileRow.clear();
                    if (filterReachMaxFilterPassed()) {
                        break;
                    }
                }
            } catch (Exception e) {
                if (task != null) {
                    task.setError(e.toString());
                }
                MyBoxLog.error(e.toString());
                return null;
            }
            DataFileCSV targetData = new DataFileCSV();
            targetData.setFile(csvFile).setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(true)
                    .setColsNumber(colsSize).setRowsNumber(rowIndex);
            return targetData;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public DataFileCSV query(String query, boolean showRowNumber) {
        if (query == null || query.isBlank()) {
            return null;
        }
        DataFileCSV targetData = null;
        try ( Connection conn = DerbyBase.getConnection();
                 PreparedStatement statement = conn.prepareStatement(query);
                 ResultSet results = statement.executeQuery()) {
            if (results != null) {
                targetData = save(task, results, showRowNumber);
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e.toString());
        }
        return targetData;
    }

    public Object mode(Connection conn, String colName) {
        if (colName == null || colName.isBlank()) {
            return null;
        }
        Object mode = null;
        String sql = "SELECT " + colName + ", count(" + colName + ") AS mybox99_mode FROM " + sheet
                + " GROUP BY " + colName + " ORDER BY mybox99_mode DESC FETCH FIRST ROW ONLY";
        try ( PreparedStatement statement = conn.prepareStatement(sql);
                 ResultSet results = statement.executeQuery()) {
            if (results.next()) {
                mode = results.getObject(colName);
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e.toString());
        }
        return mode;
    }

    // https://commons.apache.org/proper/commons-math/apidocs/org/apache/commons/math4/stat/descriptive/rank/Percentile.html
    public Object percentile(Connection conn, Data2DColumn column, int p) {
        if (column == null || p <= 0 || p > 100) {
            return null;
        }
        Object percentile = null;
        int n = tableData2D.size(conn);
        if (n == 0) {
            return null;
        }
        int offset, num;
        double d = 0;
        if (n == 1) {
            offset = 0;
            num = 1;
        } else {
            double pos = p * (n + 1) / 100d;
            if (pos < 1) {
                offset = 0;
                num = 1;
            } else if (pos >= n) {
                offset = n - 1;
                num = 1;
            } else {
                offset = (int) Math.floor(pos);
                d = pos - offset;
                num = 2;
            }
        }
        String colName = column.getColumnName();
        String sql = "SELECT " + colName + " FROM " + sheet + " ORDER BY " + colName
                + " OFFSET " + offset + " ROWS FETCH NEXT " + num + " ROWS ONLY";
        try ( PreparedStatement statement = conn.prepareStatement(sql);
                 ResultSet results = statement.executeQuery()) {
            Object first = null;
            if (results.next()) {
                first = results.getObject(colName);
            }
            if (num == 1) {
                percentile = first;
            } else if (num == 2) {
                if (results.next()) {
                    Object second = results.getObject(colName);
                    try {
                        double lower = Double.valueOf(first + "");
                        double upper = Double.valueOf(second + "");
                        percentile = lower + d * (upper - lower);
                    } catch (Exception e) {
                        percentile = first;
                    }
                } else {
                    percentile = first;
                }
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e.toString());
        }
        return percentile;
    }

    @Override
    public DataFileCSV frequency(Frequency frequency, String colName, int col, int scale) {
        if (frequency == null || colName == null || col < 0) {
            return null;
        }
        if (needFilter()) {
            return super.frequency(frequency, colName, col, scale);
        }
        File csvFile = tmpCSV("frequency");
        int total = 0, dNumber = 0;
        try ( CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile);
                 Connection conn = DerbyBase.getConnection()) {
            List<String> row = new ArrayList<>();
            row.add(colName);
            row.add(colName + "_" + message("Count"));
            row.add(colName + "_" + message("CountPercentage"));
            csvPrinter.printRecord(row);

            String sql = "SELECT count(" + colName + ") AS mybox99_count FROM " + sheet;
            try ( PreparedStatement statement = conn.prepareStatement(sql);
                     ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    total = results.getInt("mybox99_count");
                }
            } catch (Exception e) {
            }
            if (total == 0) {
                if (task != null) {
                    task.setError(message("NoData"));
                }
                return null;
            }
            row.clear();
            row.add(message("All"));
            row.add(total + "");
            row.add("100");
            dNumber = 1;
            csvPrinter.printRecord(row);
            sql = "SELECT " + colName + ", count(" + colName + ") AS mybox99_count FROM " + sheet
                    + " GROUP BY " + colName + " ORDER BY mybox99_count DESC";
            try ( PreparedStatement statement = conn.prepareStatement(sql);
                     ResultSet results = statement.executeQuery()) {
                while (results.next() && task != null && !task.isCancelled()) {
                    row.clear();
                    Object c = results.getObject(colName);
                    row.add(c != null ? c.toString() : null);
                    int count = results.getInt("mybox99_count");
                    row.add(count + "");
                    row.add(DoubleTools.percentage(count, total, scale));
                    csvPrinter.printRecord(row);
                    dNumber++;
                }
            } catch (Exception e) {
                if (task != null) {
                    task.setError(e.toString());
                }
                MyBoxLog.error(e);
                return null;
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setFile(csvFile).setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(true)
                    .setColsNumber(3).setRowsNumber(dNumber);
            return targetData;
        } else {
            return null;
        }
    }

    public boolean createTable(SingletonTask task, Connection conn, Data2D sourceData, String name) {
        try {
            tableData2D.reset();
            String tableName = DerbyBase.fixedIdentifier(name);
            tableData2D.setTableName(tableName);
            String idname = tableName.replace("\"", "") + "_id";
            Data2DColumn idcolumn = new Data2DColumn(idname, ColumnDefinition.ColumnType.Long);
            idcolumn.setAuto(true).setIsPrimaryKey(true).setNotNull(true).setEditable(false);
            columns = new ArrayList<>();
            columns.add(idcolumn);
            tableData2D.addColumn(idcolumn);
            for (Data2DColumn sourceColumn : sourceData.getColumns()) {
                Data2DColumn dataColumn = new Data2DColumn();
                dataColumn.cloneFrom(sourceColumn);
                dataColumn.setD2id(-1);
                dataColumn.setD2cid(-1);
                String columeName = DerbyBase.fixedIdentifier(sourceColumn.getColumnName());
                if (columeName.equalsIgnoreCase(idname)) {
                    columeName += "m";
                }
                dataColumn.setColumnName(columeName);
                columns.add(dataColumn);
                tableData2D.addColumn(dataColumn);
            }
            if (conn.createStatement().executeUpdate(tableData2D.createTableStatement()) < 0) {
                return false;
            }
            conn.commit();
            rowsNumber = sourceData.getRowsNumber();
            return recordTable(conn, tableName, columns);
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return false;
        }
    }

    public static DataTable toTable(SingletonTask task, DataFileCSV csvData, boolean dropExisted) {
        if (task == null || csvData == null) {
            return null;
        }
        File csvFile = csvData.getFile();
        if (csvFile == null || !csvFile.exists() || csvFile.length() == 0) {
            return null;
        }
        DataTable dataTable = new DataTable();
        try ( Connection conn = DerbyBase.getConnection()) {
            List<Data2DColumn> columns = csvData.getColumns();
            if (columns == null || columns.isEmpty()) {
                csvData.readColumns(conn);
                columns = csvData.getColumns();
            }
            if (columns == null || columns.isEmpty()) {
                return null;
            }
            TableData2D tableData2D = dataTable.getTableData2D();
            String tableName = DerbyBase.fixedIdentifier(FileNameTools.prefix(csvFile.getName()));
            if (tableData2D.exist(conn, tableName)) {
                if (!dropExisted) {
                    return null;
                }
                dataTable.getTableData2DDefinition().deleteUserTable(conn, tableName);
                conn.commit();
            }
            if (!dataTable.createTable(task, conn, csvData, tableName)) {
                return null;
            }
            columns = dataTable.getColumns();
            if (columns == null || columns.isEmpty()) {
                return null;
            }
            File validFile = FileTools.removeBOM(csvFile);
            try ( CSVParser parser = CSVParser.parse(validFile, csvData.getCharset(), csvData.cvsFormat())) {
                Iterator<CSVRecord> iterator = parser.iterator();
                int colsNumber = columns.size() - 1;
                int count = 0;
                conn.setAutoCommit(false);
                while (iterator.hasNext() && task != null && !task.isCancelled()) {
                    try {
                        CSVRecord record = iterator.next();
                        if (record != null) {
                            Data2DRow data2DRow = tableData2D.newRow();
                            for (int i = 0; i < Math.min(colsNumber, record.size()); i++) {
                                Data2DColumn column = columns.get(i + 1);
                                String name = column.getColumnName();
                                Object value = column.fromString(record.get(i));
                                if (value != null) {
                                    data2DRow.setColumnValue(name, value);
                                }
                            }
                            tableData2D.insertData(conn, data2DRow);
                            if (++count % DerbyBase.BatchSize == 0) {
                                conn.commit();
                            }
                        }
                    } catch (Exception e) {  // skip  bad lines
                        MyBoxLog.error(e);
                    }
                }
                conn.commit();
            } catch (Exception e) {
                if (task != null) {
                    task.setError(e.toString());
                }
                MyBoxLog.error(e);
                return null;
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        return dataTable;
    }

    public static DataFileCSV save(SingletonTask task, ResultSet results, boolean showRowNumber) {
        try {
            if (results == null) {
                return null;
            }
            File csvFile = TmpFileTools.csvFile();
            long count = 0;
            int colsSize;
            List<Data2DColumn> db2Columns = new ArrayList<>();
            try ( CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
                List<String> names = new ArrayList<>();
                if (showRowNumber) {
                    names.add(message("SourceRowNumber"));
                }
                ResultSetMetaData meta = results.getMetaData();

                for (int col = 1; col <= meta.getColumnCount(); col++) {
                    String name = meta.getColumnName(col);
                    names.add(name);
                    Data2DColumn dc = new Data2DColumn(name,
                            ColumnDefinition.sqlColumnType(meta.getColumnType(col)),
                            meta.isNullable(col) == ResultSetMetaData.columnNoNulls);
                    db2Columns.add(dc);
                }
                csvPrinter.printRecord(names);
                colsSize = names.size();
                List<String> fileRow = new ArrayList<>();
                while (results.next() && task != null && !task.isCancelled()) {
                    count++;
                    if (showRowNumber) {
                        fileRow.add(count + "");
                    }
                    for (Data2DColumn column : db2Columns) {
                        Object v = results.getObject(column.getColumnName());
                        fileRow.add(v == null ? "" : column.toString(v));
                    }
                    csvPrinter.printRecord(fileRow);
                    fileRow.clear();
                }
            } catch (Exception e) {
                MyBoxLog.error(e.toString());
                return null;
            }
            DataFileCSV targetData = new DataFileCSV();
            targetData.setFile(csvFile).setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(true)
                    .setColsNumber(colsSize).setRowsNumber(count);
            if (showRowNumber) {
                db2Columns.add(0, new Data2DColumn(message("SourceRowNumber"), ColumnDefinition.ColumnType.Long));
            }
            targetData.setColumns(db2Columns);
            return targetData;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    /*
        static
     */
    public static List<String> userTables() {
        List<String> userTables = new ArrayList<>();
        try ( Connection conn = DerbyBase.getConnection()) {
            List<String> allTables = DerbyBase.allTables(conn);
            for (String name : allTables) {
                if (!DataInternalTable.InternalTables.contains(name.toUpperCase())) {
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
