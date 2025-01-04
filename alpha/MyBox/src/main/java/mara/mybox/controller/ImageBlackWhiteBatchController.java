package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javafx.fxml.FXML;
import mara.mybox.image.data.ImageBinary;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.image.ColorDemos;
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
        imageBinary = binaryController.pickValues(-1);
        return imageBinary != null && super.makeMoreParameters();
    }

    @Override
    protected BufferedImage handleImage(FxTask currentTask, BufferedImage source) {
        return imageBinary.setImage(source).setTask(currentTask).start();
    }

    @Override
    public void makeDemoFiles(FxTask currentTask, List<String> files, File demoFile, BufferedImage demoImage) {
        try {
            imageBinary = binaryController.pickValues(128);
            if (imageBinary == null) {
                return;
            }
            imageBinary.setImage(demoImage);
            ColorDemos.blackWhite(currentTask, files, imageBinary, demoFile);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
