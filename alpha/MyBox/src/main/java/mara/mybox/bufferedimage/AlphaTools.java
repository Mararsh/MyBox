package mara.mybox.bufferedimage;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Colors;
import mara.mybox.value.FileExtensions;

/**
 * @Author Mara
 * @CreateDate 2018-6-27 18:58:57
 * @License Apache License Version 2.0
 */
public class AlphaTools {

    // https://bugs.java.com/bugdatabase/view_bug.do?bug_id=4836466
    public static BufferedImage checkAlpha(BufferedImage source, String targetFormat) {
        if (targetFormat == null) {
            return source;
        }
        BufferedImage checked = source;
        if (FileExtensions.NoAlphaImages.contains(targetFormat.toLowerCase())) {
            if (hasAlpha(source)) {
                checked = AlphaTools.premultipliedAlpha(source, true);
            }
        }
        return checked;
    }

    public static boolean hasAlpha(BufferedImage source) {
        switch (source.getType()) {
            case BufferedImage.TYPE_3BYTE_BGR:
            case BufferedImage.TYPE_BYTE_BINARY:
            case BufferedImage.TYPE_BYTE_GRAY:
            case BufferedImage.TYPE_BYTE_INDEXED:
            case BufferedImage.TYPE_INT_BGR:
            case BufferedImage.TYPE_INT_RGB:
            case BufferedImage.TYPE_USHORT_555_RGB:
            case BufferedImage.TYPE_USHORT_565_RGB:
            case BufferedImage.TYPE_USHORT_GRAY:
                return false;
            default:
                if (source.getColorModel() != null) {
                    return source.getColorModel().hasAlpha();
                } else {
                    return true;
                }
        }
    }

    public static BufferedImage removeAlpha(BufferedImage source) {
        if (!hasAlpha(source)) {
            return source;
        }
        return AlphaTools.removeAlpha(source, ColorConvertTools.alphaColor());
    }

