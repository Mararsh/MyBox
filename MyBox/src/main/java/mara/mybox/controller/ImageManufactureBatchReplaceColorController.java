package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.image.ImageColor;
import mara.mybox.image.ImageScope;
import mara.mybox.image.PixelsOperation;
import mara.mybox.value.AppVariables;
import mara.mybox.dev.MyBoxLog;
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
    private ToggleGroup replaceScopeGroup;
    @FXML
    private CheckBox excludeCheck, ignoreTransparentCheck;
    @FXML
    protected ColorSetController originalColorSetController, newColorSetController;

    public ImageManufactureBatchReplaceColorController() {
        baseTitle = AppVariables.message("ImageManufactureBatchReplaceColor");

    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(Bindings.isEmpty(targetPathInput.textProperty())
                    .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                    .or(Bindings.isEmpty(tableView.getItems()))
                    .or(distanceInput.styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void initOptionsSection() {
        try {
            super.initOptionsSection();

            originalColorSetController.init(this, baseName + "OriginalColor", Color.WHITE);
            newColorSetController.init(this, baseName + "NewColor", Color.TRANSPARENT);

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
            MyBoxLog.error(e.toString());
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
                    && ((Color) originalColorSetController.rect.getFill()).equals(((Color) newColorSetController.rect.getFill()))) {
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
    public boolean makeMoreParameters() {
        originalColor = ImageColor.converColor((Color) originalColorSetController.rect.getFill());
        newColor = ImageColor.converColor((Color) newColorSetController.rect.getFill());
        return super.makeMoreParameters();
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
        pixelsOperation.setSkipTransparent(ignoreTransparentCheck.isSelected());
        BufferedImage target = pixelsOperation.operate();

        return target;
    }

}
