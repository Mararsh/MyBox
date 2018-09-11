package mara.mybox.controller;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.stage.FileChooser;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.image.ImageGrayTools;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.CommonValues;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.FxmlTools;
import static mara.mybox.tools.FxmlTools.badStyle;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.image.CCITTFactory;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

/**
 * @Author Mara
 * @CreateDate 2018-9-10
 * @Description
 * @License Apache License Version 2.0
 */
public class PdfCompressImagesController extends PdfBaseController {

    private File targetFile;
    private int jpegQuality, format, threshold;

    @FXML
    private ToggleGroup formatGroup;
    @FXML
    private ComboBox<String> jpegBox;
    @FXML
    private TextField thresholdInput, authorInput;

    public static class PdfImageFormat {

        public static int Tiff = 1;
        public static int Jpeg = 2;

    }

    public PdfCompressImagesController() {

    }

    @Override
    protected void initializeNext2() {

        allowPaused = false;

        initOptionsSection();
        initTargetSection();

        operationBarController.startButton.disableProperty().bind(
                Bindings.isEmpty(sourceSelectionController.sourceFileInput.textProperty())
                        .or(Bindings.isEmpty(sourceSelectionController.fromPageInput.textProperty()))
                        .or(Bindings.isEmpty(sourceSelectionController.toPageInput.textProperty()))
                        .or(sourceSelectionController.sourceFileInput.styleProperty().isEqualTo(badStyle))
                        .or(sourceSelectionController.fromPageInput.styleProperty().isEqualTo(badStyle))
                        .or(sourceSelectionController.toPageInput.styleProperty().isEqualTo(badStyle))
                        .or(Bindings.isEmpty(targetFileInput.textProperty()))
                        .or(targetFileInput.styleProperty().isEqualTo(badStyle))
                        .or(jpegBox.styleProperty().isEqualTo(badStyle))
                        .or(thresholdInput.styleProperty().isEqualTo(badStyle))
        );
    }

