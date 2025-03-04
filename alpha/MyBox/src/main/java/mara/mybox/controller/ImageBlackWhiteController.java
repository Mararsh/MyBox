package mara.mybox.controller;

import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.image.ColorDemos;
import mara.mybox.image.data.ImageBinary;
import mara.mybox.image.data.ImageScope;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-9-2
 * @License Apache License Version 2.0
 */
public class ImageBlackWhiteController extends BasePixelsController {

    protected ImageBinary imageBinary;

    @FXML
    protected ControlImageBinary binaryController;

    public ImageBlackWhiteController() {
        baseTitle = message("BlackOrWhite");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();

            operation = message("BlackOrWhite");

            binaryController.setParameters(imageController.imageView);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean checkOptions() {
        if (!super.checkOptions()) {
            return false;
        }
        imageBinary = binaryController.pickValues(-1);
        if (imageBinary == null) {
            return false;
        }
        if (imageBinary.getAlgorithm() != ImageBinary.BinaryAlgorithm.Default) {
            opInfo = message("Threshold") + ": " + imageBinary.getIntPara1();
        }
        return true;
    }

    @Override
    protected Image handleImage(FxTask currentTask, Image inImage, ImageScope inScope) {
        try {
            imageBinary.setImage(inImage)
                    .setScope(inScope)
                    .setExcludeScope(excludeScope())
                    .setSkipTransparent(skipTransparent())
                    .setTask(currentTask);
            return imageBinary.startFx();
        } catch (Exception e) {
            displayError(e.toString());
            return null;
        }
    }

    @Override
    protected void makeDemoFiles(FxTask currentTask, List<String> files, Image demoImage) {
        try {
            imageBinary = binaryController.pickValues(128);
            if (imageBinary == null) {
                return;
            }
            imageBinary.setImage(demoImage);
            ColorDemos.blackWhite(currentTask, files, imageBinary, srcFile());
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }


    /*
        static methods
     */
    public static ImageBlackWhiteController open(BaseImageController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageBlackWhiteController controller = (ImageBlackWhiteController) WindowTools.operationStage(
                    parent, Fxmls.ImageBlackWhiteFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
