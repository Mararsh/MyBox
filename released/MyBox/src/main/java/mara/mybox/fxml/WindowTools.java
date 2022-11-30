package mara.mybox.fxml;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ScheduledFuture;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import mara.mybox.controller.BaseController;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.DerbyBase.DerbyStatus;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.db.table.TableFileBackup;
import mara.mybox.db.table.TableImageClipboard;
import mara.mybox.db.table.TableImageEditHistory;
import mara.mybox.db.table.TableUserConf;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-1-27 21:48:55
 * @License Apache License Version 2.0
 */
public class WindowTools {

    public static final Image AppIcon = new Image("img/MyBox.png");

    public static BaseController initScene(Stage stage, String newFxml, StageStyle stageStyle) {
        return initScene(stage, newFxml, AppVariables.currentBundle, stageStyle);
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

    public static BaseController initController(BaseController controller, Scene scene, Stage stage, StageStyle stageStyle) {
        try {
            if (controller == null) {
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

            controller.afterSceneLoaded();

            Platform.setImplicitExit(AppVariables.scheduledTasks == null || AppVariables.scheduledTasks.isEmpty());

            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static BaseController initController(FXMLLoader fxmlLoader) {
        return initController(fxmlLoader, newStage(), null);
    }

    public static BaseController initController(BaseController controller, Scene scene) {
        return initController(controller, scene, newStage(), null);
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

    public static BaseController loadURL(URL url) {
        try {
            if (url == null) {
                return null;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(url, AppVariables.currentBundle);
            Pane pane = fxmlLoader.load();
            try {
                pane.getStylesheets().add(WindowTools.class.getResource(UserConfig.getStyle()).toExternalForm());
            } catch (Exception e) {
            }
            Scene scene = new Scene(pane);

            BaseController controller = (BaseController) fxmlLoader.getController();
            controller.setMyScene(scene);
            controller.refreshStyle();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static FXMLLoader newFxml(String fxml) {
        try {
            return new FXMLLoader(WindowTools.class.getResource(fxml), AppVariables.currentBundle);
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

    public static BaseController openStage(String fxml) {
        return initController(newFxml(fxml), newStage(), null);
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
        return openStage(parent, newFxml, AppVariables.currentBundle, false, Modality.NONE, null);
    }

    public static BaseController openChildStage(Window parent, String newFxml) {
        return openChildStage(parent, newFxml, true);
    }

    public static BaseController openChildStage(Window parent, String newFxml, boolean isModal) {
        return openStage(parent, newFxml, AppVariables.currentBundle, true, isModal ? Modality.WINDOW_MODAL : Modality.NONE, null);
    }

    public static BaseController handling(Window parent, String newFxml) {
        return openStage(parent, newFxml, AppVariables.currentBundle, true, Modality.WINDOW_MODAL, StageStyle.DECORATED);
    }

    public static BaseController openScene(Window parent, String newFxml, StageStyle stageStyle) {
        try {
            Stage newStage = new Stage();  // new stage should be opened instead of keeping old stage, to clean resources
            newStage.initModality(Modality.NONE);
            newStage.initStyle(StageStyle.DECORATED);
            newStage.initOwner(null);
            BaseController controller = initScene(newStage, newFxml, stageStyle);
            if (controller != null) {
                closeWindow(parent);
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static BaseController openScene(Window parent, String fxml) {
        return openScene(parent, fxml, null);
    }

    public static void reloadAll() {
        try {
            List<Window> windows = new ArrayList<>();
            windows.addAll(Window.getWindows());
            for (Window window : windows) {
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
        keys.addAll(AppVariables.userConfigValues.keySet());
        for (String key : keys) {
            if (key.startsWith("Interface_")) {
                AppVariables.userConfigValues.remove(key);
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

    public static void appExit() {
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

            clearInvalidData();

            if (AppVariables.scheduledTasks != null && !AppVariables.scheduledTasks.isEmpty()) {
                if (UserConfig.getBoolean("StopAlarmsWhenExit")) {
                    for (String key : AppVariables.scheduledTasks.keySet()) {
                        ScheduledFuture future = AppVariables.scheduledTasks.get(key);
                        future.cancel(true);
                    }
                    AppVariables.scheduledTasks = null;
                    if (AppVariables.executorService != null) {
                        AppVariables.executorService.shutdownNow();
                        AppVariables.executorService = null;
                    }
                }
            } else {
                if (AppVariables.scheduledTasks != null) {
                    AppVariables.scheduledTasks = null;
                }
                if (AppVariables.executorService != null) {
                    AppVariables.executorService.shutdownNow();
                    AppVariables.executorService = null;
                }
            }

            if (AppVariables.exitTimer != null) {
                AppVariables.exitTimer.cancel();
                AppVariables.exitTimer = null;
            }

            if (AppVariables.scheduledTasks == null || AppVariables.scheduledTasks.isEmpty()) {
                MyBoxLog.info("Exit now. Bye!");
                if (DerbyBase.status == DerbyStatus.Embedded) {
                    MyBoxLog.info("Shut down Derby...");
                    DerbyBase.shutdownEmbeddedDerby();
                }
                AppVariables.handlingExit = false;

                Platform.setImplicitExit(true);
                System.gc();
                Platform.exit(); // Some thread may still be alive after this
                System.exit(0);  // Go
                Runtime.getRuntime().halt(0);
                return;
            }

        } catch (Exception e) {

        }
        AppVariables.handlingExit = false;

    }

    public static void clearInvalidData() {
        try {
            MyBoxLog.info("clearing tmeporary data...");

            FileDeleteTools.clearDir(AppVariables.MyBoxTempPath);

            try ( Connection conn = DerbyBase.getConnection()) {

                new TableImageClipboard().clearInvalid(conn);

                new TableImageEditHistory().clearInvalid(conn);

                new TableFileBackup().clearInvalid(conn);

                new TableData2DDefinition().clearInvalid(conn, true);

            } catch (Exception e) {
                MyBoxLog.error(e);
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
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

}