    private void initOptionsSection() {

        formatGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkFormat();
            }
        });
        checkFormat();

        jpegBox.getItems().addAll(Arrays.asList(
                "100",
                "75",
                "90",
                "50",
                "60",
                "80",
                "30",
                "10"
        ));
        jpegBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov,
                    String oldValue, String newValue) {
                checkJpegQuality();
            }
        });
        jpegBox.getSelectionModel().select(0);
        checkJpegQuality();

        thresholdInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                checkThreshold();
            }
        });
        checkThreshold();

    }

    private void checkFormat() {
        jpegBox.setDisable(true);
        jpegBox.setStyle(null);
        thresholdInput.setDisable(true);

        RadioButton selected = (RadioButton) formatGroup.getSelectedToggle();
        if (AppVaribles.getMessage("CCITT4").equals(selected.getText())) {
            format = ImagesCombinePdfController.PdfImageFormat.Tiff;
            thresholdInput.setDisable(false);
        } else if (AppVaribles.getMessage("JpegQuailty").equals(selected.getText())) {
            format = ImagesCombinePdfController.PdfImageFormat.Jpeg;
            jpegBox.setDisable(false);
            checkJpegQuality();
        }
    }

    private void checkJpegQuality() {
        jpegQuality = 100;
        try {
            jpegQuality = Integer.valueOf(jpegBox.getSelectionModel().getSelectedItem());
            if (jpegQuality >= 0 && jpegQuality <= 100) {
                jpegBox.setStyle(null);
            } else {
                jpegBox.setStyle(badStyle);
            }
        } catch (Exception e) {
            jpegBox.setStyle(badStyle);
        }
    }

    private void checkThreshold() {
        try {
            if (thresholdInput.getText().isEmpty()) {
                threshold = -1;
                thresholdInput.setStyle(null);
                return;
            }
            threshold = Integer.valueOf(thresholdInput.getText());
            if (threshold >= 0 && threshold <= 100) {
                thresholdInput.setStyle(null);
            } else {
                threshold = -1;
                thresholdInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            threshold = -1;
            thresholdInput.setStyle(badStyle);
        }
    }

    private void initTargetSection() {

        targetFileInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                operationBarController.openTargetButton.setDisable(true);
                try {
                    targetFile = new File(newValue);
                    if (!newValue.toLowerCase().endsWith(".pdf")) {
                        targetFile = null;
                        targetFileInput.setStyle(badStyle);
                        return;
                    }
                    targetFileInput.setStyle(null);
                    AppVaribles.setConfigValue(targetPathKey, targetFile.getParent());
                } catch (Exception e) {
                    targetFile = null;
                    targetFileInput.setStyle(badStyle);
                }
            }
        });

    }

    @Override
    protected void sourceFileChanged(final File file) {

    }

    @FXML
    protected void selectTargetFile(ActionEvent event) {
        try {
            final FileChooser fileChooser = new FileChooser();
            File path = new File(AppVaribles.getConfigValue(targetPathKey, System.getProperty("user.home")));
            if (!path.isDirectory()) {
                path = new File(System.getProperty("user.home"));
            }
            fileChooser.setInitialDirectory(path);
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
            final File file = fileChooser.showSaveDialog(getMyStage());
            if (file == null) {
                return;
            }
            targetFile = file;
            AppVaribles.setConfigValue(LastPathKey, targetFile.getParent());
            AppVaribles.setConfigValue(targetPathKey, targetFile.getParent());

            if (targetFileInput != null) {
                targetFileInput.setText(targetFile.getAbsolutePath());
            }
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    @Override
    protected void makeMoreParameters() {
        makeSingleParameters();
    }

    @Override
    protected void doCurrentProcess() {
        try {
            if (currentParameters == null) {
                return;
            }
            currentParameters.startTime = new Date();
            currentParameters.currentTotalHandled = 0;

            updateInterface("Started");
            task = new Task<Void>() {
                private boolean fail;
                private PDDocument document;

                @Override
                protected Void call() {
                    try {
                        for (; currentParameters.currentFileIndex < sourceFiles.size(); currentParameters.currentFileIndex++) {
                            if (isCancelled()) {
                                break;
                            }
                            File file = sourceFiles.get(currentParameters.currentFileIndex);
                            currentParameters.sourceFile = file;
                            updateInterface("StartFile");
                            if (currentParameters.isBatch) {
                                currentParameters.targetPrefix = FileTools.getFilePrefix(file.getName());
                            }

                            handleCurrentFile();
                            markFileHandled(currentParameters.currentFileIndex);

                            if (isCancelled() || isPreview) {
                                break;
                            }

                            currentParameters.acumStart = 0;
                            currentParameters.startPage = 0;
                            if (currentParameters.isBatch) {
                                updateInterface("CompleteFile");
                            }

                        }
                    } catch (Exception e) {
                        logger.error(e.toString());
                    }

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (!fail && targetFile.exists()) {
                                    Desktop.getDesktop().browse(targetFile.toURI());
                                    operationBarController.openTargetButton.setDisable(false);
                                } else {
                                    FxmlTools.popError(jpegBox, AppVaribles.getMessage("ImageCombinePdfFail"));
                                }
                            } catch (Exception e) {
                                logger.error(e.toString());
                            }
                        }
                    });
                    return null;
                }

                private void handleCurrentFile() {
                    try {
                        try (PDDocument doc = PDDocument.load(currentParameters.sourceFile, currentParameters.password)) {
                            document = doc;
                            if (currentParameters.acumDigit < 1) {
                                currentParameters.acumDigit = (doc.getNumberOfPages() + "").length();
                            }
                            if (!isPreview && currentParameters.isBatch) {
                                currentParameters.toPage = doc.getNumberOfPages() - 1;
                            }
                            currentParameters.currentNameNumber = currentParameters.acumStart;
                            Splitter splitter = new Splitter();
                            splitter.setStartPage(currentParameters.startPage + 1);
                            splitter.setEndPage(currentParameters.toPage + 1);
                            splitter.setSplitAtPage(currentParameters.toPage - currentParameters.startPage + 1);
                            try (PDDocument newDoc = splitter.split(document).get(0)) {
                                newDoc.save(targetFile);
                            }
                        }

                        currentParameters.currentTotalHandled = 0;
                        try (PDDocument doc = PDDocument.load(targetFile)) {
                            PDDocumentInformation info = new PDDocumentInformation();
                            info.setCreationDate(Calendar.getInstance());
                            info.setModificationDate(Calendar.getInstance());
                            info.setProducer("MyBox v" + CommonValues.AppVersion);
                            info.setAuthor(authorInput.getText());
                            doc.setDocumentInformation(info);
                            document = doc;
                            for (int i = 0; i < doc.getNumberOfPages(); i++) {
                                if (isCancelled()) {
                                    break;
                                }
                                PDPage page = doc.getPage(i);
                                handleCurrentPage(page);

                                currentParameters.currentTotalHandled++;
                                updateProgress(i, doc.getNumberOfPages());
                                updateMessage(i + "/" + doc.getNumberOfPages());
                            }
                            updateProgress(doc.getNumberOfPages(), doc.getNumberOfPages());
                            updateMessage(doc.getNumberOfPages() + "/" + doc.getNumberOfPages());

                            doc.save(targetFile);
                        }
                    } catch (Exception e) {
                        logger.error(e.toString());
                    }

                }

                protected void handleCurrentPage(PDPage pdPage) {
                    try {

                        PDResources pdResources = pdPage.getResources();
                        pdResources.getXObjectNames();
                        Iterable<COSName> iterable = pdResources.getXObjectNames();
                        if (iterable == null) {
                            return;
                        }
                        Iterator<COSName> pageIterator = iterable.iterator();
                        while (pageIterator.hasNext()) {
                            if (isCancelled()) {
                                break;
                            }
                            COSName cosName = pageIterator.next();
                            if (!pdResources.isImageXObject(cosName)) {
                                continue;
                            }
                            PDImageXObject pdxObject = (PDImageXObject) pdResources.getXObject(cosName);
                            BufferedImage sourceImage = pdxObject.getImage();
                            PDImageXObject newObject = null;
                            if (format == ImagesCombinePdfController.PdfImageFormat.Tiff) {
                                BufferedImage newImage;
                                if (threshold < 0) {
                                    newImage = ImageGrayTools.color2Binary(sourceImage);
                                } else {
                                    newImage = ImageGrayTools.color2BinaryWithPercentage(sourceImage, threshold);
                                }
                                newObject = CCITTFactory.createFromImage(document, newImage);
                            } else if (format == ImagesCombinePdfController.PdfImageFormat.Jpeg) {
                                newObject = JPEGFactory.createFromImage(document, sourceImage, jpegQuality / 100f);
                            }
                            if (newObject != null) {
                                pdResources.put(cosName, newObject);
                            }
                            if (isPreview) {
                                break;
                            }
                        }
                        pdPage.setResources(pdResources);

                    } catch (Exception e) {
                        logger.error(e.toString());
                    }
                }

                @Override
                protected void succeeded() {
                    super.succeeded();
                    updateInterface("Done");
                }

                @Override
                protected void cancelled() {
                    super.cancelled();
                    updateInterface("Canceled");
                }

                @Override
                protected void failed() {
                    super.failed();
                    updateInterface("Failed");
                }
            };
            operationBarController.progressValue.textProperty().bind(task.messageProperty());
            operationBarController.progressBar.progressProperty().bind(task.progressProperty());
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();

        } catch (Exception e) {
            updateInterface("Failed");
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    protected void openTarget(ActionEvent event) {
        if (!targetFile.exists()) {
            operationBarController.openTargetButton.setDisable(true);
            return;
        }
        operationBarController.openTargetButton.setDisable(false);
        try {
            Desktop.getDesktop().browse(targetFile.toURI());
        } catch (Exception e) {

        }
    }

}
