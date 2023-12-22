package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javafx.fxml.FXML;
import mara.mybox.bufferedimage.ImageMosaic;
import mara.mybox.fximage.PixelDemos;
import mara.mybox.fxml.FxTask;
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
        mosaic = mosaicController.pickValues(ImageMosaic.MosaicType.Mosaic);
        return mosaic != null;
    }

    @Override
    protected BufferedImage handleImage(FxTask currentTask, BufferedImage source) {
        return mosaic.setImage(source).setTask(currentTask).operateImage();
    }

    @Override
    public void makeDemoFiles(FxTask currentTask, List<String> files, File demoFile, BufferedImage demoImage) {
        PixelDemos.mosaic(currentTask, files, demoImage, ImageMosaic.MosaicType.Mosaic, demoFile);
    }

}
