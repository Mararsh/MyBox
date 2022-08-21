package mara.mybox.tools;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Random;
import mara.mybox.data2d.Data2D_Attributes.InvalidAs;
import mara.mybox.value.AppValues;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2018-6-11 17:43:53
 * @License Apache License Version 2.0
 */
public class DoubleTools {

    public static boolean invalidDouble(double value) {
        return Double.isNaN(value) || value == AppValues.InvalidDouble;
    }

    public static double value(InvalidAs invalidAs) {
        if (invalidAs == InvalidAs.Zero) {
            return 0;
        } else {
            return Double.NaN;
        }
    }

    public static double toDouble(String string, InvalidAs invalidAs) {
        try {
            double d = Double.valueOf(string.replaceAll(",", ""));
            return invalidDouble(d) ? value(invalidAs) : d;
        } catch (Exception e) {
            return value(invalidAs);
        }
    }

    public static double[] toDouble(String[] sVector, InvalidAs invalidAs) {
        try {
            if (sVector == null) {
                return null;
            }
            int len = sVector.length;
            double[] doubleVector = new double[len];
            for (int i = 0; i < len; i++) {
                doubleVector[i] = DoubleTools.toDouble(sVector[i], invalidAs);
            }
            return doubleVector;
        } catch (Exception e) {
            return null;
        }
    }

    public static double[][] toDouble(String[][] sMatrix, InvalidAs invalidAs) {
        try {
            if (sMatrix == null) {
                return null;
            }
            int rsize = sMatrix.length, csize = sMatrix[0].length;
            double[][] doubleMatrix = new double[rsize][csize];
            for (int i = 0; i < rsize; i++) {
                for (int j = 0; j < csize; j++) {
                    doubleMatrix[i][j] = DoubleTools.toDouble(sMatrix[i][j], invalidAs);
                }
            }
            return doubleMatrix;
        } catch (Exception e) {
            return null;
        }
    }

    public static String percentage(double data, double total) {
        return percentage(data, total, 2);
    }

    public static String percentage(double data, double total, int scale) {
        try {
            if (total == 0) {
                return message("Invalid");
            }
            return scale(data * 100 / total, scale) + "";
        } catch (Exception e) {
            return data + "";
        }
    }

    public static String format(double data) {
        try {
            String format = "#,###";
            String s = data + "";
            int pos = s.indexOf(".");
            if (pos >= 0) {
                format += "." + "#".repeat(s.substring(pos + 1).length());
            }
            DecimalFormat df = new DecimalFormat(format);
            return df.format(data);
        } catch (Exception e) {
            return data + "";
        }
    }

    public static String format(double data, int scale) {
        try {
            String format = "#,###";
            if (scale > 0) {
                format += "." + "#".repeat(scale);
            }
            DecimalFormat df = new DecimalFormat(format);
            return df.format(scale(data, scale));
        } catch (Exception e) {
            return data + "";
        }
    }

    public static int compare(String s1, String s2, boolean desc) {
        double d1, d2;
        try {
            d1 = Double.valueOf(s1.replaceAll(",", ""));
        } catch (Exception e) {
            d1 = Double.NaN;
        }
        try {
            d2 = Double.valueOf(s2.replaceAll(",", ""));
        } catch (Exception e) {
            d2 = Double.NaN;
        }
        return compare(d1, d2, desc);
    }

    // invalid values are counted as smaller
    public static int compare(double d1, double d2, boolean desc) {
        if (Double.isNaN(d1)) {
            if (Double.isNaN(d2)) {
                return 0;
            } else {
                return desc ? 1 : -1;
            }
        } else {
            if (Double.isNaN(d2)) {
                return desc ? -1 : 1;
            } else {
                double diff = d1 - d2;
                if (diff == 0) {
                    return 0;
                } else if (diff > 0) {
                    return desc ? -1 : 1;
                } else {
                    return desc ? 1 : -1;
                }
            }
        }
    }

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
        BigDecimal b = new BigDecimal(v);
        return scale(b, scale);
    }

    public static double scale(BigDecimal b, int scale) {
        return b.setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

    public static double random(Random r, int max, boolean nonNegative) {
        if (r == null) {
            r = new Random();
        }
        int sign = nonNegative ? 1 : r.nextInt(2);
        sign = sign == 1 ? 1 : -1;
        double d = r.nextDouble();
        int i = max > 0 ? r.nextInt(max) : 0;
        return sign == 1 ? i + d : -(i + d);
    }

}
