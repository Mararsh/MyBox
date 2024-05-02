package mara.mybox.data2d;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import mara.mybox.data.DataSort;
import mara.mybox.data.FindReplaceString;
import mara.mybox.data2d.DataTableGroup.TimeType;
import mara.mybox.data2d.tools.Data2DColumnTools;
import mara.mybox.data2d.tools.Data2DTableTools;
import mara.mybox.data2d.writer.Data2DWriter;
import mara.mybox.data2d.writer.DataFileCSVWriter;
import mara.mybox.data2d.writer.DataTableWriter;
import mara.mybox.db.Database;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.ColumnDefinition.InvalidAs;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DRow;
import static mara.mybox.db.table.BaseTable.StringMaxLength;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ExpressionCalculator;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTmpTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-12-2
 * @License Apache License Version 2.0
 */
public class TmpTable extends DataTable {

    public static String TmpTablePrefix = "MYBOXTMP__";
    public static String ExpressionColumnName = "MYBOX_GROUP_EXPRESSION__999";

    protected Data2D sourceData;
    protected List<Data2DColumn> sourceReferColumns;
    protected List<Integer> sourcePickIndice, sourceReferIndice;
    protected List<String> orders, groupEqualColumnNames;
    protected int valueIndexOffset, timeIndex, expressionIndex;
    protected String targetName, groupRangeColumnName, groupTimeColumnName,
            groupExpression, tmpOrderby;
    protected boolean importData, forStatistic, includeRowNumber, includeColName;
    protected TimeType groupTimeType;
    protected InvalidAs invalidAs;
    protected List<List<String>> importRows;
    protected Calendar calendar;
    protected ExpressionCalculator expressionCalculator;
    protected FindReplaceString findReplace;

    public TmpTable() {
        init();
    }

    public final void init() {
        sourceData = null;
        sourcePickIndice = null;
        sourceReferColumns = null;
        orders = null;
        groupEqualColumnNames = null;
        groupRangeColumnName = null;
        groupTimeColumnName = null;
        groupTimeType = null;
        groupExpression = null;
        timeIndex = -1;
        expressionIndex = -1;
        expressionCalculator = null;
        importData = forStatistic = includeRowNumber = includeColName = false;
        valueIndexOffset = -1;
        invalidAs = InvalidAs.Skip;
        targetName = null;
        importRows = null;
        tmpOrderby = "";
        calendar = Calendar.getInstance();
    }

