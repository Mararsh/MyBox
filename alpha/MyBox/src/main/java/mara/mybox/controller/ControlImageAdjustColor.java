package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import mara.mybox.image.data.PixelsOperation;
import mara.mybox.image.data.PixelsOperation.ColorActionType;
import mara.mybox.image.data.PixelsOperation.OperationType;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ValidationTools;

/**
 * @Author Mara
 * @CreateDate 2019-9-1
 * @License Apache License Version 2.0
 */
public class ControlImageAdjustColor extends BaseController {

    protected OperationType colorOperationType;
    protected ColorActionType colorActionType;
    protected int colorValue, max, min;

    @FXML
    protected ToggleGroup colorGroup, opGroup;
    @FXML
    protected RadioButton colorBrightnessRadio, colorHueRadio, colorSaturationRadio,
            colorRedRadio, colorGreenRadio, colorBlueRadio, colorOpacityRadio,
            colorYellowRadio, colorCyanRadio, colorMagentaRadio, colorRGBRadio,
            setRadio, plusRadio, minusRadio, filterRadio, invertRadio;
    @FXML
    protected ComboBox<String> valueSelector;
    @FXML
    protected Label colorUnit;
    @FXML
    protected FlowPane opPane, valuePane;

    @Override
    public void initControls() {
        try {
            super.initControls();

            colorGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkColorType();
                }
            });

            opGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkOpType();
                }
            });

            valueSelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkValue();
                }
            });

            checkColorType();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    private void checkColorType() {
        try {
            if (isSettingValues) {
                return;
            }
            valueSelector.getItems().clear();
            ValidationTools.setEditorNormal(valueSelector);
            opPane.getChildren().clear();

            if (colorRGBRadio.isSelected()) {
                colorOperationType = PixelsOperation.OperationType.RGB;
                makeValues(0, 255);
                opPane.getChildren().setAll(plusRadio, minusRadio, invertRadio);

            } else if (colorBrightnessRadio.isSelected()) {
                colorOperationType = PixelsOperation.OperationType.Brightness;
                makeValues(0, 100);
                opPane.getChildren().setAll(setRadio, plusRadio, minusRadio);

            } else if (colorSaturationRadio.isSelected()) {
                colorOperationType = PixelsOperation.OperationType.Saturation;
                makeValues(0, 100);
                opPane.getChildren().setAll(setRadio, plusRadio, minusRadio);
                plusRadio.setSelected(true);

            } else if (colorHueRadio.isSelected()) {
                colorOperationType = PixelsOperation.OperationType.Hue;
                makeValues(0, 360);
                opPane.getChildren().setAll(setRadio, plusRadio, minusRadio);

            } else if (colorRedRadio.isSelected()) {
                colorOperationType = PixelsOperation.OperationType.Red;
                opPane.getChildren().setAll(setRadio, plusRadio, minusRadio, filterRadio, invertRadio);
                makeValues(0, 255);

            } else if (colorGreenRadio.isSelected()) {
                colorOperationType = PixelsOperation.OperationType.Green;
                opPane.getChildren().setAll(setRadio, plusRadio, minusRadio, filterRadio, invertRadio);
                makeValues(0, 255);

            } else if (colorBlueRadio.isSelected()) {
                colorOperationType = PixelsOperation.OperationType.Blue;
                opPane.getChildren().setAll(setRadio, plusRadio, minusRadio, filterRadio, invertRadio);
                makeValues(0, 255);

            } else if (colorYellowRadio.isSelected()) {
                colorOperationType = PixelsOperation.OperationType.Yellow;
                opPane.getChildren().setAll(setRadio, plusRadio, minusRadio, filterRadio, invertRadio);
                makeValues(0, 255);

            } else if (colorCyanRadio.isSelected()) {
                colorOperationType = PixelsOperation.OperationType.Cyan;
                opPane.getChildren().setAll(setRadio, plusRadio, minusRadio, filterRadio, invertRadio);
                makeValues(0, 255);

            } else if (colorMagentaRadio.isSelected()) {
                colorOperationType = PixelsOperation.OperationType.Magenta;
                opPane.getChildren().setAll(setRadio, plusRadio, minusRadio, filterRadio, invertRadio);
                makeValues(0, 255);

            } else if (colorOpacityRadio.isSelected()) {
                colorOperationType = PixelsOperation.OperationType.Opacity;
                opPane.getChildren().setAll(setRadio, plusRadio, minusRadio);
                makeValues(0, 255);

            }

            refreshStyle(opPane);

            isSettingValues = true;
            plusRadio.setSelected(true);
            isSettingValues = false;

            checkOpType();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    private void makeValues(int minV, int maxV) {
        try {
            min = minV;
            max = maxV;

            colorUnit.setText(min + "-" + max);

            List<String> valueList = new ArrayList<>();
            int step = (max - min) / 10;
            for (int v = min; v < max; v += step) {
                valueList.add(v + "");
            }
            valueList.add(max + "");
            isSettingValues = true;
            valueSelector.getItems().addAll(valueList);
            valueSelector.getSelectionModel().select(valueList.size() / 2);
            isSettingValues = false;

            checkValue();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    private void checkOpType() {
        try {
            if (isSettingValues) {
                return;
            }
            if (setRadio.isSelected()) {
                colorActionType = ColorActionType.Set;
            } else if (plusRadio.isSelected()) {
                colorActionType = ColorActionType.Increase;
            } else if (minusRadio.isSelected()) {
                colorActionType = ColorActionType.Decrease;
            } else if (filterRadio.isSelected()) {
                colorActionType = ColorActionType.Filter;
            } else if (invertRadio.isSelected()) {
                colorActionType = ColorActionType.Invert;
            }

            valuePane.setVisible(needValue());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    private boolean checkValue() {
        if (isSettingValues || !needValue()) {
            return true;
        }
        int v = max + 1;
        try {
            v = Integer.parseInt(valueSelector.getValue());
        } catch (Exception e) {
        }
        if (v >= min && v <= max) {
            colorValue = v;
            ValidationTools.setEditorNormal(valueSelector);
            return true;
        } else {
            ValidationTools.setEditorBadStyle(valueSelector);
            return false;
        }
    }

    public boolean needValue() {
        return setRadio.isSelected()
                || plusRadio.isSelected()
                || minusRadio.isSelected();
    }

    public OperationType getColorOperationType() {
        return colorOperationType;
    }

    public ColorActionType getColorActionType() {
        return colorActionType;
    }

    public int getColorValue() {
        return colorValue;
    }

}
