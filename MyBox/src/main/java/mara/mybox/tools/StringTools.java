package mara.mybox.tools;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.scene.control.IndexRange;
import static mara.mybox.value.AppVariables.logger;

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

    public static boolean match(String string, String find, boolean caseInsensitive) {
        if (string == null || find == null) {
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

    public static IndexRange first(String string, String find, int from,
            boolean isRegex, boolean caseInsensitive, boolean multiline) {
        if (string == null || find == null) {
            return null;
        }
        try {
            int mode = (isRegex ? 0x00 : Pattern.LITERAL)
                    | (caseInsensitive ? Pattern.CASE_INSENSITIVE : 0x00)
                    | (multiline ? Pattern.MULTILINE : 0x00);
            Pattern pattern = Pattern.compile(find, mode);
            Matcher matcher = pattern.matcher(string);
            if (matcher.find(from < 0 ? 0 : from)) {
                IndexRange range = new IndexRange(matcher.start(), matcher.end());
                return range;
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

    public static IndexRange last(String string, String find, int from,
            boolean isRegex, boolean caseInsensitive, boolean multiline) {
        if (string == null || find == null) {
            return null;
        }
        try {
            int mode = (isRegex ? 0x00 : Pattern.LITERAL)
                    | (caseInsensitive ? Pattern.CASE_INSENSITIVE : 0x00)
                    | (multiline ? Pattern.MULTILINE : 0x00);
            Pattern pattern = Pattern.compile(find, mode);
            Matcher matcher = pattern.matcher(string);
            if (!matcher.find(from < 0 ? 0 : from)) {
                return null;
            }
            IndexRange range = new IndexRange(matcher.start(), matcher.end());
            while (matcher.find()) {
                range = new IndexRange(matcher.start(), matcher.end());
            }
            return range;
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

    public static Map<String, Object> lastAndCount(String string, String regex,
            boolean isRegex, boolean caseInsensitive, boolean multiline) {
        Map<String, Object> results = new HashMap<>();
        results.put("count", 0);
        results.put("lastRange", null);
        results.put("lastMatch", "");
        try {
            if (string == null || string.isEmpty() || regex == null || regex.isEmpty()) {
                return results;
            }
            int mode = (isRegex ? 0x00 : Pattern.LITERAL)
                    | (caseInsensitive ? Pattern.CASE_INSENSITIVE : 0x00)
                    | (multiline ? Pattern.MULTILINE : 0x00);
            int count = 0;
            String lastMatch = "";
            Pattern pattern = Pattern.compile(regex, mode);
            Matcher matcher = pattern.matcher(string);
            IndexRange range = null;
            while (matcher.find()) {
                count++;
                lastMatch = matcher.group();
                range = new IndexRange(matcher.start(), matcher.end());
            }
            results.put("count", count);
            results.put("lastRange", range);
            results.put("lastMatch", lastMatch);
        } catch (Exception e) {
            logger.debug(e.toString());
        }
        return results;
    }

    public static int countNumber(String string, String find,
            boolean isRegex, boolean caseInsensitive, boolean multiline) {
        if (string == null || string.isEmpty() || find == null || find.isEmpty()) {
            return 0;
        }
        try {
            int mode = (isRegex ? 0x00 : Pattern.LITERAL)
                    | (caseInsensitive ? Pattern.CASE_INSENSITIVE : 0x00)
                    | (multiline ? Pattern.MULTILINE : 0x00);
            Pattern pattern = Pattern.compile(find, mode);
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

    public static int countNumber(String string, String find) {
        return countNumber(string, find, false, false, true);
    }

    public static String replace(String string, String find, String newString, int from,
            boolean isRegex, boolean caseInsensitive, boolean multiline) {
        if (string == null || find == null || newString == null) {
            return string;
        }
        try {
            int mode = (isRegex ? 0x00 : Pattern.LITERAL)
                    | (caseInsensitive ? Pattern.CASE_INSENSITIVE : 0x00)
                    | (multiline ? Pattern.MULTILINE : 0x00);
            Pattern pattern = Pattern.compile(find, mode);
            Matcher matcher = pattern.matcher(string);
            if (matcher.find(from < 0 ? 0 : from)) {
                StringBuffer s = new StringBuffer();
                matcher.appendReplacement(s, newString);
                matcher.appendTail(s);
                return s.toString();
            } else {
                return string;
            }
        } catch (Exception e) {
            logger.debug(e.toString());
            return string;
        }
    }

    public static String replaceAll(String string, String find, String newString,
            boolean isRegex, boolean caseInsensitive, boolean multiline) {
        if (string == null || find == null || newString == null) {
            return string;
        }
        try {
            int mode = (isRegex ? 0x00 : Pattern.LITERAL)
                    | (caseInsensitive ? Pattern.CASE_INSENSITIVE : 0x00)
                    | (multiline ? Pattern.MULTILINE : 0x00);
            Pattern pattern = Pattern.compile(find, mode);
            Matcher matcher = pattern.matcher(string);
            StringBuffer s = new StringBuffer();
            while (matcher.find()) {
                matcher.appendReplacement(s, newString);
            }
            matcher.appendTail(s);
            return s.toString();
        } catch (Exception e) {
            logger.debug(e.toString());
            return string;
        }
    }

    public static String replaceAll(String string, String oldString, String newString) {
        return replaceAll(string, oldString, newString, false, false, true);
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
