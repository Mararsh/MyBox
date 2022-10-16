package mara.mybox.data2d;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mara.mybox.data.FindReplaceString;
import mara.mybox.data2d.Data2D_Attributes.InvalidAs;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DRow;
import mara.mybox.db.table.TableData2D;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.tools.DoubleTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-10-16
 * @License Apache License Version 2.0
 */
public class DataTableGroupStatistic {

    // Based on results of "Data2D_Convert.toTmpTable(...)"
    public static DataFileCSV groupStatisticByValues(DataTable sourceTable,
            String dname, SingletonTask task,
            List<String> colNames, List<String> calculations, List<String> sorts,
            int max, int dscale, InvalidAs invalidAs) {
        if (sourceTable == null || sourceTable.sourceColumns == null
                || colNames == null || colNames.isEmpty()) {
            return null;
        }
        try {
            Map<String, String> maps = new HashMap<>();
            String groupBy = null;
            for (String group : colNames) {
                String name = sourceTable.mappedColumnName(group);
                if (groupBy == null) {
                    groupBy = name;
                } else {
                    groupBy += ", " + name;
                }
                maps.put(group, name);
            }
            String countName = message("Count");
            for (String name : sourceTable.columnNames()) {
                if (countName.equals(name)) {
                    countName += "_";
                }
            }
            maps.put(message("Count"), countName);
            String selections = "COUNT(*) AS " + countName;
            String sourceName, mappedName, resultName, selectItem;
            if (calculations != null) {
                for (String calculation : calculations) {
                    if (calculation.endsWith("-" + message("Mean"))) {
                        sourceName = calculation.substring(0, calculation.length() - ("-" + message("Mean")).length());
                        mappedName = sourceTable.mappedColumnName(sourceName);
                        resultName = mappedName + "_" + message("Mean");
                        selectItem = (sourceTable.columnByName(mappedName).isNumberType() ? "AVG(" + mappedName + ")" : "'N/A'")
                                + " AS " + resultName;
                    } else if (calculation.endsWith("-" + message("Summation"))) {
                        sourceName = calculation.substring(0, calculation.length() - ("-" + message("Summation")).length());
                        mappedName = sourceTable.mappedColumnName(sourceName);
                        resultName = mappedName + "_" + message("Summation");
                        selectItem = (sourceTable.columnByName(mappedName).isNumberType() ? "SUM(" + mappedName + ")" : "'N/A'")
                                + " AS " + resultName;
                    } else if (calculation.endsWith("-" + message("Maximum"))) {
                        sourceName = calculation.substring(0, calculation.length() - ("-" + message("Maximum")).length());
                        mappedName = sourceTable.mappedColumnName(sourceName);
                        resultName = mappedName + "_" + message("Maximum");
                        selectItem = (sourceTable.columnByName(mappedName).isNumberType() ? "MAX(" + mappedName + ")" : "'N/A'")
                                + " AS " + resultName;
                    } else if (calculation.endsWith("-" + message("Minimum"))) {
                        sourceName = calculation.substring(0, calculation.length() - ("-" + message("Minimum")).length());
                        mappedName = sourceTable.mappedColumnName(sourceName);
                        resultName = mappedName + "_" + message("Minimum");
                        selectItem = (sourceTable.columnByName(mappedName).isNumberType() ? "MIN(" + mappedName + ")" : "'N/A'")
                                + " AS " + resultName;
                    } else if (calculation.endsWith("-" + message("PopulationVariance"))) {
                        sourceName = calculation.substring(0, calculation.length() - ("-" + message("PopulationVariance")).length());
                        mappedName = sourceTable.mappedColumnName(sourceName);
                        resultName = mappedName + "_" + message("PopulationVariance").replaceAll(" ", "");
                        selectItem = (sourceTable.columnByName(mappedName).isNumberType() ? "VAR_POP(" + mappedName + ")" : "'N/A'")
                                + " AS " + resultName;
                    } else if (calculation.endsWith("-" + message("SampleVariance"))) {
                        sourceName = calculation.substring(0, calculation.length() - ("-" + message("SampleVariance")).length());
                        mappedName = sourceTable.mappedColumnName(sourceName);
                        resultName = mappedName + "_" + message("SampleVariance").replaceAll(" ", "");
                        selectItem = (sourceTable.columnByName(mappedName).isNumberType() ? "VAR_SAMP(" + mappedName + ")" : "'N/A'")
                                + " AS " + resultName;
                    } else if (calculation.endsWith("-" + message("PopulationStandardDeviation"))) {
                        sourceName = calculation.substring(0, calculation.length() - ("-" + message("PopulationStandardDeviation")).length());
                        mappedName = sourceTable.mappedColumnName(sourceName);
                        resultName = mappedName + "_" + message("PopulationStandardDeviation").replaceAll(" ", "");
                        selectItem = (sourceTable.columnByName(mappedName).isNumberType() ? "STDDEV_POP(" + mappedName + ")" : "'N/A'")
                                + " AS " + resultName;
                    } else if (calculation.endsWith("-" + message("SampleStandardDeviation"))) {
                        sourceName = calculation.substring(0, calculation.length() - ("-" + message("SampleStandardDeviation")).length());
                        mappedName = sourceTable.mappedColumnName(sourceName);
                        resultName = mappedName + "_" + message("SampleStandardDeviation").replaceAll(" ", "");
                        selectItem = (sourceTable.columnByName(mappedName).isNumberType() ? "STDDEV_SAMP(" + mappedName + ")" : "'N/A'")
                                + " AS " + resultName;
                    } else {
                        sourceName = calculation;
                        mappedName = sourceTable.mappedColumnName(sourceName);
                        resultName = mappedName;
                        selectItem = resultName;
                    }
                    maps.put(calculation, resultName);
                    selections += ", " + selectItem;
                }
            }
            selections = groupBy + ", " + selections;
            String sql = "SELECT " + selections + " FROM " + sourceTable.getSheet() + " GROUP BY " + groupBy;
            if (sorts != null && !sorts.isEmpty()) {
                String sortString = "";
                int desclen = ("-" + message("Descending")).length();
                int asclen = ("-" + message("Ascending")).length();
                String stype;
                for (String sort : sorts) {
                    if (sort.endsWith("-" + message("Descending"))) {
                        sourceName = sort.substring(0, sort.length() - desclen);
                        stype = " DESC";
                    } else if (sort.endsWith("-" + message("Ascending"))) {
                        sourceName = sort.substring(0, sort.length() - asclen);
                        stype = " ASC";
                    } else {
                        continue;
                    }
                    resultName = maps.get(sourceName);
                    if (resultName != null) {
                        sortString += ", " + resultName + stype;
                    }
                }
                if (!sortString.isBlank()) {
                    sql += " ORDER BY " + sortString.substring(2);
                }
            }
            if (max > 0) {
                sql += " FETCH FIRST " + max + " ROWS ONLY";
            }
//            MyBoxLog.console(sql);
            DataFileCSV results = sourceTable.query(dname, task, sql, message("Group"), dscale, invalidAs);
            if (results == null) {
                return null;
            }
            results.saveAttributes();
            return results;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
            return null;
        }

    }

