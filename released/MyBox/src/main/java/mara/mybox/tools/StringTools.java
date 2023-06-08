package mara.mybox.tools;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.Collator;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppValues;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2018-6-11 17:43:53
 * @License Apache License Version 2.0
 */
public class StringTools {

    public static final int MenuMaxLen = 80;

    public static String start(String string, int maxLen) {
        if (string == null) {
            return string;
        }
        return string.length() > maxLen ? string.substring(0, maxLen) + "..." : string;
    }

    public static String end(String string, int maxLen) {
        if (string == null) {
            return string;
        }
        int len = string.length();
        return len > maxLen ? "..." + string.substring(len - maxLen, len) : string;
    }

    public static String abbreviate(String string, int maxLen) {
        if (string == null) {
            return string;
        }
        return start(replaceLineBreak(string, " ").strip(), maxLen);
    }

    // https://github.com/Mararsh/MyBox/issues/1266
    // Error popped when menu name includes "_". Not sure whether this is a bug of javafx
    public static String menuSuffix(String name) {
        if (name == null) {
            return null;
        }
        return end(name.replaceAll("_|\r\n|\r|\n", " ").strip(), MenuMaxLen);
    }

    public static String menuPrefix(String name) {
        if (name == null) {
            return null;
        }
        return start(name.replaceAll("_|\r\n|\r|\n", " ").strip(), MenuMaxLen);
    }

    public static String replaceLineBreak(String string) {
        return replaceLineBreak(string, " ");
    }

    public static String replaceHtmlLineBreak(String string) {
        return replaceLineBreak(string, "</BR>");
    }

