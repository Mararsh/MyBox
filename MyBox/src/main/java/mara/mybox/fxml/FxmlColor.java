package mara.mybox.fxml;

import javafx.scene.paint.Color;

/**
 * @Author Mara
 * @CreateDate 2018-11-13 12:38:14
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class FxmlColor {

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

}
