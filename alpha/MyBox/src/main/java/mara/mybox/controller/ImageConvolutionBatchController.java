package mara.mybox.controller;

import java.awt.image.BufferedImage;
import javafx.fxml.FXML;
import mara.mybox.bufferedimage.ImageConvolution;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.fxml.FxTask;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2018-9-23
 * @License Apache License Version 2.0
 */
public class ImageConvolutionBatchController extends BaseImageEditBatchController {

    protected ConvolutionKernel kernel;

    @FXML
    protected ControlImageConvolution convolutionController;

    public ImageConvolutionBatchController() {
        baseTitle = message("ImageBatch") + " - " + message("Convolution");
    }

    @Override
    public boolean makeMoreParameters() {
        if (!super.makeMoreParameters()) {
            return false;
        }
        kernel = convolutionController.pickValues();
        return kernel != null;
    }

    @Override
    protected BufferedImage handleImage(FxTask currentTask, BufferedImage source) {
        try {
            ImageConvolution convolution = ImageConvolution.create();
            convolution.setImage(source).setKernel(kernel).setTask(currentTask);
            return convolution.operateImage();
        } catch (Exception e) {
            displayError(e.toString());
            return null;
        }
    }

}
