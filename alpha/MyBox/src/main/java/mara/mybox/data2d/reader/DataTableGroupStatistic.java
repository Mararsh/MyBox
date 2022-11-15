package mara.mybox.data2d.reader;

import java.io.File;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.calculation.DescriptiveStatistic;
import mara.mybox.calculation.DescriptiveStatistic.StatisticType;
import mara.mybox.calculation.DoubleStatistic;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
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
 * @CreateDate 2022-10-16
 * @License Apache License Version 2.0
 */
public class DataTableGroupStatistic {

    protected DataTableGroup groups;
    protected DataTable groupResults;
    protected List<String> calNames;
    protected DescriptiveStatistic calculation;
    protected short scale;

    protected Connection conn;
    protected List<Data2DColumn> groupColumns;
    protected String idColName, parameterName, parameterValue;

    protected DataTable groupData, statisticData;
    protected TableData2D tableGroup, tableStatistic;
    protected long groupid, chartRowsCount, statisticRowsCount;
    protected PreparedStatement statisticInsert;

    protected SingletonTask task;
    protected boolean countChart, ok;

    protected File chartFile;
    protected CSVPrinter chartPrinter;
    protected DataFileCSV chartData;

    public boolean run() {
        if (groups == null || calNames == null
                || calculation == null || !calculation.need()) {
            return false;
        }
        groupResults = groups.getTargetData();
        if (groupResults == null) {
            return false;
        }
        ok = false;
        try ( Connection dconn = DerbyBase.getConnection()) {
            conn = dconn;
            init();
            String currentParameterValue;
            long currentGroupid, count = 0;
            String mappedIdColName = groupResults.tmpColumnName(idColName);
            String sql = "SELECT * FROM " + groupResults.getSheet() + " ORDER BY " + mappedIdColName;
            String mappedParameterName = groupResults.tmpColumnName(parameterName);
            if (task != null) {
                task.setInfo(sql);
            }
            try ( ResultSet query = conn.prepareStatement(sql).executeQuery();
                     PreparedStatement insert = conn.prepareStatement(tableGroup.insertStatement());) {
                while (query.next()) {
                    if (task == null || task.isCancelled()) {
                        query.close();
                        chartPrinter.close();
                        conn.close();
                        return false;
                    }
                    currentGroupid = query.getLong(mappedIdColName);
                    currentParameterValue = query.getString(mappedParameterName);
                    Data2DRow data2DRow = tableGroup.newRow();
                    for (String name : calNames) {
                        Object v = query.getObject(groupResults.tmpColumnName(name));
                        data2DRow.setColumnValue(groupData.tmpColumnName(name), v);
                    }
                    if (groupid > 0 && groupid != currentGroupid) {
                        insert.executeBatch();
                        conn.commit();
                        statistic();
                        tableGroup.clearData(conn);
                        count = 0;
                    }
                    if (tableGroup.setInsertStatement(conn, insert, data2DRow)) {
                        insert.addBatch();
                        if (++count % DerbyBase.BatchSize == 0) {
                            insert.executeBatch();
                            conn.commit();
                            if (task != null) {
                                task.setInfo(message("Inserted") + ": " + count);
                            }
                        }
                    }
                    groupid = currentGroupid;
                    parameterValue = currentParameterValue;
                }
                insert.executeBatch();
                conn.commit();
                statistic();
            }

            ok = finish();
            conn.commit();
            conn = null;
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
            ok = false;
        }
        return ok;
    }

    private boolean init() {
        try {
            idColName = groups.getIdColName();
            parameterName = groups.getParameterName();
            scale = groups.getScale();

            List<Data2DColumn> valuesColumns = new ArrayList<>();
            valuesColumns.add(new Data2DColumn(idColName, ColumnType.Long));
            valuesColumns.add(new Data2DColumn(parameterName, ColumnType.String, 200));
            valuesColumns.add(new Data2DColumn(message("ColumnName"), ColumnType.String, 200));
            for (StatisticType type : calculation.types) {
                valuesColumns.add(new Data2DColumn(message("Group") + "_" + message(type.name()),
                        ColumnType.Double, 150));
            }
            String dname = groupResults.dataName() + "_" + message("Statistic");
            statisticData = DataTable.createTable(task, conn, dname, valuesColumns, null, null, null, true);
            statisticData.setDataName(dname).setScale(scale);
            tableStatistic = statisticData.getTableData2D();
            statisticInsert = conn.prepareStatement(tableStatistic.insertStatement());

            if (countChart) {
                chartData = new DataFileCSV();
                List<Data2DColumn> chartColumns = new ArrayList<>();
                chartColumns.add(new Data2DColumn(idColName, ColumnType.Long));
                chartColumns.add(new Data2DColumn(parameterName, ColumnType.String, 200));
                chartColumns.add(new Data2DColumn(message("Group") + "_" + message("Count"), ColumnType.Long));
                for (String name : calNames) {
                    for (StatisticType type : calculation.types) {
                        if (type == StatisticType.Count) {
                            continue;
                        }
                        chartColumns.add(new Data2DColumn(name + "_" + message(type.name()), ColumnType.Double, 200));
                    }
                }
                String chartname = dname + "_Chart";
                chartFile = getPathTempFile(AppPaths.getGeneratedPath(), chartname, ".csv");
                chartData.setColumns(chartColumns).setDataName(chartname)
                        .setFile(chartFile).setCharset(Charset.forName("UTF-8"))
                        .setDelimiter(",").setHasHeader(true)
                        .setColsNumber(chartColumns.size()).setScale(scale);
                chartPrinter = CsvTools.csvPrinter(chartFile);
                chartPrinter.printRecord(chartData.columnNames());
            }

            groupColumns = new ArrayList<>();
            for (String name : calNames) {
                Data2DColumn c = groupResults.columnByName(groupResults.tmpColumnName(name)).cloneAll();
                c.setD2cid(-1).setD2id(-1).setColumnName(name);
                groupColumns.add(c);
            }
            groupData = DataTable.createTable(task, conn, groupColumns);
            tableGroup = groupData.getTableData2D();

            groupid = 0;
            chartRowsCount = 0;
            statisticRowsCount = 0;

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
            return false;
        }
    }

