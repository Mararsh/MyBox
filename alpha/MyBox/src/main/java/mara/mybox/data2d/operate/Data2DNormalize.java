package mara.mybox.data2d.operate;

import java.util.ArrayList;
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
public class Data2DNormalize extends Data2DOperate {

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
        return op.setSourceData(data) ? op : null;
    }

    @Override
    public boolean checkParameters() {
        if (!super.checkParameters()
                || cols == null || cols.isEmpty() || type == null || invalidAs == null) {
            return false;
        }
        switch (type) {
            case MinMaxColumns:
            case ZscoreColumns:
                if (statisticData == null) {
                    return false;
                }
                if (cols == null || cols.isEmpty() || type == null || invalidAs == null) {
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
    public boolean handleRow() {
        switch (type) {
            case MinMaxColumns:
                return handleNormalizeMinMaxColumns();
            case SumColumns:
                return handleNormalizeSumColumns();
            case ZscoreColumns:
                return handleNormalizeZscoreColumns();
            case Rows:
                return handleNormalizeRows(a);
            case MinMaxAll:
                return handleNormalizeMinMaxAll();
            case SumAll:
                return handleNormalizeSumAll();
            case ZscoreAll:
                return handleNormalizeZscoreAll();
        }
        return false;
    }

    public boolean handleNormalizeMinMaxColumns() {
        try {
            targetRow = null;
            if (sourceRow == null) {
                return false;
            }
            targetRow = new ArrayList<>();
            if (includeRowNumber) {
                targetRow.add(message("Row") + sourceRowIndex);
            }
            for (int c = 0; c < colsLen; c++) {
                int i = cols.get(c);
                if (i < 0 || i >= sourceRow.size()) {
                    targetRow.add(null);
                } else {
                    String s = sourceRow.get(i);
                    double v = DoubleTools.toDouble(s, invalidAs);
                    if (DoubleTools.invalidDouble(v)) {
                        switch (invalidAs) {
                            case Blank:
                                targetRow.add("");
                                break;
                            case Zero:
                                targetRow.add("0");
                                break;
                            default:
                                targetRow.add(s);
                                break;
                        }
                    } else {
                        v = from + statisticData[c].dTmp * (v - statisticData[c].minimum);
                        targetRow.add(DoubleTools.scale(v, scale) + "");
                    }
                }
            }
            if (otherCols != null) {
                for (int c : otherCols) {
                    if (c < 0 || c >= sourceRow.size()) {
                        targetRow.add(null);
                    } else {
                        targetRow.add(sourceRow.get(c));
                    }
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean handleNormalizeSumColumns() {
        try {
            targetRow = null;
            if (sourceRow == null) {
                return false;
            }
            targetRow = new ArrayList<>();
            if (includeRowNumber) {
                targetRow.add(message("Row") + sourceRowIndex);
            }
            for (int c = 0; c < colsLen; c++) {
                int i = cols.get(c);
                if (i < 0 || i >= sourceRow.size()) {
                    targetRow.add(null);
                } else {
                    String s = sourceRow.get(i);
                    double v = DoubleTools.toDouble(s, invalidAs);
                    if (DoubleTools.invalidDouble(v)) {
                        switch (invalidAs) {
                            case Blank:
                                targetRow.add("");
                                break;
                            case Zero:
                                targetRow.add("0");
                                break;
                            default:
                                targetRow.add(s);
                                break;
                        }
                    } else {
                        v = v * colValues[c];
                        targetRow.add(DoubleTools.scale(v, scale) + "");
                    }
                }
            }
            if (otherCols != null) {
                for (int c : otherCols) {
                    if (c < 0 || c >= sourceRow.size()) {
                        targetRow.add(null);
                    } else {
                        targetRow.add(sourceRow.get(c));
                    }
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean handleNormalizeZscoreColumns() {
        try {
            targetRow = null;
            if (sourceRow == null) {
                return false;
            }
            targetRow = new ArrayList<>();
            if (includeRowNumber) {
                targetRow.add(message("Row") + sourceRowIndex);
            }
            for (int c = 0; c < colsLen; c++) {
                int i = cols.get(c);
                if (i < 0 || i >= sourceRow.size()) {
                    targetRow.add(null);
                } else {
                    String s = sourceRow.get(i);
                    double v = DoubleTools.toDouble(s, invalidAs);
                    if (DoubleTools.invalidDouble(v)) {
                        switch (invalidAs) {
                            case Blank:
                                targetRow.add("");
                                break;
                            case Zero:
                                targetRow.add("0");
                                break;
                            default:
                                targetRow.add(s);
                                break;
                        }
                    } else {
                        double k = statisticData[c].getPopulationStandardDeviation();
                        if (k == 0) {
                            k = AppValues.TinyDouble;
                        }
                        v = (v - statisticData[c].mean) / k;
                        targetRow.add(DoubleTools.scale(v, scale) + "");
                    }
                }
            }
            if (otherCols != null) {
                for (int c : otherCols) {
                    if (c < 0 || c >= sourceRow.size()) {
                        targetRow.add(null);
                    } else {
                        targetRow.add(sourceRow.get(c));
                    }
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean handleNormalizeMinMaxAll() {
        try {
            targetRow = null;
            if (sourceRow == null) {
                return false;
            }
            targetRow = new ArrayList<>();
            if (includeRowNumber) {
                targetRow.add(message("Row") + sourceRowIndex);
            }
            for (int c = 0; c < colsLen; c++) {
                int i = cols.get(c);
                if (i < 0 || i >= sourceRow.size()) {
                    targetRow.add(null);
                } else {
                    String s = sourceRow.get(i);
                    double v = DoubleTools.toDouble(s, invalidAs);
                    if (DoubleTools.invalidDouble(v)) {
                        switch (invalidAs) {
                            case Blank:
                                targetRow.add("");
                                break;
                            case Zero:
                                targetRow.add("0");
                                break;
                            default:
                                targetRow.add(s);
                                break;
                        }
                    } else {
                        v = from + statisticAll.dTmp * (v - statisticAll.minimum);
                        targetRow.add(DoubleTools.scale(v, scale) + "");
                    }
                }
            }
            if (otherCols != null) {
                for (int c : otherCols) {
                    if (c < 0 || c >= sourceRow.size()) {
                        targetRow.add(null);
                    } else {
                        targetRow.add(sourceRow.get(c));
                    }
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean handleNormalizeSumAll() {
        try {
            targetRow = null;
            if (sourceRow == null) {
                return false;
            }
            targetRow = new ArrayList<>();
            if (includeRowNumber) {
                targetRow.add(message("Row") + sourceRowIndex);
            }
            for (int c = 0; c < colsLen; c++) {
                int i = cols.get(c);
                if (i < 0 || i >= sourceRow.size()) {
                    targetRow.add(null);
                } else {
                    String s = sourceRow.get(i);
                    double v = DoubleTools.toDouble(s, invalidAs);
                    if (DoubleTools.invalidDouble(v)) {
                        switch (invalidAs) {
                            case Blank:
                                targetRow.add("");
                                break;
                            case Zero:
                                targetRow.add("0");
                                break;
                            default:
                                targetRow.add(s);
                                break;
                        }
                    } else {
                        v = v * tValue;
                        targetRow.add(DoubleTools.scale(v, scale) + "");
                    }
                }
            }
            if (otherCols != null) {
                for (int c : otherCols) {
                    if (c < 0 || c >= sourceRow.size()) {
                        targetRow.add(null);
                    } else {
                        targetRow.add(sourceRow.get(c));
                    }
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean handleNormalizeZscoreAll() {
        try {
            targetRow = null;
            if (sourceRow == null) {
                return false;
            }
            targetRow = new ArrayList<>();
            if (includeRowNumber) {
                targetRow.add(message("Row") + sourceRowIndex);
            }
            for (int c = 0; c < colsLen; c++) {
                int i = cols.get(c);
                if (i < 0 || i >= sourceRow.size()) {
                    targetRow.add(null);
                } else {
                    String s = sourceRow.get(i);
                    double v = DoubleTools.toDouble(s, invalidAs);
                    if (DoubleTools.invalidDouble(v)) {
                        switch (invalidAs) {
                            case Blank:
                                targetRow.add("");
                                break;
                            case Zero:
                                targetRow.add("0");
                                break;
                            default:
                                targetRow.add(s);
                                break;
                        }
                    } else {
                        double k = statisticAll.getPopulationStandardDeviation();
                        if (k == 0) {
                            k = AppValues.TinyDouble;
                        }
                        v = (v - statisticAll.mean) / k;
                        targetRow.add(DoubleTools.scale(v, scale) + "");
                    }
                }
            }
            if (otherCols != null) {
                for (int c : otherCols) {
                    if (c < 0 || c >= sourceRow.size()) {
                        targetRow.add(null);
                    } else {
                        targetRow.add(sourceRow.get(c));
                    }
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean handleNormalizeRows(Normalization.Algorithm a) {
        try {
            targetRow = null;
            if (sourceRow == null) {
                return false;
            }
            targetRow = new ArrayList<>();
            if (includeRowNumber) {
                targetRow.add(message("Row") + sourceRowIndex);
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
                return false;
            }
            for (String s : values) {
                double d = DoubleTools.toDouble(s, invalidAs);
                if (DoubleTools.invalidDouble(d)) {
                    switch (invalidAs) {
                        case Zero:
                            targetRow.add("0");
                            break;
                        case Skip:
                            targetRow.add(s);
                            break;
                        case Blank:
                            targetRow.add(null);
                            break;
                    }
                } else {
                    targetRow.add(DoubleTools.scale(d, scale) + "");
                }
            }
            if (otherCols != null) {
                for (int c : otherCols) {
                    if (c < 0 || c >= sourceRow.size()) {
                        targetRow.add(null);
                    } else {
                        targetRow.add(sourceRow.get(c));
                    }
                }
            }
            return true;
        } catch (Exception e) {
            return false;
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
