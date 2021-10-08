package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import mara.mybox.data.DoubleStatistic;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import org.apache.commons.csv.CSVPrinter;

/**
 * @Author Mara
 * @CreateDate 2021-8-24
 * @License Apache License Version 2.0
 */
public abstract class ControlSheet_Calculation extends ControlSheet_TextsDisplay {

    public abstract void statistic(List<Integer> calCols, List<Integer> disCols, boolean mode, boolean median, boolean percentage);

    public void transpose(List<Integer> rows, List<Integer> cols) {
        transpose(data(rows, cols));
    }

    public void transpose(List<Integer> cols) {
        if (cols == null || cols.isEmpty()) {
            popError(message("NoData"));
            return;
        }
        if (sourceFile == null || pagesNumber <= 1) {
            transpose(data(cols));
            return;
        }
        synchronized (this) {
            SingletonTask calTask = new SingletonTask<Void>() {

                private String[][] data;

                @Override
                protected boolean handle() {
                    // transpose involves all data and can not handle row by row
                    data = allRows(cols);
                    return data != null;
                }

                @Override
                protected void whenSucceeded() {
                    transpose(data);
                }

            };
            start(calTask, false);
        }
    }

    public void transpose(String[][] data) {
        if (data == null) {
            popError(message("NoData"));
            return;
        }
        synchronized (this) {
            SingletonTask calTask = new SingletonTask<Void>() {
                private String[][] transposed;

                @Override
                protected boolean handle() {
                    try {
                        int rowSize = data.length;
                        int colSize = data[0].length;
                        transposed = new String[colSize][rowSize];
                        for (int r = 0; r < rowSize; ++r) {
                            for (int c = 0; c < colSize; ++c) {
                                transposed[c][r] = data[r][c];
                            }
                        }
                        return transposed != null;
                    } catch (Exception e) {
                        error = e.toString();
                        MyBoxLog.error(e.toString());
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    DataFileCSVController controller = (DataFileCSVController) WindowTools.openStage(Fxmls.DataFileCSVFxml);
                    controller.loadData(transposed, null);
                }

            };
            start(calTask, false);
        }
    }

    public double doubleValue(String v) {
        try {
            if (v == null || v.isBlank()) {
                v = defaultColValue;
            }
            return Double.valueOf(v.replaceAll(",", ""));
        } catch (Exception e) {
            return AppValues.InvalidDouble;
        }
    }

    // All as double to make things simple. 
    // To improve performance, this should be counting according to columns' types.
    public String[][] statistic(String[][] sourceData) {
        if (sourceData == null) {
            popError(message("NoData"));
            return null;
        }
        try {
            int rowSize = sourceData.length;
            int colSize = sourceData[0].length;
            String[][] sData = new String[9][colSize + 1];
            sData[0][0] = message("Count");
            sData[1][0] = message("Summation");
            sData[2][0] = message("Mean");
            sData[3][0] = message("Variance");
            sData[4][0] = message("Skewness");
            sData[5][0] = message("Maximum");
            sData[6][0] = message("Minimum");
            sData[7][0] = message("Mode");
            sData[8][0] = message("Median");
            for (int c = 0; c < colSize; c++) {
                int col = c + 1;
                double[] colData = new double[rowSize];
                for (int r = 0; r < rowSize; r++) {
                    colData[r] = doubleValue(sourceData[r][c]);
                }
                DoubleStatistic statistic = new DoubleStatistic(colData);
                sData[0][col] = StringTools.format(statistic.getCount());
                sData[1][col] = DoubleTools.format(statistic.getSum(), scale);
                sData[2][col] = DoubleTools.format(statistic.getMean(), scale);
                sData[3][col] = DoubleTools.format(statistic.getVariance(), scale);
                sData[4][col] = DoubleTools.format(statistic.getSkewness(), scale);
                sData[5][col] = DoubleTools.format(statistic.getMaximum(), scale);
                sData[6][col] = DoubleTools.format(statistic.getMinimum(), scale);
                sData[7][col] = DoubleTools.format(statistic.getMode(), scale);
                sData[8][col] = DoubleTools.format(statistic.getMedian(), scale);
            }
            return sData;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    protected String[][] statistic(String[][] sourceData, String[][] disData,
            boolean mode, boolean median, boolean percentage) {
        try {
            String[][] statisticData = statistic(sourceData);
            if (statisticData == null) {
                return null;
            }
            int rowSize = sourceData.length;
            int colSize = sourceData[0].length;
            int disSize = disData == null ? 0 : disData[0].length;
            int totalRowSize = statisticData.length;
            if (percentage || disSize > 0) {
                totalRowSize += rowSize;
            }
            String[][] resultData = new String[totalRowSize][colSize + disSize + 1];
            for (int r = 0; r < statisticData.length; r++) {
                for (int c = 0; c < statisticData[r].length; c++) {
                    resultData[r][c] = statisticData[r][c];
                }
            }
            if (percentage) {
                for (int r = 0; r < rowSize; r++) {
                    resultData[r + 9][0] = message("Percentage") + "_" + message("DataRow") + (r + 1) + "_%";
                    for (int c = 1; c <= colSize; c++) {
                        resultData[r + 9][c] = DoubleTools.percentage(doubleValue(sourceData[r][c - 1]), doubleValue(statisticData[1][c]));
                    }
                }
            } else if (disSize > 0) {
                for (int r = 0; r < rowSize; r++) {
                    resultData[r + 9][0] = message("DataRow") + (r + 1);
                }
            }
            if (disSize > 0) {
                for (int r = 0; r < rowSize; r++) {
                    for (int c = 0; c < disSize; c++) {
                        resultData[r + 9][c + colSize + 1] = disData[r][c];
                    }
                }
            }
            return resultData;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public void statistic(List<Integer> rows, List<Integer> calCols, List<Integer> disCols,
            boolean mode, boolean median, boolean percentage) {
        if (rows == null || rows.isEmpty() || calCols == null || calCols.isEmpty() || sheetInputs == null || columns == null) {
            popError(message("NoData"));
            return;
        }
        synchronized (this) {
            SingletonTask calTask = new SingletonTask<Void>() {
                private String[][] resultData;

                @Override
                protected boolean handle() {
                    try {
                        resultData = statistic(data(rows, calCols), data(rows, disCols), mode, median, percentage);
                        return resultData != null;
                    } catch (Exception e) {
                        error = e.toString();
                        MyBoxLog.error(e.toString());
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    DataFileCSVController controller = (DataFileCSVController) WindowTools.openStage(Fxmls.DataFileCSVFxml);
                    controller.loadData(resultData, statisticColumns(calCols, disCols));
                }

            };
            start(calTask, false);
        }
    }

    protected List<ColumnDefinition> statisticColumns(List<Integer> calCols, List<Integer> disCols) {
        if (calCols == null || calCols.isEmpty() || columns == null) {
            popError(message("NoData"));
            return null;
        }
        // The column name can not start with "m_", or else errors popped by javafx class "CheckBoxSkin". I guess this is a bug of Javafx.
        // https://github.com/Mararsh/MyBox/issues/1222
        List<ColumnDefinition> dataColumns = new ArrayList<>();
        dataColumns.add(new ColumnDefinition("__" + message("CalculationName") + "__", ColumnDefinition.ColumnType.String).setWidth(200));

        for (Integer i : calCols) {
            dataColumns.add(new ColumnDefinition("__" + message("Calculation") + "__" + columns.get(i).getName() + "__",
                    ColumnDefinition.ColumnType.Double).setWidth(200));
        }
        if (disCols != null) {
            for (Integer i : disCols) {
                dataColumns.add(columns.get(i).cloneBase());
            }
        }
        return dataColumns;
    }

    protected void countPageData(DoubleStatistic[] sData, List<Integer> calCols) {
        try {
            if (sheetInputs == null || calCols == null || calCols.isEmpty()
                    || sData == null || sData.length < calCols.size()) {
                return;
            }
            for (int r = 0; r < sheetInputs.length; r++) {
                for (int c = 0; c < calCols.size(); c++) {
                    sData[c].count++;
                    int col = calCols.get(c);
                    if (col >= sheetInputs[r].length) {
                        break;
                    }
                    double v = doubleValue(cellString(r, col));
                    sData[c].sum += v;
                    if (v > sData[c].maximum) {
                        sData[c].maximum = v;
                    }
                    if (v < sData[c].minimum) {
                        sData[c].minimum = v;
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void variancePageData(DoubleStatistic[] sData, List<Integer> calCols) {
        try {
            if (sData == null || sheetInputs == null || calCols == null || calCols.isEmpty()) {
                return;
            }
            for (int r = 0; r < sheetInputs.length; r++) {
                for (int c = 0; c < calCols.size(); c++) {
                    int col = calCols.get(c);
                    if (col >= sheetInputs[r].length) {
                        break;
                    }
                    double v = doubleValue(cellString(r, col));
                    sData[c].variance += Math.pow(v - sData[c].mean, 2);
                    sData[c].skewness += Math.pow(v - sData[c].mean, 3);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected boolean writeStatisticData(CSVPrinter csvPrinter, DoubleStatistic[] sData,
            List<Integer> calCols, List<Integer> disCols, boolean percentage) {
        if (csvPrinter == null || sData == null || calCols == null || calCols.isEmpty()) {
            return false;
        }
        try {
            List<ColumnDefinition> sColumns = statisticColumns(calCols, disCols);
            List<String> names = new ArrayList<>();
            for (ColumnDefinition c : sColumns) {
                names.add(c.getName());
            }
            csvPrinter.printRecord(names);

            List<String> values = new ArrayList<>();
            int calLen = sData.length, disLen = disCols == null ? 0 : disCols.size();

            values.add(message("Count"));
            for (int c = 0; c < calLen; c++) {
                values.add(StringTools.format(sData[c].count));
            }
            for (int c = 0; c < disLen; c++) {
                values.add("");
            }
            csvPrinter.printRecord(values);
            values.clear();

            values.add(message("Summation"));
            for (int c = 0; c < calLen; c++) {
                values.add(StringTools.format(sData[c].sum));
            }
            for (int c = 0; c < disLen; c++) {
                values.add("");
            }
            csvPrinter.printRecord(values);
            values.clear();

            values.add(message("Mean"));
            for (int c = 0; c < calLen; c++) {
                values.add(StringTools.format(sData[c].mean));
            }
            for (int c = 0; c < disLen; c++) {
                values.add("");
            }
            csvPrinter.printRecord(values);
            values.clear();

            values.add(message("Variance"));
            for (int c = 0; c < calLen; c++) {
                values.add(StringTools.format(sData[c].variance));
            }
            for (int c = 0; c < disLen; c++) {
                values.add("");
            }
            csvPrinter.printRecord(values);
            values.clear();

            values.add(message("Skewness"));
            for (int c = 0; c < calLen; c++) {
                values.add(StringTools.format(sData[c].skewness));
            }
            for (int c = 0; c < disLen; c++) {
                values.add("");
            }
            csvPrinter.printRecord(values);
            values.clear();

            values.add(message("Maximum"));
            for (int c = 0; c < calLen; c++) {
                values.add(StringTools.format(sData[c].maximum));
            }
            for (int c = 0; c < disLen; c++) {
                values.add("");
            }
            csvPrinter.printRecord(values);
            values.clear();

            values.add(message("Minimum"));
            for (int c = 0; c < calLen; c++) {
                values.add(StringTools.format(sData[c].minimum));
            }
            for (int c = 0; c < disLen; c++) {
                values.add("");
            }
            csvPrinter.printRecord(values);
            values.clear();

        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
        return true;
    }

    protected int writePageStatistic(CSVPrinter csvPrinter, DoubleStatistic[] sData,
            List<Integer> calCols, List<Integer> disCols, boolean percentage, int startIndex) {
        int dataIndex = startIndex;
        try {
            if (sheetInputs == null || calCols == null || calCols.isEmpty()
                    || csvPrinter == null || sData == null) {
                return startIndex;
            }
            int calLen = calCols.size(), disLen = disCols == null ? 0 : disCols.size();
            for (int r = 0; r < sheetInputs.length; r++) {
                dataIndex++;
                List<String> row = new ArrayList<>();
                if (percentage) {
                    row.add(message("Percentage") + "_" + message("DataRow") + dataIndex + "_%");
                } else {
                    row.add(message("DataRow") + dataIndex);
                }
                int rLen = sheetInputs[r].length;
                for (int c = 0; c < calLen; c++) {
                    if (percentage) {
                        int col = calCols.get(c);
                        if (col >= rLen) {
                            row.add("");
                        } else {
                            row.add(DoubleTools.percentage(doubleValue(cellString(r, col)), sData[c].sum));
                        }
                    } else {
                        row.add("");
                    }
                }
                for (int c = 0; c < disLen; c++) {
                    int col = disCols.get(c);
                    if (col >= rLen) {
                        row.add("");
                    } else {
                        String v = cellString(r, col);
                        row.add(v == null ? defaultColValue : v);
                    }
                }
                csvPrinter.printRecord(row);
                row.clear();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return dataIndex;
    }

}
