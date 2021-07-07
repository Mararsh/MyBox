package mara.mybox.controller;

import com.recognition.software.jdeskew.ImageDeskew;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlWindow;
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
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;
import mara.mybox.value.CommonValues;
import net.sourceforge.tess4j.ITessAPI;
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

    protected float scale;
    protected int threshold, rotate;
    protected LoadingController loading;
    protected Process process;

    @FXML
    protected VBox imageOptionsBox, imagesBox;
    @FXML
    protected ScrollPane originalScrollPane;
    @FXML
    protected TextArea textArea;
    @FXML
    protected Label resultLabel, originalViewLabel;
    @FXML
    protected ComboBox<String> rotateSelector, binarySelector, scaleSelector;
    @FXML
    protected CheckBox startCheck, LoadCheck;
    @FXML
    protected ImageView originalView;
    @FXML
    protected HtmlViewerController regionsTableController, wordsTableController, htmlController;
    @FXML
    protected ControlOCROptions ocrOptionsController;
    @FXML
    protected Button demoButton;
    @FXML
    protected TabPane ocrTabPane;
    @FXML
    protected Tab txtTab, regionsTab, wordsTab;
    @FXML
    protected Tab ocrOptionsTab;

    public ImageOCRController() {
        baseTitle = AppVariables.message("ImageOCR");

        TargetPathType = VisitHistory.FileType.Text;
        TargetFileType = VisitHistory.FileType.Text;

        targetPathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.Text);
        targetExtensionFilter = CommonFxValues.TextExtensionFilter;

        needNotRulers = true;
        needNotCoordinates = true;

    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            initImageBox();
            initPreprocessBox();

            ocrOptionsController.setParameters(this, false, true);

            imageOptionsBox.disableProperty().bind(originalView.imageProperty().isNull());
            imagesBox.disableProperty().bind(originalView.imageProperty().isNull());
            rightPane.disableProperty().bind(originalView.imageProperty().isNull());

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
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
                    refineOriginalPane();
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
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
    public boolean afterImageLoaded() {
        try {
            if (!super.afterImageLoaded()) {
                return false;
            }

            originalView.setImage(image);
            originalViewLabel.setText((int) image.getWidth() + " x " + (int) image.getHeight());
            paneSizeOriginal();

            String name = sourceFile != null ? FileTools.getFilePrefix(sourceFile.getName()) : "";
            regionsTableController.baseTitle = name + "_regions";
            wordsTableController.baseTitle = name + "_words";
            htmlController.baseTitle = name + "_texts";

            recoverAction();
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    @FXML
    public void zoomOutOriginal() {
        if (originalScrollPane == null || originalView == null || originalView.getImage() == null) {
            return;
        }
        FxmlControl.zoomOut(originalScrollPane, originalView, xZoomStep, yZoomStep);
    }

    @FXML
    public void zoomInOriginal() {
        if (originalScrollPane == null || originalView == null || originalView.getImage() == null) {
            return;
        }
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
            MyBoxLog.error(e.toString());
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
            MyBoxLog.error(e.toString());
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

            startCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Start", true));
            startCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    AppVariables.setUserConfigValue(baseName + "Start", startCheck.isSelected());
                }
            });

            LoadCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Start", true));
            LoadCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    AppVariables.setUserConfigValue(baseName + "Load", LoadCheck.isSelected());
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void setPreprocessImage(Image image) {
        imageView.setImage(image);
        FxmlControl.paneSize(scrollPane, imageView);
        updateLabelsTitle();
        if (startCheck.isSelected()) {
            startAction();
        }
    }

    @Override
    public void updateLabelsTitle() {
        if (imageView == null || imageView.getImage() == null) {
            return;
        }
        String s = (int) (imageView.getImage().getWidth()) + " x " + (int) (imageView.getImage().getHeight());
        if (maskRectangleLine != null && maskRectangleLine.isVisible() && maskRectangleData != null) {
            s += "  " + message("SelectedSize") + ": "
                    + (int) maskRectangleData.getWidth() + "x" + (int) maskRectangleData.getHeight();
        }
        imageLabel.setText(s);
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
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                private Image changedImage;

                @Override
                protected boolean handle() {
                    try {
                        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(imageView.getImage(), null);
                        bufferedImage = ImageManufacture.scaleImageByScale(bufferedImage, scale);
                        changedImage = SwingFXUtils.toFXImage(bufferedImage, null);
                        return changedImage != null;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    setPreprocessImage(changedImage);

                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @FXML
    protected void binary() {
        if (isSettingValues || imageView.getImage() == null || threshold <= 0) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
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
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @FXML
    protected void popAlgorithmsMenu(MouseEvent mouseEvent) {
        try {
            List<String> algorithms = new ArrayList<>();
            algorithms.addAll(Arrays.asList(
                    message("Deskew"), message("Invert"),
                    message("EdgeDetection") + "-" + message("EightNeighborLaplaceInvert"),
                    message("EdgeDetection") + "-" + message("EightNeighborLaplace"),
                    message("HSBHistogramEqualization"), message("GrayHistogramEqualization"),
                    message("GrayHistogramStretching"), message("GrayHistogramShifting"),
                    message("UnsharpMasking"),
                    message("Enhancement") + "-" + message("EightNeighborLaplace"),
                    message("Enhancement") + "-" + message("FourNeighborLaplace"),
                    message("GaussianBlur"), message("AverageBlur")
            ));

            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menu;
            for (String algorithm : algorithms) {
                menu = new MenuItem(algorithm);
                menu.setOnAction((ActionEvent event) -> {
                    algorithm(algorithm);
                });
                popMenu.getItems().add(menu);
            }

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent event) -> {
                popMenu.hide();
                popMenu = null;
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateBelow((Region) mouseEvent.getSource(), popMenu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void algorithm(String algorithm) {
        if (algorithm == null || isSettingValues || imageView.getImage() == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                private Image ocrImage;

                @Override
                protected boolean handle() {
                    try {
                        if (message("Deskew").equals(algorithm)) {
                            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(imageView.getImage(), null);
                            ImageDeskew id = new ImageDeskew(bufferedImage);
                            double imageSkewAngle = id.getSkewAngle();
                            if ((imageSkewAngle > OCRTools.MINIMUM_DESKEW_THRESHOLD
                                    || imageSkewAngle < -(OCRTools.MINIMUM_DESKEW_THRESHOLD))) {
                                bufferedImage = ImageHelper.rotateImage(bufferedImage, -imageSkewAngle);
                            }
                            ocrImage = SwingFXUtils.toFXImage(bufferedImage, null);

                        } else if (message("Invert").equals(algorithm)) {
                            PixelsOperation pixelsOperation = PixelsOperation.create(imageView.getImage(),
                                    null, PixelsOperation.OperationType.RGB, PixelsOperation.ColorActionType.Invert);
                            ocrImage = pixelsOperation.operateFxImage();

                        } else if (message("GrayHistogramEqualization").equals(algorithm)) {
                            ImageContrast imageContrast = new ImageContrast(imageView.getImage(),
                                    ContrastAlgorithm.Gray_Histogram_Equalization);
                            ocrImage = imageContrast.operateFxImage();

                        } else if (message("GrayHistogramStretching").equals(algorithm)) {
                            ImageContrast imageContrast = new ImageContrast(imageView.getImage(),
                                    ImageContrast.ContrastAlgorithm.Gray_Histogram_Stretching);
                            imageContrast.setIntPara1(50);
                            imageContrast.setIntPara2(50);
                            ocrImage = imageContrast.operateFxImage();

                        } else if (message("GrayHistogramShifting").equals(algorithm)) {
                            ImageContrast imageContrast = new ImageContrast(imageView.getImage(),
                                    ImageContrast.ContrastAlgorithm.Gray_Histogram_Shifting);
                            imageContrast.setIntPara1(10);
                            ocrImage = imageContrast.operateFxImage();

                        } else if (message("HSBHistogramEqualization").equals(algorithm)) {
                            ImageContrast imageContrast = new ImageContrast(imageView.getImage(),
                                    ImageContrast.ContrastAlgorithm.HSB_Histogram_Equalization);
                            ocrImage = imageContrast.operateFxImage();

                        } else if (message("UnsharpMasking").equals(algorithm)) {
                            ConvolutionKernel kernel = ConvolutionKernel.makeUnsharpMasking(3);
                            ImageConvolution imageConvolution = ImageConvolution.create().
                                    setImage(imageView.getImage()).setKernel(kernel);
                            ocrImage = imageConvolution.operateFxImage();

                        } else if ((message("Enhancement") + "-" + message("EightNeighborLaplace")).equals(algorithm)) {
                            ConvolutionKernel kernel = ConvolutionKernel.MakeSharpenEightNeighborLaplace();
                            ImageConvolution imageConvolution = ImageConvolution.create().
                                    setImage(imageView.getImage()).setKernel(kernel);
                            ocrImage = imageConvolution.operateFxImage();

                        } else if ((message("Enhancement") + "-" + message("FourNeighborLaplace")).equals(algorithm)) {
                            ConvolutionKernel kernel = ConvolutionKernel.MakeSharpenFourNeighborLaplace();
                            ImageConvolution imageConvolution = ImageConvolution.create().
                                    setImage(imageView.getImage()).setKernel(kernel);
                            ocrImage = imageConvolution.operateFxImage();

                        } else if (message("GaussianBlur").equals(algorithm)) {
                            ConvolutionKernel kernel = ConvolutionKernel.makeGaussBlur(2);
                            ImageConvolution imageConvolution = ImageConvolution.create().
                                    setImage(imageView.getImage()).setKernel(kernel);
                            ocrImage = imageConvolution.operateFxImage();

                        } else if (message("AverageBlur").equals(algorithm)) {
                            ConvolutionKernel kernel = ConvolutionKernel.makeAverageBlur(1);
                            ImageConvolution imageConvolution = ImageConvolution.create().
                                    setImage(imageView.getImage()).setKernel(kernel);
                            ocrImage = imageConvolution.operateFxImage();

                        } else if ((message("EdgeDetection") + "-" + message("EightNeighborLaplaceInvert")).equals(algorithm)) {
                            ConvolutionKernel kernel = ConvolutionKernel.makeEdgeDetectionEightNeighborLaplaceInvert().setGray(true);
                            ImageConvolution imageConvolution = ImageConvolution.create().
                                    setImage(imageView.getImage()).setKernel(kernel);
                            ocrImage = imageConvolution.operateFxImage();

                        } else if ((message("EdgeDetection") + "-" + message("EightNeighborLaplace")).equals(algorithm)) {
                            ConvolutionKernel kernel = ConvolutionKernel.makeEdgeDetectionEightNeighborLaplace().setGray(true);
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
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @FXML
    protected void demo() {
        if (imageView.getImage() == null) {
            return;
        }
        popInformation(message("WaitAndHandling"), 6000);
        demoButton.setDisable(true);
        Task demoTask = new Task<Void>() {
            private List<String> files;

            @Override
            protected Void call() {

                try {
                    files = new ArrayList<>();
                    BufferedImage image = SwingFXUtils.fromFXImage(imageView.getImage(), null);
                    image = ImageManufacture.scaleImageLess(image, 1000000);

                    ConvolutionKernel kernel = ConvolutionKernel.makeEdgeDetectionEightNeighborLaplaceInvert().setGray(true);
                    ImageConvolution imageConvolution = ImageConvolution.create().
                            setImage(image).setKernel(kernel);
                    BufferedImage bufferedImage = imageConvolution.operateImage();
                    String tmpFile = AppVariables.MyBoxTempPath + File.separator
                            + message("EdgeDetection") + "-" + message("EightNeighborLaplaceInvert") + ".png";
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    kernel = ConvolutionKernel.makeEdgeDetectionEightNeighborLaplace().setGray(true);
                    imageConvolution = ImageConvolution.create().
                            setImage(image).setKernel(kernel);
                    bufferedImage = imageConvolution.operateImage();
                    tmpFile = AppVariables.MyBoxTempPath + File.separator
                            + message("EdgeDetection") + "-" + message("EightNeighborLaplace") + ".png";
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    ImageContrast imageContrast = new ImageContrast(image,
                            ContrastAlgorithm.HSB_Histogram_Equalization);
                    bufferedImage = imageContrast.operateImage();
                    tmpFile = AppVariables.MyBoxTempPath + File.separator
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

                    kernel = ConvolutionKernel.makeUnsharpMasking(3);
                    imageConvolution = ImageConvolution.create().
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
                            + message("Enhancement") + "-" + message("FourNeighborLaplace") + ".png";
                    if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                    }

                    kernel = ConvolutionKernel.MakeSharpenEightNeighborLaplace();
                    imageConvolution = ImageConvolution.create().
                            setImage(image).setKernel(kernel);
                    bufferedImage = imageConvolution.operateImage();
                    tmpFile = AppVariables.MyBoxTempPath + File.separator
                            + message("Enhancement") + "-" + message("EightNeighborLaplace") + ".png";
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

                    PixelsOperation pixelsOperation = PixelsOperation.create(imageView.getImage(),
                            null, PixelsOperation.OperationType.RGB, PixelsOperation.ColorActionType.Invert);
                    bufferedImage = pixelsOperation.operateImage();
                    tmpFile = AppVariables.MyBoxTempPath + File.separator
                            + message("Invert") + ".png";
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
                                    = (ImagesBrowserController) FxmlWindow.openStage(CommonValues.ImagesBrowserFxml);
                            controller.loadFiles(files);
                        } catch (Exception e) {
                            MyBoxLog.error(e.toString());
                        }
                    }
                });

            }

        };
        Thread thread = new Thread(demoTask);
        thread.setDaemon(false);
        thread.start();

    }

    @FXML
    protected void rotate() {
        if (isSettingValues || imageView.getImage() == null || rotate == 0) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
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
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
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
                return VisitHistoryTools.getRecentPath(VisitHistory.FileType.Image);
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
                AppVariables.setUserConfigValue(VisitHistoryTools.getPathKey(VisitHistory.FileType.Image), fname);
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
            if (task != null && !task.isQuit()) {
                return;
            }

            String name = (sourceFile != null ? FileTools.getFilePrefix(sourceFile.getName()) : "") + "_preprocessed";
            final File file = chooseSaveFile(AppVariables.getUserConfigPath(VisitHistoryTools.getPathKey(VisitHistory.FileType.Image)),
                    name, CommonFxValues.ImageExtensionFilter);
            if (file == null) {
                return;
            }
            recordFileWritten(file, VisitHistory.FileType.Image);

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
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    /*
        OCR
     */
    @FXML
    @Override
    public void startAction() {
        ocrOptionsController.setLanguages();
        File dataPath = ocrOptionsController.dataPathController.file;
        if (!dataPath.exists()) {
            popError(message("InvalidParameters"));
            ocrOptionsController.dataPathController.fileInput.setStyle(badStyle);
            return;
        }
        if (ocrOptionsController.embedRadio.isSelected()) {
            embedded();
        } else {
            command();
        }
    }

    protected void command() {
        if (imageView.getImage() == null || timer != null || process != null
                || ocrOptionsController.dataPathController.file == null) {
            return;
        }
        File tesseract = ocrOptionsController.tesseractPathController.file;
        if (!tesseract.exists()) {
            popError(message("InvalidParameters"));
            ocrOptionsController.tesseractPathController.fileInput.setStyle(badStyle);
            return;
        }
        loading = openHandlingStage(Modality.WINDOW_MODAL);
        new Thread() {
            private String outputs = "";

            @Override
            public void run() {
                try {
                    Image selected = cropImage();
                    if (selected == null) {
                        selected = imageView.getImage();
                    }
                    String imageFile = FileTools.getTempFile(".png").getAbsolutePath();
                    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(selected, null);
                    bufferedImage = ImageManufacture.removeAlpha(bufferedImage);
                    ImageFileWriters.writeImageFile(bufferedImage, "png", imageFile);

                    int version = ocrOptionsController.tesseractVersion();
                    String fileBase = FileTools.getTempFile().getAbsolutePath();
                    List<String> parameters = new ArrayList<>();
                    parameters.addAll(Arrays.asList(
                            tesseract.getAbsolutePath(),
                            imageFile, fileBase,
                            "--tessdata-dir", ocrOptionsController.dataPathController.file.getAbsolutePath(),
                            version > 3 ? "--psm" : "-psm", ocrOptionsController.psm + ""
                    ));
                    if (ocrOptionsController.selectedLanguages != null) {
                        parameters.addAll(Arrays.asList("-l", ocrOptionsController.selectedLanguages));
                    }
                    File configFile = FileTools.getTempFile();
                    String s = "tessedit_create_txt 1\n"
                            + "tessedit_create_hocr 1\n";
                    Map<String, String> p = ocrOptionsController.checkParameters();
                    if (p != null) {
                        for (String key : p.keySet()) {
                            s += key + "\t" + p.get(key) + "\n";
                        }
                    }
                    FileTools.writeFile(configFile, s, Charset.forName("utf-8"));
                    parameters.add(configFile.getAbsolutePath());

                    ProcessBuilder pb = new ProcessBuilder(parameters).redirectErrorStream(true);
                    long startTime = new Date().getTime();
                    process = pb.start();
                    try ( BufferedReader inReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                        String line;
                        while ((line = inReader.readLine()) != null) {
                            outputs += line + "\n";
                        }
                    } catch (Exception e) {
                        outputs += e.toString() + "\n";
                    }
                    process.waitFor();

                    String texts;
                    File txtFile = new File(fileBase + ".txt");
                    if (txtFile.exists()) {
                        texts = FileTools.readTexts(txtFile);
                        FileTools.delete(txtFile);
                    } else {
                        texts = null;
                    }
                    String html;
                    File htmlFile = new File(fileBase + ".hocr");
                    if (htmlFile.exists()) {
                        html = FileTools.readTexts(htmlFile);
                        FileTools.delete(htmlFile);
                    } else {
                        html = null;
                    }
                    if (process != null) {
                        process.destroy();
                        process = null;
                    }
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (loading != null) {
                                loading.closeStage();
                                loading = null;
                            }
                            if (texts != null) {
                                textArea.setText(texts);
                                resultLabel.setText(MessageFormat.format(message("OCRresults"),
                                        texts.length(), DateTools.datetimeMsDuration(new Date().getTime() - startTime)));
                                ocrTabPane.getSelectionModel().select(txtTab);
                            } else {
                                if (outputs != null && !outputs.isBlank()) {
                                    alertError(outputs);
                                } else {
                                    popFailed();
                                }
                            }
                            if (html != null) {
                                htmlController.loadHtml(html);
                            }
                        }
                    });

                } catch (Exception e) {
                    MyBoxLog.debug(e.toString());
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (loading != null) {
                                loading.closeStage();
                                loading = null;
                            }
                        }
                    });
                }
            }
        }.start();
    }

    protected void embedded() {
        if (imageView.getImage() == null || ocrOptionsController.dataPathController.file == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
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
                        instance.setPageSegMode(ocrOptionsController.psm);
                        Map<String, String> p = ocrOptionsController.checkParameters();
                        if (p != null && !p.isEmpty()) {
                            for (String key : p.keySet()) {
                                instance.setTessVariable(key, p.get(key));
                            }
                        }
                        instance.setDatapath(ocrOptionsController.dataPathController.file.getAbsolutePath());
                        if (ocrOptionsController.selectedLanguages != null) {
                            instance.setLanguage(ocrOptionsController.selectedLanguages);
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
                        FileTools.delete(tmpFile);
                        instance.createDocumentsWithResults​(bufferedImage, null,
                                tmp, formats, ITessAPI.TessPageIteratorLevel.RIL_SYMBOL);
                        File txtFile = new File(tmp + ".txt");
                        texts = FileTools.readTexts(txtFile);
                        FileTools.delete(txtFile);

                        File htmlFile = new File(tmp + ".hocr");
                        html = FileTools.readTexts(htmlFile);
                        FileTools.delete(htmlFile);

                        if (ocrOptionsController.wordLevel >= 0) {
                            words = instance.getWords(bufferedImage, ocrOptionsController.wordLevel);
                        }

                        if (ocrOptionsController.regionLevel >= 0) {
                            rectangles = instance.getSegmentedRegions(bufferedImage, ocrOptionsController.regionLevel);
                        }

                        return texts != null;
                    } catch (Exception e) {
                        error = e.toString();
                        MyBoxLog.debug(e.toString());
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    if (texts.length() == 0) {
                        popWarn(message("OCRMissComments"));
                    }
                    textArea.setText(texts);
                    resultLabel.setText(MessageFormat.format(message("OCRresults"),
                            texts.length(), DateTools.datetimeMsDuration(cost)));
                    ocrTabPane.getSelectionModel().select(txtTab);

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
                        }
                        regionsTableController.displayHtml();
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
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }

    }

    @FXML
    @Override
    public void saveAsAction() {
        if (textArea.getText().isEmpty()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }

            String name = null;
            if (sourceFile != null) {
                name = FileTools.getFilePrefix(sourceFile.getName());
            }
            final File file = chooseSaveFile(AppVariables.getUserConfigPath(targetPathKey),
                    name, targetExtensionFilter);
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
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @Override
    public boolean checkBeforeNextAction() {
        if (process != null) {
            process.destroy();
            process = null;
        }
        if (loading != null) {
            loading.closeStage();
            loading = null;
        }
        return true;
    }

}
