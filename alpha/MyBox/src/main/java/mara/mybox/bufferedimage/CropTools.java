package mara.mybox.bufferedimage;

import java.awt.Color;
import java.awt.image.BufferedImage;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.DoubleShape;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.value.Colors;

/**
 * @Author Mara
 * @CreateDate 2018-6-27 18:58:57
 * @License Apache License Version 2.0
 */
public class CropTools {

    public static BufferedImage cropInside(FxTask task, BufferedImage source, DoubleRectangle rect, Color bgColor) {
        try {
            if (rect == null || rect.isEmpty()) {
                return source;
            }
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = new BufferedImage(width, height, imageType);
            int bgPixel = bgColor.getRGB();
            for (int j = 0; j < height; ++j) {
                if (task != null && !task.isWorking()) {
                    return null;
                }
                for (int i = 0; i < width; ++i) {
                    if (task != null && !task.isWorking()) {
                        return null;
                    }
                    if (DoubleShape.contains(rect, i, j)) {
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

    public static BufferedImage cropOutside(FxTask task, BufferedImage source, DoubleRectangle rect, Color bgColor) {
        try {
            if (source == null || rect == null || rect.isEmpty()) {
                return source;
            }
            int width = source.getWidth();
            int height = source.getHeight();
            int x1 = Math.max(0, (int) Math.ceil(rect.getX()));
            int y1 = Math.max(0, (int) Math.ceil(rect.getY()));
            if (x1 > width || y1 > height) {
                return null;
            }
            int x2 = Math.min(width, (int) Math.floor(rect.getMaxX()));
            int y2 = Math.min(height, (int) Math.floor(rect.getMaxY()));
            int w = x2 - x1;
            int h = y2 - y1;
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = new BufferedImage(w, h, imageType);
            int bgPixel = bgColor.getRGB();
            for (int y = 0; y < h; y++) {
                if (task != null && !task.isWorking()) {
                    return null;
                }
                for (int x = 0; x < w; x++) {
                    if (task != null && !task.isWorking()) {
                        return null;
                    }
                    if (rect.contains(x1 + x, y1 + y)) {
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

    public static BufferedImage cropOutside(FxTask task, BufferedImage source, DoubleRectangle rect) {
        return cropOutside(task, source, rect, Colors.TRANSPARENT);
    }

    public static BufferedImage cropOutside(FxTask task, BufferedImage source, double x1, double y1, double x2, double y2) {
        return cropOutside(task, source, DoubleRectangle.xy12(x1, y1, x2, y2), Colors.TRANSPARENT);
    }

    public static BufferedImage sample(FxTask task, BufferedImage source, DoubleRectangle rectangle, int xscale, int yscale) {
        try {
            if (rectangle == null) {
                return ScaleTools.scaleImageByScale(source, xscale, yscale);
            }
            int realXScale = xscale > 0 ? xscale : 1;
            int realYScale = yscale > 0 ? yscale : 1;
            BufferedImage bufferedImage = cropOutside(task, source, rectangle);
            if (bufferedImage == null) {
                return null;
            }
            int width = bufferedImage.getWidth() / realXScale;
            int height = bufferedImage.getHeight() / realYScale;
            bufferedImage = ScaleTools.scaleImageBySize(bufferedImage, width, height);
            return bufferedImage;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return source;
        }
    }

    public static BufferedImage sample(FxTask task, BufferedImage source, DoubleRectangle rectangle, int width) {
        try {
            if (rectangle == null) {
                return ScaleTools.scaleImageWidthKeep(source, width);
            }
            BufferedImage bufferedImage = cropOutside(task, source, rectangle);
            if (bufferedImage == null) {
                return null;
            }
            bufferedImage = ScaleTools.scaleImageWidthKeep(bufferedImage, width);
            return bufferedImage;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return source;
        }
    }

    public static BufferedImage sample(FxTask task, BufferedImage source, int x1, int y1, int x2, int y2, int xscale, int yscale) {
        if (x1 >= x2 || y1 >= y2 || x1 < 0 || x2 < 0 || y1 < 0 || y2 < 0) {
            return null;
        }
        return sample(task, source, DoubleRectangle.xy12(x1, y1, x2, y2), xscale, yscale);
    }

}
