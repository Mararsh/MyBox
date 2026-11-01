package mara.mybox.controller;

import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.image.ImageDemos;
import mara.mybox.fxml.image.TransformTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2026-11-1
 * @License Apache License Version 2.0
 */
public class ImageLiquifyController extends BaseImageEditController {

    @FXML
    protected ControlImageLiquify liquifyController;

    public ImageLiquifyController() {
        baseTitle = message("Liquify");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();
            operation = message("Liquify");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean checkOptions() {
        if (!super.checkOptions()) {
            return false;
        }
        return liquifyController.pickValues();
    }

    @Override
    protected void handleImage(FxTask currentTask) {
        opInfo = message("XRatio") + ":" + liquifyController.shearX + " "
                + message("YRatio") + ":" + liquifyController.shearY;
        handledImage = TransformTools.shearImage(currentTask, imageView.getImage(),
                liquifyController.shearX, liquifyController.shearY,
                liquifyController.cutCheck.isSelected());
    }

    @Override
    protected void makeDemoFiles(FxTask currentTask, List<String> files, Image demoImage) {
        ImageDemos.shear(currentTask, files, SwingFXUtils.fromFXImage(demoImage, null), srcFile());
    }


    /*
        static methods
     */
    public static ImageLiquifyController open(ImageEditorController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageLiquifyController controller = (ImageLiquifyController) WindowTools.referredStage(
                    parent, Fxmls.ImageLiquifyFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
