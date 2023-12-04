package mara.mybox.bufferedimage;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Colors;

/**
 * @Author Mara
 * @CreateDate 2018-6-27 18:58:57
 * @License Apache License Version 2.0
 */
public class ShadowTools {

    public static BufferedImage addShadow(BufferedImage source, int shadowWidth, Color shadowColor) {
        if (AlphaTools.hasAlpha(source)) {
            return addShadowAlpha(source, shadowWidth, shadowColor);
        } else {
            return addShadowNoAlpha(source, shadowWidth, shadowColor);
        }
    }

    public static BufferedImage addShadowAlpha(BufferedImage source, int shadowWidth, Color shadowColor) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            if (shadowWidth <= 0 || 2 * shadowWidth > width || 2 * shadowWidth > height) {
                return source;
            }
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage shadowImage = new BufferedImage(width, height, imageType);
            Color newColor;
            float iOpocity;
            float jOpacity;
            float opocity;
            for (int j = 0; j < height; ++j) {
                for (int i = 0; i < width; ++i) {
                    int pixel = source.getRGB(i, j);
                    if (pixel == 0) {
                        shadowImage.setRGB(i, j, 0);
                        continue;
                    }
                    iOpocity = jOpacity = 1.0F;
                    if (i < shadowWidth) {
                        iOpocity = 1.0F * i / shadowWidth;
                    } else if (i > width - shadowWidth) {
                        iOpocity = 1.0F * (width - i) / shadowWidth;
                    }
                    if (j < shadowWidth) {
                        jOpacity = 1.0F * j / shadowWidth;
                    } else if (j > height - shadowWidth) {
                        jOpacity = 1.0F * (height - j) / shadowWidth;
                    }
                    opocity = iOpocity * jOpacity;
                    if (opocity == 1.0F) {
                        newColor = shadowColor;
                    } else {
                        newColor = new Color(shadowColor.getRed() / 255.0F, shadowColor.getGreen() / 255.0F, shadowColor.getBlue() / 255.0F, opocity);
                    }
                    shadowImage.setRGB(i, j, newColor.getRGB());
                }
            }
            BufferedImage target = new BufferedImage(width + shadowWidth, height + shadowWidth, imageType);
            Graphics2D g = target.createGraphics();
            if (AppVariables.ImageHints != null) {
                g.addRenderingHints(AppVariables.ImageHints);
            }
            Color bgColor = Colors.TRANSPARENT;
            g.setColor(bgColor);
            g.fillRect(0, 0, width + shadowWidth, height + shadowWidth);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            g.drawImage(shadowImage, shadowWidth, shadowWidth, null);
            g.drawImage(source, 0, 0, null);
            g.dispose();
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static BufferedImage addShadowNoAlpha(BufferedImage source, int shadowWidth, Color shadowColor) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            if (shadowWidth <= 0 || 2 * shadowWidth > width || 2 * shadowWidth > height) {
                return source;
            }
            int imageType = BufferedImage.TYPE_INT_RGB;
            BufferedImage shadowImage = new BufferedImage(width, height, imageType);
            float iOpocity;
            float jOpacity;
            float opocity;
            Color newColor;
            Color alphaColor = ColorConvertTools.alphaColor();
            for (int j = 0; j < height; ++j) {
                for (int i = 0; i < width; ++i) {
                    int pixel = source.getRGB(i, j);
                    if (pixel == 0) {
                        shadowImage.setRGB(i, j, alphaColor.getRGB());
                        continue;
                    }
                    iOpocity = jOpacity = 1.0F;
                    if (i < shadowWidth) {
                        iOpocity = 1.0F * i / shadowWidth;
                    } else if (i > width - shadowWidth) {
                        iOpocity = 1.0F * (width - i) / shadowWidth;
                    }
                    if (j < shadowWidth) {
                        jOpacity = 1.0F * j / shadowWidth;
                    } else if (j > height - shadowWidth) {
                        jOpacity = 1.0F * (height - j) / shadowWidth;
                    }
                    opocity = iOpocity * jOpacity;
                    if (opocity == 1.0F) {
                        newColor = shadowColor;
                    } else {
                        newColor = ColorBlendTools.blendColor(shadowColor, opocity, alphaColor);
                    }
                    shadowImage.setRGB(i, j, newColor.getRGB());
                }
            }
            BufferedImage target = new BufferedImage(width + shadowWidth, height + shadowWidth, imageType);
            Graphics2D g = target.createGraphics();
            if (AppVariables.ImageHints != null) {
                g.addRenderingHints(AppVariables.ImageHints);
            }
            g.setColor(alphaColor);
            g.fillRect(0, 0, width + shadowWidth, height + shadowWidth);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            g.drawImage(shadowImage, shadowWidth, shadowWidth, null);
            g.drawImage(source, 0, 0, null);
            g.dispose();
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
