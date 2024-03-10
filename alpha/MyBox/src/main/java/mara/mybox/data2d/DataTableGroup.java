package mara.mybox.data2d;

import java.io.File;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.control.IndexRange;
import mara.mybox.controller.ControlData2DGroup;
import mara.mybox.data.DataSort;
import mara.mybox.data.ValueRange;
import static mara.mybox.data2d.Data2D_Convert.createTable;
import mara.mybox.db.Database;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.ColumnDefinition.InvalidAs;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DRow;
import mara.mybox.db.table.TableData2D;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.CsvTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import org.apache.commons.csv.CSVPrinter;

/**
 * @Author Mara
 * @CreateDate 2022-10-17
 * @License Apache License Version 2.0
 */
public class DataTableGroup {

    protected ControlData2DGroup groupController;
    protected GroupType groupType;
    protected Data2D originalData;
    protected TmpTable tmpData;
    protected String groupName, timeName;
    protected TimeType timeType;
    protected List<String> groupNames, orders;
    protected InvalidAs invalidAs;
    protected short scale;
    protected long max;
    protected TargetType targetType;
    protected FxTask task;
    protected boolean includeRowNumber, ok;
    protected List<Integer> sourcePickIndice;

    protected String tmpSheet, idColName, parameterName, parameterValue, parameterValueForFilename;
    protected String tmpOrderby, groupOrderby, mappedIdColName, mappedParameterName, dataComments;
    protected List<Data2DColumn> tmpColumns;
    protected long count, groupid, groupCurrentSize;
    protected int tmpValueOffset, targetValueOffset;

    protected Connection conn;
    protected List<String> targetColNames;
    protected List<Data2DColumn> targetColumns, finalColumns;

    protected DataTable targetData, groupParameters;
    protected TableData2D tableTmpData, tableTarget, tableGroupParameters;
    protected PreparedStatement insert;

    protected File csvFile;
    protected CSVPrinter csvPrinter;
    protected DataFileCSV targetFile;
    protected List<File> csvFiles;

    public enum GroupType {
        EqualValues, Time, Expression,
        ValueSplitInterval, ValueSplitNumber, ValueSplitList,
        RowsSplitInterval, RowsSplitNumber, RowsSplitList, Conditions
    }

    public enum TimeType {
        Century, Year, Month, Day, Hour, Minute, Second
    }

    public enum TargetType {
        SingleFile, MultipleFiles, Table, TmpTable
    }

    // This class is based on results of "Data2D_Convert.toTmpTable(...)"
    public DataTableGroup(Data2D originalData, ControlData2DGroup groupController, TmpTable tmpData) {
        this.originalData = originalData;
        this.groupController = groupController;
        this.tmpData = tmpData;
    }

    public boolean run() {
        if (originalData == null || groupController == null || tmpData == null
                || targetType == null || sourcePickIndice == null || sourcePickIndice.isEmpty()) {
            return false;
        }
        groupType = groupController.groupType();
        if (null == groupType) {
            return false;
        }
        groupName = groupController.groupName();
        groupNames = groupController.groupNames();
        timeName = groupController.timeName();
        timeType = groupController.timeType();
        tmpSheet = tmpData.getSheet();
        tmpColumns = tmpData.getColumns();
        if (tmpSheet == null || tmpColumns == null) {
            return false;
        }
        tmpOrderby = tmpData.getTmpOrderby();
        tmpValueOffset = tmpData.getValueIndexOffset();
        tableTmpData = tmpData.getTableData2D();
        ok = false;
        if (conn == null) {
            try (Connection dconn = DerbyBase.getConnection()) {
                conn = dconn;
                ok = scan();
                stopScan();
                if (ok) {
                    ok = finishGroup();
                }
                dconn.commit();
                dconn.close();
                conn = null;
            } catch (Exception e) {
                MyBoxLog.error(e);
                if (task != null) {
                    task.setError(e.toString());
                }
                ok = false;
            }
        } else {
            ok = scan();
            stopScan();
            if (ok) {
                finishGroup();
            }
        }
        if (!ok) {
            if (groupParameters != null) {
                groupParameters.drop();
                groupParameters = null;
            }
        }
        return ok;
    }

