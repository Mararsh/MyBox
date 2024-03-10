package mara.mybox.data2d;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.calculation.DescriptiveStatistic;
import mara.mybox.calculation.DescriptiveStatistic.StatisticType;
import mara.mybox.calculation.DoubleStatistic;
import mara.mybox.calculation.Normalization;
import mara.mybox.calculation.SimpleLinearRegression;
import mara.mybox.data2d.operate.Data2DCopy;
import mara.mybox.data2d.operate.Data2DExport;
import mara.mybox.data2d.operate.Data2DFrequency;
import mara.mybox.data2d.operate.Data2DNormalize;
import mara.mybox.data2d.operate.Data2DOperate;
import mara.mybox.data2d.operate.Data2DPrecentage;
import mara.mybox.data2d.operate.Data2DRange;
import mara.mybox.data2d.operate.Data2DReadColumns;
import mara.mybox.data2d.operate.Data2DReadRows;
import mara.mybox.data2d.operate.Data2DRowExpression;
import mara.mybox.data2d.operate.Data2DSimpleLinearRegression;
import mara.mybox.data2d.operate.Data2DSplit;
import mara.mybox.data2d.operate.Data2DStatistic;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.InvalidAs;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.CsvTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.NumberTools;
import mara.mybox.value.AppValues;
import static mara.mybox.value.Languages.message;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.math3.stat.Frequency;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public abstract class Data2D_Operations extends Data2D_Convert {

    public static enum ObjectType {
        Columns, Rows, All
    }

    public boolean export(Data2DExport export, List<Integer> cols) {
        if (export == null || cols == null || cols.isEmpty()) {
            return false;
        }
        export.setCols(cols).setTask(task).start();
        return !export.isFailed();
    }

    public List<List<String>> allRows(List<Integer> cols, boolean rowNumber) {
        Data2DReadColumns reader = Data2DReadColumns.create(this);
        reader.setIncludeRowNumber(rowNumber)
                .setCols(cols).setTask(task).start();
        return reader.isFailed() ? null : reader.getRows();
    }

    public List<List<String>> allRows(boolean rowNumber) {
        Data2DReadRows reader = Data2DReadRows.create(this);
        reader.setIncludeRowNumber(rowNumber).setTask(task).start();
        return reader.isFailed() ? null : reader.getRows();
    }

    public DoubleStatistic[] statisticByColumnsForCurrentPage(List<Integer> cols, DescriptiveStatistic selections) {
        try {
            if (cols == null || cols.isEmpty() || selections == null) {
                return null;
            }
            int colLen = cols.size();
            DoubleStatistic[] sData = new DoubleStatistic[colLen];
            int rNumber = pageData.size();
            for (int c = 0; c < colLen; c++) {
                int colIndex = cols.get(c);
                String[] colData = new String[rNumber];
                for (int r = 0; r < rNumber; r++) {
                    colData[r] = pageData.get(r).get(colIndex + 1);
                }
                DoubleStatistic colStatistic = new DoubleStatistic();
                colStatistic.invalidAs = selections.invalidAs;
                colStatistic.calculate(colData, selections);
                columns.get(colIndex).setStatistic(colStatistic);
                sData[c] = colStatistic;
            }
            return sData;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
    }

    // No percentile nor mode
    public DoubleStatistic[] statisticByColumnsWithoutStored(List<Integer> cols, DescriptiveStatistic selections) {
        try {
            if (cols == null || cols.isEmpty() || selections == null) {
                return null;
            }
            int colLen = cols.size();
            DoubleStatistic[] sData = new DoubleStatistic[colLen];
            for (int c = 0; c < colLen; c++) {
                Data2DColumn column = columns.get(cols.get(c));
                DoubleStatistic colStatistic = column.getStatistic();
                if (colStatistic == null) {
                    colStatistic = new DoubleStatistic();
                    column.setStatistic(colStatistic);
                }
                colStatistic.invalidAs = selections.invalidAs;
                colStatistic.options = selections;
                sData[c] = colStatistic;
            }
            Data2DOperate reader = Data2DStatistic.create(this)
                    .setStatisticData(sData)
                    .setStatisticCalculation(selections)
                    .setType(Data2DStatistic.Type.ColumnsPass1)
                    .setCols(cols).setTask(task).start();
            if (reader == null || reader.isFailed()) {
                return null;
            }
            if (selections.needVariance()) {
                reader = Data2DStatistic.create(this)
                        .setStatisticData(sData)
                        .setStatisticCalculation(selections)
                        .setType(Data2DStatistic.Type.ColumnsPass2)
                        .setCols(cols).setTask(task).start();
                if (reader == null || reader.isFailed()) {
                    return null;
                }
            }
            return sData;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
    }

    // percentile or mode
    public DoubleStatistic[] statisticByColumnsForStored(List<Integer> cols, DescriptiveStatistic selections) {
        try {
            if (cols == null || cols.isEmpty() || selections == null) {
                return null;
            }
            TmpTable tmpTable = TmpTable.toStatisticTable((Data2D) this, task, cols, selections.invalidAs);
            if (tmpTable == null) {
                return null;
            }
            List<Integer> tmpColIndices = tmpTable.columnIndices().subList(1, tmpTable.columnsNumber());
            DoubleStatistic[] statisticData = tmpTable.statisticByColumnsForStored(tmpColIndices, selections);
            if (statisticData == null) {
                return null;
            }
            for (int i = 0; i < cols.size(); i++) {
                Data2DColumn column = column(cols.get(i));
                column.setStatistic(statisticData[i]);
            }
            tmpTable.drop();
            return statisticData;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
    }

    public DataFileCSV statisticByRows(String dname, List<String> names, List<Integer> cols, DescriptiveStatistic selections) {
        if (names == null || names.isEmpty() || cols == null || cols.isEmpty()) {
            return null;
        }
        File csvFile = tmpFile(dname, "statistic", "csv");
        Data2DOperate reader = null;
        try (CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
            csvPrinter.printRecord(names);
            reader = Data2DStatistic.create(this)
                    .setStatisticCalculation(selections)
                    .setType(Data2DStatistic.Type.Rows)
                    //                    .setCsvPrinter(csvPrinter)
                    .setCols(cols).setTask(task).start();
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (reader != null && csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setFile(csvFile).setDataName(dname)
                    .setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(true)
                    .setColsNumber(names.size()).setRowsNumber(reader.getSourceRowIndex());
            return targetData;
        } else {
            return null;
        }
    }

    // No percentile nor mode
    public DoubleStatistic statisticByAllWithoutStored(List<Integer> cols, DescriptiveStatistic selections) {
        if (cols == null || cols.isEmpty()) {
            return null;
        }
        DoubleStatistic sData = new DoubleStatistic();
        sData.invalidAs = selections.invalidAs;
        Data2DOperate reader = Data2DStatistic.create(this)
                .setStatisticAll(sData)
                .setStatisticCalculation(selections)
                .setType(Data2DStatistic.Type.AllPass1)
                .setCols(cols).setTask(task).start();
        if (reader == null || reader.isFailed()) {
            return null;
        }
        if (selections.needVariance()) {
            reader = Data2DStatistic.create(this)
                    .setStatisticAll(sData)
                    .setStatisticCalculation(selections)
                    .setType(Data2DStatistic.Type.AllPass2)
                    .setCols(cols).setTask(task).start();
            if (reader == null || reader.isFailed()) {
                return null;
            }
        }
        return sData;
    }

    public List<Data2DColumn> targetColumns(List<Integer> cols, List<Integer> otherCols, boolean rowNumber, String suffix) {
        List<Data2DColumn> targetColumns = new ArrayList<>();
        if (rowNumber) {
            targetColumns.add(0, new Data2DColumn(message("SourceRowNumber"), ColumnDefinition.ColumnType.Long));
        }
        for (int c : cols) {
            Data2DColumn column = columns.get(c).cloneAll().setD2cid(-1).setD2id(-1);
            if (suffix != null) {
                column.setColumnName(column.getColumnName() + "_" + suffix).setWidth(200);
            }
            targetColumns.add(column);
        }
        if (otherCols != null && !otherCols.isEmpty()) {
            for (int c : otherCols) {
                Data2DColumn column = columns.get(c).cloneAll().setD2cid(-1).setD2id(-1);
                targetColumns.add(column);
            }
        }
        return fixColumnNames(targetColumns);
    }

    public DataFileCSV copy(FxTask task, List<Integer> cols,
            boolean includeRowNumber, boolean includeColName, boolean formatValues) {
        if (cols == null || cols.isEmpty()) {
            return null;
        }
        File dfile = FileTmpTools.getTempFile(".csv");
        Data2DOperate reader;
        List<Data2DColumn> targetColumns = targetColumns(cols, null, includeRowNumber, null);
        try (CSVPrinter csvPrinter = CsvTools.csvPrinter(dfile)) {
            List<String> names = new ArrayList<>();
            for (Data2DColumn column : targetColumns) {
                names.add(column.getColumnName());
            }
            if (includeColName) {
                csvPrinter.printRecord(names);
            }
            reader = Data2DCopy.create(this)
                    //                    .setCsvPrinter(csvPrinter)
                    .setFormatValues(formatValues)
                    .setIncludeRowNumber(includeRowNumber)
                    .setCols(cols).setTask(task).start();
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (reader != null && !reader.isFailed() && dfile != null && dfile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setColumns(targetColumns).setFile(dfile)
                    .setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(includeColName)
                    .setColsNumber(targetColumns.size())
                    .setRowsNumber(reader.getSourceRowIndex());
            targetData.saveAttributes();
            return targetData;
        } else {
            return null;
        }
    }

    public List<DataFileCSV> splitBySize(List<Integer> cols, boolean includeRowNumber, int splitSize) {
        if (cols == null || cols.isEmpty()) {
            return null;
        }
        Data2DSplit reader = Data2DSplit.create(this).setSplitSize(splitSize);
        reader.setIncludeRowNumber(includeRowNumber)
                .setCols(cols).setTask(task).start();
        return reader.getFiles();
    }

    public List<DataFileCSV> splitByList(List<Integer> cols, boolean includeRowNumber, List<Integer> list) {
        if (cols == null || cols.isEmpty() || list == null || list.isEmpty()) {
            return null;
        }
        try {
            String prefix = dataName();
            List<Data2DColumn> targetColumns = targetColumns(cols, null, includeRowNumber, null);
            List<String> names = new ArrayList<>();
            if (includeRowNumber) {
                targetColumns.add(0, new Data2DColumn(message("SourceRowNumber"), ColumnDefinition.ColumnType.Long));
            }
            for (int c : cols) {
                Data2DColumn column = column(c);
                names.add(column.getColumnName());
                targetColumns.add(column.cloneAll().setD2cid(-1).setD2id(-1));
            }
            List<DataFileCSV> files = new ArrayList<>();
            for (int i = 0; i < list.size();) {
                long start = Math.round(list.get(i++));
                long end = Math.round(list.get(i++));
                if (start <= 0) {
                    start = 1;
                }
                if (end > dataSize) {
                    end = dataSize;
                }
                if (start > end) {
                    continue;
                }
                File csvfile = tmpFile(prefix + "_" + start + "-" + end, null, "csv");
                try (CSVPrinter csvPrinter = CsvTools.csvPrinter(csvfile)) {
                    csvPrinter.printRecord(names);
                    Data2DRange reader = Data2DRange.create(this).setStart(start).setEnd(end);
                    reader.setIncludeRowNumber(includeRowNumber)
                            //                            .setCsvPrinter(csvPrinter)
                            .setCols(cols).setTask(task).start();
                } catch (Exception e) {
                    if (task != null) {
                        task.setError(e.toString());
                    }
                    MyBoxLog.error(e);
                    return null;
                }
                DataFileCSV dataFileCSV = new DataFileCSV();
                dataFileCSV.setTask(task);
                dataFileCSV.setColumns(targetColumns)
                        .setFile(csvfile)
                        .setCharset(Charset.forName("UTF-8"))
                        .setDelimiter(",")
                        .setHasHeader(true)
                        .setColsNumber(targetColumns.size())
                        .setRowsNumber(end - start + 1);
                dataFileCSV.saveAttributes();
                dataFileCSV.stopTask();
                files.add(dataFileCSV);
            }
            return files;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return null;
        }

    }

    public DataFileCSV rowExpression(String dname, String script, String name, boolean errorContinue,
            List<Integer> cols, boolean includeRowNumber, boolean includeColName) {
        if (cols == null || cols.isEmpty()) {
            return null;
        }
        File csvFile = tmpFile(dname, "RowExpression", "csv");
        Data2DOperate reader;
        List<Data2DColumn> targetColumns = targetColumns(cols, null, includeRowNumber, null);
        targetColumns.add(new Data2DColumn(name, ColumnDefinition.ColumnType.String));
        try (CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
            List<String> names = new ArrayList<>();
            for (Data2DColumn column : targetColumns) {
                names.add(column.getColumnName());
            }
            if (includeColName) {
                csvPrinter.printRecord(names);
            }
            reader = Data2DRowExpression.create(this)
                    .setScript(script).setName(name)
                    //                    .setCsvPrinter(csvPrinter)
                    .setIncludeRowNumber(includeRowNumber)
                    .setCols(cols).setTask(task).start();
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (reader != null && !reader.isFailed() && csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setColumns(targetColumns)
                    .setFile(csvFile).setDataName(dname)
                    .setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(includeColName)
                    .setColsNumber(targetColumns.size())
                    .setRowsNumber(reader.getSourceRowIndex());
            targetData.saveAttributes();
            return targetData;
        } else {
            return null;
        }
    }

    public List<Data2DColumn> makePercentageColumns(List<Integer> cols, List<Integer> otherCols, ObjectType objectType) {
        if (objectType == null) {
            objectType = ObjectType.Columns;
        }
        List<Data2DColumn> targetColumns;
        switch (objectType) {
            case Rows:
                targetColumns = targetColumns(cols, otherCols, true, message("PercentageInRow"));
                targetColumns.add(1, new Data2DColumn(message("Row") + "-" + message("Summation"),
                        ColumnDefinition.ColumnType.Double, 200));
                break;
            case All:
                targetColumns = targetColumns(cols, otherCols, true, message("PercentageInAll"));
                break;
            default:
                targetColumns = targetColumns(cols, otherCols, true, message("PercentageInColumn"));
        }
        targetColumns.get(0).setType(ColumnDefinition.ColumnType.String);
        for (int i = 0; i < cols.size(); i++) {
            targetColumns.get(i + 1).setType(ColumnDefinition.ColumnType.Double);
        }
        return fixColumnNames(targetColumns);
    }

    public DataFileCSV percentageColumns(String dname, List<Integer> cols, List<Integer> otherCols,
            int scale, String toNegative, InvalidAs invalidAs) {
        if (cols == null || cols.isEmpty()) {
            return null;
        }
        Data2DPrecentage reader = Data2DPrecentage.create(this)
                .setType(Data2DPrecentage.Type.ColumnsPass1)
                .setToNegative(toNegative);
        reader.setInvalidAs(invalidAs).setScale(scale)
                .setCols(cols).setTask(task).start();
        if (reader.isFailed()) {
            return null;
        }
        double[] colsSum = reader.getColValues();
        File csvFile = tmpFile(dname, "percentage", "csv");
        List<Data2DColumn> targetColumns = makePercentageColumns(cols, otherCols, ObjectType.Columns);
        try (CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
            List<String> names = new ArrayList<>();
            for (Data2DColumn column : targetColumns) {
                names.add(column.getColumnName());
            }
            csvPrinter.printRecord(names);
            List<String> row = new ArrayList<>();
            row.add(message("Column") + "-" + message("Summation"));
            for (int c = 0; c < cols.size(); c++) {
                row.add(DoubleTools.scale(colsSum[c], scale) + "");
            }
            if (otherCols != null) {
                for (int c : otherCols) {
                    row.add(null);
                }
            }
            csvPrinter.printRecord(row);
            reader = Data2DPrecentage.create(this)
                    .setType(Data2DPrecentage.Type.ColumnsPass2)
                    .setToNegative(toNegative)
                    .setColValues(colsSum);
            reader.setInvalidAs(invalidAs).setScale(scale)
                    //                    .setCsvPrinter(csvPrinter)
                    .setCols(cols).setOtherCols(otherCols).setTask(task).start();
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (!reader.isFailed() && csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setColumns(targetColumns)
                    .setFile(csvFile).setDataName(dname)
                    .setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(true)
                    .setColsNumber(targetColumns.size()).setRowsNumber(reader.getSourceRowIndex() + 1);
            return targetData;
        } else {
            return null;
        }
    }

    public DataFileCSV percentageAll(String dname, List<Integer> cols, List<Integer> otherCols,
            int scale, String toNegative, InvalidAs invalidAs) {
        if (cols == null || cols.isEmpty()) {
            return null;
        }
        Data2DPrecentage reader = Data2DPrecentage.create(this)
                .setType(Data2DPrecentage.Type.AllPass1)
                .setToNegative(toNegative);
        reader.setInvalidAs(invalidAs).setScale(scale)
                .setCols(cols).setTask(task).start();
        if (reader.isFailed()) {
            return null;
        }
        File csvFile = tmpFile(dname, "percentage", "csv");
        List<Data2DColumn> targetColumns = makePercentageColumns(cols, otherCols, ObjectType.All);
        try (CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
            List<String> names = new ArrayList<>();
            for (Data2DColumn column : targetColumns) {
                names.add(column.getColumnName());
            }
            csvPrinter.printRecord(names);
            List<String> row = new ArrayList<>();
            row.add(message("All") + "-" + message("Summation"));
            double sum = reader.gettValue();
            row.add(NumberTools.format(sum, scale));
            for (int c : cols) {
                row.add(null);
            }
            if (otherCols != null) {
                for (int c : otherCols) {
                    row.add(null);
                }
            }
            csvPrinter.printRecord(row);
            reader = Data2DPrecentage.create(this)
                    .setType(Data2DPrecentage.Type.AllPass2)
                    .setToNegative(toNegative).settValue(sum);
            reader.setInvalidAs(invalidAs).setScale(scale)
                    //                    .setCsvPrinter(csvPrinter)
                    .setCols(cols).setOtherCols(otherCols).setTask(task).start();
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (!reader.isFailed() && csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setColumns(targetColumns)
                    .setFile(csvFile).setDataName(dname)
                    .setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(true)
                    .setColsNumber(targetColumns.size()).setRowsNumber(reader.getSourceRowIndex() + 1);
            return targetData;
        } else {
            return null;
        }
    }

    public DataFileCSV percentageRows(String dname, List<Integer> cols, List<Integer> otherCols,
            int scale, String toNegative, InvalidAs invalidAs) {
        if (cols == null || cols.isEmpty()) {
            return null;
        }
        File csvFile = tmpFile(dname, "percentage", "csv");
        Data2DOperate reader;
        List<Data2DColumn> targetColumns = makePercentageColumns(cols, otherCols, ObjectType.Rows);
        try (CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
            List<String> names = new ArrayList<>();
            for (Data2DColumn column : targetColumns) {
                names.add(column.getColumnName());
            }
            csvPrinter.printRecord(names);
            reader = Data2DPrecentage.create(this)
                    .setType(Data2DPrecentage.Type.Rows)
                    .setToNegative(toNegative);
            reader.setInvalidAs(invalidAs).setScale(scale)
                    //                    .setCsvPrinter(csvPrinter)
                    .setCols(cols).setOtherCols(otherCols).setTask(task).start();
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (!reader.isFailed() && csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setColumns(targetColumns)
                    .setFile(csvFile).setDataName(dname)
                    .setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(true)
                    .setColsNumber(targetColumns.size()).setRowsNumber(reader.getSourceRowIndex());
            return targetData;
        } else {
            return null;
        }
    }

    public DataFileCSV frequency(String dname, Frequency frequency, String colName, int col, int scale) {
        if (frequency == null || colName == null || col < 0) {
            return null;
        }
        File csvFile = tmpFile(dname, "frequency", "csv");
        Data2DFrequency operator;
        try (CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
            List<String> row = new ArrayList<>();
            row.add(colName);
            row.add(colName + "_" + message("Count"));
            row.add(colName + "_" + message("CountPercentage"));
            csvPrinter.printRecord(row);

            operator = Data2DFrequency.create(this)
                    .setFrequency(frequency).setColIndex(col);
            operator
                    //                    .setCsvPrinter(csvPrinter)
                    .setScale(scale).setTask(task).start();

        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (!operator.isFailed() && csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setFile(csvFile).setDataName(dname)
                    .setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(true)
                    .setColsNumber(3).setRowsNumber(operator.getCount());
            return targetData;
        } else {
            return null;
        }
    }

    public DataFileCSV normalizeMinMaxColumns(String dname, List<Integer> cols, List<Integer> otherCols,
            double from, double to, boolean rowNumber, boolean colName, int scale, InvalidAs invalidAs) {
        if (cols == null || cols.isEmpty()) {
            return null;
        }
        int colLen = cols.size();
        DoubleStatistic[] sData = new DoubleStatistic[colLen];
        for (int c = 0; c < colLen; c++) {
            sData[c] = new DoubleStatistic();
            sData[c].invalidAs = invalidAs;
        }
        DescriptiveStatistic selections = DescriptiveStatistic.all(false)
                .add(StatisticType.Sum)
                .add(StatisticType.MaximumQ4)
                .add(StatisticType.MinimumQ0);
        Data2DOperate operator = Data2DStatistic.create(this)
                .setStatisticData(sData)
                .setStatisticCalculation(selections)
                .setType(Data2DStatistic.Type.ColumnsPass1)
                .setInvalidAs(invalidAs)
                .setCols(cols).setScale(scale).setTask(task).start();
        if (operator == null || operator.isFailed()) {
            return null;
        }
        for (int c = 0; c < colLen; c++) {
            double d = sData[c].maximum - sData[c].minimum;
            sData[c].dTmp = (to - from) / (d == 0 ? AppValues.TinyDouble : d);
        }
        File csvFile = tmpFile(dname, "normalizeMinMax", "csv");
        operator = null;
        List<Data2DColumn> targetColumns = targetColumns(cols, otherCols, rowNumber, message("Normalize"));
        try (CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
            List<String> names = new ArrayList<>();
            for (Data2DColumn column : targetColumns) {
                names.add(column.getColumnName());
            }
            if (colName) {
                csvPrinter.printRecord(names);
            }
            operator = Data2DNormalize.create(this)
                    .setType(Data2DNormalize.Type.MinMaxColumns)
                    .setStatisticData(sData).setFrom(from)
                    //                    .setCsvPrinter(csvPrinter)
                    .setInvalidAs(invalidAs).setIncludeRowNumber(rowNumber)
                    .setCols(cols).setOtherCols(otherCols)
                    .setScale(scale).setTask(task).start();
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (operator != null && !operator.isFailed() && csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setColumns(targetColumns)
                    .setFile(csvFile).setDataName(dname)
                    .setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(colName)
                    .setColsNumber(targetColumns.size()).setRowsNumber(operator.getSourceRowIndex());
            return targetData;
        } else {
            return null;
        }
    }

    public DataFileCSV normalizeSumColumns(String dname, List<Integer> cols, List<Integer> otherCols,
            boolean rowNumber, boolean colName, int scale, InvalidAs invalidAs) {
        if (cols == null || cols.isEmpty()) {
            return null;
        }
        int colLen = cols.size();
        DoubleStatistic[] sData = new DoubleStatistic[colLen];
        for (int c = 0; c < colLen; c++) {
            sData[c] = new DoubleStatistic();
            sData[c].invalidAs = invalidAs;
        }
        DescriptiveStatistic selections = DescriptiveStatistic.all(false);
        Data2DOperate operator = Data2DStatistic.create(this)
                .setType(Data2DStatistic.Type.ColumnsPass1)
                .setStatisticData(sData)
                .setStatisticCalculation(selections)
                .setSumAbs(true).setInvalidAs(invalidAs)
                .setCols(cols).setScale(scale).setTask(task).start();
        if (operator == null) {
            return null;
        }
        double[] colValues = new double[colLen];
        for (int c = 0; c < colLen; c++) {
            if (sData[c].sum == 0) {
                colValues[c] = 1d / AppValues.TinyDouble;
            } else {
                colValues[c] = 1d / sData[c].sum;
            }
        }
        File csvFile = tmpFile(dname, "normalizeSum", "csv");
        operator = null;
        List<Data2DColumn> targetColumns = targetColumns(cols, otherCols, rowNumber, message("Normalize"));
        try (CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
            List<String> names = new ArrayList<>();
            for (Data2DColumn column : targetColumns) {
                names.add(column.getColumnName());
            }
            if (colName) {
                csvPrinter.printRecord(names);
            }
            operator = Data2DNormalize.create(this)
                    .setType(Data2DNormalize.Type.SumColumns)
                    .setColValues(colValues)
                    //                    .setCsvPrinter(csvPrinter)
                    .setInvalidAs(invalidAs).setIncludeRowNumber(rowNumber)
                    .setCols(cols).setOtherCols(otherCols)
                    .setScale(scale).setTask(task).start();

        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (operator != null && !operator.isFailed() && csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setColumns(targetColumns)
                    .setFile(csvFile).setDataName(dname)
                    .setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(colName)
                    .setColsNumber(targetColumns.size()).setRowsNumber(operator.getSourceRowIndex());
            return targetData;
        } else {
            return null;
        }
    }

    public DataFileCSV normalizeZscoreColumns(String dname, List<Integer> cols, List<Integer> otherCols,
            boolean rowNumber, boolean colName, int scale, InvalidAs invalidAs) {
        if (cols == null || cols.isEmpty()) {
            return null;
        }
        int colLen = cols.size();
        DoubleStatistic[] sData = new DoubleStatistic[colLen];
        for (int c = 0; c < colLen; c++) {
            sData[c] = new DoubleStatistic();
            sData[c].invalidAs = invalidAs;
        }
        DescriptiveStatistic selections = DescriptiveStatistic.all(false)
                .add(StatisticType.PopulationStandardDeviation);
        Data2DOperate operator = Data2DStatistic.create(this)
                .setStatisticData(sData)
                .setStatisticCalculation(selections)
                .setType(Data2DStatistic.Type.ColumnsPass1)
                .setInvalidAs(invalidAs)
                .setCols(cols).setScale(scale).setTask(task).start();
        if (operator == null) {
            return null;
        }
        operator = Data2DStatistic.create(this)
                .setStatisticData(sData)
                .setStatisticCalculation(selections)
                .setType(Data2DStatistic.Type.ColumnsPass2)
                .setCols(cols).setScale(scale).setTask(task).start();
        if (operator == null) {
            return null;
        }
        File csvFile = tmpFile(dname, "normalizeZscore", "csv");
        operator = null;
        List<Data2DColumn> targetColumns = targetColumns(cols, otherCols, rowNumber, message("Normalize"));
        try (CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
            List<String> names = new ArrayList<>();
            for (Data2DColumn column : targetColumns) {
                names.add(column.getColumnName());
            }
            if (colName) {
                csvPrinter.printRecord(names);
            }
            operator = Data2DNormalize.create(this)
                    .setType(Data2DNormalize.Type.ZscoreColumns)
                    .setStatisticData(sData)
                    //                    .setCsvPrinter(csvPrinter)
                    .setInvalidAs(invalidAs).setIncludeRowNumber(rowNumber)
                    .setCols(cols).setOtherCols(otherCols)
                    .setScale(scale).setTask(task).start();

        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (operator != null && !operator.isFailed() && csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setColumns(targetColumns)
                    .setFile(csvFile).setDataName(dname)
                    .setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(colName)
                    .setColsNumber(targetColumns.size()).setRowsNumber(operator.getSourceRowIndex());
            return targetData;
        } else {
            return null;
        }
    }

    public DataFileCSV normalizeMinMaxAll(String dname, List<Integer> cols, List<Integer> otherCols,
            double from, double to, boolean rowNumber, boolean colName, int scale, InvalidAs invalidAs) {
        if (cols == null || cols.isEmpty()) {
            return null;
        }
        DoubleStatistic sData = new DoubleStatistic();
        sData.invalidAs = invalidAs;
        DescriptiveStatistic selections = DescriptiveStatistic.all(false)
                .add(StatisticType.Sum)
                .add(StatisticType.MaximumQ4)
                .add(StatisticType.MinimumQ0);
        Data2DOperate operator = Data2DStatistic.create(this)
                .setStatisticAll(sData)
                .setStatisticCalculation(selections)
                .setType(Data2DStatistic.Type.AllPass1)
                .setInvalidAs(invalidAs)
                .setCols(cols).setScale(scale).setTask(task).start();
        if (operator == null) {
            return null;
        }
        double d = sData.maximum - sData.minimum;
        sData.dTmp = (to - from) / (d == 0 ? AppValues.TinyDouble : d);
        File csvFile = tmpFile(dname, "normalizeMinMax", "csv");
        operator = null;
        List<Data2DColumn> targetColumns = targetColumns(cols, otherCols, rowNumber, message("Normalize"));
        try (CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
            List<String> names = new ArrayList<>();
            if (rowNumber) {
                names.add(message("RowNumber"));
            }
            for (int i = 0; i < columns.size(); i++) {
                if (cols.contains(i)) {
                    names.add(columns.get(i).getColumnName() + "_" + message("Normalize"));
                }
            }
            if (colName) {
                csvPrinter.printRecord(names);
            }
            operator = Data2DNormalize.create(this)
                    .setType(Data2DNormalize.Type.MinMaxAll)
                    .setStatisticAll(sData).setFrom(from)
                    //                    .setCsvPrinter(csvPrinter)   // #######
                    .setInvalidAs(invalidAs).setIncludeRowNumber(rowNumber)
                    .setCols(cols).setOtherCols(otherCols)
                    .setScale(scale).setTask(task).start();

        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (operator != null && !operator.isFailed() && csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setColumns(targetColumns)
                    .setFile(csvFile).setDataName(dname)
                    .setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(colName)
                    .setColsNumber(targetColumns.size()).setRowsNumber(operator.getSourceRowIndex());
            return targetData;
        } else {
            return null;
        }
    }

    public DataFileCSV normalizeSumAll(String dname, List<Integer> cols, List<Integer> otherCols,
            boolean rowNumber, boolean colName, int scale, InvalidAs invalidAs) {
        if (cols == null || cols.isEmpty()) {
            return null;
        }
        DoubleStatistic sData = new DoubleStatistic();
        sData.invalidAs = invalidAs;
        DescriptiveStatistic selections = DescriptiveStatistic.all(false);
        Data2DOperate operator = Data2DStatistic.create(this)
                .setStatisticAll(sData)
                .setStatisticCalculation(selections)
                .setType(Data2DStatistic.Type.AllPass1)
                .setSumAbs(true).setInvalidAs(invalidAs)
                .setCols(cols).setScale(scale).setTask(task).start();
        if (operator == null) {
            return null;
        }
        double k;
        if (sData.sum == 0) {
            k = 1d / AppValues.TinyDouble;
        } else {
            k = 1d / sData.sum;
        }
        File csvFile = tmpFile(dname, "normalizeSum", "csv");
        operator = null;
        List<Data2DColumn> targetColumns = targetColumns(cols, otherCols, rowNumber, message("Normalize"));
        try (CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
            List<String> names = new ArrayList<>();
            for (Data2DColumn column : targetColumns) {
                names.add(column.getColumnName());
            }
            if (colName) {
                csvPrinter.printRecord(names);
            }
            operator = Data2DNormalize.create(this)
                    .setType(Data2DNormalize.Type.SumAll)
                    .settValue(k)
                    //                    .setCsvPrinter(csvPrinter)
                    .setInvalidAs(invalidAs).setIncludeRowNumber(rowNumber)
                    .setCols(cols).setOtherCols(otherCols)
                    .setScale(scale).setTask(task).start();

        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (operator != null && !operator.isFailed() && csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setColumns(targetColumns)
                    .setFile(csvFile).setDataName(dname)
                    .setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(colName)
                    .setColsNumber(targetColumns.size()).setRowsNumber(operator.getSourceRowIndex());
            return targetData;
        } else {
            return null;
        }
    }

    public DataFileCSV normalizeZscoreAll(String dname, List<Integer> cols, List<Integer> otherCols,
            boolean rowNumber, boolean colName, int scale, InvalidAs invalidAs) {
        if (cols == null || cols.isEmpty()) {
            return null;
        }
        DoubleStatistic sData = new DoubleStatistic();
        sData.invalidAs = invalidAs;
        DescriptiveStatistic selections = DescriptiveStatistic.all(false)
                .add(StatisticType.PopulationStandardDeviation);
        Data2DOperate operator = Data2DStatistic.create(this)
                .setStatisticAll(sData)
                .setStatisticCalculation(selections)
                .setType(Data2DStatistic.Type.AllPass1)
                .setInvalidAs(invalidAs)
                .setCols(cols).setScale(scale).setTask(task).start();
        if (operator == null) {
            return null;
        }
        operator = Data2DStatistic.create(this)
                .setStatisticAll(sData)
                .setStatisticCalculation(selections)
                .setType(Data2DStatistic.Type.AllPass2)
                .setInvalidAs(invalidAs)
                .setCols(cols).setScale(scale).setTask(task).start();
        if (operator == null) {
            return null;
        }
        File csvFile = tmpFile(dname, "normalizeZscore", "csv");
        operator = null;
        List<Data2DColumn> targetColumns = targetColumns(cols, otherCols, rowNumber, message("Normalize"));
        try (CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
            List<String> names = new ArrayList<>();
            for (Data2DColumn column : targetColumns) {
                names.add(column.getColumnName());
            }
            if (colName) {
                csvPrinter.printRecord(names);
            }
            operator = Data2DNormalize.create(this)
                    .setType(Data2DNormalize.Type.ZscoreAll)
                    .setStatisticAll(sData)
                    //                    .setCsvPrinter(csvPrinter)
                    .setInvalidAs(invalidAs).setIncludeRowNumber(rowNumber)
                    .setCols(cols).setOtherCols(otherCols)
                    .setScale(scale).setTask(task).start();

        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (operator != null && !operator.isFailed() && csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setColumns(targetColumns)
                    .setFile(csvFile).setDataName(dname)
                    .setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(colName)
                    .setColsNumber(targetColumns.size()).setRowsNumber(operator.getSourceRowIndex());
            return targetData;
        } else {
            return null;
        }
    }

    public DataFileCSV normalizeRows(String dname, Normalization.Algorithm a,
            List<Integer> cols, List<Integer> otherCols,
            double from, double to, boolean rowNumber, boolean colName, int scale, InvalidAs invalidAs) {
        if (cols == null || cols.isEmpty()) {
            return null;
        }
        File csvFile = tmpFile(dname, "normalizeSum", "csv");
        Data2DOperate operator = null;
        List<Data2DColumn> targetColumns = targetColumns(cols, otherCols, rowNumber, message("Normalize"));
        try (CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
            List<String> names = new ArrayList<>();
            for (Data2DColumn column : targetColumns) {
                names.add(column.getColumnName());
            }
            if (colName) {
                csvPrinter.printRecord(names);
            }
            operator = Data2DNormalize.create(this)
                    .setType(Data2DNormalize.Type.Rows)
                    .setA(a).setFrom(from).setTo(to)
                    //                    .setCsvPrinter(csvPrinter)
                    .setInvalidAs(invalidAs).setIncludeRowNumber(rowNumber)
                    .setCols(cols).setOtherCols(otherCols)
                    .setScale(scale).setTask(task).start();
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (operator != null && !operator.isFailed() && csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setColumns(targetColumns)
                    .setFile(csvFile).setDataName(dname)
                    .setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(colName)
                    .setColsNumber(targetColumns.size()).setRowsNumber(operator.getSourceRowIndex());
            return targetData;
        } else {
            return null;
        }
    }

    public DataFileCSV simpleLinearRegression(String dname, List<Integer> cols,
            SimpleLinearRegression simpleRegression, boolean writeFile) {
        if (cols == null || cols.isEmpty() || simpleRegression == null) {
            return null;
        }
        if (writeFile) {
            File csvFile = tmpFile(dname, "simpleLinearRegression", "csv");
            int tcolsNumber = 0;
            Data2DOperate operator = null;
            try (CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
                List<String> names = new ArrayList<>();
                List<Data2DColumn> resultColumns = simpleRegression.getColumns();
                for (Data2DColumn c : resultColumns) {
                    names.add(c.getColumnName());
                }
                csvPrinter.printRecord(names);
                tcolsNumber = names.size();

                operator = Data2DSimpleLinearRegression.create(this)
                        .setSimpleRegression(simpleRegression)
                        //                        .setCsvPrinter(csvPrinter)
                        .setCols(cols).setScale(scale).setTask(task).start();

            } catch (Exception e) {
                if (task != null) {
                    task.setError(e.toString());
                }
                MyBoxLog.error(e);
                return null;
            }
            if (operator != null && !operator.isFailed() && csvFile != null && csvFile.exists()) {
                DataFileCSV targetData = new DataFileCSV();
                targetData.setFile(csvFile).setDataName(dname)
                        .setCharset(Charset.forName("UTF-8"))
                        .setDelimiter(",").setHasHeader(true)
                        .setColsNumber(tcolsNumber).setRowsNumber(operator.getSourceRowIndex());
                return targetData;
            } else {
                return null;
            }
        } else {
            Data2DSimpleLinearRegression.create(this)
                    .setSimpleRegression(simpleRegression)
                    .setCols(cols).setScale(scale).setTask(task).start();
            return null;
        }
    }

}
