package mara.mybox.fxml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.stage.FileChooser;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.BaseController_Files;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @License Apache License Version 2.0
 */
public abstract class RecentVisitMenu {

    protected BaseController controller;
    protected Event event;
    protected List<String> examples;
    protected int SourceFileType, SourcePathType, AddFileType, AddPathType, TargetFileType, TargetPathType;
    protected List<FileChooser.ExtensionFilter> sourceExtensionFilter;
    protected String baseName, defaultPath;
    protected boolean onlyPath;

    public RecentVisitMenu() {
    }

    public RecentVisitMenu(BaseController_Files controller, Event event, boolean onlyPath) {
        this.controller = (BaseController) controller;
        this.event = event;
        this.baseName = controller.getBaseName();
        this.onlyPath = onlyPath;
        SourceFileType = controller.getSourceFileType();
        SourcePathType = controller.getSourcePathType();
        AddFileType = controller.getAddFileType();
        AddPathType = controller.getAddPathType();

        TargetFileType = controller.getTargetFileType();
        TargetPathType = controller.getTargetPathType();

        if (AddFileType <= 0) {
            AddFileType = SourceFileType;
        }
    }

    public RecentVisitMenu setFileType(int fileType) {
        SourceFileType = fileType;
        SourcePathType = fileType;
        TargetFileType = fileType;
        TargetPathType = fileType;
        AddFileType = fileType;
        AddPathType = fileType;
        return this;
    }

