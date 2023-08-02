package mara.mybox.bufferedimage;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.DoubleShape;
import mara.mybox.data.ShapeStyle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Colors;

/**
 * @Author Mara
 * @CreateDate 2018-6-27 18:58:57
 * @License Apache License Version 2.0
 */
public class ShapeTools {

    public static BasicStroke stroke(DoubleShape shape, ShapeStyle style) {
        return new BasicStroke(style.getStrokeWidth(),
                style.getLineCapAwt(),
                BasicStroke.JOIN_MITER, 1.0F,
                style.isIsStrokeDash() ? style.getStrokeDashAwt() : null,
                0.0F);
    }

    public static BufferedImage drawShape(BufferedImage srcImage, DoubleShape doubleShape,
            ShapeStyle style, PixelsBlend blender) {
        try {
            if (doubleShape == null || !doubleShape.isValid()) {
                return srcImage;
            }
            int width = srcImage.getWidth();
            int height = srcImage.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = srcImage;
            Color strokeColor = style.getStrokeColorAwt();
            float strokeWidth = style.getStrokeWidth();
            float opacity = blender.getOpacity();
            if (doubleShape instanceof DoubleRectangle) {
                int arcWidth = style.getRoundArc();
                if (arcWidth > 0) {
                    arcWidth = Math.min(height - 1, arcWidth);
                }
                ((DoubleRectangle) doubleShape).setRound(arcWidth);
            }
            Shape shape = doubleShape.getShape();
            if (strokeWidth > 0) {
                BufferedImage foreImage = new BufferedImage(width, height, imageType);
                Graphics2D g = foreImage.createGraphics();
                if (AppVariables.imageRenderHints != null) {
                    g.addRenderingHints(AppVariables.imageRenderHints);
                }
                if (strokeColor.getRGB() == 0) {
                    g.setBackground(Color.WHITE);
                    g.setColor(Color.BLACK);
                } else {
                    g.setBackground(Colors.TRANSPARENT);
                    g.setColor(strokeColor);
                }
                g.setStroke(stroke(doubleShape, style));
                g.draw(shape);
                g.dispose();
                if (strokeColor.getRGB() == 0) {
                    target = new BufferedImage(width, height, imageType);
                    int black = Color.BLACK.getRGB();
                    int alpha = 255 - (int) (opacity * 255);
                    for (int j = 0; j < height; ++j) {
                        for (int i = 0; i < width; ++i) {
                            if (foreImage.getRGB(i, j) == black) {
                                target.setRGB(i, j, ColorConvertTools.setAlpha(srcImage.getRGB(i, j), alpha));
                            } else {
                                target.setRGB(i, j, srcImage.getRGB(i, j));
                            }
                        }
                    }
                } else {
                    target = PixelsBlend.blend(foreImage, srcImage, 0, 0, blender);
                }
            }
            if (style.isIsFillColor()) {
                BufferedImage foreImage = new BufferedImage(width, height, imageType);
                Graphics2D g = foreImage.createGraphics();
                if (AppVariables.imageRenderHints != null) {
                    g.addRenderingHints(AppVariables.imageRenderHints);
                }
                Color fillColor = style.getFillColorAwt();
                if (fillColor.getRGB() == 0) {
                    g.setBackground(Color.WHITE);
                    g.setColor(Color.BLACK);
                } else {
                    g.setBackground(Colors.TRANSPARENT);
                    g.setColor(fillColor);
                }
                g.fill(shape);
                g.dispose();
                BufferedImage backImage = target;
                if (fillColor.getRGB() == 0) {
                    target = new BufferedImage(width, height, imageType);
                    int black = Color.BLACK.getRGB();
                    int alpha = 255 - (int) (opacity * 255);
                    for (int j = 0; j < height; ++j) {
                        for (int i = 0; i < width; ++i) {
                            if (foreImage.getRGB(i, j) == black) {
                                target.setRGB(i, j, ColorConvertTools.setAlpha(srcImage.getRGB(i, j), alpha));
                            } else {
                                target.setRGB(i, j, srcImage.getRGB(i, j));
                            }
                        }
                    }

                } else {
                    target = PixelsBlend.blend(foreImage, backImage, 0, 0, blender);
                }
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return srcImage;
        }
    }

}
