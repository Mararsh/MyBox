package mara.mybox.fxml;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ScheduledFuture;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.BaseController_Attributes.StageType;
import mara.mybox.controller.BaseTaskController;
import mara.mybox.controller.ClearExpiredDataController;
import mara.mybox.controller.WindowsListController;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.DerbyBase.DerbyStatus;
import mara.mybox.db.table.TableUserConf;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.AppValues.AppIcon;
import mara.mybox.value.AppVariables;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-1-27 21:48:55
 * @License Apache License Version 2.0
 */
public class WindowTools {


    /*
     * make stage
     */
    public static BaseController initScene(Stage stage, String newFxml, StageStyle stageStyle) {
        return initScene(stage, newFxml, AppVariables.CurrentBundle, stageStyle);
    }

    public static BaseController initScene(Stage stage, String newFxml, ResourceBundle bundle, StageStyle stageStyle) {
        try {
            if (stage == null) {
                return null;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(WindowTools.class.getResource(newFxml), bundle);
            return initController(fxmlLoader, stage, stageStyle);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static BaseController initController(FXMLLoader fxmlLoader, Stage stage, StageStyle stageStyle) {
        try {
            if (fxmlLoader == null) {
                return null;
            }
            Scene scene = new Scene(fxmlLoader.load());
            BaseController controller = (BaseController) fxmlLoader.getController();
            return initController(controller, scene, stage, stageStyle);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    // this works for top level pane which is not part of other node
    public static BaseController initController(BaseController controller, Scene scene, Stage stage, StageStyle stageStyle) {
        try {
            if (controller == null || scene == null || stage == null) {
                return null;
            }
            controller.setMyScene(scene);
            controller.setMyStage(stage);
            controller.setMyWindow(stage);
            scene.getStylesheets().add(WindowTools.class.getResource(UserConfig.getStyle()).toExternalForm());

            stage.setUserData(controller);
            stage.getIcons().add(AppIcon);
            stage.setTitle(controller.getBaseTitle());
            if (stageStyle != null) {
                stage.initStyle(stageStyle);
            }
            // External request to close
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    if (!controller.leavingScene()) {
                        event.consume();
                    } else {
                        WindowTools.closeWindow(stage);
                    }
                }
            });

            stage.setOnHiding(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    if (controller != null) {
                        controller.leaveScene();
                    }
                    WindowTools.closeWindow(stage);   // Close anyway
                }
            });

            stage.focusedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ob, Boolean ov, Boolean nv) {
                    if (!nv && controller != null) {
                        controller.closePopup();
                    }
                }
            });

            stage.setScene(scene);
            stage.show();

            if (controller != null) {
                Parent root = scene.getRoot();
                if (controller.getThisPane() != root) {
                    controller.monitorKeyEvents(root);
                }
                controller.afterSceneLoaded();
            }

            WindowsListController.refresh();

            Platform.setImplicitExit(AppVariables.ScheduledTasks == null || AppVariables.ScheduledTasks.isEmpty());

            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static BaseController initController(FXMLLoader fxmlLoader) {
        return initController(fxmlLoader, newStage(), null);
    }

    public static BaseController loadFxml(String fxml) {
        try {
            if (fxml == null) {
                return null;
            }
            return loadURL(WindowTools.class.getResource(fxml));
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static BaseController loadFile(File file) {
        try {
            if (file == null || !file.exists()) {
                return null;
            }
            return loadURL(file.toURI().toURL());
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    // the pane may be part of other node
    public static BaseController loadURL(URL url) {
        try {
            if (url == null) {
                return null;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(url, AppVariables.CurrentBundle);
            Pane pane = fxmlLoader.load();
            try {
                pane.getStylesheets().add(WindowTools.class.getResource(UserConfig.getStyle()).toExternalForm());
            } catch (Exception e) {
            }
            Scene scene = new Scene(pane);

            BaseController controller = (BaseController) fxmlLoader.getController();
            controller.setMyScene(scene); // the final scene may be changed
            controller.refreshStyle();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static FXMLLoader newFxml(String fxml) {
        try {
            return new FXMLLoader(WindowTools.class.getResource(fxml), AppVariables.CurrentBundle);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static Stage newStage() {
        try {
            Stage newStage = new Stage();
            newStage.initModality(Modality.NONE);
            newStage.initStyle(StageStyle.DECORATED);
            newStage.initOwner(null);
            return newStage;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }


    /*
     * open stage
     */
    public static BaseController openStage(String fxml) {
        return initController(newFxml(fxml), newStage(), null);
    }

    public static BaseController replaceStage(Window parent, String fxml) {
        try {
            Stage newStage = new Stage();  // new stage should be opened instead of keeping old stage, to clean resources
            newStage.initModality(Modality.NONE);
            newStage.initStyle(StageStyle.DECORATED);
            newStage.initOwner(null);
            BaseController controller = initScene(newStage, fxml, StageStyle.DECORATED);
            if (controller != null) {
                closeWindow(parent);
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static BaseController openStage(Window parent, String fxml, ResourceBundle bundle,
            boolean isOwned, Modality modality, StageStyle stageStyle) {
        try {
            Stage stage = new Stage();
            stage.initModality(modality);
            if (isOwned && parent != null) {
                if (parent instanceof Popup) {
                    stage.initOwner(((Popup) parent).getOwnerWindow());
                } else {
                    stage.initOwner(parent);
                }
            } else {
                stage.initOwner(null);
            }
            return initScene(stage, fxml, bundle, stageStyle);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static BaseController openStage(Window parent, String newFxml) {
        return openStage(parent, newFxml, AppVariables.CurrentBundle, false, Modality.NONE, null);
    }

    public static BaseController openStage(String fxml, ResourceBundle bundle) {
        return openStage(null, fxml, bundle, false, Modality.NONE, null);
    }

    /*
     * sub stage
     */
    public static BaseController childStage(BaseController parent, String newFxml) {
        try {
            if (parent == null) {
                return openStage(newFxml);
            }
            BaseController c = openStage(parent.getMyWindow(), newFxml,
                    AppVariables.CurrentBundle, true, Modality.WINDOW_MODAL, null);
            if (c == null) {
                return null;
            }
            c.setParent(parent, StageType.Child);
            return c;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static BaseController popupStage(BaseController parent, String newFxml) {
        try {
            if (parent == null) {
                return null;
            }
            BaseController c = openStage(parent.getMyWindow(), newFxml,
                    AppVariables.CurrentBundle, true, Modality.WINDOW_MODAL, null);
            if (c == null) {
                return null;
            }
            c.setParent(parent, StageType.Popup);
            return c;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static BaseController referredTopStage(BaseController parent, String newFxml) {
        try {
            BaseController c = openStage(newFxml);
            if (c == null) {
                return null;
            }
            c.setParent(parent, StageType.RefferredTop);
            return c;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static BaseController forkStage(BaseController parent, String newFxml) {
        try {
            BaseController c = openStage(newFxml);
            if (c == null) {
                return null;
            }
            c.setParent(parent, StageType.Fork);
            return c;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static BaseController referredStage(BaseController parent, String newFxml) {
        try {
            BaseController c = openStage(newFxml);
            if (c == null) {
                return null;
            }
            c.setParent(parent, StageType.Referred);
            return c;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static BaseController topStage(BaseController parent, String newFxml) {
        try {
            BaseController c = parent != null
                    ? openStage(parent.getMyWindow(), newFxml)
                    : openStage(newFxml);
            if (c == null) {
                return null;
            }
            c.setParent(parent, StageType.Top);
            return c;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }


    /*
     * handle stage
     */
    public static void reloadAll() {
        try {
            List<Window> windows = new ArrayList<>();
            windows.addAll(Window.getWindows());
            for (Window window : windows) {
                if (!window.isShowing()) {
                    continue;
                }
                Object object = window.getUserData();
                if (object != null && object instanceof BaseController) {
                    try {
                        BaseController controller = (BaseController) object;
                        controller.reload();
                    } catch (Exception e) {
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static void refreshInterfaceAll() {
        try {
            List<Window> windows = new ArrayList<>();
            windows.addAll(Window.getWindows());
            for (Window window : windows) {
                if (!window.isShowing()) {
                    continue;
                }
                Object object = window.getUserData();
                if (object != null && object instanceof BaseController) {
                    try {
                        BaseController controller = (BaseController) object;
                        controller.refreshInterface();
                    } catch (Exception e) {
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static void styleAll(String style) {
        try {
            List<Window> windows = new ArrayList<>();
            windows.addAll(Window.getWindows());
            for (Window window : windows) {
                if (!window.isShowing()) {
                    continue;
                }
                Object object = window.getUserData();
                if (object != null && object instanceof BaseController) {
                    try {
                        BaseController controller = (BaseController) object;
                        controller.setInterfaceStyle(style);
                    } catch (Exception e) {
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static boolean resetWindows() {
        if (!TableUserConf.deletePrefix("Interface_")) {
            return false;
        }
        List<String> keys = new ArrayList<>();
        keys.addAll(AppVariables.UserConfigValues.keySet());
        for (String key : keys) {
            if (key.startsWith("Interface_")) {
                AppVariables.UserConfigValues.remove(key);
            }
        }
        return true;
    }

    public static void closeWindow(Window window) {
        try {
            if (window == null) {
                return;
            }
            Object object = window.getUserData();
            if (object != null) {
                try {
                    if (object instanceof BaseController) {
                        ((BaseController) object).leaveScene();
                    }
                } catch (Exception e) {
                }
                window.setUserData(null);
            }
            window.hide();
            WindowsListController.refresh();
            checkExit();
        } catch (Exception e) {
//            MyBoxLog.error(e.toString());
        }
    }

    public static void checkExit() {
        try {
            List<Window> windows = new ArrayList<>();
            windows.addAll(Window.getWindows());
            for (Window window : windows) {
                if (window != null && window.isShowing()) {
                    return;
                }
            }
            appExit();
        } catch (Exception e) {
//            MyBoxLog.error(e.toString());
        }

    }

    public static synchronized void appExit() {
        if (AppVariables.handlingExit) {
            return;
        }
        if (UserConfig.getBoolean("ClearExpiredDataBeforeExit", true)) {
            AppVariables.handlingExit = true;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    ClearExpiredDataController.open(true);
                }
            });

        } else {
            handleExit();
        }
    }

    public static synchronized void handleExit() {
        try {
            if (AppVariables.handlingExit) {
                return;
            }
            AppVariables.handlingExit = true;
            if (Window.getWindows() != null) {
                List<Window> windows = new ArrayList<>();
                windows.addAll(Window.getWindows());
                for (Window window : windows) {
                    closeWindow(window);
                }
            }
            ImageClipboardTools.stopImageClipboardMonitor();
            TextClipboardTools.stopTextClipboardMonitor();

            if (AppVariables.ScheduledTasks != null && !AppVariables.ScheduledTasks.isEmpty()) {
                if (UserConfig.getBoolean("StopAlarmsWhenExit")) {
                    for (String key : AppVariables.ScheduledTasks.keySet()) {
                        ScheduledFuture future = AppVariables.ScheduledTasks.get(key);
                        future.cancel(true);
                    }
                    AppVariables.ScheduledTasks = null;
                    if (AppVariables.ExecutorService != null) {
                        AppVariables.ExecutorService.shutdownNow();
                        AppVariables.ExecutorService = null;
                    }
                }
            } else {
                if (AppVariables.ScheduledTasks != null) {
                    AppVariables.ScheduledTasks = null;
                }
                if (AppVariables.ExecutorService != null) {
                    AppVariables.ExecutorService.shutdownNow();
                    AppVariables.ExecutorService = null;
                }
            }

            if (AppVariables.ExitTimer != null) {
                AppVariables.ExitTimer.cancel();
                AppVariables.ExitTimer = null;
            }

            if (AppVariables.ScheduledTasks == null || AppVariables.ScheduledTasks.isEmpty()) {
                doExit();
                return;
            }

        } catch (Exception e) {

        }
        AppVariables.handlingExit = false;

    }

    public static synchronized void doExit() {
        MyBoxLog.info("Exit now. Bye!");
        if (DerbyBase.status == DerbyStatus.Embedded) {
            MyBoxLog.info("Shut down Derby...");
            DerbyBase.shutdownEmbeddedDerby();
        }
//                AppVariables.handlingExit = false;

        Platform.setImplicitExit(true);
        System.gc();
        Platform.exit(); // Some thread may still be alive after this
        System.exit(0);  // Go
        Runtime.getRuntime().halt(0);
    }

    public static void taskInfo(FxTask task, String info) {
        if (task != null) {
            task.setInfo(info);
        }
        MyBoxLog.console(info);
    }

    public static void taskError(FxTask task, String error) {
        if (task != null) {
            task.setError(error);
        }
        MyBoxLog.error(error);
    }

    public static void recordInfo(BaseTaskController taskController, String info) {
        if (taskController != null) {
            taskController.updateLogs(info);
        }
        MyBoxLog.console(info);
    }

    public static void recordError(BaseTaskController taskController, String error) {
        if (taskController != null) {
            taskController.showLogs(error);
        }
        MyBoxLog.error(error);
    }

    public static void closeAllPopup() {
        try {
            List<Window> windows = new ArrayList<>();
            windows.addAll(Window.getWindows());
            for (Window window : windows) {
                if (window instanceof Popup) {
                    window.hide();
                }
            }
        } catch (Exception e) {
        }
    }

    public static BaseController getController(Window window) {
        if (window == null) {
            return null;
        }
        Object object = window.getUserData();
        if (object != null) {
            try {
                if (object instanceof BaseController) {
                    return (BaseController) object;
                }
            } catch (Exception e) {
            }
        }
        return null;
    }

    public static BaseController find(String interfaceName) {
        try {
            if (interfaceName == null || interfaceName.isBlank()) {
                return null;
            }
            List<Window> windows = new ArrayList<>();
            windows.addAll(Window.getWindows());
            for (Window window : windows) {
                Object object = window.getUserData();
                if (object != null && object instanceof BaseController) {
                    try {
                        BaseController controller = (BaseController) object;
                        if (interfaceName.equals(controller.getInterfaceName())) {
                            return controller;
                        }
                    } catch (Exception e) {
                    }
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static boolean isRunning(BaseController controller) {
        return controller != null
                && controller.getMyStage() != null
                && controller.getMyStage().isShowing();
    }

}
