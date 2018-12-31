package mara.mybox.controller;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringWriter;
import java.net.URL;
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
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
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
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebErrorEvent;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import static mara.mybox.objects.AppVaribles.logger;
import mara.mybox.db.TableBrowserUrls;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.objects.CommonValues;
import mara.mybox.tools.FileTools;
import static mara.mybox.fxml.FxmlTools.badStyle;
import mara.mybox.fxml.FxmlImageTools;
import mara.mybox.fxml.FxmlTools;
import mara.mybox.tools.NetworkTools;
import static mara.mybox.tools.NetworkTools.checkWeiboPassport;
import mara.mybox.tools.PdfTools;

/**
 * @Author Mara
 * @CreateDate 2018-7-31
 * @Description
 * @License Apache License Version 2.0
 */
public class HtmlEditorController extends BaseController {

    private final String HtmlFilePathKey, HtmlImagePathKey, HtmlSnapDelayKey, HtmlLastUrlsKey, HtmlPdfPathKey;
    private final String WeiBoPassportChecked;
    private WebEngine webEngine;
    private int delay, fontSize, orginalStageHeight, orginalStageY, orginalStageWidth;
    protected int lastHtmlLen, lastCodesLen, snapHeight, snapCount;
    private boolean isOneImage, isLoadingWeiboPassport;
    private URL url;
    private List<Image> images;
    private File targetFile;
    protected SimpleBooleanProperty loadedCompletely;
    private List<String> urls;
    private Stage snapingStage;
    private LoadingController loadingController;
    private float zoomScale;
    protected boolean isSettingValues;
    protected SimpleBooleanProperty fileChanged;
    protected int cols, rows;
    protected int lastTextLen;

    @FXML
    private Button loadButton, updateEditorButton, snapsotButton;
    @FXML
    private HTMLEditor htmlEdior;
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

        HtmlFilePathKey = "HtmlFilePathKey";
        HtmlImagePathKey = "HtmlImagePathKey";
        HtmlSnapDelayKey = "HtmlSnapDelayKey";
        HtmlLastUrlsKey = "HtmllastUrlKey";
        HtmlPdfPathKey = "HtmlPdfPathKey";
        WeiBoPassportChecked = "WeiBoPassportChecked";

