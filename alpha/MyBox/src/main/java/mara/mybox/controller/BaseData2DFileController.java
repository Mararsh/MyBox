package mara.mybox.controller;

import java.io.File;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import mara.mybox.data.Data2D;
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

    protected Data2D data2D;
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
    public abstract Data2D makeTargetDataFile(File file);

    public abstract void pickOptions();


    /*
        init
     */
    // class should call this before initControls()
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

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initFormatTab() {
        try {
            if (formatPane == null) {
                return;
            }
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

            createAction();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void sourceFileChanged(File file) {
        dataController.loadFile(file);
    }

    protected void checkStatus() {
        try {
            boolean changed = dataController.isChanged();
            if (data2D.isTmpFile()) {
                isSettingValues = true;
                if (formatPane != null) {
                    formatPane.setExpanded(false);
                    formatPane.setDisable(true);
                }
                if (backupPane != null) {
                    backupPane.setExpanded(false);
                    backupPane.setDisable(true);
                }
                isSettingValues = false;
            } else {
                isSettingValues = true;
                if (formatPane != null) {
                    formatPane.setExpanded(UserConfig.getBoolean(baseName + "FormatPane", true));
                    formatPane.setDisable(false);
                }
                if (backupPane != null) {
                    backupPane.setExpanded(UserConfig.getBoolean(baseName + "BackupPane", false));
                    backupPane.setDisable(false);
                }
                isSettingValues = false;
            }

            if (myStage != null) {
                String title = baseTitle;
                if (!data2D.isTmpFile()) {
                    title += " " + data2D.getFile().getAbsolutePath();
                }
                if (changed) {
                    title += " *";
                }
                myStage.setTitle(title);
            }
            updateInfoLabel();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void updateInfoLabel() {
        String info = "";
        if (!data2D.isTmpFile()) {
            info = message("FileSize") + ": " + FileTools.showFileSize(data2D.getFile().length()) + "\n"
                    + message("FileModifyTime") + ": " + DateTools.datetimeToString(data2D.getFile().lastModified()) + "\n";
            if (data2D instanceof DataFileExcel) {
                DataFileExcel e = (DataFileExcel) data2D;
                String sheet = e.getCurrentSheetName();
                info += message("CurrentSheet") + ": " + (sheet == null ? "" : sheet)
                        + (e.getSheetNames() == null ? "" : " / " + e.getSheetNames().size()) + "\n";
            } else {
                info += message("Charset") + ": " + data2D.getCharset() + "\n"
                        + message("Delimiter") + ": " + TextTools.delimiterMessage(data2D.getDelimiter()) + "\n";
            }
            info += message("FirstLineAsNames") + ": " + (data2D.isHasHeader() ? message("Yes") : message("No")) + "\n";
        }
        if (!data2D.isMutiplePages()) {
            info += message("RowsNumber") + ":" + data2D.tableRowsNumber() + "\n";
        } else {
            info += message("LinesNumberInFile") + ":" + data2D.getDataSize() + "\n";
        }
        info += message("ColumnsNumber") + ": " + data2D.columnsNumber() + "\n"
                + message("CurrentPage") + ": " + StringTools.format(data2D.getCurrentPage() + 1)
                + " / " + StringTools.format(data2D.getPagesNumber()) + "\n";
        if (data2D.isMutiplePages() && data2D.hasData()) {
            info += message("RowsRangeInPage")
                    + ": " + StringTools.format(data2D.getStartRowOfCurrentPage() + 1) + " - "
                    + StringTools.format(data2D.getStartRowOfCurrentPage() + data2D.tableRowsNumber())
                    + " ( " + StringTools.format(data2D.tableRowsNumber()) + " )\n";
        }
        info += message("PageModifyTime") + ": " + DateTools.nowString();
        fileInfoLabel.setText(info);
    }

    @FXML
    @Override
    public void createAction() {
        dataController.create();
    }

    @FXML
    @Override
    public void loadContentInSystemClipboard() {
        dataController.loadContentInSystemClipboard();
    }

    @FXML
    @Override
    public void saveAction() {
        if (data2D.isTmpFile()) {
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
        Data2D targetData = makeTargetDataFile(file);
        if (targetData == null) {
            return;
        }
        dataController.saveAs(targetData, load || saveAsType == BaseController_Attributes.SaveAsType.Load);
    }

    @FXML
    @Override
    public void recoverAction() {
        dataController.recover();
    }

    @FXML
    public void refreshFile() {
        dataController.resetStatus();
        data2D.initFile(data2D.getFile());
        pickOptions();
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
            tableController = null;
            data2D = null;
            tableData2DDefinition = null;
            tableData2DColumn = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