    public static BufferedImage removeAlpha(BufferedImage source, Color alphaColor) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = BufferedImage.TYPE_INT_RGB;
            BufferedImage target = new BufferedImage(width, height, imageType);
            int alphaPixel = alphaColor.getRGB();
            for (int j = 0; j < height; ++j) {
                for (int i = 0; i < width; ++i) {
                    int pixel = source.getRGB(i, j);
                    if (pixel == 0) {
                        target.setRGB(i, j, alphaPixel);
                    } else {
                        target.setRGB(i, j, new Color(pixel, false).getRGB());
                    }
                }
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static BufferedImage[] extractAlpha(BufferedImage source) {
        try {
            if (source == null) {
                return null;
            }
            BufferedImage[] bfs = new BufferedImage[2];
            int width = source.getWidth();
            int height = source.getHeight();
            BufferedImage alphaImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            BufferedImage noAlphaImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Color color;
            Color newColor;
            int pixel;
            int alphaPixel = ColorConvertTools.alphaColor().getRGB();
            for (int j = 0; j < height; ++j) {
                for (int i = 0; i < width; ++i) {
                    pixel = source.getRGB(i, j);
                    if (pixel == 0) {
                        noAlphaImage.setRGB(i, j, alphaPixel);
                        alphaImage.setRGB(i, j, 0);
                    } else {
                        color = new Color(pixel, true);
                        newColor = new Color(color.getRed(), color.getGreen(), color.getBlue());
                        noAlphaImage.setRGB(i, j, newColor.getRGB());
                        newColor = new Color(0, 0, 0, color.getAlpha());
                        alphaImage.setRGB(i, j, newColor.getRGB());
                    }
                }
            }
            bfs[0] = noAlphaImage;
            bfs[1] = alphaImage;
            return bfs;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static BufferedImage extractAlphaOnly(BufferedImage source) {
        try {
            if (source == null) {
                return null;
            }
            int width = source.getWidth();
            int height = source.getHeight();
            BufferedImage alphaImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Color color;
            Color newColor;
            int pixel;
            for (int j = 0; j < height; ++j) {
                for (int i = 0; i < width; ++i) {
                    pixel = source.getRGB(i, j);
                    color = new Color(pixel, true);
                    newColor = new Color(0, 0, 0, color.getAlpha());
                    alphaImage.setRGB(i, j, newColor.getRGB());
                }
            }
            return alphaImage;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static BufferedImage premultipliedAlpha(BufferedImage source, boolean removeAlpha) {
        try {
            if (source == null || !AlphaTools.hasAlpha(source) || (source.isAlphaPremultiplied() && !removeAlpha)) {
                return source;
            }
            int sourceWidth = source.getWidth();
            int sourceHeight = source.getHeight();
            int imageType;
            if (removeAlpha) {
                imageType = BufferedImage.TYPE_INT_RGB;
            } else {
                imageType = BufferedImage.TYPE_INT_ARGB_PRE;
            }
            BufferedImage target = new BufferedImage(sourceWidth, sourceHeight, imageType);
            Color sourceColor;
            Color newColor;
            Color bkColor = ColorConvertTools.alphaColor();
            int bkPixel = bkColor.getRGB();
            for (int j = 0; j < sourceHeight; ++j) {
                for (int i = 0; i < sourceWidth; ++i) {
                    int pixel = source.getRGB(i, j);
                    if (pixel == 0) {
                        target.setRGB(i, j, bkPixel);
                        continue;
                    }
                    sourceColor = new Color(pixel, true);
                    newColor = ColorBlendTools.blendColor(sourceColor, sourceColor.getAlpha(), bkColor, !removeAlpha);
                    target.setRGB(i, j, newColor.getRGB());
                }
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return source;
        }
    }

    public static BufferedImage addAlpha(BufferedImage source, BufferedImage alpha, boolean isPlus) {
        try {
            if (source == null || alpha == null || !alpha.getColorModel().hasAlpha()) {
                return source;
            }
            int sourceWidth = source.getWidth();
            int sourceHeight = source.getHeight();
            int alphaWidth = alpha.getWidth();
            int alphaHeight = alpha.getHeight();
            boolean addAlpha = isPlus && source.getColorModel().hasAlpha();
            BufferedImage target = new BufferedImage(sourceWidth, sourceHeight, BufferedImage.TYPE_INT_ARGB);
            Color sourceColor;
            Color alphaColor;
            Color newColor;
            int alphaValue;
            for (int j = 0; j < sourceHeight; ++j) {
                for (int i = 0; i < sourceWidth; ++i) {
                    if (i < alphaWidth && j < alphaHeight) {
                        sourceColor = new Color(source.getRGB(i, j), addAlpha);
                        alphaColor = new Color(alpha.getRGB(i, j), true);
                        alphaValue = alphaColor.getAlpha();
                        if (addAlpha) {
                            alphaValue = Math.min(255, alphaValue + sourceColor.getAlpha());
                        }
                        newColor = new Color(sourceColor.getRed(), sourceColor.getGreen(), sourceColor.getBlue(), alphaValue);
                        target.setRGB(i, j, newColor.getRGB());
                    } else {
                        target.setRGB(i, j, source.getRGB(i, j));
                    }
                }
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static BufferedImage addAlpha(BufferedImage source, float opacity, boolean isPlus) {
        try {
            if (source == null || opacity < 0) {
                return source;
            }
            int sourceWidth = source.getWidth();
            int sourceHeight = source.getHeight();
            boolean addAlpha = isPlus && source.getColorModel().hasAlpha();
            BufferedImage target = new BufferedImage(sourceWidth, sourceHeight, BufferedImage.TYPE_INT_ARGB);
            Color sourceColor;
            Color newColor;
            int opacityValue = Math.min(255, Math.round(opacity * 255));
            for (int j = 0; j < sourceHeight; ++j) {
                for (int i = 0; i < sourceWidth; ++i) {
                    sourceColor = new Color(source.getRGB(i, j), addAlpha);
                    if (addAlpha) {
                        opacityValue = Math.min(255, opacityValue + sourceColor.getAlpha());
                    }
                    newColor = new Color(sourceColor.getRed(), sourceColor.getGreen(), sourceColor.getBlue(), opacityValue);
                    target.setRGB(i, j, newColor.getRGB());
                }
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static BufferedImage premultipliedAlpha2(BufferedImage source, boolean removeAlpha) {
        try {
            if (source == null || !AlphaTools.hasAlpha(source) || (source.isAlphaPremultiplied() && !removeAlpha)) {
                return source;
            }
            BufferedImage target = BufferedImageTools.clone(source);
            target.coerceData(true);
            if (removeAlpha) {
                target = AlphaTools.removeAlpha(target);
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return source;
        }
    }

    public static BufferedImage[] outline(BufferedImage bgImage, BufferedImage alphaImage, DoubleRectangle rect) {
        return outline(alphaImage, rect, bgImage.getWidth(), bgImage.getHeight(), false);
    }

    public static BufferedImage[] outline(BufferedImage alphaImage,
            DoubleRectangle rect, int bgWidth, int bgHeight, boolean keepRatio) {
        try {
            if (alphaImage == null) {
                return null;
            }
            BufferedImage scaledImage = ScaleTools.scaleImage(alphaImage,
                    (int) rect.getWidth(), (int) rect.getHeight(), keepRatio, BufferedImageTools.KeepRatioType.BaseOnWidth);
            int offsetX = (int) rect.getX();
            int offsetY = (int) rect.getY();
            int scaledWidth = scaledImage.getWidth();
            int scaledHeight = scaledImage.getHeight();
            int width = offsetX >= 0 ? Math.max(bgWidth, scaledWidth + offsetX) : Math.max(bgWidth - offsetX, scaledWidth);
            int height = offsetY >= 0 ? Math.max(bgHeight, scaledHeight + offsetY) : Math.max(bgHeight - offsetY, scaledHeight);
            int startX = offsetX >= 0 ? offsetX : 0;
            int startY = offsetY >= 0 ? offsetY : 0;
            BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = target.createGraphics();
            if (AppVariables.imageRenderHints != null) {
                g.addRenderingHints(AppVariables.imageRenderHints);
            }
            g.setColor(Colors.TRANSPARENT);
            g.fillRect(0, 0, width, height);
            int imagePixel;
            int inPixel = 100, outPixel = -100;
            for (int j = 0; j < scaledHeight; ++j) {
                for (int i = 0; i < scaledWidth; ++i) {
                    imagePixel = scaledImage.getRGB(i, j);
                    target.setRGB(i + startX, j + startY, imagePixel == 0 ? outPixel : inPixel);
                }
            }
            g.dispose();
            BufferedImage[] ret = new BufferedImage[2];
            ret[0] = scaledImage;
            ret[1] = target;
            return ret;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
