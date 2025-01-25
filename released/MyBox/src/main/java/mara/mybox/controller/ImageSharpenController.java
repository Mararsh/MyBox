package mara.mybox.controller;

import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import mara.mybox.image.data.ImageConvolution;
import mara.mybox.image.data.ImageScope;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.image.PixelDemos;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-9-2
 * @License Apache License Version 2.0
 */
public class ImageSharpenController extends BasePixelsController {

    protected ConvolutionKernel kernel;

    @FXML
    protected ControlImageSharpen sharpenController;

    public ImageSharpenController() {
        baseTitle = message("Sharpen");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();
            operation = message("Sharpen");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean checkOptions() {
        if (!super.checkOptions()) {
            return false;
        }
        kernel = sharpenController.pickValues();
        return kernel != null;
    }

    @Override
    protected Image handleImage(FxTask currentTask, Image inImage, ImageScope inScope) {
        try {
            ImageConvolution convolution = ImageConvolution.create();
            convolution.setImage(inImage).setScope(inScope).setKernel(kernel)
                    .setExcludeScope(excludeScope())
                    .setSkipTransparent(skipTransparent())
                    .setTask(currentTask);
            opInfo = message("Intensity") + ": " + sharpenController.intensity;
            return convolution.startFx();
        } catch (Exception e) {
            displayError(e.toString());
            return null;
        }
    }

    @Override
    protected void makeDemoFiles(FxTask currentTask, List<String> files, Image demoImage) {
        try {
            ImageConvolution convolution = ImageConvolution.create();
            convolution.setImage(demoImage)
                    .setScope(scope())
                    .setExcludeScope(excludeScope())
                    .setSkipTransparent(skipTransparent());
            PixelDemos.sharpen(currentTask, files, convolution, srcFile());
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        static methods
     */
    public static ImageSharpenController open(BaseImageController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageSharpenController controller = (ImageSharpenController) WindowTools.branchStage(
                    parent, Fxmls.ImageSharpenFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
