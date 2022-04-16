package mara.mybox.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DoubleArrayTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppValues;
import org.apache.commons.math3.stat.descriptive.moment.Skewness;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

/**
 * @Author Mara
 * @CreateDate 2021-10-4
 * @License Apache License Version 2.0
 */
public class DoubleStatistic {

    public String name;
    public long count;
    public double sum, mean, geometricMean, sumSquares,
            populationVariance, sampleVariance, populationStandardDeviation, sampleStandardDeviation, skewness,
            minimum, maximum, median, upperQuartile, lowerQuartile, vTmp;
    public Object mode;
    public StatisticOptions options;
    public String[] strings;
    public double[] doubles;

    public DoubleStatistic() {
        init();
    }

    public final void init() {
        count = 0;
        sum = 0;
        mean = 0;
        geometricMean = 1;
        sumSquares = 0;
        populationVariance = 0;
        sampleVariance = 0;
        populationStandardDeviation = 0;
        sampleStandardDeviation = 0;
        skewness = 0;
        maximum = -Double.MAX_VALUE;
        minimum = Double.MAX_VALUE;
        median = 0;
        upperQuartile = 0;
        lowerQuartile = 0;
        mode = 0;
        vTmp = 0;
    }

    public DoubleStatistic(double[] values) {
        calculate(values, StatisticOptions.all(true));
    }

    public DoubleStatistic(double[] values, StatisticOptions options) {
        calculate(values, options);
    }

    public DoubleStatistic(String[] values, StatisticOptions options) {
        if (options != null && options.isMode()) {
            strings = values;
        }
        calculate(toDouble(values), options);
    }

