package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.PdfTools.PdfImageFormat;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.getUserConfigValue;

/**
 * @Author Mara
 * @CreateDate 2020-12-07
 * @License Apache License Version 2.0
 */
public class ControlPdfWriteOptions extends BaseController {

    protected String ttfFile, author, header;
    protected boolean isImageSize, includeImageOptions, dithering, showPageNumber;
    protected int marginSize, pageWidth, pageHeight, jpegQuality, threshold, fontSize, zoom;
    protected PdfImageFormat imageFormat;

    @FXML
    protected ComboBox<String> marginSelector, standardSizeSelector, jpegQualitySelector, fontSizeSelector, zoomSelector;
    @FXML
    protected ControlTTFSelecter ttfController;
    @FXML
    protected ToggleGroup sizeGroup, imageFormatGroup;
    @FXML
    protected RadioButton pixSizeRadio, standardSizeRadio, customSizeRadio,
            pdfMem500MRadio, pdfMem1GRadio, pdfMem2GRadio, pdfMemUnlimitRadio;
    @FXML
    protected TextField authorInput, headerInput, customWidthInput, customHeightInput, thresholdInput;
    @FXML
    protected CheckBox pageNumberCheck, ditherCheck, landscapeCheck;
    @FXML
    protected VBox imageOptionsBox;
    @FXML
    protected HBox dpiBox;

