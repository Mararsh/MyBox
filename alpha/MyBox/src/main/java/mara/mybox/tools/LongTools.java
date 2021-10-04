package mara.mybox.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * @Author Mara
 * @CreateDate 2021-10-4
 * @License Apache License Version 2.0
 */
public class LongTools {

    public static long random(long max) {
        Random r = new Random();
        return r.nextLong(max);
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
