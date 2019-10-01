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
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import mara.mybox.controller.ImageManufactureController.ImageOperation;
import mara.mybox.data.ConvolutionKernel;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.fxml.ControlStyle;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.image.ImageBinary;
import mara.mybox.image.ImageColor;
import mara.mybox.image.ImageContrast.ContrastAlgorithm;
import mara.mybox.image.ImageConvolution;
import mara.mybox.image.ImageConvolution.SmoothAlgorithm;
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
    protected int intPara1, intPara2, intPara3;
    private List<ConvolutionKernel> kernels;
    private ConvolutionKernel kernel;
    private QuantizationAlgorithm quantizationAlgorithm;
    private ContrastAlgorithm contrastAlgorithm;
    private SmoothAlgorithm blurAlgorithm;
    private ChangeListener<String> intBoxListener, stringBoxListener, intInputListener,
            intInput2Listener, intInput3Listener;
    private ChangeListener<Number> numberBoxListener;
    private ImageView calculatorView, manageView;

    @FXML
    protected ToggleGroup effectGroup, radioGroup;
    @FXML
    protected RadioButton PosterizingRadio, ThresholdingRadio, GrayRadio,
            SepiaRadio, BlackOrWhiteRadio, EdgeDetectionRadio, EmbossRadio,
            effectMosaicRadio, effectFrostedRadio, radio1, radio2, radio3;
    @FXML
    protected TextField intInput, intInput2, intInput3;
    @FXML
    protected FlowPane setBox;
    @FXML
    protected ComboBox<String> intBox, stringBox;
    @FXML
    protected CheckBox valueCheck;
    @FXML
    protected Label intLabel, intLabel2, intLabel3, stringLabel;
    @FXML
    protected Button button;

    public ImageManufactureEffectsController() {
        baseTitle = AppVariables.message("ImageManufactureEffects");
        operation = ImageOperation.Effects;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            myPane = effectPane;

            effectGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkEffectType();
                }
            });

            radio1.setText(message("OTSU"));
            radio1.setUserData(1);
            radio2.setText(message("Default"));
            radio2.setUserData(2);
            radio3.setText(message("Threshold"));
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
                    button.setDisable(intPara1 != 3);
                }
            });

            calculatorView = new ImageView();
            manageView = new ImageView();

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @Override
    public void initPane(ImageManufactureController parent) {
        try {
            super.initPane(parent);
            if (parent == null) {
                return;
            }

            checkEffectType();

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    private void clearValues() {
        setBox.getChildren().clear();
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
        intInput2.setStyle(null);
        intInput3.setStyle(null);
        stringBox.setEditable(false);
        intBox.setEditable(false);
    }

    private void checkEffectType() {
        try {
            clearValues();
            if (effectGroup.getSelectedToggle() == null) {
                okButton.setDisable(true);
                return;
            }
            RadioButton selected = (RadioButton) effectGroup.getSelectedToggle();

            if (EdgeDetectionRadio.equals(selected)) {
                effectType = OperationType.EdgeDetect;

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
            intLabel.setText(message("Radius"));
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
            stringLabel.setText(message("Algorithm"));
            stringBoxListener = new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (message("RGBUniformQuantization").equals(newValue)) {
                        quantizationAlgorithm = QuantizationAlgorithm.RGB_Uniform;
                    } else if (message("HSBUniformQuantization").equals(newValue)) {
                        quantizationAlgorithm = QuantizationAlgorithm.HSB_Uniform;
                    }
                }
            };
            stringBox.getSelectionModel().selectedItemProperty().addListener(stringBoxListener);
            stringBox.getItems().addAll(Arrays.asList(message("RGBUniformQuantization"),
                    message("HSBUniformQuantization")));
            stringBox.getSelectionModel().select(message("RGBUniformQuantization"));

            intPara1 = 64;
            intLabel.setText(message("ColorsNumber"));
            intBox.getItems().addAll(Arrays.asList(
                    "64", "512", "8", "4096", "216", "343", "27", "125", "1000", "729", "1728", "8000"));
            intBoxListener = new ChangeListener<String>() {
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
            };
            intBox.getSelectionModel().selectedItemProperty().addListener(intBoxListener);
            intBox.getSelectionModel().select(0);

            valueCheck.setText(message("Dithering"));
            valueCheck.setSelected(true);

            FxmlControl.setTooltip(tipsView, new Tooltip(message("QuantizationComments")));

            setBox.getChildren().addAll(stringLabel, stringBox, intLabel, intBox, tipsView, valueCheck);
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

            setBox.getChildren().addAll(intLabel, intInput,
                    intLabel2, intInput2,
                    intLabel3, intInput3, tipsView);
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
            radio1.setSelected(true);

            valueCheck.setText(message("Dithering"));
            if (parent.scope() != null && parent.scope().getScopeType() == ImageScope.ScopeType.Matting) {
                valueCheck.setSelected(false);
                valueCheck.setDisable(true);
            } else {
                valueCheck.setSelected(true);
                valueCheck.setDisable(false);
            }

            FxmlControl.setTooltip(tipsView, new Tooltip(message("BWThresholdComments")));

            setBox.getChildren().addAll(radio1, radio2, radio3,
                    intInput, button, tipsView, valueCheck);
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
            intLabel.setText(message("Intensity"));
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

            setBox.getChildren().addAll(intLabel, intBox);
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
            intLabel.setText(message("Intensity"));
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

            setBox.getChildren().addAll(intLabel, intBox);
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
        if (parent == null || effectType == null) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                private Image newImage;
                private String value = null;

                @Override
                protected boolean handle() {
                    PixelsOperation pixelsOperation;
                    ImageConvolution imageConvolution;
                    switch (effectType) {
                        case EdgeDetect:
                            kernel = ConvolutionKernel.makeEdgeDetectionEightNeighborLaplace();
                            imageConvolution = new ImageConvolution(imageView.getImage(), parent.scope(), kernel);
                            newImage = imageConvolution.operateFxImage();
                            break;
                        case Emboss:
                            kernel = ConvolutionKernel.makeEmbossKernel(intPara1, intPara2, valueCheck.isSelected());
                            imageConvolution = new ImageConvolution(imageView.getImage(), parent.scope(), kernel);
                            newImage = imageConvolution.operateFxImage();
                            break;
                        case Quantization:
                            int channelSize = (int) Math.round(Math.pow(intPara1, 1.0 / 3.0));
                            ImageQuantization quantization = new ImageQuantization(imageView.getImage());
                            quantization.setScope(parent.scope());
                            quantization.set(quantizationAlgorithm, channelSize);
                            quantization.setIsDithering(valueCheck.isSelected());
                            newImage = quantization.operateFxImage();
                            value = channelSize + "";
                            break;
                        case Thresholding:
                            pixelsOperation = PixelsOperation.newPixelsOperation(imageView.getImage(), parent.scope(), effectType);
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
                                    imageBinary = new ImageBinary(imageView.getImage(), parent.scope(), -1);
                                    break;
                                case 3:
                                    imageBinary = new ImageBinary(imageView.getImage(), parent.scope(), intPara2);
                                    value = intPara2 + "";
                                    break;
                                default:
                                    int t = ImageBinary.calculateThreshold(imageView.getImage());
                                    imageBinary = new ImageBinary(imageView.getImage(), parent.scope(), t);
                                    value = t + "";
                                    break;
                            }
                            imageBinary.setIsDithering(valueCheck.isSelected());
                            newImage = imageBinary.operateFxImage();
                            break;
                        case Gray:
                            ImageGray imageGray = new ImageGray(imageView.getImage(), parent.scope());
                            newImage = imageGray.operateFxImage();
                            break;
                        case Sepia:
                            pixelsOperation = PixelsOperation.newPixelsOperation(imageView.getImage(), parent.scope(), effectType);
                            pixelsOperation.setIntPara1(intPara1);
                            newImage = pixelsOperation.operateFxImage();
                            value = intPara1 + "";
                            break;
                        case Mosaic: {
                            ImageMosaic mosaic
                                    = ImageMosaic.create(imageView.getImage(), parent.scope(), ImageMosaic.MosaicType.Mosaic, intPara1);
                            newImage = mosaic.operateFxImage();
                            value = intPara1 + "";
                        }
                        break;
                        case FrostedGlass: {
                            ImageMosaic mosaic
                                    = ImageMosaic.create(imageView.getImage(), parent.scope(), ImageMosaic.MosaicType.FrostedGlass, intPara1);
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
                }

                @Override
                protected void whenSucceeded() {
                    parent.updateImage(ImageOperation.Effects, effectType.name(), value, newImage);
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
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

                    int channelSize = (int) Math.round(Math.pow(27, 1.0 / 3.0));
                    ImageQuantization quantization = new ImageQuantization(image);
                    quantization.setScope(scope);
                    quantization.set(QuantizationAlgorithm.HSB_Uniform, channelSize);
                    quantization.setIsDithering(true);
                    bufferedImage = quantization.operateImage();
                    tmpFile = AppVariables.MyBoxTempPath + File.separator
                            + message("Posterizing") + ".png";
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    pixelsOperation = PixelsOperation.newPixelsOperation(
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

                    pixelsOperation = PixelsOperation.newPixelsOperation(
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
                    imageConvolution = new ImageConvolution(image, scope, kernel);
                    bufferedImage = imageConvolution.operateImage();
                    tmpFile = AppVariables.MyBoxTempPath + File.separator
                            + message("EdgeDetection") + ".png";
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    kernel = ConvolutionKernel.makeEmbossKernel(Direction.Top, 3, true);
                    imageConvolution = new ImageConvolution(image, scope, kernel);
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
