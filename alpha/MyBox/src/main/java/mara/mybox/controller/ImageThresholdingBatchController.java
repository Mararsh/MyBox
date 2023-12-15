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
public class ImageThresholdingBatchController extends BaseImageEditBatchController {

    protected PixelsOperation pixelsOperation;

    @FXML
    protected ControlImageThresholding thresholdingController;

    public ImageThresholdingBatchController() {
        baseTitle = message("ImageBatch") + " - " + message("Thresholding");
    }

    @Override
    public boolean makeMoreParameters() {
        pixelsOperation = thresholdingController.pickValues();
        return pixelsOperation != null && super.makeMoreParameters();
    }

    @Override
    protected BufferedImage handleImage(BufferedImage source) {
        return pixelsOperation.setImage(source).setTask(task).operate();
    }

}
