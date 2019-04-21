package mara.mybox.controller;

import mara.mybox.controller.base.ImageManufactureController;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.value.CommonValues;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.image.ImageColor;
import static mara.mybox.value.AppVaribles.getMessage;
import mara.mybox.image.PixelsOperation;
import mara.mybox.image.PixelsOperation.ColorActionType;
import mara.mybox.image.PixelsOperation.OperationType;

/**
 * @Author Mara
 * @CreateDate 2018-10-11
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureColorController extends ImageManufactureController {

    protected int colorValue;
    private OperationType colorOperationType;
    private ColorActionType colorActionType;

    @FXML
    protected ToggleGroup colorGroup;
    @FXML
    protected HBox setBox;
    @FXML
    protected ComboBox<String> valuesBox;
    @FXML
    protected Label colorUnit, preAlphaTipsLabel, scopeTipsLabel;
    @FXML
    protected Button decreaseButton, increaseButton, setButton, filterButton, invertButton;
    @FXML
    protected CheckBox preAlphaCheck;

    public ImageManufactureColorController() {

    }

    @Override
    public void initializeNext2() {
        try {
            initCommon();
            initColorTab();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void keyEventsHandler(KeyEvent event) {
        super.keyEventsHandler(event);
        if (event.isControlDown()) {
            String key = event.getText();
            if (key == null || key.isEmpty()) {
                return;
            }
            switch (key) {
                case "g":
                case "G":
                    if (setButton != null && setBox.getChildren().contains(setButton)) {
                        setAction();
                    }
                    break;
                case "q":
                case "Q":
                    if (increaseButton != null && setBox.getChildren().contains(increaseButton)) {
                        increaseAction();
                    }
                    break;
                case "w":
                case "W":
                    if (decreaseButton != null && setBox.getChildren().contains(decreaseButton)) {
                        decreaseAction();
                    }
                    break;
            }
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

            setButton.disableProperty().bind(
                    valuesBox.getEditor().styleProperty().isEqualTo(badStyle)
            );

            FxmlControl.quickTooltip(setButton, new Tooltip("CTRL+g"));

            increaseButton.disableProperty().bind(
                    valuesBox.getEditor().styleProperty().isEqualTo(badStyle)
            );

            FxmlControl.quickTooltip(increaseButton, new Tooltip("CTRL+q"));

            decreaseButton.disableProperty().bind(
                    valuesBox.getEditor().styleProperty().isEqualTo(badStyle)
            );

            FxmlControl.quickTooltip(decreaseButton, new Tooltip("CTRL+w"));

            filterButton.disableProperty().bind(
                    valuesBox.getEditor().styleProperty().isEqualTo(badStyle)
            );

            invertButton.disableProperty().bind(
                    valuesBox.getEditor().styleProperty().isEqualTo(badStyle)
            );

            colorPicker = new ColorPicker(Color.TRANSPARENT);

            FxmlControl.setComments(preAlphaTipsLabel, new Tooltip(getMessage("PremultipliedAlphaTips")));

            FxmlControl.quickTooltip(scopeTipsLabel, new Tooltip(getMessage("ImageScopeTips")));

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

            isSettingValues = true;
            tabPane.getSelectionModel().select(colorTab);

            if (values.getImageInfo() != null
                    && CommonValues.NoAlphaImages.contains(values.getImageInfo().getImageFormat())) {
                preAlphaCheck.setSelected(true);
                preAlphaCheck.setDisable(true);
            } else {
                preAlphaCheck.setSelected(false);
                preAlphaCheck.setDisable(false);
            }

            isSettingValues = false;
        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    private void checkColorOperationType() {
        try {
            setBox.getChildren().clear();
            valuesBox.getItems().clear();
            FxmlControl.setEditorNormal(valuesBox);
            pickColorButton.setSelected(false);

            RadioButton selected = (RadioButton) colorGroup.getSelectedToggle();
            if (getMessage("Brightness").equals(selected.getText())) {
                colorOperationType = OperationType.Brightness;
                makeValuesBox(100, 1);
                colorUnit.setText("%");
                setBox.getChildren().addAll(valuesBox, colorUnit, setButton, increaseButton, decreaseButton);
            } else if (getMessage("Saturation").equals(selected.getText())) {
                colorOperationType = OperationType.Saturation;
                makeValuesBox(100, 1);
                colorUnit.setText("%");
                setBox.getChildren().addAll(valuesBox, colorUnit, setButton, increaseButton, decreaseButton);
            } else if (getMessage("Hue").equals(selected.getText())) {
                colorOperationType = OperationType.Hue;
                makeValuesBox(360, 1);
                colorUnit.setText(getMessage("Degree"));
                setBox.getChildren().addAll(valuesBox, colorUnit, setButton, increaseButton, decreaseButton);
            } else if (getMessage("Red").equals(selected.getText())) {
                colorOperationType = OperationType.Red;
                makeValuesBox(255, 1);
                setBox.getChildren().addAll(valuesBox, setButton, increaseButton, decreaseButton, filterButton, invertButton);
            } else if (getMessage("Green").equals(selected.getText())) {
                colorOperationType = OperationType.Green;
                makeValuesBox(255, 1);
                setBox.getChildren().addAll(valuesBox, setButton, increaseButton, decreaseButton, filterButton, invertButton);
            } else if (getMessage("Blue").equals(selected.getText())) {
                colorOperationType = OperationType.Blue;
                makeValuesBox(255, 1);
                setBox.getChildren().addAll(valuesBox, setButton, increaseButton, decreaseButton, filterButton, invertButton);
            } else if (getMessage("Yellow").equals(selected.getText())) {
                colorOperationType = OperationType.Yellow;
                makeValuesBox(255, 1);
                setBox.getChildren().addAll(valuesBox, setButton, increaseButton, decreaseButton, filterButton, invertButton);
            } else if (getMessage("Cyan").equals(selected.getText())) {
                colorOperationType = OperationType.Cyan;
                makeValuesBox(255, 1);
                setBox.getChildren().addAll(valuesBox, setButton, increaseButton, decreaseButton, filterButton, invertButton);
            } else if (getMessage("Magenta").equals(selected.getText())) {
                colorOperationType = OperationType.Magenta;
                makeValuesBox(255, 1);
                setBox.getChildren().addAll(valuesBox, setButton, increaseButton, decreaseButton, filterButton, invertButton);
            } else if (getMessage("RGB").equals(selected.getText())) {
                colorOperationType = OperationType.RGB;
                makeValuesBox(255, 1);
                setBox.getChildren().addAll(valuesBox, setButton, increaseButton, decreaseButton);
            } else if (getMessage("Opacity").equals(selected.getText())) {
                colorOperationType = OperationType.Opacity;
                makeValuesBox(100, 0);
                colorUnit.setText("%");
                setBox.getChildren().addAll(preAlphaTipsLabel, preAlphaCheck, valuesBox, colorUnit,
                        setButton, increaseButton, decreaseButton);
            } else if (getMessage("Color").equals(selected.getText())) {
                colorOperationType = OperationType.Color;
                setBox.getChildren().addAll(colorPicker, pickColorButton, setButton);
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void makeValuesBox(final int max, final int min) {
        try {
            List<String> valueList = new ArrayList<>();
            int step = (max - min) / 10;
            for (int v = min; v < max; v += step) {
                valueList.add(v + "");
            }
            valueList.add(max + "");
            valuesBox.getItems().addAll(valueList);
            valuesBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v >= min && v <= max) {
                            colorValue = v;
                            FxmlControl.setEditorNormal(valuesBox);
                        } else {
                            FxmlControl.setEditorBadStyle(valuesBox);
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(valuesBox);
                    }
                }
            });
            valuesBox.getSelectionModel().select(valueList.size() / 2);

        } catch (Exception e) {
            logger.error(e.toString());
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

    private void applyChange() {
        if (null == colorOperationType || colorActionType == null || scope == null) {
            return;
        }
        task = new Task<Void>() {
            private Image newImage;
            private boolean ok;

            @Override
            protected Void call() throws Exception {
                if (colorOperationType == OperationType.Opacity && preAlphaCheck.isSelected()) {
                    colorOperationType = OperationType.PreOpacity;
                }
                PixelsOperation pixelsOperation = PixelsOperation.newPixelsOperation(imageView.getImage(),
                        scope, colorOperationType, colorActionType);
                switch (colorOperationType) {
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
                        pixelsOperation.setIntPara1(colorValue);
                        break;
                    case Opacity:
                    case PreOpacity:
                        pixelsOperation.setIntPara1(colorValue * 255 / 100);
                        break;
                    case Color:
                        pixelsOperation.setColorPara1(ImageColor.converColor(colorPicker.getValue()));
                        break;
                }
                newImage = pixelsOperation.operateFxImage();
                if (task == null || task.isCancelled()) {
                    return null;
                }
                recordImageHistory(ImageOperationType.Color, newImage);

                ok = true;
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                if (ok) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            values.setUndoImage(imageView.getImage());
                            values.setCurrentImage(newImage);
                            imageView.setImage(newImage);
                            setImageChanged(true);
                        }
                    });
                }
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

    }

}