    public static String replaceLineBreak(String string, String replaceAs) {
        if (string == null) {
            return string;
        }
        if (replaceAs == null) {
            replaceAs = "";
        }
        return string.replaceAll("\r\n|\n|\r", replaceAs);
    }

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
        if (string == null) {
            return null;
        }
        return string.trim().split("\\s+");
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
            return AppValues.InvalidInteger;
        }
        try {
            String s = string.trim();
            int sign = 1;
            if (s.startsWith("-")) {
                if (s.length() == 1) {
                    return AppValues.InvalidInteger;
                }
                sign = -1;
                s = s.substring(1);
            }
            String prefix = "";
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (isNumber(c)) {
                    prefix += c;
                } else {
                    break;
                }
            }
            if (prefix.isBlank()) {
                return AppValues.InvalidInteger;
            }
            return Integer.parseInt(prefix) * sign;
        } catch (Exception e) {
            return AppValues.InvalidInteger;
        }
    }

    public static boolean isNumber(char c) {
        return c >= '0' && c <= '9';
    }

    public static int compareWithNumber(String string1, String string2) {
        try {
            if (string1 == null) {
                return string2 == null ? 0 : -1;
            }
            if (string2 == null) {
                return 1;
            }
            int len = Math.min(string1.length(), string2.length());
            String number1 = "", number2 = "";
            boolean numberEnd1 = false, numberEnd2 = false;
            for (int i = 0; i < len; i++) {
                char c1 = string1.charAt(i);
                char c2 = string2.charAt(i);
                if (c1 == c2) {
                    continue;
                }
                if (isNumber(c1)) {
                    if (!numberEnd1) {
                        number1 += c1;
                    }
                } else {
                    numberEnd1 = true;
                }
                if (isNumber(c2)) {
                    if (!numberEnd2) {
                        number2 += c2;
                    }
                } else {
                    numberEnd2 = true;
                }
                if (numberEnd1 && numberEnd2) {
                    break;
                }
            }
            if (!number1.isBlank() && !number2.isBlank()) {
                return compareNumber(number1, number2);
            }
            return compare(string1, string2);
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return 1;
        }
    }

    public static int compareNumber(String number1, String number2) {
        if (number1 == null) {
            return number2 == null ? 0 : -1;
        }
        if (number2 == null) {
            return 1;
        }
        int len1 = number1.length();
        int len2 = number2.length();
        if (len1 > len2) {
            return 1;
        } else if (len1 < len2) {
            return -1;
        }
        for (int i = 0; i < len1; i++) {
            char c1 = number1.charAt(i);
            char c2 = number2.charAt(i);
            if (c1 > c2) {
                return 1;
            } else if (c1 < c2) {
                return -1;
            }
        }
        return 0;
    }

    public static int compare(String s1, String s2) {
        if (s1 == null) {
            return s2 == null ? 0 : -1;
        }
        if (s2 == null) {
            return 1;
        }
        Collator compare = Collator.getInstance(Locale.getDefault());
        return compare.compare(s1, s2);
    }

    public static void sort(List<String> strings, Locale locale) {
        if (strings == null || strings.isEmpty()) {
            return;
        }
        Collections.sort(strings, new Comparator<String>() {

            private final Collator compare = Collator.getInstance(locale);

            @Override
            public int compare(String f1, String f2) {
                return compare.compare(f1, f2);
            }
        });

    }

    public static void sort(List<String> strings) {
        sort(strings, Locale.getDefault());
    }

    public static void sortDesc(List<String> strings, Locale locale) {
        if (strings == null || strings.isEmpty()) {
            return;
        }
        Collections.sort(strings, new Comparator<String>() {

            private final Collator compare = Collator.getInstance(Locale.getDefault());

            @Override
            public int compare(String f1, String f2) {
                return compare.compare(f2, f1);
            }
        });
    }

    public static void sortDesc(List<String> strings) {
        sortDesc(strings, Locale.getDefault());
    }

    public static String format(long data) {
        try {
            DecimalFormat df = new DecimalFormat("#,###");
            return df.format(data);
        } catch (Exception e) {
            return message("Invalid");
        }
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
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    public static boolean match(String string, String find, boolean isRegex,
            boolean dotAll, boolean multiline, boolean caseInsensitive) {
        if (string == null || find == null || find.isEmpty()) {
            return false;
        }
        try {
            int mode = (isRegex ? 0x00 : Pattern.LITERAL)
                    | (caseInsensitive ? Pattern.CASE_INSENSITIVE : 0x00)
                    | (dotAll ? Pattern.DOTALL : 0x00)
                    | (multiline ? Pattern.MULTILINE : 0x00);
            Pattern pattern = Pattern.compile(find, mode);
            Matcher matcher = pattern.matcher(string);
            return matcher.matches();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
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
            MyBoxLog.debug(e.toString());
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
        while ((i = unicode.indexOf("\\u", pos)) > 0) {
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

    public static String discardBlankLines(String text) {
        if (text == null) {
            return null;
        }
        String[] lines = text.split("\n");
        StringBuilder s = new StringBuilder();
        for (String line : lines) {
            if (line.isBlank()) {
                continue;
            }
            s.append(line).append("\n");
        }
        return s.toString();
    }

    public static boolean noDuplicated(List<String> names, boolean notNull) {
        try {
            if (names == null || names.isEmpty()) {
                return false;
            }
            List<String> valid = new ArrayList<>();
            for (int c = 0; c < names.size(); c++) {
                String name = names.get(c);
                if (notNull && name == null) {
                    return false;
                }
                if (valid.contains(name)) {
                    return false;
                }
                valid.add(name);
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static String[][] transpose(String[][] matrix) {
        try {
            if (matrix == null) {
                return null;
            }
            int rowsNumber = matrix.length, columnsNumber = matrix[0].length;
            String[][] result = new String[columnsNumber][rowsNumber];
            for (int row = 0; row < rowsNumber; ++row) {
                for (int col = 0; col < columnsNumber; ++col) {
                    result[col][row] = matrix[row][col];
                }
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public static String[] matrix2Array(String[][] m) {
        if (m == null || m.length == 0 || m[0].length == 0) {
            return null;
        }
        int h = m.length;
        int w = m[0].length;
        String[] a = new String[w * h];
        for (int j = 0; j < h; ++j) {
            System.arraycopy(m[j], 0, a, j * w, w);
        }
        return a;
    }

    public static String[][] array2Matrix(String[] a, int w) {
        if (a == null || a.length == 0 || w < 1) {
            return null;
        }
        int h = a.length / w;
        if (h < 1) {
            return null;
        }
        String[][] m = new String[h][w];
        for (int j = 0; j < h; ++j) {
            for (int i = 0; i < w; ++i) {
                m[j][i] = a[j * w + i];
            }
        }
        return m;
    }

    public static String[][] toString(double[][] dMatrix) {
        try {
            if (dMatrix == null) {
                return null;
            }
            int rsize = dMatrix.length, csize = dMatrix[0].length;
            String[][] sMatrix = new String[rsize][csize];
            for (int i = 0; i < rsize; i++) {
                for (int j = 0; j < csize; j++) {
                    sMatrix[i][j] = dMatrix[i][j] + "";
                }
            }
            return sMatrix;
        } catch (Exception e) {
            return null;
        }
    }

    public static List<String> toList(String value, String separater) {
        try {
            if (value == null || value.isBlank()) {
                return null;
            }
            String[] a = value.split(separater);
            if (a == null || a.length == 0) {
                return null;
            }
            List<String> values = new ArrayList<>();
            for (String v : a) {
                if (v != null && !v.isBlank()) {
                    values.add(v);
                }
            }
            if (values.isEmpty()) {
                return null;
            } else {
                return values;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static String toString(List<String> values, String separater) {
        try {
            if (values == null || values.isEmpty()) {
                return null;
            }
            String s = null;
            for (String v : values) {
                if (v != null && !v.isBlank()) {
                    if (s == null) {
                        s = v;
                    } else {
                        s += separater + v;
                    }
                }
            }
            if (s == null || s.isBlank()) {
                return null;
            } else {
                return s;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean string2Boolean(String string) {
        return isTrue(string);
    }

    public static boolean isTrue(String string) {
        if (string == null || string.isBlank()) {
            return false;
        }
        return "1".equals(string)
                || "true".equalsIgnoreCase(string) || "yes".equalsIgnoreCase(string)
                || message("true").equals(string) || message("Yes").equals(string);
    }

    public static boolean isFalse(String string) {
        if (string == null || string.isBlank()) {
            return false;
        }
        return "0".equals(string)
                || "false".equalsIgnoreCase(string) || "no".equalsIgnoreCase(string)
                || message("false").equals(string) || message("No").equals(string);
    }

}
