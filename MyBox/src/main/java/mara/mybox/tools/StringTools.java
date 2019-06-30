package mara.mybox.tools;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * @Author Mara
 * @CreateDate 2018-6-11 17:43:53
 * @Description
 * @License Apache License Version 2.0
 */
public class StringTools {

    public static String[] splitBySpace(String string) {
        String[] splitted = string.trim().split("\\s+");
        return splitted;
    }

    public static String[] splitByComma(String string) {
        String[] splitted = string.split(",");
        return splitted;
    }

    public static String fillLeftZero(int value, int digit) {
        String v = value + "";
        for (int i = v.length(); i < digit; i++) {
            v = "0" + v;
        }
        return v;
    }

    public static String fillRightZero(int value, int digit) {
        String v = value + "";
        for (int i = v.length(); i < digit; i++) {
            v += "0";
        }
        return v;
    }

    public static String fillRightBlank(int value, int digit) {
        String v = value + "";
        for (int i = v.length(); i < digit; i++) {
            v += " ";
        }
        return v;
    }

    public static String fillLeftBlank(int value, int digit) {
        String v = value + "";
        for (int i = v.length(); i < digit; i++) {
            v = " " + v;
        }
        return v;
    }

    public static String fillRightBlank(double value, int digit) {
        String v = value + "";
        for (int i = v.length(); i < digit; i++) {
            v += " ";
        }
        return v;
    }

    public static String fillLeftBlank(double value, int digit) {
        String v = new BigDecimal(value + "").toString() + "";
        for (int i = v.length(); i < digit; i++) {
            v = " " + v;
        }
        return v;
    }

    public static String fillRightBlank(String value, int digit) {
        String v = value;
        for (int i = v.length(); i < digit; i++) {
            v += " ";
        }
        return v;
    }

    public static String formatData(long data) {
        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(data);
    }

}
