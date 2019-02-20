package mara.mybox.fxml.image;

import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

/**
 * @Author Mara
 * @CreateDate 2019-2-15 16:54:15
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageContrast extends mara.mybox.image.ImageContrast {

    public ImageContrast() {
        this.operationType = OperationType.Contrast;
    }

    public ImageContrast(Image image) {
        this.image = SwingFXUtils.fromFXImage(image, null);
        this.operationType = OperationType.Contrast;
    }

    public ImageContrast(Image image, ContrastAlgorithm algorithm) {
        this.image = SwingFXUtils.fromFXImage(image, null);
        this.operationType = OperationType.Contrast;
        this.algorithm = algorithm;
    }

    public Image operateFxImage() {
        BufferedImage target = super.operate();
        if (target == null) {
            return null;
        }
        return SwingFXUtils.toFXImage(target, null);
    }

    public static Image grayHistogramEqualization(Image grayImage) {
        BufferedImage image = SwingFXUtils.fromFXImage(grayImage, null);
        image = mara.mybox.image.ImageContrast.grayHistogramEqualization(image);
        return SwingFXUtils.toFXImage(image, null);
    }

    public static Image brightnessHistogramEqualization(Image colorImage) {
        BufferedImage image = SwingFXUtils.fromFXImage(colorImage, null);
        image = mara.mybox.image.ImageContrast.brightnessHistogramEqualization(image);
        return SwingFXUtils.toFXImage(image, null);
    }

}
