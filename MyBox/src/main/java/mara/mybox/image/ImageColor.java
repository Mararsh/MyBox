package mara.mybox.image;

import java.awt.Color;
import java.awt.color.ColorSpace;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 16:07:27
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageColor {

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
        return RGB2Pixel(gray, gray, gray, a);
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

    public static int RGB2GrayValue(Color color) {
        return RGB2GrayValue(color.getRed(), color.getGreen(), color.getBlue());
    }

    public static int pixel2GrayValue(int pixel) {
        Color c = new Color(pixel);
        return RGB2GrayValue(c.getRed(), c.getGreen(), c.getBlue());
    }

    public static int grayPixel2GrayValue(int pixel) {
        Color c = pixel2RGB(pixel);
        return c.getRed();
    }

    public static Color RGB2Gray(Color color) {
        int gray = RGB2GrayValue(color);
        return new Color(gray, gray, gray, color.getAlpha());
    }

    public static String pixel2hex(int pixel) {
        Color c = new Color(pixel);
        return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
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

    // 0.0-1.0
    public static float getHue(Color color) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        return hsb[0];
    }

    // 0.0-1.0
    public static float getSaturation(Color color) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        return hsb[1];
    }

    // 0.0-1.0
    public static float getBrightness(Color color) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        return hsb[2];
    }

    public static float getBrightness(int pixel) {
        Color color = new Color(pixel);
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        return hsb[2];
    }

    public static float[] getHSB(Color color) {
        return Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
    }

    public static Color HSB2RGB(float h, float s, float b) {
        return new Color(Color.HSBtoRGB(h, s, b));
    }

    // https://en.wikipedia.org/wiki/Color_difference
    public static int calculateColorDistance(Color color1, Color color2) {
        int v = calculateColorDistance2(color1, color2);
        return (int) Math.round(Math.sqrt(v));
    }

    public static int calculateColorDistance2(Color color1, Color color2) {
        int redDiff = color1.getRed() - color2.getRed();
        int greenDiff = color1.getGreen() - color2.getGreen();
        int blueDiff = color1.getBlue() - color2.getBlue();
        int v = (int) Math.round(2 * redDiff * redDiff + 4 * greenDiff * greenDiff + 3 * blueDiff * blueDiff);
        return v;
    }

    // distance2 = Math.pow(distance, 2)
    public static boolean isColorMatch2(Color color1, Color color2, int distance2) {
        if (color1.equals(color2)) {
            return true;
        } else if (distance2 == 0) {
            return false;
        }
        return calculateColorDistance2(color1, color2) <= distance2;
    }

    // distance: 0.0-1.0
    public static boolean isHueMatch(Color color1, Color color2, float distance) {
        return Math.abs(getHue(color1) - getHue(color2)) <= distance;
    }

    // distance: 0.0-1.0
    public static boolean isBrightnessMatch(Color color1, Color color2, float distance) {
        return Math.abs(getBrightness(color1) - getBrightness(color2)) <= distance;
    }

    // distance: 0.0-1.0
    public static boolean isSaturationMatch(Color color1, Color color2, float distance) {
        return Math.abs(getSaturation(color1) - getSaturation(color2)) <= distance;
    }

    // distance: 0-255
    public static boolean isRedMatch(Color color1, Color color2, int distance) {
        return Math.abs(color1.getRed() - color2.getRed()) <= distance;
    }

    // distance: 0-255
    public static boolean isGreenMatch(Color color1, Color color2, int distance) {
        return Math.abs(color1.getGreen() - color2.getGreen()) <= distance;
    }

    // distance: 0-255
    public static boolean isBlueMatch(Color color1, Color color2, int distance) {
        return Math.abs(color1.getBlue() - color2.getBlue()) <= distance;
    }

    // distance: 0-100
    public static boolean matchHue(Color color1, Color color2,
            int distance, boolean excluded) {
        boolean isMatch = ImageColor.isHueMatch(color1, color2, distance);
        if (!excluded) {
            return isMatch;
        } else {
            return !isMatch;
        }
    }

    // distance: 0.0-1.0
    public static boolean matchHue(Color color1, Color color2,
            float distance, boolean excluded) {
        boolean isMatch = ImageColor.isHueMatch(color1, color2, distance);
        if (!excluded) {
            return isMatch;
        } else {
            return !isMatch;
        }
    }

    public static Color scaleSaturate(Color color, float scale) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        return Color.getHSBColor(hsb[0], hsb[1] * scale, hsb[2]);
    }

    // https://stackoverflow.com/questions/21899824/java-convert-a-greyscale-and-sepia-version-of-an-image-with-bufferedimage/21900125#21900125
    public static Color pixel2Sepia(int pixel, int sepiaIntensity) {
        return pixel2Sepia(pixel2RGB(pixel), sepiaIntensity);
    }

    public static Color pixel2Sepia(Color color, int sepiaIntensity) {
        int sepiaDepth = 20;
        int gray = RGB2GrayValue(color);
        int r = gray, g = gray, b = gray;
        r = Math.min(r + (sepiaDepth * 2), 255);
        g = Math.min(g + sepiaDepth, 255);
        b = Math.min(Math.max(b - sepiaIntensity, 0), 255);
        Color newColor = new Color(r, g, b, color.getAlpha());
        return newColor;
    }

    public static Color thresholdingColor(Color inColor,
            int threshold, int smallValue, int bigValue) {
        int red, green, blue;
        if (inColor.getRed() < threshold) {
            red = smallValue;
        } else {
            red = bigValue;
        }
        if (inColor.getGreen() < threshold) {
            green = smallValue;
        } else {
            green = bigValue;
        }
        if (inColor.getBlue() < threshold) {
            blue = smallValue;
        } else {
            blue = bigValue;
        }
        Color newColor = new Color(red, green, blue, inColor.getAlpha());
        return newColor;
    }

    public static javafx.scene.paint.Color converColor(Color color) {
        return new javafx.scene.paint.Color(
                color.getRed() / 255.0, color.getGreen() / 255.0,
                color.getBlue() / 255.0, color.getAlpha() / 255.0);
    }

    public static Color converColor(javafx.scene.paint.Color color) {
        return new Color(
                (int) (color.getRed() * 255), (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255), (int) (color.getOpacity() * 255));
    }

    // https://en.wikipedia.org/wiki/YCbCr
    public static int[] rgb2YCbCr(int r, int g, int b) {
        int YCbCr[] = new int[3];
        YCbCr[0] = Math.round(0.299f * r + 0.587f * g + 0.114f * b);
        YCbCr[0] = Math.max(Math.min(YCbCr[0], 255), 0);
        YCbCr[1] = Math.round(128 - 0.168736f * r + 0.331264f * g + 0.5f * b);
        YCbCr[1] = Math.max(Math.min(YCbCr[1], 255), 0);
        YCbCr[2] = Math.round(128 + 0.5f * r + 0.418688f * g + 0.081312f * b);
        YCbCr[2] = Math.max(Math.min(YCbCr[2], 255), 0);
        return YCbCr;
    }

    public static int[] rgb2YCbCr(Color color) {
        return rgb2YCbCr(color.getRed(), color.getGreen(), color.getBlue());
    }

    public static int[] pixel2YCbCr(int pixel) {
        return rgb2YCbCr(new Color(pixel));
    }

    public static int getLuma(Color color) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int Y = Math.round(0.299f * r + 0.587f * g + 0.114f * b);
        return Math.max(Math.min(Y, 255), 0);
    }

    public static Color YCbCr2rgb(int Y, int Cb, int Cr) {
        int r = Math.round(Y + 1.402f * (Cr - 128));
        r = Math.max(Math.min(r, 255), 0);
        int g = Math.round(Y - 0.344136f * (Cb - 128) - 0.714136f * (Cr - 128));
        g = Math.max(Math.min(g, 255), 0);
        int b = Math.round(Y + 1.772f * (Cb - 128));
        b = Math.max(Math.min(b, 255), 0);
        return new Color(r, g, b);
    }

    public static int YCbCr2pixel(int Y, int Cb, int Cr) {
        return YCbCr2rgb(Y, Cb, Cr).getRGB();
    }

}
