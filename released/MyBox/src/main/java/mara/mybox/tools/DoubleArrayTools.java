package mara.mybox.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @Author Mara
 * @CreateDate 2018-6-11 17:43:53
 * @Description
 * @License Apache License Version 2.0
 */
public class DoubleArrayTools {

    public static double[] sortArray(double[] numbers) {
        List<Double> list = new ArrayList<>();
        for (double i : numbers) {
            list.add(i);
        }
        Collections.sort(list, new Comparator<Double>() {
            @Override
            public int compare(Double p1, Double p2) {
                if (p1 > p2) {
                    return 1;
                } else if (p1 < p2) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        double[] sorted = new double[numbers.length];
        for (int i = 0; i < list.size(); ++i) {
            sorted[i] = list.get(i);
        }
        return sorted;
    }

    public static double[] array(double x, double y, double z) {
        double[] xyz = new double[3];
        xyz[0] = x;
        xyz[1] = y;
        xyz[2] = z;
        return xyz;
    }

    public static double[] scale(double[] data, int scale) {
        try {
            if (data == null) {
                return null;
            }
            double[] result = new double[data.length];
            for (int i = 0; i < data.length; ++i) {
                result[i] = DoubleTools.scale(data[i], scale);
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public static double[] normalizeMinMax(double[] array) {
        try {
            if (array == null) {
                return null;
            }
            double[] result = new double[array.length];
            double min = Double.MAX_VALUE, max = -Double.MAX_VALUE;
            for (int i = 0; i < array.length; ++i) {
                double d = array[i];
                if (d > max) {
                    max = d;
                }
                if (d < min) {
                    min = d;
                }
            }
            if (min == max) {
                return normalizeSum(array);
            } else {
                double s = 1d / (max - min);
                for (int i = 0; i < array.length; ++i) {
                    result[i] = (array[i] - min) * s;
                }
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public static double[] normalizeSum(double[] array) {
        try {
            if (array == null) {
                return null;
            }
            double[] result = new double[array.length];
            double sum = 0;
            for (int i = 0; i < array.length; ++i) {
                sum += Math.abs(array[i]);
            }
            if (sum == 0) {
                return null;
            }
            double s = 1d / sum;
            for (int i = 0; i < array.length; ++i) {
                result[i] = array[i] * s;
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

}
