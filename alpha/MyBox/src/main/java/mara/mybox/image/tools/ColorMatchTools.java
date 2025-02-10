package mara.mybox.image.tools;

import java.awt.Color;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 16:07:27
 * @License Apache License Version 2.0
 */
public class ColorMatchTools {

    // distanceSquare = Math.pow(distance, 2)
    public static boolean isColorMatchSquare2(Color color1, Color color2, int distanceSquare) {
        if (color1 == null || color2 == null) {
            return false;
        }
        if (color1.getRGB() == color2.getRGB()) {
            return true;
        } else if (distanceSquare == 0 || color1.getRGB() == 0 || color2.getRGB() == 0) {
            return false;
        }
        return calculateColorDistanceSquare2(color1, color2) <= distanceSquare;
    }

    // https://en.wikipedia.org/wiki/Color_difference
    public static int calculateColorDistanceSquare2(Color color1, Color color2) {
        if (color1 == null || color2 == null) {
            return Integer.MAX_VALUE;
        }
        int redDiff = color1.getRed() - color2.getRed();
        int greenDiff = color1.getGreen() - color2.getGreen();
        int blueDiff = color1.getBlue() - color2.getBlue();
        return 2 * redDiff * redDiff + 4 * greenDiff * greenDiff + 3 * blueDiff * blueDiff;
    }

    // Generally not use this value. Use distance square instead.
    public static int calculateColorDistance2(Color color1, Color color2) {
        if (color1 == null || color2 == null) {
            return Integer.MAX_VALUE;
        }
        int v = calculateColorDistanceSquare2(color1, color2);
        return (int) Math.round(Math.sqrt(v));
    }

    // distance: 0-255
}
