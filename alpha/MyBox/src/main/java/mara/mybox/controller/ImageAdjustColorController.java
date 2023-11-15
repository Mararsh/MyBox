package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import mara.mybox.bufferedimage.ColorConvertTools;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.bufferedimage.PixelsOperation;
import mara.mybox.bufferedimage.PixelsOperation.ColorActionType;
import mara.mybox.bufferedimage.PixelsOperation.OperationType;
import static mara.mybox.bufferedimage.PixelsOperation.OperationType.Blue;
import static mara.mybox.bufferedimage.PixelsOperation.OperationType.Brightness;
import static mara.mybox.bufferedimage.PixelsOperation.OperationType.Cyan;
import static mara.mybox.bufferedimage.PixelsOperation.OperationType.Green;
import static mara.mybox.bufferedimage.PixelsOperation.OperationType.Hue;
import static mara.mybox.bufferedimage.PixelsOperation.OperationType.Magenta;
import static mara.mybox.bufferedimage.PixelsOperation.OperationType.Opacity;
import static mara.mybox.bufferedimage.PixelsOperation.OperationType.RGB;
import static mara.mybox.bufferedimage.PixelsOperation.OperationType.Red;
import static mara.mybox.bufferedimage.PixelsOperation.OperationType.Saturation;
import static mara.mybox.bufferedimage.PixelsOperation.OperationType.Yellow;
import mara.mybox.bufferedimage.PixelsOperationFactory;
import mara.mybox.bufferedimage.ScaleTools;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-9-1
 * @License Apache License Version 2.0
 */
public class ImageAdjustColorController extends BasePixelsController {

    private OperationType colorOperationType;
    private ColorActionType colorActionType;
    protected int colorValue, max, min;

    @FXML
    protected ToggleGroup colorGroup, opGroup;
    @FXML
    protected RadioButton colorBrightnessRadio, colorHueRadio, colorSaturationRadio,
            colorRedRadio, colorGreenRadio, colorBlueRadio, colorOpacityRadio,
            colorYellowRadio, colorCyanRadio, colorMagentaRadio, colorRGBRadio,
            setRadio, plusRadio, minusRadio, filterRadio, invertRadio;
    @FXML
    protected ComboBox<String> valueSelector;
    @FXML
    protected Slider valueSlider;
    @FXML
    protected Label colorUnit;
    @FXML
    protected Button demoButton;

