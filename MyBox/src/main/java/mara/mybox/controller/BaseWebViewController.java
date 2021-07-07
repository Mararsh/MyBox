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
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
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
import mara.mybox.data.StringTable;
import mara.mybox.db.data.ImageClipboard;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlWindow;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.HtmlTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;
import org.jsoup.Jsoup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

/**
 * @Author Mara
 * @CreateDate 2021-7-6
 * @License Apache License Version 2.0
 */
public class BaseWebViewController extends BaseController {

    protected WebEngine webEngine;
    protected final SimpleIntegerProperty stateNotify;
    protected double linkX, linkY;
    protected float zoomScale;
    protected String address;
    protected Charset charset;
    protected Document doc;
    protected Map<Integer, Document> frameDoc;
    protected EventListener docListener;

    public static final int TmpState = -9;
    public static final int NoDoc = -3;
    public static final int DocLoading = -2;
    public static final int DocLoaded = -1;

    @FXML
    protected WebView webView;
    @FXML
    protected Button backwardButton, forwardButton;

    public BaseWebViewController() {
        linkX = linkY = -1;
        zoomScale = 1.0f;
        frameDoc = new HashMap<>();
        charset = Charset.defaultCharset();
        stateNotify = new SimpleIntegerProperty(NoDoc);
    }

    public void setParameters(BaseController parent) {
        if (parent == null) {
            return;
        }
        this.parentController = parent;
        this.baseName = parent.baseName;
    }

    public void setParameters(BaseController parent, WebView webView) {
        if (parent != null) {
            this.parentController = parent;
            this.baseName = parent.baseName;
        }
        if (myController == null) {
            myController = parent;
        }
        this.webView = webView;
        initWebView();
    }

