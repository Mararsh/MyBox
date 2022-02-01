package mara.mybox.value;

import java.awt.Color;
import mara.mybox.bufferedimage.ColorConvertTools;

/**
 * @Author Mara
 * @CreateDate 2021-8-1
 * @License Apache License Version 2.0
 */
public class Colors {

    public static Color MyBoxDarkRed = ColorConvertTools.rgba2color("0xC32136FF");
    public static Color MyBoxLightRed = ColorConvertTools.rgba2color("0xFBD5CFFF");

    public static Color MyBoxDarkPink = ColorConvertTools.rgba2color("0xFF0097FF");
    public static Color MyBoxLightPink = ColorConvertTools.rgba2color("0xEDD1D8FF");

    public static Color MyBoxDarkGreyBlue = ColorConvertTools.rgba2color("0x4C8DAEFF");
    public static Color MyBoxGreyBlue = ColorConvertTools.rgba2color("0xD6ECF0FF");

    public static Color MyBoxDarkBlue = ColorConvertTools.rgba2color("0x003472FF");
    public static Color MyBoxLightBlue = ColorConvertTools.rgba2color("0xE3F9FDFF");

    public static Color MyBoxOrange = ColorConvertTools.rgba2color("0xCA6924FF");
    public static Color MyBoxLightOrange = ColorConvertTools.rgba2color("0xFFF2DFFF");

    public static Color MyBoxDarkGreen = ColorConvertTools.rgba2color("0x0D3928FF");
    public static Color MyBoxLightGreen = ColorConvertTools.rgba2color("0xE0F0E9FF");

    public static Color TRANSPARENT = new Color(0, 0, 0, 0);

    public static int minValue = new Color(0, 0, 0, 0).getRGB();
    public static int maxValue = new Color(255, 255, 255, 255).getRGB();

}
