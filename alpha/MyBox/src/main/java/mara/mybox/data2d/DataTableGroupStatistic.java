package mara.mybox.data2d;

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
import mara.mybox.data2d.reader.DataTableGroup;
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
    protected List<String> calNames, row;
    protected DescriptiveStatistic calculation;
    protected short scale;

    protected Connection conn;
    protected List<Data2DColumn> groupColumns;
    protected String idColName, parameterName, parameterValue;

    protected DataTable groupData, statisticData;
    protected TableData2D tableGroup, tableStatistic;
    protected long groupid, mixCount, statisticCount;
    protected PreparedStatement statisticInsert;

    protected SingletonTask task;
    protected boolean mix, ok;

    protected File mixFile;
    protected CSVPrinter mixPrinter;
    protected DataFileCSV mixData;

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
            String sql = "SELECT * FROM " + groupResults.getSheet() + " ORDER BY " + idColName;
            String mappedIdColName = groupResults.tmpColumnName(idColName);
            String mappedParameterName = groupResults.tmpColumnName(parameterName);
            try ( ResultSet query = conn.prepareStatement(sql).executeQuery();
                     PreparedStatement insert = conn.prepareStatement(tableGroup.insertStatement());) {
                while (query.next()) {
                    if (task == null || task.isCancelled()) {
                        query.close();
                        mixPrinter.close();
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
                        conn.commit();
                        statistic();
                        tableGroup.clearData(conn);
                        count = 0;
                    }
                    tableGroup.insertData(conn, insert, data2DRow);
                    if (++count % DerbyBase.BatchSize == 0) {
                        conn.commit();
                    }
                    groupid = currentGroupid;
                    parameterValue = currentParameterValue;
                }
                conn.commit();
                statistic();
            }

            ok = finish();
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
            valuesColumns.add(new Data2DColumn(message("Column"), ColumnType.String, 200));
            for (StatisticType type : calculation.types) {
                valuesColumns.add(new Data2DColumn(message(type.name()), ColumnType.Double, 150));
            }
            String dname = groupResults.dataName() + "_" + message("Statistic");
            statisticData = DataTable.createTable(task, conn, dname, valuesColumns, null, null, null, true);
            statisticData.setDataName(dname).setScale(scale);
            tableStatistic = statisticData.getTableData2D();
            statisticInsert = conn.prepareStatement(tableStatistic.insertStatement());

            if (mix) {
                mixData = new DataFileCSV();
                List<Data2DColumn> mixColumns = new ArrayList<>();
                mixColumns.add(new Data2DColumn(idColName, ColumnType.Long));
                mixColumns.add(new Data2DColumn(parameterName, ColumnType.String, 200));
                mixColumns.add(new Data2DColumn(message("Count"), ColumnType.Long));
                for (String name : calNames) {
                    for (StatisticType type : calculation.types) {
                        if (type == StatisticType.Count) {
                            continue;
                        }
                        mixColumns.add(new Data2DColumn(name + "_" + message(type.name()), ColumnType.Double, 200));
                    }
                }
                String mixname = dname + "_Mixed";
                mixFile = getPathTempFile(AppPaths.getGeneratedPath(), mixname, ".csv");
                mixData.setColumns(mixColumns).setDataName(mixname)
                        .setFile(mixFile).setCharset(Charset.forName("UTF-8"))
                        .setDelimiter(",").setHasHeader(true)
                        .setColsNumber(mixColumns.size()).setScale(scale);
                mixPrinter = CsvTools.csvPrinter(mixFile);
                mixPrinter.printRecord(mixData.columnNames());
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
            mixCount = 0;
            statisticCount = 0;
            row = new ArrayList<>();
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
            groupData.setScale(groups.getScale());

            int colSize = calNames.size();
            List<Integer> cols = new ArrayList<>();
            for (int i = 1; i <= colSize; i++) {
                cols.add(i);
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
                data2DRow.setColumnValue(statisticData.tmpColumnName(message("Column")), calNames.get(i));
                DoubleStatistic s = sData[i];
                for (StatisticType type : calculation.types) {
                    data2DRow.setColumnValue(statisticData.tmpColumnName(message(type.name())),
                            DoubleTools.scale(s.value(type), scale));
                }
                tableStatistic.insertData(conn, statisticInsert, data2DRow);
                if (++statisticCount % DerbyBase.BatchSize == 0) {
                    conn.commit();
                }
            }

            if (mix) {
                row.clear();
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
                mixPrinter.printRecord(row);
                mixCount++;
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
            groupData.drop(conn);
            conn.commit();

            statisticData.setDataSize(statisticCount).setRowsNumber(statisticCount);
            Data2D.saveAttributes(conn, statisticData, statisticData.getColumns());

            if (mix) {
                mixPrinter.flush();
                mixPrinter.close();
                mixData.setDataSize(mixCount).setRowsNumber(mixCount);
                Data2D.saveAttributes(conn, mixData, mixData.getColumns());
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

    /*
        get
     */
    public DataFileCSV getMixData() {
        return mixData;
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

    public DataTableGroupStatistic setMix(boolean mix) {
        this.mix = mix;
        return this;
    }

}
