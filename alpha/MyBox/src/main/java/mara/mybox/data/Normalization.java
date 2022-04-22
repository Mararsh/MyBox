package mara.mybox.data;

import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DoubleMatrixTools;
import mara.mybox.value.AppValues;

/**
 * @Author Mara
 * @CreateDate 2022-4-16
 * @License Apache License Version 2.0
 */
public class Normalization {

    protected double from = 0, to = 1;
    protected Algorithm a = Algorithm.MinMax;

    public static enum Algorithm {
        MinMax, Sum, ZScore, Width
    }

    public double[][] columnsNormalize(double[][] matrix) {
        return columnsNormalize(matrix, a, from, to);
    }

    public double[][] rowsNormalize(double[][] matrix) {
        return rowsNormalize(matrix, a, from, to);
    }

    public double[][] allNormalize(double[][] matrix) {
        return allNormalize(matrix, a, from, to);
    }

    /*
        static
     */
    public static Normalization create() {
        return new Normalization();
    }

    public static double[] minMax(double[] vector, double from, double to) {
        try {
            if (vector == null) {
                return vector;
            }
            int len = vector.length;
            if (len == 0) {
                return vector;
            }
            double min = Double.MAX_VALUE, max = -Double.MAX_VALUE;
            for (double d : vector) {
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
            double[] result = new double[len];
            for (int i = 0; i < len; i++) {
                result[i] = from + k * (vector[i] - min);
            }
            return result;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static double[] zscore(double[] vector) {
        try {
            if (vector == null) {
                return vector;
            }
            int len = vector.length;
            if (len == 0) {
                return vector;
            }
            double sum = 0;
            for (double d : vector) {
                sum += d;
            }
            double mean = sum / len;
            double variance = 0;
            for (double d : vector) {
                variance += Math.pow(d - mean, 2);
            }
            variance = Math.sqrt(variance / len);
            if (variance == 0) {
                variance = AppValues.TinyDouble;
            }
            double[] result = new double[len];
            for (int i = 0; i < len; i++) {
                result[i] = (vector[i] - mean) / variance;
            }
            return result;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static double[] sum(double[] vector) {
        try {
            if (vector == null) {
                return vector;
            }
            int len = vector.length;
            if (len == 0) {
                return vector;
            }
            double sum = 0;
            for (double d : vector) {
                sum += Math.abs(d);
            }
            if (sum == 0) {
                sum = AppValues.TinyDouble;
            }
            double[] result = new double[len];
            for (int i = 0; i < len; i++) {
                result[i] = vector[i] / sum;
            }
            return result;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static double[] width(double[] vector, int width) {
        try {
            if (vector == null) {
                return vector;
            }
            int len = vector.length;
            if (len == 0) {
                return vector;
            }
            double maxAbs = 0;
            for (double d : vector) {
                if (Math.abs(d) > maxAbs) {
                    maxAbs = d;
                }
            }
            if (maxAbs == 0) {
                return vector;
            }
            double[] result = new double[len];
            for (int i = 0; i < len; i++) {
                double d = vector[i];
                result[i] = width * vector[i] / maxAbs;
            }
            return result;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static double[][] columnsNormalize(double[][] matrix, Algorithm a, double from, double to) {
        try {
            if (matrix == null || matrix.length == 0 || a == null) {
                return matrix;
            }
            double[][] result = DoubleMatrixTools.transpose(matrix);
            result = rowsNormalize(result, a, from, to);
            result = DoubleMatrixTools.transpose(result);
            return result;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static double[][] rowsNormalize(double[][] matrix, Algorithm a, double from, double to) {
        try {
            if (matrix == null || matrix.length == 0 || a == null) {
                return matrix;
            }
            int rlen = matrix.length, clen = matrix[0].length;
            double[][] result = new double[rlen][clen];
            switch (a) {
                case MinMax:
                    for (int i = 0; i < rlen; i++) {
                        result[i] = minMax(matrix[i], from, to);
                    }
                    break;
                case Sum:
                    for (int i = 0; i < rlen; i++) {
                        result[i] = sum(matrix[i]);
                    }
                    break;
                case ZScore:
                    for (int i = 0; i < rlen; i++) {
                        result[i] = zscore(matrix[i]);
                    }
                    break;
                case Width:
                    for (int i = 0; i < rlen; i++) {
                        result[i] = width(matrix[i], (int) from);
                    }
                    break;
                default:
                    return null;
            }
            return result;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static double[][] allNormalize(double[][] matrix, Algorithm a, double from, double to) {
        try {
            if (matrix == null || matrix.length == 0 || a == null) {
                return matrix;
            }
            int w = matrix[0].length;
            double[] vector = DoubleMatrixTools.matrix2Array(matrix);
            switch (a) {
                case MinMax:
                    vector = minMax(vector, from, to);
                    break;
                case Sum:
                    vector = sum(vector);
                    break;
                case ZScore:
                    vector = zscore(vector);
                    break;
                case Width:
                    vector = width(vector, (int) from);
                    break;
                default:
                    return null;
            }
            double[][] result = DoubleMatrixTools.array2Matrix(vector, w);
            return result;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
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

}
