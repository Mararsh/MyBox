package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;

/**
 * @Author Mara
 * @CreateDate 2019-8-13
 * @License Apache License Version 2.0
 */
public class ImageManufactureViewController extends ImageManufactureOperationController {

    protected int zoomStep;

    @FXML
    protected ComboBox<String> zoomStepSelector;

    @Override
    public void initPane() {
        try {
            imageController.rulerXCheck = rulerXCheck;
            imageController.rulerYCheck = rulerYCheck;
            imageController.coordinateCheck = coordinateCheck;
            imageController.contextMenuCheck = contextMenuCheck;

            zoomStepSelector.getItems().addAll(
                    Arrays.asList("10", "20", "5", "1", "3", "15", "30", "25", "45")
            );
            zoomStepSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
                    zoomStep = Integer.valueOf(newVal);
                    AppVariables.setUserConfigValue(baseName + "ZoomStep", zoomStep + "");
                    if (imageController != null && imageController.image != null) {
                        imageController.xZoomStep = (int) (imageController.image.getWidth() * zoomStep / 100);
                        imageController.yZoomStep = (int) (imageController.image.getHeight() * zoomStep / 100);
                    }
                    if (scopeController != null && scopeController.image != null) {
                        scopeController.xZoomStep = (int) (scopeController.image.getWidth() * zoomStep / 100);
                        scopeController.yZoomStep = (int) (scopeController.image.getHeight() * zoomStep / 100);
                    }
                }
            });
            zoomStepSelector.getSelectionModel().select(AppVariables.getUserConfigValue(baseName + "ZoomStep", "10"));

            rulerXCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (imageController == null) {
                        return;
                    }
                    AppVariables.setUserConfigValue(baseName + "RulerX", rulerXCheck.isSelected());
                    imageController.checkRulerX();
                }
            });
            rulerXCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "RulerX", false));

            rulerYCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (imageController == null) {
                        return;
                    }
                    AppVariables.setUserConfigValue(baseName + "RulerY", rulerYCheck.isSelected());
                    imageController.checkRulerY();
                }
            });
            rulerYCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "RulerY", false));

            coordinateCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (imageController == null) {
                        return;
                    }
                    AppVariables.setUserConfigValue(baseName + "PopCooridnate", coordinateCheck.isSelected());
                    imageController.checkCoordinate();
                }
            });
            coordinateCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "PopCooridnate", false));

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

}
