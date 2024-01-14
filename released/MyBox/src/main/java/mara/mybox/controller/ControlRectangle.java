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
    protected ComboBox<String> roundWidthSelector, roundHeightSelector;

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
            roundWidthSelector.setValue(shapeController.scale(shapeController.maskRectangleData.getRoundx()) + "");
            roundHeightSelector.setValue(shapeController.scale(shapeController.maskRectangleData.getRoundy()) + "");
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setRoundList() {
        if (shapeController == null) {
            return;
        }
        float roundx = 0;
        try {
            roundx = Float.parseFloat(roundWidthSelector.getValue());
        } catch (Exception e) {
        }
        if (roundx < 0) {
            roundx = 0;
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
        if (!ws.contains(roundx + "")) {
            ws.add(0, roundx + "");
        }
        roundWidthSelector.getItems().setAll(ws);
        roundWidthSelector.setValue(roundx + "");

        float roundy = 0;
        try {
            roundy = Float.parseFloat(roundHeightSelector.getValue());
        } catch (Exception e) {
        }
        if (roundy < 0) {
            roundy = 0;
        }
        if (!ws.contains(roundy + "")) {
            ws.add(0, roundy + "");
        }
        roundHeightSelector.getItems().setAll(ws);
        roundHeightSelector.setValue(roundx + "");
    }

    public boolean pickValues() {
        try {
            float x, y, w, h, roundx, roundy;
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
                roundx = Float.parseFloat(roundWidthSelector.getValue());
            } catch (Exception e) {
                roundx = 0;
            }
            if (roundx < 0) {
                roundx = 0;
            }
            try {
                roundy = Float.parseFloat(roundHeightSelector.getValue());
            } catch (Exception e) {
                roundy = 0;
            }
            if (roundy < 0) {
                roundy = 0;
            }
            shapeController.maskRectangleData = DoubleRectangle.xywh(x, y, w, h);
            shapeController.maskRectangleData.setRoundx(roundx);
            shapeController.maskRectangleData.setRoundy(roundy);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

}
