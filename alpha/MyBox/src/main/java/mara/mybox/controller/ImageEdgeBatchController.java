package mara.mybox.controller;

import java.awt.image.BufferedImage;
import javafx.fxml.FXML;
import mara.mybox.bufferedimage.ImageConvolution;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2018-9-23
 * @License Apache License Version 2.0
 */
public class ImageEdgeBatchController extends BaseImageEditBatchController {

    @FXML
    protected ControlImageEdge edgeController;

    public ImageEdgeBatchController() {
        baseTitle = message("ImageBatch") + " - " + message("Edge");
    }

    @Override
    protected BufferedImage handleImage(FxTask currentTask, BufferedImage source) {
        try {
            ConvolutionKernel kernel = edgeController.kernel();
            if (kernel == null) {
                return null;
            }
            ImageConvolution convolution = ImageConvolution.create();
            BufferedImage target = convolution.setImage(source)
                    .setKernel(kernel)
                    .setTask(currentTask).operate();
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
