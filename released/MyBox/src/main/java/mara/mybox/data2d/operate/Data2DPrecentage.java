package mara.mybox.data2d.operate;

import java.util.ArrayList;
import java.util.List;
import mara.mybox.data2d.Data2D_Edit;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DoubleTools;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public class Data2DPrecentage extends Data2DOperate {

    protected Type type;
    protected double[] colValues;
    protected double tValue;
    protected String toNegative;
    protected List<String> firstRow;

    public static enum Type {
        ColumnsPass1, ColumnsPass2, Rows, AllPass1, AllPass2
    }

    public static Data2DPrecentage create(Data2D_Edit data) {
        Data2DPrecentage op = new Data2DPrecentage();
        return op.setSourceData(data) ? op : null;
    }

    @Override
    public boolean checkParameters() {
        if (!super.checkParameters() || cols == null || cols.isEmpty() || type == null) {
            return false;
        }
        switch (type) {
            case ColumnsPass1:
                colValues = new double[colsLen];
                break;
            case ColumnsPass2:
                if (colValues == null) {
                    return false;
                }
                break;
            case AllPass1:
                tValue = 0d;
                break;
            case AllPass2:
                break;
            case Rows:
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public boolean openWriters() {
        if (!super.openWriters()) {
            return false;
        }
        if (firstRow != null) {
            targetRow = firstRow;
            writeRow();
        }
        return true;
    }

    @Override
    public boolean handleRow() {
        switch (type) {
            case ColumnsPass1:
                return handlePercentageColumnsPass1();
            case ColumnsPass2:
                return handlePercentageColumnsPass2();
            case AllPass1:
                return handlePercentageAllPass1();
            case AllPass2:
                return handlePercentageAllPass2();
            case Rows:
                return handlePercentageRows();
        }
        return false;
    }

    public boolean handlePercentageColumnsPass1() {
        try {
            if (sourceRow == null) {
                return false;
            }
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
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean handlePercentageColumnsPass2() {
        try {
            targetRow = null;
            if (sourceRow == null) {
                return false;
            }
            targetRow = new ArrayList<>();
            targetRow.add("" + sourceRowIndex);
            for (int c = 0; c < colsLen; c++) {
                int i = cols.get(c);
                double d;
                if (i >= 0 && i < sourceRow.size()) {
                    d = DoubleTools.toDouble(sourceRow.get(i), invalidAs);
                } else {
                    d = DoubleTools.value(invalidAs);
                }
                double s = colValues[c];
                if (DoubleTools.invalidDouble(d) || s == 0) {
                    targetRow.add(Double.NaN + "");
                } else {
                    if (d < 0) {
                        if ("skip".equals(toNegative)) {
                            targetRow.add(Double.NaN + "");
                            continue;
                        } else if ("abs".equals(toNegative)) {
                            d = Math.abs(d);
                        } else {
                            d = 0;
                        }
                    }
                    targetRow.add(DoubleTools.percentage(d, s, scale));
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
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return false;
        }
    }

    public boolean handlePercentageAllPass1() {
        try {
            if (sourceRow == null) {
                return false;
            }
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
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean handlePercentageAllPass2() {
        try {
            targetRow = null;
            if (sourceRow == null) {
                return false;
            }
            targetRow = new ArrayList<>();
            targetRow.add("" + sourceRowIndex);
            for (int c = 0; c < colsLen; c++) {
                int i = cols.get(c);
                double d;
                if (i >= 0 && i < sourceRow.size()) {
                    d = DoubleTools.toDouble(sourceRow.get(i), invalidAs);
                } else {
                    d = DoubleTools.value(invalidAs);
                }
                if (DoubleTools.invalidDouble(d) || tValue == 0) {
                    targetRow.add(Double.NaN + "");
                } else {
                    if (d < 0) {
                        if ("skip".equals(toNegative)) {
                            targetRow.add(Double.NaN + "");
                            continue;
                        } else if ("abs".equals(toNegative)) {
                            d = Math.abs(d);
                        } else {
                            d = 0;
                        }
                    }
                    targetRow.add(DoubleTools.percentage(d, tValue, scale));
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
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return false;
        }
    }

    public boolean handlePercentageRows() {
        try {
            targetRow = null;
            if (sourceRow == null) {
                return false;
            }
            targetRow = new ArrayList<>();
            targetRow.add("" + sourceRowIndex);
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
            targetRow.add(DoubleTools.scale(sum, scale) + "");
            for (int c = 0; c < colsLen; c++) {
                int i = cols.get(c);
                double d = 0;
                if (i >= 0 && i < sourceRow.size()) {
                    d = DoubleTools.toDouble(sourceRow.get(i), invalidAs);
                }
                if (DoubleTools.invalidDouble(d) || sum == 0) {
                    targetRow.add(Double.NaN + "");
                } else {
                    if (d < 0) {
                        if ("skip".equals(toNegative)) {
                            targetRow.add(Double.NaN + "");
                            continue;
                        } else if ("abs".equals(toNegative)) {
                            d = Math.abs(d);
                        } else {
                            d = 0;
                        }
                    }
                    targetRow.add(DoubleTools.percentage(d, sum, scale));
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
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return false;
        }
    }

    /*
        set
     */
    public Data2DPrecentage setType(Type type) {
        this.type = type;
        return this;
    }

    public Data2DPrecentage setColValues(double[] colValues) {
        this.colValues = colValues;
        return this;
    }

    public Data2DPrecentage settValue(double tValue) {
        this.tValue = tValue;
        return this;
    }

    public Data2DPrecentage setToNegative(String toNegative) {
        this.toNegative = toNegative;
        return this;
    }

    public Data2DPrecentage setFirstRow(List<String> firstRow) {
        this.firstRow = firstRow;
        return this;
    }

    /*
        get
     */
    public double[] getColValues() {
        return colValues;
    }

    public double gettValue() {
        return tValue;
    }

}
