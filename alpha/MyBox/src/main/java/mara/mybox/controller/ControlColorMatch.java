package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import mara.mybox.image.data.ImageScope;
import static mara.mybox.image.data.ImageScope.ColorScopeType.Blue;
import static mara.mybox.image.data.ImageScope.ColorScopeType.Brightness;
import static mara.mybox.image.data.ImageScope.ColorScopeType.Color;
import static mara.mybox.image.data.ImageScope.ColorScopeType.Green;
import static mara.mybox.image.data.ImageScope.ColorScopeType.Hue;
import static mara.mybox.image.data.ImageScope.ColorScopeType.Red;
import static mara.mybox.image.data.ImageScope.ColorScopeType.Saturation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-13
 * @License Apache License Version 2.0
 */
public class ControlColorMatch extends BaseController {

    protected SimpleBooleanProperty changeNotify;
    protected int distance, max;

    @FXML
    protected ToggleGroup matchGroup;
    @FXML
    protected RadioButton colorRGBRadio, colorGreenRadio, colorRedRadio, colorBlueRadio,
            colorSaturationRadio, colorHueRadio, colorBrightnessRadio;
    @FXML
    protected ComboBox<String> distanceSelector;
    @FXML
    protected CheckBox squareRootCheck;

    public ControlColorMatch() {
        TipsLabelKey = "ColorMatchComments";
    }

    public void changeNotify() {
        changeNotify.set(!changeNotify.get());
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            changeNotify = new SimpleBooleanProperty(false);
            distance = UserConfig.getInt(baseName + "Distance", 20);
            if (distance <= 0) {
                distance = 20;
            }
            max = 100;

            matchGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    setDistanceValue();
                    changeNotify();
                }
            });

            int colorDistance = UserConfig.getInt(baseName + "ColorDistance", 20);
            colorDistance = colorDistance <= 0 ? 20 : colorDistance;
            distanceSelector.setValue(colorDistance + "");
            distanceSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (!isSettingValues) {
                        changeNotify();
                    }
                }
            });

            squareRootCheck.setSelected(UserConfig.getBoolean(baseName + "ColorDistanceSquare", false));
            squareRootCheck.visibleProperty().bind(colorRGBRadio.selectedProperty());
            squareRootCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues || !colorRGBRadio.isSelected()) {
                        return;
                    }
                    changeNotify();
                }
            });

            setDistanceValue();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void setDistanceValue() {
        try {
            if (colorSaturationRadio.isSelected() || colorHueRadio.isSelected()) {
                max = 100;
            } else if (colorHueRadio.isSelected()) {
                max = 360;
            } else {
                max = 255;
            }

            NodeStyleTools.setTooltip(distanceSelector, new Tooltip("0~" + max));
            List<String> vList = new ArrayList<>();
            for (int i = 0; i <= max; i += 10) {
                vList.add(i + "");
            }
            isSettingValues = true;
            distanceSelector.getItems().setAll(vList);
            distanceSelector.getSelectionModel().select(distance + "");
            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    protected void setDistanceValue(ImageScope scope) {
        if (scope != null) {
            if (colorSaturationRadio.isSelected() || colorHueRadio.isSelected()) {
                distance = (int) (scope.getHsbDistance() * 100);
            } else if (colorHueRadio.isSelected()) {
                distance = (int) (scope.getHsbDistance() * 360);
            } else {
                distance = scope.getColorDistance();
            }
        }
        UserConfig.setInt(baseName + "Distance", distance);
        setDistanceValue();
    }

    protected boolean isSquare() {
        return squareRootCheck.isSelected() && colorRGBRadio.isSelected();
    }

    protected boolean pickValues(ImageScope scope, int defaultDistance) {
        if (scope == null) {
            return false;
        }
        boolean valid = true;
        int v = distance;
        try {
            if (colorRGBRadio.isSelected()) {
                scope.setColorScopeType(ImageScope.ColorScopeType.Color);

            } else if (colorRedRadio.isSelected()) {
                scope.setColorScopeType(ImageScope.ColorScopeType.Red);

            } else if (colorGreenRadio.isSelected()) {
                scope.setColorScopeType(ImageScope.ColorScopeType.Green);

            } else if (colorBlueRadio.isSelected()) {
                scope.setColorScopeType(ImageScope.ColorScopeType.Blue);

            } else if (colorSaturationRadio.isSelected()) {
                scope.setColorScopeType(ImageScope.ColorScopeType.Saturation);

            } else if (colorHueRadio.isSelected()) {
                scope.setColorScopeType(ImageScope.ColorScopeType.Hue);

            } else if (colorBrightnessRadio.isSelected()) {
                scope.setColorScopeType(ImageScope.ColorScopeType.Brightness);
            }

            try {
                v = Integer.parseInt(distanceSelector.getValue());
            } catch (Exception e) {
                v = defaultDistance;
                valid = false;
            }
            switch (scope.getColorScopeType()) {
                case Hue:
                    if (v >= 0 && v <= 360) {
                        scope.setHsbDistance(v / 360.0f);
                    } else {
                        valid = false;
                    }
                    break;
                case Brightness:
                case Saturation:
                    if (v >= 0 && v <= 100) {
                        scope.setHsbDistance(v / 100.0f);
                    } else {
                        valid = false;
                    }
                    break;
                default:
                    if (isSquare()) {
                        if (v >= 0 && v <= 255 * 255) {
                            scope.setColorDistanceSquare(v);
                        } else {
                            valid = false;
                        }
                    } else {
                        if (v >= 0 && v <= 255) {
                            scope.setColorDistance(v);
                        } else {
                            valid = false;
                        }
                    }
            }
        } catch (Exception e) {
            valid = false;
        }
        if (valid) {
            distance = v;
            UserConfig.setInt(baseName + "Distance", distance);
        }
        return valid;
    }

    protected void show(ImageScope scope) {
        try {
            if (scope == null) {
                return;
            }
            isSettingValues = true;
            switch (scope.getColorScopeType()) {
                case Color:
                    colorRGBRadio.setSelected(true);
                    break;
                case Red:
                    colorRedRadio.setSelected(true);
                    break;
                case Green:
                    colorGreenRadio.setSelected(true);
                    break;
                case Blue:
                    colorBlueRadio.setSelected(true);
                    break;
                case Hue:
                    colorHueRadio.setSelected(true);
                    break;
                case Brightness:
                    colorBrightnessRadio.setSelected(true);
                    break;
                case Saturation:
                    colorSaturationRadio.setSelected(true);
                    break;
            }
            isSettingValues = false;
            setDistanceValue(scope);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

}
