package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ValidationTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-8-13
 * @License Apache License Version 2.0
 */
public class ControlImageShear extends BaseController {

    protected float shearX, shearY;

    @FXML
    protected ComboBox<String> xSelector, ySelector;

    public ControlImageShear() {
        TipsLabelKey = "ImageShearComments";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            shearX = UserConfig.getFloat(baseName + "ShearX", 1f);
            xSelector.getItems().addAll(Arrays.asList(
                    "1", "-1", "1.5", "-1.5", "2", "-2", "3", "-3", "4", "-4", "5", "-5", "0",
                    "0.5", "-0.5", "0.4", "-0.4", "0.3", "-0.3", "0.2", "-0.2", "0.1", "-0.1",
                    "0.7", "-0.7", "0.8", "-0.8", "0.9", "-0.9")
            );
            xSelector.setValue(shearX + "");
            xSelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkX();
                }
            });

            shearY = UserConfig.getFloat(baseName + "ShearY", 0f);
            ySelector.getItems().addAll(Arrays.asList(
                    "1", "-1", "1.5", "-1.5", "2", "-2", "3", "-3", "4", "-4", "5", "-5", "0",
                    "0.5", "-0.5", "0.4", "-0.4", "0.3", "-0.3", "0.2", "-0.2", "0.1", "-0.1",
                    "0.7", "-0.7", "0.8", "-0.8", "0.9", "-0.9")
            );
            ySelector.setValue(shearY + "");
            ySelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkY();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    private boolean checkX() {
        try {
            shearX = Float.parseFloat(xSelector.getValue());
            ValidationTools.setEditorNormal(xSelector);
            return true;
        } catch (Exception e) {
            popError(message("InvalidParameter") + ": " + message("XRatio"));
            ValidationTools.setEditorBadStyle(xSelector);
            return false;
        }
    }

    private boolean checkY() {
        try {
            shearY = Float.parseFloat(ySelector.getValue());
            ValidationTools.setEditorNormal(ySelector);
            return true;
        } catch (Exception e) {
            popError(message("InvalidParameter") + ": " + message("YRatio"));
            ValidationTools.setEditorBadStyle(ySelector);
            return false;
        }
    }

    public boolean pickValues() {
        if (!checkX() || !checkY()) {
            return false;
        }
        UserConfig.setFloat(baseName + "ShearX", shearX);
        UserConfig.setFloat(baseName + "ShearY", shearY);
        return true;
    }

}
