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
public class ImagePolylinesController extends BaseShapeEditController {

    @FXML
    protected ControlLines linesController;

    public ImagePolylinesController() {
        baseTitle = message("Graffiti");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();
            operation = message("Graffiti");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setInputs() {
        if (maskPolylinesData != null) {
            linesController.loadList(maskPolylinesData.getLines());
        } else {
            linesController.loadList(null);
        }
    }

    @Override
    public boolean pickShape() {
        try {
            maskPolylinesData.setLines(linesController.getLines());
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public void initShape() {
        try {
            popItemMenu = popLineMenuCheck.isSelected();
            showAnchors = false;
            addPointWhenClick = false;
            popShapeMenu = true;

            maskPolylinesData = null;
            showMaskPolylines();

            goAction();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    @Override
    public boolean withdrawAction() {
        if (imageView == null || imageView.getImage() == null) {
            return false;
        }
        linesController.removeLastItem();
        goShape();
        return true;
    }

    /*
        static methods
     */
    public static ImagePolylinesController open(ImageEditorController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImagePolylinesController controller = (ImagePolylinesController) WindowTools.branchStage(
                    parent, Fxmls.ImagePolylinesFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
