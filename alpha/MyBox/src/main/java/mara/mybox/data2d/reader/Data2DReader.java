package mara.mybox.data2d.reader;

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
import mara.mybox.data2d.DataFilter;
import mara.mybox.data2d.DataTable;
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
    protected File sourceFile;
    protected Operation operation;
    protected Data2DOperator operator;
    protected long rowIndex; // 1-based 
    protected long rowsStart, rowsEnd; //  0-based
    protected long count;
    protected int columnsNumber, colsLen, scale = -1, scanPass, colIndex;
    protected List<String> sourceRow, names;
    protected List<List<String>> rows = new ArrayList<>();
    protected List<Integer> cols;
    protected boolean includeRowNumber, includeColName, withValues, failed, sumAbs;
    protected double from, to, tValue, invalidAs = 0;
    protected double[] colValues;
    protected Connection conn;
    protected DataFilter filter;
    protected DataTable writerTable;
    protected TableData2D writerTableData2D;
    protected DoubleStatistic[] statisticData;
    protected List<Skewness> skewnessList;
    protected DoubleStatistic statisticAll;
    protected String categoryName, script, name, toNegative;
    protected Skewness skewnessAll;
    protected DescriptiveStatistic statisticCalculation;
    protected Frequency frequency;
    protected SimpleLinearRegression simpleRegression;
    protected CSVPrinter csvPrinter;
    protected ControlDataConvert convertController;
    protected boolean readerHasHeader, readerStopped, needCheckTask, errorContinue;
    protected SingletonTask task;

    public static enum Operation {
        ReadCols, ReadRows, Export, Copy, RowExpression,
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

    public abstract void readRows();

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
        task = data2D.getTask();
    }

    public boolean start() {
        if (data2D == null || !data2D.validData() || operator == null) {
            return false;
        }
        sourceFile = data2D.getFile();
        readerStopped = false;
        readerHasHeader = data2D.isHasHeader();
        needCheckTask = task != null;
        columnsNumber = data2D.columnsNumber();
        rowIndex = 0;  // 1-based
        rowsStart = data2D.getStartRowOfCurrentPage();
        rowsEnd = rowsStart + data2D.getPageSize();
        count = 0;
        names = new ArrayList<>();
        rows = new ArrayList<>();
        if (scale < 0) {
            scale = data2D.getScale();
        }
        sourceRow = new ArrayList<>();
        data2D.startFilter();
        scanData();
        afterScanned();
        operator.end();
        return true;
    }

    public Data2DReader start(Operation operation) {
        if (data2D == null || !data2D.validData() || operation == null) {
            failed = true;
            return null;
        }
        sourceFile = data2D.getFile();
        MyBoxLog.console(operation + "     " + sourceFile);
        if (cols != null && !cols.isEmpty()) {
            colsLen = cols.size();
        }
        switch (operation) {
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
            case RowExpression:
                if (cols == null || cols.isEmpty() || csvPrinter == null
                        || script == null || name == null) {
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
                if (cols == null || cols.size() < 2 || simpleRegression == null) {
                    failed = true;
                    return null;
                }
                break;
        }

        this.operation = operation;
        readerStopped = false;
        readerHasHeader = data2D.isHasHeader();
        needCheckTask = task != null;
        columnsNumber = data2D.columnsNumber();
        rowIndex = 0;  // 1-based
        rowsStart = data2D.getStartRowOfCurrentPage();
        rowsEnd = rowsStart + data2D.getPageSize();
        count = 0;
        names = new ArrayList<>();
        rows = new ArrayList<>();
        if (scale < 0) {
            scale = data2D.getScale();
        }
        sourceRow = new ArrayList<>();
        data2D.startFilter();
        scanData();
        afterScanned();
        return this;
    }

    public void handleRow() {
        try {
            if (!data2D.filterDataRow(sourceRow, rowIndex)) {
                return;
            }
            if (data2D.filterReachMaxPassed()) {
                readerStopped = true;
                return;
            }
            operator.sourceRow = sourceRow;
            operator.rowIndex = rowIndex;
            operator.handleRow();
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
        }
    }

    public void handleHeader() {
        try {
            names = new ArrayList<>();
            if (readerHasHeader && StringTools.noDuplicated(sourceRow, true)) {
                names.addAll(sourceRow);
            } else {
                readerHasHeader = false;
                if (sourceRow != null) {
                    for (int i = 1; i <= sourceRow.size(); i++) {
                        names.add(data2D.colPrefix() + i);
                    }
                }
            }
            data2D.setHasHeader(readerHasHeader);
            readerStopped = true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
        }
    }

    public void handlePageRow() {
        List<String> row = new ArrayList<>();
        for (int i = 0; i < Math.min(sourceRow.size(), columnsNumber); i++) {
            row.add(sourceRow.get(i));
        }
        for (int col = row.size(); col < columnsNumber; col++) {
            row.add(data2D.defaultColValue());
        }
        row.add(0, "" + rowIndex);
        rows.add(row);
    }

    public void handleRow2() {
        try {
            if (!data2D.filterDataRow(sourceRow, rowIndex)) {
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
                case Copy:
                    handleCopy();
                    break;
                case RowExpression:
                    handleRowExpression();
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
            if (task != null) {
                task.setError(e.toString());
            }
        }
    }

    public void handleReadCols() {
        try {
            List<String> row = new ArrayList<>();
            for (int col : cols) {
                if (col >= 0 && col < sourceRow.size()) {
                    row.add(sourceRow.get(col));
                } else {
                    row.add(null);
                }
            }
            if (row.isEmpty()) {
                return;
            }
            if (includeRowNumber) {
                row.add(0, rowIndex + "");
            }
            rows.add(row);
        } catch (Exception e) {
        }
    }

    public void handleReadRows() {
        try {
            List<String> row = new ArrayList<>();
            row.addAll(sourceRow);
            if (includeRowNumber) {
                row.add(0, rowIndex + "");
            }
            rows.add(row);
        } catch (Exception e) {
        }
    }

    public void handleExport() {
        try {
            List<String> row = new ArrayList<>();
            for (int col : cols) {
                if (col >= 0 && col < sourceRow.size()) {
                    row.add(sourceRow.get(col));
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
                if (col >= 0 && col < sourceRow.size()) {
                    row.add(sourceRow.get(col));
                } else {
                    row.add(null);
                }
            }
            if (row.isEmpty()) {
                return;
            }
            if (includeRowNumber) {
                row.add(0, rowIndex + "");
            }
            csvPrinter.printRecord(row);
        } catch (Exception e) {
        }
    }

    public void handleRowExpression() {
        try {
            List<String> row = new ArrayList<>();
            for (int col : cols) {
                if (col >= 0 && col < sourceRow.size()) {
                    row.add(sourceRow.get(col));
                } else {
                    row.add(null);
                }
            }
            if (row.isEmpty()) {
                return;
            }
            if (includeRowNumber) {
                row.add(0, rowIndex + "");
            }
            if (data2D.calculateDataRowExpression(script, sourceRow, rowIndex)) {
                row.add(data2D.expressionResult());
            } else {
                if (errorContinue) {
                    row.add(null);
                } else {
                    readerStopped = true;
                    return;
                }
            }
            csvPrinter.printRecord(row);
        } catch (Exception e) {
        }
    }

    public void handlePecentageColumnsSum() {
        try {
            for (int c = 0; c < colsLen; c++) {
                int i = cols.get(c);
                if (i < 0 || i >= sourceRow.size()) {
                    continue;
                }
                double d = DoubleTools.toDouble(sourceRow.get(i), invalidAs);
                if (DoubleTools.invalidDouble(d)) {
                } else if (d < 0) {
                    if ("abs".equals(toNegative)) {
                        colValues[c] += Math.abs(d);
                    }
                } else if (d > 0) {
                    colValues[c] += d;
                }
            }
        } catch (Exception e) {
        }
    }

    public void handlePercentageColumns() {
        try {
            List<String> row = new ArrayList<>();
            row.add(message("Row") + rowIndex);
            for (int c = 0; c < colsLen; c++) {
                int i = cols.get(c);
                double d;
                if (i >= 0 && i < sourceRow.size()) {
                    d = DoubleTools.toDouble(sourceRow.get(i), invalidAs);
                } else {
                    d = invalidAs;
                }
                if (withValues) {
                    if (DoubleTools.invalidDouble(d)) {
                        row.add(Double.NaN + "");
                    } else {
                        row.add(DoubleTools.format(d, scale));
                    }
                }
                double s = colValues[c];
                if (DoubleTools.invalidDouble(d) || s == 0) {
                    row.add(Double.NaN + "");
                } else {
                    if (d < 0) {
                        if ("skip".equals(toNegative)) {
                            row.add(Double.NaN + "");
                            continue;
                        } else if ("abs".equals(toNegative)) {
                            d = Math.abs(d);
                        } else {
                            d = 0;
                        }
                    }
                    row.add(DoubleTools.percentage(d, s, scale));
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
                if (i < 0 || i >= sourceRow.size()) {
                    continue;
                }
                double d = DoubleTools.toDouble(sourceRow.get(i), invalidAs);
                if (DoubleTools.invalidDouble(d)) {
                } else if (d < 0) {
                    if ("abs".equals(toNegative)) {
                        tValue += Math.abs(d);
                    }
                } else if (d > 0) {
                    tValue += d;
                }
            }
        } catch (Exception e) {
        }
    }

    public void handlePercentageAll() {
        try {
            List<String> row = new ArrayList<>();
            row.add(message("Row") + rowIndex);
            for (int c = 0; c < colsLen; c++) {
                int i = cols.get(c);
                double d;
                if (i >= 0 && i < sourceRow.size()) {
                    d = DoubleTools.toDouble(sourceRow.get(i), invalidAs);
                } else {
                    d = invalidAs;
                }
                if (withValues) {
                    if (DoubleTools.invalidDouble(d)) {
                        row.add(Double.NaN + "");
                    } else {
                        row.add(DoubleTools.format(d, scale));
                    }
                }
                if (DoubleTools.invalidDouble(d) || tValue == 0) {
                    row.add(Double.NaN + "");
                } else {
                    if (d < 0) {
                        if ("skip".equals(toNegative)) {
                            row.add(Double.NaN + "");
                            continue;
                        } else if ("abs".equals(toNegative)) {
                            d = Math.abs(d);
                        } else {
                            d = 0;
                        }
                    }
                    row.add(DoubleTools.percentage(d, tValue, scale));
                }
            }
            csvPrinter.printRecord(row);
        } catch (Exception e) {
        }
    }

    public void handlePercentageRows() {
        try {
            List<String> row = new ArrayList<>();
            row.add(message("Row") + rowIndex);
            double sum = 0;
            for (int c = 0; c < colsLen; c++) {
                int i = cols.get(c);
                if (i >= 0 && i < sourceRow.size()) {
                    double d = DoubleTools.toDouble(sourceRow.get(i), invalidAs);
                    if (DoubleTools.invalidDouble(d)) {
                    } else if (d < 0) {
                        if ("abs".equals(toNegative)) {
                            sum += Math.abs(d);
                        }
                    } else if (d > 0) {
                        sum += d;
                    }
                }
            }
            row.add(DoubleTools.scale(sum, scale) + "");
            for (int c = 0; c < colsLen; c++) {
                int i = cols.get(c);
                double d = 0;
                if (i >= 0 && i < sourceRow.size()) {
                    d = DoubleTools.toDouble(sourceRow.get(i), invalidAs);
                }
                if (withValues) {
                    if (DoubleTools.invalidDouble(d)) {
                        row.add(Double.NaN + "");
                    } else {
                        row.add(DoubleTools.format(d, scale));
                    }
                }
                if (DoubleTools.invalidDouble(d) || sum == 0) {
                    row.add(Double.NaN + "");
                } else {
                    if (d < 0) {
                        if ("skip".equals(toNegative)) {
                            row.add(Double.NaN + "");
                            continue;
                        } else if ("abs".equals(toNegative)) {
                            d = Math.abs(d);
                        } else {
                            d = 0;
                        }
                    }
                    row.add(DoubleTools.percentage(d, sum, scale));
                }
            }
            csvPrinter.printRecord(row);
        } catch (Exception e) {
        }
    }

    public void handleFrequency() {
        try {
            frequency.addValue(sourceRow.get(colIndex));
        } catch (Exception e) {
        }
    }

    public void handleNormalizeMinMaxColumns() {
        try {
            List<String> row = new ArrayList<>();
            if (includeRowNumber) {
                row.add(message("Row") + rowIndex);
            }
            for (int c = 0; c < colsLen; c++) {
                int i = cols.get(c);
                if (i < 0 || i >= sourceRow.size()) {
                    row.add(null);
                } else {
                    double v = statisticData[c].toDouble(sourceRow.get(i));
                    if (DoubleTools.invalidDouble(v)) {
                        row.add(Double.NaN + "");
                    } else {
                        v = from + statisticData[c].dTmp * (v - statisticData[c].minimum);
                        row.add(DoubleTools.scale(v, scale) + "");
                    }
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
                row.add(message("Row") + rowIndex);
            }
            for (int c = 0; c < colsLen; c++) {
                int i = cols.get(c);
                if (i < 0 || i >= sourceRow.size()) {
                    row.add(null);
                } else {
                    double v = DoubleTools.toDouble(sourceRow.get(i), invalidAs);
                    if (DoubleTools.invalidDouble(v)) {
                        row.add(Double.NaN + "");
                    } else {
                        v = v * colValues[c];
                        row.add(DoubleTools.scale(v, scale) + "");
                    }
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
                row.add(message("Row") + rowIndex);
            }
            for (int c = 0; c < colsLen; c++) {
                int i = cols.get(c);
                if (i < 0 || i >= sourceRow.size()) {
                    row.add(null);
                } else {
                    double v = statisticData[c].toDouble(sourceRow.get(i));
                    if (DoubleTools.invalidDouble(v)) {
                        row.add(Double.NaN + "");
                    } else {
                        double k = statisticData[c].getPopulationStandardDeviation();
                        if (k == 0) {
                            k = AppValues.TinyDouble;
                        }
                        v = (v - statisticData[c].mean) / k;
                        row.add(DoubleTools.scale(v, scale) + "");
                    }
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
                row.add(message("Row") + rowIndex);
            }
            for (int c = 0; c < colsLen; c++) {
                int i = cols.get(c);
                if (i < 0 || i >= sourceRow.size()) {
                    row.add(null);
                } else {
                    double v = statisticAll.toDouble(sourceRow.get(i));
                    if (DoubleTools.invalidDouble(v)) {
                        row.add(Double.NaN + "");
                    } else {
                        v = from + statisticAll.dTmp * (v - statisticAll.minimum);
                        row.add(DoubleTools.scale(v, scale) + "");
                    }
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
                row.add(message("Row") + rowIndex);
            }
            for (int c = 0; c < colsLen; c++) {
                int i = cols.get(c);
                if (i < 0 || i >= sourceRow.size()) {
                    row.add(null);
                } else {
                    double v = DoubleTools.toDouble(sourceRow.get(i), invalidAs);
                    if (DoubleTools.invalidDouble(v)) {
                        row.add(Double.NaN + "");
                    } else {
                        v = v * tValue;
                        row.add(DoubleTools.scale(v, scale) + "");
                    }
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
                row.add(message("Row") + rowIndex);
            }
            for (int c = 0; c < colsLen; c++) {
                int i = cols.get(c);
                if (i < 0 || i >= sourceRow.size()) {
                    row.add(null);
                } else {
                    double v = statisticAll.toDouble(sourceRow.get(i));
                    if (DoubleTools.invalidDouble(v)) {
                        row.add(Double.NaN + "");
                    } else {
                        double k = statisticAll.getPopulationStandardDeviation();
                        if (k == 0) {
                            k = AppValues.TinyDouble;
                        }
                        v = (v - statisticAll.mean) / k;
                        row.add(DoubleTools.scale(v, scale) + "");
                    }
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
                row.add(message("Row") + rowIndex);
            }
            double[] values = new double[colsLen];
            for (int c = 0; c < colsLen; c++) {
                int i = cols.get(c);
                if (i < 0 || i >= sourceRow.size()) {
                    values[c] = 0;
                } else {
                    values[c] = DoubleTools.toDouble(sourceRow.get(i), invalidAs);
                }
            }
            values = Normalization.create()
                    .setA(a).setFrom(from).setTo(to).setInvalidAs(invalidAs)
                    .setSourceVector(values)
                    .calculate();
            if (values == null) {
                return;
            }
            for (double d : values) {
                if (DoubleTools.invalidDouble(d)) {
                    row.add(Double.NaN + "");
                } else {
                    row.add(DoubleTools.scale(d, scale) + "");
                }
            }
            csvPrinter.printRecord(row);
        } catch (Exception e) {
        }
    }

    public void handleSimpleLinearRegression() {
        try {
            double x = DoubleTools.toDouble(sourceRow.get(cols.get(0)), invalidAs);
            double y = DoubleTools.toDouble(sourceRow.get(cols.get(1)), invalidAs);
            List<String> row = simpleRegression.addData(rowIndex, x, y);
            if (csvPrinter != null) {
                csvPrinter.printRecord(row);
            }
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
    }

    public void afterScanned() {
        try {
            data2D.stopFilter();
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
        }
    }

    public void afterScanned2() {
        try {
            data2D.stopFilter();
            switch (operation) {
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
            if (task != null) {
                task.setError(e.toString());
            }
        }
    }

    public boolean readerStopped() {
        return readerStopped || (needCheckTask && (task == null || task.isCancelled()));
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

    public Data2DReader setTask(SingletonTask task) {
        this.task = task;
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
        this.writerTable = dataTable;
        if (dataTable != null) {
            writerTableData2D = dataTable.getTableData2D();
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

    public Data2DReader setToNegative(String toNegative) {
        this.toNegative = toNegative;
        return this;
    }

    public Data2DReader setScanPass(int scanPass) {
        this.scanPass = scanPass;
        return this;
    }

    public Data2DReader setScript(String script) {
        this.script = script;
        return this;
    }

    public Data2DReader setName(String name) {
        this.name = name;
        return this;
    }

    public Data2DReader setErrorContinue(boolean errorContinue) {
        this.errorContinue = errorContinue;
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

    public Data2DReader setStatisticCalculation(DescriptiveStatistic statisticCalculation) {
        this.statisticCalculation = statisticCalculation;
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

    public double getInvalidAs() {
        return invalidAs;
    }

    public Data2DReader setInvalidAs(double invalidAs) {
        this.invalidAs = invalidAs;
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
