package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.web.PopupFeatures;
import javafx.scene.web.PromptData;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebErrorEvent;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Callback;
import javax.imageio.ImageIO;
import mara.mybox.data.BrowserHistory;
import mara.mybox.data.VisitHistory;
import mara.mybox.db.TableBrowserBypassSSL;
import mara.mybox.db.TableBrowserHistory;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.tools.HtmlTools;
import mara.mybox.tools.NetworkTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.MyboxDataPath;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;
import mara.mybox.value.CommonValues;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

/**
 * @Author Mara
 * @CreateDate 2018-7-31
 * @Description
 * @License Apache License Version 2.0
 */
public class WebBrowserBoxController extends BaseController {

    protected WebBrowserController parent;
    protected Tab tab;
    protected WebEngine webEngine;
    protected URL url;
    protected float zoomScale;
    protected String status;
    protected boolean bypassSSL;
    protected BrowserHistory his;

    @FXML
    protected WebView webView;
    @FXML
    protected ComboBox<String> urlBox;

    public WebBrowserBoxController() {
        baseTitle = AppVariables.message("WebBrowser");

        SourceFileType = VisitHistory.FileType.Html;
        SourcePathType = VisitHistory.FileType.Html;
        TargetPathType = VisitHistory.FileType.Html;
        TargetFileType = VisitHistory.FileType.Html;
        AddFileType = VisitHistory.FileType.Html;
        AddPathType = VisitHistory.FileType.Html;

        sourcePathKey = "HtmlFilePath";
        targetPathKey = "HtmlFilePath";

        sourceExtensionFilter = CommonFxValues.HtmlExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;

    }

