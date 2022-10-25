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

    protected Connection conn;
    protected List<String> groupColNames;
    protected List<Data2DColumn> groupColumns;
    protected String idColName, parameterName, parameterValue;

    protected DataTable groupData;
    protected TableData2D tableGroup;
    protected long groupid;

    protected SingletonTask task;
    protected boolean ok;

    protected File csvFile;
    protected CSVPrinter csvPrinter;
    protected DataFileCSV targetFile;

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
            groupColumns = new ArrayList<>();
            groupColNames = new ArrayList<>();
            idColName = message("Group");
            parameterName = groups.getParameterName();
            List<String> header = new ArrayList<>();
            header.add(idColName);
            header.add(parameterName);
            header.add(message("Count"));
            for (String name : calNames) {
                Data2DColumn c = groupResults.columnByName(groupResults.tmpColumnName(name));
                groupColumns.add(c.cloneAll().setD2cid(-1).setD2id(-1));
                groupColNames.add(name);
                for (StatisticType type : calculation.types) {
                    if (type == StatisticType.Count) {
                        continue;
                    }
                    header.add(name + "_" + message(type.name()));
                }
            }
            groupData = DataTable.createTable(task, conn, groupColumns);
            tableGroup = groupData.getTableData2D();
            long currentGroupid;
            String currentParameterValue;
            groupid = 0;
            long count = 0;
            String sql = "SELECT * FROM " + groupResults.getSheet() + " ORDER BY " + idColName;
            String dname = groupResults.dataName() + "_" + message("Statistic");
            csvFile = getPathTempFile(AppPaths.getGeneratedPath(), dname, ".csv");
            csvPrinter = CsvTools.csvPrinter(csvFile);
            csvPrinter.printRecord(header);
            try ( ResultSet query = conn.prepareStatement(sql).executeQuery();
                     PreparedStatement insert = conn.prepareStatement(tableGroup.insertStatement());) {
                while (query.next()) {
                    if (task == null || task.isCancelled()) {
                        query.close();
                        csvPrinter.close();
                        conn.close();
                        return false;
                    }
                    currentGroupid = query.getLong(idColName);
                    currentParameterValue = query.getString(parameterName);
                    Data2DRow data2DRow = tableGroup.newRow();
                    for (String name : calNames) {
                        String tname = groupResults.tmpColumnName(name);
                        Object v = query.getObject(tname);
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
            csvPrinter.flush();
            csvPrinter.close();
            groupData.drop(conn);

            targetFile = new DataFileCSV();
            groupColNames = new ArrayList<>();
            List<Data2DColumn> fileColumns = new ArrayList<>();
            fileColumns.add(new Data2DColumn(idColName, ColumnType.Long));
            fileColumns.add(new Data2DColumn(groups.getParameterName(), ColumnType.String, 200));
            fileColumns.add(new Data2DColumn(message("Count"), ColumnType.Long));
            for (String name : calNames) {
                for (StatisticType type : calculation.types) {
                    if (type == StatisticType.Count) {
                        continue;
                    }
                    fileColumns.add(new Data2DColumn(name + "_" + message(type.name()), ColumnType.Double));
                }
            }
            targetFile.setColumns(fileColumns).setDataSize(count)
                    .setDataName(dname)
                    .setFile(csvFile).setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(true)
                    .setColsNumber(fileColumns.size())
                    .setRowsNumber(count);
            Data2D.saveAttributes(conn, targetFile, fileColumns);
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

    private void statistic() {
        try {
//            MyBoxLog.console(groupid + "   " + parameterValue);
            groupData.resetStatistic();
            List<Integer> cols = groupData.columnIndices().subList(1, groupData.columnsNumber());
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
            List<String> row = new ArrayList<>();
            row.add(groupid + "");
            row.add(parameterValue);
            row.add(sData[0].count + "");
            for (DoubleStatistic s : sData) {
                s.options = calculation;
                List<String> vs = s.toStringList();
                int size = vs.size();
                if (size > 1) {
                    row.addAll(vs.subList(1, size));
                }
            }
            csvPrinter.printRecord(row);
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
        }
    }

    /*
        get
     */
    public DataFileCSV getTargetFile() {
        return targetFile;
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

}
