package mara.mybox.controller;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.objects.CommonValues;
import mara.mybox.objects.ImageAttributes;
import mara.mybox.objects.PdfInformation;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.FxmlTools;
import static mara.mybox.tools.FxmlTools.badStyle;
import mara.mybox.tools.ImageGrayTools;
import mara.mybox.tools.ImageWriters;
import mara.mybox.tools.ValueTools;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 16:07:09
 * @Description
 * @License Apache License Version 2.0
 */
public class PdfConvertPicturesController extends BaseController {

    protected Stage infoStage;
    protected List<FileChooser.ExtensionFilter> fileExtensionFilter;
    private boolean isPreview;

    @FXML
    protected TextField sourceFileInput;
    @FXML
    protected TextField targetPathInput;
    @FXML
    protected TextField targetPrefixInput;
    @FXML
    protected Button openTargetButton;
    @FXML
    protected TextField statusLabel;
    @FXML
    protected Pane imageAttributes;
    @FXML
    protected ImageAttributesController imageAttributesController;
    @FXML
    protected CheckBox fillZero;
    @FXML
    protected CheckBox appendDensity;
    @FXML
    protected CheckBox appendColor;
    @FXML
    protected CheckBox appendCompressionType;
    @FXML
    protected CheckBox appendQuality;
    @FXML
    protected Button startButton;
    @FXML
    protected Button pauseButton;
    @FXML
    protected Button previewButton;
    @FXML
    protected VBox paraBox;
    @FXML
    protected ProgressBar progressBar;
    @FXML
    protected Label progressValue;
    @FXML
    private TextField fromPageInput;
    @FXML
    private TextField toPageInput;
    @FXML
    private TextField previewInput;
    @FXML
    private PasswordField passwordInput;
    @FXML
    private TextField acumFromInput;
    @FXML
    private Button fileInformationButton;

    protected class Conversion {

        public File file;
        public int fromPage, toPage, startPage, acumFrom, acumStart, acumDigit;
        public String password, status, filePrefix, fileName;
        public Date startTime, endTime;
        public int currentPage, currentNameNumber;
        public PdfInformation pdfInformation;
        boolean fill, aDensity, aColor, aCompression, aQuality;
    }

    protected Conversion fileConversion, previewConversion, currentConversion;

    public PdfConvertPicturesController() {
    }

