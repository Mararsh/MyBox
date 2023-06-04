package mara.mybox.controller;

import java.io.File;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import mara.mybox.data.JsonTreeNode;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-4-30
 * @License Apache License Version 2.0
 */
public class JsonEditorController extends BaseController {

    protected boolean domChanged, textsChanged, fileChanged;
    protected String title;
    protected final SimpleBooleanProperty loadNotify;

    @FXML
    protected Tab domTab, textsTab, optionsTab, backupTab;
    @FXML
    protected TextArea textsArea;
    @FXML
    protected Label textsLabel;
    @FXML
    protected ControlJsonTree domController;
    @FXML
    protected ControlJsonOptions optionsController;
    @FXML
    protected ControlFileBackup backupController;
    @FXML
    protected CheckBox wrapTextsCheck;

    public JsonEditorController() {
        baseTitle = message("JsonEditor");
        TipsLabelKey = "JsonEditorTips";
        loadNotify = new SimpleBooleanProperty(false);
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.JSON);
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
            domController.jsonEditor = this;
            tabChanged();

            backupController.setParameters(this, baseName);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        source
     */
    @Override
    public void sourceFileChanged(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.length() > 10 * 1024 * 1024) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(message("FileSize") + ": " + FileTools.showFileSize(file.length()));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonJson = new ButtonType(message("JsonEditor"));
            ButtonType buttonSystem = new ButtonType(message("SystemWebBrowser"));
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

        sourceFile = file;
        writePanes(TextFileTools.readTexts(file));
    }

    public boolean writePanes(String json) {
        fileChanged = false;
        openSourceButton.setDisable(sourceFile == null || !sourceFile.exists());
        isSettingValues = true;
        loadDom(json, false);
        loadText(json, false);
        updateTitle();
        isSettingValues = false;
        if (backupController != null) {
            backupController.loadBackups(sourceFile);
        }
        loadNotify.set(!loadNotify.get());
        return true;
    }

    @FXML
    @Override
    public void createAction() {
        try {
            if (!checkBeforeNextAction()) {
                return;
            }
            sourceFile = null;
            getMyStage().setTitle(getBaseTitle());
            fileChanged = false;
            if (backupController != null) {
                backupController.loadBackups(null);
            }
            writePanes("{}");
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
        String json = currentJSON(true);
        if (json == null || json.isBlank()) {
            popError(message("NoData"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {
            @Override
            protected boolean handle() {
                try {
                    File tmpFile = TextFileTools.writeFile(json);
                    if (tmpFile == null || !tmpFile.exists()) {
                        return false;
                    }
                    if (sourceFile != null && backupController.needBackup()) {
                        backupController.addBackup(task, sourceFile);
                    }
                    return FileTools.rename(tmpFile, targetFile);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                popSaved();
                recordFileWritten(targetFile);
                sourceFileChanged(targetFile);
            }

        };
        start(task);
    }

    public String currentJSON(boolean synchronize) {
        try {
            Tab currentTab = tabPane.getSelectionModel().getSelectedItem();

            if (currentTab == domTab) {
                String json = jsonByDom();
                if (synchronize) {
                    loadText(json, false);
                }
                return json;

            } else if (currentTab == textsTab) {
                String json = jsonByText();
                if (synchronize) {
                    loadDom(json, false);
                }
                return json;

            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
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
    }

    @FXML
    @Override
    public void saveAsAction() {
        File file = chooseSaveFile();
        if (file == null) {
            return;
        }
        String json = currentJSON(false);
        if (json == null || json.isBlank()) {
            popError(message("NoData"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {
            @Override
            protected boolean handle() {
                File tmpFile = TextFileTools.writeFile(json);
                if (tmpFile == null || !tmpFile.exists()) {
                    return false;
                }
                return FileTools.rename(tmpFile, file);
            }

            @Override
            protected void whenSucceeded() {
                popSaved();
                recordFileWritten(file);
                JsonEditorController.open(file);
            }
        };
        start(task);
    }


    /*
        dom
     */
    public void loadDom(String json, boolean updated) {
        if (!tabPane.getTabs().contains(domTab)) {
            return;
        }
        domController.makeTree(json);
        domChanged(updated);
    }

    public String jsonByDom() {
        return domController.jsonFormatString();
    }

    public void domChanged(boolean changed) {
        domChanged = changed;
        domTab.setText(message("Tree") + (changed ? " *" : ""));
        if (changed) {
            fileChanged();
        }
    }

    public void updateNode(TreeItem<JsonTreeNode> item) {
        domChanged(true);
    }

    public void clearDom() {
        domController.clearTree();
        domChanged(true);
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
            MyBoxLog.error(e.toString());
        }
    }

    public void loadText(String json, boolean updated) {
        if (!tabPane.getTabs().contains(textsTab)) {
            return;
        }
        try {
            textsArea.setText(json);
            textsChanged(updated);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public String jsonByText() {
        return textsArea.getText();
    }

    protected void textsChanged(boolean changed) {
//        MyBoxLog.debug(changed);
        textsChanged = changed;
        textsTab.setText(message("Texts") + (changed ? " *" : ""));
        textsLabel.setText(message("CharactersNumber") + ": " + StringTools.format(textsArea.getLength()));
        if (changed) {
            fileChanged();
        }
    }

    @FXML
    protected void editTexts() {
        TextEditorController controller = (TextEditorController) WindowTools.openStage(Fxmls.TextEditorFxml);
        controller.loadContents(textsArea.getText());
        controller.requestMouse();
    }

    @FXML
    protected void clearTexts() {
        textsArea.clear();
        textsChanged(true);
    }


    /*
        buttons
     */
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
            MyBoxLog.debug(e.toString());
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
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    public void synchronizeDom() {
        Platform.runLater(() -> {
            loadText(jsonByDom(), true);
        });
    }

    public void synchronizeTexts() {
        Platform.runLater(() -> {
            loadDom(jsonByText(), true);
        });
    }

    @FXML
    @Override
    public boolean menuAction() {
        try {
            closePopup();

            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == domTab) {
                domController.popFunctionsMenu(null);
                return true;

            } else if (tab == textsTab) {
                MenuTextEditController.open(this, textsArea);
                return true;

            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
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
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    @Override
    public void clearAction() {
        try {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
//            if (tab == backupTab) {
//                return;
//            }
//            if (!PopTools.askSure(getTitle(), message("SureClearData"))) {
//                return;
//            }
            if (tab == domTab) {
                clearDom();
            } else if (tab == textsTab) {
                clearTexts();

            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    /*
        panes
     */
    public void tabChanged() {
        try {
            TextClipboardPopController.closeAll();
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            menuButton.setDisable(tab == backupTab || tab == optionsTab);
            synchronizeButton.setDisable(tab == backupTab || tab == optionsTab);
            clearButton.setDisable(tab == backupTab || tab == optionsTab);
            saveButton.setDisable(tab == backupTab || tab == optionsTab);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public boolean checkBeforeNextAction() {
        if (isPop || !fileChanged) {
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
            } else {
                return result.get() == buttonNotSave;
            }
        }
    }

    /*
        static
     */
    public static JsonEditorController load(String json) {
        try {
            JsonEditorController controller = (JsonEditorController) WindowTools.openStage(Fxmls.JsonEditorFxml);
            controller.writePanes(json);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static JsonEditorController open(File file) {
        try {
            JsonEditorController controller = (JsonEditorController) WindowTools.openStage(Fxmls.JsonEditorFxml);
            controller.sourceFileChanged(file);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
