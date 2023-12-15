package mara.mybox.controller;

import java.awt.image.BufferedImage;
import javafx.fxml.FXML;
import mara.mybox.bufferedimage.PixelsOperation;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2018-9-23
 * @License Apache License Version 2.0
 */
public class ImageSepiaBatchController extends BaseImageEditBatchController {

    protected PixelsOperation pixelsOperation;

    @FXML
    protected ControlImageSepia sepiaController;

    public ImageSepiaBatchController() {
        baseTitle = message("ImageBatch") + " - " + message("Sepia");
    }

    @Override
    public boolean makeMoreParameters() {
        if (!super.makeMoreParameters()) {
            return false;
        }
        pixelsOperation = sepiaController.pickValues();
        return pixelsOperation != null;
    }

    @Override
    protected BufferedImage handleImage(BufferedImage source) {
        return pixelsOperation.setImage(source).setTask(task).operateImage();
    }

}
