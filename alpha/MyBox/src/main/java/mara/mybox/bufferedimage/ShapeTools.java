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
            if (srcImage == null || doubleShape == null || doubleShape.isEmpty() || style == null) {
                return srcImage;
            }
            Shape shape = doubleShape.getShape();
            int width = srcImage.getWidth();
            int height = srcImage.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = srcImage;
            if (style.isIsFillColor()) {
                BufferedImage foreImage = new BufferedImage(width, height, imageType);
                Graphics2D g = foreImage.createGraphics();
                if (AppVariables.imageRenderHints != null) {
                    g.addRenderingHints(AppVariables.imageRenderHints);
                }
                Color fillColor = style.getFillColorAwt();
                int fillPixel = fillColor.getRGB();
                if (fillColor.getRGB() == 0) {
                    g.setBackground(Color.WHITE);
                    g.setColor(Color.BLACK);
                    fillPixel = Color.BLACK.getRGB();
                } else {
                    g.setBackground(Colors.TRANSPARENT);
                    g.setColor(fillColor);
                }
                g.fill(shape);
                g.dispose();
                BufferedImage backImage = srcImage;
                target = new BufferedImage(width, height, imageType);
                for (int j = 0; j < height; ++j) {
                    for (int i = 0; i < width; ++i) {
                        int backPixel = backImage.getRGB(i, j);
                        int forePixel = foreImage.getRGB(i, j);
                        if (forePixel == fillPixel) {
                            target.setRGB(i, j, blender.blend(forePixel, backPixel));
                        } else {
                            target.setRGB(i, j, backPixel);
                        }
                    }
                }
            }

            float strokeWidth = style.getStrokeWidth();
            if (strokeWidth > 0) {
                BufferedImage backImage = target;
                BufferedImage foreImage = new BufferedImage(width, height, imageType);
                Graphics2D g = foreImage.createGraphics();
                if (AppVariables.imageRenderHints != null) {
                    g.addRenderingHints(AppVariables.imageRenderHints);
                }
                g.setStroke(stroke(style));
                Color strokeColor = style.getStrokeColorAwt();
                int strokePixel = strokeColor.getRGB();
                if (strokePixel == 0) {
                    g.setBackground(Color.WHITE);
                    g.setColor(Color.BLACK);
                    strokePixel = Color.BLACK.getRGB();
                } else {
                    g.setBackground(Colors.TRANSPARENT);
                    g.setColor(strokeColor);
                }
                g.draw(shape);
                g.dispose();
                target = new BufferedImage(width, height, imageType);
                for (int j = 0; j < height; ++j) {
                    for (int i = 0; i < width; ++i) {
                        int backPixel = backImage.getRGB(i, j);
                        int forePixel = foreImage.getRGB(i, j);
                        if (forePixel == strokePixel) {
                            target.setRGB(i, j, blender.blend(forePixel, backPixel));
                        } else {
                            target.setRGB(i, j, backPixel);
                        }
                    }
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
