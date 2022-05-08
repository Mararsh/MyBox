package mara.mybox.calculation;

import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DoubleMatrixTools;
import mara.mybox.value.AppValues;

/**
 * @Author Mara
 * @CreateDate 2022-4-16
 * @License Apache License Version 2.0
 */
public class Normalization {

    protected double from, to, max, min, sum, mean, variance, width, maxAbs;
    protected Algorithm a;
    protected double[] sourceVector, resultVector;
    protected double[][] sourceMatrix, resultMatrix;
    protected Normalization[] values;

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
            return n;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public double[] calculate() {
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
            MyBoxLog.error(e.toString());
        }
        return resultVector;
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
            for (double d : sourceVector) {
                if (d > max) {
                    max = d;
                }
                if (d < min) {
                    min = d;
                }
            }
            double k = max - min;
            if (k == 0) {
                k = AppValues.TinyDouble;
            }
            k = (to - from) / k;
            resultVector = new double[len];
            for (int i = 0; i < len; i++) {
                resultVector[i] = from + k * (sourceVector[i] - min);
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
            for (double d : sourceVector) {
                sum += d;
            }
            mean = sum / len;
            variance = 0;
            for (double d : sourceVector) {
                variance += Math.pow(d - mean, 2);
            }
            variance = Math.sqrt(variance / len);
            if (variance == 0) {
                variance = AppValues.TinyDouble;
            }
            resultVector = new double[len];
            for (int i = 0; i < len; i++) {
                resultVector[i] = (sourceVector[i] - mean) / variance;
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
            for (double d : sourceVector) {
                sum += Math.abs(d);
            }
            if (sum == 0) {
                sum = AppValues.TinyDouble;
            }
            resultVector = new double[len];
            for (int i = 0; i < len; i++) {
                resultVector[i] = sourceVector[i] / sum;
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
            for (double d : sourceVector) {
                double abs = Math.abs(d);
                if (abs > maxAbs) {
                    maxAbs = abs;
                }
            }
            resultVector = new double[len];
            for (int i = 0; i < len; i++) {
                if (maxAbs == 0 || width == 0) {
                    resultVector[i] = 0;
                } else {
                    resultVector[i] = width * sourceVector[i] / maxAbs;
                }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public double[][] columnsNormalize() {
        try {
            if (sourceMatrix == null || sourceMatrix.length == 0 || a == null) {
                return null;
            }
            double[][] orignalSource = sourceMatrix;
            sourceMatrix = DoubleMatrixTools.transpose(sourceMatrix);
            if (rowsNormalize() == null) {
                resetResults();
                return null;
            }
            resultMatrix = DoubleMatrixTools.transpose(resultMatrix);
            sourceMatrix = orignalSource;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return resultMatrix;
    }

    public double[][] rowsNormalize() {
        try {
            if (sourceMatrix == null || sourceMatrix.length == 0 || a == null) {
                return null;
            }
            int rlen = sourceMatrix.length, clen = sourceMatrix[0].length;
            resultMatrix = new double[rlen][clen];
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
            MyBoxLog.error(e.toString());
        }
        return resultMatrix;
    }

    public double[][] allNormalize() {
        try {
            if (sourceMatrix == null || sourceMatrix.length == 0 || a == null) {
                return null;
            }
            int w = sourceMatrix[0].length;
            sourceVector = DoubleMatrixTools.matrix2Array(sourceMatrix);
            if (calculate() == null) {
                resetResults();
                return null;
            }
            resultMatrix = DoubleMatrixTools.array2Matrix(resultVector, w);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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

    public double[] getSourceVector() {
        return sourceVector;
    }

    public Normalization setSourceVector(double[] sourceVector) {
        this.sourceVector = sourceVector;
        return this;
    }

    public double[] getResultVector() {
        return resultVector;
    }

    public Normalization setResultVector(double[] resultVector) {
        this.resultVector = resultVector;
        return this;
    }

    public double[][] getSourceMatrix() {
        return sourceMatrix;
    }

    public Normalization setSourceMatrix(double[][] sourceMatrix) {
        this.sourceMatrix = sourceMatrix;
        return this;
    }

    public double[][] getResultMatrix() {
        return resultMatrix;
    }

    public Normalization setResultMatrix(double[][] resultMatrix) {
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
