package mara.mybox.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import mara.mybox.value.AppValues;

/**
 * @Author Mara
 * @CreateDate 2021-10-4
 * @License Apache License Version 2.0
 */
public class ShortTools {

    public static boolean invalidShort(Short value) {
        return value == null || value == AppValues.InvalidShort;
    }

    // invalid values are always in the end
    public static int compare(String s1, String s2, boolean desc) {
        float f1, f2;
        try {
            f1 = Short.parseShort(s1.replaceAll(",", ""));
        } catch (Exception e) {
            f1 = Float.NaN;
        }
        try {
            f2 = Short.parseShort(s2.replaceAll(",", ""));
        } catch (Exception e) {
            f2 = Float.NaN;
        }
        if (Float.isNaN(f1)) {
            if (Float.isNaN(f2)) {
                return 0;
            } else {
                return 1;
            }
        } else {
            if (Float.isNaN(f2)) {
                return -1;
            } else {
                float diff = f1 - f2;
                if (diff == 0) {
                    return 0;
                } else if (diff > 0) {
                    return desc ? -1 : 1;
                } else {
                    return desc ? 1 : -1;
                }
            }
        }
    }

    public static short random(short max) {
        Random r = new Random();
        return (short) r.nextInt(max);
    }

    public static String format(short v, String format, int scale) {
        if (invalidShort(v)) {
            return null;
        }
        return NumberTools.format(v, format, scale);
    }

    public static short[] sortArray(short[] numbers) {
        List<Short> list = new ArrayList<>();
        for (short i : numbers) {
            list.add(i);
        }
        Collections.sort(list, new Comparator<Short>() {
            @Override
            public int compare(Short p1, Short p2) {
                if (p1 > p2) {
                    return 1;
                } else if (p1 < p2) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        short[] sorted = new short[numbers.length];
        for (int i = 0; i < list.size(); ++i) {
            sorted[i] = list.get(i);
        }
        return sorted;
    }

}