    public boolean createTable() {
        if (sourceData == null) {
            return false;
        }
        if (targetName == null) {
            targetName = sourceData.getDataName();
        }
        try (Connection conn = DerbyBase.getConnection()) {
            List<Data2DColumn> sourceColumns = sourceData.getColumns();
            if (sourceColumns == null || sourceColumns.isEmpty()) {
                sourceData.readColumns(conn);
            }
            if (sourceColumns == null || sourceColumns.isEmpty()) {
                return false;
            }
            valueIndexOffset = 1;
            List<Data2DColumn> tmpColumns = new ArrayList<>();
            if (includeRowNumber) {
                tmpColumns.add(new Data2DColumn(message("SourceRowNumber"), ColumnType.Long));
                valueIndexOffset++;
            }
            sourceReferIndice = new ArrayList<>();
            sourceReferColumns = new ArrayList<>();
            for (int i = 0; i < sourcePickIndice.size(); i++) {
                int col = sourcePickIndice.get(i);
                Data2DColumn sourceColumn = sourceColumns.get(col);
                sourceReferColumns.add(sourceColumn);
                sourceReferIndice.add(col);
                Data2DColumn tableColumn = sourceColumn.cloneAll();
                tableColumn.setD2cid(-1).setD2id(-1).setLength(StringMaxLength);
                tableColumn.setType(forStatistic ? ColumnType.Double : ColumnType.String);
                tmpColumns.add(tableColumn);
            }
            timeIndex = -1;
            if (groupEqualColumnNames != null && !groupEqualColumnNames.isEmpty()) {
                for (String name : groupEqualColumnNames) {
                    Data2DColumn sourceColumn = sourceData.columnByName(name);
                    sourceReferColumns.add(sourceColumn);
                    Data2DColumn tableColumn = sourceColumn.cloneAll();
                    tableColumn.setD2cid(-1).setD2id(-1);
                    tmpColumns.add(tableColumn);
                    sourceReferIndice.add(sourceData.colOrder(name));
                }

            } else if (groupRangeColumnName != null && !groupRangeColumnName.isBlank()) {
                Data2DColumn sourceColumn = sourceData.columnByName(groupRangeColumnName);
                sourceReferColumns.add(sourceColumn);
                Data2DColumn tableColumn = sourceColumn.cloneAll();
                tableColumn.setD2cid(-1).setD2id(-1).setType(ColumnType.Double);
                tmpColumns.add(tableColumn);
                sourceReferIndice.add(sourceData.colOrder(groupRangeColumnName));

            } else if (groupTimeColumnName != null && !groupTimeColumnName.isBlank()) {
                Data2DColumn sourceColumn = sourceData.columnByName(groupTimeColumnName);
                sourceReferColumns.add(sourceColumn);
                Data2DColumn tableColumn = sourceColumn.cloneAll();
                tableColumn.setD2cid(-1).setD2id(-1).setType(ColumnType.Long);
                tmpColumns.add(tableColumn);
                timeIndex = sourceReferIndice.size();
                sourceReferIndice.add(sourceData.colOrder(groupTimeColumnName));

            } else if (groupExpression != null && !groupExpression.isBlank()) {
                tmpColumns.add(new Data2DColumn(ExpressionColumnName, ColumnType.String));
                expressionIndex = sourceReferIndice.size();
                Data2DColumn sourceColumn = sourceData.column(0);
                sourceReferColumns.add(sourceColumn);
                sourceReferIndice.add(0);
                expressionCalculator = new ExpressionCalculator();

            }
            int sortStartIndex = -1;
            if (orders != null && !orders.isEmpty()) {
                sortStartIndex = sourceReferIndice.size();
                List<String> sortNames = DataSort.parseNames(orders);
                for (String name : sortNames) {
                    Data2DColumn sourceColumn = sourceData.columnByName(name);
                    sourceReferColumns.add(sourceColumn);
                    Data2DColumn tableColumn = sourceColumn.cloneAll();
                    tableColumn.setD2cid(-1).setD2id(-1);
                    tmpColumns.add(tableColumn);
                    sourceReferIndice.add(sourceData.colOrder(name));
                }
            }
            DataTable dataTable = Data2DTableTools.createTable(task, conn, null, tmpColumns);
            if (dataTable == null) {
                return false;
            }
            sheet = dataTable.getSheet();
            columns = dataTable.getColumns();
            colsNumber = columns.size();
            tableData2D = dataTable.getTableData2D();
            dataName = dataTable.getDataName();
            if (importData) {
                importData(conn);
            }
            if (sortStartIndex > 0) {
                List<DataSort> sorts = DataSort.parse(orders);
                List<DataSort> tmpsorts = new ArrayList<>();
                for (DataSort sort : sorts) {
                    String sortName = sort.getName();
                    for (int i = sortStartIndex; i < sourceReferColumns.size(); i++) {
                        if (sortName.equals(sourceReferColumns.get(i).getColumnName())) {
                            sortName = columnName(i + valueIndexOffset);
                            break;
                        }
                    }
                    tmpsorts.add(new DataSort(sortName, sort.isAscending()));
                }
                tmpOrderby = DataSort.toString(tmpsorts);
            } else {
                tmpOrderby = null;
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return false;
        }
        return true;
    }

    public long importData(Connection conn) {
        try {
            if (conn == null || sourceData == null || columns == null || columns.isEmpty()) {
                return -1;
            }
            if (importRows != null) {
                return importRows(conn);
            }
            DataTableWriter writer = new DataTableWriter();
            writer.setTargetTable(this)
                    .setColumns(columns)
                    .setHeaderNames(Data2DColumnTools.toNames(columns))
                    .setWriteHeader(false);
            return sourceData.copy(task, writer, sourceData.columnIndices(),
                    includeRowNumber, invalidAs);
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return -4;
        }
    }

    // sourceRow should include values of all source columns
    @Override
    public Data2DRow makeRow(List<String> sourceRow, InvalidAs invalidAs) {
        try {
            if (columns == null || sourceRow == null || sourceRow.isEmpty()) {
                return null;
            }
            Data2DRow data2DRow = tableData2D.newRow();
            long index;
            List<String> values;
            if (includeRowNumber) {
                index = Long.parseLong(sourceRow.get(0));
                data2DRow.setColumnValue(column(1).getColumnName(), index);
                values = sourceRow.subList(1, sourceRow.size());
            } else {
                index = -1;
                values = sourceRow;
            }
            int len = values.size();
            for (int i = 0; i < sourceReferIndice.size(); i++) {
                int col = sourceReferIndice.get(i);
                if (col < 0 || col >= len) {
                    continue;
                }
                Data2DColumn targetColumn = column(i + valueIndexOffset);
                Object tmpValue;
                if (i == expressionIndex) {
                    expressionCalculator.calculateDataRowExpression(sourceData, groupExpression, sourceRow, index);
                    tmpValue = expressionCalculator.getResult();
                } else {
                    Data2DColumn sourceColumn = sourceData.column(col);
                    String sourceValue = values.get(col);
                    switch (targetColumn.getType()) {
                        case String:
                            tmpValue = sourceValue;
                            break;
                        case Double:
                            tmpValue = sourceColumn.toDouble(sourceValue);
                            break;
                        case Long:
                            if (timeIndex != i) {
                                tmpValue = targetColumn.fromString(sourceValue, invalidAs);
                            } else {
                                Date d = DateTools.encodeDate(sourceValue);
                                if (d == null) {
                                    tmpValue = targetColumn.fromString(sourceValue, invalidAs);
                                } else {
                                    calendar.setTime(d);
                                    switch (groupTimeType) {
                                        case Century:
                                            int year = calendar.get(Calendar.YEAR);
                                            int cYear = (year / 100) * 100;
                                            if (year % 100 == 0) {
                                                calendar.set(cYear, 0, 1, 0, 0, 0);
                                            } else {
                                                calendar.set(cYear + 1, 0, 1, 0, 0, 0);
                                            }
                                            break;
                                        case Year:
                                            calendar.set(calendar.get(Calendar.YEAR), 0, 1, 0, 0, 0);
                                            break;
                                        case Month:
                                            calendar.set(calendar.get(Calendar.YEAR),
                                                    calendar.get(Calendar.MONTH),
                                                    1, 0, 0, 0);
                                            break;
                                        case Day:
                                            calendar.set(calendar.get(Calendar.YEAR),
                                                    calendar.get(Calendar.MONTH),
                                                    calendar.get(Calendar.DAY_OF_MONTH),
                                                    0, 0, 0);
                                            break;
                                        case Hour:
                                            calendar.set(calendar.get(Calendar.YEAR),
                                                    calendar.get(Calendar.MONTH),
                                                    calendar.get(Calendar.DAY_OF_MONTH),
                                                    calendar.get(Calendar.HOUR_OF_DAY),
                                                    0, 0);
                                            break;
                                        case Minute:
                                            calendar.set(calendar.get(Calendar.YEAR),
                                                    calendar.get(Calendar.MONTH),
                                                    calendar.get(Calendar.DAY_OF_MONTH),
                                                    calendar.get(Calendar.HOUR_OF_DAY),
                                                    calendar.get(Calendar.MINUTE),
                                                    0);
                                            break;
                                        case Second:
                                            calendar.set(calendar.get(Calendar.YEAR),
                                                    calendar.get(Calendar.MONTH),
                                                    calendar.get(Calendar.DAY_OF_MONTH),
                                                    calendar.get(Calendar.HOUR_OF_DAY),
                                                    calendar.get(Calendar.MINUTE),
                                                    calendar.get(Calendar.SECOND));
                                            break;
                                    }
                                    tmpValue = calendar.getTime().getTime();
                                }
                            }
                            break;
                        default:
                            tmpValue = targetColumn.fromString(sourceValue, invalidAs);
                    }
                }
                data2DRow.setColumnValue(targetColumn.getColumnName(), tmpValue);
            }
            return data2DRow;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return null;
        }
    }

    public long importRows(Connection conn) {
        if (conn == null || importRows == null || importRows.isEmpty()) {
            return -1;
        }
        try (PreparedStatement insert = conn.prepareStatement(tableData2D.insertStatement())) {
            rowsNumber = 0;
            conn.setAutoCommit(false);
            for (List<String> row : importRows) {
                Data2DRow data2DRow = makeRow(row, invalidAs);
                if (tableData2D.setInsertStatement(conn, insert, data2DRow)) {
                    insert.addBatch();
                    if (++rowsNumber % Database.BatchSize == 0) {
                        insert.executeBatch();
                        conn.commit();
                        if (task != null) {
                            task.setInfo(message("Inserted") + ": " + rowsNumber);
                        }
                    }
                }
            }
            insert.executeBatch();
            conn.commit();
            if (task != null) {
                task.setInfo(message("Inserted") + ": " + rowsNumber);
            }
            conn.setAutoCommit(true);
            return rowsNumber;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return -4;
        }
    }

    public DataFileCSV sort(int maxSortResults) {
        try {
            if (sourceData == null) {
                return null;
            }
            DataFileCSVWriter writer = new DataFileCSVWriter();
            writer.setPrintFile(FileTmpTools.getTempFile(".csv"));
            List<Data2DColumn> targetColumns = new ArrayList<>();
            List<String> names = new ArrayList<>();
            if (includeRowNumber) {
                targetColumns.add(new Data2DColumn(message("SourceRowNumber"), ColumnDefinition.ColumnType.Long));
                names.add(message("SourceRowNumber"));
            }
            for (int i : sourcePickIndice) {
                Data2DColumn column = sourceData.column(i).cloneAll();
                String name = DerbyBase.checkIdentifier(names, column.getColumnName(), true);
                column.setD2cid(-1).setD2id(-1).setColumnName(name);
                targetColumns.add(column);
            }
            writer.setColumns(targetColumns)
                    .setHeaderNames(names)
                    .setWriteHeader(false);
            if (!sort(task, writer, maxSortResults)) {
                return null;
            }
            return (DataFileCSV) writer.getTargetData();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean sort(FxTask currentTask, Data2DWriter writer, int maxSortResults) {
        if (writer == null || sourceData == null) {
            return false;
        }
        String sql = "SELECT * FROM " + sheet
                + (tmpOrderby != null && !tmpOrderby.isBlank() ? " ORDER BY " + tmpOrderby : "");
        if (maxSortResults > 0) {
            sql += " FETCH FIRST " + maxSortResults + " ROWS ONLY";
        }
        if (currentTask != null) {
            currentTask.setInfo(sql);
        }
        writer.openWriter();
        try (ResultSet query = DerbyBase.getConnection().prepareStatement(sql).executeQuery()) {
            if (includeColName) {
                writer.writeRow(writer.getHeaderNames());
            }
            long count = 0;
            String numberName = columnName(1);
            while (query.next()) {
                if (currentTask != null && !currentTask.isWorking()) {
                    break;
                }
                Data2DRow dataRow = tableData2D.readData(query);
                List<String> rowValues = new ArrayList<>();
                if (includeRowNumber) {
                    Object v = dataRow.getColumnValue(numberName);
                    rowValues.add(v == null ? null : v + "");
                }
                for (int i = 0; i < sourcePickIndice.size(); i++) {
                    Data2DColumn tmpColumn = columns.get(i + valueIndexOffset);
                    Object v = dataRow.getColumnValue(tmpColumn.getColumnName());
                    rowValues.add(v == null ? null : v + "");
                }
                writer.writeRow(rowValues);
                count++;
            }
        } catch (Exception e) {
            if (currentTask != null) {
                currentTask.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return false;
        }
        writer.closeWriter();
        return true;
    }

    public boolean transpose(FxTask currentTask, Data2DWriter writer, boolean firstColumnAsNames) {
        if (writer == null || sourceData == null) {
            return false;
        }
        writer.setColumns(null).setHeaderNames(null).setWriteHeader(false).openWriter();
        try (Connection conn = DerbyBase.getConnection()) {
            String idName = columns.get(0).getColumnName();
            List<Data2DColumn> targetColumns = new ArrayList<>();
            List<Data2DColumn> dataColumns = new ArrayList<>();
            // skip id column
            if (firstColumnAsNames && includeRowNumber) {
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
            for (int i = 0; i < dataColumns.size(); i++) {
                if (currentTask != null && !currentTask.isWorking()) {
                    break;
                }
                Data2DColumn column = dataColumns.get(i);
                String columnName = column.getColumnName();
                List<String> rowValues = new ArrayList<>();
                String sql = "SELECT " + idName + "," + columnName + " FROM " + sheet + " ORDER BY " + idName;
                if (currentTask != null) {
                    currentTask.setInfo(sql);
                }
                try (PreparedStatement statement = conn.prepareStatement(sql);
                        ResultSet results = statement.executeQuery()) {
                    String sname = DerbyBase.savedName(columnName);
                    while (results.next()) {
                        if (currentTask != null && !currentTask.isWorking()) {
                            break;
                        }
                        rowValues.add(results.getString(sname));
                    }
                } catch (Exception e) {
                    if (currentTask != null) {
                        currentTask.setError(e.toString());
                    } else {
                        MyBoxLog.error(e);
                    }
                    return false;
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
                            DerbyBase.checkIdentifier(names, name, true);
                        }
                    } else {
                        for (int c = 1; c <= cNumber; c++) {
                            names.add(message("Column") + c);
                        }
                    }
                    if (includeColName) {
                        String name = DerbyBase.checkIdentifier(names, message("ColumnName"), false);
                        names.add(0, name);
                    }
                    for (int c = 0; c < names.size(); c++) {
                        targetColumns.add(new Data2DColumn(names.get(c), ColumnType.String));
                    }
                    writer.setColumns(targetColumns).setHeaderNames(names).setWriteHeader(true);
                    writer.writeRow(names);
                }
                if (includeColName) {
                    rowValues.add(0, columnName);
                }
                writer.writeRow(rowValues);
            }
        } catch (Exception e) {
            if (currentTask != null) {
                currentTask.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return false;
        }
        writer.closeWriter();
        return true;
    }

    public int parametersOffset() {
        return sourcePickIndice.size() + valueIndexOffset;
    }

    public Data2DColumn parameterSourceColumn() {
        return sourceReferColumns.get(sourcePickIndice.size());
    }

    public FindReplaceString findReplace() {
        if (findReplace == null) {
            findReplace = FindReplaceString.create().
                    setOperation(FindReplaceString.Operation.ReplaceAll)
                    .setIsRegex(false).setCaseInsensitive(false).setMultiline(false);
        }
        return findReplace;
    }

    public String tmpScript(String script) {
        if (script == null || script.isBlank()) {
            return null;
        }
        String tmpScript = script;
        for (int i = 0; i < sourcePickIndice.size(); i++) {
            if (task == null || task.isCancelled()) {
                return null;
            }
            int col = sourcePickIndice.get(i);
            String sourceName = sourceData.columnName(col);
            String tmpName = columnName(i + valueIndexOffset);
            tmpScript = findReplace().replace(task, script, "#{" + sourceName + "}", "#{" + tmpName + "}");
        }
        return tmpScript;
    }

    /* 
        static
     */
    public static String tmpTableName() {
        return TmpTablePrefix + DateTools.nowString3();
    }

    public static String tmpTableName(String sourceName) {
        return TmpTablePrefix + sourceName + DateTools.nowString3();
    }

    public static TmpTable toStatisticTable(Data2D sourceData, FxTask task,
            List<Integer> cols, InvalidAs invalidAs) {
        if (cols == sourceData || cols == null || cols.isEmpty()) {
            return null;
        }
        TmpTable tmpTable = new TmpTable().setSourceData(sourceData)
                .setSourcePickIndice(cols)
                .setImportData(true)
                .setForStatistic(true)
                .setIncludeRowNumber(false)
                .setInvalidAs(invalidAs);
        tmpTable.setTask(task);
        if (tmpTable.createTable()) {
            return tmpTable;
        } else {
            return null;
        }
    }

    /*
        set
     */
    public TmpTable setSourceData(Data2D sourceData) {
        this.sourceData = sourceData;
        return this;
    }

    public TmpTable setSourcePickIndice(List<Integer> sourcePickIndice) {
        this.sourcePickIndice = sourcePickIndice;
        return this;
    }

    public TmpTable setTargetName(String targetName) {
        this.targetName = targetName;
        return this;
    }

    public TmpTable setImportData(boolean importData) {
        this.importData = importData;
        return this;
    }

    public TmpTable setForStatistic(boolean forStatistic) {
        this.forStatistic = forStatistic;
        return this;
    }

    public TmpTable setIncludeRowNumber(boolean includeRowNumber) {
        this.includeRowNumber = includeRowNumber;
        return this;
    }

    public TmpTable setIncludeColName(boolean includeColName) {
        this.includeColName = includeColName;
        return this;
    }

    public TmpTable setOrders(List<String> orders) {
        this.orders = orders;
        return this;
    }

    public TmpTable setGroupEqualColumnNames(List<String> groupEqualColumnNames) {
        this.groupEqualColumnNames = groupEqualColumnNames;
        return this;
    }

    public TmpTable setGroupRangleColumnName(String groupRangleColumnName) {
        this.groupRangeColumnName = groupRangleColumnName;
        return this;
    }

    public TmpTable setGroupTimeColumnName(String groupTimeColumnName) {
        this.groupTimeColumnName = groupTimeColumnName;
        return this;
    }

    public TmpTable setGroupTimeType(TimeType groupTimeType) {
        this.groupTimeType = groupTimeType;
        return this;
    }

    public TmpTable setGroupExpression(String groupExpression) {
        this.groupExpression = groupExpression;
        return this;
    }

    public TmpTable setInvalidAs(InvalidAs invalidAs) {
        this.invalidAs = invalidAs;
        return this;
    }

    public TmpTable setImportRows(List<List<String>> importRows) {
        this.importRows = importRows;
        return this;
    }

    /*
        get
     */
    public Data2D getSourceData() {
        return sourceData;
    }

    public List<Integer> getSourcePickIndice() {
        return sourcePickIndice;
    }

    public List<Integer> getSourceReferIndice() {
        return sourceReferIndice;
    }

    public void setSourceReferIndice(List<Integer> sourceReferIndice) {
        this.sourceReferIndice = sourceReferIndice;
    }

    public List<String> getOrders() {
        return orders;
    }

    public String getTmpOrderby() {
        return tmpOrderby;
    }

    public List<String> getGroupEqualColumnNames() {
        return groupEqualColumnNames;
    }

    public int getValueIndexOffset() {
        return valueIndexOffset;
    }

    public String getTargetName() {
        return targetName;
    }

    public String getGroupRangeColumnName() {
        return groupRangeColumnName;
    }

    public String getGroupExpression() {
        return groupExpression;
    }

    public boolean isImportData() {
        return importData;
    }

    public boolean isForStatistic() {
        return forStatistic;
    }

    public boolean isIncludeRowNumber() {
        return includeRowNumber;
    }

    public boolean isIncludeColName() {
        return includeColName;
    }

    public InvalidAs getInvalidAs() {
        return invalidAs;
    }

}
