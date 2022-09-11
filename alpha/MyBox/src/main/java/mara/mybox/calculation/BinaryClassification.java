package mara.mybox.calculation;

/**
 * @Author Mara
 * @CreateDate 2022-9-7
 * @License Apache License Version 2.0
 */
public class BinaryClassification {

    public static double sigMoid(double x) {
        return 1 / (1 + Math.exp(-x));
    }

    public static double sigMoidDerivative(double x) {
        return sigMoid(x) * (1 - sigMoid(x));
    }

}
