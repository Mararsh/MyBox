package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import mara.mybox.data.DoubleEllipse;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-8-13
 * @License Apache License Version 2.0
 */
public class ControlEllipse extends BaseController {

    protected BaseShapeController shapeController;

    @FXML
    protected TextField ellipseXInput, ellipseYInput, ellipseXRadiusInput, ellipseYRadiusInput;

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
        if (isSettingValues || shapeController.maskEllipseData == null) {
            return;
        }
        try {
            ellipseXInput.setText(shapeController.scale(shapeController.maskEllipseData.getCenterX()) + "");
            ellipseYInput.setText(shapeController.scale(shapeController.maskEllipseData.getCenterY()) + "");
            ellipseXRadiusInput.setText(shapeController.scale(shapeController.maskEllipseData.getRadiusX()) + "");
            ellipseYRadiusInput.setText(shapeController.scale(shapeController.maskEllipseData.getRadiusY()) + "");
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean pickValues() {
        try {
            float cx, cy, rx, ry;
            try {
                cx = Float.parseFloat(ellipseXInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": x");
                return false;
            }
            try {
                cy = Float.parseFloat(ellipseYInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": y");
                return false;
            }
            try {
                rx = Float.parseFloat(ellipseXRadiusInput.getText());
            } catch (Exception e) {
                rx = -1f;
            }
            if (rx <= 0) {
                popError(message("InvalidParameter") + ": " + message("Radius"));
                return false;
            }
            try {
                ry = Float.parseFloat(ellipseYRadiusInput.getText());
            } catch (Exception e) {
                ry = -1f;
            }
            if (ry <= 0) {
                popError(message("InvalidParameter") + ": " + message("Radius"));
                return false;
            }
            shapeController.maskEllipseData = DoubleEllipse.ellipse(cx, cy, rx, ry);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

}
