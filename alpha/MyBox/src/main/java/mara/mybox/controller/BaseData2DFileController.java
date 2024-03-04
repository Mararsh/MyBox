package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import mara.mybox.data2d.Data2D;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.StyleTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-12-25
 * @License Apache License Version 2.0
 */
public abstract class BaseData2DFileController extends Data2DManufactureController {

    @FXML
    protected TitledPane infoPane, backupPane, formatPane;
    @FXML
    protected VBox formatBox;

    public BaseData2DFileController() {
    }

    /*
        abstract
     */
    public abstract Data2D saveAsTarget();

    /*
        init
     */
    @Override
    public void initControls() {
        try {
            super.initControls();

            loadedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> o, Boolean ov, Boolean nv) {
                    checkStatus();
                }
            });

            checkStatus();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void checkStatus() {
        if (data2D != null) {
            sourceFile = data2D.getFile();
        } else {
            sourceFile = null;
        }
    }

    @Override
    public List<MenuItem> fileMenuItems(Event fevent) {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            if (sourceFile != null) {
                menu = new MenuItem(message("Information") + "    Ctrl+I " + message("Or") + " Alt+I",
                        StyleTools.getIconImageView("iconInfo.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    infoAction();
                });
                items.add(menu);
            }

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("Save") + "    Ctrl+S " + message("Or") + " Alt+S",
                    StyleTools.getIconImageView("iconSave.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                saveAction();
            });
            menu.setDisable(saveButton.isDisabled());
            items.add(menu);

            menu = new MenuItem(message("Recover") + "    Ctrl+R " + message("Or") + " Alt+R",
                    StyleTools.getIconImageView("iconRecover.png"));
            menu.setOnAction((ActionEvent event) -> {
                recoverAction();
            });
            menu.setDisable(recoverButton.isDisabled());
            items.add(menu);

            if (data2D.isDataFile() && !data2D.isTmpData()) {
                CheckMenuItem backItem = new CheckMenuItem(message("BackupWhenSave"));
                backItem.setSelected(UserConfig.getBoolean(baseName + "BackupWhenSave", true));
                backItem.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        UserConfig.setBoolean(baseName + "BackupWhenSave", backItem.isSelected());
                    }
                });
                items.add(backItem);

                menu = new MenuItem(message("FileBackups"), StyleTools.getIconImageView("iconBackup.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    openBackups();
                });
                items.add(menu);
            }

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("Create"), StyleTools.getIconImageView("iconAdd.png"));
            menu.setOnAction((ActionEvent event) -> {
                createAction();
            });
            items.add(menu);

            menu = new MenuItem(message("LoadContentInSystemClipboard"), StyleTools.getIconImageView("iconImageSystem.png"));
            menu.setOnAction((ActionEvent event) -> {
                loadContentInSystemClipboard();
            });
            items.add(menu);

            menu = new MenuItem(message("SaveAs") + "    Ctrl+B " + message("Or") + " Alt+B",
                    StyleTools.getIconImageView("iconSaveAs.png"));
            menu.setOnAction((ActionEvent event) -> {
                saveAsAction();
            });
            items.add(menu);

            if (data2D.isTexts() || data2D.isCSV()) {
                menu = new MenuItem(message("Texts"), StyleTools.getIconImageView("iconTxt.png"));
                menu.setOnAction((ActionEvent event) -> {
                    editTextFile();
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
    @Override
    public void saveAsAction() {
        Data2D targetData = saveAsTarget();
        if (targetData == null) {
            return;
        }
//        viewController.saveAsAction(targetData, saveAsType); // ############
    }

}
