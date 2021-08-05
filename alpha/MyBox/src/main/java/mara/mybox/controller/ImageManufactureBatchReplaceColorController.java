package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
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
import mara.mybox.fxml.NodeTools;
import static mara.mybox.fxml.NodeStyleTools.badStyle;
import mara.mybox.bufferedimage.ColorConvertTools;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.bufferedimage.PixelsOperation;
import mara.mybox.bufferedimage.PixelsOperationFactory;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-9-24
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureBatchReplaceColorController extends BaseImageManufactureBatchController {

    private int distance;
    private boolean isColor;
    private java.awt.Color originalColor, newColor;

    @FXML
    protected ToggleGroup replaceScopeGroup;
    @FXML
    protected ComboBox<String> distanceSelector;
    @FXML
    protected CheckBox excludeCheck, ignoreTransparentCheck, squareRootCheck;
    @FXML
    protected ColorSet originalColorSetController, newColorSetController;

    public ImageManufactureBatchReplaceColorController() {
        baseTitle = Languages.message("ImageManufactureBatchReplaceColor");

    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(Bindings.isEmpty(targetPathInput.textProperty())
                    .or(targetPathInput.styleProperty().isEqualTo(NodeStyleTools.badStyle))
                    .or(Bindings.isEmpty(tableView.getItems()))
                    .or(distanceSelector.getEditor().styleProperty().isEqualTo(NodeStyleTools.badStyle))
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

            distance = UserConfig.getUserConfigInt(baseName + "Distance", 20);
            distance = distance <= 0 ? 20 : distance;
            distanceSelector.setValue(distance + "");
            distanceSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkValues();
                }
            });

            squareRootCheck.setSelected(UserConfig.getUserConfigBoolean(baseName + "ColorDistanceSquare", false));
            squareRootCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldv, Boolean newv) {
                    checkValues();
                }
            });

            replaceScopeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                    checkValues();
                }
            });
            checkValues();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void checkValues() {
        if (isSettingValues) {
            return;
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                RadioButton selected = (RadioButton) replaceScopeGroup.getSelectedToggle();
                isColor = Languages.message("Color").equals(selected.getText());
                int max = 255, step = 10;
                if (isColor) {
                    squareRootCheck.setDisable(false);
                    if (squareRootCheck.isSelected()) {
                        max = 255 * 255;
                        step = 100;
                    }
                } else {
                    max = 360;
                    squareRootCheck.setDisable(true);
                }
                NodeStyleTools.setTooltip(distanceSelector, new Tooltip("0 ~ " + max));
                String value = distanceSelector.getValue();
                List<String> vList = new ArrayList<>();
                for (int i = 0; i <= max; i += step) {
                    vList.add(i + "");
                }
                isSettingValues = true;
                distanceSelector.getItems().clear();
                distanceSelector.getItems().addAll(vList);
                distanceSelector.setValue(value);
                isSettingValues = false;
                try {
                    int v = Integer.valueOf(value);
                    if (v == 0
                            && ((Color) originalColorSetController.rect.getFill()).equals(((Color) newColorSetController.rect.getFill()))) {
                        popError(Languages.message("OriginalNewSameColor"));
                        return;
                    }
                    if (v >= 0 && v <= max) {
                        distance = v;
                        UserConfig.setUserConfigInt(baseName + "Distance", distance);
                        distanceSelector.getEditor().setStyle(null);
                    } else {
                        distanceSelector.getEditor().setStyle(NodeStyleTools.badStyle);
                    }
                } catch (Exception e) {
                    distanceSelector.getEditor().setStyle(NodeStyleTools.badStyle);
                }
            }
        });
    }

    @Override
    public boolean makeMoreParameters() {
        originalColor = ColorConvertTools.converColor((Color) originalColorSetController.rect.getFill());
        newColor = ColorConvertTools.converColor((Color) newColorSetController.rect.getFill());
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
            if (squareRootCheck.isSelected()) {
                scope.setColorDistanceSquare(distance);
            } else {
                scope.setColorDistance(distance);
            }
        } else {
            scope.setColorScopeType(ImageScope.ColorScopeType.Hue);
            scope.setHsbDistance(distance / 360.0f);
        }
        scope.setColorExcluded(excludeCheck.isSelected());
        PixelsOperation pixelsOperation = PixelsOperationFactory.create(source, scope,
                PixelsOperation.OperationType.ReplaceColor, PixelsOperation.ColorActionType.Set);
        pixelsOperation.setColorPara1(originalColor);
        pixelsOperation.setColorPara2(newColor);
        pixelsOperation.setSkipTransparent(ignoreTransparentCheck.isSelected());
        BufferedImage target = pixelsOperation.operate();

        return target;
    }

}
