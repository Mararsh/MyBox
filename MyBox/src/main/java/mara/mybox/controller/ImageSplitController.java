package mara.mybox.controller;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import static mara.mybox.objects.AppVaribles.logger;
import mara.mybox.image.ImageConvertTools;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.objects.AppVaribles;
import mara.mybox.fxml.FxmlTools;
import mara.mybox.fxml.FxmlImageTools;
import static mara.mybox.fxml.FxmlTools.badStyle;
import mara.mybox.image.ImageValueTools;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.imagefile.ImageTiffFile;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.objects.CommonValues;
import mara.mybox.objects.ImageAttributes;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.PdfTools;
import mara.mybox.tools.PdfTools.PdfImageFormat;
import mara.mybox.tools.ValueTools;
import org.apache.pdfbox.rendering.ImageType;

/**
 * @Author Mara
 * @CreateDate 2018-8-8
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageSplitController extends ImageViewerController {

    private List<Integer> rows, cols;
    private int rowsNumber, colsNumber, width, height,
            marginSize, pageWidth, pageHeight, jpegQuality, threshold;
    private boolean isImageSize, isSettingValues;
    private double scale;
    private PdfImageFormat pdfFormat;
    protected SimpleBooleanProperty splitValid;
    private SplitMethod splitMethod;

    public static enum SplitMethod {
        ByNumber, BySize, Customize
    }

    @FXML
    private TabPane tabPane;
    @FXML
    private Tab splitTab, tiffTab, pdfTab;
    @FXML
    private ToggleGroup splitGroup, sizeGroup, formatGroup, colorGroup, compressionGroup, binaryGroup;
    @FXML
    private Button imagesButton, tiffButton, pdfButton, okButton;
    @FXML
    private TextField rowsInput, colsInput, customizedRowsInput, customizedColsInput,
            customWidthInput, customHeightInput, authorInput, pdfThresholdInput, headerInput, tiffThresholdInput;
    @FXML
    private CheckBox displaySizeCheck, pageNumberCheck;
    @FXML
    private ComboBox<Integer> lineWidthBox;
    @FXML
    protected ComboBox<String> MarginsBox, standardSizeBox, standardDpiBox, jpegBox, fontBox;
    @FXML
    private ColorPicker lineColorPicker;
    @FXML
    private ToolBar opBar;
    @FXML
    private HBox showBox, byBox, customBox, compressionBox, binaryBox, predefinedBox;
    @FXML
    private Label rowsLabel, colsLabel;

    @Override
    protected void initializeNext2() {
        try {
            initCommon();
            initSplitTab();
            initTiffTab();
            initPdfTab();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void initCommon() {
        scrollPane.setDisable(true);
        opBar.setDisable(true);
        tabPane.setDisable(true);
        showBox.setDisable(true);

        splitValid = new SimpleBooleanProperty(false);

        lineWidthBox.getItems().addAll(Arrays.asList(15, 10, 20, 5, 30, 8, 40));
        lineWidthBox.getSelectionModel().select(0);
        lineWidthBox.valueProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> ov,
                    Integer oldValue, Integer newValue) {
                indicateSplit();
            }
        });

        lineColorPicker.setValue(Color.RED);
        lineColorPicker.valueProperty().addListener(new ChangeListener<Color>() {
            @Override
            public void changed(ObservableValue<? extends Color> ov,
                    Color oldValue, Color newValue) {
                indicateSplit();

            }
        });

        Tooltip tips = new Tooltip(getMessage("SplitComments"));
        tips.setFont(new Font(16));
        FxmlTools.setComments(lineWidthBox, tips);
        FxmlTools.setComments(lineColorPicker, tips);

        tips = new Tooltip(getMessage("FilePrefixInput"));
        tips.setFont(new Font(16));
        FxmlTools.setComments(imagesButton, tips);

        displaySizeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                indicateSplit();
            }
        });

        imagesButton.disableProperty().bind(
                splitValid.isEqualTo(new SimpleBooleanProperty(false))
        );
        pdfButton.disableProperty().bind(
                splitValid.isEqualTo(new SimpleBooleanProperty(false))
                        .or(customWidthInput.styleProperty().isEqualTo(badStyle))
                        .or(customHeightInput.styleProperty().isEqualTo(badStyle))
                        .or(jpegBox.styleProperty().isEqualTo(badStyle))
                        .or(pdfThresholdInput.styleProperty().isEqualTo(badStyle))
        );
        tiffButton.disableProperty().bind(
                splitValid.isEqualTo(new SimpleBooleanProperty(false))
                        .or(tiffThresholdInput.styleProperty().isEqualTo(badStyle))
        );
    }

    private void initSplitTab() {
        splitGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkSplitMethod();
            }
        });
        checkSplitMethod();

        rowsInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                if (splitMethod == SplitMethod.ByNumber) {
                    checkNumberValues();
                } else if (splitMethod == SplitMethod.BySize) {
                    checkSizeValues();
                }
            }
        });
        colsInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                if (splitMethod == SplitMethod.ByNumber) {
                    checkNumberValues();
                } else if (splitMethod == SplitMethod.BySize) {
                    checkSizeValues();
                }
            }
        });

        customizedRowsInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                checkCustomValues();
            }
        });
        customizedColsInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                checkCustomValues();
            }
        });

        okButton.disableProperty().bind(rowsInput.styleProperty().isEqualTo(badStyle)
                .or(colsInput.styleProperty().isEqualTo(badStyle))
        );
    }

    private void checkSplitMethod() {
        byBox.setDisable(true);
        customBox.setDisable(true);
        imageView.setImage(image);
        RadioButton selected = (RadioButton) splitGroup.getSelectedToggle();
        if (AppVaribles.getMessage("Customize").equals(selected.getText())) {
            splitMethod = SplitMethod.Customize;
            customBox.setDisable(false);
            checkCustomValues();
        } else if (AppVaribles.getMessage("ByNumber").equals(selected.getText())) {
            splitMethod = SplitMethod.ByNumber;
            byBox.setDisable(false);
            predefinedBox.setDisable(false);
            rowsLabel.setText(getMessage("RowsNumber"));
            colsLabel.setText(getMessage("ColumnsNumber"));
            isSettingValues = true;
            rowsInput.setText("3");
            colsInput.setText("3");
            isSettingValues = false;
            checkNumberValues();
        } else if (AppVaribles.getMessage("BySize").equals(selected.getText())) {
            splitMethod = SplitMethod.BySize;
            byBox.setDisable(false);
            predefinedBox.setDisable(true);
            rowsLabel.setText(getMessage("Width"));
            colsLabel.setText(getMessage("Height"));
            isSettingValues = true;
            rowsInput.setText((int) (imageInformation.getWidth() / 3) + "");
            colsInput.setText((int) (imageInformation.getHeight() / 3) + "");
            isSettingValues = false;
            checkSizeValues();
        }
    }

    private void checkNumberValues() {
        if (isSettingValues) {
            return;
        }
        rowsNumber = -1;
        colsNumber = -1;
        if (rowsInput.getText().isEmpty()) {
            rowsNumber = 0;
            rowsInput.setStyle(null);
        } else {
            try {
                rowsNumber = Integer.valueOf(rowsInput.getText());
                rowsInput.setStyle(null);
                if (rowsNumber > 0) {
                    rowsInput.setStyle(null);
                } else {
                    rowsInput.setStyle(badStyle);
                }
            } catch (Exception e) {
                rowsInput.setStyle(badStyle);
            }
        }

        if (colsInput.getText().isEmpty()) {
            colsNumber = 0;
            colsInput.setStyle(null);
        } else {
            try {
                colsNumber = Integer.valueOf(colsInput.getText());
                colsInput.setStyle(null);
                if (colsNumber > 0) {
                    colsInput.setStyle(null);
                } else {
                    colsInput.setStyle(badStyle);
                }
            } catch (Exception e) {
                colsInput.setStyle(badStyle);
            }
        }
    }

    private void checkSizeValues() {
        if (isSettingValues) {
            return;
        }
        try {
            int v = Integer.valueOf(rowsInput.getText());
            if (v > 0 && v < imageInformation.getWidth()) {
                rowsInput.setStyle(null);
                width = v;
            } else {
                rowsInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            rowsInput.setStyle(badStyle);
        }
        try {
            int v = Integer.valueOf(colsInput.getText());
            if (v > 0 && v < imageInformation.getHeight()) {
                colsInput.setStyle(null);
                height = v;
            } else {
                colsInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            colsInput.setStyle(badStyle);
        }
    }

    private void checkCustomValues() {
        boolean isValidRows = true, isValidcols = true;
        rows = new ArrayList();
        rows.add(0);
        rows.add((int) imageInformation.getHeight() - 1);
        cols = new ArrayList();
        cols.add(0);
        cols.add((int) imageInformation.getWidth() - 1);
        customizedRowsInput.setStyle(null);
        customizedColsInput.setStyle(null);

        if (!customizedRowsInput.getText().isEmpty()) {
            String[] rowStrings = customizedRowsInput.getText().split(",");
            for (String row : rowStrings) {
                try {
                    int value = Integer.valueOf(row.trim());
                    if (value < 0 || value > imageInformation.getHeight() - 1) {
                        customizedRowsInput.setStyle(badStyle);
                        isValidRows = false;
                        break;
                    }
                    if (!rows.contains(value)) {
                        rows.add(value);
                    }
                } catch (Exception e) {
                    customizedRowsInput.setStyle(badStyle);
                    isValidRows = false;
                    break;
                }
            }
        }

        if (!customizedColsInput.getText().isEmpty()) {
            String[] colStrings = customizedColsInput.getText().split(",");
            for (String col : colStrings) {
                try {
                    int value = Integer.valueOf(col.trim());
                    if (value <= 0 || value >= imageInformation.getWidth() - 1) {
                        customizedColsInput.setStyle(badStyle);
                        isValidcols = false;
                        break;
                    }
                    if (!cols.contains(value)) {
                        cols.add(value);
                    }
                } catch (Exception e) {
                    customizedColsInput.setStyle(badStyle);
                    isValidcols = false;
                    break;
                }
            }
        }

        if (isValidRows || isValidcols) {
            indicateSplit();
        } else {
//            imageView.setImage(image);
//            bottomLabel.setText("");
            popInformation(getMessage("SplitCustomComments"));
        }
    }

    private void initTiffTab() {
        try {

            attributes = new ImageAttributes();
            attributes.setImageFormat("tif");

            colorGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkColorType();
                }
            });
            checkColorType();

            compressionGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkCompressionType();
                }
            });

            binaryGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkBinary();
                }
            });

            tiffThresholdInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkTiffThreshold();
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkColorType() {
        try {
            binaryBox.setDisable(true);
            tiffThresholdInput.setStyle(null);
            RadioButton selected = (RadioButton) colorGroup.getSelectedToggle();
            String s = selected.getText();
            if (getMessage("Colorful").equals(s)) {
                attributes.setColorSpace(ImageType.RGB);
            } else if (getMessage("ColorAlpha").equals(s)) {
                attributes.setColorSpace(ImageType.ARGB);
            } else if (getMessage("ShadesOfGray").equals(s)) {
                attributes.setColorSpace(ImageType.GRAY);
            } else if (getMessage("BlackOrWhite").equals(s)) {
                attributes.setColorSpace(ImageType.BINARY);
                checkBinary();
            } else {
                attributes.setColorSpace(ImageType.RGB);
            }
            setCompressionTypes();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void setCompressionTypes() {
        try {
            compressionBox.getChildren().clear();
            compressionGroup = new ToggleGroup();
            String[] compressionTypes
                    = ImageValueTools.getCompressionTypes("tif", attributes.getColorSpace());
            for (String ctype : compressionTypes) {
                if (ctype.equals("ZLib")) { // This type looks not work for mutiple frames tiff file
                    continue;
                }
                RadioButton newv = new RadioButton(ctype);
                newv.setToggleGroup(compressionGroup);
                compressionBox.getChildren().add(newv);
            }

            compressionGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkCompressionType();
                }
            });
            compressionGroup.selectToggle((RadioButton) compressionBox.getChildren().get(0));

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void checkCompressionType() {
        try {
            RadioButton selected = (RadioButton) compressionGroup.getSelectedToggle();
            attributes.setCompressionType(selected.getText());
        } catch (Exception e) {
            attributes.setCompressionType(null);
        }
    }

    protected void checkBinary() {
        try {
            binaryBox.setDisable(false);
            tiffThresholdInput.setStyle(null);
            RadioButton selected = (RadioButton) binaryGroup.getSelectedToggle();
            String s = selected.getText();
            if (getMessage("Threshold").equals(s)) {
                attributes.setBinaryConversion(ImageAttributes.BinaryConversion.BINARY_THRESHOLD);
                checkTiffThreshold();
            } else if (getMessage("OTSU").equals(s)) {
                attributes.setBinaryConversion(ImageAttributes.BinaryConversion.BINARY_OTSU);
            } else {
                attributes.setBinaryConversion(ImageAttributes.BinaryConversion.DEFAULT);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void checkTiffThreshold() {
        try {
            if (attributes.getBinaryConversion() != ImageAttributes.BinaryConversion.BINARY_THRESHOLD) {
                tiffThresholdInput.setStyle(null);
                return;
            }
            int inputValue = Integer.parseInt(tiffThresholdInput.getText());
            if (inputValue >= 0 && inputValue <= 100) {
                attributes.setThreshold(inputValue);
                tiffThresholdInput.setStyle(null);
            } else {
                tiffThresholdInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            tiffThresholdInput.setStyle(badStyle);
        }
    }

    private void initPdfTab() {

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
                checkPdfCustomValues();
            }
        });
        customHeightInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                checkPdfCustomValues();
            }
        });

        formatGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkPdfFormat();
            }
        });
        checkPdfFormat();

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

        pdfThresholdInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                checkPdfThreshold();
            }
        });
        checkPdfThreshold();

        MarginsBox.getItems().addAll(Arrays.asList("20", "10", "15", "5", "25", "30"));
        MarginsBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov,
                    String oldValue, String newValue) {
                try {
                    marginSize = Integer.valueOf(newValue);
                    if (marginSize >= 0) {
                        MarginsBox.getEditor().setStyle(null);
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
        MarginsBox.getSelectionModel().select("20");

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

            Tooltip tips = new Tooltip(getMessage("FontFileComments"));
            tips.setFont(new Font(16));
            FxmlTools.setComments(fontBox, tips);

        }

        authorInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                AppVaribles.setUserConfigValue("AuthorKey", newValue);
            }
        });
        authorInput.setText(AppVaribles.getUserConfigValue("AuthorKey", System.getProperty("user.name")));

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
            checkPdfCustomValues();
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

    private void checkPdfCustomValues() {

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

    private void checkPdfFormat() {
        jpegBox.setDisable(true);
        jpegBox.setStyle(null);
        pdfThresholdInput.setDisable(true);

        RadioButton selected = (RadioButton) formatGroup.getSelectedToggle();
        if (AppVaribles.getMessage("PNG").equals(selected.getText())) {
            pdfFormat = PdfImageFormat.Original;
        } else if (AppVaribles.getMessage("CCITT4").equals(selected.getText())) {
            pdfFormat = PdfImageFormat.Tiff;
            pdfThresholdInput.setDisable(false);
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

    private void checkPdfThreshold() {
        try {
            if (pdfThresholdInput.getText().isEmpty()) {
                threshold = -1;
                pdfThresholdInput.setStyle(null);
                return;
            }
            threshold = Integer.valueOf(pdfThresholdInput.getText());
            if (threshold >= 0 && threshold <= 100) {
                pdfThresholdInput.setStyle(null);
            } else {
                threshold = -1;
                pdfThresholdInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            threshold = -1;
            pdfThresholdInput.setStyle(badStyle);
        }
    }

    @Override
    public void afterImageLoaded() {
        try {
            super.afterImageLoaded();
            if (image == null) {
                return;
            }

            scrollPane.setDisable(false);
            opBar.setDisable(false);
            tabPane.setDisable(false);
            showBox.setDisable(false);

            cols = new ArrayList();
            rows = new ArrayList();
            Integer v = 10 + imageInformation.getWidth() * 3 / 1000;
            lineWidthBox.getItems().add(0, v);
            lineWidthBox.getSelectionModel().select(v);

            if (imageInformation.isIsSampled()) {
                scale = imageInformation.getWidth() / image.getWidth();
            } else {
                scale = 1;
            }

            splitValid.set(false);

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    @Override
    protected void handleSampledImage() {
//            logger.debug(availableMem + "  " + pixelsSize + "  " + requiredMem + " " + sampledWidth + " " + sampledSize);
        if (imageInformation.getIndex() > 0) {
            getMyStage().setTitle(getBaseTitle() + " " + sourceFile.getAbsolutePath()
                    + " - " + getMessage("Image") + " " + imageInformation.getIndex() + " " + getMessage("Sampled"));
        } else {
            getMyStage().setTitle(getBaseTitle() + " " + sourceFile.getAbsolutePath() + " " + getMessage("Sampled"));
        }

        if (bottomLabel != null) {
            if (sizes == null) {
                bottomLabel.setText(getMessage("ImageSampled") + "\n\n"
                        + getMessage("ImagePartComments"));
            } else {
                int sampledSize = (int) (image.getWidth() * image.getHeight() * imageInformation.getColorChannels() / (1014 * 1024));
                String msg = MessageFormat.format(getMessage("ImageTooLarge"),
                        imageInformation.getWidth(), imageInformation.getHeight(), imageInformation.getColorChannels(),
                        sizes.get("pixelsSize"), sizes.get("requiredMem"), sizes.get("availableMem"),
                        (int) image.getWidth(), (int) image.getHeight(), sampledSize) + "\n\n"
                        + getMessage("ImagePartComments");
                bottomLabel.setText(msg);
            }
        }
    }

    @FXML
    private void okAction(ActionEvent event) {
        if (splitMethod == SplitMethod.ByNumber) {
            divideImageByNumber();
        } else if (splitMethod == SplitMethod.BySize) {
            divideImageBySize();
        }
    }

    private void divideImageBySize() {
        if (width <= 0 || height <= 0) {
            return;
        }
        cols = new ArrayList();
        cols.add(0);
        int v = width - 1;
        while (v < imageInformation.getWidth()) {
            cols.add(v);
            v += width - 1;
        }
        cols.add(imageInformation.getWidth() - 1);

        rows = new ArrayList();
        rows.add(0);
        v = height - 1;
        while (v < imageInformation.getHeight()) {
            rows.add(v);
            v += height - 1;
        }
        rows.add(imageInformation.getHeight() - 1);

        indicateSplit();
    }

    private void divideImageByNumber() {
        if (rowsNumber < 0 || colsNumber < 0) {
            return;
        }
        cols = new ArrayList();
        cols.add(0);
        for (int i = 1; i < colsNumber; i++) {
            int v = i * imageInformation.getWidth() / colsNumber;
            cols.add(v);
        }
        cols.add(imageInformation.getWidth() - 1);
        rows = new ArrayList();
        rows.add(0);
        for (int i = 1; i < rowsNumber; i++) {
            int v = i * imageInformation.getHeight() / rowsNumber;
            rows.add(v);
        }
        rows.add(imageInformation.getHeight() - 1);
        indicateSplit();
    }

    private void divideImageByNumber(int rows, int cols) {
        isSettingValues = true;
        rowsInput.setText(rows + "");
        colsInput.setText(cols + "");
        isSettingValues = false;
        checkNumberValues();
        divideImageByNumber();
    }

    @FXML
    private void do42Action(ActionEvent event) {
        divideImageByNumber(4, 2);
    }

    @FXML
    private void do43Action(ActionEvent event) {
        divideImageByNumber(4, 3);
    }

    @FXML
    private void do44Action(ActionEvent event) {
        divideImageByNumber(4, 4);
    }

    @FXML
    private void do13Action(ActionEvent event) {
        divideImageByNumber(1, 3);
    }

    @FXML
    private void do31Action(ActionEvent event) {
        divideImageByNumber(3, 1);
    }

    @FXML
    private void do12Action(ActionEvent event) {
        divideImageByNumber(1, 2);
    }

    @FXML
    private void do21Action(ActionEvent event) {
        divideImageByNumber(2, 1);
    }

    @FXML
    private void do32Action(ActionEvent event) {
        divideImageByNumber(3, 2);
    }

    @FXML
    private void do23Action(ActionEvent event) {
        divideImageByNumber(2, 3);
    }

    @FXML
    private void do22Action(ActionEvent event) {
        divideImageByNumber(2, 2);
    }

    @FXML
    private void do33Action(ActionEvent event) {
        divideImageByNumber(3, 3);

    }

    @FXML
    private void clearRows(ActionEvent event) {
        customizedRowsInput.setText("");
    }

    @FXML
    private void clearCols(ActionEvent event) {
        customizedColsInput.setText("");
    }

    @FXML
    @Override
    public void clickImage(MouseEvent event) {
        if (image == null || splitMethod != SplitMethod.Customize) {
            return;
        }
//        imageView.setCursor(Cursor.OPEN_HAND);
        bottomLabel.setText(getMessage("SplitCustomComments"));

        if (event.getButton() == MouseButton.PRIMARY) {

            int y = (int) Math.round(event.getY() * image.getHeight() * scale / imageView.getBoundsInLocal().getHeight());
            String str = customizedRowsInput.getText().trim();
            if (str.isEmpty()) {
                customizedRowsInput.setText(y + "");
            } else {
                customizedRowsInput.setText(str + "," + y);
            }

        } else if (event.getButton() == MouseButton.SECONDARY) {
            int x = (int) Math.round(event.getX() * image.getWidth() * scale / imageView.getBoundsInLocal().getWidth());
            String str = customizedColsInput.getText().trim();
            if (str.isEmpty()) {
                customizedColsInput.setText(x + "");
            } else {
                customizedColsInput.setText(str + "," + x);
            }

        }

    }

    private void indicateSplit() {
        if (image == null) {
            return;
        }
        if (rows == null || cols == null
                || rows.size() < 2 || cols.size() < 2
                || (rows.size() == 2 && cols.size() == 2)) {
            imageView.setImage(image);
            splitValid.set(false);
            return;
        }

        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                ValueTools.sortList(rows);
                ValueTools.sortList(cols);
                final Image newImage = FxmlImageTools.indicateSplit(image, rows, cols,
                        lineColorPicker.getValue(), lineWidthBox.getValue(),
                        displaySizeCheck.isSelected(), scale);
                if (task.isCancelled()) {
                    return null;
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImage(newImage);
                        String comments = AppVaribles.getMessage("SplittedNumber") + ": "
                                + (cols.size() - 1) * (rows.size() - 1);
                        if (splitMethod == SplitMethod.ByNumber) {
                            comments += "  " + AppVaribles.getMessage("EachSplittedImageActualSize") + ": "
                                    + (int) (imageInformation.getWidth() / (cols.size() - 1))
                                    + " x " + (int) (imageInformation.getHeight() / (rows.size() - 1));

                        } else {
                            comments += "  " + AppVaribles.getMessage("EachSplittedImageActualSizeComments");
                        }
                        bottomLabel.setText(comments);
                        splitValid.set(true);
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

    private File validationBeforeSave(List<FileChooser.ExtensionFilter> ext, String diagTitle) {
        if (image == null || !splitValid.getValue()
                || rows == null || cols == null
                || rows.size() < 1 || cols.size() < 1) {
            return null;
        }
        if (imageInformation.isIsSampled()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(AppVaribles.getMessage("SureSampled"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSure = new ButtonType(AppVaribles.getMessage("Sure"));
            ButtonType buttonCancel = new ButtonType(AppVaribles.getMessage("Cancel"));
            alert.getButtonTypes().setAll(buttonSure, buttonCancel);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonCancel) {
                return null;
            }
        }
        final FileChooser fileChooser = new FileChooser();
        File path = new File(AppVaribles.getUserConfigPath(targetPathKey, CommonValues.UserFilePath));
        fileChooser.setInitialDirectory(path);
        fileChooser.getExtensionFilters().addAll(ext);
        if (diagTitle != null) {
            fileChooser.setTitle(diagTitle);
        }
        final File targetFile = fileChooser.showSaveDialog(getMyStage());
        if (targetFile == null) {
            return null;
        }
        AppVaribles.setUserConfigValue(targetPathKey, targetFile.getParent());
        return targetFile;
    }

    @FXML
    private void saveAsImagesAction(ActionEvent event) {
        final File targetFile
                = validationBeforeSave(CommonValues.ImageExtensionFilter, getMessage("FilePrefixInput"));
        if (targetFile == null) {
            return;
        }
        task = new Task<Void>() {
            List<String> fileNames = new ArrayList<>();

            @Override
            protected Void call() throws Exception {
                int x1, y1, x2, y2;
                final String targetFormat = FileTools.getFileSuffix(targetFile.getAbsolutePath()).toLowerCase();
                final String sourceFormat = imageInformation.getImageFormat();
                final String filePrefix = FileTools.getFilePrefix(targetFile.getAbsolutePath());
                final String filename = sourceFile.getAbsolutePath();
                BufferedImage wholeSource = null;
                if (!imageInformation.isIsSampled()) {
                    wholeSource = FxmlImageTools.getBufferedImage(image);
                }
                for (int i = 0; i < rows.size() - 1; i++) {
                    if (task.isCancelled()) {
                        return null;
                    }
                    y1 = rows.get(i);
                    y2 = rows.get(i + 1);
                    for (int j = 0; j < cols.size() - 1; j++) {
                        if (task.isCancelled()) {
                            return null;
                        }
                        x1 = cols.get(j);
                        x2 = cols.get(j + 1);
                        BufferedImage target;
                        if (imageInformation.isIsSampled()) {
                            target = ImageFileReaders.readRectangle(sourceFormat, filename, x1, y1, x2, y2);
                        } else {
                            target = ImageConvertTools.cropImage(wholeSource, x1, y1, x2, y2);
                        }
                        String fileName = filePrefix + "_"
                                + (rows.size() - 1) + "x" + (cols.size() - 1) + "_"
                                + (i + 1) + "-" + (j + 1)
                                + "." + targetFormat;
                        ImageFileWriters.writeImageFile(target, targetFormat, fileName);
                        fileNames.add(new File(fileName).getAbsolutePath());
                    }
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        multipleFilesGenerated(fileNames);
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

    @FXML
    private void saveAsPdfAction() {
        final File targetFile = validationBeforeSave(CommonValues.PdfExtensionFilter, null);
        if (targetFile == null) {
            return;
        }
        task = new Task<Void>() {
            private boolean ok;

            @Override
            protected Void call() throws Exception {
                final String sourceFormat = imageInformation.getImageFormat();
                final String sourcefile = sourceFile.getAbsolutePath();
                ok = PdfTools.writeSplitImages(sourceFormat, sourcefile, imageInformation,
                        rows, cols, attributes, targetFile,
                        pdfFormat, fontBox.getSelectionModel().getSelectedItem(), authorInput.getText(),
                        threshold, jpegQuality, isImageSize, pageNumberCheck.isSelected(),
                        pageWidth, pageHeight, marginSize, headerInput.getText());
                if (task.isCancelled()) {
                    return null;
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (ok && targetFile.exists()) {
                                popInformation(AppVaribles.getMessage("Successful"));
                                Desktop.getDesktop().browse(targetFile.toURI());
                            } else {
                                popError(AppVaribles.getMessage("Failed"));
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

    @FXML
    private void saveAsTiffAction(ActionEvent event) {
        final File targetFile = validationBeforeSave(CommonValues.TiffExtensionFilter, null);
        if (targetFile == null) {
            return;
        }
        task = new Task<Void>() {
            private boolean ok;

            @Override
            protected Void call() throws Exception {
                final String sourceFormat = imageInformation.getImageFormat();
                final String filename = sourceFile.getAbsolutePath();
                ok = ImageTiffFile.writeSplitImages(sourceFormat, filename,
                        imageInformation, rows, cols, attributes, targetFile);
                if (task.isCancelled()) {
                    return null;
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (ok && targetFile.exists()) {
                                popInformation(AppVaribles.getMessage("Successful"));
                                final ImageFramesViewerController controller
                                        = (ImageFramesViewerController) openStage(CommonValues.ImageFramesViewerFxml, false, true);
                                controller.setBaseTitle(AppVaribles.getMessage("ImageFramesViewer"));
                                controller.openFile(targetFile);
                            } else {
                                popError(AppVaribles.getMessage("Failed"));
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
