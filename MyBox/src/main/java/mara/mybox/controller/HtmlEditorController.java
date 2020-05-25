package mara.mybox.controller;

import com.vladsch.flexmark.html2md.converter.*;
import com.vladsch.flexmark.util.data.MutableDataSet;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.net.URI;
import java.nio.charset.Charset;
import java.text.MessageFormat;
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
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
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
import javafx.scene.image.WritableImage;
import javafx.scene.input.DragEvent;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
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
import mara.mybox.data.BrowserHistory;
import mara.mybox.data.VisitHistory;
import mara.mybox.db.TableBrowserBypassSSL;
import mara.mybox.db.TableBrowserHistory;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlImageManufacture;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.fxml.RecentVisitMenu;
import mara.mybox.image.ImageManufacture;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.HtmlTools;
import mara.mybox.tools.NetworkTools;
import mara.mybox.tools.PdfTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.MyboxDataPath;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-7-31
 * @Description
 * @License Apache License Version 2.0
 */
public class HtmlEditorController extends BaseController {

    private final String HtmlImagePathKey, HtmlSnapDelayKey, HtmlLastUrlsKey, HtmlPdfPathKey;
    private WebEngine webEngine;
    private int delay, fontSize, orginalStageHeight, orginalStageY, orginalStageWidth;
    protected int lastHtmlLen, lastCodesLen;
    private boolean isOneImage;
    private URI uri;
    private List<File> snaps;
    private LoadingController loadingController;
    private float zoomScale;
    protected boolean loadSynchronously, isFrameSet, notChangedAfterLoad;
    protected SimpleBooleanProperty fileChanged;
    protected int cols, rows;
    protected int lastTextLen;
    protected SnapshotParameters snapParameters;
    protected int snapFileWidth, snapFileHeight, snapsTotal,
            snapImageWidth, snapImageHeight, snapTotalHeight, snapHeight, snapStep, dpi;
    protected double snapScale;

    @FXML
    private Button snapshotButton;
    @FXML
    private HTMLEditor htmlEditor;
    @FXML
    private WebView webView;
    @FXML
    private TextArea codesArea, markdownArea;
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab editorTab, codesTab, browserTab, markdownTab;
    @FXML
    private ComboBox<String> urlBox, delayBox, dpiSelector;
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

        sourceExtensionFilter = CommonFxValues.HtmlExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;

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
                        Object object;

