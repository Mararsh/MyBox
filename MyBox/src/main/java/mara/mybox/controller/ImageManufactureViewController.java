package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.fxml.FxmlTools;
import static mara.mybox.fxml.FxmlTools.badStyle;

/**
 * @Author Mara
 * @CreateDate 2018-10-11
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureViewController extends ImageManufactureController {

    private final int moveStep = 40;

    @FXML
    protected Slider zoomSlider;
    @FXML
    protected TextField stepInput;

    public ImageManufactureViewController() {
    }

    @Override
    protected void initializeNext2() {
        try {
            initCommon();
            initViewTab();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

//    @Override
//    protected void initInterface() {
//        try {
//            if (values == null || values.getImage() == null) {
//                return;
//            }
//            super.initInterface();
//
//            isSettingValues = true;
//
//            isSettingValues = false;
//        } catch (Exception e) {
//            logger.debug(e.toString());
//        }
//
//    }
    protected void initViewTab() {
        try {

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

    private void checkStepInput() {
        try {
            zoomStep = Integer.valueOf(stepInput.getText());
            if (zoomStep > 0) {
                stepInput.setStyle(null);
                zoomSlider.setValue(zoomStep);
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
        FxmlTools.setScrollPane(scrollPane, 0 - moveStep, scrollPane.getVvalue());
        if (values.isRefSync() && refPane != null) {
            FxmlTools.setScrollPane(refPane, 0 - moveStep, refPane.getVvalue());
        }
        if (scopePane != null) {
            FxmlTools.setScrollPane(scopePane, 0 - moveStep, scopePane.getVvalue());
        }
    }

    @FXML
    @Override
    public void moveLeft() {
        FxmlTools.setScrollPane(scrollPane, moveStep, scrollPane.getVvalue());
        if (values.isRefSync() && refPane != null) {
            FxmlTools.setScrollPane(refPane, moveStep, refPane.getVvalue());
        }
        if (scopePane != null) {
            FxmlTools.setScrollPane(scopePane, moveStep, scopePane.getVvalue());
        }
    }

    @FXML
    @Override
    public void moveUp() {
        FxmlTools.setScrollPane(scrollPane, scrollPane.getHvalue(), moveStep);
        if (values.isRefSync() && refPane != null) {
            FxmlTools.setScrollPane(refPane, refPane.getHvalue(), moveStep);
        }
        if (scopePane != null) {
            FxmlTools.setScrollPane(scopePane, scopePane.getHvalue(), moveStep);
        }
    }

    @FXML
    @Override
    public void moveDown() {
        FxmlTools.setScrollPane(scrollPane, scrollPane.getHvalue(), 0 - moveStep);
        if (values.isRefSync() && refPane != null) {
            FxmlTools.setScrollPane(refPane, refPane.getHvalue(), 0 - moveStep);
        }
        if (scopePane != null) {
            FxmlTools.setScrollPane(scopePane, scopePane.getHvalue(), 0 - moveStep);
        }
    }

}
