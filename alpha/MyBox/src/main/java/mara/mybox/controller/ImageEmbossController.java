package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import mara.mybox.bufferedimage.ImageConvolution;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-9-2
 * @License Apache License Version 2.0
 */
public class ImageEmbossController extends BasePixelsController {

    protected ConvolutionKernel kernel;

    @FXML
    protected ControlImageEmboss embossController;

    public ImageEmbossController() {
        baseTitle = message("Emboss");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();
            operation = message("Emboss");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected boolean checkOptions() {
        if (!super.checkOptions()) {
            return false;
        }
        kernel = embossController.pickValues();
        return kernel != null;
    }

    @Override
    protected Image handleImage(Image inImage, ImageScope inScope) {
        try {
            ImageConvolution convolution = ImageConvolution.create();
            convolution.setImage(inImage)
                    .setScope(inScope)
                    .setKernel(kernel)
                    .setExcludeScope(excludeScope())
                    .setSkipTransparent(skipTransparent())
                    .setTask(task);
            opInfo = kernel.getName() + " " + message("Grey") + ": " + kernel.isGray();
            Image emboss = convolution.operateFxImage();
            kernel = null;
            return emboss;
        } catch (Exception e) {
            displayError(e.toString());
            return null;
        }
    }

    /*
        static methods
     */
    public static ImageEmbossController open(BaseImageController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageEmbossController controller = (ImageEmbossController) WindowTools.branchStage(
                    parent, Fxmls.ImageEmbossFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
