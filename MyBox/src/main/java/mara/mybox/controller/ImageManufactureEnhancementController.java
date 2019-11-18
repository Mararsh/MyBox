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
import javafx.scene.layout.VBox;
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
    private ChangeListener<String> intBoxListener, stringBoxListener,
            intInput1Listener, intInput2Listener;
    private ChangeListener<Number> numberBoxListener;
    private ImageView manageView;

    @FXML
    protected ToggleGroup enhancementGroup;
    @FXML
    protected RadioButton ContrastRadio, smoothRadio, SharpenRadio, ConvolutionRadio;
    @FXML
    protected TextField intInput1, intInput2;
    @FXML
    protected VBox setBox;
    @FXML
    protected FlowPane stringSelectorPane, intSelectorPane, intInput1Pane, intInput2Pane;
    @FXML
    protected ComboBox<String> intSelector, stringSelector;
    @FXML
    protected CheckBox valueCheck;
    @FXML
    protected Label intListLabel, stringLabel, intLabel1, intLabel2;
    @FXML
    protected Button button, demoButton;

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
            stringSelector.getSelectionModel().selectedItemProperty().removeListener(stringBoxListener);
        }
        if (numberBoxListener != null) {
            stringSelector.getSelectionModel().selectedIndexProperty().removeListener(numberBoxListener);
        }
        if (intBoxListener != null) {
            intSelector.getSelectionModel().selectedItemProperty().removeListener(intBoxListener);
        }
        if (intInput1Listener != null) {
            intInput1.textProperty().removeListener(intInput1Listener);
        }
        if (intInput2Listener != null) {
            intInput2.textProperty().removeListener(intInput2Listener);
        }
        valueCheck.setDisable(false);
        button.setOnAction(null);
        button.disableProperty().unbind();
        button.setDisable(false);
        okButton.disableProperty().unbind();
        okButton.setDisable(false);
        stringSelector.getItems().clear();
        stringSelector.getEditor().setStyle(null);
        intSelector.getItems().clear();
        intSelector.getEditor().setStyle(null);
        intInput1.setStyle(null);
        intInput2.setStyle(null);
        stringSelector.setEditable(false);
        intSelector.setEditable(false);
        intSelector.setDisable(false);
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
                        for (SmoothAlgorithm a : SmoothAlgorithm.values()) {
                            if (message(a.name()).equals(newValue)) {
                                smoothAlgorithm = a;
                                break;
                            }
                        }
                        FxmlControl.setEditorNormal(stringSelector);
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(stringSelector);
                    }
                }
            };
            stringSelector.getSelectionModel().selectedItemProperty().addListener(stringBoxListener);
            for (SmoothAlgorithm a : SmoothAlgorithm.values()) {
                stringSelector.getItems().add(message(a.name()));
            }
            stringSelector.getSelectionModel().select(0);

            intPara1 = 10;
            intListLabel.setText(message("Intensity"));
            intSelector.setEditable(true);

            intBoxListener = new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            intPara1 = v;
                            FxmlControl.setEditorNormal(intSelector);
                        } else {
                            FxmlControl.setEditorBadStyle(intSelector);
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(intSelector);
                    }
                }
            };
            intSelector.getSelectionModel().selectedItemProperty().addListener(intBoxListener);
            intSelector.getItems().addAll(Arrays.asList("3", "5", "10", "2", "1", "8", "15", "20", "30"));
            intSelector.getSelectionModel().select(0);

            setBox.getChildren().addAll(stringSelectorPane, intSelectorPane);
            okButton.disableProperty().bind(intSelector.getEditor().styleProperty().isEqualTo(badStyle)
                    .or(stringSelector.getEditor().styleProperty().isEqualTo(badStyle))
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
                        for (SharpenAlgorithm a : SharpenAlgorithm.values()) {
                            if (message(a.name()).equals(newValue)) {
                                sharpenAlgorithm = a;
                                break;
                            }
                        }
                        intSelector.setDisable(sharpenAlgorithm != SharpenAlgorithm.UnsharpMasking);
                        FxmlControl.setEditorNormal(stringSelector);
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(stringSelector);
                    }
                }
            };
            stringSelector.getSelectionModel().selectedItemProperty().addListener(stringBoxListener);
            for (SharpenAlgorithm a : SharpenAlgorithm.values()) {
                stringSelector.getItems().add(message(a.name()));
            }
            stringSelector.getSelectionModel().select(0);

            intPara1 = 2;
            intListLabel.setText(message("Intensity"));
            intSelector.setEditable(true);
            intBoxListener = new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            intPara1 = v;
                            FxmlControl.setEditorNormal(intSelector);
                        } else {
                            FxmlControl.setEditorBadStyle(intSelector);
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(intSelector);
                    }
                }
            };
            intSelector.getSelectionModel().selectedItemProperty().addListener(intBoxListener);
            intSelector.getItems().addAll(Arrays.asList("2", "1", "3", "4", "5"));
            intSelector.getSelectionModel().select(0);

            setBox.getChildren().addAll(stringSelectorPane, intSelectorPane);
            okButton.disableProperty().bind(intSelector.getEditor().styleProperty().isEqualTo(badStyle)
                    .or(stringSelector.getEditor().styleProperty().isEqualTo(badStyle))
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
                        FxmlControl.setEditorBadStyle(stringSelector);
                        return;
                    }
                    kernel = kernels.get(index);
                    FxmlControl.setEditorNormal(stringSelector);
                }
            };
            stringSelector.getSelectionModel().selectedIndexProperty().addListener(numberBoxListener);

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

            setBox.getChildren().addAll(stringSelectorPane, button);
            okButton.disableProperty().bind(stringSelector.getEditor().styleProperty().isEqualTo(badStyle)
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    private void makeContrastBox() {
        try {
            setBox.getChildren().addAll(stringSelectorPane);
            contrastAlgorithm = ContrastAlgorithm.HSB_Histogram_Equalization;
            stringLabel.setText(message("Algorithm"));
            stringSelector.getItems().addAll(Arrays.asList(message("HSBHistogramEqualization"),
                    message("GrayHistogramEqualization"),
                    message("GrayHistogramStretching"),
                    message("GrayHistogramShifting")
            //                    getMessage("LumaHistogramEqualization"),
            //                    getMessage("AdaptiveHistogramEqualization")
            ));

            stringBoxListener = new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (setBox.getChildren() != null) {
                        if (setBox.getChildren().contains(intInput1Pane)) {
                            setBox.getChildren().removeAll(intInput1Pane);
                        }
                        if (setBox.getChildren().contains(intInput2Pane)) {
                            setBox.getChildren().removeAll(intInput2Pane);
                        }
                    }
                    okButton.disableProperty().unbind();
                    if (message("GrayHistogramEqualization").equals(newValue)) {
                        contrastAlgorithm = ContrastAlgorithm.Gray_Histogram_Equalization;

                    } else if (message("GrayHistogramStretching").equals(newValue)) {
                        contrastAlgorithm = ContrastAlgorithm.Gray_Histogram_Stretching;
                        intPara1 = 100;
                        intLabel1.setText(message("LeftThreshold"));
                        intInput1.textProperty().addListener(new ChangeListener<String>() {
                            @Override
                            public void changed(ObservableValue<? extends String> observable,
                                    String oldValue, String newValue) {
                                try {
                                    int v = Integer.valueOf(intInput1.getText());
                                    if (v >= 0) {
                                        intPara1 = v;
                                        intInput1.setStyle(null);
                                    } else {
                                        intInput1.setStyle(badStyle);
                                    }
                                } catch (Exception e) {
                                    intInput1.setStyle(badStyle);
                                }
                            }
                        });
                        intInput1.setText("100");

                        intPara2 = 100;
                        intLabel2.setText(message("RightThreshold"));
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

                        setBox.getChildren().addAll(intInput1Pane, intInput2Pane);
                        okButton.disableProperty().bind(intInput1.styleProperty().isEqualTo(badStyle)
                                .or(intInput2.styleProperty().isEqualTo(badStyle))
                                .or(stringSelector.getEditor().styleProperty().isEqualTo(badStyle))
                        );

                    } else if (message("GrayHistogramShifting").equals(newValue)) {
                        contrastAlgorithm = ContrastAlgorithm.Gray_Histogram_Shifting;
                        intPara1 = 80;
                        intLabel1.setText(message("Offset"));
                        intInput1.textProperty().addListener(new ChangeListener<String>() {
                            @Override
                            public void changed(ObservableValue<? extends String> observable,
                                    String oldValue, String newValue) {
                                try {
                                    int v = Integer.valueOf(intInput1.getText());
                                    if (v >= -255 && v <= 255) {
                                        intPara1 = v;
                                        intInput1.setStyle(null);
                                    } else {
                                        intInput1.setStyle(badStyle);
                                        popError("-255 ~ 255");
                                    }
                                } catch (Exception e) {
                                    intInput1.setStyle(badStyle);
                                    popError("-255 ~ 255");
                                }
                            }
                        });
                        intInput1.setText("10");
                        FxmlControl.setTooltip(intInput1, new Tooltip("-255 ~ 255"));
                        setBox.getChildren().addAll(intInput1Pane);
                        okButton.disableProperty().bind(intInput1.styleProperty().isEqualTo(badStyle)
                                .or(stringSelector.getEditor().styleProperty().isEqualTo(badStyle))
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
            stringSelector.getSelectionModel().selectedItemProperty().addListener(stringBoxListener);
            stringSelector.getSelectionModel().select(0);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void applyKernel(ConvolutionKernel kernel) {
        ConvolutionRadio.fire();
        if (stringSelector.getItems().contains(kernel.getName())) {
            stringSelector.getSelectionModel().select(kernel.getName());
        } else {
            stringSelector.getSelectionModel().select(-1);
        }
        this.kernel = kernel;
        okAction();
    }

    public void loadKernelsList(List<ConvolutionKernel> records) {
        if (enhanceType != OperationType.Convolution || stringSelector == null) {
            return;
        }
        kernels = records;
        stringSelector.getItems().clear();
        if (kernels != null && !kernels.isEmpty()) {
            List<String> names = new ArrayList<>();
            for (ConvolutionKernel k : kernels) {
                names.add(k.getName());
            }
            stringSelector.getItems().addAll(names);
            stringSelector.getSelectionModel().select(0);
            FxmlControl.setEditorNormal(stringSelector);
        } else {
            FxmlControl.setEditorBadStyle(stringSelector);
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
                                int index = stringSelector.getSelectionModel().getSelectedIndex();
                                if (kernels == null || kernels.isEmpty() || index < 0) {
                                    return false;
                                }
                                kernel = kernels.get(index);
                            }
                            imageConvolution = ImageConvolution.create().
                                    setImage(imageView.getImage()).setScope(parent.scope()).
                                    setKernel(kernel);
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
                            imageConvolution = ImageConvolution.create().
                                    setImage(imageView.getImage()).setScope(parent.scope()).
                                    setKernel(kernel);
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
                            imageConvolution = ImageConvolution.create().
                                    setImage(imageView.getImage()).setScope(parent.scope()).
                                    setKernel(kernel);
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
                    parent.updateImage(ImageOperation.Effects, enhanceType.name(), value, newImage, cost);
                }
            };
            parent.openHandlingStage(task, Modality.WINDOW_MODAL);
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
        parent.popInformation(message("WaitAndHandling"));
        demoButton.setDisable(true);
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
                    ImageConvolution imageConvolution = ImageConvolution.create().
                            setImage(image).setScope(scope).setKernel(kernel);
                    bufferedImage = imageConvolution.operateImage();
                    tmpFile = AppVariables.MyBoxTempPath + File.separator
                            + message("UnsharpMasking") + ".png";
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    kernel = ConvolutionKernel.MakeSharpenFourNeighborLaplace();
                    imageConvolution = ImageConvolution.create().
                            setImage(image).setScope(scope).setKernel(kernel);
                    bufferedImage = imageConvolution.operateImage();
                    tmpFile = AppVariables.MyBoxTempPath + File.separator
                            + message("FourNeighborLaplace") + ".png";
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    kernel = ConvolutionKernel.MakeSharpenEightNeighborLaplace();
                    imageConvolution = ImageConvolution.create().
                            setImage(image).setScope(scope).setKernel(kernel);
                    bufferedImage = imageConvolution.operateImage();
                    tmpFile = AppVariables.MyBoxTempPath + File.separator
                            + message("EightNeighborLaplace") + ".png";
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    kernel = ConvolutionKernel.makeGaussBlur(3);
                    imageConvolution = ImageConvolution.create().
                            setImage(image).setScope(scope).setKernel(kernel);
                    bufferedImage = imageConvolution.operateImage();
                    tmpFile = AppVariables.MyBoxTempPath + File.separator
                            + message("GaussianBlur") + ".png";
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    kernel = ConvolutionKernel.makeAverageBlur(3);
                    imageConvolution = ImageConvolution.create().
                            setImage(image).setScope(scope).setKernel(kernel);
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
