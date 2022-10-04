package mara.mybox.calculation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import mara.mybox.controller.BaseData2DHandleController;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.Data2D_Attributes.InvalidAs;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.tools.NumberTools;
import mara.mybox.tools.StringTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-4-14
 * @License Apache License Version 2.0
 */
public class DescriptiveStatistic {

    public boolean count, sum, mean, geometricMean, sumSquares,
            populationVariance, sampleVariance, populationStandardDeviation, sampleStandardDeviation, skewness,
            minimum, maximum, median, upperQuartile, lowerQuartile, mode,
            upperMildOutlierLine, upperExtremeOutlierLine, lowerMildOutlierLine, lowerExtremeOutlierLine;
    public int scale;
    public InvalidAs invalidAs;

    protected BaseData2DHandleController handleController;
    protected SingletonTask<Void> task;
    protected Data2D data2D;
    protected List<String> countRow, summationRow, meanRow, geometricMeanRow, sumOfSquaresRow,
            populationVarianceRow, sampleVarianceRow, populationStandardDeviationRow, sampleStandardDeviationRow, skewnessRow,
            maximumRow, minimumRow, medianRow, upperQuartileRow, lowerQuartileRow, modeRow,
            upperMildOutlierLineRow, upperExtremeOutlierLineRow, lowerMildOutlierLineRow, lowerExtremeOutlierLineRow;
    protected List<List<String>> outputData;
    protected List<Data2DColumn> outputColumns;
    protected String categoryName;
    protected List<String> colsNames, outputNames;
    protected List<Integer> colsIndices;

    public StatisticObject statisticObject = StatisticObject.Columns;

    public enum StatisticObject {
        Columns, Rows, All
    }

    public static DescriptiveStatistic all(boolean select) {
        return new DescriptiveStatistic()
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
                .setMode(select)
                .setUpperMildOutlierLine(select)
                .setUpperExtremeOutlierLine(select)
                .setLowerMildOutlierLine(select)
                .setLowerExtremeOutlierLine(select);
    }

    public List<String> list() {
        List<String> list = new ArrayList<>();
        if (count) {
            list.add("count");
        }
        if (sum) {
            list.add("sum");
        }
        if (mean) {
            list.add("mean");
        }
        if (count) {
            list.add("count");
        }
        if (maximum) {
            list.add("maximum");
        }
        if (minimum) {
            list.add("minimum");
        }
        if (skewness) {
            list.add("skewness");
        }
        if (geometricMean) {
            list.add("geometricMean");
        }
        if (sumSquares) {
            list.add("sumSquares");
        }
        if (populationVariance) {
            list.add("populationVariance");
        }
        if (sampleVariance) {
            list.add("sampleVariance");
        }
        if (populationStandardDeviation) {
            list.add("populationStandardDeviation");
        }
        if (sampleStandardDeviation) {
            list.add("sampleStandardDeviation");
        }
        if (mode) {
            list.add("mode");
        }
        if (median) {
            list.add("median");
        }
        if (upperQuartile) {
            list.add("upperQuartile");
        }
        if (lowerQuartile) {
            list.add("lowerQuartile");
        }
        if (upperMildOutlierLine) {
            list.add("upperMildOutlierLine");
        }
        if (upperExtremeOutlierLine) {
            list.add("upperExtremeOutlierLine");
        }
        if (lowerMildOutlierLine) {
            list.add("lowerMildOutlierLine");
        }
        if (lowerExtremeOutlierLine) {
            list.add("lowerExtremeOutlierLine");
        }
        return list;
    }

    public boolean need() {
        return needNonStored() || needStored();
    }

    public boolean needNonStored() {
        return minimum || maximum || mean || sum || count || skewness
                || geometricMean || sumSquares || needVariance();
    }

    public boolean needVariance() {
        return populationVariance || sampleVariance || populationStandardDeviation || sampleStandardDeviation;
    }

    public boolean needStored() {
        return needPercentile() || mode;
    }

    public boolean needPercentile() {
        return median || upperQuartile || lowerQuartile || needOutlier();
    }

