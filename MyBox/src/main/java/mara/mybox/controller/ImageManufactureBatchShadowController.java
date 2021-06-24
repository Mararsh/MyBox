package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlImageManufacture;
import mara.mybox.image.ImageManufacture;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2018-9-25
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureBatchShadowController extends BaseImageManufactureBatchController {

    private int shadow, percent;
    private boolean isPercent;

    @FXML
    protected ColorSet colorSetController;
    @FXML
    protected ToggleGroup shadowGroup;
    @FXML
    protected ComboBox<String> perBox, shadowBox;
    @FXML
    protected CheckBox preAlphaCheck;

    public ImageManufactureBatchShadowController() {
        baseTitle = AppVariables.message("ImageManufactureBatchShadow");

    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(Bindings.isEmpty(targetPathInput.textProperty())
                    .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                    .or(Bindings.isEmpty(tableView.getItems()))
                    .or(shadowBox.getEditor().styleProperty().isEqualTo(badStyle))
                    .or(perBox.getEditor().styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void initOptionsSection() {
        try {
            super.initOptionsSection();

            shadow = AppVariables.getUserConfigInt(baseName + "Shadow", 15);
            shadowBox.getItems().addAll(Arrays.asList("8", "5", "15", "3", "6", "1", "20", "30"));
            shadowBox.setValue(shadow + "");
            shadowBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkShadow();
                }
            });
            shadowBox.getSelectionModel().select(0);

            FxmlControl.setTooltip(perBox, new Tooltip("1~100"));

            percent = AppVariables.getUserConfigInt(baseName + "Percent", 5);
            perBox.getItems().addAll(Arrays.asList("2", "1", "3", "5", "4", "6", "8", "7", "10", "9"));
            perBox.setValue(percent + "");
            perBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkPercent();
                }
            });
            perBox.getSelectionModel().select(0);

            shadowGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkType();
                }
            });
            checkType();

            colorSetController.init(this, baseName + "Color", Color.BLACK);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    private void checkType() {
        shadowBox.setDisable(true);
        perBox.setDisable(true);
        shadowBox.getEditor().setStyle(null);
        perBox.getEditor().setStyle(null);

        RadioButton selected = (RadioButton) shadowGroup.getSelectedToggle();
        if (message("WidthPercentage").equals(selected.getText())) {
            isPercent = true;
            perBox.setDisable(false);
            checkPercent();

        } else if (message("Custom").equals(selected.getText())) {
            isPercent = false;
            shadowBox.setDisable(false);
            checkShadow();

        }
    }

    private void checkPercent() {
        try {
            int v = Integer.valueOf(perBox.getValue());
            if (v > 0 && v <= 100) {
                percent = v;
                AppVariables.setUserConfigInt(baseName + "Percent", percent);
                FxmlControl.setEditorNormal(perBox);
            } else {
                FxmlControl.setEditorBadStyle(perBox);
            }
        } catch (Exception e) {
            FxmlControl.setEditorBadStyle(perBox);
        }
    }

    private void checkShadow() {
        try {
            int v = Integer.valueOf(shadowBox.getValue());
            if (v > 0) {
                shadow = v;
                AppVariables.setUserConfigInt(baseName + "Shadow", shadow);
                FxmlControl.setEditorNormal(shadowBox);
            } else {
                FxmlControl.setEditorBadStyle(shadowBox);
            }
        } catch (Exception e) {
            FxmlControl.setEditorBadStyle(shadowBox);
        }
    }

    @Override
    protected BufferedImage handleImage(BufferedImage source) {
        try {
            int value = shadow;
            if (isPercent) {
                value = source.getWidth() * percent / 100;
            }
            Color color = (Color) colorSetController.rect.getFill();
            BufferedImage target;
            if (preAlphaCheck.isSelected()) {
                target = ImageManufacture.addShadowNoAlpha(source, value, FxmlImageManufacture.toAwtColor(color));
            } else {
                target = ImageManufacture.addShadowAlpha(source, value, FxmlImageManufacture.toAwtColor(color));
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }

    }

}
