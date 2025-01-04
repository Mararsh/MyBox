package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import mara.mybox.image.data.ImageMosaic;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ValidationTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-9-2
 * @License Apache License Version 2.0
 */
public class ControlImageMosaic extends BaseController {

    protected int intensity;

    @FXML
    protected ComboBox<String> intensitySelector;

    @Override
    public void initControls() {
        try {
            super.initControls();

            intensity = UserConfig.getInt(baseName + "Intensity", 80);
            if (intensity <= 0) {
                intensity = 80;
            }
            intensitySelector.getItems().addAll(Arrays.asList("80", "20", "50", "10", "5", "100", "15", "20", "60"));
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
        if (v > 0) {
            intensity = v;
            ValidationTools.setEditorNormal(intensitySelector);
            return true;
        } else {
            popError(message("InvalidParameter") + ": " + message("Intensity"));
            ValidationTools.setEditorBadStyle(intensitySelector);
            return false;
        }
    }

    public ImageMosaic pickValues(ImageMosaic.MosaicType type) {
        if (!checkIntensity()) {
            return null;
        }
        return ImageMosaic.create().setType(type).setIntensity(intensity);
    }
}
