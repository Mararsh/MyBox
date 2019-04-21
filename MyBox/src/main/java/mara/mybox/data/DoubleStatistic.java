package mara.mybox.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import mara.mybox.tools.ValueTools;

/**
 * @Author Mara
 * @CreateDate 2019-2-11 12:53:19
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class DoubleStatistic {

    private String name;
    private double sum;
    private double mean, variance, skewness, minimum, maximum, mode, median;

    public DoubleStatistic() {
    }

    public DoubleStatistic(String name, double sum, double mean,
            double variance, double skewness, double minimum, double maximum,
            double mode, double median) {
        this.name = name;
        this.sum = sum;
        this.mean = mean;
        this.variance = variance;
        this.skewness = skewness;
        this.minimum = minimum;
        this.maximum = maximum;
        this.mode = mode;
        this.median = median;
    }

    public static double sum(double[] values) {
        double sum = 0;
        for (int i = 0; i < values.length; i++) {
            sum += values[i];
        }
        return sum;
    }

    public static double maximum(double[] values) {
        if (values == null || values.length == 0) {
            return Integer.MIN_VALUE;
        }
        double max = Integer.MIN_VALUE;
        for (int i = 0; i < values.length; i++) {
            if (values[i] > max) {
                max = values[i];
            }
        }
        return max;
    }

    public static double maximumIndex(double[] values) {
        if (values == null || values.length == 0) {
            return -1;
        }
        double max = Integer.MIN_VALUE, maxIndex = -1;
        for (int i = 0; i < values.length; i++) {
            if (values[i] > max) {
                max = values[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    public static double minimum(double[] values) {
        if (values == null || values.length == 0) {
            return Integer.MAX_VALUE;
        }
        double min = Integer.MAX_VALUE;
        for (int i = 0; i < values.length; i++) {
            if (values[i] < min) {
                min = values[i];
            }
        }
        return min;
    }

    public static double[] sumMaxMin(double[] values) {
        double[] s = new double[3];
        if (values == null || values.length == 0) {
            return s;
        }
        double sum = 0, min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (int i = 0; i < values.length; i++) {
            sum += values[i];
            if (values[i] > max) {
                max = values[i];
            }
            if (values[i] < min) {
                min = values[i];
            }
        }
        s[0] = sum;
        s[1] = min;
        s[2] = max;
        return s;
    }

    public static double mean(double[] values) {
        if (values == null || values.length == 0) {
            return 0;
        }
        return (double) (sum(values) / values.length);
    }

    public static double median(double[] values) {
        double mid = 0;
        if (values == null || values.length == 0) {
            return mid;
        }
        double[] sorted = ValueTools.sortArray(values);
        if (sorted.length % 2 == 0) {
            mid = (sorted[sorted.length / 2] + sorted[sorted.length / 2 - 1]) / 2;
        } else {
            mid = sorted[sorted.length / 2];
        }
        return mid;
    }

    public static double medianIndex(double[] values) {
        if (values == null || values.length == 0) {
            return -1;
        }
        double[] sorted = ValueTools.sortArray(values);
        double mid = sorted[sorted.length / 2];
        for (int i = 0; i < values.length; i++) {
            if (values[i] == mid) {
                return i;
            }
        }
        return -1;
    }

    public static double variance(double[] values) {
        double mean = mean(values);
        return variance(values, mean);
    }

    public static double variance(double[] values, double mean) {
        double variance = 0;
        for (int i = 0; i < values.length; i++) {
            variance += Math.pow(values[i] - mean, 2);
        }
        variance = (double) Math.sqrt(variance / values.length);
        return variance;
    }

    public static double skewness(double[] values, double mean) {
        double skewness = 0;
        for (int i = 0; i < values.length; i++) {
            skewness += Math.pow(values[i] - mean, 3);
        }
        skewness = (double) Math.pow(skewness / values.length, 1.0 / 3);
        return skewness;
    }

    public static DoubleValue median(List<DoubleValue> values) {
        if (values == null) {
            return null;
        }
        List<DoubleValue> sorted = new ArrayList<>();
        sorted.addAll(values);
        Collections.sort(sorted, new Comparator<DoubleValue>() {
            @Override
            public int compare(DoubleValue p1, DoubleValue p2) {
                return (int) (p1.getValue() - p2.getValue());
            }
        });
        DoubleValue mid = new DoubleValue();

        if (sorted.size() % 2 == 0) {
            mid.setName(sorted.get(sorted.size() / 2).getName() + " - " + sorted.get(sorted.size() / 2 + 1).getName());
            mid.setValue((sorted.get(sorted.size() / 2).getValue() + sorted.get(sorted.size() / 2 + 1).getValue()) / 2);
        } else {
            mid.setName(sorted.get(sorted.size() / 2).getName());
            mid.setValue(sorted.get(sorted.size() / 2).getValue());
        }
        return mid;
    }

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

}
