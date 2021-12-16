package mara.mybox.tools;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Random;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2018-6-11 17:43:53
 * @Description
 * @License Apache License Version 2.0
 */
public class DoubleTools {

    public static String percentage(double data, double total) {
        try {
            if (total == 0) {
                return message("Invalid");
            }
            String format = "#,###.##";
            DecimalFormat df = new DecimalFormat(format);
            return df.format(scale(data * 100 / total, 2));
        } catch (Exception e) {
            return message("Invalid");
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
            return message("Invalid");
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

    public static double random(Random r, int max) {
        if (r == null) {
            r = new Random();
        }
        int sign = r.nextInt(2);
        sign = sign == 1 ? 1 : -1;
        double d = r.nextDouble();
        int i = max > 0 ? r.nextInt(max) : 0;
        return sign == 1 ? i + d : -(i + d);
    }

}
