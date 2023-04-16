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
public class LongTools {

    public static boolean invalidLong(long value) {
        return value == AppValues.InvalidLong;
    }

    public static int compare(String s1, String s2, boolean desc) {
        double d1, d2;
        try {
            d1 = Long.parseLong(s1.replaceAll(",", ""));
        } catch (Exception e) {
            d1 = Double.NaN;
        }
        try {
            d2 = Long.parseLong(s2.replaceAll(",", ""));
        } catch (Exception e) {
            d2 = Double.NaN;
        }
        return DoubleTools.compare(d1, d2, desc);
    }

    public static long random(Random r, int max, boolean nonNegative) {
        if (r == null) {
            r = new Random();
        }
        int sign = nonNegative ? 1 : r.nextInt(2);
        long l = r.nextLong(max);
        return sign == 1 ? l : -l;
    }

    public static long[] sortArray(long[] numbers) {
        List<Long> list = new ArrayList<>();
        for (long i : numbers) {
            list.add(i);
        }
        Collections.sort(list, new Comparator<Long>() {
            @Override
            public int compare(Long p1, Long p2) {
                if (p1 > p2) {
                    return 1;
                } else if (p1 < p2) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        long[] sorted = new long[numbers.length];
        for (int i = 0; i < list.size(); ++i) {
            sorted[i] = list.get(i);
        }
        return sorted;
    }

}
