package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-8-13
 * @License Apache License Version 2.0
 */
public class ImageRectangleController extends BaseShapeEditController {

    @FXML
    protected TextField rectXInput, rectYInput, rectWidthInput, rectHeightInput;
    @FXML
    protected ComboBox<String> roundSizeSelector;

    public ImageRectangleController() {
        baseTitle = message("Rectangle");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();

            operation = "Rectangle";

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
            float x, y, w, h, round;
            try {
                x = Float.parseFloat(rectXInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": x");
                return false;
            }
            try {
                y = Float.parseFloat(rectYInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": y");
                return false;
            }
            try {
                w = Float.parseFloat(rectWidthInput.getText());
            } catch (Exception e) {
                w = -1f;
            }
            if (w <= 0) {
                popError(message("InvalidParameter") + ": " + message("Width"));
                return false;
            }
            try {
                h = Float.parseFloat(rectHeightInput.getText());
            } catch (Exception e) {
                h = -1f;
            }
            if (h <= 0) {
                popError(message("InvalidParameter") + ": " + message("Height"));
                return false;
            }
            try {
                round = Float.parseFloat(roundSizeSelector.getValue());
            } catch (Exception e) {
                round = 0;
            }
            if (round < 0) {
                round = 0;
            }
            maskRectangleData = DoubleRectangle.xywh(x, y, w, h);
            maskRectangleData.setRound(round);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public void maskShapeChanged() {
        if (isSettingValues || maskRectangleData == null) {
            return;
        }
        try {
            rectXInput.setText(scale(maskRectangleData.getX()) + "");
            rectYInput.setText(scale(maskRectangleData.getY()) + "");
            rectWidthInput.setText(scale(maskRectangleData.getWidth()) + "");
            rectHeightInput.setText(scale(maskRectangleData.getHeight()) + "");
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void initShape() {
        try {
            showMaskRectangle();

            float round = 0;
            try {
                round = Float.parseFloat(roundSizeSelector.getValue());
            } catch (Exception e) {
            }
            if (round < 0) {
                round = 0;
            }
            List<String> ws = new ArrayList<>();
            ws.addAll(Arrays.asList("0", "2", "5", "10", "15", "30", "40", "50"));
            int max = (int) (image.getWidth() / 4);
            int step = max / 10;
            for (int w = 10; w < max; w += step) {
                if (!ws.contains(w + "")) {
                    ws.add(0, w + "");
                }
            }
            roundSizeSelector.getItems().setAll(ws);
            roundSizeSelector.setValue(round + "");

            goShape();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        static methods
     */
    public static ImageRectangleController open(ImageEditorController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageRectangleController controller = (ImageRectangleController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.ImageRectangleFxml, false);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
