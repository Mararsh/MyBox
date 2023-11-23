package mara.mybox.controller;

import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import mara.mybox.data.DoublePoint;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-8-13
 * @License Apache License Version 2.0
 */
public class ImagePolygonController extends BaseShapeEditController {

    @FXML
    protected ControlPoints pointsController;

    public ImagePolygonController() {
        baseTitle = message("Polygon");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();

            operation = "Polygon";

            pointsController.tableData.addListener(new ListChangeListener<DoublePoint>() {
                @Override
                public void onChanged(ListChangeListener.Change<? extends DoublePoint> c) {
                    if (isSettingValues
                            || maskPolylineData == null
                            || pointsController.isSettingValues
                            || pointsController.isSettingTable) {
                        return;
                    }
                    maskPolygonData.setAll(pointsController.getPoints());
                    drawShape();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
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

            drawShape();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void maskShapeChanged() {
        if (isSettingValues
                || pointsController.isSettingValues
                || pointsController.isSettingTable) {
            return;
        }
        if (maskPolygonData != null) {
            pointsController.loadList(maskPolygonData.getPoints());
        } else {
            pointsController.loadList(null);
        }
    }

    /*
        static methods
     */
    public static ImagePolygonController open(ImageEditorController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImagePolygonController controller = (ImagePolygonController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.ImagePolygonFxml, false);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
