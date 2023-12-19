package mara.mybox.controller;

import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.ImageDemos;
import mara.mybox.fximage.TransformTools;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-8-13
 * @License Apache License Version 2.0
 */
public class ImageShearController extends BaseImageEditController {

    @FXML
    protected ControlImageShear shearController;

    public ImageShearController() {
        baseTitle = message("Shear");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();
            operation = message("Shear");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected boolean checkOptions() {
        if (!super.checkOptions()) {
            return false;
        }
        return shearController.pickValues();
    }

    @Override
    protected void handleImage(FxTask currentTask) {
        opInfo = message("XRatio") + ":" + shearController.shearX + " "
                + message("YRatio") + ":" + shearController.shearY;
        handledImage = TransformTools.shearImage(currentTask, imageView.getImage(),
                shearController.shearX, shearController.shearY);
    }

    @Override
    protected void makeDemoFiles(FxTask currentTask, List<String> files, Image demoImage) {
        ImageDemos.shear(currentTask, files, SwingFXUtils.fromFXImage(demoImage, null), prefix());
    }


    /*
        static methods
     */
    public static ImageShearController open(ImageEditorController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageShearController controller = (ImageShearController) WindowTools.branchStage(
                    parent, Fxmls.ImageShearFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
