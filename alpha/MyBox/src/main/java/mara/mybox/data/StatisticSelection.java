package mara.mybox.data;

/**
 * @Author Mara
 * @CreateDate 2022-4-14
 * @License Apache License Version 2.0
 */
public class StatisticSelection {

    public boolean count, sum, mean, geometricMean, sumSquares,
            populationVariance, sampleVariance, populationStandardDeviation, sampleStandardDeviation, skewness,
            minimum, maximum, median, upperQuartile, lowerQuartile, mode;

    public static StatisticSelection all(boolean select) {
        return new StatisticSelection()
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

    public StatisticSelection setCount(boolean count) {
        this.count = count;
        return this;
    }

    public boolean isSum() {
        return sum;
    }

    public StatisticSelection setSum(boolean sum) {
        this.sum = sum;
        return this;
    }

    public boolean isMean() {
        return mean;
    }

    public StatisticSelection setMean(boolean mean) {
        this.mean = mean;
        return this;
    }

    public boolean isGeometricMean() {
        return geometricMean;
    }

    public StatisticSelection setGeometricMean(boolean geometricMean) {
        this.geometricMean = geometricMean;
        return this;
    }

    public boolean isMinimum() {
        return minimum;
    }

    public StatisticSelection setMinimum(boolean minimum) {
        this.minimum = minimum;
        return this;
    }

    public boolean isMaximum() {
        return maximum;
    }

    public StatisticSelection setMaximum(boolean maximum) {
        this.maximum = maximum;
        return this;
    }

    public boolean isSumSquares() {
        return sumSquares;
    }

    public StatisticSelection setSumSquares(boolean sumSquares) {
        this.sumSquares = sumSquares;
        return this;
    }

    public boolean isPopulationVariance() {
        return populationVariance;
    }

    public StatisticSelection setPopulationVariance(boolean populationVariance) {
        this.populationVariance = populationVariance;
        return this;
    }

    public boolean isSampleVariance() {
        return sampleVariance;
    }

    public StatisticSelection setSampleVariance(boolean sampleVariance) {
        this.sampleVariance = sampleVariance;
        return this;
    }

    public boolean isPopulationStandardDeviation() {
        return populationStandardDeviation;
    }

    public StatisticSelection setPopulationStandardDeviation(boolean populationStandardDeviation) {
        this.populationStandardDeviation = populationStandardDeviation;
        return this;
    }

    public boolean isSampleStandardDeviation() {
        return sampleStandardDeviation;
    }

    public StatisticSelection setSampleStandardDeviation(boolean sampleStandardDeviation) {
        this.sampleStandardDeviation = sampleStandardDeviation;
        return this;
    }

    public boolean isSkewness() {
        return skewness;
    }

    public StatisticSelection setSkewness(boolean skewness) {
        this.skewness = skewness;
        return this;
    }

    public boolean isMode() {
        return mode;
    }

    public StatisticSelection setMode(boolean mode) {
        this.mode = mode;
        return this;
    }

    public boolean isMedian() {
        return median;
    }

    public StatisticSelection setMedian(boolean median) {
        this.median = median;
        return this;
    }

    public boolean isUpperQuartile() {
        return upperQuartile;
    }

    public StatisticSelection setUpperQuartile(boolean upperQuartile) {
        this.upperQuartile = upperQuartile;
        return this;
    }

    public boolean isLowerQuartile() {
        return lowerQuartile;
    }

    public StatisticSelection setLowerQuartile(boolean lowerQuartile) {
        this.lowerQuartile = lowerQuartile;
        return this;
    }

}
