package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import static mara.mybox.value.AppVaribles.logger;
import static mara.mybox.value.AppVaribles.getMessage;
import static mara.mybox.fxml.FxmlTools.badStyle;
import mara.mybox.image.ImageTransform;

/**
 * @Author Mara
 * @CreateDate 2018-9-26
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureBatchTransformController extends ImageManufactureBatchController {

    private int transformType, rotateAngle;
    private float shearX;

    @FXML
    private ToggleGroup transformGroup;
    @FXML
    private ComboBox<String> shearBox, angleBox;
    @FXML
    private Slider angleSlider;

    private static class TransformType {

        public static int Shear = 0;
        public static int VerticalMirror = 1;
        public static int HorizontalMirror = 2;
        public static int Rotate = 3;
    }

    public ImageManufactureBatchTransformController() {

    }

    @Override
    protected void initializeNext2() {
        try {

            operationBarController.startButton.disableProperty().bind(Bindings.isEmpty(targetPathInput.textProperty())
                    .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                    .or(Bindings.isEmpty(sourceFilesInformation))
                    .or(shearBox.getEditor().styleProperty().isEqualTo(badStyle))
                    .or(angleBox.getEditor().styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @Override
    protected void initOptionsSection() {
        try {
            super.initOptionsSection();

            transformGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkTransformType();
                }
            });
            checkTransformType();

            List<String> shears = Arrays.asList(
                    "0.5", "-0.5", "0.4", "-0.4", "0.2", "-0.2", "0.1", "-0.1",
                    "0.7", "-0.7", "0.9", "-0.9", "0.8", "-0.8", "1", "-1",
                    "1.5", "-1.5", "2", "-2");
            shearBox.getItems().addAll(shears);
            shearBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkShear();
                }
            });
            shearBox.getSelectionModel().select(0);

            angleSlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    rotateAngle = newValue.intValue();
                    angleBox.getEditor().setText(rotateAngle + "");
                }
            });

            angleBox.getItems().addAll(Arrays.asList("90", "180", "45", "30", "60", "15", "75", "120", "135"));
            angleBox.setVisibleRowCount(10);
            angleBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkAngle();
                }
            });
            angleBox.getSelectionModel().select(0);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkTransformType() {
        shearBox.setDisable(true);
        shearBox.getEditor().setStyle(null);
        angleBox.setDisable(true);
        angleBox.getEditor().setStyle(null);
        angleSlider.setDisable(true);

        RadioButton selected = (RadioButton) transformGroup.getSelectedToggle();
        if (getMessage("Shear").equals(selected.getText())) {
            transformType = TransformType.Shear;
            shearBox.setDisable(false);
            checkShear();

        } else if (getMessage("VerticalMirror").equals(selected.getText())) {
            transformType = TransformType.VerticalMirror;

        } else if (getMessage("HorizontalMirror").equals(selected.getText())) {
            transformType = TransformType.HorizontalMirror;

        } else if (getMessage("RotateAngle").equals(selected.getText())) {
            transformType = TransformType.Rotate;
            angleBox.setDisable(false);
            angleSlider.setDisable(false);
            checkAngle();

        }
    }

    private void checkShear() {
        try {
            shearX = Float.valueOf(shearBox.getValue());
            shearBox.getEditor().setStyle(null);
        } catch (Exception e) {
            shearX = 0;
            shearBox.getEditor().setStyle(badStyle);
        }
    }

    private void checkAngle() {
        try {
            rotateAngle = Integer.valueOf(angleBox.getValue());
            angleSlider.setValue(rotateAngle);
            angleBox.getEditor().setStyle(null);
        } catch (Exception e) {
            rotateAngle = 0;
            angleBox.getEditor().setStyle(badStyle);
        }
    }

    @Override
    protected BufferedImage handleImage(BufferedImage source) {
        try {
            BufferedImage target = null;
            if (transformType == TransformType.Shear) {
                target = ImageTransform.shearImage(source, shearX, 0);

            } else if (transformType == TransformType.VerticalMirror) {
                target = ImageTransform.verticalMirrorImage(source);

            } else if (transformType == TransformType.HorizontalMirror) {
                target = ImageTransform.horizontalMirrorImage(source);

            } else if (transformType == TransformType.Rotate) {
                target = ImageTransform.rotateImage(source, rotateAngle);
            }

            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }

    }

}
