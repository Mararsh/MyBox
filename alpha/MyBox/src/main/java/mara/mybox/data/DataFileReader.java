package mara.mybox.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.controller.ControlDataConvert;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.StringTools;
import org.apache.commons.csv.CSVPrinter;

/**
 * @Author Mara
 * @CreateDate 2022-1-28
 * @License Apache License Version 2.0
 */
public abstract class DataFileReader {

    protected DataFile dataFile;
    protected File readerFile;
    protected Operation operation;
    protected long rowIndex, rowsStart, rowsEnd;
    protected int columnsNumber, colsLen, scale;
    protected List<String> record, names;
    protected List<List<String>> rows = new ArrayList<>();
    protected List<Integer> cols;
    protected boolean includeRowNumber, includeColName, withValues, failed, sumAbs, countKewness;
    protected double from, to;
    protected double[] colValues;
    protected ControlDataConvert convertController;
    protected DoubleStatistic[] statisticData;
    protected CSVPrinter csvPrinter;
    protected boolean readerHasHeader, readerStopped, needCheckTask;

    protected SingletonTask readerTask;

    public static enum Operation {
        ReadDefnition, ReadTotal, ReadColumns, ReadPage,
        ReadCols, Export, Copy, CountSum, CountSumMinMax, CountVariancesKewness,
        Percentage, NormalizeMinMax, NormalizeSum, NormalizeZscore
    }

    public abstract void scanFile();

    public abstract void readColumns();

    public abstract void readTotal();

    public abstract void readPage();

    public abstract void readRecords();

    public static DataFileReader create(DataFile data) {
        if (data == null) {
            return null;
        }
        if (data instanceof DataFileExcel) {
            return new DataFileExcelReader((DataFileExcel) data);
        } else if (data instanceof DataFileCSV) {
            return new DataFileCSVReader((DataFileCSV) data);
        } else if (data instanceof DataFileText) {
            return new DataFileTextReader((DataFileText) data);
        }
        return null;
    }

    public void init(DataFile data) {
        this.dataFile = data;
        readerTask = dataFile.getTask();
    }

    public DataFileReader start(Operation operation) {
        if (dataFile == null || operation == null) {
            failed = true;
            return null;
        }
        if (cols != null && !cols.isEmpty()) {
            colsLen = cols.size();
        }
        switch (operation) {
            case ReadColumns:
                dataFile.checkForLoad();
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
            case Copy:
                if (cols == null || cols.isEmpty() || csvPrinter == null) {
                    failed = true;
                    return null;
                }
                break;
            case CountSum:
                if (cols == null || cols.isEmpty() || csvPrinter == null) {
                    failed = true;
                    return null;
                }
                colValues = new double[colsLen];
                break;
            case Percentage:
                if (cols == null || cols.isEmpty() || colValues == null || csvPrinter == null) {
                    failed = true;
                    return null;
                }
                break;
            case CountSumMinMax:
            case CountVariancesKewness:
                if (cols == null || cols.isEmpty() || statisticData == null) {
                    failed = true;
                    return null;
                }
                colValues = new double[colsLen];
                break;
            case NormalizeMinMax:
                if (cols == null || cols.isEmpty() || statisticData == null || csvPrinter == null) {
                    failed = true;
                    return null;
                }
                colValues = new double[colsLen];
                break;
            case NormalizeSum:
                if (cols == null || cols.isEmpty() || colValues == null || csvPrinter == null) {
                    failed = true;
                    return null;
                }
                colValues = new double[colsLen];
                break;
            case NormalizeZscore:
                if (cols == null || cols.isEmpty() || colValues == null || csvPrinter == null) {
                    failed = true;
                    return null;
                }
                colValues = new double[colsLen];
                break;
        }

        this.operation = operation;
        readerStopped = false;
        readerFile = dataFile.getFile();
        if (readerFile == null || !readerFile.exists() || readerFile.length() == 0) {
            failed = true;
            return null;
        }
        readerHasHeader = dataFile.isHasHeader();
        needCheckTask = readerTask != null;
        columnsNumber = dataFile.columnsNumber();
        rowIndex = 0;
        rowsStart = dataFile.startRowOfCurrentPage;
        rowsEnd = rowsStart + dataFile.pageSize;
        names = new ArrayList<>();
        rows = new ArrayList<>();
        scale = dataFile.getScale();
        record = new ArrayList<>();
        scanFile();
        afterScanned();
        return this;
    }

