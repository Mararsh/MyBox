package mara.mybox.controller;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
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
import mara.mybox.data.WeiboSnapParameters;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.PdfTools.PdfImageFormat;
import mara.mybox.value.AppPaths;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.SystemConfig;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-9-13
 * @Description
 * @License Apache License Version 2.0
 */
public class WeiboSnapController extends BaseController {

    public final static String exmapleAddress = "https://weibo.com/p/1001067609807801/home";
    private int accessInterval, webWidth, categoryType, retry, startPage, snapInterval, likeStartPage;
    private boolean isImageSize;
    private String webAddress;
    private WeiboSnapParameters parameters;
    private Date startMonth, endMonth;
    private float zoomScale;
    private int marginSize, pageWidth, pageHeight, jpegQuality, threshold, maxMergeSize, pdfScale;
    private PdfImageFormat format;
    private List<String> addressList;

    @FXML
    protected ControlPathInput targetPathInputController;
    @FXML
    protected ToggleGroup sizeGroup, formatGroup, categoryGroup, pdfMemGroup, pdfSizeGroup;
    @FXML
    protected ComboBox<String> addressBox, zoomBox, widthBox, retryBox,
            MarginsBox, standardSizeBox, jpegBox, pdfScaleBox;
    @FXML
    protected TextField startMonthInput, endMonthInput, startPageInput, accessIntervalInput,
            snapIntervalInput, customWidthInput, customHeightInput, authorInput, thresholdInput,
            headerInput, likeStartPageInput;
    @FXML
    protected Button recoverPassportButton;
    @FXML
    protected CheckBox pdfCheck, htmlCheck, pixCheck, keepPageCheck, miaoCheck, ditherCheck,
            expandCommentsCheck, expandPicturesCheck, openPathCheck, closeWindowCheck,
            likeCheck, postsCheck;
    @FXML
    protected RadioButton imageSizeRadio, monthsPathsRadio, pngRadio,
            pdfMem500MRadio, pdfMem1GRadio, pdfMem2GRadio, pdfMemUnlimitRadio,
            pdfSize500MRadio, pdfSize1GRadio, pdfSize2GRadio, pdfSizeUnlimitRadio;
    @FXML
    protected ControlTTFSelecter ttfController;

