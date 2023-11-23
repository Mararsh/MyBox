package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-8-13
 * @License Apache License Version 2.0
 */
public class ImageArcController extends BaseShapeEditController {

    @FXML
    protected ControlRectangle rectController;

    public ImageArcController() {
        baseTitle = message("Rectangle");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();

            operation = "Rectangle";
            rectController.setParameters(this);

            anchorCheck.setSelected(true);
            showAnchors = true;
            popShapeMenu = true;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean pickShape() {
        return rectController.pickValues();
    }

    @Override
    public void initShape() {
        try {
            rectController.initShape();

            goShape();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        static methods
     */
    public static ImageArcController open(ImageEditorController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageArcController controller = (ImageArcController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.ImageArcFxml, false);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
