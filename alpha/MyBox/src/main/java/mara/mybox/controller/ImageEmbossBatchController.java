package mara.mybox.controller;

import java.awt.image.BufferedImage;
import javafx.fxml.FXML;
import mara.mybox.bufferedimage.ImageConvolution;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2018-9-23
 * @License Apache License Version 2.0
 */
public class ImageEmbossBatchController extends BaseImageEditBatchController {

    protected ConvolutionKernel kernel;

    @FXML
    protected ControlImageEmboss embossController;

    public ImageEmbossBatchController() {
        baseTitle = message("ImageBatch") + " - " + message("Emboss");
    }

    @Override
    public boolean makeMoreParameters() {
        kernel = embossController.pickValues();
        return kernel != null && super.makeMoreParameters();
    }

    @Override
    protected BufferedImage handleImage(BufferedImage source) {
        try {
            ImageConvolution imageConvolution = ImageConvolution.create().
                    setImage(source).setKernel(kernel);
            return imageConvolution.setTask(task).operate();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}