package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.image.TransformTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-8-13
 * @License Apache License Version 2.0
 */
public class ImageMirrorController extends BaseImageEditController {

    @FXML
    protected RadioButton horizontalRadio;

    public ImageMirrorController() {
        baseTitle = message("Mirror");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();
            operation = message("Mirror");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected void handleImage(FxTask currentTask) {
        if (horizontalRadio.isSelected()) {
            opInfo = message("Horizontal");
            handledImage = TransformTools.horizontalImage(currentTask, srcImage());
        } else {
            opInfo = message("Vertical");
            handledImage = TransformTools.verticalImage(currentTask, srcImage());
        }
    }

    /*
        static methods
     */
    public static ImageMirrorController open(ImageEditorController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageMirrorController controller = (ImageMirrorController) WindowTools.operationStage(
                    parent, Fxmls.ImageMirrorFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
