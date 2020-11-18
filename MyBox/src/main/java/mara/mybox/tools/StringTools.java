package mara.mybox.tools;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.Collator;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static mara.mybox.value.AppVariables.logger;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-6-11 17:43:53
 * @License Apache License Version 2.0
 */
public class StringTools {

    public static String[] separatedBySpace(String string) {
        String[] ss = new String[2];
        String s = string.trim();
        int pos1 = s.indexOf(' ');
        if (pos1 < 0) {
            ss[0] = s;
            ss[1] = "";
            return ss;
        }
        ss[0] = s.substring(0, pos1);
        ss[1] = s.substring(pos1).trim();
        return ss;
    }

    public static String[] splitBySpace(String string) {
        String[] splitted = string.trim().split("\\s+");
        return splitted;
    }

    public static String[] splitByComma(String string) {
        String[] splitted = string.split(",");
        return splitted;
    }

    public static String fillLeftZero(Number value, int digit) {
        String v = value + "";
        for (int i = v.length(); i < digit; ++i) {
            v = "0" + v;
        }
        return v;
    }

    public static String fillRightZero(Number value, int digit) {
        String v = value + "";
        for (int i = v.length(); i < digit; ++i) {
            v += "0";
        }
        return v;
    }

    public static String fillRightBlank(Number value, int digit) {
        String v = value + "";
        for (int i = v.length(); i < digit; ++i) {
            v += " ";
        }
        return v;
    }

    public static String fillLeftBlank(Number value, int digit) {
        String v = new BigDecimal(value + "").toString() + "";
        for (int i = v.length(); i < digit; ++i) {
            v = " " + v;
        }
        return v;
    }

    public static String fillRightBlank(String value, int digit) {
        String v = value;
        for (int i = v.length(); i < digit; ++i) {
            v += " ";
        }
        return v;
    }

    public static int numberPrefix(String string) {
        if (string == null) {
            return CommonValues.InvalidInteger;
        }
        try {
            String s = string.trim();
            int sign = 1;
            if (s.startsWith("-")) {
                if (s.length() == 1) {
                    return CommonValues.InvalidInteger;
                }
                sign = -1;
                s = s.substring(1);
            }
            String prefix = "";
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c >= '0' && c <= '9') {
                    prefix += c;
                } else {
                    break;
                }
            }
            if (prefix.isBlank()) {
                return CommonValues.InvalidInteger;
            }
            return Integer.parseInt(prefix) * sign;
        } catch (Exception e) {
            return CommonValues.InvalidInteger;
        }
    }

    public static int firstNumber(String string) {
        if (string == null) {
            return CommonValues.InvalidInteger;
        }
        try {
            String s = string.trim();
            String number = "";
            boolean startNumber = false;
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c >= '0' && c <= '9') {
                    startNumber = true;
                    number += c;
                } else {
                    if (startNumber) {
                        break;
                    }
                }
            }
            if (number.isBlank()) {
                return CommonValues.InvalidInteger;
            }
            return Integer.parseInt(number);
        } catch (Exception e) {
            return CommonValues.InvalidInteger;
        }
    }

    public static int compareWithNumber(String string1, String string2) {
        if (string1 == null) {
            return string2 == null ? 0 : -1;
        }
        if (string2 == null) {
            return 1;
        }
        int int1 = StringTools.firstNumber(string1);
        int int2 = StringTools.firstNumber(string2);
        if (int1 != CommonValues.InvalidInteger && int2 != CommonValues.InvalidInteger) {
            int pos1 = string1.indexOf(int1 + "");
            int pos2 = string2.indexOf(int2 + "");
            if (pos1 != pos2
                    || (pos1 > 0 && !string1.substring(0, pos1).equals(string2.substring(0, pos2)))) {
                Comparator<Object> compare = Collator.getInstance(Locale.getDefault());
                return compare.compare(string1, string2);
            }
            if (int1 == int2) {
                return 0;
            } else if (int1 > int2) {
                return 1;
            } else {
                return -1;
            }
        } else {
            Comparator<Object> compare = Collator.getInstance(Locale.getDefault());
            return compare.compare(string1, string2);
        }
    }

    public static String format(long data) {
        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(data);
    }

    public static String format(double data) {
        return format(Math.round(data));
    }

    public static String leftAlgin(String name, String value, int nameLength) {
        return String.format("%-" + nameLength + "s:" + value, name);
    }

    public static boolean match(String string, String find, boolean caseInsensitive) {
        if (string == null || find == null || find.isEmpty()) {
            return false;
        }
        try {
            int mode = (caseInsensitive ? Pattern.CASE_INSENSITIVE : 0x00) | Pattern.MULTILINE;
            Pattern pattern = Pattern.compile(find, mode);
            Matcher matcher = pattern.matcher(string);
            return matcher.matches();
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean include(String string, String find, boolean caseInsensitive) {
        if (string == null || find == null || find.isEmpty()) {
            return false;
        }
        try {
            int mode = (caseInsensitive ? Pattern.CASE_INSENSITIVE : 0x00) | Pattern.MULTILINE;
            Pattern pattern = Pattern.compile(find, mode);
            Matcher matcher = pattern.matcher(string);
            return matcher.find();
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

    // https://blog.csdn.net/zx1749623383/article/details/79540748
    public static String decodeUnicode(String unicode) {
        if (unicode == null || "".equals(unicode)) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        int i, pos = 0;
        while ((i = unicode.indexOf("\\u", pos)) != -1) {
            sb.append(unicode.substring(pos, i));
            if (i + 5 < unicode.length()) {
                pos = i + 6;
                sb.append((char) Integer.parseInt(unicode.substring(i + 2, i + 6), 16));
            } else {
                break;
            }
        }
        return sb.toString();
    }

    public static String encodeUnicode(String string) {
        if (string == null || "".equals(string)) {
            return null;
        }
        StringBuilder unicode = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            unicode.append("\\u").append(Integer.toHexString(c));
        }
        return unicode.toString();
    }

    public static String convertCharset(String source, String sourceCharset, String targetCharset) {
        try {
            if (sourceCharset.equals(targetCharset)) {
                return source;
            }
            return new String(source.getBytes(sourceCharset), targetCharset);
        } catch (Exception e) {
            return source;
        }
    }

    public static String convertCharset(String source, Charset sourceCharset, Charset targetCharset) {
        try {
            if (sourceCharset.equals(targetCharset)) {
                return source;
            }
            return new String(source.getBytes(sourceCharset), targetCharset);
        } catch (Exception e) {
            return source;
        }
    }

}
