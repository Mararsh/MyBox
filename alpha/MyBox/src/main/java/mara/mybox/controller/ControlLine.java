package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import mara.mybox.data.DoubleLine;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-8-13
 * @License Apache License Version 2.0
 */
public class ControlLine extends BaseController {

    protected BaseShapeController shapeController;

    @FXML
    protected TextField lineX1Input, lineY1Input, lineX2Input, lineY2Input;

    protected void setParameters(BaseShapeController parent) {
        try {
            if (parent == null) {
                return;
            }
            shapeController = parent;
            shapeController.maskShapeChanged.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue v, Boolean ov, Boolean nv) {
                    loadValues();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initShape() {
        try {
            shapeController.showMaskLine();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadValues() {
        if (isSettingValues || shapeController.maskLineData == null) {
            return;
        }
        try {
            lineX1Input.setText(shapeController.scale(shapeController.maskLineData.getStartX()) + "");
            lineY1Input.setText(shapeController.scale(shapeController.maskLineData.getStartY()) + "");
            lineX2Input.setText(shapeController.scale(shapeController.maskLineData.getEndX()) + "");
            lineY2Input.setText(shapeController.scale(shapeController.maskLineData.getEndY()) + "");
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean pickValues() {
        try {
            float x1, y1, x2, y2;
            try {
                x1 = Float.parseFloat(lineX1Input.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": x1");
                return false;
            }
            try {
                y1 = Float.parseFloat(lineY1Input.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": y1");
                return false;
            }
            try {
                x2 = Float.parseFloat(lineX2Input.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": x2");
                return false;
            }
            try {
                y2 = Float.parseFloat(lineY2Input.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": y2");
                return false;
            }
            shapeController.maskLineData = new DoubleLine(x1, y1, x2, y2);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

}
