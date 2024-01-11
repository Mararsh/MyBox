package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.Data2DExampleTools;
import mara.mybox.data2d.Data2DMenuTools;
import mara.mybox.data2d.DataClipboard;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.FileBackup;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public class ControlData2D extends BaseController {

    protected BaseData2DController manageController;
    protected Data2D.Type type;
    protected Data2D data2D;
    protected TableData2DDefinition tableData2DDefinition;
    protected TableData2DColumn tableData2DColumn;
    protected ControlData2DEditTable tableController;
    protected ControlData2DEditCSV csvController;
    protected final SimpleBooleanProperty statusNotify, loadedNotify, savedNotify;

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
    protected FlowPane paginationPane;
    @FXML
    protected ComboBox<String> pageSizeSelector, pageSelector;
    @FXML
    protected Label pageLabel, dataSizeLabel, selectedLabel;
    @FXML
    protected Button functionsButton;

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
            csvController = editController.csvController;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            StyleTools.setIconTooltips(functionsButton, "iconFunction.png", "");
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void setParameters(BaseData2DController topController) {
        try {
            this.manageController = topController;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        database
     */
    public void setDataType(BaseController parent, Data2D.Type type) {
        try {
            parentController = parent;
            if (parent != null) {
                saveButton = parent.saveButton;
                recoverButton = parent.recoverButton;
                baseTitle = parent.baseTitle;
                baseName = parent.baseName;
            }
            this.type = type;
            editController.setParameters(this);
            viewController.setParameters(this);
            attributesController.setParameters(this);
            columnsController.setParameters(this);

            loadNull();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setData(Data2D data) {
        try {
            if (data2D == null || data2D == data || data2D.getType() != data.getType()) {
                data2D = data;
            } else {
                data2D.resetData();
                data2D.cloneAll(data);
            }
            data2D.setLoadController(tableController);
            tableData2DDefinition = data2D.getTableData2DDefinition();
            tableData2DColumn = data2D.getTableData2DColumn();

            tableController.setData(data2D);
            editController.setData(data2D);
            viewController.setData(data2D);
            attributesController.setData(data2D);
            columnsController.setData(data2D);

            switch (data2D.getType()) {
                case CSV:
                case MyBoxClipboard:
                    setFileType(VisitHistory.FileType.CSV);
                    tableController.setFileType(VisitHistory.FileType.CSV);
                    break;
                case Excel:
                    setFileType(VisitHistory.FileType.Excel);
                    tableController.setFileType(VisitHistory.FileType.Excel);
                    break;
                case Texts:
                    setFileType(VisitHistory.FileType.Text);
                    tableController.setFileType(VisitHistory.FileType.Text);
                    break;
                default:
                    setFileType(VisitHistory.FileType.CSV);
                    tableController.setFileType(VisitHistory.FileType.CSV);
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void readDefinition() {
        tableController.readDefinition();
    }

    public void recover() {
        resetStatus();
        setData(tableController.data2D);
        if (data2D.isDataFile()) {
            data2D.initFile(data2D.getFile());
        }
        readDefinition();
    }

    /*
        file
     */
    @Override
    public void sourceFileChanged(File file) {
        try {
            if (!checkBeforeNextAction()) {
                return;
            }
            resetStatus();
            setData(Data2D.create(type));
            data2D.initFile(file);
            readDefinition();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        matrix
     */
    public void loadMatrix(double[][] matrix) {
        tableController.loadMatrix(matrix);
    }

    /*
        data
     */
    public void loadDef(Data2DDefinition def) {
        if (!checkBeforeNextAction()) {
            return;
        }
        resetStatus();
        if (def == null) {
            loadNull();
        } else {
            tableController.loadDef(def);
        }
    }

    public synchronized void loadData(Data2D data) {
        setData(data);
        tableController.loadData();
        attributesController.loadData();
        columnsController.loadData();
    }

    public void loadNull() {
        loadData(Data2D.create(type));
    }

    public boolean isChanged() {
        return editController.isChanged()
                || attributesController.isChanged()
                || columnsController.isChanged();
    }

    public void notifyStatus() {
        data2D = tableController.data2D;
        statusNotify.set(!statusNotify.get());
    }

    public void notifyLoaded() {
        notifyStatus();
        loadedNotify.set(!loadedNotify.get());
    }

    public void notifySaved() {
        notifyStatus();
        savedNotify.set(!savedNotify.get());
        if (manageController != null) {
            manageController.refreshAction();
        }
    }

    public synchronized void checkStatus() {
        data2D = tableController.data2D;
        String title = message("Table");
        if (data2D != null && data2D.isTableChanged()) {
            title += "*";
        }
        editController.tableTab.setText(title);

        title = "CSV";
        if (csvController.status == ControlData2DEditCSV.Status.Applied) {
            title += "*";
        } else if (csvController.status == ControlData2DEditCSV.Status.Modified) {
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

        if (recoverButton != null) {
            recoverButton.setDisable(data2D == null || data2D.isTmpData());
        }
        if (saveButton != null) {
            saveButton.setDisable(data2D == null || !tableController.dataSizeLoaded);
        }

        notifyStatus();
    }

    public synchronized void resetStatus() {
        if (task != null) {
            task.cancel();
        }
        if (backgroundTask != null) {
            backgroundTask.cancel();
        }

        tableController.resetStatus();

        if (csvController.task != null) {
            csvController.task.cancel();
        }
        if (csvController.backgroundTask != null) {
            csvController.backgroundTask.cancel();
        }
        csvController.status = null;

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

    public synchronized int checkBeforeSave() {
        setData(tableController.data2D);
        if (!tableController.dataSizeLoaded) {
            popError(message("CountingTotalNumber"));
            return -1;
        }
        if (attributesController.status == ControlData2DAttributes.Status.Modified
                || columnsController.status == ControlData2DColumns.Status.Modified
                || csvController.status == ControlData2DEditCSV.Status.Modified) {
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
            if (result == null || !result.isPresent()) {
                return -99;
            }
            if (result.get() == buttonApply) {
                if (csvController.status == ControlData2DEditCSV.Status.Modified) {
                    csvController.okAction();
                    if (csvController.status != ControlData2DEditCSV.Status.Applied) {
                        return -2;
                    }
                }
                if (attributesController.status == ControlData2DAttributes.Status.Modified) {
                    attributesController.okAction();
                    if (attributesController.status != ControlData2DAttributes.Status.Applied) {
                        return -3;
                    }
                }
                if (columnsController.status == ControlData2DColumns.Status.Modified) {
                    columnsController.okAction();
                    if (columnsController.status != ControlData2DColumns.Status.Applied) {
                        return -4;
                    }
                }
                return 1;
            } else if (result.get() == buttonDiscard) {
                return 2;
            } else {
                return -5;
            }
        } else {
            return 0;
        }
    }

    public synchronized void save() {
        setData(tableController.data2D);
        if (task != null && !task.isQuit()) {
            return;
        }
        if (!tableController.verifyData() || checkBeforeSave() < 0) {
            return;
        }
        if (manageController != null && manageController instanceof DataManufactureController) {
            DataManufactureSaveController.open(tableController);
            return;
        }
        if (data2D.isTable() && data2D.getSheet() == null) {
            Data2DTableCreateController.open(tableController);
            return;
        }
        Data2D targetData = data2D.cloneAll();
        if (targetData.isDataFile()) {
            if (targetData.getFile() == null) {
                File file = chooseSaveFile(targetData.dataName());
                if (file == null) {
                    return;
                }
                targetData.setFile(file);
            }
        } else if (targetData.isClipboard()) {
            if (targetData.getFile() == null) {
                File file = DataClipboard.newFile();
                if (file == null) {
                    return;
                }
                targetData.setFile(file);
            }
        }
        task = new FxSingletonTask<Void>(this) {

            private boolean needBackup = false;
            private FileBackup backup;

            @Override
            protected boolean handle() {
                try {
                    needBackup = data2D.isDataFile() && !data2D.isTmpData()
                            && UserConfig.getBoolean(baseName + "BackupWhenSave", true);
                    if (needBackup) {
                        backup = addBackup(this, data2D.getFile());
                    }
                    data2D.startTask(this, null);
                    data2D.savePageData(targetData);
                    data2D.startTask(this, null);
                    data2D.countSize();
                    Data2D.saveAttributes(data2D, targetData);
                    data2D.cloneAll(targetData);
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                tableController.dataSaved();
                if (needBackup) {
                    if (backup != null && backup.getBackup() != null) {
                        popInformation(message("SavedAndBacked"));
                        FileBackupController.updateList(sourceFile);
                    } else {
                        popError(message("FailBackup"));
                    }
                }
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.stopTask();
            }
        };
        start(task);
    }

    public synchronized void saveAs(Data2D targetData, SaveAsType saveAsType) {
        setData(tableController.data2D);
        if (targetData == null || targetData.getFile() == null) {
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    data2D.startTask(this, null);
                    data2D.savePageData(targetData);
                    data2D.startTask(this, null);
                    data2D.countSize();
                    Data2D.saveAttributes(data2D, targetData);
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                popInformation(message("Done"));
                if (targetData.getFile() != null) {
                    recordFileWritten(targetData.getFile());
                }
                if (saveAsType == SaveAsType.Load) {
                    data2D.cloneAll(targetData);
                    resetStatus();
                    readDefinition();
                } else if (saveAsType == SaveAsType.Open) {
                    Data2DDefinition.open(targetData);
                }
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.stopTask();
                targetData.stopTask();
            }
        };
        start(task);
    }

    public void renameAction(BaseTablePagesController parent, int index, Data2DDefinition targetData) {
        tableController.renameAction(parent, index, targetData);
    }

    @FXML
    @Override
    public void loadContentInSystemClipboard() {
        try {
            setData(tableController.data2D);
            if (data2D == null || !checkBeforeNextAction()) {
                return;
            }
            String text = Clipboard.getSystemClipboard().getString();
            if (text == null || text.isBlank()) {
                popError(message("NoTextInClipboard"));
            }
            Data2DLoadContentInSystemClipboardController.open(tableController, text);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void create() {
        try {
            if (!checkBeforeNextAction()) {
                return;
            }
            setData(Data2D.create(type));
            tableController.loadTmpData(null, data2D.tmpColumns(3), data2D.tmpData(3, 3));
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public synchronized void loadTmpData(String name, List<Data2DColumn> cols, List<List<String>> data) {
        try {
            if (!checkBeforeNextAction()) {
                return;
            }
            setData(Data2D.create(type));
            tableController.loadTmpData(name, cols, data);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadCSVFile(DataFileCSV csvData) {
        try {
            if (csvData == null) {
                popError("Nonexistent");
                return;
            }
            if (!checkBeforeNextAction()) {
                return;
            }
            setData(Data2D.create(type));
            tableController.loadCSVData(csvData);
        } catch (Exception e) {
            MyBoxLog.error(e);
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

    @FXML
    @Override
    public void refreshAction() {
        goPage();
    }


    /*
        interface
     */
    @FXML
    public void popFunctionsMenu(Event event) {
        if (UserConfig.getBoolean("Data2DFunctionsPopWhenMouseHovering", true)) {
            showFunctionsMenu(event);
        }
    }

    @FXML
    public void showFunctionsMenu(Event event) {
        try {
            setData(tableController.data2D);

            List<MenuItem> items = new ArrayList<>();

            items.add(Data2DMenuTools.dataMenu(this));

            items.add(new SeparatorMenuItem());

            items.add(Data2DMenuTools.modifyMenu(this));

            items.add(new SeparatorMenuItem());

            items.add(Data2DMenuTools.trimMenu(tableController));
            items.add(Data2DMenuTools.calMenu(tableController));
            items.add(Data2DMenuTools.chartsMenu(tableController));
            items.add(Data2DMenuTools.groupChartsMenu(tableController));

            items.add(new SeparatorMenuItem());

            if (data2D.isDataFile() || data2D.isUserTable() || data2D.isClipboard()) {
                Menu examplesMenu = new Menu(message("Examples"), StyleTools.getIconImageView("iconExamples.png"));
                examplesMenu.getItems().addAll(Data2DExampleTools.examplesMenu(this));
                items.add(examplesMenu);

            }

            Menu helpMenu = new Menu(message("Help"), StyleTools.getIconImageView("iconClaw.png"));
            helpMenu.getItems().addAll(Data2DMenuTools.helpMenus(tableController));
            items.add(helpMenu);

            items.add(new SeparatorMenuItem());

            CheckMenuItem focusMenu = new CheckMenuItem(message("CommitModificationWhenDataCellLoseFocus"),
                    StyleTools.getIconImageView("iconInput.png"));
            focusMenu.setSelected(AppVariables.commitModificationWhenDataCellLoseFocus);
            focusMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    AppVariables.lostFocusCommitData(focusMenu.isSelected());
                }
            });
            items.add(focusMenu);

            CheckMenuItem verifyMenu = new CheckMenuItem(message("VerifyDataWhenSave"),
                    StyleTools.getIconImageView("iconVerify.png"));
            verifyMenu.setSelected(UserConfig.getBoolean("Data2DVerifyDataWhenSave", false));
            verifyMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean("Data2DVerifyDataWhenSave", verifyMenu.isSelected());
                }
            });
            items.add(verifyMenu);

            items.add(new SeparatorMenuItem());

            CheckMenuItem popItem = new CheckMenuItem(message("PopMenuWhenMouseHovering"),
                    StyleTools.getIconImageView("iconPop.png"));
            popItem.setSelected(UserConfig.getBoolean("Data2DFunctionsPopWhenMouseHovering", true));
            popItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean("Data2DFunctionsPopWhenMouseHovering", popItem.isSelected());
                }
            });
            items.add(popItem);

            popEventMenu(event, items);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (super.keyEventsFilter(event)) {
            return true;
        }
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
    }

    @Override
    public boolean controlAltC() {
        if (targetIsTextInput()) {
            return false;
        }
        if (editTab.isSelected() && editController.tableTab.isSelected()) {
            tableController.copyToSystemClipboard();
            return true;
        }
        return false;
    }

    @Override
    public boolean controlAltV() {
        if (targetIsTextInput()) {
            return false;
        }
        if (editTab.isSelected() && editController.tableTab.isSelected()) {
            tableController.pasteContentInSystemClipboard();
            return true;

        }
        return false;
    }

    @FXML
    @Override
    public void myBoxClipBoard() {
        if (editTab.isSelected() && editController.tableTab.isSelected()) {
            Data2DPasteContentInMyBoxClipboardController.open(tableController);
        } else {
            TextInMyBoxClipboardController.oneOpen();
        }
    }

    @Override
    public boolean checkBeforeNextAction() {
        boolean goOn;
        if (!isChanged()) {
            goOn = true;
        } else {

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getTitle());
            alert.setHeaderText(getTitle());
            alert.setContentText(message("NeedSaveBeforeAction"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSave = new ButtonType(Languages.message("Save"));
            ButtonType buttonNotSave = new ButtonType(Languages.message("NotSave"));
            ButtonType buttonCancel = new ButtonType(Languages.message("Cancel"));
            alert.getButtonTypes().setAll(buttonSave, buttonNotSave, buttonCancel);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            Optional<ButtonType> result = alert.showAndWait();
            if (result == null || !result.isPresent()) {
                return false;
            }
            if (result.get() == buttonSave) {
                save();
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

    /*
        get
     */
    public Data2D getData2D() {
        return data2D;
    }

    public ControlData2DEditTable getTableController() {
        return tableController;
    }

}
