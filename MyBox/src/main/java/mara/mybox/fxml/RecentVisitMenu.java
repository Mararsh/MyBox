/*
 * Apache License Version 2.0
 */
package mara.mybox.fxml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import mara.mybox.controller.BaseController;
import mara.mybox.data.VisitHistory;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;

/**
 *
 * @author mara
 */
public abstract class RecentVisitMenu {

    protected final BaseController controller;
    protected final MouseEvent event;
    protected List<String> examples;
    protected int SourceFileType, SourcePathType, AddFileType, AddPathType, TargetPathType;
    protected List<FileChooser.ExtensionFilter> sourceExtensionFilter;
    protected String defaultPathKey, LastPathKey, sourcePathKey, targetPathKey;

    public RecentVisitMenu(BaseController controller, MouseEvent event) {
        this.controller = controller;
        this.event = event;
        this.LastPathKey = controller.getLastPathKey();
        this.defaultPathKey = controller.getDefaultPathKey();
        this.sourcePathKey = controller.getSourcePathKey();
        this.targetPathKey = controller.getTargetPathKey();
        this.SourceFileType = controller.getSourceFileType();
        this.SourcePathType = controller.getSourcePathType();
        this.AddFileType = controller.getAddFileType();
        if (this.AddFileType <= 0) {
            this.AddFileType = this.SourceFileType;
        }
        this.TargetPathType = controller.getTargetPathType();
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
        menu.setOnAction((ActionEvent event1) -> {
            handleSelect();
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
                menu.setOnAction((ActionEvent event1) -> {
                    handleFile(fname);
                });
                popMenu.getItems().add(menu);
            }
        }

        if (examples != null && !examples.isEmpty()) {
            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("Examples"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            popMenu.getItems().add(menu);
            for (String example : examples) {
                menu = new MenuItem(example);
                menu.setOnAction((ActionEvent event1) -> {
                    handleFile(example);
                });
                popMenu.getItems().add(menu);
            }
        }

        List<String> paths = paths();
        if (paths != null && !paths.isEmpty()) {
            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("RecentAccessedDirectories"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            popMenu.getItems().add(menu);
            for (String path : paths) {
                menu = new MenuItem(path);
                menu.setOnAction((ActionEvent event1) -> {
                    handlePath(path);
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
        menu.setOnAction((ActionEvent event1) -> {
            controller.getPopMenu().hide();
            controller.setPopMenu(null);
        });
        popMenu.getItems().add(menu);

        FxmlControl.locateBelow((Region) event.getSource(), popMenu);

    }

    public abstract void handleSelect();

    public abstract void handleFile(String fname);

    public List<VisitHistory> recentFiles() {
        return recentSourceFiles();
    }

    public List<VisitHistory> recentPaths() {
        return recentSourcePathsBesidesFiles();
    }

    public void handlePath(String fname) {
        handleSourcePath(fname);
    }

    public List<String> paths() {
        List<VisitHistory> his = recentPaths();
        List<String> paths = new ArrayList<>();
        if (his != null) {
            for (VisitHistory h : his) {
                String pathname = h.getResourceValue();
                paths.add(pathname);
            }
        }
        if (defaultPathKey != null && !paths.contains(defaultPathKey)) {
            paths.add(defaultPathKey);
        }
        File lastPath = AppVariables.getUserConfigPath(LastPathKey);
        if (lastPath != null) {
            String lastPathString = lastPath.getAbsolutePath();
            if (!paths.contains(lastPathString)) {
                paths.add(lastPathString);
            }
        }
        return paths;
    }

    public List<VisitHistory> recentSourceFiles() {
        int fileNumber = AppVariables.fileRecentNumber * 2 / 3 + 1;
        return VisitHistory.getRecentFile(SourceFileType, fileNumber);
    }

    public List<VisitHistory> recentAddFiles() {
        int fileNumber = AppVariables.fileRecentNumber * 2 / 3 + 1;
        return VisitHistory.getRecentFile(AddFileType, fileNumber);
    }

    public List<VisitHistory> recentSourcePathsBesidesFiles() {
        int pathNumber = AppVariables.fileRecentNumber / 3 + 1;
        return VisitHistory.getRecentPath(SourcePathType, pathNumber);
    }

    public List<VisitHistory> recentSourcePaths() {
        return VisitHistory.getRecentPath(SourcePathType);
    }

    public List<VisitHistory> recentTargetPaths() {
        return VisitHistory.getRecentPath(TargetPathType);
    }

    public void handleSourcePath(String fname) {
        File file = new File(fname);
        if (!file.exists()) {
            handleSelect();
            return;
        }
        AppVariables.setUserConfigValue(sourcePathKey, fname);
        handleSelect();
    }

    public void handleTargetPath(String fname) {
        File file = new File(fname);
        if (!file.exists()) {
            handleSelect();
            return;
        }
        AppVariables.setUserConfigValue(targetPathKey, fname);
        handleSelect();
    }

    /*
        get/set
     */
    public List<String> getExamples() {
        return examples;
    }

    public RecentVisitMenu setExamples(List<String> examples) {
        this.examples = examples;
        return this;
    }

    public int getSourceFileType() {
        return SourceFileType;
    }

    public RecentVisitMenu setSourceFileType(int SourceFileType) {
        this.SourceFileType = SourceFileType;
        return this;
    }

    public int getSourcePathType() {
        return SourcePathType;
    }

    public RecentVisitMenu setSourcePathType(int SourcePathType) {
        this.SourcePathType = SourcePathType;
        return this;
    }

    public int getAddFileType() {
        return AddFileType;
    }

    public RecentVisitMenu setAddFileType(int AddFileType) {
        this.AddFileType = AddFileType;
        return this;
    }

    public int getAddPathType() {
        return AddPathType;
    }

    public RecentVisitMenu setAddPathType(int AddPathType) {
        this.AddPathType = AddPathType;
        return this;
    }

    public int getTargetPathType() {
        return TargetPathType;
    }

    public RecentVisitMenu setTargetPathType(int TargetPathType) {
        this.TargetPathType = TargetPathType;
        return this;
    }

    public List<FileChooser.ExtensionFilter> getSourceExtensionFilter() {
        return sourceExtensionFilter;
    }

    public RecentVisitMenu setSourceExtensionFilter(List<FileChooser.ExtensionFilter> sourceExtensionFilter) {
        this.sourceExtensionFilter = sourceExtensionFilter;
        return this;
    }

    public String getDefaultPathKey() {
        return defaultPathKey;
    }

    public RecentVisitMenu setDefaultPathKey(String defaultPathKey) {
        this.defaultPathKey = defaultPathKey;
        return this;
    }

    public String getLastPathKey() {
        return LastPathKey;
    }

    public RecentVisitMenu setLastPathKey(String LastPathKey) {
        this.LastPathKey = LastPathKey;
        return this;
    }

    public String getSourcePathKey() {
        return sourcePathKey;
    }

    public RecentVisitMenu setSourcePathKey(String sourcePathKey) {
        this.sourcePathKey = sourcePathKey;
        return this;
    }

    public String getTargetPathKey() {
        return targetPathKey;
    }

    public RecentVisitMenu setTargetPathKey(String targetPathKey) {
        this.targetPathKey = targetPathKey;
        return this;
    }

}
