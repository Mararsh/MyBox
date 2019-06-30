package mara.mybox.color;

import java.awt.Color;
import static mara.mybox.color.AppleRGB.XYZtoAppleRGB;
import static mara.mybox.color.RGBColorSpace.gammaSRGB;
import static mara.mybox.color.RGBColorSpace.linearSRGB;
import mara.mybox.tools.MatrixTools;

/**
 * @Author Mara
 * @CreateDate 2019-5-21 12:07:33
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class SRGB {

    public static double[] gammaSRGB(double[] linearRGB) {
        double[] srgb = new double[3];
        srgb[0] = RGBColorSpace.gammaSRGB(linearRGB[0]);
        srgb[1] = RGBColorSpace.gammaSRGB(linearRGB[1]);
        srgb[2] = RGBColorSpace.gammaSRGB(linearRGB[2]);
        return srgb;
    }

    public static double[] SRGBtoLinearSRGB(double[] sRGB) {
        double[] linearRGB = new double[3];
        linearRGB[0] = linearSRGB(sRGB[0]);
        linearRGB[1] = linearSRGB(sRGB[1]);
        linearRGB[2] = linearSRGB(sRGB[2]);
        return linearRGB;
    }

    public static double[] SRGBd50toXYZd50(double[] srgb) {
        double[] linearRGB = SRGBtoLinearSRGB(srgb);

        double[][] matrix = {
            {0.436065673828125, 0.3851470947265625, 0.14306640625},
            {0.2224884033203125, 0.7168731689453125, 0.06060791015625},
            {0.013916015625, 0.097076416015625, 0.7140960693359375}
        };
        double[] xyz = MatrixTools.multiply(matrix, linearRGB);
        return xyz;
    }

    public static double[] toXYZd65(Color color) {
        return SRGBd65toXYZd65(ColorBase.array(color));
    }

    public static double[] SRGBd65toXYZd65(double[] srgb) {
        double[] linearRGB = SRGBtoLinearSRGB(srgb);

        double[][] matrix = {
            {0.4124564, 0.3575761, 0.1804375},
            {0.2126729, 0.7151522, 0.0721750},
            {0.0193339, 0.1191920, 0.9503041}
        };
        double[] xyz = MatrixTools.multiply(matrix, linearRGB);
        return xyz;
    }

    public static double[] toXYZd50(Color color) {
        return SRGBd65toXYZd50(ColorBase.array(color));
    }

    public static double[] SRGBd65toXYZd50(double[] srgb) {
        double[] xyzD65 = SRGBd65toXYZd65(srgb);
        return ChromaticAdaptation.D65toD50(xyzD65);
    }

    public static double[] SRGBtoAdobeRGB(double[] srgb) {
        double[] xyz = SRGB.SRGBd65toXYZd65(srgb);
        return CIEColorSpace.XYZd65toAdobeRGBd65(xyz);
    }

    public static double[] SRGBtoAppleRGB(double[] srgb) {
        double[] xyz = SRGBd65toXYZd65(srgb);
        return XYZtoAppleRGB(xyz);
    }

}
