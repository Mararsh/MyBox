package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-8-13
 * @License Apache License Version 2.0
 */
public class ControlRectangle extends BaseController {

    protected BaseShapeController shapeController;

    @FXML
    protected TextField rectXInput, rectYInput, rectWidthInput, rectHeightInput;
    @FXML
    protected ComboBox<String> roundSizeSelector;

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
        if (isSettingValues || shapeController.maskRectangleData == null) {
            return;
        }
        try {
            rectXInput.setText(shapeController.scale(shapeController.maskRectangleData.getX()) + "");
            rectYInput.setText(shapeController.scale(shapeController.maskRectangleData.getY()) + "");
            rectWidthInput.setText(shapeController.scale(shapeController.maskRectangleData.getWidth()) + "");
            rectHeightInput.setText(shapeController.scale(shapeController.maskRectangleData.getHeight()) + "");
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setRoundList() {
        if (shapeController == null) {
            return;
        }
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
        int max = (int) (shapeController.image.getWidth() / 4);
        int step = max / 10;
        for (int w = 10; w < max; w += step) {
            if (!ws.contains(w + "")) {
                ws.add(0, w + "");
            }
        }
        roundSizeSelector.getItems().setAll(ws);
        roundSizeSelector.setValue(round + "");
    }

    public boolean pickValues() {
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
            shapeController.maskRectangleData = DoubleRectangle.xywh(x, y, w, h);
            shapeController.maskRectangleData.setRound(round);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

}
