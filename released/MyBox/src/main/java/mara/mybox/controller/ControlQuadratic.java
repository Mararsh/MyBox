package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import mara.mybox.data.DoubleQuadratic;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-8-13
 * @License Apache License Version 2.0
 */
public class ControlQuadratic extends BaseController {

    protected BaseShapeController shapeController;

    @FXML
    protected TextField quadStartXInput, quadStartYInput,
            quadControlXInput, quadControlYInput, quadEndXInput, quadEndYInput;

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
        if (shapeController.maskQuadraticData == null) {
            return;
        }
        try {
            quadStartXInput.setText(shapeController.scale(shapeController.maskQuadraticData.getStartX()) + "");
            quadStartYInput.setText(shapeController.scale(shapeController.maskQuadraticData.getStartY()) + "");
            quadControlXInput.setText(shapeController.scale(shapeController.maskQuadraticData.getControlX()) + "");
            quadControlYInput.setText(shapeController.scale(shapeController.maskQuadraticData.getControlY()) + "");
            quadEndXInput.setText(shapeController.scale(shapeController.maskQuadraticData.getEndX()) + "");
            quadEndYInput.setText(shapeController.scale(shapeController.maskQuadraticData.getEndY()) + "");
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean pickValues() {
        try {
            float sx, sy, cx, cy, ex, ey;
            try {
                sx = Float.parseFloat(quadStartXInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("StartPoint") + " x");
                return false;
            }
            try {
                sy = Float.parseFloat(quadStartYInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("StartPoint") + " y");
                return false;
            }
            try {
                cx = Float.parseFloat(quadControlXInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("ControlPoint") + " x");
                return false;
            }
            try {
                cy = Float.parseFloat(quadControlYInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("ControlPoint") + " y");
                return false;
            }
            try {
                ex = Float.parseFloat(quadEndXInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("EndPoint") + " x");
                return false;
            }
            try {
                ey = Float.parseFloat(quadEndYInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("EndPoint") + " y");
                return false;
            }
            shapeController.maskQuadraticData = new DoubleQuadratic(sx, sy, cx, cy, ex, ey);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

}
