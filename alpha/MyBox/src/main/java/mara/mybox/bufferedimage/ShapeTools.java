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

/**
 * @Author Mara
 * @CreateDate 2018-6-27 18:58:57
 * @License Apache License Version 2.0
 */
public class ShapeTools {

    public static BasicStroke stroke(ShapeStyle style) {
        if (style == null) {
            return new BasicStroke();
        } else {
            return new BasicStroke(style.getStrokeWidth(),
                    style.getStrokeLineCapAwt(),
                    style.getStrokeLineJoinAwt(),
                    style.getStrokeLineLimit(),
                    style.isIsStrokeDash() ? style.getStrokeDashAwt() : null,
                    0.0F);
        }
    }

    public static BufferedImage drawShape(BufferedImage srcImage, DoubleShape doubleShape,
            ShapeStyle style, PixelsBlend blender) {
        try {
            if (srcImage == null || doubleShape == null || doubleShape.isEmpty()
                    || style == null || blender == null) {
                return srcImage;
            }
            float strokeWidth = style.getStrokeWidth();
            boolean showStroke = strokeWidth > 0;
            boolean fill = style.isIsFillColor();
            if (!fill && !showStroke) {
                return srcImage;
            }
            int width = srcImage.getWidth();
            int height = srcImage.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            Color strokeColor = Color.WHITE;
            Color fillColor = Color.BLACK;
            Color backgroundColor = Color.RED;
            BufferedImage shapeImage = new BufferedImage(width, height, imageType);
            Graphics2D g = shapeImage.createGraphics();
            if (AppVariables.imageRenderHints != null) {
                g.addRenderingHints(AppVariables.imageRenderHints);
            }
            g.setStroke(stroke(style));
            g.setBackground(backgroundColor);
            Shape shape = doubleShape.getShape();
            if (fill) {
                g.setColor(fillColor);
                g.fill(shape);
            }
            if (showStroke) {
                g.setColor(strokeColor);
                g.draw(shape);
            }
            g.dispose();
            BufferedImage target = new BufferedImage(width, height, imageType);
            int strokePixel = strokeColor.getRGB();
            int fillPixel = fillColor.getRGB();
            int realStrokePixel = style.getStrokeColorAwt().getRGB();
            int realFillPixel = style.getFillColorAwt().getRGB();
            for (int j = 0; j < height; ++j) {
                for (int i = 0; i < width; ++i) {
                    int srcPixel = srcImage.getRGB(i, j);
                    int shapePixel = shapeImage.getRGB(i, j);
                    if (shapePixel == strokePixel) {
                        target.setRGB(i, j, blender.blend(realStrokePixel, srcPixel));
                    } else if (shapePixel == fillPixel) {
                        target.setRGB(i, j, blender.blend(realFillPixel, srcPixel));
                    } else {
                        target.setRGB(i, j, srcPixel);
                    }
                }
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
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
