package mara.mybox.data2d;

import java.io.File;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import mara.mybox.calculation.DescriptiveStatistic;
import mara.mybox.calculation.DoubleStatistic;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.Data2DRow;
import mara.mybox.db.table.TableData2D;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.ExpressionCalculator;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.tools.CsvTools;
import mara.mybox.tools.DoubleTools;
import static mara.mybox.value.Languages.message;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.math3.stat.Frequency;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public class DataTable extends Data2D {

    protected TableData2D tableData2D;
    protected Map<String, String> columnsMap;
    protected List<Data2DColumn> sourceColumns;

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
            sheet = DerbyBase.savedName(tableName);
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

    public String mappedColumnName(String sourceName) {
        if (columnsMap == null || columnsMap.isEmpty()
                || !columnsMap.containsKey(sourceName)) {
            return sourceName;
        }
        return columnsMap.get(sourceName);
    }

    public String sourceColumnName(String mappedName) {
        if (columnsMap == null || columnsMap.isEmpty()
                || !columnsMap.containsValue(mappedName)) {
            return mappedName;
        }
        for (String sourceName : columnsMap.keySet()) {
            String v = columnsMap.get(sourceName);
            if (mappedName == null) {
                if (v == null) {
                    return sourceName;
                }
            } else {
                if (mappedName.equals(v)) {
                    return sourceName;
                }
            }
        }
        return mappedName;
    }

    public Data2DColumn sourceColumn(Data2DColumn mappedColumn) {
        if (columnsMap == null || columnsMap.isEmpty()
                || mappedColumn == null) {
            return null;
        }
        String sourceColumnName = sourceColumnName(mappedColumn.getColumnName());
        if (sourceColumnName == null) {
            return null;
        }
        List<Data2DColumn> sColumns = sourceColumns();
        if (sColumns == null) {
            return null;
        }
        for (Data2DColumn column : sColumns) {
            if (sourceColumnName.equals(column.getColumnName())) {
                return column;
            }
        }
        return null;
    }

    public List<Data2DColumn> sourceColumns() {
        if (columnsMap == null || columnsMap.isEmpty() || sourceColumns == null) {
            return columns;
        }
        return sourceColumns;
    }

    public List<String> sourceColumnNames() {
        if (columnsMap == null || columnsMap.isEmpty() || sourceColumns == null) {
            return columnNames();
        }
        List<String> names = new ArrayList<>();
        for (Data2DColumn column : sourceColumns) {
            names.add(column.getColumnName());
        }
        return names;
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
    public boolean setValue(ExpressionCalculator calculator, List<Integer> cols, String value, boolean errorContinue) {
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
                        calculator.calculateDataRowExpression(this, script, rowValues, rowIndex);
                        error = calculator.getError();
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
                                v = calculator.getResult();
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
                    if (filterReachMaxPassed()) {
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
                    if (filterReachMaxPassed()) {
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

    public int drop() {
        if (sheet == null || sheet.isBlank()) {
            return -4;
        }
        try ( Connection conn = DerbyBase.getConnection();) {
            return drop(conn, sheet);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return -5;
        }
    }

    public int drop(Connection conn, String name) {
        if (name == null || name.isBlank()) {
            return -4;
        }
        return tableData2DDefinition.deleteUserTable(conn, name);
    }

    // Based on results of "Data2D_Convert.toTmpTable(...)"
    public DataFileCSV sort(SingletonTask task, String orderName, boolean desc) {
        if (orderName == null || orderName.isBlank() || sourceColumns == null) {
            return null;
        }
        File csvFile = tmpCSV("sort");
        try ( CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile);
                 Connection conn = DerbyBase.getConnection();
                 PreparedStatement statement = conn.prepareStatement(
                        "SELECT * FROM " + sheet + " ORDER BY " + orderName + (desc ? " DESC" : ""));
                 ResultSet results = statement.executeQuery()) {

            csvPrinter.printRecord(sourceColumnNames());
            rowIndex = 0;
            while (results.next() && task != null && !task.isCancelled()) {
                Data2DRow dataRow = tableData2D.readData(results);
                List<String> rowValues = new ArrayList<>();
                for (int i = 1; i < columns.size(); i++) {  // skip id column
                    Data2DColumn column = columns.get(i);
                    Object v = dataRow.getColumnValue(column.getColumnName());
                    rowValues.add(column.toString(v));
                }
                csvPrinter.printRecord(rowValues);
                rowIndex++;
            }
            DataFileCSV targetData = new DataFileCSV();
            targetData.setColumns(sourceColumns).setFile(csvFile)
                    .setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(true)
                    .setColsNumber(sourceColumns.size()).setRowsNumber(rowIndex);
            return targetData;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    // Based on results of "Data2D_Convert.toTmpTable(...)"
    public DataFileCSV transpose(SingletonTask task, boolean showColNames, boolean firstColumnAsNames) {
        if (sourceColumns == null) {
            return null;
        }
        File csvFile = tmpCSV("transpose");
        try ( CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile);
                 Connection conn = DerbyBase.getConnection()) {
            String idName = columns.get(0).getColumnName();
            int rNumber = 0;
            List<Data2DColumn> targetColumns = new ArrayList<>();
            List<Data2DColumn> dataColumns = new ArrayList<>();
            if (firstColumnAsNames && columns.get(1).getColumnName().equals(mappedColumnName(message("SourceRowNumber")))) {
                dataColumns.add(columns.get(2));
                dataColumns.add(columns.get(1));
                for (int i = 3; i < columns.size(); i++) {
                    dataColumns.add(columns.get(i));
                }
            } else {
                for (int i = 1; i < columns.size(); i++) {
                    dataColumns.add(columns.get(i));
                }
            }    // skip id column
            for (int i = 0; i < dataColumns.size(); i++) {
                if (task == null || task.isCancelled()) {
                    break;
                }
                Data2DColumn column = dataColumns.get(i);
                String columnName = column.getColumnName();
                List<String> rowValues = new ArrayList<>();
                try ( PreparedStatement statement = conn.prepareStatement(
                        "SELECT " + idName + "," + columnName + " FROM " + sheet + " ORDER BY " + idName);
                         ResultSet results = statement.executeQuery()) {
                    while (results.next() && task != null && !task.isCancelled()) {
                        rowValues.add(results.getString(columnName));
                    }
                } catch (Exception e) {
                    if (task != null) {
                        task.setError(e.toString());
                    }
                    MyBoxLog.error(e.toString());
                    return null;
                }
                if (i == 0) {
                    int cNumber = rowValues.size();
                    List<String> names = new ArrayList<>();
                    if (firstColumnAsNames) {
                        for (int c = 0; c < cNumber; c++) {
                            String name = rowValues.get(c);
                            if (name == null || name.isBlank()) {
                                name = message("Columns") + (c + 1);
                            }
                            while (names.contains(name)) {
                                name += "m";
                            }
                            names.add(name);
                        }
                    } else {
                        for (int c = 1; c <= cNumber; c++) {
                            names.add(message("Column") + c);
                        }
                    }
                    if (showColNames) {
                        String name = message("ColumnName");
                        while (names.contains(name)) {
                            name += "m";
                        }
                        names.add(0, name);
                    }
                    for (int c = 0; c < names.size(); c++) {
                        targetColumns.add(new Data2DColumn(names.get(c), ColumnDefinition.ColumnType.String));
                    }
                    if (!firstColumnAsNames) {
                        csvPrinter.printRecord(names);
                    }
                }
                if (showColNames) {
                    rowValues.add(0, columnName);
                }
                csvPrinter.printRecord(rowValues);
                rNumber++;
            }
            DataFileCSV targetData = new DataFileCSV();
            targetData.setColumns(targetColumns).setFile(csvFile)
                    .setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(true)
                    .setColsNumber(targetColumns.size()).setRowsNumber(rNumber);
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
                targetData = DataFileCSV.save(task, results, showRowNumber);
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

    public DoubleStatistic[] statisticByColumnsForStored(List<Integer> cols, DescriptiveStatistic selections) {
        if (cols == null || cols.isEmpty() || selections == null) {
            return null;
        }

        try ( Connection conn = DerbyBase.getConnection()) {
            int colLen = cols.size();
            DoubleStatistic[] sData = new DoubleStatistic[colLen];
            for (int c = 0; c < cols.size(); c++) {
                Data2DColumn column = columns.get(cols.get(c));
                DoubleStatistic colStatistic = column.getDoubleStatistic();
                if (colStatistic == null) {
                    colStatistic = new DoubleStatistic();
                    column.setDoubleStatistic(colStatistic);
                }
                sData[c] = colStatistic;
                if (selections.median) {
                    Object m = percentile(conn, column, 50);
                    if (m == null) {
                        colStatistic.modeValue = null;
                    } else if (column.isNumberType()) {
                        colStatistic.modeValue = DoubleTools.format(Double.valueOf(m + ""), scale);
                    } else {
                        colStatistic.modeValue = column.toString(m);
                    }
                }
                Object q1 = null, q3 = null;
                if (selections.upperQuartile || selections.needOutlier()) {
                    q3 = percentile(conn, column, 75);
                    colStatistic.upperQuartileValue = q3;
                    try {
                        colStatistic.upperQuartile = Double.valueOf(q3 + "");
                    } catch (Exception ex) {
                        colStatistic.upperQuartile = Double.NaN;
                    }
                }
                if (selections.lowerQuartile || selections.needOutlier()) {
                    q1 = percentile(conn, column, 25);
                    colStatistic.lowerQuartileValue = q1;
                    try {
                        colStatistic.lowerQuartile = Double.valueOf(q1 + "");
                    } catch (Exception ex) {
                        colStatistic.lowerQuartile = Double.NaN;
                    }
                }
                if (selections.upperExtremeOutlierLine) {
                    try {
                        double d1 = Double.valueOf(q1 + "");
                        double d3 = Double.valueOf(q3 + "");
                        colStatistic.upperExtremeOutlierLine = d3 + 3 * (d3 - d1);
                    } catch (Exception e) {
                        colStatistic.upperExtremeOutlierLine = Double.NaN;
                    }
                }
                if (selections.upperMildOutlierLine) {
                    try {
                        double d1 = Double.valueOf(q1 + "");
                        double d3 = Double.valueOf(q3 + "");
                        colStatistic.upperMildOutlierLine = d3 + 1.5 * (d3 - d1);
                    } catch (Exception e) {
                        colStatistic.upperMildOutlierLine = Double.NaN;
                    }
                }
                if (selections.lowerMildOutlierLine) {
                    try {
                        double d1 = Double.valueOf(q1 + "");
                        double d3 = Double.valueOf(q3 + "");
                        colStatistic.lowerMildOutlierLine = d1 - 1.5 * (d3 - d1);
                    } catch (Exception e) {
                        colStatistic.lowerMildOutlierLine = Double.NaN;
                    }
                }
                if (selections.lowerExtremeOutlierLine) {
                    try {
                        double d1 = Double.valueOf(q1 + "");
                        double d3 = Double.valueOf(q3 + "");
                        colStatistic.lowerExtremeOutlierLine = d1 - 3 * (d3 - d1);
                    } catch (Exception e) {
                        colStatistic.lowerExtremeOutlierLine = Double.NaN;
                    }
                }
                if (selections.mode) {
                    colStatistic.modeValue = mode(conn, column.getColumnName());
                }
            }
            return sData;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e.toString());
            return null;
        }
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

    public Map<String, String> getColumnsMap() {
        return columnsMap;
    }

    public void setColumnsMap(Map<String, String> columnsMap) {
        this.columnsMap = columnsMap;
    }

    public List<Data2DColumn> getSourceColumns() {
        return sourceColumns;
    }

    public void setSourceColumns(List<Data2DColumn> sourceColumns) {
        this.sourceColumns = sourceColumns;
    }

}
