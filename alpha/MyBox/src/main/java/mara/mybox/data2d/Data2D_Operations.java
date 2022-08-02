package mara.mybox.data2d;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.calculation.DescriptiveStatistic;
import mara.mybox.calculation.DoubleStatistic;
import mara.mybox.calculation.Normalization;
import mara.mybox.calculation.SimpleLinearRegression;
import mara.mybox.controller.ControlDataConvert;
import mara.mybox.data2d.scan.Data2DReader;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ExpressionCalculator;
import mara.mybox.tools.CsvTools;
import mara.mybox.tools.DoubleTools;
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

    public boolean export(ControlDataConvert convertController, List<Integer> cols) {
        if (convertController == null || cols == null || cols.isEmpty()) {
            return false;
        }
        Data2DReader reader = Data2DReader.create(this)
                .setConvertController(convertController).setCols(cols)
                .setReaderTask(task).start(Data2DReader.Operation.Export);
        return reader != null && !reader.isFailed();
    }

    public List<List<String>> allRows(List<Integer> cols, boolean rowNumber) {
        if (cols == null || cols.isEmpty()) {
            return null;
        }
        Data2DReader reader = Data2DReader.create(this)
                .setCols(cols).setIncludeRowNumber(rowNumber)
                .setReaderTask(task).start(Data2DReader.Operation.ReadCols);
        if (reader == null) {
            return null;
        }
        return reader.getRows();
    }

    public List<List<String>> allRows(boolean rowNumber) {
        Data2DReader reader = Data2DReader.create(this)
                .setIncludeRowNumber(rowNumber)
                .setReaderTask(task).start(Data2DReader.Operation.ReadRows);
        if (reader == null) {
            return null;
        }
        return reader.getRows();
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
                DoubleStatistic colStatistic = column.getDoubleStatistic();
                if (colStatistic == null) {
                    colStatistic = new DoubleStatistic();
                    column.setDoubleStatistic(colStatistic);
                }
                colStatistic.invalidAs = selections.invalidAs;
                sData[c] = colStatistic;
            }
            Data2DReader reader = Data2DReader.create(this)
                    .setStatisticData(sData).setCols(cols)
                    .setScanPass(1).setStatisticSelection(selections)
                    .setReaderTask(task).start(Data2DReader.Operation.StatisticColumns);
            if (reader == null) {
                return null;
            }
            if (selections.isPopulationStandardDeviation() || selections.isPopulationVariance()
                    || selections.isSampleStandardDeviation() || selections.isSampleVariance()) {
                reader = Data2DReader.create(this)
                        .setStatisticData(sData).setCols(cols)
                        .setScanPass(2).setStatisticSelection(selections)
                        .setReaderTask(task).start(Data2DReader.Operation.StatisticColumns);
                if (reader == null) {
                    return null;
                }
            }
            return sData;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    // percentile or mode
    public DoubleStatistic[] statisticByColumnsForStored(List<Integer> cols, DescriptiveStatistic selections) {
        try {
            if (cols == null || cols.isEmpty() || selections == null) {
                return null;
            }
            DataTable tmpTable = ((Data2D) this).toTmpTable(task, cols, false, true);
            if (tmpTable == null) {
                return null;
            }
            tmpTable.setTask(task);
            List<Integer> tmpColIndices = tmpTable.columnIndices().subList(1, tmpTable.columnsNumber());
            DoubleStatistic[] statisticData = tmpTable.statisticByColumnsForStored(tmpColIndices, selections);
            if (statisticData == null) {
                return null;
            }
            for (int i = 0; i < cols.size(); i++) {
                Data2DColumn column = this.column(cols.get(i));
                column.setDoubleStatistic(statisticData[i]);
            }
            tmpTable.drop();
            return statisticData;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public DataFileCSV statisticByRows(List<String> names, List<Integer> cols, DescriptiveStatistic selections) {
        if (names == null || names.isEmpty() || cols == null || cols.isEmpty()) {
            return null;
        }
        File csvFile = tmpCSV("statistic");
        Data2DReader reader = null;
        try ( CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
            csvPrinter.printRecord(names);
            reader = Data2DReader.create(this)
                    .setCsvPrinter(csvPrinter).setCols(cols).setStatisticSelection(selections)
                    .setReaderTask(task).start(Data2DReader.Operation.StatisticRows);
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (reader != null && csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setFile(csvFile).setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(true)
                    .setColsNumber(names.size()).setRowsNumber(reader.getRowIndex());
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
        Data2DReader reader = Data2DReader.create(this)
                .setStatisticAll(sData).setCols(cols)
                .setScanPass(1).setStatisticSelection(selections)
                .setReaderTask(task).start(Data2DReader.Operation.StatisticAll);
        if (reader == null) {
            return null;
        }
        if (selections.isPopulationStandardDeviation() || selections.isPopulationVariance()
                || selections.isSampleStandardDeviation() || selections.isSampleVariance()) {
            reader = Data2DReader.create(this)
                    .setStatisticAll(sData).setCols(cols)
                    .setScanPass(2).setStatisticSelection(selections)
                    .setReaderTask(task).start(Data2DReader.Operation.StatisticAll);
            if (reader == null) {
                return null;
            }
        }
        return sData;
    }

    public DataFileCSV copy(List<Integer> cols, boolean includeRowNumber, boolean includeColName) {
        if (cols == null || cols.isEmpty()) {
            return null;
        }
        File csvFile = tmpCSV("copy");
        Data2DReader reader;
        List<Data2DColumn> targetColumns = new ArrayList<>();
        try ( CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
            List<String> names = new ArrayList<>();
            if (includeRowNumber) {
                names.add(message("RowNumber"));
                targetColumns.add(new Data2DColumn(message("SourceRowNumber"), ColumnDefinition.ColumnType.Long));
            }
            for (int i = 0; i < columns.size(); i++) {
                if (cols.contains(i)) {
                    names.add(columns.get(i).getColumnName());
                    targetColumns.add(columns.get(i).cloneAll().setD2cid(-1).setD2id(-1));
                }
            }
            if (includeColName) {
                csvPrinter.printRecord(names);
            }
            reader = Data2DReader.create(this)
                    .setCsvPrinter(csvPrinter).setCols(cols).setIncludeRowNumber(includeRowNumber)
                    .setReaderTask(task).start(Data2DReader.Operation.Copy);
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (reader != null && csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setColumns(targetColumns)
                    .setFile(csvFile).setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(includeColName)
                    .setColsNumber(targetColumns.size())
                    .setRowsNumber(reader.getRowIndex());
            targetData.saveAttributes();
            return targetData;
        } else {
            return null;
        }
    }

    public DataFileCSV rowExpression(ExpressionCalculator calculator,
            String script, String name, boolean errorContinue,
            List<Integer> cols, boolean includeRowNumber, boolean includeColName) {
        if (cols == null || cols.isEmpty()) {
            return null;
        }
        File csvFile = tmpCSV("RowExpression");
        Data2DReader reader;
        List<Data2DColumn> targetColumns = new ArrayList<>();
        try ( CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
            List<String> names = new ArrayList<>();
            if (includeRowNumber) {
                names.add(message("RowNumber"));
                targetColumns.add(new Data2DColumn(message("SourceRowNumber"), ColumnDefinition.ColumnType.Long));
            }
            for (int i = 0; i < columns.size(); i++) {
                if (cols.contains(i)) {
                    names.add(columns.get(i).getColumnName());
                    targetColumns.add(columns.get(i).cloneAll().setD2cid(-1).setD2id(-1));
                }
            }
            names.add(name);
            targetColumns.add(new Data2DColumn(name, ColumnDefinition.ColumnType.String));
            if (includeColName) {
                csvPrinter.printRecord(names);
            }
            reader = Data2DReader.create(this)
                    .setCsvPrinter(csvPrinter).setCols(cols).setIncludeRowNumber(includeRowNumber)
                    .setScript(script).setName(name).setCalculator(calculator)
                    .setReaderTask(task).start(Data2DReader.Operation.RowExpression);
            calculator.stop();
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (reader != null && csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setColumns(targetColumns)
                    .setFile(csvFile).setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(includeColName)
                    .setColsNumber(targetColumns.size())
                    .setRowsNumber(reader.getRowIndex());
            targetData.saveAttributes();
            return targetData;
        } else {
            return null;
        }
    }

    public DataFileCSV percentageColumns(List<String> names, List<Integer> cols,
            int scale, boolean withValues, String toNegative, double invalidAs) {
        if (cols == null || cols.isEmpty()) {
            return null;
        }
        Data2DReader reader = Data2DReader.create(this)
                .setCols(cols).setToNegative(toNegative).setInvalidAs(invalidAs)
                .setScanPass(1).setScale(scale)
                .setReaderTask(task).start(Data2DReader.Operation.PercentageColumns);
        if (reader == null) {
            return null;
        }
        double[] colsSum = reader.getColValues();
        File csvFile = tmpCSV("percentage");
        try ( CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
            csvPrinter.printRecord(names);
            List<String> row = new ArrayList<>();
            row.add(message("Column") + "-" + message("Summation"));
            for (int c = 0; c < cols.size(); c++) {
                row.add(DoubleTools.scale(colsSum[c], scale) + "");
                if (withValues) {
                    row.add("100");
                }
            }
            csvPrinter.printRecord(row);

            reader = Data2DReader.create(this)
                    .setCsvPrinter(csvPrinter).setColValues(colsSum).setCols(cols)
                    .setWithValues(withValues).setToNegative(toNegative).setInvalidAs(invalidAs)
                    .setScanPass(2).setScale(scale)
                    .setReaderTask(task).start(Data2DReader.Operation.PercentageColumns);

        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (reader != null && csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setFile(csvFile).setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(true)
                    .setColsNumber(names.size()).setRowsNumber(reader.getRowIndex() + 1);
            return targetData;
        } else {
            return null;
        }
    }

    public DataFileCSV percentageAll(List<String> names, List<Integer> cols,
            int scale, boolean withValues, String toNegative, double invalidAs) {
        if (cols == null || cols.isEmpty()) {
            return null;
        }
        Data2DReader reader = Data2DReader.create(this)
                .setCols(cols).setToNegative(toNegative).setInvalidAs(invalidAs)
                .setScanPass(1).setScale(scale)
                .setReaderTask(task).start(Data2DReader.Operation.PercentageAll);
        if (reader == null) {
            return null;
        }
        File csvFile = tmpCSV("percentage");
        try ( CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
            csvPrinter.printRecord(names);
            List<String> row = new ArrayList<>();
            row.add(message("All") + "-" + message("Summation"));
            double sum = reader.gettValue();
            row.add(DoubleTools.format(sum, scale));
            if (withValues) {
                row.add("100");
            }
            for (int c : cols) {
                row.add(null);
                if (withValues) {
                    row.add(null);
                }
            }
            csvPrinter.printRecord(row);

            reader = Data2DReader.create(this)
                    .setCsvPrinter(csvPrinter).setCols(cols)
                    .setWithValues(withValues).setToNegative(toNegative).setInvalidAs(invalidAs)
                    .setValue(sum).setScanPass(2).setScale(scale)
                    .setReaderTask(task).start(Data2DReader.Operation.PercentageAll);

        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (reader != null && csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setFile(csvFile).setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(true)
                    .setColsNumber(names.size()).setRowsNumber(reader.getRowIndex() + 1);
            return targetData;
        } else {
            return null;
        }
    }

    public DataFileCSV percentageRows(List<String> names, List<Integer> cols,
            int scale, boolean withValues, String toNegative, double invalidAs) {
        if (cols == null || cols.isEmpty()) {
            return null;
        }
        File csvFile = tmpCSV("percentage");
        Data2DReader reader = null;
        try ( CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
            csvPrinter.printRecord(names);
            reader = Data2DReader.create(this)
                    .setCsvPrinter(csvPrinter).setCols(cols).setScale(scale)
                    .setWithValues(withValues).setToNegative(toNegative).setInvalidAs(invalidAs)
                    .setReaderTask(task).start(Data2DReader.Operation.PercentageRows);

        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (reader != null && csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setFile(csvFile).setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(true)
                    .setColsNumber(names.size()).setRowsNumber(reader.getRowIndex());
            return targetData;
        } else {
            return null;
        }
    }

    public DataFileCSV frequency(Frequency frequency, String colName, int col, int scale) {
        if (frequency == null || colName == null || col < 0) {
            return null;
        }
        File csvFile = tmpCSV("frequency");
        Data2DReader reader;
        try ( CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
            List<String> row = new ArrayList<>();
            row.add(colName);
            row.add(colName + "_" + message("Count"));
            row.add(colName + "_" + message("CountPercentage"));
            csvPrinter.printRecord(row);

            reader = Data2DReader.create(this)
                    .setCsvPrinter(csvPrinter).setFrequency(frequency)
                    .setCol(col).setScale(scale)
                    .setReaderTask(task).start(Data2DReader.Operation.Frequency);

        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (reader != null && csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setFile(csvFile).setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(true)
                    .setColsNumber(3).setRowsNumber(reader.getCount());
            return targetData;
        } else {
            return null;
        }
    }

    public DataFileCSV normalizeMinMaxColumns(List<Integer> cols, double from, double to,
            boolean rowNumber, boolean colName, int scale, double invalidAs) {
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
                .setSum(true).setMaximum(true).setMinimum(true);
        Data2DReader reader = Data2DReader.create(this).setCols(cols).setScale(scale)
                .setStatisticData(sData).setStatisticSelection(selections).setScanPass(1)
                .setReaderTask(task).start(Data2DReader.Operation.StatisticColumns);
        if (reader == null) {
            return null;
        }
        for (int c = 0; c < colLen; c++) {
            double d = sData[c].maximum - sData[c].minimum;
            sData[c].dTmp = (to - from) / (d == 0 ? AppValues.TinyDouble : d);
        }
        File csvFile = tmpCSV("normalizeMinMax");
        int tcolsNumber = 0;
        try ( CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
            List<String> names = new ArrayList<>();
            if (rowNumber) {
                names.add(message("RowNumber"));
            }
            for (int i = 0; i < columns.size(); i++) {
                if (cols.contains(i)) {
                    names.add(columns.get(i).getColumnName());
                }
            }
            if (colName) {
                csvPrinter.printRecord(names);
            }
            tcolsNumber = names.size();

            reader = Data2DReader.create(this)
                    .setStatisticData(sData).setCols(cols).setCsvPrinter(csvPrinter)
                    .setIncludeRowNumber(rowNumber).setFrom(from).setScale(scale).setInvalidAs(invalidAs)
                    .setReaderTask(task).start(Data2DReader.Operation.NormalizeMinMaxColumns);

        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (reader != null && csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setFile(csvFile).setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(colName)
                    .setColsNumber(tcolsNumber).setRowsNumber(reader.getRowIndex());
            return targetData;
        } else {
            return null;
        }
    }

    public DataFileCSV normalizeSumColumns(List<Integer> cols,
            boolean rowNumber, boolean colName, int scale, double invalidAs) {
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
        Data2DReader reader = Data2DReader.create(this).setCols(cols).setSumAbs(true)
                .setStatisticData(sData).setStatisticSelection(selections)
                .setScanPass(1).setScale(scale)
                .setReaderTask(task).start(Data2DReader.Operation.StatisticColumns);
        if (reader == null) {
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
        File csvFile = tmpCSV("normalizeSum");
        int tcolsNumber = 0;
        try ( CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
            List<String> names = new ArrayList<>();
            if (rowNumber) {
                names.add(message("RowNumber"));
            }
            for (int i = 0; i < columns.size(); i++) {
                if (cols.contains(i)) {
                    names.add(columns.get(i).getColumnName());
                }
            }
            if (colName) {
                csvPrinter.printRecord(names);
            }
            tcolsNumber = names.size();

            reader = Data2DReader.create(this)
                    .setColValues(colValues).setCols(cols).setCsvPrinter(csvPrinter)
                    .setIncludeRowNumber(rowNumber).setScale(scale).setInvalidAs(invalidAs)
                    .setReaderTask(task).start(Data2DReader.Operation.NormalizeSumColumns);

        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (reader != null && csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setFile(csvFile).setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(colName)
                    .setColsNumber(tcolsNumber).setRowsNumber(reader.getRowIndex());
            return targetData;
        } else {
            return null;
        }
    }

    public DataFileCSV normalizeZscoreColumns(List<Integer> cols,
            boolean rowNumber, boolean colName, int scale, double invalidAs) {
        if (cols == null || cols.isEmpty()) {
            return null;
        }
        int colLen = cols.size();
        int tcolsNumber = 0;
        DoubleStatistic[] sData = new DoubleStatistic[colLen];
        for (int c = 0; c < colLen; c++) {
            sData[c] = new DoubleStatistic();
            sData[c].invalidAs = invalidAs;
        }
        DescriptiveStatistic selections = DescriptiveStatistic.all(false)
                .setPopulationStandardDeviation(true);
        Data2DReader reader = Data2DReader.create(this).setCols(cols)
                .setStatisticData(sData).setStatisticSelection(selections)
                .setScanPass(1).setScale(scale)
                .setReaderTask(task).start(Data2DReader.Operation.StatisticColumns);
        if (reader == null) {
            return null;
        }
        reader = Data2DReader.create(this).setCols(cols)
                .setStatisticData(sData).setStatisticSelection(selections)
                .setScanPass(2).setScale(scale)
                .setReaderTask(task).start(Data2DReader.Operation.StatisticColumns);
        if (reader == null) {
            return null;
        }
        File csvFile = tmpCSV("normalizeZscore");
        try ( CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
            List<String> names = new ArrayList<>();
            if (rowNumber) {
                names.add(message("RowNumber"));
            }
            for (int i = 0; i < columns.size(); i++) {
                if (cols.contains(i)) {
                    names.add(columns.get(i).getColumnName());
                }
            }
            if (colName) {
                csvPrinter.printRecord(names);
            }
            tcolsNumber = names.size();

            reader = Data2DReader.create(this)
                    .setStatisticData(sData).setCols(cols).setCsvPrinter(csvPrinter)
                    .setIncludeRowNumber(rowNumber).setScale(scale).setInvalidAs(invalidAs)
                    .setReaderTask(task).start(Data2DReader.Operation.NormalizeZscoreColumns);

        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (reader != null && csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setFile(csvFile).setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(colName)
                    .setColsNumber(tcolsNumber).setRowsNumber(reader.getRowIndex());
            return targetData;
        } else {
            return null;
        }
    }

    public DataFileCSV normalizeMinMaxAll(List<Integer> cols, double from, double to,
            boolean rowNumber, boolean colName, int scale, double invalidAs) {
        if (cols == null || cols.isEmpty()) {
            return null;
        }
        DoubleStatistic sData = new DoubleStatistic();
        sData.invalidAs = invalidAs;
        DescriptiveStatistic selections = DescriptiveStatistic.all(false)
                .setSum(true).setMaximum(true).setMinimum(true);
        Data2DReader reader = Data2DReader.create(this)
                .setStatisticAll(sData).setCols(cols)
                .setScanPass(1).setStatisticSelection(selections)
                .setReaderTask(task).start(Data2DReader.Operation.StatisticAll);
        if (reader == null) {
            return null;
        }
        double d = sData.maximum - sData.minimum;
        sData.dTmp = (to - from) / (d == 0 ? AppValues.TinyDouble : d);
        File csvFile = tmpCSV("normalizeMinMax");
        int tcolsNumber = 0;
        try ( CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
            List<String> names = new ArrayList<>();
            if (rowNumber) {
                names.add(message("RowNumber"));
            }
            for (int i = 0; i < columns.size(); i++) {
                if (cols.contains(i)) {
                    names.add(columns.get(i).getColumnName());
                }
            }
            if (colName) {
                csvPrinter.printRecord(names);
            }
            tcolsNumber = names.size();

            reader = Data2DReader.create(this)
                    .setStatisticAll(sData).setCols(cols).setCsvPrinter(csvPrinter)
                    .setIncludeRowNumber(rowNumber).setFrom(from).setScale(scale).setInvalidAs(invalidAs)
                    .setReaderTask(task).start(Data2DReader.Operation.NormalizeMinMaxAll);

        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (reader != null && csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setFile(csvFile).setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(colName)
                    .setColsNumber(tcolsNumber).setRowsNumber(reader.getRowIndex());
            return targetData;
        } else {
            return null;
        }
    }

    public DataFileCSV normalizeSumAll(List<Integer> cols,
            boolean rowNumber, boolean colName, int scale, double invalidAs) {
        if (cols == null || cols.isEmpty()) {
            return null;
        }
        DoubleStatistic sData = new DoubleStatistic();
        sData.invalidAs = invalidAs;
        DescriptiveStatistic selections = DescriptiveStatistic.all(false);
        Data2DReader reader = Data2DReader.create(this).setCols(cols).setSumAbs(true)
                .setStatisticAll(sData).setStatisticSelection(selections)
                .setScanPass(1).setScale(scale)
                .setReaderTask(task).start(Data2DReader.Operation.StatisticAll);
        if (reader == null) {
            return null;
        }
        double k;
        if (sData.sum == 0) {
            k = 1d / AppValues.TinyDouble;
        } else {
            k = 1d / sData.sum;
        }
        File csvFile = tmpCSV("normalizeSum");
        int tcolsNumber = 0;
        try ( CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
            List<String> names = new ArrayList<>();
            if (rowNumber) {
                names.add(message("RowNumber"));
            }
            for (int i = 0; i < columns.size(); i++) {
                if (cols.contains(i)) {
                    names.add(columns.get(i).getColumnName());
                }
            }
            if (colName) {
                csvPrinter.printRecord(names);
            }
            tcolsNumber = names.size();

            reader = Data2DReader.create(this)
                    .setValue(k).setCols(cols).setCsvPrinter(csvPrinter)
                    .setIncludeRowNumber(rowNumber).setScale(scale).setInvalidAs(invalidAs)
                    .setReaderTask(task).start(Data2DReader.Operation.NormalizeSumAll);

        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (reader != null && csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setFile(csvFile).setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(colName)
                    .setColsNumber(tcolsNumber).setRowsNumber(reader.getRowIndex());
            return targetData;
        } else {
            return null;
        }
    }

    public DataFileCSV normalizeZscoreAll(List<Integer> cols,
            boolean rowNumber, boolean colName, int scale, double invalidAs) {
        if (cols == null || cols.isEmpty()) {
            return null;
        }
        int tcolsNumber = 0;
        DoubleStatistic sData = new DoubleStatistic();
        sData.invalidAs = invalidAs;
        DescriptiveStatistic selections = DescriptiveStatistic.all(false)
                .setPopulationStandardDeviation(true);
        Data2DReader reader = Data2DReader.create(this).setCols(cols)
                .setStatisticAll(sData).setStatisticSelection(selections)
                .setScanPass(1).setScale(scale)
                .setReaderTask(task).start(Data2DReader.Operation.StatisticAll);
        if (reader == null) {
            return null;
        }
        reader = Data2DReader.create(this).setCols(cols)
                .setStatisticAll(sData).setStatisticSelection(selections)
                .setScanPass(2).setScale(scale)
                .setReaderTask(task).start(Data2DReader.Operation.StatisticAll);
        if (reader == null) {
            return null;
        }
        File csvFile = tmpCSV("normalizeZscore");
        try ( CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
            List<String> names = new ArrayList<>();
            if (rowNumber) {
                names.add(message("RowNumber"));
            }
            for (int i = 0; i < columns.size(); i++) {
                if (cols.contains(i)) {
                    names.add(columns.get(i).getColumnName());
                }
            }
            if (colName) {
                csvPrinter.printRecord(names);
            }
            tcolsNumber = names.size();

            reader = Data2DReader.create(this)
                    .setStatisticAll(sData).setCols(cols).setCsvPrinter(csvPrinter)
                    .setIncludeRowNumber(rowNumber).setScale(scale).setInvalidAs(invalidAs)
                    .setReaderTask(task).start(Data2DReader.Operation.NormalizeZscoreAll);

        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (reader != null && csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setFile(csvFile).setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(colName)
                    .setColsNumber(tcolsNumber).setRowsNumber(reader.getRowIndex());
            return targetData;
        } else {
            return null;
        }
    }

    public DataFileCSV normalizeRows(Normalization.Algorithm a, List<Integer> cols,
            double from, double to, boolean rowNumber, boolean colName, int scale, double invalidAs) {
        if (cols == null || cols.isEmpty()) {
            return null;
        }
        File csvFile = tmpCSV("normalizeSum");
        int tcolsNumber = 0;
        Data2DReader reader = null;
        try ( CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
            List<String> names = new ArrayList<>();
            if (rowNumber) {
                names.add(message("RowNumber"));
            }
            for (int i = 0; i < columns.size(); i++) {
                if (cols.contains(i)) {
                    names.add(columns.get(i).getColumnName());
                }
            }
            if (colName) {
                csvPrinter.printRecord(names);
            }
            tcolsNumber = names.size();

            reader = Data2DReader.create(this)
                    .setCols(cols).setCsvPrinter(csvPrinter)
                    .setIncludeRowNumber(rowNumber).setScale(scale).setInvalidAs(invalidAs)
                    .setFrom(from).setTo(to)
                    .setReaderTask(task);
            switch (a) {
                case Sum:
                    reader.start(Data2DReader.Operation.NormalizeSumRows);
                    break;
                case ZScore:
                    reader.start(Data2DReader.Operation.NormalizeZscoreRows);
                    break;
                case MinMax:
                    reader.start(Data2DReader.Operation.NormalizeMinMaxRows);
                    break;
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (reader != null && csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setFile(csvFile).setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(colName)
                    .setColsNumber(tcolsNumber).setRowsNumber(reader.getRowIndex());
            return targetData;
        } else {
            return null;
        }
    }

    public DataFileCSV simpleLinearRegression(List<Integer> cols, SimpleLinearRegression simpleRegression) {
        if (cols == null || cols.isEmpty() || simpleRegression == null) {
            return null;
        }
        File csvFile = tmpCSV("simpleLinearRegression");
        int tcolsNumber = 0;
        Data2DReader reader = null;
        try ( CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
            List<String> names = new ArrayList<>();
            List<Data2DColumn> resultColumns = simpleRegression.getColumns();
            for (Data2DColumn c : resultColumns) {
                names.add(c.getColumnName());
            }
            csvPrinter.printRecord(names);
            tcolsNumber = names.size();

            reader = Data2DReader.create(this)
                    .setCols(cols).setSimpleRegression(simpleRegression).setCsvPrinter(csvPrinter)
                    .setReaderTask(task).start(Data2DReader.Operation.SimpleLinearRegression);

        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (reader != null && csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setFile(csvFile).setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(true)
                    .setColsNumber(tcolsNumber).setRowsNumber(reader.getRowIndex());
            return targetData;
        } else {
            return null;
        }
    }

}