    public boolean needOutlier() {
        return upperMildOutlierLine || upperExtremeOutlierLine
                || lowerMildOutlierLine || lowerExtremeOutlierLine;
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
            Random random = new Random();
            while (names.contains(cName)) {
                cName += random.nextInt(10);
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

            upperExtremeOutlierLineRow = null;
            if (upperExtremeOutlierLine) {
                upperExtremeOutlierLineRow = new ArrayList<>();
                upperExtremeOutlierLineRow.add(prefix + message("UpperExtremeOutlierLine"));
                outputData.add(upperExtremeOutlierLineRow);
            }

            upperMildOutlierLineRow = null;
            if (upperMildOutlierLine) {
                upperMildOutlierLineRow = new ArrayList<>();
                upperMildOutlierLineRow.add(prefix + message("UpperMildOutlierLine"));
                outputData.add(upperMildOutlierLineRow);
            }

            lowerMildOutlierLineRow = null;
            if (lowerMildOutlierLine) {
                lowerMildOutlierLineRow = new ArrayList<>();
                lowerMildOutlierLineRow.add(prefix + message("LowerMildOutlierLine"));
                outputData.add(lowerMildOutlierLineRow);
            }

            lowerExtremeOutlierLineRow = null;
            if (lowerExtremeOutlierLine) {
                lowerExtremeOutlierLineRow = new ArrayList<>();
                lowerExtremeOutlierLineRow.add(prefix + message("LowerExtremeOutlierLine"));
                outputData.add(lowerExtremeOutlierLineRow);
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

            if (upperExtremeOutlierLine) {
                cName = prefix + message("UpperExtremeOutlierLine");
                outputNames.add(cName);
                outputColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.Double, width));
            }

            if (upperMildOutlierLine) {
                cName = prefix + message("UpperMildOutlierLine");
                outputNames.add(cName);
                outputColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.Double, width));
            }

            if (lowerMildOutlierLine) {
                cName = prefix + message("LowerMildOutlierLine");
                outputNames.add(cName);
                outputColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.Double, width));
            }

            if (lowerExtremeOutlierLine) {
                cName = prefix + message("LowerExtremeOutlierLine");
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
        statisticByColumnsWriteWithoutStored(statistic);
        statisticByColumnsWriteStored(statistic);
        return true;
    }

    public boolean statisticByColumnsWriteWithoutStored(DoubleStatistic statistic) {
        if (statistic == null) {
            return false;
        }
        if (countRow != null) {
            countRow.add(StringTools.format(statistic.getCount()));
        }
        if (summationRow != null) {
            summationRow.add(NumberTools.format(statistic.getSum(), scale));
        }
        if (meanRow != null) {
            meanRow.add(NumberTools.format(statistic.getMean(), scale));
        }
        if (geometricMeanRow != null) {
            geometricMeanRow.add(NumberTools.format(statistic.getGeometricMean(), scale));
        }
        if (sumOfSquaresRow != null) {
            sumOfSquaresRow.add(NumberTools.format(statistic.getSumSquares(), scale));
        }
        if (populationVarianceRow != null) {
            populationVarianceRow.add(NumberTools.format(statistic.getPopulationVariance(), scale));
        }
        if (sampleVarianceRow != null) {
            sampleVarianceRow.add(NumberTools.format(statistic.getSampleVariance(), scale));
        }
        if (populationStandardDeviationRow != null) {
            populationStandardDeviationRow.add(NumberTools.format(statistic.getPopulationStandardDeviation(), scale));
        }
        if (sampleStandardDeviationRow != null) {
            sampleStandardDeviationRow.add(NumberTools.format(statistic.getSampleStandardDeviation(), scale));
        }
        if (skewnessRow != null) {
            skewnessRow.add(NumberTools.format(statistic.getSkewness(), scale));
        }
        if (minimumRow != null) {
            minimumRow.add(NumberTools.format(statistic.getMinimum(), scale));
        }
        if (maximumRow != null) {
            maximumRow.add(NumberTools.format(statistic.getMaximum(), scale));
        }
        return true;
    }

    public boolean statisticByColumnsWriteStored(DoubleStatistic statistic) {
        if (statistic == null) {
            return false;
        }
        if (medianRow != null) {
            Object v = statistic.getMedianValue();
            try {
                medianRow.add(NumberTools.format((double) v, scale));
            } catch (Exception e) {
                try {
                    medianRow.add(v.toString());
                } catch (Exception ex) {
                    medianRow.add("");
                }
            }
        }
        if (modeRow != null) {
            Object v = statistic.getModeValue();
            try {
                modeRow.add(NumberTools.format((double) v, scale));
            } catch (Exception e) {
                try {
                    modeRow.add(v.toString());
                } catch (Exception ex) {
                    modeRow.add("");
                }
            }
        }
        if (lowerQuartileRow != null) {
            Object v = statistic.getLowerQuartileValue();
            try {
                lowerQuartileRow.add(NumberTools.format((double) v, scale));
            } catch (Exception e) {
                try {
                    lowerQuartileRow.add(v.toString());
                } catch (Exception ex) {
                    lowerQuartileRow.add("");
                }
            }
        }

        if (upperQuartileRow != null) {
            Object v = statistic.getUpperQuartileValue();
            try {
                upperQuartileRow.add(NumberTools.format((double) v, scale));
            } catch (Exception e) {
                try {
                    upperQuartileRow.add(v.toString());
                } catch (Exception ex) {
                    upperQuartileRow.add("");
                }
            }
        }
        if (upperExtremeOutlierLineRow != null) {
            upperExtremeOutlierLineRow.add(NumberTools.format((double) statistic.getUpperExtremeOutlierLine(), scale));
        }
        if (upperMildOutlierLineRow != null) {
            upperMildOutlierLineRow.add(NumberTools.format((double) statistic.getUpperMildOutlierLine(), scale));
        }
        if (lowerMildOutlierLineRow != null) {
            lowerMildOutlierLineRow.add(NumberTools.format((double) statistic.getLowerMildOutlierLine(), scale));
        }
        if (lowerExtremeOutlierLineRow != null) {
            lowerExtremeOutlierLineRow.add(NumberTools.format((double) statistic.getLowerExtremeOutlierLine(), scale));
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
        DoubleStatistic[] statisticData = data2D.statisticByColumnsWithoutStored(colsIndices, this);
        if (statisticData == null) {
            return false;
        }
        for (DoubleStatistic statistic : statisticData) {
            statisticByColumnsWriteWithoutStored(statistic);
        }
        return true;
    }

    public boolean statisticAllByColumns() {
        if (!(data2D instanceof DataTable)) {
            return false;
        }
        try {
            DoubleStatistic[] statisticData = data2D.statisticByColumnsWithoutStored(colsIndices, this);
            if (statisticData == null) {
                return false;
            }
            DataTable dataTable = (DataTable) data2D;
            statisticData = dataTable.statisticByColumnsForStored(colsIndices, this);
            if (statisticData == null) {
                return false;
            }
            for (int c : colsIndices) {
                Data2DColumn column = data2D.getColumns().get(c);
                DoubleStatistic colStatistic = column.getStatistic();
                statisticByColumnsWrite(colStatistic);
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

    public DescriptiveStatistic setCount(boolean count) {
        this.count = count;
        return this;
    }

    public boolean isSum() {
        return sum;
    }

    public DescriptiveStatistic setSum(boolean sum) {
        this.sum = sum;
        return this;
    }

    public boolean isMean() {
        return mean;
    }

    public DescriptiveStatistic setMean(boolean mean) {
        this.mean = mean;
        return this;
    }

    public boolean isGeometricMean() {
        return geometricMean;
    }

    public DescriptiveStatistic setGeometricMean(boolean geometricMean) {
        this.geometricMean = geometricMean;
        return this;
    }

    public boolean isMinimum() {
        return minimum;
    }

    public DescriptiveStatistic setMinimum(boolean minimum) {
        this.minimum = minimum;
        return this;
    }

    public boolean isMaximum() {
        return maximum;
    }

    public DescriptiveStatistic setMaximum(boolean maximum) {
        this.maximum = maximum;
        return this;
    }

    public boolean isSumSquares() {
        return sumSquares;
    }

    public DescriptiveStatistic setSumSquares(boolean sumSquares) {
        this.sumSquares = sumSquares;
        return this;
    }

    public boolean isPopulationVariance() {
        return populationVariance;
    }

    public DescriptiveStatistic setPopulationVariance(boolean populationVariance) {
        this.populationVariance = populationVariance;
        return this;
    }

    public boolean isSampleVariance() {
        return sampleVariance;
    }

    public DescriptiveStatistic setSampleVariance(boolean sampleVariance) {
        this.sampleVariance = sampleVariance;
        return this;
    }

    public boolean isPopulationStandardDeviation() {
        return populationStandardDeviation;
    }

    public DescriptiveStatistic setPopulationStandardDeviation(boolean populationStandardDeviation) {
        this.populationStandardDeviation = populationStandardDeviation;
        return this;
    }

    public boolean isSampleStandardDeviation() {
        return sampleStandardDeviation;
    }

    public DescriptiveStatistic setSampleStandardDeviation(boolean sampleStandardDeviation) {
        this.sampleStandardDeviation = sampleStandardDeviation;
        return this;
    }

    public boolean isSkewness() {
        return skewness;
    }

    public DescriptiveStatistic setSkewness(boolean skewness) {
        this.skewness = skewness;
        return this;
    }

    public boolean isMode() {
        return mode;
    }

    public DescriptiveStatistic setMode(boolean mode) {
        this.mode = mode;
        return this;
    }

    public boolean isMedian() {
        return median;
    }

    public DescriptiveStatistic setMedian(boolean median) {
        this.median = median;
        return this;
    }

    public boolean isUpperQuartile() {
        return upperQuartile;
    }

    public DescriptiveStatistic setUpperQuartile(boolean upperQuartile) {
        this.upperQuartile = upperQuartile;
        return this;
    }

    public boolean isLowerQuartile() {
        return lowerQuartile;
    }

    public DescriptiveStatistic setLowerQuartile(boolean lowerQuartile) {
        this.lowerQuartile = lowerQuartile;
        return this;
    }

    public boolean isUpperMildOutlierLine() {
        return upperMildOutlierLine;
    }

    public DescriptiveStatistic setUpperMildOutlierLine(boolean upperMildOutlierLine) {
        this.upperMildOutlierLine = upperMildOutlierLine;
        return this;
    }

    public boolean isUpperExtremeOutlierLine() {
        return upperExtremeOutlierLine;
    }

    public DescriptiveStatistic setUpperExtremeOutlierLine(boolean upperExtremeOutlierLine) {
        this.upperExtremeOutlierLine = upperExtremeOutlierLine;
        return this;
    }

    public boolean isLowerMildOutlierLine() {
        return lowerMildOutlierLine;
    }

    public DescriptiveStatistic setLowerMildOutlierLine(boolean lowerMildOutlierLine) {
        this.lowerMildOutlierLine = lowerMildOutlierLine;
        return this;
    }

    public boolean isLowerExtremeOutlierLine() {
        return lowerExtremeOutlierLine;
    }

    public DescriptiveStatistic setLowerExtremeOutlierLine(boolean lowerExtremeOutlierLine) {
        this.lowerExtremeOutlierLine = lowerExtremeOutlierLine;
        return this;
    }

    public StatisticObject getStatisticObject() {
        return statisticObject;
    }

    public DescriptiveStatistic setStatisticObject(StatisticObject statisticObject) {
        this.statisticObject = statisticObject;
        return this;
    }

    public int getScale() {
        return scale;
    }

    public DescriptiveStatistic setScale(int scale) {
        this.scale = scale;
        return this;
    }

    public BaseData2DHandleController getHandleController() {
        return handleController;
    }

    public DescriptiveStatistic setHandleController(BaseData2DHandleController handleController) {
        this.handleController = handleController;
        return this;
    }

    public List<String> getCountRow() {
        return countRow;
    }

    public DescriptiveStatistic setCountRow(List<String> countRow) {
        this.countRow = countRow;
        return this;
    }

    public List<String> getSummationRow() {
        return summationRow;
    }

    public DescriptiveStatistic setSummationRow(List<String> summationRow) {
        this.summationRow = summationRow;
        return this;
    }

    public List<String> getMeanRow() {
        return meanRow;
    }

    public DescriptiveStatistic setMeanRow(List<String> meanRow) {
        this.meanRow = meanRow;
        return this;
    }

    public List<String> getGeometricMeanRow() {
        return geometricMeanRow;
    }

    public DescriptiveStatistic setGeometricMeanRow(List<String> geometricMeanRow) {
        this.geometricMeanRow = geometricMeanRow;
        return this;
    }

    public List<String> getSumOfSquaresRow() {
        return sumOfSquaresRow;
    }

    public DescriptiveStatistic setSumOfSquaresRow(List<String> sumOfSquaresRow) {
        this.sumOfSquaresRow = sumOfSquaresRow;
        return this;
    }

    public List<String> getPopulationVarianceRow() {
        return populationVarianceRow;
    }

    public DescriptiveStatistic setPopulationVarianceRow(List<String> populationVarianceRow) {
        this.populationVarianceRow = populationVarianceRow;
        return this;
    }

    public List<String> getSampleVarianceRow() {
        return sampleVarianceRow;
    }

    public DescriptiveStatistic setSampleVarianceRow(List<String> sampleVarianceRow) {
        this.sampleVarianceRow = sampleVarianceRow;
        return this;
    }

    public List<String> getPopulationStandardDeviationRow() {
        return populationStandardDeviationRow;
    }

    public DescriptiveStatistic setPopulationStandardDeviationRow(List<String> populationStandardDeviationRow) {
        this.populationStandardDeviationRow = populationStandardDeviationRow;
        return this;
    }

    public List<String> getSampleStandardDeviationRow() {
        return sampleStandardDeviationRow;
    }

    public DescriptiveStatistic setSampleStandardDeviationRow(List<String> sampleStandardDeviationRow) {
        this.sampleStandardDeviationRow = sampleStandardDeviationRow;
        return this;

    }

    public List<String> getSkewnessRow() {
        return skewnessRow;
    }

    public DescriptiveStatistic setSkewnessRow(List<String> skewnessRow) {
        this.skewnessRow = skewnessRow;
        return this;
    }

    public List<String> getMaximumRow() {
        return maximumRow;
    }

    public DescriptiveStatistic setMaximumRow(List<String> maximumRow) {
        this.maximumRow = maximumRow;
        return this;
    }

    public List<String> getMinimumRow() {
        return minimumRow;
    }

    public DescriptiveStatistic setMinimumRow(List<String> minimumRow) {
        this.minimumRow = minimumRow;
        return this;
    }

    public List<String> getMedianRow() {
        return medianRow;
    }

    public DescriptiveStatistic setMedianRow(List<String> medianRow) {
        this.medianRow = medianRow;
        return this;
    }

    public List<String> getUpperQuartileRow() {
        return upperQuartileRow;
    }

    public DescriptiveStatistic setUpperQuartileRow(List<String> upperQuartileRow) {
        this.upperQuartileRow = upperQuartileRow;
        return this;
    }

    public List<String> getLowerQuartileRow() {
        return lowerQuartileRow;
    }

    public DescriptiveStatistic setLowerQuartileRow(List<String> lowerQuartileRow) {
        this.lowerQuartileRow = lowerQuartileRow;
        return this;
    }

    public List<String> getModeRow() {
        return modeRow;
    }

    public DescriptiveStatistic setModeRow(List<String> modeRow) {
        this.modeRow = modeRow;
        return this;
    }

    public List<String> getUpperMildOutlierLineRow() {
        return upperMildOutlierLineRow;
    }

    public DescriptiveStatistic setUpperMildOutlierLineRow(List<String> upperMildOutlierLineRow) {
        this.upperMildOutlierLineRow = upperMildOutlierLineRow;
        return this;
    }

    public List<String> getUpperExtremeOutlierLineRow() {
        return upperExtremeOutlierLineRow;
    }

    public DescriptiveStatistic setUpperExtremeOutlierLineRow(List<String> upperExtremeOutlierLineRow) {
        this.upperExtremeOutlierLineRow = upperExtremeOutlierLineRow;
        return this;
    }

    public List<String> getLowerMildOutlierLineRow() {
        return lowerMildOutlierLineRow;
    }

    public DescriptiveStatistic setLowerMildOutlierLineRow(List<String> lowerMildOutlierLineRow) {
        this.lowerMildOutlierLineRow = lowerMildOutlierLineRow;
        return this;
    }

    public List<String> getLowerExtremeOutlierLineRow() {
        return lowerExtremeOutlierLineRow;
    }

    public DescriptiveStatistic setLowerExtremeOutlierLineRow(List<String> lowerExtremeOutlierLineRow) {
        this.lowerExtremeOutlierLineRow = lowerExtremeOutlierLineRow;
        return this;
    }

    public SingletonTask<Void> getTask() {
        return task;
    }

    public DescriptiveStatistic setTask(SingletonTask<Void> task) {
        this.task = task;
        return this;
    }

    public Data2D getData2D() {
        return data2D;
    }

    public DescriptiveStatistic setData2D(Data2D data2D) {
        this.data2D = data2D;
        return this;
    }

    public List<List<String>> getOutputData() {
        return outputData;
    }

    public DescriptiveStatistic setOutputData(List<List<String>> outputData) {
        this.outputData = outputData;
        return this;
    }

    public List<Data2DColumn> getOutputColumns() {
        return outputColumns;
    }

    public DescriptiveStatistic setOutputColumns(List<Data2DColumn> outputColumns) {
        this.outputColumns = outputColumns;
        return this;
    }

    public List<String> getOutputNames() {
        return outputNames;
    }

    public DescriptiveStatistic setHandledNames(List<String> handledNames) {
        this.outputNames = handledNames;
        return this;
    }

    public List<String> getColsNames() {
        return colsNames;
    }

    public DescriptiveStatistic setColsNames(List<String> colsNames) {
        this.colsNames = colsNames;
        return this;
    }

    public List<Integer> getColsIndices() {
        return colsIndices;
    }

    public DescriptiveStatistic setColsIndices(List<Integer> colsIndices) {
        this.colsIndices = colsIndices;
        return this;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public DescriptiveStatistic setCategoryName(String categoryName) {
        this.categoryName = categoryName;
        return this;
    }

    public InvalidAs getInvalidAs() {
        return invalidAs;
    }

    public DescriptiveStatistic setInvalidAs(InvalidAs invalidAs) {
        this.invalidAs = invalidAs;
        return this;
    }

}
