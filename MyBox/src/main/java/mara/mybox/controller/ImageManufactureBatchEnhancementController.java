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
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import mara.mybox.data.ConvolutionKernel;
import mara.mybox.db.TableConvolutionKernel;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.image.ImageContrast;
import mara.mybox.image.ImageConvolution;
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
public class ImageManufactureBatchEnhancementController extends ImageManufactureBatchController {

    private OperationType enhanceType;
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
    private ImageContrast.ContrastAlgorithm contrastAlgorithm;
    private ImageConvolution.SmoothAlgorithm smoothAlgorithm;
    private ImageConvolution.SharpenAlgorithm sharpenAlgorithm;

    @FXML
    protected ToggleGroup enhancementGroup;
    @FXML
    protected FlowPane setPane;
    @FXML
    protected RadioButton ContrastRadio, smoothRadio, SharpenRadio, ConvolutionRadio;

    public ImageManufactureBatchEnhancementController() {
        baseTitle = AppVariables.message("ImageManufactureBatchEnhancement");

    }

    @Override
    public void initOptionsSection() {
        try {
            enhancementGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkEnhancementType();
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();

        checkEnhancementType();
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
        intInput = intInput2 = intInput3 = intInput4 = null;
        intLabel = intLabel2 = intLabel3 = intLabel4 = stringLabel = null;
        radio1 = radio2 = radio3 = radio4 = null;
    }

    private void checkEnhancementType() {
        try {
            setPane.getChildren().clear();
            startButton.disableProperty().unbind();
            removeTmpControls();
            stringBox = null;
            radioGroup = null;

            RadioButton selected = (RadioButton) enhancementGroup.getSelectedToggle();
            if (smoothRadio.equals(selected)) {
                enhanceType = OperationType.Smooth;
                makeSmoothBox();

            } else if (SharpenRadio.equals(selected)) {
                enhanceType = OperationType.Sharpen;
                makeSharpenBox();

            } else if (ContrastRadio.equals(selected)) {
                enhanceType = OperationType.Contrast;
                makeContrastBox();

            } else if (ConvolutionRadio.equals(selected)) {
                enhanceType = OperationType.Convolution;
                makeConvolutionBox();

            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void makeSmoothBox() {
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
                            smoothAlgorithm = ImageConvolution.SmoothAlgorithm.AverageBlur;
                        } else {
                            smoothAlgorithm = ImageConvolution.SmoothAlgorithm.GaussianBlur;
                        }
                        FxmlControl.setEditorNormal(stringBox);
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(stringBox);
                    }
                }
            });
            stringBox.getItems().addAll(Arrays.asList(message("AverageBlur"), message("GaussianBlur")));
            stringBox.getSelectionModel().select(message("GaussianBlur"));

            setPane.getChildren().addAll(stringLabel, stringBox, intLabel, intBox);
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

