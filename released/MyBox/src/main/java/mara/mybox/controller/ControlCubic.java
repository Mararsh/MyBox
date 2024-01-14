package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import mara.mybox.data.DoubleCubic;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-8-13
 * @License Apache License Version 2.0
 */
public class ControlCubic extends BaseController {

    protected BaseShapeController shapeController;

    @FXML
    protected TextField cubicStartXInput, cubicStartYInput, cubicControlX1Input, cubicControlY1Input,
            cubicControlX2Input, cubicControlY2Input, cubicEndXInput, cubicEndYInput;

    protected void setParameters(BaseShapeController parent) {
        try {
            if (parent == null) {
                return;
            }
            shapeController = parent;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadValues() {
        if (shapeController.maskCubicData == null) {
            return;
        }
        try {
            cubicStartXInput.setText(shapeController.scale(shapeController.maskCubicData.getStartX()) + "");
            cubicStartYInput.setText(shapeController.scale(shapeController.maskCubicData.getStartY()) + "");
            cubicControlX1Input.setText(shapeController.scale(shapeController.maskCubicData.getControlX1()) + "");
            cubicControlY1Input.setText(shapeController.scale(shapeController.maskCubicData.getControlY1()) + "");
            cubicControlX2Input.setText(shapeController.scale(shapeController.maskCubicData.getControlX2()) + "");
            cubicControlY2Input.setText(shapeController.scale(shapeController.maskCubicData.getControlY2()) + "");
            cubicEndXInput.setText(shapeController.scale(shapeController.maskCubicData.getEndX()) + "");
            cubicEndYInput.setText(shapeController.scale(shapeController.maskCubicData.getEndY()) + "");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean pickValues() {
        try {
            float sx, sy, cx1, cy1, cx2, cy2, ex, ey;
            try {
                sx = Float.parseFloat(cubicStartXInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("StartPoint") + " x");
                return false;
            }
            try {
                sy = Float.parseFloat(cubicStartYInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("StartPoint") + " y");
                return false;
            }
            try {
                cx1 = Float.parseFloat(cubicControlX1Input.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("ControlPoint1") + " x");
                return false;
            }
            try {
                cy1 = Float.parseFloat(cubicControlY1Input.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("ControlPoint1") + " y");
                return false;
            }
            try {
                cx2 = Float.parseFloat(cubicControlX2Input.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("ControlPoint2") + " x");
                return false;
            }
            try {
                cy2 = Float.parseFloat(cubicControlY2Input.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("ControlPoint2") + " y");
                return false;
            }
            try {
                ex = Float.parseFloat(cubicEndXInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("EndPoint") + " x");
                return false;
            }
            try {
                ey = Float.parseFloat(cubicEndYInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("End") + " y");
                return false;
            }
            shapeController.maskCubicData = new DoubleCubic(sx, sy, cx1, cy1, cx2, cy2, ex, ey);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

}
