package mara.mybox.image;

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
public class ImageColorTools {

    private static final Logger logger = LogManager.getLogger();

    public static int RGB2Pixel(int r, int g, int b, int a) {
        return RGB2Pixel(new Color(r, g, b, a));
    }

    public static int RGB2Pixel(int r, int g, int b) {
        return RGB2Pixel(r, g, b, 255);
    }

    public static int RGB2Pixel(Color color) {
        return color.getRGB();
    }

    public static Color pixel2RGB(int pixel) {
        return new Color(pixel);
    }

    public static float[] pixel2HSB(int pixel) {
        Color rgb = pixel2RGB(pixel);
        return Color.RGBtoHSB(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), null);
    }

    public static int RGB2GrayPixel(int r, int g, int b, int a) {
        int gray = RGB2GrayValue(r, g, b);
        return ImageColorTools.RGB2Pixel(gray, gray, gray, a);
    }

    public static int pixel2GrayPixel(int pixel) {
        Color c = pixel2RGB(pixel);
        return RGB2GrayPixel(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
    }

    // https://en.wikipedia.org/wiki/HSL_and_HSV#Lightness
    // https://en.wikipedia.org/wiki/Grayscale
    // Simplest：I =  ( R + G + B )  /  3
    // PAL和NTSC(Video) Y'UV and Y'IQ primaries Rec.601 : Y ′ = 0.299 R ′ + 0.587 G ′ + 0.114 B ′
    // HDTV(High Definiton TV) ITU-R primaries Rec.709:   Y ′ = 0.2126 R ′ + 0.7152 G ′ + 0.0722 B ′
    // JDK internal: javafx.scene.paint.Color.grayscale() = 0.21 * red + 0.71 * green + 0.07 * blue
    public static int RGB2GrayValue(int r, int g, int b) {
        int gray = (2126 * r + 7152 * g + 722 * b) / 10000;
        return gray;
    }

    public static int pixel2GrayValue(int pixel) {
        Color c = new Color(pixel);

        return RGB2GrayValue(c.getRed(), c.getGreen(), c.getBlue());
    }

    public static int grayPixel2GrayValue(int pixel) {
        Color c = pixel2RGB(pixel);
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

    public static boolean isColorMatch(Color color1, Color color2, int distance) {
        if (color1 == color2) {
            return true;
        } else if (distance == 0) {
            return false;
        }
        return calculateColorDistance2(color1, color2) <= Math.pow(distance, 2);
    }

    public static boolean isHueMatch(Color color1, Color color2, int distance) {
        return Math.abs(getHue(color1) - getHue(color2)) <= distance;
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

    // https://stackoverflow.com/questions/21899824/java-convert-a-greyscale-and-sepia-version-of-an-image-with-bufferedimage/21900125#21900125
    public static Color pixel2Sepia(int pixel, int sepiaIntensity) {
        int sepiaDepth = 20;
        int gray = pixel2GrayValue(pixel);
        int r = gray, g = gray, b = gray;
        r = r + (sepiaDepth * 2);
        g = g + sepiaDepth;
        if (r > 255) {
            r = 255;
        }
        if (g > 255) {
            g = 255;
        }
        if (b > 255) {
            b = 255;
        }
        // Darken blue color to increase sepia effect
        b -= sepiaIntensity;
        // normalize if out of bounds
        if (b < 0) {
            b = 0;
        }
        if (b > 255) {
            b = 255;
        }
        Color color = new Color(pixel, true);
        Color newColor = new Color(r, g, b, color.getAlpha());
        return newColor;
    }

}