    private void makeSharpenBox() {
        try {
            intPara1 = 2;
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
            intBox.getItems().addAll(Arrays.asList("2", "1", "3", "4", "5"));
            intBox.getSelectionModel().select(0);

            stringLabel = new Label(message("Algorithm"));
            stringBox = new ComboBox<>();
            stringBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        if (message("UnsharpMasking").equals(newValue)) {
                            sharpenAlgorithm = ImageConvolution.SharpenAlgorithm.UnsharpMasking;
                            intBox.setDisable(false);
                        } else if (message("FourNeighborLaplace").equals(newValue)) {
                            sharpenAlgorithm = ImageConvolution.SharpenAlgorithm.FourNeighborLaplace;
                            intBox.setDisable(true);
                        } else if (message("EightNeighborLaplace").equals(newValue)) {
                            sharpenAlgorithm = ImageConvolution.SharpenAlgorithm.EightNeighborLaplace;
                            intBox.setDisable(true);
                        }
                        FxmlControl.setEditorNormal(stringBox);
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(stringBox);
                    }
                }
            });
            stringBox.getItems().addAll(Arrays.asList(
                    message("UnsharpMasking"),
                    message("FourNeighborLaplace"), message("EightNeighborLaplace")
            ));
            stringBox.getSelectionModel().select(message("UnsharpMasking"));

            setPane.getChildren().addAll(stringLabel, stringBox, intLabel, intBox);
            startButton.disableProperty().bind(
                    intBox.getEditor().styleProperty().isEqualTo(badStyle)
                            .or(stringBox.getEditor().styleProperty().isEqualTo(badStyle))
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
                    if (setPane.getChildren() != null) {
                        if (setPane.getChildren().contains(intInput)) {
                            setPane.getChildren().removeAll(intLabel, intInput);
                        }
                        if (setPane.getChildren().contains(intInput2)) {
                            setPane.getChildren().removeAll(intLabel2, intInput2);
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

                        setPane.getChildren().addAll(intLabel, intInput, intLabel2, intInput2);
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
                        setPane.getChildren().addAll(intLabel, intInput);
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

            setPane.getChildren().addAll(stringLabel, stringBox);
            FxmlControl.refreshStyle(setPane);

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

            settingButton = new Button(message("ManageDot"));
            settingButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    BaseController c = openStage(CommonValues.ConvolutionKernelManagerFxml);
                    c.setParentController(myController);
                    c.setParentFxml(myFxml);
                }
            });

            setPane.getChildren().addAll(stringLabel, stringBox, settingButton);
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

    public void applyKernel(ConvolutionKernel kernel) {
        if (enhanceType != OperationType.Convolution || stringBox == null) {
            return;
        }
        ConvolutionRadio.fire();
        if (stringBox.getItems().contains(kernel.getName())) {
            stringBox.getSelectionModel().select(kernel.getName());
        } else {
            stringBox.getSelectionModel().select(-1);
        }
        this.kernel = kernel;
        okAction();
    }

    public void loadKernelsList(List<ConvolutionKernel> records) {
        if (enhanceType != OperationType.Convolution || stringBox == null) {
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
    public boolean makeMoreParameters() {
        if (!super.makeMoreParameters()) {
            return false;
        }

        switch (enhanceType) {
            case Contrast:
                return true;
            case Convolution:
                if (kernel == null) {
                    int index = stringBox.getSelectionModel().getSelectedIndex();
                    if (kernels == null || kernels.isEmpty() || index < 0) {
                        return false;
                    }
                    kernel = kernels.get(index);
                }
                return true;
            case Smooth:
                switch (smoothAlgorithm) {
                    case AverageBlur:
                        kernel = ConvolutionKernel.makeAverageBlur(intPara1);
                        return true;
                    case GaussianBlur:
                        kernel = ConvolutionKernel.makeGaussBlur(intPara1);
                        return true;
                    default:
                        return false;
                }
            case Sharpen:
                switch (sharpenAlgorithm) {
                    case EightNeighborLaplace:
                        kernel = ConvolutionKernel.MakeSharpenEightNeighborLaplace();
                        return true;
                    case FourNeighborLaplace:
                        kernel = ConvolutionKernel.MakeSharpenFourNeighborLaplace();
                        return true;
                    case UnsharpMasking:
                        kernel = ConvolutionKernel.makeUnsharpMasking(intPara1);
                        return true;
                    default:
                        return false;
                }
            default:
                return false;
        }

    }

    @Override
    protected BufferedImage handleImage(BufferedImage source) {
        try {
            BufferedImage target = null;
            ImageConvolution imageConvolution;
            if (null != enhanceType) {
                switch (enhanceType) {
                    case Contrast:
                        ImageContrast imageContrast = new ImageContrast(source, contrastAlgorithm);
                        imageContrast.setIntPara1(intPara1);
                        imageContrast.setIntPara2(intPara2);
                        target = imageContrast.operate();
                        break;
                    case Convolution:
                    case Smooth:
                    case Sharpen:
                        if (kernel == null) {
                            return null;
                        }
                        imageConvolution = ImageConvolution.create().
                                setImage(source).setKernel(kernel);
                        target = imageConvolution.operate();
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
