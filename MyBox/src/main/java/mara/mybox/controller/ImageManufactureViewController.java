package mara.mybox.controller;

import mara.mybox.controller.base.ImageManufactureController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;

/**
 * @Author Mara
 * @CreateDate 2018-10-11
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureViewController extends ImageManufactureController {

    private int moveStep, zoomStep;

    @FXML
    protected Slider zoomSlider;
    @FXML
    protected TextField stepInput;

    public ImageManufactureViewController() {
    }

    @Override
    public void initializeNext2() {
        try {
            initCommon();
            initViewTab();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initViewTab() {
        try {

            moveStep = 40;

            zoomSlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    zoomStep = newValue.intValue();
                    stepInput.setText(zoomStep + "");
                }
            });
            zoomSlider.setValue(10);

            stepInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkStepInput();
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    protected void initInterface() {
        try {
            if (values == null || values.getImage() == null) {
                return;
            }
            super.initInterface();
            tabPane.getSelectionModel().select(viewTab);

            zoomStep = (int) image.getWidth() / xZoomStep;
            stepInput.setText(zoomStep + "");

            isSettingValues = true;

            isSettingValues = false;
        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    private void checkStepInput() {
        try {
            int v = Integer.valueOf(stepInput.getText());
            if (v > 0) {
                zoomStep = v;
                stepInput.setStyle(null);
                zoomSlider.setValue(zoomStep);
                if (image != null) {
                    xZoomStep = (int) image.getWidth() * zoomStep / 100;
                    yZoomStep = (int) image.getHeight() * zoomStep / 100;
                }
            } else {
                stepInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            stepInput.setStyle(badStyle);
        }
    }

    @FXML
    @Override
    public void moveRight() {
        FxmlControl.setScrollPane(scrollPane, 0 - moveStep, scrollPane.getVvalue());
        if (values.isRefSync() && refPane != null) {
            FxmlControl.setScrollPane(refPane, 0 - moveStep, refPane.getVvalue());
        }
        if (scopePane != null) {
            FxmlControl.setScrollPane(scopePane, 0 - moveStep, scopePane.getVvalue());
        }
    }

    @FXML
    @Override
    public void moveLeft() {
        FxmlControl.setScrollPane(scrollPane, moveStep, scrollPane.getVvalue());
        if (values.isRefSync() && refPane != null) {
            FxmlControl.setScrollPane(refPane, moveStep, refPane.getVvalue());
        }
        if (scopePane != null) {
            FxmlControl.setScrollPane(scopePane, moveStep, scopePane.getVvalue());
        }
    }

    @FXML
    @Override
    public void moveUp() {
        FxmlControl.setScrollPane(scrollPane, scrollPane.getHvalue(), moveStep);
        if (values.isRefSync() && refPane != null) {
            FxmlControl.setScrollPane(refPane, refPane.getHvalue(), moveStep);
        }
        if (scopePane != null) {
            FxmlControl.setScrollPane(scopePane, scopePane.getHvalue(), moveStep);
        }
    }

    @FXML
    @Override
    public void moveDown() {
        FxmlControl.setScrollPane(scrollPane, scrollPane.getHvalue(), 0 - moveStep);
        if (values.isRefSync() && refPane != null) {
            FxmlControl.setScrollPane(refPane, refPane.getHvalue(), 0 - moveStep);
        }
        if (scopePane != null) {
            FxmlControl.setScrollPane(scopePane, scopePane.getHvalue(), 0 - moveStep);
        }
    }

}
