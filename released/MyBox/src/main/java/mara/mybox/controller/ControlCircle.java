package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import mara.mybox.data.DoubleCircle;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-8-13
 * @License Apache License Version 2.0
 */
public class ControlCircle extends BaseController {

    protected BaseShapeController shapeController;

    @FXML
    protected TextField circleXInput, circleYInput, circleRadiusInput;

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
        if (isSettingValues || shapeController.maskCircleData == null) {
            return;
        }
        try {
            circleXInput.setText(shapeController.scale(shapeController.maskCircleData.getCenterX()) + "");
            circleYInput.setText(shapeController.scale(shapeController.maskCircleData.getCenterY()) + "");
            circleRadiusInput.setText(shapeController.scale(shapeController.maskCircleData.getRadius()) + "");
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean pickValues() {
        try {
            float x, y, r;
            try {
                x = Float.parseFloat(circleXInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": x");
                return false;
            }
            try {
                y = Float.parseFloat(circleYInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": y");
                return false;
            }
            try {
                r = Float.parseFloat(circleRadiusInput.getText());
            } catch (Exception e) {
                r = -1f;
            }
            if (r <= 0) {
                popError(message("InvalidParameter") + ": " + message("Radius"));
                return false;
            }
            shapeController.maskCircleData = new DoubleCircle(x, y, r);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

}
