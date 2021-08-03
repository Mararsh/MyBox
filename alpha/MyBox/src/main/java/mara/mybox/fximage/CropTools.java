package mara.mybox.fximage;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.DoubleShape;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2018-11-13 12:38:14
 * @License Apache License Version 2.0
 */
public class CropTools {

    public static Image cropInsideFx(Image image, DoubleShape shape, Color bgColor) {
        if (image == null || shape == null || !shape.isValid() || bgColor == null) {
            return image;
        }
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage(width, height);
        PixelWriter pixelWriter = newImage.getPixelWriter();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (shape.include(x, y)) {
                    pixelWriter.setColor(x, y, bgColor);
                } else {
                    pixelWriter.setColor(x, y, pixelReader.getColor(x, y));
                }
            }
        }
        return newImage;
    }

    public static Image cropOutsideFx(Image image, DoubleShape shape, Color bgColor) {
        try {
            if (image == null || shape == null || !shape.isValid() || bgColor == null) {
                return image;
            }
            int width = (int) image.getWidth();
            int height = (int) image.getHeight();
            DoubleRectangle bound = shape.getBound();
            int x1 = (int) Math.round(Math.max(0, bound.getSmallX()));
            int y1 = (int) Math.round(Math.max(0, bound.getSmallY()));
            if (x1 >= width || y1 >= height) {
                return image;
            }
            int x2 = (int) Math.round(Math.min(width - 1, bound.getBigX()));
            int y2 = (int) Math.round(Math.min(height - 1, bound.getBigY()));
            int w = x2 - x1 + 1;
            int h = y2 - y1 + 1;
            PixelReader pixelReader = image.getPixelReader();
            WritableImage newImage = new WritableImage(w, h);
            PixelWriter pixelWriter = newImage.getPixelWriter();
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    if (shape.include(x1 + x, y1 + y)) {
                        pixelWriter.setColor(x, y, pixelReader.getColor(x1 + x, y1 + y));
                    } else {
                        pixelWriter.setColor(x, y, bgColor);
                    }
                }
            }
            return newImage;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return image;
        }
    }

    public static Image cropOutsideFx(Image image, DoubleRectangle rect) {
        return cropOutsideFx(image, rect, Color.WHITE);
    }

    public static Image cropOutsideFx(Image image, double x1, double y1, double x2, double y2) {
        return cropOutsideFx(image, new DoubleRectangle(x1, y1, x2, y2), Color.WHITE);
    }

}
