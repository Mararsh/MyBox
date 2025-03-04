package mara.mybox.controller;

import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.image.PixelDemos;
import mara.mybox.image.data.ImageConvolution;
import mara.mybox.image.data.ImageScope;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-9-2
 * @License Apache License Version 2.0
 */
public class ImageEdgeController extends BasePixelsController {

    protected ConvolutionKernel kernel;

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
    public boolean checkOptions() {
        if (!super.checkOptions()) {
            return false;
        }
        kernel = edgeController.pickValues();
        return kernel != null;
    }

    @Override
    protected Image handleImage(FxTask currentTask, Image inImage, ImageScope inScope) {
        try {
            ImageConvolution convolution = ImageConvolution.create();
            convolution.setImage(inImage)
                    .setScope(inScope)
                    .setKernel(kernel)
                    .setExcludeScope(excludeScope())
                    .setSkipTransparent(skipTransparent())
                    .setTask(currentTask);
            operation = kernel.getName();
            opInfo = message("Grey") + ": " + kernel.isGray();
            return convolution.startFx();
        } catch (Exception e) {
            displayError(e.toString());
            return null;
        }
    }

    @Override
    protected void makeDemoFiles(FxTask currentTask, List<String> files, Image demoImage) {
        PixelDemos.edge(currentTask, files, SwingFXUtils.fromFXImage(demoImage, null), srcFile());
    }


    /*
        static methods
     */
    public static ImageEdgeController open(BaseImageController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageEdgeController controller = (ImageEdgeController) WindowTools.operationStage(
                    parent, Fxmls.ImageEdgeFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
