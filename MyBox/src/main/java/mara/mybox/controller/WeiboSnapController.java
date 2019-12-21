package mara.mybox.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import mara.mybox.data.WeiboSnapParameters;
import mara.mybox.db.TableStringValues;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.NetworkTools;
import mara.mybox.tools.PdfTools.PdfImageFormat;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.getUserConfigValue;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-9-13
 * @Description
 * @License Apache License Version 2.0
 */
public class WeiboSnapController extends BaseController {

    private final String WeiboLoadSpeedKey, WeiboScrollDelayKey, WeiboZoomKey;
    private final String WeiboLastAddressKey, WeiboRetryKey, WeiboOpenPathKey, WeiboColseWindowKey;
    private final String WeiboExpandPicturesKey, WeiboExpandCommentsKey, WeiboUseTempKey;
    private final String WeiboPdfKey, WeiboHtmlKey, WeiboPixKey, WeiboKeepPageKey, WeiboMiaoKey;
    final private String AuthorKey;
    private int accessInterval, webWidth, categoryType, retry, startPage, snapInterval;
    private boolean isImageSize;
    private String webAddress;
    private WeiboSnapParameters parameters;
    private Date startMonth, endMonth;
    private float zoomScale, speed;
    private int marginSize, pageWidth, pageHeight, jpegQuality, threshold, maxMergeSize, pdfScale;
    private PdfImageFormat format;
    private List<String> addressList;

    @FXML
    private ToggleGroup sizeGroup, formatGroup, categoryGroup, pdfMemGroup;
    @FXML
    private ComboBox<String> addressBox, zoomBox, widthBox, retryBox,
            MarginsBox, standardSizeBox, standardDpiBox, jpegBox, pdfScaleBox;
    @FXML
    private TextField startMonthInput, endMonthInput, startPageInput, accessIntervalInput,
            snapIntervalInput, customWidthInput, customHeightInput, authorInput, thresholdInput,
            headerInput;
    @FXML
    private Button wowButton, recoverPassportButton;
    @FXML
    private CheckBox pdfCheck, htmlCheck, pixCheck, keepPageCheck, miaoCheck, ditherCheck,
            expandCommentsCheck, expandPicturesCheck, openPathCheck, closeWindowCheck,
            bypassSSLCheck;
    @FXML
    private RadioButton imageSizeRadio, monthsPathsRadio, pngRadio,
            pdfMem500MRadio, pdfMem1GRadio, pdfMem2GRadio, pdfMemUnlimitRadio;

    public WeiboSnapController() {
        baseTitle = AppVariables.message("WeiboSnap");

        targetPathKey = "WeiboTargetPathKey";

        WeiboLoadSpeedKey = "WeiboLoadSpeedKey";
        WeiboScrollDelayKey = "WeiboScrollDelayKey";
        WeiboZoomKey = "WeiboZoomKey";
        WeiboLastAddressKey = "WeiboLastAddressKey";
        WeiboRetryKey = "WeiboRetryKey";
        AuthorKey = "AuthorKey";
        WeiboOpenPathKey = "WeiboOpenPathKey";
        WeiboColseWindowKey = "WeiboColseWindowKey";
        WeiboPdfKey = "WeiboPdfKey";
        WeiboHtmlKey = "WeiboHtmlKey";
        WeiboPixKey = "WeiboPixKey";
        WeiboKeepPageKey = "WeiboKeepPageKey";
        WeiboMiaoKey = "WeiboMiaoKey";
        WeiboExpandPicturesKey = "WeiboExpandPicturesKey";
        WeiboExpandCommentsKey = "WeiboExpandCommentsKey";
        WeiboUseTempKey = "WeiboUseTempKey";
    }

