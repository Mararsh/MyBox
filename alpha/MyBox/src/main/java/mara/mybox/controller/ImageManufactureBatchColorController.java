package mara.mybox.controller;

import java.awt.image.BufferedImage;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import mara.mybox.bufferedimage.PixelsOperation;
import mara.mybox.bufferedimage.PixelsOperation.ColorActionType;
import mara.mybox.bufferedimage.PixelsOperation.OperationType;
import mara.mybox.bufferedimage.PixelsOperationFactory;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-9-22
 * @License Apache License Version 2.0
 */
public class ImageManufactureBatchColorController extends BaseImageManufactureBatchController {

    private int colorValue, valueMax;
    private OperationType colorOperationType;
    private ColorActionType colorActionType;

    @FXML
    protected ToggleGroup colorGroup, opGroup;
    @FXML
    protected RadioButton colorColorRadio, colorBlendRadio, colorRGBRadio,
            colorBrightnessRadio, colorHueRadio, colorSaturationRadio,
            colorRedRadio, colorGreenRadio, colorBlueRadio, colorOpacityRadio,
            colorYellowRadio, colorCyanRadio, colorMagentaRadio;
    @FXML
    protected RadioButton colorSetRadio, colorInvertRadio, colorIncreaseRadio, colorDecreaseRadio, colorFilterRadio;
    @FXML
    protected TextField colorInput;
    @FXML
    protected Label colorUnit;
    @FXML
    protected CheckBox ignoreTransparentCheck, hueCheck, saturationCheck, brightnessCheck;
    @FXML
    protected ControlColorSet colorSetController;
    @FXML
    protected HBox colorBox;
    @FXML
    protected VBox setBox, valueBox, blendBox;
    @FXML
    protected FlowPane replacePane;
    @FXML
    protected ControlImagesBlend blendController;

