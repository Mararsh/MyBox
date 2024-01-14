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
public class ImagePolygonController extends BaseImageShapeController {

    @FXML
    protected ControlPoints pointsController;

    public ImagePolygonController() {
        baseTitle = message("Polygon");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();
            operation = message("Polygon");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setInputs() {
        if (maskPolygonData != null) {
            pointsController.loadList(maskPolygonData.getPoints());
        } else {
            pointsController.loadList(null);
        }
    }

    @Override
    public boolean pickShape() {
        try {
            maskPolygonData.setAll(pointsController.getPoints());
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

            maskPolygonData = null;
            showMaskPolygon();

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
    public static ImagePolygonController open(ImageEditorController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImagePolygonController controller = (ImagePolygonController) WindowTools.branchStage(
                    parent, Fxmls.ImagePolygonFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
