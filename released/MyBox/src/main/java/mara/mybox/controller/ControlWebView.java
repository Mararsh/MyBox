package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
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
import static javafx.concurrent.Worker.State.RUNNING;
import static javafx.concurrent.Worker.State.SUCCEEDED;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
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
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.ImageClipboardTools;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.fxml.WebViewTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.HtmlStyles;
import static mara.mybox.fxml.style.NodeStyleTools.attributeTextStyle;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.image.file.ImageFileReaders;
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
    protected double scrollTop, scrollLeft;
    protected ScrollType scrollType;
    protected float zoomScale;
    protected String address, content, style, defaultStyle, initStyle;
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
        addressChangedNotify = new SimpleBooleanProperty(false);
        addressInvalidNotify = new SimpleBooleanProperty(false);
        pageLoadingNotify = new SimpleBooleanProperty(false);
        pageLoadedNotify = new SimpleBooleanProperty(false);
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            zoomScale = 1.0f;
            framesDoc = new HashMap<>();
            charset = Charset.defaultCharset();
            linkInNewTab = false;
            defaultStyle = HtmlStyles.styleValue("Table");
            scrollType = ScrollType.Top;
            parentController = this;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
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
        this.baseName = parent.baseName + "_" + baseName;
        this.scrollType = scrollType;
    }

    @Override
    public void initControls() {
        try {
            webEngine = webView.getEngine();

            webView.setCache(UserConfig.getBoolean(interfaceName + "Cache", false));
            webEngine.setJavaScriptEnabled(UserConfig.getBoolean(interfaceName + "JavaScriptEnabled", true));

            webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
                @Override
                public void changed(ObservableValue ov, Worker.State oldState, Worker.State newState) {
                    Platform.runLater(() -> {
                        worker(newState);
                    });
                    Platform.requestNextPulse();
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
                        setWebViewLabel(nt.getMessage());
                        alertError(nt.getMessage());
                    });
                }
            });

            webEngine.setOnStatusChanged(new EventHandler<WebEvent<String>>() {
                @Override
                public void handle(WebEvent<String> ev) {
                    Platform.runLater(() -> {
                        statusChanged(ev);
                    });
                }
            });

            webEngine.locationProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldv, String newv) {
                    Platform.runLater(() -> {
                        locationChanged(oldv, newv);
                    });
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
            MyBoxLog.error(e);
        }
    }

    public void worker(Worker.State state) {
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
                    setWebViewLabel(message("Canceled"));
                    break;
                case FAILED:
                    if (timer != null) {
                        timer.cancel();
                    }
                    setWebViewLabel(message("Failed"));
                    break;
                default:
                    setWebViewLabel(state.name());
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void docEvent(org.w3c.dom.events.Event ev) {
        try {
            if (ev == null) {
                return;
            }
            String domEventType = ev.getType();
            String tag = null, href = null;
            if (ev.getTarget() != null) {
                element = (Element) ev.getTarget();
                HtmlElement htmlElement = new HtmlElement(element, charset);
                tag = htmlElement.getTag();
                if (tag != null) {
                    href = htmlElement.getHref();
                }
            } else {
                element = null;
            }
//            MyBoxLog.console(webView.getId() + " " + domEventType + " " + tag + " " + href);
            if (element == null) {
                return;
            }
            if (href != null) {
                String target = element.getAttribute("target");
                HtmlElement htmlElement = new HtmlElement(element, charset);
                if ("click".equals(domEventType)) {
                    if (target != null && !target.equalsIgnoreCase("_blank")) {
                        ev.stopPropagation();
                        ev.preventDefault();
                        Platform.runLater(() -> {
                            executeScript("if ( window.frames." + target
                                    + ".document.readyState==\"complete\") control.frameNameReady('" + target + "');");
                            executeScript("window.frames." + target + ".document.onreadystatechange = "
                                    + "function(){ if ( window.frames." + target
                                    + ".document.readyState==\"complete\") control.frameNameReady('" + target + "'); }");
                        });
                        Platform.requestNextPulse();
                    } else if (!href.startsWith("javascript:")) {
                        String clickAction = UserConfig.getString("WebViewWhenLeftClickImageOrLink", "PopMenu");
                        String url = htmlElement.getDecodedAddress();
                        if (linkInNewTab) {
                            ev.stopPropagation();
                            ev.preventDefault();
                            Platform.runLater(() -> {
                                WebBrowserController.openAddress(url, true);
                            });
                            Platform.requestNextPulse();
                        } else if (!"AsPage".equals(clickAction)) {
                            ev.stopPropagation();
                            ev.preventDefault();
                            Platform.runLater(() -> {
                                if (clickAction == null || "PopMenu".equals(clickAction)) {
                                    popLinkMenu(htmlElement);
                                } else if ("Load".equals(clickAction)) {
                                    loadAddress(url);
                                } else if ("System".equals(clickAction)) {
                                    browse(url);
                                } else {
                                    WebBrowserController.openAddress(url, "OpenSwitch".equals(clickAction));
                                }
                            });
                            Platform.requestNextPulse();
                        }
                    }
                } else if ("contextmenu".equals(domEventType)) {
                    ev.stopPropagation();
                    ev.preventDefault();
                    Platform.runLater(() -> {
                        popLinkMenu(htmlElement);
                    });
                    Platform.requestNextPulse();
                }

            } else if ("contextmenu".equals(domEventType) && !"frame".equalsIgnoreCase(tag)) {
                ev.stopPropagation();
                ev.preventDefault();
                Platform.runLater(() -> {
                    popElementMenu(element);
                });
                Platform.requestNextPulse();
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
            setWebViewLabel(ev.getData());
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void locationChanged(String ov, String nv) {
        try {
            if (webViewLabel != null && nv != null) {
                Platform.runLater(() -> {
                    setWebViewLabel(URLDecoder.decode(nv, charset));
                });
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void alert(WebEvent<String> ev) {
        try {
            if (UserConfig.getBoolean("WebViewInterceptPopWindow", false)) {
                return;
            }
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
            if (UserConfig.getBoolean("WebViewInterceptPopWindow", false)) {
                return;
            }
//            MyBoxLog.console(ev.toString());
            Platform.runLater(() -> {
                alertError(ev.getMessage());
            });
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public String prompt(PromptData p) {
        try {
            if (UserConfig.getBoolean("WebViewInterceptPopWindow", false)) {
                return null;
            }
            return PopTools.askValue(getTitle(), null, p.getMessage(), p.getDefaultValue());
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return null;
    }

    public Boolean confirm(String message) {
        try {
            if (UserConfig.getBoolean("WebViewInterceptPopWindow", false)) {
                return false;
            }
            return PopTools.askSure(getTitle(), message);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    /*
        status
     */
    public void clear() {
        loadContent("");
    }

    protected void reset() {
        if (timer != null) {
            timer.cancel();
        }
        content = null;
        webEngine.getLoadWorker().cancel();
//        clearListener(webEngine.getDocument());
    }

    protected void ready() {
        try {
            if (timer != null) {
                timer.cancel();
            }
            framesDoc.clear();
            charset = Charset.defaultCharset();
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
    }

    protected void running() {
        try {
            pageLoadingNotify.set(!pageLoadingNotify.get());
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        initDoc(webEngine.getDocument());
                    });
                    Platform.requestNextPulse();
                }
            }, 300, 100);
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
    }

    protected boolean initDoc(Document doc) {
        try {
            if (doc == null) {
                return false;
            }
            Object winObject = executeScript("window");
            Object docObject = executeScript("document");
            if (winObject == null || docObject == null) {
                return false;
            }
            if (timer != null) {
                timer.cancel();
            }
            ((JSObject) winObject).setMember("control", this);
            setListeners(doc);

            return true;
        } catch (Exception e) {
            MyBoxLog.console(e);
            return false;
        }
    }

    protected void setListeners(Document doc) {
        try {
            if (doc == null) {
                return;
            }
            String js = "if ( document.addEventListener ) "
                    + "{  document.addEventListener('mouseover', (event) => {\n"
                    + "      const link = event.target.closest('a');\n"
                    + "      if (link && link.href) {\n"
                    + "        try {\n"
                    + "              const url = new URL(link.href);\n"
                    + "              control.setWebViewLabel(url.href);\n"
                    + "        } catch (error) {}\n"
                    + "      }\n"
                    + "  });"
                    + "  document.addEventListener('mouseout', (event) => {\n"
                    + "     control.setWebViewLabel(null);})\n"
                    + "} ";
            executeScript(js);

            EventTarget t = (EventTarget) doc.getDocumentElement();
            t.addEventListener("contextmenu", docListener, true);
            t.addEventListener("click", docListener, true);

        } catch (Exception e) {
            MyBoxLog.console(e);
        }
    }

    protected void succeeded() {
        try {
            if (timer != null) {
                timer.cancel();
            }
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

            if (initStyle != null) {
                writeStyle(initStyle);
            } else {
                String prefix = UserConfig.getBoolean(baseName + "ShareHtmlStyle", true) ? "AllInterface" : baseName;
                writeStyle(UserConfig.getString(prefix + "HtmlStyle", defaultStyle));
            }

            try {
                executeScript("document.body.contentEditable=" + UserConfig.getBoolean(baseName + "Editable", false));
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

            setListeners(frame);
        } catch (Exception e) {
            MyBoxLog.error(e);
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
        if (content != null) {
            return content;
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

    public boolean hasHtml() {
        String html = loadedHtml();
        return html != null && !html.isBlank();
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

    public boolean loadContent(String contents) {
        return loadContent(null, contents);
    }

    public boolean loadContent(String address, String content) {
        setAddress(address);
        writeContent(content);
        return true;
    }

    public void writeContent(String content) {
        reset();
        this.content = content;
        webEngine.loadContent(content == null ? "" : content);
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
            reset();
            setWebViewLabel(message("Loading..."));
            webEngine.load(address);
        } catch (Exception e) {
            MyBoxLog.error(e);
            addressInvalidNotify.set(!addressInvalidNotify.get());
        }
    }

    /*
        action
     */
    public void setWebViewLabel(String string) {
        if (webViewLabel != null) {
            Platform.runLater(() -> {
                if (string == null || string.isBlank()) {
                    webViewLabel.setText("");
                } else {
                    webViewLabel.setText(URLDecoder.decode(string, charset));
                }
            });
        }
    }

    public Object executeScript(String js) {
        try {
            if (js == null || js.isBlank()) {
                return null;
            }
            return webEngine.executeScript(js);
        } catch (Exception e) {
            MyBoxLog.console(e + "\n" + js);
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
        if (htmlElement == null) {
            return;
        }
        String href = htmlElement.getHref();
        if (href == null) {
            return;
        }
        String linkAddress = htmlElement.getAddress();
        String finalAddress = htmlElement.getDecodedAddress();
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
        menu.setStyle(attributeTextStyle());
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
            ControlDataWebFavorite.open(myController, name, finalAddress);
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

        items.add(new SeparatorMenuItem());

        closePopup();
        popNodeMenu(webView, items);
        if (parentController != null) {
            parentController.closePopup();
            parentController.setPopMenu(popMenu);
        }
    }

    public void popElementMenu(Element element) {
        MenuWebviewController.webviewMenu(this, element);
    }

    public void handleImage(String address, String name, String target) {
        if (address == null || target == null) {
            return;
        }
        popInformation(message("Handling..."));
        FxTask bgTask = new FxTask<Void>(this) {

            private Image image = null;

            @Override
            protected boolean handle() {
                try {
                    File imageFile = HtmlReadTools.url2image(this, address, name);
                    BufferedImage bi = ImageFileReaders.readImage(this, imageFile);
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
                        ImageEditorController.openImage(image);
                        break;
                    default:
                        ImageEditorController.openImage(image);
                }
            }

        };
        start(bgTask, false);
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
            loadContent(loadedHtml());
        }
    }

    @FXML
    @Override
    public void popOperationsMenu(Event event) {
        if (UserConfig.getBoolean("WebviewOperationsPopWhenMouseHovering", true)) {
            showOperationsMenu(event);
        }
    }

    @FXML
    @Override
    public void showOperationsMenu(Event event) {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            if (address != null && !address.isBlank()) {
                menu = new MenuItem(StringTools.menuPrefix(address));
                menu.setStyle(attributeTextStyle());
                items.add(menu);
                items.add(new SeparatorMenuItem());
            }

            items.addAll(operationsMenu());

            items.add(new SeparatorMenuItem());

            CheckMenuItem popItem = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
            popItem.setSelected(UserConfig.getBoolean("WebviewOperationsPopWhenMouseHovering", true));
            popItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean("WebviewOperationsPopWhenMouseHovering", popItem.isSelected());
                }
            });
            items.add(popItem);

            popEventMenu(event, items);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public List<MenuItem> operationsMenu() {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            String html = loadedHtml();
            boolean hasAddress = address != null && !address.isBlank();
            boolean hasHtml = html != null && !html.isBlank();

            if (hasAddress) {
                menu = new MenuItem(message("AddAsFavorite"), StyleTools.getIconImageView("iconStar.png"));
                menu.setOnAction((ActionEvent event) -> {
                    ControlDataWebFavorite.open(myController, title(), address);
                });
                items.add(menu);
            }

            menu = new MenuItem(message("WebFavorites"), StyleTools.getIconImageView("iconStar.png"));
            menu.setOnAction((ActionEvent event) -> {
                DataTreeController.webFavorite(myController, false);
            });
            items.add(menu);

            menu = new MenuItem(message("WebHistories"), StyleTools.getIconImageView("iconHistory.png"));
            menu.setOnAction((ActionEvent event) -> {
                WebHistoriesController.oneOpen();
            });
            items.add(menu);

            if (hasAddress) {
                menu = new MenuItem(message("CopyLink"), StyleTools.getIconImageView("iconCopySystem.png"));
                menu.setOnAction((ActionEvent event) -> {
                    TextClipboardTools.copyToSystemClipboard(myController, address);
                });
                items.add(menu);

                menu = new MenuItem(message("QueryNetworkAddress"), StyleTools.getIconImageView("iconSSL.png"));
                menu.setOnAction((ActionEvent event) -> {
                    NetworkQueryAddressController controller
                            = (NetworkQueryAddressController) WindowTools.openStage(Fxmls.NetworkQueryAddressFxml);
                    controller.queryUrl(address);
                });
                items.add(menu);

            }

            items.add(new SeparatorMenuItem());

            if (hasHtml) {
                menu = new MenuItem(message("HtmlSnap"), StyleTools.getIconImageView("iconSnapshot.png"));
                menu.setOnAction((ActionEvent event) -> {
                    snapHtml();
                });
                items.add(menu);

                menu = new MenuItem(message("SnapshotWindow"), StyleTools.getIconImageView("iconSnapshot.png"));
                menu.setOnAction((ActionEvent event) -> {
                    snapAction();
                });
                items.add(menu);

                menu = new MenuItem(message("WebFind"), StyleTools.getIconImageView("iconQuery.png"));
                menu.setOnAction((ActionEvent event) -> {
                    find(html);
                });
                items.add(menu);

                items.add(new SeparatorMenuItem());

                menu = new MenuItem(message("OpenLinkInNewTabSwitch"), StyleTools.getIconImageView("iconWindow.png"));
                menu.setOnAction((ActionEvent event) -> {
                    if (address != null && !address.isBlank()) {
                        WebBrowserController.openAddress(address, true);
                    } else {
                        WebBrowserController.openHtml(html, true);
                    }
                });
                items.add(menu);

                menu = new MenuItem(message("OpenLinkInNewTab"), StyleTools.getIconImageView("iconWindow.png"));
                menu.setOnAction((ActionEvent event) -> {
                    if (address != null && !address.isBlank()) {
                        WebBrowserController.openAddress(address, false);
                    } else {
                        WebBrowserController.openHtml(html, false);
                    }
                });
                items.add(menu);

                if (hasAddress) {
                    menu = new MenuItem(message("OpenLinkBySystem"), StyleTools.getIconImageView("iconSystemOpen.png"));
                    menu.setOnAction((ActionEvent event) -> {
                        browse(address);
                    });
                    items.add(menu);
                }

            }
            items.add(new SeparatorMenuItem());

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

            menu = new MenuItem(message("Cancel"), StyleTools.getIconImageView("iconCancel.png"));
            menu.setOnAction((ActionEvent event) -> {
                cancelAction();
            });
            items.add(menu);

            return items;

        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @FXML
    @Override
    public void popFunctionsMenu(Event event) {
        if (UserConfig.getBoolean("WebviewFunctionsPopWhenMouseHovering", true)) {
            showFunctionsMenu(event);
        }
    }

    @FXML
    @Override
    public void showFunctionsMenu(Event fevent) {
        try {
            List<MenuItem> items = functionsMenu(fevent);

            CheckMenuItem popItem = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
            popItem.setSelected(UserConfig.getBoolean("WebviewFunctionsPopWhenMouseHovering", true));
            popItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean("WebviewFunctionsPopWhenMouseHovering", popItem.isSelected());
                }
            });
            items.add(popItem);

            popEventMenu(fevent, items);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public List<MenuItem> functionsMenu(Event fevent) {
        try {
            String html = loadedHtml();
            Document doc = webEngine.getDocument();
            boolean isFrameset = framesDoc != null && !framesDoc.isEmpty();
            boolean hasAddress = address != null && !address.isBlank();
            boolean hasHtml = html != null && !html.isBlank();

            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            if (hasAddress) {
                menu = new MenuItem(StringTools.menuPrefix(address));
                menu.setStyle(attributeTextStyle());
                items.add(menu);
                items.add(new SeparatorMenuItem());
            }

            Menu operationsMenu = new Menu(message("Operations"), StyleTools.getIconImageView("iconOperation.png"));
            operationsMenu.getItems().setAll(operationsMenu());
            items.add(operationsMenu);

            items.add(new SeparatorMenuItem());

            if (hasHtml) {

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
                            Menu frameMenu = new Menu(message("Frame"), StyleTools.getIconImageView("iconMove.png"));
                            frameMenu.getItems().addAll(frameItems);
                            items.add(frameMenu);
                        }
                    }
                }

                menu = new MenuItem(message("HtmlEditor"), StyleTools.getIconImageView("iconEdit.png"));
                menu.setOnAction((ActionEvent event) -> {
                    edit(address, html);
                });
                items.add(menu);

                menu = new MenuItem(message("HtmlCodes"), StyleTools.getIconImageView("iconMeta.png"));
                menu.setOnAction((ActionEvent event) -> {
                    htmlCodes();
                });
                items.add(menu);

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
                items.add(menu);

                menu = new MenuItem(message("Script"), StyleTools.getIconImageView("iconScript.png"));
                menu.setOnAction((ActionEvent event) -> {
                    HtmlJavaScriptController.open(parentController, this);
                });
                items.add(menu);

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

                if (sourceFile != null) {
                    menu = new MenuItem(message("OpenDirectory"), StyleTools.getIconImageView("iconOpenPath.png"));
                    menu.setOnAction((ActionEvent event) -> {
                        openSourcePath();
                    });
                    items.add(menu);

                    menu = new MenuItem(message("BrowseFiles"), StyleTools.getIconImageView("iconList.png"));
                    menu.setOnAction((ActionEvent event) -> {
                        FileBrowseController.open(myController);
                    });
                    items.add(menu);
                }

            }
            items.add(new SeparatorMenuItem());

            if (!linkInNewTab) {
                items.add(clickedMenu());
            }

            CheckMenuItem editableMenu = new CheckMenuItem(message("Editable"), StyleTools.getIconImageView("iconEdit.png"));
            editableMenu.setSelected(UserConfig.getBoolean(baseName + "Editable", false));
            editableMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    setEditable(editableMenu.isSelected());
                }
            });
            items.add(editableMenu);

            CheckMenuItem cacheMenu = new CheckMenuItem(message("Cache"), StyleTools.getIconImageView("iconBackup.png"));
            cacheMenu.setSelected(UserConfig.getBoolean(interfaceName + "Cache", false));
            cacheMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    setCache(cacheMenu.isSelected());
                }
            });
            items.add(cacheMenu);

            CheckMenuItem jsMenu = new CheckMenuItem(message("JavaScriptEnabled"), StyleTools.getIconImageView("iconScript.png"));
            jsMenu.setSelected(UserConfig.getBoolean(interfaceName + "JavaScriptEnabled", true));
            jsMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    setCache(jsMenu.isSelected());
                }
            });
            items.add(jsMenu);

            CheckMenuItem interceptMenu = new CheckMenuItem(message("InterceptPopWindow"), StyleTools.getIconImageView("iconArc.png"));
            interceptMenu.setSelected(UserConfig.getBoolean("WebViewInterceptPopWindow", false));
            interceptMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean("WebViewInterceptPopWindow", interceptMenu.isSelected());
                }
            });
            items.add(interceptMenu);

            return items;

        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public Menu clickedMenu() {
        try {
            Menu clickMenu = new Menu(message("WhenLeftClickImageOrLink"), StyleTools.getIconImageView("iconSelect.png"));
            ToggleGroup clickGroup = new ToggleGroup();
            String currentClick = UserConfig.getString("WebViewWhenLeftClickImageOrLink", "PopMenu");

            RadioMenuItem clickPopMenu = new RadioMenuItem(message("ContextMenu"), StyleTools.getIconImageView("iconMenu.png"));
            clickPopMenu.setSelected(currentClick == null || "PopMenu".equals(currentClick));
            clickPopMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setString("WebViewWhenLeftClickImageOrLink", "PopMenu");
                }
            });
            clickPopMenu.setToggleGroup(clickGroup);

            RadioMenuItem clickAsPageMenu = new RadioMenuItem(message("HandleAsPage"), StyleTools.getIconImageView("iconHtml.png"));
            clickAsPageMenu.setSelected("AsPage".equals(currentClick));
            clickAsPageMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setString("WebViewWhenLeftClickImageOrLink", "AsPage");
                }
            });
            clickAsPageMenu.setToggleGroup(clickGroup);

            RadioMenuItem clickOpenSwitchMenu = new RadioMenuItem(message("OpenLinkInNewTabSwitch"), StyleTools.getIconImageView("iconWindow.png"));
            clickOpenSwitchMenu.setSelected("OpenSwitch".equals(currentClick));
            clickOpenSwitchMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setString("WebViewWhenLeftClickImageOrLink", "OpenSwitch");
                }
            });
            clickOpenSwitchMenu.setToggleGroup(clickGroup);

            RadioMenuItem clickOpenMenu = new RadioMenuItem(message("OpenLinkInNewTab"), StyleTools.getIconImageView("iconWindow.png"));
            clickOpenMenu.setSelected("Open".equals(currentClick));
            clickOpenMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setString("WebViewWhenLeftClickImageOrLink", "Open");
                }
            });
            clickOpenMenu.setToggleGroup(clickGroup);

            RadioMenuItem clickLoadMenu = new RadioMenuItem(message("OpenLinkByCurrent"), StyleTools.getIconImageView("iconWindow.png"));
            clickLoadMenu.setSelected("Load".equals(currentClick));
            clickLoadMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setString("WebViewWhenLeftClickImageOrLink", "Load");
                }
            });
            clickLoadMenu.setToggleGroup(clickGroup);

            RadioMenuItem clickSystemMenu = new RadioMenuItem(message("OpenLinkBySystem"), StyleTools.getIconImageView("iconSystemOpen.png"));
            clickSystemMenu.setSelected("System".equals(currentClick));
            clickSystemMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setString("WebViewWhenLeftClickImageOrLink", "System");
                }
            });
            clickSystemMenu.setToggleGroup(clickGroup);

            clickMenu.getItems().addAll(clickPopMenu, clickAsPageMenu, clickOpenSwitchMenu,
                    clickOpenMenu, clickLoadMenu, clickSystemMenu);

            return clickMenu;

        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public void setEditable(boolean e) {
        UserConfig.setBoolean(baseName + "Editable", e);
        executeScript("document.body.contentEditable=" + e);
        if (e) {
            alertInformation(message("HtmlEditableComments"));
        }
    }

    public void setCache(boolean e) {
        UserConfig.setBoolean(interfaceName + "Cache", e);
        webView.setCache(e);
        popInformation(message("OK"));
    }

    public void setJavaScriptEnabled(boolean e) {
        UserConfig.setBoolean(interfaceName + "JavaScriptEnabled", e);
        webEngine.setJavaScriptEnabled(e);
        popInformation(message("OK"));
    }

    @FXML
    public void showHtmlStyle(Event event) {
        PopTools.popHtmlStyle(event, this);
    }

    @FXML
    public void popHtmlStyle(Event event) {
        if (UserConfig.getBoolean("HtmlStylesPopWhenMouseHovering", false)) {
            showHtmlStyle(event);
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

    public void htmlCodes() {
        HtmlCodesPopController.openWebView(myController, webView);
    }

    protected void links() {
        Document doc = webEngine.getDocument();
        if (doc == null) {
            popError(message("NoData"));
            return;
        }
        popInformation(message("Handling..."));
        FxTask bgTask = new FxTask<Void>(this) {

            private StringTable table;

            @Override
            protected boolean handle() {
                try {
                    NodeList aList = doc.getElementsByTagName("a");
                    if (aList == null || aList.getLength() < 1) {
                        return true;
                    }
                    List<String> names = new ArrayList<>();
                    names.addAll(Arrays.asList(message("Index"), message("Link"), message("Name"),
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
                        HtmlElement htmlElement = new HtmlElement(nodeElement, charset);
                        if (!htmlElement.isLink()) {
                            continue;
                        }
                        String name = htmlElement.getName();
                        if (name == null) {
                            name = "";
                        }
                        String linkAddress = htmlElement.getDecodedAddress();
                        List<String> row = new ArrayList<>();
                        row.addAll(Arrays.asList(
                                index + "",
                                "<a href=\"" + linkAddress + "\">" + name + "</a>",
                                name,
                                htmlElement.getDecodedHref(),
                                linkAddress
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
        FxTask bgTask = new FxTask<Void>(this) {

            private StringTable table;

            @Override
            protected boolean handle() {
                try {
                    NodeList aList = doc.getElementsByTagName("img");
                    if (aList == null || aList.getLength() < 1) {
                        return true;
                    }
                    List<String> names = new ArrayList<>();
                    names.addAll(Arrays.asList(message("Index"), message("Link"), message("Name"), message("Image"),
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
                        HtmlElement htmlElement = new HtmlElement(nodeElement, charset);
                        if (!htmlElement.isImage()) {
                            continue;
                        }
                        String name = htmlElement.getName();
                        if (name == null) {
                            name = "";
                        }
                        String linkAddress = htmlElement.getDecodedAddress();
                        List<String> row = new ArrayList<>();
                        row.addAll(Arrays.asList(
                                index + "",
                                "<a href=\"" + linkAddress + "\">" + (name.isBlank() ? message("Link") : name) + "</a>",
                                name,
                                "<img src=\"" + linkAddress + "\" " + (name.isBlank() ? "" : "alt=\"" + name + "\"") + " width=100/>",
                                htmlElement.getDecodedHref(),
                                linkAddress
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
        FxTask bgTask = new FxTask<Void>(this) {

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
                    TextEditorController.edit(toc);
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
        FxTask bgTask = new FxTask<Void>(this) {

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
                    TextEditorController.edit(texts);
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
        FxTask bgTask = new FxTask<Void>(this) {

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
                    Data2DManufactureController.loadTables(title(), tables);
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
        File file = saveAsFile(webEngine.getTitle());
        if (file == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            @Override
            protected boolean handle() {
                File tmpFile = HtmlWriteTools.writeHtml(html);
                if (tmpFile == null || !tmpFile.exists()) {
                    return false;
                }
                return FileTools.override(tmpFile, file);
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
    public boolean menuAction(Event event) {
        MenuWebviewController.webviewMenu(this);
        return true;
    }

    @FXML
    public void snapAction() {
        ImageEditorController.openImage(NodeTools.snap(webView));
    }

    public void snapHtml() {
        HtmlSnapController controller = (HtmlSnapController) WindowTools.openStage(Fxmls.HtmlSnapFxml);
        if (address != null && !address.isBlank()) {
            controller.loadAddress(address);
        } else {
            controller.loadContents(loadedHtml());
        }
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