    public ImageManufactureBatchColorController() {
        baseTitle = message("ImageManufactureBatchColor");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(targetPathController.valid.not()
                    .or(Bindings.isEmpty(tableView.getItems()))
            );

        } catch (Exception e) {
            MyBoxLog.debug(e);
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

            colorInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkColorInput();
                }
            });
            checkColorInput();

            colorSetController.init(this, baseName + "ValueColor", Color.RED);

            hueCheck.setSelected(UserConfig.getBoolean(baseName + "ReplaceHue", false));
            hueCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldv, Boolean newv) {
                    UserConfig.setBoolean(baseName + "ReplaceHue", hueCheck.isSelected());
                }
            });

            saturationCheck.setSelected(UserConfig.getBoolean(baseName + "ReplaceSaturation", false));
            saturationCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldv, Boolean newv) {
                    UserConfig.setBoolean(baseName + "ReplaceSaturation", saturationCheck.isSelected());
                }
            });

            brightnessCheck.setSelected(UserConfig.getBoolean(baseName + "ReplaceBrightness", false));
            brightnessCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldv, Boolean newv) {
                    UserConfig.setBoolean(baseName + "ReplaceBrightness", brightnessCheck.isSelected());
                }
            });

            blendController.setParameters(this, null);

            opGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                    checkColorActionType();
                }
            });
            checkColorActionType();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    private void checkOperationType() {
        setBox.getChildren().clear();

        if (colorColorRadio.isSelected()) {
            colorOperationType = OperationType.Color;
            setBox.getChildren().addAll(colorBox, replacePane);
            ignoreTransparentCheck.setVisible(true);

        } else if (colorBlendRadio.isSelected()) {
            colorOperationType = OperationType.Blend;
            setBox.getChildren().addAll(colorBox, blendBox);
            ignoreTransparentCheck.setVisible(false);

        } else {
            setBox.getChildren().addAll(valueBox);
            colorSetRadio.setDisable(false);
            colorInvertRadio.setDisable(false);
            colorFilterRadio.setDisable(false);
            colorIncreaseRadio.setDisable(false);
            colorDecreaseRadio.setDisable(false);
            colorSetRadio.setSelected(true);
            valueMax = 255;
            ignoreTransparentCheck.setVisible(true);

            if (colorRGBRadio.isSelected()) {
                colorOperationType = OperationType.RGB;
                if (colorInput.getText().trim().isEmpty()) {
                    colorInput.setText("10");
                }
                colorSetRadio.setDisable(true);
                colorFilterRadio.setDisable(true);
                colorInvertRadio.setSelected(true);

            } else if (colorBrightnessRadio.isSelected()) {
                colorOperationType = OperationType.Brightness;
                if (colorInput.getText().trim().isEmpty()) {
                    colorInput.setText("50");
                }
                colorFilterRadio.setDisable(true);
                colorInvertRadio.setDisable(true);
                valueMax = 100;

            } else if (colorSaturationRadio.isSelected()) {
                colorOperationType = OperationType.Saturation;
                if (colorInput.getText().trim().isEmpty()) {
                    colorInput.setText("50");
                }
                colorFilterRadio.setDisable(true);
                colorInvertRadio.setDisable(true);
                valueMax = 100;

            } else if (colorHueRadio.isSelected()) {
                colorOperationType = OperationType.Hue;
                valueMax = 360;
                if (colorInput.getText().trim().isEmpty()) {
                    colorInput.setText("50");
                }
                colorFilterRadio.setDisable(true);
                colorInvertRadio.setDisable(true);
                valueMax = 360;

            } else if (colorRedRadio.isSelected()) {
                colorOperationType = OperationType.Red;
                valueMax = 255;
                if (colorInput.getText().trim().isEmpty()) {
                    colorInput.setText("50");
                }

            } else if (colorGreenRadio.isSelected()) {
                colorOperationType = OperationType.Green;
                valueMax = 255;
                if (colorInput.getText().trim().isEmpty()) {
                    colorInput.setText("50");
                }

            } else if (colorBlueRadio.isSelected()) {
                colorOperationType = OperationType.Blue;
                valueMax = 255;
                if (colorInput.getText().trim().isEmpty()) {
                    colorInput.setText("50");
                }

            } else if (colorYellowRadio.isSelected()) {
                colorOperationType = OperationType.Yellow;
                valueMax = 255;
                if (colorInput.getText().trim().isEmpty()) {
                    colorInput.setText("50");
                }

            } else if (colorCyanRadio.isSelected()) {
                colorOperationType = OperationType.Cyan;
                valueMax = 255;
                if (colorInput.getText().trim().isEmpty()) {
                    colorInput.setText("50");
                }

            } else if (colorMagentaRadio.isSelected()) {
                colorOperationType = OperationType.Magenta;
                valueMax = 255;
                if (colorInput.getText().trim().isEmpty()) {
                    colorInput.setText("50");
                }

            } else if (colorOpacityRadio.isSelected()) {
                colorOperationType = OperationType.Opacity;
                valueMax = 255;
                if (colorInput.getText().trim().isEmpty()) {
                    colorInput.setText("50");
                }
                colorInvertRadio.setDisable(true);
                colorFilterRadio.setDisable(true);

            }

            colorUnit.setText("0-" + valueMax);

        }

        refreshStyle(setBox);

    }

    private void checkColorInput() {
        try {
            colorValue = Integer.parseInt(colorInput.getText());
            if (colorValue >= 0 && colorValue <= valueMax) {
                colorInput.setStyle(null);
            } else {
                colorInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            colorInput.setStyle(UserConfig.badStyle());
        }
    }

    private void checkColorActionType() {
        if (colorSetRadio.isSelected()) {
            colorActionType = ColorActionType.Set;
        } else if (colorIncreaseRadio.isSelected()) {
            colorActionType = ColorActionType.Increase;
        } else if (colorDecreaseRadio.isSelected()) {
            colorActionType = ColorActionType.Decrease;
        } else if (colorFilterRadio.isSelected()) {
            colorActionType = ColorActionType.Filter;
        } else if (colorInvertRadio.isSelected()) {
            colorActionType = ColorActionType.Invert;
        } else {
            colorActionType = ColorActionType.Set;
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
                case Color:
                    pixelsOperation.setColorPara1(colorSetController.awtColor())
                            .setBoolPara1(hueCheck.isSelected())
                            .setBoolPara2(saturationCheck.isSelected())
                            .setBoolPara3(brightnessCheck.isSelected());
                    break;
                case Blend:
                    pixelsOperation.setColorPara1(colorSetController.awtColor());
                    ((PixelsOperationFactory.BlendColor) pixelsOperation).setBlender(blendController.blender());
                    break;
                case Hue:
                    pixelsOperation.setFloatPara1(colorValue / 360.0f);
                    break;
                case Brightness:
                case Saturation:
                    pixelsOperation.setFloatPara1(colorValue / 100.0f);
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
            MyBoxLog.error(e);
            return null;
        }

    }

}
