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
public class ImageCircleController extends BaseShapeEditController {

    @FXML
    protected ControlCircle circleController;

    public ImageCircleController() {
        baseTitle = message("Circle");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();

            operation = "Circle";
            circleController.setParameters(this);

            anchorCheck.setSelected(true);
            showAnchors = true;
            popShapeMenu = true;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setInputs() {
        circleController.loadValues();
    }

    @Override
    public boolean pickShape() {
        return circleController.pickValues();
    }

    @Override
    public void initShape() {
        try {
            showMaskCircle();

            goShape();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        static methods
     */
    public static ImageCircleController open(ImageEditorController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageCircleController controller = (ImageCircleController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.ImageCircleFxml, false);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
