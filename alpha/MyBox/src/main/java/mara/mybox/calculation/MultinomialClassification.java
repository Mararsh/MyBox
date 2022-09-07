package mara.mybox.calculation;

/**
 * @Author Mara
 * @CreateDate 2022-9-7
 * @License Apache License Version 2.0
 */
public class MultinomialClassification {

    // https://www.imooc.com/article/18088
    public static double[] softMax(double[][] theta, double[] data) {
        if (theta == null || data == null) {
            return null;
        }
        int len = data.length;
        if (len != theta.length) {
            return null;
        }
        double[] result = new double[len];
        double[] v = new double[len];
        double sum = 0d;
        for (int i = 0; i < len; i++) {
            double d = 0d;
            for (int j = 0; j < len; j++) {
                d += theta[i][j] * data[j];
            }
            d = Math.pow(Math.E, d);
            v[i] = d;
            sum += d;
        }
        for (int i = 0; i < len; i++) {
            result[i] = v[i] / sum;
        }
        return result;
    }

}
