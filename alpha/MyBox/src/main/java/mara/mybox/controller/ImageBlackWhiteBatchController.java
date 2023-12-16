package mara.mybox.controller;

import java.awt.image.BufferedImage;
import javafx.fxml.FXML;
import mara.mybox.bufferedimage.ImageBinary;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2018-9-23
 * @License Apache License Version 2.0
 */
public class ImageBlackWhiteBatchController extends BaseImageEditBatchController {

    protected ImageBinary imageBinary;

    @FXML
    protected ControlImageBinary binaryController;

    public ImageBlackWhiteBatchController() {
        baseTitle = message("ImageBatch") + " - " + message("BlackOrWhite");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            binaryController.setParameters(null);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean makeMoreParameters() {
        imageBinary = binaryController.pickValues();
        return imageBinary != null && super.makeMoreParameters();
    }

    @Override
    protected BufferedImage handleImage(FxTask currentTask, BufferedImage source) {
        return imageBinary.setImage(source).setTask(currentTask).operate();
    }

}