                        if (editorTab.equals(oldValue)) {
                            if (isFrameSet) {
                                object = webEngine.executeScript("document.documentElement.outerHTML");
                            } else {
                                object = htmlEditor.getHtmlText();
                            }
                            if (object != null) {
                                contents = (String) object;
                                isFrameSet = contents.toUpperCase().contains("</FRAMESET>");
                                codesArea.setText(contents);
                                if (!isFrameSet) {
                                    webEngine.loadContent(contents);
                                }
                                html2markdown(contents);
                            }

                        } else if (codesTab.equals(oldValue)) {
                            object = codesArea.getText();
                            if (object != null) {
                                contents = (String) object;
                                isFrameSet = contents.toUpperCase().contains("</FRAMESET>");
                                if (isFrameSet) {
                                    htmlEditor.setHtmlText("<p>" + AppVariables.message("NotSupportFrameSet") + "</p>");
                                } else {
                                    htmlEditor.setHtmlText(contents);
                                    webEngine.loadContent(contents);
                                }
                                html2markdown(contents);
                            }

                        } else if (browserTab.equals(oldValue)) {
//                            object = webEngine.executeScript("document.documentElement.outerHTML");
//                            if (object != null) {
//                                contents = (String) object;
//                                isFrameSet = contents.toUpperCase().contains("</FRAMESET>");
//                                codesArea.setText(contents);
//                                if (isFrameSet) {
//                                    htmlEditor.setHtmlText("<p>" + AppVariables.message("NotSupportFrameSet") + "</p>");
//                                } else {
//                                    htmlEditor.setHtmlText(contents);
//                                }
//                                html2markdown(contents);
//                            }

                        } else if (markdownTab.equals(oldValue)) {

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
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_val, Boolean new_val) {
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
                public void changed(ObservableValue ov, String oldValue,
                        String newValue) {
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
            if (AppVariables.getUserConfigBoolean("SSLBypassAll", false)) {
                NetworkTools.trustAll();
            } else {
                NetworkTools.myBoxSSL();
            }

            urlBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov,
                        String oldValue, String newv) {
                    checkAddress();
                }
            });
            List<String> urls = TableBrowserHistory.recentBrowse();
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

            List<String> dpiValues = new ArrayList();
            dpiValues.addAll(Arrays.asList("96", "120", "160", "300"));
            String sValue = Toolkit.getDefaultToolkit().getScreenResolution() + "";
            if (dpiValues.contains(sValue)) {
                dpiValues.remove(sValue);
            }
            dpiValues.add(0, sValue);
            sValue = (int) Screen.getPrimary().getDpi() + "";
            if (dpiValues.contains(sValue)) {
                dpiValues.remove(sValue);
            }
            dpiValues.add(sValue);
            dpiSelector.getItems().addAll(dpiValues);
            dpiSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        try {
                            dpi = Integer.parseInt(newValue);
                            AppVariables.setUserConfigValue("HtmlSnapDPI", dpi + "");
                        } catch (Exception e) {
                            dpi = 96;
                        }
                    });
            dpiSelector.getSelectionModel().select(AppVariables.getUserConfigValue("HtmlSnapDPI", "96"));

            webEngine = webView.getEngine();
            webEngine.setJavaScriptEnabled(true);

            webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
                @Override
                public void changed(ObservableValue ov, State oldState,
                        State newState) {
                    try {
//                        logger.debug(newState.name() + " " + webEngine.getLocation());
                        snapBar.setDisable(true);
                        switch (newState) {
                            case READY:
                                bottomText.setText("");
                                break;
                            case SCHEDULED:
                                bottomText.setText("");
                                break;
                            case RUNNING:
                                bottomText.setText(AppVariables.message("Loading..."));
                                break;
                            case SUCCEEDED:
                                afterPageLoaded();
                                break;
                            case CANCELLED:
                                bottomText.setText(message("Canceled"));
                                if (loadingController != null) {
                                    loadingController.closeStage();
                                    loadingController = null;
                                }
                                break;
                            case FAILED:
                                bottomText.setText(message("Failed"));
                                if (loadingController != null) {
                                    loadingController.closeStage();
                                    loadingController = null;
                                }
                                break;
                            default:
                                bottomText.setText("");
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
//                    popError(event.getMessage());
//                    logger.debug(event.getMessage());
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
                public void changed(ObservableValue<? extends Throwable> ov,
                        Throwable ot, Throwable nt) {
                    if (nt == null) {
                        return;
                    }
                    try {
                        bottomText.setText(nt.getMessage());
                        if (nt.getMessage().contains("SSL handshake failed")) {
                            String host = new URI(webEngine.getLocation()).getHost();
                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                            alert.setTitle(getBaseTitle());
                            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                            stage.setAlwaysOnTop(true);
                            stage.toFront();
                            if (NetworkTools.isHostCertificateInstalled(host)) {
                                alert.setContentText(host + "\n" + message("SSLInstalledButFailed"));
                                ButtonType buttonBypass = new ButtonType(AppVariables.message("SSLVerificationByPass"));
                                ButtonType buttonCancel = new ButtonType(AppVariables.message("ISee"));
                                alert.getButtonTypes().setAll(buttonBypass, buttonCancel);
                                Optional<ButtonType> result = alert.showAndWait();
                                if (result.get() != buttonBypass) {
                                    return;
                                }
                                TableBrowserBypassSSL.write(host);
                                load();

                            } else {
                                alert.setContentText(host + "\n" + message("SSLCertificatesAskInstall"));
                                ButtonType buttonSure = new ButtonType(AppVariables.message("Sure"));
                                ButtonType buttonCancel = new ButtonType(AppVariables.message("Cancel"));
                                alert.getButtonTypes().setAll(buttonSure, buttonCancel);
                                Optional<ButtonType> result = alert.showAndWait();
                                if (result.get() == null || result.get() != buttonSure) {
                                    return;
                                }
                                String msg = NetworkTools.installCertificateByHost(host, host);
                                if (msg == null) {
                                    msg = host + "\n" + message("SSLCertificateInstalled");
                                }
                                alert.setContentText(msg);
                                ButtonType buttonISee = new ButtonType(AppVariables.message("ISee"));
                                alert.getButtonTypes().setAll(buttonISee);
                                alert.showAndWait();
                            }

                        }
                    } catch (Exception e) {
                        popError(e.toString());
                        logger.debug(e.toString());
                    }
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    protected void checkAddress() {
        try {
            String address = urlBox.getValue();
            if (isSettingValues || address == null || address.trim().isEmpty()) {
                return;
            }
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (address.startsWith("file:")) {
                            uri = new URI(address);
                        } else if (!address.startsWith("http")) {
                            uri = new URI("http://" + address);
                        } else {
                            uri = new URI(address);
                        }
                        if (uri != null) {
                            load();
                        } else {
                            urlBox.getEditor().setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        logger.error(e.toString());
                        urlBox.getEditor().setStyle(badStyle);
                    }
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void afterPageLoaded() {
        try {
            if (loadingController != null) {
                loadingController.closeStage();
                loadingController = null;
            }
            if (uri == null) {
                return;
            }
            bottomText.setText(AppVariables.message("Loaded"));
            snapBar.setDisable(false);

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
                    html2markdown(contents);
                } catch (Exception e) {
                    logger.debug(e.toString());
                }
                loadSynchronously = false;
            }

            if (notChangedAfterLoad) {
                fileChanged.set(false);
                getMyStage().setTitle(getBaseTitle());
                notChangedAfterLoad = false;
            }

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

            String address = uri.toString().toLowerCase();
            BrowserHistory his = new BrowserHistory();
            his.setAddress(address);
            his.setVisitTime(new Date().getTime());
            his.setTitle(webEngine.getTitle());
            File path = new File(MyboxDataPath + File.separator + "icons");
            if (!path.exists()) {
                path.mkdirs();
            }

            if (uri.getScheme().startsWith("http")) {
                File file = new File(MyboxDataPath + File.separator + "icons" + File.separator + uri.getHost() + ".png");
                if (!file.exists()) {
                    HtmlTools.readIcon(address, file);
                }
                if (file.exists()) {
                    his.setIcon(file.getAbsolutePath());
                } else {
                    his.setIcon("");
                }
            } else {
                his.setIcon("");
            }
            TableBrowserHistory.write(his);

            isSettingValues = true;
            urlBox.getItems().clear();
            List<String> urls = TableBrowserHistory.recentBrowse();
            if (!urls.isEmpty()) {
                urlBox.getItems().addAll(urls);
                urlBox.getEditor().setText(address);
            }
            isSettingValues = false;
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void load() {
        try {
            isFrameSet = false;
            loadSynchronously = true;
            notChangedAfterLoad = true;
            webEngine.getLoadWorker().cancel();
            if (uri == null) {
                return;
            }
            bottomText.setText(AppVariables.message("Loading..."));
            webEngine.load(uri.toString());

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
        urlBox.setValue(link);
    }

    public void loadLink(File file) {
        sourceFile = file;
        loadLink(file.toURI().toString());
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
            try ( BufferedWriter out = new BufferedWriter(new FileWriter(sourceFile, Charset.forName("utf-8"), false))) {
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
            try ( BufferedWriter out = new BufferedWriter(new FileWriter(sourceFile, Charset.forName("utf-8"), false))) {
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
                    null, CommonFxValues.ImageExtensionFilter, true);
        } else {
            file = chooseSaveFile(AppVariables.getUserConfigPath(HtmlPdfPathKey),
                    null, CommonFxValues.PdfExtensionFilter, true);
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
//                        logger.debug(" TimeOver:" + newHeight);
                        quit = true;
                    }
                    if (newHeight == lastHeight) {
//                        logger.debug(" Complete:" + newHeight);
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
                    Platform.runLater(() -> {
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
                    });

                }
            }, 0, delay);

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    private void startSnap() {
        try {
            webEngine.executeScript("window.scrollTo(0,0 );");
            snapTotalHeight = (Integer) webEngine.executeScript("document.body.scrollHeight");
            snapStep = (Integer) webEngine.executeScript("document.documentElement.clientHeight < document.body.clientHeight ? document.documentElement.clientHeight : document.body.clientHeight");
            snapHeight = 0;
            bottomText.setText(AppVariables.message("SnapingImage..."));

            // http://news.kynosarges.org/2017/02/01/javafx-snapshot-scaling/
            final Bounds bounds = webView.getLayoutBounds();
            snapScale = dpi / Screen.getPrimary().getDpi();
            snapScale = snapScale > 1 ? snapScale : 1;
            snapImageWidth = (int) Math.round(bounds.getWidth() * snapScale);
            snapImageHeight = (int) Math.round(bounds.getHeight() * snapScale);
            snapParameters = new SnapshotParameters();
            snapParameters.setFill(Color.TRANSPARENT);
            snapParameters.setTransform(javafx.scene.transform.Transform.scale(snapScale, snapScale));

            snaps = new ArrayList<>();
            snapsTotal = snapTotalHeight % snapStep == 0
                    ? snapTotalHeight / snapStep : snapTotalHeight / snapStep + 1;
            snapFileWidth = snapFileHeight = 0;

            if (timer != null) {
                timer.cancel();
            }
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        snap();
                    });
                }
            }, 2000);    // make sure page is loaded before snapping

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void snap() {
        try {
            if (loadingController == null) {
                return;
            }
            WritableImage snapshot = new WritableImage(snapImageWidth, snapImageHeight);
            snapshot = webView.snapshot(snapParameters, snapshot);
            Image cropped;
            if (snapTotalHeight < snapHeight + snapStep) { // last snap
                cropped = FxmlImageManufacture.cropOutsideFx(snapshot, 0,
                        (int) ((snapStep + snapHeight - snapTotalHeight) * snapScale),
                        (int) snapshot.getWidth() - 1, (int) snapshot.getHeight() - 1);
            } else {
                cropped = snapshot;
            }
            if (cropped.getWidth() > snapFileWidth) {
                snapFileWidth = (int) cropped.getWidth();
            }
            snapFileHeight += cropped.getHeight();
            snapHeight += snapStep;
            File tmpfile = FileTools.getTempFile(".png");
            ImageFileWriters.writeImageFile(SwingFXUtils.fromFXImage(cropped, null), "png", tmpfile.getAbsolutePath());
            snaps.add(tmpfile);
            loadingController.setInfo(AppVariables.message("CurrentPageHeight") + ": " + snapHeight);
            if (snapTotalHeight > snapHeight) {
                webEngine.executeScript("window.scrollTo(0, " + snapHeight + ");");
                if (timer != null) {
                    timer.cancel();
                }
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> {
                            snap();
                        });
                    }
                }, 300);    // make sure page is loaded before snapping

            } else { // last snap
                loadingController.setInfo(AppVariables.message("WritingFile"));
                boolean success = true;
                if (isOneImage) {
                    Runtime r = Runtime.getRuntime();
                    long availableMem = r.maxMemory() - (r.totalMemory() - r.freeMemory()) / (1024 * 1024L);
                    long requiredMem = snapFileWidth * snapFileHeight * 5L / (1024 * 1024) + 200;
                    if (availableMem < requiredMem) {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle(getBaseTitle());
                        alert.setContentText(MessageFormat.format(AppVariables.message("MergedSnapshotTooLarge"), availableMem, requiredMem));
                        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                        ButtonType buttonPdf = new ButtonType(AppVariables.message("SaveAsPdf"));
                        ButtonType buttonCancel = new ButtonType(AppVariables.message("Cancel"));
                        alert.getButtonTypes().setAll(buttonPdf, buttonCancel);
                        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                        stage.setAlwaysOnTop(true);
                        stage.toFront();

                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.get() == buttonPdf) {
                            success = PdfTools.htmlIntoPdf(snaps, targetFile, windowSizeCheck.isSelected());
                        } else {
                            success = false;
                        }
                    } else {
                        BufferedImage finalImage = ImageManufacture.mergeImagesVertical(snaps, snapFileWidth, snapFileHeight);
                        if (finalImage != null) {
                            String format = FileTools.getFileSuffix(targetFile.getAbsolutePath());
                            ImageFileWriters.writeImageFile(finalImage, format, targetFile.getAbsolutePath());
                        } else {
                            success = false;
                        }
                    }
                } else {
                    success = PdfTools.htmlIntoPdf(snaps, targetFile, windowSizeCheck.isSelected());
                }
                snaps = null;
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
            }

        } catch (Exception e) {
            webEngine.executeScript("window.scrollTo(0,0 );");
            popFailed();
            if (loadingController != null) {
                loadingController.closeStage();
                loadingController = null;
            }
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

    /*
        Markdown
     */
    protected void html2markdown(String contents) {
        if (contents == null || contents.isEmpty()) {
            markdownArea.setText("");
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                private String md;

                @Override
                protected boolean handle() {
                    try {
                        MutableDataSet options = new MutableDataSet();
                        md = FlexmarkHtmlConverter.builder(options).build().convert(contents);
                        return md != null;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    markdownArea.setText(md);
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    protected void editMarkdown() {
        if (markdownArea.getText().isEmpty()) {
            return;
        }
        MarkdownEditerController controller
                = (MarkdownEditerController) openStage(CommonValues.MarkdownEditorFxml);
        controller.loadMarkdown(markdownArea.getText());
    }

    @FXML
    protected void refreshMarkdown() {
        html2markdown(codesArea.getText());
    }

    @FXML
    protected void saveMarkdown() {
        if (markdownArea.getText().isEmpty()) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }

            String name = "";
            if (sourceFile != null) {
                name = FileTools.getFilePrefix(sourceFile.getName());
            }
            final File file = chooseSaveFile(AppVariables.getUserConfigPath("MarkdownFilePath"),
                    name, CommonFxValues.MarkdownExtensionFilter, true);
            if (file == null) {
                return;
            }
            recordFileWritten(file, "MarkdownFilePath",
                    VisitHistory.FileType.Markdown, VisitHistory.FileType.Markdown);

            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    return FileTools.writeFile(file, markdownArea.getText()) != null;
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    public void popSaveMarkdown(MouseEvent event) { //
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                return null;
            }

            @Override
            public List<VisitHistory> recentPaths() {
                return VisitHistory.getRecentPath(VisitHistory.FileType.Markdown);
            }

            @Override
            public void handleSelect() {
                saveMarkdown();
            }

            @Override
            public void handleFile(String fname) {

            }

            @Override
            public void handlePath(String fname) {
                File file = new File(fname);
                if (!file.exists()) {
                    handleSelect();
                    return;
                }
                AppVariables.setUserConfigValue("MarkdownFilePath", fname);
                handleSelect();
            }

        }.pop();
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
            } else {
                return result.get() == buttonNotSave;
            }
        }
    }

    /*
        get/set
     */
    public boolean isNotChangedAfterLoad() {
        return notChangedAfterLoad;
    }

    public void setNotChangedAfterLoad(boolean notChangedAfterLoad) {
        this.notChangedAfterLoad = notChangedAfterLoad;
    }

}
