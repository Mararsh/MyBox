package mara.mybox.data;

import java.util.HashMap;
import java.util.Map;
import mara.mybox.tools.FloatTools;
import mara.mybox.value.AppValues;

/**
 * @Author Mara
 * @CreateDate 2021-10-4
 * @License Apache License Version 2.0
 */
public class FloatStatistic {

    private String name;
    private int count;
    private float minimum, maximum, mode, median;
    private double sum, mean, variance, skewness;

    public FloatStatistic() {
    }

    public FloatStatistic(float[] values) {
        if (values == null || values.length == 0) {
            return;
        }
        count = values.length;
        sum = 0;
        minimum = Float.MAX_VALUE;
        maximum = Float.MIN_VALUE;
        for (int i = 0; i < count; ++i) {
            float v = values[i];
            sum += v;
            if (v > maximum) {
                maximum = v;
            }
            if (v < minimum) {
                minimum = v;
            }
        }
        mean = sum / values.length;
        variance = 0;
        skewness = 0;
        mode = mode(values);
        median = median(values);
        for (int i = 0; i < values.length; ++i) {
            float v = values[i];
            variance += Math.pow(v - mean, 2);
            skewness += Math.pow(v - mean, 3);
        }
        variance = Math.sqrt(variance / count);
        skewness = Math.cbrt(skewness / count);
    }


    /*
        static methods
     */
    public static double sum(float[] values) {
        if (values == null || values.length == 0) {
            return AppValues.InvalidLong;
        }
        double sum = 0;
        for (int i = 0; i < values.length; ++i) {
            sum += values[i];
        }
        return sum;
    }

    public static float maximum(float[] values) {
        if (values == null || values.length == 0) {
            return AppValues.InvalidLong;
        }
        float max = Float.MIN_VALUE;
        for (int i = 0; i < values.length; ++i) {
            if (values[i] > max) {
                max = values[i];
            }
        }
        return max;
    }

    public static float minimum(float[] values) {
        if (values == null || values.length == 0) {
            return AppValues.InvalidLong;
        }
        float min = Float.MAX_VALUE;
        for (int i = 0; i < values.length; ++i) {
            if (values[i] < min) {
                min = values[i];
            }
        }
        return min;
    }

    public static double mean(float[] values) {
        if (values == null || values.length == 0) {
            return AppValues.InvalidLong;
        }
        return sum(values) / values.length;
    }

    public static float mode(float[] values) {

        if (values == null || values.length == 0) {
            return AppValues.InvalidLong;
        }
        float mode = 0;
        Map<Float, Integer> number = new HashMap<>();
        for (float value : values) {
            if (number.containsKey(value)) {
                number.put(value, number.get(value) + 1);
            } else {
                number.put(value, 1);
            }
        }
        float num = 0;
        for (float value : number.keySet()) {
            if (num < number.get(value)) {
                mode = value;
            }
        }
        return mode;
    }

    public static float median(float[] values) {
        if (values == null || values.length == 0) {
            return AppValues.InvalidLong;
        }
        float[] sorted = FloatTools.sortArray(values);
        int len = sorted.length;
        if (len == 2) {
            return (sorted[0] + sorted[1]) / 2;
        } else if (len % 2 == 0) {
            return (sorted[len / 2] + sorted[len / 2 + 1]) / 2;
        } else {
            return sorted[len / 2];
        }
    }

    public static double variance(float[] values) {
        if (values == null || values.length == 0) {
            return AppValues.InvalidLong;
        }
        double mean = mean(values);
        return variance(values, mean);
    }

    public static double variance(float[] values, double mean) {
        if (values == null || values.length == 0) {
            return AppValues.InvalidLong;
        }
        double variance = 0;
        for (int i = 0; i < values.length; ++i) {
            variance += Math.pow(values[i] - mean, 2);
        }
        variance = Math.sqrt(variance / values.length);
        return variance;
    }

    public static double skewness(float[] values, double mean) {
        if (values == null || values.length == 0) {
            return AppValues.InvalidLong;
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

    public void setSum(float sum) {
        this.sum = sum;
    }

    public double getMean() {
        return mean;
    }

    public void setMean(float mean) {
        this.mean = mean;
    }

    public double getVariance() {
        return variance;
    }

    public void setVariance(float variance) {
        this.variance = variance;
    }

    public double getSkewness() {
        return skewness;
    }

    public void setSkewness(float skewness) {
        this.skewness = skewness;
    }

    public float getMinimum() {
        return minimum;
    }

    public void setMinimum(float minimum) {
        this.minimum = minimum;
    }

    public float getMaximum() {
        return maximum;
    }

    public void setMaximum(float maximum) {
        this.maximum = maximum;
    }

    public float getMode() {
        return mode;
    }

    public void setMode(float mode) {
        this.mode = mode;
    }

    public float getMedian() {
        return median;
    }

    public void setMedian(float median) {
        this.median = median;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

}
