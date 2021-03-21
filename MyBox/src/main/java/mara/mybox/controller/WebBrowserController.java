package mara.mybox.controller;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.tools.NetworkTools;
import mara.mybox.tools.SystemTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-7-31
 * @Description
 * @License Apache License Version 2.0
 */
public class WebBrowserController extends BaseController {

    protected int delay, fontSize, orginalStageHeight, orginalStageY, orginalStageWidth;
    protected int snapHeight, snapCount;
    protected boolean isOneImage;
    protected List<Image> images;
    protected float zoomScale;
    protected boolean loadSynchronously, isFrameSet;
    protected int cols, rows;
    protected Map<Tab, ControlWebBrowserBox> tabControllers;
    protected Tab hisTab;

    @FXML
    protected Button loadButton;
    @FXML
    protected TabPane tabPane;
    @FXML
    protected CheckBox bypassCheck;
    @FXML
    protected TextField bottomText, findInput;

    public WebBrowserController() {
        baseTitle = AppVariables.message("WebBrowser");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Html);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            isSettingValues = false;
            fontSize = 14;
            zoomScale = 1.0f;
            isFrameSet = false;

            tabControllers = new HashMap();
            newTabAction(null, false);

            initOptions();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    protected void initOptions() {
        try {

            bypassCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldv, Boolean newv) {
                    AppVariables.setUserConfigValue("SSLBypassAll", newv);
                    if (newv) {
                        NetworkTools.trustAll();
                    } else {
                        NetworkTools.myBoxSSL();
                    }
                }
            });
//            bypassCheck.setSelected(AppVariables.getUserConfigBoolean("SSLBypassAll", false));
//            if (bypassCheck.isSelected()) {
//                NetworkTools.trustAll();
//            } else {
//                NetworkTools.myBoxSSL();
//            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    protected WebEngine initWebEngine() {
        if (tabControllers == null || tabControllers.isEmpty()) {
            return null;
        }
        for (Tab tab : tabControllers.keySet()) {
            ControlWebBrowserBox c = tabControllers.get(tab);
            return c.webEngine;
        }
        return null;
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();

            hideRightPane();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    protected void newTabAction() {
        newTabAction(null, true);
    }

    protected ControlWebBrowserBox newTabAction(String address, boolean focus) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(FxmlStage.class.getResource(
                    CommonValues.ControlWebBrowserBoxFxml), AppVariables.currentBundle);
            Pane pane = fxmlLoader.load();
            Tab tab = new Tab();
            ImageView tabImage = new ImageView("img/MyBox.png");
            tabImage.setFitWidth(20);
            tabImage.setFitHeight(20);
            tab.setGraphic(tabImage);
            tab.setContent(pane);
            tabPane.getTabs().add(tab);
            if (focus) {
                tabPane.getSelectionModel().select(tab);
            }

            ControlWebBrowserBox controller = (ControlWebBrowserBox) fxmlLoader.getController();
            controller.setBrowser(this, tab);
            tabControllers.put(tab, controller);
            tab.setOnClosed(new EventHandler<Event>() {
                @Override
                public void handle(Event event) {
                    tabControllers.remove(tab);
                }
            });

            if (address != null) {
                controller.loadAddress(address);
            }
            return controller;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public void loadFile(File file) {
        ControlWebBrowserBox c = newTabAction(null, true);
        c.loadFile(file);
    }

    @FXML
    protected void zoomIn() {
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        ControlWebBrowserBox controller = tabControllers.get(tab);
        if (controller == null) {
            return;
        }
        controller.zoomIn();
    }

    @FXML
    protected void zoomOut() {
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        ControlWebBrowserBox controller = tabControllers.get(tab);
        if (controller == null) {
            return;
        }
        controller.zoomOut();
    }

    @FXML
    protected void backAction() {
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        ControlWebBrowserBox controller = tabControllers.get(tab);
        if (controller == null) {
            return;
        }
        controller.backAction();
    }

    @FXML
    protected void forwardAction() {
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        ControlWebBrowserBox controller = tabControllers.get(tab);
        if (controller == null) {
            return;
        }
        controller.forwardAction();
    }

    @FXML
    protected void refreshAction() {
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        ControlWebBrowserBox controller = tabControllers.get(tab);
        if (controller == null) {
            return;
        }
        controller.refreshAction();
    }

    @FXML
    protected void manageCertificates() {
        SecurityCertificatesController controller = (SecurityCertificatesController) openStage(CommonValues.SecurityCertificatesFxml);
        controller.sourceFileChanged(new File(SystemTools.keystore()));
    }

    @FXML
    protected void manageBypass() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(FxmlStage.class.getResource(
                    CommonValues.SecurityCertificatesBypassFxml), AppVariables.currentBundle);
            Pane pane = fxmlLoader.load();
            Tab tab = new Tab(message("SSLVerificationBypassList"));
            ImageView tabImage = new ImageView("img/MyBox.png");
            tabImage.setFitWidth(20);
            tabImage.setFitHeight(20);
            tab.setGraphic(tabImage);
            tab.setContent(pane);
            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    protected void manageHistories() {
        try {
            if (hisTab != null && tabPane.getTabs().contains(hisTab)) {
                tabPane.getSelectionModel().select(hisTab);
                return;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(FxmlStage.class.getResource(
                    CommonValues.WebBrowserHistoryFxml), AppVariables.currentBundle);
            Pane pane = fxmlLoader.load();
            hisTab = new Tab(message("ManageHistories"));
            ImageView tabImage = new ImageView("img/MyBox.png");
            tabImage.setFitWidth(20);
            tabImage.setFitHeight(20);
            hisTab.setGraphic(tabImage);
            hisTab.setContent(pane);
            tabPane.getTabs().add(hisTab);
            tabPane.getSelectionModel().select(hisTab);

            WebBrowserHistoryController controller = (WebBrowserHistoryController) fxmlLoader.getController();
            controller.browserConroller = this;
            controller.loadTableData();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean leavingScene() {
        tabControllers.clear();
        tabControllers = null;
        return super.leavingScene();
    }

    public static WebBrowserController oneOpen() {
        WebBrowserController controller = null;
        Stage stage = FxmlStage.findStage(message("WebBrowser"));
        if (stage != null && stage.getUserData() != null) {
            try {
                controller = (WebBrowserController) stage.getUserData();
            } catch (Exception e) {
            }
        }
        if (controller == null) {
            controller = (WebBrowserController) FxmlStage.openStage(CommonValues.WebBrowserFxml);
        }
        if (controller != null) {
            controller.getMyStage().requestFocus();
        }
        return controller;
    }

    public static WebBrowserController oneOpen(File file) {
        WebBrowserController controller = oneOpen();
        if (controller != null && file != null) {
            controller.loadFile(file);
        }
        return controller;
    }

    public static WebBrowserController oneOpen(String address) {
        WebBrowserController controller = oneOpen();
        if (controller != null && address != null) {
            controller.newTabAction(address, true);
        }
        return controller;
    }

    public static WebBrowserController oneOpen(boolean his) {
        WebBrowserController controller = oneOpen();
        if (controller != null && his) {
            controller.manageHistories();
        }
        return controller;
    }

}
