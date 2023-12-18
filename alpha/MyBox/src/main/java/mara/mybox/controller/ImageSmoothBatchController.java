package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.util.List;
import javafx.fxml.FXML;
import mara.mybox.bufferedimage.ImageConvolution;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.PixelDemos;
import mara.mybox.fxml.FxTask;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2018-9-23
 * @License Apache License Version 2.0
 */
public class ImageSmoothBatchController extends BaseImageEditBatchController {

    protected ConvolutionKernel kernel;

    @FXML
    protected ControlImageSmooth smoothController;

    public ImageSmoothBatchController() {
        baseTitle = message("ImageBatch") + " - " + message("Smooth");
    }

    @Override
    public boolean makeMoreParameters() {
        if (!super.makeMoreParameters()) {
            return false;
        }
        kernel = smoothController.pickValues();
        return kernel != null;
    }

    @Override
    protected BufferedImage handleImage(FxTask currentTask, BufferedImage source) {
        try {
            return ImageConvolution.create()
                    .setImage(source).setKernel(kernel)
                    .setTask(currentTask)
                    .operate();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @Override
    public void makeDemoFiles(FxTask currentTask, List<String> files, BufferedImage demoImage) {
        try {
            ImageConvolution convolution = ImageConvolution.create()
                    .setImage(demoImage);
            PixelDemos.smooth(currentTask, files, convolution);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
