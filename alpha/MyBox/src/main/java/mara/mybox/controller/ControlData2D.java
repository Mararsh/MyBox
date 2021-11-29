package mara.mybox.controller;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import mara.mybox.data.Data2D;
import mara.mybox.data.StringTable;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public class ControlData2D extends BaseController {

    protected Data2D data2D;
    protected TableData2DDefinition tableData2DDefinition;
    protected TableData2DColumn tableData2DColumn;
    protected ControlData2DEditTable tableController;
    protected ControlData2DEditText textController;
    protected final SimpleBooleanProperty statusNotify, loadedNotify, savedNotify;
    protected ControlFileBackup backupController;

    @FXML
    protected TabPane tabPane;
    @FXML
    protected Tab editTab, viewTab, attributesTab, columnsTab;
    @FXML
    protected ControlData2DEdit editController;
    @FXML
    protected ControlData2DView viewController;
    @FXML
    protected ControlData2DAttributes attributesController;
    @FXML
    protected ControlData2DColumns columnsController;
    @FXML
    protected HBox paginationBox;
    @FXML
    protected ComboBox<String> pageSizeSelector, pageSelector;
    @FXML
    protected Label pageLabel, dataSizeLabel;

    public ControlData2D() {
        statusNotify = new SimpleBooleanProperty(false);
        loadedNotify = new SimpleBooleanProperty(false);
        savedNotify = new SimpleBooleanProperty(false);
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            tableController = editController.tableController;
            textController = editController.textController;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    // parent should call this before initControls()
    public void setDataType(BaseController parent, Data2D.Type type) {
        try {
            parentController = parent;
            setFileType(parent.getSourceFileType(), parent.getTargetFileType());

            data2D = Data2D.create(type);
            data2D.setTableView(tableController.tableView);

            tableData2DDefinition = data2D.getTableData2DDefinition();
            tableData2DColumn = data2D.getTableData2DColumn();

            editController.setParameters(this);
            viewController.setParameters(this);
            attributesController.setParameters(this);
            columnsController.setParameters(this);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadFile(File file) {
        try {
            if (!checkBeforeNextAction()) {
                return;
            }
            data2D.initFile(file);
            data2D.setUserSavedDataDefinition(true);
            readDefinition();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void readDefinition() {
        if (data2D == null || !checkBeforeNextAction()) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                task.cancel();
            }
            isSettingValues = true;
            tableController.resetView();
            isSettingValues = false;
            task = new SingletonTask<Void>(this) {

                private StringTable validateTable;

                @Override
                protected boolean handle() {
                    File file = data2D.getFile();
                    if (file != null && !file.exists()) {
                        tableData2DDefinition.deleteData(data2D);
                        loadNull();
                        return false;
                    }
                    data2D.setTask(task);
                    long d2did = data2D.readDataDefinition();
                    boolean isTmpFile = data2D.isTmpFile();
                    List<String> names = data2D.readColumns();
                    if (isCancelled()) {
                        return false;
                    }
                    if (names == null || names.isEmpty()) {
                        data2D.setHasHeader(false);
                        tableData2DColumn.clearFile(data2D);
                        data2D.setColumns(null);
                    } else {
                        List<Data2DColumn> columns = new ArrayList<>();
                        List<Data2DColumn> savedColumns = data2D.getSavedColumns();
                        for (int i = 0; i < names.size(); i++) {
                            Data2DColumn column;
                            if (savedColumns != null && i < savedColumns.size()) {
                                column = savedColumns.get(i);
                                if (data2D.isHasHeader()) {
                                    column.setName(names.get(i));
                                }
                            } else {
                                column = new Data2DColumn(names.get(i), data2D.defaultColumnType());
                            }
                            column.setD2id(d2did);
                            column.setIndex(i);
                            columns.add(column);
                        }
                        data2D.setColumns(columns);
                        validateTable = Data2DColumn.validate(columns);
                        if (!isTmpFile) {
                            if (validateTable == null || validateTable.isEmpty()) {
                                tableData2DColumn.save(d2did, columns);
                            }
                        }
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (validateTable != null && !validateTable.isEmpty()) {
                        validateTable.htmlTable();
                    }
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
                    data2D.setTask(null);
                    task = null;
                    loadData();
                    notifyLoaded();
                }

            };
            start(task);
        }
    }

    public void loadData() {
        tableController.loadData();
        attributesController.loadData();
        columnsController.loadData();
    }

    public void loadNull() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                data2D.resetData();
                loadData();
                notifyLoaded();
            }
        });
    }

    public boolean isChanged() {
        return editController.isChanged()
                || attributesController.isChanged()
                || columnsController.isChanged();
    }

    public synchronized void checkStatus() {
        String title = message("Table");
        if (data2D.isTableChanged()) {
            title += "*";
        }
        editController.tableTab.setText(title);

        title = message("Text");
        if (textController.status == ControlData2DEditText.Status.Applied) {
            title += "*";
        } else if (textController.status == ControlData2DEditText.Status.Modified) {
            title += "**";
        }
        editController.textTab.setText(title);

        title = message("Edit");
        if (editController.isChanged()) {
            title += "*";
        }
        editTab.setText(title);

        title = message("Attributes");
        if (attributesController.status == ControlData2DAttributes.Status.Applied) {
            title += "*";
        } else if (attributesController.status == ControlData2DAttributes.Status.Modified) {
            title += "**";
        }
        attributesTab.setText(title);

        title = message("Columns");
        if (columnsController.status == ControlData2DColumns.Status.Applied) {
            title += "*";
        } else if (columnsController.status == ControlData2DColumns.Status.Modified) {
            title += "**";
        }
        columnsTab.setText(title);

        notifyStatus();
    }

    public void notifyStatus() {
        statusNotify.set(!statusNotify.get());
    }

    public void notifyLoaded() {
        if (backupController != null) {
            if (data2D.isTmpFile()) {
                backupController.loadBackups(null);
            } else {
                backupController.loadBackups(data2D.getFile());
            }
        }
        loadedNotify.set(!loadedNotify.get());
    }

    public void notifySaved() {
        savedNotify.set(!savedNotify.get());
    }

    public synchronized void resetStatus() {
        if (tableController.task != null) {
            tableController.task.cancel();
        }
        if (tableController.backgroundTask != null) {
            tableController.backgroundTask.cancel();
        }
        data2D.setTableChanged(false);

        if (textController.task != null) {
            textController.task.cancel();
        }
        if (textController.backgroundTask != null) {
            textController.backgroundTask.cancel();
        }
        textController.status = null;

        if (attributesController.task != null) {
            attributesController.task.cancel();
        }
        if (attributesController.backgroundTask != null) {
            attributesController.backgroundTask.cancel();
        }
        attributesController.status = null;

        if (columnsController.task != null) {
            columnsController.task.cancel();
        }
        if (columnsController.backgroundTask != null) {
            columnsController.backgroundTask.cancel();
        }
        columnsController.status = null;
    }

    public int checkBeforeSave() {
        if (attributesController.status == ControlData2DAttributes.Status.Modified
                || columnsController.status == ControlData2DColumns.Status.Modified
                || textController.status == ControlData2DEditText.Status.Modified) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setHeaderText(getMyStage().getTitle());
            alert.setContentText(Languages.message("DataModifiedNotApplied"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonApply = new ButtonType(Languages.message("ApplyModificationAndSave"));
            ButtonType buttonDiscard = new ButtonType(Languages.message("DiscardModificationAndSave"));
            ButtonType buttonCancel = new ButtonType(Languages.message("Cancel"));
            alert.getButtonTypes().setAll(buttonApply, buttonDiscard, buttonCancel);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonApply) {
                if (textController.status == ControlData2DEditText.Status.Modified) {
                    textController.okAction();
                    if (textController.status != ControlData2DEditText.Status.Applied) {
                        return -1;
                    }
                }
                if (attributesController.status == ControlData2DAttributes.Status.Modified) {
                    attributesController.okAction();
                    if (attributesController.status != ControlData2DAttributes.Status.Applied) {
                        return -1;
                    }
                }
                if (columnsController.status == ControlData2DColumns.Status.Modified) {
                    columnsController.okAction();
                    if (columnsController.status != ControlData2DColumns.Status.Applied) {
                        return -1;
                    }
                }
                return 1;
            } else if (result.get() == buttonDiscard) {
                return 2;
            } else {
                return -1;
            }
        } else {
            return 0;
        }
    }

    public void save() {
        if (!isChanged()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            if (checkBeforeSave() < 0) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    try {
                        backup();
                        data2D.setTask(task);
                        if (!data2D.savePageData(data2D)) {
                            return false;
                        }
                        saveDefinition(data2D, true);
                        return true;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    popInformation(message("Saved"));
                    if (data2D.getFile() != null) {
                        recordFileWritten(data2D.getFile());
                    }
                    tableController.afterSaved();
                    notifySaved();
                }

                @Override
                protected void finalAction() {
                    data2D.setTask(null);
                    task = null;
                }
            };
            start(task);
        }
    }

    public void backup() {
        if (backupController != null && backupController.isBack() && !data2D.isTmpFile()) {
            backupController.addBackup(data2D.getFile());
        }
    }

    @FXML
    public void saveAs(Data2D targetData) {
        saveAs(targetData, false);
    }

    public void saveAs(Data2D targetData, boolean load) {
        if (targetData == null) {
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
                    data2D.setTask(task);
                    if (!data2D.savePageData(targetData)) {
                        return false;
                    }
                    saveDefinition(targetData, load);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    popInformation(message("Saved"));
                    File file = targetData.getFile();
                    if (file != null) {
                        recordFileWritten(file);
                    }
                    if (load) {
                        data2D.load(targetData);
                        tableController.afterSaved();
                        notifySaved();
                        if (parentController instanceof ControlDataClipboard) {
                            ((ControlDataClipboard) parentController).refreshAction();
                        }
                    } else {
                        if (file != null) {
                            data2D.open(file);
                        }
                    }
                }

                @Override
                protected void finalAction() {
                    data2D.setTask(null);
                    task = null;
                }

            };
            start(task);
        }
    }

    public void saveDefinition(Data2D d, boolean load) {
        if (d == null || d.isTmpFile()) {
            return;
        }
        if (d.getD2did() >= 0
                && attributesController.status != ControlData2DAttributes.Status.Applied
                && columnsController.status != ControlData2DColumns.Status.Applied) {
            return;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            Data2DDefinition def;
            if (d.getD2did() < 0) {
                def = d.queryDefinition(conn);
                if (def != null) {
                    d.setD2did(def.getD2did());
                }
            }
            if (d.getD2did() < 0 || attributesController.status == ControlData2DAttributes.Status.Applied) {
                d.checkAttributes();
                if (d.getD2did() >= 0) {
                    def = tableData2DDefinition.updateData(conn, d);
                } else {
                    def = tableData2DDefinition.insertData(conn, d);
                }
                conn.commit();
                d.load(def);
                if (load) {
                    data2D.load(d);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            attributesController.loadData();
                        }
                    });
                }
            }
            long d2did = d.getD2did();
            if (d2did < 0) {
                return;
            }
            if (columnsController.status == ControlData2DColumns.Status.Applied) {
                tableData2DColumn.save(conn, d2did, d.getColumns());
                if (load) {
                    data2D.load(d);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            columnsController.loadData();
                        }
                    });
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void create() {
        try {
            if (!checkBeforeNextAction()) {
                return;
            }
            data2D.initFile(data2D.tmpFile());
            data2D.setHasHeader(true);
            data2D.setUserSavedDataDefinition(false);
            readDefinition();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void recover() {
        resetStatus();
        loadFile(data2D.getFile());
    }

    @FXML
    @Override
    public void myBoxClipBoard() {
        DataClipboardPopController.open(this);
    }

    @FXML
    @Override
    public void loadContentInSystemClipboard() {
        try {
            if (data2D == null || !checkBeforeNextAction()) {
                return;
            }
            String text = Clipboard.getSystemClipboard().getString();
            if (text == null || text.isBlank()) {
                popError(message("NoTextInClipboard"));
            }
            DataTextController.open(this, text);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        paigination
     */
    @FXML
    public void goPage() {
        tableController.goPage();
    }

    @FXML
    @Override
    public void pageNextAction() {
        tableController.pageNextAction();
    }

    @FXML
    @Override
    public void pagePreviousAction() {
        tableController.pagePreviousAction();
    }

    @FXML
    @Override
    public void pageFirstAction() {
        tableController.pageFirstAction();
    }

    @FXML
    @Override
    public void pageLastAction() {
        tableController.pageLastAction();
    }


    /*
        interface
     */
    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (!super.keyEventsFilter(event)) {
            if (editTab.isSelected()) {
                return editController.keyEventsFilter(event);

            } else if (viewTab.isSelected()) {
                return viewController.keyEventsFilter(event);

            } else if (attributesTab.isSelected()) {
                return attributesController.keyEventsFilter(event);

            } else if (columnsTab.isSelected()) {
                return columnsController.keyEventsFilter(event);

            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean checkBeforeNextAction() {
        boolean goOn;
        if (!isChanged()) {
            goOn = true;
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setHeaderText(getMyStage().getTitle());
            alert.setContentText(Languages.message("NeedSaveBeforeAction"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSave = new ButtonType(Languages.message("Save"));
            ButtonType buttonNotSave = new ButtonType(Languages.message("NotSave"));
            ButtonType buttonCancel = new ButtonType(Languages.message("Cancel"));
            alert.getButtonTypes().setAll(buttonSave, buttonNotSave, buttonCancel);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonSave) {
                parentController.saveAction();
                goOn = false;
            } else {
                goOn = result.get() == buttonNotSave;
            }
        }
        if (goOn) {
            resetStatus();
        }
        return goOn;
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
