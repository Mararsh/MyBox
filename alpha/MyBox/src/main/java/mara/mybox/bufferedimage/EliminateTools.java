package mara.mybox.bufferedimage;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;
import javafx.scene.shape.Line;
import mara.mybox.data.DoubleLines;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2018-6-27 18:58:57
 * @License Apache License Version 2.0
 */
public class EliminateTools {

    public static BufferedImage drawErase(BufferedImage srcImage, DoubleLines linesData, int strokeWidth) {
        try {
            if (linesData == null || linesData.getPointsSize() == 0 || strokeWidth < 1) {
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
            for (Line line : linesData.lines()) {
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

    public static BufferedImage drawMosaic(BufferedImage source, DoubleLines linesData,
            ImageMosaic.MosaicType mosaicType, int strokeWidth) {
        try {
            if (linesData == null || mosaicType == null || linesData.getPointsSize() == 0 || strokeWidth < 1) {
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
            int pixel;
            for (Line line : linesData.lines()) {
                int x1 = Math.min(width, Math.max(0, (int) line.getStartX()));
                int y1 = Math.min(height, Math.max(0, (int) line.getStartY()));
                int x2 = Math.min(width, Math.max(0, (int) line.getEndX()));
                int y2 = Math.min(height, Math.max(0, (int) line.getEndY()));
                if (x1 == x2) {
                    if (y2 > y1) {
                        for (int x = Math.max(0, x1 - strokeWidth); x <= Math.min(width, x1 + strokeWidth); x++) {
                            for (int y = y1; y <= y2; y++) {
                                pixel = mosaic(source, width, height, x, y, mosaicType, strokeWidth);
                                target.setRGB(x, y, pixel);
                            }
                        }
                    } else {
                        for (int x = Math.max(0, x1 - strokeWidth); x <= Math.min(width, x1 + strokeWidth); x++) {
                            for (int y = y2; y <= y1; y++) {
                                pixel = mosaic(source, width, height, x, y, mosaicType, strokeWidth);
                                target.setRGB(x, y, pixel);
                            }
                        }
                    }
                } else if (x2 > x1) {
                    for (int x = x1; x <= x2; x++) {
                        int y0 = (x - x1) * (y2 - y1) / (x2 - x1) + y1;
                        int offset = (int) (x / (strokeWidth * Math.sqrt(x * x + y0 * y0)));
                        for (int y = Math.max(0, y0 - offset); y <= Math.min(height, y0 + offset); y++) {
                            pixel = mosaic(source, width, height, x, y, mosaicType, strokeWidth);
                            target.setRGB(x, y, pixel);
                        }
                    }
                } else {
                    for (int x = x2; x <= x1; x++) {
                        int y0 = (x - x2) * (y1 - y2) / (x1 - x2) + y2;
                        int offset = (int) (x / (strokeWidth * Math.sqrt(x * x + y0 * y0)));
                        for (int y = Math.max(0, y0 - offset); y <= Math.min(height, y0 + offset); y++) {
                            pixel = mosaic(source, width, height, x, y, mosaicType, strokeWidth);
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
