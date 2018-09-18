package mara.mybox.controller;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.objects.CommonValues;
import mara.mybox.objects.WeiboSnapParameters;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FxmlTools;
import static mara.mybox.tools.FxmlTools.badStyle;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

/**
 * @Author Mara
 * @CreateDate 2018-9-13
 * @Description
 * @License Apache License Version 2.0
 */
public class WeiboSnapController extends BaseController {

    private final String WeiboTargetPathKey, WeiboLoadDelayKey, WeiboScrollDelayKey, WeiboMaxDelayKey, WeiboZoomKey;
    private final String WeiboLastAddressKey, WeiboAuthorKey, WeiboRetryKey, WeiboMaxMergeKey;
    private int loadDelay, scrollDelay, maxDelay;
    protected int lastHtmlLen, snapHeight, snapCount, retry;
    private boolean imagePerScreen, isImageSize;
    private String webAddress;
    private WeiboSnapParameters parameters;
    private Date startMonth, endMonth;
    protected int marginSize, pageWidth, pageHeight, jpegQuality, format, threshold, maxMergeSize;
    protected PDRectangle pageSize;

    @FXML
    private VBox mainPane;
    @FXML
    private ToggleGroup imageGroup, sizeGroup, formatGroup;
    @FXML
    private ComboBox<String> loadDelayBox, scrollDelayBox, maxDelayBox, retryBox, maxMergedBox;
    @FXML
    private TextField addressInput, pathInput, startInput, endInput;
    @FXML
    private Button startButton, exampleButton;
    @FXML
    private CheckBox pdfCheck, htmlCheck, keepPageCheck, miaoCheck, fullScreenCheck, expandCommentsCheck;
    @FXML
    protected ComboBox<String> MarginsBox, standardSizeBox, standardDpiBox, jpegBox, fontBox;
    @FXML
    protected TextField customWidthInput, customHeightInput, authorInput, thresholdInput, headerInput;
    @FXML
    protected HBox sizeBox, delayBox;

    public WeiboSnapController() {
        WeiboTargetPathKey = "WeiboTargetPathKey";
        WeiboLoadDelayKey = "WeiboLoadDelayKey";
        WeiboScrollDelayKey = "WeiboScrollDelayKey";
        WeiboMaxDelayKey = "WeiboMaxDelayKey";
        WeiboZoomKey = "WeiboZoomKey";
        WeiboLastAddressKey = "WeiboLastAddressKey";
        WeiboAuthorKey = "WeiboAuthorKey";
        WeiboRetryKey = "WeiboRetryKey";
        WeiboMaxMergeKey = "WeiboMaxMergeKey";
    }

    @Override
    protected void initializeNext() {
        try {
            initWebOptions();
            initSnapOptions();
            initPdfOptionsSection();
            initTargetOptions();

            Tooltip tips = new Tooltip(getMessage("MiaoPrompt"));
            tips.setFont(new Font(16));
            FxmlTools.quickTooltip(miaoCheck, tips);

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    private void initWebOptions() {

        addressInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                try {
                    String s = newValue;
                    int pos = s.indexOf("?");
                    if (pos > 0) {
                        s = s.substring(0, pos);
                    }
                    if (s.endsWith("/profile")) {
                        s = s.substring(0, s.length() - "/profile".length());
                    }
                    if (!s.startsWith("https://weibo.com/")
                            || s.length() <= "https://weibo.com/".length()) {
                        addressInput.setStyle(badStyle);
                        webAddress = "";
                    } else {
                        webAddress = s;
                        addressInput.setStyle(null);
                        AppVaribles.setConfigValue(WeiboLastAddressKey, s);
                    }
                } catch (Exception e) {
                    addressInput.setStyle(badStyle);
                    webAddress = "";
                }
            }
        });
        addressInput.setText(AppVaribles.getConfigValue(WeiboLastAddressKey, "https://weibo.com/wow"));

