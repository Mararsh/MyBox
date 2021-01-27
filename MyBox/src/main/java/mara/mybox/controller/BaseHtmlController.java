package mara.mybox.controller;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import mara.mybox.data.BrowserHistory;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.db.table.TableBrowserBypassSSL;
import mara.mybox.db.table.TableBrowserHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.HtmlTools;
import mara.mybox.tools.NetworkTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-10-20
 * @License Apache License Version 2.0
 */
public abstract class BaseHtmlController extends BaseController {

    protected WebEngine webEngine;
    protected URI uri;
    protected LoadingController loadingController;
    protected float zoomScale;

    @FXML
    protected WebView webView;
    @FXML
    protected ComboBox<String> urlBox;
    @FXML
    protected Label webLabel;
    @FXML
    protected Button functionsButton;

    public BaseHtmlController() {
        baseTitle = AppVariables.message("Html");

        SourceFileType = VisitHistory.FileType.Html;
        SourcePathType = VisitHistory.FileType.Html;
        TargetPathType = VisitHistory.FileType.Html;
        TargetFileType = VisitHistory.FileType.Html;
        AddFileType = VisitHistory.FileType.Html;
        AddPathType = VisitHistory.FileType.Html;

        sourcePathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.Html);
        targetPathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.Html);

        sourceExtensionFilter = CommonFxValues.HtmlExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            zoomScale = 1.0f;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            initBroswer();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initBroswer() {
        try {
            if (AppVariables.getUserConfigBoolean("SSLBypassAll", false)) {
                NetworkTools.trustAll();
            } else {
                NetworkTools.myBoxSSL();
            }
            List<String> urls = TableBrowserHistory.recentBrowse();
            if (!urls.isEmpty()) {
                isSettingValues = true;
                urlBox.getItems().addAll(urls);
                isSettingValues = false;
            }

            webEngine = webView.getEngine();
            webEngine.setJavaScriptEnabled(true);

            webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
                @Override
                public void changed(ObservableValue ov, State oldState, State newState) {
                    try {
                        if (isSettingValues) {
                            return;
                        }
                        switch (newState) {
                            case SUCCEEDED:
//                                MyBoxLog.debug("SUCCEEDED");
                                afterPageLoaded();
                                break;
                            case FAILED:
//                                MyBoxLog.debug("Failed");
                                webLabel.setText(message("Failed"));
                                afterPageFailed();
                                break;
                            case CANCELLED:
//                                MyBoxLog.debug("Canceled");
                                webLabel.setText(message("Canceled"));
                                afterPageFailed();
                                break;
                        }

                    } catch (Exception e) {
                        MyBoxLog.debug(e.toString());
                        webLabel.setText(e.toString());
                        afterPageFailed();
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
                        webLabel.setText(nt.getMessage());
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
                                goAction();

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
                        MyBoxLog.debug(e.toString());
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();

            if (functionsButton != null) {
                FxmlControl.removeTooltip(functionsButton);
            }

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void updateTitle(boolean changed) {
        String t = getBaseTitle();
        if (sourceFile != null) {
            t += "  " + sourceFile.getAbsolutePath();
        }
        if (changed) {
            t += "*";
        }
        getMyStage().setTitle(t);
    }

    @Override
    public void sourceFileChanged(final File file) {
        loadLink(file);
    }

    public void loadLink(File file) {
        if (file == null) {
            popError(message("InvalidData"));
            return;
        }
        sourceFile = file;
        loadLink(file.toURI());
    }

    public void loadLink(String address) {
        loadLink(HtmlTools.uri(address));
    }

    public void loadContents(String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        sourceFile = null;
        uri = null;
        webEngine.loadContent(text);
        updateTitle(true);
    }

    @FXML
    @Override
    public void goAction() {
        sourceFile = null;
        String address = urlBox.getValue();
        URI u = HtmlTools.uri(address);
        if (u == null) {
            urlBox.getEditor().setStyle(badStyle);
            return;
        }
        urlBox.getEditor().setStyle(null);
        loadLink(u);
    }

    public void loadLink(URI uri) {
        try {
            if (uri == null) {
                popError(message("InvalidData"));
                return;
            }
            this.uri = uri;
            urlBox.setValue(uri.toString());
            webEngine.getLoadWorker().cancel();
            webEngine.loadContent(uri + "<br>" + message("Loading..."));
            webLabel.setText(uri + "  " + message("Loading..."));
            webEngine.load(uri.toString());
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void reset() {
        if (loadingController != null) {
            loadingController.closeStage();
            loadingController = null;
        }
    }

    protected void afterPageFailed() {
        reset();
//        webEngine.loadContent("");
    }

    protected void afterPageLoaded() {
        try {
            reset();
            webLabel.setText(AppVariables.message("Loaded"));
            if (uri == null) {
                return;
            }
            String address = uri.toString();
            BrowserHistory his = new BrowserHistory();
            his.setAddress(address);
            his.setVisitTime(new Date().getTime());
            his.setTitle(webEngine.getTitle());
//            File path = new File(MyboxDataPath + File.separator + "icons");
//            if (!path.exists()) {
//                path.mkdirs();
//            }
//            if (uri.getScheme().startsWith("http")) {
//                File file = new File(MyboxDataPath + File.separator + "icons" + File.separator + uri.getHost() + ".png");
//                if (!file.exists()) {
//                    HtmlTools.readIcon(address, file);
//                }
//                if (file.exists()) {
//                    his.setIcon(file.getAbsolutePath());
//                } else {
//                    his.setIcon("");
//                }
//            } else {
//                his.setIcon("");
//            }
            TableBrowserHistory.write(his);

            isSettingValues = true;
            urlBox.getItems().clear();
            List<String> urls = TableBrowserHistory.recentBrowse();
            if (!urls.isEmpty()) {
                urlBox.getItems().addAll(urls);
                urlBox.getEditor().setText(address);
            }

            updateTitle(true);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        isSettingValues = false;
    }

    @FXML
    public void popFunctionsMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menu;

            menu = new MenuItem(message("Cancel"));
            menu.setOnAction((ActionEvent event) -> {
                webEngine.getLoadWorker().cancel();
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Refresh"));
            menu.setOnAction((ActionEvent event) -> {
                if (uri != null) {
                    loadLink(uri);
                } else {
                    webEngine.executeScript("location.reload() ;");
                }
            });
            popMenu.getItems().add(menu);
            popMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(message("ZoomIn"));
            menu.setOnAction((ActionEvent event) -> {
                zoomScale += 0.1f;
                webView.setZoom(zoomScale);
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("ZoomOut"));
            menu.setOnAction((ActionEvent event) -> {
                zoomScale -= 0.1f;
                webView.setZoom(zoomScale);
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Back"));
            menu.setOnAction((ActionEvent event) -> {
                webEngine.executeScript("window.history.back();");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Forward"));
            menu.setOnAction((ActionEvent event) -> {
                webEngine.executeScript("window.history.forward();");
            });
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("HtmlSnap"));
            menu.setOnAction((ActionEvent event) -> {
                String html = FxmlControl.getHtml(webEngine);
                if (html.isBlank()) {
                    popError(message("NoData"));
                    return;
                }
                HtmlSnapController controller = (HtmlSnapController) openStage(CommonValues.HtmlSnapFxml);
                controller.loadContents(html);
                controller.urlBox.setValue(urlBox.getValue());
            });
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateBelow((Region) mouseEvent.getSource(), popMenu);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
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

}
