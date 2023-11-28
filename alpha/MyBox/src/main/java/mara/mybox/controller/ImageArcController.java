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
    protected ControlArc arcController;

    public ImageArcController() {
        baseTitle = message("Arc");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();

            operation = "Arc";
            arcController.setParameters(this);

            anchorCheck.setSelected(true);
            showAnchors = true;
            popShapeMenu = true;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setInputs() {
        arcController.loadValues();
    }

    @Override
    public boolean pickShape() {
        return arcController.pickValues();
    }

    @Override
    public void initShape() {
        try {
            maskArcData = null;
            showMaskArc();

            goAction();
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