    public ImageAdjustColorController() {
        baseTitle = message("AdjustColor");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();

            colorGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkColorType();
                }
            });

            valueSlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    valueSelector.setValue(newValue.intValue() + "");
                }
            });

            checkColorType();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    private void checkColorType() {
        try {
            valueSelector.getItems().clear();
            ValidationTools.setEditorNormal(valueSelector);
            setRadio.setDisable(false);
            plusRadio.setDisable(false);
            minusRadio.setDisable(false);
            filterRadio.setDisable(false);
            invertRadio.setDisable(false);
            plusRadio.setSelected(true);

            if (colorRGBRadio.isSelected()) {
                colorOperationType = PixelsOperation.OperationType.RGB;
                makeValues(0, 255);
                setRadio.setDisable(true);
                filterRadio.setDisable(true);

            } else if (colorBrightnessRadio.isSelected()) {
                colorOperationType = PixelsOperation.OperationType.Brightness;
                makeValues(0, 100);
                filterRadio.setDisable(true);
                invertRadio.setDisable(true);
            } else if (colorSaturationRadio.isSelected()) {
                colorOperationType = PixelsOperation.OperationType.Saturation;
                makeValues(0, 100);
                filterRadio.setDisable(true);
                invertRadio.setDisable(true);

            } else if (colorHueRadio.isSelected()) {
                colorOperationType = PixelsOperation.OperationType.Hue;
                makeValues(0, 360);
                filterRadio.setDisable(true);
                invertRadio.setDisable(true);

            } else if (colorRedRadio.isSelected()) {
                colorOperationType = PixelsOperation.OperationType.Red;
                makeValues(0, 255);

            } else if (colorGreenRadio.isSelected()) {
                colorOperationType = PixelsOperation.OperationType.Green;
                makeValues(0, 255);

            } else if (colorBlueRadio.isSelected()) {
                colorOperationType = PixelsOperation.OperationType.Blue;
                makeValues(0, 255);

            } else if (colorYellowRadio.isSelected()) {
                colorOperationType = PixelsOperation.OperationType.Yellow;
                makeValues(0, 255);

            } else if (colorCyanRadio.isSelected()) {
                colorOperationType = PixelsOperation.OperationType.Cyan;
                makeValues(0, 255);

            } else if (colorMagentaRadio.isSelected()) {
                colorOperationType = PixelsOperation.OperationType.Magenta;
                makeValues(0, 255);

            } else if (colorOpacityRadio.isSelected()) {
                colorOperationType = PixelsOperation.OperationType.Opacity;
                makeValues(0, 255);
                filterRadio.setDisable(true);
                invertRadio.setDisable(true);

            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    private void makeValues(int minV, int maxV) {
        try {
            min = minV;
            max = maxV;

            colorUnit.setText(min + "-" + max);

            valueSlider.setMin(min);
            valueSlider.setMax(max);

            List<String> valueList = new ArrayList<>();
            int step = (max - min) / 10;
            for (int v = min; v < max; v += step) {
                valueList.add(v + "");
            }
            valueList.add(max + "");
            valueSelector.getItems().addAll(valueList);
            valueSelector.getSelectionModel().select(valueList.size() / 2);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected boolean checkOptions() {
        if (!super.checkOptions() || colorOperationType == null) {
            return false;
        }
        if (setRadio.isSelected()) {
            colorActionType = ColorActionType.Set;
        } else if (plusRadio.isSelected()) {
            colorActionType = ColorActionType.Increase;
        } else if (minusRadio.isSelected()) {
            colorActionType = ColorActionType.Decrease;
        } else if (filterRadio.isSelected()) {
            colorActionType = ColorActionType.Filter;
        } else if (invertRadio.isSelected()) {
            colorActionType = ColorActionType.Invert;
        } else {
            return false;
        }
        colorValue = max + 1;
        try {
            colorValue = Integer.parseInt(valueSelector.getValue());
        } catch (Exception e) {
        }
        if (colorValue >= min && colorValue <= max) {
            ValidationTools.setEditorNormal(valueSelector);
            return true;
        } else {
            ValidationTools.setEditorBadStyle(valueSelector);
            return false;
        }
    }

    @Override
    protected Image handleImage(Image inImage, ImageScope inScope) {
        try {
            operation = message("AdjustColor");
            opInfo = message(colorActionType.name()) + ": " + colorValue;
            PixelsOperation pixelsOperation = PixelsOperationFactory.create(
                    inImage, inScope,
                    colorOperationType, colorActionType)
                    .setExcludeScope(scopeExclude())
                    .setSkipTransparent(ignoreTransparent());
            switch (colorOperationType) {
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
            return pixelsOperation.operateFxImage();
        } catch (Exception e) {
            displayError(e.toString());
            return null;
        }
    }

    @FXML
    @Override
    protected void demo() {
        if (scopeController.srcImage() == null) {
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
                    BufferedImage image = SwingFXUtils.fromFXImage(scopeController.srcImage(), null);
                    image = ScaleTools.scaleImageLess(image, 1000000);

                    PixelsOperation pixelsOperation;
                    BufferedImage bufferedImage;
                    String tmpFile;

                    ImageScope scope = new ImageScope();
                    scope.setScopeType(ImageScope.ScopeType.Rectangle)
                            .setRectangle(DoubleRectangle.xywh(
                                    image.getWidth() / 8, image.getHeight() / 8,
                                    image.getWidth() * 3 / 4, image.getHeight() * 3 / 4));

                    pixelsOperation = PixelsOperationFactory.create(image,
                            scope, OperationType.Color, ColorActionType.Set);
                    pixelsOperation.setColorPara1(ColorConvertTools.converColor(Color.LIGHTPINK))
                            .setBoolPara1(true)
                            .setBoolPara2(false)
                            .setBoolPara3(false);
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


    /*
        static methods
     */
    public static ImageAdjustColorController open(BaseImageController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageAdjustColorController controller = (ImageAdjustColorController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.ImageAdjustColorFxml, false);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