    @Override
    public void initControls() {
        try {
            parentController = this;
            initWebView();
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
            webView.setUserData("initialized");

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
                            if (bottomLabel != null) {
                                bottomLabel.setText(message("Canceled"));
                            }
                            break;
                        case FAILED:
                            if (bottomLabel != null) {
                                bottomLabel.setText(message("Failed"));
                            }
                            break;
                        default:
                            if (bottomLabel != null) {
                                bottomLabel.setText(newState.name());
                            }
                    }
                }
            });

            webView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
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
                public void handleEvent(org.w3c.dom.events.Event ev) {
                    String domEventType = ev.getType();
                    String tag = null, href = null;
                    Element element;
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
//                    MyBoxLog.console(domEventType + " " + tag);
                    if (bottomLabel != null) {
                        if ("mouseover".equals(domEventType)) {
                            bottomLabel.setText(href != null ? URLDecoder.decode(href, charset) : tag);
                        } else if ("mouseout".equals(domEventType)) {
                            bottomLabel.setText("");
                        }
                    }
                    if (element == null) {
                        return;
                    }
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
                }
            };

            webEngine.getLoadWorker().exceptionProperty().addListener(new ChangeListener<Throwable>() {
                @Override
                public void changed(ObservableValue<? extends Throwable> ov, Throwable ot, Throwable nt) {
                    if (nt == null) {
                        return;
                    }
                    if (bottomLabel != null) {
                        bottomLabel.setText(nt.getMessage());
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
//                    bottomLabel.setText(ev.getData());
                }
            });

            webEngine.locationProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldv, String newv) {
                    if (bottomLabel != null) {
                        bottomLabel.setText(URLDecoder.decode(newv, charset));
                    }
                }
            });

            webEngine.setPromptHandler(new Callback< PromptData, String>() {
                @Override
                public String call(PromptData p) {
                    MyBoxLog.console("here:" + p.getMessage());
                    if (parentController != null) {
                        String value = FxmlControl.askValue(parentController.getBaseTitle(), null, p.getMessage(), p.getDefaultValue());
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
                        MyBoxLog.console("here:" + message);
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
                                addFrameListener(frameIndex);
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

    // Listener should ignore state change from TmpState
    public void changeState(int newState) {
        synchronized (stateNotify) {
            if (stateNotify.get() == newState) { // make sure state will change
                stateNotify.set(TmpState);
            }
            stateNotify.set(newState);
        }
    }

    protected void pageIsLoading() {
        if (bottomLabel != null) {
            bottomLabel.setText(AppVariables.message("Loading..."));
        }
        changeState(DocLoading);
    }

    protected void afterPageLoaded() {
        try {
            if (bottomLabel != null) {
                bottomLabel.setText(AppVariables.message("Loaded"));
            }
            charset = HtmlTools.charset(webEngine.getDocument());
            doc = webEngine.getDocument();
            frameDoc.clear();
            addDocListener(doc);
//            addLinksListener(doc);
            changeState(DocLoaded);
            if (backwardButton != null) {
                int hisSize = (int) webEngine.executeScript("window.history.length;");
                backwardButton.setDisable(hisSize < 2);
                forwardButton.setDisable(hisSize < 2);
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
            Document htmlDoc = (Document) c;
            frameDoc.put(frameIndex, htmlDoc);

            addDocListener(htmlDoc);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setAddress(String value) {
        address = HtmlTools.checkURL(value, Charset.defaultCharset());
        if (address != null && address.startsWith("file:/")) {
            File file = new File(address.substring(6));
            if (file.exists()) {
                setSourceFile(file);
            } else {
                setSourceFile(null);
            }
        }
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
            if (parentController != null) {
                parentController.popError(message("InvalidData"));
            }
            return;
        }
        setSourceFile(file);
        URI uri = file.toURI();
        setAddress(uri.toString());
        goAction();
    }

    public void loadURI(URI uri) {
        if (uri == null) {
            if (parentController != null) {
                parentController.popError(message("InvalidData"));
            }
            return;
        }
        loadAddress(uri.toString());
    }

    @Override
    public void goAction() {
        if (address == null) {
            return;
        }
        webEngine.getLoadWorker().cancel();
        webEngine.load(address);
    }

    @FXML
    public void zoomIn() {
        zoomScale += 0.1f;
        webView.setZoom(zoomScale);
    }

    @FXML
    public void zoomOut() {
        zoomScale -= 0.1f;
        webView.setZoom(zoomScale);
    }

    @FXML
    public void backAction() {
        webEngine.executeScript("window.history.back();");
    }

    @FXML
    public void forwardAction() {
        webEngine.executeScript("window.history.forward();");
    }

    @FXML
    public void refreshAction() {
        goAction();
    }

    @FXML
    @Override
    public void cancelAction() {
        webEngine.getLoadWorker().cancel();
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
                    = (NetworkQueryAddressController) FxmlWindow.openStage(CommonValues.NetworkQueryAddressFxml);
            controller.queryUrl(finalAddress);
        });
        items.add(menu);

        menu = new MenuItem(message("AddAsFavorite"));
        menu.setOnAction((ActionEvent event) -> {
            WebFavoriteAddController controller = (WebFavoriteAddController) FxmlWindow.openStage(CommonValues.WebFavoriteAddFxml);
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
            HtmlEditorController controller = (HtmlEditorController) FxmlWindow.openStage(CommonValues.HtmlEditorFxml);
            controller.loadAddress(finalAddress);
        });
        items.add(menu);

        menu = new MenuItem(message("OpenLinkByBrowser"));
        menu.setOnAction((ActionEvent event) -> {
            WebBrowserController.oneOpen(finalAddress);
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());

        if (tag.equalsIgnoreCase("img")) {
            menu = new MenuItem(message("CopyImageToClipboards"));
            menu.setOnAction((ActionEvent event) -> {
                copyImage(finalAddress, name);
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
            parentController.copyToSystemClipboard(finalAddress);
        });
        items.add(menu);

        if (showName) {
            menu = new MenuItem(message("CopyLinkName"));
            menu.setOnAction((ActionEvent event) -> {
                parentController.copyToSystemClipboard(name);
            });
            items.add(menu);

            menu = new MenuItem(message("CopyLinkAndName"));
            menu.setOnAction((ActionEvent event) -> {
                parentController.copyToSystemClipboard(name + "\n" + finalAddress);
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
            parentController.copyToSystemClipboard(code);
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
        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(items);
        popMenu.show(webView, linkX, linkY);
        parentController.closePopup();
        parentController.setPopMenu(popMenu);
    }

    public void popElementMenu(Element element) {
        try {
            if (linkX < 0 || linkY < 0 || element == null || parentController == null) {
                return;
            }
            closePopup();
            popup = FxmlWindow.popWindow(parentController, CommonValues.PopNodesFxml, webView, linkX, linkY);
            if (popup == null) {
                return;
            }
            Object object = popup.getUserData();
            if (object == null && !(object instanceof PopNodesController)) {
                return;
            }
            PopNodesController controller = (PopNodesController) object;
            controller.addWebviewPane(this, element);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void copyImage(String address, String name) {
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
                            suffix = FileTools.getFileSuffix(name);
                        }
                        String addrSuffix = FileTools.getFileSuffix(address);
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

                        File tmpFile = HtmlTools.url2File(address);
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
                            return ImageClipboard.add(image, ImageClipboard.ImageSource.Copy, true) != null;
                        } else {
                            return false;
                        }
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    if (parentController != null) {
                        parentController.popInformation(message("ImageSelectionInClipBoard"));
                    }
                    ControlImagesClipboard.updateClipboards();
                }

                @Override
                protected void whenFailed() {
                    if (parentController == null) {
                        return;
                    }
                    if (error != null) {
                        parentController.popError(error);
                    } else {
                        parentController.popFailed();
                    }
                }

            };
            copyTask.setSelf(copyTask);
            Thread thread = new Thread(copyTask);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @FXML
    public void popFunctionsMenu(MouseEvent mouseEvent) {
        try {
            String html = FxmlControl.getHtml(webEngine);
            doc = webEngine.getDocument();
            boolean isFrameset = frameDoc != null && frameDoc.size() > 0;

            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            if (address != null && !address.isBlank()) {
                menu = new MenuItem(address);
                menu.setStyle("-fx-text-fill: #2e598a;");
                items.add(menu);
                items.add(new SeparatorMenuItem());
            }

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
                WebFavoriteAddController controller = (WebFavoriteAddController) FxmlWindow.openStage(CommonValues.WebFavoriteAddFxml);
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
                WebFindController controller = (WebFindController) FxmlWindow.openStage(CommonValues.WebFindFxml);
                controller.loadContents(html);
                controller.setAddress(address);
                controller.toFront();
            });
            menu.setDisable(html == null || html.isBlank());
            items.add(menu);

            menu = new MenuItem(message("QueryNetworkAddress"));
            menu.setOnAction((ActionEvent event) -> {
                NetworkQueryAddressController controller
                        = (NetworkQueryAddressController) FxmlWindow.openStage(CommonValues.NetworkQueryAddressFxml);
                controller.queryUrl(address);
            });
            menu.setDisable(address == null || address.isBlank());
            items.add(menu);

            if (!(parentController instanceof HtmlSnapController)) {
                menu = new MenuItem(message("HtmlSnap"));
                menu.setOnAction((ActionEvent event) -> {
                    HtmlSnapController controller = (HtmlSnapController) FxmlWindow.openStage(CommonValues.HtmlSnapFxml);
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
                    HtmlEditorController controller = (HtmlEditorController) FxmlWindow.openStage(CommonValues.HtmlEditorFxml);
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
                            HtmlEditorController controller = (HtmlEditorController) FxmlWindow.openStage(CommonValues.HtmlEditorFxml);
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
                WebElementsController controller = (WebElementsController) FxmlWindow.openStage(CommonValues.WebElementsFxml);
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
            if (parentController != null) {
                parentController.popInformation(message("NoData"));
            }
            return;
        }
        try {
            NodeList aList = doc.getElementsByTagName("a");
            if (aList == null || aList.getLength() < 1) {
                if (parentController != null) {
                    parentController.popInformation(message("NoData"));
                }
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
            if (parentController != null) {
                parentController.popError(e.toString());
            }
        }
    }

    protected void images() {
        doc = webEngine.getDocument();
        if (doc == null) {
            if (parentController != null) {
                parentController.popInformation(message("NoData"));
            }
            return;
        }
        try {
            NodeList aList = doc.getElementsByTagName("img");
            if (aList == null || aList.getLength() < 1) {
                if (parentController != null) {
                    parentController.popInformation(message("NoData"));
                }
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
            if (parentController != null) {
                parentController.popError(e.toString());
            }
        }
    }

    protected void toc(String html) {
        if (html == null) {
            if (parentController != null) {
                parentController.popInformation(message("NoData"));
            }
            return;
        }
        String toc = HtmlTools.toc(html, 8);
        if (toc == null || toc.isBlank()) {
            if (parentController != null) {
                parentController.popInformation(message("NoData"));
            }
            return;
        }
        TextEditerController c = (TextEditerController) FxmlWindow.openStage(CommonValues.TextEditerFxml);
        c.loadContexts(toc);
        c.toFront();
    }

    protected void texts(String html) {
        if (html == null) {
            if (parentController != null) {
                parentController.popInformation(message("NoData"));
            }
            return;
        }
        String texts = Jsoup.parse(html).wholeText();
        if (texts == null || texts.isBlank()) {
            if (parentController != null) {
                parentController.popInformation(message("NoData"));
            }
            return;
        }
        TextEditerController c = (TextEditerController) FxmlWindow.openStage(CommonValues.TextEditerFxml);
        c.loadContexts(texts);
        c.toFront();
    }

}
