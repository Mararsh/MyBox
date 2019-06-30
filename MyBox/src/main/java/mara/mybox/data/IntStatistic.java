package mara.mybox.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.IntTools;

/**
 * @Author Mara
 * @CreateDate 2019-2-11 12:53:19
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class IntStatistic {

    private String name;
    private long sum;
    private int mean, variance, skewness, minimum, maximum, mode, median;

    public IntStatistic() {
    }

    public IntStatistic(String name, long sum, int mean,
            int variance, int skewness, int minimum, int maximum,
            int mode, int median) {
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

    public static long sum(int[] values) {
        long sum = 0;
        for (int i = 0; i < values.length; i++) {
            sum += values[i];
        }
        return sum;
    }

    public static int maximum(int[] values) {
        if (values == null || values.length == 0) {
            return Integer.MIN_VALUE;
        }
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < values.length; i++) {
            if (values[i] > max) {
                max = values[i];
            }
        }
        return max;
    }

    public static int maximumIndex(int[] values) {
        if (values == null || values.length == 0) {
            return -1;
        }
        int max = Integer.MIN_VALUE, maxIndex = -1;
        for (int i = 0; i < values.length; i++) {
            if (values[i] > max) {
                max = values[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    public static int minimum(int[] values) {
        if (values == null || values.length == 0) {
            return Integer.MAX_VALUE;
        }
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < values.length; i++) {
            if (values[i] < min) {
                min = values[i];
            }
        }
        return min;
    }

    public static long[] sumMaxMin(int[] values) {
        long[] s = new long[3];
        if (values == null || values.length == 0) {
            return s;
        }
        int sum = 0, min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
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

    public static int mean(int[] values) {
        if (values == null || values.length == 0) {
            return 0;
        }
        return (int) (sum(values) / values.length);
    }

    public static int median(int[] values) {
        int mid = 0;
        if (values == null || values.length == 0) {
            return mid;
        }
        int[] sorted = IntTools.sortArray(values);
        if (sorted.length % 2 == 0) {
            mid = (sorted[sorted.length / 2] + sorted[sorted.length / 2 - 1]) / 2;
        } else {
            mid = sorted[sorted.length / 2];
        }
        return mid;
    }

    public static int medianIndex(int[] values) {
        if (values == null || values.length == 0) {
            return -1;
        }
        int[] sorted = IntTools.sortArray(values);
        int mid = sorted[sorted.length / 2];
        for (int i = 0; i < values.length; i++) {
            if (values[i] == mid) {
                return i;
            }
        }
        return -1;
    }

    public static int variance(int[] values) {
        int mean = mean(values);
        return variance(values, mean);
    }

    public static int variance(int[] values, int mean) {
        int variance = 0;
        for (int i = 0; i < values.length; i++) {
            variance += Math.pow(values[i] - mean, 2);
        }
        variance = (int) Math.sqrt(variance / values.length);
        return variance;
    }

    public static int skewness(int[] values, int mean) {
        int skewness = 0;
        for (int i = 0; i < values.length; i++) {
            skewness += Math.pow(values[i] - mean, 3);
        }
        skewness = (int) Math.pow(skewness / values.length, 1.0 / 3);
        return skewness;
    }

    public static IntValue median(List<IntValue> values) {
        if (values == null) {
            return null;
        }
        List<IntValue> sorted = new ArrayList<>();
        sorted.addAll(values);
        Collections.sort(sorted, new Comparator<IntValue>() {
            @Override
            public int compare(IntValue p1, IntValue p2) {
                return p1.getValue() - p2.getValue();
            }
        });
        IntValue mid = new IntValue();

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

    public long getSum() {
        return sum;
    }

    public void setSum(long sum) {
        this.sum = sum;
    }

    public int getMean() {
        return mean;
    }

    public void setMean(int mean) {
        this.mean = mean;
    }

    public int getVariance() {
        return variance;
    }

    public void setVariance(int variance) {
        this.variance = variance;
    }

    public int getSkewness() {
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

}
