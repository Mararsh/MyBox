package mara.mybox.calculation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mara.mybox.calculation.DescriptiveStatistic.StatisticType;
import mara.mybox.db.data.ColumnDefinition.InvalidAs;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DoubleArrayTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.NumberTools;
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
    public double sum, mean, geometricMean, sumSquares,
            populationVariance, sampleVariance, populationStandardDeviation, sampleStandardDeviation, skewness,
            minimum, maximum, median, upperQuartile, lowerQuartile, mode,
            upperMildOutlierLine, upperExtremeOutlierLine, lowerMildOutlierLine, lowerExtremeOutlierLine,
            dTmp;
    public Object modeValue, minimumValue, maximumValue, medianValue, upperQuartileValue, lowerQuartileValue;
    public DescriptiveStatistic options;
    public InvalidAs invalidAs;

    private double[] doubles, valids;

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
        calculate(values, inOptions);
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
        mode = Double.NaN;
        upperQuartile = Double.NaN;
        lowerQuartile = Double.NaN;
        upperMildOutlierLine = Double.NaN;
        upperExtremeOutlierLine = Double.NaN;
        lowerMildOutlierLine = Double.NaN;
        lowerExtremeOutlierLine = Double.NaN;
        modeValue = null;
        dTmp = 0;
        invalidAs = InvalidAs.Zero;
        invalidCount = 0;
        options = null;
        doubles = null;
        valids = null;
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

    public final void calculate(String[] values, DescriptiveStatistic inOptions) {
        initOptions(inOptions);
        if (options.include(StatisticType.Mode)) {
            modeValue = modeObject(values);
            try {
                mode = Double.parseDouble(modeValue + "");
            } catch (Exception e) {
                mode = Double.NaN;
            }
        }
        calculate(toDoubleArray(values));
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
            List<Double> validList = new ArrayList<>();
            for (int i = 0; i < total; ++i) {
                double v = doubles[i];
                if (Double.isNaN(v)) {
                    switch (invalidAs) {
                        case Empty:
                        case Skip:
                            invalidCount++;
                            continue;
                        case Zero:
                            v = 0;
                            break;
                    }
                }
                count++;
                validList.add(v);
                sum += v;
                if (options.include(StatisticType.MaximumQ4) && v > maximum) {
                    maximum = v;
                }
                if (options.include(StatisticType.MinimumQ0) && v < minimum) {
                    minimum = v;
                }
                if (options.include(StatisticType.GeometricMean)) {
                    geometricMean = geometricMean * v;
                }
                if (options.include(StatisticType.SumOfSquares)) {
                    sumSquares += v * v;
                }
            }
            if (count == 0) {
                mean = Double.NaN;
                sum = Double.NaN;
                geometricMean = Double.NaN;
                sumSquares = Double.NaN;
                valids = null;
            } else {
                mean = sum / count;
                if (options.include(StatisticType.GeometricMean)) {
                    geometricMean = Math.pow(geometricMean, 1d / count);
                }
                valids = new double[validList.size()];
                int index = 0;
                for (double d : validList) {
                    valids[index++] = d;
                }
            }
            if (maximum == -Double.MAX_VALUE) {
                maximum = Double.NaN;
            }
            if (minimum == Double.MAX_VALUE) {
                minimum = Double.NaN;
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    private void calculateVariance() {
        try {
            if (valids == null || count <= 0 || options == null || count <= 0) {
                return;
            }
            if (!options.needVariance()) {
                return;
            }
            dTmp = 0;
            for (int i = 0; i < count; ++i) {
                double v = valids[i];
                double p = v - mean;
                double p2 = p * p;
                dTmp += p2;
            }
            populationVariance = dTmp / count;
            sampleVariance = count < 2 ? Double.NaN : dTmp / (count - 1);
            if (options.include(StatisticType.PopulationStandardDeviation)) {
                populationStandardDeviation = Math.sqrt(populationVariance);
            }
            if (options.include(StatisticType.SampleStandardDeviation)) {
                sampleStandardDeviation = Math.sqrt(sampleVariance);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    private void calculatePercentile() {
        try {
            if (valids == null || count <= 0 || options == null) {
                return;
            }
            if (!options.needPercentile()) {
                return;
            }
            Percentile percentile = new Percentile();

            percentile.setData(valids);
            if (options.include(StatisticType.Median)) {
                median = percentile.evaluate(50);
                medianValue = median;
            }
            boolean needOutlier = options.needOutlier();
            if (options.include(StatisticType.UpperQuartile) || needOutlier) {
                upperQuartile = percentile.evaluate(75);
                upperQuartileValue = upperQuartile;
            }
            if (options.include(StatisticType.LowerQuartile) || needOutlier) {
                lowerQuartile = percentile.evaluate(25);
                lowerQuartileValue = lowerQuartile;
            }
            if (needOutlier) {
                double qi = upperQuartile - lowerQuartile;
                if (options.include(StatisticType.UpperExtremeOutlierLine)) {
                    upperExtremeOutlierLine = upperQuartile + 3 * qi;
                }
                if (options.include(StatisticType.UpperMildOutlierLine)) {
                    upperMildOutlierLine = upperQuartile + 1.5 * qi;
                }
                if (options.include(StatisticType.LowerExtremeOutlierLine)) {
                    lowerExtremeOutlierLine = lowerQuartile - 3 * qi;
                }
                if (options.include(StatisticType.LowerMildOutlierLine)) {
                    lowerMildOutlierLine = lowerQuartile - 1.5 * qi;
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    private void calculateSkewness() {
        try {
            if (valids == null || count <= 0 || options == null || count <= 0) {
                return;
            }
            if (!options.include(StatisticType.Skewness)) {
                return;
            }
            skewness = new Skewness().evaluate(valids);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    private void calculateMode() {
        try {
            if (options == null || valids == null || count <= 0) {
                return;
            }
            if (!options.include(StatisticType.Mode)) {
                return;
            }
            mode = mode(valids);
            modeValue = mode;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public List<String> toStringList() {
        if (options == null) {
            return null;
        }
        int scale = options.getScale();
        List<String> list = new ArrayList<>();
        for (StatisticType type : options.types) {
            switch (type) {
                case Count:
                    list.add(StringTools.format(count));
                    break;
                case Sum:
                    list.add(NumberTools.format(sum, scale));
                    break;
                case Mean:
                    list.add(NumberTools.format(mean, scale));
                    break;
                case MaximumQ4:
                    list.add(NumberTools.format(maximum, scale));
                    break;
                case MinimumQ0:
                    list.add(NumberTools.format(minimum, scale));
                    break;
                case GeometricMean:
                    list.add(NumberTools.format(geometricMean, scale));
                    break;
                case SumOfSquares:
                    list.add(NumberTools.format(sumSquares, scale));
                    break;
                case PopulationVariance:
                    list.add(NumberTools.format(populationVariance, scale));
                    break;
                case SampleVariance:
                    list.add(NumberTools.format(sampleVariance, scale));
                    break;
                case PopulationStandardDeviation:
                    list.add(NumberTools.format(populationStandardDeviation, scale));
                    break;
                case SampleStandardDeviation:
                    list.add(NumberTools.format(sampleStandardDeviation, scale));
                    break;
                case Skewness:
                    list.add(NumberTools.format(skewness, scale));
                    break;
                case Median:
                    list.add(NumberTools.format(median, scale));
                    break;
                case UpperQuartile:
                    list.add(NumberTools.format(upperQuartile, scale));
                    break;
                case LowerQuartile:
                    list.add(NumberTools.format(lowerQuartile, scale));
                    break;
                case UpperExtremeOutlierLine:
                    list.add(NumberTools.format(upperExtremeOutlierLine, scale));
                    break;
                case UpperMildOutlierLine:
                    list.add(NumberTools.format(upperMildOutlierLine, scale));
                    break;
                case LowerMildOutlierLine:
                    list.add(NumberTools.format(lowerMildOutlierLine, scale));
                    break;
                case LowerExtremeOutlierLine:
                    list.add(NumberTools.format(lowerExtremeOutlierLine, scale));
                    break;
                case Mode:
                    if (modeValue == null) {
                        list.add(null);
                    } else {
                        try {
                            list.add(NumberTools.format((double) modeValue, scale));
                        } catch (Exception e) {
                            list.add(modeValue.toString());
                        }
                    }
                    break;
            }
        }
        return list;
    }

    public final double[] toDoubleArray(String[] strings) {
        if (strings == null) {
            return null;
        }
        doubles = new double[strings.length];
        for (int i = 0; i < strings.length; i++) {
            doubles[i] = DoubleTools.toDouble(strings[i], invalidAs);
        }
        return doubles;
    }

    public double value(StatisticType type) {
        if (type == null) {
            return Double.NaN;
        }
        switch (type) {
            case Count:
                return count;

            case Sum:
                return sum;

            case Mean:
                return mean;

            case MaximumQ4:
                return maximum;

            case MinimumQ0:
                return minimum;

            case GeometricMean:
                return geometricMean;

            case SumOfSquares:
                return sumSquares;

            case PopulationVariance:
                return populationVariance;

            case SampleVariance:
                return sampleVariance;

            case PopulationStandardDeviation:
                return populationStandardDeviation;

            case SampleStandardDeviation:
                return sampleStandardDeviation;

            case Skewness:
                return skewness;

            case Median:
                return median;

            case UpperQuartile:
                return upperQuartile;

            case LowerQuartile:
                return lowerQuartile;

            case UpperExtremeOutlierLine:
                return upperExtremeOutlierLine;

            case UpperMildOutlierLine:
                return upperMildOutlierLine;

            case LowerMildOutlierLine:
                return lowerMildOutlierLine;

            case LowerExtremeOutlierLine:
                return lowerExtremeOutlierLine;

            case Mode:
                return mode;

        }
        return Double.NaN;
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

    public double getMode() {
        return mode;
    }

    public void setMode(double mode) {
        this.mode = mode;
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

    public InvalidAs getInvalidAs() {
        return invalidAs;
    }

    public DoubleStatistic setInvalidAs(InvalidAs invalidAs) {
        this.invalidAs = invalidAs;
        return this;
    }

}
