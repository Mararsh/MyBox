package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import mara.mybox.bufferedimage.PixelsOperation;
import mara.mybox.bufferedimage.PixelsOperationFactory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ValidationTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-9-2
 * @License Apache License Version 2.0
 */
public class ControlImageSepia extends BaseController {

    protected int intensity;

    @FXML
    protected ComboBox<String> intensitySelector;

    @Override
    public void initControls() {
        try {
            super.initControls();

            intensity = UserConfig.getInt(baseName + "Intensity", 60);
            if (intensity <= 0 || intensity >= 255) {
                intensity = 60;
            }
            intensitySelector.getItems().addAll(Arrays.asList("60", "80", "20", "50", "10", "5", "100", "15", "20"));
            intensitySelector.setValue(intensity + "");
            intensitySelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkIntensity();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean checkIntensity() {
        int v;
        try {
            v = Integer.parseInt(intensitySelector.getValue());
        } catch (Exception e) {
            v = -1;
        }
        if (v >= 0 && v <= 255) {
            intensity = v;
            ValidationTools.setEditorNormal(intensitySelector);
            return true;
        } else {
            popError(message("InvalidParameter") + ": " + message("Intensity"));
            ValidationTools.setEditorBadStyle(intensitySelector);
            return false;
        }
    }

    public PixelsOperation pickValues() {
        if (!checkIntensity()) {
            return null;
        }
        try {
            PixelsOperation pixelsOperation = PixelsOperationFactory.create(
                    null, null, PixelsOperation.OperationType.Sepia)
                    .setIntPara1(intensity);
            return pixelsOperation;
        } catch (Exception e) {
            displayError(e.toString());
            return null;
        }
    }

}
