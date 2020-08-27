package mara.mybox.controller;

import com.recognition.software.jdeskew.ImageDeskew;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import mara.mybox.data.ConvolutionKernel;
import mara.mybox.data.VisitHistory;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.fxml.RecentVisitMenu;
import mara.mybox.image.ImageBinary;
import mara.mybox.image.ImageContrast;
import mara.mybox.image.ImageContrast.ContrastAlgorithm;
import mara.mybox.image.ImageConvolution;
import mara.mybox.image.ImageManufacture;
import mara.mybox.image.PixelsOperation;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.OCRTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;
import mara.mybox.value.CommonValues;
import net.sourceforge.tess4j.ITessAPI.TessPageIteratorLevel;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.Word;
import net.sourceforge.tess4j.util.ImageHelper;

/**
 * @Author Mara
 * @CreateDate 2019-9-17
 * @Description
 * @License Apache License Version 2.0
 */
/*
https://github.com/nguyenq/tess4j
https://github.com/tesseract-ocr/tesseract/wiki/Data-Files
Images intended for OCR should have at least 200 DPI in resolution, typically 300 DPI, 1 bpp (bit per pixel) monochome
or 8 bpp grayscale uncompressed TIFF or PNG format.
PNG is usually smaller in size than other image formats and still keeps high quality due to its employing lossless data compression algorithms;
TIFF has the advantage of the ability to contain multiple images (pages) in a file.
 */
public class ImageOCRController extends ImageViewerController {

    protected String selectedLanguages;
    protected float scale;
    protected int threshold, rotate, regionLevel, wordLevel;
    protected ContrastAlgorithm contrastAlgorithm;

    @FXML
    protected HBox imageOpBox;
    @FXML
    protected VBox imagesBox, resultBox;
    @FXML
    protected TitledPane preprocessPane, ocrOptionsPane;
    @FXML
    protected ScrollPane originalScrollPane;
    @FXML
    protected TextArea textArea;
    @FXML
    protected Label resultLabel, originalViewLabel, currentOCRFilesLabel;
    @FXML
    protected ListView<String> languageList;
    @FXML
    protected ComboBox<String> enhancementSelector, rotateSelector,
            binarySelector, scaleSelector, regionSelector, wordSelector;
    @FXML
    protected CheckBox startCheck, LoadCheck;
    @FXML
    protected ImageView originalView;
    @FXML
    protected HtmlViewerController regionsTableController, wordsTableController;
    @FXML
    protected HtmlViewerController htmlController;
    @FXML
    protected Button demoButton;

    public ImageOCRController() {
        baseTitle = AppVariables.message("ImageOCR");

        TargetPathType = VisitHistory.FileType.Text;
        TargetFileType = VisitHistory.FileType.Text;

        targetPathKey = "TextFilePath";
        targetExtensionFilter = CommonFxValues.TextExtensionFilter;

        needNotRulers = true;
        needNotCoordinates = true;

    }