    public void handleFile() {
        try {
            if (operation == null) {
                readRecords();
            } else {
                switch (operation) {
                    case ReadDefnition:
                        break;
                    case ReadColumns:
                        readColumns();
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
                for (int i = 1; i <= record.size(); i++) {
                    names.add(dataFile.colPrefix() + i);
                }
            }
            dataFile.setHasHeader(readerHasHeader);
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
            row.add(dataFile.defaultColValue());
        }
        row.add(0, "" + (rowIndex + 1));
        rows.add(row);
    }

    public void handleRecord() {
        try {
            switch (operation) {
                case ReadCols:
                    handleReadCols();
                    break;
                case Export:
                    handleExport();
                    break;
                case Copy:
                    handleCopy();
                    break;
                case CountSumMinMax:
                    handleSumMinMax();
                    break;
                case CountVariancesKewness:
                    handleVariancesKewness();
                    break;
                case CountSum:
                    handleSum();
                    break;
                case Percentage:
                    handlePercentage();
                    break;
                case NormalizeMinMax:
                    handleNormalizeMinMax();
                    break;
                case NormalizeSum:
                    handleNormalizeSum();
                    break;
                case NormalizeZscore:
                    handleNormalizeZscore();
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

    public void handleSumMinMax() {
        try {
            for (int c = 0; c < colsLen; c++) {
                statisticData[c].count++;
                int col = cols.get(c);
                if (col < 0 || col >= record.size()) {
                    continue;
                }
                double v = dataFile.doubleValue(record.get(col));
                statisticData[c].sum += v;
                if (v > statisticData[c].maximum) {
                    statisticData[c].maximum = v;
                }
                if (v < statisticData[c].minimum) {
                    statisticData[c].minimum = v;
                }
            }
        } catch (Exception e) {
        }
    }

    public void handleVariancesKewness() {
        try {
            for (int c = 0; c < colsLen; c++) {
                if (statisticData[c].count == 0) {
                    continue;
                }
                int col = cols.get(c);
                if (col < 0 || col >= record.size()) {
                    continue;
                }
                double v = dataFile.doubleValue(record.get(col));
                statisticData[c].variance += Math.pow(v - statisticData[c].mean, 2);
                if (countKewness) {
                    statisticData[c].skewness += Math.pow(v - statisticData[c].mean, 3);
                }
            }
        } catch (Exception e) {
        }
    }

    public void handleSum() {
        try {
            for (int c = 0; c < colsLen; c++) {
                int col = cols.get(c);
                if (col < 0 || col >= record.size()) {
                    continue;
                }
                double v = dataFile.doubleValue(record.get(col));
                if (sumAbs) {
                    colValues[c] += Math.abs(v);
                } else {
                    colValues[c] += v;
                }
            }
        } catch (Exception e) {
        }
    }

    public void handlePercentage() {
        try {
            List<String> row = new ArrayList<>();
            row.add((rowIndex + 1) + "");
            for (int c = 0; c < colsLen; c++) {
                int col = cols.get(c);
                double v = 0;
                if (col >= 0 && col < record.size()) {
                    v = dataFile.doubleValue(record.get(col));
                }
                if (withValues) {
                    row.add(DoubleTools.scale(v, scale) + "");
                }
                if (colValues[c] == 0) {
                    row.add("0");
                } else {
                    row.add(DoubleTools.percentage(v, colValues[c]));
                }
            }
            csvPrinter.printRecord(row);
        } catch (Exception e) {
        }
    }

    public void handleNormalizeMinMax() {
        try {
            List<String> row = new ArrayList<>();
            if (includeRowNumber) {
                row.add((rowIndex + 1) + "");
            }
            for (int c = 0; c < colsLen; c++) {
                int col = cols.get(c);
                if (col < 0 || col >= record.size()) {
                    row.add(null);
                } else {
                    double v = dataFile.doubleValue(record.get(col));
                    v = from + statisticData[c].mean * (v - statisticData[c].minimum);
                    row.add(DoubleTools.scale(v, scale) + "");
                }
            }
            csvPrinter.printRecord(row);
        } catch (Exception e) {
        }
    }

    public void handleNormalizeSum() {
        try {
            List<String> row = new ArrayList<>();
            if (includeRowNumber) {
                row.add((rowIndex + 1) + "");
            }
            for (int c = 0; c < colsLen; c++) {
                int col = cols.get(c);
                if (col < 0 || col >= record.size()) {
                    row.add(null);
                } else {
                    double v = dataFile.doubleValue(record.get(col));
                    v = v * colValues[c];
                    row.add(DoubleTools.scale(v, scale) + "");
                }
            }
            csvPrinter.printRecord(row);
        } catch (Exception e) {
        }
    }

    public void handleNormalizeZscore() {
        try {
            List<String> row = new ArrayList<>();
            if (includeRowNumber) {
                row.add((rowIndex + 1) + "");
            }
            for (int c = 0; c < colsLen; c++) {
                int col = cols.get(c);
                if (col < 0 || col >= record.size()) {
                    row.add(null);
                } else {
                    double v = dataFile.doubleValue(record.get(col));
                    v = (v - statisticData[c].mean) / statisticData[c].variance;
                    row.add(DoubleTools.scale(v, scale) + "");
                }
            }
            csvPrinter.printRecord(row);
        } catch (Exception e) {
        }
    }

    public void afterScanned() {
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

    public DataFileReader setReaderData(DataFileExcel readerData) {
        this.dataFile = readerData;
        return this;
    }

    public DataFileReader setReaderFile(File readerFile) {
        this.readerFile = readerFile;
        return this;
    }

    public DataFileReader setReaderHasHeader(boolean readerHasHeader) {
        this.readerHasHeader = readerHasHeader;
        return this;
    }

    public DataFileReader setReaderCanceled(boolean readerStopped) {
        this.readerStopped = readerStopped;
        return this;
    }

    public DataFileReader setNeedCheckTask(boolean needCheckTask) {
        this.needCheckTask = needCheckTask;
        return this;
    }

    public DataFileReader setReaderTask(SingletonTask readerTask) {
        this.readerTask = readerTask;
        return this;
    }

    public DataFileReader setNames(List<String> names) {
        this.names = names;
        return this;
    }

    public DataFileReader setDataFile(DataFile dataFile) {
        this.dataFile = dataFile;
        return this;
    }

    public DataFileReader setCols(List<Integer> cols) {
        this.cols = cols;
        return this;
    }

    public DataFileReader setIncludeRowNumber(boolean includeRowNumber) {
        this.includeRowNumber = includeRowNumber;
        return this;
    }

    public DataFileReader setIncludeColName(boolean includeColName) {
        this.includeColName = includeColName;
        return this;
    }

    public DataFileReader setWithValues(boolean withValues) {
        this.withValues = withValues;
        return this;
    }

    public DataFileReader setFrom(double from) {
        this.from = from;
        return this;
    }

    public DataFileReader setTo(double to) {
        this.to = to;
        return this;
    }

    public DataFileReader setConvertController(ControlDataConvert convertController) {
        this.convertController = convertController;
        return this;
    }

    public DataFileReader setStatisticData(DoubleStatistic[] statisticData) {
        this.statisticData = statisticData;
        return this;
    }

    public DataFileReader setCsvPrinter(CSVPrinter csvPrinter) {
        this.csvPrinter = csvPrinter;
        return this;
    }

    public DataFileReader setColValues(double[] colsSum) {
        this.colValues = colsSum;
        return this;
    }

    public DataFileReader setSumAbs(boolean sumAbs) {
        this.sumAbs = sumAbs;
        return this;
    }

    public DataFileReader setCountKewness(boolean countKewness) {
        this.countKewness = countKewness;
        return this;
    }

}