    public WeiboSnapController() {
        baseTitle = Languages.message("WeiboSnap");
        TipsLabelKey = "WeiboAddressComments";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            initWebOptions();
            initSnapOptions();
            initPdfOptionsSection();
            initNetworkOptions();
            initTargetOptions();

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(startMonthInput, new Tooltip(Languages.message("WeiboEarlestMonth")));
            NodeStyleTools.setTooltip(keepPageCheck, new Tooltip(Languages.message("MergePDFComments")));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    private void initWebOptions() {

        addressList = TableStringValues.max("WeiBoAddress", 20);

        addressBox.getItems().addAll(addressList);
        addressBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
                if (isSettingValues || newValue == null || newValue.trim().isEmpty()) {
                    return;
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String s = newValue.trim();
                            int pos1 = s.indexOf("http");
                            if (pos1 > 0) {
                                s = s.substring(pos1);
                            }
                            int pos2 = s.indexOf('?');
                            if (pos2 > 0) {
                                s = s.substring(0, pos2);
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
                                addressBox.getEditor().setStyle(UserConfig.badStyle());
                                webAddress = "";
                            } else {
                                webAddress = s;
                                addressBox.getEditor().setStyle(null);
                                UserConfig.setString(baseName + "WeiboLastAddress", s);
                                boolean in = false;
                                for (String addr : addressList) {
                                    if (addr.contains(webAddress)) {
                                        in = true;
                                        break;
                                    }
                                }
                                if (!in) {
                                    TableStringValues.add("WeiBoAddress", s);
                                    addressList = TableStringValues.max("WeiBoAddress", 20);
                                }
                            }
                        } catch (Exception e) {
                            addressBox.getEditor().setStyle(UserConfig.badStyle());
                            webAddress = "";
                        }
                    }
                });
            }
        });
        addressBox.getSelectionModel().select(0);
        if (!addressList.contains(exmapleAddress)) {
            addressBox.setValue(exmapleAddress);
            webAddress = exmapleAddress;
        }

        startMonthInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                checkTimes();
            }
        });
        startMonthInput.setText(UserConfig.getString("WeiboPostsLastMonth", "2014-09"));

        startPage = 1;
        startPageInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                if (!postsCheck.isSelected()) {
                    startPageInput.setStyle(null);
                    return;
                }
                try {
                    int v = Integer.parseInt(startPageInput.getText());
                    if (v >= 1) {
                        startPageInput.setStyle(null);
                        startPage = v;
                    } else {
                        startPageInput.setStyle(UserConfig.badStyle());
                    }
                } catch (Exception e) {
                    startPageInput.setStyle(UserConfig.badStyle());
                }
            }
        });
        startPageInput.setText(UserConfig.getString("WeiboPostsLastPage", "1"));

        likeStartPage = 1;
        likeStartPageInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                if (!likeCheck.isSelected()) {
                    likeStartPageInput.setStyle(null);
                    return;
                }
                try {
                    int v = Integer.parseInt(likeStartPageInput.getText());
                    if (v >= 1) {
                        likeStartPageInput.setStyle(null);
                        likeStartPage = v;
                    } else {
                        likeStartPageInput.setStyle(UserConfig.badStyle());
                    }
                } catch (Exception e) {
                    likeStartPageInput.setStyle(UserConfig.badStyle());
                }
            }
        });
        likeStartPageInput.setText(UserConfig.getString("WeiboLikeLastPage", "1"));

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
                UserConfig.setBoolean(baseName + "WeiboExpandComments", newValue);
            }
        });
        expandCommentsCheck.setSelected(UserConfig.getBoolean(baseName + "WeiboExpandComments"));

        expandPicturesCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov,
                    Boolean oldValue, Boolean newValue) {
                UserConfig.setBoolean(baseName + "WeiboExpandPictures", newValue);
            }
        });
        expandPicturesCheck.setSelected(UserConfig.getBoolean(baseName + "WeiboExpandPictures"));

    }

    private void checkTimes() {
        if (!postsCheck.isSelected()) {
            startMonthInput.setStyle(null);
            endMonthInput.setStyle(null);
            return;
        }
        Date weiboStart = DateTools.encodeDate("2009-08");
        Date thisMonth = DateTools.thisMonth();
        try {
            String start = startMonthInput.getText();
            if (start == null || start.isEmpty()) {
                startMonth = weiboStart;
                startMonthInput.setStyle(null);
            } else {
                startMonth = DateTools.encodeDate(start);
                if (startMonth.getTime() > thisMonth.getTime()) {
                    startMonthInput.setStyle(UserConfig.badStyle());
                    return;
                } else if (startMonth.getTime() < DateTools.encodeDate("2009-08").getTime()) {
//                    startInput.setText("2009-08");
                    startMonthInput.setStyle(UserConfig.badStyle());
                    return;
                } else {
                    startMonthInput.setStyle(null);
                }
            }
            UserConfig.setString("WeiboLastStartMonthKey", startMonthInput.getText());
        } catch (Exception e) {
            startMonthInput.setStyle(UserConfig.badStyle());
            return;
        }

        try {
            String end = endMonthInput.getText();
            if (end == null || end.isEmpty()) {
                endMonth = thisMonth;
                endMonthInput.setStyle(null);
            } else {
                endMonth = DateTools.encodeDate(end);
                if (endMonth.getTime() > thisMonth.getTime()) {
                    endMonth = thisMonth;
                }
                endMonthInput.setStyle(null);
            }
        } catch (Exception e) {
            endMonthInput.setStyle(UserConfig.badStyle());
            return;
        }

        if (startMonth.getTime() > endMonth.getTime()) {
            startMonthInput.setStyle(UserConfig.badStyle());
            endMonthInput.setStyle(UserConfig.badStyle());
        }

    }

    private void initSnapOptions() {

        zoomBox.getItems().addAll(Arrays.asList("1.0", "1.5", "2", "1.6", "1.8", "0.8"));
        zoomBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov,
                    String oldValue, String newValue) {
                try {
                    zoomScale = Float.parseFloat(newValue);
                    if (zoomScale > 0) {
                        UserConfig.setString(baseName + "Zoom", zoomScale + "");
                        if (zoomScale > 2) {
                            popInformation(Languages.message("TooLargerScale"));
                        }
                        ValidationTools.setEditorNormal(zoomBox);
                    } else {
                        zoomScale = 1.0f;
                        ValidationTools.setEditorBadStyle(zoomBox);
                    }

                } catch (Exception e) {
                    zoomScale = 1.0f;
                    ValidationTools.setEditorBadStyle(zoomBox);
                }
            }
        });
        zoomBox.getSelectionModel().select(0);

        widthBox.getItems().addAll(Arrays.asList("700", "900", "1000", "800", "1200", "1400", "1800",
                Languages.message("ScreenWidth")));
        widthBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov,
                    String oldValue, String newValue) {
                try {
                    if (newValue.equals(Languages.message("ScreenWidth"))) {
                        webWidth = -1;
                        ValidationTools.setEditorNormal(widthBox);
                        return;
                    }
                    webWidth = Integer.parseInt(newValue);
                    if (webWidth > 0) {
                        ValidationTools.setEditorNormal(widthBox);
                    } else {
                        webWidth = 700;
                        ValidationTools.setEditorBadStyle(widthBox);
                    }

                } catch (Exception e) {
                    webWidth = 700;
                    ValidationTools.setEditorBadStyle(widthBox);
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
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
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
                        UserConfig.setInt("WeiBoSnapInterval", v);
                    } else {
                        snapIntervalInput.setStyle(UserConfig.badStyle());
                    }
                } catch (Exception e) {
                    snapIntervalInput.setStyle(UserConfig.badStyle());
                }
            }
        });
        snapIntervalInput.setText(UserConfig.getInt("WeiBoSnapInterval", 2000) + "");

    }

    private void checkFormat() {
        jpegBox.setDisable(true);
        jpegBox.setStyle(null);
        thresholdInput.setDisable(true);
        ditherCheck.setDisable(true);

        RadioButton selected = (RadioButton) formatGroup.getSelectedToggle();
        if (Languages.message("PNG").equals(selected.getText())) {
            format = PdfImageFormat.Original;
        } else if (Languages.message("CCITT4").equals(selected.getText())) {
            format = PdfImageFormat.Tiff;
            thresholdInput.setDisable(false);
            ditherCheck.setDisable(false);
        } else if (Languages.message("JpegQuailty").equals(selected.getText())) {
            format = PdfImageFormat.Jpeg;
            jpegBox.setDisable(false);
            checkJpegQuality();
        }
    }

    private void checkJpegQuality() {
        jpegQuality = 100;
        try {
            jpegQuality = Integer.parseInt(jpegBox.getSelectionModel().getSelectedItem());
            if (jpegQuality >= 0 && jpegQuality <= 100) {
                jpegBox.setStyle(null);
            } else {
                jpegBox.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            jpegBox.setStyle(UserConfig.badStyle());
        }
    }

    private void checkThreshold() {
        try {
            if (thresholdInput.getText().isEmpty()) {
                threshold = -1;
                thresholdInput.setStyle(null);
                return;
            }
            threshold = Integer.parseInt(thresholdInput.getText());
            if (threshold >= 0 && threshold <= 255) {
                thresholdInput.setStyle(null);
            } else {
                threshold = -1;
                thresholdInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            threshold = -1;
            thresholdInput.setStyle(UserConfig.badStyle());
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

        standardSizeBox.getItems().addAll(Arrays.asList("A4-" + Languages.message("Horizontal") + " (16k)  29.7cm x 21.0cm",
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

        customWidthInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                checkCustomValues();
            }
        });
        customHeightInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                checkCustomValues();
            }
        });

        MarginsBox.getItems().addAll(Arrays.asList("20", "10", "15", "5", "25", "30"));
        MarginsBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov,
                    String oldValue, String newValue) {
                try {
                    marginSize = Integer.parseInt(newValue);
                    if (marginSize >= 0) {
                        ValidationTools.setEditorNormal(MarginsBox);
                    } else {
                        marginSize = 0;
                        ValidationTools.setEditorBadStyle(MarginsBox);
                    }

                } catch (Exception e) {
                    marginSize = 0;
                    ValidationTools.setEditorBadStyle(MarginsBox);
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
                    pdfScale = Integer.parseInt(newValue);
                    if (pdfScale >= 0) {
                        ValidationTools.setEditorNormal(pdfScaleBox);
                    } else {
                        pdfScale = 60;
                        ValidationTools.setEditorBadStyle(pdfScaleBox);
                    }

                } catch (Exception e) {
                    pdfScale = 60;
                    ValidationTools.setEditorBadStyle(pdfScaleBox);
                }
            }
        });
        pdfScaleBox.getSelectionModel().select(0);

        authorInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                UserConfig.setString(baseName + "Author", newValue);
            }
        });
        authorInput.setText(UserConfig.getString(baseName + "Author", System.getProperty("user.name")));

        ttfController.name(baseName);

        String pdfSize = UserConfig.getString("WeiBoSnapPdfSize", "500M");
        if ("1G".equals(pdfSize)) {
            pdfSize1GRadio.setSelected(true);
        } else if ("2G".equals(pdfSize)) {
            pdfSize2GRadio.setSelected(true);
        } else if (Languages.message("Unlimit").equals(pdfSize)) {
            pdfSizeUnlimitRadio.setSelected(true);
        } else {
            pdfSize500MRadio.setSelected(true);
        }

        pdfSizeGroup.selectedToggleProperty().addListener((ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) -> {
            UserConfig.setString("WeiBoSnapPdfSize",
                    ((RadioButton) pdfSizeGroup.getSelectedToggle()).getText());
        });

        checkPdfMem();

    }

    private void checkPageSize() {
        standardSizeBox.setDisable(true);
        customWidthInput.setDisable(true);
        customHeightInput.setDisable(true);
        customWidthInput.setStyle(null);
        customHeightInput.setStyle(null);
        isImageSize = false;

        RadioButton selected = (RadioButton) sizeGroup.getSelectedToggle();
        if (Languages.message("ImagesSize").equals(selected.getText())) {
            isImageSize = true;
        } else if (Languages.message("StandardSize").equals(selected.getText())) {
            standardSizeBox.setDisable(false);
            checkStandardValues();

        } else if (Languages.message("Custom").equals(selected.getText())) {
            customWidthInput.setDisable(false);
            customHeightInput.setDisable(false);
            checkCustomValues();
        }

//        AppVariables.setUserConfigBoolean(ImageCombineSizeKey, selected.getText());
    }

    private int calculateCmPixels(float cm, int dpi) {
        return (int) Math.round(cm * dpi / 2.54);
    }

    private void checkStandardValues() {
        String s = standardSizeBox.getSelectionModel().getSelectedItem();
        if (s.startsWith("A4-" + Languages.message("Horizontal"))) {
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
        if (!Languages.message("Custom").equals(selected.getText())) {
            return;
        }
        try {
            pageWidth = Integer.parseInt(customWidthInput.getText());
            if (pageWidth > 0) {
                customWidthInput.setStyle(null);
            } else {
                pageWidth = 0;
                customWidthInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            pageWidth = 0;
            customWidthInput.setStyle(UserConfig.badStyle());
        }

        try {
            pageHeight = Integer.parseInt(customHeightInput.getText());
            if (pageHeight > 0) {
                customHeightInput.setStyle(null);
            } else {
                pageHeight = 0;
                customHeightInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            pageHeight = 0;
            customHeightInput.setStyle(UserConfig.badStyle());
        }

    }

    private void initNetworkOptions() {

        retryBox.getItems().addAll(Arrays.asList("3", "0", "1", "5", "7", "10"));
        retryBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov,
                    String oldValue, String newValue) {
                try {
                    retry = Integer.parseInt(newValue);
                    if (retry > 0) {
                        UserConfig.setString(baseName + "Retry", retry + "");
                    } else {
                        retry = 3;
                    }
                } catch (Exception e) {
                    retry = 3;
                }
            }
        });
        retryBox.getSelectionModel().select(UserConfig.getString(baseName + "Retry", "3"));

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
                        UserConfig.setInt("WeiBoAccessInterval", v);
                    } else {
                        accessIntervalInput.setStyle(UserConfig.badStyle());
                    }
                } catch (Exception e) {
                    accessIntervalInput.setStyle(UserConfig.badStyle());
                }
            }
        });
        accessIntervalInput.setText(UserConfig.getInt("WeiBoAccessInterval", 2000) + "");

    }

    private void checkTargetFiles() {
        if (!pdfCheck.isSelected() && !htmlCheck.isSelected() && !pixCheck.isSelected()) {
            popError(Languages.message("NothingSave"));
            pdfCheck.setStyle(UserConfig.badStyle());
        } else {
            pdfCheck.setStyle(null);
        }
    }

    private void initTargetOptions() {

        pdfCheck.setSelected(UserConfig.getBoolean(baseName + "WeiboPdf"));
        pdfCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov,
                    Boolean oldValue, Boolean newValue) {
                UserConfig.setBoolean(baseName + "WeiboPdf", newValue);
                checkTargetFiles();
            }
        });

        htmlCheck.setSelected(UserConfig.getBoolean(baseName + "WeiboHtm"));
        htmlCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov,
                    Boolean oldValue, Boolean newValue) {
                UserConfig.setBoolean(baseName + "WeiboHtm", newValue);
                checkTargetFiles();
            }
        });

        pixCheck.setSelected(UserConfig.getBoolean(baseName + "WeiboPix"));
        pixCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov,
                    Boolean oldValue, Boolean newValue) {
                UserConfig.setBoolean(baseName + "WeiboPix", newValue);
                checkTargetFiles();
            }
        });
        checkTargetFiles();

        keepPageCheck.setSelected(UserConfig.getBoolean(baseName + "WeiboKeepPage"));
        keepPageCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                UserConfig.setBoolean(baseName + "WeiboKeepPage", newValue);
            }
        });

        miaoCheck.setSelected(UserConfig.getBoolean(baseName + "WeiboMiao"));
        miaoCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                UserConfig.setBoolean(baseName + "WeiboMiao", newValue);
            }
        });

        openPathCheck.setSelected(UserConfig.getBoolean(baseName + "WeiboOpenPath"));
        openPathCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                UserConfig.setBoolean(baseName + "WeiboOpenPath", newValue);
            }
        });

        closeWindowCheck.setSelected(UserConfig.getBoolean(baseName + "WeiboColseWindow", false));
        closeWindowCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                UserConfig.setBoolean(baseName + "WeiboColseWindow", newValue);
            }
        });

        categoryGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                checkCategory();
            }
        });
        checkCategory();

        targetPathInputController.baseName(baseName).init();

        startButton.disableProperty().bind(targetPathInputController.valid.not()
                .or(startMonthInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                .or(endMonthInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                .or(zoomBox.getEditor().styleProperty().isEqualTo(UserConfig.badStyle()))
                .or(addressBox.getEditor().styleProperty().isEqualTo(UserConfig.badStyle()))
                .or(addressBox.getSelectionModel().selectedItemProperty().isNull())
                .or(widthBox.getEditor().styleProperty().isEqualTo(UserConfig.badStyle()))
                .or(pdfCheck.styleProperty().isEqualTo(UserConfig.badStyle()))
        );

    }

    private void checkCategory() {
        RadioButton selected = (RadioButton) categoryGroup.getSelectedToggle();
        if (Languages.message("InMonthsPaths").equals(selected.getText())) {
            categoryType = WeiboSnapParameters.FileCategoryType.InMonthsPaths;

        } else if (Languages.message("InYearsPaths").equals(selected.getText())) {
            categoryType = WeiboSnapParameters.FileCategoryType.InYearsPaths;

        } else if (Languages.message("InOnePath").equals(selected.getText())) {
            categoryType = WeiboSnapParameters.FileCategoryType.InOnePath;
        }
    }

    protected void checkPdfMem() {
        String pm = UserConfig.getString("PdfMemDefault", "1GB");
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
        UserConfig.setPdfMem("500MB");
    }

    @FXML
    protected void PdfMem1GB(ActionEvent event) {
        UserConfig.setPdfMem("1GB");
    }

    @FXML
    protected void PdfMem2GB(ActionEvent event) {
        UserConfig.setPdfMem("2GB");
    }

    @FXML
    protected void pdfMemUnlimit(ActionEvent event) {
        UserConfig.setPdfMem("Unlimit");
    }

    @FXML
    protected void initWebview() {
        try {
            HtmlPopController controller = HtmlPopController.openAddress(this, "https://weibo.com");
            controller.handling(message("FirstRunInfo"));
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        controller.loadAddress("https://weibo.com");
                    });
                }
            }, 12000);
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        SystemConfig.setBoolean("WeiboRunFirstTime", false);
                        controller.closeStage();
                    });
                }
            }, 20000);
        } catch (Exception e) {
            closeStage();
        }
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();

            alertInformation(message("FunctionNotWork"));

            // Webview need be initialized for weibo.com.
