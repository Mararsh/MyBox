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
import javafx.scene.layout.VBox;
import mara.mybox.bufferedimage.BufferedImageTools.Direction;
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
 * @License Apache License Version 2.0
 */
public class ControlImageEffectOptions extends BaseController {

    protected ImageManufactureController editor;
    protected OperationType effectType;
    protected int intPara1, intPara2, intPara3;
    protected ConvolutionKernel kernel;
    protected ChangeListener<String> intBoxListener, stringBoxListener, intInputListener,
            intInput2Listener, intInput3Listener;
    protected ChangeListener<Number> numberBoxListener;
    protected Button paletteAddButton, htmlButton;

    @FXML
    protected ToggleGroup effectTypeGroup;
    @FXML
    protected VBox setBox, binrayBox, quanBox, edgeBox;
    @FXML
    protected RadioButton PosterizingRadio, ThresholdingRadio, GrayRadio,
            SepiaRadio, BlackOrWhiteRadio, EdgeDetectionRadio, EmbossRadio,
            effectMosaicRadio, effectFrostedRadio,
            eightLaplaceRadio, eightLaplaceExcludedRadio, fourLaplaceRadio, fourLaplaceExcludedRadio;
    @FXML
    protected TextField intInput, intInput2, intInput3;
    @FXML
    protected FlowPane stringBoxPane, intBoxPane, intInputPane,
            intInputPane2, intInputPane3, othersPane;
    @FXML
    protected ComboBox<String> intBox, stringBox;
    @FXML
    protected CheckBox valueCheck;
    @FXML
    protected Label intBoxLabel, intLabel, intLabel2, intLabel3, stringLabel;
    @FXML
    protected ControlImageQuantization quantizationController;
    @FXML
    protected ControlImageBinary binaryController;
    @FXML
    protected ImageView imageThresholdTipsView;

    @Override
    public void initControls() {
        try {
            super.initControls();

            effectTypeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkEffectType();
                }
            });

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
            okButton = pController.okButton;
            paletteAddButton = pController.paletteAddButton;
            htmlButton = pController.htmlButton;
            binaryController.setParameters(pController.imageView);
        } else {
            binaryController.setParameters(null);
        }
    }

    protected void checkEffectType() {
        try {
            if (editor != null) {
                editor.resetImagePane();
//                if (!editor.scopeController.scopeWhole()) {
//                    editor.scopeTab();
//                }
            }

            clearValues();
            if (okButton != null && effectTypeGroup.getSelectedToggle() == null) {
                okButton.setDisable(true);
                return;
            }
            if (EdgeDetectionRadio.isSelected()) {
                effectType = OperationType.EdgeDetect;
                makeEdgeBox();

            } else if (EmbossRadio.isSelected()) {
                effectType = OperationType.Emboss;
                makeEmbossBox();

            } else if (PosterizingRadio.isSelected()) {
                effectType = OperationType.Quantization;
                makePosterizingBox();

            } else if (ThresholdingRadio.isSelected()) {
                effectType = OperationType.Thresholding;
                makeThresholdingBox();

            } else if (GrayRadio.isSelected()) {
                effectType = OperationType.Gray;

            } else if (BlackOrWhiteRadio.isSelected()) {
                effectType = OperationType.BlackOrWhite;
                makeBlackWhiteBox();

            } else if (SepiaRadio.isSelected()) {
                effectType = OperationType.Sepia;
                makeSepiaBox();

            } else if (effectMosaicRadio.isSelected()) {
                effectType = OperationType.Mosaic;
                makeMosaicBox();

            } else if (effectFrostedRadio.isSelected()) {
                effectType = OperationType.FrostedGlass;
                makeMosaicBox();

            }

            refreshStyle(setBox);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
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
        quantizationController.resultsLabel.setText("");
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
                        quantizationController.quanColorsSelector.getEditor().styleProperty().isEqualTo(UserConfig.badStyle()));
            }

            if (paletteAddButton != null) {
                paletteAddButton.setVisible(false);
            }
            if (htmlButton != null) {
                htmlButton.setVisible(false);
            }

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
