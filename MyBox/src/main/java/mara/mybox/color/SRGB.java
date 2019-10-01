package mara.mybox.color;

import java.awt.Color;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import static mara.mybox.color.AppleRGB.XYZtoAppleRGB;
import static mara.mybox.color.RGBColorSpace.linearSRGB;
import mara.mybox.fxml.FxmlColor;
import mara.mybox.image.ImageColor;
import mara.mybox.image.ImageValue;
import mara.mybox.tools.MatrixTools;

/**
 * @Author Mara
 * @CreateDate 2019-5-21 12:07:33
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class SRGB {

    protected String colorValue, colorName, colorDisplay;

    /*
        static methods
     */
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
        double[] xyz = SRGBd65toXYZd65(srgb);
        return CIEColorSpace.XYZd65toAdobeRGBd65(xyz);
    }

    public static double[] SRGBtoAppleRGB(double[] srgb) {
        double[] xyz = SRGBd65toXYZd65(srgb);
        return XYZtoAppleRGB(xyz);
    }

    public static float[] srgb2profile(ICC_Profile profile, Color color) {
        return srgb2profile(profile, ImageColor.toFloat(color));
    }

    public static float[] srgb2profile(ICC_Profile profile, javafx.scene.paint.Color color) {
        return srgb2profile(profile, FxmlColor.toFloat(color));
    }

    public static float[] srgb2profile(ICC_Profile profile, float[] srgb) {
        if (profile == null) {
            profile = ImageValue.eciCmykProfile();
        }
        ICC_ColorSpace colorSpace = new ICC_ColorSpace(profile);
        return colorSpace.fromRGB(srgb);
    }

    // http://www.easyrgb.com/en/math.php
    public static double[] rgb2cmy(javafx.scene.paint.Color color) {
        double[] cmy = new double[3];
        cmy[0] = 1 - color.getRed();
        cmy[1] = 1 - color.getGreen();
        cmy[2] = 1 - color.getBlue();
        return cmy;
    }

    public static double[] rgb2cmy(Color color) {
        double[] cmy = new double[3];
        cmy[0] = 1 - color.getRed() / 255d;
        cmy[1] = 1 - color.getGreen() / 255d;
        cmy[2] = 1 - color.getBlue() / 255d;
        return cmy;
    }

    public static double[] rgb2cmyk(javafx.scene.paint.Color color) {
        return CMYKColorSpace.cmy2cmky(rgb2cmy(color));
    }

    public static double[] rgb2cmyk(Color color) {
        return CMYKColorSpace.cmy2cmky(rgb2cmy(color));
    }

    /*
        get/set
     */
    public String getColorValue() {
        return colorValue;
    }

    public void setColorValue(String colorValue) {
        this.colorValue = colorValue;
    }

    public String getColorName() {
        return colorName;
    }

    public void setColorName(String colorName) {
        this.colorName = colorName;
    }

    public String getColorDisplay() {
        return colorDisplay;
    }

    public void setColorDisplay(String colorDisplay) {
        this.colorDisplay = colorDisplay;
    }

}
