package mara.mybox.controller;

import mara.mybox.fxml.FxmlStage;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.getMessage;
import mara.mybox.value.CommonValues;
import mara.mybox.fxml.FxmlTools;
import static mara.mybox.fxml.FxmlTools.badStyle;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.data.ImageInformation;
import mara.mybox.tools.PdfTools;
import mara.mybox.tools.PdfTools.PdfImageFormat;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.font.PDFont;

/**
 * @Author Mara
 * @CreateDate 2018-9-8
 * @Description
 * @License Apache License Version 2.0
 */
public class ImagesCombinePdfController extends ImageSourcesController {

    private final String ImageCombineMarginsKey, AuthorKey;
    protected int marginSize, pageWidth, pageHeight, jpegQuality, threshold;
    protected boolean isImageSize;
    private PdfImageFormat pdfFormat;

    @FXML
    protected CheckBox pageNumberCheck, ditherCheck;
    @FXML
    protected ComboBox<String> MarginsBox, standardSizeBox, standardDpiBox, jpegBox, fontBox;
    @FXML
    protected ToggleGroup sizeGroup, formatGroup;
    @FXML
    protected TextField customWidthInput, customHeightInput, authorInput, thresholdInput, headerInput;
    @FXML
    protected HBox sizeBox;
    @FXML
    protected ToolBar targetBar;

    public ImagesCombinePdfController() {
        ImageCombineMarginsKey = "ImageCombineMarginsKey";
        AuthorKey = "AuthorKey";
        fileExtensionFilter = CommonValues.PdfExtensionFilter;
    }

    @Override
    protected void initializeNext() {
        try {
            initSourceSection();
            initOptionsSection();
            initTargetSection();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    protected void initOptionsSection() {

        Tooltip tips = new Tooltip(getMessage("PdfPageSizeComments"));
        tips.setFont(new Font(16));
        FxmlTools.setComments(sizeBox, tips);

        sizeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkPageSize();
            }
        });
        checkPageSize();

