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
public class ImageLineController extends BaseShapeEditController {

    @FXML
    protected ControlLine lineController;

    public ImageLineController() {
        baseTitle = message("StraightLine");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();

            operation = message("StraightLine");
            lineController.setParameters(this);

            anchorCheck.setSelected(true);
            showAnchors = true;
            popShapeMenu = true;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setInputs() {
        lineController.loadValues();
    }

    @Override
    public boolean pickShape() {
        return lineController.pickValues();
    }

    @Override
    public void initShape() {
        try {
            showMaskLine();

            goAction();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        static methods
     */
    public static ImageLineController open(ImageEditorController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageLineController controller = (ImageLineController) WindowTools.branch(
                    parent.getMyWindow(), Fxmls.ImageLineFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
