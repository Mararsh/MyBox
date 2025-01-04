package mara.mybox.fxml.image;

import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import mara.mybox.fxml.FxTask;

/**
 * @Author Mara
 * @CreateDate 2018-11-13 12:38:14
 * @License Apache License Version 2.0
 */
public class TransformTools {

    public static Image horizontalImage(FxTask task, Image image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage(width, height);
        PixelWriter pixelWriter = newImage.getPixelWriter();
        for (int j = 0; j < height; ++j) {
            if (task != null && !task.isWorking()) {
                return null;
            }
            int l = 0;
            int r = width - 1;
            while (l <= r) {
                if (task != null && !task.isWorking()) {
                    return null;
                }
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

    public static Image verticalImage(FxTask task, Image image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage(width, height);
        PixelWriter pixelWriter = newImage.getPixelWriter();
        for (int i = 0; i < width; ++i) {
            if (task != null && !task.isWorking()) {
                return null;
            }
            int t = 0;
            int b = height - 1;
            while (t <= b) {
                if (task != null && !task.isWorking()) {
                    return null;
                }
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

    public static Image rotateImage(FxTask task, Image image, int angle, boolean cutMargins) {
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = mara.mybox.image.tools.TransformTools.rotateImage(task, source, angle, cutMargins);
        if (target == null || (task != null && !task.isWorking())) {
            return null;
        }
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image shearImage(FxTask task, Image image, float shearX, float shearY, boolean cutMargins) {
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = mara.mybox.image.tools.TransformTools.shearImage(task, source, shearX, shearY, cutMargins);
        if (target == null || (task != null && !task.isWorking())) {
            return null;
        }
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

}
