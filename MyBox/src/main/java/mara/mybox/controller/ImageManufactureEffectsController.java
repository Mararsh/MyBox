package mara.mybox.controller;

import java.util.Arrays;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.image.ImageConvertTools.Direction;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.image.FxmlImageTools;
import mara.mybox.tools.FxmlTools;
import static mara.mybox.tools.FxmlTools.badStyle;

/**
 * @Author Mara
 * @CreateDate 2018-10-11
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureEffectsController extends ImageManufactureController {

    protected int effectType, intValue, direction;
    protected int threadholding, threadholdingSmall, threadholdingBig;

    @FXML
    protected ToggleGroup effectsGroup;
    @FXML
    protected ComboBox intBox, stringBox;
    @FXML
    protected TextField thresholdingInput, thresholdingMinInput, thresholdingMaxInput;
    @FXML
    protected RadioButton thresholdingRadio;
    @FXML
    protected Button effectsOkButton;
    @FXML
    protected HBox thresholdingBox;
    @FXML
    protected Label intLabel, stringLabel;
    @FXML
    protected CheckBox grayCheck;

    public static class EffectsOperationType {

        public static int Blur = 0;
        public static int Sharpen = 1;
        public static int Emboss = 2;
        public static int EdgeDetect = 3;
        public static int Thresholding = 4;
        public static int Posterizing = 5;

    }

    public ImageManufactureEffectsController() {
    }

    @Override
    protected void initializeNext2() {
        try {
            initCommon();
            initEffectsTab();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

//    @Override
//    protected void initInterface() {
//        try {
//            if (values == null || values.getImage() == null) {
//                return;
//            }
//            super.initInterface();
//
//            isSettingValues = true;
//
//            isSettingValues = false;
//        } catch (Exception e) {
//            logger.debug(e.toString());
//        }
//
//    }
    // Effects Methods
    protected void initEffectsTab() {
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
                    if (effectType == EffectsOperationType.Blur) {
                        defaultValue = 1;
                    } else if (effectType == EffectsOperationType.Posterizing) {
                        defaultValue = 32;
                    } else if (effectType == EffectsOperationType.Emboss) {
                        defaultValue = 3;
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
                        direction = Direction.Top;
                    } else if (getMessage("Bottom").equals(newValue)) {
                        direction = Direction.Bottom;
                    } else if (getMessage("Left").equals(newValue)) {
                        direction = Direction.Top;
                    } else if (getMessage("Right").equals(newValue)) {
                        direction = Direction.Right;
                    } else if (getMessage("LeftTop").equals(newValue)) {
                        direction = Direction.LeftTop;
                    } else if (getMessage("RightBottom").equals(newValue)) {
                        direction = Direction.RightBottom;
                    } else if (getMessage("LeftBottom").equals(newValue)) {
                        direction = Direction.LeftBottom;
                    } else if (getMessage("RightTop").equals(newValue)) {
                        direction = Direction.RightTop;
                    } else {
                        direction = Direction.Top;
                    }
                }
            });

            Tooltip tips = new Tooltip("0~255");
            tips.setFont(new Font(16));
            FxmlTools.quickTooltip(thresholdingInput, tips);
            FxmlTools.quickTooltip(thresholdingMinInput, tips);
            FxmlTools.quickTooltip(thresholdingMaxInput, tips);

            tips = new Tooltip(getMessage("ThresholdingComments"));
            tips.setFont(new Font(16));
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

            effectsOkButton.disableProperty().bind(
                    thresholdingInput.styleProperty().isEqualTo(badStyle)
                            .or(thresholdingMinInput.styleProperty().isEqualTo(badStyle))
                            .or(thresholdingMaxInput.styleProperty().isEqualTo(badStyle))
            );

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
            threadholdingSmall = Integer.valueOf(thresholdingMinInput.getText());
            if (threadholdingSmall >= 0 && threadholdingSmall <= 255) {
                thresholdingMinInput.setStyle(null);
            } else {
                thresholdingMinInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            thresholdingMinInput.setStyle(badStyle);
        }
        try {
            threadholdingBig = Integer.valueOf(thresholdingMaxInput.getText());
            if (threadholdingBig >= 0 && threadholdingBig <= 255) {
                thresholdingMaxInput.setStyle(null);
            } else {
                thresholdingMaxInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            thresholdingMaxInput.setStyle(badStyle);
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
            direction = Direction.Top;
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
        }
    }

    @FXML
    public void effectsAction() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage;
                if (effectType == EffectsOperationType.Blur) {
                    newImage = FxmlImageTools.blurImage(values.getCurrentImage(), intValue);
                } else if (effectType == EffectsOperationType.Sharpen) {
                    newImage = FxmlImageTools.sharpenImage(values.getCurrentImage());
                } else if (effectType == EffectsOperationType.EdgeDetect) {
                    newImage = FxmlImageTools.edgeDetectImage(values.getCurrentImage());
                } else if (effectType == EffectsOperationType.Emboss) {
                    newImage = FxmlImageTools.embossImage(values.getCurrentImage(), direction, intValue, grayCheck.isSelected());
                } else if (effectType == EffectsOperationType.Thresholding) {
                    newImage = FxmlImageTools.thresholdingImage(values.getCurrentImage(), threadholding, threadholdingSmall, threadholdingBig);
                } else if (effectType == EffectsOperationType.Posterizing) {
                    newImage = FxmlImageTools.posterizingImage(values.getCurrentImage(), intValue);
                } else {
                    return null;
                }
                recordImageHistory(ImageOperationType.Effects, newImage);
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
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

}
