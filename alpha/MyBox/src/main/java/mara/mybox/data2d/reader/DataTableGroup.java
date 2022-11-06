package mara.mybox.data2d.reader;

import java.io.File;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mara.mybox.calculation.DescriptiveStatistic.StatisticType;
import mara.mybox.data.FindReplaceString;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.Data2D_Attributes.InvalidAs;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataFilter;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DRow;
import mara.mybox.db.table.TableData2D;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.tools.CsvTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.DoubleTools;
import static mara.mybox.tools.TmpFileTools.getPathTempFile;
import mara.mybox.value.AppPaths;
import static mara.mybox.value.Languages.message;
import org.apache.commons.csv.CSVPrinter;

/**
 * @Author Mara
 * @CreateDate 2022-10-17
 * @License Apache License Version 2.0
 */
public class DataTableGroup {

    protected GroupType type;
    protected Data2D originalData;
    protected DataTable sourceData;
    protected String groupName, orderByString;
    protected List<String> groupNames, orders, targetNames, parameterValues;
    protected InvalidAs invalidAs;
    protected int splitNumber, splitScale;
    protected short scale;
    protected long max;
    protected double splitInterval;
    protected List<DataFilter> conditions;
    protected List<Double> splitList;
    protected TargetType targetType;
    protected SingletonTask task;
    protected boolean ok;

    protected String sourceSheet, idColName, parameterName, parameterValue, parameterValueForFilename;
    protected String mappedIdColName, mappedParameterName;
    protected List<Data2DColumn> sourceColumns;
    protected long count, groupid, groupCurrentSize;

    protected Connection conn;
    protected List<String> targetColNames;
    protected List<Data2DColumn> targetColumns, finalColumns;

    protected DataTable targetData;
    protected TableData2D tableTarget;
    protected PreparedStatement insert;

    protected File csvFile;
    protected CSVPrinter csvPrinter;
    protected DataFileCSV targetFile;
    protected List<File> csvFiles;

    public enum GroupType {
        EqualValues, ValueSplitInterval, ValueSplitNumber, ValueSplitList,
        RowsSplitInterval, RowsSplitNumber, RowsSplitList, Conditions
    }

    public enum TargetType {
        SingleFile, MultipleFiles, Table, TmpTable
    }

    // This class is based on results of "Data2D_Convert.toTmpTable(...)"
    public DataTableGroup(Data2D originalData, DataTable sourceData) {
        this.originalData = originalData;
        this.sourceData = sourceData;
    }

