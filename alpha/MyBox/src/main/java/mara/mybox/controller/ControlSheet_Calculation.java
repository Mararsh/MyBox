package mara.mybox.controller;

import java.util.List;
import mara.mybox.data.DoubleStatistic;
import mara.mybox.data.FloatStatistic;
import mara.mybox.data.IntStatistic;
import mara.mybox.data.LongStatistic;
import mara.mybox.data.ShortStatistic;
import mara.mybox.db.data.ColumnDefinition;
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
            int calSize = calCols.size();
            String[][] data = new String[rowSize + 9][calSize * 2 + disCols.size() + 1];
            data[0][0] = message("Count");
            data[1][0] = message("Summation");
            data[2][0] = message("Mean");
            data[3][0] = message("Variance");
            data[4][0] = message("Skewness");
            data[5][0] = message("Maximum");
            data[6][0] = message("Minimum");
            data[7][0] = message("Mode");
            data[8][0] = message("Median");
            for (int rowIndex = 0; rowIndex < rowSize; rowIndex++) {
                int row = rows.get(rowIndex);
                data[rowIndex + 9][0] = message("Percentage") + "_" + message("Row") + (row + 1) + "_%";
            }
            for (int index = 0; index < calSize; index++) {
                int col = calCols.get(index);
                int calCol = index * 2 + 1;
                ColumnDefinition column = columns.get(col);
                switch (column.getType()) {
                    case Double: {
                        double[] colData = new double[rowSize];
                        for (int rowIndex = 0; rowIndex < rowSize; rowIndex++) {
                            int row = rows.get(rowIndex);
                            String v = cellString(row, col);
                            data[rowIndex + 9][calCol + 1] = v;
                            colData[rowIndex] = Double.valueOf(v);
                        }
                        DoubleStatistic statistic = new DoubleStatistic(colData);
                        data[0][calCol] = StringTools.format(statistic.getCount());
                        data[1][calCol] = DoubleTools.format(statistic.getSum(), scale);
                        data[2][calCol] = DoubleTools.format(statistic.getMean(), scale);
                        data[3][calCol] = DoubleTools.format(statistic.getVariance(), scale);
                        data[4][calCol] = DoubleTools.format(statistic.getSkewness(), scale);
                        data[5][calCol] = DoubleTools.format(statistic.getMaximum(), scale);
                        data[6][calCol] = DoubleTools.format(statistic.getMinimum(), scale);
                        data[7][calCol] = DoubleTools.format(statistic.getMode(), scale);
                        data[8][calCol] = DoubleTools.format(statistic.getMedian(), scale);
                        for (int rowIndex = 0; rowIndex < rowSize; rowIndex++) {
                            data[rowIndex + 9][calCol] = DoubleTools.percentage(colData[rowIndex], statistic.getSum());
                        }
                        break;
                    }
                    case Integer: {
                        int[] colData = new int[rowSize];
                        for (int rowIndex = 0; rowIndex < rowSize; rowIndex++) {
                            int row = rows.get(rowIndex);
                            String v = cellString(row, col);
                            data[rowIndex + 9][calCol + 1] = v;
                            colData[rowIndex] = Integer.valueOf(v);
                        }
                        IntStatistic statistic = new IntStatistic(colData);
                        data[0][calCol] = StringTools.format(statistic.getCount());
                        data[1][calCol] = DoubleTools.format(statistic.getSum(), scale);
                        data[2][calCol] = DoubleTools.format(statistic.getMean(), scale);
                        data[3][calCol] = DoubleTools.format(statistic.getVariance(), scale);
                        data[4][calCol] = DoubleTools.format(statistic.getSkewness(), scale);
                        data[5][calCol] = StringTools.format(statistic.getMaximum());
                        data[6][calCol] = StringTools.format(statistic.getMinimum());
                        data[7][calCol] = StringTools.format(statistic.getMode());
                        data[8][calCol] = StringTools.format(statistic.getMedian());
                        for (int rowIndex = 0; rowIndex < rowSize; rowIndex++) {
                            data[rowIndex + 9][calCol] = DoubleTools.percentage(colData[rowIndex], statistic.getSum());
                        }
                        break;
                    }
                    case Long: {
                        long[] colData = new long[rowSize];
                        for (int rowIndex = 0; rowIndex < rowSize; rowIndex++) {
                            int row = rows.get(rowIndex);
                            String v = cellString(row, col);
                            data[rowIndex + 9][calCol + 1] = v;
                            colData[rowIndex] = Long.valueOf(v);
                        }
                        LongStatistic statistic = new LongStatistic(colData);
                        data[0][calCol] = StringTools.format(statistic.getCount());
                        data[1][calCol] = DoubleTools.format(statistic.getSum(), scale);
                        data[2][calCol] = DoubleTools.format(statistic.getMean(), scale);
                        data[3][calCol] = DoubleTools.format(statistic.getVariance(), scale);
                        data[4][calCol] = DoubleTools.format(statistic.getSkewness(), scale);
                        data[5][calCol] = StringTools.format(statistic.getMaximum());
                        data[6][calCol] = StringTools.format(statistic.getMinimum());
                        data[7][calCol] = StringTools.format(statistic.getMode());
                        data[8][calCol] = StringTools.format(statistic.getMedian());
                        for (int rowIndex = 0; rowIndex < rowSize; rowIndex++) {
                            data[rowIndex + 9][calCol] = DoubleTools.percentage(colData[rowIndex], statistic.getSum());
                        }
                        break;
                    }
                    case Short: {
                        short[] colData = new short[rowSize];
                        for (int rowIndex = 0; rowIndex < rowSize; rowIndex++) {
                            int row = rows.get(rowIndex);
                            String v = cellString(row, col);
                            data[rowIndex + 9][calCol + 1] = v;
                            colData[rowIndex] = Short.valueOf(v);
                        }
                        ShortStatistic statistic = new ShortStatistic(colData);
                        data[0][calCol] = StringTools.format(statistic.getCount());
                        data[1][calCol] = DoubleTools.format(statistic.getSum(), scale);
                        data[2][calCol] = DoubleTools.format(statistic.getMean(), scale);
                        data[3][calCol] = DoubleTools.format(statistic.getVariance(), scale);
                        data[4][calCol] = DoubleTools.format(statistic.getSkewness(), scale);
                        data[5][calCol] = StringTools.format(statistic.getMaximum());
                        data[6][calCol] = StringTools.format(statistic.getMinimum());
                        data[7][calCol] = StringTools.format(statistic.getMode());
                        data[8][calCol] = StringTools.format(statistic.getMedian());
                        for (int rowIndex = 0; rowIndex < rowSize; rowIndex++) {
                            data[rowIndex + 9][calCol] = DoubleTools.percentage(colData[rowIndex], statistic.getSum());
                        }
                        break;
                    }
                    case Float: {
                        float[] colData = new float[rowSize];
                        for (int rowIndex = 0; rowIndex < rowSize; rowIndex++) {
                            int row = rows.get(rowIndex);
                            String v = cellString(row, col);
                            data[rowIndex + 9][calCol + 1] = v;
                            colData[rowIndex] = Float.valueOf(v);
                        }
                        FloatStatistic statistic = new FloatStatistic(colData);
                        data[0][calCol] = StringTools.format(statistic.getCount());
                        data[1][calCol] = DoubleTools.format(statistic.getSum(), scale);
                        data[2][calCol] = DoubleTools.format(statistic.getMean(), scale);
                        data[3][calCol] = DoubleTools.format(statistic.getVariance(), scale);
                        data[4][calCol] = DoubleTools.format(statistic.getSkewness(), scale);
                        data[5][calCol] = FloatTools.format(statistic.getMaximum(), scale);
                        data[6][calCol] = FloatTools.format(statistic.getMinimum(), scale);
                        data[7][calCol] = FloatTools.format(statistic.getMode(), scale);
                        data[8][calCol] = FloatTools.format(statistic.getMedian(), scale);
                        for (int rowIndex = 0; rowIndex < rowSize; rowIndex++) {
                            data[rowIndex + 9][calCol] = DoubleTools.percentage(colData[rowIndex], statistic.getSum());
                        }
                        break;
                    }
                    default:
                        break;
                }
            }
            int baseCol = 1 + calSize * 2;
            for (int index = baseCol; index < disCols.size() + baseCol; index++) {
                int col = disCols.get(index - baseCol);
                for (int rowIndex = 0; rowIndex < rowSize; rowIndex++) {
                    int row = rows.get(rowIndex);
                    data[rowIndex + 9][index] = cellString(row, col);
                }
            }
            return data;
        } catch (Exception e) {
            popError(e.toString());
            MyBoxLog.console(e);
            return null;
        }
    }

    public String[][] percentage(List<Integer> rows, List<Integer> calCols, List<Integer> disCols) {
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
            for (int rowIndex = 0; rowIndex < rowSize; rowIndex++) {
                int row = rows.get(rowIndex);
                data[rowIndex + 9][0] = message("DataRow") + " " + (row + 1);
            }
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

}
