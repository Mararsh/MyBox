package mara.mybox.controller;

import java.awt.image.BufferedImage;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import static mara.mybox.controller.BaseController.logger;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.fxml.FxmlImageTools;
import mara.mybox.fxml.FxmlTools;
import static mara.mybox.fxml.FxmlTools.badStyle;
import mara.mybox.image.ImageReplaceColorTools;

/**
 * @Author Mara
 * @CreateDate 2018-9-24
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureBatchReplaceColorController extends ImageManufactureBatchController {

    private int distance;
    private boolean isColor;

    @FXML
    private HBox colorBox;
    @FXML
    private ColorPicker oldColorPicker, newColorPicker;
    @FXML
    private Button transForScopeButton, transForNewButton;
    @FXML
    private TextField distanceInput;
    @FXML
    private RadioButton colorRadio, hueRadio;
    @FXML
    private ToggleGroup replaceScopeGroup;
    @FXML
    private CheckBox excludeCheck;

    public ImageManufactureBatchReplaceColorController() {

    }

    @Override
    protected void initializeNext2() {
        try {

            operationBarController.startButton.disableProperty().bind(Bindings.isEmpty(targetPathInput.textProperty())
                    .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                    .or(Bindings.isEmpty(sourceFilesInformation))
                    .or(distanceInput.styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @Override
    protected void initOptionsSection() {
        try {
            super.initOptionsSection();

            Tooltip tips = new Tooltip(getMessage("ColorMatchComments2"));
            tips.setFont(new Font(16));
            FxmlTools.setComments(colorBox, tips);

            newColorPicker.valueProperty().addListener(new ChangeListener<Color>() {
                @Override
                public void changed(ObservableValue<? extends Color> observable,
                        Color oldValue, Color newValue) {
                    checkDistance();
                }
            });
            newColorPicker.setValue(Color.WHITE);

            oldColorPicker.valueProperty().addListener(new ChangeListener<Color>() {
                @Override
                public void changed(ObservableValue<? extends Color> observable,
                        Color oldValue, Color newValue) {
                    checkDistance();
                }
            });
            oldColorPicker.setValue(Color.BLACK);

            distanceInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkDistance();
                }
            });

            replaceScopeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkColorType();
                }
            });
            checkColorType();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkColorType() {
        RadioButton selected = (RadioButton) replaceScopeGroup.getSelectedToggle();
        isColor = getMessage("Color").equals(selected.getText());
        if (isColor) {
            FxmlTools.quickTooltip(distanceInput, new Tooltip("0 ~ 255"));
        } else {
            FxmlTools.quickTooltip(distanceInput, new Tooltip("0 ~ 360"));
        }
        checkDistance();
    }

    private void checkDistance() {
        try {
            distance = Integer.valueOf(distanceInput.getText());
            if (oldColorPicker.getValue() == newColorPicker.getValue() && distance == 0) {
                popError(getMessage("OriginalNewSameColor"));
                distanceInput.setStyle(badStyle);
                return;
            }
            int max = 255;
            if (!isColor) {
                max = 360;
            }
            if (distance >= 0 && distance <= max) {

                distanceInput.setStyle(null);
            } else {
                distanceInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            distanceInput.setStyle(badStyle);
        }
    }

    @FXML
    private void transparentForOld(ActionEvent event) {
        oldColorPicker.setValue(Color.TRANSPARENT);
    }

    @FXML
    private void whiteForOld(ActionEvent event) {
        oldColorPicker.setValue(Color.WHITE);
    }

    @FXML
    private void blackForOld(ActionEvent event) {
        oldColorPicker.setValue(Color.BLACK);
    }

    @FXML
    private void transparentForNew(ActionEvent event) {
        newColorPicker.setValue(Color.TRANSPARENT);
    }

    @FXML
    private void whiteForNew(ActionEvent event) {
        newColorPicker.setValue(Color.WHITE);
    }

    @FXML
    private void blackForNew(ActionEvent event) {
        newColorPicker.setValue(Color.BLACK);
    }

    @Override
    protected BufferedImage handleImage(BufferedImage source) {
        BufferedImage target = ImageReplaceColorTools.replaceColor(source,
                FxmlImageTools.colorConvert(oldColorPicker.getValue()),
                FxmlImageTools.colorConvert(newColorPicker.getValue()),
                distance, isColor, excludeCheck.isSelected());
        return target;
    }

}
