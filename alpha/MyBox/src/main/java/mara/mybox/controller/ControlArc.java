package mara.mybox.controller;

import java.awt.geom.Arc2D;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import mara.mybox.data.DoubleArc;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-8-13
 * @License Apache License Version 2.0
 */
public class ControlArc extends BaseController {

    protected BaseShapeController shapeController;

    @FXML
    protected TextField arcCenterXInput, arcCenterYInput,
            arcRadiusXInput, arcRadiusYInput, arcStartAngleInput, arcExtentAngleInput;
    @FXML
    protected RadioButton arcOpenRadio, arcChordRadio, arcPieRadio;

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
        if (shapeController.maskArcData == null) {
            return;
        }
        try {
            arcCenterXInput.setText(shapeController.scale(shapeController.maskArcData.getCenterX()) + "");
            arcCenterYInput.setText(shapeController.scale(shapeController.maskArcData.getCenterY()) + "");
            arcRadiusXInput.setText(shapeController.scale(shapeController.maskArcData.getRadiusX()) + "");
            arcRadiusYInput.setText(shapeController.scale(shapeController.maskArcData.getRadiusY()) + "");
            arcStartAngleInput.setText(shapeController.scale(shapeController.maskArcData.getStartAngle()) + "");
            arcExtentAngleInput.setText(shapeController.scale(shapeController.maskArcData.getExtentAngle()) + "");

            switch (shapeController.maskArcData.getType()) {
                case Arc2D.CHORD:
                    arcChordRadio.setSelected(true);
                    break;
                case Arc2D.PIE:
                    arcPieRadio.setSelected(true);
                    break;
                default:
                    arcOpenRadio.setSelected(true);
                    break;
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean pickValues() {
        try {
            float centerX, centerY, radiusX, radiusY, startAngle, extentAngle;
            int type;
            try {
                centerX = Float.parseFloat(arcCenterXInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("Center") + " x");
                return false;
            }
            try {
                centerY = Float.parseFloat(arcCenterYInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("Center") + " y");
                return false;
            }
            try {
                radiusX = Float.parseFloat(arcRadiusXInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("RadiusX"));
                return false;
            }
            try {
                radiusY = Float.parseFloat(arcRadiusYInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("RadiusY"));
                return false;
            }
            try {
                startAngle = Float.parseFloat(arcStartAngleInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("StartAngle"));
                return false;
            }
            try {
                extentAngle = Float.parseFloat(arcExtentAngleInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("ExtentAngle"));
                return false;
            }
            if (arcChordRadio.isSelected()) {
                type = Arc2D.CHORD;
            } else if (arcPieRadio.isSelected()) {
                type = Arc2D.PIE;
            } else {
                type = Arc2D.OPEN;
            }
            shapeController.maskArcData = DoubleArc.arc(
                    centerX, centerY, radiusX, radiusY, startAngle, extentAngle, type);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

}
