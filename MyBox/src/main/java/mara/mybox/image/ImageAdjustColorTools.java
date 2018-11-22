package mara.mybox.image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import mara.mybox.fxml.FxmlAdjustColorTools;
import mara.mybox.fxml.FxmlAdjustColorTools.ColorActionType;
import mara.mybox.fxml.FxmlAdjustColorTools.ColorObjectType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @Author Mara
 * @CreateDate 2018-11-10 20:00:06
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageAdjustColorTools {

    private static final Logger logger = LogManager.getLogger();

    public static BufferedImage changeSaturate(BufferedImage source, float change) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    float[] hsb = ImageColorTools.pixel2HSB(source.getRGB(i, j));
                    float v = Math.min(Math.max(hsb[1] * (1.0f + change), 0.0f), 1.0f);
                    Color newColor = Color.getHSBColor(hsb[0], v, hsb[2]);
                    target.setRGB(i, j, newColor.getRGB());
                }
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage changeBrightness(BufferedImage source, float change) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    float[] hsb = ImageColorTools.pixel2HSB(source.getRGB(i, j));
                    float v = Math.min(Math.max(hsb[2] * (1.0f + change), 0.0f), 1.0f);
                    Color newColor = Color.getHSBColor(hsb[0], hsb[1], v);
                    target.setRGB(i, j, newColor.getRGB());
                }
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage changeHue(BufferedImage source, float change) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    float[] hsb = ImageColorTools.pixel2HSB(source.getRGB(i, j));
                    float v = hsb[0] + change;
                    if (v > 1.0f) {
                        v = v - 1.0f;
                    }
                    if (v < 0.0f) {
                        v = v + 1.0f;
                    }
                    Color newColor = Color.getHSBColor(v, hsb[1], hsb[2]);
                    target.setRGB(i, j, newColor.getRGB());
                }
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage changeRed(BufferedImage source, int change) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            Color newColor;
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    Color color = new Color(source.getRGB(i, j), true);
                    int red = Math.min(Math.max(color.getRed() + change, 0), 255);
                    newColor = new Color(red, color.getGreen(), color.getBlue(), color.getAlpha());
                    target.setRGB(i, j, newColor.getRGB());
                }
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage changeGreen(BufferedImage source, int change) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            Color newColor;
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    Color color = new Color(source.getRGB(i, j), true);
                    int green = Math.min(Math.max(color.getGreen() + change, 0), 255);
                    newColor = new Color(color.getRed(), green, color.getBlue(), color.getAlpha());
                    target.setRGB(i, j, newColor.getRGB());
                }
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage changeBlue(BufferedImage source, int change) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            Color newColor;
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    Color color = new Color(source.getRGB(i, j), true);
                    int blue = Math.min(Math.max(color.getBlue() + change, 0), 255);
                    newColor = new Color(color.getRed(), color.getGreen(), blue, color.getAlpha());
                    target.setRGB(i, j, newColor.getRGB());
                }
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage changeYellow(BufferedImage source, int change) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            Color newColor;
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    Color color = new Color(source.getRGB(i, j), true);
                    int red = Math.min(Math.max(color.getRed() + change, 0), 255);
                    int green = Math.min(Math.max(color.getGreen() + change, 0), 255);
                    newColor = new Color(red, green, color.getBlue(), color.getAlpha());
                    target.setRGB(i, j, newColor.getRGB());
                }
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage changeCyan(BufferedImage source, int change) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            Color newColor;
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    Color color = new Color(source.getRGB(i, j), true);
                    int green = Math.min(Math.max(color.getGreen() + change, 0), 255);
                    int blue = Math.min(Math.max(color.getBlue() + change, 0), 255);
                    newColor = new Color(color.getRed(), green, blue, color.getAlpha());
                    target.setRGB(i, j, newColor.getRGB());
                }
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage changeMagenta(BufferedImage source, int change) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            Color newColor;
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    Color color = new Color(source.getRGB(i, j), true);
                    int red = Math.min(Math.max(color.getRed() + change, 0), 255);
                    int blue = Math.min(Math.max(color.getBlue() + change, 0), 255);
                    newColor = new Color(red, color.getGreen(), blue, color.getAlpha());
                    target.setRGB(i, j, newColor.getRGB());
                }
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage changeRGB(BufferedImage source, int change) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            Color newColor;
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    Color color = new Color(source.getRGB(i, j), true);
                    int red = Math.min(Math.max(color.getRed() + change, 0), 255);
                    int green = Math.min(Math.max(color.getGreen() + change, 0), 255);
                    int blue = Math.min(Math.max(color.getBlue() + change, 0), 255);
                    newColor = new Color(red, green, blue, color.getAlpha());
                    target.setRGB(i, j, newColor.getRGB());
                }
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage addAlpha(BufferedImage src, int alpha) {
        try {
            int width = src.getWidth();
            int height = src.getHeight();
            BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    int rgb = src.getRGB(i, j);
                    Color color = new Color(rgb, true);
                    Color newcolor = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
                    target.setRGB(i, j, newcolor.getRGB());
                }
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage changeColor(BufferedImage source,
            ColorObjectType objectType, ColorActionType actionType, float change) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    int pixel = source.getRGB(i, j);
                    Color newColor = changeColor(pixel, objectType, actionType, change);
                    target.setRGB(i, j, newColor.getRGB());
                }
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static Color changeColor(int pixel,
            ColorObjectType type, ColorActionType actionType, float change) {
        Color newColor = new Color(pixel, true);
        try {
            int red, blue, green, intChange = (int) change;
            float v;
            float[] hsb;
            switch (type) {
                case Brightness:
                    hsb = ImageColorTools.pixel2HSB(pixel);
                    if (actionType == FxmlAdjustColorTools.ColorActionType.Set) {
                        v = change;
                    } else {
                        v = hsb[2] * (1.0f + change);
                    }
                    v = Math.min(Math.max(v, 0.0f), 1.0f);
                    newColor = Color.getHSBColor(hsb[0], hsb[1], v);
                    break;
                case Sauration:
                    hsb = ImageColorTools.pixel2HSB(pixel);
                    if (actionType == FxmlAdjustColorTools.ColorActionType.Set) {
                        v = change;
                    } else {
                        v = hsb[1] * (1.0f + change);
                    }
                    v = Math.min(Math.max(v, 0.0f), 1.0f);
                    newColor = Color.getHSBColor(hsb[0], v, hsb[2]);
                    break;
                case Hue:
                    hsb = ImageColorTools.pixel2HSB(pixel);
                    if (actionType == FxmlAdjustColorTools.ColorActionType.Set) {
                        v = change;
                    } else {
                        v = hsb[0] + change;
                    }
                    if (v > 1.0f) {
                        v = v - 1.0f;
                    }
                    if (v < 0.0f) {
                        v = v + 1.0f;
                    }
                    v = Math.min(Math.max(v, 0.0f), 1.0f);
                    newColor = Color.getHSBColor(v, hsb[1], hsb[2]);
                    break;
                case Opacity:
                    intChange = Math.min(Math.max(intChange, 0), 255);
                    newColor = new Color(newColor.getRed(), newColor.getGreen(), newColor.getBlue(), intChange);
                    break;
                case Red:
                    switch (actionType) {
                        case Set:
                            red = intChange;
                            red = Math.min(Math.max(red, 0), 255);
                            newColor = new Color(red, newColor.getGreen(), newColor.getBlue(), newColor.getAlpha());
                            break;
                        case Increase:
                        case Decrease:
                            red = newColor.getRed() + intChange;
                            red = Math.min(Math.max(red, 0), 255);
                            newColor = new Color(red, newColor.getGreen(), newColor.getBlue(), newColor.getAlpha());
                            break;
                        case Filter:
                            newColor = new Color(newColor.getRed(), 0, 0, newColor.getAlpha());
                            break;
                        case Invert:
                            newColor = new Color(255 - newColor.getRed(), newColor.getGreen(), newColor.getBlue(), newColor.getAlpha());
                            break;
                    }
                    break;
                case Green:
                    switch (actionType) {
                        case Set:
                            green = intChange;
                            green = Math.min(Math.max(green, 0), 255);
                            newColor = new Color(newColor.getRed(), green, newColor.getBlue(), newColor.getAlpha());
                            break;
                        case Increase:
                        case Decrease:
                            green = newColor.getGreen() + intChange;
                            green = Math.min(Math.max(green, 0), 255);
                            newColor = new Color(newColor.getRed(), green, newColor.getBlue(), newColor.getAlpha());
                            break;
                        case Filter:
                            newColor = new Color(0, newColor.getGreen(), 0, newColor.getAlpha());
                            break;
                        case Invert:
                            newColor = new Color(newColor.getRed(), 255 - newColor.getGreen(), newColor.getBlue(), newColor.getAlpha());
                            break;
                    }
                    break;
                case Blue:
                    switch (actionType) {
                        case Set:
                            blue = intChange;
                            blue = Math.min(Math.max(blue, 0), 255);
                            newColor = new Color(newColor.getRed(), newColor.getGreen(), blue, newColor.getAlpha());
                            break;
                        case Increase:
                        case Decrease:
                            blue = newColor.getBlue() + intChange;
                            blue = Math.min(Math.max(blue, 0), 255);
                            newColor = new Color(newColor.getRed(), newColor.getGreen(), blue, newColor.getAlpha());
                            break;
                        case Filter:
                            newColor = new Color(0, 0, newColor.getBlue(), newColor.getAlpha());
                            break;
                        case Invert:
                            newColor = new Color(newColor.getRed(), newColor.getGreen(), 255 - newColor.getBlue(), newColor.getAlpha());
                            break;
                    }
                    break;
                case Yellow:
                    switch (actionType) {
                        case Set:
                            red = intChange;
                            green = intChange;
                            red = Math.min(Math.max(red, 0), 255);
                            green = Math.min(Math.max(green, 0), 255);
                            newColor = new Color(red, green, newColor.getBlue(), newColor.getAlpha());
                            break;
                        case Increase:
                        case Decrease:
                            red = newColor.getRed() + intChange;
                            green = newColor.getGreen() + intChange;
                            red = Math.min(Math.max(red, 0), 255);
                            green = Math.min(Math.max(green, 0), 255);
                            newColor = new Color(red, green, newColor.getBlue(), newColor.getAlpha());
                            break;
                        case Filter:
                            newColor = new Color(newColor.getRed(), newColor.getGreen(), 0, newColor.getAlpha());
                            break;
                        case Invert:
                            newColor = new Color(255 - newColor.getRed(), 255 - newColor.getGreen(), newColor.getBlue(), newColor.getAlpha());
                            break;
                    }
                    break;
                case Cyan:
                    switch (actionType) {
                        case Set:
                            blue = intChange;
                            green = intChange;
                            blue = Math.min(Math.max(blue, 0), 255);
                            green = Math.min(Math.max(green, 0), 255);
                            newColor = new Color(newColor.getRed(), green, blue, newColor.getAlpha());
                            break;
                        case Increase:
                        case Decrease:
                            blue = newColor.getBlue() + intChange;
                            green = newColor.getGreen() + intChange;
                            blue = Math.min(Math.max(blue, 0), 255);
                            green = Math.min(Math.max(green, 0), 255);
                            newColor = new Color(newColor.getRed(), green, blue, newColor.getAlpha());
                            break;
                        case Filter:
                            newColor = new Color(0, newColor.getGreen(), newColor.getBlue(), newColor.getAlpha());
                            break;
                        case Invert:
                            newColor = new Color(newColor.getRed(), 255 - newColor.getGreen(), 255 - newColor.getBlue(), newColor.getAlpha());
                            break;
                    }
                    break;
                case Magenta:
                    switch (actionType) {
                        case Set:
                            red = intChange;
                            blue = intChange;
                            red = Math.min(Math.max(red, 0), 255);
                            blue = Math.min(Math.max(blue, 0), 255);
                            newColor = new Color(red, newColor.getGreen(), blue, newColor.getAlpha());
                            break;
                        case Increase:
                        case Decrease:
                            red = newColor.getRed() + intChange;
                            blue = newColor.getBlue() + intChange;
                            red = Math.min(Math.max(red, 0), 255);
                            blue = Math.min(Math.max(blue, 0), 255);
                            newColor = new Color(red, newColor.getGreen(), blue, newColor.getAlpha());
                            break;
                        case Filter:
                            newColor = new Color(newColor.getRed(), 0, newColor.getBlue(), newColor.getAlpha());
                            break;
                        case Invert:
                            newColor = new Color(255 - newColor.getRed(), newColor.getGreen(), 255 - newColor.getBlue(), newColor.getAlpha());
                            break;
                    }
                    break;
                case RGB:
                    switch (actionType) {
                        case Set:
                            blue = intChange;
                            green = intChange;
                            red = intChange;
                            red = Math.min(Math.max(red, 0), 255);
                            green = Math.min(Math.max(green, 0), 255);
                            blue = Math.min(Math.max(blue, 0), 255);
                            newColor = new Color(red, green, blue, newColor.getAlpha());
                            break;
                        case Increase:
                        case Decrease:
                            blue = newColor.getBlue() + intChange;
                            green = newColor.getGreen() + intChange;
                            red = newColor.getRed() + intChange;
                            red = Math.min(Math.max(red, 0), 255);
                            green = Math.min(Math.max(green, 0), 255);
                            blue = Math.min(Math.max(blue, 0), 255);
                            newColor = new Color(red, green, blue, newColor.getAlpha());
                            break;
                        case Invert:
                            newColor = new Color(255 - newColor.getRed(), 255 - newColor.getGreen(), 255 - newColor.getBlue(), newColor.getAlpha());
                            break;
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
        return newColor;
    }

}
