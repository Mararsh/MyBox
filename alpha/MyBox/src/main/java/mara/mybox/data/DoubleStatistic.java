package mara.mybox.data;

import java.util.HashMap;
import java.util.Map;
import mara.mybox.tools.DoubleArrayTools;
import mara.mybox.value.AppValues;

/**
 * @Author Mara
 * @CreateDate 2021-10-4
 * @License Apache License Version 2.0
 */
public class DoubleStatistic {

    public String name;
    public int count;
    public double sum;
    public double mean, variance, skewness, minimum, maximum, mode, median;

    public DoubleStatistic() {
        sum = count = 0;
        minimum = Double.MAX_VALUE;
        maximum = -Double.MAX_VALUE;
    }

    public DoubleStatistic(double[] values) {
        calculate(values, true, true);
    }

    public DoubleStatistic(double[] values, boolean calMode, boolean calMedian) {
        calculate(values, calMode, calMedian);
    }

    public final void calculate(double[] values, boolean calMode, boolean calMedian) {
        if (values == null || values.length == 0) {
            return;
        }
        count = values.length;
        sum = 0;
        minimum = Double.MAX_VALUE;
        maximum = -Double.MAX_VALUE;
        for (int i = 0; i < count; ++i) {
            double v = values[i];
            sum += v;
            if (v > maximum) {
                maximum = v;
            }
            if (v < minimum) {
                minimum = v;
            }
        }
        mean = sum / count;
        variance = 0;
        skewness = 0;
        mode = calMode ? mode(values) : Double.NaN;
        median = calMedian ? median(values) : Double.NaN;
        for (int i = 0; i < values.length; ++i) {
            double v = values[i];
            variance += Math.pow(v - mean, 2);
            skewness += Math.pow(v - mean, 3);
        }
        variance = Math.sqrt(variance / count);
        skewness = Math.cbrt(skewness / count);
    }


    /*
        static methods
     */
    public static double sum(double[] values) {
        if (values == null || values.length == 0) {
            return AppValues.InvalidDouble;
        }
        double sum = 0;
        for (int i = 0; i < values.length; ++i) {
            sum += values[i];
        }
        return sum;
    }

    public static double maximum(double[] values) {
        if (values == null || values.length == 0) {
            return AppValues.InvalidDouble;
        }
        double max = -Double.MAX_VALUE;
        for (int i = 0; i < values.length; ++i) {
            if (values[i] > max) {
                max = values[i];
            }
        }
        return max;
    }

    public static double minimum(double[] values) {
        if (values == null || values.length == 0) {
            return AppValues.InvalidDouble;
        }
        double min = Double.MAX_VALUE;
        for (int i = 0; i < values.length; ++i) {
            if (values[i] < min) {
                min = values[i];
            }
        }
        return min;
    }

    public static double mean(double[] values) {
        if (values == null || values.length == 0) {
            return AppValues.InvalidDouble;
        }
        return sum(values) / values.length;
    }

    public static double mode(double[] values) {
        try {
            if (values == null || values.length == 0) {
                return AppValues.InvalidDouble;
            }
            double mode = 0;
            Map<Double, Integer> number = new HashMap<>();
            for (double value : values) {
                if (number.containsKey(value)) {
                    number.put(value, number.get(value) + 1);
                } else {
                    number.put(value, 1);
                }
            }
            double num = 0;
            for (double value : number.keySet()) {
                if (num < number.get(value)) {
                    mode = value;
                }
            }
            return mode;
        } catch (Exception e) {
            return AppValues.InvalidDouble;
        }
    }

    public static double median(double[] values) {
        try {
            if (values == null || values.length == 0) {
                return AppValues.InvalidDouble;
            }
            double[] sorted = DoubleArrayTools.sortArray(values);
            int len = sorted.length;
            if (len == 2) {
                return (sorted[0] + sorted[1]) / 2;
            } else if (len % 2 == 0) {
                return (sorted[len / 2] + sorted[len / 2 + 1]) / 2;
            } else {
                return sorted[len / 2];
            }
        } catch (Exception e) {
            return AppValues.InvalidDouble;
        }
    }

    public static double variance(double[] values) {
        if (values == null || values.length == 0) {
            return AppValues.InvalidDouble;
        }
        double mean = mean(values);
        return variance(values, mean);
    }

    public static double variance(double[] values, double mean) {
        if (values == null || values.length == 0) {
            return AppValues.InvalidDouble;
        }
        double variance = 0;
        for (int i = 0; i < values.length; ++i) {
            variance += Math.pow(values[i] - mean, 2);
        }
        variance = Math.sqrt(variance / values.length);
        return variance;
    }

    public static double skewness(double[] values, double mean) {
        if (values == null || values.length == 0) {
            return AppValues.InvalidDouble;
        }
        double skewness = 0;
        for (int i = 0; i < values.length; ++i) {
            skewness += Math.pow(values[i] - mean, 3);
        }
        skewness = Math.cbrt(skewness / values.length);
        return skewness;
    }

    /*
        get/set
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getSum() {
        return sum;
    }

    public void setSum(double sum) {
        this.sum = sum;
    }

    public double getMean() {
        return mean;
    }

    public void setMean(double mean) {
        this.mean = mean;
    }

    public double getVariance() {
        return variance;
    }

    public void setVariance(double variance) {
        this.variance = variance;
    }

    public double getSkewness() {
        return skewness;
    }

    public void setSkewness(double skewness) {
        this.skewness = skewness;
    }

    public double getMinimum() {
        return minimum;
    }

    public void setMinimum(double minimum) {
        this.minimum = minimum;
    }

    public double getMaximum() {
        return maximum;
    }

    public void setMaximum(double maximum) {
        this.maximum = maximum;
    }

    public double getMode() {
        return mode;
    }

    public void setMode(double mode) {
        this.mode = mode;
    }

    public double getMedian() {
        return median;
    }

    public void setMedian(double median) {
        this.median = median;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

}
