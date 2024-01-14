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
public class ControlImageSharpen extends BaseController {

    protected int intensity;

    @FXML
    protected ComboBox<String> intensitySelector;
    @FXML
    protected RadioButton unmaskRadio, eightRadio, fourRadio;

    @Override
    public void initControls() {
        try {
            super.initControls();

            intensity = UserConfig.getInt(baseName + "Intensity", 2);
            if (intensity <= 0) {
                intensity = 2;
            }
            intensitySelector.getItems().addAll(Arrays.asList("2", "1", "3", "4"));
            intensitySelector.setValue(intensity + "");
            intensitySelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkIntensity();
                }
            });

            intensitySelector.disableProperty().bind(unmaskRadio.selectedProperty().not());

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
            if (unmaskRadio.isSelected()) {
                return ConvolutionKernel.makeUnsharpMasking(intensity);
            } else if (eightRadio.isSelected()) {
                return ConvolutionKernel.MakeSharpenEightNeighborLaplace();
            } else if (fourRadio.isSelected()) {
                return ConvolutionKernel.MakeSharpenFourNeighborLaplace();
            }
        } catch (Exception e) {
            displayError(e.toString());
        }
        return null;
    }

}
