package mara.mybox.controller;

import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.image.ColorDemos;
import mara.mybox.image.data.ImageScope;
import mara.mybox.image.data.PixelsOperation;
import mara.mybox.image.data.PixelsOperationFactory;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-9-2
 * @License Apache License Version 2.0
 */
public class ImageSepiaController extends BasePixelsController {

    protected PixelsOperation pixelsOperation;

    @FXML
    protected ControlImageSepia sepiaController;

    public ImageSepiaController() {
        baseTitle = message("Sepia");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();
            operation = message("Sepia");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean checkOptions() {
        if (!super.checkOptions()) {
            return false;
        }
        pixelsOperation = sepiaController.pickValues();
        return pixelsOperation != null;
    }

    @Override
    protected Image handleImage(FxTask currentTask, Image inImage, ImageScope inScope) {
        try {
            pixelsOperation.setImage(inImage)
                    .setScope(inScope)
                    .setExcludeScope(excludeScope())
                    .setSkipTransparent(skipTransparent())
                    .setTask(currentTask);
            opInfo = message("Intensity") + ": " + sepiaController.intensity;
            return pixelsOperation.startFx();
        } catch (Exception e) {
            displayError(e.toString());
            return null;
        }
    }

    @Override
    protected void makeDemoFiles(FxTask currentTask, List<String> files, Image demoImage) {
        try {
            PixelsOperation op = PixelsOperationFactory.createFX(
                    demoImage, scope(), PixelsOperation.OperationType.Sepia)
                    .setExcludeScope(excludeScope())
                    .setSkipTransparent(skipTransparent())
                    .setTask(currentTask);
            ColorDemos.sepia(currentTask, files, op, srcFile());
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        static methods
     */
    public static ImageSepiaController open(BaseImageController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageSepiaController controller = (ImageSepiaController) WindowTools.operationStage(
                    parent, Fxmls.ImageSepiaFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
