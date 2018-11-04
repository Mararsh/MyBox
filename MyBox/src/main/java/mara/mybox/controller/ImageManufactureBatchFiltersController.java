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
import javafx.scene.layout.HBox;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.controller.ImageManufactureFiltersController.FiltersOperationType;
import mara.mybox.image.ImageConvertTools;
import mara.mybox.image.ImageGrayTools;
import static mara.mybox.objects.AppVaribles.getMessage;
import static mara.mybox.tools.FxmlTools.badStyle;

/**
 * @Author Mara
 * @CreateDate 2018-9-23
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureBatchFiltersController extends ImageManufactureBatchController {

    private int intValue, filtersOperationType;

    @FXML
    private ToggleGroup filtersGroup;
    @FXML
    private Slider valueSlider;
    @FXML
    private HBox bwBox;
    @FXML
    private TextField valueInput;
    @FXML
    protected Label valueLabel, unitLabel, emptyLabel;

    public ImageManufactureBatchFiltersController() {

    }

    @Override
    protected void initializeNext2() {
        try {

            operationBarController.startButton.disableProperty().bind(Bindings.isEmpty(targetPathInput.textProperty())
                    .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                    .or(Bindings.isEmpty(sourceFilesInformation))
                    .or(valueInput.styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @Override
    protected void initOptionsSection() {
        try {

            filtersGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkFiltersType();
                }
            });
            checkFiltersType();

            valueSlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    intValue = newValue.intValue();
                    valueInput.setText(intValue + "");
                }
            });

            valueInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkValueInput();
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkFiltersType() {
        bwBox.setDisable(true);
        valueInput.setStyle(null);
        valueLabel.setText("");
        valueSlider.setDisable(true);
        valueInput.setDisable(true);
        valueInput.setStyle(null);
        unitLabel.setText("");
        emptyLabel.setText("");
        RadioButton selected = (RadioButton) filtersGroup.getSelectedToggle();
        if (getMessage("BlackOrWhite").equals(selected.getText())) {
            filtersOperationType = FiltersOperationType.BlackOrWhite;
            bwBox.setDisable(false);
            valueLabel.setText(getMessage("Threshold"));
            valueSlider.setDisable(false);
            valueSlider.setMax(99);
            valueSlider.setMin(1);
            valueSlider.setBlockIncrement(1);
            valueSlider.setValue(50);
            valueInput.setDisable(false);
            unitLabel.setText("%");
            checkValueInput();
            emptyLabel.setText(getMessage("EmptyForDefault"));
        } else if (getMessage("Gray").equals(selected.getText())) {
            filtersOperationType = FiltersOperationType.Gray;
        } else if (getMessage("Invert").equals(selected.getText())) {
            filtersOperationType = FiltersOperationType.Invert;
        } else if (getMessage("Red").equals(selected.getText())) {
            filtersOperationType = FiltersOperationType.Red;
        } else if (getMessage("Green").equals(selected.getText())) {
            filtersOperationType = FiltersOperationType.Green;
        } else if (getMessage("Blue").equals(selected.getText())) {
            filtersOperationType = FiltersOperationType.Blue;
        } else if (getMessage("RedInvert").equals(selected.getText())) {
            filtersOperationType = FiltersOperationType.RedInvert;
        } else if (getMessage("GreenInvert").equals(selected.getText())) {
            filtersOperationType = FiltersOperationType.GreenInvert;
        } else if (getMessage("BlueInvert").equals(selected.getText())) {
            filtersOperationType = FiltersOperationType.BlueInvert;
        } else if (getMessage("Sepia").equals(selected.getText())) {
            filtersOperationType = FiltersOperationType.Sepia;
            bwBox.setDisable(false);
            valueLabel.setText(getMessage("Intensity"));
            valueSlider.setDisable(false);
            valueSlider.setMax(255);
            valueSlider.setMin(0);
            valueSlider.setBlockIncrement(1);
            valueSlider.setValue(80);
            valueInput.setDisable(false);
            checkValueInput();
        }

    }

    private void checkValueInput() {
        try {
            if (filtersOperationType == FiltersOperationType.BlackOrWhite
                    && valueInput.getText().trim().isEmpty()) {
                valueInput.setStyle(null);
                intValue = -1;
                return;
            }
            intValue = Integer.valueOf(valueInput.getText());
            if (intValue >= 0 && intValue <= valueSlider.getMax()) {
                valueInput.setStyle(null);
                valueSlider.setValue(intValue);
            } else {
                valueInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            valueInput.setStyle(badStyle);
        }
    }

    @Override
    protected BufferedImage handleImage(BufferedImage source) {
        try {
            BufferedImage target = null;
            if (filtersOperationType == FiltersOperationType.Gray) {
                target = ImageGrayTools.color2Gray(source);

            } else if (filtersOperationType == FiltersOperationType.Invert) {
                target = ImageConvertTools.makeInvert(source);

            } else if (filtersOperationType == FiltersOperationType.BlackOrWhite) {

                if (intValue < 0) {
                    target = ImageGrayTools.color2Binary(source);
                } else {
                    target = ImageGrayTools.color2BinaryWithPercentage(source, intValue);
                }

            } else if (filtersOperationType == FiltersOperationType.Red) {
                target = ImageConvertTools.keepRed(source);

            } else if (filtersOperationType == FiltersOperationType.Green) {
                target = ImageConvertTools.keepGreen(source);

            } else if (filtersOperationType == FiltersOperationType.Blue) {
                target = ImageConvertTools.keepBlue(source);

            } else if (filtersOperationType == FiltersOperationType.RedInvert) {
                target = ImageConvertTools.makeRedInvert(source);

            } else if (filtersOperationType == FiltersOperationType.GreenInvert) {
                target = ImageConvertTools.makeGreenInvert(source);

            } else if (filtersOperationType == FiltersOperationType.BlueInvert) {
                target = ImageConvertTools.makeBlueInvert(source);

            } else if (filtersOperationType == FiltersOperationType.Sepia) {
                target = ImageConvertTools.sepiaImage(source, intValue);

            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }

    }

}
