package mara.mybox.bufferedimage;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Colors;

/**
 * @Author Mara
 * @CreateDate 2018-6-27 18:58:57
 * @License Apache License Version 2.0
 */
public class ScaleTools {

    public static BufferedImage scaleImage(BufferedImage source, int width, int height) {
        if (width <= 0 || height <= 0 || (width == source.getWidth() && height == source.getHeight())) {
            return source;
        }
        int imageType = BufferedImage.TYPE_INT_ARGB;
        BufferedImage target = new BufferedImage(width, height, imageType);
        Graphics2D g = target.createGraphics();
        if (AppVariables.imageRenderHints != null) {
            g.addRenderingHints(AppVariables.imageRenderHints);
        }
        g.setBackground(Colors.TRANSPARENT);
        g.drawImage(source, 0, 0, width, height, null);
        g.dispose();
        return target;
    }

    public static BufferedImage scaleImage(BufferedImage source, int targetW, int targetH, boolean keepRatio, int keepType) {
        int finalW = targetW;
        int finalH = targetH;
        if (keepRatio) {
            int[] wh = ScaleTools.scaleValues(source.getWidth(), source.getHeight(), targetW, targetH, keepType);
            finalW = wh[0];
            finalH = wh[1];
        }
        return scaleImageBySize(source, finalW, finalH);
    }

    public static int[] scaleValues(int sourceX, int sourceY, int newWidth, int newHeight, int keepRatioType) {
        int finalW = newWidth;
        int finalH = newHeight;
        if (keepRatioType != BufferedImageTools.KeepRatioType.None) {
            double ratioW = (double) newWidth / sourceX;
            double ratioH = (double) newHeight / sourceY;
            if (ratioW != ratioH) {
                switch (keepRatioType) {
                    case BufferedImageTools.KeepRatioType.BaseOnWidth:
                        finalH = (int) (ratioW * sourceY);
                        break;
                    case BufferedImageTools.KeepRatioType.BaseOnHeight:
                        finalW = (int) (ratioH * sourceX);
                        break;
                    case BufferedImageTools.KeepRatioType.BaseOnLarger:
                        if (ratioW > ratioH) {
                            finalH = (int) (ratioW * sourceY);
                        } else {
                            finalW = (int) (ratioH * sourceX);
                        }
                        break;
                    case BufferedImageTools.KeepRatioType.BaseOnSmaller:
                        if (ratioW < ratioH) {
                            finalH = (int) (ratioW * sourceY);
                        } else {
                            finalW = (int) (ratioH * sourceX);
                        }
                        break;
                }
            }
        }
        int[] d = new int[2];
        d[0] = finalW;
        d[1] = finalH;
        return d;
    }

    public static BufferedImage scaleImageByScale(BufferedImage source, float scale) {
        return scaleImageByScale(source, scale, scale);
    }

    public static BufferedImage scaleImageByScale(BufferedImage source, float xscale, float yscale) {
        int width = (int) (source.getWidth() * xscale);
        int height = (int) (source.getHeight() * yscale);
        return scaleImage(source, width, height);
    }

    public static BufferedImage scaleImageHeightKeep(BufferedImage source, int height) {
        int width = source.getWidth() * height / source.getHeight();
        return scaleImage(source, width, height);
    }

    public static BufferedImage scaleImageBySize(BufferedImage source, int width, int height) {
        return scaleImage(source, width, height);
    }

    public static BufferedImage scaleImageWidthKeep(BufferedImage source, int width) {
        if (width <= 0 || width == source.getWidth()) {
            return source;
        }
        int height = source.getHeight() * width / source.getWidth();
        return scaleImage(source, width, height);
    }

    public static BufferedImage scaleImageLess(BufferedImage source, int size) {
        if (size <= 0) {
            return source;
        }
        float scale = size / (source.getWidth() * source.getHeight());
        if (scale >= 1) {
            return source;
        }
        return scaleImageByScale(source, scale);
    }

    public static BufferedImage fitSize(BufferedImage source, int targetW, int targetH) {
        try {
            int[] wh = ScaleTools.scaleValues(source.getWidth(), source.getHeight(), targetW, targetH, BufferedImageTools.KeepRatioType.BaseOnSmaller);
            int finalW = wh[0];
            int finalH = wh[1];
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = new BufferedImage(targetW, targetH, imageType);
            Graphics2D g = target.createGraphics();
            if (AppVariables.imageRenderHints != null) {
                g.addRenderingHints(AppVariables.imageRenderHints);
            }
            g.setBackground(Colors.TRANSPARENT);
            g.drawImage(source, (targetW - finalW) / 2, (targetH - finalH) / 2, finalW, finalH, null);
            g.dispose();
            return target;
        } catch (Exception e) {
            return null;
        }
    }

}
