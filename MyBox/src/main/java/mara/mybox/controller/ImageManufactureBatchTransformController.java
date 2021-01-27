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
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.image.ImageManufacture;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;

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
    protected ToggleGroup transformGroup;
    @FXML
    protected ComboBox<String> shearBox, angleBox;
    @FXML
    protected Slider angleSlider;

    private static class TransformType {

        public static int Shear = 0;
        public static int VerticalMirror = 1;
        public static int HorizontalMirror = 2;
        public static int Rotate = 3;
    }

    public ImageManufactureBatchTransformController() {
        baseTitle = AppVariables.message("ImageManufactureBatchTransform");

    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(Bindings.isEmpty(targetPathInput.textProperty())
                    .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                    .or(Bindings.isEmpty(tableView.getItems()))
                    .or(shearBox.getEditor().styleProperty().isEqualTo(badStyle))
                    .or(angleBox.getEditor().styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void initOptionsSection() {
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

            try {
                float f = Float.valueOf(AppVariables.getUserConfigValue(baseName + "Shear", "0.5"));
                if (f >= 0.0f && f <= 1.0f) {
                    shearX = 0.5f;
                }
            } catch (Exception e) {
                shearX = 1.0f;
            }
            List<String> shears = Arrays.asList(
                    "0.5", "-0.5", "0.4", "-0.4", "0.2", "-0.2", "0.1", "-0.1",
                    "0.7", "-0.7", "0.9", "-0.9", "0.8", "-0.8", "1", "-1",
                    "1.5", "-1.5", "2", "-2");
            shearBox.getItems().addAll(shears);
            shearBox.setValue(shearX + "");
            shearBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
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

            rotateAngle = AppVariables.getUserConfigInt(baseName + "Rotate", 0);
            angleBox.getItems().addAll(Arrays.asList("90", "180", "45", "30", "60", "15", "75", "120", "135"));
            angleBox.setVisibleRowCount(10);
            angleBox.setValue(rotateAngle + "");
            angleBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkAngle();
                }
            });
            angleBox.getSelectionModel().select(0);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    private void checkTransformType() {
        shearBox.setDisable(true);
        angleBox.setDisable(true);
        angleSlider.setDisable(true);
        shearBox.getEditor().setStyle(null);
        angleBox.getEditor().setStyle(null);

        RadioButton selected = (RadioButton) transformGroup.getSelectedToggle();
        if (message("Shear").equals(selected.getText())) {
            transformType = TransformType.Shear;
            shearBox.setDisable(false);
            checkShear();

        } else if (message("VerticalMirror").equals(selected.getText())) {
            transformType = TransformType.VerticalMirror;

        } else if (message("HorizontalMirror").equals(selected.getText())) {
            transformType = TransformType.HorizontalMirror;

        } else if (message("RotateAngle").equals(selected.getText())) {
            transformType = TransformType.Rotate;
            angleBox.setDisable(false);
            angleSlider.setDisable(false);
            checkAngle();

        }
    }

    private void checkShear() {
        try {
            shearX = Float.valueOf(shearBox.getValue());
            AppVariables.setUserConfigValue(baseName + "Shear", shearX + "");
            FxmlControl.setEditorNormal(shearBox);
        } catch (Exception e) {
            FxmlControl.setEditorBadStyle(shearBox);
        }
    }

    private void checkAngle() {
        try {
            rotateAngle = Integer.valueOf(angleBox.getValue());
            angleSlider.setValue(rotateAngle);
            AppVariables.setUserConfigInt(baseName + "Rotate", rotateAngle);
            FxmlControl.setEditorNormal(angleBox);
        } catch (Exception e) {
            rotateAngle = 0;
            FxmlControl.setEditorBadStyle(angleBox);
        }
    }

    @Override
    protected BufferedImage handleImage(BufferedImage source) {
        try {
            BufferedImage target = null;
            if (transformType == TransformType.Shear) {
                target = ImageManufacture.shearImage(source, shearX, 0);

            } else if (transformType == TransformType.VerticalMirror) {
                target = ImageManufacture.verticalMirrorImage(source);

            } else if (transformType == TransformType.HorizontalMirror) {
                target = ImageManufacture.horizontalMirrorImage(source);

            } else if (transformType == TransformType.Rotate) {
                target = ImageManufacture.rotateImage(source, rotateAngle);
            }

            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }

    }

}
