package mara.mybox.tools;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-10-4
 * @License Apache License Version 2.0
 */
public class NumberTools {

    public static String format(Number data, String inFormat) {
        try {
            String format = inFormat;
            if (message("en", "GroupInThousands").equals(format)
                    || message("zh", "GroupInThousands").equals(format)) {
                format = groupFormat3(data);
            } else if (message("en", "GroupInTenThousands").equals(format)
                    || message("zh", "GroupInTenThousands").equals(format)) {
                format = groupFormat4(data);
            }
            DecimalFormat df = new DecimalFormat(format);
            return df.format(data);
        } catch (Exception e) {
            return new DecimalFormat().format(data);
        }
    }

    public static String format(Number data) {
        return format(data, groupFormat3(data));
    }

    public static String groupFormat3(Number data) {
        try {
            String format = "#,###";
            String s = new DecimalFormat().format(data);
            int pos = s.indexOf(".");
            if (pos >= 0) {
                format += "." + "#".repeat(s.substring(pos + 1).length());
            }
            return format;
        } catch (Exception e) {
            return null;
        }
    }

    public static String groupFormat4(Number data) {
        try {
            String format = "#,####";
            String s = new DecimalFormat().format(data);
            int pos = s.indexOf(".");
            if (pos >= 0) {
                format += "." + "#".repeat(s.substring(pos + 1).length());
            }
            return format;
        } catch (Exception e) {
            return null;
        }
    }

    public static String format(Number data, int scale) {
        try {
            DecimalFormat formatter = new DecimalFormat(groupFormat3(data));
            formatter.setMaximumFractionDigits(scale);
            formatter.setRoundingMode(RoundingMode.HALF_UP);
            return formatter.format(data);
        } catch (Exception e) {
            return new DecimalFormat().format(data);
        }
    }

}