        Tooltip tips = new Tooltip(AppVaribles.getMessage("WeiboEarlestMonth"));
        tips.setFont(new Font(16));
        FxmlTools.quickTooltip(startInput, tips);
        startInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                checkTimes();
            }
        });
        startInput.setText(AppVaribles.getConfigValue("WeiboLastStartMonthKey", ""));

        endInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                checkTimes();
            }
        });

        tips = new Tooltip(AppVaribles.getMessage("SnapDelayCommnets"));
        tips.setFont(new Font(16));
        FxmlTools.quickTooltip(delayBox, tips);

        retryBox.getItems().addAll(Arrays.asList("3", "0", "1", "5", "7", "10"));
        retryBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov,
                    String oldValue, String newValue) {
                try {
                    retry = Integer.valueOf(newValue);
                    if (retry > 0) {
                        AppVaribles.setConfigValue(WeiboRetryKey, retry + "");
                    } else {
                        retry = 3;
                    }
                } catch (Exception e) {
                    retry = 3;
                }
            }
        });
        retryBox.getSelectionModel().select(AppVaribles.getConfigValue(WeiboRetryKey, "3"));

        loadDelayBox.getItems().addAll(Arrays.asList("2", "3", "5", "1", "7", "10"));
        loadDelayBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov,
                    String oldValue, String newValue) {
                try {
                    loadDelay = Integer.valueOf(newValue);
                    if (loadDelay > 0) {
                        loadDelayBox.getEditor().setStyle(null);
                        AppVaribles.setConfigValue(WeiboLoadDelayKey, loadDelay + "");
                    } else {
                        loadDelay = 5000;
                        loadDelayBox.getEditor().setStyle(badStyle);
                    }

                } catch (Exception e) {
                    loadDelay = 5000;
                    loadDelayBox.getEditor().setStyle(badStyle);
                }
            }
        });
        loadDelayBox.getSelectionModel().select(AppVaribles.getConfigValue(WeiboLoadDelayKey, "2000"));

        scrollDelayBox.getItems().addAll(Arrays.asList("100", "50", "300", "1000", "600", "2000", "3000", "5000"));
        scrollDelayBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov,
                    String oldValue, String newValue) {
                try {
                    scrollDelay = Integer.valueOf(newValue);
                    if (scrollDelay > 0) {
                        scrollDelayBox.getEditor().setStyle(null);
                        AppVaribles.setConfigValue(WeiboScrollDelayKey, scrollDelay + "");
                    } else {
                        scrollDelay = 100;
                        scrollDelayBox.getEditor().setStyle(badStyle);
                    }

                } catch (Exception e) {
                    scrollDelay = 100;
                    scrollDelayBox.getEditor().setStyle(badStyle);
                }
            }
        });
        scrollDelayBox.getSelectionModel().select(AppVaribles.getConfigValue(WeiboScrollDelayKey, "100"));

        maxDelayBox.getItems().addAll(Arrays.asList("60", "120", "180", "300", "150", "240"));
        maxDelayBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov,
                    String oldValue, String newValue) {
                try {
                    maxDelay = Integer.valueOf(newValue);
                    if (maxDelay > 0) {
                        maxDelayBox.getEditor().setStyle(null);
                        AppVaribles.setConfigValue(WeiboMaxDelayKey, maxDelay + "");
                    } else {
                        maxDelay = 30000;
                        maxDelayBox.getEditor().setStyle(badStyle);
                    }

                } catch (Exception e) {
                    maxDelay = 30000;
                    maxDelayBox.getEditor().setStyle(badStyle);
                }
            }
        });
        maxDelayBox.getSelectionModel().select(AppVaribles.getConfigValue(WeiboMaxDelayKey, "60000"));

    }

    private void checkTimes() {
        Date weiboStart = DateTools.parseMonth("2009-08");
        Date thisMonth = DateTools.thisMonth();
        try {
            String start = startInput.getText();
            if (start == null || start.isEmpty()) {
                startMonth = weiboStart;
                startInput.setStyle(null);
            } else {
                startMonth = DateTools.parseMonth(start);
                if (startMonth.getTime() > thisMonth.getTime()) {
                    startInput.setStyle(badStyle);
                    return;
                } else if (startMonth.getTime() < DateTools.parseMonth("2009-08").getTime()) {
//                    startInput.setText("2009-08");
                    startInput.setStyle(badStyle);
                    return;
                } else {
                    startInput.setStyle(null);
                }
            }
        } catch (Exception e) {
            startInput.setStyle(badStyle);
            return;
        }

        try {
            String end = endInput.getText();
            if (end == null || end.isEmpty()) {
                endMonth = thisMonth;
                endInput.setStyle(null);
            } else {
                endMonth = DateTools.parseMonth(end);
                if (endMonth.getTime() > thisMonth.getTime()) {
                    endMonth = thisMonth;
                }
                endInput.setStyle(null);
            }
        } catch (Exception e) {
            endInput.setStyle(badStyle);
            return;
        }

        if (startMonth.getTime() > endMonth.getTime()) {
            startInput.setStyle(badStyle);
            endInput.setStyle(badStyle);
        }

    }

    private void initSnapOptions() {
        imageGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkImageOption();
            }
        });
        checkImageOption();

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

        Tooltip tips = new Tooltip(AppVaribles.getMessage("FullScreenComments"));
        tips.setFont(new Font(16));
        FxmlTools.quickTooltip(fullScreenCheck, tips);

    }

    private void checkImageOption() {
        RadioButton selected = (RadioButton) imageGroup.getSelectedToggle();
        if (AppVaribles.getMessage("PerScreen").equals(selected.getText())) {
            imagePerScreen = true;
        } else if (AppVaribles.getMessage("PerPage").equals(selected.getText())) {
            imagePerScreen = false;
        }
    }

    private void checkFormat() {
        jpegBox.setDisable(true);
        jpegBox.setStyle(null);
        thresholdInput.setDisable(true);

        RadioButton selected = (RadioButton) formatGroup.getSelectedToggle();
        if (AppVaribles.getMessage("PNG").equals(selected.getText())) {
            format = ImagesCombinePdfController.PdfImageFormat.Original;
        } else if (AppVaribles.getMessage("CCITT4").equals(selected.getText())) {
            format = ImagesCombinePdfController.PdfImageFormat.Tiff;
            thresholdInput.setDisable(false);
        } else if (AppVaribles.getMessage("JpegQuailty").equals(selected.getText())) {
            format = ImagesCombinePdfController.PdfImageFormat.Jpeg;
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
            if (threshold >= 0 && threshold <= 100) {
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

        if (AppVaribles.showComments) {
            Tooltip tips = new Tooltip(getMessage("PdfPageSizeComments"));
            tips.setFont(new Font(16));
            FxmlTools.setComments(sizeBox, tips);
        }

        sizeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkPageSize();
            }
        });
        checkPageSize();

        standardSizeBox.getItems().addAll(Arrays.asList(
                "A4-横向 (16k)  29.7cm x 21.0cm",
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
        MarginsBox.getSelectionModel().select(0);

        authorInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                AppVaribles.setConfigValue(WeiboAuthorKey, newValue);
            }
        });
        authorInput.setText(AppVaribles.getConfigValue(WeiboAuthorKey, ""));

        Tooltip tips = new Tooltip(AppVaribles.getMessage("MergePDFComments"));
        tips.setFont(new Font(16));
        FxmlTools.quickTooltip(maxMergedBox, tips);
        maxMergedBox.getItems().addAll(Arrays.asList("500", "600", "400", "300", "700", "1000"));
        maxMergedBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov,
                    String oldValue, String newValue) {
                try {
                    maxMergeSize = Integer.valueOf(newValue);
                    if (maxMergeSize > 0) {
                        maxMergedBox.getEditor().setStyle(null);
                        AppVaribles.setConfigValue(WeiboMaxMergeKey, newValue);
                    } else {
                        maxMergeSize = 400;
                        maxMergedBox.getEditor().setStyle(badStyle);
                    }
                } catch (Exception e) {
                    maxMergeSize = 400;
                    maxMergedBox.getEditor().setStyle(badStyle);
                }
            }
        });
        maxMergedBox.getSelectionModel().select(AppVaribles.getConfigValue(WeiboMaxMergeKey, "400"));

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

