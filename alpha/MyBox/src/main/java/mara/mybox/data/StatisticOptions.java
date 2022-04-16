package mara.mybox.data;

/**
 * @Author Mara
 * @CreateDate 2022-4-14
 * @License Apache License Version 2.0
 */
public class StatisticOptions {

    public boolean count, sum, mean, geometricMean, sumSquares,
            populationVariance, sampleVariance, populationStandardDeviation, sampleStandardDeviation, skewness,
            minimum, maximum, median, upperQuartile, lowerQuartile, mode;
    public int scale;

    public StatisticObject statisticObject = StatisticObject.Columns;

    public enum StatisticObject {
        Columns, Rows, All
    }

    public static StatisticOptions all(boolean select) {
        return new StatisticOptions()
                .setCount(select)
                .setSum(select)
                .setMean(select)
                .setGeometricMean(select)
                .setSumSquares(select)
                .setPopulationStandardDeviation(select)
                .setPopulationVariance(select)
                .setSampleStandardDeviation(select)
                .setSampleVariance(select)
                .setSkewness(select)
                .setMaximum(select)
                .setMinimum(select)
                .setMedian(select)
                .setUpperQuartile(select)
                .setLowerQuartile(select)
                .setMode(select);
    }

    public boolean needVariance() {
        return populationVariance || sampleVariance || populationStandardDeviation || sampleStandardDeviation;
    }

    public boolean needPercentile() {
        return median || upperQuartile || lowerQuartile;
    }

    public boolean needStored() {
        return needPercentile() || mode;
    }

    /*
        get/set
     */
    public boolean isCount() {
        return count;
    }

    public StatisticOptions setCount(boolean count) {
        this.count = count;
        return this;
    }

    public boolean isSum() {
        return sum;
    }

    public StatisticOptions setSum(boolean sum) {
        this.sum = sum;
        return this;
    }

    public boolean isMean() {
        return mean;
    }

    public StatisticOptions setMean(boolean mean) {
        this.mean = mean;
        return this;
    }

    public boolean isGeometricMean() {
        return geometricMean;
    }

    public StatisticOptions setGeometricMean(boolean geometricMean) {
        this.geometricMean = geometricMean;
        return this;
    }

    public boolean isMinimum() {
        return minimum;
    }

    public StatisticOptions setMinimum(boolean minimum) {
        this.minimum = minimum;
        return this;
    }

    public boolean isMaximum() {
        return maximum;
    }

    public StatisticOptions setMaximum(boolean maximum) {
        this.maximum = maximum;
        return this;
    }

    public boolean isSumSquares() {
        return sumSquares;
    }

    public StatisticOptions setSumSquares(boolean sumSquares) {
        this.sumSquares = sumSquares;
        return this;
    }

    public boolean isPopulationVariance() {
        return populationVariance;
    }

    public StatisticOptions setPopulationVariance(boolean populationVariance) {
        this.populationVariance = populationVariance;
        return this;
    }

    public boolean isSampleVariance() {
        return sampleVariance;
    }

    public StatisticOptions setSampleVariance(boolean sampleVariance) {
        this.sampleVariance = sampleVariance;
        return this;
    }

    public boolean isPopulationStandardDeviation() {
        return populationStandardDeviation;
    }

    public StatisticOptions setPopulationStandardDeviation(boolean populationStandardDeviation) {
        this.populationStandardDeviation = populationStandardDeviation;
        return this;
    }

    public boolean isSampleStandardDeviation() {
        return sampleStandardDeviation;
    }

    public StatisticOptions setSampleStandardDeviation(boolean sampleStandardDeviation) {
        this.sampleStandardDeviation = sampleStandardDeviation;
        return this;
    }

    public boolean isSkewness() {
        return skewness;
    }

    public StatisticOptions setSkewness(boolean skewness) {
        this.skewness = skewness;
        return this;
    }

    public boolean isMode() {
        return mode;
    }

    public StatisticOptions setMode(boolean mode) {
        this.mode = mode;
        return this;
    }

    public boolean isMedian() {
        return median;
    }

    public StatisticOptions setMedian(boolean median) {
        this.median = median;
        return this;
    }

    public boolean isUpperQuartile() {
        return upperQuartile;
    }

    public StatisticOptions setUpperQuartile(boolean upperQuartile) {
        this.upperQuartile = upperQuartile;
        return this;
    }

    public boolean isLowerQuartile() {
        return lowerQuartile;
    }

    public StatisticOptions setLowerQuartile(boolean lowerQuartile) {
        this.lowerQuartile = lowerQuartile;
        return this;
    }

    public StatisticObject getStatisticObject() {
        return statisticObject;
    }

    public StatisticOptions setStatisticObject(StatisticObject statisticObject) {
        this.statisticObject = statisticObject;
        return this;
    }

    public int getScale() {
        return scale;
    }

    public StatisticOptions setScale(int scale) {
        this.scale = scale;
        return this;
    }

}
