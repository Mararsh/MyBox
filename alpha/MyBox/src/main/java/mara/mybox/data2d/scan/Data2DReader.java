package mara.mybox.data2d.scan;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import mara.mybox.calculation.DescriptiveStatistic;
import mara.mybox.calculation.DoubleStatistic;
import mara.mybox.calculation.Normalization;
import mara.mybox.calculation.SimpleLinearRegression;
import mara.mybox.controller.ControlDataConvert;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.Data2D_Edit;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataFileExcel;
import mara.mybox.data2d.DataFileText;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DRow;
import mara.mybox.db.table.TableData2D;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppValues;
import static mara.mybox.value.Languages.message;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.math3.stat.Frequency;
import org.apache.commons.math3.stat.descriptive.moment.Skewness;
import org.apache.commons.math3.stat.regression.SimpleRegression;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public abstract class Data2DReader {

    protected Data2D data2D;
    protected File readerFile;
    protected Operation operation;
    protected long rowIndex, rowsStart, rowsEnd, count;
    protected int columnsNumber, colsLen, scale = -1, scanPass, colIndex;
    protected List<String> record, names;
    protected List<List<String>> rows = new ArrayList<>();
    protected List<Integer> cols;
    protected boolean includeRowNumber, includeColName, withValues, failed, sumAbs;
    protected double from, to, tValue;
    protected double[] colValues;
    protected ControlDataConvert convertController;
    protected Connection conn;
    protected DataTable dataTable;
    protected TableData2D tableData2D;
    protected DoubleStatistic[] statisticData;
    protected List<Skewness> skewnessList;
    protected DoubleStatistic statisticAll;
    protected String categoryName;
    protected Skewness skewnessAll;
    protected DescriptiveStatistic statisticCalculation;
    protected Frequency frequency;
    protected SimpleLinearRegression simpleRegression;
    protected CSVPrinter csvPrinter;
    protected boolean readerHasHeader, readerStopped, needCheckTask;
    protected SingletonTask readerTask;

    public static enum Operation {
        ReadDefinition, ReadTotal, ReadColumnNames, ReadPage,
        ReadCols, ReadRows, Export, WriteTable, Copy, SingleColumn,
        StatisticColumns, StatisticRows, StatisticAll,
        PercentageColumns, PercentageRows, PercentageAll,
        NormalizeMinMaxColumns, NormalizeSumColumns, NormalizeZscoreColumns,
        NormalizeMinMaxRows, NormalizeSumRows, NormalizeZscoreRows,
        NormalizeMinMaxAll, NormalizeSumAll, NormalizeZscoreAll,
        Frequency, SimpleLinearRegression
    }

    public abstract void scanData();

    public abstract void readColumnNames();

    public abstract void readTotal();

    public abstract void readPage();

    public abstract void readRecords();

    public static Data2DReader create(Data2D_Edit data) {
        if (data == null) {
            return null;
        }
        if (data instanceof DataFileExcel) {
            return new DataFileExcelReader((DataFileExcel) data);
        } else if (data instanceof DataFileCSV) {
            return new DataFileCSVReader((DataFileCSV) data);
        } else if (data instanceof DataFileText) {
            return new DataFileTextReader((DataFileText) data);
        } else if (data instanceof DataTable) {
            return new DataTableReader((DataTable) data);
        }
        return null;
    }

    public void init(Data2D data) {
        this.data2D = data;
        readerTask = data2D.getTask();
    }

    public Data2DReader start(Operation operation) {
        if (data2D == null || operation == null) {
            failed = true;
            return null;
        }
        readerFile = data2D.getFile();
        if (data2D.isDataFile()) {
            if (readerFile == null || !readerFile.exists() || readerFile.length() == 0) {
                failed = true;
                return null;
            }
        } else if (data2D.isTable()) {
            if (data2D.getSheet() == null) {
                failed = true;
                return null;
            }
        }
        if (cols != null && !cols.isEmpty()) {
            colsLen = cols.size();
        }
        switch (operation) {
            case ReadColumnNames:
                data2D.checkForLoad();
                break;
            case ReadCols:
                if (cols == null || cols.isEmpty()) {
                    failed = true;
                    return null;
                }
                break;
            case Export:
                if (cols == null || cols.isEmpty() || convertController == null) {
                    failed = true;
                    return null;
                }
                break;
            case WriteTable:
                if (cols == null || cols.isEmpty() || conn == null || dataTable == null) {
                    failed = true;
                    return null;
                }
                names = data2D.columnNames();
                break;
            case SingleColumn:
                if (cols == null || cols.isEmpty() || conn == null || dataTable == null) {
                    failed = true;
                    return null;
                }
                break;
            case Copy:
                if (cols == null || cols.isEmpty() || csvPrinter == null) {
                    failed = true;
                    return null;
                }
                break;
            case PercentageColumns:
                if (cols == null || cols.isEmpty()) {
                    failed = true;
                    return null;
                }
                if (scanPass == 1) {
                    colValues = new double[colsLen];
                } else if (colValues == null || csvPrinter == null) {
                    failed = true;
                    return null;
                }
                break;
            case PercentageAll:
                if (cols == null || cols.isEmpty()) {
                    failed = true;
                    return null;
                }
                if (scanPass == 1) {
                    tValue = 0d;
                } else if (csvPrinter == null) {
                    failed = true;
                    return null;
                }
                break;
            case PercentageRows:
                if (cols == null || cols.isEmpty() || csvPrinter == null) {
                    failed = true;
                    return null;
                }
                break;
            case Frequency:
                if (frequency == null || csvPrinter == null) {
                    failed = true;
                    return null;
                }
                break;
            case StatisticColumns:
                if (cols == null || cols.isEmpty() || scanPass < 1
                        || statisticData == null || statisticCalculation == null) {
                    failed = true;
                    return null;
                }
                if (scanPass == 1) {
                    colValues = new double[colsLen];
                    if (statisticCalculation.isSkewness()) {
                        skewnessList = new ArrayList<>();
                        for (int i = 0; i < cols.size(); i++) {
                            skewnessList.add(new Skewness());
                        }
                    }
                } else if (scanPass == 2) {
                    for (int i = 0; i < cols.size(); i++) {
                        statisticData[i].vTmp = 0;
                    }
                }
                break;
            case StatisticAll:
                if (cols == null || cols.isEmpty() || scanPass < 1
                        || statisticAll == null || statisticCalculation == null) {
                    failed = true;
                    return null;
                }
                if (scanPass == 1) {
                    if (statisticCalculation.isSkewness()) {
                        skewnessAll = new Skewness();
                    }
                } else if (scanPass == 2) {
                    statisticAll.vTmp = 0;
                }
                break;
            case StatisticRows:
                if (cols == null || cols.isEmpty() || csvPrinter == null || statisticCalculation == null) {
                    failed = true;
                    return null;
                }
                break;
            case NormalizeMinMaxColumns:
                if (cols == null || cols.isEmpty() || statisticData == null || csvPrinter == null) {
                    failed = true;
                    return null;
                }
                break;
            case NormalizeSumColumns:
                if (cols == null || cols.isEmpty() || colValues == null || csvPrinter == null) {
                    failed = true;
                    return null;
                }
                break;
            case NormalizeZscoreColumns:
                if (cols == null || cols.isEmpty() || statisticData == null || csvPrinter == null) {
                    failed = true;
                    return null;
                }
                break;
            case NormalizeMinMaxRows:
                if (cols == null || cols.isEmpty() || csvPrinter == null) {
                    failed = true;
                    return null;
                }
                break;
            case NormalizeSumRows:
                if (cols == null || cols.isEmpty() || csvPrinter == null) {
                    failed = true;
                    return null;
                }
                break;
            case NormalizeZscoreRows:
                if (cols == null || cols.isEmpty() || csvPrinter == null) {
                    failed = true;
                    return null;
                }
                break;
            case NormalizeMinMaxAll:
                if (cols == null || cols.isEmpty() || statisticAll == null || csvPrinter == null) {
                    failed = true;
                    return null;
                }
                break;
            case NormalizeSumAll:
                if (cols == null || cols.isEmpty() || csvPrinter == null) {
                    failed = true;
                    return null;
                }
                break;
            case NormalizeZscoreAll:
                if (cols == null || cols.isEmpty() || statisticAll == null || csvPrinter == null) {
                    failed = true;
                    return null;
                }
                break;
            case SimpleLinearRegression:
                if (cols == null || cols.size() < 2 || simpleRegression == null || csvPrinter == null) {
                    failed = true;
                    return null;
                }
                break;
        }

        this.operation = operation;
        readerStopped = false;
        readerHasHeader = data2D.isHasHeader();
        needCheckTask = readerTask != null;
        columnsNumber = data2D.columnsNumber();
        rowIndex = 0;
        rowsStart = data2D.getStartRowOfCurrentPage();
        rowsEnd = rowsStart + data2D.getPageSize();
        count = 0;
        names = new ArrayList<>();
        rows = new ArrayList<>();
        if (scale < 0) {
            scale = data2D.getScale();
        }
        record = new ArrayList<>();
        scanData();
        afterScanned();
        return this;
    }

    public void handleData() {
        try {
            if (operation == null) {
                readRecords();
            } else {
                switch (operation) {
                    case ReadDefinition:
                        break;
                    case ReadColumnNames:
                        readColumnNames();
                        break;
                    case ReadTotal:
                        readTotal();
                        break;
                    case ReadPage:
                        readPage();
                        break;
                    default:
                        readRecords();
                        break;
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (readerTask != null) {
                readerTask.setError(e.toString());
            }
            failed = true;
        }
    }

    public void handleHeader() {
        try {
            names = new ArrayList<>();
            if (readerHasHeader && StringTools.noDuplicated(record, true)) {
                names.addAll(record);
            } else {
                readerHasHeader = false;
                if (record != null) {
                    for (int i = 1; i <= record.size(); i++) {
                        names.add(data2D.colPrefix() + i);
                    }
                }
            }
            data2D.setHasHeader(readerHasHeader);
            readerStopped = true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (readerTask != null) {
                readerTask.setError(e.toString());
            }
        }
    }

    public void handlePageRow() {
        List<String> row = new ArrayList<>();
        for (int i = 0; i < Math.min(record.size(), columnsNumber); i++) {
            row.add(record.get(i));
        }
        for (int col = row.size(); col < columnsNumber; col++) {
            row.add(data2D.defaultColValue());
        }
        row.add(0, "" + (rowIndex + 1));
        rows.add(row);
    }

    public void handleRecord() {
        try {
            if (!data2D.filterDataRow(record, rowIndex + 1)) {
                return;
            }
            if (data2D.filterReachMaxPassed()) {
                readerStopped = true;
                return;
            }
            switch (operation) {
                case ReadCols:
                    handleReadCols();
                    break;
                case ReadRows:
                    handleReadRows();
                    break;
                case Export:
                    handleExport();
                    break;
                case WriteTable:
                    handleWriteTable();
                    break;
                case SingleColumn:
                    handleSingleColumn();
                    break;
                case Copy:
                    handleCopy();
                    break;
                case StatisticColumns:
                    handleStatisticColumns();
                    break;
                case StatisticAll:
                    handleStatisticAll();
                    break;
                case StatisticRows:
                    handleStatisticRows();
                    break;
                case PercentageColumns:
                    if (scanPass == 1) {
                        handlePecentageColumnsSum();
                    } else if (scanPass == 2) {
                        handlePercentageColumns();
                    }
                    break;
                case PercentageAll:
                    if (scanPass == 1) {
                        handlePecentageAllSum();
                    } else if (scanPass == 2) {
                        handlePercentageAll();
                    }
                    break;
                case PercentageRows:
                    handlePercentageRows();
                    break;
                case Frequency:
                    handleFrequency();
                    break;
                case NormalizeMinMaxColumns:
                    handleNormalizeMinMaxColumns();
                    break;
                case NormalizeSumColumns:
                    handleNormalizeSumColumns();
                    break;
                case NormalizeZscoreColumns:
                    handleNormalizeZscoreColumns();
                    break;
                case NormalizeMinMaxAll:
                    handleNormalizeMinMaxAll();
                    break;
                case NormalizeSumAll:
                    handleNormalizeSumAll();
                    break;
                case NormalizeZscoreAll:
                    handleNormalizeZscoreAll();
                    break;
                case NormalizeMinMaxRows:
                    handleNormalizeRows(Normalization.Algorithm.MinMax);
                    break;
                case NormalizeSumRows:
                    handleNormalizeRows(Normalization.Algorithm.Sum);
                    break;
                case NormalizeZscoreRows:
                    handleNormalizeRows(Normalization.Algorithm.ZScore);
                    break;
                case SimpleLinearRegression:
                    handleSimpleLinearRegression();
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (readerTask != null) {
                readerTask.setError(e.toString());
            }
        }
    }

    public void handleReadCols() {
        try {
            List<String> row = new ArrayList<>();
            for (int col : cols) {
                if (col >= 0 && col < record.size()) {
                    row.add(record.get(col));
                } else {
                    row.add(null);
                }
            }
            if (row.isEmpty()) {
                return;
            }
            if (includeRowNumber) {
                row.add(0, (rowIndex + 1) + "");
            }
            rows.add(row);
        } catch (Exception e) {
        }
    }

    public void handleReadRows() {
        try {
            List<String> row = new ArrayList<>();
            row.addAll(record);
            if (includeRowNumber) {
                row.add(0, (rowIndex + 1) + "");
            }
            rows.add(row);
        } catch (Exception e) {
        }
    }

    public void handleExport() {
        try {
            List<String> row = new ArrayList<>();
            for (int col : cols) {
                if (col >= 0 && col < record.size()) {
                    row.add(record.get(col));
                } else {
                    row.add(null);
                }
            }
            if (row.isEmpty()) {
                return;
            }
            convertController.writeRow(row);
        } catch (Exception e) {
        }
    }

    public void handleWriteTable() {
        try {
            Data2DRow data2DRow = tableData2D.newRow();
            int len = record.size();
            for (int col : cols) {
                if (col >= 0 && col < len) {
                    Data2DColumn sourceColumn = data2D.getColumns().get(col);
                    Object value = sourceColumn.fromString(record.get(col));
                    if (value != null) {
                        data2DRow.setColumnValue(dataTable.mappedColumnName(sourceColumn.getColumnName()), value);
                    }
                }
            }
            if (data2DRow.isEmpty()) {
                return;
            }
            if (includeRowNumber) {
                data2DRow.setColumnValue(dataTable.mappedColumnName(message("SourceRowNumber")), rowIndex + 1);
            }
            tableData2D.insertData(conn, data2DRow);
            if (++count % DerbyBase.BatchSize == 0) {
                conn.commit();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void handleSingleColumn() {
        try {
            int len = record.size();
            for (int col : cols) {
                if (col >= 0 && col < len) {
                    Data2DColumn sourceColumn = data2D.getColumns().get(col);
                    Object value = sourceColumn.fromString(record.get(col));
                    Data2DRow data2DRow = tableData2D.newRow();
                    data2DRow.setColumnValue("data", value);
                    tableData2D.insertData(conn, data2DRow);
                    if (++count % DerbyBase.BatchSize == 0) {
                        conn.commit();
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void handleCopy() {
        try {
            List<String> row = new ArrayList<>();
            for (int col : cols) {
                if (col >= 0 && col < record.size()) {
                    row.add(record.get(col));
                } else {
                    row.add(null);
                }
            }
            if (row.isEmpty()) {
                return;
            }
            if (includeRowNumber) {
                row.add(0, (rowIndex + 1) + "");
            }
            csvPrinter.printRecord(row);
        } catch (Exception e) {
        }
    }

    public void handleStatisticColumns() {
        if (scanPass == 1) {
            handleStatisticColumnsPass1();
        } else if (scanPass == 2) {
            handleStatisticColumnsPass2();
        }
    }

    public void handleStatisticColumnsPass1() {
        try {
            for (int c = 0; c < colsLen; c++) {
                statisticData[c].count++;
                int i = cols.get(c);
                if (i < 0 || i >= record.size()) {
                    continue;
                }
                double v = data2D.doubleValue(record.get(i));
                if (sumAbs) {
                    statisticData[c].sum += Math.abs(v);
                } else {
                    statisticData[c].sum += v;
                }
                if (statisticCalculation.isMaximum() && v > statisticData[c].maximum) {
                    statisticData[c].maximum = v;
                }
                if (statisticCalculation.isMinimum() && v < statisticData[c].minimum) {
                    statisticData[c].minimum = v;
                }
                if (statisticCalculation.isGeometricMean()) {
                    statisticData[c].geometricMean = statisticData[c].geometricMean * v;
                }
                if (statisticCalculation.isSumSquares()) {
                    statisticData[c].sumSquares += v * v;
                }
                if (statisticCalculation.isSkewness()) {
                    skewnessList.get(c).increment(v);
                }
            }
        } catch (Exception e) {

        }
    }

    public void handleStatisticColumnsPass2() {
        try {
            for (int c = 0; c < colsLen; c++) {
                if (statisticData[c].count == 0) {
                    continue;
                }
                int i = cols.get(c);
                if (i < 0 || i >= record.size()) {
                    continue;
                }
                double v = data2D.doubleValue(record.get(i)) - statisticData[c].mean;
                statisticData[c].vTmp += v * v;
            }
        } catch (Exception e) {
        }
    }

    public void handleStatisticAll() {
        if (scanPass == 1) {
            handleStatisticAllPass1();
        } else if (scanPass == 2) {
            handleStatisticAllPass2();
        }
    }

    public void handleStatisticAllPass1() {
        try {
            for (int c = 0; c < colsLen; c++) {
                statisticAll.count++;
                int i = cols.get(c);
                if (i < 0 || i >= record.size()) {
                    continue;
                }
                double v = data2D.doubleValue(record.get(i));
                statisticAll.sum += v;
                if (statisticCalculation.isMaximum() && v > statisticAll.maximum) {
                    statisticAll.maximum = v;
                }
                if (statisticCalculation.isMinimum() && v < statisticAll.minimum) {
                    statisticAll.minimum = v;
                }
                if (statisticCalculation.isGeometricMean()) {
                    statisticAll.geometricMean = statisticAll.geometricMean * v;
                }
                if (statisticCalculation.isSumSquares()) {
                    statisticAll.sumSquares += v * v;
                }
                if (statisticCalculation.isSkewness()) {
                    skewnessAll.increment(v);
                }
            }
        } catch (Exception e) {

        }
    }

    public void handleStatisticAllPass2() {
        try {
            for (int c = 0; c < colsLen; c++) {
                if (statisticAll.count == 0) {
                    continue;
                }
                int i = cols.get(c);
                if (i < 0 || i >= record.size()) {
                    continue;
                }
                double v = data2D.doubleValue(record.get(i)) - statisticAll.mean;
                statisticAll.vTmp += v * v;
            }
        } catch (Exception e) {
        }
    }

    public void handleStatisticRows() {
        try {
            List<String> row = new ArrayList<>();
            int startIndex;
            if (statisticCalculation.getCategoryName() == null) {
                row.add(message("Row") + " " + (rowIndex + 1));
                startIndex = 0;
            } else {
                row.add(record.get(cols.get(0)));
                startIndex = 1;
            }
            String[] values = new String[colsLen - startIndex];
            for (int c = startIndex; c < colsLen; c++) {
                int i = cols.get(c);
                if (i < 0 || i >= record.size()) {
                    continue;
                }
                values[c - startIndex] = record.get(i);
            }
            DoubleStatistic statistic = new DoubleStatistic(values, statisticCalculation);
            row.addAll(statistic.toStringList());
            csvPrinter.printRecord(row);
        } catch (Exception e) {
        }
    }

    public void handlePecentageColumnsSum() {
        try {
            for (int c = 0; c < colsLen; c++) {
                int i = cols.get(c);
                if (i < 0 || i >= record.size()) {
                    continue;
                }
                double v = data2D.doubleValue(record.get(i));
                if (v < 0) {
                    if (sumAbs) {
                        colValues[c] += Math.abs(v);
                    }
                } else if (v > 0) {
                    colValues[c] += v;
                }
            }
        } catch (Exception e) {
        }
    }

    public void handlePercentageColumns() {
        try {
            List<String> row = new ArrayList<>();
            row.add(message("Row") + (rowIndex + 1));
            for (int c = 0; c < colsLen; c++) {
                int i = cols.get(c);
                double v = 0;
                if (i >= 0 && i < record.size()) {
                    v = data2D.doubleValue(record.get(i));
                }
                if (withValues) {
                    row.add(DoubleTools.scale(v, scale) + "");
                }
                if (v < 0) {
                    if (sumAbs) {
                        v = Math.abs(v);
                    } else {
                        v = 0;
                    }
                }
                double s = colValues[c];
                if (s == 0) {
                    row.add("0");
                } else {
                    row.add(DoubleTools.percentage(v, s, scale));
                }
            }
            csvPrinter.printRecord(row);
        } catch (Exception e) {
        }
    }

    public void handlePecentageAllSum() {
        try {
            for (int c = 0; c < colsLen; c++) {
                int i = cols.get(c);
                if (i < 0 || i >= record.size()) {
                    continue;
                }
                double v = data2D.doubleValue(record.get(i));
                if (v < 0) {
                    if (sumAbs) {
                        tValue += Math.abs(v);
                    }
                } else if (v > 0) {
                    tValue += v;
                }
            }
        } catch (Exception e) {
        }
    }

    public void handlePercentageAll() {
        try {
            List<String> row = new ArrayList<>();
            row.add(message("Row") + (rowIndex + 1));
            for (int c = 0; c < colsLen; c++) {
                int i = cols.get(c);
                double v = 0;
                if (i >= 0 && i < record.size()) {
                    v = data2D.doubleValue(record.get(i));
                }
                if (withValues) {
                    row.add(DoubleTools.scale(v, scale) + "");
                }
                if (v < 0) {
                    if (sumAbs) {
                        v = Math.abs(v);
                    } else {
                        v = 0;
                    }
                }
                if (tValue == 0) {
                    row.add("0");
                } else {
                    row.add(DoubleTools.percentage(v, tValue, scale));
                }
            }
            csvPrinter.printRecord(row);
        } catch (Exception e) {
        }
    }

    public void handlePercentageRows() {
        try {
            List<String> row = new ArrayList<>();
            row.add(message("Row") + (rowIndex + 1));
            double sum = 0;
            for (int c = 0; c < colsLen; c++) {
                int i = cols.get(c);
                if (i >= 0 && i < record.size()) {
                    double v = data2D.doubleValue(record.get(i));
                    if (v < 0) {
                        if (sumAbs) {
                            sum += Math.abs(v);
                        }
                    } else if (v > 0) {
                        sum += v;
                    }
                }
            }
            row.add(DoubleTools.scale(sum, scale) + "");
            for (int c = 0; c < colsLen; c++) {
                int i = cols.get(c);
                double v = 0;
                if (i >= 0 && i < record.size()) {
                    v = data2D.doubleValue(record.get(i));
                }
                if (withValues) {
                    row.add(DoubleTools.scale(v, scale) + "");
                }
                if (v < 0) {
                    if (sumAbs) {
                        v = Math.abs(v);
                    } else {
                        v = 0;
                    }
                }
                if (sum == 0) {
                    row.add("0");
                } else {
                    row.add(DoubleTools.percentage(v, sum, scale));
                }
            }
            csvPrinter.printRecord(row);
        } catch (Exception e) {
        }
    }

    public void handleFrequency() {
        try {
            frequency.addValue(record.get(colIndex));
        } catch (Exception e) {
        }
    }

    public void handleNormalizeMinMaxColumns() {
        try {
            List<String> row = new ArrayList<>();
            if (includeRowNumber) {
                row.add(message("Row") + (rowIndex + 1));
            }
            for (int c = 0; c < colsLen; c++) {
                int i = cols.get(c);
                if (i < 0 || i >= record.size()) {
                    row.add(null);
                } else {
                    double v = data2D.doubleValue(record.get(i));
                    v = from + statisticData[c].vTmp * (v - statisticData[c].minimum);
                    row.add(DoubleTools.scale(v, scale) + "");
                }
            }
            csvPrinter.printRecord(row);
        } catch (Exception e) {
        }
    }

    public void handleNormalizeSumColumns() {
        try {
            List<String> row = new ArrayList<>();
            if (includeRowNumber) {
                row.add(message("Row") + (rowIndex + 1));
            }
            for (int c = 0; c < colsLen; c++) {
                int i = cols.get(c);
                if (i < 0 || i >= record.size()) {
                    row.add(null);
                } else {
                    double v = data2D.doubleValue(record.get(i));
                    v = v * colValues[c];
                    row.add(DoubleTools.scale(v, scale) + "");
                }
            }
            csvPrinter.printRecord(row);
        } catch (Exception e) {
        }
    }

    public void handleNormalizeZscoreColumns() {
        try {
            List<String> row = new ArrayList<>();
            if (includeRowNumber) {
                row.add(message("Row") + (rowIndex + 1));
            }
            for (int c = 0; c < colsLen; c++) {
                int i = cols.get(c);
                if (i < 0 || i >= record.size()) {
                    row.add(null);
                } else {
                    double v = data2D.doubleValue(record.get(i));
                    double k = statisticData[c].getPopulationStandardDeviation();
                    if (k == 0) {
                        k = AppValues.TinyDouble;
                    }
                    v = (v - statisticData[c].mean) / k;
                    row.add(DoubleTools.scale(v, scale) + "");
                }
            }
            csvPrinter.printRecord(row);
        } catch (Exception e) {
        }
    }

    public void handleNormalizeMinMaxAll() {
        try {
            List<String> row = new ArrayList<>();
            if (includeRowNumber) {
                row.add(message("Row") + (rowIndex + 1));
            }
            for (int c = 0; c < colsLen; c++) {
                int i = cols.get(c);
                if (i < 0 || i >= record.size()) {
                    row.add(null);
                } else {
                    double v = data2D.doubleValue(record.get(i));
                    v = from + statisticAll.vTmp * (v - statisticAll.minimum);
                    row.add(DoubleTools.scale(v, scale) + "");
                }
            }
            csvPrinter.printRecord(row);
        } catch (Exception e) {
        }
    }

    public void handleNormalizeSumAll() {
        try {
            List<String> row = new ArrayList<>();
            if (includeRowNumber) {
                row.add(message("Row") + (rowIndex + 1));
            }
            for (int c = 0; c < colsLen; c++) {
                int i = cols.get(c);
                if (i < 0 || i >= record.size()) {
                    row.add(null);
                } else {
                    double v = data2D.doubleValue(record.get(i));
                    v = v * tValue;
                    row.add(DoubleTools.scale(v, scale) + "");
                }
            }
            csvPrinter.printRecord(row);
        } catch (Exception e) {
        }
    }

    public void handleNormalizeZscoreAll() {
        try {
            List<String> row = new ArrayList<>();
            if (includeRowNumber) {
                row.add(message("Row") + (rowIndex + 1));
            }
            for (int c = 0; c < colsLen; c++) {
                int i = cols.get(c);
                if (i < 0 || i >= record.size()) {
                    row.add(null);
                } else {
                    double v = data2D.doubleValue(record.get(i));
                    double k = statisticAll.getPopulationStandardDeviation();
                    if (k == 0) {
                        k = AppValues.TinyDouble;
                    }
                    v = (v - statisticAll.mean) / k;
                    row.add(DoubleTools.scale(v, scale) + "");
                }
            }
            csvPrinter.printRecord(row);
        } catch (Exception e) {
        }
    }

    public void handleNormalizeRows(Normalization.Algorithm a) {
        try {
            List<String> row = new ArrayList<>();
            if (includeRowNumber) {
                row.add(message("Row") + (rowIndex + 1));
            }
            double[] values = new double[colsLen];
            for (int c = 0; c < colsLen; c++) {
                int i = cols.get(c);
                if (i < 0 || i >= record.size()) {
                    values[c] = 0;
                } else {
                    values[c] = data2D.doubleValue(record.get(i));
                }
            }
            values = Normalization.create()
                    .setA(a).setFrom(from).setTo(to).setSourceVector(values)
                    .calculate();
            if (values == null) {
                return;
            }
            for (double d : values) {
                row.add(DoubleTools.scale(d, scale) + "");
            }
            csvPrinter.printRecord(row);
        } catch (Exception e) {
        }
    }

    public void handleSimpleLinearRegression() {
        try {
            double x = data2D.doubleValue(record.get(cols.get(0)));
            double y = data2D.doubleValue(record.get(cols.get(1)));
            List<String> row = simpleRegression.addData(rowIndex, x, y);
            csvPrinter.printRecord(row);
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
    }

    public void afterScanned() {
        try {
            switch (operation) {
                case StatisticColumns:
                    if (scanPass == 1) {
                        for (int c = 0; c < colsLen; c++) {
                            if (statisticData[c].count > 0) {
                                statisticData[c].mean = statisticData[c].sum / statisticData[c].count;
                                if (statisticCalculation.isGeometricMean()) {
                                    statisticData[c].geometricMean = Math.pow(statisticData[c].geometricMean, 1d / statisticData[c].count);
                                }
                            }
                            if (statisticCalculation.isSkewness()) {
                                statisticData[c].skewness = skewnessList.get(c).getResult();
                            }
                        }
                    } else if (scanPass == 2) {
                        for (int c = 0; c < colsLen; c++) {
                            if (statisticData[c].count > 0) {
                                statisticData[c].populationVariance = statisticData[c].vTmp / statisticData[c].count;
                                statisticData[c].sampleVariance = statisticData[c].vTmp / (statisticData[c].count - 1);
                                if (statisticCalculation.isPopulationStandardDeviation()) {
                                    statisticData[c].populationStandardDeviation = Math.sqrt(statisticData[c].populationVariance);
                                }
                                if (statisticCalculation.isSampleStandardDeviation()) {
                                    statisticData[c].sampleStandardDeviation = Math.sqrt(statisticData[c].sampleVariance);
                                }
                            }
                        }
                    }
                    break;
                case StatisticAll:
                    if (scanPass == 1) {
                        if (statisticAll.count > 0) {
                            statisticAll.mean = statisticAll.sum / statisticAll.count;
                            if (statisticCalculation.isGeometricMean()) {
                                statisticAll.geometricMean = Math.pow(statisticAll.geometricMean, 1d / statisticAll.count);
                            }
                        }
                        if (statisticCalculation.isSkewness()) {
                            statisticAll.skewness = skewnessAll.getResult();
                        }
                    } else if (scanPass == 2) {
                        if (statisticAll.count > 0) {
                            statisticAll.populationVariance = statisticAll.vTmp / statisticAll.count;
                            statisticAll.sampleVariance = statisticAll.vTmp / (statisticAll.count - 1);
                            if (statisticCalculation.isPopulationStandardDeviation()) {
                                statisticAll.populationStandardDeviation = Math.sqrt(statisticAll.populationVariance);
                            }
                            if (statisticCalculation.isSampleStandardDeviation()) {
                                statisticAll.sampleStandardDeviation = Math.sqrt(statisticAll.sampleVariance);
                            }
                        }
                    }
                    break;
                case Frequency:
                    List<String> row = new ArrayList<>();
                    row.add(message("All"));
                    row.add(frequency.getSumFreq() + "");
                    row.add("100");
                    csvPrinter.printRecord(row);
                    count = 1;
                    Iterator iterator = frequency.valuesIterator();
                    if (iterator != null) {
                        while (iterator.hasNext()) {
                            Object o = iterator.next();
                            row.clear();
                            String value = o == null ? null : (String) o;
                            row.add(value);
                            row.add(frequency.getCount(value) + "");
                            row.add(DoubleTools.format(frequency.getPct(value) * 100, scale));
                            csvPrinter.printRecord(row);
                            count++;
                        }
                    }
                    frequency.clear();
                    break;
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (readerTask != null) {
                readerTask.setError(e.toString());
            }
        }
    }

    public boolean readerStopped() {
        return readerStopped || (needCheckTask && (readerTask == null || readerTask.isCancelled()));
    }

    /*
        get/set
     */
    public boolean isFailed() {
        return failed;
    }

    public List<String> getNames() {
        return names;
    }

    public List<List<String>> getRows() {
        return rows;
    }

    public long getRowIndex() {
        return rowIndex;
    }

    public double[] getColValues() {
        return colValues;
    }

    public Data2DReader setReaderData(Data2D readerData) {
        this.data2D = readerData;
        return this;
    }

    public Data2DReader setReaderHasHeader(boolean readerHasHeader) {
        this.readerHasHeader = readerHasHeader;
        return this;
    }

    public Data2DReader setReaderCanceled(boolean readerStopped) {
        this.readerStopped = readerStopped;
        return this;
    }

    public Data2DReader setNeedCheckTask(boolean needCheckTask) {
        this.needCheckTask = needCheckTask;
        return this;
    }

    public Data2DReader setReaderTask(SingletonTask readerTask) {
        this.readerTask = readerTask;
        return this;
    }

    public Data2DReader setNames(List<String> names) {
        this.names = names;
        return this;
    }

    public Data2DReader setCols(List<Integer> cols) {
        this.cols = cols;
        return this;
    }

    public Data2DReader setIncludeRowNumber(boolean includeRowNumber) {
        this.includeRowNumber = includeRowNumber;
        return this;
    }

    public Data2DReader setIncludeColName(boolean includeColName) {
        this.includeColName = includeColName;
        return this;
    }

    public Data2DReader setWithValues(boolean withValues) {
        this.withValues = withValues;
        return this;
    }

    public Data2DReader setFrom(double from) {
        this.from = from;
        return this;
    }

    public Data2DReader setTo(double to) {
        this.to = to;
        return this;
    }

    public Data2DReader setConvertController(ControlDataConvert convertController) {
        this.convertController = convertController;
        return this;
    }

    public Data2DReader setDataTable(DataTable dataTable) {
        this.dataTable = dataTable;
        if (dataTable != null) {
            tableData2D = dataTable.getTableData2D();
        }
        return this;
    }

    public Data2DReader setConn(Connection conn) {
        this.conn = conn;
        return this;
    }

    public Data2DReader setStatisticData(DoubleStatistic[] statisticData) {
        this.statisticData = statisticData;
        return this;
    }

    public Data2DReader setCsvPrinter(CSVPrinter csvPrinter) {
        this.csvPrinter = csvPrinter;
        return this;
    }

    public Data2DReader setColValues(double[] colsSum) {
        this.colValues = colsSum;
        return this;
    }

    public Data2DReader setSumAbs(boolean sumAbs) {
        this.sumAbs = sumAbs;
        return this;
    }

    public Data2DReader setStatisticSelection(DescriptiveStatistic statisticSelection) {
        this.statisticCalculation = statisticSelection;
        return this;
    }

    public Data2DReader setScanPass(int scanPass) {
        this.scanPass = scanPass;
        return this;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public int getColIndex() {
        return colIndex;
    }

    public Data2DReader setCol(int col) {
        this.colIndex = col;
        return this;
    }

    public Frequency getFrequency() {
        return frequency;
    }

    public Data2DReader setFrequency(Frequency frequency) {
        this.frequency = frequency;
        return this;
    }

    public DoubleStatistic getStatisticAll() {
        return statisticAll;
    }

    public Data2DReader setStatisticAll(DoubleStatistic statisticAll) {
        this.statisticAll = statisticAll;
        return this;
    }

    public double gettValue() {
        return tValue;
    }

    public Data2DReader setValue(double value) {
        this.tValue = value;
        return this;
    }

    public int getScale() {
        return scale;
    }

    public Data2DReader setScale(int scale) {
        this.scale = scale;
        return this;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public Data2DReader setCategoryName(String categoryName) {
        this.categoryName = categoryName;
        return null;
    }

    public SimpleRegression getSimpleRegression() {
        return simpleRegression;
    }

    public Data2DReader setSimpleRegression(SimpleLinearRegression simpleRegression) {
        this.simpleRegression = simpleRegression;
        return this;
    }

}
