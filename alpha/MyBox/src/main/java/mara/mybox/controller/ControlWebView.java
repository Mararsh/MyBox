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
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import static javafx.concurrent.Worker.State.CANCELLED;
import static javafx.concurrent.Worker.State.FAILED;
import static javafx.concurrent.Worker.State.READY;
import static javafx.concurrent.Worker.State.SUCCEEDED;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.PromptData;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebErrorEvent;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.util.Callback;
import mara.mybox.data.HtmlElement;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.ImageClipboard;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ImageClipboardTools;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.fxml.WebViewTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.HtmlReadTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.UrlTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import netscape.javascript.JSObject;
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
    protected String address, contents, style, defaultStyle, initStyle;
    protected Charset charset;
    protected Map<Integer, Document> framesDoc;
    protected EventListener docListener;
    protected Element element;
    protected final SimpleBooleanProperty addressChangedNotify, addressInvalidNotify,
            pageLoadingNotify, pageLoadedNotify;
    protected final String StyleNodeID = "MyBox__Html_Style20211118";
    protected boolean linkInNewTab;

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
        addressChangedNotify = new SimpleBooleanProperty(false);
        addressInvalidNotify = new SimpleBooleanProperty(false);
        pageLoadingNotify = new SimpleBooleanProperty(false);
        pageLoadedNotify = new SimpleBooleanProperty(false);
        linkInNewTab = false;
        defaultStyle = HtmlStyles.styleValue("Default");
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
            MyBoxLog.error(e);
        }
    }

    /*
        init
     */
    public void initWebView() {
        try {
            webEngine = webView.getEngine();

            webView.setCache(false);
            webEngine.setJavaScriptEnabled(true);

            webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
                @Override
                public void changed(ObservableValue ov, Worker.State oldState, Worker.State newState) {
                    worker(newState);
                }
            });

            webView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    mouseClicked(mouseEvent);
                }
            });

            // http://blogs.kiyut.com/tonny/2013/07/30/javafx-webview-addhyperlinklistener/
            docListener = new EventListener() {
                @Override
                public void handleEvent(org.w3c.dom.events.Event ev) {
                    docEvent(ev);
                }
            };

            webEngine.getLoadWorker().exceptionProperty().addListener(new ChangeListener<Throwable>() {
                @Override
                public void changed(ObservableValue<? extends Throwable> ov, Throwable ot, Throwable nt) {
                    if (nt == null) {
                        return;
                    }
                    Platform.runLater(() -> {
                        if (webViewLabel != null) {
                            webViewLabel.setText(nt.getMessage());
                        }
                        alertError(nt.getMessage());
                    });
                }
            });

            webEngine.setOnStatusChanged(new EventHandler<WebEvent<String>>() {
                @Override
                public void handle(WebEvent<String> ev) {
                    statusChanged(ev);
                }
            });

            webEngine.locationProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldv, String newv) {
                    locationChanged(oldv, newv);
                }
            });

            webEngine.setOnAlert(new EventHandler<WebEvent<String>>() {
                @Override
                public void handle(WebEvent<String> ev) {
                    alert(ev);
                }
            });

            webEngine.setOnError(new EventHandler<WebErrorEvent>() {
                @Override
                public void handle(WebErrorEvent event) {
                    error(event);
                }
            });

            webEngine.setPromptHandler(new Callback< PromptData, String>() {
                @Override
                public String call(PromptData p) {
                    return prompt(p);
                }
            });

            webEngine.setConfirmHandler(new Callback< String, Boolean>() {
                @Override
                public Boolean call(String message) {
                    return confirm(message);
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void worker(Worker.State state) {
        Platform.runLater(() -> {
            try {
                switch (state) {
                    case READY:
                        ready();
                        break;
                    case RUNNING:
                        running();
                        break;
                    case SUCCEEDED:
                        succeeded();
                        break;
                    case CANCELLED:
                        if (timer != null) {
                            timer.cancel();
                        }
                        if (webViewLabel != null) {
                            webViewLabel.setText(message("Canceled"));
                        }
                        break;
                    case FAILED:
                        if (timer != null) {
                            timer.cancel();
                        }
                        if (webViewLabel != null) {
                            webViewLabel.setText(message("Failed"));
                        }
                        break;
                    default:
                        if (webViewLabel != null) {
                            webViewLabel.setText(state.name());
                        }
                }
            } catch (Exception e) {
                MyBoxLog.error(e);
            }
        });
    }

    public void mouseClicked(MouseEvent mouseEvent) {
        try {
            linkX = mouseEvent.getScreenX();
            linkY = mouseEvent.getScreenY();
//            MyBoxLog.console(linkX + " " + linkY);
            Platform.runLater(() -> {
                closePopup();
                if (parentController != null) {
                    parentController.closePopup();
                }
            });
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public synchronized void docEvent(org.w3c.dom.events.Event ev) {
        try {
            if (ev == null) {
                return;
            }
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
                String label;
                if ("mouseover".equals(domEventType)) {
                    label = href != null ? URLDecoder.decode(href, charset) : tag;
                } else if ("mouseout".equals(domEventType)) {
                    label = "";
                } else {
                    label = null;
                }
                if (label != null) {
                    Platform.runLater(() -> {
                        webViewLabel.setText(label);
                    });
                }
            }
            if (element == null) {
                return;
            }
//                        MyBoxLog.console(webView.getId() + " " + domEventType + " " + tag + " " + href);
            if (href != null) {
                String target = element.getAttribute("target");

                HtmlElement htmlElement = new HtmlElement(element, charset);
//                            MyBoxLog.console(target);
                if ("click".equals(domEventType)) {
                    if (target != null && !target.equalsIgnoreCase("_blank")) {
                        Platform.runLater(() -> {
                            executeScript("if ( window.frames." + target
                                    + ".document.readyState==\"complete\") control.frameNameReady('" + target + "');");
                            executeScript("window.frames." + target + ".document.onreadystatechange = "
                                    + "function(){ if ( window.frames." + target
                                    + ".document.readyState==\"complete\") control.frameNameReady('" + target + "'); }");
                        });
                    } else if (!href.startsWith("javascript:")) {
                        String clickAction = UserConfig.getString("WebViewWhenClickImageOrLink", "PopMenu");
                        if (linkInNewTab) {
                            ev.preventDefault();
                            WebBrowserController.openAddress(htmlElement.getFinalAddress(), true);
                        } else if (!"AsPage".equals(clickAction)) {
                            ev.preventDefault();
                            if (clickAction == null || "PopMenu".equals(clickAction)) {
                                timer = new Timer();
                                timer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        Platform.runLater(() -> {
                                            popLinkMenu(htmlElement);
                                        });
                                    }
                                }, 100);
                            } else if ("Load".equals(clickAction)) {
                                loadAddress(htmlElement.getFinalAddress());
                            } else {
                                WebBrowserController.openAddress(htmlElement.getFinalAddress(), "OpenSwitch".equals(clickAction));
                            }
                        }
                    }
                } else if ("contextmenu".equals(domEventType)) {
                    ev.preventDefault();
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            Platform.runLater(() -> {
                                popLinkMenu(htmlElement);
                            });
                        }
                    }, 100);
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

    public void statusChanged(WebEvent<String> ev) {
        try {
//            MyBoxLog.console(ev.toString());
            if (webViewLabel != null) {
                Platform.runLater(() -> {
                    webViewLabel.setText(ev.getData());
                });
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void alert(WebEvent<String> ev) {
        try {
//            MyBoxLog.console(ev.toString());
            Platform.runLater(() -> {
                alertInformation(ev.getData());
            });
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void error(WebErrorEvent ev) {
        try {
//            MyBoxLog.console(ev.toString());
            Platform.runLater(() -> {
                alertError(ev.getMessage());
            });
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void locationChanged(String ov, String nv) {
        try {
            if (webViewLabel != null && nv != null) {
                Platform.runLater(() -> {
                    webViewLabel.setText(URLDecoder.decode(nv, charset));
                });
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public String prompt(PromptData p) {
        try {
            return PopTools.askValue(getTitle(), null, p.getMessage(), p.getDefaultValue());
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return null;
    }

    public Boolean confirm(String message) {
        try {
            return PopTools.askSure(getTitle(), message);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    /*
        status
     */
    private void ready() {
        try {
            if (timer != null) {
                timer.cancel();
            }
            framesDoc.clear();
            charset = Charset.defaultCharset();
            pageLoadingNotify.set(!pageLoadingNotify.get());
            Document doc = webEngine.getDocument();
            if (doc != null) {
                EventTarget t = (EventTarget) doc.getDocumentElement();
                t.removeEventListener("contextmenu", docListener, false);
                t.removeEventListener("click", docListener, false);
                t.removeEventListener("mouseover", docListener, false);
                t.removeEventListener("mouseout", docListener, false);
            }
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
    }

    private void running() {
        try {
            // Exceptions happen if add listeners before doc is complete. Give up.
//            timer = new Timer();
//            timer.schedule(new TimerTask() {
//                @Override
//                public void run() {
//                    Platform.runLater(() -> {
//                        initDoc(webEngine.getDocument());
//                    });
//                }
//            }, 300, 1000);
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
    }

    private boolean initDoc(Document doc) {
        try {
            if (doc == null) {
                return false;
            }
            JSObject jsObject = (JSObject) executeScript("window");
            if (jsObject == null) {
                return false;
            }
            jsObject.setMember("control", this);
//            executeScript("if ( document.readyState==\"complete\" || document.readyState==\"interactive\") {alert('setDocListeners');control.setDocListeners();}");
//                        initDoc(webEngine.getDocument());

            setDocListeners(doc);
            return true;
        } catch (Exception e) {
            MyBoxLog.console(e);
            return false;
        }
    }

    public void setDocListeners() {
        try {
            if (timer != null) {
                timer.cancel();
            }
//            MyBoxLog.console(executeScript("document.readyState"));
            Document doc = webEngine.getDocument();
            if (doc == null) {
                return;
            }
            charset = charset();
            EventTarget t = (EventTarget) doc.getDocumentElement();
//            t.removeEventListener("contextmenu", docListener, false);
//            t.removeEventListener("click", docListener, false);
//            t.removeEventListener("mouseover", docListener, false);
//            t.removeEventListener("mouseout", docListener, false);

            MyBoxLog.debug("here");
            t.addEventListener("contextmenu", docListener, false);
            t.addEventListener("click", docListener, false);
            t.addEventListener("mouseover", docListener, false);
            t.addEventListener("mouseout", docListener, false);
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
    }

    private void setDocListeners(Document doc) {
        try {
            if (doc == null) {
                return;
            }
            EventTarget t = (EventTarget) doc.getDocumentElement();
            t.removeEventListener("contextmenu", docListener, false);
            t.removeEventListener("click", docListener, false);
            t.removeEventListener("mouseover", docListener, false);
            t.removeEventListener("mouseout", docListener, false);

            t.addEventListener("contextmenu", docListener, false);
            t.addEventListener("click", docListener, false);
            t.addEventListener("mouseover", docListener, false);
            t.addEventListener("mouseout", docListener, false);
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
    }

    private void succeeded() {
        try {
            if (timer != null) {
                timer.cancel();
            }
            if (initStyle != null) {
                writeStyle(initStyle);
            } else {
                String prefix = UserConfig.getBoolean(baseName + "ShareHtmlStyle", true) ? "AllInterface" : baseName;
                setStyle(UserConfig.getString(prefix + "HtmlStyle", defaultStyle));
            }

            try {
                executeScript("document.body.contentEditable=" + UserConfig.getBoolean("WebViewEditable", false));
            } catch (Exception e) {
            }
            if (null == scrollType) {
                executeScript("setTimeout(window.scrollTo(" + scrollLeft + "," + scrollTop + "), 1000);");
            } else {
                switch (scrollType) {
                    case Bottom:
                        executeScript("setTimeout(window.scrollTo(0, document.documentElement.scrollHeight || document.body.scrollHeight), 1000);");
                        break;
                    case Top:
                        executeScript("window.scrollTo(0, 0);");
                        break;
                    default:
                        executeScript("setTimeout(window.scrollTo(" + scrollLeft + "," + scrollTop + "), 1000);");
                        break;
                }
            }
            setWebViewLabel(message("Loaded"));

            Document doc = webEngine.getDocument();
            if (doc != null) {
                initDoc(doc);
                NodeList frameList = doc.getElementsByTagName("frame");
                for (int i = 0; i < frameList.getLength(); i++) {
                    executeScript("if ( window.frames[" + i + "].document.readyState==\"complete\") control.frameIndexReady(" + i + ");");
                    executeScript("window.frames[" + i + "].document.onreadystatechange = "
                            + "function(){ if ( window.frames[" + i + "].document.readyState==\"complete\") control.frameIndexReady(" + i + "); }");
                }

                executeScript("window.onscroll=function(){ control.scrolled();}");
            }

            pageLoadedNotify.set(!pageLoadedNotify.get());

        } catch (Exception e) {
            MyBoxLog.console(e);
        }
    }

    public void scrolled() {
        try {
            scrollTop = (int) executeScript("document.documentElement.scrollTop || document.body.scrollTop;");
            scrollLeft = (int) executeScript("document.documentElement.scrollLeft || document.body.scrollLeft;");
//            MyBoxLog.console("scrolled:" + scrollTop + " " + scrollLeft);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void frameIndexReady(int frameIndex) {
        try {
//            MyBoxLog.console(frameIndex);
            if (framesDoc.containsKey(frameIndex)) {
                return;
            }
            Timer atimer = new Timer();
            atimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        setFrameListener(frameIndex);
                    });
                }
            }, 500);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void frameNameReady(String frameName) {
        try {
            int frameIndex = WebViewTools.frameIndex(webEngine, frameName);
//            MyBoxLog.console(frameName + "   " + frameIndex);
            frameIndexReady(frameIndex);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void setFrameListener(int frameIndex) {
        try {
            if (frameIndex < 0) {
                return;
            }
            Object c = executeScript("window.frames[" + frameIndex + "].document");
            if (c == null) {
                return;
            }
            Document frame = (Document) c;
            framesDoc.put(frameIndex, frame);

            setDocListeners(frame);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        value
     */
    public Charset charset() {
        try {
            Object head = executeScript("document.head.outerHTML");
            if (head == null) {
                return charset;
            }
            Charset hc = HtmlReadTools.charset((String) head);
            if (hc != null) {
                charset = hc;
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return charset;
    }

    public String loadedHtml() {
        if (contents != null) {
            return contents;
        } else {
            return currentHtml();
        }
    }

    public String currentHtml() {
        return HtmlReadTools.removeNode(WebViewTools.getHtml(webEngine), StyleNodeID);
    }

    public String title() {
        String title = webEngine.getTitle();
        if (title == null || title.isBlank()) {
            title = address;
        }
        return title;
    }

    public void initStyle(String style) {
        this.initStyle = style;
    }

    /*
        source
     */
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
        this.contents = contents;
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
            contents = null;
            setWebViewLabel(message("Loading..."));
            webEngine.getLoadWorker().cancel();
            webEngine.load(address);
        } catch (Exception e) {
            MyBoxLog.error(e);
            addressInvalidNotify.set(!addressInvalidNotify.get());
        }
    }

    /*
        action
     */
    protected void setWebViewLabel(String string) {
        if (webViewLabel != null) {
            webViewLabel.setText(string);
        }
    }

    public Object executeScript(String js) {
        try {
            if (js == null || js.isBlank()) {
                return null;
            }
            return webEngine.executeScript(js);
        } catch (Exception e) {
            MyBoxLog.console(e);
            return null;
        }
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

    public void popLinkMenu(HtmlElement htmlElement) {
        if (linkX < 0 || linkY < 0 || htmlElement == null) {
            return;
        }
        String href = htmlElement.getHref();
        if (href == null) {
            return;
        }
        String linkAddress = htmlElement.getLinkAddress();
        String finalAddress = htmlElement.getFinalAddress();
        String tag = htmlElement.getTag();
        String name = htmlElement.getName();
        List<MenuItem> items = new ArrayList<>();
        boolean showName = name != null && !name.isBlank() && !name.equalsIgnoreCase(href);
        String title = "";
        if (showName) {
            title = message("Name") + ": " + StringTools.menuPrefix(name) + "\n";
        }
        title += message("Link") + ": " + StringTools.menuPrefix(URLDecoder.decode(href, charset));
        if (!linkAddress.equalsIgnoreCase(href)) {
            title += "\n" + message("Address") + ": " + StringTools.menuPrefix(finalAddress);
        }
        MenuItem menu = new MenuItem(title);
        menu.setStyle("-fx-text-fill: #2e598a;");
        items.add(menu);
        items.add(new SeparatorMenuItem());

        if (!linkInNewTab) {
            items.add(clickedMenu());
        }

        menu = new MenuItem(message("QueryNetworkAddress"), StyleTools.getIconImageView("iconQuery.png"));
        menu.setOnAction((ActionEvent event) -> {
            NetworkQueryAddressController controller
                    = (NetworkQueryAddressController) WindowTools.openStage(Fxmls.NetworkQueryAddressFxml);
            controller.queryUrl(finalAddress);
        });
        items.add(menu);

        menu = new MenuItem(message("AddAsFavorite"), StyleTools.getIconImageView("iconStar.png"));
        menu.setOnAction((ActionEvent event) -> {
            WebFavoriteAddController.open(name, finalAddress);
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());

        Menu openMenu = new Menu(message("Open"), StyleTools.getIconImageView("iconWindow.png"));
        items.add(openMenu);

        menu = new MenuItem(message("OpenLinkInNewTab"));
        menu.setOnAction((ActionEvent event) -> {
            WebBrowserController.openAddress(finalAddress, false);
        });
        openMenu.getItems().add(menu);

        menu = new MenuItem(message("OpenLinkInNewTabSwitch"));
        menu.setOnAction((ActionEvent event) -> {
            WebBrowserController.openAddress(finalAddress, true);
        });
        openMenu.getItems().add(menu);

        menu = new MenuItem(message("OpenLinkByCurrent"));
        menu.setOnAction((ActionEvent event) -> {
            loadAddress(finalAddress);
        });
        openMenu.getItems().add(menu);

        menu = new MenuItem(message("OpenLinkBySystem"));
        menu.setOnAction((ActionEvent event) -> {
            browse(finalAddress);
        });
        openMenu.getItems().add(menu);

        menu = new MenuItem(message("Edit"), StyleTools.getIconImageView("iconEdit.png"));
        menu.setOnAction((ActionEvent event) -> {
            HtmlEditorController.openAddress(finalAddress);
        });
        items.add(menu);

        if (tag.equalsIgnoreCase("img")) {
            Menu imageMenu = new Menu(message("Image"), StyleTools.getIconImageView("iconSample.png"));
            items.add(imageMenu);
            if (ImageClipboardTools.isMonitoringCopy()) {
                menu = new MenuItem(message("CopyImageToClipboards"), StyleTools.getIconImageView("iconCopySystem.png"));
            } else {
                menu = new MenuItem(message("CopyImageToSystemClipboard"), StyleTools.getIconImageView("iconCopySystem.png"));
            }
            menu.setOnAction((ActionEvent event) -> {
                handleImage(finalAddress, name, "toSystemClipboard");
            });
            imageMenu.getItems().add(menu);

            menu = new MenuItem(message("CopyImageToMyBoxClipboard"), StyleTools.getIconImageView("iconCopy.png"));
            menu.setOnAction((ActionEvent event) -> {
                handleImage(finalAddress, name, "toMyBoxClipboard");
            });
            imageMenu.getItems().add(menu);

            menu = new MenuItem(message("ViewImage"), StyleTools.getIconImageView("iconView.png"));
            menu.setOnAction((ActionEvent event) -> {
                handleImage(finalAddress, name, "view");
            });
            imageMenu.getItems().add(menu);

            menu = new MenuItem(message("EditImage"), StyleTools.getIconImageView("iconEdit.png"));
            menu.setOnAction((ActionEvent event) -> {
                handleImage(finalAddress, name, "edit");
            });
            imageMenu.getItems().add(menu);
        }

        Menu dlMenu = new Menu(message("Download"), StyleTools.getIconImageView("iconDownload.png"));
        items.add(dlMenu);
        menu = new MenuItem(message("DownloadBySysBrowser"), StyleTools.getIconImageView("iconDownload.png"));
        menu.setOnAction((ActionEvent event) -> {
            browse(finalAddress);
        });
        dlMenu.getItems().add(menu);

        menu = new MenuItem(message("DownloadByMyBox"), StyleTools.getIconImageView("iconDownload.png"));
        menu.setOnAction((ActionEvent event) -> {
            WebBrowserController controller = WebBrowserController.oneOpen();
            controller.download(finalAddress, name);
        });
        dlMenu.getItems().add(menu);

        Menu copyMenu = new Menu(message("Copy"), StyleTools.getIconImageView("iconCopySystem.png"));
        items.add(copyMenu);

        menu = new MenuItem(message("CopyLink"));
        menu.setOnAction((ActionEvent event) -> {
            TextClipboardTools.copyToSystemClipboard(myController, finalAddress);
        });
        copyMenu.getItems().add(menu);

        if (showName) {
            menu = new MenuItem(message("CopyLinkName"));
            menu.setOnAction((ActionEvent event) -> {
                TextClipboardTools.copyToSystemClipboard(myController, name);
            });
            copyMenu.getItems().add(menu);

            menu = new MenuItem(message("CopyLinkAndName"));
            menu.setOnAction((ActionEvent event) -> {
                TextClipboardTools.copyToSystemClipboard(myController, name + "\n" + finalAddress);
            });
            copyMenu.getItems().add(menu);
        }

        menu = new MenuItem(message("CopyLinkCode"));
        menu.setOnAction((ActionEvent event) -> {
            String code;
            if (tag.equalsIgnoreCase("img")) {
                code = "<img src=\"" + finalAddress + "\" " + (name == null || name.isBlank() ? "" : " alt=\"" + name + "\"") + " />";
            } else {
                code = "<a href=\"" + finalAddress + "\">" + (name == null || name.isBlank() ? finalAddress : name) + "</a>";
            }
            TextClipboardTools.copyToSystemClipboard(myController, code);
        });
        copyMenu.getItems().add(menu);

        closePopup();
        popMenu(webView, items, linkX, linkY);
        if (parentController != null) {
            parentController.closePopup();
            parentController.setPopMenu(popMenu);
        }
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
                        File imageFile = HtmlReadTools.url2image(address, name);
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
                            ImageManufactureController.openImage(image);
                            break;
                        default:
                            ImageViewerController.openImage(image);
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
        executeScript("window.history.back();");
    }

    public void forwardAction() {
        executeScript("window.history.forward();");
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

//            int hisSize = (int) executeScript("window.history.length;");
            menu = new MenuItem(message("Backward"), StyleTools.getIconImageView("iconPrevious.png"));
            menu.setOnAction((ActionEvent event) -> {
                backAction();
            });
//            menu.setDisable(hisSize < 2);
            items.add(menu);

            menu = new MenuItem(message("Forward"), StyleTools.getIconImageView("iconNext.png"));
            menu.setOnAction((ActionEvent event) -> {
                forwardAction();
            });
//            menu.setDisable(hisSize < 2);
            items.add(menu);

            menu = new MenuItem(message("ZoomIn"), StyleTools.getIconImageView("iconZoomIn.png"));
            menu.setOnAction((ActionEvent event) -> {
                zoomIn();
            });
            items.add(menu);

            menu = new MenuItem(message("ZoomOut"), StyleTools.getIconImageView("iconZoomOut.png"));
            menu.setOnAction((ActionEvent event) -> {
                zoomOut();
            });
            items.add(menu);

            menu = new MenuItem(message("Refresh"), StyleTools.getIconImageView("iconRefresh.png"));
            menu.setOnAction((ActionEvent event) -> {
                refresh();
            });
            items.add(menu);

            menu = new MenuItem(message("Snapshot"), StyleTools.getIconImageView("iconSnapshot.png"));
            menu.setOnAction((ActionEvent event) -> {
                snapAction();
            });
            items.add(menu);

            menu = new MenuItem(message("Cancel"), StyleTools.getIconImageView("iconCancel.png"));
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
                menu = new MenuItem(StringTools.menuPrefix(address));
                menu.setStyle("-fx-text-fill: #2e598a;");
                items.add(menu);
                items.add(new SeparatorMenuItem());
            }

            items.addAll(operationsMenu());

            popMouseMenu(mouseEvent, items);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public Menu clickedMenu() {
        try {
            Menu clickMenu = new Menu(message("WhenClickImageOrLink"), StyleTools.getIconImageView("iconSelect.png"));
            ToggleGroup clickGroup = new ToggleGroup();
            String currentClick = UserConfig.getString("WebViewWhenClickImageOrLink", "PopMenu");

            RadioMenuItem clickPopMenu = new RadioMenuItem(message("PopMenu"), StyleTools.getIconImageView("iconMenu.png"));
            clickPopMenu.setSelected(currentClick == null || "PopMenu".equals(currentClick));
            clickPopMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setString("WebViewWhenClickImageOrLink", "PopMenu");
                }
            });
            clickPopMenu.setToggleGroup(clickGroup);

            RadioMenuItem clickAsPageMenu = new RadioMenuItem(message("HandleAsPage"), StyleTools.getIconImageView("iconHtml.png"));
            clickAsPageMenu.setSelected("AsPage".equals(currentClick));
            clickAsPageMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setString("WebViewWhenClickImageOrLink", "AsPage");
                }
            });
            clickAsPageMenu.setToggleGroup(clickGroup);

            RadioMenuItem clickOpenSwitchMenu = new RadioMenuItem(message("OpenLinkInNewTabSwitch"), StyleTools.getIconImageView("iconWindow.png"));
            clickOpenSwitchMenu.setSelected("OpenSwitch".equals(currentClick));
            clickOpenSwitchMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setString("WebViewWhenClickImageOrLink", "OpenSwitch");
                }
            });
            clickOpenSwitchMenu.setToggleGroup(clickGroup);

            RadioMenuItem clickOpenMenu = new RadioMenuItem(message("OpenLinkInNewTab"), StyleTools.getIconImageView("iconWindow.png"));
            clickOpenMenu.setSelected("Open".equals(currentClick));
            clickOpenMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setString("WebViewWhenClickImageOrLink", "Open");
                }
            });
            clickOpenMenu.setToggleGroup(clickGroup);

            RadioMenuItem clickLoadMenu = new RadioMenuItem(message("OpenLinkByCurrent"), StyleTools.getIconImageView("iconWindow.png"));
            clickLoadMenu.setSelected("Load".equals(currentClick));
            clickLoadMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setString("WebViewWhenClickImageOrLink", "Load");
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
                menu = new MenuItem(StringTools.menuPrefix(address));
                menu.setStyle("-fx-text-fill: #2e598a;");
                items.add(menu);
                items.add(new SeparatorMenuItem());
            }

            Menu operationsMenu = new Menu(message("Operations"), StyleTools.getIconImageView("iconAsterisk.png"));
            operationsMenu.getItems().setAll(operationsMenu());
            items.add(operationsMenu);

            items.add(new SeparatorMenuItem());

            Menu viewMenu = new Menu(message("View"), StyleTools.getIconImageView("iconView.png"));
            items.add(viewMenu);

            if (address != null && !address.isBlank()) {
                menu = new MenuItem(message("AddAsFavorite"), StyleTools.getIconImageView("iconStar.png"));
                menu.setOnAction((ActionEvent event) -> {
                    WebFavoriteAddController.open(title(), address);
                });
                viewMenu.getItems().add(menu);
            }

            menu = new MenuItem(message("WebFavorites"), StyleTools.getIconImageView("iconStarFilled.png"));
            menu.setOnAction((ActionEvent event) -> {
                WebFavoritesController.oneOpen();
            });
            viewMenu.getItems().add(menu);

            menu = new MenuItem(message("WebHistories"), StyleTools.getIconImageView("iconHistory.png"));
            menu.setOnAction((ActionEvent event) -> {
                WebHistoriesController.oneOpen();
            });
            viewMenu.getItems().add(menu);

            if (address != null && !address.isBlank()) {
                menu = new MenuItem(message("QueryNetworkAddress"), StyleTools.getIconImageView("iconSSL.png"));
                menu.setOnAction((ActionEvent event) -> {
                    NetworkQueryAddressController controller
                            = (NetworkQueryAddressController) WindowTools.openStage(Fxmls.NetworkQueryAddressFxml);
                    controller.queryUrl(address);
                });
                viewMenu.getItems().add(menu);
            }

            viewMenu.getItems().add(new SeparatorMenuItem());

            if (html != null && !html.isBlank()) {
                menu = new MenuItem(message("HtmlSnap"), StyleTools.getIconImageView("iconSnapshot.png"));
                menu.setOnAction((ActionEvent event) -> {
                    HtmlSnapController controller = (HtmlSnapController) WindowTools.openStage(Fxmls.HtmlSnapFxml);
                    if (address != null && !address.isBlank()) {
                        controller.loadAddress(address);
                    } else {
                        controller.loadContents(html);
                    }
                });
                viewMenu.getItems().add(menu);

                menu = new MenuItem(message("WebFind"), StyleTools.getIconImageView("iconQuery.png"));
                menu.setOnAction((ActionEvent event) -> {
                    find(html);
                });
                viewMenu.getItems().add(menu);

                viewMenu.getItems().add(new SeparatorMenuItem());

                if (address != null && !address.isBlank()) {
                    menu = new MenuItem(message("OpenLinkBySystem"), StyleTools.getIconImageView("iconWindow.png"));
                    menu.setOnAction((ActionEvent event) -> {
                        browse(address);
                    });
                    viewMenu.getItems().add(menu);
                }

                menu = new MenuItem(message("OpenLinkInNewTabSwitch"), StyleTools.getIconImageView("iconWindow.png"));
                menu.setOnAction((ActionEvent event) -> {
                    if (address != null && !address.isBlank()) {
                        WebBrowserController.openAddress(address, true);
                    } else {
                        WebBrowserController.openHtml(html, true);
                    }
                });
                viewMenu.getItems().add(menu);

                menu = new MenuItem(message("OpenLinkInNewTab"), StyleTools.getIconImageView("iconWindow.png"));
                menu.setOnAction((ActionEvent event) -> {
                    if (address != null && !address.isBlank()) {
                        WebBrowserController.openAddress(address, false);
                    } else {
                        WebBrowserController.openHtml(html, false);
                    }
                });
                viewMenu.getItems().add(menu);

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
                                if (src != null && !src.isBlank()) {
                                    WebBrowserController.openAddress(UrlTools.fullAddress(address, src), true);
                                } else {
                                    WebBrowserController.openHtml(WebViewTools.getFrame(webEngine, index), true);
                                }

                            });
                            frameItems.add(menu);
                        }
                        if (!frameItems.isEmpty()) {
                            Menu frameMenu = new Menu(message("Frame"), StyleTools.getIconImageView("iconRef.png"));
                            frameMenu.getItems().addAll(frameItems);
                            items.add(frameMenu);
                        }
                    }
                }

                Menu codesMenu = new Menu(message("Analyse"), StyleTools.getIconImageView("iconAnalyse.png"));
                items.add(codesMenu);

                menu = new MenuItem(message("HtmlEditor"), StyleTools.getIconImageView("iconEdit.png"));
                menu.setOnAction((ActionEvent event) -> {
                    edit(address, html);
                });
                codesMenu.getItems().add(menu);

                menu = new MenuItem(message("HtmlCodes"), StyleTools.getIconImageView("iconMeta.png"));
                menu.setOnAction((ActionEvent event) -> {
                    HtmlCodesPopController.openWebView(myController, webView);
                });
                codesMenu.getItems().add(menu);

                menu = new MenuItem(message("WebElements"), StyleTools.getIconImageView("iconQuery.png"));
                menu.setOnAction((ActionEvent event) -> {
                    HtmlElementsController controller = (HtmlElementsController) WindowTools.openStage(Fxmls.HtmlElementsFxml);
                    if (address != null && !address.isBlank()) {
                        controller.loadAddress(address);
                    } else {
                        controller.loadContents(html);
                    }
                    controller.requestMouse();
                });
                codesMenu.getItems().add(menu);

                menu = new MenuItem(message("Script"), StyleTools.getIconImageView("iconScript.png"));
                menu.setOnAction((ActionEvent event) -> {
                    JavaScriptController.open(this);
                });
                codesMenu.getItems().add(menu);

                Menu extractMenu = new Menu(message("Extract"), StyleTools.getIconImageView("iconExport.png"));
                items.add(extractMenu);

                menu = new MenuItem(message("Table"), StyleTools.getIconImageView("iconData.png"));
                menu.setOnAction((ActionEvent event) -> {
                    tables(html, sourceFile != null ? sourceFile.getName() : address);
                });
                menu.setDisable(isFrameset);
                extractMenu.getItems().add(menu);

                menu = new MenuItem(message("Texts"), StyleTools.getIconImageView("iconTxt.png"));
                menu.setOnAction((ActionEvent event) -> {
                    texts(html);
                });
                menu.setDisable(isFrameset);
                extractMenu.getItems().add(menu);

                menu = new MenuItem(message("Links"), StyleTools.getIconImageView("iconLink.png"));
                menu.setOnAction((ActionEvent event) -> {
                    links();
                });
                menu.setDisable(isFrameset || doc == null);
                extractMenu.getItems().add(menu);

                menu = new MenuItem(message("Images"), StyleTools.getIconImageView("iconSample.png"));
                menu.setOnAction((ActionEvent event) -> {
                    images();
                });
                menu.setDisable(isFrameset || doc == null);
                extractMenu.getItems().add(menu);

                menu = new MenuItem(message("Headings"), StyleTools.getIconImageView("iconHeader.png"));
                menu.setOnAction((ActionEvent event) -> {
                    toc(html);
                });
                menu.setDisable(isFrameset);
                extractMenu.getItems().add(menu);

                items.add(new SeparatorMenuItem());

                menu = new MenuItem(message("SaveAs"), StyleTools.getIconImageView("iconSaveAs.png"));
                menu.setOnAction((ActionEvent event) -> {
                    saveAsAction();
                });
                items.add(menu);

            }

            CheckMenuItem editableMenu = new CheckMenuItem(message("Editable"), StyleTools.getIconImageView("iconEdit.png"));
            editableMenu.setSelected(UserConfig.getBoolean("WebViewEditable", false));
            editableMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    setEditable(editableMenu.isSelected());
                }
            });
            items.add(editableMenu);

            if (!linkInNewTab) {
                items.add(clickedMenu());
            }

            popMouseMenu(mouseEvent, items);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setEditable(boolean e) {
        UserConfig.setBoolean("WebViewEditable", e);
        executeScript("document.body.contentEditable=" + e);
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
                        return true;
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
                if (table == null) {
                    popInformation(message("NoData"));
                } else {
                    table.htmlTable();
                }
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
                        return true;
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
                if (table == null) {
                    popInformation(message("NoData"));
                } else {
                    table.htmlTable();
                }
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
                return true;
            }

            @Override
            protected void whenSucceeded() {
                if (toc == null || toc.isBlank()) {
                    popInformation(message("NoData"));
                } else {
                    TextEditorController c = (TextEditorController) WindowTools.openStage(Fxmls.TextEditorFxml);
                    c.loadContents(toc);
                    c.toFront();
                }
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
                return true;
            }

            @Override
            protected void whenSucceeded() {
                if (texts == null || texts.isBlank()) {
                    popInformation(message("NoData"));
                } else {
                    TextEditorController c = (TextEditorController) WindowTools.openStage(Fxmls.TextEditorFxml);
                    c.loadContents(texts);
                    c.toFront();
                }
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
                return true;
            }

            @Override
            protected void whenSucceeded() {
                if (tables == null || tables.isEmpty()) {
                    popInformation(message("NoData"));
                } else {
                    DataFileCSVController c = (DataFileCSVController) WindowTools.openStage(Fxmls.DataFileCSVFxml);
                    c.loadData(tables);
                    c.toFront();
                }
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
                    WebBrowserController.openFile(file);
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

    @FXML
    @Override
    public boolean menuAction() {
        try {
            Point2D localToScreen = webView.localToScreen(webView.getWidth() - 80, 80);
            MenuWebviewController.pop(this, null, localToScreen.getX(), localToScreen.getY());
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    @FXML
    public void snapAction() {
        ImageViewerController.openImage(NodeTools.snap(webView));
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
