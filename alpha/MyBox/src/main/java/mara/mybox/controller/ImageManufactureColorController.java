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
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import mara.mybox.bufferedimage.AlphaTools;
import mara.mybox.bufferedimage.ColorConvertTools;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.bufferedimage.PixelsOperation;
import mara.mybox.bufferedimage.PixelsOperation.ColorActionType;
import mara.mybox.bufferedimage.PixelsOperation.OperationType;
import mara.mybox.bufferedimage.PixelsOperationFactory;
import mara.mybox.bufferedimage.PixelsOperationFactory.BlendColor;
import mara.mybox.bufferedimage.ScaleTools;
import mara.mybox.controller.ImageManufactureController_Image.ImageOperation;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxImageTools;
import mara.mybox.fximage.ImageViewTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.value.AppPaths;
import mara.mybox.value.AppValues;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

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
    protected VBox setBox, replaceBox, blendBox;
    @FXML
    protected HBox valueBox, valueColorBox;
    @FXML
    protected FlowPane opBox, originalColorPane, newColorPane;
    @FXML
    protected ToggleGroup colorGroup, distanceGroup;
    @FXML
    protected RadioButton colorReplaceRadio, colorColorRadio, colorBlendRadio, colorRGBRadio,
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
            colorInvertButton, goBlendButton, demoButton;
    @FXML
    protected CheckBox distanceExcludeCheck, squareRootCheck, ignoreTransparentCheck;
    @FXML
    protected ImageView distanceTipsView;
    @FXML
    protected ColorSet originalColorSetController, newColorSetController, valueColorSetController;
    @FXML
    protected ControlImagesBlend blendController;

    @Override
    public void initPane() {
        try {
            super.initPane();

            colorGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkColorType();
                }
            });

            setButton.disableProperty().bind(valueSelector.getEditor().styleProperty().isEqualTo(UserConfig.badStyle()));
            colorIncreaseButton.disableProperty().bind(setButton.disableProperty());
            colorDecreaseButton.disableProperty().bind(setButton.disableProperty());
            colorInvertButton.disableProperty().bind(setButton.disableProperty());
            colorFilterButton.disableProperty().bind(setButton.disableProperty());

            valueColorSetController.init(this, baseName + "ValueColor", Color.TRANSPARENT);
            originalColorSetController.init(this, baseName + "OriginalColor", Color.WHITE);
            newColorSetController.init(this, baseName + "NewColor", Color.TRANSPARENT);

            colorDistance = UserConfig.getInt(baseName + "ColorDistance", 20);
            colorDistance = colorDistance <= 0 ? 20 : colorDistance;
            distanceSelector.setValue(colorDistance + "");
            distanceSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkDistance();
                }
            });

            squareRootCheck.setSelected(UserConfig.getBoolean(baseName + "ColorDistanceSquare", false));
            squareRootCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldv, Boolean newv) {
                    checkDistance();
                }
            });
            squareRootCheck.disableProperty().bind(distanceColorRadio.selectedProperty().not());

            distanceGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                    checkDistance();
                }
            });

            ignoreTransparentCheck.setSelected(UserConfig.getBoolean(baseName + "IgnoreTransparent", true));
            ignoreTransparentCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldv, Boolean newv) {
                    UserConfig.setBoolean(baseName + "IgnoreTransparent", ignoreTransparentCheck.isSelected());
                }
            });

            originalColorSetController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> observable, Paint oldValue, Paint newValue) {
                    ignoreTransparentCheck.setVisible(!originalColorSetController.color().equals(Color.TRANSPARENT));
                }
            });

            blendController.setParameters(this);

            checkDistance();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    protected void checkDistance() {
        if (isSettingValues) {
            return;
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                int max = 255, step = 10;
                if (distanceColorRadio.isSelected()) {
                    if (squareRootCheck.isSelected()) {
                        max = 255 * 255;
                        step = 100;
                    }
                } else {
                    max = 360;
                }
                NodeStyleTools.setTooltip(distanceSelector, new Tooltip("0 ~ " + max));
                String value = distanceSelector.getValue();
                List<String> vList = new ArrayList<>();
                for (int i = 0; i <= max; i += step) {
                    vList.add(i + "");
                }
                isSettingValues = true;
                distanceSelector.getItems().clear();
                distanceSelector.getItems().addAll(vList);
                distanceSelector.setValue(value);
                isSettingValues = false;
                try {
                    int v = Integer.parseInt(value);
                    if (v == 0 && originalColorSetController.color().equals(newColorSetController.color())) {
                        popError(message("OriginalNewSameColor"));
                        return;
                    }
                    if (v >= 0 && v <= max) {
                        colorDistance = v;
                        UserConfig.setInt(baseName + "ColorDistance", colorDistance);
                        distanceSelector.getEditor().setStyle(null);
                    } else {
                        distanceSelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                } catch (Exception e) {
                    distanceSelector.getEditor().setStyle(UserConfig.badStyle());
                }
            }
        });
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
            ValidationTools.setEditorNormal(valueSelector);
            commentsLabel.setText("");
            colorLabel.setText(message("Value"));
            colorUnit.setText("0-255");

            if (colorGroup.getSelectedToggle() == null) {
                return;
            }
            if (colorReplaceRadio.isSelected()) {
                imageController.imageTab();
                colorOperationType = OperationType.ReplaceColor;
                setBox.getChildren().addAll(replaceBox, opBox);
                opBox.getChildren().addAll(setButton, colorFilterButton);
                commentsLabel.setText(message("ManufactureWholeImage"));
                scopeCheck.setSelected(false);
                scopeCheck.setDisable(true);

            } else {
                commentsLabel.setText(message("DefineScopeAndManufacture"));
                scopeCheck.setDisable(false);
                if (!scopeController.scopeWhole()) {
                    imageController.scopeTab();
                    scopeCheck.setSelected(true);
                }

                if (colorColorRadio.isSelected()) {
                    colorOperationType = OperationType.Color;
                    setBox.getChildren().addAll(valueColorBox, scopeCheck, opBox);
                    opBox.getChildren().addAll(setButton, colorFilterButton);

                } else if (colorBlendRadio.isSelected()) {
                    colorOperationType = OperationType.Blend;
                    setBox.getChildren().addAll(valueColorBox, scopeCheck, blendBox);
                    setBlender();

                } else if (colorRGBRadio.isSelected()) {
                    colorOperationType = OperationType.RGB;
                    makeValuesBox(0, 255);
                    setBox.getChildren().addAll(valueBox, scopeCheck, opBox);
                    valueBox.getChildren().addAll(colorLabel, valueSelector, colorUnit);
                    opBox.getChildren().addAll(colorIncreaseButton, colorDecreaseButton, colorInvertButton);

                } else if (colorBrightnessRadio.isSelected()) {
                    colorOperationType = OperationType.Brightness;
                    makeValuesBox(0, 100);
                    colorUnit.setText("0-100");
                    setBox.getChildren().addAll(valueBox, scopeCheck, opBox);
                    valueBox.getChildren().addAll(colorLabel, valueSelector, colorUnit);
                    opBox.getChildren().addAll(setButton, colorIncreaseButton, colorDecreaseButton);

                } else if (colorSaturationRadio.isSelected()) {
                    colorOperationType = OperationType.Saturation;
                    makeValuesBox(0, 100);
                    colorUnit.setText("0-100");
                    setBox.getChildren().addAll(valueBox, scopeCheck, opBox);
                    valueBox.getChildren().addAll(colorLabel, valueSelector, colorUnit);
                    opBox.getChildren().addAll(setButton, colorIncreaseButton, colorDecreaseButton);

                } else if (colorHueRadio.isSelected()) {
                    colorOperationType = OperationType.Hue;
                    makeValuesBox(0, 360);
                    colorUnit.setText("0-360");
                    setBox.getChildren().addAll(valueBox, scopeCheck, opBox);
                    valueBox.getChildren().addAll(colorLabel, valueSelector, colorUnit);
                    opBox.getChildren().addAll(setButton, colorIncreaseButton, colorDecreaseButton);

                } else if (colorRedRadio.isSelected()) {
                    colorOperationType = OperationType.Red;
                    makeValuesBox(0, 255);
                    setBox.getChildren().addAll(valueBox, scopeCheck, opBox);
                    valueBox.getChildren().addAll(colorLabel, valueSelector, colorUnit);
                    opBox.getChildren().addAll(setButton, colorIncreaseButton, colorDecreaseButton, colorFilterButton, colorInvertButton);

                } else if (colorGreenRadio.isSelected()) {
                    colorOperationType = OperationType.Green;
                    makeValuesBox(0, 255);
                    setBox.getChildren().addAll(valueBox, scopeCheck, opBox);
                    valueBox.getChildren().addAll(colorLabel, valueSelector, colorUnit);
                    opBox.getChildren().addAll(setButton, colorIncreaseButton, colorDecreaseButton, colorFilterButton, colorInvertButton);

                } else if (colorBlueRadio.isSelected()) {
                    colorOperationType = OperationType.Blue;
                    makeValuesBox(0, 255);
                    setBox.getChildren().addAll(valueBox, scopeCheck, opBox);
                    valueBox.getChildren().addAll(colorLabel, valueSelector, colorUnit);
                    opBox.getChildren().addAll(setButton, colorIncreaseButton, colorDecreaseButton, colorFilterButton, colorInvertButton);

                } else if (colorYellowRadio.isSelected()) {
                    colorOperationType = OperationType.Yellow;
                    makeValuesBox(0, 255);
                    setBox.getChildren().addAll(valueBox, scopeCheck, opBox);
                    valueBox.getChildren().addAll(colorLabel, valueSelector, colorUnit);
                    opBox.getChildren().addAll(setButton, colorIncreaseButton, colorDecreaseButton, colorFilterButton, colorInvertButton);

                } else if (colorCyanRadio.isSelected()) {
                    colorOperationType = OperationType.Cyan;
                    makeValuesBox(0, 255);
                    setBox.getChildren().addAll(valueBox, scopeCheck, opBox);
                    valueBox.getChildren().addAll(colorLabel, valueSelector, colorUnit);
                    opBox.getChildren().addAll(setButton, colorIncreaseButton, colorDecreaseButton, colorFilterButton, colorInvertButton);

                } else if (colorMagentaRadio.isSelected()) {
                    colorOperationType = OperationType.Magenta;
                    makeValuesBox(0, 255);
                    setBox.getChildren().addAll(valueBox, scopeCheck, opBox);
                    valueBox.getChildren().addAll(colorLabel, valueSelector, colorUnit);
                    opBox.getChildren().addAll(setButton, colorIncreaseButton, colorDecreaseButton, colorFilterButton, colorInvertButton);

                } else if (colorOpacityRadio.isSelected()) {
                    colorOperationType = OperationType.Opacity;
                    makeValuesBox(0, 255);
                    setBox.getChildren().addAll(valueBox, scopeCheck, opBox);
                    valueBox.getChildren().addAll(colorLabel, valueSelector, colorUnit);
                    opBox.getChildren().addAll(setButton, colorIncreaseButton, colorDecreaseButton);

                }

                setBox.getChildren().addAll(demoButton);
            }

            refreshStyle(setBox);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    private void makeValuesBox(final int min, final int max) {
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
                        int v = Integer.parseInt(newValue);
                        if (v >= min && v <= max) {
                            colorValue = v;
                            ValidationTools.setEditorNormal(valueSelector);
                        } else {
                            ValidationTools.setEditorBadStyle(valueSelector);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(valueSelector);
                    }
                }
            });
            valueSelector.getSelectionModel().select(valueList.size() / 2);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void setBlender() {
        blendController.backImage = imageView.getImage();
        blendController.foreImage = FxImageTools.createImage(
                (int) (imageView.getImage().getWidth() / 2), (int) (imageView.getImage().getHeight() / 2),
                valueColorSetController.color());
        blendController.x = (int) (blendController.backImage.getWidth() - blendController.foreImage.getWidth()) / 2;
        blendController.y = (int) (blendController.backImage.getHeight() - blendController.foreImage.getHeight()) / 2;
    }

    @Override
    public void imageClicked(MouseEvent event, DoublePoint p) {
        if (imageController.isPickingColor) {
            Color color = ImageViewTools.imagePixel(p, imageView);
            if (color != null) {
                originalColorSetController.setColor(color);
                valueColorSetController.setColor(color);
            }
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
    public void filterAction() {
        colorActionType = ColorActionType.Filter;
        applyChange();
    }

    @FXML
    public void invertAction() {
        colorActionType = ColorActionType.Invert;
        applyChange();
    }

    @FXML
    public void blendAction() {
        colorActionType = ColorActionType.Set;
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
            task = new SingletonTask<Void>(this) {

                private Image newImage;

                @Override
                protected boolean handle() {
                    PixelsOperation pixelsOperation;
                    if (colorOperationType == OperationType.ReplaceColor) {
                        java.awt.Color originalColor = originalColorSetController.awtColor();
                        java.awt.Color newColor = newColorSetController.awtColor();
                        ImageScope scope = new ImageScope(imageView.getImage());
                        scope.setScopeType(ImageScope.ScopeType.Color);
                        scope.setColorScopeType(ImageScope.ColorScopeType.Color);
                        List<java.awt.Color> colors = new ArrayList();
                        colors.add(originalColor);
                        scope.setColors(colors);
                        if (distanceColorRadio.isSelected()) {
                            scope.setColorScopeType(ImageScope.ColorScopeType.Color);
                            if (squareRootCheck.isSelected()) {
                                scope.setColorDistanceSquare(colorDistance);
                            } else {
                                scope.setColorDistance(colorDistance);
                            }
                        } else {
                            scope.setColorScopeType(ImageScope.ColorScopeType.Hue);
                            scope.setHsbDistance(colorDistance / 360.0f);
                        }
                        scope.setColorExcluded(distanceExcludeCheck.isSelected());
                        pixelsOperation = PixelsOperationFactory.create(imageView.getImage(),
                                scope, OperationType.ReplaceColor, colorActionType)
                                .setColorPara1(originalColor)
                                .setColorPara2(newColor)
                                .setSkipTransparent(originalColor.getRGB() != 0 && ignoreTransparentCheck.isSelected());

                    } else {
                        pixelsOperation = PixelsOperationFactory.create(imageView.getImage(),
                                scopeController.scope, colorOperationType, colorActionType)
                                .setSkipTransparent(scopeController.ignoreTransparentCheck.isSelected());
                        switch (colorOperationType) {
                            case Color:
                                pixelsOperation.setColorPara1(valueColorSetController.awtColor());
                                break;
                            case Blend:
                                pixelsOperation.setColorPara1(valueColorSetController.awtColor());
                                ((BlendColor) pixelsOperation).setBlender(blendController.blender());
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
                                pixelsOperation.setIntPara1(colorValue);
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
            imageController.start(task);
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
                    image = ScaleTools.scaleImageLess(image, 1000000);

                    PixelsOperation pixelsOperation;
                    BufferedImage bufferedImage;
                    String tmpFile;

                    BufferedImage outlineSource = SwingFXUtils.fromFXImage(
                            new Image("img/cover" + AppValues.AppYear + "g9.png"), null);
                    ImageScope scope = new ImageScope(SwingFXUtils.toFXImage(image, null));
                    scope.setScopeType(ImageScope.ScopeType.Outline);
                    if (sourceFile != null) {
                        scope.setFile(sourceFile.getAbsolutePath());
                    }
                    scope.setRectangle(new DoubleRectangle(0, 0, image.getWidth() - 1, image.getHeight() - 1));
                    BufferedImage[] outline = AlphaTools.outline(outlineSource,
                            scope.getRectangle(), image.getWidth(), image.getHeight(),
                            false, ColorConvertTools.converColor(Color.WHITE), false);
                    scope.setOutlineSource(outlineSource);
                    scope.setOutline(outline[1]);

                    pixelsOperation = PixelsOperationFactory.create(image,
                            scope, OperationType.Color, ColorActionType.Filter);
                    pixelsOperation.setColorPara1(ColorConvertTools.converColor(Color.LIGHTPINK));
                    pixelsOperation.setFloatPara1(0.5f);
                    bufferedImage = pixelsOperation.operate();
                    tmpFile = AppPaths.getGeneratedPath() + File.separator
                            + message("Color") + "_" + message("Filter") + ".png";
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    pixelsOperation = PixelsOperationFactory.create(image,
                            scope, OperationType.Brightness, ColorActionType.Increase);
                    pixelsOperation.setFloatPara1(0.5f);
                    bufferedImage = pixelsOperation.operate();
                    tmpFile = AppPaths.getGeneratedPath() + File.separator
                            + message("Brightness") + "_" + message("Increase") + ".png";
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    pixelsOperation = PixelsOperationFactory.create(image,
                            scope, OperationType.Hue, ColorActionType.Decrease);
                    pixelsOperation.setFloatPara1(0.3f);
                    bufferedImage = pixelsOperation.operate();
                    tmpFile = AppPaths.getGeneratedPath() + File.separator
                            + message("Hue") + "_" + message("Decrease") + ".png";
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    pixelsOperation = PixelsOperationFactory.create(image,
                            scope, OperationType.Saturation, ColorActionType.Increase);
                    pixelsOperation.setFloatPara1(0.5f);
                    bufferedImage = pixelsOperation.operate();
                    tmpFile = AppPaths.getGeneratedPath() + File.separator
                            + message("Saturation") + "_" + message("Increase") + ".png";
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    pixelsOperation = PixelsOperationFactory.create(image,
                            scope, OperationType.Opacity, ColorActionType.Decrease);
                    pixelsOperation.setIntPara1(128);
                    bufferedImage = pixelsOperation.operate();
                    tmpFile = AppPaths.getGeneratedPath() + File.separator
                            + message("Opacity") + "_" + message("Decrease") + ".png";
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    pixelsOperation = PixelsOperationFactory.create(image,
                            scope, OperationType.RGB, ColorActionType.Invert);
                    bufferedImage = pixelsOperation.operate();
                    tmpFile = AppPaths.getGeneratedPath() + File.separator
                            + message("RGB") + "_" + message("Invert") + ".png";
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    pixelsOperation = PixelsOperationFactory.create(image,
                            scope, OperationType.Red, ColorActionType.Filter);
                    bufferedImage = pixelsOperation.operate();
                    tmpFile = AppPaths.getGeneratedPath() + File.separator
                            + message("Red") + "_" + message("Filter") + ".png";
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    pixelsOperation = PixelsOperationFactory.create(image,
                            scope, OperationType.Yellow, ColorActionType.Increase);
                    pixelsOperation.setIntPara1(60);
                    bufferedImage = pixelsOperation.operate();
                    tmpFile = AppPaths.getGeneratedPath() + File.separator
                            + message("Yellow") + "_" + message("Increase") + ".png";
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    pixelsOperation = PixelsOperationFactory.create(image,
                            scope, OperationType.Magenta, ColorActionType.Decrease);
                    pixelsOperation.setIntPara1(60);
                    bufferedImage = pixelsOperation.operate();
                    tmpFile = AppPaths.getGeneratedPath() + File.separator
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
                                    = (ImagesBrowserController) WindowTools.openStage(Fxmls.ImagesBrowserFxml);
                            controller.loadFiles(files);
                        } catch (Exception e) {
                            MyBoxLog.error(e.toString());
                        }
                    }
                });

            }

        };
        start(demoTask, false);
    }

    @Override
    public boolean altFilter(KeyEvent event) {
        if (!event.isAltDown() || event.getCode() == null) {
            return false;
        }
        switch (event.getCode()) {
            case DIGIT1:
                if (opBox.getChildren().contains(setButton) && !setButton.isDisabled()) {
                    setAction();
                }
                return true;
            case DIGIT2:
                if (opBox.getChildren().contains(colorIncreaseButton) && !colorIncreaseButton.isDisabled()) {
                    increaseAction();
                }
                return true;
            case DIGIT3:
                if (opBox.getChildren().contains(colorDecreaseButton) && !colorDecreaseButton.isDisabled()) {
                    decreaseAction();
                }
                return true;
            case DIGIT4:
                if (opBox.getChildren().contains(colorFilterButton) && !colorFilterButton.isDisabled()) {
                    filterAction();
                }
                return true;
            case DIGIT5:
                if (opBox.getChildren().contains(colorInvertButton) && !colorInvertButton.isDisabled()) {
                    invertAction();
                }
                return true;
        }
        return super.altFilter(event);
    }

}
