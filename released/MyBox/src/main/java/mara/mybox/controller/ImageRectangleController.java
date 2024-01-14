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
public class ImageRectangleController extends BaseImageShapeController {

    @FXML
    protected ControlRectangle rectController;

    public ImageRectangleController() {
        baseTitle = message("Rectangle");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();
            operation = message("Rectangle");

            rectController.setParameters(this);

            anchorCheck.setSelected(true);
            showAnchors = true;
            popShapeMenu = true;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setInputs() {
        rectController.loadValues();
    }

    @Override
    public boolean pickShape() {
        return rectController.pickValues();
    }

    @Override
    public void initShape() {
        try {
            showMaskRectangle();
            rectController.setRoundList();

            goAction();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        static methods
     */
    public static ImageRectangleController open(ImageEditorController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageRectangleController controller = (ImageRectangleController) WindowTools.branchStage(
                    parent, Fxmls.ImageRectangleFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
