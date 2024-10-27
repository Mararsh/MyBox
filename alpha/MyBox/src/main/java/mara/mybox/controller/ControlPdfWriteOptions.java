package mara.mybox.controller;

import java.sql.Connection;
import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.PdfTools.PdfImageFormat;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-12-07
 * @License Apache License Version 2.0
 */
public class ControlPdfWriteOptions extends BaseController {

    protected String author, header, footer, ttfFile;
    protected boolean isImageSize, includeImageOptions, dithering, showPageNumber, landscape;
    protected int marginSize, pageWidth, pageHeight, jpegQuality, threshold, fontSize, zoom;
    protected PdfImageFormat imageFormat;

    @FXML
    protected ComboBox<String> marginSelector, standardSizeSelector, jpegQualitySelector, fontSizeSelector, zoomSelector;
    @FXML
    protected ControlTTFSelector ttfController;
    @FXML
    protected ToggleGroup sizeGroup, imageFormatGroup;
    @FXML
    protected RadioButton pixSizeRadio, standardSizeRadio, customSizeRadio,
            pngRadio, jpgRadio, bwRadio;
    @FXML
    protected TextField authorInput, headerInput, footerInput,
            customWidthInput, customHeightInput, thresholdInput;
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

    public ControlPdfWriteOptions set(String baseName, boolean imageOptions) {
        this.baseName = baseName;
        this.includeImageOptions = imageOptions;
        ttfController.name(baseName);
        setControls();
        return this;
    }

