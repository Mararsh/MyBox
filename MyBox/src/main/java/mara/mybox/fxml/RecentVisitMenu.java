/*
 * Apache License Version 2.0
 */
package mara.mybox.fxml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import mara.mybox.controller.base.BaseController;
import mara.mybox.data.VisitHistory;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.message;

/**
 *
 * @author mara
 */
public abstract class RecentVisitMenu {

    protected final BaseController controller;
    protected final MouseEvent event;

    public RecentVisitMenu(BaseController controller, MouseEvent event) {
        this.controller = controller;
        this.event = event;
    }

    public void pop() {
        if (controller == null || event == null) {
            return;
        }
        ContextMenu popMenu = controller.getPopMenu();
        if (popMenu != null && popMenu.isShowing()) {
            popMenu.hide();
        }
        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);

        MenuItem menu = new MenuItem(message("Select..."));
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                handleSelect();
            }
        });
        popMenu.getItems().add(menu);

        List<VisitHistory> his = recentFiles();
        if (his != null && !his.isEmpty()) {
            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("RecentAccessedFiles"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            popMenu.getItems().add(menu);
            for (VisitHistory h : his) {
                final String fname = h.getResourceValue();
                menu = new MenuItem(fname);
                menu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        handleFile(fname);
                    }
                });
                popMenu.getItems().add(menu);
            }
        }

        List<String> paths = paths();
        if (!paths.isEmpty()) {
            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("RecentAccessedDirectories"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            popMenu.getItems().add(menu);
            for (String path : paths) {
                menu = new MenuItem(path);
                menu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        handlePath(path);
                    }
                });
                popMenu.getItems().add(menu);
            }
        }

        if (popMenu.getItems().isEmpty()) {
            return;
        }

        controller.setPopMenu(popMenu);
        popMenu.getItems().add(new SeparatorMenuItem());
        menu = new MenuItem(message("MenuClose"));
        menu.setStyle("-fx-text-fill: #2e598a;");
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                controller.getPopMenu().hide();
                controller.setPopMenu(null);
            }
        });
        popMenu.getItems().add(menu);

        FxmlControl.locateBelow((Region) event.getSource(), popMenu);

    }

    public abstract List<VisitHistory> recentFiles();

    public abstract List<VisitHistory> recentPaths();

    public abstract void handleSelect();

    public abstract void handleFile(String fname);

    public abstract void handlePath(String fname);

    public List<String> paths() {
        List<VisitHistory> his = recentPaths();
        List<String> paths = new ArrayList();
        if (his != null) {
            for (VisitHistory h : his) {
                String pathname = h.getResourceValue();
                paths.add(pathname);
            }
        }
        if (controller.getDefaultPathKey() != null
                && !paths.contains(controller.getDefaultPathKey())) {
            paths.add(controller.getDefaultPathKey());
        }
        File lastPath = AppVaribles.getUserConfigPath(controller.getLastPathKey());
        if (lastPath != null) {
            String lastPathString = lastPath.getAbsolutePath();
            if (!paths.contains(lastPathString)) {
                paths.add(lastPathString);
            }
        }
        return paths;
    }

    public List<VisitHistory> recentSourceFiles() {
        int fileNumber = AppVaribles.fileRecentNumber * 2 / 3 + 1;
        return VisitHistory.getRecentFile(controller.getSourceFileType(), fileNumber);
    }

    public List<VisitHistory> recentAddFiles() {
        int fileNumber = AppVaribles.fileRecentNumber * 2 / 3 + 1;
        if (controller.getAddFileType() <= 0) {
            controller.setAddFileType(controller.getSourceFileType());
        }
        return VisitHistory.getRecentFile(controller.getAddFileType(), fileNumber);
    }

    public List<VisitHistory> recentSourcePathsBesidesFiles() {
        int pathNumber = AppVaribles.fileRecentNumber / 3 + 1;
        return VisitHistory.getRecentPath(controller.getSourcePathType(), pathNumber);
    }

    public List<VisitHistory> recentSourcePaths() {
        return VisitHistory.getRecentPath(controller.getSourcePathType());
    }

    public List<VisitHistory> recentTargetPaths() {
        return VisitHistory.getRecentPath(controller.getTargetPathType());
    }

    public void handleSourcePath(String fname) {
        File file = new File(fname);
        if (!file.exists()) {
            handleSelect();
            return;
        }
        AppVaribles.setUserConfigValue(controller.getSourcePathKey(), fname);
        handleSelect();
    }

    public void handleTargetPath(String fname) {
        File file = new File(fname);
        if (!file.exists()) {
            handleSelect();
            return;
        }
        AppVaribles.setUserConfigValue(controller.getTargetPathKey(), fname);
        handleSelect();
    }

}
