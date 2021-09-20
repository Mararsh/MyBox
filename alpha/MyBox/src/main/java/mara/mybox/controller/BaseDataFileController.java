package mara.mybox.controller;

import java.io.File;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
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
        TipsLabelKey = "DataFileTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            initFormatTab();
            initBackupsTab();
            initSaveAsTab();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initFormatTab() {
        try {
            if (formatPane == null) {
                return;
            }
            formatPane.setExpanded(UserConfig.getBoolean(baseName + "FormatPane", true));
            formatPane.expandedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                UserConfig.setBoolean(baseName + "FormatPane", formatPane.isExpanded());
            });

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
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void pickOptions() {
        try {

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
            pickOptions();
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
        dataController.initCurrentPage();
        dataController.userSavedDataDefinition = true;
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
        pickOptions();
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
        getMyStage().setTitle(title);
        updateInfoLabel();
    }

    protected void updateInfoLabel() {
    }

    @FXML
    @Override
    public void saveAction() {
        if (sourceFile == null) {
            saveAsAction();
        } else {
            dataController.saveFile();
        }
    }

    @FXML
    @Override
    public void saveAsAction() {
        dataController.sourceFile = sourceFile;
        dataController.saveAsType = saveAsType;
        dataController.saveAs();
    }

    @FXML
    public void editTextFile() {
        if (sourceFile == null) {
            return;
        }
        TextEditorController controller = (TextEditorController) WindowTools.openStage(Fxmls.TextEditorFxml);
        controller.sourceFileChanged(sourceFile);
        controller.toFront();
    }

    @Override
    public boolean checkBeforeNextAction() {
        return dataController.checkBeforeNextAction();
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (!super.keyEventsFilter(event)) {
            return dataController.keyEventsFilter(event);
        }
        return true;
    }

}
