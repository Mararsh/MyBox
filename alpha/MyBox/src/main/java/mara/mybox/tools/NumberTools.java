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

    public static String format(Number data, String format) {
        return format(data, format, -1);
    }

    public static String format(Number data, String inFormat, int scale) {
        try {
            if (data == null) {
                return null;
            }
            String format = inFormat;
            if (format == null || format.isBlank()
                    || message("en", "None").equals(format)
                    || message("zh", "None").equals(format)) {
                return noFormat(data);

            } else if (message("en", "ScientificNotation").equals(format)
                    || message("zh", "ScientificNotation").equals(format)) {
                return scientificNotation(data);

            } else if (message("en", "GroupInThousands").equals(format)
                    || message("zh", "GroupInThousands").equals(format)) {
                return format(data, 3, scale);

            } else if (message("en", "GroupInTenThousands").equals(format)
                    || message("zh", "GroupInTenThousands").equals(format)) {
                return format(data, 4, scale);

            } else {
                DecimalFormat df = new DecimalFormat(format);
                df.setMaximumFractionDigits(scale >= 0 ? scale : 340);
                df.setRoundingMode(RoundingMode.HALF_UP);
                return df.format(data);
            }

        } catch (Exception e) {
            return new DecimalFormat().format(data);
        }
    }

    public static String noFormat(Number data) {
        return format(data, -1, -1);
    }

    public static String scientificNotation(Number data) {
        return data == null ? null : data.toString();
    }

    public static String format(Number data) {
        return format(data, 3, -1);
    }

    public static String format(Number data, int scale) {
        return format(data, -1, scale);
    }

    public static String format(Number data, int groupSize, int scale) {
        try {
            if (data == null) {
                return null;
            }
            DecimalFormat df = new DecimalFormat();
            if (groupSize >= 0) {
                df.setGroupingUsed(true);
                df.setGroupingSize(groupSize);
            } else {
                df.setGroupingUsed(false);
            }
            df.setMaximumFractionDigits(scale >= 0 ? scale : 340);
            df.setRoundingMode(RoundingMode.HALF_UP);
            return df.format(data);
        } catch (Exception e) {
            return null;
        }
    }

}
