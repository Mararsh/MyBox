package mara.mybox.tools;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import javafx.scene.control.IndexRange;

/**
 * @Author Mara
 * @CreateDate 2018-6-11 17:43:53
 * @Description
 * @License Apache License Version 2.0
 */
public class ValueTools {

    public static int getRandomInt(int max) {
        Random r = new Random();
        return r.nextInt(max);
    }

    public static String fillLeftZero(int value, int digit) {
        String v = value + "";
        for (int i = v.length(); i < digit; i++) {
            v = "0" + v;
        }
        return v;
    }

    public static String fillRightZero(int value, int digit) {
        String v = value + "";
        for (int i = v.length(); i < digit; i++) {
            v += "0";
        }
        return v;
    }

    public static String fillRightBlank(int value, int digit) {
        String v = value + "";
        for (int i = v.length(); i < digit; i++) {
            v += " ";
        }
        return v;
    }

    public static String fillLeftBlank(int value, int digit) {
        String v = value + "";
        for (int i = v.length(); i < digit; i++) {
            v = " " + v;
        }
        return v;
    }

    public static String formatData(long data) {
        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(data);
    }

    public static float roundFloat2(float fvalue) {
        return (float) (Math.round(fvalue * 100)) / 100;
    }

    public static float roundFloat3(float fvalue) {
        return (float) Math.round(fvalue * 1000) / 1000;
    }

    public static float roundFloat4(float fvalue) {
        return (float) Math.round(fvalue * 10000) / 10000;
    }

    public static float roundFloat5(float fvalue) {
        return (float) Math.round(fvalue * 100000) / 100000;
    }

    public static double roundDouble3(double invalue) {
        return (double) Math.round(invalue * 1000) / 1000;
    }

    public static double roundDouble2(double invalue) {
        return (double) Math.round(invalue * 100) / 100;
    }

    public static double roundDouble4(double invalue) {
        return (double) Math.round(invalue * 10000) / 10000;
    }

    public static double roundDouble5(double invalue) {
        return (double) Math.round(invalue * 100000) / 100000;
    }

    public static float[] matrix2Array(float[][] m) {
        if (m == null || m.length == 0 || m[0].length == 0) {
            return null;
        }
        int h = m.length;
        int w = m[0].length;
        float[] a = new float[w * h];
        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                a[j * w + i] = m[j][i];
            }
        }
        return a;
    }

    public static float[][] array2Matrix(float[] a, int w) {
        if (a == null || a.length == 0 || w < 1) {
            return null;
        }
        int h = a.length / w;
        if (h < 1) {
            return null;
        }
        float[][] m = new float[h][w];
        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                m[j][i] = a[j * w + i];
            }
        }
        return m;
    }

    public static long getAvaliableMemory() {
        Runtime r = Runtime.getRuntime();
        return r.maxMemory() - (r.totalMemory() - r.freeMemory());
    }

    public static long getAvaliableMemoryMB() {
        return getAvaliableMemory() / (1024 * 1024);
    }

    public static void sortList(List<Integer> numbers) {
        Collections.sort(numbers, new Comparator<Integer>() {
            @Override
            public int compare(Integer p1, Integer p2) {
                return p1 - p2;
            }
        });
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

    public static double[] sortArray(double[] numbers) {
        List<Double> list = new ArrayList<>();
        for (double i : numbers) {
            list.add(i);
        }
        Collections.sort(list, new Comparator<Double>() {
            @Override
            public int compare(Double p1, Double p2) {
                return (int) (p1 - p2);
            }
        });
        double[] sorted = new double[numbers.length];
        for (int i = 0; i < list.size(); i++) {
            sorted[i] = list.get(i);
        }
        return sorted;
    }

    public static int mapInt(int value, IndexRange originalRange, IndexRange newlRange) {
        if (originalRange == null || newlRange == null
                || originalRange.getStart() > value || originalRange.getEnd() < value) {
            return value;
        }
        int len = value - originalRange.getStart() + 1;
        double ratio = newlRange.getLength() * 1.0 / originalRange.getLength();
        return newlRange.getStart() + (int) Math.round(len * ratio);
    }

    public static int zipInt(int value, int zipStep) {
        return (int) (Math.round((value + zipStep / 2) / zipStep)) * zipStep;
    }

}
