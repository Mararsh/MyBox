package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import javafx.stage.Window;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.HtmlReadTools;

import mara.mybox.tools.LocationTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import mara.mybox.value.SystemConfig;

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
        baseTitle = Languages.message("WebBrowser");
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
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeTools.setTooltip(addIcon, new Tooltip(Languages.message("Add")));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();

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
            FXMLLoader fxmlLoader = new FXMLLoader(WindowTools.class.getResource(
                    Fxmls.ControlWebBrowserBoxFxml), AppVariables.currentBundle);
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
            refreshStyle(pane);

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

    public ControlWebBrowserBox loadAddress(String address, boolean focus) {
        ControlWebBrowserBox controller = newTab(focus);
        if (address != null) {
            controller.loadAddress(address);
        }
        return controller;
    }

    public ControlWebBrowserBox loadContents(String contents, boolean focus) {
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

    public void download(String address, String name) {
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
                String nameSuffix = FileNameTools.getFileSuffix(name);
                String addrSuffix = FileNameTools.getFileSuffix(address);
                if (addrSuffix != null && !addrSuffix.isBlank()) {
                    if (nameSuffix == null || nameSuffix.isBlank()
                            || !addrSuffix.equalsIgnoreCase(nameSuffix)) {
                        dname = name + "." + addrSuffix;
                    }
                }
            } else {
                dname = address;
            }
            int pos = dname.lastIndexOf("/");
            if (pos >= 0) {
                dname = (pos < dname.length() - 1) ? dname.substring(pos + 1) : "";
            }
            File dnFile = chooseSaveFile(VisitHistory.FileType.All, dname);
            if (dnFile == null) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    File tmpFile = HtmlReadTools.url2File(address);
                    return FileTools.rename(tmpFile, dnFile);
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    browseURI(dnFile.toURI());
                }

            };
            handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
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
            LoadingController c = handling(Languages.message("FirstRunInfo"));
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
                        SystemConfig.setSystemConfigBoolean("WeiboRunFirstTime", false);
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
            LoadingController c = handling(Languages.message("FirstRunInfo"));
            c.cancelButton.setDisable(true);
            loadContents(LocationTools.gaodeMap(), false);
            loadAddress(LocationTools.tiandituFile(true).toURI().toString(), true);
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        SystemConfig.setSystemConfigBoolean("MapRunFirstTime", false);
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
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof WebBrowserController) {
                try {
                    controller = (WebBrowserController) object;
                    controller.toFront();
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (WebBrowserController) WindowTools.openStage(Fxmls.WebBrowserFxml);
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
        WebBrowserController controller = (WebBrowserController) WindowTools.openStage(Fxmls.WebBrowserFxml);
        controller.initWeibo();
        return controller;
    }

    public static WebBrowserController mapFirstRun() {
        WebBrowserController controller = (WebBrowserController) WindowTools.openStage(Fxmls.WebBrowserFxml);
        controller.initMap();
        return controller;
    }

}
