package mara.mybox.controller;

import java.awt.geom.Rectangle2D;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoubleShape;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2023-8-16
 * @License Apache License Version 2.0
 */
public class ShapeTranslateInputController extends BaseShapeTransformController {

    @FXML
    protected RadioButton centerRadio, leftTopRadio, rightBottomRadio, leftBottomRadio, rightTopRadio;

    @Override
    public void setParameters(BaseShapeController parent, DoubleShape shapeData, DoublePoint point) {
        try {
            super.setParameters(parent, shapeData, point);

            centerRadio.setSelected(true);

            imageCenter();

        } catch (Exception e) {
            MyBoxLog.debug(e);

        }
    }

    @FXML
    @Override
    public void okAction() {
        if (!checkInput()) {
            return;
        }
        Rectangle2D bounds = DoubleShape.getBound(shapeData);
        if (bounds == null) {
            return;
        }
        double offsetX, offsetY;
        if (centerRadio.isSelected()) {
            DoublePoint center = DoubleShape.getCenter(shapeData);
            offsetX = x - center.getX();
            offsetY = y - center.getY();
        } else if (leftTopRadio.isSelected()) {
            offsetX = x - bounds.getMinX();
            offsetY = y - bounds.getMinY();
        } else if (rightBottomRadio.isSelected()) {
            offsetX = x - bounds.getMaxX();
            offsetY = y - bounds.getMaxY();
        } else if (leftBottomRadio.isSelected()) {
            offsetX = x - bounds.getMinX();
            offsetY = y - bounds.getMaxY();
        } else if (rightTopRadio.isSelected()) {
            offsetX = x - bounds.getMaxX();
            offsetY = y - bounds.getMinY();
        } else {
            return;
        }
        shapeData.translateRel(offsetX, offsetY);
        imageController.maskShapeDataChanged();
        close();
    }

    public static ShapeTranslateInputController open(BaseShapeController parent, DoubleShape shapeData, DoublePoint point) {
        try {
            ShapeTranslateInputController controller = (ShapeTranslateInputController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.ShapeTranslateInputFxml, true);
            controller.setParameters(parent, shapeData, point);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