    private void statistic() {
        try {
            groupData.resetStatistic();
            groupData.setTask(task).setScale(groups.getScale());

            int colSize = calNames.size();
            List<Integer> cols = new ArrayList<>();
            for (int i = 1; i <= colSize; i++) {
                cols.add(i);
            }
            if (task != null) {
                task.setInfo(parameterName + ": " + parameterValue + "\n"
                        + message("Statistic") + ": " + calculation.names());
            }
            DoubleStatistic[] sData = null;
            if (calculation.needNonStored()) {
                sData = groupData.statisticByColumnsWithoutStored(cols, calculation);
            }
            if (calculation.needStored()) {
                sData = groupData.statisticByColumnsForStored(cols, calculation);
            }
            if (sData == null) {
                return;
            }
            for (int i = 0; i < colSize; i++) {
                Data2DRow data2DRow = tableStatistic.newRow();
                data2DRow.setColumnValue(statisticData.tmpColumnName(idColName), groupid);
                data2DRow.setColumnValue(statisticData.tmpColumnName(parameterName), parameterValue);
                data2DRow.setColumnValue(statisticData.tmpColumnName(message("ColumnName")), calNames.get(i));
                DoubleStatistic s = sData[i];
                for (StatisticType type : calculation.types) {
                    data2DRow.setColumnValue(statisticData.tmpColumnName(message("Group") + "_" + message(type.name())),
                            DoubleTools.scale(s.value(type), scale));
                }
                if (tableStatistic.setInsertStatement(conn, statisticInsert, data2DRow)) {
                    statisticInsert.addBatch();
                    if (++statisticRowsCount % DerbyBase.BatchSize == 0) {
                        statisticInsert.executeBatch();
                        conn.commit();
                        if (task != null) {
                            task.setInfo(message("Inserted") + ": " + statisticRowsCount);
                        }
                    }
                }
            }
            statisticInsert.executeBatch();
            conn.commit();
            if (task != null) {
                task.setInfo(message("Inserted") + ": " + statisticRowsCount);
            }
            if (countChart) {
                List<String> row = new ArrayList<>();
                row.add(groupid + "");
                row.add(parameterValue);
                row.add(sData[0].count + "");
                for (DoubleStatistic s : sData) {
                    List<String> vs = s.toStringList();
                    int size = vs.size();
                    if (size > 1) {
                        row.addAll(vs.subList(1, size));
                    }
                }
                chartPrinter.printRecord(row);
                chartRowsCount++;
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
        }
    }

    private boolean finish() {
        try {
            statisticInsert.executeBatch();
            conn.commit();

            groupData.drop(conn);

            statisticData.setDataSize(statisticRowsCount).setRowsNumber(statisticRowsCount);
            Data2D.saveAttributes(conn, statisticData, statisticData.getColumns());

            if (countChart) {
                chartPrinter.flush();
                chartPrinter.close();
                chartData.setDataSize(chartRowsCount).setRowsNumber(chartRowsCount);
                Data2D.saveAttributes(conn, chartData, chartData.getColumns());
            }

            ok = true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
            ok = false;
        }
        return ok;
    }

    // groupid is 1-based
    public List<List<String>> groupData(Connection qconn, long groupid) {
        if (statisticData == null || qconn == null
                || groupid < 1 || groupid > statisticRowsCount) {
            return null;
        }
        List<List<String>> data = new ArrayList<>();
        String sql = "SELECT * FROM " + statisticData.getSheet()
                + " WHERE " + statisticData.tmpColumnName(idColName) + "=" + groupid;
        try ( ResultSet query = qconn.prepareStatement(sql).executeQuery()) {
            while (query.next() && qconn != null && !qconn.isClosed()) {
                List<String> vrow = new ArrayList<>();
                for (int i = 3; i < statisticData.getColumns().size(); i++) {
                    Data2DColumn column = statisticData.getColumns().get(i);
                    String name = column.getColumnName();
                    String s = column.toString(query.getObject(name));
                    vrow.add(s);
                }
                data.add(vrow);
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
    public DataFileCSV getChartData() {
        return chartData;
    }

    public DataTable getStatisticData() {
        return statisticData;
    }

    /*
        set
     */
    public DataTableGroupStatistic setGroups(DataTableGroup groups) {
        this.groups = groups;
        return this;
    }

    public DataTableGroupStatistic setTask(SingletonTask task) {
        this.task = task;
        return this;
    }

    public DataTableGroupStatistic setCalNames(List<String> calNames) {
        this.calNames = calNames;
        return this;
    }

    public DataTableGroupStatistic setCalculation(DescriptiveStatistic calculation) {
        this.calculation = calculation;
        return this;
    }

    public DataTableGroupStatistic setCountChart(boolean countChart) {
        this.countChart = countChart;
        return this;
    }

}
