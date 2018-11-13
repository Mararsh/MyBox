package mara.mybox.controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.fxml.FxmlAdjustColorTools;
import mara.mybox.fxml.FxmlAdjustColorTools.ColorOperationType;
import mara.mybox.fxml.FxmlTools;
import mara.mybox.objects.CommonValues;
import mara.mybox.objects.ImageScope.OperationType;
import static mara.mybox.fxml.FxmlTools.badStyle;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.objects.ImageScope.ScopeType;

/**
 * @Author Mara
 * @CreateDate 2018-10-11
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureColorController extends ImageManufactureController {

    protected int colorValue;
    private ColorOperationType colorOperationType;

    @FXML
    protected Slider colorSlider;
    @FXML
    protected ToggleGroup colorGroup;
    @FXML
    protected TextField colorInput;
    @FXML
    protected Button colorDecreaseButton, colorIncreaseButton;
    @FXML
    protected RadioButton opacityRadio;
    @FXML
    protected Label colorUnit;

    public ImageManufactureColorController() {
    }

    @Override
    protected void initializeNext2() {
        try {
            initCommon();
            initColorTab();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initColorTab() {
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

            Tooltip tips = new Tooltip(getMessage("CTRL+a"));
            tips.setFont(new Font(16));
            FxmlTools.quickTooltip(colorIncreaseButton, tips);

            tips = new Tooltip(getMessage("CTRL+q"));
            tips.setFont(new Font(16));
            FxmlTools.quickTooltip(colorDecreaseButton, tips);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    protected void initInterface() {
        try {
            if (values == null || values.getImage() == null) {
                return;
            }
            super.initInterface();
            values.getScope().setOperationType(OperationType.Color);

            isSettingValues = true;
            if (CommonValues.NoAlphaImages.contains(values.getImageInfo().getImageFormat())) {
                opacityRadio.setDisable(true);
            } else {
                opacityRadio.setDisable(false);
            }

            isSettingValues = false;
        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    private void checkColorOperationType() {
        RadioButton selected = (RadioButton) colorGroup.getSelectedToggle();
        if (getMessage("Brightness").equals(selected.getText())) {
            colorOperationType = ColorOperationType.Brightness;
            colorSlider.setMax(100);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("%");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
            colorDecreaseButton.setVisible(true);
            colorIncreaseButton.setText(getMessage("Increase"));
        } else if (getMessage("Saturation").equals(selected.getText())) {
            colorOperationType = ColorOperationType.Sauration;
            colorSlider.setMax(100);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("%");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
            colorDecreaseButton.setVisible(true);
            colorIncreaseButton.setText(getMessage("Increase"));
        } else if (getMessage("Hue").equals(selected.getText())) {
            colorOperationType = ColorOperationType.Hue;
            colorSlider.setMax(359);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText(getMessage("Degree"));
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
            colorDecreaseButton.setVisible(true);
            colorIncreaseButton.setText(getMessage("Increase"));
        } else if (getMessage("Opacity").equals(selected.getText())) {
            colorOperationType = ColorOperationType.Opacity;
            colorSlider.setMax(100);
            colorSlider.setMin(0);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("%");
            colorInput.setText("50");
            colorDecreaseButton.setVisible(false);
            colorIncreaseButton.setText(getMessage("OK"));
        } else if (getMessage("Red").equals(selected.getText())) {
            colorOperationType = ColorOperationType.Red;
            colorSlider.setMax(255);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
            colorDecreaseButton.setVisible(true);
            colorIncreaseButton.setText(getMessage("Increase"));
        } else if (getMessage("Green").equals(selected.getText())) {
            colorOperationType = ColorOperationType.Green;
            colorSlider.setMax(255);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
            colorDecreaseButton.setVisible(true);
            colorIncreaseButton.setText(getMessage("Increase"));
        } else if (getMessage("Blue").equals(selected.getText())) {
            colorOperationType = ColorOperationType.Blue;
            colorSlider.setMax(255);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
            colorDecreaseButton.setVisible(true);
            colorIncreaseButton.setText(getMessage("Increase"));
        } else if (getMessage("Yellow").equals(selected.getText())) {
            colorOperationType = ColorOperationType.Yellow;
            colorSlider.setMax(255);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
            colorDecreaseButton.setVisible(true);
            colorIncreaseButton.setText(getMessage("Increase"));
        } else if (getMessage("Cyan").equals(selected.getText())) {
            colorOperationType = ColorOperationType.Cyan;
            colorSlider.setMax(255);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
            colorDecreaseButton.setVisible(true);
            colorIncreaseButton.setText(getMessage("Increase"));
        } else if (getMessage("Magenta").equals(selected.getText())) {
            colorOperationType = ColorOperationType.Magenta;
            colorSlider.setMax(255);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
            colorDecreaseButton.setVisible(true);
            colorIncreaseButton.setText(getMessage("Increase"));
        } else if (getMessage("RGB").equals(selected.getText())) {
            colorOperationType = ColorOperationType.RGB;
            colorSlider.setMax(255);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
            colorDecreaseButton.setVisible(true);
            colorIncreaseButton.setText(getMessage("Increase"));
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

    @FXML
    public void increaseColor() {
        applyChange(colorValue);
    }

    @FXML
    public void decreaseColor() {
        applyChange(0 - colorValue);
    }

    @Override
    protected void keyEventsHandler(KeyEvent event) {
        super.keyEventsHandler(event);
        String key = event.getText();
        if (key == null || key.isEmpty()) {
            return;
        }
        if (event.isControlDown()) {
            switch (key) {
                case "a":
                case "A":
                    increaseColor();
                    break;
                case "q":
                case "Q":
                    decreaseColor();
                    break;
            }
        }
    }

    private void applyChange(final int change) {
        if (null == colorOperationType || scope == null) {
            return;
        }
        Task increaseTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                double value;
                switch (colorOperationType) {
                    case Brightness:
                    case Sauration:
                    case Opacity:
                        value = change / 100.0f;
                        break;
                    case Hue:
                        value = change;
                        break;
                    case Red:
                    case Green:
                    case Blue:
                    case Yellow:
                    case Cyan:
                    case Magenta:
                    case RGB:
                        value = change / 255.0;
                        break;
                    default:
                        return null;
                }
                final Image newImage;
                if (scope.getScopeType() == ScopeType.Matting) {
                    newImage = FxmlAdjustColorTools.ajustColorByMatting(values.getCurrentImage(),
                            colorOperationType, value, scope.getPoints(), scope.getColorDistance());
                } else {
                    newImage = FxmlAdjustColorTools.ajustColorByScope(values.getCurrentImage(),
                            colorOperationType, value, scope);
                }
                recordImageHistory(ImageOperationType.Color, newImage);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        values.setUndoImage(values.getCurrentImage());
                        values.setCurrentImage(newImage);
                        imageView.setImage(newImage);
                        setImageChanged(true);
                    }
                });
                return null;
            }
        };
        openHandlingStage(increaseTask, Modality.WINDOW_MODAL);
        Thread thread = new Thread(increaseTask);
        thread.setDaemon(true);
        thread.start();

    }

}
