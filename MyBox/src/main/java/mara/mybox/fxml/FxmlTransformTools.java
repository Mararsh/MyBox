package mara.mybox.fxml;

import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import mara.mybox.image.ImageTransformTools;

/**
 * @Author Mara
 * @CreateDate 2018-11-10 19:41:09
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class FxmlTransformTools {

    public static Image rotateImage(Image image, int angle) {
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = ImageTransformTools.rotateImage(source, angle);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image horizontalImage(Image image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage(width, height);
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int j = 0; j < height; j++) {
            int l = 0, r = width - 1;
            while (l <= r) {
                Color cl = pixelReader.getColor(l, j);
                Color cr = pixelReader.getColor(r, j);
                pixelWriter.setColor(l, j, cr);
                pixelWriter.setColor(r, j, cl);
                l++;
                r--;
            }
        }
        return newImage;
    }

    public static Image verticalImage(Image image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage(width, height);
        PixelWriter pixelWriter = newImage.getPixelWriter();
        for (int i = 0; i < width; i++) {
            int t = 0, b = height - 1;
            while (t <= b) {
                Color ct = pixelReader.getColor(i, t);
                Color cb = pixelReader.getColor(i, b);
                pixelWriter.setColor(i, t, cb);
                pixelWriter.setColor(i, b, ct);
                t++;
                b--;
            }
        }
        return newImage;
    }

    public static Image shearImage(Image image, float shearX, float shearY) {
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = ImageTransformTools.shearImage(source, shearX, shearY);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

}
