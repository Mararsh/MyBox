package mara.mybox.controller;

import java.util.List;
import javafx.embed.swing.SwingFXUtils;
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
    public boolean checkOptions() {
        if (!super.checkOptions()) {
            return false;
        }
        kernel = embossController.pickValues();
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
            opInfo = kernel.getName() + " " + message("Grey") + ": " + kernel.isGray();
            Image emboss = convolution.startFx();
            kernel = null;
            return emboss;
        } catch (Exception e) {
            displayError(e.toString());
            return null;
        }
    }

    @Override
    protected void makeDemoFiles(FxTask currentTask, List<String> files, Image demoImage) {
        PixelDemos.emboss(currentTask, files, SwingFXUtils.fromFXImage(demoImage, null), srcFile());
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
