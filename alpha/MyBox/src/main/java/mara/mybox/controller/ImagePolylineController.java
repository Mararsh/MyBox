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
public class ImagePolylineController extends BaseShapeEditController {

    @FXML
    protected ControlPoints pointsController;

    public ImagePolylineController() {
        baseTitle = message("Polyline");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();

            operation = message("Polyline");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setInputs() {
        if (maskPolylineData != null) {
            pointsController.loadList(maskPolylineData.getPoints());
        } else {
            pointsController.loadList(null);
        }
    }

    @Override
    public boolean pickShape() {
        try {
            maskPolylineData.setAll(pointsController.getPoints());
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public void initShape() {
        try {
            addPointCheck.setSelected(true);
            anchorCheck.setSelected(true);
            showAnchors = true;
            popShapeMenu = true;
            popItemMenu = popLineMenuCheck.isSelected();

            maskPolylineData = null;
            showMaskPolyline();

            goAction();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public boolean withdrawAction() {
        if (imageView == null || imageView.getImage() == null) {
            return false;
        }
        pointsController.removeLastItem();
        goShape();
        return true;
    }

    /*
        static methods
     */
    public static ImagePolylineController open(ImageEditorController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImagePolylineController controller = (ImagePolylineController) WindowTools.branch(
                    parent.getMyWindow(), Fxmls.ImagePolylineFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
