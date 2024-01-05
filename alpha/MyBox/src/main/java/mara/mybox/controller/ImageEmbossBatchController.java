package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
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
    protected BufferedImage handleImage(FxTask currentTask, BufferedImage source) {
        try {
            ImageConvolution imageConvolution = ImageConvolution.create().
                    setImage(source).setKernel(kernel);
            return imageConvolution.setTask(currentTask).start();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @Override
    public void makeDemoFiles(FxTask currentTask, List<String> files,
            File demoFile, BufferedImage demoImage) {
        PixelDemos.emboss(currentTask, files, demoImage, demoFile);
    }

}
