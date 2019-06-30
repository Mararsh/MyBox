package mara.mybox.tools;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
public class DoubleTools {

    /*
        https://stackoverflow.com/questions/322749/retain-precision-with-double-in-java
        "Do not waste your efford using BigDecimal. In 99.99999% cases you don't need it"
        "BigDecimal is much slower than double"
        "The solution depends on what exactly your problem is:
            - If it's that you just don't like seeing all those noise digits, then fix your string formatting.
                Don't display more than 15 significant digits (or 7 for float).
            - If it's that the inexactness of your numbers is breaking things like "if" statements,
                then you should write if (abs(x - 7.3) < TOLERANCE) instead of if (x == 7.3).
            - If you're working with money, then what you probably really want is decimal fixed point.
                Store an integer number of cents or whatever the smallest unit of your currency is.
            - (VERY UNLIKELY) If you need more than 53 significant bits (15-16 significant digits) of precision,
                then use a high-precision floating-point type, like BigDecimal."
     */
    public static double scale3(double invalue) {
        return DoubleTools.scale(invalue, 3);
    }

    public static double scale2(double invalue) {
        return DoubleTools.scale(invalue, 2);
    }

    public static double scale6(double invalue) {
        return DoubleTools.scale(invalue, 6);
    }

    public static double scale(double v, int scale) {
        BigDecimal b = new BigDecimal(Double.toString(v));
        return scale(b, scale);
    }

    public static double scale(BigDecimal b, int scale) {
        return b.setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

    public static double[] scale(double[] data, int scale) {
        try {
            if (data == null) {
                return null;
            }
            double[] result = new double[data.length];
            for (int i = 0; i < data.length; i++) {
                result[i] = scale(data[i], scale);
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public static void sortList(List<Integer> numbers) {
        Collections.sort(numbers, new Comparator<Integer>() {
            @Override
            public int compare(Integer p1, Integer p2) {
                return p1 - p2;
            }
        });
    }

    public static double[] sortArray(double[] numbers) {
        List<Double> list = new ArrayList<>();
        for (double i : numbers) {
            list.add(i);
        }
        Collections.sort(list, new Comparator<Double>() {
            @Override
            public int compare(Double p1, Double p2) {
                return (int) (p1 - p2);
            }
        });
        double[] sorted = new double[numbers.length];
        for (int i = 0; i < list.size(); i++) {
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

}
