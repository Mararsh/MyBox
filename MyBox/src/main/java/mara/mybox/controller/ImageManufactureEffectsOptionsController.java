package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ControlStyle;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.image.ImageBinary;
import mara.mybox.image.ImageManufacture.Direction;
import mara.mybox.image.ImageQuantization.QuantizationAlgorithm;
import mara.mybox.image.ImageScope;
import mara.mybox.image.PixelsOperation.OperationType;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2019-9-2
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureEffectsOptionsController extends ImageManufactureOperationController {

    protected OperationType effectType;
    protected int intPara1, intPara2, intPara3, regionSize, weight1, weight2, weight3,
            quanColors, kmeansLoop;
    protected ConvolutionKernel kernel;
    protected QuantizationAlgorithm quantizationAlgorithm;
    protected ChangeListener<String> intBoxListener, stringBoxListener, intInputListener,
            intInput2Listener, intInput3Listener;
    protected ChangeListener<Number> numberBoxListener;
    protected ImageView calculatorView;
    protected Button paletteAddButton, htmlButton;

    @FXML
    protected ToggleGroup effectGroup, bwGroup, quanGroup;
    @FXML
    protected VBox setBox, bwBox, quanBox, edgeBox;
    @FXML
    protected RadioButton PosterizingRadio, ThresholdingRadio, GrayRadio,
            SepiaRadio, BlackOrWhiteRadio, EdgeDetectionRadio, EmbossRadio,
            effectMosaicRadio, effectFrostedRadio, otsuRadio,
            rgbQuanRadio, hsbQuanRadio, popularQuanRadio, kmeansQuanRadio,
            eightLaplaceRadio, eightLaplaceExcludedRadio, fourLaplaceRadio, fourLaplaceExcludedRadio;
    @FXML
    protected TextField intInput, intInput2, intInput3;
    @FXML
    protected FlowPane stringBoxPane, intBoxPane, intInputPane,
            intInputPane2, intInputPane3, othersPane;
    @FXML
    protected HBox regionBox, loopBox;
    @FXML
    protected ComboBox<String> intBox, stringBox, weightSelector, quanColorsSelector,
            regionSizeSelector, kmeansLoopSelector;
    @FXML
    protected CheckBox valueCheck, quanDitherCheck, quanDataCheck, ceilCheck;
    @FXML
    protected Label intBoxLabel, intLabel, intLabel2, intLabel3, stringLabel,
            actualLoopLabel, weightLabel;
    @FXML
    protected Button button;
    @FXML
    protected ImageView bitDepthTipsView, quanTipsView;

    @Override
    public void initControls() {
        try {
            super.initControls();

            effectGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkEffectType();
                }
            });

            bwGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov, Toggle oldv, Toggle newv) {
                    String selected = ((RadioButton) newv).getText();
                    if (message("OTSU").equals(selected)) {
                        intPara1 = 1;
                    } else if (message("Default").equals(selected)) {
                        intPara1 = 2;
                    } else if (message("Threshold").equals(selected)) {
                        intPara1 = 3;
                    }
                    intInput.setDisable(intPara1 != 3);
                    button.setDisable(intPara1 != 3);
                }
            });

            calculatorView = new ImageView();

            initPosterizing();

            checkEffectType();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setValues(BaseController parent) {
        parentController = parent;
        if (parentController == null) {
            return;
        }
        if (parentController instanceof ImageManufactureEffectsController) {
            ImageManufactureEffectsController pController = (ImageManufactureEffectsController) parentController;
            imageController = pController.imageController;
            scopeController = pController.scopeController;
            okButton = pController.okButton;
            imageView = pController.imageView;
            paletteAddButton = pController.paletteAddButton;
            htmlButton = pController.htmlButton;
        }
    }

    protected void checkEffectType() {
        try {
            if (imageController != null) {
                imageController.resetImagePane();
                if (scopeController != null && scopeController.scope != null
                        && scopeController.scope.getScopeType() != ImageScope.ScopeType.All) {
                    imageController.hideImagePane();
                    imageController.showScopePane();
                } else {
                    imageController.hideScopePane();
                    imageController.showImagePane();
                }
            }

            clearValues();
            if (okButton != null && effectGroup.getSelectedToggle() == null) {
                okButton.setDisable(true);
                return;
            }
            RadioButton selected = (RadioButton) effectGroup.getSelectedToggle();
            if (EdgeDetectionRadio.equals(selected)) {
                effectType = OperationType.EdgeDetect;
                makeEdgeBox();

            } else if (EmbossRadio.equals(selected)) {
                effectType = OperationType.Emboss;
                makeEmbossBox();

            } else if (PosterizingRadio.equals(selected)) {
                effectType = OperationType.Quantization;
                makePosterizingBox();

            } else if (ThresholdingRadio.equals(selected)) {
                effectType = OperationType.Thresholding;
                makeThresholdingBox();

            } else if (GrayRadio.equals(selected)) {
                effectType = OperationType.Gray;

            } else if (BlackOrWhiteRadio.equals(selected)) {
                effectType = OperationType.BlackOrWhite;
                makeBlackWhiteBox();

            } else if (SepiaRadio.equals(selected)) {
                effectType = OperationType.Sepia;
                makeSepiaBox();

            } else if (effectMosaicRadio.equals(selected)) {
                effectType = OperationType.Mosaic;
                makeMosaicBox();

            } else if (effectFrostedRadio.equals(selected)) {
                effectType = OperationType.FrostedGlass;
                makeMosaicBox();

            }

            FxmlControl.refreshStyle(setBox);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initPosterizing() {
        try {
            FxmlControl.setTooltip(quanTipsView, new Tooltip(message("QuantizationComments")));

            quanGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov, Toggle oldv, Toggle newv) {
                    checkPosterizingAlgorithm();
                }
            });

            quanColors = AppVariables.getUserConfigInt(baseName + "QuanColorsNumber", 256);
            quanColors = regionSize <= 0 ? 256 : quanColors;
            quanColorsSelector.getItems().addAll(Arrays.asList(
                    "27", "64", "8", "16", "256", "512", "1024", "2048", "4096", "216", "343", "128", "1000", "729", "1728", "8000"));
            quanColorsSelector.setValue(quanColors + "");
            quanColorsSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            quanColors = v;
                            AppVariables.setUserConfigInt(baseName + "QuanColorsNumber", quanColors);
                            FxmlControl.setEditorNormal(intBox);
                        } else {
                            FxmlControl.setEditorBadStyle(intBox);
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(intBox);
                    }
                }
            });

            regionSize = AppVariables.getUserConfigInt(baseName + "RegionSize", 256);
            regionSize = regionSize <= 0 ? 256 : regionSize;
            regionSizeSelector.getItems().addAll(Arrays.asList("256", "1024", "64", "512", "1024", "4096", "128"));
            regionSizeSelector.setValue(regionSize + "");
            regionSizeSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            regionSize = v;
                            AppVariables.setUserConfigInt(baseName + "RegionSize", regionSize);
                            regionSizeSelector.getEditor().setStyle(null);
                        } else {
                            regionSizeSelector.getEditor().setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        regionSizeSelector.getEditor().setStyle(badStyle);
                    }
                }
            });

            weightSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        if (isSettingValues) {
                            return;
                        }
                        String[] values = newValue.split(":");
                        int v1 = Integer.parseInt(values[0]);
                        int v2 = Integer.parseInt(values[1]);
                        int v3 = Integer.parseInt(values[2]);
                        if (v1 <= 0 || v2 <= 0 || v3 <= 0) {
                            weightSelector.getEditor().setStyle(badStyle);
                            return;
                        }
                        weight1 = v1;
                        weight2 = v2;
                        weight3 = v3;
                        weightSelector.getEditor().setStyle(null);
                        AppVariables.setUserConfigValue(baseName + (hsbQuanRadio.isSelected() ? "HSBWeights" : "RGBWeights"), newValue);
                    } catch (Exception e) {
                        weightSelector.getEditor().setStyle(badStyle);
                    }
                }
            });

            kmeansLoop = AppVariables.getUserConfigInt(baseName + "KmeansLoop", 10000);
            kmeansLoop = kmeansLoop <= 0 ? 10000 : kmeansLoop;
            kmeansLoopSelector.getItems().addAll(Arrays.asList(
                    "10000", "5000", "3000", "1000", "500", "100", "20000"));
            kmeansLoopSelector.setValue(kmeansLoop + "");
            kmeansLoopSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            kmeansLoop = v;
                            AppVariables.setUserConfigInt(baseName + "KmeansLoop", kmeansLoop);
                            FxmlControl.setEditorNormal(kmeansLoopSelector);
                        } else {
                            FxmlControl.setEditorBadStyle(kmeansLoopSelector);
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(kmeansLoopSelector);
                    }
                }
            });

            quanDitherCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "QuanDither", true));
            quanDitherCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue(baseName + "QuanDither", newValue);
                }
            });

            quanDataCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "QuanData", true));
            quanDataCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue(baseName + "QuanData", newValue);
                }
            });

            ceilCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "QuanCeil", true));
            ceilCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue(baseName + "QuanCeil", newValue);
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void checkPosterizingAlgorithm() {
        String selected = ((RadioButton) quanGroup.getSelectedToggle()).getText();
        for (QuantizationAlgorithm algorithm : QuantizationAlgorithm.values()) {
            if (message(algorithm.name()).equals(selected)) {
                quantizationAlgorithm = algorithm;
                break;
            }
        }
        if (paletteAddButton != null) {
            paletteAddButton.setVisible(false);
        }
        if (htmlButton != null) {
            htmlButton.setVisible(false);
        }
        switch (quantizationAlgorithm) {
            case HSBUniformQuantization:
                if (quanBox.getChildren().contains(regionBox)) {
                    quanBox.getChildren().removeAll(regionBox, loopBox, actualLoopLabel);
                }
                weightLabel.setText(message("HSBWeight"));
                break;
            case RGBUniformQuantization:
                if (quanBox.getChildren().contains(regionBox)) {
                    quanBox.getChildren().removeAll(regionBox, loopBox, actualLoopLabel);
                }
                weightLabel.setText(message("RGBWeight"));
                break;
            case KMeansClustering:
                if (!quanBox.getChildren().contains(regionBox)) {
                    quanBox.getChildren().add(7, regionBox);
                }
                if (!quanBox.getChildren().contains(loopBox)) {
                    quanBox.getChildren().add(8, loopBox);
                    quanBox.getChildren().add(actualLoopLabel);
                }
                actualLoopLabel.setText("");
                weightLabel.setText(message("RGBWeight"));
                break;
            case PopularityQuantization:
                if (!quanBox.getChildren().contains(regionBox)) {
                    quanBox.getChildren().add(7, regionBox);
                }
                if (quanBox.getChildren().contains(loopBox)) {
                    quanBox.getChildren().removeAll(loopBox, actualLoopLabel);
                }
                weightLabel.setText(message("RGBWeight"));
        }
        isSettingValues = true;
        weightSelector.getItems().clear();
        if (hsbQuanRadio.isSelected()) {
            weight1 = 4;
            weight2 = 4;
            weight3 = 1;
            String defaultV = AppVariables.getUserConfigValue(baseName + "HSBWeights", "4:4:1");
            weightSelector.getItems().addAll(Arrays.asList(
                    "4:4:1", "4:4:2", "1:1:1", "5:3:2", "2:2:1", "4:2:1"
            ));
            weightSelector.setValue(defaultV);
        } else {
            weight1 = 2;
            weight2 = 4;
            weight3 = 4;
            String defaultV = AppVariables.getUserConfigValue(baseName + "RGBWeights", "2:4:3");
            weightSelector.getItems().addAll(Arrays.asList(
                    "2:4:3", "1:1:1", "4:4:2", "2:1:1", "21:71:7", "299:587:114", "2126:7152:722"
            ));
            weightSelector.setValue(defaultV);
        }

        isSettingValues = false;
    }

    protected void clearValues() {
        setBox.getChildren().clear();
        othersPane.getChildren().clear();
        if (stringBoxListener != null) {
            stringBox.getSelectionModel().selectedItemProperty().removeListener(stringBoxListener);
        }
        if (numberBoxListener != null) {
            stringBox.getSelectionModel().selectedIndexProperty().removeListener(numberBoxListener);
        }
        if (intBoxListener != null) {
            intBox.getSelectionModel().selectedItemProperty().removeListener(intBoxListener);
        }
        if (intInputListener != null) {
            intInput.textProperty().removeListener(intInputListener);
        }
        if (intInput2Listener != null) {
            intInput2.textProperty().removeListener(intInput2Listener);
        }
        if (intInput3Listener != null) {
            intInput3.textProperty().removeListener(intInput3Listener);
        }
        valueCheck.setDisable(false);
        button.setOnAction(null);
        button.disableProperty().unbind();
        button.setDisable(false);
        stringBox.getItems().clear();
        stringBox.getEditor().setStyle(null);
        intBox.getItems().clear();
        intBox.getEditor().setStyle(null);
        intInput.setStyle(null);
        intInput.setDisable(false);
        intInput2.setStyle(null);
        intInput3.setStyle(null);
        stringBox.setEditable(false);
        intBox.setEditable(false);
        actualLoopLabel.setText("");
        if (paletteAddButton != null) {
            paletteAddButton.setVisible(false);
        }
        if (htmlButton != null) {
            htmlButton.setVisible(false);
        }
        if (okButton != null) {
            okButton.disableProperty().unbind();
            okButton.setDisable(false);
        }
    }

    protected void makeEdgeBox() {
        try {
            valueCheck.setText(message("Gray"));
            valueCheck.setSelected(true);
            setBox.getChildren().addAll(edgeBox, valueCheck);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void makeEmbossBox() {
        try {
            intPara1 = Direction.Top;
            stringLabel.setText(message("Direction"));
            stringBoxListener = new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (newValue == null || newValue.trim().isEmpty()) {
                        return;
                    }
                    if (message("Top").equals(newValue)) {
                        intPara1 = Direction.Top;
                    } else if (message("Bottom").equals(newValue)) {
                        intPara1 = Direction.Bottom;
                    } else if (message("Left").equals(newValue)) {
                        intPara1 = Direction.Top;
                    } else if (message("Right").equals(newValue)) {
                        intPara1 = Direction.Right;
                    } else if (message("LeftTop").equals(newValue)) {
                        intPara1 = Direction.LeftTop;
                    } else if (message("RightBottom").equals(newValue)) {
                        intPara1 = Direction.RightBottom;
                    } else if (message("LeftBottom").equals(newValue)) {
                        intPara1 = Direction.LeftBottom;
                    } else if (message("RightTop").equals(newValue)) {
                        intPara1 = Direction.RightTop;
                    } else {
                        intPara1 = Direction.Top;
                    }
                }
            };
            stringBox.getSelectionModel().selectedItemProperty().addListener(stringBoxListener);
            stringBox.getItems().addAll(Arrays.asList(message("Top"), message("Bottom"),
                    message("Left"), message("Right"),
                    message("LeftTop"), message("RightBottom"),
                    message("LeftBottom"), message("RightTop")));
            stringBox.getSelectionModel().select(message("Top"));

            intPara2 = 3;
            intBoxLabel.setText(message("Radius"));
            intBoxListener = new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            intPara2 = v;
                            FxmlControl.setEditorNormal(intBox);
                        } else {
                            FxmlControl.setEditorBadStyle(intBox);
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(intBox);
                    }
                }
            };
            intBox.getSelectionModel().selectedItemProperty().addListener(intBoxListener);
            intBox.getItems().addAll(Arrays.asList("3", "5"));
            intBox.getSelectionModel().select(0);

            valueCheck.setText(message("Gray"));
            valueCheck.setSelected(true);

            setBox.getChildren().addAll(stringBoxPane, intBoxPane, valueCheck);
            if (okButton != null) {
                okButton.disableProperty().bind(
                        intBox.getEditor().styleProperty().isEqualTo(badStyle)
                                .or(stringBox.getEditor().styleProperty().isEqualTo(badStyle))
                );
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void makePosterizingBox() {
        try {
            setBox.getChildren().addAll(quanBox);
            if (okButton != null) {
                okButton.disableProperty().bind(
                        quanColorsSelector.getEditor().styleProperty().isEqualTo(badStyle));
            }

            checkPosterizingAlgorithm();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    protected void makeThresholdingBox() {
        try {
            intPara1 = 128;
            intLabel.setText(message("Threshold"));
            intInputListener = new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v >= 0 && v <= 255) {
                            intPara1 = v;
                            intInput.setStyle(null);
                        } else {
                            popError("0~255");
                            intInput.setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        popError("0~255");
                        intInput.setStyle(badStyle);
                    }
                }
            };
            intInput.textProperty().addListener(intInputListener);

            intInput.setText("128");
            FxmlControl.setTooltip(intInput, new Tooltip("0~255"));

            intPara2 = 0;
            intLabel2.setText(message("SmallValue"));
            intInput2Listener = new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v >= 0 && v <= 255) {
                            intPara2 = v;
                            intInput2.setStyle(null);
                        } else {
                            popError("0~255");
                            intInput2.setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        popError("0~255");
                        intInput2.setStyle(badStyle);
                    }
                }
            };
            intInput2.textProperty().addListener(intInput2Listener);
            intInput2.setText("0");
            FxmlControl.setTooltip(intInput2, new Tooltip("0~255"));

            intPara3 = 255;
            intLabel3.setText(message("BigValue"));
            intInput3Listener = new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v >= 0 && v <= 255) {
                            intPara3 = v;
                            intInput3.setStyle(null);
                        } else {
                            popError("0~255");
                            intInput3.setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        popError("0~255");
                        intInput3.setStyle(badStyle);
                    }
                }
            };
            intInput3.textProperty().addListener(intInput3Listener);
            intInput3.setText("255");
            FxmlControl.setTooltip(intInput3, new Tooltip("0~255"));

            FxmlControl.setTooltip(tipsView, new Tooltip(message("ThresholdingComments")));

            setBox.getChildren().addAll(intInputPane, intInputPane2, intInputPane3, tipsView);
            if (okButton != null) {
                okButton.disableProperty().bind(
                        intInput.styleProperty().isEqualTo(badStyle)
                                .or(intInput3.styleProperty().isEqualTo(badStyle))
                                .or(intInput2.styleProperty().isEqualTo(badStyle))
                );
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    protected void makeBlackWhiteBox() {
        try {
            intPara2 = 128;
            intLabel.setText("");
            intInputListener = new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    try {
                        if (newValue == null || newValue.trim().isEmpty()) {
                            intPara2 = -1;
                            intInput.setStyle(null);
                            return;
                        }
                        int v = Integer.valueOf(intInput.getText());
                        if (v >= 0 && v <= 255) {
                            intPara2 = v;
                            intInput.setStyle(null);
                        } else {
                            intInput.setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        intInput.setStyle(badStyle);
                    }
                }
            };
            intInput.textProperty().addListener(intInputListener);
            intInput.setText("128");
            FxmlControl.setTooltip(intInput, new Tooltip("0~255"));

            if (imageView != null) {
                calculatorView.setImage(new Image(ControlStyle.getIcon("iconCalculator.png")));
                calculatorView.setFitWidth(20);
                calculatorView.setFitHeight(20);
                button.setGraphic(calculatorView);
                button.setText("");
                FxmlControl.setTooltip(button, new Tooltip(message("Calculate")));
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        int scaleValue = ImageBinary.calculateThreshold(imageView.getImage());
                        intInput.setText(scaleValue + "");
                    }
                });
            }

            intPara1 = 1;
            otsuRadio.setSelected(true);

            valueCheck.setText(message("Dithering"));
            if (scopeController != null && scopeController.scope != null
                    && scopeController.scope.getScopeType() == ImageScope.ScopeType.Matting) {
                valueCheck.setSelected(false);
                valueCheck.setDisable(true);
            } else {
                valueCheck.setSelected(true);
                valueCheck.setDisable(false);
            }

            FxmlControl.setTooltip(tipsView, new Tooltip(message("BWThresholdComments")));

            if (imageView != null) {
                othersPane.getChildren().addAll(tipsView, button, valueCheck);
            } else {
                othersPane.getChildren().addAll(tipsView, valueCheck);
            }
            setBox.getChildren().addAll(bwBox, intInputPane, othersPane);
            if (okButton != null) {
                okButton.disableProperty().bind(
                        intInput.styleProperty().isEqualTo(badStyle)
                );
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    protected void makeSepiaBox() {
        try {
            intPara1 = 80;
            intBoxLabel.setText(message("Intensity"));
            intBoxListener = new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v >= 0 && v <= 255) {
                            intPara1 = v;
                            FxmlControl.setEditorNormal(intBox);
                            AppVariables.setUserConfigInt("ImageSepiaIntensity", v);
                        } else {
                            FxmlControl.setEditorBadStyle(intBox);
                            popError("0~255");
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(intBox);
                        popError("0~255");
                    }
                }
            };
            intBox.setEditable(true);
            intBox.getSelectionModel().selectedItemProperty().addListener(intBoxListener);
            intBox.getItems().addAll(Arrays.asList("80", "20", "50", "10", "5", "100", "15", "20", "60"));
            intBox.getSelectionModel().select(AppVariables.getUserConfigInt("ImageSepiaIntensity", 80) + "");

            setBox.getChildren().addAll(intBoxPane);
            if (okButton != null) {
                okButton.disableProperty().bind(
                        intBox.getEditor().styleProperty().isEqualTo(badStyle)
                );
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    protected void makeMosaicBox() {
        try {
            intPara1 = 80;
            intBoxLabel.setText(message("Intensity"));
            intBoxListener = new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            intPara1 = v;
                            FxmlControl.setEditorNormal(intBox);
                            AppVariables.setUserConfigInt("ImageMosaicIntensity", v);
                        } else {
                            FxmlControl.setEditorBadStyle(intBox);
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(intBox);
                    }
                }
            };
            intBox.setEditable(true);
            intBox.getSelectionModel().selectedItemProperty().addListener(intBoxListener);
            intBox.getItems().addAll(Arrays.asList("20", "50", "10", "5", "80", "100", "15", "20", "60"));
            intBox.getSelectionModel().select(AppVariables.getUserConfigInt("ImageMosaicIntensity", 20) + "");

            setBox.getChildren().addAll(intBoxPane);
            if (okButton != null) {
                okButton.disableProperty().bind(
                        intBox.getEditor().styleProperty().isEqualTo(badStyle)
                );
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

}
