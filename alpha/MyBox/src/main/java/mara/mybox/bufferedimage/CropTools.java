package mara.mybox.bufferedimage;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.DoubleShape;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2018-6-27 18:58:57
 * @License Apache License Version 2.0
 */
public class CropTools {

    public static BufferedImage cropInside(BufferedImage source, DoubleShape shape, Color bgColor) {
        try {
            if (shape == null || !shape.isValid()) {
                return source;
            }
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = new BufferedImage(width, height, imageType);
            int bgPixel = bgColor.getRGB();
            for (int j = 0; j < height; ++j) {
                for (int i = 0; i < width; ++i) {
                    if (DoubleShape.contains(shape, i, j)) {
                        target.setRGB(i, j, bgPixel);
                    } else {
                        target.setRGB(i, j, source.getRGB(i, j));
                    }
                }
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return source;
        }
    }

    public static BufferedImage cropOutside(BufferedImage source, DoubleShape shape, Color bgColor) {
        try {
            if (source == null || shape == null || !shape.isValid()) {
                return source;
            }
            int width = source.getWidth();
            int height = source.getHeight();
            Rectangle shapeBound = DoubleShape.getBound(shape);

            int x1 = Math.max(0, (int) Math.ceil(shapeBound.getMinX()));
            int y1 = Math.max(0, (int) Math.ceil(shapeBound.getMinY()));
            if (x1 > width || y1 > height) {
                return null;
            }
            int x2 = Math.min(width, (int) Math.round(shapeBound.getMaxX()));
            int y2 = Math.min(height, (int) Math.round(shapeBound.getMaxY()));
            int w = x2 - x1;
            int h = y2 - y1;
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = new BufferedImage(w, h, imageType);
            int bgPixel = bgColor.getRGB();
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    if (DoubleShape.contains(shape, x1 + x, y1 + y)) {
                        target.setRGB(x, y, source.getRGB(x1 + x, y1 + y));
                    } else {
                        target.setRGB(x, y, bgPixel);
                    }
                }
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return source;
        }
    }

    public static BufferedImage cropOutside(BufferedImage source, DoubleRectangle rectangle) {
        if (rectangle == null) {
            return source;
        }
        return cropOutside(source, rectangle.getX(), rectangle.getY(), rectangle.getMaxX(), rectangle.getMaxY());
    }

    public static BufferedImage cropOutside(BufferedImage source, double x1, double y1, double x2, double y2) {
        return cropOutside(source, DoubleRectangle.xy12(x1, y1, x2, y2), Color.WHITE);
    }

    public static BufferedImage cropOutside(BufferedImage source, double x1, double y1, double x2, double y2, Color bgColor) {
        return cropOutside(source, DoubleRectangle.xy12(x1, y1, x2, y2), bgColor);
    }

    public static BufferedImage sample(BufferedImage source, DoubleRectangle rectangle, int xscale, int yscale) {
        try {
            if (rectangle == null) {
                return ScaleTools.scaleImageByScale(source, xscale, yscale);
            }
            int realXScale = xscale > 0 ? xscale : 1;
            int realYScale = yscale > 0 ? yscale : 1;
            BufferedImage bufferedImage = cropOutside(source, rectangle);
            int width = bufferedImage.getWidth() / realXScale;
            int height = bufferedImage.getHeight() / realYScale;
            bufferedImage = ScaleTools.scaleImageBySize(bufferedImage, width, height);
            return bufferedImage;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return source;
        }
    }

    public static BufferedImage sample(BufferedImage source, DoubleRectangle rectangle, int width) {
        try {
            if (rectangle == null) {
                return ScaleTools.scaleImageWidthKeep(source, width);
            }
            BufferedImage bufferedImage = cropOutside(source, rectangle);
            bufferedImage = ScaleTools.scaleImageWidthKeep(bufferedImage, width);
            return bufferedImage;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return source;
        }
    }

    public static BufferedImage sample(BufferedImage source, int x1, int y1, int x2, int y2, int xscale, int yscale) {
        if (x1 >= x2 || y1 >= y2 || x1 < 0 || x2 < 0 || y1 < 0 || y2 < 0) {
            return null;
        }
        return sample(source, DoubleRectangle.xy12(x1, y1, x2, y2), xscale, yscale);
    }

}
