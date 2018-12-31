package mara.mybox.image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Random;
import static mara.mybox.objects.AppVaribles.logger;


/**
 * @Author Mara
 * @CreateDate 2018-10-31 20:03:32
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageBlendTools {

    

    public enum ImagesRelativeLocation {
        Foreground_In_Background,
        Background_In_Foreground
    }

    public enum ImagesCompositeMode {
        A_OR_B,
        A_AND_B,
        A,
        B
    }

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

    public static BufferedImage blendImages(BufferedImage foreImage, BufferedImage backImage,
            ImagesRelativeLocation location, int x, int y,
            boolean intersectOnly, ImagesBlendMode blendMode, float alpha) {
        try {
            if (foreImage == null || backImage == null || blendMode == null) {
                return null;
            }
            switch (location) {
                case Foreground_In_Background:
                    if (intersectOnly) {
                        return blendImagesFinBIntrsectOnly(foreImage, backImage, x, y, blendMode, alpha);
                    } else {
                        return blendImagesFinB(foreImage, backImage, x, y, blendMode, alpha);
                    }
                case Background_In_Foreground:
                    if (intersectOnly) {
                        return blendImagesBinFIntrsectOnly(foreImage, backImage, x, y, blendMode, alpha);
                    } else {
                        return blendImagesBinF(foreImage, backImage, x, y, blendMode, alpha);
                    }
                default:
                    return foreImage;
            }

        } catch (Exception e) {
            logger.error(e.toString());
            return foreImage;
        }
    }

    public static BufferedImage blendImagesFinB(BufferedImage foreImage, BufferedImage backImage,
            int x, int y, ImagesBlendMode blendMode, float alpha) {
        try {
            if (foreImage == null || backImage == null || blendMode == null) {
                return null;
            }
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = new BufferedImage(backImage.getWidth(), backImage.getHeight(), imageType);
            for (int j = 0; j < backImage.getHeight(); j++) {
                for (int i = 0; i < backImage.getWidth(); i++) {
                    target.setRGB(i, j, backImage.getRGB(i, j));
                }
            }
            int areaWidth = Math.min(backImage.getWidth() - x, foreImage.getWidth());
            int areaHeight = Math.min(backImage.getHeight() - y, foreImage.getHeight());
            for (int j = 0; j < areaHeight; j++) {
                for (int i = 0; i < areaWidth; i++) {
                    int pixelFore = foreImage.getRGB(i, j);
                    int pixelBack = backImage.getRGB(i + x, j + y);
                    target.setRGB(i + x, j + y, blendColors(pixelFore, pixelBack, blendMode, alpha));
                }
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage blendImagesFinBIntrsectOnly(BufferedImage foreImage, BufferedImage backImage,
            int x, int y, ImagesBlendMode blendMode, float alpha) {
        try {
            if (foreImage == null || backImage == null || blendMode == null) {
                return null;
            }
            int imageType = BufferedImage.TYPE_INT_ARGB;
            int areaWidth = Math.min(backImage.getWidth() - x, foreImage.getWidth());
            int areaHeight = Math.min(backImage.getHeight() - y, foreImage.getHeight());
            BufferedImage target = new BufferedImage(areaWidth, areaHeight, imageType);
            for (int j = 0; j < areaHeight; j++) {
                for (int i = 0; i < areaWidth; i++) {
                    int pixelFore = foreImage.getRGB(i, j);
                    int pixelBack = backImage.getRGB(i + x, j + y);
                    target.setRGB(i, j, blendColors(pixelFore, pixelBack, blendMode, alpha));
                }
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage blendImagesBinF(BufferedImage foreImage, BufferedImage backImage,
            int x, int y, ImagesBlendMode blendMode, float alpha) {
        try {
            if (foreImage == null || backImage == null || blendMode == null) {
                return null;
            }
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = new BufferedImage(foreImage.getWidth(), foreImage.getHeight(), imageType);
            for (int j = 0; j < foreImage.getHeight(); j++) {
                for (int i = 0; i < foreImage.getWidth(); i++) {
                    target.setRGB(i, j, foreImage.getRGB(i, j));
                }
            }
            int areaWidth = Math.min(foreImage.getWidth() - x, backImage.getWidth());
            int areaHeight = Math.min(foreImage.getHeight() - y, backImage.getHeight());
            for (int j = 0; j < areaHeight; j++) {
                for (int i = 0; i < areaWidth; i++) {
                    int pixelFore = foreImage.getRGB(i + x, j + y);
                    int pixelBack = backImage.getRGB(i, j);
                    target.setRGB(i + x, j + y, blendColors(pixelFore, pixelBack, blendMode, alpha));
                }
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage blendImagesBinFIntrsectOnly(BufferedImage foreImage, BufferedImage backImage,
            int x, int y, ImagesBlendMode blendMode, float alpha) {
        try {
            if (foreImage == null || backImage == null || blendMode == null) {
                return null;
            }
            int imageType = BufferedImage.TYPE_INT_ARGB;
            int areaWidth = Math.min(foreImage.getWidth() - x, backImage.getWidth());
            int areaHeight = Math.min(foreImage.getHeight() - y, backImage.getHeight());
            BufferedImage target = new BufferedImage(areaWidth, areaHeight, imageType);
            for (int j = 0; j < areaHeight; j++) {
                for (int i = 0; i < areaWidth; i++) {
                    int pixelFore = foreImage.getRGB(i + x, j + y);
                    int pixelBack = backImage.getRGB(i, j);
                    target.setRGB(i, j, blendColors(pixelFore, pixelBack, blendMode, alpha));
                }
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    // https://en.wikipedia.org/wiki/Blend_modes
    // https://blog.csdn.net/bravebean/article/details/51392440
    // https://www.cnblogs.com/bigdream6/p/8385886.html
    // https://baike.baidu.com/item/%E6%B7%B7%E5%90%88%E6%A8%A1%E5%BC%8F/6700481?fr=aladdin
    public static int blendColors(int forePixel, int bacckPixel,
            ImagesBlendMode blendMode, float alpha) {
        try {
            if (blendMode == null) {
                return forePixel;
            }
            Color foreColor = new Color(forePixel);
            Color backColor = new Color(bacckPixel);
            int red, green, blue;
            switch (blendMode) {
                case NORMAL:
                    red = (int) (foreColor.getRed() * alpha + backColor.getRed() * (1.0f - alpha));
                    green = (int) (foreColor.getGreen() * alpha + backColor.getGreen() * (1.0f - alpha));
                    blue = (int) (foreColor.getBlue() * alpha + backColor.getBlue() * (1.0f - alpha));
                    break;
                case DISSOLVE:
                    float opacity = new Random().nextInt(101) / 100.0f;
                    red = (int) (foreColor.getRed() * opacity + backColor.getRed() * (1.0f - opacity));
                    green = (int) (foreColor.getGreen() * opacity + backColor.getGreen() * (1.0f - opacity));
                    blue = (int) (foreColor.getBlue() * opacity + backColor.getBlue() * (1.0f - opacity));
                    break;
                case MULTIPLY:
                    red = foreColor.getRed() * backColor.getRed() / 255;
                    green = foreColor.getGreen() * backColor.getGreen() / 255;
                    blue = foreColor.getBlue() * backColor.getBlue() / 255;
                    break;
                case SCREEN:
                    red = 255 - (255 - foreColor.getRed()) * (255 - backColor.getRed()) / 255;
                    green = 255 - (255 - foreColor.getGreen()) * (255 - backColor.getGreen()) / 255;
                    blue = 255 - (255 - foreColor.getBlue()) * (255 - backColor.getBlue()) / 255;
                    break;
                case OVERLAY:
                    if (backColor.getRed() < 128) {
                        red = foreColor.getRed() * backColor.getRed() / 128;
                    } else {
                        red = 255 - (255 - foreColor.getRed()) * (255 - backColor.getRed()) / 128;
                    }
                    if (backColor.getGreen() < 128) {
                        green = foreColor.getGreen() * backColor.getGreen() / 128;
                    } else {
                        green = 255 - (255 - foreColor.getGreen()) * (255 - backColor.getGreen()) / 128;
                    }
                    if (backColor.getBlue() < 128) {
                        blue = foreColor.getBlue() * backColor.getBlue() / 128;
                    } else {
                        blue = 255 - (255 - foreColor.getBlue()) * (255 - backColor.getBlue()) / 128;
                    }
                    break;
                case HARD_LIGHT:
                    if (foreColor.getRed() < 128) {
                        red = foreColor.getRed() * backColor.getRed() / 128;
                    } else {
                        red = 255 - (255 - foreColor.getRed()) * (255 - backColor.getRed()) / 128;
                    }
                    if (foreColor.getGreen() < 128) {
                        green = foreColor.getGreen() * backColor.getGreen() / 128;
                    } else {
                        green = 255 - (255 - foreColor.getGreen()) * (255 - backColor.getGreen()) / 128;
                    }
                    if (foreColor.getBlue() < 128) {
                        blue = foreColor.getBlue() * backColor.getBlue() / 128;
                    } else {
                        blue = 255 - (255 - foreColor.getBlue()) * (255 - backColor.getBlue()) / 128;
                    }
                    break;
                case SOFT_LIGHT:
                    if (foreColor.getRed() < 128) {
                        red = backColor.getRed()
                                + (2 * foreColor.getRed() - 255) * (backColor.getRed() - backColor.getRed() * backColor.getRed() / 255) / 255;
                    } else {
                        red = (int) (backColor.getRed()
                                + (2 * foreColor.getRed() - 255) * (Math.sqrt(backColor.getRed() / 255.0f) * 255 - backColor.getRed()) / 255);
                    }
                    if (foreColor.getRed() < 128) {
                        green = backColor.getGreen()
                                + (2 * foreColor.getGreen() - 255) * (backColor.getGreen() - backColor.getGreen() * backColor.getGreen() / 255) / 255;
                    } else {
                        green = (int) (backColor.getRed()
                                + (2 * foreColor.getGreen() - 255) * (Math.sqrt(backColor.getGreen() / 255.0f) * 255 - backColor.getGreen()) / 255);
                    }
                    if (foreColor.getRed() < 128) {
                        blue = backColor.getBlue()
                                + (2 * foreColor.getBlue() - 255) * (backColor.getBlue() - backColor.getBlue() * backColor.getBlue() / 255) / 255;
                    } else {
                        blue = (int) (backColor.getRed()
                                + (2 * foreColor.getBlue() - 255) * (Math.sqrt(backColor.getBlue() / 255.0f) * 255 - backColor.getBlue()) / 255);
                    }
                    break;
                case COLOR_DODGE:
                    red = foreColor.getRed() == 255 ? 255
                            : (backColor.getRed() + (foreColor.getRed() * backColor.getRed()) / (255 - foreColor.getRed()));
                    green = foreColor.getGreen() == 255 ? 255
                            : (backColor.getGreen() + (foreColor.getGreen() * backColor.getGreen()) / (255 - foreColor.getGreen()));
                    blue = foreColor.getBlue() == 255 ? 255
                            : (backColor.getBlue() + (foreColor.getBlue() * backColor.getBlue()) / (255 - foreColor.getBlue()));
                    break;
                case LINEAR_DODGE:
                    red = foreColor.getRed() + backColor.getRed();
                    green = foreColor.getGreen() + backColor.getGreen();
                    blue = foreColor.getBlue() + backColor.getBlue();
                    break;
                case DIVIDE:
                    red = foreColor.getRed() == 0 ? 255 : ((backColor.getRed() * 255) / foreColor.getRed());
                    green = foreColor.getGreen() == 0 ? 255 : ((backColor.getGreen() * 255) / foreColor.getGreen());
                    blue = foreColor.getBlue() == 0 ? 255 : ((backColor.getBlue() * 255) / foreColor.getBlue());
                    break;
                case COLOR_BURN:
                    red = foreColor.getRed() == 0 ? 0
                            : (backColor.getRed() - (255 - foreColor.getRed()) * 255 / foreColor.getRed());
                    green = foreColor.getGreen() == 0 ? 0
                            : (backColor.getGreen() - (255 - foreColor.getGreen()) * 255 / foreColor.getGreen());
                    blue = foreColor.getBlue() == 0 ? 0
                            : (backColor.getBlue() - (255 - foreColor.getBlue()) * 255 / foreColor.getBlue());
                    break;
                case LINEAR_BURN:
                    red = backColor.getRed() == 0 ? 0
                            : foreColor.getRed() + backColor.getRed() - 255;
                    green = backColor.getGreen() == 0 ? 0
                            : foreColor.getGreen() + backColor.getGreen() - 255;
                    blue = backColor.getBlue() == 0 ? 0
                            : foreColor.getBlue() + backColor.getBlue() - 255;
                    break;
                case VIVID_LIGHT:
                    if (foreColor.getRed() < 128) {
                        red = foreColor.getRed() == 0 ? backColor.getRed()
                                : (backColor.getRed() - (255 - backColor.getRed()) * (255 - 2 * foreColor.getRed()) / (2 * foreColor.getRed()));
                    } else {
                        red = foreColor.getRed() == 255 ? backColor.getRed()
                                : (backColor.getRed() + backColor.getRed() * (2 * foreColor.getRed() - 255) / (2 * (255 - foreColor.getRed())));
                    }
                    if (foreColor.getGreen() < 128) {
                        green = foreColor.getGreen() == 0 ? backColor.getGreen()
                                : (backColor.getGreen() - (255 - backColor.getGreen()) * (255 - 2 * foreColor.getGreen()) / (2 * foreColor.getGreen()));
                    } else {
                        green = foreColor.getGreen() == 255 ? backColor.getGreen()
                                : (backColor.getGreen() + backColor.getGreen() * (2 * foreColor.getGreen() - 255) / (2 * (255 - foreColor.getGreen())));
                    }
                    if (foreColor.getBlue() < 128) {
                        blue = foreColor.getBlue() == 0 ? backColor.getBlue()
                                : (backColor.getBlue() - (255 - backColor.getBlue()) * (255 - 2 * foreColor.getBlue()) / (2 * foreColor.getBlue()));
                    } else {
                        blue = foreColor.getBlue() == 255 ? backColor.getBlue()
                                : (backColor.getBlue() + backColor.getBlue() * (2 * foreColor.getBlue() - 255) / (2 * (255 - foreColor.getBlue())));
                    }
                    break;
                case LINEAR_LIGHT:
                    red = 2 * foreColor.getRed() + backColor.getRed() - 255;
                    green = 2 * foreColor.getGreen() + backColor.getGreen() - 255;
                    blue = 2 * foreColor.getBlue() + backColor.getBlue() - 255;
                    break;
                case SUBTRACT:
                    red = backColor.getRed() - foreColor.getRed();
                    green = backColor.getGreen() - foreColor.getGreen();
                    blue = backColor.getBlue() - foreColor.getBlue();
                    break;
                case DIFFERENCE:
                    red = Math.abs(backColor.getRed() - foreColor.getRed());
                    green = Math.abs(backColor.getGreen() - foreColor.getGreen());
                    blue = Math.abs(backColor.getBlue() - foreColor.getBlue());
                    break;
                case EXCLUSION:
                    red = backColor.getRed() + foreColor.getRed() - backColor.getRed() * foreColor.getRed() / 128;
                    green = backColor.getGreen() + foreColor.getGreen() - backColor.getGreen() * foreColor.getGreen() / 128;
                    blue = backColor.getBlue() + foreColor.getBlue() - backColor.getBlue() * foreColor.getBlue() / 128;
                    break;
                case DARKEN:
                    red = Math.min(backColor.getRed(), foreColor.getRed());
                    green = Math.min(backColor.getGreen(), foreColor.getGreen());
                    blue = Math.min(backColor.getBlue(), foreColor.getBlue());
                    break;
                case LIGHTEN:
                    red = Math.max(backColor.getRed(), foreColor.getRed());
                    green = Math.max(backColor.getGreen(), foreColor.getGreen());
                    blue = Math.max(backColor.getBlue(), foreColor.getBlue());
                    break;
                case HUE:
                    float[] hA = ImageColorTools.pixel2HSB(forePixel);
                    float[] hB = ImageColorTools.pixel2HSB(bacckPixel);
                    Color hColor = Color.getHSBColor(hA[0], hB[1], hB[2]);
                    return hColor.getRGB();
                case SATURATION:
                    float[] sA = ImageColorTools.pixel2HSB(forePixel);
                    float[] sB = ImageColorTools.pixel2HSB(bacckPixel);
                    Color sColor = Color.getHSBColor(sB[0], sA[1], sB[2]);
                    return sColor.getRGB();
                case LUMINOSITY:
                    float[] bA = ImageColorTools.pixel2HSB(forePixel);
                    float[] bB = ImageColorTools.pixel2HSB(bacckPixel);
                    Color newColor = Color.getHSBColor(bB[0], bB[1], bA[2]);
                    return newColor.getRGB();
                case COLOR:
                    float[] cA = ImageColorTools.pixel2HSB(forePixel);
                    float[] cB = ImageColorTools.pixel2HSB(bacckPixel);
                    Color cColor = Color.getHSBColor(cA[0], cA[1], cB[2]);
                    return cColor.getRGB();
                default:
                    return forePixel;
            }
            Color newColor = new Color(
                    Math.min(Math.max(red, 0), 255),
                    Math.min(Math.max(green, 0), 255),
                    Math.min(Math.max(blue, 0), 255),
                    Math.min(foreColor.getAlpha() + backColor.getAlpha(), 255));
            return newColor.getRGB();
        } catch (Exception e) {
            logger.error(e.toString());
            return forePixel;
        }
    }

}
