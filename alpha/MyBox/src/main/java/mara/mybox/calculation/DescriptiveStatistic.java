package mara.mybox.calculation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mara.mybox.controller.BaseData2DHandleController;
import mara.mybox.data2d.Data2D;
import mara.mybox.db.data.ColumnDefinition.InvalidAs;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.DerbyBase;
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

    public List<StatisticType> types = new ArrayList<>();
    public int scale;
    public InvalidAs invalidAs;

    protected BaseData2DHandleController handleController;
    protected SingletonTask<Void> task;
    protected Data2D data2D;
    protected Map<StatisticType, List<String>> statisticRows;
    protected List<List<String>> outputData;
    protected List<Data2DColumn> outputColumns;
    protected String categoryName;
    protected List<String> colsNames, outputNames;
    protected List<Integer> colsIndices;

    public StatisticObject statisticObject = StatisticObject.Columns;

    public enum StatisticType {
        Count, Sum, Mean, GeometricMean, SumOfSquares, Skewness, Mode,
        PopulationVariance, SampleVariance, PopulationStandardDeviation, SampleStandardDeviation,
        MinimumQ0, LowerQuartile, Median, UpperQuartile, MaximumQ4,
        LowerExtremeOutlierLine, LowerMildOutlierLine, UpperMildOutlierLine, UpperExtremeOutlierLine
    }

    public enum StatisticObject {
        Columns, Rows, All
    }

    public static DescriptiveStatistic all(boolean select) {
        DescriptiveStatistic s = new DescriptiveStatistic();
        s.types.clear();
        if (select) {
            s.types.addAll(Arrays.asList(StatisticType.values()));
        }
        return s;
    }

    public DescriptiveStatistic add(StatisticType type) {
        if (!types.contains(type)) {
            types.add(type);
        }
        return this;
    }

    public DescriptiveStatistic remove(StatisticType type) {
        types.remove(type);
        return this;
    }

    public List<String> names() {
        List<String> names = new ArrayList<>();
        for (StatisticType type : types) {
            names.add(message(type.name()));
        }
        return names;
    }

    public boolean include(StatisticType type) {
        return types.contains(type);
    }

    public boolean need() {
        return !types.isEmpty();
    }

    public boolean needNonStored() {
        return include(StatisticType.MinimumQ0)
                || include(StatisticType.MaximumQ4)
                || include(StatisticType.Mean)
                || include(StatisticType.Sum)
                || include(StatisticType.Count)
                || include(StatisticType.Skewness)
                || include(StatisticType.GeometricMean)
                || include(StatisticType.SumOfSquares)
                || needVariance();
    }

    public boolean needVariance() {
        return include(StatisticType.PopulationVariance)
                || include(StatisticType.SampleVariance)
                || include(StatisticType.PopulationStandardDeviation)
                || include(StatisticType.SampleStandardDeviation);
    }

    public boolean needStored() {
        return needPercentile() || include(StatisticType.Mode);
    }

    public boolean needPercentile() {
        return include(StatisticType.Median)
                || include(StatisticType.UpperQuartile)
                || include(StatisticType.LowerQuartile) || needOutlier();
    }

    public boolean needOutlier() {
        return include(StatisticType.LowerExtremeOutlierLine)
                || include(StatisticType.LowerMildOutlierLine)
                || include(StatisticType.UpperMildOutlierLine)
                || include(StatisticType.UpperExtremeOutlierLine);
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
            if (names == null || names.isEmpty() || types.size() < 1) {
                handleController.popError(message("SelectToHandle") + " " + prefix);
                return false;
            }
            outputNames = new ArrayList<>();
            outputColumns = new ArrayList<>();

            String cName = DerbyBase.checkIdentifier(names, prefix + message("Calculation"), false);
            outputNames.add(cName);
            outputNames.addAll(names);
            outputColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.String, 300));

            for (String name : names) {
                outputColumns.add(new Data2DColumn(name, ColumnDefinition.ColumnType.Double));
            }

            outputData = new ArrayList<>();
            statisticRows = new HashMap<>();
            for (StatisticType type : types) {
                List<String> row = new ArrayList<>();
                row.add(prefix + message(type.name()));
                statisticRows.put(type, row);
                outputData.add(row);
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
            if (types.size() < 1) {
                handleController.popError(message("SelectToHandle"));
                return false;
            }
            outputNames = new ArrayList<>();
            outputColumns = new ArrayList<>();

            String cName = categoryName != null ? categoryName : message("SourceRowNumber");
            outputNames.add(cName);
            outputColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.String));

            String prefix = message("Rows") + "-";
            int width = 150;

            for (StatisticType type : types) {
                cName = prefix + message(type.name());
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
        if (statistic == null || statisticRows == null) {
            return false;
        }
        for (StatisticType type : types) {
            List<String> row = statisticRows.get(type);
            if (row == null) {
                continue;
            }
            switch (type) {
                case Count:
                    row.add(StringTools.format(statistic.getCount()));
                    break;
                case Sum:
                    row.add(NumberTools.format(statistic.getSum(), scale));
                    break;
                case Mean:
                    row.add(NumberTools.format(statistic.getMean(), scale));
                    break;
                case GeometricMean:
                    row.add(NumberTools.format(statistic.getGeometricMean(), scale));
                    break;
                case SumOfSquares:
                    row.add(NumberTools.format(statistic.getSumSquares(), scale));
                    break;
                case PopulationVariance:
                    row.add(NumberTools.format(statistic.getPopulationVariance(), scale));
                    break;
                case SampleVariance:
                    row.add(NumberTools.format(statistic.getSampleVariance(), scale));
                    break;
                case PopulationStandardDeviation:
                    row.add(NumberTools.format(statistic.getPopulationStandardDeviation(), scale));
                    break;
                case SampleStandardDeviation:
                    row.add(NumberTools.format(statistic.getSampleStandardDeviation(), scale));
                    break;
                case Skewness:
                    row.add(NumberTools.format(statistic.getSkewness(), scale));
                    break;
                case MinimumQ0:
                    row.add(NumberTools.format(statistic.getMinimum(), scale));
                    break;
                case MaximumQ4:
                    row.add(NumberTools.format(statistic.getMaximum(), scale));
                    break;
            }
        }

        return true;
    }

    public boolean statisticByColumnsWriteStored(DoubleStatistic statistic) {
        if (statistic == null || statisticRows == null) {
            return false;
        }
        for (StatisticType type : types) {
            List<String> row = statisticRows.get(type);
            if (row == null) {
                continue;
            }
            Object v;
            switch (type) {
                case Median:
                    v = statistic.getMedianValue();
                    break;
                case Mode:
                    v = statistic.getModeValue();
                    break;
                case LowerQuartile:
                    v = statistic.getLowerQuartileValue();
                    break;
                case UpperQuartile:
                    v = statistic.getUpperQuartileValue();
                    break;
                case UpperExtremeOutlierLine:
                    v = statistic.getUpperExtremeOutlierLine();
                    break;
                case UpperMildOutlierLine:
                    v = statistic.getUpperMildOutlierLine();
                    break;
                case LowerMildOutlierLine:
                    v = statistic.getLowerMildOutlierLine();
                    break;
                case LowerExtremeOutlierLine:
                    v = statistic.getLowerExtremeOutlierLine();
                    break;
                default:
                    continue;
            }
            try {
                row.add(NumberTools.format((double) v, scale));
            } catch (Exception e) {
                try {
                    row.add(v.toString());
                } catch (Exception ex) {
                    row.add("");
                }
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
            MyBoxLog.error(e);
            return false;
        }
    }


    /*
        get/set
     */
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
