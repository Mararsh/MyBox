package mara.mybox.calculation;

import java.util.HashMap;
import java.util.Map;
import mara.mybox.tools.IntTools;
import mara.mybox.value.AppValues;

/**
 * @Author Mara
 * @CreateDate 2019-2-11 12:53:19
 * @License Apache License Version 2.0
 */
public class IntStatistic {

    private String name;
    private int count;
    private long sum;
    private int minimum, maximum, mode, median;
    private double mean, standardDeviation, skewness;

    public IntStatistic() {
    }

    public IntStatistic(int[] values) {
        if (values == null || values.length == 0) {
            return;
        }
        count = values.length;
        sum = 0;
        minimum = Integer.MAX_VALUE;
        maximum = Integer.MIN_VALUE;
        for (int i = 0; i < count; ++i) {
            int v = values[i];
            sum += v;
            if (v > maximum) {
                maximum = v;
            }
            if (v < minimum) {
                minimum = v;
            }
        }
        mean = sum / values.length;
        standardDeviation = 0;
        skewness = 0;
        mode = mode(values);
        median = median(values);
        for (int i = 0; i < values.length; ++i) {
            double v = values[i] - mean;
            double v2 = v * v;
            standardDeviation += v2;
            skewness += v2 * v;
        }
        standardDeviation = Math.sqrt(standardDeviation / count);
        skewness = Math.cbrt(skewness / count);
    }

    public IntStatistic(String name, long sum, int mean, int variance, int skewness,
            int minimum, int maximum, int[] histogram) {
        this.name = name;
        this.sum = sum;
        this.mean = mean;
        this.standardDeviation = variance;
        this.skewness = skewness;
        this.minimum = minimum;
        this.maximum = maximum;
        this.mode = maximumIndex(histogram);
        this.median = medianIndex(histogram);
    }


    /*
        static methods
     */
    public static long sum(int[] values) {
        if (values == null || values.length == 0) {
            return AppValues.InvalidLong;
        }
        long sum = 0;
        for (int i = 0; i < values.length; ++i) {
            sum += values[i];
        }
        return sum;
    }

    public static int maximum(int[] values) {
        if (values == null || values.length == 0) {
            return AppValues.InvalidInteger;
        }
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < values.length; ++i) {
            if (values[i] > max) {
                max = values[i];
            }
        }
        return max;
    }

    public static int minimum(int[] values) {
        if (values == null || values.length == 0) {
            return AppValues.InvalidInteger;
        }
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < values.length; ++i) {
            if (values[i] < min) {
                min = values[i];
            }
        }
        return min;
    }

    public static double mean(int[] values) {
        if (values == null || values.length == 0) {
            return AppValues.InvalidInteger;
        }
        return sum(values) * 1d / values.length;
    }

    public static int mode(int[] values) {
        if (values == null || values.length == 0) {
            return AppValues.InvalidInteger;
        }
        int mode = 0;
        Map<Integer, Integer> number = new HashMap<>();
        for (int value : values) {
            if (number.containsKey(value)) {
                number.put(value, number.get(value) + 1);
            } else {
                number.put(value, 1);
            }
        }
        int num = 0;
        for (int value : number.keySet()) {
            if (num < number.get(value)) {
                mode = value;
            }
        }
        return mode;
    }

    public static int median(int[] values) {
        if (values == null || values.length == 0) {
            return AppValues.InvalidInteger;
        }
        int[] sorted = IntTools.sortArray(values);
        int len = sorted.length;
        if (len == 2) {
            return (sorted[0] + sorted[1]) / 2;
        } else if (len % 2 == 0) {
            return (sorted[len / 2] + sorted[len / 2 + 1]) / 2;
        } else {
            return sorted[len / 2];
        }
    }

    public static double variance(int[] values) {
        if (values == null || values.length == 0) {
            return AppValues.InvalidDouble;
        }
        double mean = mean(values);
        return variance(values, mean);
    }

    public static double variance(int[] values, double mean) {
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

    public static double skewness(int[] values, double mean) {
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

    public static int medianIndex(int[] values) {
        if (values == null || values.length == 0) {
            return -1;
        }
        int[] sorted = IntTools.sortArray(values);
        int mid = sorted[sorted.length / 2];
        for (int i = 0; i < values.length; ++i) {
            if (values[i] == mid) {
                return i;
            }
        }
        return -1;
    }

    public static int maximumIndex(int[] values) {
        if (values == null || values.length == 0) {
            return -1;
        }
        int max = Integer.MIN_VALUE, maxIndex = -1;
        for (int i = 0; i < values.length; ++i) {
            if (values[i] > max) {
                max = values[i];
                maxIndex = i;
            }
        }
        return maxIndex;
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

    public long getSum() {
        return sum;
    }

    public void setSum(long sum) {
        this.sum = sum;
    }

    public double getMean() {
        return mean;
    }

    public void setMean(double mean) {
        this.mean = mean;
    }

    public double getStandardDeviation() {
        return standardDeviation;
    }

    public void setStandardDeviation(double standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    public double getSkewness() {
        return skewness;
    }

    public void setSkewness(int skewness) {
        this.skewness = skewness;
    }

    public int getMinimum() {
        return minimum;
    }

    public void setMinimum(int minimum) {
        this.minimum = minimum;
    }

    public int getMaximum() {
        return maximum;
    }

    public void setMaximum(int maximum) {
        this.maximum = maximum;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getMedian() {
        return median;
    }

    public void setMedian(int median) {
        this.median = median;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

}
