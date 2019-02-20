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
public class PixelsOperation extends mara.mybox.image.PixelsOperation {

    public PixelsOperation() {
    }

    public PixelsOperation(Image image) {
        this.image = SwingFXUtils.fromFXImage(image, null);
    }

    public PixelsOperation(Image image, ImageScope scope, OperationType operationType) {
        this.image = SwingFXUtils.fromFXImage(image, null);
        this.operationType = operationType;
        this.scope = scope;
    }

    public PixelsOperation(Image image, ImageScope scope,
            OperationType operationType, ColorActionType colorActionType) {
        this.image = SwingFXUtils.fromFXImage(image, null);
        this.operationType = operationType;
        this.scope = scope;
        this.colorActionType = colorActionType;
    }

    public Image operateFxImage() {
        BufferedImage target = operate();
        if (target == null) {
            return null;
        }
        return SwingFXUtils.toFXImage(target, null);
    }

}