        standardSizeBox.getItems().addAll(Arrays.asList(
                "A4-" + getMessage("Horizontal") + " (16k)  29.7cm x 21.0cm",
                "A4 (16k)  21.0cm x 29.7cm",
                "A5 (32k)  14.8cm x 21.0cm",
                "A6 (64k)  10.5cm x 14.8cm",
                "A3 (8k)   29.7cm x 42.0cm",
                "A2 (4k)   42.0cm x 59.4cm",
                "A1 (2k)   59.4cm x 84.1cm",
                "A0 (1k)   84.1cm x 118.9cm",
                "B5        17.6cm x 25.0cm",
                "B4	    25.0cm x 35.3cm",
                "B2	    35.3cm x 50.0cm",
                "C4	    22.9cm x 32.4cm",
                "C5	    16.2cm x 22.9cm",
                "C6	    11.4cm x 16.2cm"
        ));
        standardSizeBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov,
                    String oldValue, String newValue) {
                checkStandardValues();
            }
        });
        standardSizeBox.getSelectionModel().select(0);
        isImageSize = true;

        standardDpiBox.getItems().addAll(Arrays.asList(
                "72 dpi",
                "96 dpi",
                "150 dpi",
                "300 dpi",
                "450 dpi",
                "720 dpi",
                "120 dpi",
                "160 dpi",
                "240 dpi",
                "320 dpi"
        ));
        standardDpiBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov,
                    String oldValue, String newValue) {
                checkStandardValues();
            }
        });
        standardDpiBox.getSelectionModel().select(0);

        customWidthInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                checkCustomValues();
            }
        });
        customHeightInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                checkCustomValues();
            }
        });

        formatGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkFormat();
            }
        });
        checkFormat();

        jpegBox.getItems().addAll(Arrays.asList("100", "75", "90", "50", "60", "80", "30", "10"));
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
        FxmlTools.setComments(ditherCheck, new Tooltip(getMessage("DitherComments")));

        MarginsBox.getItems().addAll(Arrays.asList("20", "10", "15", "5", "25", "30"));
        MarginsBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov,
                    String oldValue, String newValue) {
                try {
                    marginSize = Integer.valueOf(newValue);
                    if (marginSize >= 0) {
                        MarginsBox.getEditor().setStyle(null);
                        AppVaribles.setUserConfigValue(ImageCombineMarginsKey, newValue);
                    } else {
                        marginSize = 0;
                        MarginsBox.getEditor().setStyle(badStyle);
                    }

                } catch (Exception e) {
                    marginSize = 0;
                    MarginsBox.getEditor().setStyle(badStyle);
                }
            }
        });
        MarginsBox.getSelectionModel().select(AppVaribles.getUserConfigValue(ImageCombineMarginsKey, "20"));

        if (fontBox != null) {
            fontBox.getItems().addAll(Arrays.asList(
                    "幼圆",
                    "仿宋",
                    "隶书",
                    "Helvetica",
                    "Courier",
                    "Times New Roman"
            ));
            fontBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov,
                        String oldValue, String newValue) {
                }
            });
            fontBox.getSelectionModel().select(0);

            tips = new Tooltip(getMessage("FontFileComments"));
            tips.setFont(new Font(16));
            FxmlTools.setComments(fontBox, tips);

        }

        authorInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                AppVaribles.setUserConfigValue(AuthorKey, newValue);
            }
        });
        authorInput.setText(AppVaribles.getUserConfigValue(AuthorKey, System.getProperty("user.name")));

    }

    private void initTargetSection() {

        targetBar.disableProperty().bind(
                Bindings.isEmpty(sourceImages)
                        .or(customWidthInput.styleProperty().isEqualTo(badStyle))
                        .or(customHeightInput.styleProperty().isEqualTo(badStyle))
                        .or(jpegBox.styleProperty().isEqualTo(badStyle))
                        .or(thresholdInput.styleProperty().isEqualTo(badStyle))
        );

    }

    private void checkPageSize() {
        standardSizeBox.setDisable(true);
        standardDpiBox.setDisable(true);
        customWidthInput.setDisable(true);
        customHeightInput.setDisable(true);
        customWidthInput.setStyle(null);
        customHeightInput.setStyle(null);
        isImageSize = false;

        RadioButton selected = (RadioButton) sizeGroup.getSelectedToggle();
        if (AppVaribles.getMessage("ImagesSize").equals(selected.getText())) {
            isImageSize = true;
        } else if (AppVaribles.getMessage("StandardSize").equals(selected.getText())) {
            standardSizeBox.setDisable(false);
            standardDpiBox.setDisable(false);
            checkStandardValues();

        } else if (AppVaribles.getMessage("Custom").equals(selected.getText())) {
            customWidthInput.setDisable(false);
            customHeightInput.setDisable(false);
            checkCustomValues();
        }

//        AppVaribles.setUserConfigValue(ImageCombineSizeKey, selected.getText());
    }

    private int calculateCmPixels(float cm, int dpi) {
        return (int) Math.round(cm * dpi / 2.54);
    }

    private void checkStandardValues() {
        String d = standardDpiBox.getSelectionModel().getSelectedItem();
        int dpi = 72;
        try {
            dpi = Integer.valueOf(d.substring(0, d.length() - 4));
        } catch (Exception e) {
        }
        String s = standardSizeBox.getSelectionModel().getSelectedItem();
        if (s.startsWith("A4-" + getMessage("Horizontal"))) {
            pageWidth = calculateCmPixels(29.7f, dpi);
            pageHeight = calculateCmPixels(21.0f, dpi);
        } else {
            switch (s.substring(0, 2)) {
                case "A4":
                    pageWidth = calculateCmPixels(21.0f, dpi);
                    pageHeight = calculateCmPixels(29.7f, dpi);
                    break;
                case "A5":
                    pageWidth = calculateCmPixels(14.8f, dpi);
                    pageHeight = calculateCmPixels(21.0f, dpi);
                    break;
                case "A6":
                    pageWidth = calculateCmPixels(10.5f, dpi);
                    pageHeight = calculateCmPixels(14.8f, dpi);
                    break;
                case "A3":
                    pageWidth = calculateCmPixels(29.7f, dpi);
                    pageHeight = calculateCmPixels(42.0f, dpi);
                    break;
                case "A2":
                    pageWidth = calculateCmPixels(42.0f, dpi);
                    pageHeight = calculateCmPixels(59.4f, dpi);
                    break;
                case "A1":
                    pageWidth = calculateCmPixels(59.4f, dpi);
                    pageHeight = calculateCmPixels(84.1f, dpi);
                    break;

                case "A0":
                    pageWidth = calculateCmPixels(84.1f, dpi);
                    pageHeight = calculateCmPixels(118.9f, dpi);
                    break;
                case "B5":
                    pageWidth = calculateCmPixels(17.6f, dpi);
                    pageHeight = calculateCmPixels(25.0f, dpi);
                    break;
                case "B4":
                    pageWidth = calculateCmPixels(25.0f, dpi);
                    pageHeight = calculateCmPixels(35.3f, dpi);
                    break;
                case "B2":
                    pageWidth = calculateCmPixels(35.3f, dpi);
                    pageHeight = calculateCmPixels(50.0f, dpi);
                    break;
                case "C4":
                    pageWidth = calculateCmPixels(22.9f, dpi);
                    pageHeight = calculateCmPixels(32.4f, dpi);
                    break;
                case "C5":
                    pageWidth = calculateCmPixels(16.2f, dpi);
                    pageHeight = calculateCmPixels(22.9f, dpi);
                    break;
                case "C6":
                    pageWidth = calculateCmPixels(11.4f, dpi);
                    pageHeight = calculateCmPixels(16.2f, dpi);
                    break;
            }
        }
        customWidthInput.setText(pageWidth + "");
        customHeightInput.setText(pageHeight + "");
    }

    private void checkCustomValues() {

        RadioButton selected = (RadioButton) sizeGroup.getSelectedToggle();
        if (!AppVaribles.getMessage("Custom").equals(selected.getText())) {
            return;
        }
        try {
            pageWidth = Integer.valueOf(customWidthInput.getText());
            if (pageWidth > 0) {
                customWidthInput.setStyle(null);
            } else {
                pageWidth = 0;
                customWidthInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            pageWidth = 0;
            customWidthInput.setStyle(badStyle);
        }

        try {
            pageHeight = Integer.valueOf(customHeightInput.getText());
            if (pageHeight > 0) {
                customHeightInput.setStyle(null);
            } else {
                pageHeight = 0;
                customHeightInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            pageHeight = 0;
            customHeightInput.setStyle(badStyle);
        }

    }

    private void checkFormat() {
        jpegBox.setDisable(true);
        jpegBox.setStyle(null);
        thresholdInput.setDisable(true);

        RadioButton selected = (RadioButton) formatGroup.getSelectedToggle();
        if (AppVaribles.getMessage("PNG").equals(selected.getText())) {
            pdfFormat = PdfImageFormat.Original;
        } else if (AppVaribles.getMessage("CCITT4").equals(selected.getText())) {
            pdfFormat = PdfImageFormat.Tiff;
            thresholdInput.setDisable(false);
        } else if (AppVaribles.getMessage("JpegQuailty").equals(selected.getText())) {
            pdfFormat = PdfImageFormat.Jpeg;
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
            if (threshold >= 0 && threshold <= 255) {
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

    @FXML
    @Override
    public void saveAction() {
        if (sourceImages == null || sourceImages.isEmpty()) {
            return;
        }
        if (hasSampled()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(AppVaribles.getMessage("SureSampled"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSure = new ButtonType(AppVaribles.getMessage("Sure"));
            ButtonType buttonCancel = new ButtonType(AppVaribles.getMessage("Cancel"));
            alert.getButtonTypes().setAll(buttonSure, buttonCancel);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonCancel) {
                return;
            }
        }

        final FileChooser fileChooser = new FileChooser();
        File path = AppVaribles.getUserConfigPath(targetPathKey);
        if (path.exists()) {
            fileChooser.setInitialDirectory(path);
        }
        fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
        final File file = fileChooser.showSaveDialog(getMyStage());
        if (file == null) {
            return;
        }
        AppVaribles.setUserConfigValue(targetPathKey, file.getParent());
        task = new Task<Void>() {
            private boolean fail;

            @Override
            protected Void call() throws Exception {
                try {
                    try (PDDocument document = new PDDocument(AppVaribles.PdfMemUsage)) {
                        PDFont font = PdfTools.getFont(document, fontBox.getSelectionModel().getSelectedItem());
                        PDDocumentInformation info = new PDDocumentInformation();
                        info.setCreationDate(Calendar.getInstance());
                        info.setModificationDate(Calendar.getInstance());
                        info.setProducer("MyBox v" + CommonValues.AppVersion);
                        info.setAuthor(authorInput.getText());
                        document.setDocumentInformation(info);
                        int count = 0;
                        int total = sourceImages.size();
                        for (ImageInformation source : sourceImages) {
                            if (task.isCancelled()) {
                                document.close();
                                return null;
                            }
                            BufferedImage bufferedImage = ImageFileReaders.getBufferedImage(source);
                            if (bufferedImage != null) {
                                PdfTools.writePage(document, font, source.getImageFormat(), bufferedImage,
                                        ++count, total, pdfFormat,
                                        threshold, jpegQuality, isImageSize, pageNumberCheck.isSelected(),
                                        pageWidth, pageHeight, marginSize, headerInput.getText(), ditherCheck.isSelected());
                            }
                        }
                        document.save(file);
                        fail = false;
                    }

                } catch (Exception e) {
                    fail = true;
                    logger.error(e.toString());
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (!fail && file.exists()) {
                                popInformation(AppVaribles.getMessage("Successful"));
                                if (viewCheck.isSelected()) {
                                    FxmlStage.openTarget(getClass(), null, file.getAbsolutePath());
                                }
                            } else {
                                popError(AppVaribles.getMessage("ImageCombinePdfFail"));
                            }
                        } catch (Exception e) {
                            logger.error(e.toString());
                        }
                    }
                });
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

}
