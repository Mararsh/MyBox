package mara.mybox.pdf;

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
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.StageStyle;
import mara.mybox.MyBoxBaseController;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.objects.CommonValues;
import mara.mybox.objects.PdfInformation;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.FxmlTools;
import mara.mybox.tools.ImageTools;
import mara.mybox.tools.ValueTools;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 16:07:09
 * @Description
 * @License Apache License Version 2.0
 */
public class PdfConvertPicturesController extends MyBoxBaseController {

    private static final Logger logger = LogManager.getLogger();

    private PdfInformation pdfInformation;
    private Stage infoStage;
    private ImageType imageColor;
    private int quality, threshold, density;
    private String lastSource, imageFormat, compressionType, status;
    private int colorConversion;
    private Date startTime;
    private int handlingPage, handlingNumber, totalHandling;

    public static class ColorConversion {

        public static int DEFAULT = 0;
        public static int OTSU = 1;
        public static int THRESHOLD = 9;
    }

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
    private TextField qualityInput;
    @FXML
    private TextField thresholdInput;
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
    private Label statusLabel;
    @FXML
    private ToggleGroup DensityGroup;
    @FXML
    private ToggleGroup ImageColorGroup;
    @FXML
    private ToggleGroup ImageFormatGroup;
    @FXML
    private ToggleGroup QualityGroup;
    @FXML
    private ToggleGroup CompressionGroup;
    @FXML
    private ToggleGroup ColorConversionGroup;
    @FXML
    private RadioButton ARGB;
    @FXML
    private RadioButton RGB;
    @FXML
    private HBox compressBox;
    @FXML
    private HBox qualityBox;
    @FXML
    private HBox colorBox;
    @FXML
    private Button startButton;
    @FXML
    private Button pauseButton;
    @FXML
    private Button previewButton;
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
                        .or(qualityBox.disableProperty().isEqualTo(new SimpleBooleanProperty(false)).and(qualityInput.styleProperty().isEqualTo(badStyle)))
                        .or(colorBox.disableProperty().isEqualTo(new SimpleBooleanProperty(false)).and(thresholdInput.styleProperty().isEqualTo(badStyle)))
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

        FxmlTools.setFileValidation(targetPath, goodStyle, badStyle);
        FxmlTools.setNonnegativeValidation(fromPage, goodStyle, badStyle);
        FxmlTools.setNonnegativeValidation(toPage, goodStyle, badStyle);
        FxmlTools.setNonnegativeValidation(acumFrom, goodStyle, badStyle);
        FxmlTools.setNonnegativeValidation(densityInput, goodStyle, badStyle);

        ImageFormatGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                getImageFormat();
            }
        });
        FxmlTools.setRadioSelected(ImageFormatGroup, AppVaribles.getConfigValue("imageFormat", null));
        getImageFormat();

        DensityGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                getDensity();
            }
        });
        FxmlTools.setRadioSelected(DensityGroup, AppVaribles.getConfigValue("density", null));
        getDensity();

        densityInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                getDensity();
            }
        });

        ImageColorGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                getImageColor();
            }
        });
        FxmlTools.setRadioSelected(ImageColorGroup, AppVaribles.getConfigValue("imageColor", null));
        getImageColor();

        QualityGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                getQuality();
            }
        });
        FxmlTools.setRadioSelected(QualityGroup, AppVaribles.getConfigValue("quality", null));
        getQuality();

        qualityInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                getQuality();
            }
        });

        ColorConversionGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                getColorConversion();
            }
        });
        FxmlTools.setRadioSelected(ColorConversionGroup, AppVaribles.getConfigValue("colorConversion", null));

        thresholdInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                getColorConversion();
            }
        });
    }

    private void getImageFormat() {
        try {
            RadioButton selected = (RadioButton) ImageFormatGroup.getSelectedToggle();
            imageFormat = selected.getText();
            AppVaribles.setConfigValue("imageFormat", imageFormat);

            String[] compressionTypes = ImageTools.getCompressionTypes(imageFormat, imageColor);
            showCompressionTypes(compressionTypes);

            if (compressionTypes == null) {
                qualityBox.setDisable(true);
            } else {
                qualityBox.setDisable(false);
                FxmlTools.setRadioFirstSelected(QualityGroup);
            }

            if ("jpg".equals(imageFormat) || "bmp".equals(imageFormat)) {
                ARGB.setDisable(true);
                if (ARGB.isSelected()) {
                    RGB.setSelected(true);
                }
            } else {
                ARGB.setDisable(false);
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void getImageColor() {
        try {
            RadioButton selected = (RadioButton) ImageColorGroup.getSelectedToggle();
            String s = selected.getText();
            AppVaribles.setConfigValue("imageColor", s);
            if (getMessage("Color").equals(s)) {
                imageColor = ImageType.RGB;
            } else if (getMessage("ColorAlpha").equals(s)) {
                imageColor = ImageType.ARGB;
            } else if (getMessage("ShadesOfGray").equals(s)) {
                imageColor = ImageType.GRAY;
            } else if (getMessage("BlackOrWhite").equals(s)) {
                imageColor = ImageType.BINARY;
            } else {
                imageColor = ImageType.RGB;
            }

//            if ("tif".equals(imageFormat) || "bmp".equals(imageFormat)) {
            String[] compressionTypes = ImageTools.getCompressionTypes(imageFormat, imageColor);
            showCompressionTypes(compressionTypes);
//            }

            if (imageColor == ImageType.BINARY) {
                colorBox.setDisable(false);
                getColorConversion();
            } else {
                colorBox.setDisable(true);
                FxmlTools.setRadioFirstSelected(ColorConversionGroup);
            }

//            logger.debug(imageColor);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void showCompressionTypes(String[] types) {
        compressBox.getChildren().removeAll(compressBox.getChildren());
        CompressionGroup = new ToggleGroup();
        if (types == null) {
            RadioButton newv = new RadioButton("none");
            newv.setToggleGroup(CompressionGroup);
            compressBox.getChildren().add(newv);
            newv.setSelected(true);
            qualityBox.setDisable(true);
        } else {
            boolean cSelected = false;
            for (String ctype : types) {
                RadioButton newv = new RadioButton(ctype);
                newv.setToggleGroup(CompressionGroup);
                compressBox.getChildren().add(newv);
                if (!cSelected) {
                    newv.setSelected(true);
                    cSelected = true;
                }
            }
            qualityBox.setDisable(false);
        }

        CompressionGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                getCompressionType();
            }
        });
        FxmlTools.setRadioSelected(CompressionGroup, AppVaribles.getConfigValue("compressionType", null));
        getCompressionType();
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
                        AppVaribles.setConfigValue("densityInput", density + "");
                    } else {
                        densityInput.setStyle(badStyle);
                    }
                } catch (Exception e) {
                    densityInput.setStyle(badStyle);
                }
            } else {
                density = Integer.parseInt(s.substring(0, s.length() - 3));
                AppVaribles.setConfigValue("density", s);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void getCompressionType() {
        try {
            RadioButton selected = (RadioButton) CompressionGroup.getSelectedToggle();
            compressionType = selected.getText();
            AppVaribles.setConfigValue("compressionType", compressionType);
        } catch (Exception e) {
            compressionType = null;
        }
    }

    private void getColorConversion() {
        threshold = -1;
        thresholdInput.setStyle(goodStyle);
        try {
            RadioButton selected = (RadioButton) ColorConversionGroup.getSelectedToggle();
            String s = selected.getText();

            if (getMessage("Threshold").equals(s)) {
                colorConversion = ColorConversion.THRESHOLD;
            } else if (getMessage("OTSU").equals(s)) {
                colorConversion = ColorConversion.OTSU;
                AppVaribles.setConfigValue("colorConversion", s);
            } else {
                colorConversion = ColorConversion.DEFAULT;
                AppVaribles.setConfigValue("colorConversion", s);
            }

            if (colorConversion == ColorConversion.THRESHOLD) {
                try {
                    int v = Integer.parseInt(thresholdInput.getText());
                    if (v >= 0 && v <= 100) {
                        threshold = v;
                        AppVaribles.setConfigValue("thresholdInput", threshold + "");
                    } else {
                        thresholdInput.setStyle(badStyle);
                    }
                } catch (Exception e) {
                    thresholdInput.setStyle(badStyle);
                }
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void getQuality() {
        try {
            RadioButton selected = (RadioButton) QualityGroup.getSelectedToggle();
            String s = selected.getText();
            qualityInput.setStyle(goodStyle);
            if (getMessage("InputValue").equals(s)) {
                try {
                    int v = Integer.parseInt(qualityInput.getText());
                    if (v >= 0 && v <= 100) {
                        quality = v;
                        AppVaribles.setConfigValue("qualityInput", quality + "");
                    } else {
                        qualityInput.setStyle(badStyle);
                    }
                } catch (Exception e) {
                    qualityInput.setStyle(badStyle);
                }
            } else {
                quality = Integer.parseInt(s.substring(0, s.length() - 1));
                AppVaribles.setConfigValue("quality", s);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
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
        AppVaribles.setConfigValue("LastPath", file.getParent());
        targetPath.setText(file.getParent());
        targetPrefix.setText(FileTools.getFilePrefix(file.getName()));
        toPage.setText("");
        pdfInformation = null;
        fileInformationButton.setDisable(true);
        previewButton.setDisable(true);

        Task<Void> openPdfTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                pdfInformation = new PdfInformation(file);
                pdfInformation.loadInformation();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        fileInformationButton.setDisable(false);
                        previewButton.setDisable(false);
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
    private void showHelp() {

        try {
            File help = FileTools.getHelpFile(getClass(), "/docs/ImageHelp.html", "ImageHelp.html");
            Desktop.getDesktop().browse(help.toURI());
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    private void selectTargetPath() {
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setInitialDirectory(new File(AppVaribles.getConfigValue("LastPath", System.getProperty("user.home"))));
            File directory = chooser.showDialog(getStage());
            if (directory != null) {
                AppVaribles.setConfigValue("LastPath", directory.getPath());
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
            final boolean aColor = appendColor.isSelected();
            final boolean aCompression = appendCompressionType.isSelected();
            final boolean aQuality = appendQuality.isSelected();
            final int digit = (pdfInformation.getNumberOfPages() + "").length();
            final String filePrefix = targetPath.getText() + "/" + targetPrefix.getText();
            paraBox.setDisable(true);
            pauseButton.setVisible(true);
            pauseButton.setDisable(false);
            statusLabel.setText(getMessage("Handling...") + " " + getMessage("StartTime")
                    + ": " + DateTools.datetimeToString(startTime));
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
                            fname = filePrefix + "_" + pn;
                            if (aColor) {
                                if (colorConversion == ColorConversion.THRESHOLD) {
                                    fname += "_" + "BINARY-Threshold";
                                    if (threshold >= 0) {
                                        fname += "-" + threshold + "%";
                                    }
                                } else if (colorConversion == ColorConversion.OTSU) {
                                    fname += "_" + "BINARY-OTSU";
                                } else {
                                    fname += "_" + imageColor;
                                }
                            }
                            if (aDensity) {
                                fname += "_" + density + "dpi";
                            }
                            if (compressionType != null && !"none".equals(compressionType)) {
                                if (aCompression) {
                                    fname += "_" + compressionType.replace(" ", "_");
                                }
                                if (aQuality) {
                                    fname += "_quality-" + quality + "%";
                                }
                            }
                            fname += "." + imageFormat;
                            converted = i - from + 1;
                            updateProgress(converted, total);
                            updateMessage(converted + "/" + total);

                            BufferedImage image;
                            if (colorConversion == ColorConversion.THRESHOLD && threshold >= 0) {
                                image = renderer.renderImageWithDPI(i, density, ImageType.RGB);
                                image = ImageTools.color2BinaryWithPercentage(image, threshold);
                            } else if (colorConversion == ColorConversion.OTSU) {
                                image = renderer.renderImageWithDPI(i, density, ImageType.RGB);
                                image = ImageTools.color2Binary(image);
                            } else {
                                image = renderer.renderImageWithDPI(i, density, imageColor);
                            }

                            Map<String, Object> parameters = new HashMap();
                            parameters.put("density", density);
                            parameters.put("imageColor", imageColor);
                            if (compressionType != null && !"none".equals(compressionType)) {
                                parameters.put("compressionType", compressionType);
                            }
                            if (quality > 0) {
                                parameters.put("quality", quality);
                            }
                            ImageTools.writeImageFile(image, imageFormat, parameters, fname);

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
