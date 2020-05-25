package mara.mybox.tools;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author Mara
 * @CreateDate 2018-6-11 17:43:53
 * @Description
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

    public static String fillLeftZero(int value, int digit) {
        String v = value + "";
        for (int i = v.length(); i < digit; ++i) {
            v = "0" + v;
        }
        return v;
    }

    public static String fillRightZero(int value, int digit) {
        String v = value + "";
        for (int i = v.length(); i < digit; ++i) {
            v += "0";
        }
        return v;
    }

    public static String fillRightBlank(int value, int digit) {
        String v = value + "";
        for (int i = v.length(); i < digit; ++i) {
            v += " ";
        }
        return v;
    }

    public static String fillLeftBlank(int value, int digit) {
        String v = value + "";
        for (int i = v.length(); i < digit; ++i) {
            v = " " + v;
        }
        return v;
    }

    public static String fillRightBlank(double value, int digit) {
        String v = value + "";
        for (int i = v.length(); i < digit; ++i) {
            v += " ";
        }
        return v;
    }

    public static String fillLeftBlank(double value, int digit) {
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

    public static String replaceAll(String string, String oldString,
            String newString) {
        if (string == null || string.isEmpty()
                || oldString == null || oldString.isEmpty()
                || newString == null) {
            return string;
        }
        try {
            String replaced = string.replace(
                    oldString.subSequence(0, oldString.length()),
                    newString.subSequence(0, newString.length())
            );
            return replaced;
        } catch (Exception e) {
            //            logger.debug(e.toString());
            return string;
        }
    }

    public static Map<String, Object> lastAndCount(String string,
            String subString) {
        Map<String, Object> results = new HashMap<>();
        results.put("count", 0);
        results.put("lastIndex", -1);
        results.put("lastMatch", "");
        if (string == null || string.isEmpty() || subString == null || subString.isEmpty() || string.length() < subString.length()) {
            return results;
        }
        int fromIndex = 0;
        int count = 0;
        int lastIndex = -1;
        while (true) {
            int index = string.indexOf(subString, fromIndex);
            if (index < 0) {
                break;
            }
            lastIndex = index;
            fromIndex = index + 1;
            count++;
        }
        results.put("count", count);
        results.put("lastIndex", lastIndex);
        results.put("lastMatch", subString);
        return results;
    }

    public static boolean match(String string, String regex) {
        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(string);
            return matcher.matches();
        } catch (Exception e) {
            //            logger.debug(e.toString());
            return false;
        }
    }

    public static int lastRegex(String string, String regex) {
        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(string);
            int index = -1;
            while (matcher.find()) {
                index = matcher.start();
            }
            return index;
        } catch (Exception e) {
            //            logger.debug(e.toString());
            return -1;
        }
    }

    public static int firstRegex(String string, String regex) {
        return firstRegex(string, regex, 0);
    }

    public static int firstRegex(String string, String regex, int from) {
        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(string);
            if (matcher.find(from)) {
                return matcher.start();
            } else {
                return -1;
            }
        } catch (Exception e) {
            //            logger.debug(e.toString());
            return -1;
        }
    }

    public static Map<String, Object> lastAndCountRegex(String string,
            String regex) {
        Map<String, Object> results = new HashMap<>();
        results.put("count", 0);
        results.put("lastIndex", -1);
        results.put("lastMatch", "");
        try {
            if (string == null || string.isEmpty() || regex == null || regex.isEmpty()) {
                return results;
            }
            int count = 0;
            int lastIndex = -1;
            String lastMatch = "";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(string);
            while (matcher.find()) {
                count++;
                lastMatch = matcher.group();
                lastIndex = matcher.start();
            }
            results.put("count", count);
            results.put("lastIndex", lastIndex);
            results.put("lastMatch", lastMatch);
        } catch (Exception e) {
            //            logger.debug(e.toString());
        }
        return results;
    }

    public static int countNumberRegex(String string, String regex) {
        if (string == null || string.isEmpty() || regex == null || regex.isEmpty()) {
            return 0;
        }
        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(string);
            int count = 0;
            while (matcher.find()) {
                count++;
            }
            return count;
        } catch (Exception e) {
            //            logger.debug(e.toString());
            return 0;
        }
    }

    public static int countNumber(String string, String subString) {
        if (string == null || string.isEmpty() || subString == null || subString.isEmpty() || string.length() < subString.length()) {
            return 0;
        }
        int fromIndex = 0;
        int count = 0;
        while (true) {
            int index = string.indexOf(subString, fromIndex);
            if (index < 0) {
                break;
            }
            fromIndex = index + 1;
            count++;
        }
        return count;
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

}
