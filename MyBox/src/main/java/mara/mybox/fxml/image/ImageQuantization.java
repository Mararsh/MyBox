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
public class ImageQuantization extends mara.mybox.image.ImageQuantization {

    public ImageQuantization() {
        this.operationType = OperationType.Quantization;
    }

    public ImageQuantization(Image image) {
        this.image = SwingFXUtils.fromFXImage(image, null);
        this.operationType = OperationType.Quantization;
    }

    public Image operateFxImage() {
        BufferedImage target = operate();
        if (target == null) {
            return null;
        }
        return SwingFXUtils.toFXImage(target, null);
    }

}
