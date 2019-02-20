package mara.mybox.fxml.image;

import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import mara.mybox.image.ImageScope;

/**
 * @Author Mara
 * @CreateDate 2019-2-15 16:54:15
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageGray extends mara.mybox.image.ImageGray {

    public ImageGray() {
        this.operationType = OperationType.Gray;
    }

    public ImageGray(Image image) {
        this.image = SwingFXUtils.fromFXImage(image, null);
        this.operationType = OperationType.Gray;
    }

    public ImageGray(Image image, ImageScope scope) {
        this.image = SwingFXUtils.fromFXImage(image, null);
        this.operationType = OperationType.Gray;
        this.scope = scope;
    }

    public Image operateFxImage() {
        BufferedImage target = operate();
        if (target == null) {
            return null;
        }
        return SwingFXUtils.toFXImage(target, null);
    }

}
