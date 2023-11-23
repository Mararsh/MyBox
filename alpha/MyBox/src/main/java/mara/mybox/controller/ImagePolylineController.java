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

            operation = "Polyline";

            pointsController.tableData.addListener(new ListChangeListener<DoublePoint>() {
                @Override
                public void onChanged(ListChangeListener.Change<? extends DoublePoint> c) {
                    if (isSettingValues
                            || maskPolylineData == null
                            || pointsController.isSettingValues
                            || pointsController.isSettingTable) {
                        return;
                    }
                    maskPolylineData.setAll(pointsController.getPoints());
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

            maskPolylineData = null;
            showMaskPolyline();

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
        if (maskPolylineData != null) {
            pointsController.loadList(maskPolylineData.getPoints());
        } else {
            pointsController.loadList(null);
        }
    }

    /*
        static methods
     */
    public static ImagePolylineController open(ImageEditorController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImagePolylineController controller = (ImagePolylineController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.ImagePolylineFxml, false);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
