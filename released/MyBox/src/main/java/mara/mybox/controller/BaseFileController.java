package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.FileSortTools;
import mara.mybox.tools.FileSortTools.FileSortMode;
import mara.mybox.tools.FileTools;
import mara.mybox.value.FileFilters;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-6-13
 * @License Apache License Version 2.0
 */
public abstract class BaseFileController extends BaseTaskController {

    protected FileSortTools.FileSortMode sortMode;
    protected int dpi;

    @FXML
    protected ComboBox<String> dpiSelector;

    @Override
    public void initControls() {
        try {
            super.initControls();

            sortMode = FileSortMode.NameAsc;

            initDPI();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initDPI() {
        try {
            dpi = UserConfig.getInt(baseName + "DPI", 96);
            if (dpi < 0) {
                dpi = 96;
            }
            if (dpiSelector == null) {
                return;
            }
            List<String> dpiValues = new ArrayList();
            dpiValues.addAll(Arrays.asList("96", "72", "300", "160", "240", "120", "600", "400"));
            String sValue = (int) NodeTools.screenResolution() + "";
            if (dpiValues.contains(sValue)) {
                dpiValues.remove(sValue);
            }
            dpiValues.add(0, sValue);
            sValue = (int) NodeTools.screenDpi() + "";
            if (dpiValues.contains(sValue)) {
                dpiValues.remove(sValue);
            }
            dpiValues.add(sValue);
            dpiSelector.getItems().addAll(dpiValues);
            dpiSelector.setValue(dpi + "");
            dpiSelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        if (isSettingValues) {
                            return;
                        }
                        int v = Integer.parseInt(dpiSelector.getValue());
                        if (v > 0) {
                            dpi = v;
                            UserConfig.setInt(baseName + "DPI", dpi);
                            dpiSelector.getEditor().setStyle(null);
                            dpiChanged();
                        } else {
                            dpiSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        dpiSelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                }
            });
        } catch (Exception e) {
            dpiSelector.getEditor().setStyle(UserConfig.badStyle());
        }
    }

    public void dpiChanged() {
    }

    @FXML
    public void popFileMenu(Event event) {
        if (UserConfig.getBoolean(baseName + "FileMenuPopWhenMouseHovering", true)) {
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
            popItem.setSelected(UserConfig.getBoolean(baseName + "FileMenuPopWhenMouseHovering", true));
            popItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(baseName + "FileMenuPopWhenMouseHovering", popItem.isSelected());
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
            if (sourceFile == null) {
                return null;
            }
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            menu = new MenuItem(message("Information") + "    Ctrl+I " + message("Or") + " Alt+I",
                    StyleTools.getIconImageView("iconInfo.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                infoAction();
            });
            items.add(menu);

            menu = new MenuItem(message("Refresh"), StyleTools.getIconImageView("iconRefresh.png"));
            menu.setOnAction((ActionEvent event) -> {
                refreshAction();
            });
            items.add(menu);

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
    public void popDataMenu(Event event) {
        if (UserConfig.getBoolean(baseName + "DataMenuPopWhenMouseHovering", true)) {
            showDataMenu(event);
        }
    }

    @FXML
    public void showDataMenu(Event fevent) {
        try {
            List<MenuItem> items = dataMenuItems(fevent);
            if (items == null || items.isEmpty()) {
                return;
            }

            items.add(new SeparatorMenuItem());

            CheckMenuItem popItem = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
            popItem.setSelected(UserConfig.getBoolean(baseName + "DataMenuPopWhenMouseHovering", true));
            popItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(baseName + "DataMenuPopWhenMouseHovering", popItem.isSelected());
                }
            });
            items.add(popItem);

            popEventMenu(fevent, items);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public List<MenuItem> dataMenuItems(Event fevent) {
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

    @Override
    public boolean controlAltI() {
        return infoAction();
    }

    @FXML
    @Override
    public boolean infoAction() {
        String info = fileInfo();
        if (info != null && !info.isBlank()) {
            TextPopController.loadText(info);
            return true;
        }
        return false;
    }

    public String fileInfo() {
        if (sourceFile != null) {
            return FileTools.fileInformation(sourceFile);
        } else {
            return null;
        }
    }

}