//            if (SystemConfig.getBoolean("WeiboRunFirstTime" + AppValues.AppVersion, true)) {
//                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//                alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
//                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
//                alert.setTitle(this.baseTitle);
//                alert.setContentText(Languages.message("WeiboSSL"));
//                ButtonType buttonSSL = new ButtonType("SSL");
//                ButtonType buttonCancel = new ButtonType(Languages.message("Cancel"));
//                alert.getButtonTypes().setAll(buttonSSL, buttonCancel);
//                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
//                stage.setAlwaysOnTop(true);
//                stage.toFront();
//                stage.sizeToScene();
//                Optional<ButtonType> result = alert.showAndWait();
//                if (result.get() == buttonSSL) {
//                    initWebview();
//                    SystemConfig.setBoolean("WeiboRunFirstTime" + AppValues.AppVersion, false);
//                }
//            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    @Override
    public void startAction() {
        MyBoxLog.debug(webAddress);
        makeParameters(webAddress);
        if (parameters == null) {
            popError(Languages.message("ParametersError"));
            return;
        }
        startSnap();
    }

    @FXML
    protected void demo(ActionEvent event) {
        if (!addressList.contains(exmapleAddress)) {
            addressBox.setValue(exmapleAddress);
            webAddress = exmapleAddress;
        }
        makeParameters(webAddress);
        if (parameters == null) {
            popError(Languages.message("ParametersError"));
            return;
        }
        parameters.setWebAddress(exmapleAddress);
        parameters.setStartMonth(DateTools.encodeDate("2014-09"));
        parameters.setEndMonth(DateTools.encodeDate("2014-10"));
        targetPath = targetPathInputController.file;
        parameters.setTargetPath(targetPath == null ? new File(AppPaths.getGeneratedPath()) : targetPath);
        startSnap();
    }

    @FXML
    protected void suggestedSettings(ActionEvent event) {
        if (addressBox.getValue() == null) {
            addressBox.setValue(exmapleAddress);
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

    protected WeiboSnapParameters makeParameters(String address) {
        try {
            parameters = new WeiboSnapParameters();
            parameters.setWebAddress(address);
            if (startMonth == null) {
                startMonth = DateTools.encodeDate("2009-08");
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
            targetPath = targetPathInputController.file;
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
            parameters.setFontFile(ttfController.ttfFile);
            parameters.setDithering(ditherCheck.isSelected());
            parameters.setUseTempFiles(true);
            parameters.setSnapInterval(snapInterval);
            parameters.setDpi(dpi);
            parameters.setLikeStartPage(likeStartPage);
            parameters.setMaxMergedSize(((RadioButton) pdfSizeGroup.getSelectedToggle()).getText());
            parameters.setRetried(0);
            return parameters;
        } catch (Exception e) {
            parameters = null;
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    protected void startSnap() {
        try {
            targetPath = targetPathInputController.file;
            if (webAddress == null || webAddress.isEmpty() || parameters == null || targetPath == null) {
                popError(Languages.message("ParametersError"));
                return;
            }

            if (postsCheck.isSelected()) {
                WeiboSnapPostsController pageController = (WeiboSnapPostsController) openStage(Fxmls.WeiboSnapPostsFxml);
                pageController.start(parameters);
                if (closeWindowCheck.isSelected()) {
                    pageController.setParent(null);
                    closeStage();
                } else {
                    pageController.setParent(this);
                }
            }

            if (likeCheck.isSelected()) {
                WeiboSnapLikeController likeController = (WeiboSnapLikeController) openStage(Fxmls.WeiboSnapLikeFxml);
                likeController.start(parameters);
                if (closeWindowCheck.isSelected()) {
                    likeController.setParent(null);
                    closeStage();
                } else {
                    likeController.setParent(this);
                }
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setDuration(String start, String end) {
        startMonthInput.setText(start);
        endMonthInput.setText(end);
    }

}