    public boolean run() {
        if (originalData == null || sourceData == null
                || type == null || targetType == null
                || targetNames == null || targetNames.isEmpty()) {
            return false;
        }
        sourceSheet = sourceData.getSheet();
        sourceColumns = sourceData.getColumns();
        if (sourceSheet == null || sourceColumns == null) {
            return false;
        }
        ok = false;
        if (conn == null) {
            try ( Connection dconn = DerbyBase.getConnection()) {
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
            parameterValues = null;
        }
        return ok;
    }

    private boolean scan() {
        try {
            if (type == GroupType.RowsSplitInterval
                    || type == GroupType.RowsSplitNumber
                    || type == GroupType.RowsSplitList) {
                parameterName = message("DataRowNumber");
            } else if (conditions != null) {
                parameterName = message("Condition");
            } else if (groupName != null) {
                parameterName = message("Range") + "_" + groupName;
            } else if (groupNames != null && !groupNames.isEmpty()) {
                parameterName = message("EqualValues");
                for (String name : groupNames) {
                    parameterName += "_" + name;
                }
            } else {
                return false;
            }
            groupid = 0;
            groupCurrentSize = 0;
            count = 0;
            idColName = message("Group");
            parameterValues = new ArrayList<>();
            orderByString = orderByString();
            targetData = null;
            insert = null;
            csvPrinter = null;
            csvFiles = new ArrayList<>();

            targetColNames = new ArrayList<>();
            targetColNames.add(idColName);
            targetColNames.add(parameterName);
            targetColumns = new ArrayList<>();
            targetColumns.add(new Data2DColumn(idColName, ColumnDefinition.ColumnType.Long));
            targetColumns.add(new Data2DColumn(parameterName, ColumnDefinition.ColumnType.String, 200));
            for (String name : targetNames) {
                Data2DColumn c = sourceData.columnByName(sourceData.tmpColumnName(name)).cloneAll();
                c.setD2cid(-1).setD2id(-1).setColumnName(name);
                targetColumns.add(c);
                targetColNames.add(name);
            }

            switch (type) {
                case EqualValues:
                    return byEqualValues();
                case ValueSplitInterval:
                case ValueSplitNumber:
                    return byValueInteval();
                case ValueSplitList:
                    return byValueList();
                case RowsSplitInterval:
                case RowsSplitNumber:
                    return byRowsInteval();
                case RowsSplitList:
                    return byRowsList();
                case Conditions:
                    return byConditions();
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

    private boolean byEqualValues() {
        try {
            if (type != GroupType.EqualValues || groupNames == null || groupNames.isEmpty()) {
                return false;
            }
            String orderBy = null;
            List<String> mappedGroupNames = new ArrayList<>();
            for (String group : groupNames) {
                String name = sourceData.tmpColumnName(group);
                if (orderBy == null) {
                    orderBy = name;
                } else {
                    orderBy += ", " + name;
                }
                mappedGroupNames.add(name);
            }
            if (orders != null && !orders.isEmpty()) {
                int desclen = ("-" + message("Descending")).length();
                int asclen = ("-" + message("Ascending")).length();
                String name, stype;
                for (String order : orders) {
                    if (groupNames.contains(order)) {
                        continue;
                    }
                    if (order.endsWith("-" + message("Descending"))) {
                        name = order.substring(0, order.length() - desclen);
                        stype = " DESC";
                    } else if (order.endsWith("-" + message("Ascending"))) {
                        name = order.substring(0, order.length() - asclen);
                        stype = " ASC";
                    } else {
                        continue;
                    }
                    name = sourceData.tmpColumnName(name);
                    orderBy += ", " + name + stype;
                }
            }
            String sql = "SELECT * FROM " + sourceSheet + " ORDER BY " + orderBy;
            if (task != null) {
                task.setInfo(sql);
            }
            try ( ResultSet query = conn.prepareStatement(sql).executeQuery()) {
                Map<String, Object> sourceRow, lastRow = null;
                Map<String, Object> groupMap = new HashMap<>();
                boolean groupChanged;
                conn.setAutoCommit(false);
                while (query.next() && task != null && !task.isCancelled()) {
                    if (task == null || task.isCancelled()) {
                        query.close();
                        return false;
                    }
                    try {
                        sourceRow = new HashMap<>();
                        for (Data2DColumn column : sourceColumns) {
                            String sourceColName = column.getColumnName();
                            Object v = query.getObject(sourceColName);
                            sourceRow.put(sourceColName, v);
                        }
                        if (lastRow == null) {
                            groupChanged = true;
                        } else {
                            groupChanged = false;
                            for (String group : mappedGroupNames) {
                                Object sv = sourceRow.get(group);
                                Object lv = lastRow.get(group);
                                if (sv == null) {
                                    if (lv != null) {
                                        groupChanged = true;
                                        break;
                                    }
                                } else {
                                    if (!sv.equals(lv)) {
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
                            for (String name : groupNames) {
                                Object v = sourceRow.get(sourceData.tmpColumnName(name));
                                groupMap.put(name, v);
                            }
                            parameterValue = groupMap.toString();
                            parameterValues.add(parameterValue);
                        }
                        if (++groupCurrentSize <= max || max <= 0) {
                            writeRow(sourceRow);
                        }
                        lastRow = sourceRow;
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
            String mappedGroupName = sourceData.tmpColumnName(groupName);

            double maxValue = Double.NaN, minValue = Double.NaN;
            String sql = "SELECT MAX(" + mappedGroupName + ") AS dmax, MIN("
                    + mappedGroupName + ") AS dmin FROM " + sourceSheet;
            if (task != null) {
                task.setInfo(sql);
            }
            try ( ResultSet results = conn.prepareStatement(sql).executeQuery()) {
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
            double interval = splitInterval;
            if (type == GroupType.ValueSplitNumber) {
                if (splitNumber == 0) {
                    return false;
                }
                interval = (maxValue - minValue) / splitNumber;
                maxGroup = splitNumber;
            }
            double from = minValue, to;
            int rscale = sourceData.columnByName(mappedGroupName).needScale() ? splitScale : 0;
            String condition;
            conn.setAutoCommit(false);
            while (from <= maxValue) {
                if (task == null || task.isCancelled()) {
                    return false;
                }
                to = from + interval;
                if (groupid < maxGroup) {
                    groupChanged();
                }
                if (to >= maxValue || groupid >= maxGroup) {
                    String bs = DoubleTools.scaleString(from, rscale);
                    String es = DoubleTools.scaleString(maxValue, rscale);
                    parameterValue = "[" + bs + "," + es + "]";
                    parameterValueForFilename = bs + "-" + es;
                    condition = mappedGroupName + " >= " + from;
                    from = maxValue + 1;
                } else {
                    String bs = DoubleTools.scaleString(from, rscale);
                    String es = DoubleTools.scaleString(to, rscale);
                    parameterValue = "[" + bs + "," + es + ")";
                    parameterValueForFilename = bs + "-" + es;
                    to = Double.valueOf(es);
                    condition = mappedGroupName + " >= " + from + " AND " + mappedGroupName + " < " + to;
                    from = to;
                }
                parameterValues.add(parameterValue);
                sql = "SELECT * FROM " + sourceSheet + " WHERE " + condition + orderByString;
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
            if (groupName == null || groupName.isBlank()
                    || splitList == null || splitList.isEmpty()) {
                return false;
            }
            String mappedGroupName = sourceData.tmpColumnName(groupName);
            String condition;

            conn.setAutoCommit(false);
            double from, to;
            for (int i = 0; i < splitList.size();) {
                if (task == null || task.isCancelled()) {
                    return false;
                }
                groupChanged();
                from = splitList.get(i++);
                to = splitList.get(i++);
                if (from > to) {
                    continue;
                }
                condition = mappedGroupName + " >= " + from + " AND " + mappedGroupName + " <= " + to;
                parameterValue = "[" + from + "," + to + "]";
                parameterValueForFilename = from + "-" + to;
                parameterValues.add(parameterValue);
                String sql = "SELECT * FROM " + sourceSheet + " WHERE " + condition + orderByString;
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
        try ( ResultSet query = conn.prepareStatement(sql).executeQuery()) {
            Map<String, Object> sourceRow;
            while (query.next()) {
                if (task == null || task.isCancelled()) {
                    query.close();
                    return;
                }
                try {
                    sourceRow = new HashMap<>();
                    for (Data2DColumn column : sourceColumns) {
                        String sourceColName = column.getColumnName();
                        Object v = query.getObject(sourceColName);
                        sourceRow.put(sourceColName, v);
                    }
                    if (++groupCurrentSize <= max || max <= 0) {
                        writeRow(sourceRow);
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

    private boolean byRowsInteval() {
        try {
            long total = 0;
            String sql = "SELECT COUNT(*) FROM " + sourceSheet;
            if (task != null) {
                task.setInfo(sql);
            }
            try ( ResultSet query = conn.prepareStatement(sql).executeQuery()) {
                if (query.next()) {
                    total = query.getLong(1);
                }
            }
            if (total <= 0) {
                return false;
            }
            long maxGroup = total;
            if (type == GroupType.RowsSplitNumber) {
                if (splitNumber == 0) {
                    return false;
                }
                splitInterval = total / splitNumber;
                maxGroup = splitNumber;
            }
            conn.setAutoCommit(false);
            sql = "SELECT * FROM " + sourceSheet + orderByString;
            if (task != null) {
                task.setInfo(sql);
            }
            long rowIndex = 0, from, to, interval = Math.round(splitInterval);
            try ( ResultSet query = conn.prepareStatement(sql).executeQuery()) {
                Map<String, Object> sourceRow;
                conn.setAutoCommit(false);
                while (query.next() && task != null && !task.isCancelled()) {
                    if (task == null || task.isCancelled()) {
                        query.close();
                        return false;
                    }
                    try {
                        sourceRow = new HashMap<>();
                        for (Data2DColumn column : sourceColumns) {
                            String sourceColName = column.getColumnName();
                            Object v = query.getObject(sourceColName);
                            sourceRow.put(sourceColName, v);
                        }
                        if (rowIndex++ % interval == 0 && groupid < maxGroup) {
                            groupChanged();
                            from = rowIndex;
                            to = from + interval - 1;
                            if (to >= total || groupid >= maxGroup) {
                                to = total;
                            }
                            parameterValue = "[" + from + "," + to + "]";
                            parameterValueForFilename = from + "-" + to;
                            parameterValues.add(parameterValue);
                        }
                        if (++groupCurrentSize <= max || max <= 0) {
                            writeRow(sourceRow);
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
            if (splitList == null || splitList.isEmpty()) {
                return false;
            }
            long from, to;
            String sql;
            for (int i = 0; i < splitList.size();) {
                if (task == null || task.isCancelled()) {
                    return false;
                }
                from = Math.round(splitList.get(i++));
                to = Math.round(splitList.get(i++));
                if (from < 0) {
                    from = 0;
                }
                if (from > to) {
                    continue;
                }
                groupChanged();
                parameterValue = "[" + from + "," + to + "]";
                parameterValueForFilename = from + "-" + to;
                parameterValues.add(parameterValue);
                sql = "SELECT * FROM " + sourceSheet + orderByString
                        + " OFFSET " + from + " ROWS FETCH NEXT " + (to - from + 1) + " ROWS ONLY";
                if (task != null) {
                    task.setInfo(sql);
                }
                try ( ResultSet query = conn.prepareStatement(sql).executeQuery()) {
                    Map<String, Object> sourceRow;
                    conn.setAutoCommit(false);
                    while (query.next() && task != null && !task.isCancelled()) {
                        if (task == null || task.isCancelled()) {
                            query.close();
                            return false;
                        }
                        try {
                            sourceRow = new HashMap<>();
                            for (Data2DColumn column : sourceColumns) {
                                String sourceColName = column.getColumnName();
                                Object v = query.getObject(sourceColName);
                                sourceRow.put(sourceColName, v);
                            }
                            if (++groupCurrentSize <= max || max <= 0) {
                                writeRow(sourceRow);
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
            if (conditions == null || conditions.isEmpty()) {
                return false;
            }
            FindReplaceString findReplace = FindReplaceString.create().
                    setOperation(FindReplaceString.Operation.ReplaceAll)
                    .setIsRegex(false).setCaseInsensitive(false).setMultiline(false);
            long rowIndex;
            String sql;
            for (DataFilter filter : conditions) {
                if (task == null || task.isCancelled()) {
                    return false;
                }
                sql = "SELECT * FROM " + sourceSheet + orderByString;
                rowIndex = 0;
                groupChanged();
                long fmax = filter.getMaxPassed();
                parameterValue = filter.getSourceScript()
                        + (filter.isReversed() ? "\n" + message("Reverse") : "")
                        + (fmax > 0 ? "\n" + message("MaximumNumber") + ": " + fmax : "");
                parameterValueForFilename = message("Condition") + groupid;
                parameterValues.add(parameterValue);
                fmax = Math.min(max <= 0 ? Long.MAX_VALUE : max,
                        fmax <= 0 ? Long.MAX_VALUE : fmax);
                String script = filter.getFilledScript();
                if (script != null && !script.isBlank()) {
                    for (Data2DColumn column : sourceData.sourceColumns()) {
                        String sourceName = column.getColumnName();
                        String tmpName = sourceData.tmpColumnName(sourceName);
                        script = findReplace.replace(script, "#{" + sourceName + "}", "#{" + tmpName + "}");
                        for (StatisticType stype : StatisticType.values()) {
                            script = findReplace.replace(script, "#{" + sourceName + "-" + message(stype.name()) + "}",
                                    "#{" + tmpName + "-" + message(stype.name()) + "}");
                        }
                        filter.setFilledScript(script);
                    }
                }
                if (task != null) {
                    task.setInfo(sql);
                }
                try ( ResultSet query = conn.prepareStatement(sql).executeQuery()) {
                    Map<String, Object> sourceRow;
                    conn.setAutoCommit(false);
                    while (query.next() && task != null && !task.isCancelled()) {
                        if (task == null || task.isCancelled()) {
                            query.close();
                            return false;
                        }
                        try {
                            sourceRow = new HashMap<>();
                            List<String> rowStrings = new ArrayList<>();
                            for (Data2DColumn column : sourceColumns) {
                                String sourceColName = column.getColumnName();
                                Object v = query.getObject(sourceColName);
                                sourceRow.put(sourceColName, v);
                                rowStrings.add(column.toString(v));
                            }
                            rowIndex++;
                            if (filter.filterDataRow(sourceData, rowStrings, rowIndex)) {
                                if (++groupCurrentSize <= fmax) {
                                    writeRow(sourceRow);
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

    private void writeRow(Map<String, Object> sourceRow) {
        try {
            switch (targetType) {
                case Table:
                case TmpTable:
                    if (targetData == null) {
                        String tableName = null;
                        if (targetType == TargetType.Table) {
                            tableName = DerbyBase.fixedIdentifier(originalData.dataName()
                                    + "_" + type + "_" + DateTools.nowString3());
                        }
                        targetData = DataTable.createTable(task, conn,
                                tableName, targetColumns, null, null, null, true);
                        tableTarget = targetData.getTableData2D();
                        finalColumns = targetData.getColumns();
                        insert = conn.prepareStatement(tableTarget.insertStatement());
                        mappedIdColName = targetData.tmpColumnName(idColName);
                        mappedParameterName = targetData.tmpColumnName(parameterName);
                        if (task != null) {
                            task.setInfo(message("Table") + ": " + targetData.getSheet());
                        }
                    }
                    Data2DRow data2DRow = tableTarget.newRow();
                    data2DRow.setColumnValue(mappedIdColName, groupid);
                    data2DRow.setColumnValue(mappedParameterName, parameterValue);
                    for (int i = 2; i < targetColumns.size(); i++) {
                        String name = targetColumns.get(i).getColumnName();
                        data2DRow.setColumnValue(finalColumns.get(i + 1).getColumnName(),
                                sourceRow.get(sourceData.tmpColumnName(name)));
                    }
                    if (tableTarget.setInsertStatement(conn, insert, data2DRow)) {
                        insert.addBatch();
                        if (++count % DerbyBase.BatchSize == 0) {
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
                        csvFile = getPathTempFile(AppPaths.getGeneratedPath(), fname, ".csv");
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
                    for (int i = 2; i < targetColumns.size(); i++) {
                        Data2DColumn column = targetColumns.get(i);
                        String name = column.getColumnName();
                        Object v = sourceRow.get(sourceData.tmpColumnName(name));
                        String s = column.toString(v);
                        if (column.needScale() && scale >= 0) {
                            s = DoubleTools.scaleString(s, invalidAs, scale);
                        }
                        fileRow.add(s);
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
            if (task != null) {
                if (parameterValue != null) {
                    task.setInfo(message("Group") + ": " + parameterValue);
                } else {
                    task.setInfo(message("Group") + ": " + groupid);
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
                        .setRowsNumber(count);
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
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
        }
        return false;
    }

    public String orderByString() {
        String orderBy = null;
        if (orders != null && !orders.isEmpty()) {
            int desclen = ("-" + message("Descending")).length();
            int asclen = ("-" + message("Ascending")).length();
            String name, stype;
            for (String order : orders) {
                if (order.endsWith("-" + message("Descending"))) {
                    name = order.substring(0, order.length() - desclen);
                    stype = " DESC";
                } else if (order.endsWith("-" + message("Ascending"))) {
                    name = order.substring(0, order.length() - asclen);
                    stype = " ASC";
                } else {
                    continue;
                }
                name = sourceData.tmpColumnName(name);
                if (orderBy == null) {
                    orderBy = name + stype;
                } else {
                    orderBy += ", " + name + stype;
                }
            }
        }
        return orderBy == null ? "" : " ORDER BY " + orderBy;
    }

    public String parameterValue(int index) {
        try {
            return parameterValues.get(index);
        } catch (Exception e) {
            return null;
        }
    }

    // groupid is 1-based
    public List<List<String>> groupData(Connection qconn, long groupid, List<Data2DColumn> columns) {
        if (qconn == null || targetData == null || columns == null) {
            return null;
        }
        List<List<String>> data = new ArrayList<>();
        String sql = "SELECT * FROM " + targetData.getSheet()
                + " WHERE " + idColName + "=" + groupid + orderByString;
        try ( ResultSet query = qconn.prepareStatement(sql).executeQuery()) {
            while (query.next()) {
                if (parameterValue == null) {
                    parameterValue = query.getString(2);
                }
                List<String> row = new ArrayList<>();
                for (Data2DColumn column : columns) {
                    if (qconn == null || qconn.isClosed()) {
                        return null;
                    }
                    String name = column.getColumnName();
                    String gname = targetData.tmpColumnName(name);
                    String s = column.toString(query.getObject(gname));
                    if (s != null && column.needScale() && scale >= 0) {
                        s = DoubleTools.scaleString(s, invalidAs, scale);
                    }
                    row.add(s);
                }
                data.add(row);
            }
            return data;
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
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

    public long groupsNumber() {
        return parameterValues == null ? 0 : parameterValues.size();
    }

    public String getParameterName() {
        return parameterName;
    }

    public List<String> getParameterValues() {
        return parameterValues;
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
    public DataTableGroup setOriginalData(Data2D originalData) {
        this.originalData = originalData;
        return this;
    }

    public DataTableGroup setType(GroupType type) {
        this.type = type;
        return this;
    }

    public DataTableGroup setSourceTable(DataTable sourceTable) {
        this.sourceData = sourceTable;
        return this;
    }

    public DataTableGroup setGroupNames(List<String> groupNames) {
        this.groupNames = groupNames;
        return this;
    }

    public DataTableGroup setGroupName(String groupName) {
        this.groupName = groupName;
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

    public DataTableGroup setSplitScale(int splitScale) {
        this.splitScale = splitScale;
        return this;
    }

    public DataTableGroup setSplitNumber(int splitNumber) {
        this.splitNumber = splitNumber;
        return this;
    }

    public DataTableGroup setSplitInterval(double splitInterval) {
        this.splitInterval = splitInterval;
        return this;
    }

    public DataTableGroup setSplitList(List<Double> splitList) {
        this.splitList = splitList;
        return this;
    }

    public DataTableGroup setConditions(List<DataFilter> conditions) {
        this.conditions = conditions;
        return this;
    }

    public DataTableGroup setTargetType(TargetType targetType) {
        this.targetType = targetType;
        return this;
    }

    public DataTableGroup setTargetNames(List<String> targetNames) {
        this.targetNames = targetNames;
        return this;
    }

    public DataTableGroup setTask(SingletonTask task) {
        this.task = task;
        return this;
    }

}
