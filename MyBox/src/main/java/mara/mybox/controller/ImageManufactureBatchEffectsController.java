package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.fxml.FxmlEffectTools.EffectsOperationType;
import mara.mybox.image.ImageConvertTools;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.fxml.FxmlTools;
import static mara.mybox.fxml.FxmlTools.badStyle;
import mara.mybox.image.ImageEffectTools;
import mara.mybox.image.ImageGrayTools;
import mara.mybox.objects.ConvolutionKernel;

/**
 * @Author Mara
 * @CreateDate 2018-9-23
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureBatchEffectsController extends ImageManufactureBatchController {

    private EffectsOperationType effectType;
    protected int threadholding, threadholdingMin, threadholdingMax, intValue, direction, bwValue;

    @FXML
    protected ToggleGroup effectsGroup;
    @FXML
    protected HBox thresholdingBox, bwBox;
    @FXML
    protected ComboBox intBox, stringBox;
    @FXML
    protected TextField thresholdingInput, thresholdingMinInput, thresholdingMaxInput, bwInput;
    @FXML
    protected RadioButton thresholdingRadio;
    @FXML
    protected Label intLabel, stringLabel, bwLabel1, bwLabel2, bwLabel3;
    @FXML
    protected CheckBox grayCheck;
    @FXML
    private Slider bwSlider;

    public ImageManufactureBatchEffectsController() {

    }

    @Override
    protected void initializeNext2() {
        try {

            operationBarController.startButton.disableProperty().bind(Bindings.isEmpty(targetPathInput.textProperty())
                    .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                    .or(Bindings.isEmpty(sourceFilesInformation))
                    .or(thresholdingInput.styleProperty().isEqualTo(badStyle))
                    .or(thresholdingMinInput.styleProperty().isEqualTo(badStyle))
                    .or(thresholdingMaxInput.styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @Override
    protected void initOptionsSection() {
        try {

            effectsGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkEffetcsOperationType();
                }
            });
            checkEffetcsOperationType();

            intBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    int defaultValue = 0;
                    if (null != effectType) {
                        switch (effectType) {
                            case Blur:
                                defaultValue = 1;
                                break;
                            case Posterizing:
                                defaultValue = 32;
                                break;
                            case Emboss:
                                defaultValue = 3;
                                break;
                            default:
                                break;
                        }
                    }
                    try {
                        String v = newValue;
                        int pos = v.indexOf(" ");
                        if (pos > 0) {
                            v = v.substring(0, pos);
                        }
                        intValue = Integer.valueOf(v);
                        if (intValue > 0) {
                            intBox.getEditor().setStyle(null);
                        } else {
                            intValue = defaultValue;
                            intBox.getEditor().setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        intValue = defaultValue;
                        intBox.getEditor().setStyle(badStyle);
                    }
                }
            });

            stringBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (newValue == null || newValue.trim().isEmpty()) {
                        return;
                    }
                    if (getMessage("Top").equals(newValue)) {
                        direction = ImageConvertTools.Direction.Top;
                    } else if (getMessage("Bottom").equals(newValue)) {
                        direction = ImageConvertTools.Direction.Bottom;
                    } else if (getMessage("Left").equals(newValue)) {
                        direction = ImageConvertTools.Direction.Top;
                    } else if (getMessage("Right").equals(newValue)) {
                        direction = ImageConvertTools.Direction.Right;
                    } else if (getMessage("LeftTop").equals(newValue)) {
                        direction = ImageConvertTools.Direction.LeftTop;
                    } else if (getMessage("RightBottom").equals(newValue)) {
                        direction = ImageConvertTools.Direction.RightBottom;
                    } else if (getMessage("LeftBottom").equals(newValue)) {
                        direction = ImageConvertTools.Direction.LeftBottom;
                    } else if (getMessage("RightTop").equals(newValue)) {
                        direction = ImageConvertTools.Direction.RightTop;
                    } else {
                        direction = ImageConvertTools.Direction.Top;
                    }
                }
            });

            Tooltip tips = new Tooltip("0~255");
//            tips.setFont(new Font(16));
            FxmlTools.quickTooltip(thresholdingInput, tips);
            FxmlTools.quickTooltip(thresholdingMinInput, tips);
            FxmlTools.quickTooltip(thresholdingMaxInput, tips);

            tips = new Tooltip(getMessage("ThresholdingComments"));
//            tips.setFont(new Font(16));
            FxmlTools.setComments(thresholdingBox, tips);
            FxmlTools.setComments(thresholdingRadio, tips);

            thresholdingInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkThresholding();
                }
            });
            thresholdingInput.setText("128");

            thresholdingMinInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkThresholding();
                }
            });
            thresholdingMinInput.setText("0");

            thresholdingMaxInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkThresholding();
                }
            });
            thresholdingMaxInput.setText("255");

            bwSlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    bwValue = newValue.intValue();
                    bwInput.setText(bwValue + "");
                }
            });

            bwInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkBwInput();
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkEffetcsOperationType() {
        intLabel.setText("");
        intBox.setDisable(true);
        stringLabel.setText("");
        stringBox.setDisable(true);
        thresholdingBox.setDisable(true);
        thresholdingInput.setStyle(null);
        thresholdingMinInput.setStyle(null);
        thresholdingMaxInput.setStyle(null);
        grayCheck.setDisable(true);
        bwBox.setDisable(true);
        bwInput.setStyle(null);
        RadioButton selected = (RadioButton) effectsGroup.getSelectedToggle();
        if (getMessage("Blur").equals(selected.getText())) {
            effectType = EffectsOperationType.Blur;
            intLabel.setText(getMessage("Radius"));
            intBox.setDisable(false);
            intBox.getItems().clear();
            intBox.getItems().addAll(Arrays.asList("10", "5", "3", "8", "15", "20", "30"));
            intBox.setEditable(true);
            intValue = 10;
            intBox.getSelectionModel().select("10");
        } else if (getMessage("Sharpen").equals(selected.getText())) {
            effectType = EffectsOperationType.Sharpen;
        } else if (getMessage("Clarity").equals(selected.getText())) {
            effectType = EffectsOperationType.Clarity;
        } else if (getMessage("EdgeDetection").equals(selected.getText())) {
            effectType = EffectsOperationType.EdgeDetect;
        } else if (getMessage("Emboss").equals(selected.getText())) {
            effectType = EffectsOperationType.Emboss;
            intLabel.setText(getMessage("Radius"));
            intBox.setDisable(false);
            intBox.getItems().clear();
            intBox.getItems().addAll(Arrays.asList("3", "5"));
            intBox.setEditable(false);
            intValue = 3;
            intBox.getSelectionModel().select("3");
            stringLabel.setText(getMessage("Direction"));
            stringBox.setDisable(false);
            stringBox.getItems().clear();
            stringBox.getItems().addAll(Arrays.asList(getMessage("Top"), getMessage("Bottom"),
                    getMessage("Left"), getMessage("Right"),
                    getMessage("LeftTop"), getMessage("RightBottom"),
                    getMessage("LeftBottom"), getMessage("RightTop")));
            direction = ImageConvertTools.Direction.Top;
            stringBox.getSelectionModel().select(getMessage("Top"));
            grayCheck.setDisable(false);
        } else if (getMessage("Posterizing").equals(selected.getText())) {
            effectType = EffectsOperationType.Posterizing;
            intLabel.setText(getMessage("Size"));
            intBox.setDisable(false);
            intBox.getItems().clear();
            intBox.getItems().addAll(Arrays.asList("64", "32", "128", "16"));
            intBox.setEditable(false);
            intBox.getSelectionModel().select("64");
        } else if (getMessage("Thresholding").equals(selected.getText())) {
            effectType = EffectsOperationType.Thresholding;
            thresholdingBox.setDisable(false);
            checkThresholding();
        } else if (getMessage("Gray").equals(selected.getText())) {
            effectType = EffectsOperationType.Gray;
        } else if (getMessage("BlackOrWhite").equals(selected.getText())) {
            effectType = EffectsOperationType.BlackOrWhite;
            bwBox.setDisable(false);
            bwLabel1.setText(getMessage("Threshold"));
            bwSlider.setMax(99);
            bwSlider.setMin(1);
            bwSlider.setBlockIncrement(1);
            bwSlider.setValue(50);
            bwLabel2.setText("%");
            bwLabel3.setVisible(true);
            checkBwInput();
        } else if (getMessage("Sepia").equals(selected.getText())) {
            effectType = EffectsOperationType.Sepia;
            bwBox.setDisable(false);
            bwLabel1.setText(getMessage("Intensity"));
            bwSlider.setMax(255);
            bwSlider.setMin(0);
            bwSlider.setBlockIncrement(1);
            bwSlider.setValue(80);
            bwLabel2.setText("");
            bwLabel3.setVisible(false);
            checkBwInput();
        }
    }

    private void checkThresholding() {
        try {
            threadholding = Integer.valueOf(thresholdingInput.getText());
            if (threadholding >= 0 && threadholding <= 255) {
                thresholdingInput.setStyle(null);
            } else {
                thresholdingInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            thresholdingInput.setStyle(badStyle);
        }
        try {
            threadholdingMin = Integer.valueOf(thresholdingMinInput.getText());
            if (threadholdingMin >= 0 && threadholdingMin <= 255) {
                thresholdingMinInput.setStyle(null);
            } else {
                thresholdingMinInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            thresholdingMinInput.setStyle(badStyle);
        }
        try {
            threadholdingMax = Integer.valueOf(thresholdingMaxInput.getText());
            if (threadholdingMax >= 0 && threadholdingMax <= 255) {
                thresholdingMaxInput.setStyle(null);
            } else {
                thresholdingMaxInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            thresholdingMaxInput.setStyle(badStyle);
        }
    }

    private void checkBwInput() {
        try {
            if (effectType == EffectsOperationType.BlackOrWhite
                    && bwInput.getText().trim().isEmpty()) {
                bwInput.setStyle(null);
                bwValue = -1;
                return;
            }
            bwValue = Integer.valueOf(bwInput.getText());
            if (bwValue >= 0 && bwValue <= bwSlider.getMax()) {
                bwInput.setStyle(null);
                bwSlider.setValue(bwValue);
            } else {
                bwValue = -1;
                bwInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            bwValue = -1;
            bwInput.setStyle(badStyle);
        }
    }

    @Override
    protected BufferedImage handleImage(BufferedImage source) {
        try {
            BufferedImage target = null;
            if (null != effectType) {
                switch (effectType) {
                    case Blur:
                        target = ImageEffectTools.applyConvolution(source, ConvolutionKernel.makeGaussKernel(intValue));
                        break;
                    case Sharpen:
                        target = ImageEffectTools.applyConvolution(source, ConvolutionKernel.makeSharpen3b());
                        break;
                    case Clarity:
                        target = ImageEffectTools.applyConvolution(source, ConvolutionKernel.makeUnsharpMasking5());
                        break;
                    case EdgeDetect:
                        target = ImageEffectTools.applyConvolution(source, ConvolutionKernel.makeEdgeDetection3b());
                        break;
                    case Emboss:
                        target = ImageEffectTools.applyConvolution(source, ConvolutionKernel.makeEmbossKernel(direction, intValue, grayCheck.isSelected()));
                        break;
                    case Thresholding:
                        target = ImageEffectTools.thresholding(ImageConvertTools.removeAlpha(source), threadholding, threadholdingMin, threadholdingMax);
                        break;
                    case Posterizing:
                        target = ImageEffectTools.posterizing(ImageConvertTools.removeAlpha(source), intValue);
                        break;
                    case Gray:
                        target = ImageGrayTools.color2Gray(source);
                        break;
                    case BlackOrWhite:
                        if (bwValue < 0) {
                            target = ImageGrayTools.color2Binary(source);
                        } else {
                            target = ImageGrayTools.color2BinaryWithPercentage(source, bwValue);
                        }
                        break;
                    case Sepia:
                        target = ImageEffectTools.sepiaImage(source, bwValue);
                        break;
                    default:
                        break;
                }
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }

    }

}
