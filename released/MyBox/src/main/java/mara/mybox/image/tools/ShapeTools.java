package mara.mybox.image.tools;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;
import javafx.scene.shape.Line;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoublePolylines;
import mara.mybox.data.DoubleShape;
import mara.mybox.data.ShapeStyle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.image.data.ImageMosaic;
import mara.mybox.image.data.PixelsBlend;
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

    public static BufferedImage drawShape(FxTask task, BufferedImage srcImage,
            DoubleShape doubleShape, ShapeStyle style, PixelsBlend blender) {
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
            if (AppVariables.ImageHints != null) {
                g.addRenderingHints(AppVariables.ImageHints);
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
            if (task != null && !task.isWorking()) {
                return null;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            int strokePixel = strokeColor.getRGB();
            int fillPixel = fillColor.getRGB();
            int realStrokePixel = style.getStrokeColorAwt().getRGB();
            int realFillPixel = style.getFillColorAwt().getRGB();
            for (int j = 0; j < height; ++j) {
                if (task != null && !task.isWorking()) {
                    return null;
                }
                for (int i = 0; i < width; ++i) {
                    if (task != null && !task.isWorking()) {
                        return null;
                    }
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

    public static BufferedImage drawErase(FxTask task, BufferedImage srcImage,
            DoublePolylines linesData, ShapeStyle style) {
        try {
            if (linesData == null || linesData.getLinesSize() == 0 || style == null) {
                return srcImage;
            }
            int width = srcImage.getWidth();
            int height = srcImage.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage linesImage = new BufferedImage(width, height, imageType);
            Graphics2D linesg = linesImage.createGraphics();
            if (AppVariables.ImageHints != null) {
                linesg.addRenderingHints(AppVariables.ImageHints);
            }
            linesg.setStroke(stroke(style));
            linesg.setBackground(Color.WHITE);
            linesg.setColor(Color.BLACK);
            for (List<DoublePoint> line : linesData.getLines()) {
                if (task != null && !task.isWorking()) {
                    return null;
                }
                Path2D.Double path = new Path2D.Double();
                DoublePoint p = line.get(0);
                path.moveTo(p.getX(), p.getY());
                for (int i = 1; i < line.size(); i++) {
                    p = line.get(i);
                    path.lineTo(p.getX(), p.getY());
                }
                linesg.draw(path);
            }
            if (task != null && !task.isWorking()) {
                return null;
            }
            linesg.dispose();
            BufferedImage target = new BufferedImage(width, height, imageType);
            int black = Color.BLACK.getRGB();
            for (int j = 0; j < height; ++j) {
                if (task != null && !task.isWorking()) {
                    return null;
                }
                for (int i = 0; i < width; ++i) {
                    if (task != null && !task.isWorking()) {
                        return null;
                    }
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

    public static boolean inLine(Line line, int x, int y) {
        double d = (x - line.getStartX()) * (line.getStartY() - line.getEndY())
                - ((line.getStartX() - line.getEndX()) * (y - line.getStartY()));
        return Math.abs(d) < 1.0E-4
                && (x >= Math.min(line.getStartX(), line.getEndX())
                && x <= Math.max(line.getStartX(), line.getEndX()))
                && (y >= Math.min(line.getStartY(), line.getEndY()))
                && (y <= Math.max(line.getStartY(), line.getEndY()));
    }

    public static int mosaic(BufferedImage source,
            int imageWidth, int imageHeight, int x, int y, ImageMosaic.MosaicType type, int intensity) {
        int newColor;
        if (type == ImageMosaic.MosaicType.Mosaic) {
            int mx = Math.max(0, Math.min(imageWidth - 1, x - x % intensity));
            int my = Math.max(0, Math.min(imageHeight - 1, y - y % intensity));
            newColor = source.getRGB(mx, my);
        } else {
            int fx = Math.max(0, Math.min(imageWidth - 1, x - new Random().nextInt(intensity)));
            int fy = Math.max(0, Math.min(imageHeight - 1, y - new Random().nextInt(intensity)));
            newColor = source.getRGB(fx, fy);
        }
        return newColor;
    }

    public static BufferedImage drawMosaic(FxTask task, BufferedImage source,
            DoublePolylines linesData, ImageMosaic.MosaicType mosaicType, int strokeWidth, int intensity) {
        try {
            if (linesData == null || mosaicType == null || linesData.getLinesSize() == 0 || strokeWidth < 1 || intensity < 1) {
                return source;
            }
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = new BufferedImage(width, height, imageType);
            Graphics2D gt = target.createGraphics();
            if (AppVariables.ImageHints != null) {
                gt.addRenderingHints(AppVariables.ImageHints);
            }
            gt.drawImage(source, 0, 0, width, height, null);
            gt.dispose();
            if (task != null && !task.isWorking()) {
                return null;
            }
            int pixel;
            int w = strokeWidth / 2;
            for (Line line : linesData.getLineList()) {
                if (task != null && !task.isWorking()) {
                    return null;
                }
                int x1 = Math.min(width, Math.max(0, (int) line.getStartX()));
                int y1 = Math.min(height, Math.max(0, (int) line.getStartY()));
                int x2 = Math.min(width, Math.max(0, (int) line.getEndX()));
                int y2 = Math.min(height, Math.max(0, (int) line.getEndY()));
                Polygon polygon = new Polygon();
                polygon.addPoint(x1 - w, y1);
                polygon.addPoint(x1, y1 - w);
                polygon.addPoint(x1 + w, y1);
                polygon.addPoint(x2 - w, y2);
                polygon.addPoint(x2, y2 + w);
                polygon.addPoint(x2 + w, y2);
                Rectangle rect = polygon.getBounds();
                int bx = (int) rect.getX();
                int by = (int) rect.getY();
                int bw = (int) rect.getWidth();
                int bh = (int) rect.getHeight();
                for (int x = bx; x < bx + bw; x++) {
                    if (task != null && !task.isWorking()) {
                        return null;
                    }
                    for (int y = by; y < by + bh; y++) {
                        if (task != null && !task.isWorking()) {
                            return null;
                        }
                        if (polygon.contains(x, y)) {
                            pixel = mosaic(source, width, height, x, y, mosaicType, intensity);
                            target.setRGB(x, y, pixel);
                        }
                    }
                }
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return source;
        }
    }

}
