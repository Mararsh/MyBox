package mara.mybox.fxml;

import javafx.scene.paint.Color;

/**
 * @Author Mara
 * @CreateDate 2018-11-13 12:38:14
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class FxmlColorTools {

    public static String rgb2Hex(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    public static String rgb2AlphaHex(Color color) {
        return String.format("#%02X%02X%02X%02X",
                (int) (color.getOpacity() * 255),
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    // https://en.wikipedia.org/wiki/Color_difference
    public static double calculateColorDistance2(Color color1, Color color2) {
        if (color1 == color2) {
            return 0;
        }
        double v = 2 * Math.pow(color1.getRed() - color2.getRed(), 2)
                + 4 * Math.pow(color1.getGreen() - color2.getGreen(), 2)
                + 3 * Math.pow(color1.getBlue() - color2.getBlue(), 2);
        return v;
    }

    //distance: 0.0 - 1.0
    public static boolean isColorMatch(Color color1, Color color2, double distance) {
        if (color1 == color2) {
            return true;
        } else if (distance == 0) {
            return false;
        }
        return calculateColorDistance2(color1, color2) <= Math.pow(distance, 2);
    }

    public static boolean isRedMatch(Color color1, Color color2, double distance) {
        return Math.abs(color1.getRed() - color2.getRed()) <= distance;
    }

    public static boolean isGreenMatch(Color color1, Color color2, double distance) {
        return Math.abs(color1.getGreen() - color2.getGreen()) <= distance;
    }

    public static boolean isBlueMatch(Color color1, Color color2, double distance) {
        return Math.abs(color1.getBlue() - color2.getBlue()) <= distance;
    }

    public static boolean isHueMatch(Color color1, Color color2, int distance) {
        return Math.abs(color1.getHue() - color2.getHue()) <= distance;
    }

    public static boolean isBrightnessMatch(Color color1, Color color2, double distance) {
        return Math.abs(color1.getBrightness() - color2.getBrightness()) <= distance;
    }

    public static boolean isSaturationMatch(Color color1, Color color2, double distance) {
        return Math.abs(color1.getSaturation() - color2.getSaturation()) <= distance;
    }

    public static Color thresholdingColor(Color inColor,
            int threshold, int smallValue, int bigValue) {
        if (inColor == Color.TRANSPARENT) {
            return inColor;
        }
        double red, green, blue;
        double thresholdDouble = threshold / 255.0, smallDouble = smallValue / 255.0, bigDouble = bigValue / 255.0;
        if (inColor.getRed() < thresholdDouble) {
            red = smallDouble;
        } else {
            red = bigDouble;
        }
        if (inColor.getGreen() < thresholdDouble) {
            green = smallDouble;
        } else {
            green = bigDouble;
        }
        if (inColor.getBlue() < thresholdDouble) {
            blue = smallDouble;
        } else {
            blue = bigDouble;
        }
        Color newColor = new Color(red, green, blue, inColor.getOpacity());
        return newColor;
    }

    public static Color posterizingColor(Color inColor, int size) {
        if (inColor == Color.TRANSPARENT) {
            return inColor;
        }
        double red, green, blue;

        int v = (int) (inColor.getRed() * 255);
        v = v - (v % size);
        red = Math.min(Math.max(v / 255.0, 0.0), 1.0);

        v = (int) (inColor.getGreen() * 255);
        v = v - (v % size);
        green = Math.min(Math.max(v / 255.0, 0.0), 1.0);

        v = (int) (inColor.getBlue() * 255);
        v = v - (v % size);
        blue = Math.min(Math.max(v / 255.0, 0.0), 1.0);

        Color newColor = new Color(red, green, blue, inColor.getOpacity());
        return newColor;
    }

    public static Color pixel2Sepia(Color color, double sepiaIntensity) {
        double sepiaDepth = 20;
        double gray = color.grayscale().getRed() * 255;
        double r = gray, g = gray, b = gray;
        r = Math.min(r + (sepiaDepth * 2), 255);
        g = Math.min(g + sepiaDepth, 255);
        b = Math.min(Math.max(b - sepiaIntensity, 0), 255);
        Color newColor = new Color(r / 255.0f, g / 255.0f, b / 255.0f, color.getOpacity());
        return newColor;
    }

}
