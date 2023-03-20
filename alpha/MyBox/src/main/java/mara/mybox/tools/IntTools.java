package mara.mybox.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * @Author Mara
 * @CreateDate 2019-5-28 15:28:13
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class IntTools {

    public static int compare(String s1, String s2, boolean desc) {
        float f1, f2;
        try {
            f1 = Integer.parseInt(s1.replaceAll(",", ""));
        } catch (Exception e) {
            f1 = Float.NaN;
        }
        try {
            f2 = Integer.parseInt(s2.replaceAll(",", ""));
        } catch (Exception e) {
            f2 = Float.NaN;
        }
        return FloatTools.compare(f1, f2, desc);
    }

    public static int random(int max) {
        return new Random().nextInt(max);
    }

    public static int random(Random r, int max, boolean nonNegative) {
        if (r == null) {
            r = new Random();
        }
        int sign = nonNegative ? 1 : r.nextInt(2);
        int i = r.nextInt(max);
        return sign == 1 ? i : -i;
    }

    public static int[] sortArray(int[] numbers) {
        List<Integer> list = new ArrayList<>();
        for (int i : numbers) {
            list.add(i);
        }
        Collections.sort(list, new Comparator<Integer>() {
            @Override
            public int compare(Integer p1, Integer p2) {
                if (p1 > p2) {
                    return 1;
                } else if (p1 < p2) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        int[] sorted = new int[numbers.length];
        for (int i = 0; i < list.size(); ++i) {
            sorted[i] = list.get(i);
        }
        return sorted;
    }

    public static void sortList(List<Integer> numbers) {
        Collections.sort(numbers, new Comparator<Integer>() {
            @Override
            public int compare(Integer p1, Integer p2) {
                return p1 - p2;
            }
        });
    }

}