    public void pop() {
        try {
            if (controller == null || event == null) {
                return;
            }
            List<MenuItem> items = menu();

            controller.popEventMenu(event, items);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public List<MenuItem> menu() {
        try {
            List<MenuItem> items = new ArrayList<>();

            MenuItem menu = new MenuItem(message("Select..."));
            menu.setOnAction((ActionEvent event1) -> {
                handleSelect();
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            if (onlyPath) {
                List<String> paths = paths();
                if (paths != null && !paths.isEmpty()) {
                    menu = new MenuItem(message("RecentAccessedDirectories"));
                    menu.setStyle("-fx-text-fill: #2e598a;");
                    items.add(menu);
                    for (String path : paths) {
                        menu = new MenuItem(StringTools.menuSuffix(path));
                        menu.setOnAction((ActionEvent event1) -> {
                            handlePath(path);
                        });
                        items.add(menu);
                    }
                    items.add(new SeparatorMenuItem());
                }
            } else {

                List<VisitHistory> written = recentWrittenFiles();
                if (written != null && !written.isEmpty()) {
                    List<String> files = new ArrayList<>();
                    for (VisitHistory h : written) {
                        String fname = h.getResourceValue();
                        if (!files.contains(fname)) {
                            files.add(fname);
                        }
                    }
                    if (!files.isEmpty()) {
                        Menu wmenu = new Menu(message("RecentWrittenFiles"));
                        items.add(wmenu);
                        for (String fname : files) {
                            menu = new MenuItem(StringTools.menuSuffix(fname));
                            menu.setOnAction((ActionEvent event1) -> {
                                handleFile(fname);
                            });
                            wmenu.getItems().add(menu);
                        }
                    }
                }

                List<String> paths = paths();
                if (paths != null && !paths.isEmpty()) {
                    Menu pmenu = new Menu(message("RecentAccessedDirectories"));
                    items.add(pmenu);
                    for (String path : paths) {
                        menu = new MenuItem(StringTools.menuSuffix(path));
                        menu.setOnAction((ActionEvent event1) -> {
                            handlePath(path);
                        });
                        pmenu.getItems().add(menu);
                    }
                }

                List<VisitHistory> opened = recentOpenedFiles();
                if (opened != null && !opened.isEmpty()) {
                    List<String> files = new ArrayList<>();
                    for (VisitHistory h : opened) {
                        String fname = h.getResourceValue();
                        if (!files.contains(fname)) {
                            files.add(fname);
                        }
                    }
                    if (!files.isEmpty()) {
                        items.add(new SeparatorMenuItem());
                        menu = new MenuItem(message("RecentOpenedFiles"));
                        menu.setStyle("-fx-text-fill: #2e598a;");
                        items.add(menu);
                        for (String fname : files) {
                            menu = new MenuItem(StringTools.menuSuffix(fname));
                            menu.setOnAction((ActionEvent event1) -> {
                                handleFile(fname);
                            });
                            items.add(menu);
                        }
                    }

                }

                if (examples != null && !examples.isEmpty()) {
                    items.add(new SeparatorMenuItem());
                    menu = new MenuItem(message("Examples"));
                    menu.setStyle("-fx-text-fill: #2e598a;");
                    items.add(menu);
                    for (String example : examples) {
                        menu = new MenuItem(StringTools.menuSuffix(example));
                        menu.setOnAction((ActionEvent event1) -> {
                            handleFile(example);
                        });
                        items.add(menu);
                    }
                }

                items.add(new SeparatorMenuItem());
            }

            CheckMenuItem hoverMenu = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
            hoverMenu.setSelected(UserConfig.getBoolean("RecentVisitMenuPopWhenMouseHovering", true));
            hoverMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean("RecentVisitMenuPopWhenMouseHovering", hoverMenu.isSelected());
                }
            });
            items.add(hoverMenu);

            return items;

        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public abstract void handleSelect();

    public List<VisitHistory> recentFiles() {
        return recentOpenedFiles();
    }

    public List<VisitHistory> recentPaths() {
        return recentSourcePathsBesidesFiles();
    }

    public void handleFile(String fname) {
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
                if (!paths.contains(pathname)) {
                    paths.add(pathname);
                }
            }
        }
        if (defaultPath != null && !paths.contains(defaultPath)) {
            paths.add(defaultPath);
        }
        File lastPath = UserConfig.getPath("LastPath");
        if (lastPath != null) {
            String lastPathString = lastPath.getAbsolutePath();
            if (!paths.contains(lastPathString)) {
                paths.add(lastPathString);
            }
        }
        return paths;
    }

    public List<VisitHistory> recentOpenedFiles() {
        int fileNumber = AppVariables.fileRecentNumber;
        if (fileNumber == 0) {
            fileNumber = 1;
        }
        return VisitHistoryTools.getRecentFileRead(SourceFileType, fileNumber);
    }

    public List<VisitHistory> recentWrittenFiles() {
        int fileNumber = AppVariables.fileRecentNumber;
        if (fileNumber == 0) {
            fileNumber = 1;
        }
        return VisitHistoryTools.getRecentFileWrite(SourceFileType, fileNumber);
    }

    public List<VisitHistory> recentAddFiles() {
        int fileNumber = AppVariables.fileRecentNumber;
        if (fileNumber == 0) {
            fileNumber = 1;
        }
        return VisitHistoryTools.getRecentFileRead(AddFileType, fileNumber);
    }

    public List<VisitHistory> recentSourcePathsBesidesFiles() {
        int pathNumber = AppVariables.fileRecentNumber;
        if (pathNumber == 0) {
            pathNumber = 1;
        }
        return VisitHistoryTools.getRecentPathRead(SourcePathType, pathNumber);
    }

    public List<VisitHistory> recentTargetPathsBesidesFiles() {
        int pathNumber = AppVariables.fileRecentNumber;
        if (pathNumber == 0) {
            pathNumber = 1;
        }
        return VisitHistoryTools.getRecentPathWrite(TargetPathType, pathNumber);
    }

    public List<VisitHistory> recentSourcePaths() {
        return VisitHistoryTools.getRecentPathRead(SourcePathType);
    }

    public List<VisitHistory> recentTargetPaths() {
        return VisitHistoryTools.getRecentPathWrite(TargetPathType);
    }

    public void handleSourcePath(String fname) {
        File file = new File(fname);
        if (!file.exists()) {
            handleSelect();
            return;
        }
        UserConfig.setString(baseName + "SourcePath", fname);
        handleSelect();
    }

    public void handleTargetPath(String fname) {
        File file = new File(fname);
        if (!file.exists()) {
            handleSelect();
            return;
        }
        UserConfig.setString(baseName + "TargetPath", fname);
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

    public String getDefaultPath() {
        return defaultPath;
    }

    public RecentVisitMenu setDefaultPath(String defaultPath) {
        this.defaultPath = defaultPath;
        return this;
    }

}
