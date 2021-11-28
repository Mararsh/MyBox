package mara.mybox.controller;

import java.io.File;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import mara.mybox.data.Data2D;
import mara.mybox.data.DataFile;
import mara.mybox.data.DataFileExcel;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-12-25
 * @License Apache License Version 2.0
 */
public abstract class BaseData2DFileController extends BaseController {

    protected DataFile dataFile;
    protected TableData2DDefinition tableData2DDefinition;
    protected TableData2DColumn tableData2DColumn;
    protected ControlData2DEditTable tableController;
    protected ControlData2DEditText textController;
    protected ControlData2DAttributes attributesController;
    protected ControlData2DColumns columnsController;

    @FXML
    protected TitledPane filePane, saveAsPane, backupPane, formatPane;
    @FXML
    protected VBox fileBox, formatBox;
    @FXML
    protected ControlFileBackup backupController;
    @FXML
    protected Label fileInfoLabel;
    @FXML
    protected ControlData2D dataController;

    public BaseData2DFileController() {
        TipsLabelKey = "DataFileTips";
    }

    /*
        abstract
     */
    public abstract DataFile makeTargetDataFile(File file);

    public abstract void pickOptions();


    /*
        init
     */
    // class should call this before initControls()
    public void setDataType(Data2D.Type type) {
        try {
            dataController.setDataType(this, type);
            dataController.backupController = backupController;
            dataFile = (DataFile) dataController.data2D;
            tableData2DDefinition = dataController.tableData2DDefinition;
            tableData2DColumn = dataController.tableData2DColumn;
            tableController = dataController.editController.tableController;
            textController = dataController.editController.textController;
            attributesController = dataController.attributesController;
            columnsController = dataController.columnsController;
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
                    updateStatus();
                }
            });

            dataController.loadedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> o, Boolean ov, Boolean nv) {
                    afterFileLoaded();
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

            createFile();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void sourceFileChanged(File file) {
        if (!checkBeforeNextAction()) {
            return;
        }
        initFile(file);
        dataFile.setUserSavedDataDefinition(!dataFile.isTmpFile());
        dataController.loadDefinition();
    }

    protected void afterFileLoaded() {
        updateStatus();
    }

    protected void updateStatus() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                boolean changed = dataController.isChanged();
                saveButton.setDisable(!changed);
                recoverButton.setDisable(!changed);

                String title = baseTitle;
                if (!dataFile.isTmpFile()) {
                    title += " " + dataFile.getFile().getAbsolutePath();
                }
                if (changed) {
                    title += " *";
                }
                getMyStage().setTitle(title);
                updateInfoLabel();
            }
        });
    }

    protected void updateInfoLabel() {
        String info = "";
        if (!dataFile.isTmpFile()) {
            info = message("FileSize") + ": " + FileTools.showFileSize(dataFile.getFile().length()) + "\n"
                    + message("FileModifyTime") + ": " + DateTools.datetimeToString(dataFile.getFile().lastModified()) + "\n";
            if (dataFile instanceof DataFileExcel) {
                DataFileExcel e = (DataFileExcel) dataFile;
                String sheet = e.getCurrentSheetName();
                info += message("CurrentSheet") + ": " + (sheet == null ? "" : sheet)
                        + (e.getSheetNames() == null ? "" : " / " + e.getSheetNames().size()) + "\n";
            } else {
                info += message("Charset") + ": " + dataFile.getCharset() + "\n"
                        + message("Delimiter") + ": " + TextTools.delimiterMessage(dataFile.getDelimiter()) + "\n";
            }
            info += message("FirstLineAsNames") + ": " + (dataFile.isHasHeader() ? message("Yes") : message("No")) + "\n";
        }
        if (!dataFile.isMutiplePages()) {
            info += message("RowsNumber") + ":" + dataFile.tableRowsNumber() + "\n";
        } else {
            info += message("LinesNumberInFile") + ":" + dataFile.getDataSize() + "\n";
        }
        info += message("ColumnsNumber") + ": " + dataFile.columnsNumber() + "\n"
                + message("CurrentPage") + ": " + StringTools.format(dataFile.getCurrentPage() + 1)
                + " / " + StringTools.format(dataFile.getPagesNumber()) + "\n";
        if (dataFile.isMutiplePages() && dataFile.hasData()) {
            info += message("RowsRangeInPage")
                    + ": " + StringTools.format(dataFile.getStartRowOfCurrentPage() + 1) + " - "
                    + StringTools.format(dataFile.getStartRowOfCurrentPage() + dataFile.tableRowsNumber())
                    + " ( " + StringTools.format(dataFile.tableRowsNumber()) + " )\n";
        }
        info += message("PageModifyTime") + ": " + DateTools.nowString();
        fileInfoLabel.setText(info);
    }

    public void initFile(File file) {
        dataFile.initFile(file);
    }

    @FXML
    public void createFile() {
        try {
            if (!checkBeforeNextAction()) {
                return;
            }
            sourceFileChanged(dataFile.tmpFile());
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void loadContentInSystemClipboard() {
        dataController.loadContentInSystemClipboard();
    }

    @FXML
    @Override
    public void saveAction() {
        if (dataFile == null || !dataController.isChanged()) {
            return;
        }
        if (dataFile.isTmpFile()) {
            saveAs(true);
            return;
        }
        dataController.save();
    }

    @FXML
    @Override
    public void saveAsAction() {
        saveAs(false);
    }

    public void saveAs(boolean load) {
        if (dataController.checkBeforeSave() < 0) {
            return;
        }
        File file = chooseSaveFile();
        if (file == null) {
            return;
        }
        DataFile targetData = makeTargetDataFile(file);
        if (targetData == null) {
            return;
        }
        dataController.saveAs(targetData, load || saveAsType == BaseController_Attributes.SaveAsType.Load);
    }

    @FXML
    @Override
    public void recoverAction() {
        dataController.resetStatus();
        sourceFileChanged(dataFile.getFile());
    }

    @FXML
    public void refreshFile() {
        dataController.resetStatus();
        initFile(dataFile.getFile());
        dataFile.setUserSavedDataDefinition(false);
        pickOptions();
        dataController.loadDefinition();
    }

    @FXML
    public void editTextFile() {
        if (dataFile == null || dataFile.getFile() == null) {
            return;
        }
        TextEditorController controller = (TextEditorController) WindowTools.openStage(Fxmls.TextEditorFxml);
        controller.sourceFileChanged(dataFile.getFile());
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
    public void cleanPane() {
        try {
            tableController = null;
            dataFile = null;
            tableData2DDefinition = null;
            tableData2DColumn = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
