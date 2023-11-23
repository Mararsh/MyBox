package mara.mybox.controller;

import java.util.List;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import mara.mybox.data.DoublePoint;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.ShapeTools;
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
        baseTitle = message("Polylines");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();

            operation = "Polylines";
            showAnchors = false;
            addPointWhenClick = false;
            popShapeMenu = false;

            linesController.tableData.addListener(new ListChangeListener<List<DoublePoint>>() {
                @Override
                public void onChanged(ListChangeListener.Change<? extends List<DoublePoint>> c) {
                    if (isSettingValues
                            || maskPolylinesData == null
                            || linesController.isSettingValues
                            || linesController.isSettingTable) {
                        return;
                    }
                    maskPolylinesData.setLines(linesController.getLines());
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
            popItemMenu = popLineMenuCheck.isSelected();

            maskPolylinesData = null;
            showMaskPolylines();

            drawShape();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void maskShapeChanged() {
        if (isSettingValues
                || linesController.isSettingValues
                || linesController.isSettingTable) {
            return;
        }
        if (maskPolylinesData != null) {
            linesController.loadList(maskPolylinesData.getLines());
        } else {
            linesController.loadList(null);
        }
    }

    @FXML
    @Override
    public boolean withdrawAction() {
        if (imageView == null || imageView.getImage() == null) {
            return false;
        }
        linesController.removeLastItem();
        return true;
    }

    @FXML
    @Override
    public void clearAction() {
        loadImage(srcImage());
    }

    @Override
    protected void handleImage() {
        handledImage = ShapeTools.drawShape(srcImage(),
                maskPolylinesData, shapeStyle, blender);
    }

    /*
        static methods
     */
    public static ImagePolylinesController open(ImageEditorController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImagePolylinesController controller = (ImagePolylinesController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.ImagePolylinesFxml, false);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
