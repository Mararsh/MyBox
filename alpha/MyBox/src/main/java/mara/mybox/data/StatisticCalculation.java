package mara.mybox.data;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mara.mybox.controller.BaseData2DHandleController;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.StringTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-4-14
 * @License Apache License Version 2.0
 */
public class StatisticCalculation {

    public boolean count, sum, mean, geometricMean, sumSquares,
            populationVariance, sampleVariance, populationStandardDeviation, sampleStandardDeviation, skewness,
            minimum, maximum, median, upperQuartile, lowerQuartile, mode;
    public int scale;

    protected BaseData2DHandleController handleController;
    protected SingletonTask<Void> task;
    protected Data2D data2D;
    protected List<String> countRow, summationRow, meanRow, geometricMeanRow, sumOfSquaresRow,
            populationVarianceRow, sampleVarianceRow, populationStandardDeviationRow, sampleStandardDeviationRow, skewnessRow,
            maximumRow, minimumRow, medianRow, upperQuartileRow, lowerQuartileRow, modeRow;
    protected List<List<String>> outputData;
    protected List<Data2DColumn> outputColumns;
    protected String categoryName;
    protected List<String> colsNames, outputNames;
    protected List<Integer> colsIndices;

    public StatisticObject statisticObject = StatisticObject.Columns;

    public enum StatisticObject {
        Columns, Rows, All
    }

    public static StatisticCalculation all(boolean select) {
        return new StatisticCalculation()
                .setCount(select)
                .setSum(select)
                .setMean(select)
                .setGeometricMean(select)
                .setSumSquares(select)
                .setPopulationStandardDeviation(select)
                .setPopulationVariance(select)
                .setSampleStandardDeviation(select)
                .setSampleVariance(select)
                .setSkewness(select)
                .setMaximum(select)
                .setMinimum(select)
                .setMedian(select)
                .setUpperQuartile(select)
                .setLowerQuartile(select)
                .setMode(select);
    }

    public boolean needVariance() {
        return populationVariance || sampleVariance || populationStandardDeviation || sampleStandardDeviation;
    }

    public boolean needPercentile() {
        return median || upperQuartile || lowerQuartile;
    }

    public boolean needStored() {
        return needPercentile() || mode;
    }

