package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
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
import mara.mybox.data.StringTable;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
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
    public abstract boolean savePageData(File file);

    public abstract boolean savePageDataAs(File file);

    public abstract void open(File file);

    public abstract void pickOptions();

    protected abstract void updateInfoLabel();

    /*
        init
     */
    // class should call this before initControls()
    public void setDataType(Data2D.Type type) {
        try {
            dataController.setDataType(this, type);
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

    @FXML
    @Override
    public void createAction() {
        try {
            if (!checkBeforeNextAction()) {
                return;
            }
            sourceFile = null;
            dataFile.resetData();
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
        dataFile.setFile(file);
        dataFile.setUserSavedDataDefinition(true);
        loadFile();
    }

    public void initFile() {
        dataFile.initFile(sourceFile);
        tableController.resetView();
        if (backupController != null) {
            backupController.loadBackups(sourceFile);
        }
//        BaseDataOperationController.closeAll(this);
    }

    public void loadFile() {
        if (dataFile == null || !checkBeforeNextAction()) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                task.cancel();
            }
            initFile();
            if (!dataFile.isUserSavedDataDefinition()) {
                pickOptions();
            }
            task = new SingletonTask<Void>(this) {

                private StringTable validateTable;

                @Override
                protected boolean handle() {
                    dataFile.setTask(task);
                    long d2did = dataFile.readDataDefinition();
                    if (d2did < 0 || isCancelled()) {
                        return false;
                    }
                    List<String> names = dataFile.readColumns();
                    if (isCancelled()) {
                        return false;
                    }
                    if (names == null || names.isEmpty()) {
                        dataFile.setHasHeader(false);
                        tableData2DColumn.clearFile(dataFile);
                        dataFile.setColumns(null);
                    } else {
                        List<Data2DColumn> columns = new ArrayList<>();
                        List<Data2DColumn> savedColumns = dataFile.getSavedColumns();
                        for (int i = 0; i < names.size(); i++) {
                            Data2DColumn column;
                            if (savedColumns != null && i < savedColumns.size()) {
                                column = savedColumns.get(i);
                                if (dataFile.isHasHeader()) {
                                    column.setName(names.get(i));
                                }
                            } else {
                                column = new Data2DColumn(names.get(i), dataFile.defaultColumnType());
                            }
                            column.setD2id(d2did);
                            column.setIndex(i);
                            columns.add(column);
                        }
                        dataFile.setColumns(columns);
                        validateTable = Data2DColumn.validate(columns);
                        if (validateTable == null || validateTable.isEmpty()) {
                            tableData2DColumn.save(d2did, columns);
                        }
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (validateTable != null && !validateTable.isEmpty()) {
                        validateTable.editHtml();
                    }
                    loadTotal();
                }

                @Override
                protected void whenFailed() {
                    if (isCancelled()) {
                        return;
                    }
                    if (error != null) {
                        popError(message(error));
                    } else {
                        popFailed();
                    }
                }

                @Override
                protected void finalAction() {
                    dataFile.setTask(null);
                    dataController.loadData();
                    task = null;
                    updateStatus();
                }

            };
            start(task);
        }
    }

    public void loadTotal() {
        if (sourceFile == null) {
            return;
        }
        synchronized (this) {
            if (backgroundTask != null) {
                backgroundTask.cancel();
            }
            dataFile.setDataSize(0);
            dataFile.setTotalRead(false);
            dataController.paginationBox.setVisible(false);
            backgroundTask = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    dataFile.setBackgroundTask(backgroundTask);
                    return dataFile.readTotal() >= 0;
                }

                @Override
                protected void whenSucceeded() {
                    dataFile.setTotalRead(true);
                }

                @Override
                protected void whenFailed() {
                    if (isCancelled()) {
                        return;
                    }
                    if (error != null) {
                        popError(message(error));
                    } else {
                        popFailed();
                    }
                }

                @Override
                protected void finalAction() {
                    dataFile.setBackgroundTask(null);
                    backgroundTask = null;
                    tableController.refreshPagination();
                    updateStatus();
                }

            };
            start(backgroundTask, false);
        }

    }

    public void backup() {
        if (backupController != null && backupController.isBack()) {
            backupController.addBackup(sourceFile);
        }
    }

    @FXML
    public void refreshAction() {
        dataFile.setUserSavedDataDefinition(false);
        loadFile();
    }

    protected void updateStatus() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                saveButton.setDisable(!dataController.changed);
                recoverButton.setDisable(!dataController.changed);

                String title = baseTitle;
                if (sourceFile != null) {
                    title += " " + sourceFile.getAbsolutePath();
                }
                if (dataController.changed) {
                    title += " *";
                }
                getMyStage().setTitle(title);
                updateInfoLabel();
            }
        });
    }

    @FXML
    @Override
    public void saveAction() {
        if (dataFile == null || !dataController.changed) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            if (dataController.checkBeforeSave() < 0) {
                return;
            }
            File file = dataFile.getFile();
            if (file == null) {
                file = chooseSaveFile();
                if (file == null) {
                    return;
                }
                dataFile.setFile(file);
            }
            task = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    dataController.saveDefinition();
                    if (task == null || task.isCancelled()) {
                        return false;
                    }
                    try {
                        backup();
                        dataFile.setTask(task);
                        return savePageData(targetFile);
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    popInformation(message("Saved"));
                    recordFileWritten(targetFile);
                    if (sourceFile == null) {
                        sourceFile = targetFile;
                    }
                    tableController.tableChanged(false);
                    dataFile.setEndRowOfCurrentPage(dataFile.getStartRowOfCurrentPage() + dataFile.tableRowsNumber());
                    dataFile.setTotalRead(false);

                    updateStatus();
                    loadTotal();
                }

                @Override
                protected void finalAction() {
                    dataFile.setTask(null);
                    task = null;
                }
            };
            start(task);
        }
    }

    @FXML
    @Override
    public void saveAsAction() {
        File file = chooseSaveFile();
        if (file == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {
                String[][] data;

                @Override
                protected boolean handle() {
                    dataFile.setTask(task);
                    return savePageDataAs(file);
                }

                @Override
                protected void whenSucceeded() {
                    popInformation(message("Saved"));
                    recordFileWritten(file);
                    if (sourceFile == null || saveAsType == BaseController_Attributes.SaveAsType.Load) {
                        if (parentController != null) {
                            parentController.sourceFileChanged(file);
                            return;
                        }
                        sourceFileChanged(file);
                    } else {
                        open(file);
                    }
                }

                @Override
                protected void finalAction() {
                    dataFile.setTask(null);
                    task = null;
                }

            };
            start(task);
        }
    }

    @FXML
    @Override
    public void recoverAction() {
        dataController.resetStatus();
        dataFile.setUserSavedDataDefinition(true);
        loadFile();
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
        return dataController.needSave();
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
