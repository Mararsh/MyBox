package mara.mybox.fxml;

import java.awt.image.BufferedImage;
import java.util.Random;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import mara.mybox.image.ImageConvertTools;

/**
 * @Author Mara
 * @CreateDate 2018-11-10 19:42:55
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class FxmlCoverTools {

    public static Image makeMosaic(Image image, int leftX, int leftY, int rightX, int rightY, int size) {
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();
        pixelWriter.setPixels(0, 0, (int) image.getWidth(), (int) image.getHeight(), pixelReader, 0, 0);

        for (int y = leftY; y <= rightY; y += size) {
            for (int x = leftX; x <= rightX; x += size) {
                for (int mx = x; mx <= Math.min((int) image.getWidth() - 1, x + size); mx++) {
                    for (int my = y; my <= Math.min((int) image.getHeight() - 1, y + size); my++) {
                        pixelWriter.setColor(mx, my, pixelReader.getColor(x, y));
                    }
                }
            }
        }

        return newImage;

    }

    public static Image makeMosaic(Image image, int centerX, int centerY, int radius, int size) {
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();
        pixelWriter.setPixels(0, 0, (int) image.getWidth(), (int) image.getHeight(), pixelReader, 0, 0);

        for (int x = Math.max(0, centerX - radius); x <= Math.min((int) image.getWidth() - 1, centerX + radius); x += size) {
            for (int y = Math.max(0, centerY - radius); y <= Math.min((int) image.getHeight() - 1, centerY + radius); y += size) {
                for (int mx = x; mx <= Math.min((int) image.getWidth() - 1, x + size); mx++) {
                    for (int my = y; my <= Math.min((int) image.getHeight() - 1, y + size); my++) {
                        long r = Math.round(Math.sqrt((mx - centerX) * (mx - centerX) + (my - centerY) * (my - centerY)));
                        if (r > radius) {
                            continue;
                        }
                        pixelWriter.setColor(mx, my, pixelReader.getColor(x, y));
                    }
                }
            }
        }

        return newImage;

    }

    public static Image makeFrosted(Image image, int leftX, int leftY, int rightX, int rightY, int size) {
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();
        pixelWriter.setPixels(0, 0, (int) image.getWidth(), (int) image.getHeight(), pixelReader, 0, 0);

        for (int y = leftY; y <= rightY; y += size) {
            for (int x = leftX; x <= rightX; x += size) {
                for (int mx = x; mx <= x + size; mx++) {
                    for (int my = y; my <= y + size; my++) {
                        int fx = x + new Random().nextInt(size);
                        int fy = y + new Random().nextInt(size);
                        pixelWriter.setColor(mx, my, pixelReader.getColor(fx, fy));
                    }
                }
            }
        }

        return newImage;

    }

    public static Image makeFrosted(Image image, int centerX, int centerY, int radius, int size) {
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();
        pixelWriter.setPixels(0, 0, (int) image.getWidth(), (int) image.getHeight(), pixelReader, 0, 0);

        for (int x = Math.max(0, centerX - radius); x <= Math.min((int) image.getWidth() - 1, centerX + radius); x += size) {
            for (int y = Math.max(0, centerY - radius); y <= Math.min((int) image.getHeight() - 1, centerY + radius); y += size) {
                for (int mx = x; mx <= Math.min((int) image.getWidth() - 1, x + size); mx++) {
                    for (int my = y; my <= Math.min((int) image.getHeight() - 1, y + size); my++) {
                        long r = Math.round(Math.sqrt((mx - centerX) * (mx - centerX) + (my - centerY) * (my - centerY)));
                        if (r > radius) {
                            continue;
                        }
                        int fx = x + new Random().nextInt(size);
                        int fy = y + new Random().nextInt(size);
                        pixelWriter.setColor(mx, my, pixelReader.getColor(fx, fy));
                    }
                }
            }
        }

        return newImage;

    }

    public static Image addPicture(Image image, Image picture, int x, int y, int w, int h,
            boolean keepRatio, float transparent) {
        if (image == null) {
            return null;
        }
        if (picture == null) {
            return image;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage pic = SwingFXUtils.fromFXImage(picture, null);
        BufferedImage target = ImageConvertTools.addPicture(source, pic, x, y, w, h, keepRatio, transparent);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

}
