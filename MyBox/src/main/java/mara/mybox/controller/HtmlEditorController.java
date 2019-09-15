package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebErrorEvent;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Callback;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import mara.mybox.data.VisitHistory;
import mara.mybox.db.TableBrowserUrls;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlImageManufacture;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.NetworkTools;
import static mara.mybox.tools.NetworkTools.checkWeiboPassport;
import mara.mybox.tools.PdfTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonImageValues;

/**
 * @Author Mara
 * @CreateDate 2018-7-31
 * @Description
 * @License Apache License Version 2.0
 */
public class HtmlEditorController extends BaseController {

    private final String HtmlImagePathKey, HtmlSnapDelayKey, HtmlLastUrlsKey, HtmlPdfPathKey;
    private final String WeiBoPassportChecked;
    private WebEngine webEngine;
    private int delay, fontSize, orginalStageHeight, orginalStageY, orginalStageWidth;
    protected int lastHtmlLen, lastCodesLen, snapHeight, snapCount;
    private boolean isOneImage, isLoadingWeiboPassport;
    private URL url;
    private List<Image> images;
    private List<String> urls;
    private LoadingController loadingController;
    private float zoomScale;
    protected boolean loadSynchronously, isFrameSet;
    protected SimpleBooleanProperty fileChanged;
    protected int cols, rows;
    protected int lastTextLen;

    @FXML
    private Button loadButton, snapshotButton;
    @FXML
    private HTMLEditor htmlEditor;
    @FXML
    private WebView webView;
    @FXML
    private TextArea codesArea;
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab editorTab, codesTab, browserTab;
    @FXML
    private ComboBox<String> urlBox, delayBox;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private ToolBar snapBar;
    @FXML
    private ToggleGroup snapGroup;
    @FXML
    private CheckBox windowSizeCheck;
    @FXML
    protected TextField bottomText;

    public HtmlEditorController() {
        baseTitle = AppVariables.message("HtmlEditor");

        SourceFileType = VisitHistory.FileType.Html;
        SourcePathType = VisitHistory.FileType.Html;
        TargetPathType = VisitHistory.FileType.Html;
        TargetFileType = VisitHistory.FileType.Html;
        AddFileType = VisitHistory.FileType.Html;
        AddPathType = VisitHistory.FileType.Html;

        sourcePathKey = "HtmlFilePath";
        HtmlImagePathKey = "HtmlImagePath";
        HtmlSnapDelayKey = "HtmlSnapDelay";
        HtmlLastUrlsKey = "HtmllastUrl";
        HtmlPdfPathKey = "PdfFilePath";
        WeiBoPassportChecked = "WeiBoPassportChecked";

        sourceExtensionFilter = CommonImageValues.HtmlExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;

    }

    private boolean isPasteEvent(KeyEvent event) {
        return event.isShortcutDown() && event.getCode() == KeyCode.V;
    }

