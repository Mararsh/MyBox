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
    protected DataTable sourceData; // Results of "Data2D_Convert.toTmpTable(...)"
    protected String groupName;
    protected List<String> groupNames, copyNames, sorts, targetNames;
    protected InvalidAs invalidAs;
    protected int max, scale, splitNumber;
    protected double splitInterval;
    protected List<DataFilter> conditions;
    protected List<Double> splitList;
    protected TargetType targetType;
    protected SingletonTask task;
    protected boolean ok;

    protected String sourceSheet, groupColName, currentGroupName, currentGroupValue;
    protected List<Data2DColumn> sourceColumns;
    protected long count, groupid, groupCurrentSize;

    protected Connection conn;
    protected List<String> targetColNames;
    protected List<Data2DColumn> targetColumns;

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
        Table, SingleFile, MultipleFiles
    }

    public DataTableGroup(DataTable sourceTable) {
        this.sourceData = sourceTable;
        sourceSheet = sourceTable.getSheet();
        sourceColumns = sourceTable.getColumns();
    }

    public boolean run() {
        if (sourceData == null || sourceColumns == null
                || type == null || targetType == null
                || targetNames == null || targetNames.isEmpty()) {
            return false;
        }
        ok = false;
        if (conn == null) {
            try ( Connection dconn = DerbyBase.getConnection()) {
                conn = dconn;
                ok = scan();
                stopScan();
                if (ok) {
                    finishGroup();
                }
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
        return ok;
    }

    private boolean scan() {
        try {
            if (type == GroupType.RowsSplitInterval
                    || type == GroupType.RowsSplitNumber
                    || type == GroupType.RowsSplitList) {
                groupColName = message("DataRowNumber");
            } else if (groupName != null) {
                groupColName = groupName + "_" + message("Range");
            } else {
                groupColName = message("Group");
            }
            groupid = 0;
            groupCurrentSize = 0;
            count = 0;
            targetData = null;
            insert = null;
            csvPrinter = null;
            csvFiles = new ArrayList<>();

            targetColNames = new ArrayList<>();
            targetColNames.add(groupColName);
            targetColumns = new ArrayList<>();
            targetColumns.add(new Data2DColumn(groupColName, ColumnDefinition.ColumnType.String));
            for (String name : targetNames) {
                Data2DColumn c = sourceData.columnByName(sourceData.mappedColumnName(name));
                targetColumns.add(c.cloneAll().setD2cid(-1).setD2id(-1));
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
                String name = sourceData.mappedColumnName(group);
                if (orderBy == null) {
                    orderBy = name;
                } else {
                    orderBy += ", " + name;
                }
                mappedGroupNames.add(name);
            }
            if (sorts != null && !sorts.isEmpty()) {
                int desclen = ("-" + message("Descending")).length();
                int asclen = ("-" + message("Ascending")).length();
                String name, stype;
                for (String sort : sorts) {
                    if (groupNames.contains(sort)) {
                        continue;
                    }
                    if (sort.endsWith("-" + message("Descending"))) {
                        name = sort.substring(0, sort.length() - desclen);
                        stype = " DESC";
                    } else if (sort.endsWith("-" + message("Ascending"))) {
                        name = sort.substring(0, sort.length() - asclen);
                        stype = " ASC";
                    } else {
                        continue;
                    }
                    name = sourceData.mappedColumnName(name);
                    orderBy += ", " + name + stype;
                }
            }
            String sql = "SELECT * FROM " + sourceSheet + " ORDER BY " + orderBy;
            try ( ResultSet query = conn.prepareStatement(sql).executeQuery()) {
                Map<String, Object> sourceRow, lastRow = null;
                boolean groupChanged;
                currentGroupName = groupColName + groupid;
                currentGroupValue = currentGroupName;
                conn.setAutoCommit(false);
                while (query.next() && task != null && !task.isCancelled()) {
                    if (task == null || task.isCancelled()) {
                        query.close();
                        stopScan();
                        return false;
                    }
                    try {
                        sourceRow = new HashMap<>();
                        for (Data2DColumn column : sourceColumns) {
                            String sourceColName = column.getColumnName();
                            Object v = query.getObject(sourceColName);
                            sourceRow.put(sourceColName, v);
                        }
                        groupChanged = false;
                        if (lastRow != null) {
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
                            currentGroupName = groupColName + groupid;
                            currentGroupValue = currentGroupName;
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
            stopScan();
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
            String mappedGroupName = sourceData.mappedColumnName(groupName);
            double maxValue = Double.NaN, minValue = Double.NaN;
            String sql = "SELECT MAX(" + mappedGroupName + ") AS dmax, MIN("
                    + mappedGroupName + ") AS dmin FROM " + sourceSheet;
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
                stopScan();
                return false;
            }
            if (type == GroupType.ValueSplitNumber) {
                if (splitNumber == 0) {
                    stopScan();
                    return false;
                }
                splitInterval = (maxValue - minValue) / splitNumber;
            }
            double from = minValue, to;
            String condition;
            String orderBy = valueOrderBy();
            conn.setAutoCommit(false);
            while (from <= maxValue) {
                if (task == null || task.isCancelled()) {
                    stopScan();
                    return false;
                }
                groupChanged();
                to = from + splitInterval;
                if (to >= maxValue) {
                    condition = mappedGroupName + " >= " + from;
                    String bs = DoubleTools.scaleString(from, scale);
                    String es = DoubleTools.scaleString(maxValue, scale);
                    currentGroupValue = message("Group") + groupid + ": [" + bs + "," + es + "]";
                    currentGroupName = bs + "-" + es;
                    from = maxValue + 1;
                } else {
                    condition = mappedGroupName + " >= " + from + " AND " + mappedGroupName + " < " + to;
                    String bs = DoubleTools.scaleString(from, scale);
                    String es = DoubleTools.scaleString(to, scale);
                    currentGroupValue = message("Group") + groupid + ": [" + bs + "," + es + ")";
                    currentGroupName = bs + "-" + es;
                    from = to;
                }
                sql = "SELECT * FROM " + sourceSheet + " WHERE " + condition + orderBy;
                valueQeury(sql);
            }
            stopScan();
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
            String mappedGroupName = sourceData.mappedColumnName(groupName);
            String condition;

            conn.setAutoCommit(false);
            double from, to;
            String orderBy = valueOrderBy();
            for (int i = 0; i < splitList.size();) {
                if (task == null || task.isCancelled()) {
                    stopScan();
                    return false;
                }
                groupChanged();
                from = splitList.get(i++);
                to = splitList.get(i++);
                condition = mappedGroupName + " >= " + from + " AND " + mappedGroupName + " <= " + to;
                String bs = DoubleTools.scaleString(from, scale);
                String es = DoubleTools.scaleString(to, scale);
                currentGroupValue = message("Group") + groupid + ": [" + bs + "," + es + "]";
                currentGroupName = bs + "-" + es;
                String sql = "SELECT * FROM " + sourceSheet + " WHERE " + condition + orderBy;
                valueQeury(sql);
            }
            stopScan();
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
        try ( ResultSet query = conn.prepareStatement(sql).executeQuery()) {
            Map<String, Object> sourceRow;
            while (query.next()) {
                if (task == null || task.isCancelled()) {
                    query.close();
                    stopScan();
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
            conn.commit();
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e.toString());
        }
    }

    private String valueOrderBy() {
        String orderBy = null;
        if (sorts != null && !sorts.isEmpty()) {
            int desclen = ("-" + message("Descending")).length();
            int asclen = ("-" + message("Ascending")).length();
            String name, stype;
            for (String sort : sorts) {
                if (sort.endsWith("-" + message("Descending"))) {
                    name = sort.substring(0, sort.length() - desclen);
                    stype = " DESC";
                } else if (sort.endsWith("-" + message("Ascending"))) {
                    name = sort.substring(0, sort.length() - asclen);
                    stype = " ASC";
                } else {
                    continue;
                }
                name = sourceData.mappedColumnName(name);
                if (orderBy == null) {
                    orderBy = name + stype;
                } else {
                    orderBy += ", " + name + stype;
                }
            }
        }
        return orderBy == null ? "" : " ORDER BY " + orderBy;
    }

    private boolean byRowsInteval() {
        try {
            long total = 0;
            String sql = "SELECT COUNT(*) FROM " + sourceSheet;
            try ( ResultSet query = conn.prepareStatement(sql).executeQuery()) {
                if (query.next()) {
                    total = query.getLong(1);
                }
            }
            if (total <= 0) {
                return false;
            }
            if (type == GroupType.RowsSplitNumber) {
                if (splitNumber == 0) {
                    stopScan();
                    return false;
                }
                splitInterval = total / splitNumber;
            }
            conn.setAutoCommit(false);
            String orderBy = valueOrderBy();
            sql = "SELECT * FROM " + sourceSheet + orderBy;
            long rowIndex = 0, from, to, interval = Math.round(splitInterval);
            try ( ResultSet query = conn.prepareStatement(sql).executeQuery()) {
                Map<String, Object> sourceRow;
                currentGroupName = groupColName + groupid;
                currentGroupValue = currentGroupName;
                conn.setAutoCommit(false);
                while (query.next() && task != null && !task.isCancelled()) {
                    if (task == null || task.isCancelled()) {
                        query.close();
                        stopScan();
                        return false;
                    }
                    try {
                        sourceRow = new HashMap<>();
                        for (Data2DColumn column : sourceColumns) {
                            String sourceColName = column.getColumnName();
                            Object v = query.getObject(sourceColName);
                            sourceRow.put(sourceColName, v);
                        }
                        if (rowIndex++ % interval == 0) {
                            groupChanged();
                            from = rowIndex;
                            to = from + interval - 1;
                            if (to >= total) {
                                to = total;
                            }
                            currentGroupValue = message("Group") + groupid + ": [" + from + "," + to + "]";
                            currentGroupName = from + "-" + to;
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
            stopScan();
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
                    if (targetData == null) {
                        targetData = DataTable.createTable(task, conn, targetColumns);
                        for (String name : sourceData.getColumnsMap().keySet()) {
                            targetData.getColumnsMap().put(name,
                                    targetData.getColumnsMap().get(sourceData.getColumnsMap().get(name)));
                        }
                        tableTarget = targetData.getTableData2D();
                        insert = conn.prepareStatement(tableTarget.insertStatement());
                    }
                    Data2DRow data2DRow = tableTarget.newRow();
                    data2DRow.setColumnValue(groupColName, currentGroupValue);
                    for (int i = 1; i < targetColNames.size(); i++) {
                        String name = targetColNames.get(i);
                        data2DRow.setColumnValue(name, sourceRow.get(sourceData.mappedColumnName(name)));
                    }
                    tableTarget.insertData(conn, insert, data2DRow);
                    if (++count % DerbyBase.BatchSize == 0) {
                        conn.commit();
                    }
                    break;

                case SingleFile:
                case MultipleFiles:
                    if (csvPrinter == null) {
                        String fname = targetType == TargetType.SingleFile
                                ? message("GroupBy") : currentGroupName;
                        csvFile = getPathTempFile(AppPaths.getGeneratedPath(),
                                sourceData.dataName() + "_" + fname, ".csv");
                        csvPrinter = CsvTools.csvPrinter(csvFile);
                        csvPrinter.printRecord(targetColNames);
                    }
                    List<String> fileRow = new ArrayList<>();
                    fileRow.add(currentGroupValue);
                    for (int i = 1; i < targetColNames.size(); i++) {
                        Data2DColumn column = targetColumns.get(i);
                        String name = column.getColumnName();
                        Object v = sourceRow.get(sourceData.mappedColumnName(name));
                        String s = column.toString(v);
                        if (column.needScale()) {
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
                targetFile = new DataFileCSV();
                targetFile.setColumns(targetColumns).setDataSize(count)
                        .setDataName(originalData.dataName() + "_" + currentGroupValue)
                        .setFile(csvFile).setCharset(Charset.forName("UTF-8"))
                        .setDelimiter(",").setHasHeader(true)
                        .setColsNumber(targetColumns.size())
                        .setRowsNumber(count);
                Data2D.saveAttributes(conn, targetFile, targetColumns);
                csvFiles.add(csvFile);
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
            if (conn != null) {
                conn.commit();
            }
            if (insert != null) {
                insert.close();
            }
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

    private void finishGroup() {
        try {
            switch (targetType) {
                case Table:
                    if (targetData != null) {
                        targetData.setColumns(targetColumns).setDataSize(count)
                                .setDataName(originalData.dataName() + "_" + message("GroupBy"))
                                .setColsNumber(targetColumns.size())
                                .setRowsNumber(count);
                        Data2D.saveAttributes(conn, targetData, targetColumns);
                    }
                    break;

                case SingleFile:
                case MultipleFiles:
                    writeCurrentCSV();
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

    public long groupNumber() {
        return groupid;
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

    public DataTableGroup setCopyNames(List<String> copyNames) {
        this.copyNames = copyNames;
        return this;
    }

    public DataTableGroup setSorts(List<String> sorts) {
        this.sorts = sorts;
        return this;
    }

    public DataTableGroup setInvalidAs(InvalidAs invalidAs) {
        this.invalidAs = invalidAs;
        return this;
    }

    public DataTableGroup setMax(int max) {
        this.max = max;
        return this;
    }

    public DataTableGroup setScale(int scale) {
        this.scale = scale;
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
