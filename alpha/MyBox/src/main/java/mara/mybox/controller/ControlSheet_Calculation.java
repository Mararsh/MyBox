package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import mara.mybox.data.DoubleStatistic;
import mara.mybox.data.FloatStatistic;
import mara.mybox.data.IntStatistic;
import mara.mybox.data.LongStatistic;
import mara.mybox.data.ShortStatistic;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.FloatTools;
import mara.mybox.tools.StringTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-8-24
 * @License Apache License Version 2.0
 */
public abstract class ControlSheet_Calculation extends ControlSheet_TextsDisplay {

    public String[][] transpose(List<Integer> rows, List<Integer> cols) {
        if (rows == null || rows.isEmpty() || cols == null || cols.isEmpty() || sheetInputs == null || columns == null) {
            popError(message("NoData"));
            return null;
        }
        String[][] data = new String[cols.size()][rows.size()];
        for (int r = 0; r < rows.size(); ++r) {
            int row = rows.get(r);
            for (int c = 0; c < cols.size(); ++c) {
                data[c][r] = cellString(row, cols.get(c));
            }
        }
        return data;
    }

    public String[][] statistic(List<Integer> rows, List<Integer> calCols, List<Integer> disCols) {
        try {
            if (rows == null || rows.isEmpty() || calCols == null || calCols.isEmpty() || sheetInputs == null || columns == null) {
                popError(message("NoData"));
                return null;
            }
            int rowSize = rows.size();
            String[][] data = new String[rowSize + 9][calCols.size() + disCols.size() + 1];
            data[0][0] = message("Count");
            data[1][0] = message("Summation");
            data[2][0] = message("Mean");
            data[3][0] = message("Variance");
            data[4][0] = message("Skewness");
            data[5][0] = message("Maximum");
            data[6][0] = message("Minimum");
            data[7][0] = message("Mode");
            data[8][0] = message("Median");
            int baseCol = 1;
            for (int colIndex = baseCol; colIndex < calCols.size() + baseCol; colIndex++) {
                int col = calCols.get(colIndex - baseCol);
                ColumnDefinition column = columns.get(col);
                switch (column.getType()) {
                    case Double: {
                        double[] colData = new double[rowSize];
                        for (int rowIndex = 0; rowIndex < rowSize; rowIndex++) {
                            int row = rows.get(rowIndex);
                            String v = cellString(row, col);
                            data[rowIndex + 9][colIndex] = v;
                            colData[rowIndex] = Double.valueOf(v);
                        }
                        DoubleStatistic statistic = new DoubleStatistic(colData);
                        data[0][colIndex] = StringTools.format(statistic.getCount());
                        data[1][colIndex] = DoubleTools.format(statistic.getSum(), scale);
                        data[2][colIndex] = DoubleTools.format(statistic.getMean(), scale);
                        data[3][colIndex] = DoubleTools.format(statistic.getVariance(), scale);
                        data[4][colIndex] = DoubleTools.format(statistic.getSkewness(), scale);
                        data[5][colIndex] = DoubleTools.format(statistic.getMaximum(), scale);
                        data[6][colIndex] = DoubleTools.format(statistic.getMinimum(), scale);
                        data[7][colIndex] = DoubleTools.format(statistic.getMode(), scale);
                        data[8][colIndex] = DoubleTools.format(statistic.getMedian(), scale);
                        break;
                    }
                    case Integer: {
                        int[] colData = new int[rowSize];
                        for (int rowIndex = 0; rowIndex < rowSize; rowIndex++) {
                            int row = rows.get(rowIndex);
                            String v = cellString(row, col);
                            data[rowIndex + 9][colIndex] = v;
                            colData[rowIndex] = Integer.valueOf(v);
                        }
                        IntStatistic statistic = new IntStatistic(colData);
                        data[0][colIndex] = StringTools.format(statistic.getCount());
                        data[1][colIndex] = DoubleTools.format(statistic.getSum(), scale);
                        data[2][colIndex] = DoubleTools.format(statistic.getMean(), scale);
                        data[3][colIndex] = DoubleTools.format(statistic.getVariance(), scale);
                        data[4][colIndex] = DoubleTools.format(statistic.getSkewness(), scale);
                        data[5][colIndex] = StringTools.format(statistic.getMaximum());
                        data[6][colIndex] = StringTools.format(statistic.getMinimum());
                        data[7][colIndex] = StringTools.format(statistic.getMode());
                        data[8][colIndex] = StringTools.format(statistic.getMedian());
                        break;
                    }
                    case Long: {
                        long[] colData = new long[rowSize];
                        for (int rowIndex = 0; rowIndex < rowSize; rowIndex++) {
                            int row = rows.get(rowIndex);
                            String v = cellString(row, col);
                            data[rowIndex + 9][colIndex] = v;
                            colData[rowIndex] = Long.valueOf(v);
                        }
                        LongStatistic statistic = new LongStatistic(colData);
                        data[0][colIndex] = StringTools.format(statistic.getCount());
                        data[1][colIndex] = DoubleTools.format(statistic.getSum(), scale);
                        data[2][colIndex] = DoubleTools.format(statistic.getMean(), scale);
                        data[3][colIndex] = DoubleTools.format(statistic.getVariance(), scale);
                        data[4][colIndex] = DoubleTools.format(statistic.getSkewness(), scale);
                        data[5][colIndex] = StringTools.format(statistic.getMaximum());
                        data[6][colIndex] = StringTools.format(statistic.getMinimum());
                        data[7][colIndex] = StringTools.format(statistic.getMode());
                        data[8][colIndex] = StringTools.format(statistic.getMedian());
                        break;
                    }
                    case Short: {
                        short[] colData = new short[rowSize];
                        for (int rowIndex = 0; rowIndex < rowSize; rowIndex++) {
                            int row = rows.get(rowIndex);
                            String v = cellString(row, col);
                            data[rowIndex + 9][colIndex] = v;
                            colData[rowIndex] = Short.valueOf(v);
                        }
                        ShortStatistic statistic = new ShortStatistic(colData);
                        data[0][colIndex] = StringTools.format(statistic.getCount());
                        data[1][colIndex] = DoubleTools.format(statistic.getSum(), scale);
                        data[2][colIndex] = DoubleTools.format(statistic.getMean(), scale);
                        data[3][colIndex] = DoubleTools.format(statistic.getVariance(), scale);
                        data[4][colIndex] = DoubleTools.format(statistic.getSkewness(), scale);
                        data[5][colIndex] = StringTools.format(statistic.getMaximum());
                        data[6][colIndex] = StringTools.format(statistic.getMinimum());
                        data[7][colIndex] = StringTools.format(statistic.getMode());
                        data[8][colIndex] = StringTools.format(statistic.getMedian());
                        break;
                    }
                    case Float: {
                        float[] colData = new float[rowSize];
                        for (int rowIndex = 0; rowIndex < rowSize; rowIndex++) {
                            int row = rows.get(rowIndex);
                            String v = cellString(row, col);
                            data[rowIndex + 9][colIndex] = v;
                            colData[rowIndex] = Float.valueOf(v);
                        }
                        FloatStatistic statistic = new FloatStatistic(colData);
                        data[0][colIndex] = StringTools.format(statistic.getCount());
                        data[1][colIndex] = DoubleTools.format(statistic.getSum(), scale);
                        data[2][colIndex] = DoubleTools.format(statistic.getMean(), scale);
                        data[3][colIndex] = DoubleTools.format(statistic.getVariance(), scale);
                        data[4][colIndex] = DoubleTools.format(statistic.getSkewness(), scale);
                        data[5][colIndex] = FloatTools.format(statistic.getMaximum(), scale);
                        data[6][colIndex] = FloatTools.format(statistic.getMinimum(), scale);
                        data[7][colIndex] = FloatTools.format(statistic.getMode(), scale);
                        data[8][colIndex] = FloatTools.format(statistic.getMedian(), scale);
                        break;
                    }
                    default:
                        break;
                }
            }
            baseCol = 1 + calCols.size();
            for (int colIndex = baseCol; colIndex < disCols.size() + baseCol; colIndex++) {
                int col = disCols.get(colIndex - baseCol);
                for (int rowIndex = 0; rowIndex < rowSize; rowIndex++) {
                    int row = rows.get(rowIndex);
                    data[rowIndex + 9][colIndex] = cellString(row, col);
                }
            }
            return data;
        } catch (Exception e) {
            popError(e.toString());
            MyBoxLog.console(e);
            return null;
        }
    }

