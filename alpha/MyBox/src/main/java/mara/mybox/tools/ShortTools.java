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
public class ShortTools {

    public static short random(short max) {
        Random r = new Random();
        return (short) r.nextInt(max);
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