    public static DataFileCSV groupStatisticByValueRange(DataTable sourceTable, String dname, SingletonTask task,
            boolean isInterval, String colName, double splitParamter, List<String> calculations, List<String> sorts,
            int max, int dscale, InvalidAs invalidAs) {
        if (sourceTable == null || sourceTable.sourceColumns == null
                || colName == null || colName.isEmpty() || (!isInterval && splitParamter <= 0)) {
            return null;
        }
        List<Data2DColumn> tmpColumns = new ArrayList<>();
        String rangeName = colName + "_" + message("Range");
        tmpColumns.add(new Data2DColumn(rangeName, ColumnDefinition.ColumnType.String));
        tmpColumns.addAll(sourceTable.columns.subList(1, sourceTable.columns.size()));
        DataTable rangeData;
        long count = 0;
        try ( Connection conn = DerbyBase.getConnection();
                 Statement query = conn.createStatement()) {
            rangeData = DataTable.createTable(task, conn, tmpColumns);
            rangeName = rangeData.mappedColumnName(rangeName);
            TableData2D rangeTable = rangeData.getTableData2D();
            double maxValue = Double.NaN, minValue = Double.NaN;
            String sql = "SELECT MAX(" + colName + ") AS dmax, MIN(" + colName + ") AS dmin FROM " + sourceTable.getSheet();
            try ( ResultSet results = query.executeQuery(sql)) {
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
                query.close();
                conn.close();
                return null;
            }
            double interval;
            if (isInterval) {
                interval = splitParamter;
            } else {
                interval = (maxValue - minValue) / splitParamter;
            }
            double from = minValue, to;
            String condition, range;
            conn.setAutoCommit(false);
            try ( PreparedStatement insert = conn.prepareStatement(rangeTable.insertStatement())) {
                while (from <= maxValue) {
                    if (task == null || task.isCancelled()) {
                        query.close();
                        conn.close();
                        return null;
                    }
                    to = from + interval;
                    if (to >= maxValue) {
                        condition = colName + " >= " + from;
                        range = "[" + DoubleTools.scaleString(from, dscale) + ","
                                + DoubleTools.scaleString(maxValue, dscale) + "]";
                        from = maxValue + 1;
                    } else {
                        condition = colName + " >= " + from + " AND " + colName + " < " + to;
                        range = "[" + DoubleTools.scaleString(from, dscale) + ","
                                + DoubleTools.scaleString(to, dscale) + ")";
                        from = to;
                    }
                    sql = "SELECT * FROM " + sourceTable.getSheet() + " WHERE " + condition;
                    try ( ResultSet results = query.executeQuery(sql)) {
                        while (results.next()) {
                            if (task == null || task.isCancelled()) {
                                results.close();
                                query.close();
                                conn.close();
                                return null;
                            }
                            try {
                                Data2DRow data2DRow = rangeTable.newRow();
                                for (int i = 1; i < sourceTable.columns.size(); i++) {
                                    Data2DColumn sourceColumn = sourceTable.columns.get(i);
                                    String sourceColName = sourceColumn.getColumnName();
                                    String targetColName = rangeData.mappedColumnName(sourceColName);
                                    data2DRow.setColumnValue(targetColName, results.getObject(sourceColName));
                                }
                                data2DRow.setColumnValue(rangeName, range);
                                rangeTable.insertData(conn, insert, data2DRow);
                                if (++count % DerbyBase.BatchSize == 0) {
                                    conn.commit();
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
            } catch (Exception e) {
                if (task != null) {
                    task.setError(e.toString());
                } else {
                    MyBoxLog.error(e.toString());
                }
                return null;
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
            return null;
        }
        if (count < 1) {
            if (task != null) {
                task.setError(message("NoData"));
            }
            return null;
        }
        List<String> groups = new ArrayList<>();
        groups.add(rangeName);
        for (String name : sourceTable.columnsMap.keySet()) {
            rangeData.columnsMap.put(name, rangeData.columnsMap.get(sourceTable.columnsMap.get(name)));
        }
        DataFileCSV resultsFile = groupStatisticByValues(sourceTable, dname, task,
                groups, calculations, sorts, max, dscale, invalidAs);
        rangeData.drop();
        return resultsFile;
    }

    public static DataFileCSV groupStatisticByFilters(DataTable sourceTable, String dname, SingletonTask task,
            List<DataFilter> filters, List<String> calculations, List<String> sorts,
            int max, int dscale, InvalidAs invalidAs) {
        if (sourceTable == null || sourceTable.sourceColumns == null
                || filters == null || filters.isEmpty()) {
            return null;
        }
        List<Data2DColumn> tmpColumns = new ArrayList<>();
        String filterColName = message("RowFilter");
        tmpColumns.add(new Data2DColumn(filterColName, ColumnDefinition.ColumnType.String));
        tmpColumns.addAll(sourceTable.columns.subList(1, sourceTable.columns.size()));
        DataTable filtersData;
        long count = 0;
        try ( Connection conn = DerbyBase.getConnection();
                 Statement query = conn.createStatement();
                 ResultSet results = query.executeQuery("SELECT * FROM " + sourceTable.getSheet())) {
            filtersData = DataTable.createTable(task, conn, tmpColumns);
            filterColName = filtersData.mappedColumnName(filterColName);
            TableData2D filtersTable = filtersData.getTableData2D();
            long rindex = 0;
            List<DataFilter> filledFilters = new ArrayList<>();
            List<String> sourceScripts = new ArrayList<>();
            FindReplaceString findReplace = FindReplaceString.create().
                    setOperation(FindReplaceString.Operation.ReplaceAll)
                    .setIsRegex(false).setCaseInsensitive(false).setMultiline(false);
            for (int i = 0; i < filters.size(); i++) {
                String script = filters.get(i).getSourceScript();
                for (String name : sourceTable.columnsMap.keySet()) {
                    script = findReplace.replaceStringAll(script, "#{" + name + "}", "#{" + sourceTable.columnsMap.get(name) + "}");
                    script = findReplace.replaceStringAll(script, "#{" + name + "-" + message("Mean") + "}",
                            "#{" + sourceTable.columnsMap.get(name) + "-" + message("Mean") + "}");
                    script = findReplace.replaceStringAll(script, "#{" + name + "-" + message("Summation") + "}",
                            "#{" + sourceTable.columnsMap.get(name) + "-" + message("Summation") + "}");
                    script = findReplace.replaceStringAll(script, "#{" + name + "-" + message("Maximum") + "}",
                            "#{" + sourceTable.columnsMap.get(name) + "-" + message("Maximum") + "}");
                    script = findReplace.replaceStringAll(script, "#{" + name + "-" + message("Minimum") + "}",
                            "#{" + sourceTable.columnsMap.get(name) + "-" + message("Minimum") + "}");
                    script = findReplace.replaceStringAll(script, "#{" + name + "-" + message("PopulationVariance") + "}",
                            "#{" + sourceTable.columnsMap.get(name) + "-" + message("PopulationVariance") + "}");
                    script = findReplace.replaceStringAll(script, "#{" + name + "-" + message("SampleVariance") + "}",
                            "#{" + sourceTable.columnsMap.get(name) + "-" + message("SampleVariance") + "}");
                    script = findReplace.replaceStringAll(script, "#{" + name + "-" + message("PopulationStandardDeviation") + "}",
                            "#{" + sourceTable.columnsMap.get(name) + "-" + message("PopulationStandardDeviation") + "}");
                    script = findReplace.replaceStringAll(script, "#{" + name + "-" + message("SampleStandardDeviation") + "}",
                            "#{" + sourceTable.columnsMap.get(name) + "-" + message("SampleStandardDeviation") + "}");
                }
                sourceScripts.add(script);
            }
            List<String> filledScripts = sourceTable.calculateScriptsStatistic(sourceScripts);
            for (int i = 0; i < filters.size(); i++) {
                DataFilter sfilter = filters.get(i);
                DataFilter tfilter = new DataFilter()
                        .setSourceScript(filledScripts.get(i))
                        .setReversed(sfilter.isReversed())
                        .setMaxPassed(sfilter.getMaxPassed());
                filledFilters.add(tfilter);
            }
            try ( PreparedStatement insert = conn.prepareStatement(filtersTable.insertStatement())) {
                while (results.next()) {
                    if (task == null || task.isCancelled()) {
                        results.close();
                        query.close();
                        conn.close();
                        return null;
                    }
                    try {
                        Data2DRow data2DRow = filtersTable.newRow();
                        List<String> sourceRow = new ArrayList<>();
                        for (Data2DColumn sourceColumn : sourceTable.columns) {
                            String sourceColName = sourceColumn.getColumnName();
                            Object value = results.getObject(sourceColName);
                            sourceRow.add(sourceColumn.toString(value));
                            String targetColName = filtersData.mappedColumnName(sourceColName);
                            data2DRow.setColumnValue(targetColName, value);
                        }
                        rindex++;
                        for (int i = 0; i < filledFilters.size(); i++) {
                            DataFilter vfilter = filledFilters.get(i);
                            if (vfilter.filterDataRow(sourceTable, sourceRow, rindex)) {
                                data2DRow.setColumnValue(filterColName, filterColName + (i + 1));
                                filtersTable.insertData(conn, insert, data2DRow);
                                if (++count % DerbyBase.BatchSize == 0) {
                                    conn.commit();
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
                conn.commit();
            } catch (Exception e) {
                if (task != null) {
                    task.setError(e.toString());
                } else {
                    MyBoxLog.error(e.toString());
                }
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
            return null;
        }
        if (count < 1) {
            if (task != null) {
                task.setError(message("NoData"));
            }
            return null;
        }
        List<String> groups = new ArrayList<>();
        groups.add(filterColName);
        for (String name : sourceTable.columnsMap.keySet()) {
            filtersData.columnsMap.put(name, filtersData.columnsMap.get(sourceTable.columnsMap.get(name)));
        }
        DataFileCSV resultsFile = groupStatisticByValues(sourceTable, dname, task,
                groups, calculations, sorts, max, dscale, invalidAs);
        filtersData.drop();
        return resultsFile;
    }

}
