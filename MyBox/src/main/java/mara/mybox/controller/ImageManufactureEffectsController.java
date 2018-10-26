package mara.mybox.controller;

import java.util.Arrays;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.tools.FxmlImageTools;
import mara.mybox.tools.FxmlTools;
import static mara.mybox.tools.FxmlTools.badStyle;

/**
 * @Author Mara
 * @CreateDate 2018-10-11
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureEffectsController extends ImageManufactureController {

    protected int effectType, blurRadius, posterizingSize;
    protected int threadholding, threadholdingSmall, threadholdingBig;

    @FXML
    protected ToggleGroup effectsGroup;
    @FXML
    protected ComboBox blurRadiusBox, posterizingBox;
    @FXML
    protected TextField thresholdingInput, thresholdingMinInput, thresholdingMaxInput;
    @FXML
    protected RadioButton thresholdingRadio;
    @FXML
    protected Button effectsOkButton;
    @FXML
    protected HBox blurBox, thresholdingBox, postBox;

    public static class EffectsOperationType {

        public static int Blur = 0;
        public static int Sharpen = 1;
        public static int EdgeDetect = 2;
        public static int Thresholding = 3;
        public static int Posterizing = 4;

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

    private void checkEffetcssOperationType() {
        blurBox.setDisable(true);
        thresholdingBox.setDisable(true);
        postBox.setDisable(true);
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
            postBox.setDisable(false);
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
                    newImage = FxmlImageTools.blurImage(values.getCurrentImage(), blurRadius);
                } else if (effectType == EffectsOperationType.Sharpen) {
                    newImage = FxmlImageTools.sharpenImage(values.getCurrentImage());
                } else if (effectType == EffectsOperationType.EdgeDetect) {
                    newImage = FxmlImageTools.edgeDetectImage(values.getCurrentImage());
                } else if (effectType == EffectsOperationType.Thresholding) {
                    newImage = FxmlImageTools.thresholdingImage(values.getCurrentImage(), threadholding, threadholdingSmall, threadholdingBig);
                } else if (effectType == EffectsOperationType.Posterizing) {
                    newImage = FxmlImageTools.posterizingImage(values.getCurrentImage(), posterizingSize);
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
