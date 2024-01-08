package mara.mybox.controller;

import java.io.File;
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
import javafx.scene.input.KeyEvent;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-7-31
 * @License Apache License Version 2.0
 */
public class HtmlEditorController extends WebAddressController {

    @FXML
    protected ControlHtmlEditor editController;

    public HtmlEditorController() {
        baseTitle = message("HtmlEditor");
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            webViewController = editController.webViewController;
            webView = webViewController.webView;
            webEngine = webViewController.webEngine;

            editController.parentController = this;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            editController.loadNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldv, Boolean newv) {
                    panesLoad();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void goAction() {
        editController.loadAddress(addressInput.getText());
    }

    @Override
    public boolean loadFile(File file) {
        return editController.loadFile(file);
    }

    @Override
    public boolean loadAddress(String address) {
        return editController.loadAddress(address);
    }

    @Override
    public boolean loadContents(String contents) {
        return editController.loadContents(contents);
    }

    @Override
    public boolean loadContents(String address, String contents) {
        return editController.loadContents(address, contents);
    }

    public void panesLoad() {
        sourceFile = editController.sourceFile;
    }

    @FXML
    @Override
    public void createAction() {
        if (editController.create()) {
            addressInput.setText("");
        }
    }

    @FXML
    @Override
    public void refreshAction() {
        editController.refreshAction();
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

            menu = new MenuItem(message("Create") + "    Ctrl+N " + message("Or") + " Alt+N",
                    StyleTools.getIconImageView("iconAdd.png"));
            menu.setOnAction((ActionEvent event) -> {
                createAction();
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("Save") + "    Ctrl+S " + message("Or") + " Alt+S",
                    StyleTools.getIconImageView("iconSave.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                editController.saveAction();
            });
            menu.setDisable(editController.saveButton.isDisabled());
            items.add(menu);

            if (sourceFile != null) {
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

            menu = new MenuItem(message("Refresh"), StyleTools.getIconImageView("iconRefresh.png"));
            menu.setOnAction((ActionEvent event) -> {
                refreshAction();
            });
            items.add(menu);

            menu = new MenuItem(message("SaveAs") + "    Ctrl+B " + message("Or") + " Alt+B",
                    StyleTools.getIconImageView("iconSaveAs.png"));
            menu.setOnAction((ActionEvent event) -> {
                editController.saveAsAction();
            });
            items.add(menu);

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

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (editController.keyEventsFilter(event)) {
            return true;
        }
        return super.keyEventsFilter(event);
    }

    /*
        static
     */
    public static HtmlEditorController open() {
        try {
            HtmlEditorController controller = (HtmlEditorController) WindowTools.openStage(Fxmls.HtmlEditorFxml);
            if (controller != null) {
                controller.requestMouse();
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static HtmlEditorController openFile(File file) {
        try {
            HtmlEditorController controller = open();
            if (controller != null && file != null) {
                controller.sourceFileChanged(file);
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static HtmlEditorController openAddress(String address) {
        try {
            HtmlEditorController controller = open();
            if (controller != null && address != null) {
                controller.loadAddress(address);
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static HtmlEditorController openHtml(String html) {
        try {
            HtmlEditorController controller = open();
            if (controller != null && html != null) {
                controller.loadContents(html);
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
