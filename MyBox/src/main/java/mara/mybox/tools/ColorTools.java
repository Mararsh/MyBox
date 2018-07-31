package mara.mybox.tools;

import java.awt.Color;
import java.awt.color.ColorSpace;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 16:07:27
 * @Description
 * @License Apache License Version 2.0
 */
public class ColorTools {

    private static final Logger logger = LogManager.getLogger();

    public static int color2Pixel(int a, int r, int g, int b) {
        return color2Pixel(new Color(a, r, g, b));
//        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int color2Pixel(int r, int g, int b) {
        return color2Pixel(255, r, g, b);
    }

    public static int color2Pixel(Color color) {
        return adjustPixelColor(color.getRGB());
//        try {
//            int a, r, g, b;
//            a = color.getAlpha();
//            r = color.getRed();
//            g = color.getGreen();
//            b = color.getBlue();
//            return a | (r << 16) | (g << 8) | b;
//        } catch (Exception e) {
//            return -1;
//        }
    }

    /*
        加上颜色最大值就是实际颜色值
            -16777216 对应 0xff000000
            -1 对应 0xffffffff
            0xffffff 的值 16777215
     */
 /*
    new Color(255, 255, 255, 0): 16777215
    new Color(255, 255, 255, 255): -1
    Color.WHITE: -1
    new Color(0, 0, 0, 0): 0
    new Color(0, 0, 0, 255): -16777216
    Color.BLACK: -16777216
    new Color(255, 0, 0, 0): 16711680
    new Color(255, 0, 0, 255): -65536
    Color.RED: -65536
    Color.BLUE: -16776961
     */
    public static int adjustPixelColor(int pixelColor) {
//        logger.debug("new Color(255, 255, 255, 0): " + new Color(255, 255, 255, 0).getRGB());
//        logger.debug("new Color(255, 255, 255, 255): " + new Color(255, 255, 255, 255).getRGB());
//        logger.debug("Color.WHITE: " + Color.WHITE.getRGB());
//        logger.debug("new Color(0, 0, 0, 0): " + new Color(0, 0, 0, 0).getRGB());
//        logger.debug("new Color(0, 0, 0, 255): " + new Color(0, 0, 0, 255).getRGB());
//        logger.debug("Color.BLACK: " + Color.BLACK.getRGB());
//        logger.debug("new Color(255, 0, 0, 0): " + new Color(255, 0, 0, 0).getRGB());
//        logger.debug("new Color(255, 0, 0, 255): " + new Color(255, 0, 0, 255).getRGB());
//        logger.debug("Color.RED: " + Color.RED.getRGB());
//        logger.debug("Color.BLUE: " + Color.BLUE.getRGB());
        return 16777216 + pixelColor;
//        if (pixelColor > 8388608) {
//            return pixelColor - 16777216;
//        } else {
//            return pixelColor;
//        }
    }

    public static Color pixel2Color(int pixel) {
        return new Color(pixel);
//        try {
//            int a, r, g, b;
//            a = pixel & 0xff000000;
//            r = (pixel & 0xff0000) >> 16;
//            g = (pixel & 0xff00) >> 8;
//            b = (pixel & 0xff);
//            return new Color(a, r, g, b);
//        } catch (Exception e) {
//            return null;
//        }
    }

    public static int color2GrayPixel(int a, int r, int g, int b) {
        return color2GrayPixel(r, g, b);
    }