    @Override
    public void initializeNext() {
        try {

            initWebOptions();
            initSnapOptions();
            initPdfOptionsSection();
            initNetworkOptions();
            initTargetOptions();

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    private void initWebOptions() {

        addressList = TableStringValues.max("WeiBoAddress", 20);
        addressBox.getItems().addAll(addressList);
        addressBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov,
                    String oldValue, String newValue) {
                if (newValue == null || newValue.trim().isEmpty()) {
                    return;
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String s = newValue.trim();
                            int pos = s.indexOf("?");
                            if (pos > 0) {
                                s = s.substring(0, pos);
                            }
                            if (s.endsWith("/profile")) {
                                s = s.substring(0, s.length() - "/profile".length());
                            }
                            if (s.endsWith("/")) {
                                s = s.substring(0, s.length() - 1);
                            }
                            if ((!s.startsWith("https://weibo.com/")
                                    && !s.startsWith("http://weibo.com/")
                                    && !s.startsWith("https://www.weibo.com/")
                                    && !s.startsWith("http://www.weibo.com/")
                                    && !s.startsWith("www.weibo.com/")
                                    && !s.startsWith("weibo.com/"))) {
                                addressBox.getEditor().setStyle(badStyle);
                                webAddress = "";
                            } else {
                                webAddress = s;
                                addressBox.getEditor().setStyle(null);
                                AppVariables.setUserConfigValue(WeiboLastAddressKey, s);
                                if (!addressList.contains(s)) {
                                    TableStringValues.add("WeiBoAddress", s);
                                    addressList = TableStringValues.max("WeiBoAddress", 20);
                                }

                            }
                        } catch (Exception e) {
                            addressBox.getEditor().setStyle(badStyle);
                            webAddress = "";
                        }
                    }
                });

            }
        });
        addressBox.getSelectionModel().select(0);

        FxmlControl.setTooltip(startMonthInput, new Tooltip(AppVariables.message("WeiboEarlestMonth")));
        startMonthInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                checkTimes();
            }
        });
        startMonthInput.setText(AppVariables.getUserConfigValue("WeiboLastStartMonthKey", ""));

        startPage = 1;
        startPageInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                try {
                    int v = Integer.valueOf(startPageInput.getText());
                    if (v >= 1) {
                        startPageInput.setStyle(null);
                        startPage = v;
                    } else {
                        startPageInput.setStyle(badStyle);
                    }
                } catch (Exception e) {
                    startPageInput.setStyle(badStyle);
                }
            }
        });
        startPageInput.setText("1");

        endMonthInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                checkTimes();
            }
        });

        expandCommentsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov,
                    Boolean oldValue, Boolean newValue) {
                AppVariables.setUserConfigValue(WeiboExpandCommentsKey, newValue);
            }
        });
        expandCommentsCheck.setSelected(AppVariables.getUserConfigBoolean(WeiboExpandCommentsKey));

        expandPicturesCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov,
                    Boolean oldValue, Boolean newValue) {
                AppVariables.setUserConfigValue(WeiboExpandPicturesKey, newValue);
            }
        });
        expandPicturesCheck.setSelected(AppVariables.getUserConfigBoolean(WeiboExpandPicturesKey));

        bypassSSLCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldv, Boolean newv) {
                AppVariables.setUserConfigValue("SSLBypassAll", newv);
                if (newv) {
                    NetworkTools.trustAll();
                } else {
                    NetworkTools.myBoxSSL();
                }
            }
        });
        bypassSSLCheck.setSelected(AppVariables.getUserConfigBoolean("SSLBypassAll", false));
        if (bypassSSLCheck.isSelected()) {
            NetworkTools.trustAll();
        } else {
            NetworkTools.myBoxSSL();
        }

    }

    private void checkTimes() {
        Date weiboStart = DateTools.parseMonth("2009-08");
        Date thisMonth = DateTools.thisMonth();
        try {
            String start = startMonthInput.getText();
            if (start == null || start.isEmpty()) {
                startMonth = weiboStart;
                startMonthInput.setStyle(null);
            } else {
                startMonth = DateTools.parseMonth(start);
                if (startMonth.getTime() > thisMonth.getTime()) {
                    startMonthInput.setStyle(badStyle);
                    return;
                } else if (startMonth.getTime() < DateTools.parseMonth("2009-08").getTime()) {
//                    startInput.setText("2009-08");
                    startMonthInput.setStyle(badStyle);
                    return;
                } else {
                    startMonthInput.setStyle(null);
                }
            }
        } catch (Exception e) {
            startMonthInput.setStyle(badStyle);
            return;
        }

        try {
            String end = endMonthInput.getText();
            if (end == null || end.isEmpty()) {
                endMonth = thisMonth;
                endMonthInput.setStyle(null);
            } else {
                endMonth = DateTools.parseMonth(end);
                if (endMonth.getTime() > thisMonth.getTime()) {
                    endMonth = thisMonth;
                }
                endMonthInput.setStyle(null);
            }
        } catch (Exception e) {
            endMonthInput.setStyle(badStyle);
            return;
        }

        if (startMonth.getTime() > endMonth.getTime()) {
            startMonthInput.setStyle(badStyle);
            endMonthInput.setStyle(badStyle);
        }

    }

    private void initSnapOptions() {
        zoomBox.getItems().addAll(Arrays.asList("1.0", "1.5", "2", "1.6", "1.8", "0.8"));
        zoomBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov,
                    String oldValue, String newValue) {
                try {
                    zoomScale = Float.valueOf(newValue);
                    if (zoomScale > 0) {
                        AppVariables.setUserConfigValue(WeiboZoomKey, zoomScale + "");
                        if (zoomScale > 2) {
                            popInformation(AppVariables.message("TooLargerScale"));
                        }
                        FxmlControl.setEditorNormal(zoomBox);
                    } else {
                        zoomScale = 1.0f;
                        FxmlControl.setEditorBadStyle(zoomBox);
                    }

                } catch (Exception e) {
                    zoomScale = 1.0f;
                    FxmlControl.setEditorBadStyle(zoomBox);
                }
            }
        });
        zoomBox.getSelectionModel().select(0);

        widthBox.getItems().addAll(Arrays.asList("700", "900", "1000", "800", "1200", "1400", "1800",
                AppVariables.message("ScreenWidth")));
        widthBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov,
                    String oldValue, String newValue) {
                try {
                    if (newValue.equals(AppVariables.message("ScreenWidth"))) {
                        webWidth = -1;
                        FxmlControl.setEditorNormal(widthBox);
                        return;
                    }
                    webWidth = Integer.valueOf(newValue);
                    if (webWidth > 0) {
                        FxmlControl.setEditorNormal(widthBox);
                    } else {
                        webWidth = 700;
                        FxmlControl.setEditorBadStyle(widthBox);
                    }

                } catch (Exception e) {
                    webWidth = 700;
                    FxmlControl.setEditorBadStyle(widthBox);
                }
            }
        });
        widthBox.getSelectionModel().select(0);

        formatGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkFormat();
            }
        });
        checkFormat();

        jpegBox.getItems().addAll(Arrays.asList("100", "75", "90", "50", "60", "80", "30", "10"));
        jpegBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
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

        snapInterval = 2000;
        snapIntervalInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                try {
                    int v = Integer.parseInt(newValue);
                    if (v > 0) {
                        snapIntervalInput.setStyle(null);
                        snapInterval = v;
                        AppVariables.setUserConfigInt("WeiBoSnapInterval", v);
                    } else {
                        snapIntervalInput.setStyle(badStyle);
                    }
                } catch (Exception e) {
                    snapIntervalInput.setStyle(badStyle);
                }
            }
        });
        snapIntervalInput.setText(AppVariables.getUserConfigInt("WeiBoSnapInterval", 2000) + "");

    }

    private void checkFormat() {
        jpegBox.setDisable(true);
        jpegBox.setStyle(null);
        thresholdInput.setDisable(true);
        ditherCheck.setDisable(true);

        RadioButton selected = (RadioButton) formatGroup.getSelectedToggle();
        if (AppVariables.message("PNG").equals(selected.getText())) {
            format = PdfImageFormat.Original;
        } else if (AppVariables.message("CCITT4").equals(selected.getText())) {
            format = PdfImageFormat.Tiff;
            thresholdInput.setDisable(false);
            ditherCheck.setDisable(false);
        } else if (AppVariables.message("JpegQuailty").equals(selected.getText())) {
            format = PdfImageFormat.Jpeg;
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

    protected void initPdfOptionsSection() {

        sizeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkPageSize();
            }
        });
        checkPageSize();

        standardSizeBox.getItems().addAll(Arrays.asList("A4-" + message("Horizontal") + " (16k)  29.7cm x 21.0cm",
                "A4 (16k) 21.0cm x 29.7cm",
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
        standardSizeBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
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
        standardDpiBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
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

        MarginsBox.getItems().addAll(Arrays.asList("20", "10", "15", "5", "25", "30"));
        MarginsBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov,
                    String oldValue, String newValue) {
                try {
                    marginSize = Integer.valueOf(newValue);
                    if (marginSize >= 0) {
                        FxmlControl.setEditorNormal(MarginsBox);
                    } else {
                        marginSize = 0;
                        FxmlControl.setEditorBadStyle(MarginsBox);
                    }

                } catch (Exception e) {
                    marginSize = 0;
                    FxmlControl.setEditorBadStyle(MarginsBox);
                }
            }
        });
        MarginsBox.getSelectionModel().select(0);

        pdfScaleBox.getItems().addAll(Arrays.asList("60", "100", "75", "50", "125", "30"));
        pdfScaleBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov,
                    String oldValue, String newValue) {
                try {
                    pdfScale = Integer.valueOf(newValue);
                    if (pdfScale >= 0) {
                        FxmlControl.setEditorNormal(pdfScaleBox);
                    } else {
                        pdfScale = 60;
                        FxmlControl.setEditorBadStyle(pdfScaleBox);
                    }

                } catch (Exception e) {
                    pdfScale = 60;
                    FxmlControl.setEditorBadStyle(pdfScaleBox);
                }
            }
        });
        pdfScaleBox.getSelectionModel().select(0);

        authorInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                AppVariables.setUserConfigValue(AuthorKey, newValue);
            }
        });
        authorInput.setText(AppVariables.getUserConfigValue(AuthorKey, System.getProperty("user.name")));

        checkPdfMem();

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
        if (AppVariables.message("ImagesSize").equals(selected.getText())) {
            isImageSize = true;
        } else if (AppVariables.message("StandardSize").equals(selected.getText())) {
            standardSizeBox.setDisable(false);
            standardDpiBox.setDisable(false);
            checkStandardValues();

        } else if (AppVariables.message("Custom").equals(selected.getText())) {
            customWidthInput.setDisable(false);
            customHeightInput.setDisable(false);
            checkCustomValues();
        }

