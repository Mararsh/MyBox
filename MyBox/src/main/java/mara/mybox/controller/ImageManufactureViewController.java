package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2019-8-13
 * @License Apache License Version 2.0
 */
public class ImageManufactureViewController extends ImageManufactureOperationController {

    @FXML
    protected CheckBox fitSizeCheck;

    @Override
    public void initPane() {
        try {
            imageController.rulerXCheck = rulerXCheck;
            imageController.rulerYCheck = rulerYCheck;
            imageController.coordinateCheck = coordinateCheck;
            imageController.contextMenuCheck = contextMenuCheck;
            imageController.zoomStepSelector = zoomStepSelector;
            image = imageController.image;

            imageController.initViewControls();
            imageController.setZoomStep(image);

            fitSizeCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "FitSize", false));
            fitSizeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue(baseName + "FitSize", fitSizeCheck.isSelected());
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void initViewControls() {
    }

}
