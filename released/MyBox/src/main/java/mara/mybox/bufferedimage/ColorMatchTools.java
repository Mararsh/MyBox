package mara.mybox.bufferedimage;

import java.awt.Color;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 16:07:27
 * @License Apache License Version 2.0
 */
public class ColorMatchTools {

    // distanceSquare = Math.pow(distance, 2)
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

    // Generally not use this value. Use distance square instead.
    public static int calculateColorDistance(Color color1, Color color2) {
        if (color1 == null || color2 == null) {
            return Integer.MAX_VALUE;
        }
        int v = calculateColorDistanceSquare(color1, color2);
        return (int) Math.round(Math.sqrt(v));
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

    // distance: 0.0-1.0
    public static boolean isHueMatch(Color color1, Color color2, float distance) {
        if (color1 == null || color2 == null) {
            return false;
        }
        return Math.abs(ColorConvertTools.getHue(color1) - ColorConvertTools.getHue(color2)) <= distance;
    }

    // distance: 0.0-1.0
    public static boolean isSaturationMatch(Color color1, Color color2, float distance) {
        if (color1 == null || color2 == null) {
            return false;
        }
        return Math.abs(ColorConvertTools.getSaturation(color1) - ColorConvertTools.getSaturation(color2)) <= distance;
    }

    // distance: 0.0-1.0
    public static boolean isBrightnessMatch(Color color1, Color color2, float distance) {
        if (color1 == null || color2 == null) {
            return false;
        }
        return Math.abs(ColorConvertTools.getBrightness(color1) - ColorConvertTools.getBrightness(color2)) <= distance;
    }

}
