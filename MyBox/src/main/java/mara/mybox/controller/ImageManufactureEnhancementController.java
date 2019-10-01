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
import javafx.stage.Modality;
import mara.mybox.controller.ImageManufactureController.ImageOperation;
import mara.mybox.data.ConvolutionKernel;
import mara.mybox.db.TableConvolutionKernel;
import mara.mybox.fxml.ControlStyle;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.image.ImageContrast;
import mara.mybox.image.ImageContrast.ContrastAlgorithm;
import mara.mybox.image.ImageConvolution;
import mara.mybox.image.ImageConvolution.SharpenAlgorithm;
import mara.mybox.image.ImageConvolution.SmoothAlgorithm;
import mara.mybox.image.ImageManufacture;
import mara.mybox.image.ImageScope;
import mara.mybox.image.PixelsOperation.OperationType;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2019-9-29
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureEnhancementController extends ImageManufactureOperationController {

    private OperationType enhanceType;
    protected int intPara1, intPara2, intPara3;
    private List<ConvolutionKernel> kernels;
    private ConvolutionKernel kernel;
    private ContrastAlgorithm contrastAlgorithm;
    private SmoothAlgorithm smoothAlgorithm;
    private SharpenAlgorithm sharpenAlgorithm;
    private ChangeListener<String> intBoxListener, stringBoxListener, intInputListener,
            intInput2Listener, intInput3Listener;
    private ChangeListener<Number> numberBoxListener;
    private ImageView manageView;

    @FXML
    protected ToggleGroup enhancementGroup;
    @FXML
    protected RadioButton ContrastRadio, smoothRadio, SharpenRadio, ConvolutionRadio;
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

    public ImageManufactureEnhancementController() {
        baseTitle = AppVariables.message("ImageManufactureEnhancement");
        operation = ImageOperation.Enhancement;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            myPane = enhancementPane;

            enhancementGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkEnhanceType();
                }
            });

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

            checkEnhanceType();

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
        intBox.setDisable(false);
    }

    private void checkEnhanceType() {
        try {
            clearValues();
            if (enhancementGroup.getSelectedToggle() == null) {
                okButton.setDisable(true);
                return;
            }
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

            FxmlControl.refreshStyle(setBox);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void makeSmoothBox() {
        try {
            stringLabel.setText(message("Algorithm"));
            stringBoxListener = new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        if (message("AverageBlur").equals(newValue)) {
                            smoothAlgorithm = SmoothAlgorithm.AverageBlur;
                        } else {
                            smoothAlgorithm = SmoothAlgorithm.GaussianBlur;
                        }
                        FxmlControl.setEditorNormal(stringBox);
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(stringBox);
                    }
                }
            };
            stringBox.getSelectionModel().selectedItemProperty().addListener(stringBoxListener);
            stringBox.getItems().addAll(Arrays.asList(
                    message("GaussianBlur"), message("AverageBlur")
            ));
            stringBox.getSelectionModel().select(message("GaussianBlur"));

            intPara1 = 10;
            intLabel.setText(message("Radius"));
            intBox.setEditable(true);

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
            intBox.getItems().addAll(Arrays.asList("3", "5", "10", "2", "1", "8", "15", "20", "30"));
            intBox.getSelectionModel().select(0);

            setBox.getChildren().addAll(stringLabel, stringBox, intLabel, intBox);
            okButton.disableProperty().bind(
                    intBox.getEditor().styleProperty().isEqualTo(badStyle)
                            .or(stringBox.getEditor().styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void makeSharpenBox() {
        try {
            stringLabel.setText(message("Algorithm"));
            stringBoxListener = new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        if (message("UnsharpMasking").equals(newValue)) {
                            sharpenAlgorithm = SharpenAlgorithm.UnsharpMasking;
                            intBox.setDisable(false);
                        } else if (message("FourNeighborLaplace").equals(newValue)) {
                            sharpenAlgorithm = SharpenAlgorithm.FourNeighborLaplace;
                            intBox.setDisable(true);
                        } else if (message("EightNeighborLaplace").equals(newValue)) {
                            sharpenAlgorithm = SharpenAlgorithm.EightNeighborLaplace;
                            intBox.setDisable(true);
                        }
                        FxmlControl.setEditorNormal(stringBox);
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(stringBox);
                    }
                }
            };
            stringBox.getSelectionModel().selectedItemProperty().addListener(stringBoxListener);
            stringBox.getItems().addAll(Arrays.asList(
                    message("UnsharpMasking"),
                    message("FourNeighborLaplace"), message("EightNeighborLaplace")
            ));
            stringBox.getSelectionModel().select(message("UnsharpMasking"));

            intPara1 = 2;
            intLabel.setText(message("Radius"));
            intBox.setEditable(true);

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
            intBox.getItems().addAll(Arrays.asList("2", "1", "3", "4", "5"));
            intBox.getSelectionModel().select(0);

            setBox.getChildren().addAll(stringLabel, stringBox, intLabel, intBox);
            okButton.disableProperty().bind(
                    intBox.getEditor().styleProperty().isEqualTo(badStyle)
                            .or(stringBox.getEditor().styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void makeConvolutionBox() {
        try {
            stringLabel.setText(message("ConvolutionKernel"));
            kernel = null;
            if (kernels == null) {
                kernels = TableConvolutionKernel.read();
            }
            loadKernelsList(kernels);
            numberBoxListener = new ChangeListener<Number>() {
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
            };
            stringBox.getSelectionModel().selectedIndexProperty().addListener(numberBoxListener);

            manageView.setImage(new Image(ControlStyle.getIcon("iconSetting.png")));
            manageView.setFitWidth(20);
            manageView.setFitHeight(20);
            button.setGraphic(manageView);
            button.setText("");
            FxmlControl.setTooltip(button, new Tooltip(message("ManageDot")));
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    BaseController c = openStage(CommonValues.ConvolutionKernelManagerFxml);
                    c.setParentController(myController);
                    c.setParentFxml(myFxml);
                }
            });

            setBox.getChildren().addAll(stringLabel, stringBox, button);
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
            stringLabel.setText(message("Algorithm"));
            stringBoxListener = new ChangeListener<String>() {
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
                    if (message("GrayHistogramEqualization").equals(newValue)) {
                        contrastAlgorithm = ContrastAlgorithm.Gray_Histogram_Equalization;
                    } else if (message("GrayHistogramStretching").equals(newValue)) {
                        contrastAlgorithm = ContrastAlgorithm.Gray_Histogram_Stretching;
                        intPara1 = 100;
                        intLabel.setText(message("LeftThreshold"));
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
                        okButton.disableProperty().bind(
                                intInput.styleProperty().isEqualTo(badStyle)
                                        .or(intInput2.styleProperty().isEqualTo(badStyle))
                                        .or(stringBox.getEditor().styleProperty().isEqualTo(badStyle))
                        );
                    } else if (message("GrayHistogramShifting").equals(newValue)) {
                        contrastAlgorithm = ContrastAlgorithm.Gray_Histogram_Shifting;
                        intPara1 = 80;
                        intLabel.setText(message("Offset"));
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

                        intInput.setText("10");
                        FxmlControl.setTooltip(intInput, new Tooltip("-255 ~ 255"));
                        setBox.getChildren().addAll(intLabel, intInput);
                        okButton.disableProperty().bind(
                                intInput.styleProperty().isEqualTo(badStyle)
                                        .or(stringBox.getEditor().styleProperty().isEqualTo(badStyle))
                        );
                    } else if (message("LumaHistogramEqualization").equals(newValue)) {
                        contrastAlgorithm = ContrastAlgorithm.Luma_Histogram_Equalization;
                    } else if (message("HSBHistogramEqualization").equals(newValue)) {
                        contrastAlgorithm = ContrastAlgorithm.HSB_Histogram_Equalization;
                    } else if (message("AdaptiveHistogramEqualization").equals(newValue)) {
                        contrastAlgorithm = ContrastAlgorithm.Adaptive_Histogram_Equalization;
                    }
                }
            };
            stringBox.getSelectionModel().selectedItemProperty().addListener(stringBoxListener);
            stringBox.getItems().addAll(Arrays.asList(message("HSBHistogramEqualization"),
                    message("GrayHistogramEqualization"),
                    message("GrayHistogramStretching"),
                    message("GrayHistogramShifting")
            //                    getMessage("LumaHistogramEqualization"),
            //                    getMessage("AdaptiveHistogramEqualization")
            ));
            stringBox.getSelectionModel().select(0);

            setBox.getChildren().addAll(stringLabel, stringBox);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void applyKernel(ConvolutionKernel kernel) {
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
            FxmlControl.setEditorNormal(stringBox);
        } else {
            FxmlControl.setEditorBadStyle(stringBox);
        }
    }

    @FXML
    @Override
    public void okAction() {
        if (parent == null || enhanceType == null) {
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
                    ImageConvolution imageConvolution;
                    switch (enhanceType) {
                        case Contrast:
                            ImageContrast imageContrast = new ImageContrast(imageView.getImage(), contrastAlgorithm);
                            imageContrast.setIntPara1(intPara1);
                            imageContrast.setIntPara2(intPara2);
                            newImage = imageContrast.operateFxImage();
                            break;
                        case Convolution:
                            if (kernel == null) {
                                int index = stringBox.getSelectionModel().getSelectedIndex();
                                if (kernels == null || kernels.isEmpty() || index < 0) {
                                    return false;
                                }
                                kernel = kernels.get(index);
                            }
                            imageConvolution = new ImageConvolution(imageView.getImage(), parent.scope(), kernel);
                            newImage = imageConvolution.operateFxImage();
                            break;
                        case Smooth:
                            switch (smoothAlgorithm) {
                                case AverageBlur:
                                    kernel = ConvolutionKernel.makeAverageBlur(intPara1);
                                    break;
                                case GaussianBlur:
                                    kernel = ConvolutionKernel.makeGaussBlur(intPara1);
                                    break;
                                default:
                                    return false;
                            }
                            imageConvolution = new ImageConvolution(imageView.getImage(), parent.scope(), kernel);
                            newImage = imageConvolution.operateFxImage();
                            value = intPara1 + "";
                            break;
                        case Sharpen:
                            switch (sharpenAlgorithm) {
                                case EightNeighborLaplace:
                                    kernel = ConvolutionKernel.MakeSharpenEightNeighborLaplace();
                                    break;
                                case FourNeighborLaplace:
                                    kernel = ConvolutionKernel.MakeSharpenFourNeighborLaplace();
                                    break;
                                case UnsharpMasking:
                                    kernel = ConvolutionKernel.makeUnsharpMasking(intPara1);
                                    break;
                                default:
                                    return false;
                            }
                            imageConvolution = new ImageConvolution(imageView.getImage(), parent.scope(), kernel);
                            newImage = imageConvolution.operateFxImage();
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
                    parent.updateImage(ImageOperation.Effects, enhanceType.name(), value, newImage);
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

                    ImageContrast imageContrast = new ImageContrast(image,
                            ContrastAlgorithm.HSB_Histogram_Equalization);
                    BufferedImage bufferedImage = imageContrast.operateImage();
                    String tmpFile = AppVariables.MyBoxTempPath + File.separator
                            + message("HSBHistogramEqualization") + ".png";
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    imageContrast = new ImageContrast(image,
                            ContrastAlgorithm.Gray_Histogram_Equalization);
                    bufferedImage = imageContrast.operateImage();
                    tmpFile = AppVariables.MyBoxTempPath + File.separator
                            + message("GrayHistogramEqualization") + ".png";
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    imageContrast = new ImageContrast(image,
                            ContrastAlgorithm.Gray_Histogram_Stretching);
                    imageContrast.setIntPara1(100);
                    imageContrast.setIntPara2(100);
                    bufferedImage = imageContrast.operateImage();
                    tmpFile = AppVariables.MyBoxTempPath + File.separator
                            + message("GrayHistogramStretching") + ".png";
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    imageContrast = new ImageContrast(image,
                            ContrastAlgorithm.Gray_Histogram_Shifting);
                    imageContrast.setIntPara1(40);
                    bufferedImage = imageContrast.operateImage();
                    tmpFile = AppVariables.MyBoxTempPath + File.separator
                            + message("GrayHistogramShifting") + ".png";
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

//                    BufferedImage outlineSource = SwingFXUtils.fromFXImage(new Image("img/ImageTools.png"), null);
//                    ImageScope scope = new ImageScope(SwingFXUtils.toFXImage(image, null));
//                    scope.setScopeType(ImageScope.ScopeType.Outline);
//                    if (sourceFile != null) {
//                        scope.setFile(sourceFile.getAbsolutePath());
//                    }
//                    BufferedImage[] outline = ImageManufacture.outline(outlineSource,
//                            scope.getRectangle(), image.getWidth(), image.getHeight(),
//                            true, ImageColor.converColor(Color.WHITE), false);
//                    scope.setOutlineSource(outlineSource);
//                    scope.setOutline(outline[1]);
                    ImageScope scope = null;

                    ConvolutionKernel kernel = ConvolutionKernel.makeUnsharpMasking(3);
                    ImageConvolution imageConvolution
                            = new ImageConvolution(image, scope, kernel);
                    bufferedImage = imageConvolution.operateImage();
                    tmpFile = AppVariables.MyBoxTempPath + File.separator
                            + message("UnsharpMasking") + ".png";
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    kernel = ConvolutionKernel.MakeSharpenFourNeighborLaplace();
                    imageConvolution
                            = new ImageConvolution(image, null, kernel);
                    bufferedImage = imageConvolution.operateImage();
                    tmpFile = AppVariables.MyBoxTempPath + File.separator
                            + message("FourNeighborLaplace") + ".png";
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    kernel = ConvolutionKernel.MakeSharpenEightNeighborLaplace();
                    imageConvolution
                            = new ImageConvolution(image, scope, kernel);
                    bufferedImage = imageConvolution.operateImage();
                    tmpFile = AppVariables.MyBoxTempPath + File.separator
                            + message("EightNeighborLaplace") + ".png";
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    kernel = ConvolutionKernel.makeGaussBlur(3);
                    imageConvolution
                            = new ImageConvolution(image, scope, kernel);
                    bufferedImage = imageConvolution.operateImage();
                    tmpFile = AppVariables.MyBoxTempPath + File.separator
                            + message("GaussianBlur") + ".png";
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    kernel = ConvolutionKernel.makeAverageBlur(3);
                    imageConvolution
                            = new ImageConvolution(image, scope, kernel);
                    bufferedImage = imageConvolution.operateImage();
                    tmpFile = AppVariables.MyBoxTempPath + File.separator
                            + message("AverageBlur") + ".png";
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