        fileExtensionFilter = new ArrayList() {
            {
                add(new FileChooser.ExtensionFilter("html", "*.html", "*.htm"));
            }
        };

    }

    private boolean isPasteEvent(KeyEvent event) {
        return event.isShortcutDown() && event.getCode() == KeyCode.V;
    }

    @Override
    protected void initializeNext() {
        try {

            lastCodesLen = lastHtmlLen = 0;
            isSettingValues = false;
            fontSize = 14;
            zoomScale = 1.0f;

            initHtmlEdtior();
            initCodeEdtior();
            initBroswer();

            tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
                @Override
                public void changed(ObservableValue<? extends Tab> observable,
                        Tab oldValue, Tab newValue) {
                    if (fileChanged.get()) {
                        isSettingValues = true;
                        if (newValue.equals(editorTab)) {
                            htmlEdior.setHtmlText(codesArea.getText());
                        } else if (newValue.equals(codesTab)) {
                            codesArea.setText(htmlEdior.getHtmlText());
                        }
                        isSettingValues = false;
                    }
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
            htmlEdior.addEventHandler(DragEvent.DRAG_EXITED, new EventHandler<InputEvent>() { // work
                @Override
                public void handle(InputEvent event) {
                    checkHtmlEditorChanged();
                }
            });
            htmlEdior.setOnKeyReleased(new EventHandler<KeyEvent>() {
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
        int len = htmlEdior.getHtmlText().length();
//        logger.debug(isSettingValues + "  " + len + " " + lastHtmlLen);
        if (!isSettingValues && len != lastHtmlLen) {
            fileChanged.set(true);
        }
        lastHtmlLen = len;
        bottomText.setText(AppVaribles.getMessage("Total") + ": " + len);
    }

    protected void initCodeEdtior() {
        try {
            codesArea.setOnKeyReleased(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent event) {
                    int len = codesArea.getText().length();
                    if (!isSettingValues && len != lastTextLen) {
                        fileChanged.set(true);
                    }
                    lastCodesLen = len;
                    bottomText.setText(AppVaribles.getMessage("Total") + ": " + len);
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    protected void initBroswer() {
        try {

            urlBox.valueProperty().addListener(new ChangeListener<String>() {
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
            delayBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov,
                        String oldValue, String newValue) {
                    try {
                        delay = Integer.valueOf(newValue);
                        if (delay > 0) {
                            delayBox.getEditor().setStyle(null);
                            delay = delay * 1000;
                            AppVaribles.setUserConfigValue(HtmlSnapDelayKey, newValue);
                        } else {
                            delay = 2000;
                            delayBox.getEditor().setStyle(badStyle);
                        }

                    } catch (Exception e) {
                        delay = 2000;
                        delayBox.getEditor().setStyle(badStyle);
                    }
                }
            });
            delayBox.getSelectionModel().select(AppVaribles.getUserConfigValue(HtmlSnapDelayKey, "2"));

            snapGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkOneImage();
                }
            });
            checkOneImage();

            Tooltip tips = new Tooltip(getMessage("htmlSnapComments"));
            tips.setFont(new Font(16));
            FxmlTools.setComments(snapBar, tips);

            webEngine = webView.getEngine();
            webEngine.setJavaScriptEnabled(true);
            webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
                @Override
                public void changed(ObservableValue ov, State oldState, State newState) {
//                    logger.debug(newState.name());
                    snapBar.setDisable(true);
                    switch (newState) {
                        case READY:
                            bottomText.setText("");
                            loadButton.setText(getMessage("Load"));
                            break;
                        case SCHEDULED:
                            bottomText.setText("");
                            loadButton.setText(getMessage("Stop"));
                            break;
                        case RUNNING:
                            bottomText.setText(AppVaribles.getMessage("Loading..."));
                            loadButton.setText(getMessage("Stop"));
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
                                            AppVaribles.setUserConfigValue("WeiboPassportChecked", "true");
                                            logger.debug(checkWeiboPassport());
                                            Platform.runLater(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (checkWeiboPassport()) {
                                                        webEngine.load(url.toString());
                                                        if (loadingController != null && loadingController.getMyStage() != null) {
                                                            loadingController.getMyStage().close();
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
                                                            if (loadingController != null && loadingController.getMyStage() != null) {
                                                                loadingController.getMyStage().close();
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
                                    String s = (String) webEngine.executeScript("getComputedStyle(document.body).fontSize");
                                    fontSize = Integer.valueOf(s.substring(0, s.length() - 2));
                                } catch (Exception e) {
                                    fontSize = 14;
                                }
                                bottomText.setText(AppVaribles.getMessage("Loaded"));
                                if (loadingController != null && loadingController.getMyStage() != null) {
                                    loadingController.getMyStage().close();
                                }
                                loadButton.setText(getMessage("Load"));
                                snapBar.setDisable(false);
                            }

                            break;
                        case CANCELLED:
                            bottomText.setText(getMessage("Canceled"));
                            loadButton.setText(getMessage("Load"));
                            break;
                        case FAILED:
                            bottomText.setText(getMessage("Failed"));
                            loadButton.setText(getMessage("Load"));
                            break;
                        default:
                            bottomText.setText("");
                            loadButton.setText(getMessage("Load"));
                            break;
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
                    logger.debug("Received exception: " + t1.getMessage());
                }
            });
//            loadedCompletely = new SimpleBooleanProperty(false);
//            loadedCompletely.addListener(new ChangeListener<Boolean>() {
//                @Override
//                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
//                    if (loadedCompletely.getValue()) {
//                    } else {
//
//                    }
//
//                }
//            });
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    private void checkOneImage() {
        RadioButton selected = (RadioButton) snapGroup.getSelectedToggle();
        if (AppVaribles.getMessage("OneImage").equals(selected.getText())) {
            isOneImage = true;
            windowSizeCheck.setDisable(true);
        } else {
            isOneImage = false;
            windowSizeCheck.setDisable(false);
        }
    }

    @FXML
    protected void openAction() {
        try {
            if (!checkSavingForNextAction()) {
                return;
            }
            isSettingValues = true;
//            sourceFile = null;
//            htmlEdior.setHtmlText("");
            final FileChooser fileChooser = new FileChooser();
            File path = new File(AppVaribles.getUserConfigValue(HtmlFilePathKey, CommonValues.UserFilePath));
            fileChooser.setInitialDirectory(path);
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
            final File file = fileChooser.showOpenDialog(getMyStage());
            if (file == null) {
                return;
            }
            AppVaribles.setUserConfigValue(LastPathKey, file.getParent());
            AppVaribles.setUserConfigValue(HtmlFilePathKey, file.getParent());
            sourceFile = file;

            StringBuilder contents = new StringBuilder();
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
                String lineTxt;
                cols = 0;
                rows = 0;
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    if (lineTxt.length() > rows) {
                        rows = lineTxt.length();
                    }
                    cols++;
                    contents.append(lineTxt).append(System.getProperty("line.separator"));
                }
            }
            final String conString = contents.toString();
            htmlEdior.setHtmlText(conString);
            codesArea.setText(conString);
            webEngine.loadContent(conString);

            lastCodesLen = lastHtmlLen = contents.length();
            fileChanged.set(false);
            getMyStage().setTitle(getBaseTitle() + "  " + sourceFile.getAbsolutePath());
            isSettingValues = false;

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    private void loadAction(ActionEvent event) {
        if (loadButton.getText().equals(getMessage("Stop"))) {
            webEngine.getLoadWorker().cancel();
            return;
        }
        isLoadingWeiboPassport = false;
        loadingController = openHandlingStage(Modality.NONE, AppVaribles.getMessage("Loading..."));
        bottomText.setText(AppVaribles.getMessage("Loading..."));
        try {
            webEngine.getLoadWorker().cancel();
            Thread.sleep(1000);
            String urlString = url.toString();
            if (urlString.contains("weibo.com/") && !checkWeiboPassport()) {
                isLoadingWeiboPassport = true;
                bottomText.setText(AppVaribles.getMessage("LoadingWeiboCertificate"));
                logger.debug(AppVaribles.getMessage("LoadingWeiboCertificate"));
                loadingController.setInfo(AppVaribles.getMessage("LoadingWeiboCertificate"));
                webEngine.load("https://passport.weibo.com/visitor/visitor?entry=miniblog");
            } else {
                webEngine.load(urlString);
            }

        } catch (Exception e) {
            logger.error(e.toString());
            if (loadingController != null && loadingController.getMyStage() != null) {
                loadingController.getMyStage().close();
            }
        }

    }

    @FXML
    private void updateBrowser(ActionEvent event) {
        webEngine.loadContent(htmlEdior.getHtmlText());
//        Platform.runLater(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    logger.debug("updateBrowser");
//
//                } catch (Exception e) {
//                    logger.error(e.toString());
//                }
//            }
//        });

    }

    @FXML
    private void updateEditor() {
        try {
            if (!checkSavingForNextAction()) {
                return;
            }

//            String contents = getBrowserContents();
//            logger.debug(contents.length());
            String contents = (String) webEngine.executeScript("document.documentElement.outerHTML");

            htmlEdior.setHtmlText(contents);
            codesArea.setText(contents);
            fileChanged.set(true);
        } catch (Exception e) {
            logger.debug(e.toString());
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
    protected void saveAction() {
        try {
            isSettingValues = true;
            if (sourceFile == null) {
                final FileChooser fileChooser = new FileChooser();
                File path = new File(AppVaribles.getUserConfigValue(HtmlFilePathKey, CommonValues.UserFilePath));
                fileChooser.setInitialDirectory(path);
                fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
                final File file = fileChooser.showSaveDialog(getMyStage());
                if (file == null) {
                    return;
                }
                AppVaribles.setUserConfigValue(LastPathKey, file.getParent());
                AppVaribles.setUserConfigValue(HtmlFilePathKey, file.getParent());
                sourceFile = file;
            }
            String contents;
            if (tabPane.getSelectionModel().getSelectedItem().equals(codesTab)) {
                contents = codesArea.getText();
            } else {
                contents = htmlEdior.getHtmlText();
            }
            try (BufferedWriter out = new BufferedWriter(new FileWriter(sourceFile, false))) {
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
    protected void saveAsAction() {
        try {
            isSettingValues = true;
            final FileChooser fileChooser = new FileChooser();
            File path = new File(AppVaribles.getUserConfigValue(HtmlFilePathKey, CommonValues.UserFilePath));
            fileChooser.setInitialDirectory(path);
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
            final File file = fileChooser.showSaveDialog(getMyStage());
            if (file == null) {
                return;
            }
            AppVaribles.setUserConfigValue(LastPathKey, file.getParent());
            AppVaribles.setUserConfigValue(HtmlFilePathKey, file.getParent());
            sourceFile = file;
            String contents;
            if (AppVaribles.getMessage("Editor").equals(tabPane.getSelectionModel().getSelectedItem().getText())) {
                contents = htmlEdior.getHtmlText();
            } else {
                contents = codesArea.getText();
            }
            try (BufferedWriter out = new BufferedWriter(new FileWriter(sourceFile, false))) {
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
    protected void createAction() {
        try {
            isSettingValues = true;
            sourceFile = null;
            htmlEdior.setHtmlText("");
            codesArea.setText("");
            lastCodesLen = lastHtmlLen = 0;
            fileChanged.set(false);
            getMyStage().setTitle(getBaseTitle());
            isSettingValues = false;
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    private void snapshot(ActionEvent event) {
        final FileChooser fileChooser = new FileChooser();
        File path;
        if (isOneImage) {
            path = new File(AppVaribles.getUserConfigValue(HtmlImagePathKey, CommonValues.UserFilePath));
            fileChooser.getExtensionFilters().addAll(CommonValues.ImageExtensionFilter);
        } else {
            path = new File(AppVaribles.getUserConfigValue(HtmlPdfPathKey, CommonValues.UserFilePath));
            fileChooser.getExtensionFilters().addAll(CommonValues.PdfExtensionFilter);
        }
        fileChooser.setInitialDirectory(path);
        final File file = fileChooser.showSaveDialog(getMyStage());
        if (file == null) {
            return;
        }
        AppVaribles.setUserConfigValue(LastPathKey, file.getParent());
        if (isOneImage) {
            AppVaribles.setUserConfigValue(HtmlImagePathKey, file.getParent());
        } else {
            AppVaribles.setUserConfigValue(HtmlPdfPathKey, file.getParent());
        }
        targetFile = file;
        images = new ArrayList();

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

            loadedCompletely.set(false);
            snapsotButton.setDisable(true);
            final int maxDelay = delay * 30;
            final long startTime = new Date().getTime();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(CommonValues.LoadingFxml), AppVaribles.CurrentBundle);
            Pane pane = fxmlLoader.load();
            loadingController = fxmlLoader.getController();
            snapingStage = new Stage();
            snapingStage.initModality(Modality.WINDOW_MODAL);
            snapingStage.initStyle(StageStyle.TRANSPARENT);
            snapingStage.initOwner(getMyStage());
            snapingStage.setScene(new Scene(pane));
            snapingStage.show();

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
                                loadingController.setInfo(AppVaribles.getMessage("CurrentPageHeight") + ": " + newHeight);
                                logger.debug(lastHeight + "  newHeight:" + newHeight);
                                if (newHeight == lastHeight) {
                                    loadingController.setInfo(AppVaribles.getMessage("ExpandingPage"));
                                    startSnap();
                                } else {
                                    webEngine.executeScript("window.scrollTo(0," + newHeight + ");");
                                }

                            } catch (Exception e) {
                                logger.error(e.toString());
                                if (snapingStage != null) {
                                    snapingStage.close();
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
            bottomText.setText(AppVaribles.getMessage("SnapingImage..."));
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
                                        snapshot = FxmlImageTools.cropImage(snapshot, 0, snapStep - (totalHeight - snapHeight),
                                                width - 1, (int) snapshot.getHeight() - 1);
                                    } else {
                                        snapshot = FxmlImageTools.cropImage(snapshot, 0, 0,
                                                width - 1, (int) snapshot.getHeight() - 1);
                                    }
                                    images.add(snapshot);
                                    snapHeight += snapStep;
                                    loadingController.setInfo(AppVaribles.getMessage("CurrentPageHeight") + ": " + snapHeight);
                                    if (totalHeight <= snapHeight) { // last snap

                                        loadingController.setInfo(AppVaribles.getMessage("WritingFile"));
                                        boolean success = true;
                                        if (isOneImage) {
                                            Image finalImage = FxmlImageTools.combineSingleColumn(images);
                                            if (finalImage != null) {
                                                String format = FileTools.getFileSuffix(targetFile.getAbsolutePath());
                                                final BufferedImage bufferedImage = FxmlImageTools.getBufferedImage(finalImage);
                                                ImageFileWriters.writeImageFile(bufferedImage, format, targetFile.getAbsolutePath());
                                            } else {
                                                success = false;
                                            }
                                        } else {
                                            success = PdfTools.htmlIntoPdf(images, targetFile, windowSizeCheck.isSelected());
                                        }
                                        if (success && targetFile.exists()) {
                                            if (isOneImage) {
                                                openImageManufactureInNew(targetFile.getAbsolutePath());
                                            } else {
                                                Desktop.getDesktop().browse(targetFile.toURI());
                                            }
                                        } else {
                                            popError(AppVaribles.getMessage("Failed"));
                                        }

                                        webEngine.executeScript("window.scrollTo(0,0 );");
                                        bottomText.setText("");
                                        snapsotButton.setDisable(false);

                                        if (snapingStage != null) {
                                            snapingStage.close();
                                        }
                                        myStage.setY(orginalStageY);
                                        myStage.setHeight(orginalStageHeight);
                                    } else {
                                        webEngine.executeScript("window.scrollTo(0, " + snapHeight + ");");
                                    }
                                } catch (Exception e) {
                                    logger.error(e.toString());
                                    webEngine.executeScript("window.scrollTo(0,0 );");
                                    popError(AppVaribles.getMessage("Failed"));
                                    if (snapingStage != null) {
                                        snapingStage.close();
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
        webEngine.executeScript("window.history.back();");
    }

    @FXML
    private void forwardAction(ActionEvent event) {
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
            isSettingValues = true;
            urlBox.setValue("https://weibo.com");
            isSettingValues = false;
            webEngine.load("https://weibo.com");
            popInformation(getMessage("WeiboAfterLogin"), -1);
        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    public void loadHtml(File file) {
        webEngine.load(file.toURI().toString());

    }

    @Override
    public boolean stageReloading() {
//        logger.debug("stageReloading");
        return checkSavingForNextAction();
    }

    @Override
    public boolean stageClosing() {
        super.stageClosing();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    webEngine.getLoadWorker().cancel();
                } catch (Exception e) {
                    logger.error(e.toString());
                }
            }
        });
        if (snapingStage != null) {
            snapingStage.close();
        }
        if (timer != null) {
            timer.cancel();
        }
        loadingController = null;
        return true;
    }

    public boolean checkSavingForNextAction() {
//        logger.debug(fileChanged.getValue());

        if (fileChanged == null || !fileChanged.getValue()) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(AppVaribles.getMessage("FileChanged"));
            ButtonType buttonSave = new ButtonType(AppVaribles.getMessage("Save"));
            ButtonType buttonNotSave = new ButtonType(AppVaribles.getMessage("NotSave"));
            ButtonType buttonCancel = new ButtonType(AppVaribles.getMessage("Cancel"));
            alert.getButtonTypes().setAll(buttonSave, buttonNotSave, buttonCancel);

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
