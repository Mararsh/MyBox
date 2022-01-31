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
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import mara.mybox.data.Data2D;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-12-25
 * @License Apache License Version 2.0
 */
public abstract class BaseData2DFileController extends BaseController {

    protected Data2D data2D;
    protected TableData2DDefinition tableData2DDefinition;
    protected TableData2DColumn tableData2DColumn;
    protected ControlData2DEditTable tableController;
    protected ControlData2DEditText textController;
    protected ControlData2DAttributes attributesController;
    protected ControlData2DColumns columnsController;

    @FXML
    protected TitledPane infoPane, saveAsPane, backupPane, formatPane;
    @FXML
    protected VBox formatBox;
    @FXML
    protected ControlFileBackup backupController;
    @FXML
    protected ControlData2D dataController;
    @FXML
    protected Label infoLabel, nameLabel;

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
    // subclass should call this
    public void setDataType(Data2D.Type type) {
        try {
            dataController.setDataType(this, type);
            dataController.backupController = backupController;
            data2D = dataController.data2D;
            tableData2DDefinition = dataController.tableData2DDefinition;
            tableData2DColumn = dataController.tableData2DColumn;
            tableController = dataController.editController.tableController;
            textController = dataController.editController.textController;
            attributesController = dataController.attributesController;
            columnsController = dataController.columnsController;

            tableController.dataLabel = nameLabel;
            tableController.baseTitle = baseTitle;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {

            super.initControls();
            initFormatTab();
            initBackupsTab();
            initSaveAsTab();

            dataController.statusNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> o, Boolean ov, Boolean nv) {
                    checkStatus();
                }
            });

            dataController.savedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> o, Boolean ov, Boolean nv) {
                    checkStatus();
                }
            });

            checkStatus();

            infoLabel.textProperty().bind(attributesController.infoArea.textProperty());

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

    public void loadDef(Data2DDefinition def) {
        dataController.loadDef(def);
    }

    protected void checkStatus() {
        leftPane.setDisable(data2D == null || data2D.isTmpData());
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
    public void createAction() {
        dataController.create();
    }

    @FXML
    @Override
    public void recoverAction() {
        dataController.recoverFile();
    }

    @FXML
    @Override
    public void loadContentInSystemClipboard() {
        dataController.loadContentInSystemClipboard();
    }

    @FXML
    @Override
    public void saveAction() {
        dataController.save();
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
        data2D.initFile(data2D.getFile());
        pickRefreshOptions();
        dataController.readDefinition();
    }

    @FXML
    public void editTextFile() {
        if (data2D == null || data2D.getFile() == null) {
            return;
        }
        TextEditorController controller = (TextEditorController) WindowTools.openStage(Fxmls.TextEditorFxml);
        controller.sourceFileChanged(data2D.getFile());
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

    @Override
    public void myBoxClipBoard() {
        dataController.myBoxClipBoard();
    }

    @Override
    public void cleanPane() {
        try {
            dataController = null;
            tableController = null;
            data2D = null;
            tableData2DDefinition = null;
            tableData2DColumn = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
