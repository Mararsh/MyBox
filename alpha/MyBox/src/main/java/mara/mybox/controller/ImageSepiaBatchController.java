package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.util.List;
import javafx.fxml.FXML;
import mara.mybox.bufferedimage.PixelsOperation;
import mara.mybox.bufferedimage.PixelsOperationFactory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.ImageDemoTools;
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
    protected BufferedImage handleImage(FxTask currentTask, BufferedImage source) {
        return pixelsOperation.setImage(source).setTask(currentTask).operateImage();
    }

    @Override
    public void makeDemoFiles(FxTask currentTask, List<String> files, BufferedImage demoImage) {
        try {
            PixelsOperation op = PixelsOperationFactory.create(
                    demoImage, null, PixelsOperation.OperationType.Sepia)
                    .setTask(currentTask);
            ImageDemoTools.sepia(currentTask, files, op);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