    @Override
    public void initializeNext2() {
        try {
            initImageBox();
            initPreprocessBox();
            initOCROptionsBox();

            imageOpBox.disableProperty().bind(originalView.imageProperty().isNull());
            imagesBox.disableProperty().bind(originalView.imageProperty().isNull());
            resultBox.disableProperty().bind(originalView.imageProperty().isNull());
            preprocessPane.disableProperty().bind(originalView.imageProperty().isNull());
            ocrOptionsPane.disableProperty().bind(originalView.imageProperty().isNull());

            preprocessPane.expandedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                        AppVariables.setUserConfigValue(baseName + "PreprocessPane", preprocessPane.isExpanded());
                    });
            preprocessPane.setExpanded(AppVariables.getUserConfigBoolean(baseName + "PreprocessPane", false));
            ocrOptionsPane.expandedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                        AppVariables.setUserConfigValue(baseName + "OcrOptionsPane", ocrOptionsPane.isExpanded());
                    });
            ocrOptionsPane.setExpanded(AppVariables.getUserConfigBoolean(baseName + "OcrOptionsPane", false));

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();
            try {
                String lv = AppVariables.getUserConfigValue("ImageOCRLeftPanePosition", "0.15");
                splitPane.setDividerPositions(Double.parseDouble(lv), 0.65);
            } catch (Exception e) {
                splitPane.setDividerPositions(0.15, 0.65);
            }
            leftDividerListener = new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    AppVariables.setUserConfigValue("ImageOCRLeftPanePosition", newValue.doubleValue() + "");
                }
            };
            splitPane.getDividers().get(0).positionProperty().addListener(leftDividerListener);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    /*
        Image
     */
    protected void initImageBox() {
        try {
            originalView.fitWidthProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                    refineOriginalPane();
                }
            });
            originalView.fitHeightProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                    refineOriginalPane();
                }
            });
            originalScrollPane.widthProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                    refineOriginalPane();
                }
            });
            originalScrollPane.heightProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                    viewSizeChanged(Math.abs(new_val.intValue() - old_val.intValue()));
                }
            });

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public void refineOriginalPane() {
        if (originalScrollPane == null || originalView == null || originalView.getImage() == null) {
            return;
        }
        FxmlControl.moveXCenter(originalScrollPane, originalView);
        originalScrollPane.setVvalue(originalScrollPane.getVmin());
    }

    @Override
    public void afterImageLoaded() {
        super.afterImageLoaded();

        originalView.setImage(image);
        originalViewLabel.setText((int) image.getWidth() + " x " + (int) image.getHeight());
        paneSizeOriginal();

        regionsTableController.baseTitle = FileTools.getFilePrefix(sourceFile.getName()) + "_regions";
        wordsTableController.baseTitle = FileTools.getFilePrefix(sourceFile.getName()) + "_words";
        htmlController.baseTitle = FileTools.getFilePrefix(sourceFile.getName()) + "_texts";

        recoverAction();
    }

    @FXML
    public void zoomOutOriginal() {
        if (originalScrollPane == null || originalView == null || originalView.getImage() == null) {
            return;
        }
        isPaneSize = false;
        FxmlControl.zoomOut(originalScrollPane, originalView, xZoomStep, yZoomStep);
    }

    @FXML
    public void zoomInOriginal() {
        if (originalScrollPane == null || originalView == null || originalView.getImage() == null) {
            return;
        }
        isPaneSize = false;
        FxmlControl.zoomIn(originalScrollPane, originalView, xZoomStep, yZoomStep);
    }

    @FXML
    public void paneSizeOriginal() {
        if (originalView == null || originalView.getImage() == null || originalScrollPane == null) {
            return;
        }
        try {
            FxmlControl.paneSize(originalScrollPane, originalView);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void loadedSizeOriginal() {
        if (originalView == null || originalView.getImage() == null || originalScrollPane == null) {
            return;
        }
        try {
            FxmlControl.imageSize(originalScrollPane, originalView);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    /*
        Preprocess
     */
    protected void initPreprocessBox() {
        try {
            scale = 1.0f;
            scaleSelector.getItems().addAll(Arrays.asList(
                    "1.0", "1.5", "2.0", "2.5", "3.0", "5.0", "10.0"
            ));
            scaleSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String oldV, String newV) {
                    try {
                        if (newV == null || newV.isEmpty()) {
                            return;
                        }
                        float f = Float.valueOf(newV);
                        if (f > 0) {
                            scale = f;
                            scaleSelector.getEditor().setStyle(null);
                        } else {
                            scaleSelector.getEditor().setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        scaleSelector.getEditor().setStyle(badStyle);
                    }
                }
            });
            scaleSelector.getSelectionModel().select(0);

            threshold = 0;
            binarySelector.getItems().addAll(Arrays.asList(
                    "65", "50", "75", "45", "30", "80", "85", "15"
            ));
            binarySelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String oldV, String newV) {
                    try {
                        if (newV == null || newV.isEmpty()) {
                            return;
                        }
                        int i = Integer.valueOf(newV);
                        if (i > 0) {
                            threshold = i;
                            binarySelector.getEditor().setStyle(null);
                        } else {
                            binarySelector.getEditor().setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        binarySelector.getEditor().setStyle(badStyle);
                    }
                }
            });

            contrastAlgorithm = null;
            enhancementSelector.getItems().addAll(Arrays.asList(
                    message("HSBHistogramEqualization"), message("GrayHistogramEqualization"),
                    message("GrayHistogramStretching"), message("GrayHistogramShifting"),
                    message("UnsharpMasking"),
                    message("FourNeighborLaplace"), message("EightNeighborLaplace"),
                    message("GaussianBlur"), message("AverageBlur")
            ));

            rotate = 0;
            rotateSelector.getItems().addAll(Arrays.asList(
                    "0", "90", "45", "15", "30", "60", "75", "180", "105", "135", "120", "150", "165", "270", "300", "315"
            ));
            rotateSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String oldV, String newV) {
                    try {
                        if (newV == null || newV.isEmpty()) {
                            return;
                        }
                        rotate = Integer.valueOf(newV);
                    } catch (Exception e) {

                    }
                }
            });

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    protected void setPreprocessImage(Image image) {
        imageView.setImage(image);
        FxmlControl.paneSize(originalScrollPane, imageView);
        String s = (int) image.getWidth() + " x " + (int) image.getHeight();
        if (maskRectangleLine != null && maskRectangleLine.isVisible() && maskRectangleData != null) {
            s += "  " + message("SelectedSize") + ": "
                    + (int) maskRectangleData.getWidth() + "x" + (int) maskRectangleData.getHeight();
        }
        imageLabel.setText(s);
        if (startCheck.isSelected()) {
            startAction();
        }
    }

    @FXML
    @Override
    public void recoverAction() {
        textArea.clear();
        regionsTableController.clear();
        wordsTableController.clear();
        htmlController.clear();
        resultLabel.setText("");

        setPreprocessImage(image);
    }

    @FXML
    protected void scale() {
        if (isSettingValues || imageView.getImage() == null || scale <= 0) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {
                private Image ocrImage;

                @Override
                protected boolean handle() {
                    try {
                        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(imageView.getImage(), null);
                        bufferedImage = ImageManufacture.scaleImage(bufferedImage, scale);
                        ocrImage = SwingFXUtils.toFXImage(bufferedImage, null);
                        return ocrImage != null;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    setPreprocessImage(ocrImage);

                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    protected void binary() {
        if (isSettingValues || imageView.getImage() == null || threshold <= 0) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {
                private Image ocrImage;

                @Override
                protected boolean handle() {
                    try {
                        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(imageView.getImage(), null);
                        ImageBinary bin = new ImageBinary(bufferedImage, threshold);
                        bufferedImage = bin.operateImage();
                        ocrImage = SwingFXUtils.toFXImage(bufferedImage, null);
                        return ocrImage != null;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    setPreprocessImage(ocrImage);

                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    protected void enhance() {
        String enhance = enhancementSelector.getValue();
        if (enhance == null
                || isSettingValues || imageView.getImage() == null) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {
                private Image ocrImage;

                @Override
                protected boolean handle() {
                    try {
                        if (message("GrayHistogramEqualization").equals(enhance)) {
                            ImageContrast imageContrast = new ImageContrast(imageView.getImage(),
                                    ContrastAlgorithm.Gray_Histogram_Equalization);
                            ocrImage = imageContrast.operateFxImage();

                        } else if (message("GrayHistogramStretching").equals(enhance)) {
                            ImageContrast imageContrast = new ImageContrast(imageView.getImage(),
                                    ImageContrast.ContrastAlgorithm.Gray_Histogram_Stretching);
                            imageContrast.setIntPara1(50);
                            imageContrast.setIntPara2(50);
                            ocrImage = imageContrast.operateFxImage();

                        } else if (message("GrayHistogramShifting").equals(enhance)) {
                            ImageContrast imageContrast = new ImageContrast(imageView.getImage(),
                                    ImageContrast.ContrastAlgorithm.Gray_Histogram_Shifting);
                            imageContrast.setIntPara1(10);
                            ocrImage = imageContrast.operateFxImage();

                        } else if (message("HSBHistogramEqualization").equals(enhance)) {
                            ImageContrast imageContrast = new ImageContrast(imageView.getImage(),
                                    ImageContrast.ContrastAlgorithm.HSB_Histogram_Equalization);
                            ocrImage = imageContrast.operateFxImage();

                        } else if (message("UnsharpMasking").equals(enhance)) {
                            ConvolutionKernel kernel = ConvolutionKernel.makeUnsharpMasking(3);
                            ImageConvolution imageConvolution = ImageConvolution.create().
                                    setImage(imageView.getImage()).setKernel(kernel);
                            ocrImage = imageConvolution.operateFxImage();

                        } else if (message("FourNeighborLaplace").equals(enhance)) {
                            ConvolutionKernel kernel = ConvolutionKernel.MakeSharpenFourNeighborLaplace();
                            ImageConvolution imageConvolution = ImageConvolution.create().
                                    setImage(imageView.getImage()).setKernel(kernel);
                            ocrImage = imageConvolution.operateFxImage();

                        } else if (message("EightNeighborLaplace").equals(enhance)) {
                            ConvolutionKernel kernel = ConvolutionKernel.MakeSharpenEightNeighborLaplace();
                            ImageConvolution imageConvolution = ImageConvolution.create().
                                    setImage(imageView.getImage()).setKernel(kernel);
                            ocrImage = imageConvolution.operateFxImage();

                        } else if (message("GaussianBlur").equals(enhance)) {
                            ConvolutionKernel kernel = ConvolutionKernel.makeGaussBlur(2);
                            ImageConvolution imageConvolution = ImageConvolution.create().
                                    setImage(imageView.getImage()).setKernel(kernel);
                            ocrImage = imageConvolution.operateFxImage();

                        } else if (message("AverageBlur").equals(enhance)) {
                            ConvolutionKernel kernel = ConvolutionKernel.makeAverageBlur(1);
                            ImageConvolution imageConvolution = ImageConvolution.create().
                                    setImage(imageView.getImage()).setKernel(kernel);
                            ocrImage = imageConvolution.operateFxImage();

                        }

                        return ocrImage != null;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    setPreprocessImage(ocrImage);

                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    protected void demoEnhance() {
        if (imageView.getImage() == null) {
            return;
        }
        popInformation(message("WaitAndHandling"));
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

                    ConvolutionKernel kernel = ConvolutionKernel.makeUnsharpMasking(3);
                    ImageConvolution imageConvolution = ImageConvolution.create().
                            setImage(image).setKernel(kernel);
                    bufferedImage = imageConvolution.operateImage();
                    tmpFile = AppVariables.MyBoxTempPath + File.separator
                            + message("UnsharpMasking") + ".png";
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    kernel = ConvolutionKernel.MakeSharpenFourNeighborLaplace();
                    imageConvolution = ImageConvolution.create().
                            setImage(image).setKernel(kernel);
                    bufferedImage = imageConvolution.operateImage();
                    tmpFile = AppVariables.MyBoxTempPath + File.separator
                            + message("FourNeighborLaplace") + ".png";
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    kernel = ConvolutionKernel.MakeSharpenEightNeighborLaplace();
                    imageConvolution = ImageConvolution.create().
                            setImage(image).setKernel(kernel);
                    bufferedImage = imageConvolution.operateImage();
                    tmpFile = AppVariables.MyBoxTempPath + File.separator
                            + message("EightNeighborLaplace") + ".png";
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    kernel = ConvolutionKernel.makeGaussBlur(3);
                    imageConvolution = ImageConvolution.create().
                            setImage(image).setKernel(kernel);
                    bufferedImage = imageConvolution.operateImage();
                    tmpFile = AppVariables.MyBoxTempPath + File.separator
                            + message("GaussianBlur") + ".png";
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    kernel = ConvolutionKernel.makeAverageBlur(2);
                    imageConvolution = ImageConvolution.create().
                            setImage(image).setKernel(kernel);
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

    @FXML
    protected void rotate() {
        if (isSettingValues || imageView.getImage() == null || rotate == 0) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {
                private Image ocrImage;

                @Override
                protected boolean handle() {
                    try {
                        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(imageView.getImage(), null);
                        bufferedImage = ImageManufacture.rotateImage(bufferedImage, rotate);
                        ocrImage = SwingFXUtils.toFXImage(bufferedImage, null);
                        return ocrImage != null;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    setPreprocessImage(ocrImage);

                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    public void deskew() {
        if (isSettingValues || imageView.getImage() == null) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {
                private Image ocrImage;

                @Override
                protected boolean handle() {
                    try {
                        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(imageView.getImage(), null);
                        ImageDeskew id = new ImageDeskew(bufferedImage);
                        double imageSkewAngle = id.getSkewAngle();
                        if ((imageSkewAngle > OCRTools.MINIMUM_DESKEW_THRESHOLD
                                || imageSkewAngle < -(OCRTools.MINIMUM_DESKEW_THRESHOLD))) {
                            bufferedImage = ImageHelper.rotateImage(bufferedImage, -imageSkewAngle);
                        }
                        ocrImage = SwingFXUtils.toFXImage(bufferedImage, null);
                        return ocrImage != null;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    setPreprocessImage(ocrImage);
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    protected void invert() {
        if (isSettingValues || imageView.getImage() == null) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {
                private Image ocrImage;

                @Override
                protected boolean handle() {
                    try {
                        PixelsOperation pixelsOperation = PixelsOperation.create(imageView.getImage(),
                                null, PixelsOperation.OperationType.RGB, PixelsOperation.ColorActionType.Invert);
                        ocrImage = pixelsOperation.operateFxImage();
                        return ocrImage != null;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    setPreprocessImage(ocrImage);

                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    public void popSavePreprocess(MouseEvent event) { //
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                return null;
            }

            @Override
            public List<VisitHistory> recentPaths() {
                return VisitHistory.getRecentPath(VisitHistory.FileType.Image);
            }

            @Override
            public void handleSelect() {
                savePreprocessAction();
            }

            @Override
            public void handleFile(String fname) {

            }

            @Override
            public void handlePath(String fname) {
                File file = new File(fname);
                if (!file.exists()) {
                    handleSelect();
                    return;
                }
                AppVariables.setUserConfigValue("ImageFilePath", fname);
                handleSelect();
            }

        }.pop();
    }

    @FXML
    public void savePreprocessAction() {
        if (imageView.getImage() == null) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }

            String name = null;
            if (sourceFile != null) {
                name = FileTools.getFilePrefix(sourceFile.getName()) + "_preprocessed";
            }
            final File file = chooseSaveFile(AppVariables.getUserConfigPath("ImageFilePath"),
                    name, CommonFxValues.ImageExtensionFilter, true);
            if (file == null) {
                return;
            }
            recordFileWritten(file);

            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    String format = FileTools.getFileSuffix(file.getName());
                    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(imageView.getImage(), null);
                    return ImageFileWriters.writeImageFile(bufferedImage, format, file.getAbsolutePath());
                }

                @Override
                protected void whenSucceeded() {
                    if (LoadCheck.isSelected()) {
                        sourceFileChanged(file);
                    }
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    /*
        OCR options
     */
    protected void initOCROptionsBox() {
        try {
            languageList.getItems().clear();
            languageList.getItems().addAll(OCRTools.namesList());
            languageList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            languageList.setPrefHeight(200);
            languageList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
                    checkLanguages();
                }
            });
            selectedLanguages = AppVariables.getUserConfigValue("ImageOCRLanguages", null);
            if (selectedLanguages != null && !selectedLanguages.isEmpty()) {
                currentOCRFilesLabel.setText(
                        MessageFormat.format(message("CurrentDataFiles"), selectedLanguages));
                isSettingValues = true;
                String[] langs = selectedLanguages.split("\\+");
                Map<String, String> codes = OCRTools.codeName();
                for (String code : langs) {
                    String name = codes.get(code);
                    if (name == null) {
                        name = code;
                    }
                    languageList.getSelectionModel().select(name);
                }
                isSettingValues = false;
            } else {
                currentOCRFilesLabel.setText(
                        MessageFormat.format(message("CurrentDataFiles"), ""));
            }

            regionLevel = -1;
            regionSelector.getItems().addAll(Arrays.asList(message("None"),
                    message("Block"), message("Paragraph"), message("Textline"),
                    message("Word"), message("Symbol")
            ));
            regionSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (message("Block").equals(newValue)) {
                        regionLevel = TessPageIteratorLevel.RIL_BLOCK;

                    } else if (message("Paragraph").equals(newValue)) {
                        regionLevel = TessPageIteratorLevel.RIL_PARA;

                    } else if (message("Textline").equals(newValue)) {
                        regionLevel = TessPageIteratorLevel.RIL_TEXTLINE;

                    } else if (message("Word").equals(newValue)) {
                        regionLevel = TessPageIteratorLevel.RIL_WORD;

                    } else if (message("Symbol").equals(newValue)) {
                        regionLevel = TessPageIteratorLevel.RIL_SYMBOL;

                    } else {
                        regionLevel = -1;
                    }
                    AppVariables.setUserConfigValue("ImageOCRRegionLevel", newValue);
                }
            });
            regionSelector.getSelectionModel().select(
                    AppVariables.getUserConfigValue("ImageOCRRegionLevel", message("Symbol")));

            wordLevel = -1;
            wordSelector.getItems().addAll(Arrays.asList(message("None"),
                    message("Block"), message("Paragraph"), message("Textline"),
                    message("Word"), message("Symbol")
            ));
            wordSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (message("Block").equals(newValue)) {
                        wordLevel = TessPageIteratorLevel.RIL_BLOCK;

                    } else if (message("Paragraph").equals(newValue)) {
                        wordLevel = TessPageIteratorLevel.RIL_PARA;

                    } else if (message("Textline").equals(newValue)) {
                        wordLevel = TessPageIteratorLevel.RIL_TEXTLINE;

                    } else if (message("Word").equals(newValue)) {
                        wordLevel = TessPageIteratorLevel.RIL_WORD;

                    } else if (message("Symbol").equals(newValue)) {
                        wordLevel = TessPageIteratorLevel.RIL_SYMBOL;

                    } else {
                        wordLevel = -1;
                    }
                    AppVariables.setUserConfigValue("ImageOCRWordLevel", newValue);
                }
            });
            wordSelector.getSelectionModel().select(
                    AppVariables.getUserConfigValue("ImageOCRWordLevel", message("Symbol")));

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public void checkLanguages() {
        if (isSettingValues) {
            return;
        }
        List<String> langsList = languageList.getSelectionModel().getSelectedItems();
        selectedLanguages = null;
        Map<String, String> names = OCRTools.nameCode();
        for (String name : langsList) {
            String code = names.get(name);
            if (code == null) {
                code = name;
            }
            if (selectedLanguages == null) {
                selectedLanguages = code;
            } else {
                selectedLanguages += "+" + code;
            }
        }
        if (selectedLanguages != null) {
            AppVariables.setUserConfigValue("ImageOCRLanguages", selectedLanguages);
            currentOCRFilesLabel.setText(
                    MessageFormat.format(message("CurrentDataFiles"), selectedLanguages));
        } else {
            currentOCRFilesLabel.setText(
                    MessageFormat.format(message("CurrentDataFiles"), ""));
        }
    }

    @FXML
    public void upAction() {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(languageList.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        isSettingValues = true;
        List<Integer> newselected = new ArrayList<>();
        for (Integer index : selected) {
            if (index == 0 || newselected.contains(index - 1)) {
                newselected.add(index);
                continue;
            }
            String lang = (String) languageList.getItems().get(index);
            languageList.getItems().set(index, languageList.getItems().get(index - 1));
            languageList.getItems().set(index - 1, lang);
            newselected.add(index - 1);
        }
        languageList.getSelectionModel().clearSelection();
        for (int index : newselected) {
            languageList.getSelectionModel().select(index);
        }
        languageList.refresh();
        isSettingValues = false;
        checkLanguages();
    }

    @FXML
    public void downAction() {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(languageList.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        isSettingValues = true;
        List<Integer> newselected = new ArrayList<>();
        for (int i = selected.size() - 1; i >= 0; --i) {
            int index = selected.get(i);
            if (index == languageList.getItems().size() - 1
                    || newselected.contains(index + 1)) {
                newselected.add(index);
                continue;
            }
            String lang = (String) languageList.getItems().get(index);
            languageList.getItems().set(index, languageList.getItems().get(index + 1));
            languageList.getItems().set(index + 1, lang);
            newselected.add(index + 1);
        }
        languageList.getSelectionModel().clearSelection();
        for (int index : newselected) {
            languageList.getSelectionModel().select(index);
        }
        languageList.refresh();
        isSettingValues = false;
        checkLanguages();
    }

    @FXML
    public void topAction() {
        List<Integer> selectedIndices = new ArrayList<>();
        selectedIndices.addAll(languageList.getSelectionModel().getSelectedIndices());
        if (selectedIndices.isEmpty()) {
            return;
        }
        List<String> selected = new ArrayList<>();
        selected.addAll(languageList.getSelectionModel().getSelectedItems());
        isSettingValues = true;
        int size = selectedIndices.size();
        for (int i = size - 1; i >= 0; --i) {
            int index = selectedIndices.get(i);
            languageList.getItems().remove(index);
        }
        languageList.getSelectionModel().clearSelection();
        languageList.getItems().addAll(0, selected);
        languageList.getSelectionModel().selectRange(0, size);
        languageList.refresh();
        isSettingValues = false;
        checkLanguages();
    }

    /*
        OCR
     */
    @FXML
    @Override
    public void startAction() {
        if (imageView.getImage() == null) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                private String texts, html;
                private List<Rectangle> rectangles;
                private List<Word> words;

                @Override
                protected boolean handle() {
                    try {
                        ITesseract instance = new Tesseract();
                        // https://stackoverflow.com/questions/58286373/tess4j-pdf-to-tiff-to-tesseract-warning-invalid-resolution-0-dpi-using-70/58296472#58296472
                        instance.setTessVariable("user_defined_dpi", "96");
                        instance.setTessVariable("debug_file", "/dev/null");
                        String path = AppVariables.getUserConfigValue("TessDataPath", null);
                        if (path != null) {
                            instance.setDatapath(path);
                        }
                        if (selectedLanguages != null) {
                            instance.setLanguage(selectedLanguages);
                        }
                        Image selected = cropImage();
                        if (selected == null) {
                            selected = imageView.getImage();
                        }

                        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(selected, null);
                        bufferedImage = ImageManufacture.removeAlpha(bufferedImage);
                        if (task == null || isCancelled() || bufferedImage == null) {
                            return false;
                        }

                        List<ITesseract.RenderedFormat> formats = new ArrayList<>();
                        formats.add(ITesseract.RenderedFormat.TEXT);
                        formats.add(ITesseract.RenderedFormat.HOCR);

                        File tmpFile = File.createTempFile("MyboxOCR", "");
                        String tmp = File.createTempFile("MyboxOCR", "").getAbsolutePath();
                        tmpFile.delete();
                        instance.createDocumentsWithResults​(bufferedImage, null,
                                tmp, formats, TessPageIteratorLevel.RIL_SYMBOL);
                        File txtFile = new File(tmp + ".txt");
                        texts = FileTools.readTexts(txtFile);
                        txtFile.delete();

                        File htmlFile = new File(tmp + ".hocr");
                        html = FileTools.readTexts(htmlFile);
                        htmlFile.delete();

                        if (wordLevel >= 0) {
                            words = instance.getWords(bufferedImage, wordLevel);
                        }

                        if (regionLevel >= 0) {
                            rectangles = instance.getSegmentedRegions(bufferedImage, regionLevel);
                        }

                        return texts != null;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    if (texts.length() == 0) {
                        popText(message("OCRMissComments"), 5000, "white", "1.1em", null);
                    }
                    textArea.setText(texts);
                    resultLabel.setText(MessageFormat.format(message("OCRresults"),
                            texts.length(), DateTools.datetimeMsDuration(cost)));

                    htmlController.loadHtml(html);

                    if (rectangles != null) {
                        List<String> names = new ArrayList<>();
                        names.addAll(Arrays.asList(message("Index"),
                                message("CoordinateX"), message("CoordinateY"),
                                message("Width"), message("Height")
                        ));
                        regionsTableController.initTable(message(""), names);
                        for (int i = 0; i < rectangles.size(); ++i) {
                            Rectangle rect = rectangles.get(i);
                            List<String> data = new ArrayList<>();
                            data.addAll(Arrays.asList(
                                    i + "", rect.x + "", rect.y + "", rect.width + "", rect.height + ""
                            ));
                            regionsTableController.addData(data);
                            regionsTableController.displayHtml();
                        }
                    } else {
                        regionsTableController.clear();
                    }

                    if (words != null) {
                        List<String> names = new ArrayList<>();
                        names.addAll(Arrays.asList(message("Index"),
                                message("Contents"), message("Confidence"),
                                message("CoordinateX"), message("CoordinateY"),
                                message("Width"), message("Height")
                        ));
                        wordsTableController.initTable(message(""), names);
                        for (int i = 0; i < words.size(); ++i) {
                            Word word = words.get(i);
                            Rectangle rect = word.getBoundingBox();
                            List<String> data = new ArrayList<>();
                            data.addAll(Arrays.asList(
                                    i + "", word.getText(), word.getConfidence() + "",
                                    rect.x + "", rect.y + "", rect.width + "", rect.height + ""
                            ));
                            wordsTableController.addData(data);
                            wordsTableController.displayHtml();
                        }
                    } else {
                        wordsTableController.clear();
                    }

                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }

    }

    @FXML
    public void settingsAction() {
        SettingsController controller = (SettingsController) openStage(CommonValues.SettingsFxml);
        controller.setParentController(this);
        controller.setParentFxml(myFxml);
        controller.tabPane.getSelectionModel().select(controller.ocrTab);
    }

    @FXML
    @Override
    public void saveAsAction() {
        if (textArea.getText().isEmpty()) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }

            String name = null;
            if (sourceFile != null) {
                name = FileTools.getFilePrefix(sourceFile.getName());
            }
            final File file = chooseSaveFile(AppVariables.getUserConfigPath(targetPathKey),
                    name, targetExtensionFilter, true);
            if (file == null) {
                return;
            }
            recordFileWritten(file);

            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    return FileTools.writeFile(file, textArea.getText()) != null;
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

}