//        AppVaribles.setConfigValue(ImageCombineSizeKey, selected.getText());
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
        s = s.substring(0, s.indexOf(" "));
        switch (s) {
            case "A4":
                pageWidth = calculateCmPixels(21.0f, dpi);
                pageHeight = calculateCmPixels(29.7f, dpi);
                break;
            case "A4-横向":
                pageWidth = calculateCmPixels(29.7f, dpi);
                pageHeight = calculateCmPixels(21.0f, dpi);
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

    private void checkTargetFiles() {
        if (!pdfCheck.isSelected() && !htmlCheck.isSelected()) {
            popError(AppVaribles.getMessage("NothingSave"));
            pdfCheck.setStyle(badStyle);
        } else {
            pdfCheck.setStyle(null);
        }
    }

    private void initTargetOptions() {

        pathInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try {
                    final File file = new File(newValue);
                    if (!file.exists() || !file.isDirectory()) {
                        pathInput.setStyle(badStyle);
                        return;
                    }
                    pathInput.setStyle(null);
                    AppVaribles.setConfigValue(WeiboTargetPathKey, file.getAbsolutePath());
                    targetPath = file;
                } catch (Exception e) {
                    pathInput.setStyle(badStyle);
                }
            }
        });
        pathInput.setText(AppVaribles.getConfigValue(WeiboTargetPathKey, System.getProperty("user.home")));

        pdfCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                checkTargetFiles();
            }
        });

        htmlCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                checkTargetFiles();
            }
        });

        Tooltip tips = new Tooltip(AppVaribles.getMessage("MergePDFComments"));
        tips.setFont(new Font(16));
        FxmlTools.quickTooltip(keepPageCheck, tips);

        startButton.disableProperty().bind(Bindings.isEmpty(pathInput.textProperty())
                .or(pathInput.styleProperty().isEqualTo(badStyle))
                .or(startInput.styleProperty().isEqualTo(badStyle))
                .or(endInput.styleProperty().isEqualTo(badStyle))
                .or(loadDelayBox.getEditor().styleProperty().isEqualTo(badStyle))
                .or(scrollDelayBox.getEditor().styleProperty().isEqualTo(badStyle))
                .or(maxDelayBox.getEditor().styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(addressInput.textProperty()))
                .or(addressInput.styleProperty().isEqualTo(badStyle))
                .or(pdfCheck.styleProperty().isEqualTo(badStyle))
        );

        exampleButton.disableProperty().bind(startButton.disableProperty());

    }

    @FXML
    protected void selectPath(ActionEvent event) {
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            File path = new File(AppVaribles.getConfigValue(WeiboTargetPathKey, System.getProperty("user.home")));
            if (!path.isDirectory()) {
                path = new File(System.getProperty("user.home"));
            }
            chooser.setInitialDirectory(path);
            File directory = chooser.showDialog(getMyStage());
            if (directory == null) {
                return;
            }
            AppVaribles.setConfigValue(LastPathKey, directory.getPath());
            AppVaribles.setConfigValue(targetPathKey, directory.getPath());

            pathInput.setText(directory.getPath());
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private WeiboSnapParameters makeParameters() {
        try {
            parameters = new WeiboSnapParameters();
            parameters.setWebAddress(webAddress);
            logger.debug(parameters.getWebAddress());
            if (startMonth == null) {
                startMonth = DateTools.parseMonth("2009-08");
            }
            parameters.setStartMonth(startMonth);
            if (endMonth == null) {
                endMonth = new Date();
            }
            parameters.setEndMonth(endMonth);
            parameters.setLoadDelay(loadDelay);
            parameters.setScrollDelay(scrollDelay);
            parameters.setMaxDelay(maxDelay);
            parameters.setImagePerScreen(imagePerScreen);
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
            parameters.setFullScreen(fullScreenCheck.isSelected());
            return parameters;
        } catch (Exception e) {
            parameters = null;
            logger.debug(e.toString());
            return null;
        }
    }

    @FXML
    protected void startAction(ActionEvent event) {
        makeParameters();
        if (parameters == null) {
            popError(AppVaribles.getMessage("ParametersError"));
            return;
        }
        startSnap();
    }

    @FXML
    protected void exampleAction(ActionEvent event) {
        makeParameters();
        if (parameters == null) {
            popError(AppVaribles.getMessage("ParametersError"));
            return;
        }
        parameters.setWebAddress("https://weibo.com/wow");
        parameters.setStartMonth(DateTools.parseMonth("2012-01"));
        parameters.setEndMonth(DateTools.parseMonth("2012-01"));
        startSnap();
    }

    @FXML
    protected void callMiao(MouseEvent event) {
        FxmlTools.miao();
    }

    @FXML
    protected void startSnap() {
        try {
            if (parameters == null) {
                popError(AppVaribles.getMessage("ParametersError"));
                return;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(CommonValues.WeiboSnapRunFxml), AppVaribles.CurrentBundle);
            Pane pane = fxmlLoader.load();
            final WeiboSnapRunController controller = fxmlLoader.getController();
            Stage stage = new Stage();
            controller.setMyStage(stage);
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    if (!controller.stageClosing()) {
                        event.consume();
                    }
                }
            });

            stage.initModality(Modality.NONE);
            stage.initOwner(null);
            stage.initStyle(StageStyle.DECORATED);
            stage.getIcons().add(CommonValues.AppIcon);
            stage.setScene(new Scene(pane));
            stage.show();

            controller.start(parameters);
            controller.setParent(this);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void setDuration(String start, String end) {
        startInput.setText(start);
        endInput.setText(end);
    }

}
