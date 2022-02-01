package mara.mybox.controller;

import java.awt.image.BufferedImage;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import mara.mybox.bufferedimage.ColorConvertTools;
import mara.mybox.bufferedimage.PixelsOperation;
import mara.mybox.bufferedimage.PixelsOperation.ColorActionType;
import mara.mybox.bufferedimage.PixelsOperation.OperationType;
import mara.mybox.bufferedimage.PixelsOperationFactory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-9-22
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureBatchColorController extends BaseImageManufactureBatchController {

    private int colorValue;
    private OperationType colorOperationType;
    private ColorActionType colorActionType;

    @FXML
    protected ToggleGroup colorGroup, opGroup;
    @FXML
    protected RadioButton colorColorRadio, colorRGBRadio,
            colorBrightnessRadio, colorHueRadio, colorSaturationRadio,
            colorRedRadio, colorGreenRadio, colorBlueRadio, colorOpacityRadio,
            colorYellowRadio, colorCyanRadio, colorMagentaRadio;
    @FXML
    protected RadioButton setRadio, invertRadio, increaseRadio, decreaseRadio, filterRadio;
    @FXML
    protected Slider colorSlider;
    @FXML
    protected TextField colorInput;
    @FXML
    protected Label colorUnit;
    @FXML
    protected CheckBox ignoreTransparentCheck;
    @FXML
    protected ColorSet colorSetController;
    @FXML
    protected HBox colorBox;

    public ImageManufactureBatchColorController() {
        baseTitle = Languages.message("ImageManufactureBatchColor");

    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(targetPathController.valid.not()
                    .or(Bindings.isEmpty(tableView.getItems()))
                    .or(colorInput.styleProperty().isEqualTo(UserConfig.badStyle()))
            );

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void initOptionsSection() {
        try {
            super.initOptionsSection();

            colorGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                    checkOperationType();
                }
            });
            checkOperationType();

            colorSlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    colorValue = newValue.intValue();
                    colorInput.setText(colorValue + "");
                }
            });

            colorInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkColorInput();
                }
            });
            checkColorInput();

            colorSetController.init(this, baseName + "ValueColor", Color.RED);

            opGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                    checkColorActionType();
                }
            });
            checkColorActionType();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    private void checkOperationType() {
        setRadio.setDisable(false);
        invertRadio.setDisable(false);
        filterRadio.setDisable(false);
        increaseRadio.setDisable(false);
        decreaseRadio.setDisable(false);
        setRadio.setSelected(true);
        colorUnit.setText("0-255");
        colorBox.setVisible(false);

        if (colorBrightnessRadio.isSelected()) {
            colorOperationType = OperationType.Brightness;
            colorSlider.setMax(100);
            colorSlider.setMin(0);
            colorSlider.setBlockIncrement(1);
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
            filterRadio.setDisable(true);
            invertRadio.setDisable(true);
            colorUnit.setText("0-100");
        } else if (colorSaturationRadio.isSelected()) {
            colorOperationType = OperationType.Saturation;
            colorSlider.setMax(100);
            colorSlider.setMin(0);
            colorSlider.setBlockIncrement(1);
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
            filterRadio.setDisable(true);
            invertRadio.setDisable(true);
            colorUnit.setText("0-100");
        } else if (colorHueRadio.isSelected()) {
            colorOperationType = OperationType.Hue;
            colorSlider.setMax(360);
            colorSlider.setMin(0);
            colorSlider.setBlockIncrement(1);
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
            filterRadio.setDisable(true);
            invertRadio.setDisable(true);
            colorUnit.setText("0-360");
        } else if (colorRedRadio.isSelected()) {
            colorOperationType = OperationType.Red;
            colorSlider.setMax(255);
            colorSlider.setMin(0);
            colorSlider.setBlockIncrement(1);
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
        } else if (colorGreenRadio.isSelected()) {
            colorOperationType = OperationType.Green;
            colorSlider.setMax(255);
            colorSlider.setMin(0);
            colorSlider.setBlockIncrement(1);
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
        } else if (colorBlueRadio.isSelected()) {
            colorOperationType = OperationType.Blue;
            colorSlider.setMax(255);
            colorSlider.setMin(0);
            colorSlider.setBlockIncrement(1);
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
        } else if (colorYellowRadio.isSelected()) {
            colorOperationType = OperationType.Yellow;
            colorSlider.setMax(255);
            colorSlider.setMin(0);
            colorSlider.setBlockIncrement(1);
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
        } else if (colorCyanRadio.isSelected()) {
            colorOperationType = OperationType.Cyan;
            colorSlider.setMax(255);
            colorSlider.setMin(0);
            colorSlider.setBlockIncrement(1);
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
        } else if (colorMagentaRadio.isSelected()) {
            colorOperationType = OperationType.Magenta;
            colorSlider.setMax(255);
            colorSlider.setMin(0);
            colorSlider.setBlockIncrement(1);
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
        } else if (colorOpacityRadio.isSelected()) {
            colorOperationType = OperationType.Opacity;
            colorSlider.setMax(255);
            colorSlider.setMin(0);
            colorSlider.setBlockIncrement(1);
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
            invertRadio.setDisable(true);
            filterRadio.setDisable(true);
        } else if (colorRGBRadio.isSelected()) {
            colorOperationType = OperationType.RGB;
            colorSlider.setMax(255);
            colorSlider.setMin(0);
            colorSlider.setBlockIncrement(1);
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("10");
            }
            setRadio.setDisable(true);
            filterRadio.setDisable(true);
            invertRadio.setSelected(true);
        } else if (colorColorRadio.isSelected()) {
            colorOperationType = OperationType.Color;
            colorSlider.setMax(100);
            colorSlider.setMin(0);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("0-100");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
            setRadio.setDisable(true);
            invertRadio.setDisable(true);
            increaseRadio.setDisable(true);
            decreaseRadio.setDisable(true);
            filterRadio.setSelected(true);
            colorBox.setVisible(true);
        }

    }

    private void checkColorInput() {
        try {
            colorValue = Integer.valueOf(colorInput.getText());
            if (colorValue >= 0 && colorValue <= colorSlider.getMax()) {
                colorInput.setStyle(null);
                colorSlider.setValue(colorValue);
            } else {
                colorInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            colorInput.setStyle(UserConfig.badStyle());
        }
    }

    private void checkColorActionType() {
        if (setRadio.isSelected()) {
            colorActionType = ColorActionType.Set;
        } else if (increaseRadio.isSelected()) {
            colorActionType = ColorActionType.Increase;
        } else if (decreaseRadio.isSelected()) {
            colorActionType = ColorActionType.Decrease;
        } else if (filterRadio.isSelected()) {
            colorActionType = ColorActionType.Filter;
        } else if (invertRadio.isSelected()) {
            colorActionType = ColorActionType.Invert;
        } else {
            colorActionType = null;
        }
    }

    @Override
    protected BufferedImage handleImage(BufferedImage source) {
        if (null == colorOperationType || colorActionType == null) {
            return null;
        }
        try {
            PixelsOperation pixelsOperation = PixelsOperationFactory.create(source, null,
                    colorOperationType, colorActionType);
            pixelsOperation.setSkipTransparent(ignoreTransparentCheck.isSelected());
            switch (colorOperationType) {
                case Hue:
                    pixelsOperation.setFloatPara1(colorValue / 360.0f);
                    break;
                case Brightness:
                case Saturation:
                    pixelsOperation.setFloatPara1(colorValue / 100.0f);
                    break;
                case Color:
                    pixelsOperation.setFloatPara1(colorValue / 100.0f);
                    pixelsOperation.setColorPara1(ColorConvertTools.converColor((Color) colorSetController.rect.getFill()));
                    break;
                case Red:
                case Green:
                case Blue:
                case Yellow:
                case Cyan:
                case Magenta:
                case RGB:
                case Opacity:
                    pixelsOperation.setIntPara1(colorValue);
                    break;
            }
            BufferedImage target = pixelsOperation.operate();
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }

    }

}
