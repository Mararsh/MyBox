package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import mara.mybox.data.DoublePoint;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-8-2
 * @License Apache License Version 2.0
 */
public class PointInputController extends BaseInputController {

    protected double x, y;
    protected DoublePoint point, picked;

    @FXML
    protected TextField xInput, yInput;

    public void setParameters(BaseController parent, String title, DoublePoint inPoint) {
        try {
            super.setParameters(parent, title);

            point = inPoint;
            recoverButton.setVisible(point != null);
            if (point != null) {
                int scale = UserConfig.imageScale();
                xInput.setText(DoubleTools.scale(point.getX(), scale) + "");
                yInput.setText(DoubleTools.scale(point.getY(), scale) + "");
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
        picked = DoublePoint.imageCoordinate(x, y);
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

    public static PointInputController open(BaseController parent, String title, DoublePoint point) {
        try {
            PointInputController controller = (PointInputController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.PointInputFxml, true);
            controller.setParameters(parent, title, point);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
