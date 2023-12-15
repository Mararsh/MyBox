package mara.mybox.controller;

import java.awt.image.BufferedImage;
import javafx.fxml.FXML;
import mara.mybox.bufferedimage.ImageMosaic;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2018-9-23
 * @License Apache License Version 2.0
 */
public class ImageMosaicBatchController extends BaseImageEditBatchController {

    protected ImageMosaic mosaic;

    @FXML
    protected ControlImageMosaic mosaicController;

    public ImageMosaicBatchController() {
        baseTitle = message("ImageBatch") + " - " + message("Mosaic");
    }

    @Override
    public boolean makeMoreParameters() {
        if (!super.makeMoreParameters()) {
            return false;
        }
        mosaic = mosaicController.pickValues(false);
        return mosaic != null;
    }

    @Override
    protected BufferedImage handleImage(BufferedImage source) {
        try {
            mosaic.setImage(source);
            return mosaic.init().setTask(task).operateImage();
        } catch (Exception e) {
            displayError(e.toString());
            return null;
        }
    }

}
