package mara.mybox.image.tools;

import java.awt.Color;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 16:07:27
 * @License Apache License Version 2.0
 */
public class ColorBlendTools {

    // https://www.cnblogs.com/xiaonanxia/p/9448444.html
    public static Color blendColor(Color foreColor, float opacity, Color backColor, boolean keepAlpha) {
        int red = (int) (foreColor.getRed() * opacity + backColor.getRed() * (1.0f - opacity));
        int green = (int) (foreColor.getGreen() * opacity + backColor.getGreen() * (1.0f - opacity));
        int blue = (int) (foreColor.getBlue() * opacity + backColor.getBlue() * (1.0f - opacity));
        int alpha = keepAlpha
                ? (int) (foreColor.getAlpha() * opacity + backColor.getAlpha() * (1.0f - opacity))
                : 255;
        return new Color(
                Math.min(Math.max(red, 0), 255),
                Math.min(Math.max(green, 0), 255),
                Math.min(Math.max(blue, 0), 255),
                Math.min(Math.max(alpha, 0), 255));
    }

    public static Color blendColor(Color color, int opocity, Color bgColor, boolean keepAlpha) {
        return blendColor(color, opocity / 255.0F, bgColor, keepAlpha);
    }

    public static Color blendColor(Color color, float opocity, Color bgColor) {
        return blendColor(color, opocity, bgColor, false);
    }

    public static int blendPixel(int forePixel, int backPixel, float opacity, boolean orderReversed, boolean ignoreTransparency) {
        if (ignoreTransparency && forePixel == 0) {
            return backPixel;
        }
        if (ignoreTransparency && backPixel == 0) {
            return forePixel;
        }
        Color foreColor, backColor;
        if (orderReversed) {
            foreColor = new Color(backPixel, true);
            backColor = new Color(forePixel, true);
        } else {
            foreColor = new Color(forePixel, true);
            backColor = new Color(backPixel, true);
        }
        Color newColor = blendColor(foreColor, opacity, backColor, true);
        return newColor.getRGB();
    }

    public static int blendPixel(int forePixel, int backPixel, float opacity) {
        return blendPixel(forePixel, backPixel, opacity, false, false);
    }
}
