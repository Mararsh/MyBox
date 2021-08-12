package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
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
import javafx.concurrent.Worker;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
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
import mara.mybox.data.BaseTask;
import mara.mybox.db.data.ImageClipboard;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.WebHistory;
import mara.mybox.db.table.TableWebHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ImageClipboardTools;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.fxml.WebViewTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.HtmlReadTools;
import mara.mybox.tools.UrlTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

/**
 * @Author Mara
 * @CreateDate 2021-8-3
 * @License Apache License Version 2.0
 */
public abstract class BaseWebViewController_Assist extends BaseController {

    protected WebEngine webEngine;
    protected final SimpleIntegerProperty stateNotify;
    protected boolean addressChanged;
    protected double linkX, linkY;
    protected float zoomScale;
    protected String address;
    protected Charset charset;
    protected Document doc;
    protected Map<Integer, Document> framesDoc;
    protected EventListener docListener;
    protected TableWebHistory tableWebHistory;
    protected Element element;

    public static final int TmpState = -9;
    public static final int NoDoc = -3;
    public static final int DocLoading = -2;
    public static final int DocLoaded = -1;

    @FXML
    protected WebView webView;
    @FXML
    protected ComboBox<String> urlSelector;
    @FXML
    protected Button backwardButton, forwardButton;
    @FXML
    protected Label webViewLabel;

