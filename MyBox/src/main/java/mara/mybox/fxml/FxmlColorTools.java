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

}
