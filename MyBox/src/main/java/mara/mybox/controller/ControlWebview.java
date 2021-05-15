package mara.mybox.controller;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.web.PromptData;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebErrorEvent;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Callback;
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.tools.HtmlTools;
import mara.mybox.tools.NetworkTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;
import org.jsoup.Jsoup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

/**
 * @Author Mara
 * @CreateDate 2021-3-5
 * @License Apache License Version 2.0
 */
public class ControlWebview extends BaseController {

    protected WebEngine webEngine;
    protected EventListener linkListener, frameLinkListener;
    protected double linkX, linkY;
    protected float zoomScale;
    protected String address;
    protected Charset charset;
    protected Document doc;
    protected Map<Integer, Document> frameDoc;
    protected final SimpleIntegerProperty stateNotify;

    public static final int TmpState = -9;
    public static final int NoDoc = -3;
    public static final int DocLoading = -2;
    public static final int DocLoaded = -1;

    @FXML
    protected WebView webView;
    @FXML
    protected Button backwardButton, forwardButton;
    @FXML
    protected FlowPane buttonsPane;

    public ControlWebview() {
        baseTitle = AppVariables.message("Html");
        stateNotify = new SimpleIntegerProperty(NoDoc);
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            webEngine = webView.getEngine();
            linkX = linkY = -1;
            zoomScale = 1.0f;
            frameDoc = new HashMap<>();
            charset = Charset.defaultCharset();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    // Listener should ignore state change from TmpState
    public void changeState(int newState) {
        synchronized (stateNotify) {
            if (stateNotify.get() == newState) { // make sure state will change
                stateNotify.set(TmpState);
            }
            stateNotify.set(newState);
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            initWebView();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    public void initWebView() {
        try {
            webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
                @Override
                public void changed(ObservableValue ov, State oldState, State newState) {
                    switch (newState) {
                        case RUNNING:
                            pageIsLoading();
                            break;
                        case SUCCEEDED:
                            afterPageLoaded();
                            break;
                        case CANCELLED:
                            bottomLabel.setText(message("Canceled"));
                            break;
                        case FAILED:
                            bottomLabel.setText(message("Failed"));
                            break;
                        default:
                            bottomLabel.setText(newState.name());
                    }
                }
            });

            // http://blogs.kiyut.com/tonny/2013/07/30/javafx-webview-addhyperlinklistener/
            linkListener = new EventListener() {
                @Override
                public void handleEvent(org.w3c.dom.events.Event ev) {

                    String domEventType = ev.getType();
                    Element element = (Element) ev.getTarget();
                    String tag = element.getTagName();
                    if (tag == null) {
                        return;
                    }
                    ev.preventDefault();
                    String href = null;
                    if (tag.equalsIgnoreCase("a")) {
                        href = element.getAttribute("href");
                    } else if (tag.equalsIgnoreCase("img")) {
                        href = element.getAttribute("src");
                    }
                    if ("mouseover".equals(domEventType)) {
                        bottomLabel.setText(href != null ? URLDecoder.decode(href, charset) : null);
                    } else if ("mouseout".equals(domEventType)) {
                        bottomLabel.setText("");
                    } else if (href != null && ("click".equals(domEventType) || "contextmenu".equals(domEventType))) {
                        timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Platform.runLater(() -> {
                                    popLinkMenu(element);
                                });
                            }
                        }, 100);
                    }
                }
            };

            frameLinkListener = new EventListener() {
                @Override
                public void handleEvent(org.w3c.dom.events.Event ev) {
                    String domEventType = ev.getType();
                    Element element = (Element) ev.getTarget();
                    String href = element.getAttribute("href");
                    String finalHref = href != null ? URLDecoder.decode(href, charset) : null;
                    if ("mouseover".equals(domEventType)) {
                        bottomLabel.setText(finalHref);
                    } else if ("mouseout".equals(domEventType)) {
                        bottomLabel.setText("");
                    } else if (finalHref != null && "click".equals(domEventType)) {
                        String target = element.getAttribute("target");
                        if (target != null && !target.equalsIgnoreCase("_blank")) {
                            webEngine.executeScript("if ( window.frames." + target + ".document.readyState==\"complete\") alert('FrameReadyName-" + target + "');");
                            webEngine.executeScript("window.frames." + target + ".document.onreadystatechange = "
                                    + "function(){ if ( window.frames." + target + ".document.readyState==\"complete\") alert('FrameReadyName-" + target + "'); }");
                        }
                    }
                }
            };