    @Override
    protected void initializeNext() {
        try {
            if (imageAttributesController != null) {
                imageAttributesController.setParentFxml(myFxml);
            }

            fileExtensionFilter = new ArrayList();
            fileExtensionFilter.add(new FileChooser.ExtensionFilter("pdf", "*.pdf"));
            fileExtensionFilter.add(new FileChooser.ExtensionFilter("PDF", "*.PDF"));

            fillZero.setSelected(AppVaribles.getConfigBoolean("pci_fill"));
            appendDensity.setSelected(AppVaribles.getConfigBoolean("pci_aDensity"));
            appendColor.setSelected(AppVaribles.getConfigBoolean("pci_aColor"));
            appendCompressionType.setSelected(AppVaribles.getConfigBoolean("pci_aCompression"));
            appendQuality.setSelected(AppVaribles.getConfigBoolean("pci_aQuality"));

            initializeNext2();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initializeNext2() {
        try {
            if (sourceFileInput != null) {
                sourceFileInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        if (newValue == null || newValue.isEmpty() || !FileTools.isPDF(newValue)) {
                            sourceFileInput.setStyle(badStyle);
                            return;
                        }
                        final File file = new File(newValue);
                        if (!file.exists()) {
                            sourceFileInput.setStyle(badStyle);
                            return;
                        }
                        sourceFileInput.setStyle(null);
                        sourceFileChanged(file);
                        if (file.isDirectory()) {
                            AppVaribles.setConfigValue("pdfSourcePath", file.getPath());
//                            if (targetPathInput != null && targetPathInput.getText().isEmpty()) {
//                                targetPathInput.setText(file.getPath());
//                            }

                        } else {
                            AppVaribles.setConfigValue("pdfSourcePath", file.getParent());
                            if (targetPathInput != null && targetPathInput.getText().isEmpty()) {
                                targetPathInput.setText(AppVaribles.getConfigValue("pdfTargetPath", System.getProperty("user.home")));
                            }
                            if (targetPrefixInput != null) {
                                targetPrefixInput.setText(FileTools.getFilePrefix(file.getName()));
                            }
                        }
                    }
                });
            }

            if (targetPathInput != null) {
                targetPathInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        try {
                            final File file = new File(newValue);
                            if (file.isDirectory()) {
                                AppVaribles.setConfigValue("pdfTargetPath", file.getPath());
                            } else {
                                AppVaribles.setConfigValue("pdfTargetPath", file.getParent());
                            }
                            if (openTargetButton != null) {
                                if (!file.exists()) {
                                    openTargetButton.setDisable(true);
                                } else {
                                    openTargetButton.setDisable(false);
                                }
                            }
                        } catch (Exception e) {
                        }
                    }
                });
                FxmlTools.setFileValidation(targetPathInput);
            }

            startButton.disableProperty().bind(
                    Bindings.isEmpty(sourceFileInput.textProperty())
                            .or(Bindings.isEmpty(targetPathInput.textProperty()))
                            .or(Bindings.isEmpty(fromPageInput.textProperty()))
                            .or(Bindings.isEmpty(toPageInput.textProperty()))
                            .or(Bindings.isEmpty(acumFromInput.textProperty()))
                            .or(sourceFileInput.styleProperty().isEqualTo(badStyle))
                            .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                            .or(fromPageInput.styleProperty().isEqualTo(badStyle))
                            .or(toPageInput.styleProperty().isEqualTo(badStyle))
                            .or(acumFromInput.styleProperty().isEqualTo(badStyle))
                            .or(imageAttributesController.getDensityInput().styleProperty().isEqualTo(badStyle))
                            .or(imageAttributesController.getQualityBox().disableProperty().isEqualTo(new SimpleBooleanProperty(false)).and(imageAttributesController.getQualityInput().styleProperty().isEqualTo(badStyle)))
                            .or(imageAttributesController.getColorBox().disableProperty().isEqualTo(new SimpleBooleanProperty(false)).and(imageAttributesController.getThresholdInput().styleProperty().isEqualTo(badStyle)))
            );

            previewButton.disableProperty().bind(
                    Bindings.isEmpty(sourceFileInput.textProperty())
                            .or(imageAttributesController.getRawSelect().selectedProperty())
                            .or(startButton.disableProperty())
                            .or(startButton.textProperty().isNotEqualTo(AppVaribles.getMessage("Start")))
                            .or(previewInput.styleProperty().isEqualTo(badStyle))
            );
            FxmlTools.setNonnegativeValidation(fromPageInput);
            FxmlTools.setNonnegativeValidation(toPageInput);
            FxmlTools.setNonnegativeValidation(acumFromInput);
            FxmlTools.setNonnegativeValidation(previewInput);

            previewInput.setText(AppVaribles.getConfigValue("pci_preview", "0"));
            previewInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (newValue == null || newValue.isEmpty()) {
                        return;
                    }
                    AppVaribles.setConfigValue("pci_preview", newValue);
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    protected void selectSourceFile() {
        try {
            final FileChooser fileChooser = new FileChooser();
            File path = new File(AppVaribles.getConfigValue("pdfSourcePath", System.getProperty("user.home")));
            if (!path.isDirectory()) {
                path = new File(System.getProperty("user.home"));
            }
            fileChooser.setInitialDirectory(path);
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
            final File file = fileChooser.showOpenDialog(getMyStage());
            if (file != null) {
                sourceFileInput.setText(file.getAbsolutePath());
            }
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    @FXML
    protected void selectTargetPath() {
        if (targetPathInput == null) {
            return;
        }
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            File path = new File(AppVaribles.getConfigValue("pdfTargetPath", System.getProperty("user.home")));
            if (!path.isDirectory()) {
                path = new File(System.getProperty("user.home"));
            }
            chooser.setInitialDirectory(path);
            File directory = chooser.showDialog(getMyStage());
            if (directory != null) {
                targetPathInput.setText(directory.getPath());
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    protected void openTargetPath() {
        try {
            Desktop.getDesktop().browse(new File(targetPathInput.getText()).toURI());
//            new ProcessBuilder("Explorer", targetPath.getText()).start();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void sourceFileChanged(final File file) {
        if (file == null) {
            return;
        }
        toPageInput.setText("");
        fileInformationButton.setDisable(true);
        imageAttributesController.getPreviewButton().setDisable(true);
        statusLabel.setText(getMessage("Loading..."));
        imageAttributesController.getPreviewButton().setDisable(true);

        fileConversion = new Conversion();
        fileConversion.file = file;
        fileConversion.pdfInformation = null;
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                fileConversion.pdfInformation = new PdfInformation(file);
                fileConversion.pdfInformation.loadInformation();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        statusLabel.setText("");
                        if (fileConversion.pdfInformation != null) {
                            toPageInput.setText((fileConversion.pdfInformation.getNumberOfPages() - 1) + "");
                            fileInformationButton.setDisable(false);
                        }
                    }
                });
                return null;
            }
        };
        openLoadingStage(task);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    protected void startConversion() {
        isPreview = false;
        makeFileConversion();
        currentConversion = fileConversion;
        convertCurrentFile();
    }

    @FXML
    protected void preview() {
        isPreview = true;
        makeFileConversion();
        previewConversion = copyConversion(fileConversion);
        int page = FxmlTools.getInputInt(previewInput);
        if (page > previewConversion.pdfInformation.getNumberOfPages()) {
            page = 0;
        }
        previewConversion.fromPage = page;
        previewConversion.startPage = page;
        previewConversion.toPage = page;
        previewConversion.currentPage = 0;
        previewConversion.status = "start";
        currentConversion = previewConversion;
        convertCurrentFile();
    }

    protected void makeFileConversion() {
        fileConversion.fromPage = FxmlTools.getInputInt(fromPageInput);
        fileConversion.toPage = FxmlTools.getInputInt(toPageInput);
        fileConversion.acumFrom = FxmlTools.getInputInt(acumFromInput);
        fileConversion.password = passwordInput.getText();
        if ("Paused".equals(fileConversion.status)) {
            fileConversion.startPage = fileConversion.currentPage + 1;
            fileConversion.acumStart = fileConversion.currentNameNumber + 1;
        } else {
            fileConversion.startPage = fileConversion.fromPage;
            fileConversion.acumStart = fileConversion.acumFrom;
        }
        fileConversion.fill = fillZero.isSelected();
        fileConversion.aDensity = appendDensity.isSelected();
        fileConversion.aColor = appendColor.isSelected();
        fileConversion.aCompression = appendCompressionType.isSelected();
        fileConversion.aQuality = appendQuality.isSelected();
        fileConversion.acumDigit = (fileConversion.toPage + "").length();
        fileConversion.filePrefix = targetPathInput.getText() + "/" + targetPrefixInput.getText();

        AppVaribles.setConfigValue("pci_fill", fileConversion.fill);
        AppVaribles.setConfigValue("pci_aDensity", fileConversion.aDensity);
        AppVaribles.setConfigValue("pci_aColor", fileConversion.aColor);
        AppVaribles.setConfigValue("pci_aCompression", fileConversion.aCompression);
        AppVaribles.setConfigValue("pci_aQuality", fileConversion.aQuality);

    }

    protected Conversion copyConversion(Conversion theConversion) {
        Conversion newConversion = new Conversion();
        newConversion.aColor = theConversion.aColor;
        newConversion.aCompression = theConversion.aCompression;
        newConversion.aDensity = theConversion.aDensity;
        newConversion.aQuality = theConversion.aQuality;
        newConversion.acumDigit = theConversion.acumDigit;
        newConversion.acumFrom = theConversion.acumFrom;
        newConversion.acumStart = theConversion.acumStart;
        newConversion.currentNameNumber = theConversion.currentNameNumber;
        newConversion.currentPage = theConversion.currentPage;
        newConversion.file = theConversion.file;
        newConversion.filePrefix = theConversion.filePrefix;
        newConversion.fill = theConversion.fill;
        newConversion.fromPage = theConversion.fromPage;
        newConversion.password = theConversion.password;
        newConversion.pdfInformation = theConversion.pdfInformation;
        newConversion.startPage = theConversion.startPage;
        newConversion.status = theConversion.status;
        newConversion.toPage = theConversion.toPage;
        newConversion.startTime = theConversion.startTime;
        return newConversion;
    }

    protected void convertCurrentFile() {
        try {
            if (currentConversion == null) {
                return;
            }
            currentConversion.startTime = new Date();
            final ImageAttributes attributes = imageAttributesController.getAttributes();
            updateInterface("started");
            task = new Task<Void>() {
                @Override
                protected Void call() {
                    try {
                        PDDocument doc = PDDocument.load(currentConversion.file, currentConversion.password);
                        PDFRenderer renderer = new PDFRenderer(doc);
                        int nameNumber = currentConversion.acumStart;
                        int total = currentConversion.toPage - currentConversion.fromPage + 1;
                        for (int currentPage = currentConversion.startPage; currentPage <= currentConversion.toPage; currentPage++, nameNumber++) {
                            if (isCancelled()) {
                                break;
                            }
                            currentConversion.fileName = makeFilename(nameNumber);
                            BufferedImage image;
                            if (ImageType.BINARY == attributes.getColorSpace()) {
                                if (attributes.getBinaryConversion() == ImageAttributes.BinaryConversion.BINARY_THRESHOLD
                                        && attributes.getThreshold() >= 0) {
                                    image = renderer.renderImageWithDPI(currentPage, attributes.getDensity(), ImageType.RGB);
                                    image = ImageGrayTools.color2BinaryWithPercentage(image, attributes.getThreshold());
                                } else if (attributes.getBinaryConversion() == ImageAttributes.BinaryConversion.BINARY_OTSU) {
                                    image = renderer.renderImageWithDPI(currentPage, attributes.getDensity(), ImageType.RGB);
                                    image = ImageGrayTools.color2Binary(image);
                                } else {
                                    image = renderer.renderImageWithDPI(currentPage, attributes.getDensity(), attributes.getColorSpace());
                                }
                            } else {
                                image = renderer.renderImageWithDPI(currentPage, attributes.getDensity(), attributes.getColorSpace());
                            }
                            ImageWriters.writeImageFile(image, attributes, currentConversion.fileName);

                            currentConversion.currentPage = currentPage;
                            currentConversion.currentNameNumber = nameNumber;
                            int pages = currentConversion.currentPage - currentConversion.fromPage + 1;
                            updateProgress(pages, total);
                            updateMessage(pages + "/" + total);
                        }
                        doc.close();
                    } catch (Exception e) {
                        logger.error(e.toString());
                    }
                    return null;
                }

                private String makeFilename(int number) {
                    String pageNumber = number + "";
                    if (currentConversion.fill) {
                        pageNumber = ValueTools.fillNumber(number, currentConversion.acumDigit);
                    }
                    String fname = currentConversion.filePrefix + "_" + pageNumber;
                    if (currentConversion.aColor) {
                        if (ImageType.BINARY == attributes.getColorSpace()) {
                            if (attributes.getBinaryConversion() == ImageAttributes.BinaryConversion.BINARY_THRESHOLD) {
                                fname += "_" + "BINARY-Threshold";
                                if (attributes.getThreshold() >= 0) {
                                    fname += "-" + attributes.getThreshold() + "%";
                                }
                            } else if (attributes.getBinaryConversion() == ImageAttributes.BinaryConversion.BINARY_OTSU) {
                                fname += "_" + "BINARY-OTSU";
                            } else {
                                fname += "_" + attributes.getColorSpace();
                            }
                        } else {
                            fname += "_" + attributes.getColorSpace();
                        }
                    }
                    if (currentConversion.aDensity) {
                        fname += "_" + attributes.getDensity() + "dpi";
                    }
                    if (attributes.getCompressionType() != null && !"none".equals(attributes.getCompressionType())) {
                        if (currentConversion.aCompression) {
                            fname += "_" + attributes.getCompressionType().replace(" ", "_");
                        }
                        if (currentConversion.aQuality) {
                            fname += "_quality-" + attributes.getQuality() + "%";
                        }
                    }
                    fname += "." + attributes.getImageFormat();
                    return fname;
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
            progressValue.textProperty().bind(task.messageProperty());
            progressBar.progressProperty().bind(task.progressProperty());
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();

        } catch (Exception e) {
            updateInterface("Failed");
            logger.error(e.toString());
        }
    }

    protected void updateInterface(String newStatus) {
        currentConversion.status = newStatus;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                if (null != currentConversion.status) {
                    switch (currentConversion.status) {
                        case "started":
                            statusLabel.setText(currentConversion.file.getName() + " "
                                    + getMessage("Handling...") + " "
                                    + getMessage("StartTime")
                                    + ": " + DateTools.datetimeToString(currentConversion.startTime));
                            startButton.setText(AppVaribles.getMessage("Cancel"));
                            startButton.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    cancelConversion();
                                }
                            });
                            pauseButton.setVisible(true);
                            pauseButton.setDisable(false);
                            pauseButton.setText(AppVaribles.getMessage("Pause"));
                            pauseButton.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    pauseConversion();
                                }
                            });
                            paraBox.setDisable(true);
                            break;

                        case "Paused":
                            startButton.setText(AppVaribles.getMessage("Cancel"));
                            startButton.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    cancelConversion();
                                }
                            });
                            pauseButton.setVisible(true);
                            pauseButton.setDisable(false);
                            pauseButton.setText(AppVaribles.getMessage("Continue"));
                            pauseButton.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    startConversion();
                                }
                            });
                            paraBox.setDisable(true);
                            showCost();
                            break;

                        case "Done":
                            if (isPreview) {
                                showImage(currentConversion.fileName);
                            }

                        default:
                            startButton.setText(AppVaribles.getMessage("Start"));
                            startButton.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    startConversion();
                                }
                            });
                            pauseButton.setVisible(false);
                            pauseButton.setDisable(true);
                            paraBox.setDisable(false);
                            showCost();

                    }
                }

            }
        });
    }

    protected void showCost() {
        if (statusLabel == null) {
            return;
        }
        long cost = (new Date().getTime() - currentConversion.startTime.getTime()) / 1000;
        int pages = currentConversion.currentPage - currentConversion.startPage + 1;
        double avg = ValueTools.roundDouble((double) cost / pages);
        String s = getMessage(currentConversion.status) + ". "
                + getMessage("HandledThisTime") + ": " + pages + " "
                + getMessage("Cost") + ": " + cost + " " + getMessage("Seconds") + ". "
                + getMessage("Average") + ": " + avg + " " + getMessage("SecondsPerItem") + ". "
                + getMessage("StartTime") + ": " + DateTools.datetimeToString(currentConversion.startTime) + ", "
                + getMessage("EndTime") + ": " + DateTools.datetimeToString(new Date());
        statusLabel.setText(s);
    }

    @FXML
    protected void pauseConversion() {
        if (task != null && task.isRunning()) {
            task.cancel();
        }
        updateInterface("Paused");
    }

    protected void cancelConversion() {
        if (task != null && task.isRunning()) {
            task.cancel();
        }
        updateInterface("Canceled");
    }

    @FXML
    private void showFileInformation() {
        if (fileConversion == null || fileConversion.pdfInformation == null) {
            return;
        }
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(CommonValues.PdfInformationFxml), AppVaribles.CurrentBundle);
            Pane root = fxmlLoader.load();
            PdfInformationController controller = fxmlLoader.getController();
            controller.setInformation(fileConversion.pdfInformation);

            infoStage = new Stage();
            infoStage.initModality(Modality.NONE);
            infoStage.initStyle(StageStyle.DECORATED);
            infoStage.initOwner(null);
            infoStage.getIcons().add(new Image("img/mybox.png"));
            infoStage.setScene(new Scene(root));
            infoStage.show();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
