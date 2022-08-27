package mara.mybox.data2d.reader;

import java.util.ArrayList;
import java.util.List;
import mara.mybox.data2d.Data2D_Attributes.InvalidAs;
import mara.mybox.data2d.Data2D_Edit;
import mara.mybox.tools.DoubleTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public class Data2DPrecentage extends Data2DOperator {

    protected Type type;
    protected double[] colValues;
    protected boolean withValues;
    protected double tValue;
    protected String toNegative;

    public static enum Type {
        ColumnsPass1, ColumnsPass2, Rows, AllPass1, AllPass2
    }

    public static Data2DPrecentage create(Data2D_Edit data) {
        Data2DPrecentage op = new Data2DPrecentage();
        return op.setData(data) ? op : null;
    }

    @Override
    public boolean checkParameters() {
        if (cols == null || cols.isEmpty() || type == null) {
            return false;
        }
        switch (type) {
            case ColumnsPass1:
                colValues = new double[colsLen];
                break;
            case ColumnsPass2:
                if (colValues == null || csvPrinter == null) {
                    return false;
                }
                break;
            case AllPass1:
                tValue = 0d;
                break;
            case AllPass2:
                if (csvPrinter == null) {
                    return false;
                }
                break;
            case Rows:
                if (csvPrinter == null) {
                    return false;
                }
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public void handleRow() {
        switch (type) {
            case ColumnsPass1:
                handlePercentageColumnsPass1();
                break;
            case ColumnsPass2:
                handlePercentageColumnsPass2();
                break;
            case AllPass1:
                handlePercentageAllPass1();
                break;
            case AllPass2:
                handlePercentageAllPass2();
                break;
            case Rows:
                handlePercentageRows();
                break;
        }
    }

    public void handlePercentageColumnsPass1() {
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

    public void handlePercentageColumnsPass2() {
        try {
            List<String> row = new ArrayList<>();
            row.add(message("Row") + rowIndex);
            for (int c = 0; c < colsLen; c++) {
                int i = cols.get(c);
                String v;
                double d;
                if (i >= 0 && i < sourceRow.size()) {
                    v = sourceRow.get(i);
                    d = DoubleTools.toDouble(v, invalidAs);
                } else {
                    d = DoubleTools.value(invalidAs);
                    v = d + "";
                }
                if (withValues) {
                    if (DoubleTools.invalidDouble(d)) {
                        if (invalidAs == InvalidAs.Skip) {
                            row.add(v);
                        } else {
                            row.add(Double.NaN + "");
                        }
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

    public void handlePercentageAllPass1() {
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

    public void handlePercentageAllPass2() {
        try {
            List<String> row = new ArrayList<>();
            row.add(message("Row") + rowIndex);
            for (int c = 0; c < colsLen; c++) {
                int i = cols.get(c);
                double d;
                String v;
                if (i >= 0 && i < sourceRow.size()) {
                    v = sourceRow.get(i);
                    d = DoubleTools.toDouble(v, invalidAs);
                } else {
                    d = DoubleTools.value(invalidAs);
                    v = d + "";
                }
                if (withValues) {
                    if (DoubleTools.invalidDouble(d)) {
                        if (invalidAs == InvalidAs.Skip) {
                            row.add(v);
                        } else {
                            row.add(Double.NaN + "");
                        }
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
                String v = null;
                if (i >= 0 && i < sourceRow.size()) {
                    v = sourceRow.get(i);
                    d = DoubleTools.toDouble(sourceRow.get(i), invalidAs);
                }
                if (withValues) {
                    if (DoubleTools.invalidDouble(d)) {
                        if (invalidAs == InvalidAs.Skip) {
                            row.add(v);
                        } else {
                            row.add(Double.NaN + "");
                        }
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

    public Data2DPrecentage setWithValues(boolean withValues) {
        this.withValues = withValues;
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
