package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.FileSortTools;
import mara.mybox.tools.FileSortTools.FileSortMode;
import mara.mybox.value.FileFilters;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-6-13
 * @License Apache License Version 2.0
 */
public abstract class BaseFileController extends BaseController {

    protected FileSortTools.FileSortMode sortMode;

    @FXML
    protected ControlFileBrowse browseController;
    @FXML
    protected Label fileInfoLabel;
    @FXML
    protected Button fileButton, operationsButton;

    @Override
    public void initControls() {
        try {
            super.initControls();
            if (browseController != null) {
                browseController.setParameter(this);
            }

            sortMode = FileSortMode.NameAsc;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void popFileMenu(Event event) {
        if (UserConfig.getBoolean(baseName + "FilePopWhenMouseHovering", true)) {
            showFileMenu(event);
        }
    }

    @FXML
    public void showFileMenu(Event fevent) {
        try {
            List<MenuItem> items = fileMenuItems(fevent);
            if (items == null || items.isEmpty()) {
                return;
            }

            items.add(new SeparatorMenuItem());

            CheckMenuItem popItem = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
            popItem.setSelected(UserConfig.getBoolean(baseName + "FilePopWhenMouseHovering", true));
            popItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(baseName + "FilePopWhenMouseHovering", popItem.isSelected());
                }
            });
            items.add(popItem);

            popEventMenu(fevent, items);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public List<MenuItem> fileMenuItems(Event fevent) {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            menu = new MenuItem(message("Save"), StyleTools.getIconImageView("iconSave.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                saveAction();
            });
            items.add(menu);

            if (sourceFile != null) {
                menu = new MenuItem(message("Recover"), StyleTools.getIconImageView("iconRecover.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    recoverAction();
                });
                items.add(menu);

                CheckMenuItem backItem = new CheckMenuItem(message("BackupWhenSave"));
                backItem.setSelected(UserConfig.getBoolean(baseName + "BackupWhenSave", false));
                backItem.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        UserConfig.setBoolean(baseName + "BackupWhenSave", backItem.isSelected());
                    }
                });
                items.add(backItem);

                menu = new MenuItem(message("FileBackups"), StyleTools.getIconImageView("iconBackup.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    FileBackupController.load(this);
                });
                items.add(menu);

            }

            if (sourceFile == null) {
                return items;
            }
            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("OpenDirectory"), StyleTools.getIconImageView("iconOpenPath.png"));
            menu.setOnAction((ActionEvent event) -> {
                openSourcePath();
            });
            items.add(menu);

            menu = new MenuItem(message("BrowseFiles"), StyleTools.getIconImageView("iconList.png"));
            menu.setOnAction((ActionEvent event) -> {
                FileBrowseController.open(this);
            });
            items.add(menu);

            menu = new MenuItem(message("SystemMethod"), StyleTools.getIconImageView("iconSystemOpen.png"));
            menu.setOnAction((ActionEvent event) -> {
                systemMethod();
            });
            items.add(menu);

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @FXML
    public void popOperationsMenu(Event event) {
        if (UserConfig.getBoolean(baseName + "OperationsPopWhenMouseHovering", true)) {
            showOperationsMenu(event);
        }
    }

    @FXML
    public void showOperationsMenu(Event fevent) {
        try {
            List<MenuItem> items = operationsMenuItems(fevent);
            if (items == null || items.isEmpty()) {
                return;
            }

            items.add(new SeparatorMenuItem());

            CheckMenuItem popItem = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
            popItem.setSelected(UserConfig.getBoolean(baseName + "OperationsPopWhenMouseHovering", true));
            popItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(baseName + "OperationsPopWhenMouseHovering", popItem.isSelected());
                }
            });
            items.add(popItem);

            popEventMenu(fevent, items);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public List<MenuItem> operationsMenuItems(Event fevent) {
        return null;
    }

    @FXML
    public void popViewMenu(Event event) {
        if (UserConfig.getBoolean(baseName + "ViewPopWhenMouseHovering", true)) {
            showViewMenu(event);
        }
    }

    @FXML
    public void showViewMenu(Event fevent) {
        try {
            List<MenuItem> items = viewMenuItems(fevent);
            if (items == null || items.isEmpty()) {
                return;
            }

            items.add(new SeparatorMenuItem());

            CheckMenuItem popItem = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
            popItem.setSelected(UserConfig.getBoolean(baseName + "ViewPopWhenMouseHovering", true));
            popItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(baseName + "ViewPopWhenMouseHovering", popItem.isSelected());
                }
            });
            items.add(popItem);

            popEventMenu(fevent, items);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public List<MenuItem> viewMenuItems(Event fevent) {
        return null;
    }

    @FXML
    public void popFunctionsMenu(Event event) {
        if (UserConfig.getBoolean(baseName + "FunctionsPopWhenMouseHovering", true)) {
            showFunctionsMenu(event);
        }
    }

    @FXML
    public void showFunctionsMenu(Event fevent) {
        try {
            List<MenuItem> items = functionsMenuItems(fevent);
            if (items == null || items.isEmpty()) {
                return;
            }
            items.add(new SeparatorMenuItem());

            CheckMenuItem popItem = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
            popItem.setSelected(UserConfig.getBoolean(baseName + "FunctionsPopWhenMouseHovering", true));
            popItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(baseName + "FunctionsPopWhenMouseHovering", popItem.isSelected());
                }
            });
            items.add(popItem);

            popEventMenu(fevent, items);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public List<MenuItem> functionsMenuItems(Event fevent) {
        return null;
    }

    public List<File> pathTypeFiles() {
        try {
            if (sourceFile == null) {
                return null;
            }
            File path = sourceFile.getParentFile();
            File[] filesList = path.listFiles();
            if (filesList == null || filesList.length == 0) {
                return null;
            }
            List<File> files = new ArrayList<>();
            for (File file : filesList) {
                if (file.isFile() && FileFilters.accept(sourceExtensionFilter, file)) {
                    files.add(file);
                }
            }
            FileSortTools.sortFiles(files, sortMode);
            return files;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public File nextFile() {
        try {
            if (sourceFile == null) {
                return null;
            }
            List<File> files = pathTypeFiles();
            if (files == null || files.isEmpty()) {
                return null;
            }
            for (int i = 0; i < files.size() - 1; i++) {
                File file = files.get(i);
                if (sourceFile.equals(file)) {
                    return files.get(i + 1);
                }
            }
            return null;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public File previousFile() {
        try {
            if (sourceFile == null) {
                return null;
            }
            List<File> files = pathTypeFiles();
            if (files == null || files.isEmpty()) {
                return null;
            }
            for (int i = 1; i < files.size(); i++) {
                File file = files.get(i);
                if (sourceFile.equals(file)) {
                    return files.get(i - 1);
                }
            }
            return null;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

}
