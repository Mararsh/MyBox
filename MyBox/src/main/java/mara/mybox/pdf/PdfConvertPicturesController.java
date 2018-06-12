package mara.mybox.pdf;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Date;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
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
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.StageStyle;
import javax.imageio.ImageIO;
import mara.mybox.MyBoxBaseController;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.objects.CommonValues;
import mara.mybox.tools.ConfigTools;
import mara.mybox.objects.PdfInformation;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.FxmlTools;
import mara.mybox.tools.ValueTools;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 16:07:09
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class PdfConvertPicturesController extends MyBoxBaseController {

    private static final Logger logger = LogManager.getLogger();

    private PdfInformation pdfInformation;
    private Stage infoStage;
    private String lastSource;
    private int density;
    private ImageType imageType;
    private String imageFormat;
    private String status;
    private Date startTime;
    private int handlingPage, handlingNumber, totalHandling;
    final private String goodStyle;
    final private String badStyle;

    private Task convertTask;

    @FXML
    private TextField sourceFile;
    @FXML
    private TextField fromPage;
    @FXML
    private TextField toPage;
    @FXML
    private TextField targetPath;
    @FXML
    private TextField targetPrefix;
    @FXML
    private TextField densityInput;
    @FXML
    private TextField acumFrom;
    @FXML
    private CheckBox fillZero;
    @FXML
    private CheckBox appendDensity;
    @FXML
    private CheckBox appendType;
    @FXML
    private Label statusLabel;
    @FXML
    private ToggleGroup DensityGroup;
    @FXML
    private ToggleGroup ImageTypeGroup;
    @FXML
    private ToggleGroup ImageFormatGroup;
    @FXML
    private Button startButton;
    @FXML
    private Button pauseButton;
    @FXML
    private Button fileInformationButton;
    @FXML
    private VBox paraBox;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label progressValue;

    public PdfConvertPicturesController() {
        this.goodStyle = "-fx-text-box-border: black;";
        this.badStyle = "-fx-text-box-border: red;";
    }

    @Override
    protected void initStage2() {
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
                        .or(densityInput.styleProperty().isEqualTo(badStyle))
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
                sourceFile.setStyle(goodStyle);
                if (newValue.trim().equals(lastSource)) {
                    return;
                }
                sourceFileChanged(file);
                lastSource = newValue.trim();
            }
        });

        FxmlTools.setFileValidation(targetPath, goodStyle, badStyle);
        FxmlTools.setNonnegativeValidation(fromPage, goodStyle, badStyle);
        FxmlTools.setNonnegativeValidation(toPage, goodStyle, badStyle);
        FxmlTools.setNonnegativeValidation(acumFrom, goodStyle, badStyle);
        FxmlTools.setNonnegativeValidation(densityInput, goodStyle, badStyle);

        getDensity();
        DensityGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                getDensity();
            }
        });
        densityInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                getDensity();
            }
        });

        getImageType();
        ImageTypeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                getImageType();
            }
        });

        getImageFormat();
        ImageFormatGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                getImageFormat();
            }
        });
    }

    private void getDensity() {
        try {
            RadioButton selected = (RadioButton) DensityGroup.getSelectedToggle();
            String s = selected.getText();
            densityInput.setStyle(goodStyle);
            if (getMessage("InputValue").equals(s)) {
                try {
                    int v = Integer.parseInt(densityInput.getText());
                    if (v > 0) {
                        density = v;
                    } else {
                        densityInput.setStyle(badStyle);
                    }
                } catch (Exception e) {
                    densityInput.setStyle(badStyle);
                }
            } else {
                density = Integer.parseInt(s.substring(0, s.length() - 3));
            }
//            logger.debug(density);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void getImageType() {
        try {
            RadioButton selected = (RadioButton) ImageTypeGroup.getSelectedToggle();
            switch (selected.getText()) {
                case "RGB":
                    imageType = ImageType.RGB;
                    break;
                case "Alpha RGB":
                    imageType = ImageType.ARGB;
                    break;
                case "Gray":
                    imageType = ImageType.GRAY;
                    break;
                case "Binary":
                    imageType = ImageType.BINARY;
                    break;
                default:
                    imageType = ImageType.RGB;
            }
//            logger.debug(imageType);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void getImageFormat() {
        try {
            RadioButton selected = (RadioButton) ImageFormatGroup.getSelectedToggle();
            imageFormat = selected.getText();
//            logger.debug(imageFormat);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    private void selectSourceFile() {
        try {
            final FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("View Pictures");
            fileChooser.setInitialDirectory(new File(ConfigTools.getLastPath()));
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("PDF", "*.pdf")
            );
            final File file = fileChooser.showOpenDialog(getStage());
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
        ConfigTools.setLastPath(file.getParent());
        targetPath.setText(file.getParent());
        targetPrefix.setText(FileTools.getFilePrefix(file.getName()));
        toPage.setText("");
        pdfInformation = null;
        fileInformationButton.setDisable(true);

        Task<Void> openPdfTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                pdfInformation = new PdfInformation(file);
                pdfInformation.loadInformation();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        fileInformationButton.setDisable(false);
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
    private void closeFileInformation() {
        if (pdfInformation == null || infoStage == null) {
            return;
        }
        try {
            infoStage.close();
            infoStage = null;
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    private void selectTargetPath() {
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setInitialDirectory(new File(ConfigTools.getLastPath()));
            File directory = chooser.showDialog(getStage());
            if (directory != null) {
                ConfigTools.setLastPath(directory.getPath());
                targetPath.setText(directory.getPath());
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    private void startConvertion() {
        if (pdfInformation == null) {
            status = "Invalid";
            setFinished();
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
                status = "Invalid";
                setFinished();
                return;
            }
            totalHandling = 0;
            startTime = new Date();
            final boolean fill = fillZero.isSelected();
            final boolean aDensity = appendDensity.isSelected();
            final boolean aType = appendType.isSelected();
            final int digit = (pdfInformation.getNumberOfPages() + "").length();
            final String filePrefix = targetPath.getText() + "/" + targetPrefix.getText();
            paraBox.setDisable(true);
            pauseButton.setVisible(true);
            pauseButton.setDisable(false);
            statusLabel.setText(getMessage("Handling...") + " " + getMessage("StartTime") + ": " + DateTools.datetimeToString(startTime));
            startButton.setText(getMessage("Cancel"));
            startButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    status = "Cancelled";
                    if (convertTask != null && convertTask.isRunning()) {
                        convertTask.cancel();
                    }
                }
            });
            convertTask = new Task<Void>() {
                @Override
                protected Void call() {
                    try {
                        PDDocument doc = PDDocument.load(file);
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
                            fname = filePrefix + "-" + pn;
                            if (aType) {
                                fname += "-" + imageType;
                            }
                            if (aDensity) {
                                fname += "-" + density + "dpi";
                            }
                            fname += "." + imageFormat;
                            converted = i - from + 1;
                            updateProgress(converted, total);
                            updateMessage(converted + "/" + total);
                            BufferedImage image = renderer.renderImageWithDPI(i, density, imageType);
                            ImageIO.write(image, imageFormat, new File(fname));
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
                    status = "Done";
                    setFinished();
                }

                @Override
                protected void cancelled() {
                    super.cancelled();
                    setFinished();
                }

                @Override
                protected void failed() {
                    super.failed();
                    status = "Failed";
                    setFinished();
                }
            };
            progressValue.textProperty().bind(convertTask.messageProperty());
            progressBar.progressProperty().bind(convertTask.progressProperty());
            Thread thread = new Thread(convertTask);
            thread.setDaemon(true);
            thread.start();

        } catch (Exception e) {
            setFinished();
            logger.error(e.toString());
        }
    }

    private void setFinished() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (!"Paused".equals(status)) {
                    paraBox.setDisable(false);
                    startButton.setText(AppVaribles.getMessage("Start"));
                } else {
                    startButton.setText(AppVaribles.getMessage("Continue"));
                }
                startButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        startConvertion();
                    }
                });
                long cost = (new Date().getTime() - startTime.getTime()) / 1000;
                double avg = ValueTools.roundDouble((double) cost / totalHandling);
                String s = getMessage(status) + ". "
                        + getMessage("Cost") + ": " + cost + " " + getMessage("Seconds") + ". "
                        + getMessage("Average") + ": " + avg + " " + getMessage("SecondsPerItem") + ". "
                        + getMessage("StartTime") + ": " + DateTools.datetimeToString(startTime) + ", "
                        + getMessage("EndTime") + ": " + DateTools.datetimeToString(new Date());
                statusLabel.setText(s);
                pauseButton.setVisible(false);
                pauseButton.setDisable(true);

            }
        });
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
        status = "Paused";
        if (convertTask != null && convertTask.isRunning()) {
            convertTask.cancel();
        }
    }

}
