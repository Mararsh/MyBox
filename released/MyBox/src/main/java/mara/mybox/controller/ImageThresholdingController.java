package mara.mybox.controller;

import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.bufferedimage.PixelsOperation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.ColorDemos;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-9-2
 * @License Apache License Version 2.0
 */
public class ImageThresholdingController extends BasePixelsController {

    protected PixelsOperation pixelsOperation;

    @FXML
    protected ControlImageThresholding thresholdingController;

    public ImageThresholdingController() {
        baseTitle = message("Thresholding");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();
            operation = message("Thresholding");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean checkOptions() {
        if (!super.checkOptions()) {
            return false;
        }
        pixelsOperation = thresholdingController.pickValues();
        return pixelsOperation != null;
    }

    @Override
    protected Image handleImage(FxTask currentTask, Image inImage, ImageScope inScope) {
        try {
            pixelsOperation.setImage(inImage).setScope(inScope)
                    .setExcludeScope(excludeScope())
                    .setSkipTransparent(skipTransparent())
                    .setTask(currentTask);
            opInfo = message("Threshold") + ": " + thresholdingController.threshold;
            Image result = pixelsOperation.startFx();
            pixelsOperation = null;
            return result;
        } catch (Exception e) {
            displayError(e.toString());
            return null;
        }
    }

    @Override
    protected void makeDemoFiles(FxTask currentTask, List<String> files, Image demoImage) {
        ColorDemos.thresholding(currentTask, files, SwingFXUtils.fromFXImage(demoImage, null), srcFile());
    }

    /*
        static methods
     */
    public static ImageThresholdingController open(BaseImageController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageThresholdingController controller = (ImageThresholdingController) WindowTools.branchStage(
                    parent, Fxmls.ImageThresholdingFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
