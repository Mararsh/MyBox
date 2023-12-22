package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javafx.fxml.FXML;
import mara.mybox.bufferedimage.PixelsOperation;
import mara.mybox.fximage.ColorDemos;
import mara.mybox.fxml.FxTask;
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
    protected BufferedImage handleImage(FxTask currentTask, BufferedImage source) {
        return pixelsOperation.setImage(source).setTask(currentTask).operate();
    }

    @Override
    public void makeDemoFiles(FxTask currentTask, List<String> files,
            File demoFile, BufferedImage demoImage) {
        ColorDemos.thresholding(currentTask, files, demoImage, demoFile);
    }

}
