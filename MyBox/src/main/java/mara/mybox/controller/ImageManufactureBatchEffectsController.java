package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.binding.Bindings;
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
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import mara.mybox.data.ConvolutionKernel;
import mara.mybox.db.TableConvolutionKernel;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.image.ImageBinary;
import mara.mybox.image.ImageContrast;
import mara.mybox.image.ImageConvolution;
import mara.mybox.image.ImageGray;
import mara.mybox.image.ImageManufacture;
import mara.mybox.image.ImageQuantization;
import mara.mybox.image.ImageQuantization.QuantizationAlgorithm;
import mara.mybox.image.PixelsOperation;
import mara.mybox.image.PixelsOperation.OperationType;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-9-23
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureBatchEffectsController extends ImageManufactureBatchController {

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
    private Button settingButton;
    private QuantizationAlgorithm quantizationAlgorithm;
    private ImageContrast.ContrastAlgorithm contrastAlgorithm;

    @FXML
    protected ToggleGroup effectsGroup;
    @FXML
    protected HBox setBox;
    @FXML
    protected RadioButton thresholdingRadio, posterizingRadio, bwRadio,
            convolutionRadio, contrastRadio;
    @FXML
    protected ImageView effectTipsView, ditherTipsView;

    public ImageManufactureBatchEffectsController() {
        baseTitle = AppVariables.message("ImageManufactureBatchEffects");

    }

    @Override
    public void initOptionsSection() {
        try {
            effectsGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkEffetcsOperationType();
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();

        checkEffetcsOperationType();
    }

    @Override
    public void keyEventsHandler(KeyEvent event) {
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

    private void removeTmpControls() {
        intBox = null;
        valueCheck = null;
        intInput = intInput2 = intInput3 = intInput4 = null;
        intLabel = intLabel2 = intLabel3 = intLabel4 = stringLabel = null;
        radio1 = radio2 = radio3 = radio4 = null;
    }

    private void checkEffetcsOperationType() {
        try {
            setBox.getChildren().clear();
            startButton.disableProperty().unbind();
            removeTmpControls();
            stringBox = null;
            radioGroup = null;

            RadioButton selected = (RadioButton) effectsGroup.getSelectedToggle();
            String selectedString = selected.getText();
            if (message("Blur").equals(selectedString)) {
                effectType = OperationType.Blur;
                makeBlurBox();

            } else if (message("Sharpen").equals(selectedString)) {
                effectType = OperationType.Sharpen;
                bindStart();

            } else if (message("Clarity").equals(selectedString)) {
                effectType = OperationType.Clarity;
                bindStart();

            } else if (message("EdgeDetection").equals(selectedString)) {
                effectType = OperationType.EdgeDetect;
                bindStart();

            } else if (message("Emboss").equals(selectedString)) {
                effectType = OperationType.Emboss;
                makeEmbossBox();

            } else if (message("Posterizing").equals(selectedString)) {
                effectType = OperationType.Quantization;
                makePosterizingBox();

            } else if (message("Thresholding").equals(selectedString)) {
                effectType = OperationType.Thresholding;
                makeThresholdingBox();

            } else if (message("Gray").equals(selectedString)) {
                effectType = OperationType.Gray;
                bindStart();

            } else if (message("BlackOrWhite").equals(selectedString)) {
                effectType = OperationType.BlackOrWhite;
                makeBlackWhiteBox();

            } else if (message("Sepia").equals(selectedString)) {
                effectType = OperationType.Sepia;
                makeSepiaBox();

            } else if (message("Contrast").equals(selectedString)) {
                effectType = OperationType.Contrast;
                makeContrastBox();

            } else if (message("Convolution").equals(selectedString)) {
                effectType = OperationType.Convolution;
                makeConvolutionBox();

            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void bindStart() {
        startButton.disableProperty().bind(
                Bindings.isEmpty(targetPathInput.textProperty())
                        .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                        .or(Bindings.isEmpty(tableView.getItems()))
        );
    }

    private void makeBlurBox() {
        try {
            intPara1 = 10;
            intLabel = new Label(message("Radius"));
            intBox = new ComboBox<>();
            intBox.setEditable(true);
            intBox.setPrefWidth(80);
            intBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            intPara1 = v;
                            FxmlControl.setEditorNormal(intBox);
                        } else {
                            FxmlControl.setEditorBadStyle(intBox);
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(intBox);
                    }
                }
            });
            intBox.getItems().addAll(Arrays.asList("10", "5", "3", "2", "1", "8", "15", "20", "30"));
            intBox.getSelectionModel().select(0);
            stringLabel = new Label(message("Algorithm"));
            stringBox = new ComboBox<>();
            stringBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        if (message("AverageBlur").equals(newValue)) {
                            kernel = ConvolutionKernel.makeAverageBlur(intPara1);
                        } else {
                            kernel = ConvolutionKernel.makeGaussKernel(intPara1);
                        }
                        FxmlControl.setEditorNormal(stringBox);
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(stringBox);
                    }
                }
            });
            stringBox.getItems().addAll(Arrays.asList(message("AverageBlur"), message("GaussianBlur")));
            stringBox.getSelectionModel().select(message("AverageBlur"));
            setBox.getChildren().addAll(stringLabel, stringBox, intLabel, intBox);
            startButton.disableProperty().bind(
                    intBox.getEditor().styleProperty().isEqualTo(badStyle)
                            .or(stringBox.getEditor().styleProperty().isEqualTo(badStyle))
                            .or(Bindings.isEmpty(targetPathInput.textProperty()))
                            .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                            .or(Bindings.isEmpty(tableView.getItems()))
            );
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void makeEmbossBox() {
        try {
            intPara1 = ImageManufacture.Direction.Top;
            stringLabel = new Label(message("Direction"));
            stringBox = new ComboBox<>();
            stringBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (newValue == null || newValue.trim().isEmpty()) {
                        return;
                    }
                    if (message("Top").equals(newValue)) {
                        intPara1 = ImageManufacture.Direction.Top;
                    } else if (message("Bottom").equals(newValue)) {
                        intPara1 = ImageManufacture.Direction.Bottom;
                    } else if (message("Left").equals(newValue)) {
                        intPara1 = ImageManufacture.Direction.Top;
                    } else if (message("Right").equals(newValue)) {
                        intPara1 = ImageManufacture.Direction.Right;
                    } else if (message("LeftTop").equals(newValue)) {
                        intPara1 = ImageManufacture.Direction.LeftTop;
                    } else if (message("RightBottom").equals(newValue)) {
                        intPara1 = ImageManufacture.Direction.RightBottom;
                    } else if (message("LeftBottom").equals(newValue)) {
                        intPara1 = ImageManufacture.Direction.LeftBottom;
                    } else if (message("RightTop").equals(newValue)) {
                        intPara1 = ImageManufacture.Direction.RightTop;
                    } else {
                        intPara1 = ImageManufacture.Direction.Top;
                    }
                }
            });
            stringBox.getItems().addAll(Arrays.asList(message("Top"), message("Bottom"),
                    message("Left"), message("Right"),
                    message("LeftTop"), message("RightBottom"),
                    message("LeftBottom"), message("RightTop")));
            stringBox.getSelectionModel().select(message("Top"));
            intPara2 = 3;
            intLabel = new Label(message("Radius"));
            intBox = new ComboBox<>();
            intBox.setEditable(false);
            intBox.setPrefWidth(80);
            intBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
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
            });
            intBox.getItems().addAll(Arrays.asList("3", "5"));
            intBox.getSelectionModel().select(0);
            valueCheck = new CheckBox(message("Gray"));
            valueCheck.setSelected(true);
            setBox.getChildren().addAll(stringLabel, stringBox, intLabel, intBox, valueCheck);
            startButton.disableProperty().bind(
                    intBox.getEditor().styleProperty().isEqualTo(badStyle)
                            .or(Bindings.isEmpty(targetPathInput.textProperty()))
                            .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                            .or(Bindings.isEmpty(tableView.getItems()))
                            .or(stringBox.getEditor().styleProperty().isEqualTo(badStyle))
            );
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void makePosterizingBox() {
        try {
            FxmlControl.setTooltip(effectTipsView, new Tooltip(message("QuantizationComments")));

            quantizationAlgorithm = QuantizationAlgorithm.RGB_Uniform;
            stringLabel = new Label(message("Algorithm"));
            stringBox = new ComboBox<>();
            stringBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (message("RGBUniformQuantization").equals(newValue)) {
                        quantizationAlgorithm = QuantizationAlgorithm.RGB_Uniform;
                    } else if (message("HSBUniformQuantization").equals(newValue)) {
                        quantizationAlgorithm = QuantizationAlgorithm.HSB_Uniform;
                    }
                }
            });
            stringBox.getItems().addAll(Arrays.asList(message("RGBUniformQuantization"),
                    message("HSBUniformQuantization")));
            stringBox.getSelectionModel().select(message("RGBUniformQuantization"));
            intPara1 = 64;
            intLabel = new Label(message("ColorsNumber"));
            intBox = new ComboBox<>();
            intBox.setEditable(false);
            intBox.setPrefWidth(120);
            intBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            intPara1 = v;
                            FxmlControl.setEditorNormal(intBox);
                        } else {
                            FxmlControl.setEditorBadStyle(intBox);
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(intBox);
                    }
                }
            });
            intBox.getItems().addAll(Arrays.asList(
                    "64", "512", "8", "4096", "216", "343", "27", "125", "1000", "729", "1728", "8000"));
            intBox.getSelectionModel().select(0);
            valueCheck = new CheckBox(message("Dithering"));
            valueCheck.setSelected(true);

            setBox.getChildren().addAll(effectTipsView, stringLabel, stringBox, intLabel, intBox, ditherTipsView, valueCheck);
            startButton.disableProperty().bind(
                    intBox.getEditor().styleProperty().isEqualTo(badStyle)
                            .or(Bindings.isEmpty(targetPathInput.textProperty()))
                            .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                            .or(Bindings.isEmpty(tableView.getItems()))
                            .or(stringBox.getEditor().styleProperty().isEqualTo(badStyle))
            );
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    private void makeThresholdingBox() {
        try {
            FxmlControl.setTooltip(effectTipsView, new Tooltip(message("ThresholdingComments")));

            intPara1 = 128;
            intLabel = new Label(message("Threshold"));
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
            FxmlControl.setTooltip(intInput, new Tooltip("0~255"));

            intPara2 = 0;
            Label smallValueLabel = new Label(message("SmallValue"));
            final TextField thresholdingMinInput = new TextField();
            thresholdingMinInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v >= 0 && v <= 255) {
                            intPara2 = v;
                            thresholdingMinInput.setStyle(null);
                        } else {
                            popError("0~255");
                            thresholdingMinInput.setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        popError("0~255");
                        thresholdingMinInput.setStyle(badStyle);
                    }
                }
            });
            thresholdingMinInput.setPrefWidth(100);
            thresholdingMinInput.setText("0");
            FxmlControl.setTooltip(thresholdingMinInput, new Tooltip("0~255"));

            intPara3 = 255;
            Label bigValueLabel = new Label(message("BigValue"));
            final TextField thresholdingMaxInput = new TextField();
            thresholdingMaxInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v >= 0 && v <= 255) {
                            intPara3 = v;
                            thresholdingMaxInput.setStyle(null);
                        } else {
                            popError("0~255");
                            thresholdingMaxInput.setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        popError("0~255");
                        thresholdingMaxInput.setStyle(badStyle);
                    }
                }
            });
            thresholdingMaxInput.setPrefWidth(100);
            thresholdingMaxInput.setText("255");
            FxmlControl.setTooltip(thresholdingMaxInput, new Tooltip("0~255"));

            setBox.getChildren().addAll(effectTipsView, intLabel, intInput,
                    bigValueLabel, thresholdingMaxInput,
                    smallValueLabel, thresholdingMinInput);
            startButton.disableProperty().bind(
                    intInput.styleProperty().isEqualTo(badStyle)
                            .or(Bindings.isEmpty(targetPathInput.textProperty()))
                            .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                            .or(Bindings.isEmpty(tableView.getItems()))
                            .or(thresholdingMinInput.styleProperty().isEqualTo(badStyle))
                            .or(thresholdingMaxInput.styleProperty().isEqualTo(badStyle))
            );
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    private void makeBlackWhiteBox() {
        try {
            FxmlControl.setTooltip(effectTipsView, new Tooltip(message("BWThresholdComments")));

            intPara2 = 128;
            intInput = new TextField();
            intInput.textProperty().addListener(new ChangeListener<String>() {
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
            });
            intInput.setPrefWidth(100);
            intInput.setText("128");
            FxmlControl.setTooltip(intInput, new Tooltip("0~255"));

            intPara1 = 1;
            radioGroup = new ToggleGroup();
            radio1 = new RadioButton(message("OTSU"));
            radio1.setToggleGroup(radioGroup);
            radio1.setUserData(1);
            radio2 = new RadioButton(message("Default"));
            radio2.setToggleGroup(radioGroup);
            radio2.setUserData(2);
            radio3 = new RadioButton(message("Threshold"));
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
                    intInput.setStyle(null);
                }
            });
            radio1.setSelected(true);

            valueCheck = new CheckBox(message("Dithering"));
            valueCheck.setSelected(true);

            setBox.getChildren().addAll(effectTipsView, radio1, radio2, radio3, intInput, ditherTipsView, valueCheck);
            startButton.disableProperty().bind(
                    intInput.styleProperty().isEqualTo(badStyle)
            );
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    private void makeSepiaBox() {
        try {
            intPara1 = 80;
            intLabel = new Label(message("Intensity"));
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
            FxmlControl.setTooltip(intInput, new Tooltip("0~255"));

            setBox.getChildren().addAll(intLabel, intInput);
            startButton.disableProperty().bind(
                    intInput.styleProperty().isEqualTo(badStyle)
                            .or(Bindings.isEmpty(targetPathInput.textProperty()))
                            .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                            .or(Bindings.isEmpty(tableView.getItems()))
            );
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    private void makeConvolutionBox() {
        try {
            stringLabel = new Label(message("ConvolutionKernel"));
            stringBox = new ComboBox<>();
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
                        FxmlControl.setEditorBadStyle(stringBox);
                        return;
                    }
                    kernel = kernels.get(index);
                    FxmlControl.setEditorNormal(stringBox);
                }
            });

            settingButton.setText(message("ManageDot"));
            settingButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    BaseController c = openStage(CommonValues.ConvolutionKernelManagerFxml);
                    c.setParentController(myController);
                    c.setParentFxml(myFxml);
                }
            });

            setBox.getChildren().addAll(stringLabel, stringBox, settingButton);
            startButton.disableProperty().bind(
                    stringBox.getEditor().styleProperty().isEqualTo(badStyle)
                            .or(Bindings.isEmpty(targetPathInput.textProperty()))
                            .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                            .or(Bindings.isEmpty(tableView.getItems()))
            );
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    private void makeContrastBox() {
        try {
            contrastAlgorithm = ImageContrast.ContrastAlgorithm.HSB_Histogram_Equalization;
            stringLabel = new Label(message("Algorithm"));
            stringBox = new ComboBox<>();
            stringBox.getItems().addAll(Arrays.asList(message("HSBHistogramEqualization"),
                    message("GrayHistogramEqualization"),
                    message("GrayHistogramStretching"),
                    message("GrayHistogramShifting")
            //                    getMessage("LumaHistogramEqualization"),
            //                    getMessage("AdaptiveHistogramEqualization")
            ));
            stringBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
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
                    startButton.disableProperty().unbind();
                    if (message("GrayHistogramStretching").equals(newValue)) {
                        contrastAlgorithm = ImageContrast.ContrastAlgorithm.Gray_Histogram_Stretching;
                        intPara1 = 100;
                        intLabel = new Label(message("LeftThreshold"));
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
                        intLabel2 = new Label(message("RightThreshold"));
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
                        startButton.disableProperty().bind(
                                intInput.styleProperty().isEqualTo(badStyle)
                                        .or(intInput2.styleProperty().isEqualTo(badStyle))
                                        .or(Bindings.isEmpty(targetPathInput.textProperty()))
                                        .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                                        .or(Bindings.isEmpty(tableView.getItems()))
                                        .or(stringBox.getEditor().styleProperty().isEqualTo(badStyle))
                        );
                    } else if (message("GrayHistogramShifting").equals(newValue)) {
                        contrastAlgorithm = ImageContrast.ContrastAlgorithm.Gray_Histogram_Shifting;
                        intPara1 = 80;
                        intLabel = new Label(message("Offset"));
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
                                    intInput.setStyle(badStyle);
                                    popError("-255 ~ 255");
                                }
                            }
                        });
                        intInput.setPrefWidth(100);
                        intInput.setText("10");
                        FxmlControl.setTooltip(intInput, new Tooltip("-255 ~ 255"));
                        setBox.getChildren().addAll(intLabel, intInput);
                        startButton.disableProperty().bind(
                                intInput.styleProperty().isEqualTo(badStyle)
                                        .or(Bindings.isEmpty(targetPathInput.textProperty()))
                                        .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                                        .or(Bindings.isEmpty(tableView.getItems()))
                                        .or(stringBox.getEditor().styleProperty().isEqualTo(badStyle))
                        );
                    } else {
                        if (message("GrayHistogramEqualization").equals(newValue)) {
                            contrastAlgorithm = ImageContrast.ContrastAlgorithm.Gray_Histogram_Equalization;
                        } else if (message("LumaHistogramEqualization").equals(newValue)) {
                            contrastAlgorithm = ImageContrast.ContrastAlgorithm.Luma_Histogram_Equalization;
                        } else if (message("HSBHistogramEqualization").equals(newValue)) {
                            contrastAlgorithm = ImageContrast.ContrastAlgorithm.HSB_Histogram_Equalization;
                        } else if (message("AdaptiveHistogramEqualization").equals(newValue)) {
                            contrastAlgorithm = ImageContrast.ContrastAlgorithm.Adaptive_Histogram_Equalization;
                        }
                        startButton.disableProperty().bind(
                                Bindings.isEmpty(targetPathInput.textProperty())
                                        .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                                        .or(Bindings.isEmpty(tableView.getItems()))
                                        .or(stringBox.getEditor().styleProperty().isEqualTo(badStyle))
                        );
                    }
                }
            });
            stringBox.getSelectionModel().select(0);

            setBox.getChildren().addAll(stringLabel, stringBox);
            FxmlControl.refreshStyle(setBox);

        } catch (Exception e) {
            logger.error(e.toString());
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

    @Override
    protected BufferedImage handleImage(BufferedImage source) {
        try {
            BufferedImage target = null;
            PixelsOperation pixelsOperation;
            ImageConvolution imageConvolution;
            if (null != effectType) {
                switch (effectType) {
                    case Contrast:
                        ImageContrast imageContrast = new ImageContrast(source, contrastAlgorithm);
                        imageContrast.setIntPara1(intPara1);
                        imageContrast.setIntPara2(intPara2);
                        target = imageContrast.operate();
                        break;
                    case Convolution:
                        if (kernel == null) {
                            int index = stringBox.getSelectionModel().getSelectedIndex();
                            if (kernels == null || kernels.isEmpty() || index < 0) {
                                return null;
                            }
                            kernel = kernels.get(index);
                        }
                        imageConvolution = new ImageConvolution(source, kernel);
                        target = imageConvolution.operate();
                        break;
                    case Blur:
                        if (kernel == null) {
                            return null;
                        }
                        imageConvolution = new ImageConvolution(source, kernel);
                        target = imageConvolution.operate();
                        break;
                    case Sharpen:
                        kernel = ConvolutionKernel.makeSharpen3b();
                        imageConvolution = new ImageConvolution(source, kernel);
                        target = imageConvolution.operate();
                        break;
                    case Clarity:
                        kernel = ConvolutionKernel.makeUnsharpMasking5();
                        imageConvolution = new ImageConvolution(source, kernel);
                        target = imageConvolution.operate();
                        break;
                    case EdgeDetect:
                        kernel = ConvolutionKernel.makeEdgeDetection3b();
                        imageConvolution = new ImageConvolution(source, kernel);
                        target = imageConvolution.operate();
                        break;
                    case Emboss:
                        kernel = ConvolutionKernel.makeEmbossKernel(intPara1, intPara2, valueCheck.isSelected());
                        imageConvolution = new ImageConvolution(source, kernel);
                        target = imageConvolution.operate();
                        break;
                    case Thresholding:
                        pixelsOperation = PixelsOperation.newPixelsOperation(ImageManufacture.removeAlpha(source), null, effectType);
                        pixelsOperation.setIntPara1(intPara1);
                        pixelsOperation.setIntPara2(intPara2);
                        pixelsOperation.setIntPara3(intPara3);
                        target = pixelsOperation.operate();
                        break;
                    case Quantization:
                        int channelSize = (int) Math.round(Math.pow(intPara1, 1.0 / 3.0));
                        ImageQuantization quantization = new ImageQuantization(ImageManufacture.removeAlpha(source),
                                quantizationAlgorithm, channelSize);
                        quantization.setIsDithering(valueCheck.isSelected());
                        target = quantization.operate();
                        break;
                    case Gray:
                        target = ImageGray.byteGray(source);
                        break;
                    case BlackOrWhite:
                        ImageBinary imageBinary;
                        switch (intPara1) {
                            case 2:
                                imageBinary = new ImageBinary(source, -1);
                                break;
                            case 3:
                                imageBinary = new ImageBinary(source, intPara2);
                                break;
                            default:
                                int t = ImageBinary.calculateThreshold(source);
                                imageBinary = new ImageBinary(source, t);
                                break;
                        }
                        imageBinary.setIsDithering(valueCheck.isSelected());
                        target = imageBinary.operate();
                        break;
                    case Sepia:
                        pixelsOperation = PixelsOperation.newPixelsOperation(source, null, effectType);
                        pixelsOperation.setIntPara1(intPara1);
                        target = pixelsOperation.operate();
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
