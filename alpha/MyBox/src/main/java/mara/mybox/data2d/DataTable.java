package mara.mybox.data2d;

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
import mara.mybox.data2d.tools.Data2DTableTools;
import mara.mybox.data2d.writer.Data2DWriter;
import mara.mybox.data2d.writer.DataTableWriter;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.InvalidAs;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.Data2DRow;
import mara.mybox.db.table.TableData2D;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.DoubleTools;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public class DataTable extends Data2D {

    protected TableData2D tableData2D;

    public DataTable() {
        dataType = DataType.DatabaseTable;
        tableData2D = new TableData2D();
    }

    public int type() {
        return type(DataType.DatabaseTable);
    }

    public void cloneAll(DataTable d) {
        try {
            if (d == null) {
                return;
            }
            super.cloneData(d);
            tableData2D = d.tableData2D;
            if (tableData2D == null) {
                tableData2D = new TableData2D();
            }
            tableData2D.setTableName(sheet);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void resetData() {
        super.resetData();
        tableData2D.reset();
    }

    public boolean readDefinitionFromDB(Connection conn, String tname) {
        try {
            if (conn == null || tname == null) {
                return false;
            }
            resetData();
            sheet = DerbyBase.fixedIdentifier(tname);
            tableData2D.setTableName(sheet);
            tableData2D.readDefinitionFromDB(conn, sheet);
            List<ColumnDefinition> dbColumns = tableData2D.getColumns();
            List<Data2DColumn> dataColumns = new ArrayList<>();
            if (dbColumns != null) {
                for (ColumnDefinition dbColumn : dbColumns) {
                    Data2DColumn dataColumn = new Data2DColumn();
                    dataColumn.cloneFrom(dbColumn);
                    dataColumns.add(dataColumn);
                }
            }
            return recordTable(conn, sheet, dataColumns, comments);
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
            sheet = DerbyBase.fixedIdentifier(tableName);
            dataName = sheet;
            colsNumber = dataColumns.size();
            this.comments = comments;
            tableData2DDefinition.writeTable(conn, this);
            conn.commit();

            for (Data2DColumn column : dataColumns) {
                column.setD2id(d2did);
                column.setColumnName(DerbyBase.fixedIdentifier(column.getColumnName()));
            }
            columns = dataColumns;
            tableData2DColumn.save(conn, d2did, dataColumns);
            conn.commit();
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
        return tableData2DDefinition.queryTable(conn, sheet, dataType);
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
                    for (Data2DColumn savedColumn : savedColumns) {
                        if (dbColumn.getColumnName().equalsIgnoreCase(savedColumn.getColumnName())) {
                            dbColumn.setIndex(savedColumn.getIndex());
                            dbColumn.setType(savedColumn.getType());
                            dbColumn.setFormat(savedColumn.getFormat());
                            dbColumn.setScale(savedColumn.getScale());
                            dbColumn.setColor(savedColumn.getColor());
                            dbColumn.setWidth(savedColumn.getWidth());
                            dbColumn.setEditable(savedColumn.isEditable());
                            if (dbColumn.getDefaultValue() == null) {
                                dbColumn.setDefaultValue(savedColumn.getDefaultValue());
                            }
                            dbColumn.setDescription(savedColumn.getDescription());
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
            try {
                data2DRow.setRowIndex(Integer.parseInt(values.get(0)));
            } catch (Exception e) {
                data2DRow.setRowIndex(-1);
            }
            for (int i = 0; i < Math.min(columns.size(), values.size() - 1); i++) {
                Data2DColumn column = columns.get(i);
                String name = column.getColumnName();
                data2DRow.setMapValue(name, column.fromString(values.get(i + 1), invalidAs));
            }
            return data2DRow;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
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
    public long savePageData(FxTask task) {
        try (Connection conn = DerbyBase.getConnection()) {
            return savePageData(task, conn);
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return -1;
        }
    }

    public long savePageData(FxTask task, Connection conn) {
        try {
            List<Data2DRow> dbRows = tableData2D.query(conn, pageQuery());
            List<Data2DRow> pageRows = new ArrayList<>();
            conn.setAutoCommit(false);
            if (pageData != null) {
                for (int i = 0; i < pageData.size(); i++) {
                    Data2DRow row = fromTableRow(pageData.get(i), InvalidAs.Empty);
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
            rowsNumber = tableData2D.size(conn);
            saveAttributes(conn);
            return rowsNumber;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return -1;
        }
    }

    @Override
    public long saveAttributes(FxTask task, Data2D attributes) {
        if (attributes == null) {
            return -1;
        }
        try (Connection conn = DerbyBase.getConnection()) {
            if (savePageData(task, conn) < 0) {
                return -1;
            }
            List<String> currentNames = columnNames();
            List<String> newNames = new ArrayList<>();
            for (Data2DColumn column : attributes.columns) {
                String name = column.getColumnName();
                newNames.add(name);
                if (currentNames.contains(name) && column.getIndex() < 0) {
                    tableData2D.dropColumn(conn, name);
                    conn.commit();
                    currentNames.remove(name);
                }
            }
            for (String name : currentNames) {
                if (!newNames.contains(name)) {
                    tableData2D.dropColumn(conn, name);
                    conn.commit();
                }
            }
            for (Data2DColumn column : attributes.columns) {
                String name = column.getColumnName();
                if (!currentNames.contains(name)) {
                    tableData2D.addColumn(conn, column);
                    conn.commit();
                }
            }
            attributes.rowsNumber = rowsNumber;
            attributes.tableChanged = false;
            attributes.currentPage = currentPage;
            cloneData(attributes);
            return rowsNumber;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return -1;
        }
    }

    @Override
    public Data2DWriter selfWriter() {
        DataTableWriter writer = new DataTableWriter();
        writer.setTargetTable(this)
                .setTargetData(this)
                .setRecordTargetFile(false)
                .setRecordTargetData(true);
        return writer;
    }

    public Data2DRow makeRow(List<String> values, InvalidAs invalidAs) {
        try {
            if (columns == null || values == null || values.isEmpty()) {
                return null;
            }
            List<Data2DColumn> vColumns = new ArrayList<>();
            for (Data2DColumn c : columns) {
                if (!c.isAuto()) {
                    vColumns.add(c);
                }
            }
            Data2DRow data2DRow = tableData2D.newRow();
            int rowSize = values.size();
            for (int i = 0; i < values.size(); i++) {
                Data2DColumn column = vColumns.get(i);
                String name = column.getColumnName();
                String value = i < rowSize ? values.get(i) : null;
                data2DRow.setMapValue(name, column.fromString(value, invalidAs));
            }
            return data2DRow;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @Override
    public int drop() {
        if (sheet == null || sheet.isBlank()) {
            return -4;
        }
        try (Connection conn = DerbyBase.getConnection();) {
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

    public boolean query(FxTask task, Data2DWriter writer,
            String query, String rowNumberName) {
        if (writer == null || query == null || query.isBlank()) {
            return false;
        }
        if (task != null) {
            task.setInfo(query);
        }
        try (Connection conn = DerbyBase.getConnection();
                PreparedStatement statement = conn.prepareStatement(query);
                ResultSet results = statement.executeQuery()) {
            return Data2DTableTools.write(task, this, writer, results, rowNumberName,
                    scale, InvalidAs.Empty);
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
            return false;
        }
    }

    public Object mode(Connection conn, String colName) {
        if (colName == null || colName.isBlank()) {
            return null;
        }
        Object mode = null;
        String sql = "SELECT " + colName + ", count(*) AS mybox99_mode FROM " + sheet
                + " GROUP BY " + colName + " ORDER BY mybox99_mode DESC FETCH FIRST ROW ONLY";
        if (task != null) {
            task.setInfo(sql);
        }
        try (PreparedStatement statement = conn.prepareStatement(sql);
                ResultSet results = statement.executeQuery()) {
            if (results.next()) {
                mode = results.getObject(DerbyBase.savedName(colName));
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
        if (task != null) {
            task.setInfo(sql);
        }
        try (PreparedStatement statement = conn.prepareStatement(sql);
                ResultSet results = statement.executeQuery()) {
            Object first = null;
            if (results.next()) {
                first = column.value(results);
            }
            if (num == 1) {
                percentile = first;
            } else if (num == 2) {
                if (results.next()) {
                    Object second = column.value(results);;
                    try {
                        double lower = Double.parseDouble(first + "");
                        double upper = Double.parseDouble(second + "");
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
        try (Connection conn = DerbyBase.getConnection()) {
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
                        colStatistic.median = Double.parseDouble(colStatistic.medianValue + "");
                    } catch (Exception ex) {
                        colStatistic.median = DoubleTools.value(colStatistic.invalidAs);
                    }
                }
                Object q1 = null, q3 = null;
                if (selections.include(StatisticType.UpperQuartile) || selections.needOutlier()) {
                    q3 = percentile(conn, column, 75);
                    colStatistic.upperQuartileValue = q3;
                    try {
                        colStatistic.upperQuartile = Double.parseDouble(q3 + "");
                    } catch (Exception ex) {
                        colStatistic.upperQuartile = DoubleTools.value(colStatistic.invalidAs);
                    }
                }
                if (selections.include(StatisticType.LowerQuartile) || selections.needOutlier()) {
                    q1 = percentile(conn, column, 25);
                    colStatistic.lowerQuartileValue = q1;
                    try {
                        colStatistic.lowerQuartile = Double.parseDouble(q1 + "");
                    } catch (Exception ex) {
                        colStatistic.lowerQuartile = DoubleTools.value(colStatistic.invalidAs);
                    }
                }
                if (selections.include(StatisticType.UpperExtremeOutlierLine)) {
                    try {
                        double d1 = Double.parseDouble(q1 + "");
                        double d3 = Double.parseDouble(q3 + "");
                        colStatistic.upperExtremeOutlierLine = d3 + 3 * (d3 - d1);
                    } catch (Exception e) {
                        colStatistic.upperExtremeOutlierLine = DoubleTools.value(colStatistic.invalidAs);
                    }
                }
                if (selections.include(StatisticType.UpperMildOutlierLine)) {
                    try {
                        double d1 = Double.parseDouble(q1 + "");
                        double d3 = Double.parseDouble(q3 + "");
                        colStatistic.upperMildOutlierLine = d3 + 1.5 * (d3 - d1);
                    } catch (Exception e) {
                        colStatistic.upperMildOutlierLine = DoubleTools.value(colStatistic.invalidAs);
                    }
                }
                if (selections.include(StatisticType.LowerMildOutlierLine)) {
                    try {
                        double d1 = Double.parseDouble(q1 + "");
                        double d3 = Double.parseDouble(q3 + "");
                        colStatistic.lowerMildOutlierLine = d1 - 1.5 * (d3 - d1);
                    } catch (Exception e) {
                        colStatistic.lowerMildOutlierLine = DoubleTools.value(colStatistic.invalidAs);
                    }
                }
                if (selections.include(StatisticType.LowerExtremeOutlierLine)) {
                    try {
                        double d1 = Double.parseDouble(q1 + "");
                        double d3 = Double.parseDouble(q3 + "");
                        colStatistic.lowerExtremeOutlierLine = d1 - 3 * (d3 - d1);
                    } catch (Exception e) {
                        colStatistic.lowerExtremeOutlierLine = DoubleTools.value(colStatistic.invalidAs);
                    }
                }
                if (selections.include(StatisticType.Mode)) {
                    colStatistic.modeValue = mode(conn, column.getColumnName());
                    try {
                        colStatistic.mode = Double.parseDouble(colStatistic.modeValue + "");
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
