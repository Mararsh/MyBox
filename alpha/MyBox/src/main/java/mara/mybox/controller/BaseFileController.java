package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.menu.MenuTools;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.FileSortTools;
import mara.mybox.tools.FileTools;
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
    public void initValues() {
        try {
            super.initValues();

            sortMode = FileSortTools.sortMode(UserConfig.getString(baseName + "SortMode", "NameAsc"));

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

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
        if (MenuTools.isPopMenu(baseName + "File")) {
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

            items.add(MenuTools.popCheckMenu(baseName + "File"));

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
            List<MenuItem> items = MenuTools.initMenu(message("File"));
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
        if (MenuTools.isPopMenu(baseName + "Data")) {
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

            items.add(MenuTools.popCheckMenu(baseName + "Data"));

            popEventMenu(fevent, items);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public List<MenuItem> dataMenuItems(Event fevent) {
        return null;
    }

    public File nextFile() {
        return FileSortTools.nextFile(sourceFile, SourceFileType, sortMode);
    }

    public File previousFile() {
        return FileSortTools.previousFile(sourceFile, SourceFileType, sortMode);
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
