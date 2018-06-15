package mara.mybox.controller;

import mara.mybox.objects.PdfInformation;
import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.StageStyle;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.objects.CommonValues;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.FxmlTools;
import static mara.mybox.tools.FxmlTools.badStyle;
import mara.mybox.tools.ImageTools;
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

    private PdfInformation pdfInformation;
    private Stage infoStage;
    private String lastSource, status;
    private Date startTime;
    private int handlingPage, handlingNumber, totalHandling;

    public static class ColorConversion {

        public static int DEFAULT = 0;
        public static int OTSU = 1;
        public static int THRESHOLD = 9;
    }

    private Task convertTask;

    @FXML
    private Pane titleBar;
    @FXML
    private TitleBarController titleBarController;
    @FXML
    private Pane commonBar;
    @FXML
    private CommonBarController commonBarController;
    @FXML
    private Pane functionsBar;
    @FXML
    private FunctionsBarController functionsBarController;
    @FXML
    private Pane imageAttributes;
    @FXML
    private ImageAttributesController imageAttributesController;

    @FXML
    private TextField sourceFile;
    @FXML
    private TextField fromPage;
    @FXML
    private TextField toPage;
    @FXML
    private PasswordField passwordInput;

    @FXML
    private TextField targetPath;
    @FXML
    private TextField targetPrefix;
    @FXML
    private TextField acumFrom;
    @FXML
    private CheckBox fillZero;
    @FXML
    private CheckBox appendDensity;
    @FXML
    private CheckBox appendColor;
    @FXML
    private CheckBox appendCompressionType;
    @FXML
    private CheckBox appendQuality;
    @FXML
    private TextField statusLabel;

    @FXML
    private Button startButton;
    @FXML
    private Button pauseButton;
    @FXML
    private Button openTargetButton;
    @FXML
    private Button fileInformationButton;
    @FXML
    private VBox paraBox;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label progressValue;

    public PdfConvertPicturesController() {
    }

    @Override
    protected void initStage2() {

        commonBarController.setParentFxml(thisFxml);
        titleBarController.setParentFxml(thisFxml);
        functionsBarController.setParentFxml(thisFxml);
        imageAttributesController.setParentFxml(thisFxml);

        titleBarController.setTitle(AppVaribles.getMessage("PdfTools"));

        startButton.disableProperty().bind(
                Bindings.isEmpty(sourceFile.textProperty())
                        .or(Bindings.isEmpty(targetPath.textProperty()))
                        .or(Bindings.isEmpty(fromPage.textProperty()))
                        .or(Bindings.isEmpty(toPage.textProperty()))
                        .or(Bindings.isEmpty(acumFrom.textProperty()))
                        .or(sourceFile.styleProperty().isEqualTo(badStyle))
                        .or(targetPath.styleProperty().isEqualTo(badStyle))
                        .or(fromPage.styleProperty().isEqualTo(badStyle))
                        .or(toPage.styleProperty().isEqualTo(badStyle))
                        .or(acumFrom.styleProperty().isEqualTo(badStyle))
                        .or(imageAttributesController.getDensityInput().styleProperty().isEqualTo(badStyle))
                        .or(imageAttributesController.getQualityBox().disableProperty().isEqualTo(new SimpleBooleanProperty(false)).and(imageAttributesController.getQualityInput().styleProperty().isEqualTo(badStyle)))
                        .or(imageAttributesController.getColorBox().disableProperty().isEqualTo(new SimpleBooleanProperty(false)).and(imageAttributesController.getThresholdInput().styleProperty().isEqualTo(badStyle)))
        );

        sourceFile.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue == null || newValue.isEmpty() || !FileTools.isPDF(newValue)) {
                    sourceFile.setStyle(badStyle);
                    return;
                }
                final File file = new File(newValue);
                if (!file.exists()) {
                    sourceFile.setStyle(badStyle);
                    return;
                }
                sourceFile.setStyle(null);
                if (newValue.trim().equals(lastSource)) {
                    return;
                }
                sourceFileChanged(file);
                lastSource = newValue.trim();
            }
        });

        targetPath.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue == null || newValue.isEmpty()) {
                    openTargetButton.setDisable(true);
                    return;
                }
                final File file = new File(newValue);
                if (!file.exists()) {
                    openTargetButton.setDisable(true);
                    return;
                }
                openTargetButton.setDisable(false);
            }
        });

        FxmlTools.setFileValidation(targetPath);
        FxmlTools.setNonnegativeValidation(fromPage);
        FxmlTools.setNonnegativeValidation(toPage);
        FxmlTools.setNonnegativeValidation(acumFrom);

    }

    @FXML
    private void selectSourceFile() {
        try {
            final FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("View Pictures");
            fileChooser.setInitialDirectory(new File(AppVaribles.getConfigValue("LastPath", System.getProperty("user.home"))));
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("PDF", "*.pdf")
            );
            final File file = fileChooser.showOpenDialog(getThisStage());
            sourceFile.setText(file.getAbsolutePath());
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    private void sourceFileChanged(final File file) {
        if (file == null) {
            return;
        }
        statusLabel.setText(getMessage("Loading..."));
        AppVaribles.setConfigValue("LastPath", file.getParent());
        targetPath.setText(file.getParent());
        targetPrefix.setText(FileTools.getFilePrefix(file.getName()));
        toPage.setText("");
        pdfInformation = null;
        fileInformationButton.setDisable(true);
        imageAttributesController.getPreviewButton().setDisable(true);

        Task<Void> openPdfTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                pdfInformation = new PdfInformation(file);
                pdfInformation.loadInformation();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        fileInformationButton.setDisable(false);
                        imageAttributesController.getPreviewButton().setDisable(false);
                        toPage.setText((pdfInformation.getNumberOfPages() - 1) + "");
                        statusLabel.setText("");
                    }
                });
                return null;
            }
        };
        openLoadingStage(openPdfTask);
        Thread thread = new Thread(openPdfTask);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void showFileInformation() {
        if (pdfInformation == null) {
            return;
        }
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(CommonValues.PdfInformation), AppVaribles.CurrentBundle);
            Pane root = fxmlLoader.load();
            PdfInformationController controller = fxmlLoader.getController();
            controller.setInformation(pdfInformation);

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

    @FXML
    private void selectTargetPath() {
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setInitialDirectory(new File(AppVaribles.getConfigValue("LastPath", System.getProperty("user.home"))));
            File directory = chooser.showDialog(getThisStage());
            if (directory != null) {
                AppVaribles.setConfigValue("LastPath", directory.getPath());
                targetPath.setText(directory.getPath());
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    private void startConversion() {
        if (pdfInformation == null) {
            updateInterface("Invalid");
            return;
        }
        try {
            final File file = pdfInformation.getFile();
            final int from = FxmlTools.getInputInt(fromPage);
            final int start;
            final int num;
            if ("Paused".equals(status)) {
                start = handlingPage + 1;
                num = handlingNumber + 1;
            } else {
                start = from;
                num = FxmlTools.getInputInt(acumFrom);
            }
            int toIn = FxmlTools.getInputInt(toPage);
            final int to;
            if (toIn >= pdfInformation.getNumberOfPages()) {
                to = pdfInformation.getNumberOfPages() - 1;
            } else {
                to = toIn;
            }
            if (from > to) {
                updateInterface("Invalid");
                return;
            }
            totalHandling = 0;
            startTime = new Date();
            final boolean fill = fillZero.isSelected();
            final boolean aDensity = appendDensity.isSelected();
            final boolean aColor = appendColor.isSelected();
            final boolean aCompression = appendCompressionType.isSelected();
            final boolean aQuality = appendQuality.isSelected();
            final int digit = (pdfInformation.getNumberOfPages() + "").length();
            final String filePrefix = targetPath.getText() + "/" + targetPrefix.getText();
            final String password = passwordInput.getText();

            updateInterface("started");
            convertTask = new Task<Void>() {
                @Override
                protected Void call() {
                    try {
                        PDDocument doc = PDDocument.load(file, password);
                        PDFRenderer renderer = new PDFRenderer(doc);
                        int pnumber = num;
                        int total = to - from + 1;
                        int converted;
                        String fname;
                        for (int i = start; i <= to; i++, pnumber++) {
                            if (isCancelled()) {
                                break;
                            }
                            handlingPage = i;
                            handlingNumber = pnumber;
                            totalHandling++;
                            String pn = pnumber + "";
                            if (fill) {
                                pn = ValueTools.fillNumber(pnumber, digit);
                            }
                            fname = filePrefix + "_" + pn;
                            if (aColor) {
                                if (imageAttributesController.getColorConversion() == ColorConversion.THRESHOLD) {
                                    fname += "_" + "BINARY-Threshold";
                                    if (imageAttributesController.getThreshold() >= 0) {
                                        fname += "-" + imageAttributesController.getThreshold() + "%";
                                    }
                                } else if (imageAttributesController.getColorConversion() == ColorConversion.OTSU) {
                                    fname += "_" + "BINARY-OTSU";
                                } else {
                                    fname += "_" + imageAttributesController.getImageColor();
                                }
                            }
                            if (aDensity) {
                                fname += "_" + imageAttributesController.getDensity() + "dpi";
                            }
                            if (imageAttributesController.getCompressionType() != null && !"none".equals(imageAttributesController.getCompressionType())) {
                                if (aCompression) {
                                    fname += "_" + imageAttributesController.getCompressionType().replace(" ", "_");
                                }
                                if (aQuality) {
                                    fname += "_quality-" + imageAttributesController.getQuality() + "%";
                                }
                            }
                            fname += "." + imageAttributesController.getImageFormat();
                            converted = i - from + 1;
                            updateProgress(converted, total);
                            updateMessage(converted + "/" + total);

                            BufferedImage image;
                            if (imageAttributesController.getColorConversion() == ColorConversion.THRESHOLD && imageAttributesController.getThreshold() >= 0) {
                                image = renderer.renderImageWithDPI(i, imageAttributesController.getDensity(), ImageType.RGB);
                                image = ImageTools.color2BinaryWithPercentage(image, imageAttributesController.getThreshold());
                            } else if (imageAttributesController.getColorConversion() == ColorConversion.OTSU) {
                                image = renderer.renderImageWithDPI(i, imageAttributesController.getDensity(), ImageType.RGB);
                                image = ImageTools.color2Binary(image);
                            } else {
                                image = renderer.renderImageWithDPI(i, imageAttributesController.getDensity(), imageAttributesController.getImageColor());
                            }

                            Map<String, Object> parameters = new HashMap();
                            parameters.put("density", imageAttributesController.getDensity());
                            parameters.put("imageColor", imageAttributesController.getImageColor());
                            if (imageAttributesController.getCompressionType() != null && !"none".equals(imageAttributesController.getCompressionType())) {
                                parameters.put("compressionType", imageAttributesController.getCompressionType());
                            }
                            if (imageAttributesController.getQuality() > 0) {
                                parameters.put("quality", imageAttributesController.getQuality());
                            }
                            ImageTools.writeImageFile(image, imageAttributesController.getImageFormat(), parameters, fname);

                        }
                        doc.close();
                    } catch (Exception e) {
                        logger.error(e.toString());
                    }
                    return null;
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
            progressValue.textProperty().bind(convertTask.messageProperty());
            progressBar.progressProperty().bind(convertTask.progressProperty());
            Thread thread = new Thread(convertTask);
            thread.setDaemon(true);
            thread.start();

        } catch (Exception e) {
            updateInterface("Failed");
            logger.error(e.toString());
        }
    }

    private void updateInterface(String newStatus) {
        status = newStatus;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                if (null != status) {
                    switch (status) {
                        case "started":
                            statusLabel.setText(getMessage("Handling...") + " " + getMessage("StartTime")
                                    + ": " + DateTools.datetimeToString(startTime));

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

    private void showCost() {
        long cost = (new Date().getTime() - startTime.getTime()) / 1000;
        double avg = ValueTools.roundDouble((double) cost / totalHandling);
        String s = getMessage(status) + ". "
                + getMessage("Cost") + ": " + cost + " " + getMessage("Seconds") + ". "
                + getMessage("Average") + ": " + avg + " " + getMessage("SecondsPerItem") + ". "
                + getMessage("StartTime") + ": " + DateTools.datetimeToString(startTime) + ", "
                + getMessage("EndTime") + ": " + DateTools.datetimeToString(new Date());
        statusLabel.setText(s);
    }

    @FXML
    private void openFileManager() {
        try {
            Desktop.getDesktop().browse(new File(targetPath.getText()).toURI());
//            new ProcessBuilder("Explorer", targetPath.getText()).start();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    private void pauseConversion() {
        if (convertTask != null && convertTask.isRunning()) {
            convertTask.cancel();
        }
        updateInterface("Paused");
    }

    private void cancelConversion() {
        if (convertTask != null && convertTask.isRunning()) {
            convertTask.cancel();
        }
        updateInterface("Canceled");
    }

}