    // 1-based, include
    public void sum(List<Integer> calculationColumns, List<Integer> displayColumns, int intFrom, int inTo) {
        try {
            if (calculationColumns == null || calculationColumns.isEmpty()
                    || pageData == null || intFrom > inTo) {
                popError(message("InvalidParameters"));
                return;
            }
            int from = Math.min(pageData.length, Math.max(1, intFrom));
            int to = Math.min(pageData.length, Math.max(1, inTo));
            if (from > to) {
                popError(message("InvalidParameters"));
                return;
            }
            MyBoxLog.console(from + " " + to);
            int calSize = calculationColumns.size();
            int displaySize = displayColumns.size();
            List<ColumnDefinition> dataColumns = new ArrayList<>();
            dataColumns.add(new ColumnDefinition(message("Calculation"), ColumnType.String));
            for (int c : calculationColumns) {
                ColumnDefinition def = columns.get(c);
                if (!def.isNumberType()) {
                    popError(message("InvalidParameters"));
                    return;
                }
                dataColumns.add(new ColumnDefinition(def.getName(), ColumnType.Double));
            }
            for (int c : displayColumns) {
                dataColumns.add(columns.get(c));
            }

            String[][] data = new String[to - from + 2][calSize + displaySize + 1];
            data[0][0] = message("Total");
            for (int c = 0; c < calSize; ++c) {
                double sum = 0;
                int colIndex = calculationColumns.get(c);
                for (int r = from - 1; r <= to - 1; ++r) {
                    try {
                        sum += Double.valueOf(pageData[r][colIndex]);
                    } catch (Exception e) {
                    }
                }
                data[0][c + 1] = DoubleTools.format(sum, 2);
            }
            for (int c = 0; c < displaySize; ++c) {
                data[0][c + calSize + 1] = "";
            }
            for (int r = 1; r <= to - from + 1; ++r) {
                data[r][0] = null;
            }
            for (int c = 0; c < calSize; ++c) {
                int colIndex = calculationColumns.get(c);
                for (int r = 1; r <= to - from + 1; ++r) {
                    data[r][c + 1] = pageData[r + from - 2][colIndex];
                }
            }
            for (int c = 0; c < displaySize; ++c) {
                int colIndex = displayColumns.get(c);
                for (int r = 1; r <= to - from + 1; ++r) {
                    data[r][c + calSize + 1] = pageData[r + from - 2][colIndex];
                }
            }
//            DataClipboardController.open(data, dataColumns);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