    public void setControls() {
        try (Connection conn = DerbyBase.getConnection()) {

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
            standardSizeSelector.setValue(UserConfig.getString(conn, baseName + "PdfStandardSize", "A4 (16k)  21.0cm x 29.7cm"));
            standardSizeSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
                    checkStandardValues();
                }
            });

            pageWidth = UserConfig.getInt(conn, baseName + "PdfCustomWidth", 1024);
            customWidthInput.setText(pageWidth + "");

            pageHeight = UserConfig.getInt(conn, baseName + "PdfCustomHeight", 500);
            customHeightInput.setText(pageHeight + "");

            marginSelector.getItems().addAll(Arrays.asList("20", "10", "15", "5", "25", "30", "40"));
            marginSize = UserConfig.getInt(conn, baseName + "PdfMarginSize", 20);
            marginSelector.setValue(marginSize + "");

            fontSizeSelector.getItems().addAll(Arrays.asList(
                    "20", "14", "18", "15", "9", "10", "12", "17", "24", "36", "48", "64", "72", "96"));
            fontSize = UserConfig.getInt(conn, baseName + "PdfFontSize", 20);
            fontSizeSelector.getSelectionModel().select(fontSize + "");

            zoomSelector.getItems().addAll(Arrays.asList("60", "100", "75", "50", "125", "30", "45", "200"));
            zoom = UserConfig.getInt(conn, baseName + "PdfZoom", 60);
            zoomSelector.getSelectionModel().select(zoom + "");

            author = UserConfig.getString(conn, baseName + "PdfAuthor", System.getProperty("user.name"));
            authorInput.setText(author);

            header = UserConfig.getString(conn, baseName + "PdfHeader", "");
            headerInput.setText(header);

            footer = UserConfig.getString(conn, baseName + "PdfFooter", "");
            footerInput.setText(footer);

            showPageNumber = UserConfig.getBoolean(conn, baseName + "PdfShowPageNumber", true);
            pageNumberCheck.setSelected(showPageNumber);

            landscape = UserConfig.getBoolean(conn, baseName + "PdfPageHorizontal", false);
            landscapeCheck.setSelected(landscape);
            landscapeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    checkStandardValues();
                }
            });

            initImageOptions(conn);

            checkPageSize();

        } catch (Exception e) {
            MyBoxLog.error(e, baseName);
        }
    }

    protected void checkPageSize() {
        standardSizeSelector.setDisable(true);
        dpiBox.setDisable(true);
        landscapeCheck.setDisable(true);
        isImageSize = false;

        if (pixSizeRadio.isSelected()) {
            isImageSize = true;
        } else if (standardSizeRadio.isSelected()) {
            standardSizeSelector.setDisable(false);
            dpiBox.setDisable(false);
            landscapeCheck.setDisable(false);
            checkStandardValues();

        } else if (customSizeRadio.isSelected()) {
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

    protected void initImageOptions(Connection conn) {
        try {
            if (!includeImageOptions) {
                thisPane.getChildren().removeAll(pixSizeRadio, imageOptionsBox);
                standardSizeRadio.setSelected(true);
                return;
            }

            jpegQualitySelector.getItems().addAll(Arrays.asList("100", "75", "90", "50", "60", "80", "30", "10"));
            jpegQuality = UserConfig.getInt(conn, baseName + "PdfJpegQuality", 100);
            jpegQualitySelector.setValue(jpegQuality + "");

            threshold = UserConfig.getInt(conn, baseName + "PdfThreshold", -1);
            thresholdInput.setText(threshold + "");

            dithering = UserConfig.getBoolean(conn, baseName + "PdfImageDithering", true);
            ditherCheck.setSelected(dithering);

        } catch (Exception e) {
            MyBoxLog.error(e, baseName);
        }
    }

    public boolean pickValues() {
        if (customSizeRadio.isSelected()) {
            try {
                pageWidth = Integer.parseInt(customWidthInput.getText());
            } catch (Exception e) {
                pageWidth = -1;
            }
            try {
                pageHeight = Integer.parseInt(customHeightInput.getText());
            } catch (Exception e) {
                pageHeight = -1;
            }
        } else if (standardSizeRadio.isSelected()) {
            try {
                dpi = Integer.parseInt(dpiSelector.getValue());
            } catch (Exception e) {
                dpi = -1;
            }
            if (dpi <= 0) {
                popError(message("InvalidParameter") + ": " + "DPI");
                return false;
            }
        }
        if (!isImageSize) {
            if (pageWidth <= 0) {
                popError(message("InvalidParameter") + ": " + message("Width"));
                return false;
            }
            if (pageHeight <= 0) {
                popError(message("InvalidParameter") + ": " + message("Height"));
                return false;
            }
        }
        try {
            fontSize = Integer.parseInt(fontSizeSelector.getValue());
        } catch (Exception e) {
            fontSize = -1;
        }
        if (fontSize <= 0) {
            popError(message("InvalidParameter") + ": " + message("FontSize"));
            return false;
        }
        try {
            zoom = Integer.parseInt(zoomSelector.getValue());
        } catch (Exception e) {
            zoom = -1;
        }
        if (zoom <= 0) {
            popError(message("InvalidParameter") + ": " + message("DefaultDisplayScale"));
            return false;
        }
        if (includeImageOptions) {
            if (bwRadio.isSelected()) {
                try {
                    threshold = Integer.parseInt(thresholdInput.getText());
                } catch (Exception e) {
                    threshold = -1;
                }
                if (threshold < 0 || threshold > 255) {
                    popError(message("InvalidParameter") + ": " + message("CCITT4"));
                    return false;
                }
                imageFormat = PdfImageFormat.Tiff;
            } else if (jpgRadio.isSelected()) {
                try {
                    jpegQuality = Integer.parseInt(thresholdInput.getText());
                } catch (Exception e) {
                    jpegQuality = -1;
                }
                if (jpegQuality < 0 || jpegQuality > 100) {
                    popError(message("InvalidParameter") + ": " + message("JpegQuailty"));
                    return false;
                }
                imageFormat = PdfImageFormat.Jpeg;
            } else {
                imageFormat = PdfImageFormat.Original;
            }
        }
        try {
            marginSize = Integer.parseInt(marginSelector.getValue());
        } catch (Exception e) {
            marginSize = 0;
        }
        showPageNumber = pageNumberCheck.isSelected();
        landscape = landscapeCheck.isSelected();
        dithering = ditherCheck.isSelected();
        ttfFile = ttfController.ttfFile;
        author = authorInput.getText();
        header = headerInput.getText();
        footer = footerInput.getText();
        try (Connection conn = DerbyBase.getConnection()) {
            UserConfig.setInt(conn, baseName + "PdfZoom", zoom);
            UserConfig.setInt(conn, baseName + "PdfFontSize", fontSize);
            UserConfig.setInt(conn, baseName + "PdfMarginSize", marginSize);
            UserConfig.setInt(conn, baseName + "PdfCustomWidth", pageWidth);
            UserConfig.setInt(conn, baseName + "PdfCustomHeight", pageHeight);
            UserConfig.setInt(conn, baseName + "PdfThreshold", threshold);
            UserConfig.setInt(conn, baseName + "PdfJpegQuality", jpegQuality);
            UserConfig.setString(conn, baseName + "PdfHeader", header);
            UserConfig.setString(conn, baseName + "PdfAuthor", author);
            UserConfig.setString(conn, baseName + "PdfFooter", footer);
            UserConfig.setString(conn, baseName + "PdfStandardSize", standardSizeSelector.getValue());
            UserConfig.setBoolean(conn, baseName + "PdfShowPageNumber", showPageNumber);
            UserConfig.setBoolean(conn, baseName + "PdfPageHorizontal", landscape);
            UserConfig.setBoolean(conn, baseName + "PdfImageDithering", dithering);
        } catch (Exception e) {
        }
        return true;
    }

    /*
        get
     */
    public String getAuthor() {
        return author;
    }

    public String getHeader() {
        return header;
    }

    public String getFooter() {
        return footer;
    }

    public String getTtfFile() {
        return ttfFile;
    }

    public boolean isIsImageSize() {
        return isImageSize;
    }

    public boolean isDithering() {
        return dithering;
    }

    public boolean isShowPageNumber() {
        return showPageNumber;
    }

    public boolean isLandscape() {
        return landscape;
    }

    public int getMarginSize() {
        return marginSize;
    }

    public int getPageWidth() {
        return pageWidth;
    }

    public int getPageHeight() {
        return pageHeight;
    }

    public int getJpegQuality() {
        return jpegQuality;
    }

    public int getThreshold() {
        return threshold;
    }

    public int getFontSize() {
        return fontSize;
    }

    public int getZoom() {
        return zoom;
    }

    public PdfImageFormat getImageFormat() {
        return imageFormat;
    }

}
