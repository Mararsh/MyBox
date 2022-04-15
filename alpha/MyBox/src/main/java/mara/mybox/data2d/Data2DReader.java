package mara.mybox.data2d;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.controller.ControlDataConvert;
import mara.mybox.data.DoubleStatistic;
import mara.mybox.data.StatisticSelection;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DRow;
import mara.mybox.db.table.TableData2D;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.StringTools;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.math3.stat.descriptive.moment.Skewness;

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
    protected int columnsNumber, colsLen, scale, scanPass;
    protected List<String> record, names;
    protected List<List<String>> rows = new ArrayList<>();
    protected List<Integer> cols;
    protected boolean includeRowNumber, includeColName, withValues, failed, sumAbs;
    protected double from;
    protected double[] colValues;
    protected ControlDataConvert convertController;
    protected Connection conn;
    protected TableData2D tableData2D;
    protected DoubleStatistic[] statisticData;
    protected List<Skewness> skewnessList;
    protected StatisticSelection statisticSelection;
    protected CSVPrinter csvPrinter;
    protected boolean readerHasHeader, readerStopped, needCheckTask;
    protected SingletonTask readerTask;

    public static enum Operation {
        ReadDefinition, ReadTotal, ReadColumnNames, ReadPage,
        ReadCols, Export, WriteTable, Copy, Statistic,
        Percentage, NormalizeMinMax, NormalizeSum, NormalizeZscore
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
                if (cols == null || cols.isEmpty() || conn == null || tableData2D == null) {
                    failed = true;
                    return null;
                }
                names = data2D.columnNames();
                break;
            case Copy:
                if (cols == null || cols.isEmpty() || csvPrinter == null) {
                    failed = true;
                    return null;
                }
                break;
            case Percentage:
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
            case Statistic:
                if (cols == null || cols.isEmpty() || scanPass < 1
                        || statisticData == null || statisticSelection == null) {
                    failed = true;
                    return null;
                }
                if (scanPass == 1) {
                    colValues = new double[colsLen];
                    if (statisticSelection.isSkewness()) {
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
            case NormalizeMinMax:
                if (cols == null || cols.isEmpty() || statisticData == null || csvPrinter == null) {
                    failed = true;
                    return null;
                }
                break;
            case NormalizeSum:
                if (cols == null || cols.isEmpty() || colValues == null || csvPrinter == null) {
                    failed = true;
                    return null;
                }
                break;
            case NormalizeZscore:
                if (cols == null || cols.isEmpty() || statisticData == null || csvPrinter == null) {
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
        rowsStart = data2D.startRowOfCurrentPage;
        rowsEnd = rowsStart + data2D.pageSize;
        count = 0;
        names = new ArrayList<>();
        rows = new ArrayList<>();
        scale = data2D.getScale();
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
            switch (operation) {
                case ReadCols:
                    handleReadCols();
                    break;
                case Export:
                    handleExport();
                    break;
                case WriteTable:
                    handleWriteTable();
                    break;
                case Copy:
                    handleCopy();
                    break;
                case Statistic:
                    if (scanPass == 1) {
                        handleStatisticPass1();
                    } else if (scanPass == 2) {
                        handleStatisticPass2();
                    }
                    break;
                case Percentage:
                    if (scanPass == 1) {
                        handlePecentageSum();
                    } else if (scanPass == 2) {
                        handlePercentage();
                    }
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

    public void handleWriteTable() {
        try {
            Data2DRow data2DRow = tableData2D.newRow();
            int len = record.size();
            for (int col : cols) {
                if (col >= 0 && col < len) {
                    Data2DColumn column = data2D.getColumns().get(col);
                    String name = column.getColumnName();
                    Object value = column.fromString(record.get(col));
                    if (value != null) {
                        data2DRow.setColumnValue(name, value);
                    }
                }
            }
            if (data2DRow.isEmpty()) {
                return;
            }
            tableData2D.insertData(conn, data2DRow);
            if (++count % DerbyBase.BatchSize == 0) {
                conn.commit();
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

    public void handleStatistic() {
        if (scanPass == 1) {
            handleStatisticPass1();
        } else if (scanPass == 2) {
            handleStatisticPass2();
        }
    }

    public void handleStatisticPass1() {
        try {
            for (int c = 0; c < colsLen; c++) {
                statisticData[c].count++;
                int col = cols.get(c);
                if (col < 0 || col >= record.size()) {
                    continue;
                }
                double v = data2D.doubleValue(record.get(col));
                if (sumAbs) {
                    statisticData[c].sum += Math.abs(v);
                } else {
                    statisticData[c].sum += v;
                }
                if (statisticSelection.isMaximum() && v > statisticData[c].maximum) {
                    statisticData[c].maximum = v;
                }
                if (statisticSelection.isMinimum() && v < statisticData[c].minimum) {
                    statisticData[c].minimum = v;
                }
                if (statisticSelection.isGeometricMean()) {
                    statisticData[c].geometricMean = statisticData[c].geometricMean * v;
                }
                if (statisticSelection.isSumSquares()) {
                    statisticData[c].sumSquares += v * v;
                }
                if (statisticSelection.isSkewness()) {
                    skewnessList.get(c).increment(v);
                }
            }
        } catch (Exception e) {

        }
    }

    public void handleStatisticPass2() {
        try {
            for (int c = 0; c < colsLen; c++) {
                if (statisticData[c].count == 0) {
                    continue;
                }
                int col = cols.get(c);
                if (col < 0 || col >= record.size()) {
                    continue;
                }
                double v = data2D.doubleValue(record.get(col)) - statisticData[c].mean;
                statisticData[c].vTmp += v * v;
            }
        } catch (Exception e) {
        }
    }

    public void handlePecentageSum() {
        try {
            for (int c = 0; c < colsLen; c++) {
                int col = cols.get(c);
                if (col < 0 || col >= record.size()) {
                    continue;
                }
                double v = data2D.doubleValue(record.get(col));
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

    public void handlePercentage() {
        try {
            List<String> row = new ArrayList<>();
            row.add((rowIndex + 1) + "");
            for (int c = 0; c < colsLen; c++) {
                int col = cols.get(c);
                double v = 0;
                if (col >= 0 && col < record.size()) {
                    v = data2D.doubleValue(record.get(col));
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
                    row.add(DoubleTools.percentage(v, s));
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
                    double v = data2D.doubleValue(record.get(col));
                    v = from + statisticData[c].vTmp * (v - statisticData[c].minimum);
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
                    double v = data2D.doubleValue(record.get(col));
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
                    double v = data2D.doubleValue(record.get(col));
                    double k = statisticData[c].getSampleStandardDeviation();
                    if (k == 0) {
                        k = Float.MIN_VALUE;
                    }
                    v = (v - statisticData[c].mean) / k;
                    row.add(DoubleTools.scale(v, scale) + "");
                }
            }
            csvPrinter.printRecord(row);
        } catch (Exception e) {
        }
    }

    public void afterScanned() {
        try {
            switch (operation) {
                case Statistic:
                    if (scanPass == 1) {
                        for (int c = 0; c < colsLen; c++) {
                            if (statisticData[c].count > 0) {
                                statisticData[c].mean = statisticData[c].sum / statisticData[c].count;
                                if (statisticSelection.isGeometricMean()) {
                                    statisticData[c].geometricMean = Math.pow(statisticData[c].geometricMean, 1d / statisticData[c].count);
                                }
                            }
                            if (statisticSelection.isSkewness()) {
                                statisticData[c].skewness = skewnessList.get(c).getResult();
                            }
                        }
                    } else if (scanPass == 2) {
                        for (int c = 0; c < colsLen; c++) {
                            if (statisticData[c].count > 0) {
                                statisticData[c].populationVariance = statisticData[c].vTmp / statisticData[c].count;
                                statisticData[c].sampleVariance = statisticData[c].vTmp / (statisticData[c].count - 1);
                                if (statisticSelection.isPopulationStandardDeviation()) {
                                    statisticData[c].populationStandardDeviation = Math.sqrt(statisticData[c].populationVariance);
                                }
                                if (statisticSelection.isSampleStandardDeviation()) {
                                    statisticData[c].sampleStandardDeviation = Math.sqrt(statisticData[c].sampleVariance);
                                }
                            }
                        }
                    }
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

    public Data2DReader setConvertController(ControlDataConvert convertController) {
        this.convertController = convertController;
        return this;
    }

    public Data2DReader setTableData2D(TableData2D tableData2D) {
        this.tableData2D = tableData2D;
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

    public Data2DReader setStatisticSelection(StatisticSelection statisticSelection) {
        this.statisticSelection = statisticSelection;
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

}
