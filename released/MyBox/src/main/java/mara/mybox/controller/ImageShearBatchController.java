package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javafx.fxml.FXML;
import mara.mybox.bufferedimage.TransformTools;
import mara.mybox.fximage.ImageDemos;
import mara.mybox.fxml.FxTask;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2018-9-23
 * @License Apache License Version 2.0
 */
public class ImageShearBatchController extends BaseImageEditBatchController {

    @FXML
    protected ControlImageShear shearController;

    public ImageShearBatchController() {
        baseTitle = message("ImageBatch") + " - " + message("Shear");
    }

    @Override
    public boolean makeMoreParameters() {
        if (!super.makeMoreParameters()) {
            return false;
        }
        return shearController.pickValues();
    }

    @Override
    protected BufferedImage handleImage(FxTask currentTask, BufferedImage source) {
        return TransformTools.shearImage(currentTask, source,
                shearController.shearX, shearController.shearY,
                shearController.cutCheck.isSelected());
    }

    @Override
    public void makeDemoFiles(FxTask currentTask, List<String> files, File demoFile, BufferedImage demoImage) {
        ImageDemos.shear(currentTask, files, demoImage, demoFile);
    }

}
