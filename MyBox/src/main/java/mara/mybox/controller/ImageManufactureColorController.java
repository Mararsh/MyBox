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
import javafx.scene.layout.HBox;
import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.fxml.FxmlAdjustColorTools;
import mara.mybox.fxml.FxmlAdjustColorTools.ColorActionType;
import mara.mybox.fxml.FxmlAdjustColorTools.ColorObjectType;
import mara.mybox.fxml.FxmlTools;
import mara.mybox.objects.CommonValues;
import static mara.mybox.fxml.FxmlTools.badStyle;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.objects.ImageScope;

/**
 * @Author Mara
 * @CreateDate 2018-10-11
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureColorController extends ImageManufactureController {

    protected int colorValue;
    private ColorObjectType colorOperationType;
    private ColorActionType colorActionType;

    @FXML
    protected Slider colorSlider;
    @FXML
    protected ToggleGroup colorGroup;
    @FXML
    protected TextField colorInput;
    @FXML
    protected Button decreaseButton, increaseButton, setButton, filterButton, invertButton;
    @FXML
    protected RadioButton opacityRadio;
    @FXML
    protected Label colorUnit;
    @FXML
    protected HBox colorHBox, sliderHBox;

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

            colorPicker.setValue(Color.TRANSPARENT);

            Tooltip tips = new Tooltip("CTRL+a");
            tips.setFont(new Font(16));
            FxmlTools.quickTooltip(increaseButton, tips);

            tips = new Tooltip("CTRL+q");
            tips.setFont(new Font(16));
            FxmlTools.quickTooltip(decreaseButton, tips);

            tips = new Tooltip("CTRL+w");
            tips.setFont(new Font(16));
            FxmlTools.quickTooltip(setButton, tips);

            colorPicker.setValue(Color.TRANSPARENT);

//            tips = new Tooltip(getMessage("ClickCurrentForNewColor"));
//            tips.setFont(new Font(16));
//            FxmlTools.setComments(colorPicker, tips);
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
            checkColorOperationType();
            checkColorInput();

            isSettingValues = true;
            if (CommonValues.NoAlphaImages.contains(values.getImageInfo().getImageFormat())) {
                opacityRadio.setDisable(true);
//                opacityRadio.setPrefWidth(0);
            } else {
                opacityRadio.setDisable(false);
                opacityRadio.setPrefWidth(USE_COMPUTED_SIZE);
            }

            isSettingValues = false;
        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    private void checkColorOperationType() {
        colorHBox.setDisable(true);
        sliderHBox.setDisable(false);
        setButton.setDisable(false);
        decreaseButton.setDisable(false);
        increaseButton.setDisable(false);
        filterButton.setDisable(false);
        invertButton.setDisable(false);
        if (scope != null) {
            scope.setOperationType(ImageScope.OperationType.Color);
        }
        RadioButton selected = (RadioButton) colorGroup.getSelectedToggle();
        if (getMessage("Brightness").equals(selected.getText())) {
            colorOperationType = ColorObjectType.Brightness;
            colorSlider.setMax(100);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("%");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
            filterButton.setDisable(true);
            invertButton.setDisable(true);
        } else if (getMessage("Saturation").equals(selected.getText())) {
            colorOperationType = ColorObjectType.Sauration;
            colorSlider.setMax(100);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("%");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
            filterButton.setDisable(true);
            invertButton.setDisable(true);
        } else if (getMessage("Hue").equals(selected.getText())) {
            colorOperationType = ColorObjectType.Hue;
            colorSlider.setMax(359);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText(getMessage("Degree"));
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
            filterButton.setDisable(true);
            invertButton.setDisable(true);
        } else if (getMessage("Red").equals(selected.getText())) {
            colorOperationType = ColorObjectType.Red;
            colorSlider.setMax(255);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
        } else if (getMessage("Green").equals(selected.getText())) {
            colorOperationType = ColorObjectType.Green;
            colorSlider.setMax(255);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
        } else if (getMessage("Blue").equals(selected.getText())) {
            colorOperationType = ColorObjectType.Blue;
            colorSlider.setMax(255);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
        } else if (getMessage("Yellow").equals(selected.getText())) {
            colorOperationType = ColorObjectType.Yellow;
            colorSlider.setMax(255);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
        } else if (getMessage("Cyan").equals(selected.getText())) {
            colorOperationType = ColorObjectType.Cyan;
            colorSlider.setMax(255);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
        } else if (getMessage("Magenta").equals(selected.getText())) {
            colorOperationType = ColorObjectType.Magenta;
            colorSlider.setMax(255);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
        } else if (getMessage("RGB").equals(selected.getText())) {
            colorOperationType = ColorObjectType.RGB;
            colorSlider.setMax(255);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
            filterButton.setDisable(true);
            setButton.setDisable(true);
        } else if (getMessage("Opacity").equals(selected.getText())) {
            colorOperationType = ColorObjectType.Opacity;
            colorSlider.setMax(100);
            colorSlider.setMin(0);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("%");
            colorInput.setText("50");
            decreaseButton.setDisable(true);
            increaseButton.setDisable(true);
            filterButton.setDisable(true);
            invertButton.setDisable(true);
        } else if (getMessage("Color").equals(selected.getText())) {
            if (scope != null) {
                scope.setOperationType(ImageScope.OperationType.ReplaceColor);
            }
            colorOperationType = ColorObjectType.Color;
            decreaseButton.setDisable(true);
            increaseButton.setDisable(true);
            filterButton.setDisable(true);
            invertButton.setDisable(true);
            colorHBox.setDisable(false);
            sliderHBox.setDisable(true);
        }
        if (scope != null) {
            showLabels();
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
    public void increaseAction() {
        colorActionType = ColorActionType.Increase;
        applyChange();
    }

    @FXML
    public void decreaseAction() {
        colorActionType = ColorActionType.Decrease;
        applyChange();
    }

    @FXML
    public void setAction() {
        colorActionType = ColorActionType.Set;
        applyChange();
    }

    @FXML
    public void filterAction() {
        colorActionType = ColorActionType.Filter;
        applyChange();
    }

    @FXML
    public void invertAction() {
        colorActionType = ColorActionType.Invert;
        applyChange();
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
                    increaseAction();
                    break;
                case "q":
                case "Q":
                    decreaseAction();
                    break;
                case "w":
                case "W":
                    setAction();
                    break;
            }
        }
    }

    private void applyChange() {
        if (null == colorOperationType || colorActionType == null || scope == null) {
            return;
        }
        Task increaseTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                double change = colorValue;
                switch (colorOperationType) {
                    case Brightness:
                    case Sauration:
                    case Opacity:
                        change = change / 100.0f;
                        break;
                    case Red:
                    case Green:
                    case Blue:
                    case Yellow:
                    case Cyan:
                    case Magenta:
                    case RGB:
                        change = change / 255.0;
                        break;
                }
                final Image newImage = FxmlAdjustColorTools.ajustColor(values.getCurrentImage(),
                        colorOperationType, colorActionType, change, colorPicker.getValue(), scope);
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
