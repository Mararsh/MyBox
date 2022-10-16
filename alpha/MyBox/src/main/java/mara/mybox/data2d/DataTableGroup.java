package mara.mybox.data2d;

import java.io.File;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import mara.mybox.data2d.Data2D_Attributes.InvalidAs;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
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
public class DataTableGroup {

    // Based on results of "Data2D_Convert.toTmpTable(...)"
    public static DataFileCSV groupByValues(DataTable sourceTable,
            String dname, SingletonTask task,
            List<String> groupNames, List<String> copyNames, List<String> sorts,
            int max, int dscale, InvalidAs invalidAs) {
        if (sourceTable == null || sourceTable.sourceColumns == null
                || groupNames == null || groupNames.isEmpty()) {
            return null;
        }
        try {
            String selections = null;
            List<String> valueNames = new ArrayList<>();
            for (String group : groupNames) {
                if (valueNames.contains(group)) {
                    continue;
                }
                valueNames.add(group);
                String name = sourceTable.mappedColumnName(group);
                if (selections == null) {
                    selections = name;
                } else {
                    selections += ", " + name;
                }
            }
            String orderBy = selections;
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
                    name = sourceTable.mappedColumnName(name);
                    orderBy += ", " + name + stype;
                }
            }
            if (copyNames != null && !copyNames.isEmpty()) {
                for (String copy : copyNames) {
                    if (valueNames.contains(copy)) {
                        continue;
                    }
                    String name = sourceTable.mappedColumnName(copy);
                    if (selections == null) {
                        selections = name;
                    } else {
                        selections += ", " + name;
                    }
                }
            }
            String sql = "SELECT " + selections + " FROM " + sourceTable.getSheet() + " ORDER BY " + orderBy;
            MyBoxLog.console(sql);
            long count = 0;
            File csvFile = getPathTempFile(AppPaths.getGeneratedPath(), dname + "_group", ".csv");
            List<Data2DColumn> fileColumns = new ArrayList<>();
            List<String> fileColumnNames = new ArrayList<>();
            String groupName = message("Group");
            fileColumnNames.add(groupName);
            fileColumns.add(new Data2DColumn(groupName, ColumnDefinition.ColumnType.String));
            Random random = new Random();
            for (Data2DColumn sourceColumn : sourceTable.sourceColumns) {
                String name = sourceColumn.getColumnName();
                if (!valueNames.contains(name)) {
                    continue;
                }
                Data2DColumn dataColumn = new Data2DColumn();
                dataColumn.cloneFrom(sourceColumn);
                dataColumn.setD2id(-1).setD2cid(-1)
                        .setAuto(false).setIsPrimaryKey(false);
                while (fileColumnNames.contains(name)) {
                    name += random.nextInt(10);
                }
                dataColumn.setColumnName(name);
                fileColumnNames.add(name);
                fileColumns.add(dataColumn);
            }
            try ( CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile);
                     Connection conn = DerbyBase.getConnection();
                     PreparedStatement statement = conn.prepareStatement(sql);
                     ResultSet results = statement.executeQuery()) {
                csvPrinter.printRecord(fileColumnNames);
                Map<String, String> sourceRow, lastRow = null;
                boolean groupChanged;
                long groupid = 1, groupSize = 0;
                List<String> fileRow = new ArrayList<>();
                String value;
                while (results.next() && task != null && !task.isCancelled()) {
                    sourceRow = new HashMap<>();
                    for (String name : valueNames) {
                        String mappedName = sourceTable.mappedColumnName(name);
                        if (mappedName == null) {
                            value = null;
                        } else {
                            Data2DColumn column = sourceTable.columnByName(mappedName);
                            Object v = results.getObject(mappedName);
                            value = column.toString(v);
                            sourceRow.put(name, value);
                            if (column.needScale()) {
                                value = DoubleTools.scaleString(value, invalidAs, dscale);
                            }
                        }
                        fileRow.add(value);
                    }
                    groupChanged = false;
                    if (lastRow != null) {
                        for (String group : groupNames) {
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
                        groupid++;
                        groupSize = 0;
                    }
                    if (groupSize++ <= max) {
                        fileRow.add(0, groupName + groupid);
                        csvPrinter.printRecord(fileRow);
                    }
                    lastRow = sourceRow;
                    fileRow.clear();
                    count++;
                }
            } catch (Exception e) {
                if (task != null) {
                    task.setError(e.toString());
                } else {
                    MyBoxLog.error(e.toString());
                }
            }
            if (task == null || task.isCancelled()) {
                return null;
            }
            DataFileCSV targetData = new DataFileCSV();
            targetData.setColumns(fileColumns)
                    .setFile(csvFile).setDataName(dname)
                    .setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(true)
                    .setColsNumber(fileColumns.size()).setRowsNumber(count);
            targetData.saveAttributes();
            return targetData;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
            return null;
        }

    }

}