//        AppVariables.setUserConfigValue(ImageCombineSizeKey, selected.getText());
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
        if (s.startsWith("A4-" + message("Horizontal"))) {
            pageWidth = calculateCmPixels(29.7f, dpi);
            pageHeight = calculateCmPixels(21.0f, dpi);
        } else {
            s = s.substring(0, s.indexOf(" "));
            switch (s) {
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
        if (!AppVariables.message("Custom").equals(selected.getText())) {
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

    private void initNetworkOptions() {

        retryBox.getItems().addAll(Arrays.asList("3", "0", "1", "5", "7", "10"));
        retryBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov,
                    String oldValue, String newValue) {
                try {
                    retry = Integer.valueOf(newValue);
                    if (retry > 0) {
                        AppVariables.setUserConfigValue(WeiboRetryKey, retry + "");
                    } else {
                        retry = 3;
                    }
                } catch (Exception e) {
                    retry = 3;
                }
            }
        });
        retryBox.getSelectionModel().select(AppVariables.getUserConfigValue(WeiboRetryKey, "3"));

        accessInterval = 2000;
        accessIntervalInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                try {
                    int v = Integer.parseInt(newValue);
                    if (v > 0) {
                        accessIntervalInput.setStyle(null);
                        accessInterval = v;
                        AppVariables.setUserConfigInt("WeiBoAccessInterval", v);
                    } else {
                        accessIntervalInput.setStyle(badStyle);
                    }
                } catch (Exception e) {
                    accessIntervalInput.setStyle(badStyle);
                }
            }
        });
        accessIntervalInput.setText(AppVariables.getUserConfigInt("WeiBoAccessInterval", 2000) + "");

    }

    private void checkTargetFiles() {
        if (!pdfCheck.isSelected() && !htmlCheck.isSelected() && !pixCheck.isSelected()) {
            popError(AppVariables.message("NothingSave"));
            pdfCheck.setStyle(badStyle);
        } else {
            pdfCheck.setStyle(null);
        }
    }

    private void initTargetOptions() {

        pdfCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov,
                    Boolean oldValue, Boolean newValue) {
                AppVariables.setUserConfigValue(WeiboPdfKey, newValue);
                checkTargetFiles();
            }
        });
        pdfCheck.setSelected(AppVariables.getUserConfigBoolean(WeiboPdfKey));

        htmlCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov,
                    Boolean oldValue, Boolean newValue) {
                AppVariables.setUserConfigValue(WeiboHtmlKey, newValue);
                checkTargetFiles();
            }
        });
        htmlCheck.setSelected(AppVariables.getUserConfigBoolean(WeiboHtmlKey));

        pixCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov,
                    Boolean oldValue, Boolean newValue) {
                AppVariables.setUserConfigValue(WeiboPixKey, newValue);
                checkTargetFiles();
            }
        });
        pixCheck.setSelected(AppVariables.getUserConfigBoolean(WeiboPixKey));

        keepPageCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov,
                    Boolean oldValue, Boolean newValue) {
                AppVariables.setUserConfigValue(WeiboKeepPageKey, newValue);
            }
        });
        keepPageCheck.setSelected(AppVariables.getUserConfigBoolean(WeiboKeepPageKey));

        miaoCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov,
                    Boolean oldValue, Boolean newValue) {
                AppVariables.setUserConfigValue(WeiboMiaoKey, newValue);
            }
        });
        miaoCheck.setSelected(AppVariables.getUserConfigBoolean(WeiboMiaoKey));

        openPathCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov,
                    Boolean oldValue, Boolean newValue) {
                AppVariables.setUserConfigValue(WeiboOpenPathKey, newValue);
            }
        });
        openPathCheck.setSelected(AppVariables.getUserConfigBoolean(WeiboOpenPathKey));

        closeWindowCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov,
                    Boolean oldValue, Boolean newValue) {
                AppVariables.setUserConfigValue(WeiboColseWindowKey, newValue);
            }
        });
        closeWindowCheck.setSelected(AppVariables.getUserConfigBoolean(WeiboColseWindowKey));

        categoryGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkCategory();
            }
        });
        checkCategory();

        FxmlControl.setTooltip(keepPageCheck, new Tooltip(AppVariables.message("MergePDFComments")));

        startButton.disableProperty().bind(Bindings.isEmpty(targetPathInput.textProperty())
                .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                .or(startMonthInput.styleProperty().isEqualTo(badStyle))
                .or(endMonthInput.styleProperty().isEqualTo(badStyle))
                .or(zoomBox.getEditor().styleProperty().isEqualTo(badStyle))
                .or(addressBox.getEditor().styleProperty().isEqualTo(badStyle))
                .or(addressBox.getSelectionModel().selectedItemProperty().isNull())
                .or(widthBox.getEditor().styleProperty().isEqualTo(badStyle))
                .or(pdfCheck.styleProperty().isEqualTo(badStyle))
        //                .or(bypassSSLCheck.selectedProperty().not())
        );

