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
public class ImageManufactureBatchArcController extends ImageManufactureBatchController {

    private int arc, percent;
    private boolean isPercent;

    @FXML
    protected ColorSetController colorSetController;
    @FXML
    protected ComboBox<String> arcBox, perBox;
    @FXML
    protected ToggleGroup arcGroup;

    public ImageManufactureBatchArcController() {
        baseTitle = AppVariables.message("ImageManufactureBatchArc");

    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(Bindings.isEmpty(targetPathInput.textProperty())
                    .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                    .or(Bindings.isEmpty(tableView.getItems()))
                    .or(arcBox.getEditor().styleProperty().isEqualTo(badStyle))
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

            arc = AppVariables.getUserConfigInt(baseName + "Arc", 15);
            arcBox.getItems().addAll(Arrays.asList("15", "30", "50", "150", "300", "10", "3"));
            arcBox.setValue(arc + "");
            arcBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkArc();
                }
            });

            FxmlControl.setTooltip(perBox, new Tooltip("1~100"));

            percent = AppVariables.getUserConfigInt(baseName + "Percent", 15);
            perBox.getItems().addAll(Arrays.asList("15", "25", "30", "10", "12", "8"));
            perBox.setValue(percent + "");
            perBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkPercent();
                }
            });

            arcGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkType();
                }
            });
            checkType();

            colorSetController.init(this, baseName + "Color");

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    private void checkType() {
        arcBox.setDisable(true);
        perBox.setDisable(true);
        arcBox.getEditor().setStyle(null);
        perBox.getEditor().setStyle(null);

        RadioButton selected = (RadioButton) arcGroup.getSelectedToggle();
        if (message("WidthPercentage").equals(selected.getText())) {
            isPercent = true;
            perBox.setDisable(false);
            checkPercent();

        } else if (message("Custom").equals(selected.getText())) {
            isPercent = false;
            arcBox.setDisable(false);
            checkArc();

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

    private void checkArc() {
        try {
            int v = Integer.valueOf(arcBox.getValue());
            if (v > 0) {
                arc = v;
                AppVariables.setUserConfigInt(baseName + "Arc", arc);
                FxmlControl.setEditorNormal(arcBox);
            } else {
                FxmlControl.setEditorBadStyle(arcBox);
            }
        } catch (Exception e) {
            FxmlControl.setEditorBadStyle(arcBox);
        }
    }

    @Override
    protected BufferedImage handleImage(BufferedImage source) {
        try {
            int value = arc;
            if (isPercent) {
                value = source.getWidth() * percent / 100;
            }
            BufferedImage target = ImageManufacture.addArc(source, value,
                    FxmlImageManufacture.toAwtColor((Color) colorSetController.rect.getFill()));
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }

    }

}