            webView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (popMenu != null) {
                        popMenu.hide();
                    }
                    linkX = mouseEvent.getScreenX();
                    linkY = mouseEvent.getScreenY();
                }
            });

            webEngine.getLoadWorker().exceptionProperty().addListener(new ChangeListener<Throwable>() {
                @Override
                public void changed(ObservableValue<? extends Throwable> ov, Throwable ot, Throwable nt) {
                    if (nt == null) {
                        return;
                    }
                    try {
                        bottomLabel.setText(nt.getMessage());
                        // https://stackoverflow.com/questions/3964703/can-i-add-a-new-certificate-to-the-keystore-without-restarting-the-jvm?r=SearchResults
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
                                ButtonType buttonCancel = new ButtonType(AppVariables.message("ISee"));
                                alert.getButtonTypes().setAll(buttonCancel);
                                alert.showAndWait();

                            } else if (AppVariables.getUserConfigBoolean("AskInstallCert" + host, true)) {
                                alert.setContentText(host + "\n" + message("SSLCertificatesAskInstall"));
                                ButtonType buttonSure = new ButtonType(message("Sure"));
                                ButtonType buttonCancel = new ButtonType(message("Cancel"));
                                ButtonType buttonNoAsk = new ButtonType(message("NotAskAnyMore"));
                                alert.getButtonTypes().setAll(buttonSure, buttonNoAsk, buttonCancel);
                                Optional<ButtonType> result = alert.showAndWait();
                                if (result.get() == buttonNoAsk) {
                                    AppVariables.setUserConfigValue("AskInstallCert" + host, false);
                                    return;
                                } else if (result.get() == buttonCancel) {
                                    return;
                                }
                                String msg = NetworkTools.installCertificateByHost(host, false);
                                if (msg == null) {
                                    msg = host + "\n" + message("SSLCertificateInstalled");
                                }
                                alert.setContentText(msg);
                                ButtonType buttonISee = new ButtonType(AppVariables.message("ISee"));
                                alert.getButtonTypes().setAll(buttonISee);
                                alert.showAndWait();
                            } else {
                                alertError(nt.getMessage());
                            }

                        }
                    } catch (Exception e) {
                        popError(e.toString());
                        MyBoxLog.debug(e.toString());
                    }
                }
            });

            webEngine.setOnStatusChanged(new EventHandler<WebEvent<String>>() {
                @Override
                public void handle(WebEvent<String> ev) {
//                    javafx.event.EventTarget t = ev.getTarget();
//                    MyBoxLog.console("here:" + ev.getData());
//                    bottomLabel.setText(ev.getData());
                }
            });

            webEngine.locationProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldv, String newv) {
                    bottomLabel.setText(URLDecoder.decode(newv, charset));
                }
            });

            webEngine.setPromptHandler(new Callback< PromptData, String>() {
                @Override
                public String call(PromptData p) {
                    MyBoxLog.console("here:" + p.getMessage());
                    String value = FxmlControl.askValue(baseTitle, null, p.getMessage(), p.getDefaultValue());
                    return value;
                }
            });

            webEngine.setConfirmHandler(new Callback< String, Boolean>() {
                @Override
                public Boolean call(String message) {
                    try {
                        MyBoxLog.console("here:" + message);
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle(myStage.getTitle());
                        alert.setHeaderText(null);
                        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                        alert.getDialogPane().setContent(new Label(message));
                        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                        stage.setAlwaysOnTop(true);
                        stage.toFront();
                        alert.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
                        Optional<ButtonType> result = alert.showAndWait();
                        return result.get() == ButtonType.YES;
                    } catch (Exception e) {
                        MyBoxLog.error(e.toString());
                        return false;
                    }

                }
            });

            webEngine.setOnAlert(new EventHandler<WebEvent<String>>() {
                @Override
                public void handle(WebEvent<String> ev) {
                    String msg = ev.getData();
                    int value = -1;
                    if (msg.startsWith("FrameReadyIndex-")) {
                        value = Integer.parseInt(msg.substring("FrameReadyIndex-".length()));
//                        MyBoxLog.console("Frame " + index + " ready");
                    } else if (msg.startsWith("FrameReadyName-")) {
                        value = HtmlTools.frameIndex(webEngine, msg.substring("FrameReadyName-".length()));
                    }
                    if (value < 0) {
                        return;
                    }
                    int frameIndex = value;
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            Platform.runLater(() -> {
                                addFrameLinksListener(frameIndex);
                                changeState(frameIndex);
                            });
                        }
                    }, 500);
                }
            });

            webEngine.setOnError(new EventHandler<WebErrorEvent>() {

                @Override
                public void handle(WebErrorEvent event) {
//                    popError(event.getMessage());
                    MyBoxLog.debug(event.getMessage());
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setValues(BaseController parent) {
        try {
            this.parentController = parent;
            this.baseName = parent.baseName;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void pageIsLoading() {
        bottomLabel.setText(AppVariables.message("Loading..."));
        changeState(DocLoading);
    }

    protected void afterPageLoaded() {
        try {
            bottomLabel.setText(AppVariables.message("Loaded"));
            charset = HtmlTools.charset(webEngine.getDocument());
            doc = webEngine.getDocument();
            frameDoc.clear();
            addLinksListener(doc);
            changeState(DocLoaded);
            int hisSize = (int) webEngine.executeScript("window.history.length;");
            if (backwardButton != null) {
                backwardButton.setDisable(hisSize < 2);
                forwardButton.setDisable(hisSize < 2);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void addLinksListener(Document doc) {
        try {
            if (doc == null) {
                return;
            }
            NodeList nodeList = doc.getElementsByTagName("a");
            for (int i = 0; i < nodeList.getLength(); i++) {
                EventTarget t = (EventTarget) nodeList.item(i);
                t.removeEventListener("click", linkListener, false);
                t.removeEventListener("mouseover", linkListener, false);
                t.removeEventListener("mouseout", linkListener, false);
                t.removeEventListener("contextmenu", linkListener, false);
                t.addEventListener("click", linkListener, false);
                t.addEventListener("mouseover", linkListener, false);
                t.addEventListener("mouseout", linkListener, false);
                t.addEventListener("contextmenu", linkListener, false);
            }

            nodeList = doc.getElementsByTagName("img");
            for (int i = 0; i < nodeList.getLength(); i++) {
                EventTarget t = (EventTarget) nodeList.item(i);
                t.removeEventListener("click", linkListener, false);
                t.removeEventListener("mouseover", linkListener, false);
                t.removeEventListener("mouseout", linkListener, false);
                t.removeEventListener("contextmenu", linkListener, false);
                t.addEventListener("click", linkListener, false);
                t.addEventListener("mouseover", linkListener, false);
                t.addEventListener("mouseout", linkListener, false);
                t.addEventListener("contextmenu", linkListener, false);
            }

            NodeList frameList = doc.getElementsByTagName("frame");
            for (int i = 0; i < frameList.getLength(); i++) {
                webEngine.executeScript("if ( window.frames[" + i + "].document.readyState==\"complete\") alert('FrameReadyIndex-" + i + "');");
                webEngine.executeScript("window.frames[" + i + "].document.onreadystatechange = "
                        + "function(){ if ( window.frames[" + i + "].document.readyState==\"complete\") alert('FrameReadyIndex-" + i + "'); }");
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void addFrameLinksListener(int frameIndex) {
        try {
            if (frameIndex < 0) {
                return;
            }
            Object c = webEngine.executeScript("window.frames[" + frameIndex + "].document");
            if (c == null) {
                return;
            }
            Document htmlDoc = (Document) c;
            frameDoc.put(frameIndex, htmlDoc);
            addFrameLinksListener(htmlDoc);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void addFrameLinksListener(Document frameDoc) {
        try {
            if (frameDoc == null) {
                return;
            }
            NodeList aList = frameDoc.getElementsByTagName("a");
            for (int j = 0; j < aList.getLength(); j++) {
                Node node = aList.item(j);
                if (node == null) {
                    continue;
                }
                EventTarget t = (EventTarget) node;
                Element element = (Element) node;
                String target = element.getAttribute("target");
                if (target != null && !target.equalsIgnoreCase("_blank")) {
                    t.removeEventListener("click", frameLinkListener, false);
                    t.removeEventListener("mouseover", frameLinkListener, false);
                    t.removeEventListener("mouseout", frameLinkListener, false);
                    t.removeEventListener("contextmenu", frameLinkListener, false);
                    t.addEventListener("click", frameLinkListener, false);
                    t.addEventListener("mouseover", frameLinkListener, false);
                    t.addEventListener("mouseout", frameLinkListener, false);
                    t.addEventListener("contextmenu", frameLinkListener, false);
                } else {
                    t.removeEventListener("click", linkListener, false);
                    t.removeEventListener("mouseover", linkListener, false);
                    t.removeEventListener("mouseout", linkListener, false);
                    t.removeEventListener("contextmenu", linkListener, false);
                    t.addEventListener("click", linkListener, false);
                    t.addEventListener("mouseover", linkListener, false);
                    t.addEventListener("mouseout", linkListener, false);
                    t.addEventListener("contextmenu", linkListener, false);
                }
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setAddress(String value) {
        address = HtmlTools.checkURL(value);
        if (address != null && address.startsWith("file:/")) {
            File file = new File(address.substring(6));
            if (file.exists()) {
                setSourceFile(file);
            } else {
                setSourceFile(null);
            }
        }
    }

    public void setSourceFile(File file) {
        this.sourceFile = file;
        if (parentController != null) {
            parentController.sourceFile = file;
        }
        if (address == null && sourceFile != null) {
            address = sourceFile.getAbsolutePath();
        }
    }

    public void loadContents(String contents) {
        setSourceFile(null);
        setAddress(null);
        webEngine.getLoadWorker().cancel();
        webEngine.loadContent(contents);
    }

    public void loadAddress(String address) {
        setSourceFile(null);
        setAddress(address);
        goAction();
    }

    public void loadFile(File file) {
        if (file == null || !file.exists()) {
            popError(message("InvalidData"));
            return;
        }
        setSourceFile(file);
        URI uri = file.toURI();
        setAddress(uri.toString());
        goAction();
    }

    public void loadURI(URI uri) {
        if (uri == null) {
            popError(message("InvalidData"));
            return;
        }
        loadAddress(uri.toString());
    }

    @FXML
    @Override
    public void goAction() {
        if (address == null) {
            return;
        }
        webEngine.getLoadWorker().cancel();
        webEngine.load(address);
    }

    protected void popLinkMenu(Element element) {
        if (linkX < 0 || linkY < 0 || element == null) {
            return;
        }
        String tag = element.getTagName();
        if (tag == null) {
            return;
        }
        String href = null, hname = null;
        if (tag.equalsIgnoreCase("a")) {
            href = element.getAttribute("href");
            hname = element.getTextContent();
        } else if (tag.equalsIgnoreCase("img")) {
            href = element.getAttribute("src");
            hname = element.getAttribute("alt");
        }
        if (href == null) {
            return;
        }
        String linkAddress;
        try {
            linkAddress = new URL(new URL(element.getBaseURI()), href).toString();
        } catch (Exception e) {
            linkAddress = href;
        }
        String finalAddress = linkAddress;
        String name = hname;
        List<MenuItem> items = new ArrayList<>();
        boolean showName = name != null && !name.isBlank() && !name.equalsIgnoreCase(href);
        String title = message("Name") + ": " + (showName ? name + "\n" : "")
                + message("Link") + ": " + URLDecoder.decode(href, charset)
                + (!linkAddress.equalsIgnoreCase(href) ? "\n" + message("Address") + ": "
                + URLDecoder.decode(linkAddress, charset) : "");
        MenuItem menu = new MenuItem(title);
        menu.setStyle("-fx-text-fill: #2e598a;");
        items.add(menu);
        items.add(new SeparatorMenuItem());

        menu = new MenuItem(message("QueryNetworkAddress"));
        menu.setOnAction((ActionEvent event) -> {
            NetworkQueryAddressController controller
                    = (NetworkQueryAddressController) FxmlStage.openStage(CommonValues.NetworkQueryAddressFxml);
            controller.queryUrl(finalAddress);
        });
        items.add(menu);

        menu = new MenuItem(message("AddAsFavorite"));
        menu.setOnAction((ActionEvent event) -> {
            WebFavoriteAddController controller = (WebFavoriteAddController) openStage(CommonValues.WebFavoriteAddFxml);
            controller.setValues(name == null || name.isBlank() ? finalAddress : name, finalAddress);

        });
        items.add(menu);

        items.add(new SeparatorMenuItem());

        if (parentController != null && parentController instanceof WebBrowserController) {
            menu = new MenuItem(message("OpenLinkInNewTab"));
            menu.setOnAction((ActionEvent event) -> {
                WebBrowserController c = (WebBrowserController) parentController;
                c.loadAddress(finalAddress, false);
            });
            items.add(menu);

            menu = new MenuItem(message("OpenLinkInNewTabSwitch"));
            menu.setOnAction((ActionEvent event) -> {
                WebBrowserController c = (WebBrowserController) parentController;
                c.loadAddress(finalAddress, true);
            });
            items.add(menu);
        }

        menu = new MenuItem(message("OpenLinkByCurrent"));
        menu.setOnAction((ActionEvent event) -> {
            loadAddress(finalAddress);
        });
        items.add(menu);

        menu = new MenuItem(message("OpenLinkBySystem"));
        menu.setOnAction((ActionEvent event) -> {
            browse(finalAddress);
        });
        items.add(menu);

        menu = new MenuItem(message("OpenLinkByEditor"));
        menu.setOnAction((ActionEvent event) -> {
            HtmlEditorController controller = (HtmlEditorController) openStage(CommonValues.HtmlEditorFxml);
            controller.loadAddress(finalAddress);
        });
        items.add(menu);

        menu = new MenuItem(message("OpenLinkByBrowser"));
        menu.setOnAction((ActionEvent event) -> {
            WebBrowserController.oneOpen(finalAddress);
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());

        menu = new MenuItem(message("DownloadBySysBrowser"));
        menu.setOnAction((ActionEvent event) -> {
            browse(finalAddress);
        });
        items.add(menu);

        menu = new MenuItem(message("DownloadByMyBox"));
        menu.setOnAction((ActionEvent event) -> {
            WebBrowserController controller = WebBrowserController.oneOpen();
            controller.download(URLDecoder.decode(finalAddress, charset), name);
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());

        menu = new MenuItem(message("CopyLink"));
        menu.setOnAction((ActionEvent event) -> {
            if (FxmlControl.copyToSystemClipboard(finalAddress)) {
                popInformation(message("CopiedToSystemClipboard"));
            }
        });
        items.add(menu);

        if (showName) {
            menu = new MenuItem(message("CopyLinkName"));
            menu.setOnAction((ActionEvent event) -> {
                if (FxmlControl.copyToSystemClipboard(name)) {
                    popInformation(message("CopiedToSystemClipboard"));
                }
            });
            items.add(menu);

            menu = new MenuItem(message("CopyLinkAndName"));
            menu.setOnAction((ActionEvent event) -> {
                if (FxmlControl.copyToSystemClipboard(name + "\n" + finalAddress)) {
                    popInformation(message("CopiedToSystemClipboard"));
                }
            });
            items.add(menu);
        }

        menu = new MenuItem(message("CopyLinkCode"));
        menu.setOnAction((ActionEvent event) -> {
            String code;
            if (tag.equalsIgnoreCase("img")) {
                code = "<img src=\"" + finalAddress + "\" " + (name == null || name.isBlank() ? "" : " alt=\"" + name + "\"") + " />";
            } else {
                code = "<a href=\"" + finalAddress + "\">" + (name == null || name.isBlank() ? finalAddress : name) + "</a>";
            }
            if (FxmlControl.copyToSystemClipboard(code)) {
                popInformation(message("CopiedToSystemClipboard"));
            }
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());
        menu = new MenuItem(message("PopupClose"));
        menu.setStyle("-fx-text-fill: #2e598a;");
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            if (popMenu != null) {
                popMenu.hide();
            }
            popMenu = null;
        });
        items.add(menu);

        if (popMenu != null) {
            popMenu.hide();
        }
        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(items);
        popMenu.show(webView, linkX, linkY);

    }

    @FXML
    protected void zoomIn() {
        zoomScale += 0.1f;
        webView.setZoom(zoomScale);
    }

    @FXML
    protected void zoomOut() {
        zoomScale -= 0.1f;
        webView.setZoom(zoomScale);
    }

    @FXML
    protected void backAction() {
        webEngine.executeScript("window.history.back();");
    }

    @FXML
    protected void forwardAction() {
        webEngine.executeScript("window.history.forward();");
    }

    @FXML
    protected void refreshAction() {
        goAction();
    }

    @FXML
    @Override
    public void cancelAction() {
        webEngine.getLoadWorker().cancel();
    }

    @FXML
    protected void popFunctionsMenu(MouseEvent mouseEvent) {
        try {
            String html = FxmlControl.getHtml(webEngine);
            doc = webEngine.getDocument();
            boolean isFrameset = frameDoc != null && frameDoc.size() > 0;

            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            items.add(new SeparatorMenuItem());

            if (backwardButton == null) {
                int hisSize = (int) webEngine.executeScript("window.history.length;");

                menu = new MenuItem(message("ZoomIn"));
                menu.setOnAction((ActionEvent event) -> {
                    zoomIn();
                });
                items.add(menu);

                menu = new MenuItem(message("ZoomOut"));
                menu.setOnAction((ActionEvent event) -> {
                    zoomOut();
                });
                items.add(menu);

                menu = new MenuItem(message("Refresh"));
                menu.setOnAction((ActionEvent event) -> {
                    refreshAction();
                });
                items.add(menu);

                menu = new MenuItem(message("Cancel"));
                menu.setOnAction((ActionEvent event) -> {
                    cancelAction();
                });
                items.add(menu);

                menu = new MenuItem(message("Backward"));
                menu.setOnAction((ActionEvent event) -> {
                    backAction();
                });
                menu.setDisable(hisSize < 2);
                items.add(menu);

                menu = new MenuItem(message("Forward"));
                menu.setOnAction((ActionEvent event) -> {
                    forwardAction();
                });
                menu.setDisable(hisSize < 2);
                items.add(menu);
            }

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("AddAsFavorite"));
            menu.setOnAction((ActionEvent event) -> {
                WebFavoriteAddController controller = (WebFavoriteAddController) openStage(CommonValues.WebFavoriteAddFxml);
                controller.setValues(webEngine.getTitle(), address);

            });
            menu.setDisable(address == null || address.isBlank());
            items.add(menu);

            menu = new MenuItem(message("WebFavorites"));
            menu.setOnAction((ActionEvent event) -> {
                WebFavoritesController.oneOpen();
            });
            items.add(menu);

            menu = new MenuItem(message("WebHistories"));
            menu.setOnAction((ActionEvent event) -> {
                WebHistoriesController.oneOpen();
            });
            items.add(menu);

            menu = new MenuItem(message("WebFind"));
            menu.setOnAction((ActionEvent event) -> {
                WebFindController controller = (WebFindController) openStage(CommonValues.WebFindFxml);
                controller.loadContents(html);
                controller.setAddress(address);
                controller.toFront();
            });
            menu.setDisable(html == null || html.isBlank());
            items.add(menu);

            menu = new MenuItem(message("QueryNetworkAddress"));
            menu.setOnAction((ActionEvent event) -> {
                NetworkQueryAddressController controller
                        = (NetworkQueryAddressController) FxmlStage.openStage(CommonValues.NetworkQueryAddressFxml);
                controller.queryUrl(address);
            });
            menu.setDisable(address == null || address.isBlank());
            items.add(menu);

            if (!(parentController instanceof HtmlSnapController)) {
                menu = new MenuItem(message("HtmlSnap"));
                menu.setOnAction((ActionEvent event) -> {
                    HtmlSnapController controller = (HtmlSnapController) openStage(CommonValues.HtmlSnapFxml);
                    if (address != null && !address.isBlank()) {
                        controller.loadAddress(address);
                    } else if (html != null && !html.isBlank()) {
                        controller.loadContents(html);
                    }
                });
                menu.setDisable(html == null || html.isBlank());
                items.add(menu);
            }

            items.add(new SeparatorMenuItem());

            List<MenuItem> editItems = new ArrayList<>();

            if (!(parentController instanceof HtmlEditorController)) {
                menu = new MenuItem(message("HtmlEditor"));
                menu.setOnAction((ActionEvent event) -> {
                    HtmlEditorController controller = (HtmlEditorController) openStage(CommonValues.HtmlEditorFxml);
                    if (address != null && !address.isBlank()) {
                        controller.loadAddress(address);
                    } else if (html != null && !html.isBlank()) {
                        controller.loadContents(html);
                    }
                });
                menu.setDisable(html == null || html.isBlank());
                editItems.add(menu);
            }

            if (isFrameset) {
                NodeList frameList = webEngine.getDocument().getElementsByTagName("frame");
                if (frameList != null) {
                    List<MenuItem> frameItems = new ArrayList<>();
                    for (int i = 0; i < frameList.getLength(); i++) {
                        org.w3c.dom.Node node = frameList.item(i);
                        if (node == null) {
                            continue;
                        }
                        int index = i;
                        Element element = (Element) node;
                        String src = element.getAttribute("src");
                        String name = element.getAttribute("name");
                        String frame = message("Frame") + index;
                        if (name != null && !name.isBlank()) {
                            frame += " :   " + name;
                        } else if (src != null && !src.isBlank()) {
                            frame += " :   " + src;
                        }
                        menu = new MenuItem(frame);
                        menu.setOnAction((ActionEvent event) -> {
                            HtmlEditorController controller = (HtmlEditorController) openStage(CommonValues.HtmlEditorFxml);
                            if (src != null && !src.isBlank()) {
                                controller.loadAddress(HtmlTools.fullAddress(address, src));
                            } else {
                                controller.loadContents(FxmlControl.getFrame(webEngine, index));
                            }

                        });
                        menu.setDisable(html == null || html.isBlank());
                        frameItems.add(menu);
                    }
                    if (!frameItems.isEmpty()) {
                        Menu frameMenu = new Menu(message("Frame"));
                        frameMenu.getItems().addAll(frameItems);
                        editItems.add(frameMenu);
                    }
                }
            }

            if (!editItems.isEmpty()) {
                editItems.add(new SeparatorMenuItem());
                items.addAll(editItems);
            }

            menu = new MenuItem(message("WebElements"));
            menu.setOnAction((ActionEvent event) -> {
                WebElementsController controller = (WebElementsController) openStage(CommonValues.WebElementsFxml);
                if (address != null && !address.isBlank()) {
                    controller.loadAddress(address);
                } else if (html != null && !html.isBlank()) {
                    controller.loadContents(html);
                }
                controller.toFront();
            });
            menu.setDisable(html == null || html.isBlank());
            items.add(menu);

            Menu elementsMenu = new Menu(message("Extract"));
            List<MenuItem> elementsItems = new ArrayList<>();

            menu = new MenuItem(message("Texts"));
            menu.setOnAction((ActionEvent event) -> {
                texts(html);
            });
            menu.setDisable(isFrameset || html == null || html.isBlank());
            elementsItems.add(menu);

            menu = new MenuItem(message("Links"));
            menu.setOnAction((ActionEvent event) -> {
                links();
            });
            menu.setDisable(isFrameset || doc == null);
            elementsItems.add(menu);

            menu = new MenuItem(message("Images"));
            menu.setOnAction((ActionEvent event) -> {
                images();
            });
            menu.setDisable(isFrameset || doc == null);
            elementsItems.add(menu);

            menu = new MenuItem(message("Headings"));
            menu.setOnAction((ActionEvent event) -> {
                toc(html);
            });
            menu.setDisable(isFrameset || html == null || html.isBlank());
            elementsItems.add(menu);

            elementsMenu.getItems().setAll(elementsItems);
            items.add(elementsMenu);
            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                if (popMenu != null && popMenu.isShowing()) {
                    popMenu.hide();
                }
                popMenu = null;
            });
            items.add(menu);

            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);
            popMenu.getItems().addAll(items);
            FxmlControl.locateCenter((Region) mouseEvent.getSource(), popMenu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void links() {
        doc = webEngine.getDocument();
        if (doc == null) {
            parentController.popInformation(message("NoData"));
            return;
        }
        try {
            NodeList aList = doc.getElementsByTagName("a");
            if (aList == null || aList.getLength() < 1) {
                parentController.popInformation(message("NoData"));
                return;
            }
            List<String> names = new ArrayList<>();
            names.addAll(Arrays.asList(
                    message("Index"), message("Link"), message("Name"), message("Title"),
                    message("Address"), message("FullAddress")
            ));
            StringTable table = new StringTable(names);
            int index = 1;
            for (int i = 0; i < aList.getLength(); i++) {
                org.w3c.dom.Node node = aList.item(i);
                if (node == null) {
                    continue;
                }
                Element element = (Element) node;
                String href = element.getAttribute("href");
                if (href == null || href.isBlank()) {
                    continue;
                }
                String linkAddress = href;
                try {
                    URL url = new URL(new URL(element.getBaseURI()), href);
                    linkAddress = url.toString();
                } catch (Exception e) {
                }
                String name = element.getTextContent();
                String title = element.getAttribute("title");
                List<String> row = new ArrayList<>();
                row.addAll(Arrays.asList(
                        index + "",
                        "<a href=\"" + linkAddress + "\">" + (name == null ? title : name) + "</a>",
                        name == null ? "" : name,
                        title == null ? "" : title,
                        URLDecoder.decode(href, charset),
                        URLDecoder.decode(linkAddress, charset)
                ));
                table.add(row);
                index++;
            }
            table.editHtml();
        } catch (Exception e) {
            parentController.popError(e.toString());
        }
    }

    protected void images() {
        doc = webEngine.getDocument();
        if (doc == null) {
            parentController.popInformation(message("NoData"));
            return;
        }
        try {
            NodeList aList = doc.getElementsByTagName("img");
            if (aList == null || aList.getLength() < 1) {
                parentController.popInformation(message("NoData"));
                return;
            }
            List<String> names = new ArrayList<>();
            names.addAll(Arrays.asList(
                    message("Index"), message("Link"), message("Name"), message("Title"),
                    message("Address"), message("FullAddress")
            ));
            StringTable table = new StringTable(names);
            int index = 1;
            for (int i = 0; i < aList.getLength(); i++) {
                org.w3c.dom.Node node = aList.item(i);
                if (node == null) {
                    continue;
                }
                Element element = (Element) node;
                String href = element.getAttribute("src");
                if (href == null || href.isBlank()) {
                    continue;
                }
                String linkAddress = href;
                try {
                    URL url = new URL(new URL(element.getBaseURI()), href);
                    linkAddress = url.toString();
                } catch (Exception e) {
                }
                String name = element.getAttribute("alt");
                List<String> row = new ArrayList<>();
                row.addAll(Arrays.asList(
                        index + "",
                        "<a href=\"" + linkAddress + "\">" + (name == null ? message("Link") : name) + "</a>",
                        "<img src=\"" + linkAddress + "\" " + (name == null ? "" : "alt=\"" + name + "\"") + " width=100/>",
                        name == null ? "" : name,
                        URLDecoder.decode(href, charset),
                        URLDecoder.decode(linkAddress, charset)
                ));
                table.add(row);
                index++;
            }
            table.editHtml();
        } catch (Exception e) {
            parentController.popError(e.toString());
        }
    }

    protected void toc(String html) {
        if (html == null) {
            parentController.popInformation(message("NoData"));
            return;
        }
        String toc = HtmlTools.toc(html, 8);
        if (toc == null || toc.isBlank()) {
            parentController.popInformation(message("NoData"));
            return;
        }
        TextEditerController c = (TextEditerController) openStage(CommonValues.TextEditerFxml);
        c.loadContexts(toc);
        c.toFront();
    }

    protected void texts(String html) {
        if (html == null) {
            parentController.popInformation(message("NoData"));
            return;
        }
        String texts = Jsoup.parse(html).wholeText();
        if (texts == null || texts.isBlank()) {
            parentController.popInformation(message("NoData"));
            return;
        }
        TextEditerController c = (TextEditerController) openStage(CommonValues.TextEditerFxml);
        c.loadContexts(texts);
        c.toFront();
    }

}
