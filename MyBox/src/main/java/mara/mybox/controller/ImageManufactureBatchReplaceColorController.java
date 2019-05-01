package mara.mybox.controller;

import mara.mybox.controller.base.ImageManufactureBatchController;
import java.awt.image.BufferedImage;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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
import static mara.mybox.value.AppVaribles.logger;
import static mara.mybox.value.AppVaribles.getMessage;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.image.ImageColor;
import mara.mybox.image.ImageScope;
import mara.mybox.image.PixelsOperation;
import mara.mybox.value.AppVaribles;

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
    private TextField distanceInput;
    @FXML
    private RadioButton colorRadio, hueRadio;
    @FXML
    private ToggleGroup replaceScopeGroup;
    @FXML
    private CheckBox excludeCheck;

    public ImageManufactureBatchReplaceColorController() {
        baseTitle = AppVaribles.getMessage("ImageManufactureBatchReplaceColor");

    }

    @Override
    public void initializeNext2() {
        try {

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(Bindings.isEmpty(targetPathInput.textProperty())
                    .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                    .or(Bindings.isEmpty(tableView.getItems()))
                    .or(distanceInput.styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @Override
    public void initOptionsSection() {
        try {
            super.initOptionsSection();

            Tooltip tips = new Tooltip(getMessage("ColorMatchComments2"));
            tips.setFont(new Font(16));
            FxmlControl.setComments(colorBox, tips);

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
            FxmlControl.quickTooltip(distanceInput, new Tooltip("0 ~ 255"));
        } else {
            FxmlControl.quickTooltip(distanceInput, new Tooltip("0 ~ 360"));
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
        ImageScope scope = new ImageScope();
        if (isColor) {
            scope.setColorScopeType(ImageScope.ColorScopeType.Color);
            scope.setColorDistance(distance);
        } else {
            scope.setColorScopeType(ImageScope.ColorScopeType.Hue);
            scope.setHsbDistance(distance / 360.0f);
        }
        scope.setColorExcluded(excludeCheck.isSelected());
        PixelsOperation pixelsOperation = PixelsOperation.newPixelsOperation(source, scope,
                PixelsOperation.OperationType.ReplaceColor);
        pixelsOperation.setColorPara1(ImageColor.converColor(oldColorPicker.getValue()));
        pixelsOperation.setColorPara2(ImageColor.converColor(newColorPicker.getValue()));
        BufferedImage target = pixelsOperation.operate();

//        BufferedImage target = ImageReplaceColorTools.replaceColor(source,
//                FxmlManufactureTools.colorConvert(oldColorPicker.getValue()),
//                FxmlManufactureTools.colorConvert(newColorPicker.getValue()),
//                distance, isColor, excludeCheck.isSelected());
        return target;
    }

}