    public static int color2GrayPixel(int r, int g, int b) {
        int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);
        int pixel = color2Pixel(gray, gray, gray);
        return pixel;
    }

    public static int pixel2GrayPixel(int pixel) {
        Color c = pixel2Color(pixel);
        return color2GrayPixel(c.getRed(), c.getGreen(), c.getBlue());
    }

    public static int color2GrayValue(int r, int g, int b) {
        int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);
        return gray;
    }

    public static int pixel2GrayValue(int pixel) {
        Color c = pixel2Color(pixel);
        return color2GrayValue(c.getRed(), c.getGreen(), c.getBlue());
    }

    public static int grayPixel2GrayValue(int pixel) {
        Color c = pixel2Color(pixel);
        return c.getRed();
    }

    public static String getColorSpaceName(int colorType) {
        switch (colorType) {
            case ColorSpace.TYPE_XYZ:
                return "XYZ";
            case ColorSpace.TYPE_Lab:
                return "Lab";
            case ColorSpace.TYPE_Luv:
                return "Luv";
            case ColorSpace.TYPE_YCbCr:
                return "YCbCr";
            case ColorSpace.TYPE_Yxy:
                return "Yxy";
            case ColorSpace.TYPE_RGB:
                return "RGB";
            case ColorSpace.TYPE_GRAY:
                return "GRAY";
            case ColorSpace.TYPE_HSV:
                return "HSV";
            case ColorSpace.TYPE_HLS:
                return "HLS";
            case ColorSpace.TYPE_CMYK:
                return "CMYK";
            case ColorSpace.TYPE_CMY:
                return "CMY";
            case ColorSpace.TYPE_2CLR:
                return "2CLR";
            case ColorSpace.TYPE_3CLR:
                return "3CLR";
            case ColorSpace.TYPE_4CLR:
                return "4CLR";
            case ColorSpace.TYPE_5CLR:
                return "5CLR";
            case ColorSpace.TYPE_6CLR:
                return "6CLR";
            case ColorSpace.TYPE_7CLR:
                return "CMY";
            case ColorSpace.TYPE_8CLR:
                return "8CLR";
            case ColorSpace.TYPE_9CLR:
                return "9CLR";
            case ColorSpace.TYPE_ACLR:
                return "ACLR";
            case ColorSpace.TYPE_BCLR:
                return "BCLR";
            case ColorSpace.TYPE_CCLR:
                return "CCLR";
            case ColorSpace.TYPE_DCLR:
                return "DCLR";
            case ColorSpace.TYPE_ECLR:
                return "ECLR";
            case ColorSpace.TYPE_FCLR:
                return "FCLR";
            case ColorSpace.CS_sRGB:
                return "sRGB";
            case ColorSpace.CS_LINEAR_RGB:
                return "LINEAR_RGB";
            case ColorSpace.CS_CIEXYZ:
                return "CIEXYZ";
            case ColorSpace.CS_PYCC:
                return "PYCC";
            case ColorSpace.CS_GRAY:
                return "GRAY";
            default:
                return "UNKOWN";

        }

    }

    // https://en.wikipedia.org/wiki/Color_difference
    public static double calculateColorDistance(Color color1, Color color2) {
        double v = 2 * Math.pow(color1.getRed() - color2.getRed(), 2)
                + 4 * Math.pow(color1.getGreen() - color2.getGreen(), 2)
                + 3 * Math.pow(color1.getBlue() - color2.getBlue(), 2);
        return Math.sqrt(v);
    }

    public static double calculateColorDistance2(Color color1, Color color2) {
        double v = 2 * Math.pow(color1.getRed() - color2.getRed(), 2)
                + 4 * Math.pow(color1.getGreen() - color2.getGreen(), 2)
                + 3 * Math.pow(color1.getBlue() - color2.getBlue(), 2);
        return v;
    }

    public static boolean isColorMatch(Color color1, Color color2, int threshold) {
        double v = 2 * Math.pow(color1.getRed() - color2.getRed(), 2)
                + 4 * Math.pow(color1.getGreen() - color2.getGreen(), 2)
                + 3 * Math.pow(color1.getBlue() - color2.getBlue(), 2);
        return calculateColorDistance2(color1, color2) <= Math.pow(threshold, 2);
    }

    public static int getHue(Color color) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        return (int) (hsb[0] * 360);
    }

    public static float getSaturate(Color color) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        return hsb[1];
    }

    public static Color scaleSaturate(Color color, float scale) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        return Color.getHSBColor(hsb[0], hsb[1] * scale, hsb[2]);
    }
}
