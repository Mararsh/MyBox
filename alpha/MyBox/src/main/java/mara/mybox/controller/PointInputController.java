package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import mara.mybox.data.DoublePoint;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-8-2
 * @License Apache License Version 2.0
 */
public class PointInputController extends BaseInputController {

    protected double x, y;
    protected int scale;
    protected DoublePoint point, picked;

    @FXML
    protected TextField xInput, yInput;

    public void setParameters(BaseController parent, String title,
            DoublePoint inPoint, int scale) {
        try {
            super.setParameters(parent, title);

            point = inPoint;
            recoverButton.setVisible(point != null);
            if (point != null) {
                xInput.setText(point.getX() + "");
                yInput.setText(point.getY() + "");
            }

        } catch (Exception e) {
            MyBoxLog.debug(e);

        }
    }

    @FXML
    @Override
    public boolean checkInput() {
        picked = null;
        try {
            x = Double.parseDouble(xInput.getText());
        } catch (Exception e) {
            popError(message("InvalidValue") + ": x");
            return false;
        }
        try {
            y = Double.parseDouble(yInput.getText());
        } catch (Exception e) {
            popError(message("InvalidValue") + ": y");
            return false;
        }
        picked = new DoublePoint(x, y);
        return picked != null;
    }

    @FXML
    @Override
    public void recoverAction() {
        if (point == null) {
            return;
        }
        xInput.setText(point.getX() + "");
        yInput.setText(point.getY() + "");
    }

    public static PointInputController open(BaseController parent,
            String title, DoublePoint point, int scale) {
        try {
            PointInputController controller = (PointInputController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.PointInputFxml, true);
            controller.setParameters(parent, title, point, scale);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