//        wowButton.disableProperty().bind(startButton.disableProperty());
    }

    private void checkCategory() {
        RadioButton selected = (RadioButton) categoryGroup.getSelectedToggle();
        if (AppVariables.message("InMonthsPaths").equals(selected.getText())) {
            categoryType = WeiboSnapParameters.FileCategoryType.InMonthsPaths;

        } else if (AppVariables.message("InYearsPaths").equals(selected.getText())) {
            categoryType = WeiboSnapParameters.FileCategoryType.InYearsPaths;

        } else if (AppVariables.message("InOnePath").equals(selected.getText())) {
            categoryType = WeiboSnapParameters.FileCategoryType.InOnePath;
        }
    }

    protected void checkPdfMem() {
        String pm = getUserConfigValue("PdfMemDefault", "1GB");
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
    }

    @FXML
    protected void PdfMem500MB(ActionEvent event) {
        AppVariables.setPdfMem("500MB");
    }

    @FXML
    protected void PdfMem1GB(ActionEvent event) {
        AppVariables.setPdfMem("1GB");
    }

    @FXML
    protected void PdfMem2GB(ActionEvent event) {
        AppVariables.setPdfMem("2GB");
    }

    @FXML
    protected void pdfMemUnlimit(ActionEvent event) {
        AppVariables.setPdfMem("Unlimit");
    }

    @FXML
    @Override
    public void startAction() {
        makeParameters();
        if (parameters == null) {
            popError(AppVariables.message("ParametersError"));
            return;
        }
        startSnap();
    }

    @FXML
    protected void exampleAction(ActionEvent event) {
        if (!addressList.contains("https://www.weibo.com/wow")) {
            addressBox.setValue("https://www.weibo.com/wow");
            webAddress = "https://www.weibo.com/wow";
        }
        makeParameters();
        if (parameters == null) {
            popError(AppVariables.message("ParametersError"));
            return;
        }
        parameters.setWebAddress("https://weibo.com/wow");
        parameters.setStartMonth(DateTools.parseMonth("2012-01"));
        parameters.setEndMonth(DateTools.parseMonth("2012-01"));
        startSnap();
    }

    @FXML
    protected void callMiao(MouseEvent event) {
        FxmlControl.miao3();
    }

    @FXML
    protected void suggestedSettings(ActionEvent event) {
        if (addressBox.getValue() == null) {
            addressBox.setValue("https://www.weibo.com/wow");
        }
        retryBox.getSelectionModel().select("3");
        zoomBox.getSelectionModel().select("1.0");
        widthBox.getSelectionModel().select("700");
        imageSizeRadio.setSelected(true);
        MarginsBox.getSelectionModel().select("20");
        pdfScaleBox.getSelectionModel().select("60");
        pdfMem1GRadio.setSelected(true);
        monthsPathsRadio.setSelected(true);
        pngRadio.setSelected(true);
    }

    private WeiboSnapParameters makeParameters() {
        try {
            if (webAddress == null || webAddress.isEmpty()) {
                return null;
            }
            parameters = new WeiboSnapParameters();
            parameters.setWebAddress(webAddress);
            if (startMonth == null) {
                startMonth = DateTools.parseMonth("2009-08");
            }
            parameters.setStartMonth(startMonth);
            if (endMonth == null) {
                endMonth = new Date();
            }
            parameters.setEndMonth(endMonth);
            if (startPage <= 0) {
                startPage = 1;
            }
            parameters.setStartPage(startPage);
            parameters.setZoomScale(zoomScale);
            parameters.setWebWidth(webWidth);
            parameters.setLoadInterval(accessInterval);
            parameters.setImagePerScreen(true);
            parameters.setFormat(format);
            parameters.setJpegQuality(jpegQuality);
            parameters.setThreshold(threshold);
            parameters.setIsImageSize(isImageSize);
            parameters.setPageWidth(pageWidth);
            parameters.setPageHeight(pageHeight);
            parameters.setMarginSize(marginSize);
            parameters.setAuthor(authorInput.getText());
            parameters.setTargetPath(targetPath);
            parameters.setCreatePDF(pdfCheck.isSelected());
            parameters.setCreateHtml(htmlCheck.isSelected());
            parameters.setKeepPagePdf(keepPageCheck.isSelected());
            parameters.setMiao(miaoCheck.isSelected());
            parameters.setRetry(retry);
            parameters.setMaxMergeSize(maxMergeSize);
            parameters.setExpandComments(expandCommentsCheck.isSelected());
            parameters.setFullScreen(false);
            parameters.setSavePictures(pixCheck.isSelected());
            parameters.setExpandPicture(expandPicturesCheck.isSelected());
            parameters.setCategory(categoryType);
            parameters.setTempdir(AppVariables.MyBoxTempPath);
            parameters.setPdfScale(pdfScale);
            parameters.setOpenPathWhenStop(openPathCheck.isSelected());
            parameters.setFontName("幼圆");
            parameters.setDithering(ditherCheck.isSelected());
            parameters.setUseTempFiles(true);
            parameters.setSnapInterval(snapInterval);
            parameters.setBypassSSL(bypassSSLCheck.isSelected());
            return parameters;
        } catch (Exception e) {
            parameters = null;
            logger.debug(e.toString());
            return null;
        }
    }

    protected void startSnap() {
        try {
            if (parameters == null) {
                popError(AppVariables.message("ParametersError"));
                return;
            }

            WeiboSnapRunController controller = (WeiboSnapRunController) openStage(CommonValues.WeiboSnapRunFxml);
            controller.start(parameters);
            if (closeWindowCheck.isSelected()) {
                controller.setParent(null);
                closeStage();
            } else {
                controller.setParent(this);
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void setDuration(String start, String end) {
        startMonthInput.setText(start);
        endMonthInput.setText(end);
    }

}
