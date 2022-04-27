package mara.mybox.controller;

import java.awt.image.BufferedImage;
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
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.web.PromptData;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebErrorEvent;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Callback;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.ImageClipboard;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ControllerTools;
import mara.mybox.fxml.ImageClipboardTools;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.fxml.WebViewTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.HtmlReadTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.UrlTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

/**
 * @Author Mara
 * @CreateDate 2021-8-18
 * @License Apache License Version 2.0
 */
public class ControlWebView extends BaseController {

    protected WebEngine webEngine;
    protected double linkX, linkY, scrollTop, scrollLeft;
    protected ScrollType scrollType;
    protected float zoomScale;
    protected String address, style, defaultStyle, initStyle;
    protected Charset charset;
    protected Map<Integer, Document> framesDoc;
    protected EventListener docListener;
    protected Element element;
    protected final String onScrollScript;
    protected final SimpleBooleanProperty addressChangedNotify, addressInvalidNotify,
            pageLoadingNotify, pageLoadedNotify;
    protected final String StyleNodeID = "MyBox__Html_Style20211118";

    @FXML
    protected WebView webView;
    @FXML
    protected Label webViewLabel;

    public enum ScrollType {
        Top, Bottom, Last
    }

    public ControlWebView() {
        linkX = linkY = -1;
        zoomScale = 1.0f;
        framesDoc = new HashMap<>();
        charset = Charset.defaultCharset();
        onScrollScript = "window.onscroll = function(){ "
                + "    var topValue = document.documentElement.scrollTop || document.body.scrollTop;"
                + "    alert('scrollTop-' + topValue);"
                + "    var leftValue = document.documentElement.scrollTop || document.body.scrollLeft;"
                + "    alert('scrollLeft-' + leftValue);"
                + "} ";
        addressChangedNotify = new SimpleBooleanProperty(false);
        addressInvalidNotify = new SimpleBooleanProperty(false);
        pageLoadingNotify = new SimpleBooleanProperty(false);
        pageLoadedNotify = new SimpleBooleanProperty(false);
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Html);
    }

    public void setParent(BaseController parent) {
        setParent(parent, ScrollType.Last);
    }

    public void setParent(BaseController parent, ScrollType scrollType) {
        if (parent == null) {
            return;
        }
        this.parentController = parent;
        this.baseName = parent.baseName;
        this.scrollType = scrollType;
    }

    @Override
    public void initControls() {
        try {
            initWebView();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initWebView() {
        try {
            webEngine = webView.getEngine();

            webView.setCache(false);
            webEngine.setJavaScriptEnabled(true);
            webEngine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/610.2 (KHTML, like Gecko) JavaFX/17 Safari/610.2");

            webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
                @Override
                public void changed(ObservableValue ov, Worker.State oldState, Worker.State newState) {
                    switch (newState) {
                        case RUNNING:
                            pageIsLoading();
                            break;
                        case SUCCEEDED:
                            afterPageLoaded();
                            break;
                        case CANCELLED:
                            if (webViewLabel != null) {
                                webViewLabel.setText(message("Canceled"));
                            }
                            break;
                        case FAILED:
                            if (webViewLabel != null) {
                                webViewLabel.setText(message("Failed"));
                            }
                            break;
                        default:
                            if (webViewLabel != null) {
                                webViewLabel.setText(newState.name());
                            }
                    }
                }
            });

            webView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    closePopup();
                    if (parentController != null) {
                        parentController.closePopup();
                    }
                    linkX = mouseEvent.getScreenX();
                    linkY = mouseEvent.getScreenY();
                }
            });

            // http://blogs.kiyut.com/tonny/2013/07/30/javafx-webview-addhyperlinklistener/
            docListener = new EventListener() {
                @Override
                public synchronized void handleEvent(org.w3c.dom.events.Event ev) {
                    try {
                        String domEventType = ev.getType();
                        String tag = null, href = null;
                        if (ev.getTarget() != null) {
                            element = (Element) ev.getTarget();
                            tag = element.getTagName();
                            if (tag != null) {
                                if (tag.equalsIgnoreCase("a")) {
                                    href = element.getAttribute("href");
                                } else if (tag.equalsIgnoreCase("img")) {
                                    href = element.getAttribute("src");
                                }
                            }
                        } else {
                            element = null;
                        }
//                        MyBoxLog.console(webView.getId() + " " + domEventType + " " + tag + " " + href);
                        if (webViewLabel != null) {
                            if ("mouseover".equals(domEventType)) {
                                webViewLabel.setText(href != null ? URLDecoder.decode(href, charset) : tag);
                            } else if ("mouseout".equals(domEventType)) {
                                webViewLabel.setText("");
                            }
                        }
                        if (element == null) {
                            return;
                        }
//                        MyBoxLog.console(webView.getId() + " " + domEventType + " " + tag + " " + href);
                        if (href != null) {
                            String target = element.getAttribute("target");
                            String clickAction = UserConfig.getString("WebViewWhenClickImageLink", "PopMenu");
                            if ("click".equals(domEventType) && target != null && !target.equalsIgnoreCase("_blank")) {
                                webEngine.executeScript("if ( window.frames." + target
                                        + ".document.readyState==\"complete\") alert('FrameReadyName-" + target + "');");
                                webEngine.executeScript("window.frames." + target + ".document.onreadystatechange = "
                                        + "function(){ if ( window.frames." + target
                                        + ".document.readyState==\"complete\") alert('FrameReadyName-" + target + "'); }");
                            } else if ("contextmenu".equals(domEventType)
                                    || ("click".equals(domEventType) && !href.startsWith("javascript:")
                                    && (clickAction == null || "PopMenu".equals(clickAction)))) {
                                ev.preventDefault();
                                timer = new Timer();
                                timer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        Platform.runLater(() -> {
                                            popLinkMenu(element);
                                        });
                                    }
                                }, 100);
                            } else if ("click".equals(domEventType) && !href.startsWith("javascript:")) {
                                switch (clickAction) {
                                    case "AsPage":
                                        break;
                                    case "Load":
                                        ev.preventDefault();
                                        loadAddress(finalAddress(element));
                                        break;
                                    default:
                                        ev.preventDefault();
                                        WebBrowserController.oneOpen(finalAddress(element), "OpenSwitch".equals(clickAction));
                                        break;
                                }
                            }

                        } else if ("contextmenu".equals(domEventType) && !"frame".equalsIgnoreCase(tag)) {
                            ev.preventDefault();
                            timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    Platform.runLater(() -> {
                                        popElementMenu(element);
                                    });
                                }
                            }, 100);
                        }
                        MenuWebviewController menu = MenuWebviewController.running(webView);
                        if (menu != null) {
                            menu.setElement(element);
                        }
                    } catch (Exception e) {
                        MyBoxLog.error(e);
                    }
                }
            };

            webEngine.getLoadWorker().exceptionProperty().addListener(new ChangeListener<Throwable>() {
                @Override
                public void changed(ObservableValue<? extends Throwable> ov, Throwable ot, Throwable nt) {
                    if (nt == null) {
                        return;
                    }
                    if (webViewLabel != null) {
                        webViewLabel.setText(nt.getMessage());
                    }
                    alertError(nt.getMessage());
                }
            });

            webEngine.setOnStatusChanged(new EventHandler<WebEvent<String>>() {
                @Override
                public void handle(WebEvent<String> ev) {
                    if (webViewLabel != null) {
                        webViewLabel.setText(ev.getData());
                    }
                }
            });

            webEngine.locationProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldv, String newv) {
                    if (webViewLabel != null && newv != null) {
                        webViewLabel.setText(URLDecoder.decode(newv, charset));
                    }
                }
            });

            webEngine.setPromptHandler(new Callback< PromptData, String>() {
                @Override
                public String call(PromptData p) {
//                    MyBoxLog.console("here:" + p.getMessage());
                    String value = PopTools.askValue(
                            parentController != null ? parentController.getBaseTitle() : myController.getBaseTitle(),
                            null, p.getMessage(), p.getDefaultValue());
                    return value;
                }
            });

            webEngine.setConfirmHandler(new Callback< String, Boolean>() {
                @Override
                public Boolean call(String message) {
                    try {
//                        MyBoxLog.console("here:" + message);
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle(parentController != null ? parentController.getBaseTitle() : myController.getBaseTitle());
                        alert.setHeaderText(null);
                        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                        alert.getDialogPane().setContent(new Label(message));
                        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                        stage.setAlwaysOnTop(true);
                        stage.toFront();
                        alert.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
                        Optional<ButtonType> result = alert.showAndWait();
                        return result != null && result.get() == ButtonType.YES;
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
                    if (msg == null) {
                        return;
                    }
                    int value = -1;
                    if (msg.startsWith("FrameReadyIndex-")) {
                        value = Integer.parseInt(msg.substring("FrameReadyIndex-".length()));
                    } else if (msg.startsWith("FrameReadyName-")) {
                        value = WebViewTools.frameIndex(webEngine, msg.substring("FrameReadyName-".length()));
                    } else if (msg.startsWith("scrollTop-")) {
                        scrollTop = Double.valueOf(msg.substring("scrollTop-".length()));
                    } else if (msg.startsWith("scrollLeft-")) {
                        scrollLeft = Double.valueOf(msg.substring("scrollLeft-".length()));
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
                                addFrameListener(frameIndex);
                            });
                        }
                    }, 500);
                }
            });

            webEngine.setOnError(new EventHandler<WebErrorEvent>() {

                @Override
                public void handle(WebErrorEvent event) {
                    if (webViewLabel != null) {
                        webViewLabel.setText(event.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public boolean loadFile(File file) {
        if (file == null || !file.exists()) {
            addressInvalidNotify.set(!addressInvalidNotify.get());
            return false;
        }
        return loadAddress(UrlTools.decodeURL(file, Charset.defaultCharset()));
    }

    public boolean loadURI(URI uri) {
        if (uri == null) {
            addressInvalidNotify.set(!addressInvalidNotify.get());
            return false;
        }
        return loadAddress(uri.toString());
    }

    public boolean loadAddress(String value) {
        value = UrlTools.checkURL(value, Charset.forName("UTF-8"));
        if (value == null) {
            addressInvalidNotify.set(!addressInvalidNotify.get());
            return false;
        }
        goAddress(value);
        return true;
    }

    public boolean loadContents(String contents) {
        return loadContents(null, contents);
    }

    public boolean loadContents(String address, String contents) {
        setAddress(address);
        writeContents(contents);
        return true;
    }

    public void writeContents(String contents) {
        webEngine.getLoadWorker().cancel();
        if (contents == null) {
            webEngine.loadContent("");
        } else {
            webEngine.loadContent(contents);
        }
    }

    private boolean setAddress(String value) {
        try {
            setSourceFile(null);
            address = UrlTools.checkURL(value, Charset.forName("UTF-8"));
            if (address != null && address.startsWith("file:/")) {
                File file = new File(address.substring(6));
                if (file.exists()) {
                    setSourceFile(file);
                }
            }
            addressChangedNotify.set(!addressChangedNotify.get());
            return true;
        } catch (Exception e) {
            addressInvalidNotify.set(!addressInvalidNotify.get());
            return false;
        }
    }

    @Override
    public void setSourceFile(File file) {
        this.sourceFile = file;
        if (address == null && sourceFile != null) {
            address = sourceFile.toURI().toString();
        }
    }

    private void goAddress(String value) {
        try {
            if (!setAddress(value)) {
                return;
            }
            setWebViewLabel(message("Loading..."));
            webEngine.getLoadWorker().cancel();
            webEngine.load(address);
        } catch (Exception e) {
            MyBoxLog.error(e);
            addressInvalidNotify.set(!addressInvalidNotify.get());
        }
    }

    protected void setWebViewLabel(String string) {
        if (webViewLabel != null) {
            webViewLabel.setText(string);
        }
    }

    private void pageIsLoading() {
        setWebViewLabel(message("Loading..."));
        pageLoadingNotify.set(!pageLoadingNotify.get());
    }

    private void afterPageLoaded() {
        try {
            if (initStyle != null) {
                writeStyle(initStyle);
            } else {
                String prefix = UserConfig.getBoolean(baseName + "ShareHtmlStyle", true) ? "AllInterface" : baseName;
                setStyle(UserConfig.getString(prefix + "HtmlStyle", defaultStyle));
            }
            setWebViewLabel(message("Loaded"));

            Document doc = webEngine.getDocument();
            charset = HtmlReadTools.charset(doc);
            framesDoc.clear();
            addDocListener(doc);
            pageLoadedNotify.set(!pageLoadedNotify.get());

            if (!(this instanceof ControlHtmlEditor)) {
                try {
                    webEngine.executeScript("document.body.contentEditable=" + UserConfig.getBoolean("WebViewEditable", false));
                } catch (Exception e) {
                }
            }

            try {
                if (null == scrollType) {
                    webEngine.executeScript("setTimeout(window.scrollTo(" + scrollLeft + "," + scrollTop + "), 1000);");
                } else {
                    switch (scrollType) {
                        case Bottom:
                            webEngine.executeScript("setTimeout(window.scrollTo(0, document.documentElement.scrollHeight || document.body.scrollHeight), 1000);");
                            break;
                        case Top:
                            webEngine.executeScript("window.scrollTo(0, 0);");
                            break;
                        default:
                            webEngine.executeScript("setTimeout(window.scrollTo(" + scrollLeft + "," + scrollTop + "), 1000);");
                            break;
                    }
                }
            } catch (Exception e) {
//                MyBoxLog.debug(e);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void addDocListener(Document doc) {
        try {
            if (doc == null) {
                return;
            }
            Element docNode = doc.getDocumentElement();
            EventTarget t = (EventTarget) docNode;
            t.removeEventListener("click", docListener, false);
            t.removeEventListener("mouseover", docListener, false);
            t.removeEventListener("mouseout", docListener, false);
            t.removeEventListener("contextmenu", docListener, false);
            t.addEventListener("click", docListener, false);
            t.addEventListener("mouseover", docListener, false);
            t.addEventListener("mouseout", docListener, false);
            t.addEventListener("contextmenu", docListener, false);

            webEngine.executeScript(onScrollScript);

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

    protected void addFrameListener(int frameIndex) {
        try {
            if (frameIndex < 0) {
                return;
            }
            Object c = webEngine.executeScript("window.frames[" + frameIndex + "].document");
            if (c == null) {
                return;
            }
            Document frame = (Document) c;
            framesDoc.put(frameIndex, frame);

            addDocListener(frame);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public String loadedHtml() {
        return HtmlReadTools.removeNode(WebViewTools.getHtml(webEngine), StyleNodeID);
    }

    public void setStyle(String style) {
        String prefix = UserConfig.getBoolean(baseName + "ShareHtmlStyle", true) ? "AllInterface" : baseName;
        UserConfig.setString(prefix + "HtmlStyle", style);

        writeStyle(style);
    }

    public void writeStyle(String style) {
        WebViewTools.removeNode(webEngine, StyleNodeID);
        this.style = style;
        if (style != null && !style.isBlank()) {
            WebViewTools.addStyle(webEngine, style, StyleNodeID);
        }
    }

    public String finalAddress(Element element) {
        if (element == null) {
            return null;
        }
        String tag = element.getTagName();
        if (tag == null) {
            return null;
        }
        String href = null;
        if (tag.equalsIgnoreCase("a")) {
            href = element.getAttribute("href");
        } else if (tag.equalsIgnoreCase("img")) {
            href = element.getAttribute("src");
        }
        if (href == null) {
            return null;
        }
        String linkAddress;
        try {
            linkAddress = new URL(new URL(element.getBaseURI()), href).toString();
        } catch (Exception e) {
            linkAddress = href;
        }
        return URLDecoder.decode(linkAddress, charset);
    }

    public void popLinkMenu(Element element) {
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
        String finalAddress = URLDecoder.decode(linkAddress, charset);
        String name = hname;
        List<MenuItem> items = new ArrayList<>();
        boolean showName = name != null && !name.isBlank() && !name.equalsIgnoreCase(href);
        String title = (showName ? message("Name") + ": " + name + "\n" : "")
                + message("Link") + ": " + URLDecoder.decode(href, charset)
                + (!linkAddress.equalsIgnoreCase(href) ? "\n" + message("Address") + ": "
                + finalAddress : "");
        MenuItem menu = new MenuItem(title);
        menu.setStyle("-fx-text-fill: #2e598a;");
        items.add(menu);
        items.add(new SeparatorMenuItem());

        items.add(clickMenu());

        menu = new MenuItem(message("QueryNetworkAddress"), StyleTools.getIconImage("iconQuery.png"));
        menu.setOnAction((ActionEvent event) -> {
            NetworkQueryAddressController controller
                    = (NetworkQueryAddressController) WindowTools.openStage(Fxmls.NetworkQueryAddressFxml);
            controller.queryUrl(finalAddress);
        });
        items.add(menu);

        menu = new MenuItem(message("AddAsFavorite"), StyleTools.getIconImage("iconStar.png"));
        menu.setOnAction((ActionEvent event) -> {
            WebFavoriteAddController controller = (WebFavoriteAddController) WindowTools.openStage(Fxmls.WebFavoriteAddFxml);
            controller.setValues(name == null || name.isBlank() ? finalAddress : name, finalAddress);

        });
        items.add(menu);

        items.add(new SeparatorMenuItem());

        menu = new MenuItem(message("OpenLinkInNewTab"), StyleTools.getIconImage("iconWindow.png"));
        menu.setOnAction((ActionEvent event) -> {
            WebBrowserController.oneOpen(finalAddress, false);
        });
        items.add(menu);

        menu = new MenuItem(message("OpenLinkInNewTabSwitch"), StyleTools.getIconImage("iconWindow.png"));
        menu.setOnAction((ActionEvent event) -> {
            WebBrowserController.oneOpen(finalAddress, true);
        });
        items.add(menu);

        menu = new MenuItem(message("OpenLinkByCurrent"), StyleTools.getIconImage("iconWindow.png"));
        menu.setOnAction((ActionEvent event) -> {
            loadAddress(finalAddress);
        });
        items.add(menu);

        menu = new MenuItem(message("OpenLinkBySystem"), StyleTools.getIconImage("iconWindow.png"));
        menu.setOnAction((ActionEvent event) -> {
            browse(finalAddress);
        });
        items.add(menu);

        menu = new MenuItem(message("OpenLinkByEditor"), StyleTools.getIconImage("iconEdit.png"));
        menu.setOnAction((ActionEvent event) -> {
            HtmlEditorController controller = (HtmlEditorController) WindowTools.openStage(Fxmls.HtmlEditorFxml);
            controller.loadAddress(finalAddress);
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());

        if (tag.equalsIgnoreCase("img")) {
            if (ImageClipboardTools.isMonitoringCopy()) {
                menu = new MenuItem(message("CopyImageToClipboards"), StyleTools.getIconImage("iconCopySystem.png"));
            } else {
                menu = new MenuItem(message("CopyImageToSystemClipboard"), StyleTools.getIconImage("iconCopySystem.png"));
            }
            menu.setOnAction((ActionEvent event) -> {
                handleImage(finalAddress, name, "toSystemClipboard");
            });
            items.add(menu);

            menu = new MenuItem(message("CopyImageToMyBoxClipboard"), StyleTools.getIconImage("iconCopy.png"));
            menu.setOnAction((ActionEvent event) -> {
                handleImage(finalAddress, name, "toMyBoxClipboard");
            });
            items.add(menu);

            menu = new MenuItem(message("ViewImage"), StyleTools.getIconImage("iconView.png"));
            menu.setOnAction((ActionEvent event) -> {
                handleImage(finalAddress, name, "view");
            });
            items.add(menu);

            menu = new MenuItem(message("EditImage"), StyleTools.getIconImage("iconEdit.png"));
            menu.setOnAction((ActionEvent event) -> {
                handleImage(finalAddress, name, "edit");
            });
            items.add(menu);
        }

        menu = new MenuItem(message("DownloadBySysBrowser"), StyleTools.getIconImage("iconDownload.png"));
        menu.setOnAction((ActionEvent event) -> {
            browse(finalAddress);
        });
        items.add(menu);

        menu = new MenuItem(message("DownloadByMyBox"), StyleTools.getIconImage("iconDownload.png"));
        menu.setOnAction((ActionEvent event) -> {
            WebBrowserController controller = WebBrowserController.oneOpen();
            controller.download(finalAddress, name);
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());

        menu = new MenuItem(message("CopyLink"), StyleTools.getIconImage("iconCopySystem.png"));
        menu.setOnAction((ActionEvent event) -> {
            TextClipboardTools.copyToSystemClipboard(myController, finalAddress);
        });
        items.add(menu);

        if (showName) {
            menu = new MenuItem(message("CopyLinkName"), StyleTools.getIconImage("iconCopySystem.png"));
            menu.setOnAction((ActionEvent event) -> {
                TextClipboardTools.copyToSystemClipboard(myController, name);
            });
            items.add(menu);

            menu = new MenuItem(message("CopyLinkAndName"), StyleTools.getIconImage("iconCopySystem.png"));
            menu.setOnAction((ActionEvent event) -> {
                TextClipboardTools.copyToSystemClipboard(myController, name + "\n" + finalAddress);
            });
            items.add(menu);
        }

        menu = new MenuItem(message("CopyLinkCode"), StyleTools.getIconImage("iconCopySystem.png"));
        menu.setOnAction((ActionEvent event) -> {
            String code;
            if (tag.equalsIgnoreCase("img")) {
                code = "<img src=\"" + finalAddress + "\" " + (name == null || name.isBlank() ? "" : " alt=\"" + name + "\"") + " />";
            } else {
                code = "<a href=\"" + finalAddress + "\">" + (name == null || name.isBlank() ? finalAddress : name) + "</a>";
            }
            TextClipboardTools.copyToSystemClipboard(myController, code);
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());
        menu = new MenuItem(message("PopupClose"), StyleTools.getIconImage("iconCancel.png"));
        menu.setStyle("-fx-text-fill: #2e598a;");
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            if (popMenu != null) {
                popMenu.hide();
                popMenu = null;
            }
        });
        items.add(menu);

        closePopup();
        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(items);
        if (parentController != null) {
            parentController.closePopup();
            parentController.setPopMenu(popMenu);
        }
        popMenu.show(webView, linkX, linkY);

    }

    public void popElementMenu(Element element) {
        try {
            if (linkX < 0 || linkY < 0) {
                return;
            }
            MenuWebviewController.pop(this, element, linkX, linkY);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void handleImage(String address, String name, String target) {
        if (address == null || target == null) {
            return;
        }
        synchronized (this) {
            popInformation(message("Handling..."));
            SingletonTask bgTask = new SingletonTask<Void>(this) {

                private Image image = null;

                @Override
                protected boolean handle() {
                    try {
                        File imageFile = HtmlReadTools.url2Image(address, name);
                        BufferedImage bi = ImageFileReaders.readImage(imageFile);
                        if (bi == null) {
                            return false;
                        }
                        image = SwingFXUtils.toFXImage(bi, null);
                        return image != null;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    switch (target) {
                        case "toSystemClipboard":
                            ImageClipboardTools.copyToSystemClipboard(myController, image);
                            break;
                        case "toMyBoxClipboard":
                            ImageClipboardTools.copyToMyBoxClipboard(myController, image, ImageClipboard.ImageSource.Link);
                            break;
                        case "edit":
                            ImageManufactureController.load(image);
                            break;
                        default:
                            ImageViewerController.load(image);
                    }
                }

            };
            start(bgTask, false);
        }
    }

    public void zoomIn() {
        zoomScale += 0.1f;
        webView.setZoom(zoomScale);
    }

    public void zoomOut() {
        zoomScale -= 0.1f;
        webView.setZoom(zoomScale);
    }

    public void backAction() {
        webEngine.executeScript("window.history.back();");
    }

    public void forwardAction() {
        webEngine.executeScript("window.history.forward();");
    }

    public void refresh() {
        if (address != null) {
            goAddress(address);
        } else {
            loadContents(loadedHtml());
        }
    }

    public List<MenuItem> operationsMenu() {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            int hisSize = (int) webEngine.executeScript("window.history.length;");
            menu = new MenuItem(message("Backward"), StyleTools.getIconImage("iconPrevious.png"));
            menu.setOnAction((ActionEvent event) -> {
                backAction();
            });
//            menu.setDisable(hisSize < 2);
            items.add(menu);

            menu = new MenuItem(message("Forward"), StyleTools.getIconImage("iconNext.png"));
            menu.setOnAction((ActionEvent event) -> {
                forwardAction();
            });
//            menu.setDisable(hisSize < 2);
            items.add(menu);

            menu = new MenuItem(message("ZoomIn"), StyleTools.getIconImage("iconZoomIn.png"));
            menu.setOnAction((ActionEvent event) -> {
                zoomIn();
            });
            items.add(menu);

            menu = new MenuItem(message("ZoomOut"), StyleTools.getIconImage("iconZoomOut.png"));
            menu.setOnAction((ActionEvent event) -> {
                zoomOut();
            });
            items.add(menu);

            menu = new MenuItem(message("Refresh"), StyleTools.getIconImage("iconRefresh.png"));
            menu.setOnAction((ActionEvent event) -> {
                refresh();
            });
            items.add(menu);

            menu = new MenuItem(message("Cancel"), StyleTools.getIconImage("iconCancel.png"));
            menu.setOnAction((ActionEvent event) -> {
                cancelAction();
            });
            items.add(menu);

            return items;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    @FXML
    public void popOperationsMenu(MouseEvent mouseEvent) {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            if (address != null && !address.isBlank()) {
                menu = new MenuItem(address);
                menu.setStyle("-fx-text-fill: #2e598a;");
                items.add(menu);
                items.add(new SeparatorMenuItem());
            }

            items.addAll(operationsMenu());
            items.add(new SeparatorMenuItem());

            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);
            popMenu.getItems().addAll(items);
            LocateTools.locateCenter((Region) mouseEvent.getSource(), popMenu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public Menu clickMenu() {
        try {
            Menu clickMenu = new Menu(message("WhenClickImageLink"));
            ToggleGroup clickGroup = new ToggleGroup();
            String currentClick = UserConfig.getString("WebViewWhenClickImageLink", "PopMenu");

            RadioMenuItem clickPopMenu = new RadioMenuItem(message("PopMenu"), StyleTools.getIconImage("iconMenu.png"));
            clickPopMenu.setSelected(currentClick == null || "PopMenu".equals(currentClick));
            clickPopMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setString("WebViewWhenClickImageLink", "PopMenu");
                }
            });
            clickPopMenu.setToggleGroup(clickGroup);

            RadioMenuItem clickAsPageMenu = new RadioMenuItem(message("HandleAsPage"), StyleTools.getIconImage("iconHtml.png"));
            clickAsPageMenu.setSelected("AsPage".equals(currentClick));
            clickAsPageMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setString("WebViewWhenClickImageLink", "AsPage");
                }
            });
            clickAsPageMenu.setToggleGroup(clickGroup);

            RadioMenuItem clickOpenSwitchMenu = new RadioMenuItem(message("OpenLinkInNewTabSwitch"), StyleTools.getIconImage("iconWindow.png"));
            clickOpenSwitchMenu.setSelected("OpenSwitch".equals(currentClick));
            clickOpenSwitchMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setString("WebViewWhenClickImageLink", "OpenSwitch");
                }
            });
            clickOpenSwitchMenu.setToggleGroup(clickGroup);

            RadioMenuItem clickOpenMenu = new RadioMenuItem(message("OpenLinkInNewTab"), StyleTools.getIconImage("iconWindow.png"));
            clickOpenMenu.setSelected("Open".equals(currentClick));
            clickOpenMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setString("WebViewWhenClickImageLink", "Open");
                }
            });
            clickOpenMenu.setToggleGroup(clickGroup);

            RadioMenuItem clickLoadMenu = new RadioMenuItem(message("OpenLinkByCurrent"), StyleTools.getIconImage("iconWindow.png"));
            clickLoadMenu.setSelected("Load".equals(currentClick));
            clickLoadMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setString("WebViewWhenClickImageLink", "Load");
                }
            });
            clickLoadMenu.setToggleGroup(clickGroup);

            clickMenu.getItems().addAll(clickPopMenu, clickAsPageMenu, clickOpenSwitchMenu, clickOpenMenu, clickLoadMenu);

            return clickMenu;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    @FXML
    public void popFunctionsMenu(MouseEvent mouseEvent) {
        try {
            String html = loadedHtml();
            Document doc = webEngine.getDocument();
            boolean isFrameset = framesDoc != null && !framesDoc.isEmpty();

            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            if (address != null && !address.isBlank()) {
                menu = new MenuItem(address);
                menu.setStyle("-fx-text-fill: #2e598a;");
                items.add(menu);
                items.add(new SeparatorMenuItem());
            }

            Menu operationsMenu = new Menu(message("Operations"), StyleTools.getIconImage("iconAsterisk.png"));
            operationsMenu.getItems().setAll(operationsMenu());
            items.add(operationsMenu);

            items.add(clickMenu());

            if (!(this instanceof ControlHtmlEditor)) {
                CheckMenuItem editableMenu = new CheckMenuItem(message("Editable"), StyleTools.getIconImage("iconEdit.png"));
                editableMenu.setSelected(UserConfig.getBoolean("WebViewEditable", false));
                editableMenu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        setEditable(editableMenu.isSelected());
                    }
                });
                items.add(editableMenu);
            }
            items.add(new SeparatorMenuItem());

            if (address != null && !address.isBlank()) {
                menu = new MenuItem(message("AddAsFavorite"), StyleTools.getIconImage("iconStar.png"));
                menu.setOnAction((ActionEvent event) -> {
                    WebFavoriteAddController controller = (WebFavoriteAddController) WindowTools.openStage(Fxmls.WebFavoriteAddFxml);
                    controller.setValues(webEngine.getTitle(), address);

                });
                items.add(menu);
            }

            menu = new MenuItem(message("WebFavorites"), StyleTools.getIconImage("iconStarFilled.png"));
            menu.setOnAction((ActionEvent event) -> {
                WebFavoritesController.oneOpen();
            });
            items.add(menu);

            menu = new MenuItem(message("WebHistories"), StyleTools.getIconImage("iconHistory.png"));
            menu.setOnAction((ActionEvent event) -> {
                WebHistoriesController.oneOpen();
            });
            items.add(menu);

            if (address != null && !address.isBlank()) {
                menu = new MenuItem(message("QueryNetworkAddress"), StyleTools.getIconImage("iconSSL.png"));
                menu.setOnAction((ActionEvent event) -> {
                    NetworkQueryAddressController controller
                            = (NetworkQueryAddressController) WindowTools.openStage(Fxmls.NetworkQueryAddressFxml);
                    controller.queryUrl(address);
                });
                items.add(menu);
            }

            items.add(new SeparatorMenuItem());

            if (html != null && !html.isBlank()) {
                List<MenuItem> editItems = new ArrayList<>();
                menu = new MenuItem(message("HtmlEditor"), StyleTools.getIconImage("iconEdit.png"));
                menu.setOnAction((ActionEvent event) -> {
                    edit(address, html);
                });
                editItems.add(menu);

                menu = new MenuItem(message("HtmlSnap"), StyleTools.getIconImage("iconSnapshot.png"));
                menu.setOnAction((ActionEvent event) -> {
                    HtmlSnapController controller = (HtmlSnapController) WindowTools.openStage(Fxmls.HtmlSnapFxml);
                    if (address != null && !address.isBlank()) {
                        controller.loadAddress(address);
                    } else {
                        controller.loadContents(html);
                    }
                });
                editItems.add(menu);

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
                            Element e = (Element) node;
                            String src = e.getAttribute("src");
                            String name = e.getAttribute("name");
                            String frame = message("Frame") + index;
                            if (name != null && !name.isBlank()) {
                                frame += " :   " + name;
                            } else if (src != null && !src.isBlank()) {
                                frame += " :   " + src;
                            }
                            menu = new MenuItem(frame);
                            menu.setOnAction((ActionEvent event) -> {
                                HtmlEditorController controller = (HtmlEditorController) WindowTools.openStage(Fxmls.HtmlEditorFxml);
                                if (src != null && !src.isBlank()) {
                                    controller.loadAddress(UrlTools.fullAddress(address, src));
                                } else {
                                    controller.loadContents(WebViewTools.getFrame(webEngine, index));
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

                editItems.add(new SeparatorMenuItem());
                items.addAll(editItems);

                menu = new MenuItem(message("HtmlCodes"), StyleTools.getIconImage("iconMeta.png"));
                menu.setOnAction((ActionEvent event) -> {
                    HtmlCodesPopController.openWebView(myController, webView);
                });
                items.add(menu);

                menu = new MenuItem(message("WebFind"), StyleTools.getIconImage("iconQuery.png"));
                menu.setOnAction((ActionEvent event) -> {
                    find(html);
                });
                items.add(menu);

                menu = new MenuItem(message("WebElements"), StyleTools.getIconImage("iconQuery.png"));
                menu.setOnAction((ActionEvent event) -> {
                    HtmlElementsController controller = (HtmlElementsController) WindowTools.openStage(Fxmls.HtmlElementsFxml);
                    if (address != null && !address.isBlank()) {
                        controller.loadAddress(address);
                    } else {
                        controller.loadContents(html);
                    }
                    controller.requestMouse();
                });
                items.add(menu);

                menu = new MenuItem(message("Script"), StyleTools.getIconImage("iconScript.png"));
                menu.setOnAction((ActionEvent event) -> {
                    JavaScriptController.open(this);
                });
                items.add(menu);

                Menu elementsMenu = new Menu(message("Extract"));
                List<MenuItem> elementsItems = new ArrayList<>();

                menu = new MenuItem(message("Table"), StyleTools.getIconImage("iconData.png"));
                menu.setOnAction((ActionEvent event) -> {
                    tables(html, sourceFile != null ? sourceFile.getName() : address);
                });
                menu.setDisable(isFrameset);
                elementsItems.add(menu);

                menu = new MenuItem(message("Texts"), StyleTools.getIconImage("iconTxt.png"));
                menu.setOnAction((ActionEvent event) -> {
                    texts(html);
                });
                menu.setDisable(isFrameset);
                elementsItems.add(menu);

                menu = new MenuItem(message("Links"), StyleTools.getIconImage("iconLink.png"));
                menu.setOnAction((ActionEvent event) -> {
                    links();
                });
                menu.setDisable(isFrameset || doc == null);
                elementsItems.add(menu);

                menu = new MenuItem(message("Images"), StyleTools.getIconImage("iconFlower.png"));
                menu.setOnAction((ActionEvent event) -> {
                    images();
                });
                menu.setDisable(isFrameset || doc == null);
                elementsItems.add(menu);

                menu = new MenuItem(message("Headings"), StyleTools.getIconImage("iconHeader.png"));
                menu.setOnAction((ActionEvent event) -> {
                    toc(html);
                });
                menu.setDisable(isFrameset);
                elementsItems.add(menu);

                elementsMenu.getItems().setAll(elementsItems);
                items.add(elementsMenu);

                items.add(new SeparatorMenuItem());
            }

            if (address != null && !address.isBlank()) {
                menu = new MenuItem(message("OpenLinkBySystem"), StyleTools.getIconImage("iconWindow.png"));
                menu.setOnAction((ActionEvent event) -> {
                    browse(address);
                });
                items.add(menu);

                menu = new MenuItem(message("OpenLinkInNewTabSwitch"), StyleTools.getIconImage("iconWindow.png"));
                menu.setOnAction((ActionEvent event) -> {
                    WebBrowserController.oneOpen(address, true);
                });
                items.add(menu);

                menu = new MenuItem(message("OpenLinkInNewTab"), StyleTools.getIconImage("iconWindow.png"));
                menu.setOnAction((ActionEvent event) -> {
                    WebBrowserController.oneOpen(address, false);
                });
                items.add(menu);

                items.add(new SeparatorMenuItem());

            }

            if (html != null && !html.isBlank()) {
                menu = new MenuItem(message("SaveAs"), StyleTools.getIconImage("iconSaveAs.png"));
                menu.setOnAction((ActionEvent event) -> {
                    saveAsAction();
                });
                items.add(menu);
                items.add(new SeparatorMenuItem());
            }

            menu = new MenuItem(message("PopupClose"), StyleTools.getIconImage("iconCancel.png"));
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
            LocateTools.locateCenter((Region) mouseEvent.getSource(), popMenu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setEditable(boolean e) {
        UserConfig.setBoolean("WebViewEditable", e);
        webEngine.executeScript("document.body.contentEditable=" + e);
        if (e) {
            alertInformation(message("HtmlEditableComments"));
        }
    }

    @FXML
    public void editAction() {
        edit(address, loadedHtml());
    }

    public HtmlEditorController edit(String address, String html) {
        HtmlEditorController controller = (HtmlEditorController) WindowTools.openStage(Fxmls.HtmlEditorFxml);
        if (address != null) {
            controller.loadAddress(address);
        } else {
            controller.loadContents(address, html);
        }
        return controller;
    }

    protected void links() {
        Document doc = webEngine.getDocument();
        if (doc == null) {
            popError(message("NoData"));
            return;
        }
        popInformation(message("Handling..."));
        SingletonTask bgTask = new SingletonTask<Void>(this) {

            private StringTable table;

            @Override
            protected boolean handle() {
                try {
                    NodeList aList = doc.getElementsByTagName("a");
                    if (aList == null || aList.getLength() < 1) {
                        error = message("NoData");
                        return false;
                    }
                    List<String> names = new ArrayList<>();
                    names.addAll(Arrays.asList(message("Index"), message("Link"), message("Name"), message("Title"),
                            message("Address"), message("FullAddress")
                    ));
                    table = new StringTable(names);
                    int index = 1;
                    for (int i = 0; i < aList.getLength(); i++) {
                        org.w3c.dom.Node node = aList.item(i);
                        if (node == null) {
                            continue;
                        }
                        Element nodeElement = (Element) node;
                        NamedNodeMap m = nodeElement.getAttributes();
                        String href = null, title = null;
                        for (int k = 0; k < m.getLength(); k++) {
                            if ("href".equalsIgnoreCase(m.item(k).getNodeName())) {
                                href = m.item(k).getNodeValue();
                            } else if ("title".equalsIgnoreCase(m.item(k).getNodeName())) {
                                title = m.item(k).getNodeValue();
                            }
                        }
//                            String href = nodeElement.getAttribute("href"); // do not konw why this does not work
//                            String title = nodeElement.getAttribute("title");
                        if (href == null || href.isBlank()) {
                            continue;
                        }
                        String linkAddress = href;
                        try {
                            URL url = new URL(new URL(nodeElement.getBaseURI()), href);
                            linkAddress = url.toString();
                        } catch (Exception e) {
                        }
                        String name = nodeElement.getTextContent();
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
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                table.editHtml();
            }

        };
        start(bgTask, false);
    }

    protected void images() {
        Document doc = webEngine.getDocument();
        if (doc == null) {
            popError(message("NoData"));
            return;
        }
        popInformation(message("Handling..."));
        SingletonTask bgTask = new SingletonTask<Void>(this) {

            private StringTable table;

            @Override
            protected boolean handle() {
                try {
                    NodeList aList = doc.getElementsByTagName("img");
                    if (aList == null || aList.getLength() < 1) {
                        error = message("NoData");
                        return false;
                    }
                    List<String> names = new ArrayList<>();
                    names.addAll(Arrays.asList(message("Index"), message("Link"), message("Name"), message("Title"),
                            message("Address"), message("FullAddress")
                    ));
                    table = new StringTable(names);
                    int index = 1;
                    for (int i = 0; i < aList.getLength(); i++) {
                        org.w3c.dom.Node node = aList.item(i);
                        if (node == null) {
                            continue;
                        }
                        Element nodeElement = (Element) node;
                        NamedNodeMap m = nodeElement.getAttributes();
                        String href = null, name = null;
                        for (int k = 0; k < m.getLength(); k++) {
                            if ("src".equalsIgnoreCase(m.item(k).getNodeName())) {
                                href = m.item(k).getNodeValue();
                            } else if ("alt".equalsIgnoreCase(m.item(k).getNodeName())) {
                                name = m.item(k).getNodeValue();
                            }
                        }
//                        String href = nodeElement.getAttribute("src"); // do not konw why this does not work
//                        String name = nodeElement.getAttribute("alt");
                        if (href == null || href.isBlank()) {
                            continue;
                        }
                        String linkAddress = href;
                        try {
                            URL url = new URL(new URL(nodeElement.getBaseURI()), href);
                            linkAddress = url.toString();
                        } catch (Exception e) {
                        }
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
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                table.editHtml();
            }

        };
        start(bgTask, false);

    }

    protected void toc(String html) {
        if (html == null) {
            popError(message("NoData"));
            return;
        }
        popInformation(message("Handling..."));
        SingletonTask bgTask = new SingletonTask<Void>(this) {

            private String toc;

            @Override
            protected boolean handle() {
                toc = HtmlReadTools.toc(html, 8);
                if (toc == null || toc.isBlank()) {
                    error = message("NoData");
                    return false;
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                TextEditorController c = (TextEditorController) WindowTools.openStage(Fxmls.TextEditorFxml);
                c.loadContents(toc);
                c.toFront();
            }

        };
        start(bgTask, false);
    }

    protected void texts(String html) {
        if (html == null) {
            popError(message("NoData"));
            return;
        }
        popInformation(message("Handling..."));
        SingletonTask bgTask = new SingletonTask<Void>(this) {

            private String texts;

            @Override
            protected boolean handle() {
                texts = HtmlWriteTools.htmlToText(html);
                if (texts == null || texts.isBlank()) {
                    error = message("NoData");
                    return false;
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                TextEditorController c = (TextEditorController) WindowTools.openStage(Fxmls.TextEditorFxml);
                c.loadContents(texts);
                c.toFront();
            }

        };
        start(bgTask, false);
    }

    protected void tables(String html, String title) {
        if (html == null) {
            popError(message("NoData"));
            return;
        }
        popInformation(message("Handling..."));
        SingletonTask bgTask = new SingletonTask<Void>(this) {

            private List<StringTable> tables;

            @Override
            protected boolean handle() {
                tables = HtmlReadTools.Tables(html, title);
                if (tables == null || tables.isEmpty()) {
                    error = message("NoData");
                    return false;
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                DataFileCSVController c = (DataFileCSVController) WindowTools.openStage(Fxmls.DataFileCSVFxml);
                c.loadData(tables);
                c.toFront();
            }

        };
        start(bgTask, false);
    }

    @FXML
    @Override
    public void findAction() {
        find(loadedHtml());
    }

    public void find(String html) {
        HtmlFindController controller = (HtmlFindController) WindowTools.openStage(Fxmls.HtmlFindFxml);
        controller.loadContents(address, html);
        controller.requestMouse();
    }

    @FXML
    @Override
    public void saveAsAction() {
        saveAs(loadedHtml());
    }

    public void saveAs(String html) {
        if (html == null || html.isBlank()) {
            popError(message("NoData"));
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            File file = chooseSaveFile();
            if (file == null) {
                return;
            }
            task = new SingletonTask<Void>(this) {
                @Override
                protected boolean handle() {
                    File tmpFile = HtmlWriteTools.writeHtml(html);
                    if (tmpFile == null || !tmpFile.exists()) {
                        return false;
                    }
                    return FileTools.rename(tmpFile, file);
                }

                @Override
                protected void whenSucceeded() {
                    popSaved();
                    recordFileWritten(file);
                    ControllerTools.openHtmlEditor(null, file);
                }
            };
            start(task);
        }
    }

    @FXML
    @Override
    public void cancelAction() {
        webEngine.getLoadWorker().cancel();
    }

    @FXML
    @Override
    public boolean popAction() {
        HtmlPopController.openWebView(parentController != null ? parentController : myController, webView);
        return true;
    }

    @Override
    public boolean controlAltO() {
        selectNoneAction();
        return true;
    }

    @FXML
    @Override
    public void selectNoneAction() {
        WebViewTools.selectNone(webView.getEngine());
    }

    @Override
    public boolean controlAltU() {
        selectAction();
        return true;
    }

    @FXML
    @Override
    public void selectAction() {
        WebViewTools.selectElement(webView, element);
    }

    @Override
    public boolean controlAltT() {
        copyTextToSystemClipboard();
        return true;
    }

    @FXML
    public boolean copyTextToSystemClipboard() {
        if (webView == null) {
            return false;
        }
        String text = WebViewTools.selectedText(webView.getEngine());
        if (text == null || text.isEmpty()) {
            popError(message("SelectedNone"));
            return false;
        }
        TextClipboardTools.copyToSystemClipboard(myController, text);
        return true;
    }

    @FXML
    public boolean copyTextToMyboxClipboard() {
        if (webView == null) {
            return false;
        }
        String text = WebViewTools.selectedText(webView.getEngine());
        if (text == null || text.isEmpty()) {
            popError(message("SelectedNone"));
            return false;
        }
        TextClipboardTools.copyToMyBoxClipboard(myController, text);
        return true;
    }

    @Override
    public boolean controlAltH() {
        copyHtmlToSystemClipboard();
        return true;
    }

    @FXML
    public boolean copyHtmlToSystemClipboard() {
        if (webView == null) {
            return false;
        }
        String chtml = WebViewTools.selectedHtml(webView.getEngine());
        if (chtml == null || chtml.isEmpty()) {
            popError(message("SelectedNone"));
            return false;
        }
        TextClipboardTools.copyToSystemClipboard(myController, chtml);
        return true;
    }

    @FXML
    public boolean copyHtmlToMyboxClipboard() {
        if (webView == null) {
            return false;
        }
        String chtml = WebViewTools.selectedHtml(webView.getEngine());
        if (chtml == null || chtml.isEmpty()) {
            popError(message("SelectedNone"));
            return false;
        }
        TextClipboardTools.copyToMyBoxClipboard(myController, chtml);
        return true;
    }

    @Override
    public void cleanPane() {
        try {
            if (timer != null) {
                timer.cancel();
            }
            if (webEngine != null && webEngine.getLoadWorker() != null) {
                webEngine.getLoadWorker().cancel();
            }
            if (webView != null) {
                webView.setUserData(null);
            }
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
