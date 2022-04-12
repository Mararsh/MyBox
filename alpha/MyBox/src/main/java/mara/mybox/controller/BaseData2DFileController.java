package mara.mybox.controller;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import mara.mybox.data2d.Data2D;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-12-25
 * @License Apache License Version 2.0
 */
public abstract class BaseData2DFileController extends BaseData2DController {

    @FXML
    protected TitledPane infoPane, saveAsPane, backupPane, formatPane;
    @FXML
    protected VBox formatBox;
    @FXML
    protected ControlFileBackup backupController;
    @FXML
    protected Label infoLabel;

    public BaseData2DFileController() {
        TipsLabelKey = "DataFileTips";
    }

    /*
        abstract
     */
    public abstract void pickRefreshOptions();

    public abstract Data2D saveAsTarget();

    /*
        init
     */
    @Override
    public void initControls() {
        try {
            super.initControls();

            initInfoTab();
            initFormatTab();
            initBackupsTab();
            initSaveAsTab();

            dataController.backupController = backupController;
            dataController.loadedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> o, Boolean ov, Boolean nv) {
                    checkStatus();
                }
            });

            checkStatus();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initInfoTab() {
        try {
            if (infoPane == null) {
                return;
            }
            infoPane.setExpanded(UserConfig.getBoolean(baseName + "InfoPane", true));
            infoPane.expandedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                if (!isSettingValues) {
                    UserConfig.setBoolean(baseName + "InfoPane", infoPane.isExpanded());
                }
            });

            infoLabel.textProperty().bind(dataController.attributesController.infoArea.textProperty());

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
                if (!isSettingValues) {
                    UserConfig.setBoolean(baseName + "FormatPane", formatPane.isExpanded());
                }
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
            backupPane.setExpanded(UserConfig.getBoolean(baseName + "BackupPane", true));
            backupPane.expandedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                if (!isSettingValues) {
                    UserConfig.setBoolean(baseName + "BackupPane", backupPane.isExpanded());
                }
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
                if (!isSettingValues) {
                    UserConfig.setBoolean(baseName + "SaveAsPane", saveAsPane.isExpanded());
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void sourceFileChanged(File file) {
        dataController.sourceFileChanged(file);
    }

    protected void checkStatus() {
        leftPane.setDisable(dataController.data2D == null || dataController.data2D.isTmpData());
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    leftPane.setHvalue(0);
                });
            }
        }, 500);
    }

    @FXML
    @Override
    public void saveAsAction() {
        Data2D targetData = saveAsTarget();
        if (targetData == null) {
            return;
        }
        dataController.saveAs(targetData, saveAsType);
    }

    @FXML
    public void refreshFile() {
        dataController.resetStatus();
        dataController.data2D.initFile(dataController.data2D.getFile());
        pickRefreshOptions();
        dataController.readDefinition();
    }

    @FXML
    public void editTextFile() {
        if (dataController.data2D == null || dataController.data2D.getFile() == null) {
            return;
        }
        TextEditorController controller = (TextEditorController) WindowTools.openStage(Fxmls.TextEditorFxml);
        controller.sourceFileChanged(dataController.data2D.getFile());
        controller.toFront();
    }

}