    @Override
    public void initializeNext() {
        try {
            isSettingValues = false;
            zoomScale = 1.0f;

            initURLBox();
            initWebEngine();

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    public void initURLBox() {
        try {
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

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    public void initWebEngine() {
        try {
            webEngine = webView.getEngine();
            webEngine.setJavaScriptEnabled(true);

            webEngine.setCreatePopupHandler(new Callback<PopupFeatures, WebEngine>() {
                @Override
                public WebEngine call(PopupFeatures pop) {

                    if (parent == null) {
                        WebBrowserController controller
                                = (WebBrowserController) openStage(CommonValues.WebBrowserFxml);
                        return controller.initWebEngine();
                    } else {
                        WebBrowserBoxController controller = parent.newTabAction(null, false);
                        if (controller != null) {
                            return controller.webEngine;
                        } else {
                            return null;
                        }
                    }
                }
            });

            webEngine.setPromptHandler(new Callback< PromptData, String>() {
                @Override
                public String call(PromptData p) {
                    logger.debug(p.getMessage());
                    popInformation(p.getMessage() + " " + p.getDefaultValue());
                    return p.getDefaultValue();
                }
            });
            webEngine.setOnAlert(new EventHandler<WebEvent<String>>() {
                @Override
                public void handle(WebEvent<String> ev) {
                    FxmlStage.alertError(getMyStage(), ev.getData());
                    logger.debug(ev.getData());
                }
            });
            webEngine.setConfirmHandler(new Callback< String, Boolean>() {
                @Override
                public Boolean call(String message) {
                    try {
                        logger.debug(message);
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
                        logger.error(e.toString());
                        return false;
                    }

                }
            });
            webEngine.setOnError(new EventHandler<WebErrorEvent>() {

                @Override
                public void handle(WebErrorEvent event) {
                    popError(event.getMessage());
                    logger.debug(event.getMessage());
                }
            });
            webEngine.setOnStatusChanged(new EventHandler<WebEvent<String>>() {
                @Override
                public void handle(WebEvent<String> ev) {
                    bottomLabel.setText(ev.getData());
//                    logger.debug(ev.getData());
                }
            });

            webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
                @Override
                public void changed(ObservableValue ov, State oldState, State newState) {
                    try {
                        status = newState.name();
                        bottomLabel.setText(status);
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
                        }

                    } catch (Exception e) {
                        logger.debug(e.toString());
                    }

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
                                if (result.get() == buttonCancel) {
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
                                if (result.get() == buttonCancel) {
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

            webEngine.locationProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldv, String newv) {
                    bottomLabel.setText(newv);
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
                        String finalAddress = address;
                        if (!address.startsWith("http") && !new File(address).exists()) {
                            finalAddress = "http://" + address;
                        }
                        url = new URL(finalAddress);
                        if (url != null) {
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

    protected void clickLink() {
        EventListener listener = new EventListener() {
            public void handleEvent(Event ev) {
//                    ev.;
            }
        };
        Document doc = webEngine.getDocument();
        Element el = doc.getElementById("exit-app");
        ((EventTarget) el).addEventListener("click", listener, false);

        NodeList links = doc.getElementsByTagName("a");
        for (int i = 0; i < links.getLength(); i++) {
//            Node node = links.item(i);
//            String link = links.item(i).toString();
//
//            EventListener listener = new EventListener() {
//                public void handleEvent(Event ev) {
////                    ev.;
//                }
//            };

        }
    }

    public void loadLink(String link) {
        urlBox.setValue(link);
    }

    public void loadFile(File file) {
        sourceFile = file;
        loadLink(file.toURI().toString());

    }

    @FXML
    protected void load() {
        try {
            webEngine.getLoadWorker().cancel();
            if (url == null) {
                return;
            }
            bottomLabel.setText(AppVariables.message("Loading..."));
            final String address = url.toString().toLowerCase();

            his = new BrowserHistory();
            his.setAddress(address);
            his.setVisitTime(new Date().getTime());
            his.setTitle(address);
            webEngine.load(address);

            File iconFile = new File(MyboxDataPath + File.separator + "icons" + File.separator + url.getHost() + ".png");
            if (iconFile.exists()) {
                his.setIcon(iconFile.getAbsolutePath());
                if (tab != null) {
                    BufferedImage image = ImageIO.read(iconFile);
                    if (image != null) {
                        ImageView tabImage = new ImageView(SwingFXUtils.toFXImage(image, null));
                        tabImage.setFitWidth(20);
                        tabImage.setFitHeight(20);
                        tab.setGraphic(tabImage);
                    }
                }
            }

            TableBrowserHistory.write(his);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void afterPageLoaded() {
        try {
            bottomLabel.setText(AppVariables.message("Loaded"));
            if (url == null) {
                return;
            }
            String address = url.toString().toLowerCase();
            if (his == null) {
                his = new BrowserHistory();
                his.setAddress(address);
            }
            his.setVisitTime(new Date().getTime());
            String title = webEngine.getTitle();
            if (title != null) {
                his.setTitle(title);
                if (tab != null) {
                    tab.setText(title.substring(0, Math.min(10, title.length())));
                }
            } else {
                his.setTitle("");
                if (tab != null) {
                    tab.setText("");
                }
            }
            File path = new File(MyboxDataPath + File.separator + "icons");
            if (!path.exists()) {
                path.mkdirs();
            }
            File file = new File(MyboxDataPath + File.separator + "icons" + File.separator + url.getHost() + ".png");
            if (!file.exists()) {
                HtmlTools.readIcon(address, file);
            }
            if (file.exists()) {
                his.setIcon(file.getAbsolutePath());
                if (tab != null) {
                    BufferedImage image = ImageIO.read(file);
                    if (image != null) {
                        ImageView tabImage = new ImageView(SwingFXUtils.toFXImage(image, null));
                        tabImage.setFitWidth(20);
                        tabImage.setFitHeight(20);
                        tab.setGraphic(tabImage);
                    }
                }
            } else {
                his.setIcon("");
                if (tab != null) {
                    ImageView tabImage = new ImageView("img/MyBox.png");
                    tabImage.setFitWidth(20);
                    tabImage.setFitHeight(20);
                    tab.setGraphic(tabImage);
                }
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
//        if (webEngine.getHistory() == null || webEngine.getHistory().getCurrentIndex() == 0) {
//            return;
//        }
//        List<Entry> his = webEngine.getHistory().getEntries();
//        if (his == null || his.isEmpty()) {
//            return;
//        }
//       Entry pre = his.get(webEngine.getHistory().getCurrentIndex() - 1);
//       pre.
        webEngine.executeScript("window.history.forward();");
    }

    @FXML
    protected void refreshAction() {
        load();
    }

    @Override
    public boolean leavingScene() {
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

}
