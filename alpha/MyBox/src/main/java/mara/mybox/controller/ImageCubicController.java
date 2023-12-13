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
public class ImageCubicController extends BaseShapeEditController {

    @FXML
    protected ControlCubic cubicController;

    public ImageCubicController() {
        baseTitle = message("CubicCurve");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();
            operation = message("CubicCurve");

            cubicController.setParameters(this);

            anchorCheck.setSelected(true);
            showAnchors = true;
            popShapeMenu = true;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setInputs() {
        cubicController.loadValues();
    }

    @Override
    public boolean pickShape() {
        return cubicController.pickValues();
    }

    @Override
    public void initShape() {
        try {
            maskCubicData = null;
            showMaskCubic();

            goAction();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        static methods
     */
    public static ImageCubicController open(ImageEditorController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageCubicController controller = (ImageCubicController) WindowTools.branchStage(
                    parent, Fxmls.ImageCubicFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
