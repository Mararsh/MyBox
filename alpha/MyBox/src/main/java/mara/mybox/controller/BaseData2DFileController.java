package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
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

    @Override
    public void initValues() {
        try {
            super.initValues();

            if (leftPaneControl == null) {
                leftPaneControl = dataController.leftPaneControl;
            }
            tableController = dataController.editController.tableController;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    // class should call this before initControls()
    public void setDataType(Data2D.Type type) {
        try {
            dataController.setDataType(type);
            dataFile = (DataFile) dataController.data2D;
            tableData2DDefinition = dataController.tableData2DDefinition;
            tableData2DColumn = dataController.tableData2DColumn;
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

            dataFile.getTableChangedNotify().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                        updateStatus();
                    });

            dataFile.getPageLoadedNotify().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                        updateStatus();
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
        dataController.updateLabel();
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
            task = new SingletonTask<Void>(this) {

                private StringTable validateTable;

                @Override
                protected boolean handle() {
                    dataFile.setTask(task);
                    long d2did = dataFile.readDataDefinition();
                    if (d2did < 0 || isCancelled()) {
                        return false;
                    }
                    List<Data2DColumn> fileColumns = dataFile.readColumns();
                    if (isCancelled()) {
                        return false;
                    }
                    List<Data2DColumn> columns = new ArrayList<>();
                    List<Data2DColumn> savedColumns = dataFile.getSavedColumns();
                    if (fileColumns == null || fileColumns.isEmpty()) {
                        columns.addAll(savedColumns);
                    } else {
                        for (Data2DColumn c : fileColumns) {
                            boolean found = false;
                            if (savedColumns != null) {
                                for (Data2DColumn def : savedColumns) {
                                    if (def.getName().equals(c.getName())) {
                                        columns.add(def);
                                        found = true;
                                        break;
                                    }
                                }
                            }
                            if (!found) {
                                columns.add(c);
                            }
                        }
                    }
                    dataFile.setColumns(columns);
                    if (!columns.isEmpty()) {
                        validateTable = Data2DColumn.validate(columns);
                        if (validateTable == null || validateTable.isEmpty()) {
                            tableData2DColumn.save(d2did, columns);
                        }
                        return true;
                    } else {
                        tableData2DColumn.clearFile(dataFile);
                        return false;
                    }
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

    public void recover() {
        dataFile.setTableChanged(false);
        loadFile();
    }

    @FXML
    public void refreshAction() {
        if (!checkBeforeNextAction()) {
            return;
        }
        dataFile.setUserSavedDataDefinition(false);
        pickOptions();
        loadFile();
    }

    public void pickOptions() {
    }

    @FXML
    @Override
    public void recoverAction() {
        dataController.recoverAction();
    }

    protected void updateStatus() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                String title = baseTitle;
                if (sourceFile != null) {
                    title += " " + sourceFile.getAbsolutePath();
                }
                if (dataFile != null && dataFile.isTableChanged()) {
                    title += " *";
                }
                getMyStage().setTitle(title);
                updateInfoLabel();
            }
        });

    }

    protected void updateInfoLabel() {
    }

    @FXML
    @Override
    public void saveAction() {
        if (dataFile == null) {
            return;
        }
        if (dataFile.getFile() == null) {
            saveAsAction();
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    backup();
                    error = savePageData();
                    return error == null;
                }

                @Override
                protected void whenSucceeded() {
                    popInformation(message("Saved"));
                    recordFileWritten(dataFile.getFile());
                    loadFile();
                }

            };
            start(task);
        }
    }

    public String savePageData() {
        return null;
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
                    error = savePageDataAs(file);
                    return error == null;
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
                        return;
                    }
                    open(file);
                }

            };
            start(task);
        }
    }

    public String savePageDataAs(File file) {
        return null;
    }

    public void open(File file) {

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
