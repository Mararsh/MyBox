package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mara.mybox.bufferedimage.BufferedImageTools.Direction;
import mara.mybox.bufferedimage.ImageQuantization.QuantizationAlgorithm;
import mara.mybox.bufferedimage.PixelsOperation.OperationType;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.style.NodeStyleTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

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
    protected Button paletteAddButton, htmlButton;

    @FXML
    protected ToggleGroup effectGroup, quanGroup;
    @FXML
    protected VBox setBox, binrayBox, quanBox, edgeBox;
    @FXML
    protected RadioButton PosterizingRadio, ThresholdingRadio, GrayRadio,
            SepiaRadio, BlackOrWhiteRadio, EdgeDetectionRadio, EmbossRadio,
            effectMosaicRadio, effectFrostedRadio,
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
    protected CheckBox valueCheck, quanDitherCheck, quanDataCheck, firstColorCheck;
    @FXML
    protected Label intBoxLabel, intLabel, intLabel2, intLabel3, stringLabel,
            actualLoopLabel, weightLabel;
    @FXML
    protected ControlImageBinary binaryController;
    @FXML
    protected ImageView imageQuantizationTipsView, imageThresholdTipsView;

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

            initPosterizing();

            checkEffectType();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setValues(BaseController parent) {
        parentController = parent;
        if (parentController == null) {
            return;
        }
        if (parentController instanceof ImageManufactureEffectsController) {
            ImageManufactureEffectsController pController = (ImageManufactureEffectsController) parentController;
            editor = pController.editor;
            scopeController = pController.scopeController;
            okButton = pController.okButton;
            imageView = pController.imageView;
            paletteAddButton = pController.paletteAddButton;
            htmlButton = pController.htmlButton;
            binaryController.setParameters(parentController, imageView);
        } else {
            binaryController.setParameters(parentController, null);
        }
    }

    protected void checkEffectType() {
        try {
            if (editor != null) {
                editor.resetImagePane();
                if (scopeController != null && !scopeController.scopeWhole()) {
                    editor.scopeTab();
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

            refreshStyle(setBox);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void initPosterizing() {
        try {
            quanGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov, Toggle oldv, Toggle newv) {
                    checkPosterizingAlgorithm();
                }
            });

            quanColors = UserConfig.getInt(baseName + "QuanColorsNumber", 256);
            quanColors = regionSize <= 0 ? 256 : quanColors;
            quanColorsSelector.getItems().addAll(Arrays.asList(
                    "27", "64", "8", "16", "256", "512", "1024", "2048", "4096", "216", "343", "128", "1000", "729", "1728", "8000"));
            quanColorsSelector.setValue(quanColors + "");
            quanColorsSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v > 0) {
                            quanColors = v;
                            UserConfig.setInt(baseName + "QuanColorsNumber", quanColors);
                            ValidationTools.setEditorNormal(intBox);
                        } else {
                            ValidationTools.setEditorBadStyle(intBox);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(intBox);
                    }
                }
            });

            regionSize = UserConfig.getInt(baseName + "RegionSize", 4096);
            regionSize = regionSize <= 0 ? 4096 : regionSize;
            regionSizeSelector.getItems().addAll(Arrays.asList("4096", "1024", "256", "8192", "512", "128"));
            regionSizeSelector.setValue(regionSize + "");
            regionSizeSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v > 0) {
                            regionSize = v;
                            UserConfig.setInt(baseName + "RegionSize", regionSize);
                            regionSizeSelector.getEditor().setStyle(null);
                        } else {
                            regionSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        regionSizeSelector.getEditor().setStyle(UserConfig.badStyle());
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
                            weightSelector.getEditor().setStyle(UserConfig.badStyle());
                            return;
                        }
                        weight1 = v1;
                        weight2 = v2;
                        weight3 = v3;
                        weightSelector.getEditor().setStyle(null);
                        UserConfig.setString(baseName + (hsbQuanRadio.isSelected() ? "HSBWeights" : "RGBWeights"), newValue);
                    } catch (Exception e) {
                        weightSelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                }
            });

            kmeansLoop = UserConfig.getInt(baseName + "KmeansLoop", 10000);
            kmeansLoop = kmeansLoop <= 0 ? 10000 : kmeansLoop;
            kmeansLoopSelector.getItems().addAll(Arrays.asList(
                    "10000", "5000", "3000", "1000", "500", "100", "20000"));
            kmeansLoopSelector.setValue(kmeansLoop + "");
            kmeansLoopSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v > 0) {
                            kmeansLoop = v;
                            UserConfig.setInt(baseName + "KmeansLoop", kmeansLoop);
                            ValidationTools.setEditorNormal(kmeansLoopSelector);
                        } else {
                            ValidationTools.setEditorBadStyle(kmeansLoopSelector);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(kmeansLoopSelector);
                    }
                }
            });

            quanDitherCheck.setSelected(UserConfig.getBoolean(baseName + "QuanDither", true));
            quanDitherCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "QuanDither", newValue);
                }
            });

            quanDataCheck.setSelected(UserConfig.getBoolean(baseName + "QuanData", true));
            quanDataCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "QuanData", newValue);
                }
            });

            firstColorCheck.setSelected(UserConfig.getBoolean(baseName + "QuanFirstColor", true));
            firstColorCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "QuanFirstColor", newValue);
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
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
            String defaultV = UserConfig.getString(baseName + "HSBWeights", "6:10:100");
            weightSelector.getItems().addAll(Arrays.asList(
                    "6:10:100", "12:4:10", "24:10:10", "12:10:40", "24:10:40", "12:20:40", "12:10:80", "6:10:80"
            ));
            weightSelector.setValue(defaultV);
        } else {
            weight1 = 2;
            weight2 = 4;
            weight3 = 4;
            String defaultV = UserConfig.getString(baseName + "RGBWeights", "2:4:3");
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
            MyBoxLog.error(e);
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
                        int v = Integer.parseInt(newValue);
                        if (v > 0) {
                            intPara2 = v;
                            ValidationTools.setEditorNormal(intBox);
                        } else {
                            ValidationTools.setEditorBadStyle(intBox);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(intBox);
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
                        intBox.getEditor().styleProperty().isEqualTo(UserConfig.badStyle())
                                .or(stringBox.getEditor().styleProperty().isEqualTo(UserConfig.badStyle()))
                );
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void makePosterizingBox() {
        try {
            setBox.getChildren().addAll(quanBox);
            if (okButton != null) {
                okButton.disableProperty().bind(
                        quanColorsSelector.getEditor().styleProperty().isEqualTo(UserConfig.badStyle()));
            }

            checkPosterizingAlgorithm();

        } catch (Exception e) {
            MyBoxLog.error(e);
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
                        int v = Integer.parseInt(newValue);
                        if (v >= 0 && v <= 255) {
                            intPara1 = v;
                            intInput.setStyle(null);
                        } else {
                            popError("0~255");
                            intInput.setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        popError("0~255");
                        intInput.setStyle(UserConfig.badStyle());
                    }
                }
            };
            intInput.textProperty().addListener(intInputListener);

            intInput.setText("128");
            NodeStyleTools.setTooltip(intInput, new Tooltip("0~255"));

            intPara2 = 0;
            intLabel2.setText(message("SmallValue"));
            intInput2Listener = new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v >= 0 && v <= 255) {
                            intPara2 = v;
                            intInput2.setStyle(null);
                        } else {
                            popError("0~255");
                            intInput2.setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        popError("0~255");
                        intInput2.setStyle(UserConfig.badStyle());
                    }
                }
            };
            intInput2.textProperty().addListener(intInput2Listener);
            intInput2.setText("0");
            NodeStyleTools.setTooltip(intInput2, new Tooltip("0~255"));

            intPara3 = 255;
            intLabel3.setText(message("BigValue"));
            intInput3Listener = new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v >= 0 && v <= 255) {
                            intPara3 = v;
                            intInput3.setStyle(null);
                        } else {
                            popError("0~255");
                            intInput3.setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        popError("0~255");
                        intInput3.setStyle(UserConfig.badStyle());
                    }
                }
            };
            intInput3.textProperty().addListener(intInput3Listener);
            intInput3.setText("255");
            NodeStyleTools.setTooltip(intInput3, new Tooltip("0~255"));

            setBox.getChildren().addAll(intInputPane, intInputPane2, intInputPane3, imageThresholdTipsView);
            if (okButton != null) {
                okButton.disableProperty().bind(
                        intInput.styleProperty().isEqualTo(UserConfig.badStyle())
                                .or(intInput3.styleProperty().isEqualTo(UserConfig.badStyle()))
                                .or(intInput2.styleProperty().isEqualTo(UserConfig.badStyle()))
                );
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    protected void makeBlackWhiteBox() {
        try {
            setBox.getChildren().addAll(binrayBox);

            if (okButton != null) {
                okButton.disableProperty().bind(
                        binaryController.thresholdInput.styleProperty().isEqualTo(UserConfig.badStyle())
                );
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
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
                        int v = Integer.parseInt(newValue);
                        if (v >= 0 && v <= 255) {
                            intPara1 = v;
                            ValidationTools.setEditorNormal(intBox);
                            UserConfig.setInt("ImageSepiaIntensity", v);
                        } else {
                            ValidationTools.setEditorBadStyle(intBox);
                            popError("0~255");
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(intBox);
                        popError("0~255");
                    }
                }
            };
            intBox.setEditable(true);
            intBox.getSelectionModel().selectedItemProperty().addListener(intBoxListener);
            intBox.getItems().addAll(Arrays.asList("80", "20", "50", "10", "5", "100", "15", "20", "60"));
            intBox.getSelectionModel().select(UserConfig.getInt("ImageSepiaIntensity", 80) + "");

            setBox.getChildren().addAll(intBoxPane);
            if (okButton != null) {
                okButton.disableProperty().bind(
                        intBox.getEditor().styleProperty().isEqualTo(UserConfig.badStyle())
                );
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
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
                        int v = Integer.parseInt(newValue);
                        if (v > 0) {
                            intPara1 = v;
                            ValidationTools.setEditorNormal(intBox);
                            UserConfig.setInt("ImageMosaicIntensity", v);
                        } else {
                            ValidationTools.setEditorBadStyle(intBox);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(intBox);
                    }
                }
            };
            intBox.setEditable(true);
            intBox.getSelectionModel().selectedItemProperty().addListener(intBoxListener);
            intBox.getItems().addAll(Arrays.asList("20", "50", "10", "5", "80", "100", "15", "20", "60"));
            intBox.getSelectionModel().select(UserConfig.getInt("ImageMosaicIntensity", 20) + "");

            setBox.getChildren().addAll(intBoxPane);
            if (okButton != null) {
                okButton.disableProperty().bind(
                        intBox.getEditor().styleProperty().isEqualTo(UserConfig.badStyle())
                );
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

}
