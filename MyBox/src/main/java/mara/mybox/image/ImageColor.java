package mara.mybox.image;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 16:07:27
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageColor {

    private int index, red, green, blue, alpha = 255;

    public ImageColor(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public ImageColor(int red, int green, int blue, int alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    public ImageColor(int index, int red, int green, int blue, int alpha) {
        this.index = index;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    @Override
    public String toString() {
        return message("Red") + ": " + red
                + message("Green") + ": " + green
                + message("Blue") + ": " + blue
                + message("Alpha") + ": " + alpha;
    }

    /*
        Static Data/Methods
     */
    public static enum ColorComponent {
        Gray, RedChannel, GreenChannel, BlueChannel, Hue, Saturation, Brightness,
        AlphaChannel
    }

    public static Map<ColorComponent, Color> ComponentColor;

    public static Color color(ColorComponent c) {
        if (ComponentColor == null) {
            ComponentColor = new HashMap<>();
            ComponentColor.put(ColorComponent.RedChannel, Color.RED);
            ComponentColor.put(ColorComponent.GreenChannel, Color.GREEN);
            ComponentColor.put(ColorComponent.BlueChannel, Color.BLUE);
            ComponentColor.put(ColorComponent.Hue, Color.PINK);
            ComponentColor.put(ColorComponent.Brightness, Color.ORANGE);
            ComponentColor.put(ColorComponent.Saturation, Color.CYAN);
            ComponentColor.put(ColorComponent.AlphaChannel, Color.YELLOW);
            ComponentColor.put(ColorComponent.Gray, Color.GRAY);
        }
        if (c == null) {
            return null;
        }
        return ComponentColor.get(c);
    }

    public static Color componentColor(String name) {
        return color(component(name));
    }

    public static ColorComponent component(String name) {
        for (ColorComponent c : ColorComponent.values()) {
            if (c.name().equals(name) || message(c.name()).equals(name)) {
                return c;
            }
        }
        return null;
    }

    public static Color getAlphaColor() {
        return ImageColor.converColor(AppVariables.getAlphaColor());
    }

    public static int RGB2Pixel(int r, int g, int b, int a) {
        return RGB2Pixel(new Color(r, g, b, a));
    }

    public static int RGB2Pixel(int r, int g, int b) {
        return RGB2Pixel(r, g, b, 255);
    }

    public static int RGB2Pixel(Color color) {
        if (color == null) {
            return 0;
        }
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
        if (color == null) {
            return 0;
        }
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
        if (color == null) {
            return null;
        }
        int gray = RGB2GrayValue(color);
        return new Color(gray, gray, gray, color.getAlpha());
    }

    public static String pixel2hex(int pixel) {
        Color c = new Color(pixel);
        return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
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
    public static float getSaturation(Color color) {
        if (color == null) {
            return 0;
        }
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        return hsb[1];
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

    public static float[] getHSB(Color color) {
        if (color == null) {
            return null;
        }
        return Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
    }

    public static Color HSB2RGB(float h, float s, float b) {
        return new Color(Color.HSBtoRGB(h, s, b));
    }

    public static float[] toFloat(Color color) {
        if (color == null) {
            return null;
        }
        float[] srgb = new float[3];
        srgb[0] = color.getRed() / 255f;
        srgb[1] = color.getGreen() / 255f;
        srgb[2] = color.getBlue() / 255f;
        return srgb;
    }

    // Generally not use this value. Use distance square instead.
    public static int calculateColorDistance(Color color1, Color color2) {
        if (color1 == null || color2 == null) {
            return Integer.MAX_VALUE;
        }
        int v = calculateColorDistanceSquare(color1, color2);
        return (int) Math.round(Math.sqrt(v));
    }

    // https://en.wikipedia.org/wiki/Color_difference
    public static int calculateColorDistanceSquare(Color color1, Color color2) {
        if (color1 == null || color2 == null) {
            return Integer.MAX_VALUE;
        }
        int redDiff = color1.getRed() - color2.getRed();
        int greenDiff = color1.getGreen() - color2.getGreen();
        int blueDiff = color1.getBlue() - color2.getBlue();
        return 2 * redDiff * redDiff + 4 * greenDiff * greenDiff + 3 * blueDiff * blueDiff;
    }

    // https://www.compuphase.com/cmetric.htm
//    public static int calculateColorDistance2(Color color1, Color color2) {
//        int redDiff = color1.getRed() - color2.getRed();
//        int greenDiff = color1.getGreen() - color2.getGreen();
//        int blueDiff = color1.getBlue() - color2.getBlue();
//        int redAvg = (color1.getRed() + color2.getRed()) / 2;
//        return Math.round(((512 + redAvg) * redDiff * redDiff) >> 8
//                + 4 * greenDiff * greenDiff
//                + ((767 - redAvg) * blueDiff * blueDiff) >> 8);
//    }
    // distance2 = Math.pow(distance, 2)
    public static boolean isColorMatchSquare(Color color1, Color color2, int distanceSquare) {
        if (color1 == null || color2 == null) {
            return false;
        }
        if (color1.getRGB() == color2.getRGB()) {
            return true;
        } else if (distanceSquare == 0 || color1.getRGB() == 0 || color2.getRGB() == 0) {
            return false;
        }
        return calculateColorDistanceSquare(color1, color2) <= distanceSquare;
    }

    // distance: 0.0-1.0
    public static boolean isHueMatch(Color color1, Color color2, float distance) {
        if (color1 == null || color2 == null) {
            return false;
        }
        return Math.abs(getHue(color1) - getHue(color2)) <= distance;
    }

    // distance: 0.0-1.0
    public static boolean isBrightnessMatch(Color color1, Color color2, float distance) {
        if (color1 == null || color2 == null) {
            return false;
        }
        return Math.abs(getBrightness(color1) - getBrightness(color2)) <= distance;
    }

    // distance: 0.0-1.0
    public static boolean isSaturationMatch(Color color1, Color color2, float distance) {
        if (color1 == null || color2 == null) {
            return false;
        }
        return Math.abs(getSaturation(color1) - getSaturation(color2)) <= distance;
    }

    // distance: 0-255
    public static boolean isRedMatch(Color color1, Color color2, int distance) {
        if (color1 == null || color2 == null) {
            return false;
        }
        return Math.abs(color1.getRed() - color2.getRed()) <= distance;
    }

    // distance: 0-255
    public static boolean isGreenMatch(Color color1, Color color2, int distance) {
        if (color1 == null || color2 == null) {
            return false;
        }
        return Math.abs(color1.getGreen() - color2.getGreen()) <= distance;
    }

    // distance: 0-255
    public static boolean isBlueMatch(Color color1, Color color2, int distance) {
        if (color1 == null || color2 == null) {
            return false;
        }
        return Math.abs(color1.getBlue() - color2.getBlue()) <= distance;
    }

    // distance: 0-100
    public static boolean matchHue(Color color1, Color color2, int distance, boolean excluded) {
        if (color1 == null || color2 == null) {
            return false;
        }
        boolean isMatch = ImageColor.isHueMatch(color1, color2, distance);
        if (!excluded) {
            return isMatch;
        } else {
            return !isMatch;
        }
    }

    // distance: 0.0-1.0
    public static boolean matchHue(Color color1, Color color2, float distance, boolean excluded) {
        if (color1 == null || color2 == null) {
            return false;
        }
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

    public static int getRGB(javafx.scene.paint.Color color) {
        return ImageColor.converColor(color).getRGB();
    }

    public static int getRGB(String rgba) {
        return ImageColor.converColor(javafx.scene.paint.Color.web(rgba)).getRGB();
    }

    public static javafx.scene.paint.Color getColor(int pixel) {
        return ImageColor.converColor(new Color(pixel));
    }

    // https://www.cnblogs.com/xiaonanxia/p/9448444.html
    public static Color blendAlpha(Color color, float opocity, Color bgColor, boolean keepAlpha) {
        int red = (int) (color.getRed() * opocity + bgColor.getRed() * (1 - opocity));
        int green = (int) (color.getGreen() * opocity + bgColor.getGreen() * (1 - opocity));
        int blue = (int) (color.getBlue() * opocity + bgColor.getBlue() * (1 - opocity));
        if (keepAlpha) {
            return new Color(red, green, blue, Math.round(opocity * 255));
        } else {
            return new Color(red, green, blue);
        }
    }

    public static Color blendAlpha(Color color, int opocity, Color bgColor, boolean keepAlpha) {
        return blendAlpha(color, opocity / 255f, bgColor, keepAlpha);
    }

    public static Color blendAlpha(Color color, float opocity, Color bgColor) {
        return blendAlpha(color, opocity, bgColor, false);
    }

    public static Color blendAlpha(Color color, int opocity, Color bgColor) {
        return blendAlpha(color, opocity, bgColor, false);
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

    // Convert from 24-bit to 15-bit color
    public static int convert24BitsTo15Bits(int c) {
        int r = (c & 0xf80000) >> 19;
        int g = (c & 0xf800) >> 6;
        int b = (c & 0xf8) << 7;
        return b | g | r;
    }

    // Get red component of a 15-bit color
    public static int redOf15Bits(int x) {
        return (x & 31) << 3;
    }

    // Get green component of a 15-bit color
    public static int greenOf15Bits(int x) {
        return (x >> 2) & 0xf8;
    }

    // Get blue component of a 15-bit color
    public static int blueOf15Bits(int x) {
        return (x >> 7) & 0xf8;
    }

    public static Color ColorOf15Bits(int c) {
        return new Color(redOf15Bits(c), greenOf15Bits(c), blueOf15Bits(c));
    }

    /*
        get/set
     */
    public int getRed() {
        return red;
    }

    public void setRed(int red) {
        this.red = red;
    }

    public int getGreen() {
        return green;
    }

    public void setGreen(int green) {
        this.green = green;
    }

    public int getBlue() {
        return blue;
    }

    public void setBlue(int blue) {
        this.blue = blue;
    }

    public int getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

}