    public final void calculate(double[] values, StatisticOptions options) {
        try {
            init();
            if (values == null || options == null) {
                return;
            }
            this.options = options;
            doubles = values;
            calculateBase();
            calculateVariance();
            calculatePercentile();
            calculateSkewness();
            calculateMode();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    private void calculateBase() {
        try {
            if (doubles == null || options == null) {
                return;
            }
            sum = 0;
            count = doubles.length;
            if (count == 0) {
                return;
            }
            for (int i = 0; i < count; ++i) {
                double v = doubles[i];
                sum += v;
                if (options.isMaximum() && v > maximum) {
                    maximum = v;
                }
                if (options.isMinimum() && v < minimum) {
                    minimum = v;
                }
                if (options.isGeometricMean()) {
                    geometricMean = geometricMean * v;
                }
                if (options.isSumSquares()) {
                    sumSquares += v * v;
                }
            }
            mean = sum / count;
            if (options.isGeometricMean()) {
                geometricMean = Math.pow(geometricMean, 1d / count);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    private void calculateVariance() {
        try {
            if (doubles == null || options == null || count <= 0) {
                return;
            }
            if (!options.needVariance()) {
                return;
            }
            vTmp = 0;
            for (int i = 0; i < count; ++i) {
                double p = doubles[i] - mean;
                double p2 = p * p;
                vTmp += p2;
            }
            populationVariance = vTmp / count;
            sampleVariance = vTmp / (count - 1);
            if (options.populationStandardDeviation) {
                populationStandardDeviation = Math.sqrt(populationVariance);
            }
            if (options.isSampleStandardDeviation()) {
                sampleStandardDeviation = Math.sqrt(sampleVariance);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    private void calculatePercentile() {
        try {
            if (doubles == null || options == null || count <= 0) {
                return;
            }
            if (!options.needPercentile()) {
                return;
            }
            Percentile percentile = new Percentile();
            percentile.setData(doubles);
            if (options.isMedian()) {
                median = percentile.evaluate(50);
            }
            if (options.isUpperQuartile()) {
                upperQuartile = percentile.evaluate(25);
            }
            if (options.isLowerQuartile()) {
                lowerQuartile = percentile.evaluate(75);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    private void calculateSkewness() {
        try {
            if (doubles == null || options == null || count <= 0) {
                return;
            }
            if (!options.isSkewness()) {
                return;
            }
            skewness = new Skewness().evaluate(doubles);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    private void calculateMode() {
        try {
            if (options == null || count <= 0) {
                return;
            }
            if (!options.isMode()) {
                return;
            }
            if (strings != null) {
                mode = modeObject(strings);
            } else if (doubles != null) {
                mode = mode(doubles);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public List<String> toStringList() {
        int scale = options.getScale();
        List<String> list = new ArrayList<>();
        if (options.isCount()) {
            list.add(StringTools.format(count));
        }
        if (options.isSum()) {
            list.add(DoubleTools.format(sum, scale));
        }
        if (options.isMean()) {
            list.add(DoubleTools.format(mean, scale));
        }
        if (options.isMaximum()) {
            list.add(DoubleTools.format(maximum, scale));
        }
        if (options.isMinimum()) {
            list.add(DoubleTools.format(minimum, scale));
        }
        if (options.isGeometricMean()) {
            list.add(DoubleTools.format(geometricMean, scale));
        }
        if (options.isSumSquares()) {
            list.add(DoubleTools.format(sumSquares, scale));
        }
        if (options.isPopulationStandardDeviation()) {
            list.add(DoubleTools.format(populationVariance, scale));
        }
        if (options.isSampleVariance()) {
            list.add(DoubleTools.format(sampleVariance, scale));
        }
        if (options.isPopulationStandardDeviation()) {
            list.add(DoubleTools.format(populationStandardDeviation, scale));
        }
        if (options.isSampleStandardDeviation()) {
            list.add(DoubleTools.format(sampleStandardDeviation, scale));
        }
        if (options.isSkewness()) {
            list.add(DoubleTools.format(skewness, scale));
        }
        if (options.isMedian()) {
            list.add(DoubleTools.format(median, scale));
        }
        if (options.isUpperQuartile()) {
            list.add(DoubleTools.format(upperQuartile, scale));
        }
        if (options.isLowerQuartile()) {
            list.add(DoubleTools.format(lowerQuartile, scale));
        }
        if (options.isMode()) {
            try {
                list.add(DoubleTools.format((double) mode, scale));
            } catch (Exception e) {
                list.add(mode.toString());
            }
        }
        return list;
    }

    /*
        static methods
     */
    public static double[] toDouble(String[] strings) {
        if (strings == null) {
            return null;
        }
        double[] doubles = new double[strings.length];
        for (int i = 0; i < strings.length; i++) {
            String s = strings[i];
            try {
                doubles[i] = Double.parseDouble(s.replaceAll(",", ""));
            } catch (Exception e) {
                doubles[i] = 0;
            }
        }
        return doubles;
    }

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
            Object[] objects = new Object[values.length];
            for (int i = 0; i < values.length; i++) {
                objects[i] = values[i];
            }
            Object mode = modeObject(objects);
            return mode != null ? (double) mode : AppValues.InvalidDouble;
        } catch (Exception e) {
            return AppValues.InvalidDouble;
        }
    }

    public static Object modeObject(Object[] values) {
        Object mode = null;
        try {
            if (values == null || values.length == 0) {
                return mode;
            }
            Map<Object, Integer> number = new HashMap<>();
            for (Object value : values) {
                if (number.containsKey(value)) {
                    number.put(value, number.get(value) + 1);
                } else {
                    number.put(value, 1);
                }
            }
            double num = 0;
            for (Object value : number.keySet()) {
                if (num < number.get(value)) {
                    mode = value;
                }
            }
        } catch (Exception e) {
        }
        return mode;
    }

    public static double median(double[] values) {
        try {
            if (values == null) {
                return AppValues.InvalidDouble;
            }
            int len = values.length;
            if (len == 0) {
                return AppValues.InvalidDouble;
            } else if (len == 1) {
                return values[0];
            }
            double[] sorted = DoubleArrayTools.sortArray(values);
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

    public double getPopulationVariance() {
        return populationVariance;
    }

    public void setPopulationVariance(double populationVariance) {
        this.populationVariance = populationVariance;
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

    public Object getMode() {
        return mode;
    }

    public void setMode(Object mode) {
        this.mode = mode;
    }

    public double getMedian() {
        return median;
    }

    public void setMedian(double median) {
        this.median = median;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public double getGeometricMean() {
        return geometricMean;
    }

    public void setGeometricMean(double geometricMean) {
        this.geometricMean = geometricMean;
    }

    public double getSumSquares() {
        return sumSquares;
    }

    public void setSumSquares(double sumSquares) {
        this.sumSquares = sumSquares;
    }

    public double getSampleVariance() {
        return sampleVariance;
    }

    public void setSampleVariance(double sampleVariance) {
        this.sampleVariance = sampleVariance;
    }

    public double getPopulationStandardDeviation() {
        return populationStandardDeviation;
    }

    public void setPopulationStandardDeviation(double populationStandardDeviation) {
        this.populationStandardDeviation = populationStandardDeviation;
    }

    public double getSampleStandardDeviation() {
        return sampleStandardDeviation;
    }

    public void setSampleStandardDeviation(double sampleStandardDeviation) {
        this.sampleStandardDeviation = sampleStandardDeviation;
    }

    public double getUpperQuartile() {
        return upperQuartile;
    }

    public void setUpperQuartile(double upperQuartile) {
        this.upperQuartile = upperQuartile;
    }

    public double getLowerQuartile() {
        return lowerQuartile;
    }

    public void setLowerQuartile(double lowerQuartile) {
        this.lowerQuartile = lowerQuartile;
    }

    public double getvTmp() {
        return vTmp;
    }

    public void setvTmp(double vTmp) {
        this.vTmp = vTmp;
    }

    public StatisticOptions getOptions() {
        return options;
    }

    public DoubleStatistic setOptions(StatisticOptions options) {
        this.options = options;
        return this;
    }

    public double[] getDoubles() {
        return doubles;
    }

    public DoubleStatistic setValues(double[] values) {
        this.doubles = values;
        return this;
    }

}
