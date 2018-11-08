package mara.mybox.controller;

import java.awt.image.BufferedImage;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.controller.ImageManufactureColorController.ColorOperationType;
import mara.mybox.image.ImageConvertTools;
import static mara.mybox.objects.AppVaribles.getMessage;
import static mara.mybox.tools.FxmlTools.badStyle;

/**
 * @Author Mara
 * @CreateDate 2018-9-22
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureBatchColorController extends ImageManufactureBatchController {

    private int colorValue;
    private boolean isIncrease;
    private ColorOperationType colorOperationType;

    @FXML
    private ToggleGroup colorGroup, opGroup;
    @FXML
    private RadioButton redRadio, opacityRadio, increaseRadio, decreaseRadio;
    @FXML
    private Slider colorSlider;
    @FXML
    private TextField colorInput;
    @FXML
    private Label colorUnit;

    public ImageManufactureBatchColorController() {

    }

    @Override
    protected void initializeNext2() {
        try {
            super.initOptionsSection();

            operationBarController.startButton.disableProperty().bind(Bindings.isEmpty(targetPathInput.textProperty())
                    .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                    .or(Bindings.isEmpty(sourceFilesInformation))
                    .or(colorInput.styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @Override
    protected void initOptionsSection() {
        try {

            colorGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkColorOperationType();
                }
            });
            checkColorOperationType();

            colorSlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    colorValue = newValue.intValue();
                    colorInput.setText(colorValue + "");
                }
            });

            colorInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkColorInput();
                }
            });
            checkColorInput();

            opGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkColorOperationType2();
                }
            });
            checkColorOperationType2();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkColorOperationType() {
        increaseRadio.setDisable(false);
        decreaseRadio.setDisable(false);
        RadioButton selected = (RadioButton) colorGroup.getSelectedToggle();
        if (getMessage("Brightness").equals(selected.getText())) {
            colorOperationType = ColorOperationType.Brightness;
            colorSlider.setMax(100);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("%");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("10");
            }

        } else if (getMessage("Saturation").equals(selected.getText())) {
            colorOperationType = ColorOperationType.Sauration;
            colorSlider.setMax(100);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("%");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("10");
            }

        } else if (getMessage("Hue").equals(selected.getText())) {
            colorOperationType = ColorOperationType.Hue;
            colorSlider.setMax(359);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText(getMessage("Degree"));
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("10");
            }

        } else if (getMessage("Opacity").equals(selected.getText())) {
            colorOperationType = ColorOperationType.Opacity;
            colorSlider.setMax(100);
            colorSlider.setMin(0);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("%");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
            increaseRadio.setDisable(true);
            decreaseRadio.setDisable(true);

        } else if (getMessage("Red").equals(selected.getText())) {
            colorOperationType = ColorOperationType.Red;
            colorSlider.setMax(255);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("10");
            }

        } else if (getMessage("Green").equals(selected.getText())) {
            colorOperationType = ColorOperationType.Green;
            colorSlider.setMax(255);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("10");
            }

        } else if (getMessage("Blue").equals(selected.getText())) {
            colorOperationType = ColorOperationType.Blue;
            colorSlider.setMax(255);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("10");
            }

        } else if (getMessage("Yellow").equals(selected.getText())) {
            colorOperationType = ColorOperationType.Yellow;
            colorSlider.setMax(255);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("10");
            }

        } else if (getMessage("Cyan").equals(selected.getText())) {
            colorOperationType = ColorOperationType.Cyan;
            colorSlider.setMax(255);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("10");
            }

        } else if (getMessage("Magenta").equals(selected.getText())) {
            colorOperationType = ColorOperationType.Magenta;
            colorSlider.setMax(255);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("10");
            }

        } else if (getMessage("RGB").equals(selected.getText())) {
            colorOperationType = ColorOperationType.RGB;
            colorSlider.setMax(255);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("10");
            }

        }
    }

    private void checkColorInput() {
        try {
            colorValue = Integer.valueOf(colorInput.getText());
            if (colorValue >= 0 && colorValue <= colorSlider.getMax()) {
                colorInput.setStyle(null);
                colorSlider.setValue(colorValue);
            } else {
                colorInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            colorInput.setStyle(badStyle);
        }
    }

    private void checkColorOperationType2() {
        RadioButton selected = (RadioButton) opGroup.getSelectedToggle();
        isIncrease = getMessage("Increase").equals(selected.getText());
    }

//    @Override
//    protected String handleCurrentFile() {
//        if (colorOperationType == ImageManufactureController.ColorOperationType.Opacity) {
//            if (CommonValues.NoAlphaImages.contains(targetFormat)) {
//                return AppVaribles.getMessage("NotSupported");
//            }
//        }
//        return super.handleCurrentFile();
//    }
    @Override
    protected BufferedImage handleImage(BufferedImage source) {
        try {
            int value = colorValue;
            if (!isIncrease) {
                value = 0 - colorValue;
            }
            BufferedImage target = null;
            if (colorOperationType == ColorOperationType.Brightness) {
                target = ImageConvertTools.changeBrightness(source, value / 100.0f);

            } else if (colorOperationType == ColorOperationType.Sauration) {
                target = ImageConvertTools.changeSaturate(source, value / 100.0f);

            } else if (colorOperationType == ColorOperationType.Hue) {
                target = ImageConvertTools.changeHue(source, value / 360f);

            } else if (colorOperationType == ColorOperationType.Opacity) {
                int v = (int) ((colorValue * 255 / 100.0f));
                target = ImageConvertTools.addAlpha(source, v);

            } else if (colorOperationType == ColorOperationType.Red) {
                target = ImageConvertTools.changeRed(source, value);

            } else if (colorOperationType == ColorOperationType.Green) {
                target = ImageConvertTools.changeGreen(source, value);

            } else if (colorOperationType == ColorOperationType.Blue) {
                target = ImageConvertTools.changeBlue(source, value);

            } else if (colorOperationType == ColorOperationType.Yellow) {
                target = ImageConvertTools.changeYellow(source, value);

            } else if (colorOperationType == ColorOperationType.Cyan) {
                target = ImageConvertTools.changeCyan(source, value);

            } else if (colorOperationType == ColorOperationType.Magenta) {
                target = ImageConvertTools.changeMagenta(source, value);

            } else if (colorOperationType == ColorOperationType.RGB) {
                target = ImageConvertTools.changeRGB(source, value);

            }

            return target;

        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }

    }

}
