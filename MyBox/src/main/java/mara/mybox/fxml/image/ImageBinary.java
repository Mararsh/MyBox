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
public class ImageBinary extends mara.mybox.image.ImageBinary {

    public ImageBinary() {
        intPara1 = -1;
        grayed = false;
        this.operationType = OperationType.BlackOrWhite;
    }

    public ImageBinary(Image image) {
        this.image = SwingFXUtils.fromFXImage(image, null);
        this.operationType = OperationType.BlackOrWhite;
        intPara1 = -1;
        grayed = false;
    }

    public ImageBinary(Image image, ImageScope scope) {
        this.image = SwingFXUtils.fromFXImage(image, null);
        this.scope = scope;
        this.operationType = OperationType.BlackOrWhite;
        intPara1 = -1;
        grayed = false;
    }

    public ImageBinary(Image image, int threshold) {
        this.image = SwingFXUtils.fromFXImage(image, null);
        this.scope = null;
        this.operationType = OperationType.BlackOrWhite;
        intPara1 = threshold;
        grayed = false;
    }

    public ImageBinary(Image image, ImageScope scope, int threshold) {
        this.image = SwingFXUtils.fromFXImage(image, null);
        this.operationType = OperationType.BlackOrWhite;
        this.scope = scope;
        intPara1 = threshold;
        grayed = false;
    }

    public Image operateFxImage() {
        BufferedImage target = operate();
        if (target == null) {
            return null;
        }
        return SwingFXUtils.toFXImage(target, null);
    }

}
