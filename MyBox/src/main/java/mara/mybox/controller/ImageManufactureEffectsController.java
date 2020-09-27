package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
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
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import mara.mybox.controller.ImageManufactureController.ImageOperation;
import mara.mybox.data.ConvolutionKernel;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.StringTable;
import mara.mybox.fxml.ControlStyle;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.image.ImageBinary;
import mara.mybox.image.ImageColor;
import mara.mybox.image.ImageConvolution;
import mara.mybox.image.ImageGray;
import mara.mybox.image.ImageManufacture;
import mara.mybox.image.ImageManufacture.Direction;
import mara.mybox.image.ImageMosaic;
import mara.mybox.image.ImageQuantization;
import mara.mybox.image.ImageQuantization.QuantizationAlgorithm;
import mara.mybox.image.ImageScope;
import mara.mybox.image.PixelsOperation;
import mara.mybox.image.PixelsOperation.OperationType;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2019-9-2
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureEffectsController extends ImageManufactureOperationController {

    private OperationType effectType;
    protected int intPara1, intPara2, intPara3, bitDepth, quanColors;
    private ConvolutionKernel kernel;
    private QuantizationAlgorithm quantizationAlgorithm;
    private ChangeListener<String> intBoxListener, stringBoxListener, intInputListener,
            intInput2Listener, intInput3Listener;
    private ChangeListener<Number> numberBoxListener;
    private ImageView calculatorView;

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
    protected HBox depthBox;
    @FXML
    protected ComboBox<String> intBox, stringBox, depthSelector, quanColorsSelector;
    @FXML
    protected CheckBox valueCheck, quanDitherCheck, quanDataCheck;
    @FXML
    protected Label intBoxLabel, intLabel, intLabel2, intLabel3, stringLabel, depthLabel;
    @FXML
    protected Button button, demoButton;
    @FXML
    protected ImageView bitDepthTipsView, quanTipsView;

    @Override
    public void initPane() {
        try {
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

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    protected void paneExpanded() {
        imageController.showRightPane();
        checkEffectType();
    }

    private void checkEffectType() {
        try {
            imageController.resetImagePane();
            imageController.showScopePane();
            imageController.hideImagePane();
            clearValues();
            if (effectGroup.getSelectedToggle() == null) {
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
            logger.error(e.toString());
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

            quanColors = 27;
            quanColorsSelector.getItems().addAll(Arrays.asList(
                    "27", "64", "8", "16", "256", "512", "1024", "2048", "4096", "216", "343", "128", "1000", "729", "1728", "8000"));
            quanColorsSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            quanColors = v;
                            AppVariables.setUserConfigValue(baseName + "QuanColorsNumber", v + "");
                            FxmlControl.setEditorNormal(intBox);
                        } else {
                            FxmlControl.setEditorBadStyle(intBox);
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(intBox);
                    }
                }
            });
            quanColorsSelector.getSelectionModel().select(AppVariables.getUserConfigValue(baseName + "QuanColorsNumber", "27"));

            bitDepth = 4;
            depthSelector.getItems().addAll(Arrays.asList(
                    "4", "5", "6", "7", "8", "3", "2", "1"));
            depthSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        bitDepth = Integer.valueOf(newValue);
                        AppVariables.setUserConfigValue(baseName + "QuanDepth", bitDepth + "");
                    } catch (Exception e) {
                    }
                }
            });
            depthSelector.getSelectionModel().select(AppVariables.getUserConfigValue(baseName + "QuanDepth", "4"));

            quanDitherCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue(baseName + "QuanDither", newValue);
                }
            });
            quanDitherCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "QuanDither", true));

            quanDataCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue(baseName + "QuanData", newValue);
                }
            });
            quanDataCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "QuanData", true));

        } catch (Exception e) {
            logger.error(e.toString());
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
        if (quantizationAlgorithm == QuantizationAlgorithm.HSBUniformQuantization
                || quantizationAlgorithm == QuantizationAlgorithm.RGBUniformQuantization) {
            depthBox.setVisible(false);
        } else {
            depthBox.setVisible(true);
        }
        // KMeansClustering is best for selection of major colors but worse for quailty of image quantization
        // But actual results are depended on image itself.
//                    if (quantizationAlgorithm == QuantizationAlgorithm.KMeansClustering) {
//                        // these parameters seems best for selection of major colors
//                        intBox.getSelectionModel().select("16");
//                        depthBox.getSelectionModel().select("4");
//                    } else if (quantizationAlgorithm == QuantizationAlgorithm.PopularityQuantization) {
//                        // these parameters seems best for quailty of image quantization
//                        intBox.getSelectionModel().select("256");
//                        depthBox.getSelectionModel().select("4");
//                    }
    }

    private void clearValues() {
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
        okButton.disableProperty().unbind();
        okButton.setDisable(false);
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
    }

    private void makeEdgeBox() {
        try {
            valueCheck.setText(message("Gray"));
            valueCheck.setSelected(true);
            setBox.getChildren().addAll(edgeBox, valueCheck);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void makeEmbossBox() {
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
            okButton.disableProperty().bind(
                    intBox.getEditor().styleProperty().isEqualTo(badStyle)
                            .or(stringBox.getEditor().styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void makePosterizingBox() {
        try {
            setBox.getChildren().addAll(quanBox);
            okButton.disableProperty().bind(quanColorsSelector.getEditor().styleProperty().isEqualTo(badStyle));

            checkPosterizingAlgorithm();

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    private void makeThresholdingBox() {
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
            okButton.disableProperty().bind(
                    intInput.styleProperty().isEqualTo(badStyle)
                            .or(intInput3.styleProperty().isEqualTo(badStyle))
                            .or(intInput2.styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    private void makeBlackWhiteBox() {
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

            intPara1 = 1;
            otsuRadio.setSelected(true);

            valueCheck.setText(message("Dithering"));
            if (scopeController.scope != null && scopeController.scope.getScopeType() == ImageScope.ScopeType.Matting) {
                valueCheck.setSelected(false);
                valueCheck.setDisable(true);
            } else {
                valueCheck.setSelected(true);
                valueCheck.setDisable(false);
            }

            FxmlControl.setTooltip(tipsView, new Tooltip(message("BWThresholdComments")));

            othersPane.getChildren().addAll(tipsView, button, valueCheck);
            setBox.getChildren().addAll(bwBox, intInputPane, othersPane);
            okButton.disableProperty().bind(
                    intInput.styleProperty().isEqualTo(badStyle)
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    private void makeSepiaBox() {
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
            okButton.disableProperty().bind(
                    intBox.getEditor().styleProperty().isEqualTo(badStyle)
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    private void makeMosaicBox() {
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
            okButton.disableProperty().bind(
                    intBox.getEditor().styleProperty().isEqualTo(badStyle)
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    @Override
    public void okAction() {
        if (imageController == null || effectType == null) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                private Image newImage;
                private String value = null;
                private ImageQuantization quantization;

                @Override
                protected boolean handle() {
                    try {
                        PixelsOperation pixelsOperation;
                        ImageConvolution imageConvolution;
                        switch (effectType) {
                            case EdgeDetect:
                                if (eightLaplaceRadio.isSelected()) {
                                    kernel = ConvolutionKernel.makeEdgeDetectionEightNeighborLaplace();
                                } else if (eightLaplaceExcludedRadio.isSelected()) {
                                    kernel = ConvolutionKernel.makeEdgeDetectionEightNeighborLaplaceInvert();
                                } else if (fourLaplaceRadio.isSelected()) {
                                    kernel = ConvolutionKernel.makeEdgeDetectionFourNeighborLaplace();
                                } else if (fourLaplaceExcludedRadio.isSelected()) {
                                    kernel = ConvolutionKernel.makeEdgeDetectionFourNeighborLaplaceInvert();
                                } else {
                                    return false;
                                }
                                kernel.setGray(valueCheck.isSelected());
                                imageConvolution = ImageConvolution.create().
                                        setImage(imageView.getImage()).setScope(scopeController.scope).
                                        setKernel(kernel);
                                newImage = imageConvolution.operateFxImage();
                                break;
                            case Emboss:
                                kernel = ConvolutionKernel.makeEmbossKernel(intPara1, intPara2, valueCheck.isSelected());
                                imageConvolution = ImageConvolution.create().
                                        setImage(imageView.getImage()).setScope(scopeController.scope).
                                        setKernel(kernel);
                                newImage = imageConvolution.operateFxImage();
                                break;
                            case Quantization:
                                quantization = ImageQuantization.create(imageView.getImage(),
                                        scopeController.scope, quantizationAlgorithm, quanColors, bitDepth,
                                        quanDataCheck.isSelected(), quanDitherCheck.isSelected());
                                newImage = quantization.operateFxImage();
                                value = intPara1 + "";
                                break;
                            case Thresholding:
                                pixelsOperation = PixelsOperation.create(imageView.getImage(), scopeController.scope, effectType);
                                pixelsOperation.setIntPara1(intPara1);
                                pixelsOperation.setIntPara2(intPara2);
                                pixelsOperation.setIntPara3(intPara3);
                                pixelsOperation.setIsDithering(false);
                                newImage = pixelsOperation.operateFxImage();
                                break;
                            case BlackOrWhite:
                                ImageBinary imageBinary;
                                switch (intPara1) {
                                    case 2:
                                        imageBinary = new ImageBinary(imageView.getImage(), scopeController.scope, -1);
                                        break;
                                    case 3:
                                        imageBinary = new ImageBinary(imageView.getImage(), scopeController.scope, intPara2);
                                        value = intPara2 + "";
                                        break;
                                    default:
                                        int t = ImageBinary.calculateThreshold(imageView.getImage());
                                        imageBinary = new ImageBinary(imageView.getImage(), scopeController.scope, t);
                                        value = t + "";
                                        break;
                                }
                                imageBinary.setIsDithering(valueCheck.isSelected());
                                newImage = imageBinary.operateFxImage();
                                break;
                            case Gray:
                                ImageGray imageGray = new ImageGray(imageView.getImage(), scopeController.scope);
                                newImage = imageGray.operateFxImage();
                                break;
                            case Sepia:
                                pixelsOperation = PixelsOperation.create(imageView.getImage(), scopeController.scope, effectType);
                                pixelsOperation.setIntPara1(intPara1);
                                newImage = pixelsOperation.operateFxImage();
                                value = intPara1 + "";
                                break;
                            case Mosaic: {
                                ImageMosaic mosaic
                                        = ImageMosaic.create(imageView.getImage(), scopeController.scope, ImageMosaic.MosaicType.Mosaic, intPara1);
                                newImage = mosaic.operateFxImage();
                                value = intPara1 + "";
                            }
                            break;
                            case FrostedGlass: {
                                ImageMosaic mosaic
                                        = ImageMosaic.create(imageView.getImage(), scopeController.scope, ImageMosaic.MosaicType.FrostedGlass, intPara1);
                                newImage = mosaic.operateFxImage();
                                value = intPara1 + "";
                            }
                            break;
                            default:
                                return false;
                        }
                        if (task == null || isCancelled()) {
                            return false;
                        }
                        return newImage != null;
                    } catch (Exception e) {
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    imageController.popSuccessful();
                    imageController.updateImage(ImageOperation.Effects, effectType.name(), value, newImage, cost);

                    if (quantization != null && quanDataCheck.isSelected()) {
                        String name = null;
                        if (imageController.sourceFile != null) {
                            name = imageController.sourceFile.getName();
                        }
                        StringTable table = quantization.countTable(name);
                        if (table != null) {
                            HtmlViewerController controller
                                    = (HtmlViewerController) FxmlStage.openStage(CommonValues.HtmlViewerFxml);
                            controller.loadTable(table);
                        }
                    }
                }
            };
            imageController.openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
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
                    image = ImageManufacture.scaleImageLess(image, 1000000);

                    PixelsOperation pixelsOperation;
                    ImageConvolution imageConvolution;
                    ConvolutionKernel kernel;
                    BufferedImage bufferedImage;
                    String tmpFile;

                    BufferedImage outlineSource = SwingFXUtils.fromFXImage(new Image("img/NetworkTools.png"), null);
                    ImageScope scope = new ImageScope(SwingFXUtils.toFXImage(image, null));
                    scope.setScopeType(ImageScope.ScopeType.Outline);
                    if (sourceFile != null) {
                        scope.setFile(sourceFile.getAbsolutePath());
                    }
                    scope.setRectangle(new DoubleRectangle(0, 0, image.getWidth(), image.getHeight()));
                    BufferedImage[] outline = ImageManufacture.outline(outlineSource,
                            scope.getRectangle(), image.getWidth(), image.getHeight(),
                            false, ImageColor.converColor(Color.WHITE), false);
                    scope.setOutlineSource(outlineSource);
                    scope.setOutline(outline[1]);

                    ImageQuantization quantization
                            = ImageQuantization.create(image, scope,
                                    QuantizationAlgorithm.PopularityQuantization, 16, 4, false, true);
                    bufferedImage = quantization.operateImage();
                    tmpFile = AppVariables.MyBoxTempPath + File.separator
                            + message("Posterizing") + ".png";
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    pixelsOperation = PixelsOperation.create(
                            image, scope, OperationType.Thresholding);
                    pixelsOperation.setIntPara1(128);
                    pixelsOperation.setIntPara2(0);
                    pixelsOperation.setIntPara3(255);
                    pixelsOperation.setIsDithering(false);
                    bufferedImage = pixelsOperation.operateImage();
                    tmpFile = AppVariables.MyBoxTempPath + File.separator
                            + message("Thresholding") + ".png";
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    ImageGray imageGray = new ImageGray(image, scope);
                    bufferedImage = imageGray.operate();
                    tmpFile = AppVariables.MyBoxTempPath + File.separator
                            + message("Gray") + ".png";
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    pixelsOperation = PixelsOperation.create(
                            image, scope, OperationType.Sepia);
                    pixelsOperation.setIntPara1(60);
                    bufferedImage = pixelsOperation.operate();
                    tmpFile = AppVariables.MyBoxTempPath + File.separator
                            + message("Sepia") + ".png";
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    ImageBinary imageBinary = new ImageBinary(imageView.getImage(), scope, -1);
                    imageBinary.setIsDithering(true);
                    bufferedImage = imageBinary.operate();
                    tmpFile = AppVariables.MyBoxTempPath + File.separator
                            + message("BlackOrWhite") + ".png";
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    kernel = ConvolutionKernel.makeEdgeDetectionEightNeighborLaplace();
                    imageConvolution = ImageConvolution.create().
                            setImage(image).setScope(scope).setKernel(kernel);
                    bufferedImage = imageConvolution.operateImage();
                    tmpFile = AppVariables.MyBoxTempPath + File.separator
                            + message("EdgeDetection") + ".png";
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    kernel = ConvolutionKernel.makeEmbossKernel(Direction.Top, 3, true);
                    imageConvolution = ImageConvolution.create().
                            setImage(image).setScope(scope).setKernel(kernel);
                    bufferedImage = imageConvolution.operate();
                    tmpFile = AppVariables.MyBoxTempPath + File.separator
                            + message("Emboss") + ".png";
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    ImageMosaic mosaic = ImageMosaic.create(
                            image, scope, ImageMosaic.MosaicType.Mosaic, 30);
                    bufferedImage = mosaic.operate();
                    tmpFile = AppVariables.MyBoxTempPath + File.separator
                            + message("Mosaic") + ".png";
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    mosaic = ImageMosaic.create(image, scope,
                            ImageMosaic.MosaicType.FrostedGlass, 20);
                    bufferedImage = mosaic.operate();
                    tmpFile = AppVariables.MyBoxTempPath + File.separator
                            + message("FrostedGlass") + ".png";
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
                                    = (ImagesBrowserController) FxmlStage.openStage(CommonValues.ImagesBrowserFxml);
                            controller.loadFiles(files);
                        } catch (Exception e) {
                            logger.error(e.toString());
                        }
                    }
                });

            }

        };
        Thread thread = new Thread(demoTask);
        thread.setDaemon(true);
        thread.start();

    }

}
