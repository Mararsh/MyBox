package mara.mybox.color;

import static mara.mybox.color.RGBColorSpace.gamma18;
import static mara.mybox.color.RGBColorSpace.linear18;

/**
 * @Author Mara
 * @CreateDate 2019-5-21 12:17:46
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class AppleRGB {

    public static double linearAppleRGB(double v) {
        return linear18(v);
    }

    public static double gammaAppleRGB(double v) {
        return gamma18(v);
    }

    public static double[] gammaAppleRGB(double[] linearRGB) {
        double[] rgb = new double[3];
        rgb[0] = gamma18(linearRGB[0]);
        rgb[1] = gamma18(linearRGB[1]);
        rgb[2] = gamma18(linearRGB[2]);
        return rgb;
    }

    public static double[] AppleRGBtoXYZ(double[] applergb) {
        double linearRed = linearAppleRGB(applergb[0]);
        double linearGreen = linearAppleRGB(applergb[1]);
        double linearBlue = linearAppleRGB(applergb[2]);

        double[] xyz = new double[3];
        xyz[0] = 0.4497288 * linearRed + 0.3162486 * linearGreen + 0.1844926 * linearBlue;
        xyz[1] = 0.2446525 * linearRed + 0.6720283 * linearGreen + 0.0833192 * linearBlue;
        xyz[2] = 0.0251848 * linearRed + 0.1411824 * linearGreen + 0.9224628 * linearBlue;
        return xyz;
    }

    public static double[] XYZtoAppleRGB(double[] xyz) {
        double linearRed = 2.9515373 * xyz[0] - 1.2894116 * xyz[1] - 0.4738445 * xyz[2];
        double linearGreen = -1.0851093 * xyz[0] + 1.9908566 * xyz[1] + 0.0372026 * xyz[2];
        double linearBlue = 0.0854934 * xyz[0] - 0.2694964 * xyz[1] + 1.0912975 * xyz[2];

        double[] applergb = new double[3];
        applergb[0] = gammaAppleRGB(linearRed);
        applergb[1] = gammaAppleRGB(linearGreen);
        applergb[2] = gammaAppleRGB(linearBlue);
        return applergb;
    }

    public static double[] AppleRGBtoSRGB(double[] applergb) {
        double[] xyz = AppleRGBtoXYZ(applergb);
        return CIEColorSpace.XYZd65toSRGBd65(xyz);
    }

}