    @Override
    public void initializeNext() {
        try {

            lastCodesLen = lastHtmlLen = 0;
            isSettingValues = false;
            fontSize = 14;
            zoomScale = 1.0f;
            isFrameSet = false;

            initHtmlEdtior();

            initCodeEdtior();

            initBroswer();

            tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
                @Override
                public void changed(ObservableValue<? extends Tab> observable,
                        Tab oldValue, Tab newValue) {
                    isSettingValues = true;
                    try {
                        String contents = "";
                        Object object = null;

                        if (editorTab.equals(newValue)) {

                            if (codesTab.equals(oldValue)) {
                                object = codesArea.getText();
                            } else if (browserTab.equals(oldValue)) {
                                object = webEngine.executeScript("document.documentElement.outerHTML");
                            }

                            if (object != null) {
                                contents = (String) object;
                            }

                            isFrameSet = contents.toUpperCase().contains("</FRAMESET>");
                            if (isFrameSet) {
//                            popError(AppVariables.getMessage("NotSupportFrameSet"));
                                htmlEditor.setHtmlText("<p>" + AppVariables.message("NotSupportFrameSet") + "</p>");
                            } else {
                                htmlEditor.setHtmlText(contents);
                            }

                        } else if (codesTab.equals(newValue)) {

                            if (editorTab.equals(oldValue)) {

                                if (isFrameSet) {
                                    object = webEngine.executeScript("document.documentElement.outerHTML");
                                } else {
                                    object = htmlEditor.getHtmlText();
                                }

                            } else if (browserTab.equals(oldValue)) {
                                object = webEngine.executeScript("document.documentElement.outerHTML");
                            }

                            if (object != null) {
                                contents = (String) object;
                            }

                            isFrameSet = contents.toUpperCase().contains("</FRAMESET>");
                            codesArea.setText(contents);

                        } else if (browserTab.equals(newValue)) {
                            if (editorTab.equals(oldValue)) {
                                if (isFrameSet) {
                                    object = codesArea.getText();
                                } else {
                                    object = htmlEditor.getHtmlText();
                                }
                            } else if (codesTab.equals(oldValue)) {
                                object = codesArea.getText();
                            }
                            if (object != null) {
                                contents = (String) object;
                            }
                            isFrameSet = contents.toUpperCase().contains("</FRAMESET>");
                            if (!isFrameSet) {
                                webEngine.loadContent(contents);
                            }
                        }
                    } catch (Exception e) {
                        logger.debug(e.toString());
                    }

                    isSettingValues = false;
                }
            });

            fileChanged = new SimpleBooleanProperty(false);
            fileChanged.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    if (isSettingValues) {
                        return;
                    }
                    String t = getBaseTitle();
                    if (sourceFile != null) {
                        t += "  " + sourceFile.getAbsolutePath();
                    }
                    if (fileChanged.getValue()) {
                        getMyStage().setTitle(t + "*");
                    } else {
                        getMyStage().setTitle(t);
                    }
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    protected void initHtmlEdtior() {
        try {

            // As my testing, only DragEvent.DRAG_EXITED, KeyEvent.KEY_TYPED, KeyEvent.KEY_RELEASED working for HtmlEdior
            htmlEditor.addEventHandler(DragEvent.DRAG_EXITED, new EventHandler<InputEvent>() { // work
                @Override
                public void handle(InputEvent event) {
                    checkHtmlEditorChanged();
                }
            });
            htmlEditor.setOnKeyReleased(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent event) {
//                    logger.debug("setOnKeyReleased");
                    checkHtmlEditorChanged();
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    private void checkHtmlEditorChanged() {
        try {
            String c = htmlEditor.getHtmlText();
            int len = 0;
            if (c != null && !c.isEmpty()) {
                len = htmlEditor.getHtmlText().length();
//        logger.debug(isSettingValues + "  " + len + " " + lastHtmlLen);
                if (!isSettingValues && len != lastHtmlLen) {
                    fileChanged.set(true);
                }
            }
            lastHtmlLen = len;
            bottomText.setText(AppVariables.message("Total") + ": " + len);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initCodeEdtior() {
        try {

            codesArea.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (!isSettingValues) {
                        fileChanged.set(true);
                    }
                    int len = codesArea.getText().length();
                    lastCodesLen = len;
                    bottomText.setText(AppVariables.message("Total") + ": " + len);
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    protected void initBroswer() {
        try {

            urlBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov,
                        String oldValue, String newv) {
                    if (isSettingValues) {
                        return;
                    }
                    final String newValue = newv;
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                url = new URL(newValue);
                                if (url.getProtocol().toLowerCase().startsWith("http")) {
                                    urlBox.getEditor().setStyle(null);
                                    isSettingValues = true;
                                    urls = TableBrowserUrls.write(newValue);
                                    try {
                                        urlBox.getItems().clear();
                                        urlBox.getItems().addAll(urls);
                                        urlBox.getSelectionModel().select(0);
                                    } catch (Exception e) {
                                        logger.error(e.toString());
                                    }
                                    isSettingValues = false;
                                } else {
                                    urlBox.getEditor().setStyle(badStyle);
                                }
                            } catch (Exception e) {
//                                    loadFailed = loadCompleted = true;
//                                    errorString = e.toString();
                                logger.error(e.toString());
                                urlBox.getEditor().setStyle(badStyle);
                            }
                        }
                    });
                }
            });
            urls = TableBrowserUrls.read();
            if (!urls.isEmpty()) {
                isSettingValues = true;
                urlBox.getItems().addAll(urls);
                isSettingValues = false;
            }

            delayBox.getItems().addAll(Arrays.asList("2", "3", "5", "1", "10"));
            delayBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov,
                        String oldValue, String newValue) {
                    try {
                        delay = Integer.valueOf(newValue);
                        if (delay > 0) {
                            delay = delay * 1000;
                            AppVariables.setUserConfigValue(HtmlSnapDelayKey, newValue);
                            FxmlControl.setEditorNormal(delayBox);
                        } else {
                            delay = 2000;
                            FxmlControl.setEditorBadStyle(delayBox);
                        }

                    } catch (Exception e) {
                        delay = 2000;
                        FxmlControl.setEditorBadStyle(delayBox);
                    }
                }
            });
            delayBox.getSelectionModel().select(AppVariables.getUserConfigValue(HtmlSnapDelayKey, "2"));

            snapGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkOneImage();
                }
            });
            checkOneImage();

            webEngine = webView.getEngine();
            webEngine.setJavaScriptEnabled(true);

            //handle popup windows
