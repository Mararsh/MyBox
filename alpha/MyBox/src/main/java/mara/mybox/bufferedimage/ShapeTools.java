package mara.mybox.bufferedimage;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.List;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoublePolylines;
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

    public static BasicStroke stroke(ShapeStyle style) {
        return new BasicStroke(style.getStrokeWidth(),
                style.getStrokeLineCapAwt(),
                style.getStrokeLineJoinAwt(),
                style.getStrokeLineLimit(),
                style.isIsStrokeDash() ? style.getStrokeDashAwt() : null,
                0.0F);
    }

    public static BufferedImage drawShape(BufferedImage srcImage, DoubleShape doubleShape,
            ShapeStyle style, PixelsBlend blender) {
        try {
            if (doubleShape == null || doubleShape.isEmpty() || style == null) {
                return srcImage;
            }
            Shape shape = doubleShape.getShape();
            int width = srcImage.getWidth();
            int height = srcImage.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = srcImage;
            Color strokeColor = style.getStrokeColorAwt();
            float strokeWidth = style.getStrokeWidth();
            float opacity = blender.getOpacity();
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
                g.setStroke(stroke(style));
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

    public static BufferedImage drawErase(BufferedImage srcImage, DoublePolylines linesData, ShapeStyle style) {
        try {
            if (linesData == null || linesData.getLinesSize() == 0 || style == null) {
                return srcImage;
            }
            int width = srcImage.getWidth();
            int height = srcImage.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage linesImage = new BufferedImage(width, height, imageType);
            Graphics2D linesg = linesImage.createGraphics();
            if (AppVariables.imageRenderHints != null) {
                linesg.addRenderingHints(AppVariables.imageRenderHints);
            }
            linesg.setStroke(stroke(style));
            linesg.setBackground(Color.WHITE);
            linesg.setColor(Color.BLACK);
            for (List<DoublePoint> line : linesData.getLines()) {
                Path2D.Double path = new Path2D.Double();
                DoublePoint p = line.get(0);
                path.moveTo(p.getX(), p.getY());
                for (int i = 1; i < line.size(); i++) {
                    p = line.get(i);
                    path.lineTo(p.getX(), p.getY());
                }
                linesg.draw(path);
            }
            linesg.dispose();
            BufferedImage target = new BufferedImage(width, height, imageType);
            int black = Color.BLACK.getRGB();
            for (int j = 0; j < height; ++j) {
                for (int i = 0; i < width; ++i) {
                    if (linesImage.getRGB(i, j) == black) {
                        target.setRGB(i, j, 0);
                    } else {
                        target.setRGB(i, j, srcImage.getRGB(i, j));
                    }
                }
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return srcImage;
        }
    }

}
