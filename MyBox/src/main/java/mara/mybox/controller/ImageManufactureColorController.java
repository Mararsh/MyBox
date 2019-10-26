package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import mara.mybox.controller.ImageManufactureController.ImageOperation;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.fxml.FxmlColor;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.image.ImageColor;
import mara.mybox.image.ImageManufacture;
import mara.mybox.image.ImageScope;
import mara.mybox.image.PixelsOperation;
import mara.mybox.image.PixelsOperation.ColorActionType;
import mara.mybox.image.PixelsOperation.OperationType;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2019-9-1
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureColorController extends ImageManufactureOperationController {

    protected int colorValue;
    private OperationType colorOperationType;
    private ColorActionType colorActionType;

    @FXML
    protected VBox setBox;
    @FXML
    protected HBox valueBox, alphaBox;
    @FXML
    protected FlowPane opBox;
    @FXML
    protected ToggleGroup colorGroup;
    @FXML
    protected RadioButton colorBrightnessRadio, colorHueRadio, colorSaturationRadio, colorRedRadio,
            colorGreenRadio, colorBlueRadio, colorYellowRadio, colorCyanRadio, colorMagentaRadio,
            colorOpacityRadio, colorColorRadio, colorRGBRadio;
    @FXML
    protected ComboBox<String> valueSelector;
    @FXML
    protected Label colorLabel, colorUnit;
    @FXML
    protected Button colorIncreaseButton, colorDecreaseButton, colorFilterButton,
            colorInvertButton, paletteButton, demoButton;
    @FXML
    protected CheckBox preAlphaCheck;
    @FXML
    protected ImageView preAlphaTipsView;
    @FXML
    protected Rectangle colorRect;

    public ImageManufactureColorController() {
        baseTitle = message("ImageManufactureColor");
        operation = ImageOperation.Color;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            myPane = colorPane;

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @Override
    public void eventsHandler(KeyEvent event) {
        keyEventsHandlerDo(event);
        if (!event.isAltDown()) {
            return;
        }
        String key = event.getText();
        if (key == null || key.isEmpty()) {
            return;
        }
        switch (key) {
            case "1":
                if (opBox.getChildren().contains(setButton) && !setButton.isDisabled()) {
                    setAction();
                }
                break;
            case "2":
                if (opBox.getChildren().contains(colorIncreaseButton) && !colorIncreaseButton.isDisabled()) {
                    increaseAction();
                }
                break;
            case "3":
                if (opBox.getChildren().contains(colorDecreaseButton) && !colorDecreaseButton.isDisabled()) {
                    decreaseAction();
                }
                break;
            case "4":
                if (opBox.getChildren().contains(colorFilterButton) && !colorFilterButton.isDisabled()) {
                    filterAction();
                }
                break;
            case "5":
                if (opBox.getChildren().contains(colorInvertButton) && !colorInvertButton.isDisabled()) {
                    invertAction();
                }
                break;
        }

    }

    @Override
    public void initPane(ImageManufactureController parent) {
        try {
            super.initPane(parent);
            if (parent == null) {
                return;
            }

            colorGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkColorType();
                }
            });

            setButton.disableProperty().bind(valueSelector.getEditor().styleProperty().isEqualTo(badStyle));
            colorIncreaseButton.disableProperty().bind(setButton.disableProperty());
            colorDecreaseButton.disableProperty().bind(setButton.disableProperty());
            colorInvertButton.disableProperty().bind(setButton.disableProperty());
            colorFilterButton.disableProperty().bind(setButton.disableProperty());

            if (parent.imageInformation != null
                    && CommonValues.NoAlphaImages.contains(parent.imageInformation.getImageFormat())) {
                preAlphaCheck.setSelected(true);
                preAlphaCheck.setDisable(true);
            } else {
                preAlphaCheck.setSelected(false);
                preAlphaCheck.setDisable(false);
            }

            try {
                String c = AppVariables.getUserConfigValue("ImageColorSet", Color.TRANSPARENT.toString());
                colorRect.setFill(Color.web(c));
            } catch (Exception e) {
                colorRect.setFill(Color.TRANSPARENT);
                AppVariables.setUserConfigValue("ImageColorSet", Color.TRANSPARENT.toString());
            }
            FxmlControl.setTooltip(colorRect, FxmlColor.colorNameDisplay((Color) colorRect.getFill()));

            checkColorType();

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    private void checkColorType() {
        try {
            setBox.getChildren().removeAll(valueBox, alphaBox, opBox);
            if (colorGroup.getSelectedToggle() == null) {
                return;
            }
            setBox.getChildren().addAll(valueBox, opBox);
            opBox.getChildren().clear();
            valueBox.getChildren().clear();
            valueSelector.getItems().clear();
            FxmlControl.setEditorNormal(valueSelector);

            RadioButton selected = (RadioButton) colorGroup.getSelectedToggle();

            if (colorBrightnessRadio.equals(selected)) {
                colorOperationType = OperationType.Brightness;
                makeValuesBox(100, 1);
                colorUnit.setText("%");
                valueBox.getChildren().addAll(colorLabel, valueSelector, colorUnit);
                opBox.getChildren().addAll(setButton, colorIncreaseButton, colorDecreaseButton);

            } else if (colorSaturationRadio.equals(selected)) {
                colorOperationType = OperationType.Saturation;
                makeValuesBox(100, 1);
                colorUnit.setText("%");
                valueBox.getChildren().addAll(colorLabel, valueSelector, colorUnit);
                opBox.getChildren().addAll(setButton, colorIncreaseButton, colorDecreaseButton);

            } else if (colorHueRadio.equals(selected)) {
                colorOperationType = OperationType.Hue;
                makeValuesBox(360, 1);
                colorUnit.setText(message("Degree"));
                valueBox.getChildren().addAll(colorLabel, valueSelector, colorUnit);
                opBox.getChildren().addAll(setButton, colorIncreaseButton, colorDecreaseButton);

            } else if (colorRedRadio.equals(selected)) {
                colorOperationType = OperationType.Red;
                makeValuesBox(255, 1);
                valueBox.getChildren().addAll(colorLabel, valueSelector, colorUnit);
                opBox.getChildren().addAll(setButton, colorIncreaseButton, colorDecreaseButton, colorFilterButton, colorInvertButton);

            } else if (colorGreenRadio.equals(selected)) {
                colorOperationType = OperationType.Green;
                makeValuesBox(255, 1);
                valueBox.getChildren().addAll(colorLabel, valueSelector, colorUnit);
                opBox.getChildren().addAll(setButton, colorIncreaseButton, colorDecreaseButton, colorFilterButton, colorInvertButton);

            } else if (colorBlueRadio.equals(selected)) {
                colorOperationType = OperationType.Blue;
                makeValuesBox(255, 1);
                valueBox.getChildren().addAll(colorLabel, valueSelector, colorUnit);
                opBox.getChildren().addAll(setButton, colorIncreaseButton, colorDecreaseButton, colorFilterButton, colorInvertButton);

            } else if (colorYellowRadio.equals(selected)) {
                colorOperationType = OperationType.Yellow;
                makeValuesBox(255, 1);
                valueBox.getChildren().addAll(colorLabel, valueSelector, colorUnit);
                opBox.getChildren().addAll(setButton, colorIncreaseButton, colorDecreaseButton, colorFilterButton, colorInvertButton);

            } else if (colorCyanRadio.equals(selected)) {
                colorOperationType = OperationType.Cyan;
                makeValuesBox(255, 1);
                valueBox.getChildren().addAll(colorLabel, valueSelector, colorUnit);
                opBox.getChildren().addAll(setButton, colorIncreaseButton, colorDecreaseButton, colorFilterButton, colorInvertButton);

            } else if (colorMagentaRadio.equals(selected)) {
                colorOperationType = OperationType.Magenta;
                makeValuesBox(255, 1);
                valueBox.getChildren().addAll(colorLabel, valueSelector, colorUnit);
                opBox.getChildren().addAll(setButton, colorIncreaseButton, colorDecreaseButton, colorFilterButton, colorInvertButton);

            } else if (colorRGBRadio.equals(selected)) {
                colorOperationType = OperationType.RGB;
                makeValuesBox(255, 1);
                valueBox.getChildren().addAll(colorLabel, valueSelector, colorUnit);
                opBox.getChildren().addAll(colorIncreaseButton, colorDecreaseButton, colorInvertButton);

            } else if (colorOpacityRadio.equals(selected)) {
                colorOperationType = OperationType.Opacity;
                makeValuesBox(100, 0);
                colorUnit.setText("%");
                valueBox.getChildren().addAll(colorLabel, valueSelector, colorUnit);
                opBox.getChildren().addAll(setButton, colorIncreaseButton, colorDecreaseButton);
                alphaBox.setVisible(true);

            } else if (colorColorRadio.equals(selected)) {
                colorOperationType = OperationType.Color;
                setBox.getChildren().add(2, alphaBox);
                valueBox.getChildren().addAll(colorRect, paletteButton);
                opBox.getChildren().addAll(setButton);

            }

            FxmlControl.refreshStyle(setBox);

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
            valueSelector.getItems().addAll(valueList);
            valueSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v >= min && v <= max) {
                            colorValue = v;
                            FxmlControl.setEditorNormal(valueSelector);
                        } else {
                            FxmlControl.setEditorBadStyle(valueSelector);
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(valueSelector);
                    }
                }
            });
            valueSelector.getSelectionModel().select(valueList.size() / 2);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public boolean setColor(Control control, Color color) {
        if (control == null || color == null) {
            return false;
        }
        if (paletteButton.equals(control)) {
            colorRect.setFill(color);
            FxmlControl.setTooltip(colorRect, FxmlColor.colorNameDisplay(color));
            AppVariables.setUserConfigValue("ImageColorSet", color.toString());
        }
        return true;
    }

    @FXML
    @Override
    public void showPalette(ActionEvent event) {
        showPalette(paletteButton, message("Color"), true);
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
    @Override
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
        if (parent == null || colorOperationType == null || colorActionType == null) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                private Image newImage;

                @Override
                protected boolean handle() {
                    if (colorOperationType == OperationType.Opacity && preAlphaCheck.isSelected()) {
                        colorOperationType = OperationType.PreOpacity;
                    }
                    PixelsOperation pixelsOperation = PixelsOperation.create(imageView.getImage(),
                            parent.scope(), colorOperationType, colorActionType);
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
                            pixelsOperation.setColorPara1(ImageColor.converColor((Color) colorRect.getFill()));
                            break;
                    }
                    newImage = pixelsOperation.operateFxImage();
                    if (task == null || isCancelled()) {
                        return false;
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    parent.updateImage(ImageOperation.Color,
                            colorOperationType.name(), colorActionType.name(), newImage, cost);
                }
            };
            parent.openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    protected void demo() {
        if (imageView.getImage() == null) {
            return;
        }
        parent.popInformation(message("WaitAndHandling"));
        demoButton.setDisable(true);
        Task demoTask = new Task<Void>() {
            private List<String> files;

            @Override
            protected Void call() {

                try {
                    files = new ArrayList<>();
                    BufferedImage image = SwingFXUtils.fromFXImage(imageView.getImage(), null);
                    image = ImageManufacture.scaleImageLess(image, 1000000);

                    PixelsOperation pixelsOperation;
                    BufferedImage bufferedImage;
                    String tmpFile;

                    BufferedImage outlineSource = SwingFXUtils.fromFXImage(new Image("img/About.png"), null);
                    ImageScope scope = new ImageScope(SwingFXUtils.toFXImage(image, null));
                    scope.setScopeType(ImageScope.ScopeType.Outline);
                    if (sourceFile != null) {
                        scope.setFile(sourceFile.getAbsolutePath());
                    }
                    scope.setRectangle(new DoubleRectangle(0, 0, image.getWidth(), image.getHeight()));
                    BufferedImage[] outline = ImageManufacture.outline(outlineSource,
                            scope.getRectangle(), image.getWidth(), image.getHeight(),
                            false, ImageColor.converColor(Color.WHITE), false);
                    scope.setOutlineSource(outlineSource);
                    scope.setOutline(outline[1]);

                    pixelsOperation = PixelsOperation.create(image,
                            scope, OperationType.Color, ColorActionType.Set);
                    pixelsOperation.setColorPara1(ImageColor.converColor(Color.LIGHTPINK));
                    bufferedImage = pixelsOperation.operate();
                    tmpFile = AppVariables.MyBoxTempPath + File.separator
                            + message("Color") + "_" + message("Set") + ".png";
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    pixelsOperation = PixelsOperation.create(image,
                            scope, OperationType.Brightness, ColorActionType.Increase);
                    pixelsOperation.setFloatPara1(0.5f);
                    bufferedImage = pixelsOperation.operate();
                    tmpFile = AppVariables.MyBoxTempPath + File.separator
                            + message("Brightness") + "_" + message("Increase") + ".png";
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    pixelsOperation = PixelsOperation.create(image,
                            scope, OperationType.Hue, ColorActionType.Decrease);
                    pixelsOperation.setFloatPara1(0.3f);
                    bufferedImage = pixelsOperation.operate();
                    tmpFile = AppVariables.MyBoxTempPath + File.separator
                            + message("Hue") + "_" + message("Decrease") + ".png";
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    pixelsOperation = PixelsOperation.create(image,
                            scope, OperationType.Saturation, ColorActionType.Increase);
                    pixelsOperation.setFloatPara1(0.5f);
                    bufferedImage = pixelsOperation.operate();
                    tmpFile = AppVariables.MyBoxTempPath + File.separator
                            + message("Saturation") + "_" + message("Increase") + ".png";
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    pixelsOperation = PixelsOperation.create(image,
                            scope, OperationType.Opacity, ColorActionType.Decrease);
                    pixelsOperation.setIntPara1(128);
                    bufferedImage = pixelsOperation.operate();
                    tmpFile = AppVariables.MyBoxTempPath + File.separator
                            + message("Opacity") + "_" + message("Decrease") + ".png";
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    pixelsOperation = PixelsOperation.create(image,
                            scope, OperationType.RGB, ColorActionType.Invert);
                    bufferedImage = pixelsOperation.operate();
                    tmpFile = AppVariables.MyBoxTempPath + File.separator
                            + message("RGB") + "_" + message("Invert") + ".png";
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    pixelsOperation = PixelsOperation.create(image,
                            scope, OperationType.Red, ColorActionType.Filter);
                    bufferedImage = pixelsOperation.operate();
                    tmpFile = AppVariables.MyBoxTempPath + File.separator
                            + message("Red") + "_" + message("Filter") + ".png";
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    pixelsOperation = PixelsOperation.create(image,
                            scope, OperationType.Yellow, ColorActionType.Increase);
                    pixelsOperation.setIntPara1(60);
                    bufferedImage = pixelsOperation.operate();
                    tmpFile = AppVariables.MyBoxTempPath + File.separator
                            + message("Yellow") + "_" + message("Increase") + ".png";
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    pixelsOperation = PixelsOperation.create(image,
                            scope, OperationType.Magenta, ColorActionType.Decrease);
                    pixelsOperation.setIntPara1(60);
                    bufferedImage = pixelsOperation.operate();
                    tmpFile = AppVariables.MyBoxTempPath + File.separator
                            + message("Magenta") + "_" + message("Decrease") + ".png";
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                } catch (Exception e) {

                }
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                demoButton.setDisable(false);
                if (files.isEmpty()) {
                    return;
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ImagesBrowserController controller
                                    = (ImagesBrowserController) FxmlStage.openStage(CommonValues.ImagesBrowserFxml);
                            controller.loadFiles(files);
                        } catch (Exception e) {
                            logger.error(e.toString());
                        }
                    }
                });

            }

        };
        Thread thread = new Thread(demoTask);
        thread.setDaemon(true);
        thread.start();

    }

}
