package mara.mybox.image.data;

import java.awt.Color;
import java.awt.image.BufferedImage;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.DoubleShape;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;

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
    protected boolean baseAbove = false;

    protected Color foreColor, backColor;
    protected int red, green, blue, alpha;
    protected TransparentAs baseTransparentAs = TransparentAs.Transparent,
            overlayTransparentAs = TransparentAs.Another;

    public enum TransparentAs {
        Another, Transparent, Blend
    }

    public PixelsBlend() {
    }

    public int blend(int overlayPixel, int basePixel) {
        if (basePixel == 0) {
            if (baseTransparentAs == TransparentAs.Transparent) {
                return 0;
            } else if (baseTransparentAs == TransparentAs.Another) {
                return overlayPixel;
            }
        }
        if (overlayPixel == 0) {
            if (overlayTransparentAs == TransparentAs.Another) {
                return basePixel;
            } else if (overlayTransparentAs == TransparentAs.Transparent) {
                return 0;
            }
        }
        if (baseAbove) {
            foreColor = new Color(basePixel, true);
            backColor = new Color(overlayPixel, true);
        } else {
            foreColor = new Color(overlayPixel, true);
            backColor = new Color(basePixel, true);
        }
        makeRGB();
        makeAlpha();
        Color newColor = new Color(
                Math.min(Math.max(red, 0), 255),
                Math.min(Math.max(green, 0), 255),
                Math.min(Math.max(blue, 0), 255),
                Math.min(Math.max(alpha, 0), 255));
        return newColor.getRGB();
    }

    public Color blend(Color overlayColor, Color baseColor) {
        if (overlayColor == null) {
            return baseColor;
        }
        if (baseColor == null) {
            return overlayColor;
        }
        int b = blend(overlayColor.getRGB(), baseColor.getRGB());
        return new Color(b, true);
    }

    // replace this in different blend mode. Refer to "PixelsBlendFactory"
    public void makeRGB() {
        red = blendValues(foreColor.getRed(), backColor.getRed(), opacity);
        green = blendValues(foreColor.getGreen(), backColor.getGreen(), opacity);
        blue = blendValues(foreColor.getBlue(), backColor.getBlue(), opacity);
    }

    protected void makeAlpha() {
        float w = fixedOpacity(opacity);
        alpha = (int) (foreColor.getAlpha() * w + backColor.getAlpha() * (1.0f - w));
    }

    public BufferedImage blend(FxTask task, BufferedImage overlay, BufferedImage baseImage, int x, int y) {
        try {
            if (overlay == null || baseImage == null) {
                return null;
            }
            int imageType = BufferedImage.TYPE_INT_ARGB;
            DoubleRectangle rect = DoubleRectangle.xywh(x, y, overlay.getWidth(), overlay.getHeight());
            BufferedImage target = new BufferedImage(baseImage.getWidth(), baseImage.getHeight(), imageType);
            int height = baseImage.getHeight();
            int width = baseImage.getWidth();
            for (int j = 0; j < height; ++j) {
                if (task != null && !task.isWorking()) {
                    return null;
                }
                for (int i = 0; i < width; ++i) {
                    if (task != null && !task.isWorking()) {
                        return null;
                    }
                    int basePixel = baseImage.getRGB(i, j);
                    if (DoubleShape.contains(rect, i, j)) {
                        int overlayPixel = overlay.getRGB(i - x, j - y);
                        target.setRGB(i, j, blend(overlayPixel, basePixel));
                    } else {
                        target.setRGB(i, j, basePixel);
                    }
                }
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return overlay;
        }
    }

    /*
        static
     */
    public static Color blend2(Color foreColor, Color backColor, float opacity, boolean ignoreTransparency) {
        if (backColor.getRGB() == 0 && ignoreTransparency) {
            return backColor;
        }
        return blend(foreColor, backColor, opacity);
    }

    public static Color blend(Color foreColor, Color backColor, float opacity) {
        int red = blendValues(foreColor.getRed(), backColor.getRed(), opacity);
        int green = blendValues(foreColor.getGreen(), backColor.getRed(), opacity);
        int blue = blendValues(foreColor.getBlue(), backColor.getRed(), opacity);
        int alpha = blendValues(foreColor.getAlpha(), backColor.getRed(), opacity);
        Color newColor = new Color(
                Math.min(Math.max(red, 0), 255),
                Math.min(Math.max(green, 0), 255),
                Math.min(Math.max(blue, 0), 255),
                Math.min(Math.max(alpha, 0), 255));
        return newColor;
    }

    public static float fixedOpacity(float v) {
        if (v > 1f) {
            return 1f;
        } else if (v < 0) {
            return 0f;
        } else {
            return v;
        }
    }

    public static int blendValues(int A, int B, float weight) {
        float w = fixedOpacity(weight);
        return (int) (A * w + B * (1.0f - w));
    }

    public static BufferedImage blend(FxTask task, BufferedImage overlay, BufferedImage baseImage,
            int x, int y, PixelsBlend blender) {
        try {
            if (overlay == null || baseImage == null || blender == null) {
                return null;
            }
            int imageType = BufferedImage.TYPE_INT_ARGB;
            DoubleRectangle rect = DoubleRectangle.xywh(x, y, overlay.getWidth(), overlay.getHeight());
            BufferedImage target = new BufferedImage(baseImage.getWidth(), baseImage.getHeight(), imageType);
            for (int j = 0; j < baseImage.getHeight(); ++j) {
                if (task != null && !task.isWorking()) {
                    return null;
                }
                for (int i = 0; i < baseImage.getWidth(); ++i) {
                    if (task != null && !task.isWorking()) {
                        return null;
                    }
                    int basePixel = baseImage.getRGB(i, j);
                    if (DoubleShape.contains(rect, i, j)) {
                        int overlayPixel = overlay.getRGB(i - x, j - y);
                        target.setRGB(i, j, blender.blend(overlayPixel, basePixel));
                    } else {
                        target.setRGB(i, j, basePixel);
                    }
                }
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return overlay;
        }
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
        this.opacity = fixedOpacity(opacity);
        return this;
    }

    public boolean isBaseAbove() {
        return baseAbove;
    }

    public PixelsBlend setBaseAbove(boolean orderReversed) {
        this.baseAbove = orderReversed;
        return this;
    }

    public TransparentAs getBaseTransparentAs() {
        return baseTransparentAs;
    }

    public PixelsBlend setBaseTransparentAs(TransparentAs baseTransparentAs) {
        this.baseTransparentAs = baseTransparentAs;
        return this;
    }

    public TransparentAs getOverlayTransparentAs() {
        return overlayTransparentAs;
    }

    public PixelsBlend setOverlayTransparentAs(TransparentAs overlayTransparentAs) {
        this.overlayTransparentAs = overlayTransparentAs;
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
