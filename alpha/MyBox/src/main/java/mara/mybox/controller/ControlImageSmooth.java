package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ValidationTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-9-2
 * @License Apache License Version 2.0
 */
public class ControlImageSmooth extends BaseController {

    protected int intensity;

    @FXML
    protected ComboBox<String> intensitySelector;
    @FXML
    protected RadioButton avarageRadio, gaussianRadio, motionRadio;

    @Override
    public void initControls() {
        try {
            super.initControls();

            intensity = UserConfig.getInt(baseName + "Intensity", 10);
            if (intensity <= 0) {
                intensity = 10;
            }
            intensitySelector.getItems().addAll(Arrays.asList("3", "5", "2", "1", "4"));
            intensitySelector.setValue(intensity + "");
            intensitySelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v > 0) {
                            intensity = v;
                            UserConfig.setInt(baseName + "Intensity", intensity);
                            ValidationTools.setEditorNormal(intensitySelector);
                        } else {
                            ValidationTools.setEditorBadStyle(intensitySelector);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(intensitySelector);
                    }
                }
            });
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected boolean checkIntensity() {
        if (isSettingValues) {
            return true;
        }
        int v;
        try {
            v = Integer.parseInt(intensitySelector.getValue());
        } catch (Exception e) {
            v = -1;
        }
        if (v > 0) {
            intensity = v;
            UserConfig.setInt(baseName + "Intensity", v);
            ValidationTools.setEditorNormal(intensitySelector);
            return true;
        } else {
            popError(message("InvalidParameter") + ": " + message("Intensity"));
            ValidationTools.setEditorBadStyle(intensitySelector);
            return false;
        }
    }

    protected ConvolutionKernel pickValues() {
        if (!checkIntensity()) {
            return null;
        }
        try {
            if (avarageRadio.isSelected()) {
                return ConvolutionKernel.makeAverageBlur(intensity);
            } else if (gaussianRadio.isSelected()) {
                return ConvolutionKernel.makeGaussBlur(intensity);
            } else if (motionRadio.isSelected()) {
                return ConvolutionKernel.makeMotionBlur(intensity);
            }
        } catch (Exception e) {
            displayError(e.toString());
        }
        return null;
    }

}
