package mara.mybox.tools;

/**
 * @Author Mara
 * @CreateDate 2018-6-11 17:43:53
 * @Description
 * @License Apache License Version 2.0
 */
public class ValueTools {

    public static String fillNumber(int value, int digit) {
        String v = value + "";
        for (int i = v.length(); i < digit; i++) {
            v = "0" + v;
        }
        return v;
    }

    public static float roundFloat2(float fvalue) {
        return (float) Math.round(fvalue * 100.0) / 100.0f;
    }

    public static float roundFloat3(float fvalue) {
        return (float) Math.round(fvalue * 1000.0) / 1000.0f;
    }

    public static float roundFloat5(float fvalue) {
        return (float) Math.round(fvalue * 100000.0) / 100000.0f;
    }

    public static double roundDouble3(double invalue) {
        return (double) Math.round(invalue * 1000.0) / 1000.0;
    }

    public static double roundDouble2(double invalue) {
        return (double) Math.round(invalue * 100.0) / 100.0;
    }

    public static double roundDouble4(double invalue) {
        return (double) Math.round(invalue * 10000.0) / 10000.0;
    }

    public static double roundDouble5(double invalue) {
        return (double) Math.round(invalue * 100000.0) / 100000.0;
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

}
