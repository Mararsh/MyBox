package mara.mybox.data2d.reader;

import java.util.ArrayList;
import java.util.List;
import mara.mybox.calculation.DoubleStatistic;
import mara.mybox.calculation.Normalization;
import mara.mybox.data2d.Data2D_Edit;
import mara.mybox.tools.DoubleTools;
import mara.mybox.value.AppValues;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public class Data2DNormalize extends Data2DOperator {

    protected Type type;
    protected DoubleStatistic[] statisticData;
    protected DoubleStatistic statisticAll;
    protected double[] colValues;
    protected double from, to, tValue;
    protected Normalization.Algorithm a;

    public static enum Type {
        MinMaxColumns, SumColumns, ZscoreColumns,
        Rows, MinMaxAll, SumAll, ZscoreAll
    }

    public static Data2DNormalize create(Data2D_Edit data) {
        Data2DNormalize op = new Data2DNormalize();
        return op.setData(data) ? op : null;
    }

    @Override
    public boolean checkParameters() {
        if (cols == null || cols.isEmpty() || type == null || csvPrinter == null || invalidAs == null) {
            return false;
        }
        switch (type) {
            case MinMaxColumns:
            case ZscoreColumns:
                if (statisticData == null) {
                    return false;
                }
                if (cols == null || cols.isEmpty() || type == null || csvPrinter == null || invalidAs == null) {
                    return false;
                }
                break;
            case SumColumns:
                if (colValues == null) {
                    return false;
                }
                break;
            case Rows:
                if (a == null) {
                    return false;
                }
                break;
            case MinMaxAll:
            case ZscoreAll:
                if (statisticAll == null) {
                    return false;
                }
                break;
        }
        return true;
    }

    @Override
    public void handleRow() {
        switch (type) {
            case MinMaxColumns:
                handleNormalizeMinMaxColumns();
                break;
            case SumColumns:
                handleNormalizeSumColumns();
                break;
            case ZscoreColumns:
                handleNormalizeZscoreColumns();
                break;
            case Rows:
                handleNormalizeRows(a);
                break;
            case MinMaxAll:
                handleNormalizeMinMaxAll();
                break;
            case SumAll:
                handleNormalizeSumAll();
                break;
            case ZscoreAll:
                handleNormalizeZscoreAll();
                break;

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
                    String s = sourceRow.get(i);
                    double v = DoubleTools.toDouble(s, invalidAs);
                    if (DoubleTools.invalidDouble(v)) {
                        switch (invalidAs) {
                            case Blank:
                                row.add("");
                                break;
                            case Zero:
                                row.add("0");
                                break;
                            default:
                                row.add(s);
                                break;
                        }
                    } else {
                        v = from + statisticData[c].dTmp * (v - statisticData[c].minimum);
                        row.add(DoubleTools.scale(v, scale) + "");
                    }
                }
            }
            if (otherCols != null) {
                for (int c : otherCols) {
                    if (c < 0 || c >= sourceRow.size()) {
                        row.add(null);
                    } else {
                        row.add(sourceRow.get(c));
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
                    String s = sourceRow.get(i);
                    double v = DoubleTools.toDouble(s, invalidAs);
                    if (DoubleTools.invalidDouble(v)) {
                        switch (invalidAs) {
                            case Blank:
                                row.add("");
                                break;
                            case Zero:
                                row.add("0");
                                break;
                            default:
                                row.add(s);
                                break;
                        }
                    } else {
                        v = v * colValues[c];
                        row.add(DoubleTools.scale(v, scale) + "");
                    }
                }
            }
            if (otherCols != null) {
                for (int c : otherCols) {
                    if (c < 0 || c >= sourceRow.size()) {
                        row.add(null);
                    } else {
                        row.add(sourceRow.get(c));
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
                    String s = sourceRow.get(i);
                    double v = DoubleTools.toDouble(s, invalidAs);
                    if (DoubleTools.invalidDouble(v)) {
                        switch (invalidAs) {
                            case Blank:
                                row.add("");
                                break;
                            case Zero:
                                row.add("0");
                                break;
                            default:
                                row.add(s);
                                break;
                        }
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
            if (otherCols != null) {
                for (int c : otherCols) {
                    if (c < 0 || c >= sourceRow.size()) {
                        row.add(null);
                    } else {
                        row.add(sourceRow.get(c));
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
                    String s = sourceRow.get(i);
                    double v = DoubleTools.toDouble(s, invalidAs);
                    if (DoubleTools.invalidDouble(v)) {
                        switch (invalidAs) {
                            case Blank:
                                row.add("");
                                break;
                            case Zero:
                                row.add("0");
                                break;
                            default:
                                row.add(s);
                                break;
                        }
                    } else {
                        v = from + statisticAll.dTmp * (v - statisticAll.minimum);
                        row.add(DoubleTools.scale(v, scale) + "");
                    }
                }
            }
            if (otherCols != null) {
                for (int c : otherCols) {
                    if (c < 0 || c >= sourceRow.size()) {
                        row.add(null);
                    } else {
                        row.add(sourceRow.get(c));
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
                    String s = sourceRow.get(i);
                    double v = DoubleTools.toDouble(s, invalidAs);
                    if (DoubleTools.invalidDouble(v)) {
                        switch (invalidAs) {
                            case Blank:
                                row.add("");
                                break;
                            case Zero:
                                row.add("0");
                                break;
                            default:
                                row.add(s);
                                break;
                        }
                    } else {
                        v = v * tValue;
                        row.add(DoubleTools.scale(v, scale) + "");
                    }
                }
            }
            if (otherCols != null) {
                for (int c : otherCols) {
                    if (c < 0 || c >= sourceRow.size()) {
                        row.add(null);
                    } else {
                        row.add(sourceRow.get(c));
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
                    String s = sourceRow.get(i);
                    double v = DoubleTools.toDouble(s, invalidAs);
                    if (DoubleTools.invalidDouble(v)) {
                        switch (invalidAs) {
                            case Blank:
                                row.add("");
                                break;
                            case Zero:
                                row.add("0");
                                break;
                            default:
                                row.add(s);
                                break;
                        }
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
            if (otherCols != null) {
                for (int c : otherCols) {
                    if (c < 0 || c >= sourceRow.size()) {
                        row.add(null);
                    } else {
                        row.add(sourceRow.get(c));
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
            String[] values = new String[colsLen];
            for (int c = 0; c < colsLen; c++) {
                int i = cols.get(c);
                if (i < 0 || i >= sourceRow.size()) {
                    values[c] = DoubleTools.value(invalidAs) + "";
                } else {
                    values[c] = sourceRow.get(i);
                }
            }
            values = Normalization.create()
                    .setA(a).setFrom(from).setTo(to).setInvalidAs(invalidAs)
                    .setSourceVector(values)
                    .calculate();
            if (values == null) {
                return;
            }
            for (String s : values) {
                double d = DoubleTools.toDouble(s, invalidAs);
                if (DoubleTools.invalidDouble(d)) {
                    switch (invalidAs) {
                        case Zero:
                            row.add("0");
                            break;
                        case Skip:
                            row.add(s);
                            break;
                        case Blank:
                            row.add(null);
                            break;
                    }
                } else {
                    row.add(DoubleTools.scale(d, scale) + "");
                }
            }
            if (otherCols != null) {
                for (int c : otherCols) {
                    if (c < 0 || c >= sourceRow.size()) {
                        row.add(null);
                    } else {
                        row.add(sourceRow.get(c));
                    }
                }
            }
            csvPrinter.printRecord(row);
        } catch (Exception e) {
        }
    }


    /*
        set
     */
    public Data2DNormalize setType(Type type) {
        this.type = type;
        return this;
    }

    public Data2DNormalize setStatisticData(DoubleStatistic[] statisticData) {
        this.statisticData = statisticData;
        return this;
    }

    public Data2DNormalize setStatisticAll(DoubleStatistic statisticAll) {
        this.statisticAll = statisticAll;
        return this;
    }

    public Data2DNormalize setColValues(double[] colValues) {
        this.colValues = colValues;
        return this;
    }

    public Data2DNormalize setFrom(double from) {
        this.from = from;
        return this;
    }

    public Data2DNormalize setTo(double to) {
        this.to = to;
        return this;
    }

    public Data2DNormalize settValue(double tValue) {
        this.tValue = tValue;
        return this;
    }

    public Data2DNormalize setA(Normalization.Algorithm a) {
        this.a = a;
        return this;
    }

}
