package mara.mybox.controller;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.HtmlTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-7-31
 * @License Apache License Version 2.0
 */
public class WebBrowserController extends BaseController {

    protected Map<Tab, ControlWebBrowserBox> tabControllers;
    protected Tab hisTab, favoriteTab;

    @FXML
    protected TabPane tabPane;
    @FXML
    protected Tab addTab;
    @FXML
    protected ImageView addIcon;

    public WebBrowserController() {
        baseTitle = AppVariables.message("WebBrowser");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Html);
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            tabControllers = new HashMap();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();

            FxmlControl.setTooltip(addIcon, new Tooltip(message("Add")));
            newTabAction();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    protected void newTabAction() {
        newTab(true);
    }

    protected ControlWebBrowserBox newTab(boolean focus) {
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
            tabPane.getTabs().remove(addTab);
            tabPane.getTabs().addAll(tab, addTab);
            if (focus) {
                tabPane.getSelectionModel().select(tab);
            }
            FxmlControl.refreshStyle(pane);

            ControlWebBrowserBox controller = (ControlWebBrowserBox) fxmlLoader.getController();
            controller.initTab(this, tab);
            if (tabControllers == null) {
                tabControllers = new HashMap();
            }
            tabControllers.put(tab, controller);
            tab.setOnClosed(new EventHandler<Event>() {
                @Override
                public void handle(Event event) {
                    tabControllers.remove(tab);
                }
            });
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    protected ControlWebBrowserBox loadAddress(String address, boolean focus) {
        ControlWebBrowserBox controller = newTab(focus);
        if (address != null) {
            controller.loadAddress(address);
        }
        return controller;
    }

    protected ControlWebBrowserBox loadContents(String contents, boolean focus) {
        ControlWebBrowserBox controller = newTab(focus);
        if (contents != null) {
            controller.loadContents(contents);
        }
        return controller;
    }

    public ControlWebBrowserBox loadFile(File file) {
        ControlWebBrowserBox controller = newTab(true);
        controller.loadFile(file);
        return controller;
    }

    protected void download(String address, String name) {
        if (address == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            String dname;
            if (name != null && !name.isBlank()) {
                dname = name;
                String nameSuffix = FileTools.getFileSuffix(name);
                String addrSuffix = FileTools.getFileSuffix(address);
                if (addrSuffix != null && !addrSuffix.isBlank()) {
                    if (nameSuffix == null || nameSuffix.isBlank()
                            || !addrSuffix.equalsIgnoreCase(nameSuffix)) {
                        dname = name + "." + addrSuffix;
                    }
                }
            } else {
                dname = FileTools.getName(address);
            }
            File dnFile = chooseSaveFile(VisitHistory.FileType.All, dname);
            if (dnFile == null) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    File tmpFile = HtmlTools.url2File(address);
                    return FileTools.rename(tmpFile, dnFile);
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    browseURI(dnFile.toURI());
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @Override
    public boolean leavingScene() {
        tabControllers.clear();
        tabControllers = null;
        return super.leavingScene();
    }

    protected void initWeibo() {
        try {
            getMyStage().toBack();
            LoadingController c = openHandlingStage(Modality.WINDOW_MODAL, message("FirstRunInfo"));
            c.cancelButton.setDisable(true);
            loadAddress("https://weibo.com", true);
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        loadAddress("https://weibo.com", true);
                    });
                }
            }, 12000);
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        AppVariables.setSystemConfigValue("WeiboRunFirstTime", false);
                        closeStage();
                    });
                }
            }, 20000);
        } catch (Exception e) {
            closeStage();
        }
    }

    protected void initMap() {
        try {
            getMyStage().toBack();
            LoadingController c = openHandlingStage(Modality.WINDOW_MODAL, message("FirstRunInfo"));
            c.cancelButton.setDisable(true);
            loadContents(FxmlControl.gaodeMap(), false);
            loadAddress(FxmlControl.tiandituFile(true).toURI().toString(), true);
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        AppVariables.setSystemConfigValue("MapRunFirstTime", false);
                        closeStage();
                    });
                }
            }, 3000);
        } catch (Exception e) {
            closeStage();
        }
    }

    /*
        static methods
     */
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
            controller.getMyStage().toFront();
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
            controller.loadAddress(address, true);
        }
        return controller;
    }

    public static WebBrowserController weiboSnapFirstRun() {
        WebBrowserController controller = (WebBrowserController) FxmlStage.openStage(CommonValues.WebBrowserFxml);
        controller.initWeibo();
        return controller;
    }

    public static WebBrowserController mapFirstRun() {
        WebBrowserController controller = (WebBrowserController) FxmlStage.openStage(CommonValues.WebBrowserFxml);
        controller.initMap();
        return controller;
    }

}
