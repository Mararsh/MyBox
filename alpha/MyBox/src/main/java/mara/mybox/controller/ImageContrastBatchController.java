package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javafx.fxml.FXML;
import mara.mybox.bufferedimage.ImageContrast;
import mara.mybox.fximage.PixelDemos;
import mara.mybox.fxml.FxTask;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2018-9-23
 * @License Apache License Version 2.0
 */
public class ImageContrastBatchController extends BaseImageEditBatchController {

    protected ImageContrast contrast;

    @FXML
    protected ControlImageContrast contrastController;

    public ImageContrastBatchController() {
        baseTitle = message("ImageBatch") + " - " + message("Contrast");
    }

    @Override
    public boolean makeMoreParameters() {
        if (!super.makeMoreParameters()) {
            return false;
        }
        contrast = contrastController.pickValues();
        return contrast != null;
    }

    @Override
    protected BufferedImage handleImage(FxTask currentTask, BufferedImage source) {
        return contrast.setImage(source).setTask(currentTask).operate();
    }

    @Override
    public void makeDemoFiles(FxTask currentTask, List<String> files, File demoFile, BufferedImage demoImage) {
        PixelDemos.contrast(currentTask, files, demoImage, demoFile);
    }

}
