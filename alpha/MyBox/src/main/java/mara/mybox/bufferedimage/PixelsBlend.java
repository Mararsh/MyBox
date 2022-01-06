package mara.mybox.bufferedimage;

import java.awt.Color;

/**
 * @Author Mara
 * @CreateDate 2019-3-24 11:24:03
 * @License Apache License Version 2.0
 */
public abstract class PixelsBlend {

    public enum ImagesBlendMode {
        NORMAL,
        DISSOLVE,
        DARKEN,
        MULTIPLY,
        COLOR_BURN,
        LINEAR_BURN,
        SOFT_BURN,
        LIGHTEN,
        SCREEN,
        COLOR_DODGE,
        LINEAR_DODGE,
        SOFT_DODGE,
        DIVIDE,
        VIVID_LIGHT,
        LINEAR_LIGHT,
        SUBTRACT,
        AVERAGE,
        OVERLAY,
        HARD_LIGHT,
        SOFT_LIGHT,
        DIFFERENCE,
        NEGATION,
        EXCLUSION,
        REFLECT,
        GLOW,
        FREEZE,
        HEAT,
        STAMP,
        RED,
        GREEN,
        BLUE,
        HUE,
        SATURATION,
        COLOR,
        LUMINOSITY
    }

    protected ImagesBlendMode blendMode;
    protected float opacity;
    protected boolean orderReversed = false, ignoreTransparency = true;

    protected Color foreColor, backColor;
    protected int red, green, blue, alpha;

    public PixelsBlend() {
    }

    public PixelsBlend(ImagesBlendMode blendMode) {
        this.blendMode = blendMode;
    }

    public PixelsBlend(ImagesBlendMode blendMode, float opacity) {
        this.blendMode = blendMode;
        this.opacity = opacity;
    }

    public PixelsBlend(ImagesBlendMode blendMode, float opacity, boolean orderReversed) {
        this.blendMode = blendMode;
        this.opacity = opacity;
        this.orderReversed = orderReversed;
    }

    public PixelsBlend(ImagesBlendMode blendMode, float opacity, boolean orderReversed, boolean ignoreTransparency) {
        this.blendMode = blendMode;
        this.opacity = opacity;
        this.orderReversed = orderReversed;
        this.ignoreTransparency = ignoreTransparency;
    }

    protected int blend(int forePixel, int backPixel) {
        if (ignoreTransparency && forePixel == 0) {
            return backPixel;
        }
        if (ignoreTransparency && backPixel == 0) {
            return forePixel;
        }
        if (orderReversed) {
            foreColor = new Color(backPixel, true);
            backColor = new Color(forePixel, true);
        } else {
            foreColor = new Color(forePixel, true);
            backColor = new Color(backPixel, true);
        }
        makeRGB();
        alpha = (int) (foreColor.getAlpha() * opacity + backColor.getAlpha() * (1.0f - opacity));
        Color newColor = new Color(
                Math.min(Math.max(red, 0), 255),
                Math.min(Math.max(green, 0), 255),
                Math.min(Math.max(blue, 0), 255),
                Math.min(Math.max(alpha, 0), 255));
        return newColor.getRGB();
    }

    protected void makeRGB() {
        red = (int) (foreColor.getRed() * opacity + backColor.getRed() * (1.0f - opacity));
        green = (int) (foreColor.getGreen() * opacity + backColor.getGreen() * (1.0f - opacity));
        blue = (int) (foreColor.getBlue() * opacity + backColor.getBlue() * (1.0f - opacity));
    }

    /*
        static
     */
    public static Color blendColors(Color foreColor, Color backColor, float opacity, boolean ignoreTransparency) {
        if (ignoreTransparency && foreColor.getRGB() == 0) {
            return backColor;
        }
        if (ignoreTransparency && backColor.getRGB() == 0) {
            return foreColor;
        }
        return makeRGB(foreColor, backColor, opacity);
    }

    public static Color makeRGB(Color foreColor, Color backColor, float opacity) {
        int red = (int) (foreColor.getRed() * opacity + backColor.getRed() * (1.0f - opacity));
        int green = (int) (foreColor.getGreen() * opacity + backColor.getGreen() * (1.0f - opacity));
        int blue = (int) (foreColor.getBlue() * opacity + backColor.getBlue() * (1.0f - opacity));
        int alpha = (int) (foreColor.getAlpha() * opacity + backColor.getAlpha() * (1.0f - opacity));
        Color newColor = new Color(
                Math.min(Math.max(red, 0), 255),
                Math.min(Math.max(green, 0), 255),
                Math.min(Math.max(blue, 0), 255),
                Math.min(Math.max(alpha, 0), 255));
        return newColor;
    }


    /*
        get/set
     */
    public ImagesBlendMode getBlendMode() {
        return blendMode;
    }

    public PixelsBlend setBlendMode(ImagesBlendMode blendMode) {
        this.blendMode = blendMode;
        return this;
    }

    public float getOpacity() {
        return opacity;
    }

    public PixelsBlend setOpacity(float opacity) {
        this.opacity = opacity;
        return this;
    }

    public boolean isOrderReversed() {
        return orderReversed;
    }

    public PixelsBlend setOrderReversed(boolean orderReversed) {
        this.orderReversed = orderReversed;
        return this;
    }

    public boolean isIgnoreTransparency() {
        return ignoreTransparency;
    }

    public PixelsBlend setIgnoreTransparency(boolean ignoreTransparency) {
        this.ignoreTransparency = ignoreTransparency;
        return this;
    }

    public Color getForeColor() {
        return foreColor;
    }

    public void setForeColor(Color foreColor) {
        this.foreColor = foreColor;
    }

    public Color getBackColor() {
        return backColor;
    }

    public void setBackColor(Color backColor) {
        this.backColor = backColor;
    }

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

}
