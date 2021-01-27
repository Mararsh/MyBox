package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
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
import javafx.stage.Modality;
import mara.mybox.controller.ImageManufactureController.ImageOperation;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.dev.MyBoxLog;
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
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2019-9-1
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureColorController extends ImageManufactureOperationController {

    protected int colorValue, colorDistance;
    private OperationType colorOperationType;
    private ColorActionType colorActionType;

    @FXML
    protected VBox setBox, replaceBox;
    @FXML
    protected HBox valueBox, alphaBox, valueColorBox;
    @FXML
    protected FlowPane opBox, originalColorPane, newColorPane;
    @FXML
    protected ToggleGroup colorGroup;
    @FXML
    protected RadioButton colorReplaceRadio, colorColorRadio, colorRGBRadio,
            colorBrightnessRadio, colorHueRadio, colorSaturationRadio,
            colorRedRadio, colorGreenRadio, colorBlueRadio, colorOpacityRadio,
            colorYellowRadio, colorCyanRadio, colorMagentaRadio,
            distanceColorRadio, distanceHueRadio;
    @FXML
    protected ComboBox<String> valueSelector, distanceSelector;
    @FXML
    protected Label colorLabel, colorUnit, commentsLabel;
    @FXML
    protected Button colorIncreaseButton, colorDecreaseButton, colorFilterButton,
            colorInvertButton, demoButton;
    @FXML
    protected CheckBox preAlphaCheck, distanceExcludeCheck;
    @FXML
    protected ImageView preAlphaTipsView, distanceTipsView;
    @FXML
    protected ColorSetController originalColorSetController;
    @FXML
    protected ColorSetController newColorSetController;
    @FXML
    protected ColorSetController valueColorSetController;

    @Override
    public void initPane() {
        try {
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

            if (imageController.imageInformation != null
                    && CommonValues.NoAlphaImages.contains(imageController.imageInformation.getImageFormat())) {
                preAlphaCheck.setSelected(true);
                preAlphaCheck.setDisable(true);
            } else {
                preAlphaCheck.setSelected(false);
                preAlphaCheck.setDisable(false);
            }

            valueColorSetController.init(this, baseName + "ValueColor", Color.TRANSPARENT);
            originalColorSetController.init(this, baseName + "OriginalColor", Color.WHITE);
            newColorSetController.init(this, baseName + "NewColor", Color.TRANSPARENT);

            colorDistance = AppVariables.getUserConfigInt(baseName + "ColorDistance", 20);
            colorDistance = colorDistance <= 0 ? 20 : colorDistance;
            distanceSelector.getItems().addAll(Arrays.asList(
                    "20", "50", "80", "100", "10", "5", "120", "160", "127", "200", "180", "220", "230", "245"));
            distanceSelector.setValue(colorDistance + "");
            distanceSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            colorDistance = v;
                            AppVariables.setUserConfigInt(baseName + "ColorDistance", colorDistance);
                            FxmlControl.setEditorNormal(distanceSelector);
                        } else {
                            FxmlControl.setEditorBadStyle(distanceSelector);
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(distanceSelector);
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    @Override
    protected void paneExpanded() {
        imageController.showRightPane();
        checkColorType();
    }

    private void checkColorType() {
        try {
            imageController.resetImagePane();
            setBox.getChildren().clear();
            opBox.getChildren().clear();
            valueBox.getChildren().clear();
            valueSelector.getItems().clear();
            FxmlControl.setEditorNormal(valueSelector);
            okButton.disableProperty().unbind();
            commentsLabel.setText("");

            if (colorGroup.getSelectedToggle() == null) {
                return;
            }
            if (colorReplaceRadio.isSelected()) {
                imageController.hideScopePane();
                imageController.showImagePane();
                okButton.disableProperty().bind(distanceSelector.getEditor().styleProperty().isEqualTo(badStyle));
                colorOperationType = OperationType.ReplaceColor;
                setBox.getChildren().addAll(replaceBox);
                commentsLabel.setText(message("ManufactureWholeImage"));

            } else {
                if (scopeController != null && scopeController.scope != null
                        && scopeController.scope.getScopeType() != ImageScope.ScopeType.All) {
                    imageController.hideImagePane();
                    imageController.showScopePane();
                } else {
                    imageController.hideScopePane();
                    imageController.showImagePane();
                }
                okButton.setDisable(true);
                commentsLabel.setText(message("DefineScopeAndManufacture"));

                if (colorColorRadio.isSelected()) {
                    colorOperationType = OperationType.Color;
                    setBox.getChildren().addAll(valueBox, alphaBox, opBox);
                    valueBox.getChildren().addAll(valueColorBox);
                    opBox.getChildren().addAll(setButton);

                } else if (colorRGBRadio.isSelected()) {
                    colorOperationType = OperationType.RGB;
                    makeValuesBox(255, 1);
                    setBox.getChildren().addAll(valueBox, opBox);
                    valueBox.getChildren().addAll(colorLabel, valueSelector, colorUnit);
                    opBox.getChildren().addAll(colorIncreaseButton, colorDecreaseButton, colorInvertButton);

                } else if (colorBrightnessRadio.isSelected()) {
                    colorOperationType = OperationType.Brightness;
                    makeValuesBox(100, 1);
                    colorUnit.setText("%");
                    setBox.getChildren().addAll(valueBox, opBox);
                    valueBox.getChildren().addAll(colorLabel, valueSelector, colorUnit);
                    opBox.getChildren().addAll(setButton, colorIncreaseButton, colorDecreaseButton);

                } else if (colorSaturationRadio.isSelected()) {
                    colorOperationType = OperationType.Saturation;
                    makeValuesBox(100, 1);
                    colorUnit.setText("%");
                    setBox.getChildren().addAll(valueBox, opBox);
                    valueBox.getChildren().addAll(colorLabel, valueSelector, colorUnit);
                    opBox.getChildren().addAll(setButton, colorIncreaseButton, colorDecreaseButton);

                } else if (colorHueRadio.isSelected()) {
                    colorOperationType = OperationType.Hue;
                    makeValuesBox(360, 1);
                    colorUnit.setText(message("Degree"));
                    setBox.getChildren().addAll(valueBox, opBox);
                    valueBox.getChildren().addAll(colorLabel, valueSelector, colorUnit);
                    opBox.getChildren().addAll(setButton, colorIncreaseButton, colorDecreaseButton);

                } else if (colorRedRadio.isSelected()) {
                    colorOperationType = OperationType.Red;
                    makeValuesBox(255, 1);
                    setBox.getChildren().addAll(valueBox, opBox);
                    valueBox.getChildren().addAll(colorLabel, valueSelector, colorUnit);
                    opBox.getChildren().addAll(setButton, colorIncreaseButton, colorDecreaseButton, colorFilterButton, colorInvertButton);

                } else if (colorGreenRadio.isSelected()) {
                    colorOperationType = OperationType.Green;
                    makeValuesBox(255, 1);
                    setBox.getChildren().addAll(valueBox, opBox);
                    valueBox.getChildren().addAll(colorLabel, valueSelector, colorUnit);
                    opBox.getChildren().addAll(setButton, colorIncreaseButton, colorDecreaseButton, colorFilterButton, colorInvertButton);

                } else if (colorBlueRadio.isSelected()) {
                    colorOperationType = OperationType.Blue;
                    makeValuesBox(255, 1);
                    setBox.getChildren().addAll(valueBox, opBox);
                    valueBox.getChildren().addAll(colorLabel, valueSelector, colorUnit);
                    opBox.getChildren().addAll(setButton, colorIncreaseButton, colorDecreaseButton, colorFilterButton, colorInvertButton);

                } else if (colorYellowRadio.isSelected()) {
                    colorOperationType = OperationType.Yellow;
                    makeValuesBox(255, 1);
                    setBox.getChildren().addAll(valueBox, opBox);
                    valueBox.getChildren().addAll(colorLabel, valueSelector, colorUnit);
                    opBox.getChildren().addAll(setButton, colorIncreaseButton, colorDecreaseButton, colorFilterButton, colorInvertButton);

                } else if (colorCyanRadio.isSelected()) {
                    colorOperationType = OperationType.Cyan;
                    makeValuesBox(255, 1);
                    setBox.getChildren().addAll(valueBox, opBox);
                    valueBox.getChildren().addAll(colorLabel, valueSelector, colorUnit);
                    opBox.getChildren().addAll(setButton, colorIncreaseButton, colorDecreaseButton, colorFilterButton, colorInvertButton);

                } else if (colorMagentaRadio.isSelected()) {
                    colorOperationType = OperationType.Magenta;
                    makeValuesBox(255, 1);
                    setBox.getChildren().addAll(valueBox, opBox);
                    valueBox.getChildren().addAll(colorLabel, valueSelector, colorUnit);
                    opBox.getChildren().addAll(setButton, colorIncreaseButton, colorDecreaseButton, colorFilterButton, colorInvertButton);

                } else if (colorOpacityRadio.isSelected()) {
                    colorOperationType = OperationType.Opacity;
                    makeValuesBox(100, 0);
                    colorUnit.setText("%");
                    setBox.getChildren().addAll(valueBox, alphaBox, opBox);
                    valueBox.getChildren().addAll(colorLabel, valueSelector, colorUnit);
                    opBox.getChildren().addAll(setButton, colorIncreaseButton, colorDecreaseButton);

                }

                setBox.getChildren().addAll(demoButton);
            }

            FxmlControl.refreshStyle(setBox);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
            MyBoxLog.error(e.toString());
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
    @Override
    public void setAction() {
        colorActionType = ColorActionType.Set;
        applyChange();
    }

    @FXML
    @Override
    public void okAction() {
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
        if (imageController == null || colorOperationType == null || colorActionType == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                private Image newImage;

                @Override
                protected boolean handle() {
                    PixelsOperation pixelsOperation;
                    if (colorOperationType == OperationType.ReplaceColor) {
                        java.awt.Color originalColor = ImageColor.converColor((Color) originalColorSetController.rect.getFill());
                        java.awt.Color newColor = ImageColor.converColor((Color) newColorSetController.rect.getFill());
                        ImageScope scope = new ImageScope(imageView.getImage());
                        scope.setScopeType(ImageScope.ScopeType.Color);
                        scope.setColorScopeType(ImageScope.ColorScopeType.Color);
                        List<java.awt.Color> colors = new ArrayList();
                        colors.add(originalColor);
                        scope.setColors(colors);
                        if (distanceColorRadio.isSelected()) {
                            scope.setColorScopeType(ImageScope.ColorScopeType.Color);
                            scope.setColorDistance(colorDistance);
                        } else {
                            scope.setColorScopeType(ImageScope.ColorScopeType.Hue);
                            scope.setHsbDistance(colorDistance / 360.0f);
                        }
                        scope.setColorExcluded(distanceExcludeCheck.isSelected());
                        pixelsOperation = PixelsOperation.create(imageView.getImage(),
                                scope, OperationType.ReplaceColor, ColorActionType.Set);
                        pixelsOperation.setColorPara1(originalColor);
                        pixelsOperation.setColorPara2(newColor);
                        if (originalColor.getRGB() == 0 || !scopeController.ignoreTransparentCheck.isSelected()) {
                            pixelsOperation.setSkipTransparent(false);
                        }

                    } else {
                        if (colorOperationType == OperationType.Opacity && preAlphaCheck.isSelected()) {
                            colorOperationType = OperationType.PreOpacity;
                        }
                        pixelsOperation = PixelsOperation.create(imageView.getImage(),
                                scopeController.scope, colorOperationType, colorActionType);
                        pixelsOperation.setSkipTransparent(scopeController.ignoreTransparentCheck.isSelected());
                        switch (colorOperationType) {
                            case Color:
                                pixelsOperation.setColorPara1(ImageColor.converColor((Color) valueColorSetController.rect.getFill()));
                                break;
                            case RGB:
                                pixelsOperation.setIntPara1(colorValue);
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
                            case Opacity:
                            case PreOpacity:
                                pixelsOperation.setIntPara1(colorValue * 255 / 100);
                                break;
                        }
                    }
                    newImage = pixelsOperation.operateFxImage();
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    imageController.popSuccessful();
                    imageController.updateImage(ImageOperation.Color,
                            colorOperationType.name(), colorActionType.name(), newImage, cost);
                }
            };
            imageController.openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
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
        imageController.popInformation(message("WaitAndHandling"));
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
                            MyBoxLog.error(e.toString());
                        }
                    }
                });

            }

        };
        Thread thread = new Thread(demoTask);
        thread.setDaemon(true);
        thread.start();

    }

    @Override
    public void keyEventsHandler(KeyEvent event) {
        if (event.isAltDown() && event.getCode() != null) {
            switch (event.getCode()) {
                case DIGIT1:
                    if (opBox.getChildren().contains(setButton) && !setButton.isDisabled()) {
                        setAction();
                    }
                    return;
                case DIGIT2:
                    if (opBox.getChildren().contains(colorIncreaseButton) && !colorIncreaseButton.isDisabled()) {
                        increaseAction();
                    }
                    return;
                case DIGIT3:
                    if (opBox.getChildren().contains(colorDecreaseButton) && !colorDecreaseButton.isDisabled()) {
                        decreaseAction();
                    }
                    return;
                case DIGIT4:
                    if (opBox.getChildren().contains(colorFilterButton) && !colorFilterButton.isDisabled()) {
                        filterAction();
                    }
                    return;
                case DIGIT5:
                    if (opBox.getChildren().contains(colorInvertButton) && !colorInvertButton.isDisabled()) {
                        invertAction();
                    }
                    return;
            }
        }
        super.keyEventsHandler(event);
    }

}
