package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import mara.mybox.fxml.FxmlColor;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlImageManufacture;
import mara.mybox.image.ImageManufacture;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
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
    protected Rectangle bgRect;
    @FXML
    protected Button paletteButton;
    @FXML
    private ComboBox<String> arcBox, perBox;
    @FXML
    private ToggleGroup arcGroup;

    public ImageManufactureBatchArcController() {
        baseTitle = AppVariables.message("ImageManufactureBatchArc");

    }

    @Override
    public void initializeNext() {
        try {
            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(Bindings.isEmpty(targetPathInput.textProperty())
                    .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                    .or(Bindings.isEmpty(tableView.getItems()))
                    .or(arcBox.getEditor().styleProperty().isEqualTo(badStyle))
                    .or(perBox.getEditor().styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @Override
    public void initOptionsSection() {
        try {
            super.initOptionsSection();

            arcBox.getItems().addAll(Arrays.asList("15", "30", "50", "150", "300", "10", "3"));
            arcBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkArc();
                }
            });
            arcBox.getSelectionModel().select(0);

            FxmlControl.setTooltip(perBox, new Tooltip("1~100"));

            perBox.getItems().addAll(Arrays.asList("15", "25", "30", "10", "12", "8"));
            perBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkPercent();
                }
            });
            perBox.getSelectionModel().select(0);

            arcGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkType();
                }
            });
            checkType();

            String c = AppVariables.getUserConfigValue("ImageArcBackground", Color.TRANSPARENT.toString());
            bgRect.setFill(Color.web(c));
            FxmlControl.setTooltip(bgRect, FxmlColor.colorNameDisplay((Color) bgRect.getFill()));

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public boolean setColor(Control control, Color color) {
        if (control == null || color == null) {
            return false;
        }
        if (paletteButton.equals(control)) {
            bgRect.setFill(color);
            FxmlControl.setTooltip(bgRect, FxmlColor.colorNameDisplay(color));
            AppVariables.setUserConfigValue("ImageArcBackground", color.toString());
        }
        return true;
    }

    @FXML
    @Override
    public void showPalette(ActionEvent event) {
        showPalette(paletteButton, message("Arc"), true);
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
            percent = Integer.valueOf(perBox.getValue());
            if (percent > 0 && percent <= 100) {
                FxmlControl.setEditorNormal(perBox);
            } else {
                percent = 15;
                FxmlControl.setEditorBadStyle(perBox);
            }
        } catch (Exception e) {
            logger.debug(e.toString());
            percent = 15;
            FxmlControl.setEditorBadStyle(perBox);
        }
    }

    private void checkArc() {
        try {
            arc = Integer.valueOf(arcBox.getValue());
            if (arc >= 0) {
                FxmlControl.setEditorNormal(arcBox);
            } else {
                arc = 0;
                FxmlControl.setEditorBadStyle(arcBox);
            }
        } catch (Exception e) {
            logger.debug(e.toString());
            arc = 0;
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
                    FxmlImageManufacture.toAwtColor((Color) bgRect.getFill()));
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }

    }

}
