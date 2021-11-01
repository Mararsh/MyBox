package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import mara.mybox.data.Data2D;
import mara.mybox.data.DataFile;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.table.TableDataColumn;
import mara.mybox.db.table.TableDataDefinition;
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
    protected TableDataDefinition tableDataDefinition;
    protected TableDataColumn tableDataColumn;

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
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    // class should call this before initControls()
    public void setDataType(Data2D.Type type) {
        try {
            dataController.setDataType(type);
            dataFile = (DataFile) dataController.data2D;
            tableDataDefinition = dataController.tableDataDefinition;
            tableDataColumn = dataController.tableDataColumn;
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

            dataFile.getPageDataChangedNotify().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                        updateStatus();
                    });

            dataFile.getPageDataLoadedNotify().addListener(
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
            dataFile.initData();
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
        pickOptions();
        loadFile();
    }

    public void pickOptions() {

    }

    public void initFile() {
        dataFile.initFile();
        dataController.paginationBox.setVisible(false);
//        dataController.clearSheet();
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

                @Override
                protected boolean handle() {
                    if (!dataFile.readDataDefinition(task) || isCancelled()) {
                        return false;
                    }
                    if (!dataFile.readColumns(task) || isCancelled()) {
                        return false;
                    }
                    List<ColumnDefinition> columns = dataFile.getColumns();
                    List<ColumnDefinition> savedColumns = dataFile.getSavedColumns();
                    if (columns == null || columns.isEmpty()) {
                        if (savedColumns != null) {
                            columns = savedColumns;
                        } else {
                            columns = new ArrayList<>();
                        }
                    }
                    ;
                    if (!dataFile.readPageData(task) || isCancelled()) {
                        return false;
                    }
                    if (dataFile.hasData()) {
                        int colsNumber = dataFile.pageColsNumber();
                        if (columns.size() < colsNumber) {
                            for (int col = columns.size() + 1; col <= colsNumber; col++) {
                                ColumnDefinition column = new ColumnDefinition(message(dataFile.colPrefix()) + col, ColumnDefinition.ColumnType.String);
                                columns.add(column);
                            }
                            tableDataColumn.save(dataFile.getDefinition().getDfid(), columns);
                        }
                    }
                    if (isCancelled()) {
                        return false;
                    }
                    dataFile.setColumns(columns);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
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
                    task = null;
                    dataController.updateInterface();
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
            dataFile.setDataNumber(0);
            dataFile.setTotalRead(false);
            dataController.paginationBox.setVisible(false);
            backgroundTask = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    if (!dataFile.readTotal(backgroundTask) || isCancelled()) {
                        return false;
                    }
                    dataController.countPagination((int) (dataFile.getStartRowOfCurrentPage() / dataFile.getPageSize()));
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    dataFile.setTotalRead(true);
                    dataController.paginationBox.setVisible(true);
                    dataController.setPagination();
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
                    backgroundTask = null;
                    dataController.updateLabel();
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
        dataFile.setPageDataChanged(false);
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

    public void loadData(List<List<String>> data, List<ColumnDefinition> dataColumns) {
        sourceFile = null;
        dataController.loadData(data, dataColumns);
    }

    @FXML
    @Override
    public void recoverAction() {
        dataController.recoverAction();
    }

    protected void updateStatus() {
        String title = baseTitle;
        if (sourceFile != null) {
            title += " " + sourceFile.getAbsolutePath();
        }
        if (dataFile.isPageDataChanged()) {
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
        dataController.saveAction();
    }

    @FXML
    @Override
    public void saveAsAction() {
        dataController.sourceFile = sourceFile;
        dataController.saveAsType = saveAsType;
//        dataController.saveAs();
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
