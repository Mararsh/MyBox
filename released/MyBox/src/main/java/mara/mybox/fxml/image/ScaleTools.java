package mara.mybox.fxml.image;

import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

/**
 * @Author Mara
 * @CreateDate 2018-11-13 12:38:14
 * @License Apache License Version 2.0
 */
public class ScaleTools {

    public static Image scaleImage(Image image, float scale) {
        int targetW = (int) Math.round(image.getWidth() * scale);
        int targetH = (int) Math.round(image.getHeight() * scale);
        return scaleImage(image, targetW, targetH);
    }

    public static Image scaleImage(Image image, int width) {
        if (width <= 0 || width == image.getWidth()) {
            return image;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = mara.mybox.image.tools.ScaleTools.scaleImageWidthKeep(source, width);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image scaleImage(Image image, int width, int height) {
        if (width == image.getWidth() && height == image.getHeight()) {
            return image;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = mara.mybox.image.tools.ScaleTools.scaleImageBySize(source, width, height);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image scaleImage(Image image, int width, int height, boolean keepRatio, int keepType) {
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = mara.mybox.image.tools.ScaleTools.scaleImage(source, width, height, keepRatio, keepType);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image demoImage(Image image) {
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = mara.mybox.image.tools.ScaleTools.demoImage(source);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

}
