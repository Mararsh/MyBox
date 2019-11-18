package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyEvent;
import mara.mybox.controller.ImageManufactureController.ImageOperation;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;

/**
 * @Author Mara
 * @CreateDate 2019-8-13
 * @License Apache License Version 2.0
 */
public class ImageManufactureViewController extends ImageManufactureOperationController {

    @FXML
    protected ComboBox<String> zoomStepSelector;
    @FXML
    protected CheckBox syncCheck, rulerXCheck, rulerYCheck, coordinateCheck;

    public ImageManufactureViewController() {
        baseTitle = AppVariables.message("ImageManufactureView");
        operation = ImageOperation.Crop;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            myPane = viewPane;

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @Override
    public void initPane(ImageManufactureController parent) {
        try {
            super.initPane(parent);
            if (parent == null) {
                return;
            }
            imageController.operatingNeedNotScope();

            zoomStepSelector.getItems().addAll(
                    Arrays.asList("10", "20", "5", "1", "3", "15", "30", "25", "45")
            );
            zoomStepSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
                    if (parent == null || parent.image == null
                            || parent.currentImageController == null) {
                        return;
                    }
                    parent.zoomStep = Integer.valueOf(newVal);
                    AppVariables.setUserConfigValue("ImageZoomStep", parent.zoomStep + "");
                    parent.xZoomStep = (int) (parent.image.getWidth() * parent.zoomStep / 100);
                    parent.yZoomStep = (int) (parent.image.getHeight() * parent.zoomStep / 100);
                    parent.currentImageController.xZoomStep
                            = (int) (parent.currentImageController.image.getWidth() * parent.zoomStep / 100);
                    parent.currentImageController.yZoomStep
                            = (int) (parent.currentImageController.image.getHeight() * parent.zoomStep / 100);
                    parent.hisImageController.xZoomStep
                            = (int) (parent.hisImageController.image.getWidth() * parent.zoomStep / 100);
                    parent.hisImageController.yZoomStep
                            = (int) (parent.hisImageController.image.getHeight() * parent.zoomStep / 100);
                    parent.refImageController.xZoomStep
                            = (int) (parent.refImageController.image.getWidth() * parent.zoomStep / 100);
                    parent.refImageController.yZoomStep
                            = (int) (parent.refImageController.image.getHeight() * parent.zoomStep / 100);
                }
            });
            zoomStepSelector.getSelectionModel().select(AppVariables.getUserConfigValue("ImageZoomStep", "10"));

            rulerXCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (parent == null) {
                        return;
                    }
                    AppVariables.setUserConfigValue("ImageRulerXKey", rulerXCheck.isSelected());
                    parent.checkRulerX();
                }
            });
            rulerXCheck.setSelected(AppVariables.getUserConfigBoolean("ImageRulerXKey", false));

            rulerYCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (parent == null) {
                        return;
                    }
                    AppVariables.setUserConfigValue("ImageRulerYKey", rulerYCheck.isSelected());
                    parent.checkRulerY();
                }
            });
            rulerYCheck.setSelected(AppVariables.getUserConfigBoolean("ImageRulerYKey", false));

            coordinateCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (parent == null) {
                        return;
                    }
                    AppVariables.setUserConfigValue("ImagePopCooridnate", coordinateCheck.isSelected());
                    AppVariables.ImagePopCooridnate = coordinateCheck.isSelected();
                    parent.checkCoordinate();
                }
            });
            coordinateCheck.setSelected(AppVariables.ImagePopCooridnate);

            syncCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    if (parent == null) {
                        return;
                    }
                    parent.sychronizeZoom = newVal;
                    AppVariables.setUserConfigValue("ImageZoomSychronize", newVal);
                }
            });
            syncCheck.setSelected(AppVariables.getUserConfigBoolean("ImageZoomSychronize", true));

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @Override
    public void eventsHandler(KeyEvent event) {
        // do thing. "Crop button" exists in both this pane and frame pane. Should only response once.
    }

}
