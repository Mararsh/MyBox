package mara.mybox.calculation;

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
    public long count, invalidCount;
    public double invalidAs, sum, mean, geometricMean, sumSquares,
            populationVariance, sampleVariance, populationStandardDeviation, sampleStandardDeviation, skewness,
            minimum, maximum, median, upperQuartile, lowerQuartile,
            upperMildOutlierLine, upperExtremeOutlierLine, lowerMildOutlierLine, lowerExtremeOutlierLine,
            dTmp;
    public Object modeValue, minimumValue, maximumValue, medianValue, upperQuartileValue, lowerQuartileValue;
    public DescriptiveStatistic options;
    private double[] doubles;

    public DoubleStatistic() {
        init();
    }

    public DoubleStatistic(double[] values) {
        initOptions(null);
        calculate(values);
    }

    public DoubleStatistic(double[] values, DescriptiveStatistic inOptions) {
        initOptions(inOptions);
        calculate(values);
    }

    public DoubleStatistic(String[] values, DescriptiveStatistic inOptions) {
        initOptions(inOptions);
        if (options.isMode()) {
            modeValue = modeObject(values);
        }
        calculate(toDoubleArray(values));
    }

    private void init() {
        count = 0;
        sum = 0;
        mean = 0;
        geometricMean = 1;
        sumSquares = 0;
        populationVariance = Double.NaN;
        sampleVariance = Double.NaN;
        populationStandardDeviation = Double.NaN;
        sampleStandardDeviation = Double.NaN;
        skewness = Double.NaN;
        maximum = -Double.MAX_VALUE;
        minimum = Double.MAX_VALUE;
        median = Double.NaN;
        upperQuartile = Double.NaN;
        lowerQuartile = Double.NaN;
        upperMildOutlierLine = Double.NaN;
        upperExtremeOutlierLine = Double.NaN;
        lowerMildOutlierLine = Double.NaN;
        lowerExtremeOutlierLine = Double.NaN;
        modeValue = null;
        dTmp = 0;
        invalidAs = 0;
        invalidCount = 0;
        options = null;
        doubles = null;
    }

    private void initOptions(DescriptiveStatistic inOptions) {
        init();
        if (inOptions == null) {
            options = DescriptiveStatistic.all(true);
        } else {
            options = inOptions;
        }
        invalidAs = options.invalidAs;
    }

    private void calculate(double[] values) {
        try {
            if (values == null) {
                return;
            }
            doubles = values;
            calculateBase();
            calculateVariance();
            calculatePercentile();
            calculateSkewness();
            if (modeValue == null) {
                calculateMode();
            }
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
            count = 0;
            invalidCount = 0;
            double total = doubles.length;
            if (total == 0) {
                return;
            }
            for (int i = 0; i < total; ++i) {
                double v = doubles[i];
                if (Double.isNaN(v)) {
                    invalidCount++;
                    continue;
                }
                count++;
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
            if (count == 0) {
                mean = Double.NaN;
                sum = Double.NaN;
                geometricMean = Double.NaN;
                sumSquares = Double.NaN;
            } else {
                mean = sum / count;
                if (options.isGeometricMean()) {
                    geometricMean = Math.pow(geometricMean, 1d / count);
                }
                if (invalidCount > 0) {
                    double[] valid = new double[(int) count];
                    int index = 0;
                    for (int i = 0; i < doubles.length; i++) {
                        double v = doubles[i];
                        if (!Double.isNaN(v)) {
                            valid[index++] = v;
                        }
                    }
                    doubles = valid;
                }
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
            dTmp = 0;
            for (int i = 0; i < count; ++i) {
                double p = doubles[i] - mean;
                double p2 = p * p;
                dTmp += p2;
            }
            populationVariance = dTmp / count;
            sampleVariance = count < 2 ? Double.NaN : dTmp / (count - 1);
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
                medianValue = median;
            }
            boolean needOutlier = options.needOutlier();
            if (options.isUpperQuartile() || needOutlier) {
                upperQuartile = percentile.evaluate(75);
                upperQuartileValue = upperQuartile;
            }
            if (options.isLowerQuartile() || needOutlier) {
                lowerQuartile = percentile.evaluate(25);
                lowerQuartileValue = lowerQuartile;
            }
            if (needOutlier) {
                double qi = upperQuartile - lowerQuartile;
                if (options.isUpperExtremeOutlierLine()) {
                    upperExtremeOutlierLine = upperQuartile + 3 * qi;
                }
                if (options.isUpperMildOutlierLine()) {
                    upperMildOutlierLine = upperQuartile + 1.5 * qi;
                }
                if (options.isLowerExtremeOutlierLine()) {
                    lowerExtremeOutlierLine = lowerQuartile - 3 * qi;
                }
                if (options.isLowerMildOutlierLine()) {
                    lowerMildOutlierLine = lowerQuartile - 1.5 * qi;
                }
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
            modeValue = mode(doubles);
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
        if (options.isUpperExtremeOutlierLine()) {
            list.add(DoubleTools.format(upperExtremeOutlierLine, scale));
        }
        if (options.isUpperMildOutlierLine()) {
            list.add(DoubleTools.format(upperMildOutlierLine, scale));
        }
        if (options.isLowerExtremeOutlierLine()) {
            list.add(DoubleTools.format(lowerMildOutlierLine, scale));
        }
        if (options.isLowerMildOutlierLine()) {
            list.add(DoubleTools.format(lowerExtremeOutlierLine, scale));
        }
        if (options.isMode()) {
            try {
                list.add(DoubleTools.format((double) modeValue, scale));
            } catch (Exception e) {
                list.add(modeValue.toString());
            }
        }
        return list;
    }

    public double toDouble(String string) {
        try {
            return Double.valueOf(string.replaceAll(",", ""));
        } catch (Exception e) {
            return invalidAs;
        }
    }

    public final double[] toDoubleArray(String[] strings) {
        if (strings == null) {
            return null;
        }
        doubles = new double[strings.length];
        for (int i = 0; i < strings.length; i++) {
            doubles[i] = toDouble(strings[i]);
        }
        return doubles;
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
                return Double.NaN;
            }
            Object[] objects = new Object[values.length];
            for (int i = 0; i < values.length; i++) {
                objects[i] = values[i];
            }
            Object mode = modeObject(objects);
            return mode != null ? (double) mode : Double.NaN;
        } catch (Exception e) {
            return Double.NaN;
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

    public Object getModeValue() {
        return modeValue;
    }

    public void setModeValue(Object modeValue) {
        this.modeValue = modeValue;
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

    public double getUpperMildOutlierLine() {
        return upperMildOutlierLine;
    }

    public void setUpperMildOutlierLine(double upperMildOutlierLine) {
        this.upperMildOutlierLine = upperMildOutlierLine;
    }

    public double getUpperExtremeOutlierLine() {
        return upperExtremeOutlierLine;
    }

    public void setUpperExtremeOutlierLine(double upperExtremeOutlierLine) {
        this.upperExtremeOutlierLine = upperExtremeOutlierLine;
    }

    public double getLowerMildOutlierLine() {
        return lowerMildOutlierLine;
    }

    public void setLowerMildOutlierLine(double lowerMildOutlierLine) {
        this.lowerMildOutlierLine = lowerMildOutlierLine;
    }

    public double getLowerExtremeOutlierLine() {
        return lowerExtremeOutlierLine;
    }

    public void setLowerExtremeOutlierLine(double lowerExtremeOutlierLine) {
        this.lowerExtremeOutlierLine = lowerExtremeOutlierLine;
    }

    public Object getMinimumValue() {
        return minimumValue;
    }

    public void setMinimumValue(Object minimumValue) {
        this.minimumValue = minimumValue;
    }

    public Object getMaximumValue() {
        return maximumValue;
    }

    public void setMaximumValue(Object maximumValue) {
        this.maximumValue = maximumValue;
    }

    public Object getMedianValue() {
        return medianValue;
    }

    public void setMedianValue(Object medianValue) {
        this.medianValue = medianValue;
    }

    public Object getUpperQuartileValue() {
        return upperQuartileValue;
    }

    public void setUpperQuartileValue(Object upperQuartileValue) {
        this.upperQuartileValue = upperQuartileValue;
    }

    public Object getLowerQuartileValue() {
        return lowerQuartileValue;
    }

    public void setLowerQuartileValue(Object lowerQuartileValue) {
        this.lowerQuartileValue = lowerQuartileValue;
    }

    public double getdTmp() {
        return dTmp;
    }

    public void setdTmp(double dTmp) {
        this.dTmp = dTmp;
    }

    public DescriptiveStatistic getOptions() {
        return options;
    }

    public DoubleStatistic setOptions(DescriptiveStatistic options) {
        this.options = options;
        return this;
    }

    public DoubleStatistic setValues(double[] values) {
        this.doubles = values;
        return this;
    }

    public long getInvalidCount() {
        return invalidCount;
    }

    public DoubleStatistic setInvalidCount(long invalidCount) {
        this.invalidCount = invalidCount;
        return this;
    }

    public double getInvalidAs() {
        return invalidAs;
    }

    public DoubleStatistic setInvalidAs(double invalidAs) {
        this.invalidAs = invalidAs;
        return this;
    }

}
