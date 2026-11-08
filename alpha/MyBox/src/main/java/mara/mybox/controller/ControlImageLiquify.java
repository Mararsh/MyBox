package mara.mybox.controller;

import java.util.Arrays;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-8-13
 * @License Apache License Version 2.0
 */
public class ControlImageLiquify extends BaseController {

    protected int radius = 50, step = 20, safeMargin = 12;

    @FXML
    protected ComboBox<String> fallOffSelector, samplerSelector, radiusSelector;
    @FXML
    protected ToggleGroup operationGroup;
    @FXML
    protected RadioButton pushRadio, expandRadio, shrinkRadio, rotateRadio, rotateCounterRadio, turbulenceRadio;

    public ControlImageLiquify() {
        TipsLabelKey = "ImageLiquifyComments";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            radius = UserConfig.getInt(baseName + "Radius", 80);
            radiusSelector.getItems().addAll(Arrays.asList(
                    "80", "100", "50", "200", "20", "400", "10", "5", "150", "30")
            );
            radiusSelector.setValue(radius + "");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    private boolean checkRadius() {
        int v = -1;
        try {
            v = Integer.parseInt(radiusSelector.getValue());
        } catch (Exception e) {
        }
        if (v > 0) {
            radius = v;
            UserConfig.setInt(baseName + "Radius", radius);
            return true;
        } else {
            popError(message("InvalidParameter") + ": " + message("BrushRadius"));
            return false;
        }
    }

    public boolean pickValues() {
        if (!checkRadius()) {
            return false;
        }
        return true;
    }

    @FXML
    public void goPointAction() {

    }

}
