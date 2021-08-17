package mara.mybox.bufferedimage;

import java.awt.Color;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 16:07:27
 * @License Apache License Version 2.0
 */
public class ColorConvertTools {

    /*
         rgba
     */
    public static Color pixel2rgba(int pixel) {
        return new Color(pixel, true);
    }

    public static Color rgba2color(String rgba) {
        javafx.scene.paint.Color c = javafx.scene.paint.Color.web(rgba);
        return converColor(c);
    }

    public static int rgba2Pixel(int r, int g, int b, int a) {
        return color2Pixel(new Color(r, g, b, a));
    }

    public static int color2Pixel(Color color) {
        if (color == null) {
            return 0;
        }
        return color.getRGB();
    }

    public static int setAlpha(int pixel, int a) {
        Color c = new Color(pixel);
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), a).getRGB();
    }

    /*
        rgb
     */
    public static Color pixel2rgb(int pixel) {
        return new Color(pixel);
    }

    public static int rgb2Pixel(int r, int g, int b) {
        return rgba2Pixel(r, g, b, 255);
    }

    /*
        hsb
     */
    public static float[] color2hsb(Color color) {
        if (color == null) {
            return null;
        }
        return Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
    }

    public static float[] pixel2hsb(int pixel) {
        Color rgb = pixel2rgba(pixel);
        return Color.RGBtoHSB(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), null);
    }

    public static Color hsb2rgb(float h, float s, float b) {
        return new Color(Color.HSBtoRGB(h, s, b));
    }

    // 0.0-1.0
    public static float getHue(Color color) {
        if (color == null) {
            return 0;
        }
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        return hsb[0];
    }

    // 0.0-1.0
    public static float getBrightness(Color color) {
        if (color == null) {
            return 0;
        }
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        return hsb[2];
    }

    public static float getBrightness(int pixel) {
        Color color = new Color(pixel);
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        return hsb[2];
    }

    // 0.0-1.0
    public static float getSaturation(Color color) {
        if (color == null) {
            return 0;
        }
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        return hsb[1];
    }


    /*
        grey
     */
    public static Color color2gray(Color color) {
        if (color == null) {
            return null;
        }
        int gray = color2grayValue(color);
        return new Color(gray, gray, gray, color.getAlpha());
    }

    public static int color2grayValue(Color color) {
        if (color == null) {
            return 0;
        }
        return rgb2grayValue(color.getRed(), color.getGreen(), color.getBlue());
    }

    public static int rgba2grayPixel(int r, int g, int b, int a) {
        int gray = rgb2grayValue(r, g, b);
        return rgba2Pixel(gray, gray, gray, a);
    }

    // https://en.wikipedia.org/wiki/HSL_and_HSV#Lightness
    // https://en.wikipedia.org/wiki/Grayscale
    // Simplest：I =  ( R + G + B )  /  3
    // PAL和NTSC(Video) Y'UV and Y'IQ primaries Rec.601 : Y ′ = 0.299 R ′ + 0.587 G ′ + 0.114 B ′
    // HDTV(High Definiton TV) ITU-R primaries Rec.709:   Y ′ = 0.2126 R ′ + 0.7152 G ′ + 0.0722 B ′
    // JDK internal: javafx.scene.paint.Color.grayscale() = 0.21 * red + 0.71 * green + 0.07 * blue
    public static int rgb2grayValue(int r, int g, int b) {
        int gray = (2126 * r + 7152 * g + 722 * b) / 10000;
        return gray;
    }

    public static int pixel2GrayPixel(int pixel) {
        Color c = pixel2rgb(pixel);
        return rgba2grayPixel(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
    }

    public static int pixel2grayValue(int pixel) {
        Color c = new Color(pixel);
        return rgb2grayValue(c.getRed(), c.getGreen(), c.getBlue());
    }

    public static int grayPixel2grayValue(int pixel) {
        Color c = pixel2rgb(pixel);
        return c.getRed();
    }


    /*
        others
     */
    public static javafx.scene.paint.Color converColor(Color color) {
        return new javafx.scene.paint.Color(color.getRed() / 255.0, color.getGreen() / 255.0, color.getBlue() / 255.0, color.getAlpha() / 255.0);
    }

    public static Color converColor(javafx.scene.paint.Color color) {
        return new Color((int) (color.getRed() * 255), (int) (color.getGreen() * 255), (int) (color.getBlue() * 255), (int) (color.getOpacity() * 255));
    }

    // https://stackoverflow.com/questions/21899824/java-convert-a-greyscale-and-sepia-version-of-an-image-with-bufferedimage/21900125#21900125
    public static Color pixel2Sepia(int pixel, int sepiaIntensity) {
        return pixel2Sepia(pixel2rgb(pixel), sepiaIntensity);
    }

    public static Color pixel2Sepia(Color color, int sepiaIntensity) {
        int sepiaDepth = 20;
        int gray = color2grayValue(color);
        int r = gray;
        int g = gray;
        int b = gray;
        r = Math.min(r + (sepiaDepth * 2), 255);
        g = Math.min(g + sepiaDepth, 255);
        b = Math.min(Math.max(b - sepiaIntensity, 0), 255);
        Color newColor = new Color(r, g, b, color.getAlpha());
        return newColor;
    }

    public static String pixel2hex(int pixel) {
        Color c = new Color(pixel);
        return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }

    public static float[] color2srgb(Color color) {
        if (color == null) {
            return null;
        }
        float[] srgb = new float[3];
        srgb[0] = color.getRed() / 255.0F;
        srgb[1] = color.getGreen() / 255.0F;
        srgb[2] = color.getBlue() / 255.0F;
        return srgb;
    }

    public static Color getAlphaColor() {
        return converColor(UserConfig.getAlphaColor());
    }

    public static Color thresholdingColor(Color inColor, int threshold, int smallValue, int bigValue) {
        int red;
        int green;
        int blue;
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

    public static Color scaleSaturate(Color color, float scale) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        return Color.getHSBColor(hsb[0], hsb[1] * scale, hsb[2]);
    }

}
