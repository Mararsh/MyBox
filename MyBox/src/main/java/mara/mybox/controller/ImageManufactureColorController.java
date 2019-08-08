package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
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
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import mara.mybox.controller.base.ImageManufactureController;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.image.ImageColor;
import mara.mybox.image.PixelsOperation;
import mara.mybox.image.PixelsOperation.ColorActionType;
import mara.mybox.image.PixelsOperation.OperationType;
import static mara.mybox.value.AppVaribles.logger;
import static mara.mybox.value.AppVaribles.message;
import mara.mybox.value.CommonValues;

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
    protected HBox setBox;
    @FXML
    protected ComboBox<String> valuesBox, objectBox;
    @FXML
    protected Label colorUnit;
    @FXML
    protected Button decreaseButton, increaseButton, setButton, filterButton, invertButton;
    @FXML
    protected CheckBox preAlphaCheck;
    @FXML
    protected ImageView scopeTipsView, preAlphaTipsView;

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
            List<String> objects = Arrays.asList(message("Brightness"), message("Hue"), message("Saturation"),
                    message("Red"), message("Green"), message("Blue"),
                    message("Cyan"), message("Yellow"), message("Magenta"),
                    message("RGB"), message("Opacity"), message("Color"));
            objectBox.getItems().addAll(objects);
            objectBox.setVisibleRowCount(objects.size());
            objectBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue ov, Number oldValue, Number newValue) {
                    checkColorOperationType();
                }
            });

            setButton.disableProperty().bind(
                    valuesBox.getEditor().styleProperty().isEqualTo(badStyle)
            );

            FxmlControl.setTooltip(setButton, new Tooltip("CTRL+g"));

            increaseButton.disableProperty().bind(
                    valuesBox.getEditor().styleProperty().isEqualTo(badStyle)
            );

            FxmlControl.setTooltip(increaseButton, new Tooltip("CTRL+q"));

            decreaseButton.disableProperty().bind(
                    valuesBox.getEditor().styleProperty().isEqualTo(badStyle)
            );

            FxmlControl.setTooltip(decreaseButton, new Tooltip("CTRL+w"));

            filterButton.disableProperty().bind(
                    valuesBox.getEditor().styleProperty().isEqualTo(badStyle)
            );

            invertButton.disableProperty().bind(
                    valuesBox.getEditor().styleProperty().isEqualTo(badStyle)
            );

            colorPicker = new ColorPicker(Color.TRANSPARENT);

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

            objectBox.getSelectionModel().select(0);

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

            String selected = objectBox.getSelectionModel().getSelectedItem();
            if (message("Brightness").equals(selected)) {
                colorOperationType = OperationType.Brightness;
                makeValuesBox(100, 1);
                colorUnit.setText("%");
                setBox.getChildren().addAll(valuesBox, colorUnit, setButton, increaseButton, decreaseButton);
            } else if (message("Saturation").equals(selected)) {
                colorOperationType = OperationType.Saturation;
                makeValuesBox(100, 1);
                colorUnit.setText("%");
                setBox.getChildren().addAll(valuesBox, colorUnit, setButton, increaseButton, decreaseButton);
            } else if (message("Hue").equals(selected)) {
                colorOperationType = OperationType.Hue;
                makeValuesBox(360, 1);
                colorUnit.setText(message("Degree"));
                setBox.getChildren().addAll(valuesBox, colorUnit, setButton, increaseButton, decreaseButton);
            } else if (message("Red").equals(selected)) {
                colorOperationType = OperationType.Red;
                makeValuesBox(255, 1);
                setBox.getChildren().addAll(valuesBox, setButton, increaseButton, decreaseButton, filterButton, invertButton);
            } else if (message("Green").equals(selected)) {
                colorOperationType = OperationType.Green;
                makeValuesBox(255, 1);
                setBox.getChildren().addAll(valuesBox, setButton, increaseButton, decreaseButton, filterButton, invertButton);
            } else if (message("Blue").equals(selected)) {
                colorOperationType = OperationType.Blue;
                makeValuesBox(255, 1);
                setBox.getChildren().addAll(valuesBox, setButton, increaseButton, decreaseButton, filterButton, invertButton);
            } else if (message("Yellow").equals(selected)) {
                colorOperationType = OperationType.Yellow;
                makeValuesBox(255, 1);
                setBox.getChildren().addAll(valuesBox, setButton, increaseButton, decreaseButton, filterButton, invertButton);
            } else if (message("Cyan").equals(selected)) {
                colorOperationType = OperationType.Cyan;
                makeValuesBox(255, 1);
                setBox.getChildren().addAll(valuesBox, setButton, increaseButton, decreaseButton, filterButton, invertButton);
            } else if (message("Magenta").equals(selected)) {
                colorOperationType = OperationType.Magenta;
                makeValuesBox(255, 1);
                setBox.getChildren().addAll(valuesBox, setButton, increaseButton, decreaseButton, filterButton, invertButton);
            } else if (message("RGB").equals(selected)) {
                colorOperationType = OperationType.RGB;
                makeValuesBox(255, 1);
                setBox.getChildren().addAll(valuesBox, increaseButton, decreaseButton, invertButton);
            } else if (message("Opacity").equals(selected)) {
                colorOperationType = OperationType.Opacity;
                makeValuesBox(100, 0);
                colorUnit.setText("%");
                setBox.getChildren().addAll(preAlphaTipsView, preAlphaCheck, valuesBox, colorUnit,
                        setButton, increaseButton, decreaseButton);
            } else if (message("Color").equals(selected)) {
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
    public void popObjectBox() {
        objectBox.show();
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
