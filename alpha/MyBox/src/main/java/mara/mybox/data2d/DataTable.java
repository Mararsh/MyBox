package mara.mybox.data2d;

import java.io.File;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import mara.mybox.calculation.DescriptiveStatistic;
import mara.mybox.calculation.DescriptiveStatistic.StatisticType;
import mara.mybox.calculation.DoubleStatistic;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.Data2DRow;
import mara.mybox.db.table.TableData2D;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
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
            return recordTable(conn, referredName, dataColumns, comments);
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return false;
        }
    }

    public boolean recordTable(Connection conn, String tableName, List<Data2DColumn> dataColumns, String comments) {
        try {
            sheet = DerbyBase.savedName(tableName);
            dataName = tableName;
            colsNumber = dataColumns.size();
            this.comments = comments;
            tableData2DDefinition.insertData(conn, this);
            conn.commit();

            for (Data2DColumn column : dataColumns) {
                column.setD2id(d2did);
            }
            columns = dataColumns;
            tableData2DColumn.save(conn, d2did, dataColumns);
            conn.commit();
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
                            if (dbColumn.isStringType() && scolumn.isDBStringType()
                                    || dbColumn.isDoubleType() && scolumn.isDBDoubleType()
                                    || dbColumn.isNumberType() && scolumn.isDateType()) {
                                dbColumn.setType(scolumn.getType());
                            }
                            dbColumn.setFormat(scolumn.getFormat());
                            dbColumn.setScale(scolumn.getScale());
                            dbColumn.setColor(scolumn.getColor());
                            dbColumn.setWidth(scolumn.getWidth());
                            dbColumn.setEditable(scolumn.isEditable());
                            if (dbColumn.getDefaultValue() == null) {
                                dbColumn.setDefaultValue(scolumn.getDefaultValue());
                            }
                            dbColumn.setDescription(scolumn.getDescription());
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
            } else {
                MyBoxLog.error(e);
            }
            return false;
        }
    }

    @Override
    public List<String> readColumnNames() {
        return null;
    }

    @Override
    public Data2DColumn columnByName(String name) {
        try {
            if (name == null || name.isBlank()) {
                return null;
            }
            for (Data2DColumn c : columns) {
                if (name.equalsIgnoreCase(c.getColumnName())) {
                    return c;
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    public Data2DRow fromTableRow(List<String> values, InvalidAs invalidAs) {
        try {
            if (columns == null || values == null || values.isEmpty()) {
                return null;
            }
            Data2DRow data2DRow = tableData2D.newRow();
            data2DRow.setRowIndex(Integer.valueOf(values.get(0)));
            for (int i = 0; i < Math.min(columns.size(), values.size() - 1); i++) {
                Data2DColumn column = columns.get(i);
                String name = column.getColumnName();
                data2DRow.setColumnValue(name, column.fromString(values.get(i + 1), invalidAs));
            }
            return data2DRow;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public String tmpColumnName(String sourceName) {
        try {
            if (sourceColumns == null) {
                return sourceName;
            }
            int index = tmpIndex(sourceName);
            if (index < 0) {
                return null;
            }
            return columns.get(index).getColumnName();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public int tmpIndex(String sourceName) {
        if (sourceName == null || sourceColumns == null) {
            return -1;
        }
        for (int i = 0; i < sourceColumns.size(); i++) {
            Data2DColumn column = sourceColumns.get(i);
            if (sourceName.equalsIgnoreCase(column.getColumnName())) {
                return i + 1;
            }
        }
        return -1;
    }

    public String sourceColumnName(String tmpName) {
        if (tmpName == null || sourceColumns == null) {
            return tmpName;
        }
        try {
            int index = colOrder(tmpName);
            if (index < 0) {
                return null;
            }
            return sourceColumns.get(index - 1).getColumnName();
        } catch (Exception e) {
            return null;
        }
    }

    public List<Data2DColumn> sourceColumns() {
        if (sourceColumns == null) {
            return columns;
        }
        return sourceColumns;
    }

    public List<String> sourceColumnNames() {
        if (sourceColumns == null) {
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
            } else {
                MyBoxLog.error(e);
            }
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
                    Data2DRow row = fromTableRow(pageData.get(i), InvalidAs.Blank);
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
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
        }
        return false;
    }

    @Override
    public int drop() {
        if (sheet == null || sheet.isBlank()) {
            return -4;
        }
        try ( Connection conn = DerbyBase.getConnection();) {
            return drop(conn, sheet);
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return -5;
        }
    }

    public int drop(Connection conn) {
        return drop(conn, sheet);
    }

    public int drop(Connection conn, String name) {
        if (name == null || name.isBlank()) {
            return -4;
        }
        return tableData2DDefinition.deleteUserTable(conn, name);
    }

    // Based on results of "Data2D_Convert.toTmpTable(...)"
    public DataFileCSV sort(String dname, SingletonTask task,
            List<String> orders, int maxData, boolean includeColName) {
        if (sourceColumns == null) {
            return null;
        }
        File csvFile = tmpFile(dname, "sort", ".csv");
        String sql;
        try {
            String name;
            boolean asc;
            String orderby = null;
            if (orders == null || orders.isEmpty()) {
                orderby = "";
            } else {
                for (String order : orders) {
                    asc = order.endsWith("-" + message("Ascending"));
                    if (asc) {
                        name = order.substring(0, order.length() - ("-" + message("Ascending")).length());
                    } else {
                        name = order.substring(0, order.length() - ("-" + message("Descending")).length());
                    }
                    name = tmpColumnName(name) + (asc ? " ASC" : " DESC");
                    if (orderby == null) {
                        orderby = name;
                    } else {
                        orderby += ", " + name;
                    }
                }
                orderby = " ORDER BY " + orderby;
            }
            sql = "SELECT * FROM " + sheet + orderby;
            if (maxData > 0) {
                sql += " FETCH FIRST " + maxData + " ROWS ONLY";
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return null;
        }
        try ( CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile);
                 ResultSet query = DerbyBase.getConnection()
                        .prepareStatement(sql).executeQuery()) {
            if (includeColName) {
                csvPrinter.printRecord(sourceColumnNames());
            }
            rowIndex = 0;
            while (query.next() && task != null && !task.isCancelled()) {
                Data2DRow dataRow = tableData2D.readData(query);
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
            targetData.setColumns(sourceColumns)
                    .setFile(csvFile).setDataName(dname)
                    .setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(includeColName)
                    .setColsNumber(sourceColumns.size()).setRowsNumber(rowIndex);
            return targetData;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return null;
        }
    }

    // Based on results of "Data2D_Convert.toTmpTable(...)"
    public DataFileCSV transpose(String dname, SingletonTask task,
            boolean showColNames, boolean firstColumnAsNames) {
        if (sourceColumns == null) {
            return null;
        }
        File csvFile = tmpFile(dname, "transpose", ".csv");
        try ( CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile);
                 Connection conn = DerbyBase.getConnection()) {
            String idName = columns.get(0).getColumnName();
            int rNumber = 0;
            List<Data2DColumn> targetColumns = new ArrayList<>();
            List<Data2DColumn> dataColumns = new ArrayList<>();
            if (firstColumnAsNames && columns.get(1).getColumnName().equals(tmpColumnName(message("SourceRowNumber")))) {
                dataColumns.add(columns.get(2));
                dataColumns.add(columns.get(1));
                for (int i = 3; i < columns.size(); i++) {
                    dataColumns.add(columns.get(i));
                }
            } else {
                for (int i = 1; i < columns.size(); i++) {
                    dataColumns.add(columns.get(i));
                }
            }
            Random random = new Random();
            // skip id column
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
                    } else {
                        MyBoxLog.error(e);
                    }
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
                                name += random.nextInt(10);
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
                            name += random.nextInt(10);
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
            targetData.setColumns(targetColumns)
                    .setFile(csvFile).setDataName(dname)
                    .setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(true)
                    .setColsNumber(targetColumns.size()).setRowsNumber(rNumber);
            return targetData;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return null;
        }
    }

    public DataFileCSV query(String dname, SingletonTask task, String query, String rowNumberName) {
        return query(dname, task, query, rowNumberName, scale, InvalidAs.Blank);
    }

    public DataFileCSV query(String dname, SingletonTask task, String query, String rowNumberName,
            int dscale, InvalidAs invalidAs) {
        if (query == null || query.isBlank()) {
            return null;
        }
        DataFileCSV targetData = null;
        try ( Connection conn = DerbyBase.getConnection();
                 PreparedStatement statement = conn.prepareStatement(query);
                 ResultSet results = statement.executeQuery()) {
            if (results != null) {
                targetData = DataFileCSV.save(this, dname, task, results, rowNumberName, dscale, invalidAs);
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
        }
        return targetData;
    }

    public Object mode(Connection conn, String colName) {
        if (colName == null || colName.isBlank()) {
            return null;
        }
        Object mode = null;
        String sql = "SELECT " + colName + ", count(*) AS mybox99_mode FROM " + sheet
                + " GROUP BY " + colName + " ORDER BY mybox99_mode DESC FETCH FIRST ROW ONLY";
        try ( PreparedStatement statement = conn.prepareStatement(sql);
                 ResultSet results = statement.executeQuery()) {
            if (results.next()) {
                mode = results.getObject(colName);
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
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
            } else {
                MyBoxLog.error(e.toString());
            }
        }
        return percentile;
    }

    @Override
    public DoubleStatistic[] statisticByColumnsForStored(List<Integer> cols, DescriptiveStatistic selections) {
        if (cols == null || cols.isEmpty() || selections == null) {
            return null;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            int colLen = cols.size();
            DoubleStatistic[] sData = new DoubleStatistic[colLen];
            for (int c = 0; c < cols.size(); c++) {
                Data2DColumn column = columns.get(cols.get(c));
                DoubleStatistic colStatistic = column.getStatistic();
                if (colStatistic == null) {
                    colStatistic = new DoubleStatistic();
                    column.setStatistic(colStatistic);
                }
                colStatistic.invalidAs = selections.invalidAs;
                colStatistic.options = selections;
                sData[c] = colStatistic;
                if (selections.include(StatisticType.Median)) {
                    colStatistic.medianValue = percentile(conn, column, 50);
                    try {
                        colStatistic.median = Double.valueOf(colStatistic.medianValue + "");
                    } catch (Exception ex) {
                        colStatistic.median = DoubleTools.value(colStatistic.invalidAs);
                    }
                }
                Object q1 = null, q3 = null;
                if (selections.include(StatisticType.UpperQuartile) || selections.needOutlier()) {
                    q3 = percentile(conn, column, 75);
                    colStatistic.upperQuartileValue = q3;
                    try {
                        colStatistic.upperQuartile = Double.valueOf(q3 + "");
                    } catch (Exception ex) {
                        colStatistic.upperQuartile = DoubleTools.value(colStatistic.invalidAs);
                    }
                }
                if (selections.include(StatisticType.LowerQuartile) || selections.needOutlier()) {
                    q1 = percentile(conn, column, 25);
                    colStatistic.lowerQuartileValue = q1;
                    try {
                        colStatistic.lowerQuartile = Double.valueOf(q1 + "");
                    } catch (Exception ex) {
                        colStatistic.lowerQuartile = DoubleTools.value(colStatistic.invalidAs);
                    }
                }
                if (selections.include(StatisticType.UpperExtremeOutlierLine)) {
                    try {
                        double d1 = Double.valueOf(q1 + "");
                        double d3 = Double.valueOf(q3 + "");
                        colStatistic.upperExtremeOutlierLine = d3 + 3 * (d3 - d1);
                    } catch (Exception e) {
                        colStatistic.upperExtremeOutlierLine = DoubleTools.value(colStatistic.invalidAs);
                    }
                }
                if (selections.include(StatisticType.UpperMildOutlierLine)) {
                    try {
                        double d1 = Double.valueOf(q1 + "");
                        double d3 = Double.valueOf(q3 + "");
                        colStatistic.upperMildOutlierLine = d3 + 1.5 * (d3 - d1);
                    } catch (Exception e) {
                        colStatistic.upperMildOutlierLine = DoubleTools.value(colStatistic.invalidAs);
                    }
                }
                if (selections.include(StatisticType.LowerMildOutlierLine)) {
                    try {
                        double d1 = Double.valueOf(q1 + "");
                        double d3 = Double.valueOf(q3 + "");
                        colStatistic.lowerMildOutlierLine = d1 - 1.5 * (d3 - d1);
                    } catch (Exception e) {
                        colStatistic.lowerMildOutlierLine = DoubleTools.value(colStatistic.invalidAs);
                    }
                }
                if (selections.include(StatisticType.LowerExtremeOutlierLine)) {
                    try {
                        double d1 = Double.valueOf(q1 + "");
                        double d3 = Double.valueOf(q3 + "");
                        colStatistic.lowerExtremeOutlierLine = d1 - 3 * (d3 - d1);
                    } catch (Exception e) {
                        colStatistic.lowerExtremeOutlierLine = DoubleTools.value(colStatistic.invalidAs);
                    }
                }
                if (selections.include(StatisticType.Mode)) {
                    colStatistic.modeValue = mode(conn, column.getColumnName());
                    try {
                        colStatistic.mode = Double.valueOf(colStatistic.modeValue + "");
                    } catch (Exception ex) {
                        colStatistic.mode = DoubleTools.value(colStatistic.invalidAs);
                    }
                }
            }
            return sData;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
            return null;
        }
    }

    @Override
    public DataFileCSV frequency(String dname, Frequency frequency, String colName, int col, int scale) {
        if (frequency == null || colName == null || col < 0) {
            return null;
        }
        if (needFilter()) {
            return super.frequency(dname, frequency, colName, col, scale);
        }
        File csvFile = tmpFile(dname, "frequency", ".csv");
        int total = 0, dNumber = 0;
        try ( CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile);
                 Connection conn = DerbyBase.getConnection()) {
            List<String> row = new ArrayList<>();
            row.add(colName);
            row.add(colName + "_" + message("Count"));
            row.add(colName + "_" + message("CountPercentage"));
            csvPrinter.printRecord(row);

            String sql = "SELECT count(*) AS mybox99_count FROM " + sheet;
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
            sql = "SELECT " + colName + ", count(*) AS mybox99_count FROM " + sheet
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
                } else {
                    MyBoxLog.error(e);
                }
                return null;
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return null;
        }
        if (csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setFile(csvFile).setDataName(dname)
                    .setCharset(Charset.forName("UTF-8"))
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

    public List<Data2DColumn> getSourceColumns() {
        return sourceColumns;
    }

    public void setSourceColumns(List<Data2DColumn> sourceColumns) {
        this.sourceColumns = sourceColumns;
    }

}