    public ControlPdfWriteOptions() {
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.TTF);
    }

    public static ControlPdfWriteOptions create() {
        return new ControlPdfWriteOptions();
    }

    public ControlPdfWriteOptions set(String baseName, boolean includeImageOptions) {
        this.baseName = baseName;
        this.includeImageOptions = includeImageOptions;
        try {
            sizeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                    checkPageSize();
                }
            });

            standardSizeSelector.getItems().addAll(Arrays.asList(
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
            standardSizeSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
                    AppVariables.setUserConfigValue(baseName + "PdfStandardSize", newValue);
                    checkStandardValues();
                }
            });
            standardSizeSelector.setValue(AppVariables.getUserConfigValue(baseName + "PdfStandardSize", "A4 (16k)  21.0cm x 29.7cm"));

            dpiSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
                    checkStandardValues();
                }
            });

            customWidthInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    AppVariables.setUserConfigValue(baseName + "PdfCustomWidth", newValue);
                    checkCustomValues();
                }
            });
            customWidthInput.setText(AppVariables.getUserConfigValue(baseName + "PdfCustomWidth", ""));
            customHeightInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    AppVariables.setUserConfigValue(baseName + "PdfCustomHeight", newValue);
                    checkCustomValues();
                }
            });
            customHeightInput.setText(AppVariables.getUserConfigValue(baseName + "PdfCustomHeight", ""));

            marginSelector.getItems().addAll(Arrays.asList("20", "10", "15", "5", "25", "30", "40"));
            marginSize = AppVariables.getUserConfigInt(baseName + "PdfMarginSize", 20);
            marginSelector.setValue(marginSize + "");
            marginSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v >= 0) {
                            marginSize = v;
                            FxmlControl.setEditorNormal(marginSelector);
                            AppVariables.setUserConfigInt(baseName + "PdfMarginSize", v);
                        } else {
                            FxmlControl.setEditorBadStyle(marginSelector);
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(marginSelector);
                    }
                }
            });

            ttfController.name(baseName);

            fontSizeSelector.getItems().addAll(Arrays.asList(
                    "20", "14", "18", "15", "9", "10", "12", "17", "24", "36", "48", "64", "72", "96"));
            fontSize = AppVariables.getUserConfigInt(baseName + "PdfFontSize", 20);
            fontSizeSelector.getSelectionModel().select(fontSize + "");
            fontSizeSelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            fontSize = v;
                            AppVariables.setUserConfigInt(baseName + "PdfFontSize", v);
                            FxmlControl.setEditorNormal(fontSizeSelector);
                        } else {
                            FxmlControl.setEditorBadStyle(fontSizeSelector);
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(fontSizeSelector);
                    }
                }
            });

            zoomSelector.getItems().addAll(Arrays.asList("60", "100", "75", "50", "125", "30", "45", "200"));
            zoom = AppVariables.getUserConfigInt(baseName + "PdfZoom", 60);
            zoomSelector.getSelectionModel().select(zoom + "");
            zoomSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov,
                        String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            zoom = v;
                            FxmlControl.setEditorNormal(zoomSelector);
                            AppVariables.setUserConfigInt(baseName + "PdfZoom", v);
                        } else {
                            FxmlControl.setEditorBadStyle(zoomSelector);
                        }

                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(zoomSelector);
                    }
                }
            });

            authorInput.setText(AppVariables.getUserConfigValue(baseName + "PdfAuthor", System.getProperty("user.name")));
            authorInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    AppVariables.setUserConfigValue(baseName + "PdfAuthor", newValue);
                }
            });

            headerInput.setText(AppVariables.getUserConfigValue(baseName + "PdfHeader", ""));
            headerInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    AppVariables.setUserConfigValue(baseName + "PdfHeader", newValue);
                }
            });

            showPageNumber = AppVariables.getUserConfigBoolean(baseName + "PdfShowPageNumber", true);
            pageNumberCheck.setSelected(showPageNumber);
            pageNumberCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    showPageNumber = newValue;
                    AppVariables.setUserConfigValue(baseName + "PdfShowPageNumber", newValue);
                }
            });

            landscapeCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "PdfPageHorizontal", false));
            landscapeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue(baseName + "PdfPageHorizontal", newValue);
                    checkStandardValues();
                }
            });

            String pm = getUserConfigValue("PdfMemDefault", "1GB");
            isSettingValues = true;
            switch (pm) {
                case "1GB":
                    pdfMem1GRadio.setSelected(true);
                    break;
                case "2GB":
                    pdfMem2GRadio.setSelected(true);
                    break;
                case "Unlimit":
                    pdfMemUnlimitRadio.setSelected(true);
                    break;
                case "500MB":
                default:
                    pdfMem500MRadio.setSelected(true);
            }
            isSettingValues = false;

            initImageOptions();

            checkPageSize();

        } catch (Exception e) {
            MyBoxLog.error(e, baseName);
        }
        return this;
    }

    protected void checkPageSize() {
        standardSizeSelector.setDisable(true);
        dpiBox.setDisable(true);
        landscapeCheck.setDisable(true);
        customWidthInput.setDisable(true);
        customHeightInput.setDisable(true);
        customWidthInput.setStyle(null);
        customHeightInput.setStyle(null);
        isImageSize = false;

        RadioButton selected = (RadioButton) sizeGroup.getSelectedToggle();
        if (AppVariables.message("ImagesSize").equals(selected.getText())) {
            isImageSize = true;
        } else if (AppVariables.message("StandardSize").equals(selected.getText())) {
            standardSizeSelector.setDisable(false);
            dpiBox.setDisable(false);
            landscapeCheck.setDisable(false);
            checkStandardValues();

        } else if (AppVariables.message("Custom").equals(selected.getText())) {
            customWidthInput.setDisable(false);
            customHeightInput.setDisable(false);
            checkCustomValues();
        }
    }

    protected void checkStandardValues() {
        String s = standardSizeSelector.getSelectionModel().getSelectedItem();
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
        if (landscapeCheck.isSelected()) {
            int tmp = pageWidth;
            pageWidth = pageHeight;
            pageHeight = tmp;
        }
        customWidthInput.setText(pageWidth + "");
        customHeightInput.setText(pageHeight + "");
    }

    protected int calculateCmPixels(float cm, int dpi) {
        return (int) Math.round(cm * dpi / 2.54);
    }

    protected void checkCustomValues() {
        RadioButton selected = (RadioButton) sizeGroup.getSelectedToggle();
        if (!AppVariables.message("Custom").equals(selected.getText())) {
            return;
        }
        try {
            int v = Integer.valueOf(customWidthInput.getText());
            if (v > 0) {
                pageWidth = v;
                customWidthInput.setStyle(null);
            } else {
                customWidthInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            customWidthInput.setStyle(badStyle);
        }
        try {
            int v = Integer.valueOf(customHeightInput.getText());
            if (pageHeight > 0) {
                pageHeight = v;
                customHeightInput.setStyle(null);
            } else {
                customHeightInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            customHeightInput.setStyle(badStyle);
        }
    }

    protected void initImageOptions() {
        try {
            if (!includeImageOptions) {
                thisPane.getChildren().removeAll(pixSizeRadio, imageOptionsBox);
                standardSizeRadio.fire();
                return;
            }

            imageFormatGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                    checkImageFormat();
                }
            });

            jpegQualitySelector.getItems().addAll(Arrays.asList("100", "75", "90", "50", "60", "80", "30", "10"));
            jpegQuality = AppVariables.getUserConfigInt(baseName + "PdfJpegQuality", 100);
            jpegQualitySelector.setValue(jpegQuality + "");
            jpegQualitySelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
                    checkJpegQuality();
                }
            });

            threshold = AppVariables.getUserConfigInt(baseName + "PdfThreshold", -1);
            if (threshold > 0 && threshold < 100) {
                thresholdInput.setText(threshold + "");
            }
            thresholdInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkThreshold();
                    AppVariables.setUserConfigValue(baseName + "PdfThreshold", newValue);
                }
            });

            dithering = AppVariables.getUserConfigBoolean(baseName + "PdfImageDithering", true);
            ditherCheck.setSelected(dithering);
            ditherCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    dithering = newValue;
                    AppVariables.setUserConfigValue(baseName + "PdfImageDithering", newValue);
                }
            });

            checkImageFormat();

        } catch (Exception e) {
            MyBoxLog.error(e, baseName);
        }
    }

    protected void checkJpegQuality() {
        try {
            int v = Integer.valueOf(jpegQualitySelector.getSelectionModel().getSelectedItem());
            if (v >= 0 && v <= 100) {
                jpegQuality = v;
                jpegQualitySelector.setStyle(null);
                AppVariables.setUserConfigInt(baseName + "PdfJpegQuality", jpegQuality);
            } else {
                jpegQualitySelector.setStyle(badStyle);
            }
        } catch (Exception e) {
            jpegQualitySelector.setStyle(badStyle);
        }
    }

    protected void checkImageFormat() {
        jpegQualitySelector.setDisable(true);
        jpegQualitySelector.setStyle(null);
        thresholdInput.setDisable(true);

        RadioButton selected = (RadioButton) imageFormatGroup.getSelectedToggle();
        if (AppVariables.message("PNG").equals(selected.getText())) {
            imageFormat = PdfImageFormat.Original;
        } else if (AppVariables.message("CCITT4").equals(selected.getText())) {
            imageFormat = PdfImageFormat.Tiff;
            thresholdInput.setDisable(false);
            checkThreshold();
        } else if (AppVariables.message("JpegQuailty").equals(selected.getText())) {
            imageFormat = PdfImageFormat.Jpeg;
            jpegQualitySelector.setDisable(false);
            checkJpegQuality();
        }
        dithering = ditherCheck.isSelected();
    }

    protected void checkThreshold() {
        try {
            threshold = -1;
            if (thresholdInput.getText().isEmpty()) {
                thresholdInput.setStyle(null);
                return;
            }
            int v = Integer.valueOf(thresholdInput.getText());
            if (v >= 0 && v <= 255) {
                threshold = v;
                thresholdInput.setStyle(null);
            } else {
                thresholdInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            thresholdInput.setStyle(badStyle);
        }
    }

    @FXML
    protected void PdfMem500MB(ActionEvent event) {
        if (isSettingValues) {
            return;
        }
        AppVariables.setPdfMem("500MB");
    }

    @FXML
    protected void PdfMem1GB(ActionEvent event) {
        if (isSettingValues) {
            return;
        }
        AppVariables.setPdfMem("1GB");
    }

    @FXML
    protected void PdfMem2GB(ActionEvent event) {
        if (isSettingValues) {
            return;
        }
        AppVariables.setPdfMem("2GB");
    }

    @FXML
    protected void pdfMemUnlimit(ActionEvent event) {
        if (isSettingValues) {
            return;
        }
        AppVariables.setPdfMem("Unlimit");
    }


    /*
        get/set
     */
    public String getTtfFile() {
        if (ttfController != null) {
            ttfFile = ttfController.ttfFile;
        }
        return ttfFile;
    }

    public void setTtfFile(String ttfFile) {
        this.ttfFile = ttfFile;
    }

    public boolean isIsImageSize() {
        return isImageSize;
    }

    public void setIsImageSize(boolean isImageSize) {
        this.isImageSize = isImageSize;
    }

    public boolean isIncludeImageOptions() {
        return includeImageOptions;
    }

    public void setIncludeImageOptions(boolean includeImageOptions) {
        this.includeImageOptions = includeImageOptions;
    }

    public int getMarginSize() {
        return marginSize;
    }

    public void setMarginSize(int marginSize) {
        this.marginSize = marginSize;
    }

    public int getPageWidth() {
        return pageWidth;
    }

    public void setPageWidth(int pageWidth) {
        this.pageWidth = pageWidth;
    }

    public int getPageHeight() {
        return pageHeight;
    }

    public void setPageHeight(int pageHeight) {
        this.pageHeight = pageHeight;
    }

    public int getJpegQuality() {
        return jpegQuality;
    }

    public void setJpegQuality(int jpegQuality) {
        this.jpegQuality = jpegQuality;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public PdfImageFormat getImageFormat() {
        return imageFormat;
    }

    public void setImageFormat(PdfImageFormat imageFormat) {
        this.imageFormat = imageFormat;
    }

    public ComboBox<String> getMarginSelector() {
        return marginSelector;
    }

    public void setMarginSelector(ComboBox<String> marginSelector) {
        this.marginSelector = marginSelector;
    }

    public ComboBox<String> getStandardSizeSelector() {
        return standardSizeSelector;
    }

    public void setStandardSizeSelector(ComboBox<String> standardSizeSelector) {
        this.standardSizeSelector = standardSizeSelector;
    }

    public ComboBox<String> getJpegQualitySelector() {
        return jpegQualitySelector;
    }

    public void setJpegQualitySelector(ComboBox<String> jpegQualitySelector) {
        this.jpegQualitySelector = jpegQualitySelector;
    }

    public ComboBox<String> getFontSizeSelector() {
        return fontSizeSelector;
    }

    public void setFontSizeSelector(ComboBox<String> fontSizeSelector) {
        this.fontSizeSelector = fontSizeSelector;
    }

    public ToggleGroup getSizeGroup() {
        return sizeGroup;
    }

    public void setSizeGroup(ToggleGroup sizeGroup) {
        this.sizeGroup = sizeGroup;
    }

    public ToggleGroup getImageFormatGroup() {
        return imageFormatGroup;
    }

    public void setImageFormatGroup(ToggleGroup imageFormatGroup) {
        this.imageFormatGroup = imageFormatGroup;
    }

    public RadioButton getPixSizeRadio() {
        return pixSizeRadio;
    }

    public void setPixSizeRadio(RadioButton pixSizeRadio) {
        this.pixSizeRadio = pixSizeRadio;
    }

    public RadioButton getStandardSizeRadio() {
        return standardSizeRadio;
    }

    public void setStandardSizeRadio(RadioButton standardSizeRadio) {
        this.standardSizeRadio = standardSizeRadio;
    }

    public RadioButton getCustomSizeRadio() {
        return customSizeRadio;
    }

    public void setCustomSizeRadio(RadioButton customSizeRadio) {
        this.customSizeRadio = customSizeRadio;
    }

    public TextField getAuthorInput() {
        return authorInput;
    }

    public void setAuthorInput(TextField authorInput) {
        this.authorInput = authorInput;
    }

    public TextField getHeaderInput() {
        return headerInput;
    }

    public void setHeaderInput(TextField headerInput) {
        this.headerInput = headerInput;
    }

    public TextField getCustomWidthInput() {
        return customWidthInput;
    }

    public void setCustomWidthInput(TextField customWidthInput) {
        this.customWidthInput = customWidthInput;
    }

    public TextField getCustomHeightInput() {
        return customHeightInput;
    }

    public void setCustomHeightInput(TextField customHeightInput) {
        this.customHeightInput = customHeightInput;
    }

    public TextField getThresholdInput() {
        return thresholdInput;
    }

    public void setThresholdInput(TextField thresholdInput) {
        this.thresholdInput = thresholdInput;
    }

    public CheckBox getPageNumberCheck() {
        return pageNumberCheck;
    }

    public void setPageNumberCheck(CheckBox pageNumberCheck) {
        this.pageNumberCheck = pageNumberCheck;
    }

    public CheckBox getDitherCheck() {
        return ditherCheck;
    }

    public void setDitherCheck(CheckBox ditherCheck) {
        this.ditherCheck = ditherCheck;
    }

    public CheckBox getLandscapeCheck() {
        return landscapeCheck;
    }

    public void setLandscapeCheck(CheckBox landscapeCheck) {
        this.landscapeCheck = landscapeCheck;
    }

    public VBox getImageOptionsBox() {
        return imageOptionsBox;
    }

    public void setImageOptionsBox(VBox imageOptionsBox) {
        this.imageOptionsBox = imageOptionsBox;
    }

    public boolean isDithering() {
        return dithering;
    }

    public void setDithering(boolean dithering) {
        this.dithering = dithering;
    }

    public boolean isShowPageNumber() {
        return showPageNumber;
    }

    public void setShowPageNumber(boolean showPageNumber) {
        this.showPageNumber = showPageNumber;
    }

    public String getAuthor() {
        if (authorInput != null) {
            author = authorInput.getText();
        }
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getHeader() {
        if (headerInput != null) {
            header = headerInput.getText();
        }
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public int getZoom() {
        return zoom;
    }

    public void setZoom(int zoom) {
        this.zoom = zoom;
    }

    public ComboBox<String> getZoomSelector() {
        return zoomSelector;
    }

    public void setZoomSelector(ComboBox<String> zoomSelector) {
        this.zoomSelector = zoomSelector;
    }

    public ControlTTFSelecter getTtfController() {
        return ttfController;
    }

    public void setTtfController(ControlTTFSelecter ttfController) {
        this.ttfController = ttfController;
    }

}
