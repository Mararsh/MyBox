package mara.mybox.tools;

import java.math.BigDecimal;

/**
 * @Author Mara
 * @CreateDate 2018-6-11 17:43:53
 * @Description
 * @License Apache License Version 2.0
 */
public class ValueTools {

    public static String fillNumber(int value, int digit) {
        String v = value + "";
        for (int i = v.length(); i < digit; i++) {
            v = "0" + v;
        }
        return v;
    }

    public static float roundFloat2(float fvalue) {
        int scale = 2;
        int roundingMode = 4;
        BigDecimal bd = new BigDecimal(fvalue);
        bd = bd.setScale(scale, roundingMode);
        return bd.floatValue();
    }

    public static float roundFloat3(float fvalue) {
        int scale = 3;
        int roundingMode = 4;
        BigDecimal bd = new BigDecimal(fvalue);
        bd = bd.setScale(scale, roundingMode);
        return bd.floatValue();
    }

    public static double roundDouble3(double invalue) {
        return (double) Math.round(invalue * 1000.0) / 1000.0;
    }

    public static double roundDouble2(double invalue) {
        return (double) Math.round(invalue * 100.0) / 100.0;
    }

    public static double roundDouble4(double invalue) {
        return (double) Math.round(invalue * 10000.0) / 10000.0;
    }

}
