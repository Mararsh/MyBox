package mara.mybox.color;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2019-5-21 12:26:36
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ColorConversion {

    public enum SpaceType {
        RGB, CIE, CMYK, Others
    }

    public enum RangeType {
        Normalized, RGB, Hundred
    }

    public static BufferedImage sRGB(BufferedImage source, int colorSpace) {
        ICC_ColorSpace iccColorSpace = new ICC_ColorSpace(ICC_Profile.getInstance(ColorSpace.CS_sRGB));
        ColorConvertOp converter = new ColorConvertOp(iccColorSpace, null);
        BufferedImage target = converter.filter(source, null);
        return target;
    }

    public static BufferedImage convertColorSpace(BufferedImage source, int colorSpace) {
        ICC_Profile profile = ICC_Profile.getInstance(colorSpace);
        ICC_ColorSpace iccColorSpace = new ICC_ColorSpace(profile);
        ColorConvertOp converter = new ColorConvertOp(iccColorSpace, null);
        BufferedImage target = converter.filter(source, null);
        return target;
    }

    public static BufferedImage convertColorSpace(BufferedImage source, String iccFile) {
        try {
            ICC_Profile profile = ICC_Profile.getInstance(iccFile);
            ICC_ColorSpace iccColorSpace = new ICC_ColorSpace(profile);
            ColorConvertOp converter = new ColorConvertOp(iccColorSpace, null);
            BufferedImage target = converter.filter(source, null);
            return target;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static float[] fromSRGB(int colorSpace, float[] srgb) {
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        float[] xyz = cs.toCIEXYZ(srgb);
        cs = ColorSpace.getInstance(colorSpace);
        float[] rgb = cs.fromCIEXYZ(xyz);
        return rgb;
    }

    public static float[] toSRGB(int colorSpace, float[] rgb) {
        ColorSpace cs = ColorSpace.getInstance(colorSpace);
        float[] xyz = cs.toCIEXYZ(rgb);
        cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        float[] srgb = cs.fromCIEXYZ(xyz);
        return srgb;
    }

    public static float[] SRGBtoXYZ(float[] srgb) {
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        float[] xyz = cs.toCIEXYZ(srgb);
        return xyz;
    }

    public static float[] XYZtoSRGB(float[] xyz) {
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        float[] srgb = cs.fromCIEXYZ(xyz);
        return srgb;
    }

    // http://what-when-how.com/introduction-to-video-and-image-processing/conversion-between-rgb-and-yuvycbcr-introduction-to-video-and-image-processing
    /*
        YUV
        Y: 0~255    U: -111~111     V: -157~157
     */
    public static float[] RGBtoYUV(float[] rgb) {
        float[] yuv = new float[3];
        yuv[0] = 0.299f * rgb[0] + 0.587f * rgb[1] + 0.114f * rgb[2];
        yuv[1] = 0.492f * (rgb[2] - yuv[0]);
        yuv[2] = 0.877f * (rgb[0] - yuv[0]);
        return yuv;
    }

    public static float[] YUVtoRGB(float[] yuv) {
        float[] rgb = new float[3];
        rgb[0] = yuv[0] + 1.140f * yuv[2];
        rgb[1] = yuv[0] - 0.395f * yuv[1] - 0.581f * yuv[2];
        rgb[2] = yuv[0] + 2.032f * yuv[1];
        return rgb;
    }

    public static double[] RGBtoYUV(double[] rgb) {
        double[] yuv = new double[3];
        yuv[0] = 0.299 * rgb[0] + 0.587 * rgb[1] + 0.114 * rgb[2];
        yuv[1] = 0.492 * (rgb[2] - yuv[0]);
        yuv[2] = 0.877 * (rgb[0] - yuv[0]);
        return yuv;
    }

    public static double[] YUVtoRGB(double[] yuv) {
        double[] rgb = new double[3];
        rgb[0] = yuv[0] + 1.140 * yuv[2];
        rgb[1] = yuv[0] - 0.395 * yuv[1] - 0.581 * yuv[2];
        rgb[2] = yuv[0] + 2.032 * yuv[1];
        return rgb;
    }

    // https://wolfcrow.com/whats-the-difference-between-yuv-yiq-ypbpr-and-ycbcr/
    /*
        YCbCr
        Y: 0~255    Cb: 0~255     Cr: 0~255
     */
    public static float[] RGBtoYCrCb(float[] rgb) {
        float[] YCbCr = new float[3];
        float offset = 128f / 255;
        YCbCr[0] = 0.299f * rgb[0] + 0.578f * rgb[1] + 0.114f * rgb[2];
        YCbCr[1] = -0.1687f * rgb[0] - 0.3313f * rgb[1] + 0.500f * rgb[2] + offset;
        YCbCr[2] = 0.500f * rgb[0] - 0.4187f * rgb[1] - 0.0813f * rgb[2] + offset;
        return YCbCr;
    }

    public static float[] YCrCbtoRGB(float[] YCbCr) {
        float[] rgb = new float[3];
        float offset = 128f / 255;
        rgb[0] = YCbCr[0] + 1.4020f * (YCbCr[2] - offset);
        rgb[1] = YCbCr[0] - 0.3441f * (YCbCr[1] - offset) - 0.7141f * (YCbCr[2] - offset);
        rgb[2] = YCbCr[0] + 1.7720f * (YCbCr[1] - offset);
        return rgb;
    }

    public static double[] RGBtoYCrCb(double[] rgb) {
        double[] YCbCr = new double[3];
        double offset = 128d / 255;
        YCbCr[0] = 0.299 * rgb[0] + 0.578 * rgb[1] + 0.114 * rgb[2];
        YCbCr[1] = -0.1687 * rgb[0] - 0.3313 * rgb[1] + 0.500 * rgb[2] + offset;
        YCbCr[2] = 0.500 * rgb[0] - 0.4187 * rgb[1] - 0.0813 * rgb[2] + offset;
        return YCbCr;
    }

    public static double[] YCrCbtoRGB(double[] YCbCr) {
        double[] rgb = new double[3];
        double offset = 128d / 255;
        rgb[0] = YCbCr[0] + 1.4020 * (YCbCr[2] - offset);
        rgb[1] = YCbCr[0] - 0.3441 * (YCbCr[1] - offset) - 0.7141 * (YCbCr[2] - offset);
        rgb[2] = YCbCr[0] + 1.7720 * (YCbCr[1] - offset);
        return rgb;
    }

    /*
         Yxy
     */
    public static float[] XYZtoYXY(float[] xyz) {
        float x = xyz[0];
        float y = xyz[1];
        float z = xyz[2];
        float[] Yxy = new float[3];
        Yxy[0] = y;
        Yxy[1] = x / (x + y + z);
        Yxy[2] = y / (x + y + z);
        return Yxy;
    }

    public static float[] YXYtoXYZ(float[] Yxy) {
        float Y = Yxy[0];
        float x = Yxy[1];
        float y = Yxy[2];
        float[] xyz = new float[3];
        xyz[0] = x * (Y / y);
        xyz[1] = Y;
        xyz[2] = (1 - x - y) * (Y / y);
        return xyz;
    }

    public static double[] XYZtoYXY(double[] xyz) {
        double x = xyz[0];
        double y = xyz[1];
        double z = xyz[2];
        double[] Yxy = new double[3];
        Yxy[0] = y;
        Yxy[1] = x / (x + y + z);
        Yxy[2] = y / (x + y + z);
        return Yxy;
    }

    public static double[] YXYtoXYZ(double[] Yxy) {
        double Y = Yxy[0];
        double x = Yxy[1];
        double y = Yxy[2];
        double[] xyz = new double[3];
        xyz[0] = x * (Y / y);
        xyz[1] = Y;
        xyz[2] = (1 - x - y) * (Y / y);
        return xyz;
    }

}
