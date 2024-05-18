package mara.mybox.tools;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import javafx.scene.control.IndexRange;
import mara.mybox.db.data.ColumnDefinition.InvalidAs;
import mara.mybox.value.Languages;

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
                    || Languages.matchIgnoreCase("None", format)) {
                return noFormat(data, scale);

            } else if (Languages.matchIgnoreCase("ScientificNotation", format)) {
                return scientificNotation(data, scale);

            } else if (Languages.matchIgnoreCase("GroupInThousands", format)) {
                return format(data, 3, scale);

            } else if (Languages.matchIgnoreCase("GroupInTenThousands", format)) {
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

    public static String noFormat(Number data, int scale) {
        return format(data, -1, scale);
    }

    public static String scientificNotation(Number data, int scale) {
        try {
            if (data == null) {
                return null;
            }
            double d = DoubleTools.scale(data.toString(), InvalidAs.Empty, scale);
            return d + "";
        } catch (Exception e) {
            return null;
        }
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

    // 0-based, exclude to and end
    public static IndexRange scrollRange(int scrollSize, int total, int from, int to, int currentIndex) {
        if (total < 1) {
            return null;
        }
        int start = Math.max(from, currentIndex - scrollSize / 2);
        if (start < 0 || start >= total) {
            start = 0;
        }
        int end = Math.min(to, start + scrollSize);
        if (end < 0 || end > total) {
            end = total;
        }
        if (start >= end) {
            return null;
        }
        return new IndexRange(start, end);
    }

    public static IndexRange scrollRange(int scrollSize, int total, int currentIndex) {
        return scrollRange(scrollSize, total, 0, total, currentIndex);
    }

}