    private boolean scan() {
        try {
            switch (groupType) {
                case RowsSplitInterval:
                case RowsSplitNumber:
                case RowsSplitList:
                    parameterName = message("DataRowNumber");
                    break;
                case Conditions:
                    if (groupController.groupConditions() == null
                            || groupController.groupConditions().isEmpty()) {
                        return false;
                    }
                    parameterName = message("Condition");
                    break;
                case ValueSplitInterval:
                case ValueSplitNumber:
                case ValueSplitList:
                    if (groupName == null || groupName.isBlank()) {
                        return false;
                    }
                    parameterName = message("Range") + "_" + groupName;
                    break;
                case EqualValues:
                    if (groupNames == null || groupNames.isEmpty()) {
                        return false;
                    }
                    parameterName = message("EqualValues");
                    for (String name : groupNames) {
                        parameterName += "_" + name;
                    }
                    break;
                case Time:
                    if (timeName == null || timeName.isBlank()) {
                        return false;
                    }
                    parameterName = message(timeType.name()) + "_" + timeName;
                    break;
                case Expression:
                    parameterName = message("Expression");
                    break;
                default:
                    return false;
            }
            groupid = 0;
            groupCurrentSize = 0;
            count = 0;
            idColName = message("GroupID");
            targetData = null;
            insert = null;
            csvPrinter = null;
            csvFiles = new ArrayList<>();
            groupOrderby = null;
            dataComments = null;

            targetColNames = new ArrayList<>();
            targetColNames.add(idColName);
            targetColNames.add(parameterName);
            targetColumns = new ArrayList<>();
            targetColumns.add(new Data2DColumn(idColName, ColumnType.Long));
            targetColumns.add(new Data2DColumn(parameterName, ColumnType.String, 200));
            targetValueOffset = 2;
            if (includeRowNumber) {
                targetColumns.add(new Data2DColumn(message("SourceRowNumber"), ColumnType.Long));
                targetValueOffset++;
            }
            for (int c : sourcePickIndice) {
                Data2DColumn column = originalData.column(c).cloneAll();
                column.setD2cid(-1).setD2id(-1);
                targetColumns.add(column);
                targetColNames.add(column.getColumnName());
            }

            List<Data2DColumn> parametersColumns = new ArrayList<>();
            parametersColumns.add(new Data2DColumn("group_index", ColumnType.Long));
            parametersColumns.add(new Data2DColumn("group_parameters", ColumnType.String));
            groupParameters = createTable(task, conn, null, parametersColumns, null, null, null, true);
            tableGroupParameters = groupParameters.getTableData2D();

            dataComments = message("GroupBy") + ": " + message(groupType.name()) + "\n";
            switch (groupType) {
                case EqualValues:
                    dataComments += message("Columns") + ": " + groupNames.toString();
                    return byEqualValues();
                case ValueSplitInterval:
                    dataComments += message("Column") + ": " + groupName + "\n"
                            + message("Inteval") + ": " + groupController.valueSplitInterval();
                    return byValueInteval();
                case ValueSplitNumber:
                    dataComments += message("Column") + ": " + groupName + "\n"
                            + message("NumberOfSplit") + ": " + groupController.valueSplitNumber();
                    return byValueInteval();
                case ValueSplitList:
                    dataComments += message("Column") + ": " + groupName + "\n"
                            + message("List") + ":\n";
                    for (ValueRange range : groupController.valueSplitList()) {
                        dataComments += range.toString() + "\n";
                    }
                    return byValueList();
                case RowsSplitInterval:
                    dataComments += message("Interval") + ": " + groupController.rowsSplitInterval();
                    return byRowsInteval();
                case RowsSplitNumber:
                    dataComments += message("NumberOfSplit") + ": " + groupController.rowsSplitNumber();
                    return byRowsInteval();
                case RowsSplitList:
                    dataComments += message("List") + ":\n";
                    List<Integer> splitList = groupController.rowsSplitList();
                    for (int i = 0; i < splitList.size();) {
                        dataComments += "[" + splitList.get(i++) + "," + splitList.get(i++) + "]" + "\n";
                    }
                    return byRowsList();
                case Conditions:
                    dataComments += message("List") + ":\n";
                    for (DataFilter filter : groupController.groupConditions()) {
                        dataComments += filter.toString() + "\n";
                    }
                    return byConditions();
                case Time:
                    dataComments += message("Column") + ": " + timeName + "\n"
                            + message("Same") + ": " + timeType.name();
                    return byTime();
                case Expression:
                    dataComments += message("RowExpression") + ": " + "\n"
                            + tmpData.getGroupExpression();
                    return byExpression();
            }
            return false;
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
            return false;
        }
    }

    private int parametersOffset() {
        return tmpData.parametersOffset();
    }

