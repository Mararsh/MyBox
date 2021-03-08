package mara.mybox.controller;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
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
import mara.mybox.db.table.TableBrowserBypassSSL;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.tools.NetworkTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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
    protected EventListener linkListener;
    protected double linkX, linkY;
    protected float zoomScale;
    protected String address;

    @FXML
    protected WebView webView;
    @FXML
    protected Button snapshotButton, editButton;
    @FXML
    protected FlowPane buttonsPane;

    public ControlWebview() {
        baseTitle = AppVariables.message("Html");

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
            if (AppVariables.getUserConfigBoolean("SSLBypassAll", false)) {
                NetworkTools.trustAll();
            } else {
                NetworkTools.myBoxSSL();
            }
            webEngine = webView.getEngine();
            linkX = linkY = -1;
            zoomScale = 1.0f;

            webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
                @Override
                public void changed(ObservableValue ov, State oldState, State newState) {
                    switch (newState) {
                        case RUNNING:
                            bottomLabel.setText(AppVariables.message("Loading..."));
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
                    ev.preventDefault();
                    String domEventType = ev.getType();
                    Element element = (Element) ev.getTarget();
                    String href = element.getAttribute("href");
                    if (href == null) {
                        return;
                    }
                    String value = element.getTextContent();
                    if ("mouseover".equals(domEventType)) {
                        bottomLabel.setText(href);
                    } else if ("mouseout".equals(domEventType)) {
                        bottomLabel.setText("");
                    } else if ("click".equals(domEventType) || "contextmenu".equals(domEventType)) {
                        timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Platform.runLater(() -> {
                                    popLinkMenu(href, value);
                                });
                            }
                        }, 100);
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
                                refreshAction();

                            } else {
                                alert.setContentText(host + "\n" + message("SSLCertificatesAskInstall"));
                                ButtonType buttonSure = new ButtonType(AppVariables.message("Sure"));
                                ButtonType buttonCancel = new ButtonType(AppVariables.message("Cancel"));
                                alert.getButtonTypes().setAll(buttonSure, buttonCancel);
                                Optional<ButtonType> result = alert.showAndWait();
                                if (result.get() != buttonSure) {
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
                        MyBoxLog.debug(e.toString());
                    }
                }
            });

            webEngine.setOnStatusChanged(new EventHandler<WebEvent<String>>() {
                @Override
                public void handle(WebEvent<String> ev) {
                    bottomLabel.setText(ev.getData());
                }
            });

            webEngine.locationProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldv, String newv) {
                    bottomLabel.setText(newv);
                }
            });

            webEngine.setPromptHandler(new Callback< PromptData, String>() {
                @Override
                public String call(PromptData p) {
                    String value = FxmlControl.askValue(baseTitle, null, p.getMessage(), p.getDefaultValue());
                    return value;
                }
            });

            webEngine.setConfirmHandler(new Callback< String, Boolean>() {
                @Override
                public Boolean call(String message) {
                    try {
                        MyBoxLog.debug(message);
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
                    FxmlStage.alertError(getMyStage(), ev.getData());
                    MyBoxLog.debug(ev.getData());
                }
            });

            webEngine.setOnError(new EventHandler<WebErrorEvent>() {

                @Override
                public void handle(WebErrorEvent event) {
                    popError(event.getMessage());
                    MyBoxLog.debug(event.getMessage());
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setValues(BaseController parent, boolean snap, boolean edit) {
        try {
            this.parentController = parent;
            this.baseName = parent.baseName;
            if (!snap) {
                buttonsPane.getChildren().remove(snapshotButton);
            }
            if (!edit) {
                buttonsPane.getChildren().remove(editButton);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void afterPageLoaded() {
        try {
            bottomLabel.setText(AppVariables.message("Loaded"));
            Document doc = webEngine.getDocument();
            if (doc == null) {
                return;
            }
            NodeList nodeList = doc.getElementsByTagName("a");
            for (int i = 0; i < nodeList.getLength(); i++) {
                EventTarget t = (EventTarget) nodeList.item(i);
                t.addEventListener("click", linkListener, false);
                t.addEventListener("mouseover", linkListener, false);
                t.addEventListener("mouseout", linkListener, false);
                t.addEventListener("contextmenu", linkListener, false);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setAddress(String value) {
        this.address = value;
        if (address != null) {
            if (address.toLowerCase().startsWith("file:/")) {
                setSourceFile(new File(address.substring(6)));
            } else if (address.startsWith("//")) {
                this.address = "http:" + value;
            } else {
                File file = new File(address);
                if (file.exists()) {
                    setSourceFile(file);
                } else {
                    setSourceFile(null);
                }
            }
        }
    }

    public void setSourceFile(File file) {
        this.sourceFile = file;
        parentController.sourceFile = file;
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

    protected void popLinkMenu(String href, String name) {
        if (linkX < 0 || linkY < 0 || href == null || href.isBlank()) {
            return;
        }
        String linkAddress;
        try {
            linkAddress = new URL(new URL(address), href).toString();
        } catch (Exception e) {
            linkAddress = href;
        }
        String finalAddress = linkAddress == null ? href : linkAddress;
        List<MenuItem> items = new ArrayList<>();
        boolean showName = name != null && !name.isBlank() && !name.equalsIgnoreCase(href);
        String title = message("Name") + ": " + (showName ? name + "\n" : "")
                + message("Link") + ": " + href
                + (!finalAddress.equalsIgnoreCase(href) ? "\n" + message("Address") + ": " + linkAddress : "");
        MenuItem menu = new MenuItem(title);
        menu.setStyle("-fx-text-fill: #2e598a;");
        items.add(menu);
        items.add(new SeparatorMenuItem());

        if (parentController instanceof WebBrowserController) {
            menu = new MenuItem(message("OpenLinkInNewTab"));
            menu.setOnAction((ActionEvent event) -> {
                WebBrowserController c = (WebBrowserController) parentController;
                c.newTabAction(finalAddress, true);
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
            WebBrowserController controller = (WebBrowserController) openStage(CommonValues.WebBrowserFxml);
            controller.newTabAction(finalAddress, true);
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
    protected void snap() {
        String html = FxmlControl.getHtml(webEngine);
        if (html.isBlank()) {
            popError(message("NoData"));
            return;
        }
        HtmlSnapController controller = (HtmlSnapController) openStage(CommonValues.HtmlSnapFxml);
        controller.load(address, html);
    }

    @FXML
    protected void editAction() {
        String html = FxmlControl.getHtml(webEngine);
        if (html.isBlank()) {
            popError(message("NoData"));
            return;
        }
        HtmlEditorController controller = (HtmlEditorController) openStage(CommonValues.HtmlEditorFxml);
        controller.load(address, html);
    }

}
