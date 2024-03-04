package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import mara.mybox.db.data.FileBackup;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.TextFileTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-12-25
 * @License Apache License Version 2.0
 */
public abstract class BaseDomEditorController extends BaseFileController {

    protected boolean domChanged, textsChanged, fileChanged;
    protected String title, typeName;
    protected final SimpleBooleanProperty loadNotify;

    @FXML
    protected Tab domTab, textsTab;
    @FXML
    protected TextArea textsArea;
    @FXML
    protected Label textsLabel;
    @FXML
    protected CheckBox wrapTextsCheck;
    @FXML
    protected Button examplesButton;

    public abstract String makeBlank();

    public abstract void openSavedFile(File file);

    public abstract void loadDom(String texts, boolean updated);

    public abstract String textsByDom();

    public abstract void clearDom();

    public abstract void domMenuAction();

    @FXML
    protected abstract void exampleAction();

    @FXML
    protected abstract void options();

    public BaseDomEditorController() {
        loadNotify = new SimpleBooleanProperty(false);
        typeName = "DOM";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
                @Override
                public void changed(ObservableValue ov, Tab oldValue, Tab newValue) {
                    tabChanged();
                }
            });
            initTextsTab();

            recoverButton.setDisable(true);

            tabChanged();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        source
     */
    @Override
    public void sourceFileChanged(File file) {
        if (!checkBeforeNextAction()) {
            return;
        }
        if (file == null || !file.exists()) {
            popError(message("InvalidData"));
            return;
        }
        if (file.length() > 10 * 1024 * 1024) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(message("FileSize") + ": " + FileTools.showFileSize(file.length()));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonJson = new ButtonType(message("JsonEditor"));
            ButtonType buttonSystem = new ButtonType(message("SystemMethod"));
            ButtonType buttontext = new ButtonType(message("TextEditor"));
            ButtonType buttonCancel = new ButtonType(message("Cancel"));
            alert.getButtonTypes().setAll(buttonJson, buttonSystem, buttontext, buttonCancel);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            Optional<ButtonType> result = alert.showAndWait();
            if (result == null || !result.isPresent() || result.get() == buttonCancel) {
                return;
            }
            if (result.get() == buttonSystem) {
                browse(file);
                return;

            } else if (result.get() == buttontext) {
                TextEditorController.open(file);
                return;

            }
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private String texts;

            @Override
            protected boolean handle() {
                try {
                    texts = TextFileTools.readTexts(this, file);
                    return texts != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                sourceFile = file;
                recordFileOpened(sourceFile);
                writePanes(texts);
            }

        };
        start(task);
    }

    public boolean writePanes(String texts) {
        fileChanged = false;
        isSettingValues = true;
        loadDom(texts, false);
        loadText(texts, false);
        updateTitle();
        isSettingValues = false;
        recoverButton.setDisable(true);
        loadNotify.set(!loadNotify.get());
        return true;
    }

    @FXML
    @Override
    public void createAction() {
        loadTexts(makeBlank());
    }

    @FXML
    @Override
    public void recoverAction() {
        if (sourceFile != null && sourceFile.exists()) {
            fileChanged = false;
            sourceFileChanged(sourceFile);
        }
    }

    public void loadTexts(String texts) {
        try {
            if (!checkBeforeNextAction()) {
                return;
            }
            sourceFile = null;
            writePanes(texts);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        file
     */
    @FXML
    @Override
    public void saveAction() {
        if (sourceFile == null) {
            targetFile = chooseSaveFile();
        } else {
            targetFile = sourceFile;
        }
        if (targetFile == null) {
            return;
        }
        String texts = currentTexts(true);
        if (texts == null || texts.isBlank()) {
            popError(message("NoData"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            private boolean needBackup = false;
            private FileBackup backup;

            @Override
            protected boolean handle() {
                try {
                    String encoding = currentEncoding();
                    if (encoding == null) {
                        encoding = "utf-8";
                    }
                    File tmpFile = TextFileTools.writeFile(FileTmpTools.getTempFile(),
                            texts, Charset.forName(encoding));
                    if (tmpFile == null || !tmpFile.exists()) {
                        return false;
                    }
                    needBackup = sourceFile != null && UserConfig.getBoolean(baseName + "BackupWhenSave", true);
                    if (needBackup) {
                        backup = addBackup(this, sourceFile);
                    }
                    return FileTools.override(tmpFile, targetFile);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                recordFileWritten(targetFile);
                if (needBackup) {
                    if (backup != null && backup.getBackup() != null) {
                        popInformation(message("SavedAndBacked"));
                        FileBackupController.updateList(sourceFile);
                    } else {
                        popError(message("FailBackup"));
                    }
                } else {
                    popInformation(targetFile + "   " + message("Saved"));
                }

                fileChanged = false;
                sourceFileChanged(targetFile);
            }

        };
        start(task);
    }

    public String currentEncoding() {
        return "utf-8";
    }

    public String currentTexts(boolean synchronize) {
        try {
            Tab currentTab = tabPane.getSelectionModel().getSelectedItem();

            if (currentTab == domTab) {
                String texts = textsByDom();
                if (synchronize) {
                    loadText(texts, false);
                }
                return texts;

            } else if (currentTab == textsTab) {
                String texts = textsByText();
                if (synchronize) {
                    loadDom(texts, false);
                }
                return texts;

            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return null;
    }

    public void updateTitle() {
        if (getMyStage() == null) {
            return;
        }
        title = baseTitle;
        if (sourceFile != null) {
            title += " - " + sourceFile.getAbsolutePath();
        }
        if (fileChanged) {
            title += " *";
        }
        myStage.setTitle(title);
    }

    protected void fileChanged() {
        fileChanged = true;
        updateTitle();
        recoverButton.setDisable(sourceFile == null);
    }

    @FXML
    @Override
    public void saveAsAction() {
        File file = chooseSaveFile();
        if (file == null) {
            return;
        }
        String texts = currentTexts(false);
        if (texts == null || texts.isBlank()) {
            popError(message("NoData"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            @Override
            protected boolean handle() {
                String encoding = currentEncoding();
                if (encoding == null) {
                    encoding = "utf-8";
                }
                File tmpFile = TextFileTools.writeFile(FileTmpTools.getTempFile(),
                        texts, Charset.forName(encoding));
                if (tmpFile == null || !tmpFile.exists()) {
                    return false;
                }
                return FileTools.override(tmpFile, file);
            }

            @Override
            protected void whenSucceeded() {
                popSaved();
                recordFileWritten(file);
                openSavedFile(file);
            }
        };
        start(task);
    }

    /*
        dom
     */
    public void domChanged(boolean changed) {
        domChanged = changed;
        domTab.setText(message("Tree") + (changed ? " *" : ""));
        if (changed) {
            fileChanged();
        }
    }

    @FXML
    @Override
    public void refreshAction() {
        fileChanged = false;
    }

    /*
        texts
     */
    protected void initTextsTab() {
        try {
            textsArea.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    textsChanged(true);
                }
            });

            wrapTextsCheck.setSelected(UserConfig.getBoolean(baseName + "WrapText", true));
            textsArea.setWrapText(wrapTextsCheck.isSelected());
            wrapTextsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "WrapText", wrapTextsCheck.isSelected());
                    textsArea.setWrapText(wrapTextsCheck.isSelected());
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadText(String texts, boolean updated) {
        if (!tabPane.getTabs().contains(textsTab)) {
            return;
        }
        try {
            textsArea.setText(texts);
            textsChanged(updated);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public String textsByText() {
        return textsArea.getText();
    }

    protected void textsChanged(boolean changed) {
        textsChanged = changed;
        textsTab.setText(typeName + (changed ? " *" : ""));
        textsLabel.setText(message("CharactersNumber") + ": " + StringTools.format(textsArea.getLength()));
        if (changed) {
            fileChanged();
        }
    }

    @FXML
    protected void clearTexts() {
        textsArea.clear();
        textsChanged(true);
    }


    /*
        buttons
     */
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

            menu = new MenuItem(message("Save") + "    Ctrl+S " + message("Or") + " Alt+S",
                    StyleTools.getIconImageView("iconSave.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                saveAction();
            });
            items.add(menu);

            if (sourceFile != null) {
                menu = new MenuItem(message("Recover") + "    Ctrl+R " + message("Or") + " Alt+R",
                        StyleTools.getIconImageView("iconRecover.png"));
                menu.setOnAction((ActionEvent event) -> {
                    recoverAction();
                });
                menu.setDisable(recoverButton.isDisable());
                items.add(menu);

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

            menu = new MenuItem(message("Create") + "    Ctrl+N " + message("Or") + " Alt+N",
                    StyleTools.getIconImageView("iconAdd.png"));
            menu.setOnAction((ActionEvent event) -> {
                createAction();
            });
            items.add(menu);

            if (examplesButton == null) {
                menu = new MenuItem(message("Example"), StyleTools.getIconImageView("iconExamples.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    exampleAction();
                });
                items.add(menu);
            }

            menu = new MenuItem(message("SaveAs") + "    Ctrl+B " + message("Or") + " Alt+B",
                    StyleTools.getIconImageView("iconSaveAs.png"));
            menu.setOnAction((ActionEvent event) -> {
                saveAsAction();
            });
            items.add(menu);

            menu = new MenuItem(message("Texts"), StyleTools.getIconImageView("iconTxt.png"));
            menu.setOnAction((ActionEvent event) -> {
                editTexts();
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
    public List<MenuItem> operationsMenuItems(Event fevent) {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            menu = new MenuItem(message("Synchronize") + "    F10 ", StyleTools.getIconImageView("iconSynchronize.png"));
            menu.setOnAction((ActionEvent event) -> {
                synchronizeAction();
            });
            items.add(menu);

            menu = new MenuItem(message("Clear") + "    Ctrl+L " + message("Or") + " Alt+L",
                    StyleTools.getIconImageView("iconClear.png"));
            menu.setOnAction((ActionEvent event) -> {
                clearAction();
            });
            items.add(menu);

            menu = new MenuItem(message("Options"), StyleTools.getIconImageView("iconOptions.png"));
            menu.setOnAction((ActionEvent event) -> {
                options();
            });
            items.add(menu);

            menu = new MenuItem(message("ContextMenu") + "    F6", StyleTools.getIconImageView("iconMenu.png"));
            menu.setOnAction((ActionEvent event) -> {
                menuAction();
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
    public boolean popAction() {
        try {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == textsTab) {
                TextPopController.openInput(this, textsArea);
                return true;

            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return false;
    }

    @FXML
    @Override
    public boolean synchronizeAction() {
        try {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();

            if (tab == domTab) {
                synchronizeDom();
                return true;

            } else if (tab == textsTab) {
                synchronizeTexts();
                return true;

            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return false;
    }

    public void synchronizeDom() {
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            String texts;

            @Override
            protected boolean handle() {
                try {
                    texts = textsByDom();
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                loadText(texts, true);
            }

        };
        start(task);
    }

    public void synchronizeTexts() {
        Platform.runLater(() -> {
            loadDom(textsByText(), true);
        });
    }

    @FXML
    @Override
    public boolean menuAction() {
        try {
            closePopup();

            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == domTab) {
                domMenuAction();
                return true;

            } else if (tab == textsTab) {
                MenuTextEditController.textMenu(this, textsArea);
                return true;

            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return false;
    }

    @FXML
    @Override
    public void myBoxClipBoard() {
        try {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == textsTab) {
                TextClipboardPopController.open(this, textsArea);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    @Override
    public void clearAction() {
        try {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == domTab) {
                clearDom();
            } else if (tab == textsTab) {
                clearTexts();
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    protected void editTexts() {
        String texts = currentTexts(false);
        if (texts == null || texts.isBlank()) {
            popError(message("NoData"));
            return;
        }
        TextEditorController.edit(texts);
    }

    @FXML
    @Override
    public void systemMethod() {
        String texts = currentTexts(false);
        if (texts == null || texts.isBlank()) {
            popError(message("NoData"));
            return;
        }
        File tmpFile = FileTmpTools.getTempFile("." + typeName.toLowerCase());
        TextFileTools.writeFile(tmpFile, texts);
        if (tmpFile != null && tmpFile.exists()) {
            browse(tmpFile);
        } else {
            popError(message("Failed"));
        }
    }

    @Override
    public boolean controlAltN() {
        createAction();
        return true;
    }

    @Override
    public boolean controlAltS() {
        saveAction();
        return true;
    }

    @Override
    public boolean controlAltB() {
        saveAsAction();
        return true;
    }

    @Override
    public boolean controlAltR() {
        recoverAction();
        return true;
    }

    @Override
    public boolean controlAltL() {
        clearAction();
        return true;
    }

    /*
        panes
     */
    public void tabChanged() {
        try {
            TextClipboardPopController.closeAll();

            Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
            if (currentTab == domTab || currentTab == textsTab) {
                menuButton.setDisable(false);
                synchronizeButton.setDisable(false);
                saveButton.setDisable(false);
                clearButton.setDisable(false);

            } else {
                menuButton.setDisable(true);
                synchronizeButton.setDisable(true);
                saveButton.setDisable(true);
                clearButton.setDisable(true);

            }

            recoverButton.setDisable(sourceFile == null);

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public boolean checkBeforeNextAction() {
        if (!isIndependantStage() || !fileChanged) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(message("FileChanged"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSave = new ButtonType(message("Save"));
            ButtonType buttonNotSave = new ButtonType(message("NotSave"));
            ButtonType buttonCancel = new ButtonType(message("Cancel"));
            alert.getButtonTypes().setAll(buttonSave, buttonNotSave, buttonCancel);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            Optional<ButtonType> result = alert.showAndWait();
            if (result == null || !result.isPresent()) {
                return false;
            }
            if (result.get() == buttonSave) {
                saveAction();
                return false;
            } else if (result.get() == buttonNotSave) {
                fileChanged = false;
                return true;
            } else {
                return false;
            }
        }
    }

    @FXML
    protected void popHelps(Event event) {
        if (UserConfig.getBoolean(typeName + "HelpsPopWhenMouseHovering", false)) {
            showHelps(event);
        }
    }

    @FXML
    protected void showHelps(Event event) {
        try {
            List<MenuItem> items = helpMenus(event);
            if (items == null || items.isEmpty()) {
                return;
            }

            items.add(new SeparatorMenuItem());

            CheckMenuItem hoverMenu = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
            hoverMenu.setSelected(UserConfig.getBoolean(typeName + "HelpsPopWhenMouseHovering", false));
            hoverMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(typeName + "HelpsPopWhenMouseHovering", hoverMenu.isSelected());
                }
            });
            items.add(hoverMenu);

            popEventMenu(event, items);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected List<MenuItem> helpMenus(Event event) {
        return null;
    }

}
