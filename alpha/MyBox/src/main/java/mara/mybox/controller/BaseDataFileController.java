package mara.mybox.controller;

import java.io.File;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-12-25
 * @License Apache License Version 2.0
 */
public abstract class BaseDataFileController extends BaseController {

    protected ControlSheetFile dataController;

    @FXML
    protected TitledPane filePane, saveAsPane, backupPane, formatPane;
    @FXML
    protected VBox fileBox, formatBox;
    @FXML
    protected ControlFileBackup backupController;
    @FXML
    protected Label fileInfoLabel;

    public BaseDataFileController() {
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            initBackupsTab();
            initSaveAsTab();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initBackupsTab() {
        try {
            if (backupPane == null) {
                return;
            }
            backupPane.setExpanded(UserConfig.getBoolean(baseName + "BackupPane", false));
            backupPane.expandedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                UserConfig.setBoolean(baseName + "BackupPane", backupPane.isExpanded());
            });

            backupController.setControls(this, baseName);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initSaveAsTab() {
        try {
            if (saveAsPane == null) {
                return;
            }
            saveAsPane.setExpanded(UserConfig.getBoolean(baseName + "SaveAsPane", true));
            saveAsPane.expandedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                UserConfig.setBoolean(baseName + "SaveAsPane", saveAsPane.isExpanded());
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();

            if (dataController != null) {
                dataController.fileLoadedNotify.addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                            fileLoaded();
                        });
                dataController.sheetChangedNotify.addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                            sheetChanged();
                        });
                dataController.newSheet(3, 3);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void createAction() {
        try {
            if (!checkBeforeNextAction()) {
                return;
            }
            sourceFile = null;
            dataController.createAction();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void sourceFileChanged(File file) {
        if (!checkBeforeNextAction()) {
            return;
        }
        sourceFile = file;
        dataController.userSavedDataDefinition = true;
        dataController.initCurrentPage();
        loadFile();
    }

    @FXML
    public void refreshAction() {
        if (!checkBeforeNextAction()) {
            return;
        }
        dataController.userSavedDataDefinition = false;
        loadFile();
    }

    public void loadFile() {
        dataController.sourceFile = sourceFile;
        dataController.loadFile();
    }

    @FXML
    @Override
    public void recoverAction() {
        dataController.recover();
    }

    protected void fileLoaded() {
        updateStatus();
    }

    protected void sheetChanged() {
        updateStatus();
    }

    protected void updateStatus() {
        String title = baseTitle;
        if (sourceFile != null) {
            title += " " + sourceFile.getAbsolutePath();
        }
        if (dataController.dataChangedNotify.get()) {
            title += " *";
        }
        myStage.setTitle(title);
        updateInfoLabel();
    }

    protected void updateInfoLabel() {

    }

    @FXML
    @Override
    public void saveAction() {
        dataController.saveFile();
    }

    @Override
    public boolean checkBeforeNextAction() {
        return dataController.checkBeforeNextAction();
    }

}
