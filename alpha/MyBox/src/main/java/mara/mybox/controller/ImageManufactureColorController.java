package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.FileTmpTools;
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
    protected VBox setBox, colorMatchBox, newColorBox, blendBox;
    @FXML
    protected HBox valueBox;
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
    protected Button colorSetButton, colorIncreaseButton, colorDecreaseButton, colorFilterButton,
            colorInvertButton, demoButton;
    @FXML
    protected CheckBox distanceExcludeCheck, squareRootCheck, ignoreTransparentCheck,
            hueCheck, saturationCheck, brightnessCheck;
    @FXML
    protected ImageView distanceTipsView;
    @FXML
    protected ControlColorSet originalColorSetController, newColorSetController, valueColorSetController;
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

            colorSetButton.disableProperty().bind(valueSelector.getEditor().styleProperty().isEqualTo(UserConfig.badStyle()));
            colorIncreaseButton.disableProperty().bind(colorSetButton.disableProperty());
            colorDecreaseButton.disableProperty().bind(colorSetButton.disableProperty());
            colorInvertButton.disableProperty().bind(colorSetButton.disableProperty());
            colorFilterButton.disableProperty().bind(colorSetButton.disableProperty());

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

            originalColorSetController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> observable, Paint oldValue, Paint newValue) {
                    ignoreTransparentCheck.setVisible(!originalColorSetController.color().equals(Color.TRANSPARENT));
                }
            });

            blendController.setParameters(this);

            checkDistance();
        } catch (Exception e) {
            MyBoxLog.error(e);
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
        editor.showRightPane();
        checkColorType();
    }

    private void checkColorType() {
        try {
            editor.resetImagePane();
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
                editor.imageTab();
                colorOperationType = OperationType.ReplaceColor;
                setBox.getChildren().addAll(colorMatchBox, newColorBox, opBox);
                opBox.getChildren().addAll(goButton);
                commentsLabel.setText(message("ManufactureWholeImage"));
                scopeCheck.setSelected(false);
                scopeCheck.setDisable(true);

            } else {
                commentsLabel.setText(message("DefineScopeAndManufacture"));
                scopeCheck.setDisable(false);
                if (!scopeController.scopeWhole()) {
                    editor.scopeTab();
                    scopeCheck.setSelected(true);
                }

                if (colorColorRadio.isSelected()) {
                    colorOperationType = OperationType.Color;
                    setBox.getChildren().addAll(newColorBox, opBox);
                    opBox.getChildren().addAll(goButton);

                } else if (colorBlendRadio.isSelected()) {
                    colorOperationType = OperationType.Blend;
                    setBox.getChildren().addAll(blendBox, opBox);
                    opBox.getChildren().addAll(goButton);
                    setBlender();

                } else if (colorRGBRadio.isSelected()) {
                    colorOperationType = OperationType.RGB;
                    makeValuesBox(0, 255);
                    setBox.getChildren().addAll(valueBox, opBox);
                    valueBox.getChildren().addAll(colorLabel, valueSelector, colorUnit);
                    opBox.getChildren().addAll(colorIncreaseButton, colorDecreaseButton, colorInvertButton);

                } else if (colorBrightnessRadio.isSelected()) {
                    colorOperationType = OperationType.Brightness;
                    makeValuesBox(0, 100);
                    colorUnit.setText("0-100");
                    setBox.getChildren().addAll(valueBox, opBox);
                    valueBox.getChildren().addAll(colorLabel, valueSelector, colorUnit);
                    opBox.getChildren().addAll(colorSetButton, colorIncreaseButton, colorDecreaseButton);

                } else if (colorSaturationRadio.isSelected()) {
                    colorOperationType = OperationType.Saturation;
                    makeValuesBox(0, 100);
                    colorUnit.setText("0-100");
                    setBox.getChildren().addAll(valueBox, opBox);
                    valueBox.getChildren().addAll(colorLabel, valueSelector, colorUnit);
                    opBox.getChildren().addAll(colorSetButton, colorIncreaseButton, colorDecreaseButton);

                } else if (colorHueRadio.isSelected()) {
                    colorOperationType = OperationType.Hue;
                    makeValuesBox(0, 360);
                    colorUnit.setText("0-360");
                    setBox.getChildren().addAll(valueBox, opBox);
                    valueBox.getChildren().addAll(colorLabel, valueSelector, colorUnit);
                    opBox.getChildren().addAll(colorSetButton, colorIncreaseButton, colorDecreaseButton);

                } else if (colorRedRadio.isSelected()) {
                    colorOperationType = OperationType.Red;
                    makeValuesBox(0, 255);
                    setBox.getChildren().addAll(valueBox, opBox);
                    valueBox.getChildren().addAll(colorLabel, valueSelector, colorUnit);
                    opBox.getChildren().addAll(colorSetButton, colorIncreaseButton, colorDecreaseButton, colorFilterButton, colorInvertButton);

                } else if (colorGreenRadio.isSelected()) {
                    colorOperationType = OperationType.Green;
                    makeValuesBox(0, 255);
                    setBox.getChildren().addAll(valueBox, opBox);
                    valueBox.getChildren().addAll(colorLabel, valueSelector, colorUnit);
                    opBox.getChildren().addAll(colorSetButton, colorIncreaseButton, colorDecreaseButton, colorFilterButton, colorInvertButton);

                } else if (colorBlueRadio.isSelected()) {
                    colorOperationType = OperationType.Blue;
                    makeValuesBox(0, 255);
                    setBox.getChildren().addAll(valueBox, opBox);
                    valueBox.getChildren().addAll(colorLabel, valueSelector, colorUnit);
                    opBox.getChildren().addAll(colorSetButton, colorIncreaseButton, colorDecreaseButton, colorFilterButton, colorInvertButton);

                } else if (colorYellowRadio.isSelected()) {
                    colorOperationType = OperationType.Yellow;
                    makeValuesBox(0, 255);
                    setBox.getChildren().addAll(valueBox, opBox);
                    valueBox.getChildren().addAll(colorLabel, valueSelector, colorUnit);
                    opBox.getChildren().addAll(colorSetButton, colorIncreaseButton, colorDecreaseButton, colorFilterButton, colorInvertButton);

                } else if (colorCyanRadio.isSelected()) {
                    colorOperationType = OperationType.Cyan;
                    makeValuesBox(0, 255);
                    setBox.getChildren().addAll(valueBox, opBox);
                    valueBox.getChildren().addAll(colorLabel, valueSelector, colorUnit);
                    opBox.getChildren().addAll(colorSetButton, colorIncreaseButton, colorDecreaseButton, colorFilterButton, colorInvertButton);

                } else if (colorMagentaRadio.isSelected()) {
                    colorOperationType = OperationType.Magenta;
                    makeValuesBox(0, 255);
                    setBox.getChildren().addAll(valueBox, opBox);
                    valueBox.getChildren().addAll(colorLabel, valueSelector, colorUnit);
                    opBox.getChildren().addAll(colorSetButton, colorIncreaseButton, colorDecreaseButton, colorFilterButton, colorInvertButton);

                } else if (colorOpacityRadio.isSelected()) {
                    colorOperationType = OperationType.Opacity;
                    makeValuesBox(0, 255);
                    setBox.getChildren().addAll(valueBox, opBox);
                    valueBox.getChildren().addAll(colorLabel, valueSelector, colorUnit);
                    opBox.getChildren().addAll(colorSetButton, colorIncreaseButton, colorDecreaseButton);

                }

                setBox.getChildren().addAll(demoButton);
            }

            refreshStyle(setBox);

        } catch (Exception e) {
            MyBoxLog.error(e);
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
            MyBoxLog.error(e);
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
    public void paneClicked(MouseEvent event, DoublePoint p) {
        if (editor.isPickingColor) {
            Color color = ImageViewTools.imagePixel(p, imageView);
            if (color != null) {
                if (colorReplaceRadio.isSelected()) {
                    originalColorSetController.setColor(color);
                } else {
                    newColorSetController.setColor(color);
                    valueColorSetController.setColor(color);
                }
            }
        }
    }

    @FXML
    @Override
    public void goAction() {
        colorActionType = ColorActionType.Set;
        applyChange();
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
        if (editor == null || colorOperationType == null || colorActionType == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

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
                            .setSkipTransparent(originalColor.getRGB() != 0 && ignoreTransparentCheck.isSelected())
                            .setBoolPara1(hueCheck.isSelected())
                            .setBoolPara2(saturationCheck.isSelected())
                            .setBoolPara3(brightnessCheck.isSelected());

                } else {
                    pixelsOperation = PixelsOperationFactory.create(imageView.getImage(),
                            scopeController.scope, colorOperationType, colorActionType)
                            .setSkipTransparent(ignoreTransparentCheck.isSelected());
                    switch (colorOperationType) {
                        case Color:
                            pixelsOperation.setColorPara1(newColorSetController.awtColor())
                                    .setBoolPara1(hueCheck.isSelected())
                                    .setBoolPara2(saturationCheck.isSelected())
                                    .setBoolPara3(brightnessCheck.isSelected());
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
                editor.popSuccessful();
                editor.updateImage(ImageOperation.Color,
                        colorOperationType.name(), colorActionType.name(), newImage, cost);
            }
        };
        start(task);
    }

    @FXML
    protected void demo() {
        if (imageView.getImage() == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {
            private List<String> files;

            @Override
            protected boolean handle() {
                try {
                    files = new ArrayList<>();
                    BufferedImage image = SwingFXUtils.fromFXImage(imageView.getImage(), null);
                    image = ScaleTools.scaleImageLess(image, 1000000);

                    PixelsOperation pixelsOperation;
                    BufferedImage bufferedImage;
                    String tmpFile;

                    BufferedImage outlineSource = SwingFXUtils.fromFXImage(
                            new Image("img/cover" + AppValues.AppYear + "g5.png"), null);
                    ImageScope scope = new ImageScope(SwingFXUtils.toFXImage(image, null));
                    scope.setScopeType(ImageScope.ScopeType.Outline);
                    if (sourceFile != null) {
                        scope.setFile(sourceFile.getAbsolutePath());
                    }
                    scope.setRectangle(DoubleRectangle.image(image));
                    BufferedImage[] outline = AlphaTools.outline(image, outlineSource, scope.getRectangle());
                    scope.setOutlineSource(outlineSource);
                    scope.setOutline(outline[1]);

                    pixelsOperation = PixelsOperationFactory.create(image,
                            scope, OperationType.Color, ColorActionType.Filter);
                    pixelsOperation.setColorPara1(ColorConvertTools.converColor(Color.LIGHTPINK));
                    pixelsOperation.setFloatPara1(0.5f);
                    bufferedImage = pixelsOperation.operate();
                    tmpFile = FileTmpTools.generateFile(message("Color") + "_" + message("Filter"), "png")
                            .getAbsolutePath();
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                        task.setInfo(tmpFile);
                    }

                    pixelsOperation = PixelsOperationFactory.create(image,
                            scope, OperationType.Brightness, ColorActionType.Increase);
                    pixelsOperation.setFloatPara1(0.5f);
                    bufferedImage = pixelsOperation.operate();
                    tmpFile = FileTmpTools.generateFile(message("Brightness") + "_" + message("Increase"), "png")
                            .getAbsolutePath();
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                        task.setInfo(tmpFile);
                    }

                    pixelsOperation = PixelsOperationFactory.create(image,
                            scope, OperationType.Hue, ColorActionType.Decrease);
                    pixelsOperation.setFloatPara1(0.3f);
                    bufferedImage = pixelsOperation.operate();
                    tmpFile = FileTmpTools.generateFile(message("Hue") + "_" + message("Decrease"), "png")
                            .getAbsolutePath();
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                        task.setInfo(tmpFile);
                    }

                    pixelsOperation = PixelsOperationFactory.create(image,
                            scope, OperationType.Saturation, ColorActionType.Increase);
                    pixelsOperation.setFloatPara1(0.5f);
                    bufferedImage = pixelsOperation.operate();
                    tmpFile = FileTmpTools.generateFile(message("Saturation") + "_" + message("Increase"), "png")
                            .getAbsolutePath();
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                        task.setInfo(tmpFile);
                    }

                    pixelsOperation = PixelsOperationFactory.create(image,
                            scope, OperationType.Opacity, ColorActionType.Decrease);
                    pixelsOperation.setIntPara1(128);
                    bufferedImage = pixelsOperation.operate();
                    tmpFile = FileTmpTools.generateFile(message("Opacity") + "_" + message("Decrease"), "png")
                            .getAbsolutePath();
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                        task.setInfo(tmpFile);
                    }

                    pixelsOperation = PixelsOperationFactory.create(image,
                            scope, OperationType.RGB, ColorActionType.Invert);
                    bufferedImage = pixelsOperation.operate();
                    tmpFile = FileTmpTools.generateFile(message("RGB") + "_" + message("Invert"), "png")
                            .getAbsolutePath();
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                        task.setInfo(tmpFile);
                    }

                    pixelsOperation = PixelsOperationFactory.create(image,
                            scope, OperationType.Red, ColorActionType.Filter);
                    bufferedImage = pixelsOperation.operate();
                    tmpFile = FileTmpTools.generateFile(message("Red") + "_" + message("Filter"), "png")
                            .getAbsolutePath();
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                        task.setInfo(tmpFile);
                    }

                    pixelsOperation = PixelsOperationFactory.create(image,
                            scope, OperationType.Yellow, ColorActionType.Increase);
                    pixelsOperation.setIntPara1(60);
                    bufferedImage = pixelsOperation.operate();
                    tmpFile = FileTmpTools.generateFile(message("Yellow") + "_" + message("Increase"), "png")
                            .getAbsolutePath();
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                        task.setInfo(tmpFile);
                    }

                    pixelsOperation = PixelsOperationFactory.create(image,
                            scope, OperationType.Magenta, ColorActionType.Decrease);
                    pixelsOperation.setIntPara1(60);
                    bufferedImage = pixelsOperation.operate();
                    tmpFile = FileTmpTools.generateFile(message("Magenta") + "_" + message("Decrease"), "png")
                            .getAbsolutePath();
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                        task.setInfo(tmpFile);
                    }

                    return !files.isEmpty();
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                if (files != null && !files.isEmpty()) {
                    ImagesBrowserController b
                            = (ImagesBrowserController) WindowTools.openStage(Fxmls.ImagesBrowserFxml);
                    b.loadFiles(files);
                }
            }

        };
        start(task);
    }

    @Override
    public boolean controlAlt1() {
        if (opBox.getChildren().contains(colorSetButton) && !colorSetButton.isDisabled()) {
            setAction();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean controlAlt2() {
        if (opBox.getChildren().contains(colorIncreaseButton) && !colorIncreaseButton.isDisabled()) {
            increaseAction();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean controlAlt3() {
        if (opBox.getChildren().contains(colorDecreaseButton) && !colorDecreaseButton.isDisabled()) {
            decreaseAction();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean controlAlt4() {
        if (opBox.getChildren().contains(colorFilterButton) && !colorFilterButton.isDisabled()) {
            filterAction();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean controlAlt5() {
        if (opBox.getChildren().contains(colorInvertButton) && !colorInvertButton.isDisabled()) {
            invertAction();
            return true;
        } else {
            return false;
        }
    }

}
