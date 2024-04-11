package mara.mybox.calculation;

import mara.mybox.db.data.ColumnDefinition.InvalidAs;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppValues;

/**
 * @Author Mara
 * @CreateDate 2022-4-16
 * @License Apache License Version 2.0
 */
public class Normalization {

    protected double from, to, max, min, sum, mean, variance, width, maxAbs;
    protected Algorithm a;
    protected String[] sourceVector, resultVector;
    protected String[][] sourceMatrix, resultMatrix;
    protected Normalization[] values;
    protected InvalidAs invalidAs;

    public static enum Algorithm {
        MinMax, Sum, ZScore, Absoluate
    }

    public Normalization() {
        initParameters();
        resetResults();
    }

    private void initParameters() {
        from = 0;
        to = 1;
        width = 0;
        a = Algorithm.MinMax;
        sourceVector = null;
        sourceMatrix = null;
        invalidAs = InvalidAs.Zero;
    }

    private void resetResults() {
        max = min = sum = mean = variance = maxAbs = Double.NaN;
        resultVector = null;
    }

    public Normalization cloneValues() {
        try {
            Normalization n = new Normalization();
            n.a = a;
            n.from = from;
            n.to = to;
            n.max = max;
            n.min = min;
            n.sum = sum;
            n.mean = mean;
            n.variance = variance;
            n.width = width;
            n.maxAbs = maxAbs;
            n.invalidAs = invalidAs;
            return n;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public String[] calculate() {
        try {
            resetResults();
            if (a == null) {
                return null;
            }
            switch (a) {
                case MinMax:
                    minMax();
                    break;
                case Sum:
                    sum();
                    break;
                case ZScore:
                    zscore();
                    break;
                case Absoluate:
                    absoluate();
                    break;
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return resultVector;
    }

    public double[] toDoubles(InvalidAs invalidAs) {
        calculate();
        if (resultVector == null) {
            return null;
        }
        double[] doubleVector = new double[resultVector.length];
        for (int i = 0; i < resultVector.length; i++) {
            String s = resultVector[i];
            doubleVector[i] = DoubleTools.toDouble(s, invalidAs);
        }
        return doubleVector;
    }

    public boolean minMax() {
        try {
            resetResults();
            if (sourceVector == null) {
                return false;
            }
            int len = sourceVector.length;
            if (len == 0) {
                return false;
            }
            min = Double.MAX_VALUE;
            max = -Double.MAX_VALUE;
            for (String s : sourceVector) {
                double d = DoubleTools.toDouble(s, invalidAs);
                if (DoubleTools.invalidDouble(d)) {
                    switch (invalidAs) {
                        case Zero:
                            d = 0;
                            break;
                        case Skip:
                        case Empty:
                            continue;
                    }
                }
                if (d > max) {
                    max = d;
                }
                if (d < min) {
                    min = d;
                }
            }
            if (min == Double.MAX_VALUE) {
                return false;
            }
            double k = max - min;
            if (k == 0) {
                k = AppValues.TinyDouble;
            }
            k = (to - from) / k;
            resultVector = new String[len];
            for (int i = 0; i < len; i++) {
                String s = sourceVector[i];
                double d = DoubleTools.toDouble(s, invalidAs);
                if (DoubleTools.invalidDouble(d)) {
                    switch (invalidAs) {
                        case Zero:
                            resultVector[i] = "0";
                            break;
                        case Skip:
                            resultVector[i] = s;
                            break;
                        case Empty:
                            resultVector[i] = "";
                            break;
                    }
                    continue;
                }
                resultVector[i] = from + k * (d - min) + "";
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean zscore() {
        try {
            resetResults();
            if (sourceVector == null) {
                return false;
            }
            int len = sourceVector.length;
            if (len == 0) {
                return false;
            }
            sum = 0;
            for (String s : sourceVector) {
                double d = DoubleTools.toDouble(s, invalidAs);
                if (DoubleTools.invalidDouble(d)) {
                    continue;
                }
                sum += d;
            }
            mean = sum / len;
            variance = 0;
            for (String s : sourceVector) {
                double d = DoubleTools.toDouble(s, invalidAs);
                if (DoubleTools.invalidDouble(d)) {
                    switch (invalidAs) {
                        case Zero:
                            d = 0;
                            break;
                        case Skip:
                        case Empty:
                            continue;
                    }
                }
                double v = d - mean;
                variance += v * v;
            }
            variance = Math.sqrt(variance / (len == 1 ? len : len - 1));
            if (variance == 0) {
                variance = AppValues.TinyDouble;
            }
            resultVector = new String[len];
            for (int i = 0; i < len; i++) {
                String s = sourceVector[i];
                double d = DoubleTools.toDouble(s, invalidAs);
                if (DoubleTools.invalidDouble(d)) {
                    switch (invalidAs) {
                        case Zero:
                            resultVector[i] = "0";
                            break;
                        case Skip:
                            resultVector[i] = s;
                            break;
                        case Empty:
                            resultVector[i] = "";
                            break;
                    }
                    continue;
                }
                resultVector[i] = (d - mean) / variance + "";
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean sum() {
        try {
            resetResults();
            if (sourceVector == null) {
                return false;
            }
            int len = sourceVector.length;
            if (len == 0) {
                return false;
            }
            sum = 0;
            for (String s : sourceVector) {
                double d = DoubleTools.toDouble(s, invalidAs);
                if (DoubleTools.invalidDouble(d)) {
                    continue;
                }
                sum += Math.abs(d);
            }
            if (sum == 0) {
                sum = AppValues.TinyDouble;
            }
            resultVector = new String[len];
            for (int i = 0; i < len; i++) {
                String s = sourceVector[i];
                double d = DoubleTools.toDouble(s, invalidAs);
                if (DoubleTools.invalidDouble(d)) {
                    switch (invalidAs) {
                        case Zero:
                            resultVector[i] = "0";
                            break;
                        case Skip:
                            resultVector[i] = s;
                            break;
                        case Empty:
                            resultVector[i] = "";
                            break;
                    }
                    continue;
                }
                resultVector[i] = d / sum + "";
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean absoluate() {
        try {
            resetResults();
            if (sourceVector == null) {
                return false;
            }
            int len = sourceVector.length;
            if (len == 0) {
                return false;
            }
            maxAbs = 0;
            for (String s : sourceVector) {
                double d = DoubleTools.toDouble(s, invalidAs);
                if (DoubleTools.invalidDouble(d)) {
                    continue;
                }
                double abs = Math.abs(d);
                if (abs > maxAbs) {
                    maxAbs = abs;
                }
            }
            resultVector = new String[len];
            for (int i = 0; i < len; i++) {
                String s = sourceVector[i];
                double d = DoubleTools.toDouble(s, invalidAs);
                if (DoubleTools.invalidDouble(d)) {
                    switch (invalidAs) {
                        case Zero:
                            resultVector[i] = "0";
                            break;
                        case Skip:
                            resultVector[i] = s;
                            break;
                        case Empty:
                            resultVector[i] = "";
                            break;
                    }
                    continue;
                }
                if (maxAbs == 0 || width == 0) {
                    resultVector[i] = "0";
                } else {
                    resultVector[i] = width * d / maxAbs + "";
                }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public String[][] columnsNormalize() {
        try {
            if (sourceMatrix == null || sourceMatrix.length == 0 || a == null) {
                return null;
            }
            String[][] orignalSource = sourceMatrix;
            sourceMatrix = StringTools.transpose(sourceMatrix);
            if (rowsNormalize() == null) {
                resetResults();
                return null;
            }
            resultMatrix = StringTools.transpose(resultMatrix);
            sourceMatrix = orignalSource;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return resultMatrix;
    }

    public String[][] rowsNormalize() {
        try {
            if (sourceMatrix == null || sourceMatrix.length == 0 || a == null) {
                return null;
            }
            int rlen = sourceMatrix.length, clen = sourceMatrix[0].length;
            resultMatrix = new String[rlen][clen];
            values = new Normalization[rlen];
            for (int i = 0; i < rlen; i++) {
                sourceVector = sourceMatrix[i];
                if (calculate() == null) {
                    resetResults();
                    return null;
                }
                resultMatrix[i] = resultVector;
                values[i] = this.cloneValues();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return resultMatrix;
    }

    public String[][] allNormalize() {
        try {
            if (sourceMatrix == null || sourceMatrix.length == 0 || a == null) {
                return null;
            }
            int w = sourceMatrix[0].length;
            sourceVector = StringTools.matrix2Array(sourceMatrix);
            if (calculate() == null) {
                resetResults();
                return null;
            }
            resultMatrix = StringTools.array2Matrix(resultVector, w);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return resultMatrix;
    }

    /*
        static
     */
    public static Normalization create() {
        return new Normalization();
    }

    /*
        get/set
     */
    public double getFrom() {
        return from;
    }

    public Normalization setFrom(double from) {
        this.from = from;
        return this;
    }

    public double getTo() {
        return to;
    }

    public Normalization setTo(double to) {
        this.to = to;
        return this;
    }

    public Algorithm getA() {
        return a;
    }

    public Normalization setA(Algorithm a) {
        this.a = a;
        return this;
    }

    public double getWidth() {
        return width;
    }

    public Normalization setWidth(double width) {
        this.width = width;
        return this;
    }

    public InvalidAs getInvalidAs() {
        return invalidAs;
    }

    public Normalization setInvalidAs(InvalidAs invalidAs) {
        this.invalidAs = invalidAs;
        return this;
    }

    public double getMax() {
        return max;
    }

    public Normalization setMax(double max) {
        this.max = max;
        return this;
    }

    public double getMin() {
        return min;
    }

    public Normalization setMin(double min) {
        this.min = min;
        return this;
    }

    public double getSum() {
        return sum;
    }

    public Normalization setSum(double sum) {
        this.sum = sum;
        return this;
    }

    public double getMean() {
        return mean;
    }

    public Normalization setMean(double mean) {
        this.mean = mean;
        return this;
    }

    public double getVariance() {
        return variance;
    }

    public Normalization setVariance(double variance) {
        this.variance = variance;
        return this;
    }

    public String[] getSourceVector() {
        return sourceVector;
    }

    public Normalization setSourceVector(String[] sourceVector) {
        this.sourceVector = sourceVector;
        return this;
    }

    public String[] getResultVector() {
        return resultVector;
    }

    public Normalization setResultVector(String[] resultVector) {
        this.resultVector = resultVector;
        return this;
    }

    public String[][] getSourceMatrix() {
        return sourceMatrix;
    }

    public Normalization setSourceMatrix(String[][] sourceMatrix) {
        this.sourceMatrix = sourceMatrix;
        return this;
    }

    public String[][] getResultMatrix() {
        return resultMatrix;
    }

    public Normalization setResultMatrix(String[][] resultMatrix) {
        this.resultMatrix = resultMatrix;
        return this;
    }

    public double getMaxAbs() {
        return maxAbs;
    }

    public Normalization setMaxAbs(double maxAbs) {
        this.maxAbs = maxAbs;
        return this;
    }

    public Normalization[] getValues() {
        return values;
    }

    public Normalization setValues(Normalization[] values) {
        this.values = values;
        return this;
    }

}
