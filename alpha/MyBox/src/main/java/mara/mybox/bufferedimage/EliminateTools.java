package mara.mybox.bufferedimage;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Random;
import javafx.scene.shape.Line;
import mara.mybox.data.DoublePolylines;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2018-6-27 18:58:57
 * @License Apache License Version 2.0
 */
public class EliminateTools {

    public static BufferedImage drawErase2(BufferedImage srcImage, DoublePolylines linesData, int strokeWidth) {
        try {
            if (linesData == null || linesData.getLinesSize() == 0 || strokeWidth < 1) {
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
            linesg.setBackground(Color.WHITE);
            linesg.setColor(Color.BLACK);
            linesg.setStroke(new BasicStroke(strokeWidth));
            for (Line line : linesData.getLineList()) {
                int x1 = Math.min(width, Math.max(0, (int) line.getStartX()));
                int y1 = Math.min(height, Math.max(0, (int) line.getStartY()));
                int x2 = Math.min(width, Math.max(0, (int) line.getEndX()));
                int y2 = Math.min(height, Math.max(0, (int) line.getEndY()));
                linesg.drawLine(x1, y1, x2, y2);
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

    public static BufferedImage drawMosaic(BufferedImage source, DoublePolylines linesData,
            ImageMosaic.MosaicType mosaicType, int strokeWidth, int intensity) {
        try {
            if (linesData == null || mosaicType == null || linesData.getLinesSize() == 0
                    || strokeWidth < 1 || intensity < 1) {
                return source;
            }
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = new BufferedImage(width, height, imageType);
            Graphics2D gt = target.createGraphics();
            if (AppVariables.imageRenderHints != null) {
                gt.addRenderingHints(AppVariables.imageRenderHints);
            }
            gt.drawImage(source, 0, 0, width, height, null);
            gt.dispose();
            int pixel, w = strokeWidth / 2;
            for (Line line : linesData.getLineList()) {
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
                    for (int y = by; y < by + bh; y++) {
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

    protected static int mosaic(BufferedImage source, int imageWidth, int imageHeight, int x, int y,
            ImageMosaic.MosaicType type, int intensity) {
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

    public static boolean inLine(Line line, int x, int y) {
        double d = (x - line.getStartX()) * (line.getStartY() - line.getEndY()) - ((line.getStartX() - line.getEndX()) * (y - line.getStartY()));
        return Math.abs(d) < 1.0E-4 && (x >= Math.min(line.getStartX(), line.getEndX())
                && x <= Math.max(line.getStartX(), line.getEndX()))
                && (y >= Math.min(line.getStartY(), line.getEndY()))
                && (y <= Math.max(line.getStartY(), line.getEndY()));
    }

}