    public boolean prepare() {
        try {
            switch (statisticObject) {
                case Rows:
                    return prepareByRows();
                case All:
                    return prepareByColumns("", Arrays.asList(message("All")));
                default:
                    return prepareByColumns(message("Column") + "-", colsNames);
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean prepareByColumns(String prefix, List<String> names) {
        try {
            if (names == null || names.isEmpty()) {
                return false;
            }
            String cName = prefix + message("Calculation");
            while (names.contains(cName)) {
                cName += "m";
            }
            outputNames = new ArrayList<>();
            outputNames.add(cName);
            outputNames.addAll(names);

            outputColumns = new ArrayList<>();
            outputColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.String, 300));
            for (String name : names) {
                outputColumns.add(new Data2DColumn(name, ColumnDefinition.ColumnType.Double));
            }

            outputData = new ArrayList<>();

            countRow = null;
            if (count) {
                countRow = new ArrayList<>();
                countRow.add(prefix + message("Count"));
                outputData.add(countRow);
            }

            summationRow = null;
            if (sum) {
                summationRow = new ArrayList<>();
                summationRow.add(prefix + message("Summation"));
                outputData.add(summationRow);
            }

            meanRow = null;
            if (mean) {
                meanRow = new ArrayList<>();
                meanRow.add(prefix + message("Mean"));
                outputData.add(meanRow);
            }

            geometricMeanRow = null;
            if (geometricMean) {
                geometricMeanRow = new ArrayList<>();
                geometricMeanRow.add(prefix + message("GeometricMean"));
                outputData.add(geometricMeanRow);
            }

            sumOfSquaresRow = null;
            if (sumSquares) {
                sumOfSquaresRow = new ArrayList<>();
                sumOfSquaresRow.add(prefix + message("SumOfSquares"));
                outputData.add(sumOfSquaresRow);
            }

            populationVarianceRow = null;
            if (populationVariance) {
                populationVarianceRow = new ArrayList<>();
                populationVarianceRow.add(prefix + message("PopulationVariance"));
                outputData.add(populationVarianceRow);
            }

            sampleVarianceRow = null;
            if (sampleVariance) {
                sampleVarianceRow = new ArrayList<>();
                sampleVarianceRow.add(prefix + message("SampleVariance"));
                outputData.add(sampleVarianceRow);
            }

            populationStandardDeviationRow = null;
            if (populationStandardDeviation) {
                populationStandardDeviationRow = new ArrayList<>();
                populationStandardDeviationRow.add(prefix + message("PopulationStandardDeviation"));
                outputData.add(populationStandardDeviationRow);
            }

            sampleStandardDeviationRow = null;
            if (sampleStandardDeviation) {
                sampleStandardDeviationRow = new ArrayList<>();
                sampleStandardDeviationRow.add(prefix + message("SampleStandardDeviation"));
                outputData.add(sampleStandardDeviationRow);
            }

            skewnessRow = null;
            if (skewness) {
                skewnessRow = new ArrayList<>();
                skewnessRow.add(prefix + message("Skewness"));
                outputData.add(skewnessRow);
            }

            minimumRow = null;
            if (minimum) {
                minimumRow = new ArrayList<>();
                minimumRow.add(prefix + message("MinimumQ0"));
                outputData.add(minimumRow);
            }

            lowerQuartileRow = null;
            if (lowerQuartile) {
                lowerQuartileRow = new ArrayList<>();
                lowerQuartileRow.add(prefix + message("LowerQuartile"));
                outputData.add(lowerQuartileRow);
            }

            medianRow = null;
            if (median) {
                medianRow = new ArrayList<>();
                medianRow.add(prefix + message("Median"));
                outputData.add(medianRow);
            }

            upperQuartileRow = null;
            if (upperQuartile) {
                upperQuartileRow = new ArrayList<>();
                upperQuartileRow.add(prefix + message("UpperQuartile"));
                outputData.add(upperQuartileRow);
            }

            maximumRow = null;
            if (maximum) {
                maximumRow = new ArrayList<>();
                maximumRow.add(prefix + message("MaximumQ4"));
                outputData.add(maximumRow);
            }

            modeRow = null;
            if (mode) {
                modeRow = new ArrayList<>();
                modeRow.add(prefix + message("Mode"));
                outputData.add(modeRow);
            }

            if (outputData.size() < 1) {
                handleController.popError(prefix + message("SelectToHandle"));
                return false;
            }

            return true;
        } catch (Exception e) {
            handleController.popError(e.toString());
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean prepareByRows() {
        try {
            outputNames = new ArrayList<>();
            outputColumns = new ArrayList<>();

            String cName = categoryName != null ? categoryName : message("SourceRowNumber");
            outputNames.add(cName);
            outputColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.String));

            String prefix = message("Rows") + "-";
            int width = 150;
            if (count) {
                cName = prefix + message("Count");
                outputNames.add(cName);
                outputColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.Double, width));
            }

            if (sum) {
                cName = prefix + message("Summation");
                outputNames.add(cName);
                outputColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.Double, width));
            }

            if (mean) {
                cName = prefix + message("Mean");
                outputNames.add(cName);
                outputColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.Double, width));
            }

            if (geometricMean) {
                cName = prefix + message("GeometricMean");
                outputNames.add(cName);
                outputColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.Double, width));
            }

            if (sumSquares) {
                cName = prefix + message("SumOfSquares");
                outputNames.add(cName);
                outputColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.Double, width));
            }

            if (populationVariance) {
                cName = prefix + message("PopulationVariance");
                outputNames.add(cName);
                outputColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.Double, width));
            }

            if (sampleVariance) {
                cName = prefix + message("SampleVariance");
                outputNames.add(cName);
                outputColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.Double, width));
            }

            if (populationStandardDeviation) {
                cName = prefix + message("PopulationStandardDeviation");
                outputNames.add(cName);
                outputColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.Double, width));
            }

            if (sampleStandardDeviation) {
                cName = prefix + message("SampleStandardDeviation");
                outputNames.add(cName);
                outputColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.Double, width));
            }

            if (skewness) {
                cName = prefix + message("Skewness");
                outputNames.add(cName);
                outputColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.Double, width));
            }

            if (minimum) {
                cName = prefix + message("MinimumQ0");
                outputNames.add(cName);
                outputColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.Double, width));
            }

            if (lowerQuartile) {
                cName = prefix + message("LowerQuartile");
                outputNames.add(cName);
                outputColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.Double, width));
            }

            if (median) {
                cName = prefix + message("Median");
                outputNames.add(cName);
                outputColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.Double, width));
            }

            if (upperQuartile) {
                cName = prefix + message("UpperQuartile");
                outputNames.add(cName);
                outputColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.Double, width));
            }

            if (maximum) {
                cName = prefix + message("MaximumQ4");
                outputNames.add(cName);
                outputColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.Double, width));
            }

            if (mode) {
                cName = prefix + message("Mode");
                outputNames.add(cName);
                outputColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.Double, width));
            }

            if (outputNames.size() < 2) {
                handleController.popError(message("SelectToHandle"));
                return false;
            }

            return true;
        } catch (Exception e) {
            handleController.popError(e.toString());
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean statisticData(List<List<String>> rows) {
        try {
            if (rows == null || rows.isEmpty()) {
                if (task != null) {
                    task.setError(message("SelectToHandle"));
                }
                return false;
            }
            switch (statisticObject) {
                case Rows:
                    return statisticByRows(rows);
                case All:
                    return statisticByAll(rows);
                default:
                    return statisticByColumns(rows);
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean statisticByColumns(List<List<String>> rows) {
        try {
            if (rows == null || rows.isEmpty()) {
                return false;
            }
            int rowsNumber = rows.size();
            int colsNumber = rows.get(0).size();
            for (int c = 0; c < colsNumber; c++) {
                String[] colData = new String[rowsNumber];
                for (int r = 0; r < rowsNumber; r++) {
                    colData[r] = rows.get(r).get(c);
                }
                DoubleStatistic statistic = new DoubleStatistic(colData, this);
                statisticByColumnsWrite(statistic);
            }
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean statisticByColumnsWrite(DoubleStatistic statistic) {
        if (statistic == null) {
            return false;
        }
        if (countRow != null) {
            countRow.add(StringTools.format(statistic.getCount()));
        }
        if (summationRow != null) {
            summationRow.add(DoubleTools.format(statistic.getSum(), scale));
        }
        if (meanRow != null) {
            meanRow.add(DoubleTools.format(statistic.getMean(), scale));
        }
        if (maximumRow != null) {
            maximumRow.add(DoubleTools.format(statistic.getMaximum(), scale));
        }
        if (minimumRow != null) {
            minimumRow.add(DoubleTools.format(statistic.getMinimum(), scale));
        }
        if (geometricMeanRow != null) {
            geometricMeanRow.add(DoubleTools.format(statistic.getGeometricMean(), scale));
        }
        if (sumOfSquaresRow != null) {
            sumOfSquaresRow.add(DoubleTools.format(statistic.getSumSquares(), scale));
        }
        if (populationVarianceRow != null) {
            populationVarianceRow.add(DoubleTools.format(statistic.getPopulationVariance(), scale));
        }
        if (sampleVarianceRow != null) {
            sampleVarianceRow.add(DoubleTools.format(statistic.getSampleVariance(), scale));
        }
        if (populationStandardDeviationRow != null) {
            populationStandardDeviationRow.add(DoubleTools.format(statistic.getPopulationStandardDeviation(), scale));
        }
        if (sampleStandardDeviationRow != null) {
            sampleStandardDeviationRow.add(DoubleTools.format(statistic.getSampleStandardDeviation(), scale));
        }
        if (skewnessRow != null) {
            skewnessRow.add(DoubleTools.format(statistic.getSkewness(), scale));
        }
        if (medianRow != null) {
            medianRow.add(DoubleTools.format(statistic.getMedian(), scale));
        }
        if (upperQuartileRow != null) {
            upperQuartileRow.add(DoubleTools.format(statistic.getUpperQuartile(), scale));
        }
        if (lowerQuartileRow != null) {
            lowerQuartileRow.add(DoubleTools.format(statistic.getLowerQuartile(), scale));
        }
        if (modeRow != null) {
            try {
                modeRow.add(DoubleTools.format((double) statistic.getMode(), scale));
            } catch (Exception e) {
                modeRow.add(statistic.getMode().toString());
            }
        }
        return true;
    }

    public boolean statisticByRows(List<List<String>> rows) {
        try {
            if (rows == null || rows.isEmpty()) {
                return false;
            }
            outputData = new ArrayList<>();
            int rowsNumber = rows.size();
            for (int r = 0; r < rowsNumber; r++) {
                List<String> rowStatistic = new ArrayList<>();
                List<String> row = rows.get(r);
                if (categoryName == null) {
                    rowStatistic.add(message("Row") + row.get(0));
                } else {
                    rowStatistic.add(row.get(0));
                }
                int colsNumber = row.size();
                String[] rowData = new String[colsNumber - 1];
                for (int c = 1; c < colsNumber; c++) {
                    rowData[c - 1] = row.get(c);
                }
                DoubleStatistic statistic = new DoubleStatistic(rowData, this);
                rowStatistic.addAll(statistic.toStringList());
                outputData.add(rowStatistic);
            }
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean statisticByAll(List<List<String>> rows) {
        try {
            if (rows == null || rows.isEmpty()) {
                return false;
            }
            int rowsNumber = rows.size();
            int colsNumber = rows.get(0).size();
            String[] allData = new String[rowsNumber * colsNumber];
            int index = 0;
            for (int r = 0; r < rowsNumber; r++) {
                for (int c = 0; c < colsNumber; c++) {
                    allData[index++] = rows.get(r).get(c);
                }
            }
            DoubleStatistic statistic = new DoubleStatistic(allData, this);
            statisticByColumnsWrite(statistic);
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean statisticAllByColumnsWithoutStored() {
        DoubleStatistic[] statisticData = data2D.statisticByColumns(colsIndices, this);
        if (statisticData == null) {
            return false;
        }
        for (DoubleStatistic statistic : statisticData) {
            statisticByColumnsWrite(statistic);
        }
        return true;
    }

    public boolean statisticAllByColumnsInDataTable() {
        if (!statisticAllByColumnsWithoutStored() || !(data2D instanceof DataTable)) {
            return false;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            DataTable dataTable = (DataTable) data2D;
            for (int c : colsIndices) {
                Data2DColumn column = data2D.getColumns().get(c);
                if (medianRow != null) {
                    Object m = dataTable.percentile(conn, column, 50);
                    if (column.isNumberType()) {
                        medianRow.add(DoubleTools.format(Double.valueOf(m + ""), scale));
                    } else {
                        medianRow.add(column.toString(m));
                    }
                }
                if (upperQuartileRow != null) {
                    Object o = dataTable.percentile(conn, column, 75);
                    if (column.isNumberType()) {
                        upperQuartileRow.add(DoubleTools.format(Double.valueOf(o + ""), scale));
                    } else {
                        upperQuartileRow.add(column.toString(o));
                    }
                }
                if (lowerQuartileRow != null) {
                    Object o = dataTable.percentile(conn, column, 25);
                    if (column.isNumberType()) {
                        lowerQuartileRow.add(DoubleTools.format(Double.valueOf(o + ""), scale));
                    } else {
                        lowerQuartileRow.add(column.toString(o));
                    }
                }
                if (modeRow != null) {
                    Object m = dataTable.mode(conn, column.getColumnName());
                    if (column.isNumberType()) {
                        modeRow.add(DoubleTools.format(Double.valueOf(m + ""), scale));
                    } else {
                        modeRow.add(column.toString(m));
                    }
                }
            }
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e.toString());
            return false;
        }
    }


    /*
        get/set
     */
    public boolean isCount() {
        return count;
    }

    public StatisticCalculation setCount(boolean count) {
        this.count = count;
        return this;
    }

    public boolean isSum() {
        return sum;
    }

    public StatisticCalculation setSum(boolean sum) {
        this.sum = sum;
        return this;
    }

    public boolean isMean() {
        return mean;
    }

    public StatisticCalculation setMean(boolean mean) {
        this.mean = mean;
        return this;
    }

    public boolean isGeometricMean() {
        return geometricMean;
    }

    public StatisticCalculation setGeometricMean(boolean geometricMean) {
        this.geometricMean = geometricMean;
        return this;
    }

    public boolean isMinimum() {
        return minimum;
    }

    public StatisticCalculation setMinimum(boolean minimum) {
        this.minimum = minimum;
        return this;
    }

    public boolean isMaximum() {
        return maximum;
    }

    public StatisticCalculation setMaximum(boolean maximum) {
        this.maximum = maximum;
        return this;
    }

    public boolean isSumSquares() {
        return sumSquares;
    }

    public StatisticCalculation setSumSquares(boolean sumSquares) {
        this.sumSquares = sumSquares;
        return this;
    }

    public boolean isPopulationVariance() {
        return populationVariance;
    }

    public StatisticCalculation setPopulationVariance(boolean populationVariance) {
        this.populationVariance = populationVariance;
        return this;
    }

    public boolean isSampleVariance() {
        return sampleVariance;
    }

    public StatisticCalculation setSampleVariance(boolean sampleVariance) {
        this.sampleVariance = sampleVariance;
        return this;
    }

    public boolean isPopulationStandardDeviation() {
        return populationStandardDeviation;
    }

    public StatisticCalculation setPopulationStandardDeviation(boolean populationStandardDeviation) {
        this.populationStandardDeviation = populationStandardDeviation;
        return this;
    }

    public boolean isSampleStandardDeviation() {
        return sampleStandardDeviation;
    }

    public StatisticCalculation setSampleStandardDeviation(boolean sampleStandardDeviation) {
        this.sampleStandardDeviation = sampleStandardDeviation;
        return this;
    }

    public boolean isSkewness() {
        return skewness;
    }

    public StatisticCalculation setSkewness(boolean skewness) {
        this.skewness = skewness;
        return this;
    }

    public boolean isMode() {
        return mode;
    }

    public StatisticCalculation setMode(boolean mode) {
        this.mode = mode;
        return this;
    }

    public boolean isMedian() {
        return median;
    }

    public StatisticCalculation setMedian(boolean median) {
        this.median = median;
        return this;
    }

    public boolean isUpperQuartile() {
        return upperQuartile;
    }

    public StatisticCalculation setUpperQuartile(boolean upperQuartile) {
        this.upperQuartile = upperQuartile;
        return this;
    }

    public boolean isLowerQuartile() {
        return lowerQuartile;
    }

    public StatisticCalculation setLowerQuartile(boolean lowerQuartile) {
        this.lowerQuartile = lowerQuartile;
        return this;
    }

    public StatisticObject getStatisticObject() {
        return statisticObject;
    }

    public StatisticCalculation setStatisticObject(StatisticObject statisticObject) {
        this.statisticObject = statisticObject;
        return this;
    }

    public int getScale() {
        return scale;
    }

    public StatisticCalculation setScale(int scale) {
        this.scale = scale;
        return this;
    }

    public BaseData2DHandleController getHandleController() {
        return handleController;
    }

    public StatisticCalculation setHandleController(BaseData2DHandleController handleController) {
        this.handleController = handleController;
        return this;
    }

    public List<String> getCountRow() {
        return countRow;
    }

    public StatisticCalculation setCountRow(List<String> countRow) {
        this.countRow = countRow;
        return this;
    }

    public List<String> getSummationRow() {
        return summationRow;
    }

    public StatisticCalculation setSummationRow(List<String> summationRow) {
        this.summationRow = summationRow;
        return this;
    }

    public List<String> getMeanRow() {
        return meanRow;
    }

    public StatisticCalculation setMeanRow(List<String> meanRow) {
        this.meanRow = meanRow;
        return this;
    }

    public List<String> getGeometricMeanRow() {
        return geometricMeanRow;
    }

    public StatisticCalculation setGeometricMeanRow(List<String> geometricMeanRow) {
        this.geometricMeanRow = geometricMeanRow;
        return this;
    }

    public List<String> getSumOfSquaresRow() {
        return sumOfSquaresRow;
    }

    public StatisticCalculation setSumOfSquaresRow(List<String> sumOfSquaresRow) {
        this.sumOfSquaresRow = sumOfSquaresRow;
        return this;
    }

    public List<String> getPopulationVarianceRow() {
        return populationVarianceRow;
    }

    public StatisticCalculation setPopulationVarianceRow(List<String> populationVarianceRow) {
        this.populationVarianceRow = populationVarianceRow;
        return this;
    }

    public List<String> getSampleVarianceRow() {
        return sampleVarianceRow;
    }

    public StatisticCalculation setSampleVarianceRow(List<String> sampleVarianceRow) {
        this.sampleVarianceRow = sampleVarianceRow;
        return this;
    }

    public List<String> getPopulationStandardDeviationRow() {
        return populationStandardDeviationRow;
    }

    public StatisticCalculation setPopulationStandardDeviationRow(List<String> populationStandardDeviationRow) {
        this.populationStandardDeviationRow = populationStandardDeviationRow;
        return this;
    }

    public List<String> getSampleStandardDeviationRow() {
        return sampleStandardDeviationRow;
    }

    public StatisticCalculation setSampleStandardDeviationRow(List<String> sampleStandardDeviationRow) {
        this.sampleStandardDeviationRow = sampleStandardDeviationRow;
        return this;

    }

    public List<String> getSkewnessRow() {
        return skewnessRow;
    }

    public StatisticCalculation setSkewnessRow(List<String> skewnessRow) {
        this.skewnessRow = skewnessRow;
        return this;
    }

    public List<String> getMaximumRow() {
        return maximumRow;
    }

    public StatisticCalculation setMaximumRow(List<String> maximumRow) {
        this.maximumRow = maximumRow;
        return this;
    }

    public List<String> getMinimumRow() {
        return minimumRow;
    }

    public StatisticCalculation setMinimumRow(List<String> minimumRow) {
        this.minimumRow = minimumRow;
        return this;
    }

    public List<String> getMedianRow() {
        return medianRow;
    }

    public StatisticCalculation setMedianRow(List<String> medianRow) {
        this.medianRow = medianRow;
        return this;
    }

    public List<String> getUpperQuartileRow() {
        return upperQuartileRow;
    }

    public StatisticCalculation setUpperQuartileRow(List<String> upperQuartileRow) {
        this.upperQuartileRow = upperQuartileRow;
        return this;
    }

    public List<String> getLowerQuartileRow() {
        return lowerQuartileRow;
    }

    public StatisticCalculation setLowerQuartileRow(List<String> lowerQuartileRow) {
        this.lowerQuartileRow = lowerQuartileRow;
        return this;
    }

    public List<String> getModeRow() {
        return modeRow;
    }

    public StatisticCalculation setModeRow(List<String> modeRow) {
        this.modeRow = modeRow;
        return this;
    }

    public SingletonTask<Void> getTask() {
        return task;
    }

    public StatisticCalculation setTask(SingletonTask<Void> task) {
        this.task = task;
        return this;
    }

    public Data2D getData2D() {
        return data2D;
    }

    public StatisticCalculation setData2D(Data2D data2D) {
        this.data2D = data2D;
        return this;
    }

    public List<List<String>> getOutputData() {
        return outputData;
    }

    public StatisticCalculation setOutputData(List<List<String>> outputData) {
        this.outputData = outputData;
        return this;
    }

    public List<Data2DColumn> getOutputColumns() {
        return outputColumns;
    }

    public StatisticCalculation setOutputColumns(List<Data2DColumn> outputColumns) {
        this.outputColumns = outputColumns;
        return this;
    }

    public List<String> getOutputNames() {
        return outputNames;
    }

    public StatisticCalculation setHandledNames(List<String> handledNames) {
        this.outputNames = handledNames;
        return this;
    }

    public List<String> getColsNames() {
        return colsNames;
    }

    public StatisticCalculation setColsNames(List<String> colsNames) {
        this.colsNames = colsNames;
        return this;
    }

    public List<Integer> getColsIndices() {
        return colsIndices;
    }

    public StatisticCalculation setColsIndices(List<Integer> colsIndices) {
        this.colsIndices = colsIndices;
        return this;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

}
