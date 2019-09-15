package mara.mybox.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import javafx.scene.control.IndexRange;

/**
 * @Author Mara
 * @CreateDate 2019-5-28 15:28:13
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class IntTools {

    public static int mapInt(int value, IndexRange originalRange, IndexRange newlRange) {
        if (originalRange == null || newlRange == null || originalRange.getStart() > value || originalRange.getEnd() < value) {
            return value;
        }
        int len = value - originalRange.getStart() + 1;
        double ratio = newlRange.getLength() * 1.0 / originalRange.getLength();
        return newlRange.getStart() + (int) Math.round(len * ratio);
    }

    public static int zipInt(int value, int zipStep) {
        return Math.round((value + zipStep / 2) / zipStep) * zipStep;
    }

    public static int getRandomInt(int max) {
        Random r = new Random();
        return r.nextInt(max);
    }

    public static int[] sortArray(int[] numbers) {
        List<Integer> list = new ArrayList<>();
        for (int i : numbers) {
            list.add(i);
        }
        Collections.sort(list, new Comparator<Integer>() {
            @Override
            public int compare(Integer p1, Integer p2) {
                return p1 - p2;
            }
        });
        int[] sorted = new int[numbers.length];
        for (int i = 0; i < list.size(); i++) {
            sorted[i] = list.get(i);
        }
        return sorted;
    }

}