    private boolean byEqualValues() {
        try {
            if (groupType != GroupType.EqualValues || groupNames == null || groupNames.isEmpty()) {
                return false;
            }
            String finalOrderBy = null;
            List<String> mappedGroupNames = new ArrayList<>();
            int offset = parametersOffset();
            for (int i = 0; i < groupNames.size(); i++) {
                String name = tmpData.columnName(i + offset);
                if (finalOrderBy == null) {
                    finalOrderBy = name;
                } else {
                    finalOrderBy += ", " + name;
                }
                mappedGroupNames.add(name);
            }
            if (tmpOrderby != null && !tmpOrderby.isBlank()) {
                finalOrderBy += "," + tmpOrderby;
            }
            String sql = "SELECT * FROM " + tmpSheet + " ORDER BY " + finalOrderBy;
            if (task != null) {
                task.setInfo(sql);
            }
            try (ResultSet query = conn.prepareStatement(sql).executeQuery()) {
                Data2DRow tmpRow, lastRow = null;
                Map<String, Object> groupMap = new HashMap<>();
                boolean groupChanged;
                conn.setAutoCommit(false);
                while (query.next() && task != null && !task.isCancelled()) {
                    if (task == null || task.isCancelled()) {
                        query.close();
                        return false;
                    }
                    try {
                        tmpRow = tableTmpData.readData(query);
                        if (lastRow == null) {
                            groupChanged = true;
                        } else {
                            groupChanged = false;
                            for (String group : mappedGroupNames) {
                                Object tv = tmpRow.getColumnValue(group);
                                Object lv = lastRow.getColumnValue(group);
                                if (tv == null) {
                                    if (lv != null) {
                                        groupChanged = true;
                                        break;
                                    }
                                } else {
                                    if (!tv.equals(lv)) {
                                        groupChanged = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if (groupChanged) {
                            groupChanged();
                            parameterValueForFilename = idColName + groupid;
                            parameterValue = null;
                            groupMap.clear();
                            for (int i = 0; i < groupNames.size(); i++) {
                                groupMap.put(groupNames.get(i), tmpRow.getColumnValue(mappedGroupNames.get(i)));
                            }
                            parameterValue = groupMap.toString();
                            recordGroup(groupid, parameterValue);
                        }
                        if (++groupCurrentSize <= max || max <= 0) {
                            writeRow(tmpRow);
                        }
                        lastRow = tmpRow;
                    } catch (Exception e) {
                        if (task != null) {
                            task.setError(e.toString());
                        } else {
                            MyBoxLog.error(e.toString());
                        }
                    }
                }
            } catch (Exception e) {
                if (task != null) {
                    task.setError(e.toString());
                } else {
                    MyBoxLog.error(e.toString());
                }
            }
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
            return false;
        }
    }

    private boolean byValueInteval() {
        try {
            if (groupName == null || groupName.isBlank()) {
                return false;
            }
            Data2DColumn rangeColumn = tmpData.column(parametersOffset());
            String rangeColumnName = rangeColumn.getColumnName();
            double maxValue = Double.NaN, minValue = Double.NaN;
            String sql = "SELECT MAX(" + rangeColumnName + ") AS dmax, MIN("
                    + rangeColumnName + ") AS dmin FROM " + tmpSheet;
            if (task != null) {
                task.setInfo(sql);
            }
            try (ResultSet results = conn.prepareStatement(sql).executeQuery()) {
                if (results.next()) {
                    maxValue = results.getDouble("dmax");
                    minValue = results.getDouble("dmin");
                }
            } catch (Exception e) {
                if (task != null) {
                    task.setError(e.toString());
                }
                MyBoxLog.error(e.toString());
            }
            if (DoubleTools.invalidDouble(maxValue) || DoubleTools.invalidDouble(minValue)
                    || task == null || task.isCancelled()) {
                if (task != null) {
                    task.setError(message("NoData"));
                }
                return false;
            }
            if (task != null) {
                task.setInfo("max: " + maxValue + "   min: " + minValue);
            }
            long maxGroup = Long.MAX_VALUE;
            double interval = groupController.valueSplitInterval();
            if (groupType == GroupType.ValueSplitNumber) {
                int splitNumber = groupController.valueSplitNumber();
                if (splitNumber == 0) {
                    return false;
                }
                interval = (maxValue - minValue) / splitNumber;
                maxGroup = splitNumber;
            }
            double start = minValue, end;
            Data2DColumn rangeSourceColumn = tmpData.parameterSourceColumn();
            int rscale = rangeSourceColumn.needScale() ? groupController.splitScale() : 0;
            boolean isDate = rangeSourceColumn.isTimeType();
            String condition;
            conn.setAutoCommit(false);
            while (start <= maxValue) {
                if (task == null || task.isCancelled()) {
                    return false;
                }
                end = start + interval;
                if (groupid < maxGroup) {
                    groupChanged();
                }
                if (end >= maxValue || groupid >= maxGroup) {
                    String startName = isDate ? DateTools.textEra(Math.round(start))
                            : DoubleTools.scaleString(start, rscale);
                    String endName = isDate ? DateTools.textEra(Math.round(maxValue))
                            : DoubleTools.scaleString(maxValue, rscale);
                    parameterValue = "[" + startName + "," + endName + "]";
                    parameterValueForFilename = startName + "-" + endName;
                    condition = rangeColumnName + " >= " + start;
                    start = maxValue + 1;
                } else {
                    String startName = isDate ? DateTools.textEra(Math.round(start))
                            : DoubleTools.scaleString(start, rscale);
                    String endName = isDate ? DateTools.textEra(Math.round(end))
                            : DoubleTools.scaleString(end, rscale);
                    parameterValue = "[" + startName + "," + endName + ")";
                    parameterValueForFilename = startName + "-" + endName;
                    end = DoubleTools.scale(end, rscale);
                    condition = rangeColumnName + " >= " + start + " AND " + rangeColumnName + " < " + end;
                    start = end;
                }
                recordGroup(groupid, parameterValue);
                sql = "SELECT * FROM " + tmpSheet + " WHERE " + condition
                        + (tmpOrderby != null && !tmpOrderby.isBlank() ? " ORDER BY " + tmpOrderby : "");
                valueQeury(sql);
            }
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
            return false;
        }
    }

    private boolean byValueList() {
        try {
            if (groupName == null || groupName.isBlank() || groupController == null) {
                return false;
            }
            Data2DColumn rangeColumn = tmpData.column(parametersOffset());
            String rangeColumnName = rangeColumn.getColumnName();
            List<ValueRange> splitList = groupController.valueSplitList();
            if (splitList == null || splitList.isEmpty()) {
                return false;
            }
            String condition;
            conn.setAutoCommit(false);
            double start, end;
            Data2DColumn rangeSourceColumn = tmpData.parameterSourceColumn();
            int rscale = rangeSourceColumn.needScale() ? groupController.splitScale() : 0;
            boolean isDate = rangeSourceColumn.isTimeType();
            for (ValueRange range : splitList) {
                if (task == null || task.isCancelled()) {
                    return false;
                }
                try {
                    if (isDate) {
                        start = DateTools.encodeDate((String) range.getStart()).getTime();
                    } else {
                        start = (double) range.getStart();
                    }
                } catch (Exception e) {
                    continue;
                }
                try {
                    if (isDate) {
                        end = DateTools.encodeDate((String) range.getEnd()).getTime();
                    } else {
                        end = (double) range.getEnd();
                    }
                } catch (Exception e) {
                    continue;
                }
                if (start > end) {
                    continue;
                }
                groupChanged();
                condition = rangeColumnName
                        + (range.isIncludeStart() ? " >= " : " > ") + start
                        + " AND " + rangeColumnName
                        + (range.isIncludeEnd() ? " <= " : " < ") + end;
                String startName = isDate ? DateTools.textEra(Math.round(start))
                        : DoubleTools.scaleString(start, rscale);
                String endName = isDate ? DateTools.textEra(Math.round(end))
                        : DoubleTools.scaleString(end, rscale);
                parameterValue = (range.isIncludeStart() ? "[" : "(")
                        + startName + "," + endName
                        + (range.isIncludeEnd() ? "]" : ")");
                parameterValueForFilename = startName + "-" + endName;
                recordGroup(groupid, parameterValue);
                String sql = "SELECT * FROM " + tmpSheet + " WHERE " + condition
                        + (tmpOrderby != null && !tmpOrderby.isBlank() ? " ORDER BY " + tmpOrderby : "");
                valueQeury(sql);
            }
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
            return false;
        }
    }

    private void valueQeury(String sql) {
        if (task != null) {
            task.setInfo(sql);
        }
        try (ResultSet query = conn.prepareStatement(sql).executeQuery()) {
            Data2DRow tmpRow;
            while (query.next()) {
                if (task == null || task.isCancelled()) {
                    query.close();
                    return;
                }
                try {
                    tmpRow = tableTmpData.readData(query);
                    if (++groupCurrentSize <= max || max <= 0) {
                        writeRow(tmpRow);
                    } else {
                        break;
                    }
                } catch (Exception e) {
                    if (task != null) {
                        task.setError(e.toString());
                    } else {
                        MyBoxLog.error(e.toString());
                    }
                }
            }
            if (insert != null) {
                insert.executeBatch();
            }
            conn.commit();
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e.toString());
        }
    }

    private boolean byTime() {
        try {
            if (timeName == null || timeName.isBlank()) {
                return false;
            }
            Data2DColumn timeColumn = tmpData.column(parametersOffset());
            String tmpTimeName = timeColumn.getColumnName();
            String finalOrderBy = tmpTimeName;
            if (tmpOrderby != null && !tmpOrderby.isBlank()) {
                finalOrderBy += "," + tmpOrderby;
            }
            String sql = "SELECT * FROM " + tmpSheet + " ORDER BY " + finalOrderBy;
            if (task != null) {
                task.setInfo(sql);
            }
            try (ResultSet query = conn.prepareStatement(sql).executeQuery()) {
                long timeValue, lastTimeValue = Long.MAX_VALUE;
                boolean groupChanged, isChinese = Languages.isChinese();
                long zeroYear = DateTools.zeroYear();
                Calendar calendar = Calendar.getInstance();
                conn.setAutoCommit(false);
                while (query.next() && task != null && !task.isCancelled()) {
                    if (task == null || task.isCancelled()) {
                        query.close();
                        return false;
                    }
                    try {
                        Data2DRow tmpRow = tableTmpData.readData(query);
                        Object tv = tmpRow.getColumnValue(tmpTimeName);
                        timeValue = tv == null ? null : (long) tv;
                        groupChanged = lastTimeValue == Long.MAX_VALUE || lastTimeValue != timeValue;
                        if (groupChanged) {
                            groupChanged();
                            parameterValueForFilename = idColName + groupid;
                            boolean isBC = timeValue < zeroYear;
                            if (timeType == TimeType.Century) {
                                calendar.setTimeInMillis(timeValue);
                                int v = calendar.get(Calendar.YEAR);
                                if (v % 100 == 0) {
                                    v = v / 100;
                                } else {
                                    v = (v / 100) + 1;
                                }
                                if (isChinese) {
                                    parameterValue = (isBC ? "公元前" : "") + v + "世纪";
                                } else {
                                    parameterValue = v + "th century" + (isBC ? " BC" : "");
                                }
                            } else {
                                String format = "";
                                switch (timeType) {
                                    case Year:
                                        format = "y";
                                        break;
                                    case Month:
                                        format = "y-MM";
                                        break;
                                    case Day:
                                        format = "y-MM-dd";
                                        break;
                                    case Hour:
                                        format = "y-MM-dd HH";
                                        break;
                                    case Minute:
                                        format = "y-MM-dd HH:mm";
                                        break;
                                    case Second:
                                        format = "y-MM-dd HH:mm:ss";
                                        break;
                                }
                                if (isChinese) {
                                    parameterValue = new SimpleDateFormat((isBC ? "G" : "") + format, Languages.LocaleZhCN)
                                            .format(timeValue);
                                } else {
                                    parameterValue = new SimpleDateFormat(format + (isBC ? " G" : ""), Languages.LocaleEn)
                                            .format(timeValue);
                                }
                            }
                            recordGroup(groupid, parameterValue);
                        }
                        if (++groupCurrentSize <= max || max <= 0) {
                            writeRow(tmpRow);
                        }
                        lastTimeValue = timeValue;
                    } catch (Exception e) {
                        if (task != null) {
                            task.setError(e.toString());
                        } else {
                            MyBoxLog.error(e.toString());
                        }
                    }
                }
            } catch (Exception e) {
                if (task != null) {
                    task.setError(e.toString());
                } else {
                    MyBoxLog.error(e.toString());
                }
            }
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
            return false;
        }
    }

    private boolean byExpression() {
        try {
            Data2DColumn expColumn = tmpData.column(parametersOffset());
            String tmpExpName = expColumn.getColumnName();
            String finalOrderBy = tmpExpName;
            if (tmpOrderby != null && !tmpOrderby.isBlank()) {
                finalOrderBy += "," + tmpOrderby;
            }
            String sql = "SELECT * FROM " + tmpSheet + " ORDER BY " + finalOrderBy;
            if (task != null) {
                task.setInfo(sql);
            }
            try (ResultSet query = conn.prepareStatement(sql).executeQuery()) {
                String expValue, lastExpValue = null;
                boolean groupChanged;
                conn.setAutoCommit(false);
                while (query.next() && task != null && !task.isCancelled()) {
                    if (task == null || task.isCancelled()) {
                        query.close();
                        return false;
                    }
                    try {
                        Data2DRow tmpRow = tableTmpData.readData(query);
                        Object tv = tmpRow.getColumnValue(tmpExpName);
                        expValue = tv == null ? null : (String) tv;
                        groupChanged = lastExpValue == null || !lastExpValue.equals(expValue);
                        if (groupChanged) {
                            groupChanged();
                            parameterValueForFilename = idColName + groupid;
                            parameterValue = expValue;
                            recordGroup(groupid, parameterValue);
                        }
                        if (++groupCurrentSize <= max || max <= 0) {
                            writeRow(tmpRow);
                        }
                        lastExpValue = expValue;
                    } catch (Exception e) {
                        if (task != null) {
                            task.setError(e.toString());
                        } else {
                            MyBoxLog.error(e.toString());
                        }
                    }
                }
            } catch (Exception e) {
                if (task != null) {
                    task.setError(e.toString());
                } else {
                    MyBoxLog.error(e.toString());
                }
            }
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
            return false;
        }
    }

    private boolean byRowsInteval() {
        try {
            long total = 0;
            String sql = "SELECT COUNT(*) FROM " + tmpSheet;
            if (task != null) {
                task.setInfo(sql);
            }
            try (ResultSet query = conn.prepareStatement(sql).executeQuery()) {
                if (query.next()) {
                    total = query.getLong(1);
                }
            }
            if (total <= 0) {
                return false;
            }
            long maxGroup = total;
            int splitInterval = groupController.rowsSplitInterval();
            if (groupType == GroupType.RowsSplitNumber) {
                int splitNumber = groupController.rowsSplitNumber();
                if (splitNumber == 0) {
                    return false;
                }
                splitInterval = (int) (total / splitNumber);
                maxGroup = splitNumber;
            }
            conn.setAutoCommit(false);
            sql = "SELECT * FROM " + tmpSheet
                    + (tmpOrderby != null && !tmpOrderby.isBlank() ? " ORDER BY " + tmpOrderby : "");
            if (task != null) {
                task.setInfo(sql);
            }
            long rowIndex = 0, from, to, interval = Math.round(splitInterval);
            try (ResultSet query = conn.prepareStatement(sql).executeQuery()) {
                Data2DRow tmpRow;
                conn.setAutoCommit(false);
                while (query.next() && task != null && !task.isCancelled()) {
                    if (task == null || task.isCancelled()) {
                        query.close();
                        return false;
                    }
                    try {
                        tmpRow = tableTmpData.readData(query);
                        if (rowIndex++ % interval == 0 && groupid < maxGroup) {
                            groupChanged();
                            from = rowIndex;
                            to = from + interval - 1;
                            if (to >= total || groupid >= maxGroup) {
                                to = total;
                            }
                            parameterValue = "[" + from + "," + to + "]";
                            parameterValueForFilename = from + "-" + to;
                            recordGroup(groupid, parameterValue);
                        }
                        if (++groupCurrentSize <= max || max <= 0) {
                            writeRow(tmpRow);
                        }
                    } catch (Exception e) {
                        if (task != null) {
                            task.setError(e.toString());
                        } else {
                            MyBoxLog.error(e.toString());
                        }
                    }
                }
            } catch (Exception e) {
                if (task != null) {
                    task.setError(e.toString());
                } else {
                    MyBoxLog.error(e.toString());
                }
            }
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
            return false;
        }
    }

    private boolean byRowsList() {
        try {
            List<Integer> splitList = groupController.rowsSplitList();
            if (splitList == null || splitList.isEmpty()) {
                return false;
            }
            int from, to;
            String sql;
            for (int i = 0; i < splitList.size();) {
                if (task == null || task.isCancelled()) {
                    return false;
                }
                from = splitList.get(i++);
                to = splitList.get(i++);
                if (from < 0) {
                    from = 0;
                }
                if (from > to) {
                    continue;
                }
                groupChanged();
                parameterValue = "[" + from + "," + to + "]";
                parameterValueForFilename = from + "-" + to;
                recordGroup(groupid, parameterValue);
                sql = "SELECT * FROM " + tmpSheet
                        + (tmpOrderby != null && !tmpOrderby.isBlank() ? " ORDER BY " + tmpOrderby : "")
                        + " OFFSET " + from + " ROWS FETCH NEXT " + (to - from + 1) + " ROWS ONLY";
                if (task != null) {
                    task.setInfo(sql);
                }
                try (ResultSet query = conn.prepareStatement(sql).executeQuery()) {
                    Data2DRow tmpRow;
                    conn.setAutoCommit(false);
                    while (query.next() && task != null && !task.isCancelled()) {
                        if (task == null || task.isCancelled()) {
                            query.close();
                            return false;
                        }
                        try {
                            tmpRow = tableTmpData.readData(query);
                            if (++groupCurrentSize <= max || max <= 0) {
                                writeRow(tmpRow);
                            } else {
                                query.close();
                                break;
                            }
                        } catch (Exception e) {
                            if (task != null) {
                                task.setError(e.toString());
                            } else {
                                MyBoxLog.error(e.toString());
                            }
                        }
                    }
                } catch (Exception e) {
                    if (task != null) {
                        task.setError(e.toString());
                    } else {
                        MyBoxLog.error(e.toString());
                    }
                }
            }
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
            return false;
        }
    }

    private boolean byConditions() {
        try {
            List<DataFilter> conditions = groupController.groupConditions();
            if (conditions == null || conditions.isEmpty()) {
                return false;
            }
            long rowIndex;
            String sql, script;
            for (DataFilter filter : conditions) {
                if (task == null || task.isCancelled()) {
                    return false;
                }
                sql = "SELECT * FROM " + tmpSheet
                        + (tmpOrderby != null && !tmpOrderby.isBlank() ? " ORDER BY " + tmpOrderby : "");
                rowIndex = 0;
                groupChanged();
                long fmax = filter.getMaxPassed();
                parameterValue = filter.getSourceScript()
                        + (filter.isReversed() ? "\n" + message("Reverse") : "")
                        + (fmax > 0 ? "\n" + message("MaximumNumber") + ": " + fmax : "");
                parameterValueForFilename = message("Condition") + groupid;
                recordGroup(groupid, parameterValue);
                fmax = Math.min(max <= 0 ? Long.MAX_VALUE : max,
                        fmax <= 0 ? Long.MAX_VALUE : fmax);
                script = tmpData.tmpScript(filter.getFilledScript());
                if (task == null || task.isCancelled()) {
                    return false;
                }
                filter.setFilledScript(script);
                if (task != null) {
                    task.setInfo(sql);
                }
                try (ResultSet query = conn.prepareStatement(sql).executeQuery()) {
                    Data2DRow tmpRow;
                    conn.setAutoCommit(false);
                    while (query.next() && task != null && !task.isCancelled()) {
                        if (task == null || task.isCancelled()) {
                            query.close();
                            return false;
                        }
                        try {
                            tmpRow = tableTmpData.readData(query);
                            List<String> rowStrings = tmpRow.toStrings(tmpColumns);
                            rowIndex = Long.parseLong(rowStrings.get(1));
                            if (filter.filterDataRow(tmpData, rowStrings, rowIndex)) {
                                if (++groupCurrentSize <= fmax) {
                                    writeRow(tmpRow);
                                } else {
                                    query.close();
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            if (task != null) {
                                task.setError(e.toString());
                            } else {
                                MyBoxLog.error(e.toString());
                            }
                        }
                    }
                } catch (Exception e) {
                    if (task != null) {
                        task.setError(e.toString());
                    } else {
                        MyBoxLog.error(e.toString());
                    }
                }
            }
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
            return false;
        }
    }

    private boolean recordGroup(long index, String value) {
        try {
            Data2DRow group = tableGroupParameters.newRow();
            group.setColumnValue("group_index", index);
            group.setColumnValue("group_parameters", value);
            tableGroupParameters.insertData(conn, group);
            if (task != null) {
                task.setInfo(message("GroupID") + ": " + groupid);
                task.setInfo(message("GroupID") + ": " + parameterValue);
            }
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
            return false;
        }
    }

    private void writeRow(Data2DRow tmpRow) {
        try {
            switch (targetType) {
                case Table:
                case TmpTable:
                    if (targetData == null) {
                        String tableName = null;
                        if (targetType == TargetType.Table) {
                            tableName = DerbyBase.appendIdentifier(originalData.dataName(),
                                    "_" + groupType + "_" + DateTools.nowString3());
                        }
                        targetData = DataTable.createTable(task, conn,
                                tableName, targetColumns, null, null, null, true);
                        targetData.setComments(dataComments);
                        tableTarget = targetData.getTableData2D();
                        finalColumns = targetData.getColumns();
                        insert = conn.prepareStatement(tableTarget.insertStatement());
                        mappedIdColName = targetData.columnName(1);
                        mappedParameterName = targetData.columnName(2);
                        if (task != null) {
                            task.setInfo(message("Table") + ": " + targetData.getSheet());
                        }
                        targetValueOffset++;
                    }
                    Data2DRow data2DRow = tableTarget.newRow();
                    data2DRow.setColumnValue(mappedIdColName, groupid);
                    data2DRow.setColumnValue(mappedParameterName, parameterValue);
                    if (includeRowNumber) {
                        data2DRow.setColumnValue(finalColumns.get(3).getColumnName(),
                                tmpRow.getColumnValue(tmpData.columnName(1)));
                    }
                    for (int i = 0; i < sourcePickIndice.size(); i++) {
                        Object value = tmpRow.getColumnValue(tmpData.columnName(sourcePickIndice.get(i) + tmpValueOffset));
                        Data2DColumn finalColumn = finalColumns.get(i + targetValueOffset);
                        if (finalColumn.needScale() && scale >= 0 && value != null) {
                            value = DoubleTools.scaleString(value + "", invalidAs, scale);
                        }
                        data2DRow.setColumnValue(finalColumn.getColumnName(),
                                finalColumn.fromString(value == null ? null : value + ""));
                    }
                    if (tableTarget.setInsertStatement(conn, insert, data2DRow)) {
                        insert.addBatch();
                        if (++count % Database.BatchSize == 0) {
                            insert.executeBatch();
                            conn.commit();
                            if (task != null) {
                                task.setInfo(message("Inserted") + ": " + count);
                            }
                        }
                    }
                    break;

                case SingleFile:
                case MultipleFiles:
                    if (csvPrinter == null) {
                        targetFile = new DataFileCSV();
                        String fname = originalData.dataName() + "_" + parameterName;
                        if (targetType == TargetType.MultipleFiles) {
                            fname += "_" + parameterValueForFilename;
                        }
                        csvFile = targetFile.tmpFile(fname, "group", "csv");
                        finalColumns = targetFile.fixColumnNames(targetColumns);
                        targetFile.setColumns(finalColumns)
                                .setFile(csvFile).setCharset(Charset.forName("UTF-8"))
                                .setDelimiter(",").setHasHeader(true).setScale(scale)
                                .setColsNumber(finalColumns.size());
                        csvPrinter = CsvTools.csvPrinter(csvFile);
                        csvPrinter.printRecord(targetFile.columnNames());
                    }
                    List<String> fileRow = new ArrayList<>();
                    fileRow.add(groupid + "");
                    fileRow.add(parameterValue);
                    if (includeRowNumber) {
                        Object value = tmpRow.getColumnValue(tmpData.columnName(1));
                        fileRow.add(value == null ? null : value + "");
                    }
                    for (int i = 0; i < sourcePickIndice.size(); i++) {
                        Object value = tmpRow.getColumnValue(tmpData.columnName(sourcePickIndice.get(i) + tmpValueOffset));
                        Data2DColumn finalColumn = finalColumns.get(i + targetValueOffset);
                        if (finalColumn.needScale() && scale >= 0 && value != null) {
                            value = DoubleTools.scaleString(value + "", invalidAs, scale);
                        }
                        fileRow.add(value == null ? null : value + "");
                    }
                    csvPrinter.printRecord(fileRow);
                    break;
            }

        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e.toString());
        }
    }

    private void groupChanged() {
        try {
            groupid++;
            groupCurrentSize = 0;
            if (targetType == TargetType.MultipleFiles) {
                if (csvPrinter != null) {
                    csvPrinter.flush();
                    csvPrinter.close();
                    writeCurrentCSV();
                    csvPrinter = null;
                }
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
        }
    }

    private void writeCurrentCSV() {
        try {
            if (csvFile != null && csvFile.exists()) {
                targetFile.setDataSize(count)
                        .setDataName(originalData.dataName() + "_" + parameterName
                                + (targetType == TargetType.MultipleFiles ? "_" + parameterValueForFilename : ""))
                        .setRowsNumber(count)
                        .setComments(dataComments);
                Data2D.saveAttributes(conn, targetFile, finalColumns);
                csvFiles.add(csvFile);
                if (task != null) {
                    task.setInfo(message("Created") + ": " + csvFile);
                }
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
        }
    }

    private void stopScan() {
        try {
            if (csvPrinter != null) {
                csvPrinter.flush();
                csvPrinter.close();
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
        }
    }

    private boolean finishGroup() {
        try {
            switch (targetType) {
                case Table:
                    if (targetData != null) {
                        insert.executeBatch();
                        conn.commit();
                        insert.close();
                        targetData.setDataSize(count)
                                .setDataName(originalData.dataName() + "_" + parameterName)
                                .setRowsNumber(count).setScale(scale);
                        Data2D.saveAttributes(conn, targetData, targetData.getColumns());
                        if (task != null) {
                            task.setInfo(message("Created") + ": " + message("Table") + "  " + targetData.getSheet());
                        }
                        return true;
                    } else {
                        return false;
                    }

                case SingleFile:
                case MultipleFiles:
                    writeCurrentCSV();
                    return !csvFiles.isEmpty();

            }
            groupOrderby = null;
            if (orders != null && !orders.isEmpty()) {
                List<DataSort> sorts = DataSort.parse(orders);
                if (sorts != null && !sorts.isEmpty()) {
                    List<DataSort> groupsorts = new ArrayList<>();
                    int offset = targetType == TargetType.Table || targetType == TargetType.TmpTable ? 1 : 0;
                    for (DataSort sort : sorts) {
                        String sortName = sort.getName();
                        for (int i = 0; i < targetColumns.size(); i++) {
                            if (sortName.equals(targetColumns.get(i).getColumnName())) {
                                sortName = finalColumns.get(i + offset).getColumnName();
                                break;
                            }
                        }
                        groupsorts.add(new DataSort(sortName, sort.isAscending()));
                    }
                    groupOrderby = DataSort.toString(groupsorts);
                }
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
        }
        return false;
    }

    public String parameterValue(Connection qconn, long index) {
        if (qconn == null || index < 0 || tableGroupParameters == null) {
            return null;
        }
        String sql = "SELECT * from " + tableGroupParameters.getTableName()
                + " WHERE group_index=" + index;
        try (PreparedStatement statement = qconn.prepareStatement(sql)) {
            Data2DRow row = tableGroupParameters.query(qconn, statement);
            Object v = row.getColumnValue("group_parameters");
            return v == null ? null : (String) v;
        } catch (Exception e) {
            return null;
        }
    }

    public long groupsNumber() {
        return tableGroupParameters == null ? 0 : tableGroupParameters.size();
    }

    public List<String> getParameterLabels(Connection qconn, IndexRange range) {
        List<String> values = new ArrayList<>();
        if (qconn == null || tableGroupParameters == null || range == null) {
            return values;
        }
        String sql = "SELECT group_parameters from " + tableGroupParameters.getTableName()
                + " ORDER BY group_index ASC "
                + " OFFSET " + range.getStart() + " ROWS FETCH NEXT " + range.getLength() + " ROWS ONLY ";
        try (PreparedStatement statement = qconn.prepareStatement(sql);
                ResultSet results = statement.executeQuery()) {
            int index = range.getStart() + 1;
            while (results.next()) {
                String p = results.getString("group_parameters");
                if (p != null) {
                    values.add(index++ + "   " + p);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return values;
    }

    // groupid is 1-based
    public List<List<String>> groupData(Connection qconn, long groupid, List<Data2DColumn> columns) {
        if (qconn == null || targetData == null || columns == null) {
            return null;
        }
        List<List<String>> data = new ArrayList<>();
        String sql = "SELECT * FROM " + targetData.getSheet()
                + " WHERE " + targetData.columnName(1) + "=" + groupid
                + (groupOrderby != null && !groupOrderby.isBlank() ? " ORDER BY " + groupOrderby : "");
        if (task != null) {
            task.setInfo(sql);
        }
        try (ResultSet query = qconn.prepareStatement(sql).executeQuery()) {
            while (query.next() && task != null && !task.isCancelled()) {
                if (parameterValue == null) {
                    parameterValue = query.getString(2);
                }
                List<String> row = new ArrayList<>();
                for (Data2DColumn column : columns) {
                    if (qconn == null || qconn.isClosed()) {
                        return null;
                    }
                    String s = column.toString(column.value(query));
                    if (s != null && column.needScale() && scale >= 0) {
                        s = DoubleTools.scaleString(s, invalidAs, scale);
                    }
                    row.add(s);
                }
                data.add(row);
            }
            return data;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    /*
        get
     */
    public DataTable getTargetData() {
        return targetData;
    }

    public DataFileCSV getTargetFile() {
        return targetFile;
    }

    public List<File> getCsvFiles() {
        return csvFiles;
    }

    public String getParameterName() {
        return parameterName;
    }

    public String getIdColName() {
        return idColName;
    }

    public short getScale() {
        return scale;
    }

    /*
        set
     */
    public DataTableGroup setGroupController(ControlData2DGroup groupController) {
        this.groupController = groupController;
        return this;
    }

    public DataTableGroup setOriginalData(Data2D originalData) {
        this.originalData = originalData;
        return this;
    }

    public DataTableGroup setOrders(List<String> sorts) {
        this.orders = sorts;
        return this;
    }

    public DataTableGroup setInvalidAs(InvalidAs invalidAs) {
        this.invalidAs = invalidAs;
        return this;
    }

    public DataTableGroup setMax(long max) {
        this.max = max;
        return this;
    }

    public DataTableGroup setScale(short scale) {
        this.scale = scale;
        return this;
    }

    public DataTableGroup setIncludeRowNumber(boolean includeRowNumber) {
        this.includeRowNumber = includeRowNumber;
        return this;
    }

    public DataTableGroup setTargetType(TargetType targetType) {
        this.targetType = targetType;
        return this;
    }

    public DataTableGroup setSourcePickIndice(List<Integer> sourcePickIndice) {
        this.sourcePickIndice = sourcePickIndice;
        return this;
    }

    public DataTableGroup setTask(FxTask task) {
        this.task = task;
        return this;
    }

}