    public BaseWebViewController_Assist() {
        linkX = linkY = -1;
        zoomScale = 1.0f;
        framesDoc = new HashMap<>();
        charset = Charset.defaultCharset();
        stateNotify = new SimpleIntegerProperty(NoDoc);
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            parentController = this;
            if (urlSelector != null) {
                tableWebHistory = new TableWebHistory();
            }
            addressChanged = false;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Html);
    }

    @Override
    public void initControls() {
        try {
            initWebView();
            initToolbar();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initWebView() {
        try {
            if (webView == null || webEngine != null) {
                return;
            }
            webEngine = webView.getEngine();

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
                    parentController.closePopup();
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
                        if (href != null && ("click".equals(domEventType) || "contextmenu".equals(domEventType))) {
                            String target = element.getAttribute("target");
                            if (target != null && !target.equalsIgnoreCase("_blank")) {
                                webEngine.executeScript("if ( window.frames." + target + ".document.readyState==\"complete\") alert('FrameReadyName-" + target + "');");
                                webEngine.executeScript("window.frames." + target + ".document.onreadystatechange = "
                                        + "function(){ if ( window.frames." + target + ".document.readyState==\"complete\") alert('FrameReadyName-" + target + "'); }");
                            } else {
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
                    if (parentController != null) {
                        parentController.alertError(nt.getMessage());
                    }

                }
            });

            webEngine.setOnStatusChanged(new EventHandler<WebEvent<String>>() {
                @Override
                public void handle(WebEvent<String> ev) {
//                    javafx.event.EventTarget t = ev.getTarget();
//                    MyBoxLog.console("here:" + ev.getData());
//                    webViewLabel.setText(ev.getData());
                }
            });

            webEngine.locationProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldv, String newv) {
                    if (webViewLabel != null) {
                        webViewLabel.setText(URLDecoder.decode(newv, charset));
                    }
                }
            });

            webEngine.setPromptHandler(new Callback< PromptData, String>() {
                @Override
                public String call(PromptData p) {
//                    MyBoxLog.console("here:" + p.getMessage());
                    if (parentController != null) {
                        String value = PopTools.askValue(parentController.getBaseTitle(), null, p.getMessage(), p.getDefaultValue());
                        return value;
                    } else {
                        return null;
                    }
                }
            });

            webEngine.setConfirmHandler(new Callback< String, Boolean>() {
                @Override
                public Boolean call(String message) {
                    try {
                        if (parentController == null) {
                            return false;
                        }
//                        MyBoxLog.console("here:" + message);
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle(parentController.getBaseTitle());
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
                        value = WebViewTools.frameIndex(webEngine, msg.substring("FrameReadyName-".length()));
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
                                changeDocState(frameIndex);
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

    public void initToolbar() {
        try {
            if (urlSelector != null) {
                List<String> his = tableWebHistory.recent(20);
                if (!his.isEmpty()) {
                    urlSelector.getItems().addAll(his);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void sourceFileChanged(File file) {
        loadFile(file);
    }

    public boolean loadFile(File file) {
        if (file == null || !file.exists()) {
            popError(message("InvalidData"));
            return false;
        }
        return loadAddress(UrlTools.decodeURL(file, Charset.defaultCharset()));
    }

    public boolean loadURI(URI uri) {
        if (uri == null) {
            popError(message("InvalidData"));
            return false;
        }
        return loadAddress(uri.toString());
    }

    public boolean loadAddress(String value) {
        value = UrlTools.checkURL(value, Charset.forName("UTF-8"));
        if (value == null) {
            popError(message("InvalidData"));
            return false;
        }
        goAddress(value);
        return true;
    }

    public boolean loadContents(String contents) {
        return loadContents(null, contents);
    }

    public boolean loadContents(String address, String contents) {
        if (!checkBeforeNextAction()) {
            return false;
        }
        setAddress(address);
        addressChanged = true;
        webEngine.getLoadWorker().cancel();
        webEngine.loadContent(contents);
        return true;
    }

    public void setAddress(String value) {
        setSourceFile(null);
        address = UrlTools.checkURL(value, Charset.forName("UTF-8"));
        if (address != null && address.startsWith("file:/")) {
            File file = new File(address.substring(6));
            if (file.exists()) {
                setSourceFile(file);
            }
        }
        if (urlSelector != null) {
            Platform.runLater(() -> {
                tableWebHistory.insertData(makeHis(address));

                urlSelector.getItems().clear();
                List<String> urls = tableWebHistory.recent(20);
                if (!urls.isEmpty()) {
                    urlSelector.getItems().addAll(urls);
                    urlSelector.getEditor().setText(address);
                }
            });
        }
    }

    public WebHistory makeHis(String value) {
        WebHistory his = new WebHistory();
        his.setAddress(value);
        his.setVisitTime(new Date());
        return his;
    }

    @Override
    public void setSourceFile(File file) {
        this.sourceFile = file;
        if (parentController != null) {
            parentController.sourceFile = sourceFile;
        }
        if (address == null && sourceFile != null) {
            address = sourceFile.getAbsolutePath();
        }
    }

    @Override
    public void goAction() {
        if (urlSelector != null) {
            goAddress(urlSelector.getEditor().getText());
        } else {
            goAddress(address);
        }
    }

    public void goAddress(String value) {
        if (webEngine == null) {
            return;
        }
        try {
            URL url = new URL(value);
            if (urlSelector != null) {
                urlSelector.getEditor().setStyle(null);
            }
        } catch (Exception e) {
            popError(message("InvalidLink"));
            if (urlSelector != null) {
                urlSelector.getEditor().setStyle(NodeStyleTools.badStyle);
            }
            return;
        }
        if (!checkBeforeNextAction()) {
            return;
        }
        try {
            setAddress(value);
            addressChanged = true;
            if (webViewLabel != null) {
                webViewLabel.setText(message("Loading..."));
            }
            webEngine.getLoadWorker().cancel();
            webEngine.load(address);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    // Listener should ignore state change from TmpState
    // positive is frame index, and negative is status
    public void changeDocState(int newState) {
        synchronized (stateNotify) {
            if (stateNotify.get() == newState) { // make sure state will change
                stateNotify.set(TmpState);
            }
            stateNotify.set(newState);
        }
    }

    protected void pageIsLoading() {
        if (webViewLabel != null) {
            webViewLabel.setText(message("Loading..."));
        }
        changeDocState(DocLoading);
    }

    protected void afterPageLoaded() {
        try {
            addressChanged = false;
            if (webViewLabel != null) {
                webViewLabel.setText(message("Loaded"));
            }
            doc = webEngine.getDocument();
            charset = HtmlReadTools.charset(doc);
            framesDoc.clear();
            addDocListener(doc);
            changeDocState(DocLoaded);
            if (backwardButton != null) {
                int hisSize = (int) webEngine.executeScript("window.history.length;");
                backwardButton.setDisable(hisSize < 2);
                forwardButton.setDisable(hisSize < 2);
            }
            updateTitle();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void updateTitle() {
        if (myStage == null) {
            return;
        }
        String title = getBaseTitle();
        if (address != null) {
            title += "  " + address;
        } else if (sourceFile != null) {
            title += "  " + sourceFile.getAbsolutePath();
        }
        myStage.setTitle(title);
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

    public void popLinkMenu(Element element) {
        if (linkX < 0 || linkY < 0 || element == null || parentController == null) {
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

        menu = new MenuItem(message("QueryNetworkAddress"));
        menu.setOnAction((ActionEvent event) -> {
            NetworkQueryAddressController controller
                    = (NetworkQueryAddressController) WindowTools.openStage(Fxmls.NetworkQueryAddressFxml);
            controller.queryUrl(finalAddress);
        });
        items.add(menu);

        menu = new MenuItem(message("AddAsFavorite"));
        menu.setOnAction((ActionEvent event) -> {
            WebFavoriteAddController controller = (WebFavoriteAddController) WindowTools.openStage(Fxmls.WebFavoriteAddFxml);
            controller.setValues(name == null || name.isBlank() ? finalAddress : name, finalAddress);

        });
        items.add(menu);

        items.add(new SeparatorMenuItem());

        if (parentController instanceof WebBrowserController) {
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
            HtmlEditorController controller = (HtmlEditorController) WindowTools.openStage(Fxmls.HtmlEditorFxml);
            controller.loadAddress(finalAddress);
        });
        items.add(menu);

        if (!(parentController instanceof WebBrowserController)) {
            menu = new MenuItem(message("OpenLinkByBrowser"));
            menu.setOnAction((ActionEvent event) -> {
                WebBrowserController.oneOpen(finalAddress);
            });
            items.add(menu);
        }

        items.add(new SeparatorMenuItem());

        if (tag.equalsIgnoreCase("img")) {
            if (ImageClipboardTools.isMonitoringCopy()) {
                menu = new MenuItem(message("CopyImageToClipboards"));
            } else {
                menu = new MenuItem(message("CopyImageToSystemClipboard"));
            }
            menu.setOnAction((ActionEvent event) -> {
                copyImage(finalAddress, name, true);
            });
            items.add(menu);

            menu = new MenuItem(message("CopyImageToMyBoxClipboard"));
            menu.setOnAction((ActionEvent event) -> {
                copyImage(finalAddress, name, false);
            });
            items.add(menu);
        }

        menu = new MenuItem(message("DownloadBySysBrowser"));
        menu.setOnAction((ActionEvent event) -> {
            browse(finalAddress);
        });
        items.add(menu);

        menu = new MenuItem(message("DownloadByMyBox"));
        menu.setOnAction((ActionEvent event) -> {
            WebBrowserController controller = WebBrowserController.oneOpen();
            controller.download(finalAddress, name);
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());

        menu = new MenuItem(message("CopyLink"));
        menu.setOnAction((ActionEvent event) -> {
            TextClipboardTools.copyToSystemClipboard(parentController, finalAddress);
        });
        items.add(menu);

        if (showName) {
            menu = new MenuItem(message("CopyLinkName"));
            menu.setOnAction((ActionEvent event) -> {
                TextClipboardTools.copyToSystemClipboard(parentController, name);
            });
            items.add(menu);

            menu = new MenuItem(message("CopyLinkAndName"));
            menu.setOnAction((ActionEvent event) -> {
                TextClipboardTools.copyToSystemClipboard(parentController, name + "\n" + finalAddress);
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
            TextClipboardTools.copyToSystemClipboard(parentController, code);
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());
        menu = new MenuItem(message("PopupClose"));
        menu.setStyle("-fx-text-fill: #2e598a;");
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            if (popMenu != null) {
                popMenu.hide();
                popMenu = null;
            }
        });
        items.add(menu);

        closePopup();
        parentController.closePopup();
        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(items);
        popMenu.show(webView, linkX, linkY);
        parentController.setPopMenu(popMenu);
    }

    public void popElementMenu(Element element) {
        try {
            if (linkX < 0 || linkY < 0) {
                return;
            }
            MenuWebviewController.pop((BaseWebViewController) this, webView, element, linkX, linkY);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void copyImage(String address, String name, boolean toSystemClipboard) {
        if (address == null) {
            return;
        }
        synchronized (this) {
            BaseTask copyTask = new BaseTask<Void>() {

                @Override
                protected boolean handle() {
                    try {
                        String suffix = null;
                        if (name != null && !name.isBlank()) {
                            suffix = FileNameTools.getFileSuffix(name);
                        }
                        String addrSuffix = FileNameTools.getFileSuffix(address);
                        if (addrSuffix != null && !addrSuffix.isBlank()) {
                            if (suffix == null || suffix.isBlank()
                                    || !addrSuffix.equalsIgnoreCase(suffix)) {
                                suffix = addrSuffix;
                            }
                        }
                        if (suffix == null || (suffix.length() != 3
                                && !"jpeg".equalsIgnoreCase(suffix) && !"tiff".equalsIgnoreCase(suffix))) {
                            suffix = "jpg";
                        }

                        File tmpFile = HtmlReadTools.url2File(address);
                        if (tmpFile == null) {
                            return false;
                        }
                        File imageFile = new File(tmpFile.getAbsoluteFile() + "." + suffix);
                        if (FileTools.rename(tmpFile, imageFile)) {
                            BufferedImage bi = ImageFileReaders.readImage(imageFile);
                            if (bi == null) {
                                return false;
                            }
                            Image image = SwingFXUtils.toFXImage(bi, null);
                            if (image == null) {
                                return false;
                            }
                            if (toSystemClipboard) {
                                ImageClipboardTools.copyToSystemClipboard(myController, image);
                                return true;
                            } else {
                                ImageClipboardTools.copyToMyBoxClipboard(myController, image, ImageClipboard.ImageSource.Link);
                                return true;
                            }
                        } else {
                            return false;
                        }
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

            };
            copyTask.setSelf(copyTask);
            Thread thread = new Thread(copyTask);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @Override
    public boolean keyEnter() {
        if (urlSelector.isFocused()) {
            goAction();
            return true;
        }
        return false;
    }

}
