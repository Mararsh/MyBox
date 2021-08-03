package mara.mybox.color;

import static mara.mybox.color.RGBColorSpace.gamma22;
import static mara.mybox.color.RGBColorSpace.linear22;

/**
 * @Author Mara
 * @CreateDate 2019-5-21 12:14:18
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class AdobeRGB {

    public static double linearAdobeRGB(double v) {
        return linear22(v);
    }

    public static double gammaAdobeRGB(double v) {
        return gamma22(v);
    }

    public static double[] gammaAdobeRGB(double[] linearRGB) {
        double[] rgb = new double[3];
        rgb[0] = gamma22(linearRGB[0]);
        rgb[1] = gamma22(linearRGB[1]);
        rgb[2] = gamma22(linearRGB[2]);
        return rgb;
    }

    public static double[] AdobeRGBtoXYZ(double[] adobergb) {
        double linearRed = linearAdobeRGB(adobergb[0]);
        double linearGreen = linearAdobeRGB(adobergb[1]);
        double linearBlue = linearAdobeRGB(adobergb[2]);

        double[] xyz = new double[3];
        xyz[0] = 0.5767309 * linearRed + 0.1855540 * linearGreen + 0.1881852 * linearBlue;
        xyz[1] = 0.2973769 * linearRed + 0.6273491 * linearGreen + 0.0752741 * linearBlue;
        xyz[2] = 0.0270343 * linearRed + 0.0706872 * linearGreen + 0.9911085 * linearBlue;
        return xyz;
    }

    public static double[] AdobeRGBtoSRGB(double[] adobergb) {
        double[] xyz = AdobeRGBtoXYZ(adobergb);
        return CIEColorSpace.XYZd65toSRGBd65(xyz);
    }

}