//            webEngine.setCreatePopupHandler(new Callback<PopupFeatures, WebEngine>() {
//                @Override
//                public WebEngine call(PopupFeatures config) {
////                    smallView.setFontScale(0.8);
////                    if (!toolBar.getChildren().contains(smallView)) {
////                        toolBar.getChildren().add(smallView);
////                    }
////                    return smallView.getEngine();
//                }
//            }
//            );
            webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
                @Override
                public void changed(ObservableValue ov, State oldState, State newState) {
                    try {
//                        logger.debug(newState.name() + " " + webEngine.getLocation());
                        snapBar.setDisable(true);
                        switch (newState) {
                            case READY:
                                bottomText.setText("");
                                loadButton.setText(message("Load"));
                                break;
                            case SCHEDULED:
                                bottomText.setText("");
                                loadButton.setText(message("Stop"));
                                break;
                            case RUNNING:
                                bottomText.setText(AppVariables.message("Loading..."));
                                loadButton.setText(message("Stop"));
                                break;
                            case SUCCEEDED:
//                            logger.debug((String) webEngine.executeScript("document.cookie;"));
//                            logger.debug((String) webEngine.executeScript("document.referrer;"));
                                if (isLoadingWeiboPassport) {

                                    isLoadingWeiboPassport = false;
                                    if (timer != null) {
                                        timer.cancel();
                                    }
                                    timer = new Timer();
                                    if (NetworkTools.isOtherPlatforms()) {
                                        timer.schedule(new TimerTask() {
                                            @Override
                                            public void run() {
                                                AppVariables.setUserConfigValue("WeiboPassportChecked", "true");
                                                Platform.runLater(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if (checkWeiboPassport()) {
                                                            webEngine.load(url.toString());
                                                            if (loadingController != null) {
                                                                loadingController.closeStage();
                                                                loadingController = null;
                                                            }
                                                        }
                                                    }
                                                });
                                            }
                                        }, 10000);

                                    } else {
                                        timer.schedule(new TimerTask() {
                                            private boolean done = false;

                                            @Override
                                            public void run() {
                                                if (done) {
                                                    this.cancel();
                                                } else {
                                                    Platform.runLater(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            if (checkWeiboPassport()) {
                                                                webEngine.load(url.toString());
                                                                done = true;
                                                                if (loadingController != null) {
                                                                    loadingController.closeStage();
                                                                    loadingController = null;
                                                                }
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        }, 1000, 1000);
                                    }
                                } else {

                                    try {
                                        Object c = webEngine.executeScript("getComputedStyle(document.body).fontSize");
                                        if (c != null) {
                                            String s = (String) c;
                                            fontSize = Integer.valueOf(s.substring(0, s.length() - 2));
                                        } else {
                                            fontSize = 14;
                                        }
                                    } catch (Exception e) {

                                        fontSize = 14;
                                    }
                                    bottomText.setText(AppVariables.message("Loaded"));
                                    if (loadingController != null) {

                                        loadingController.closeStage();
                                        loadingController = null;
                                    }
                                    loadButton.setText(message("Load"));
                                    snapBar.setDisable(false);

                                }
                                if (loadSynchronously) {
                                    try {
                                        String contents;
                                        Object c = webEngine.executeScript("document.documentElement.outerHTML");
                                        if (c == null) {
                                            contents = "";
                                        } else {
                                            contents = (String) c;
                                        }
                                        isFrameSet = contents.toUpperCase().contains("</FRAMESET>");
                                        if (isFrameSet) {
//                                        popError(AppVariables.getMessage("NotSupportFrameSet"));
                                            htmlEditor.setHtmlText("<p>" + AppVariables.message("NotSupportFrameSet") + "</p>");
                                        } else {
                                            htmlEditor.setHtmlText(contents);
                                        }
                                        codesArea.setText(contents);
                                    } catch (Exception e) {
                                        logger.debug(e.toString());
                                    }
                                    loadSynchronously = false;
                                }

                                break;
                            case CANCELLED:
                                bottomText.setText(message("Canceled"));
                                loadButton.setText(message("Load"));
                                if (loadingController != null) {
                                    loadingController.closeStage();
                                    loadingController = null;
                                }
                                break;
                            case FAILED:
                                bottomText.setText(message("Failed"));
                                loadButton.setText(message("Load"));
                                if (loadingController != null) {
                                    loadingController.closeStage();
                                    loadingController = null;
                                }
                                break;
                            default:
                                bottomText.setText("");
                                loadButton.setText(message("Load"));
                                break;
                        }

                    } catch (Exception e) {
                        logger.debug(e.toString());
                        if (loadingController != null) {
                            loadingController.closeStage();
                            loadingController = null;
                        }
                    }

                }

            });

            webEngine.setOnAlert(new EventHandler<WebEvent<String>>() {

                @Override
                public void handle(WebEvent<String> event) {
                    logger.debug("setOnAlert " + event.getData());
                }
            });

            webEngine.setOnError(new EventHandler<WebErrorEvent>() {

                @Override
                public void handle(WebErrorEvent event) {
                    logger.debug("onError " + event.getMessage());
                }
            });

            webEngine.setConfirmHandler(new Callback<String, Boolean>() {

                @Override
                public Boolean call(String param) {
                    logger.debug("setConfirmHandler " + param);
                    return null;
                }
            });

            webEngine.getLoadWorker().exceptionProperty().addListener(new ChangeListener<Throwable>() {
                @Override
                public void changed(ObservableValue<? extends Throwable> ov, Throwable t, Throwable t1) {
                    if (t != null) {
                        logger.debug("Received exception: " + t.getMessage());
                    }
                    if (t1 != null) {
                        logger.debug("Received exception: " + t1.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    private void checkOneImage() {
        RadioButton selected = (RadioButton) snapGroup.getSelectedToggle();
        if (AppVariables.message("OneImage").equals(selected.getText())) {
            isOneImage = true;
            windowSizeCheck.setDisable(true);
        } else {
            isOneImage = false;
            windowSizeCheck.setDisable(false);
        }
    }

    @Override
    public void sourceFileChanged(final File file) {
        sourceFile = file;
        loadLink(file);
    }

    public void loadLink(String link) {
        isFrameSet = false;
        loadSynchronously = true;
        webEngine.load(link);
        urlBox.getEditor().setText(link);
    }

    public void loadLink(File file) {
        loadLink(file.toURI().toString());
    }

    @FXML
    private void loadAction(ActionEvent event) {
        isFrameSet = false;

        if (loadButton.getText().equals(message("Stop"))) {
            webEngine.getLoadWorker().cancel();
            return;
        }
        isLoadingWeiboPassport = false;

        if (loadingController != null) {
            loadingController.closeStage();
            loadingController = null;
        }
        loadingController = openHandlingStage(Modality.NONE, AppVariables.message("Loading..."));
        bottomText.setText(AppVariables.message("Loading..."));
        try {
            final String urlString = url.toString().toLowerCase();
//            logger.debug(urlString);

            if (urlString.contains("weibo.com/") && !checkWeiboPassport()) {
                isLoadingWeiboPassport = true;
                bottomText.setText(AppVariables.message("LoadingWeiboCertificate"));
                loadingController.setInfo(AppVariables.message("LoadingWeiboCertificate"));
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        webEngine.load("https://passport.weibo.com/visitor/visitor?entry=miniblog");
                    }
                });

            } else {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        webEngine.load(urlString);
                    }
                });

            }

        } catch (Exception e) {
            logger.error(e.toString());
            if (loadingController != null) {
                loadingController.closeStage();
                loadingController = null;
            }
        }

    }

    private String getBrowserContents() {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            StringWriter stringWriter = new StringWriter();
            transformer.transform(new DOMSource(webEngine.getDocument()), new StreamResult(stringWriter));
            String contents = stringWriter.getBuffer().toString();
            return contents;
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

    @FXML
    @Override
    public void saveAction() {
        try {
            isSettingValues = true;
            if (sourceFile == null) {
                final File file = chooseSaveFile(AppVariables.getUserConfigPath(targetPathKey),
                        null, targetExtensionFilter, true);
                if (file == null) {
                    return;
                }
                recordFileWritten(file);
                sourceFile = file;
            }
            String contents;
            if (tabPane.getSelectionModel().getSelectedItem().equals(codesTab)) {
                contents = codesArea.getText();
            } else {
                contents = htmlEditor.getHtmlText();
            }
            try (BufferedWriter out = new BufferedWriter(new FileWriter(sourceFile, Charset.forName("utf-8"), false))) {
                out.write(contents);
                out.flush();
            }
            fileChanged.set(false);
            getMyStage().setTitle(getBaseTitle() + "  " + sourceFile.getAbsolutePath());
            isSettingValues = false;

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    @Override
    public void saveAsAction() {
        try {
            isSettingValues = true;
            String name = null;
            if (sourceFile != null) {
                name = FileTools.getFilePrefix(sourceFile.getName());
            }
            final File file = chooseSaveFile(AppVariables.getUserConfigPath(targetPathKey),
                    name, targetExtensionFilter, true);
            if (file == null) {
                return;
            }
            recordFileWritten(file);
            sourceFile = file;
            String contents;
            if (AppVariables.message("Editor").equals(tabPane.getSelectionModel().getSelectedItem().getText())) {
                contents = htmlEditor.getHtmlText();
            } else {
                contents = codesArea.getText();
            }
            try (BufferedWriter out = new BufferedWriter(new FileWriter(sourceFile, Charset.forName("utf-8"), false))) {
                out.write(contents);
                out.flush();
            }
            fileChanged.set(false);
            getMyStage().setTitle(getBaseTitle() + "  " + sourceFile.getAbsolutePath());
            isSettingValues = false;
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    @Override
    public void createAction() {
        try {
            isSettingValues = true;
            sourceFile = null;
            htmlEditor.setHtmlText("");
            codesArea.setText("");
            lastCodesLen = lastHtmlLen = 0;
            fileChanged.set(false);
            isFrameSet = false;
            getMyStage().setTitle(getBaseTitle());
            isSettingValues = false;
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    private void snapshot(ActionEvent event) {
        File file;
        if (isOneImage) {
            file = chooseSaveFile(AppVariables.getUserConfigPath(HtmlImagePathKey),
                    null, CommonImageValues.ImageExtensionFilter, true);
        } else {
            file = chooseSaveFile(AppVariables.getUserConfigPath(HtmlPdfPathKey),
                    null, CommonImageValues.PdfExtensionFilter, true);
        }
        if (file == null) {
            return;
        }
        recordFileWritten(file);
        if (isOneImage) {
            AppVariables.setUserConfigValue(HtmlImagePathKey, file.getParent());
        } else {
            AppVariables.setUserConfigValue(HtmlPdfPathKey, file.getParent());
        }
        targetFile = file;
        images = new ArrayList<>();

        loadWholePage();

    }

    private void loadWholePage() {
        try {
            orginalStageHeight = (int) getMyStage().getHeight();
            orginalStageY = (int) myStage.getY();
            orginalStageWidth = (int) getMyStage().getWidth();
            Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
            myStage.setY(0);
            myStage.setHeight(primaryScreenBounds.getHeight());
//            myStage.setWidth(900);
//            webEngine.executeScript("document.body.style.fontSize = '15px' ;");

            snapshotButton.setDisable(true);
            final int maxDelay = delay * 30;
            final long startTime = new Date().getTime();

            if (loadingController != null) {
                loadingController.closeStage();
                loadingController = null;
            }
            loadingController = FxmlStage.openLoadingStage(myStage, Modality.WINDOW_MODAL, null);

            if (timer != null) {
                timer.cancel();
            }
            timer = new Timer();
            timer.schedule(new TimerTask() {
                int lastHeight = 0, newHeight = -1;

                @Override
                public void run() {
                    boolean quit = false;
                    if (new Date().getTime() - startTime > maxDelay) {
                        logger.debug(" TimeOver:" + newHeight);
                        quit = true;
                    }
                    if (newHeight == lastHeight) {
                        logger.debug(" Complete:" + newHeight);
                        quit = true;
                    }
                    if (quit) {
//                        Platform.runLater(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (snapingStage != null) {
//                                    snapingStage.close();
//                                }
//                            }
//                        });
                        this.cancel();
                        return;
                    }

                    lastHeight = newHeight;
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                newHeight = (Integer) webEngine.executeScript("document.body.scrollHeight");
                                loadingController.setInfo(AppVariables.message("CurrentPageHeight") + ": " + newHeight);
                                if (newHeight == lastHeight) {
                                    loadingController.setInfo(AppVariables.message("ExpandingPage"));
                                    startSnap();
                                } else {
                                    webEngine.executeScript("window.scrollTo(0," + newHeight + ");");
                                }

                            } catch (Exception e) {
                                logger.error(e.toString());
                                if (loadingController != null) {
                                    loadingController.closeStage();
                                    loadingController = null;
                                }
                            }
                        }
                    });

                }
            }, 0, delay);

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    private void startSnap() {
        try {
            final int totalHeight = (Integer) webEngine.executeScript("document.body.scrollHeight");
            final int snapStep = (Integer) webEngine.executeScript("document.documentElement.clientHeight < document.body.clientHeight ? document.documentElement.clientHeight : document.body.clientHeight");
            final int width = (Integer) webEngine.executeScript("document.documentElement.clientWidth < document.body.clientWidth ? document.documentElement.clientWidth : document.body.clientWidth ");
            snapHeight = 0;
            snapCount = 0;
            webEngine.executeScript("window.scrollTo(0,0 );");
            final int scrollDelay = 300;
            bottomText.setText(AppVariables.message("SnapingImage..."));
            final SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);
            if (timer != null) {
                timer.cancel();
            }
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (totalHeight <= snapHeight) {
                        this.cancel();
                    } else {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                try {

                                    Image snapshot = webView.snapshot(parameters, null);
                                    if (totalHeight < snapHeight + snapStep) { // last snap
                                        snapshot = FxmlImageManufacture.cropOutsideFx(snapshot, 0, snapStep - (totalHeight - snapHeight),
                                                width - 1, (int) snapshot.getHeight() - 1);
                                    } else {
                                        snapshot = FxmlImageManufacture.cropOutsideFx(snapshot, 0, 0,
                                                width - 1, (int) snapshot.getHeight() - 1);
                                    }
                                    images.add(snapshot);
                                    snapHeight += snapStep;
                                    loadingController.setInfo(AppVariables.message("CurrentPageHeight") + ": " + snapHeight);
                                    if (totalHeight <= snapHeight) { // last snap

                                        loadingController.setInfo(AppVariables.message("WritingFile"));
                                        boolean success = true;
                                        if (isOneImage) {
                                            Image finalImage = FxmlImageManufacture.combineSingleColumn(images);
                                            if (finalImage != null) {
                                                String format = FileTools.getFileSuffix(targetFile.getAbsolutePath());
                                                final BufferedImage bufferedImage = FxmlImageManufacture.getBufferedImage(finalImage);
                                                ImageFileWriters.writeImageFile(bufferedImage, format, targetFile.getAbsolutePath());
                                            } else {
                                                success = false;
                                            }
                                        } else {
                                            success = PdfTools.htmlIntoPdf(images, targetFile, windowSizeCheck.isSelected());
                                        }
                                        if (success && targetFile.exists()) {
                                            view(targetFile);
                                        } else {
                                            popFailed();
                                        }

                                        webEngine.executeScript("window.scrollTo(0,0 );");
                                        bottomText.setText("");
                                        snapshotButton.setDisable(false);

                                        if (loadingController != null) {
                                            loadingController.closeStage();
                                            loadingController = null;
                                        }
                                        myStage.setY(orginalStageY);
                                        myStage.setHeight(orginalStageHeight);
                                    } else {
                                        webEngine.executeScript("window.scrollTo(0, " + snapHeight + ");");
                                    }
                                } catch (Exception e) {
                                    logger.error(e.toString());
                                    webEngine.executeScript("window.scrollTo(0,0 );");
                                    popFailed();
                                    if (loadingController != null) {
                                        loadingController.closeStage();
                                        loadingController = null;
                                    }
                                }
                            }
                        });
                    }
                }
            }, delay, scrollDelay);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    private void zoomIn(ActionEvent event) {
//        ++fontSize;
//        webEngine.executeScript("document.body.style.fontSize = '" + fontSize + "px' ;");
        zoomScale += 0.1f;
        webView.setZoom(zoomScale);
    }

    @FXML
    private void zoomOut(ActionEvent event) {
//        --fontSize;
//        webEngine.executeScript("document.body.style.fontSize = '" + fontSize + "px' ;");
        zoomScale -= 0.1f;
        webView.setZoom(zoomScale);
    }

    @FXML
    private void backAction(ActionEvent event) {
        isFrameSet = false;
        webEngine.executeScript("window.history.back();");
    }

    @FXML
    private void forwardAction(ActionEvent event) {
        isFrameSet = false;
        webEngine.executeScript("window.history.forward();");
    }

    @FXML
    private void refreshAction(ActionEvent event) {
        webEngine.executeScript("location.reload() ;");
    }

    public void switchBroswerTab() {
        tabPane.getSelectionModel().select(browserTab);
    }

    public void loginWeibo() {
        try {
            tabPane.getSelectionModel().select(browserTab);
            isFrameSet = false;
            isSettingValues = true;
            urlBox.setValue("https://weibo.com");
            isSettingValues = false;
            webEngine.load("https://weibo.com");
            popInformation(message("WeiboAfterLogin"), -1);
        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    @Override
    public boolean leavingScene() {
        if (loadingController != null) {
            loadingController.closeStage();
            loadingController = null;
        }
        if (timer != null) {
            timer.cancel();
        }
        if (webEngine != null && webEngine.getLoadWorker() != null) {
            webEngine.getLoadWorker().cancel();
        }
        webEngine = null;

        webView = null;

        return super.leavingScene();

    }

    @Override
    public boolean checkBeforeNextAction() {
//        logger.debug(fileChanged.getValue());

        if (fileChanged == null || !fileChanged.getValue()) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(AppVariables.message("FileChanged"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSave = new ButtonType(AppVariables.message("Save"));
            ButtonType buttonNotSave = new ButtonType(AppVariables.message("NotSave"));
            ButtonType buttonCancel = new ButtonType(AppVariables.message("Cancel"));
            alert.getButtonTypes().setAll(buttonSave, buttonNotSave, buttonCancel);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonSave) {
                saveAction();
                return true;
            } else if (result.get() == buttonNotSave) {
                return true;
            } else {
                return false;
            }
        }
    }

}
