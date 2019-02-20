package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
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
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import mara.mybox.db.TableConvolutionKernel;
import mara.mybox.fxml.FxmlTools;
import static mara.mybox.fxml.FxmlTools.badStyle;
import static mara.mybox.value.AppVaribles.logger;
import static mara.mybox.value.AppVaribles.getMessage;
import mara.mybox.fxml.image.ImageConvolution;
import mara.mybox.fxml.image.PixelsOperation;
import mara.mybox.fxml.image.ImageBinary;
import mara.mybox.fxml.image.ImageContrast;
import mara.mybox.fxml.image.ImageGray;
import mara.mybox.fxml.image.ImageQuantization;
import mara.mybox.image.ImageContrast.ContrastAlgorithm;
import mara.mybox.image.ImageConvert.Direction;
import mara.mybox.image.ImageQuantization.QuantizationAlgorithm;
import mara.mybox.data.ConvolutionKernel;
import mara.mybox.image.PixelsOperation.OperationType;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-10-11
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureEffectsController extends ImageManufactureController {

    private OperationType effectType;
    protected int intPara1, intPara2, intPara3;
    private List<ConvolutionKernel> kernels;
    private ConvolutionKernel kernel;
    private ComboBox<String> intBox, stringBox;
    private Label intLabel, intLabel2, intLabel3, intLabel4, stringLabel;
    private CheckBox valueCheck;
    private TextField intInput, intInput2, intInput3, intInput4;
    private RadioButton radio1, radio2, radio3, radio4;
    private ToggleGroup radioGroup;
    private Button setButton;
    private QuantizationAlgorithm quantizationAlgorithm;
    private ContrastAlgorithm contrastAlgorithm;

    @FXML
    protected ToggleGroup effectsGroup;
    @FXML
    protected HBox setBox;
    @FXML
    protected RadioButton thresholdingRadio, posterizingRadio, bwRadio,
            convolutionRadio, contrastRadio;

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

            FxmlTools.setComments(thresholdingRadio, new Tooltip(getMessage("ThresholdingComments")));
            FxmlTools.setComments(posterizingRadio, new Tooltip(getMessage("QuantizationComments")));
            FxmlTools.setComments(bwRadio, new Tooltip(getMessage("BWThresholdComments")));

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    protected void initInterface() {
        try {
            if (values == null || values.getImage() == null) {
                return;
            }
            super.initInterface();

            isSettingValues = true;

            isSettingValues = false;
        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    private void removeTmpControls() {
        intBox = null;
        valueCheck = null;
        intInput = intInput2 = intInput3 = intInput4 = null;
        intLabel = intLabel2 = intLabel3 = intLabel4 = stringLabel = null;
        radio1 = radio2 = radio3 = radio4 = null;
        setButton = null;
        scopeListBox.setDisable(false);
    }

    private void checkEffetcsOperationType() {
        try {
            setBox.getChildren().clear();
            okButton.disableProperty().unbind();
            removeTmpControls();
            stringBox = null;
            radioGroup = null;

            RadioButton selected = (RadioButton) effectsGroup.getSelectedToggle();
            String selectedString = selected.getText();
            if (getMessage("Blur").equals(selectedString)) {
                effectType = OperationType.Blur;
                makeBlurBox();

            } else if (getMessage("Sharpen").equals(selectedString)) {
                effectType = OperationType.Sharpen;

            } else if (getMessage("Clarity").equals(selectedString)) {
                effectType = OperationType.Clarity;

            } else if (getMessage("EdgeDetection").equals(selectedString)) {
                effectType = OperationType.EdgeDetect;

            } else if (getMessage("Emboss").equals(selectedString)) {
                effectType = OperationType.Emboss;
                makeEmbossBox();

            } else if (getMessage("Posterizing").equals(selectedString)) {
                effectType = OperationType.Quantization;
                makePosterizingBox();

            } else if (getMessage("Thresholding").equals(selectedString)) {
                effectType = OperationType.Thresholding;
                makeThresholdingBox();

            } else if (getMessage("Gray").equals(selectedString)) {
                effectType = OperationType.Gray;

            } else if (getMessage("BlackOrWhite").equals(selectedString)) {
                effectType = OperationType.BlackOrWhite;
                makeBlackWhiteBox();

            } else if (getMessage("Sepia").equals(selectedString)) {
                effectType = OperationType.Sepia;
                makeSepiaBox();

            } else if (getMessage("Contrast").equals(selectedString)) {
                effectType = OperationType.Contrast;
                makeContrastBox();

            } else if (getMessage("Convolution").equals(selectedString)) {
                effectType = OperationType.Convolution;
                makeConvolutionBox();

            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void makeBlurBox() {
        try {
            intPara1 = 10;
            intLabel = new Label(getMessage("Radius"));
            intBox = new ComboBox();
            intBox.setEditable(true);
            intBox.setPrefWidth(80);
            intBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            intPara1 = v;
                            intBox.getEditor().setStyle(null);
                        } else {
                            intBox.getEditor().setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        intBox.getEditor().setStyle(badStyle);
                    }
                }
            });
            intBox.getItems().addAll(Arrays.asList("10", "5", "3", "2", "1", "8", "15", "20", "30"));
            intBox.getSelectionModel().select(0);
            stringLabel = new Label(getMessage("Algorithm"));
            stringBox = new ComboBox();
            stringBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        if (getMessage("AverageBlur").equals(newValue)) {
                            kernel = ConvolutionKernel.makeAverageBlur(intPara1);
                        } else {
                            kernel = ConvolutionKernel.makeGaussKernel(intPara1);
                        }
                        stringBox.getEditor().setStyle(null);
                    } catch (Exception e) {
                        stringBox.getEditor().setStyle(badStyle);
                    }
                }
            });
            stringBox.getItems().addAll(Arrays.asList(getMessage("AverageBlur"), getMessage("GaussianBlur")));
            stringBox.getSelectionModel().select(getMessage("AverageBlur"));
            setBox.getChildren().addAll(stringLabel, stringBox, intLabel, intBox);
            okButton.disableProperty().bind(
                    intBox.getEditor().styleProperty().isEqualTo(badStyle)
                            .or(stringBox.getEditor().styleProperty().isEqualTo(badStyle))
            );
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void makeEmbossBox() {
        try {
            intPara1 = Direction.Top;
            stringLabel = new Label(getMessage("Direction"));
            stringBox = new ComboBox();
            stringBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (newValue == null || newValue.trim().isEmpty()) {
                        return;
                    }
                    if (getMessage("Top").equals(newValue)) {
                        intPara1 = Direction.Top;
                    } else if (getMessage("Bottom").equals(newValue)) {
                        intPara1 = Direction.Bottom;
                    } else if (getMessage("Left").equals(newValue)) {
                        intPara1 = Direction.Top;
                    } else if (getMessage("Right").equals(newValue)) {
                        intPara1 = Direction.Right;
                    } else if (getMessage("LeftTop").equals(newValue)) {
                        intPara1 = Direction.LeftTop;
                    } else if (getMessage("RightBottom").equals(newValue)) {
                        intPara1 = Direction.RightBottom;
                    } else if (getMessage("LeftBottom").equals(newValue)) {
                        intPara1 = Direction.LeftBottom;
                    } else if (getMessage("RightTop").equals(newValue)) {
                        intPara1 = Direction.RightTop;
                    } else {
                        intPara1 = Direction.Top;
                    }
                }
            });
            stringBox.getItems().addAll(Arrays.asList(getMessage("Top"), getMessage("Bottom"),
                    getMessage("Left"), getMessage("Right"),
                    getMessage("LeftTop"), getMessage("RightBottom"),
                    getMessage("LeftBottom"), getMessage("RightTop")));
            stringBox.getSelectionModel().select(getMessage("Top"));
            intPara2 = 3;
            intLabel = new Label(getMessage("Radius"));
            intBox = new ComboBox();
            intBox.setEditable(false);
            intBox.setPrefWidth(80);
            intBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            intPara2 = v;
                            intBox.getEditor().setStyle(null);
                        } else {
                            intBox.getEditor().setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        intBox.getEditor().setStyle(badStyle);
                    }
                }
            });
            intBox.getItems().addAll(Arrays.asList("3", "5"));
            intBox.getSelectionModel().select(0);
            valueCheck = new CheckBox(getMessage("Gray"));
            valueCheck.setSelected(true);
            setBox.getChildren().addAll(stringLabel, stringBox, intLabel, intBox, valueCheck);
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
            quantizationAlgorithm = QuantizationAlgorithm.RGB_Uniform;
            stringLabel = new Label(getMessage("Algorithm"));
            stringBox = new ComboBox();
            stringBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (getMessage("RGBUniformQuantization").equals(newValue)) {
                        quantizationAlgorithm = QuantizationAlgorithm.RGB_Uniform;
                    } else if (getMessage("HSBUniformQuantization").equals(newValue)) {
                        quantizationAlgorithm = QuantizationAlgorithm.HSB_Uniform;
                    }
                }
            });
            stringBox.getItems().addAll(Arrays.asList(getMessage("RGBUniformQuantization"),
                    getMessage("HSBUniformQuantization")));
            stringBox.getSelectionModel().select(getMessage("RGBUniformQuantization"));
            intPara1 = 64;
            intLabel = new Label(getMessage("ColorsNumber"));
            intBox = new ComboBox();
            intBox.setEditable(false);
            intBox.setPrefWidth(120);
            intBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            intPara1 = v;
                            intBox.getEditor().setStyle(null);
                        } else {
                            intBox.getEditor().setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        intBox.getEditor().setStyle(badStyle);
                    }
                }
            });
            intBox.getItems().addAll(Arrays.asList(
                    "64", "512", "8", "4096", "216", "343", "27", "125", "1000", "729", "1728", "8000"));
            intBox.getSelectionModel().select(0);
            valueCheck = new CheckBox(getMessage("Dithering"));
            valueCheck.setSelected(true);
            FxmlTools.setComments(valueCheck, new Tooltip(getMessage("DitherComments")));
            setBox.getChildren().addAll(stringLabel, stringBox, intLabel, intBox, valueCheck);
            okButton.disableProperty().bind(
                    intBox.getEditor().styleProperty().isEqualTo(badStyle)
                            .or(stringBox.getEditor().styleProperty().isEqualTo(badStyle))
            );
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    private void makeThresholdingBox() {
        try {
            intPara1 = 128;
            intLabel = new Label(getMessage("Threshold"));
            intInput = new TextField();
            intInput.textProperty().addListener(new ChangeListener<String>() {
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
            });
            intInput.setPrefWidth(100);
            intInput.setText("128");
            FxmlTools.quickTooltip(intInput, new Tooltip("0~255"));

            intPara2 = 0;
            intLabel2 = new Label(getMessage("SmallValue"));
            intInput2 = new TextField();
            intInput2.textProperty().addListener(new ChangeListener<String>() {
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
            });
            intInput2.setPrefWidth(100);
            intInput2.setText("0");
            FxmlTools.quickTooltip(intInput2, new Tooltip("0~255"));

            intPara3 = 255;
            intLabel3 = new Label(getMessage("BigValue"));
            intInput3 = new TextField();
            intInput3.textProperty().addListener(new ChangeListener<String>() {
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
            });
            intInput3.setPrefWidth(100);
            intInput3.setText("255");
            FxmlTools.quickTooltip(intInput3, new Tooltip("0~255"));

            setBox.getChildren().addAll(intLabel, intInput,
                    intLabel2, intInput2,
                    intLabel3, intInput3);
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
            intInput = new TextField();
            intInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    try {
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
            });
            intInput.setPrefWidth(100);
            intInput.setText("128");
            FxmlTools.quickTooltip(intInput, new Tooltip("0~255"));

            setButton = new Button(getMessage("Calculate"));
            setButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    int scaleValue = ImageBinary.calculateThreshold(imageView.getImage());
                    intInput.setText(scaleValue + "");
                }
            });

            intPara1 = 1;
            radioGroup = new ToggleGroup();
            radio1 = new RadioButton(getMessage("OTSU"));
            radio1.setToggleGroup(radioGroup);
            radio1.setUserData(1);
            radio2 = new RadioButton(getMessage("Default"));
            radio2.setToggleGroup(radioGroup);
            radio2.setUserData(2);
            radio3 = new RadioButton(getMessage("Threshold"));
            radio3.setToggleGroup(radioGroup);
            radio3.setUserData(3);
            radioGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    if (radioGroup.getSelectedToggle() == null) {
                        return;
                    }
                    intPara1 = (int) ((RadioButton) new_toggle).getUserData();
                    intInput.setDisable(intPara1 != 3);
                    setButton.setDisable(intPara1 != 3);
                }
            });
            radio1.setSelected(true);

            valueCheck = new CheckBox(getMessage("Dithering"));
            valueCheck.setSelected(true);
            FxmlTools.setComments(valueCheck, new Tooltip(getMessage("DitherComments")));

            setBox.getChildren().addAll(radio1, radio2, radio3,
                    intInput, setButton, valueCheck);
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
            intLabel = new Label(getMessage("Intensity"));
            intInput = new TextField();
            intInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(intInput.getText());
                        if (v >= 0 && v <= 255) {
                            intPara1 = v;
                            intInput.setStyle(null);
                        } else {
                            intInput.setStyle(badStyle);
                            popError("0~255");
                        }
                    } catch (Exception e) {
                        popError("0~255");
                        intInput.setStyle(badStyle);
                    }
                }
            });
            intInput.setPrefWidth(100);
            intInput.setText("80");
            FxmlTools.quickTooltip(intInput, new Tooltip("0~255"));

            setBox.getChildren().addAll(intLabel, intInput);
            okButton.disableProperty().bind(
                    intInput.styleProperty().isEqualTo(badStyle)
            );
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    private void makeConvolutionBox() {
        try {
            stringLabel = new Label(getMessage("ConvolutionKernel"));
            stringBox = new ComboBox();
            kernel = null;
            if (kernels == null) {
                kernels = TableConvolutionKernel.read();
            }
            loadKernelsList(kernels);
            stringBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue ov, Number oldValue, Number newValue) {
                    int index = newValue.intValue();
                    if (index < 0 || index >= kernels.size()) {
                        kernel = null;
                        stringBox.getEditor().setStyle(badStyle);
                        return;
                    }
                    kernel = kernels.get(index);
                    stringBox.getEditor().setStyle(null);
                }
            });
            FxmlTools.quickTooltip(stringBox, new Tooltip(getMessage("CTRL+k")));
            setButton = new Button(getMessage("ManageDot"));
            setButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    BaseController c = openStage(CommonValues.ConvolutionKernelManagerFxml, false);
                    c.setParentController(getMyController());
                    c.setParentFxml(getMyFxml());
                }
            });
            setBox.getChildren().addAll(stringLabel, stringBox, setButton);
            okButton.disableProperty().bind(
                    stringBox.getEditor().styleProperty().isEqualTo(badStyle)
            );
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    private void makeContrastBox() {
        try {
            contrastAlgorithm = ContrastAlgorithm.HSB_Histogram_Equalization;
            stringLabel = new Label(getMessage("Algorithm"));
            stringBox = new ComboBox();
            stringBox.getItems().addAll(Arrays.asList(
                    getMessage("HSBHistogramEqualization"),
                    getMessage("GrayHistogramEqualization"),
                    getMessage("GrayHistogramStretching"),
                    getMessage("GrayHistogramShifting")
            //                    getMessage("LumaHistogramEqualization"),
            //                    getMessage("AdaptiveHistogramEqualization")
            ));
            stringBox.getSelectionModel().select(0);
            stringBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (setBox.getChildren() != null) {
                        if (setBox.getChildren().contains(intInput)) {
                            setBox.getChildren().removeAll(intLabel, intInput);
                        }
                        if (setBox.getChildren().contains(intInput2)) {
                            setBox.getChildren().removeAll(intLabel2, intInput2);
                        }
                    }
                    okButton.disableProperty().unbind();
                    if (getMessage("GrayHistogramEqualization").equals(newValue)) {
                        contrastAlgorithm = ContrastAlgorithm.Gray_Histogram_Equalization;
                    } else if (getMessage("GrayHistogramStretching").equals(newValue)) {
                        contrastAlgorithm = ContrastAlgorithm.Gray_Histogram_Stretching;
                        intPara1 = 100;
                        intLabel = new Label(getMessage("LeftThreshold"));
                        intInput = new TextField();
                        intInput.textProperty().addListener(new ChangeListener<String>() {
                            @Override
                            public void changed(ObservableValue<? extends String> observable,
                                    String oldValue, String newValue) {
                                try {
                                    int v = Integer.valueOf(intInput.getText());
                                    if (v >= 0) {
                                        intPara1 = v;
                                        intInput.setStyle(null);
                                    } else {
                                        intInput.setStyle(badStyle);
                                    }
                                } catch (Exception e) {
                                    intInput.setStyle(badStyle);
                                }
                            }
                        });
                        intInput.setPrefWidth(100);
                        intInput.setText("100");

                        intPara2 = 100;
                        intLabel2 = new Label(getMessage("RightThreshold"));
                        intInput2 = new TextField();
                        intInput2.textProperty().addListener(new ChangeListener<String>() {
                            @Override
                            public void changed(ObservableValue<? extends String> observable,
                                    String oldValue, String newValue) {
                                try {
                                    int v = Integer.valueOf(intInput2.getText());
                                    if (v >= 0) {
                                        intPara2 = v;
                                        intInput2.setStyle(null);
                                    } else {
                                        intInput2.setStyle(badStyle);
                                    }
                                } catch (Exception e) {
                                    intInput2.setStyle(badStyle);
                                }
                            }
                        });
                        intInput2.setPrefWidth(100);
                        intInput2.setText("100");

                        setBox.getChildren().addAll(intLabel, intInput, intLabel2, intInput2);
                        okButton.disableProperty().bind(
                                intInput.styleProperty().isEqualTo(badStyle)
                                        .or(intInput2.styleProperty().isEqualTo(badStyle))
                                        .or(stringBox.getEditor().styleProperty().isEqualTo(badStyle))
                        );
                    } else if (getMessage("GrayHistogramShifting").equals(newValue)) {
                        contrastAlgorithm = ContrastAlgorithm.Gray_Histogram_Shifting;
                        intPara1 = 80;
                        intLabel = new Label(getMessage("Offset"));
                        intInput = new TextField();
                        intInput.textProperty().addListener(new ChangeListener<String>() {
                            @Override
                            public void changed(ObservableValue<? extends String> observable,
                                    String oldValue, String newValue) {
                                try {
                                    int v = Integer.valueOf(intInput.getText());
                                    if (v >= -255 && v <= 255) {
                                        intPara1 = v;
                                        intInput.setStyle(null);
                                    } else {
                                        intInput.setStyle(badStyle);
                                        popError("-255 ~ 255");
                                    }
                                } catch (Exception e) {
                                    popError("-255 ~ 255");
                                }
                            }
                        });
                        intInput.setPrefWidth(100);
                        intInput.setText("10");
                        FxmlTools.quickTooltip(intInput, new Tooltip("-255 ~ 255"));
                        setBox.getChildren().addAll(intLabel, intInput);
                        okButton.disableProperty().bind(
                                intInput.styleProperty().isEqualTo(badStyle)
                                        .or(stringBox.getEditor().styleProperty().isEqualTo(badStyle))
                        );
                    } else if (getMessage("LumaHistogramEqualization").equals(newValue)) {
                        contrastAlgorithm = ContrastAlgorithm.Luma_Histogram_Equalization;
                    } else if (getMessage("HSBHistogramEqualization").equals(newValue)) {
                        logger.debug(contrastAlgorithm);
                        contrastAlgorithm = ContrastAlgorithm.HSB_Histogram_Equalization;
                    } else if (getMessage("AdaptiveHistogramEqualization").equals(newValue)) {
                        contrastAlgorithm = ContrastAlgorithm.Adaptive_Histogram_Equalization;
                    }
                    logger.debug(contrastAlgorithm);
                }
            });

            setBox.getChildren().addAll(stringLabel, stringBox);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    protected void keyEventsHandler(KeyEvent event) {
        super.keyEventsHandler(event);
        if (event.isControlDown()) {
            String key = event.getText();
            if (key == null || key.isEmpty()) {
                return;
            }
            switch (key) {
                case "k":
                case "K":
                    if (stringBox != null) {
                        stringBox.show();
                    }
                    break;
            }
        }
    }

    public void applyKernel(ConvolutionKernel kernel) {
        if (effectType != OperationType.Convolution || stringBox == null) {
            return;
        }
        convolutionRadio.fire();
        if (stringBox.getItems().contains(kernel.getName())) {
            stringBox.getSelectionModel().select(kernel.getName());
        } else {
            stringBox.getSelectionModel().select(-1);
        }
        this.kernel = kernel;
        okAction();
    }

    public void loadKernelsList(List<ConvolutionKernel> records) {
        if (effectType != OperationType.Convolution || stringBox == null) {
            return;
        }
        kernels = records;
        stringBox.getItems().clear();
        if (kernels != null && !kernels.isEmpty()) {
            List<String> names = new ArrayList<>();
            for (ConvolutionKernel k : kernels) {
                names.add(k.getName());
            }
            stringBox.getItems().addAll(names);
            stringBox.getSelectionModel().select(0);
            stringBox.getEditor().setStyle(null);
        } else {
            stringBox.getEditor().setStyle(badStyle);
        }
    }

    @FXML
    @Override
    public void okAction() {
        if (null == effectType) {
            return;
        }
        task = new Task<Void>() {
            private Image newImage;

            @Override
            protected Void call() throws Exception {
                PixelsOperation pixelsOperation;
                ImageConvolution imageConvolution;
                switch (effectType) {
                    case Contrast:
                        ImageContrast imageContrast = new ImageContrast(values.getCurrentImage(), contrastAlgorithm);
                        imageContrast.setIntPara1(intPara1);
                        imageContrast.setIntPara2(intPara2);
                        newImage = imageContrast.operateFxImage();
                        break;
                    case Convolution:
                        if (kernel == null) {
                            int index = stringBox.getSelectionModel().getSelectedIndex();
                            if (kernels == null || kernels.isEmpty() || index < 0) {
                                return null;
                            }
                            kernel = kernels.get(index);
                        }
                        imageConvolution = new ImageConvolution(values.getCurrentImage(), scope, kernel);
                        newImage = imageConvolution.operateFxImage();
                        break;
                    case Blur:
                        if (kernel == null) {
                            return null;
                        }
                        imageConvolution = new ImageConvolution(values.getCurrentImage(), scope, kernel);
                        newImage = imageConvolution.operateFxImage();
                        break;
                    case Sharpen:
                        kernel = ConvolutionKernel.makeSharpen3b();
                        imageConvolution = new ImageConvolution(values.getCurrentImage(), scope, kernel);
                        newImage = imageConvolution.operateFxImage();
                        break;
                    case Clarity:
                        kernel = ConvolutionKernel.makeUnsharpMasking5();
                        imageConvolution = new ImageConvolution(values.getCurrentImage(), scope, kernel);
                        newImage = imageConvolution.operateFxImage();
                        break;
                    case EdgeDetect:
                        kernel = ConvolutionKernel.makeEdgeDetection3b();
                        imageConvolution = new ImageConvolution(values.getCurrentImage(), scope, kernel);
                        newImage = imageConvolution.operateFxImage();
                        break;
                    case Emboss:
                        kernel = ConvolutionKernel.makeEmbossKernel(intPara1, intPara2, valueCheck.isSelected());
                        imageConvolution = new ImageConvolution(values.getCurrentImage(), scope, kernel);
                        newImage = imageConvolution.operateFxImage();
                        break;
                    case Quantization:
                        int channelSize = (int) Math.round(Math.pow(intPara1, 1.0 / 3.0));
                        ImageQuantization quantization = new ImageQuantization(values.getCurrentImage());
                        quantization.setScope(scope);
                        quantization.set(quantizationAlgorithm, channelSize);
                        quantization.setIsDithering(valueCheck.isSelected());
                        newImage = quantization.operateFxImage();
                        break;
                    case Thresholding:
                        pixelsOperation = new PixelsOperation(values.getCurrentImage(), scope, effectType);
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
                                imageBinary = new ImageBinary(values.getCurrentImage(), scope, -1);
                                break;
                            case 3:
                                imageBinary = new ImageBinary(values.getCurrentImage(), scope, intPara2);
                                break;
                            default:
                                int t = ImageBinary.calculateThreshold(values.getCurrentImage());
                                imageBinary = new ImageBinary(values.getCurrentImage(), scope, t);
                                break;
                        }
                        imageBinary.setIsDithering(valueCheck.isSelected());
                        newImage = imageBinary.operateFxImage();
                        break;
                    case Gray:
                        ImageGray imageGray = new ImageGray(values.getCurrentImage(), scope);
                        newImage = imageGray.operateFxImage();
                        break;
                    case Sepia:
                        pixelsOperation = new PixelsOperation(values.getCurrentImage(), scope, effectType);
                        pixelsOperation.setIntPara1(intPara1);
                        newImage = pixelsOperation.operateFxImage();
                        break;
                    default:
                        return null;
                }
                if (task.isCancelled() || newImage == null) {
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
