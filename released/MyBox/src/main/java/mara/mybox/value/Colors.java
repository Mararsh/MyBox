package mara.mybox.value;

import java.awt.Color;
import mara.mybox.fxml.style.StyleData.StyleColor;
import static mara.mybox.fxml.style.StyleData.StyleColor.Blue;
import static mara.mybox.fxml.style.StyleData.StyleColor.Customize;
import static mara.mybox.fxml.style.StyleData.StyleColor.Green;
import static mara.mybox.fxml.style.StyleData.StyleColor.LightBlue;
import static mara.mybox.fxml.style.StyleData.StyleColor.Orange;
import static mara.mybox.fxml.style.StyleData.StyleColor.Pink;
import mara.mybox.image.tools.ColorConvertTools;

/**
 * @Author Mara
 * @CreateDate 2021-8-1
 * @License Apache License Version 2.0
 */
public class Colors {

    public static final Color TRANSPARENT = new Color(0, 0, 0, 0);

    public static Color color(StyleColor inStyle, boolean dark) {
        return ColorConvertTools.rgba2color("0x" + colorValue(inStyle, dark) + "FF");
    }

    public static String colorValue(StyleColor inStyle, boolean dark) {
        String color;
        try {
            StyleColor style = inStyle;
            if (style == null) {
                style = StyleColor.Red;
            }
            switch (style) {
                case Blue:
                    color = dark ? "003472" : "E3F9FD";
                    break;
                case LightBlue:
                    color = dark ? "4C8DAE" : "D6ECF0";
                    break;
                case Pink:
                    color = dark ? "9E004F" : "FFFFFF";
                    break;
                case Orange:
                    color = dark ? "622A1D" : "FFF8D8";
                    break;
                case Green:
                    color = dark ? "0D3928" : "E0F0E9";
                    break;
                case Customize:
                    color = dark ? customizeColorDarkValue() : customizeColorLightValue();
                    color = color.substring(2, 8);
                    break;
                default:
                    color = dark ? "C32136" : "FBD5CF";
            }
        } catch (Exception e) {
            color = dark ? "C32136" : "FBD5CF";
        }
        return color;
    }

    public static String customizeColorDarkValue() {
        return UserConfig.getString("CustomizeColorDark", "0x8B008BFF");
    }

    public static javafx.scene.paint.Color customizeColorDark() {
        return javafx.scene.paint.Color.web(customizeColorDarkValue());
    }

    public static String customizeColorLightValue() {
        return UserConfig.getString("CustomizeColorLight", "0xF8F8FFFF");
    }

    public static javafx.scene.paint.Color customizeColorLight() {
        return javafx.scene.paint.Color.web(customizeColorLightValue());
    }

}
