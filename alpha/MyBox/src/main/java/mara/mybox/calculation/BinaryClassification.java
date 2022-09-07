package mara.mybox.calculation;

/**
 * @Author Mara
 * @CreateDate 2022-9-7
 * @License Apache License Version 2.0
 */
public class BinaryClassification {

    public static double sigMoid(double d) {
        return 1 / (1 + Math.pow(Math.E, -d));
    }

    public static double sigMoidDerivative(double d) {
        return sigMoid(d) * (1 - sigMoid(d));
    }

}
