package mara.mybox.bufferedimage;

import java.awt.Color;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 16:07:27
 * @License Apache License Version 2.0
 */
public class ColorBlendTools {

    // https://www.cnblogs.com/xiaonanxia/p/9448444.html
    public static Color blendColor(Color color, float opocity, Color bgColor, boolean keepAlpha) {
        int red = (int) (color.getRed() * opocity + bgColor.getRed() * (1 - opocity));
        int green = (int) (color.getGreen() * opocity + bgColor.getGreen() * (1 - opocity));
        int blue = (int) (color.getBlue() * opocity + bgColor.getBlue() * (1 - opocity));
        if (keepAlpha) {
            return new Color(red, green, blue, Math.round(opocity * 255));
        } else {
            return new Color(red, green, blue);
        }
    }

    public static Color blendColor(Color color, int opocity, Color bgColor, boolean keepAlpha) {
        return blendColor(color, opocity / 255.0F, bgColor, keepAlpha);
    }

    public static Color blendColor(Color color, float opocity, Color bgColor) {
        return blendColor(color, opocity, bgColor, false);
    }

    public static Color blendColor(Color color, int opocity, Color bgColor) {
        return blendColor(color, opocity, bgColor, false);
    }
}
