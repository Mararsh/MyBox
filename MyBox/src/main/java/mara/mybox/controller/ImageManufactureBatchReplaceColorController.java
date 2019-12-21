package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import mara.mybox.fxml.FxmlColor;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.image.ImageColor;
import mara.mybox.image.ImageScope;
import mara.mybox.image.PixelsOperation;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2018-9-24
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureBatchReplaceColorController extends ImageManufactureBatchController {

    private int distance;
    private boolean isColor;
    private java.awt.Color originalColor, newColor;

    @FXML
    private TextField distanceInput;
    @FXML
    private RadioButton colorRadio, hueRadio;
    @FXML
    private ToggleGroup replaceScopeGroup;
    @FXML
    private CheckBox excludeCheck;
    @FXML
    protected Rectangle originalRect, newRect;
    @FXML
    protected Button originalPaletteButton, newPaletteButton;

    public ImageManufactureBatchReplaceColorController() {
        baseTitle = AppVariables.message("ImageManufactureBatchReplaceColor");

    }

    @Override
    public void initializeNext() {
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

            try {
                String c = AppVariables.getUserConfigValue("ImageColorOriginal", Color.WHITE.toString());
                originalRect.setFill(Color.web(c));
            } catch (Exception e) {
                originalRect.setFill(Color.WHITE);
                AppVariables.setUserConfigValue("ImageColorOriginal", Color.WHITE.toString());
            }
            FxmlControl.setTooltip(originalRect, FxmlColor.colorNameDisplay((Color) originalRect.getFill()));

            try {
                String c = AppVariables.getUserConfigValue("ImageColorNew", Color.TRANSPARENT.toString());
                newRect.setFill(Color.web(c));
            } catch (Exception e) {
                newRect.setFill(Color.TRANSPARENT);
                AppVariables.setUserConfigValue("ImageColorNew", Color.TRANSPARENT.toString());
            }
            FxmlControl.setTooltip(newRect, FxmlColor.colorNameDisplay((Color) newRect.getFill()));

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
        isColor = message("Color").equals(selected.getText());
        if (isColor) {
            FxmlControl.setTooltip(distanceInput, new Tooltip("0 ~ 255"));
        } else {
            FxmlControl.setTooltip(distanceInput, new Tooltip("0 ~ 360"));
        }
        checkDistance();
    }

    private void checkDistance() {
        try {
            int v = Integer.valueOf(distanceInput.getText());

            if (v == 0
                    && ((Color) originalRect.getFill()).equals(((Color) newRect.getFill()))) {
                popError(message("OriginalNewSameColor"));
                distanceInput.setStyle(badStyle);
                return;
            }
            int max = 255;
            if (!isColor) {
                max = 360;
            }
            if (distance >= 0 && distance <= max) {
                distance = v;
                distanceInput.setStyle(null);
            } else {
                distanceInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            distanceInput.setStyle(badStyle);
        }
    }

    @Override
    public boolean setColor(Control control, Color color) {
        if (control == null || color == null) {
            return false;
        }
        if (originalPaletteButton.equals(control)) {
            originalRect.setFill(color);
            FxmlControl.setTooltip(originalRect, FxmlColor.colorNameDisplay(color));
            AppVariables.setUserConfigValue("ImageColorOriginal", color.toString());

        } else if (newPaletteButton.equals(control)) {
            newRect.setFill(color);
            FxmlControl.setTooltip(newRect, FxmlColor.colorNameDisplay(color));
            AppVariables.setUserConfigValue("ImageColorNew", color.toString());
        }
        return true;
    }

    @FXML
    public void originalPalette(ActionEvent event) {
        showPalette(originalPaletteButton, message("OriginalColor"));
    }

    @FXML
    public void newPalette(ActionEvent event) {
        showPalette(newPaletteButton, message("NewColor"));
    }

    @Override
    public boolean makeBatchParameters() {
        originalColor = ImageColor.converColor((Color) originalRect.getFill());
        newColor = ImageColor.converColor((Color) newRect.getFill());
        return super.makeBatchParameters();
    }

    @Override
    protected BufferedImage handleImage(BufferedImage source) {

        ImageScope scope = new ImageScope();
        scope.setScopeType(ImageScope.ScopeType.Color);
        scope.setColorScopeType(ImageScope.ColorScopeType.Color);
        List<java.awt.Color> colors = new ArrayList();
        colors.add(originalColor);
        scope.setColors(colors);
        if (isColor) {
            scope.setColorScopeType(ImageScope.ColorScopeType.Color);
            scope.setColorDistance(distance);
        } else {
            scope.setColorScopeType(ImageScope.ColorScopeType.Hue);
            scope.setHsbDistance(distance / 360.0f);
        }
        scope.setColorExcluded(excludeCheck.isSelected());
        PixelsOperation pixelsOperation = PixelsOperation.create(source, scope,
                PixelsOperation.OperationType.ReplaceColor, PixelsOperation.ColorActionType.Set);
        pixelsOperation.setColorPara1(originalColor);
        pixelsOperation.setColorPara2(newColor);
        BufferedImage target = pixelsOperation.operate();

        return target;
    }

}
