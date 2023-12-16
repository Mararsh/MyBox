package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import mara.mybox.bufferedimage.ImageConvolution;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-9-2
 * @License Apache License Version 2.0
 */
public class ImageEdgeController extends BasePixelsController {

    @FXML
    protected ControlImageEdge edgeController;

    public ImageEdgeController() {
        baseTitle = message("EdgeDetection");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();
            operation = message("EdgeDetection");
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected Image handleImage(FxTask currentTask, Image inImage, ImageScope inScope) {
        try {
            ConvolutionKernel kernel = edgeController.kernel();
            if (kernel == null) {
                return null;
            }
            ImageConvolution convolution = ImageConvolution.create();
            convolution.setImage(inImage)
                    .setScope(inScope)
                    .setKernel(kernel)
                    .setExcludeScope(excludeScope())
                    .setSkipTransparent(skipTransparent())
                    .setTask(currentTask);
            operation = kernel.getName();
            opInfo = message("Grey") + ": " + kernel.isGray();
            return convolution.operateFxImage();
        } catch (Exception e) {
            displayError(e.toString());
            return null;
        }
    }


    /*
        static methods
     */
    public static ImageEdgeController open(BaseImageController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageEdgeController controller = (ImageEdgeController) WindowTools.branchStage(
                    parent, Fxmls.ImageEdgeFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
