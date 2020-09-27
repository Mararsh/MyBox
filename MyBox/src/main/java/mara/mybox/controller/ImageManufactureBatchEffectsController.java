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
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import mara.mybox.data.ConvolutionKernel;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.image.ImageBinary;
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

/**
 * @Author Mara
 * @CreateDate 2018-9-23
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureBatchEffectsController extends ImageManufactureBatchController {

    private OperationType effectType;
    protected int intPara1, intPara2, intPara3, bitDepth;
    private ConvolutionKernel kernel;
    private ComboBox<String> intBox, depthBox, stringBox;
    private Label intLabel, stringLabel, depthLabel;
    private CheckBox valueCheck;
    private TextField intInput;
    private RadioButton radio1, radio2, radio3;
    private ToggleGroup radioGroup;
    private QuantizationAlgorithm quantizationAlgorithm;

    @FXML
    protected ToggleGroup effectsGroup;
    @FXML
    protected FlowPane setPane;
    @FXML
    protected RadioButton thresholdingRadio, posterizingRadio, bwRadio;
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
        if (event.isControlDown() && event.getCode() != null) {
            switch (event.getCode()) {
                case K:
                    if (stringBox != null) {
                        stringBox.show();
                    }
                    return;
            }
        }
        super.keyEventsHandler(event);
    }

    private void removeTmpControls() {
        intBox = null;
        valueCheck = null;
        intInput = null;
        intLabel = stringLabel = null;
        radio1 = radio2 = radio3 = null;
    }

    private void checkEffetcsOperationType() {
        try {
            setPane.getChildren().clear();
            startButton.disableProperty().unbind();
            removeTmpControls();
            stringBox = null;
            radioGroup = null;

            RadioButton selected = (RadioButton) effectsGroup.getSelectedToggle();
            String selectedString = selected.getText();
            if (message("EdgeDetection").equals(selectedString)) {
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
            setPane.getChildren().addAll(stringLabel, stringBox, intLabel, intBox, valueCheck);
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

            quantizationAlgorithm = QuantizationAlgorithm.RGBUniformQuantization;
            stringLabel = new Label(message("Algorithm"));
            stringBox = new ComboBox<>();
            stringBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    for (QuantizationAlgorithm algorithm : QuantizationAlgorithm.values()) {
                        if (message(algorithm.name()).equals(newValue)) {
                            quantizationAlgorithm = algorithm;
                            break;
                        }
                    }
                    if (quantizationAlgorithm == QuantizationAlgorithm.HSBUniformQuantization
                            || quantizationAlgorithm == QuantizationAlgorithm.RGBUniformQuantization) {
                        if (setPane.getChildren().contains(depthBox)) {
                            setPane.getChildren().removeAll(depthBox, depthLabel);
                        }
                    } else {
                        if (!setPane.getChildren().contains(depthBox)) {
                            setPane.getChildren().add(5, depthLabel);
                            setPane.getChildren().add(6, depthBox);
                        }
                    }
                }
            });
            for (QuantizationAlgorithm algorithm : QuantizationAlgorithm.values()) {
                stringBox.getItems().add(message(algorithm.name()));
            }
            stringBox.getSelectionModel().select(0);

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
                    "256", "64", "8", "16", "27", "512", "4096", "216", "343", "125", "1000", "729", "1728", "8000"));
            intBox.getSelectionModel().select(0);

            valueCheck = new CheckBox(message("Dithering"));
            valueCheck.setSelected(true);

            bitDepth = 4;
            depthLabel = new Label(message("RegionsBitDepth"));
            depthBox = new ComboBox<>();
            depthBox.setEditable(false);
            depthBox.setPrefWidth(100);
            depthBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        bitDepth = Integer.valueOf(newValue);
                    } catch (Exception e) {
                    }
                }
            });
            depthBox.getItems().addAll(Arrays.asList(
                    "4", "5", "6", "7", "8", "3", "2", "1"));
            depthBox.getSelectionModel().select(0);

            setPane.getChildren().addAll(effectTipsView, stringLabel, stringBox, intLabel, intBox, valueCheck);
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

            setPane.getChildren().addAll(effectTipsView, intLabel, intInput,
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

            setPane.getChildren().addAll(effectTipsView, radio1, radio2, radio3, intInput, ditherTipsView, valueCheck);
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

            setPane.getChildren().addAll(intLabel, intInput);
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

    @Override
    protected BufferedImage handleImage(BufferedImage source) {
        try {
            BufferedImage target = null;
            PixelsOperation pixelsOperation;
            ImageConvolution imageConvolution;
            if (null != effectType) {
                switch (effectType) {
                    case EdgeDetect:
                        kernel = ConvolutionKernel.makeEdgeDetectionEightNeighborLaplace();
                        imageConvolution = ImageConvolution.create().
                                setImage(source).setKernel(kernel);
                        target = imageConvolution.operate();
                        break;
                    case Emboss:
                        kernel = ConvolutionKernel.makeEmbossKernel(intPara1, intPara2, valueCheck.isSelected());
                        imageConvolution = ImageConvolution.create().
                                setImage(source).setKernel(kernel);
                        target = imageConvolution.operate();
                        break;
                    case Thresholding:
                        pixelsOperation = PixelsOperation.create(ImageManufacture.removeAlpha(source), null, effectType);
                        pixelsOperation.setIntPara1(intPara1);
                        pixelsOperation.setIntPara2(intPara2);
                        pixelsOperation.setIntPara3(intPara3);
                        target = pixelsOperation.operate();
                        break;
                    case Quantization:
                        ImageQuantization quantization = ImageQuantization.create(
                                ImageManufacture.removeAlpha(source),
                                null, quantizationAlgorithm, intPara1, bitDepth, false,
                                valueCheck.isSelected());
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
                        pixelsOperation = PixelsOperation.create(source, null, effectType);
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
