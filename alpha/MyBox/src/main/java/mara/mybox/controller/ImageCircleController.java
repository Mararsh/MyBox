package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import mara.mybox.data.DoubleCircle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-8-13
 * @License Apache License Version 2.0
 */
public class ImageCircleController extends BaseShapeEditController {

    @FXML
    protected TextField circleXInput, circleYInput, circleRadiusInput;

    public ImageCircleController() {
        baseTitle = message("Circle");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();

            operation = "Circle";

            anchorCheck.setSelected(true);
            showAnchors = true;
            popShapeMenu = true;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean pickShape() {
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
            maskCircleData = new DoubleCircle(x, y, r);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public void maskShapeChanged() {
        if (isSettingValues || maskCircleData == null) {
            return;
        }
        try {
            circleXInput.setText(scale(maskCircleData.getCenterX()) + "");
            circleYInput.setText(scale(maskCircleData.getCenterY()) + "");
            circleRadiusInput.setText(scale(maskCircleData.getRadius()) + "");
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void initShape() {
        try {
            showMaskCircle();

            goShape();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }


    /*
        static methods
     */
    public static ImageCircleController open(ImageEditorController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageCircleController controller = (ImageCircleController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.ImageCircleFxml, false);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
