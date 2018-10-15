package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.controller.ImageManufactureEffectsController.EffectsOperationType;
import mara.mybox.image.ImageConvertTools;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.tools.FxmlTools;
import static mara.mybox.tools.FxmlTools.badStyle;

/**
 * @Author Mara
 * @CreateDate 2018-9-23
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureBatchEffectsController extends ImageManufactureBatchController {

    protected int threadholding, threadholdingMin, threadholdingMax, effectType, blurRadius, posterizingSize;

    @FXML
    protected ToggleGroup effectsGroup;
    @FXML
    protected HBox blurBox, thresholdingBox;
    @FXML
    protected ComboBox blurRadiusBox, posterizingBox;
    @FXML
    protected TextField thresholdingInput, thresholdingMinInput, thresholdingMaxInput;
    @FXML
    protected RadioButton thresholdingRadio;

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
                    checkEffetcssOperationType();
                }
            });
            checkEffetcssOperationType();

            blurRadiusBox.getItems().addAll(Arrays.asList("10", "5", "3", "8", "15", "20", "30"));
            blurRadiusBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        blurRadius = Integer.valueOf(newValue);
                        if (blurRadius > 0) {
                            blurRadiusBox.getEditor().setStyle(null);

                        } else {
                            blurRadius = 1;
                            blurRadiusBox.getEditor().setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        blurRadius = 1;
                        blurRadiusBox.getEditor().setStyle(badStyle);
                    }
                }
            });
            blurRadiusBox.getSelectionModel().select(0);

            posterizingBox.getItems().addAll(Arrays.asList("64", "32", "128", "16"));
            posterizingBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        posterizingSize = Integer.valueOf(newValue);
                        if (posterizingSize > 0) {
                            posterizingBox.getEditor().setStyle(null);
                        } else {
                            posterizingSize = 32;
                            posterizingBox.getEditor().setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        posterizingSize = 32;
                        posterizingBox.getEditor().setStyle(badStyle);
                    }
                }
            });
            posterizingBox.getSelectionModel().select(0);

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

        } catch (Exception e) {
            logger.error(e.toString());
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

    private void checkEffetcssOperationType() {
        blurBox.setDisable(true);
        thresholdingBox.setDisable(true);
        posterizingBox.setDisable(true);
        thresholdingInput.setStyle(null);
        thresholdingMinInput.setStyle(null);
        thresholdingMaxInput.setStyle(null);
        RadioButton selected = (RadioButton) effectsGroup.getSelectedToggle();
        if (getMessage("Blur").equals(selected.getText())) {
            effectType = EffectsOperationType.Blur;
            blurBox.setDisable(false);
        } else if (getMessage("Sharpen").equals(selected.getText())) {
            effectType = EffectsOperationType.Sharpen;
        } else if (getMessage("EdgeDetection").equals(selected.getText())) {
            effectType = EffectsOperationType.EdgeDetect;
        } else if (getMessage("Posterizing").equals(selected.getText())) {
            effectType = EffectsOperationType.Posterizing;
            posterizingBox.setDisable(false);
        } else if (getMessage("Thresholding").equals(selected.getText())) {
            effectType = EffectsOperationType.Thresholding;
            thresholdingBox.setDisable(false);
            checkThresholding();
        }
    }

    @Override
    protected BufferedImage handleImage(BufferedImage source) {
        try {
            BufferedImage target = null;
            if (effectType == EffectsOperationType.Blur) {
                target = ImageConvertTools.blurImage(source, blurRadius);

            } else if (effectType == EffectsOperationType.Sharpen) {
                target = ImageConvertTools.sharpenImage(source);

            } else if (effectType == EffectsOperationType.EdgeDetect) {
                target = ImageConvertTools.edgeDetect(ImageConvertTools.clearAlpha(source));

            } else if (effectType == EffectsOperationType.Thresholding) {
                target = ImageConvertTools.thresholding(ImageConvertTools.removeAlpha(source), threadholding, threadholdingMin, threadholdingMax);

            } else if (effectType == EffectsOperationType.Posterizing) {
                target = ImageConvertTools.posterizing(ImageConvertTools.removeAlpha(source), posterizingSize);
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }

    }

}
