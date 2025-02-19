package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javafx.fxml.FXML;
import mara.mybox.image.data.ImageMosaic;
import mara.mybox.fxml.image.PixelDemos;
import mara.mybox.fxml.FxTask;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2018-9-23
 * @License Apache License Version 2.0
 */
public class ImageGlassBatchController extends BaseImageEditBatchController {

    protected ImageMosaic mosaic;

    @FXML
    protected ControlImageMosaic mosaicController;

    public ImageGlassBatchController() {
        baseTitle = message("ImageBatch") + " - " + message("FrostedGlass");
    }

    @Override
    public boolean makeMoreParameters() {
        if (!super.makeMoreParameters()) {
            return false;
        }
        mosaic = mosaicController.pickValues(ImageMosaic.MosaicType.FrostedGlass);
        return mosaic != null;
    }

    @Override
    protected BufferedImage handleImage(FxTask currentTask, BufferedImage source) {
        return mosaic.setImage(source).setTask(currentTask).start();
    }

    @Override
    public void makeDemoFiles(FxTask currentTask, List<String> files, File demoFile, BufferedImage demoImage) {
        PixelDemos.mosaic(currentTask, files, demoImage, ImageMosaic.MosaicType.FrostedGlass, demoFile);
    }

}
