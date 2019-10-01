package mara.mybox.tools;

/**
 * @Author Mara
 * @CreateDate 2019-5-28 15:31:28
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class FloatTools {

    public static float roundFloat2(float fvalue) {
        return (float) DoubleTools.scale(fvalue, 2);
    }

    public static float roundFloat5(float fvalue) {
        return (float) DoubleTools.scale(fvalue, 5);
    }

    public static double[] toDouble(float[] f) {
        if (f == null) {
            return null;
        }
        double[] d = new double[f.length];
        for (int i = 0; i < f.length; i++) {
            d[i] = f[i];
        }
        return d;
    }

    public static double[][] toDouble(float[][] f) {
        if (f == null) {
            return null;
        }
        double[][] d = new double[f.length][f[0].length];
        for (int i = 0; i < f.length; i++) {
            for (int j = 0; j < f[i].length; j++) {
                d[i][j] = f[i][j];
            }
        }
        return d;
    }
}
