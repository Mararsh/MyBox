package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import mara.mybox.bufferedimage.ShadowTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-9-25
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureBatchShadowController extends BaseImageEditBatchController {

    private int shadow, percent;
    private boolean isPercent;

    @FXML
    protected ControlColorSet colorSetController;
    @FXML
    protected ToggleGroup shadowGroup;
    @FXML
    protected ComboBox<String> perBox, shadowBox;

    public ImageManufactureBatchShadowController() {
        baseTitle = Languages.message("ImageManufactureBatchShadow");

    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(targetPathController.valid.not()
                    .or(Bindings.isEmpty(tableView.getItems()))
                    .or(shadowBox.getEditor().styleProperty().isEqualTo(UserConfig.badStyle()))
                    .or(perBox.getEditor().styleProperty().isEqualTo(UserConfig.badStyle()))
            );

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(perBox, new Tooltip("1~100"));
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void initOptionsSection() {
        try {
            super.initOptionsSection();

            shadow = UserConfig.getInt(baseName + "Shadow", 15);
            shadowBox.getItems().addAll(Arrays.asList("8", "5", "15", "3", "6", "1", "20", "30"));
            shadowBox.setValue(shadow + "");
            shadowBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkShadow();
                }
            });
            shadowBox.getSelectionModel().select(0);

            percent = UserConfig.getInt(baseName + "Percent", 5);
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
            MyBoxLog.error(e);
        }
    }

    private void checkType() {
        shadowBox.setDisable(true);
        perBox.setDisable(true);
        shadowBox.getEditor().setStyle(null);
        perBox.getEditor().setStyle(null);

        RadioButton selected = (RadioButton) shadowGroup.getSelectedToggle();
        if (Languages.message("WidthPercentage").equals(selected.getText())) {
            isPercent = true;
            perBox.setDisable(false);
            checkPercent();

        } else if (Languages.message("Custom").equals(selected.getText())) {
            isPercent = false;
            shadowBox.setDisable(false);
            checkShadow();

        }
    }

    private void checkPercent() {
        try {
            int v = Integer.parseInt(perBox.getValue());
            if (v > 0 && v <= 100) {
                percent = v;
                UserConfig.setInt(baseName + "Percent", percent);
                ValidationTools.setEditorNormal(perBox);
            } else {
                ValidationTools.setEditorBadStyle(perBox);
            }
        } catch (Exception e) {
            ValidationTools.setEditorBadStyle(perBox);
        }
    }

    private void checkShadow() {
        try {
            int v = Integer.parseInt(shadowBox.getValue());
            if (v > 0) {
                shadow = v;
                UserConfig.setInt(baseName + "Shadow", shadow);
                ValidationTools.setEditorNormal(shadowBox);
            } else {
                ValidationTools.setEditorBadStyle(shadowBox);
            }
        } catch (Exception e) {
            ValidationTools.setEditorBadStyle(shadowBox);
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
            target = ShadowTools.addShadowAlpha(source, value, FxColorTools.toAwtColor(color));
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }

    }

}
