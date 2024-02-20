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
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import mara.mybox.data.StringTable;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.Data2DMenuTools;
import mara.mybox.data2d.DataClipboard;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.FileBackup;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public class ControlData2D extends ControlData2DLoad {

    protected BaseData2DController manageController;
    protected final SimpleBooleanProperty savedNotify;

    public ControlData2D() {
        readOnly = false;
        savedNotify = new SimpleBooleanProperty(false);
    }

    public void setParameters(BaseData2DController topController) {
        try {
            this.manageController = topController;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        data
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
            this.dataType = type;
            loadNull();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void sourceFileChanged(File file) {
        try {
            if (!checkBeforeNextAction()) {
                return;
            }
            resetStatus();
            setData(Data2D.create(dataType));
            data2D.initFile(file);
            readDefinition();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void notifySaved() {
        notifyStatus();
        savedNotify.set(!savedNotify.get());
        if (manageController != null) {
            manageController.refreshAction();
        }
    }

    @Override
    public boolean loadDef(Data2DDefinition def) {
        if (!super.loadDef(def)) {
            return false;
        }
        if (manageController != null) {
            manageController.refreshAction();
        }
        return true;
    }

    /*
        table
     */
    @Override
    public synchronized void tableChanged(boolean changed) {
        if (isSettingValues || data2D == null) {
            return;
        }
        data2D.setTableChanged(changed);
        validateData();
        if (data2D != null && data2D.isTableChanged()) {
            // #####
        }

        if (recoverButton != null) {
            recoverButton.setDisable(data2D == null || data2D.isTmpData());
        }
        if (saveButton != null) {
            saveButton.setDisable(data2D == null || !dataSizeLoaded);
        }

        notifyStatus();
    }

    @Override
    public boolean checkBeforeLoadingTableData() {
        return checkBeforeNextAction();
    }

    public boolean isChanged() {
        return data2D != null && data2D.isTableChanged();
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
                saveAction();
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
    public List<String> newData() {
        return data2D.newRow();
    }

    @Override
    public List<String> dataCopy(List<String> data) {
        return data2D.copyRow(data);
    }

    /*
        menus
     */
    @Override
    public List<MenuItem> operationsMenuItems(Event fevent) {
        return Data2DMenuTools.operateMenus(this);
    }

    @FXML
    public void popDataMenu(Event event) {
        if (UserConfig.getBoolean(baseName + "DataPopWhenMouseHovering", true)) {
            showDataMenu(event);
        }
    }

    @FXML
    public void showDataMenu(Event mevent) {
        try {
            List<MenuItem> items = Data2DMenuTools.dataMenus(this);
            items.add(new SeparatorMenuItem());

            CheckMenuItem popItem = new CheckMenuItem(message("PopMenuWhenMouseHovering"),
                    StyleTools.getIconImageView("iconPop.png"));
            popItem.setSelected(UserConfig.getBoolean(baseName + "DataPopWhenMouseHovering", true));
            popItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(baseName + "DataPopWhenMouseHovering", popItem.isSelected());
                }
            });
            items.add(popItem);

            popEventMenu(mevent, items);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void popTrimMenu(Event event) {
        if (UserConfig.getBoolean(baseName + "TrimPopWhenMouseHovering", true)) {
            showTrimMenu(event);
        }
    }

    @FXML
    public void showTrimMenu(Event event) {
        try {
            List<MenuItem> items = Data2DMenuTools.trimMenu(this);

            items.add(new SeparatorMenuItem());

            CheckMenuItem popItem = new CheckMenuItem(message("PopMenuWhenMouseHovering"),
                    StyleTools.getIconImageView("iconPop.png"));
            popItem.setSelected(UserConfig.getBoolean(baseName + "TrimPopWhenMouseHovering", true));
            popItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(baseName + "TrimPopWhenMouseHovering", popItem.isSelected());
                }
            });
            items.add(popItem);

            popEventMenu(event, items);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void popCalculateMenu(Event event) {
        if (UserConfig.getBoolean(baseName + "CalculatePopWhenMouseHovering", true)) {
            showCalculateMenu(event);
        }
    }

    @FXML
    public void showCalculateMenu(Event event) {
        try {
            List<MenuItem> items = Data2DMenuTools.calMenu(this);

            items.add(new SeparatorMenuItem());

            CheckMenuItem popItem = new CheckMenuItem(message("PopMenuWhenMouseHovering"),
                    StyleTools.getIconImageView("iconPop.png"));
            popItem.setSelected(UserConfig.getBoolean(baseName + "CalculatePopWhenMouseHovering", true));
            popItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(baseName + "CalculatePopWhenMouseHovering", popItem.isSelected());
                }
            });
            items.add(popItem);

            popEventMenu(event, items);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void popChartsMenu(Event event) {
        if (UserConfig.getBoolean(baseName + "ChartsPopWhenMouseHovering", true)) {
            showChartsMenu(event);
        }
    }

    @FXML
    public void showChartsMenu(Event event) {
        try {
            List<MenuItem> items = Data2DMenuTools.chartsMenu(this);

            items.add(new SeparatorMenuItem());

            items.addAll(Data2DMenuTools.groupChartsMenu(this));

            items.add(new SeparatorMenuItem());

            CheckMenuItem popItem = new CheckMenuItem(message("PopMenuWhenMouseHovering"),
                    StyleTools.getIconImageView("iconPop.png"));
            popItem.setSelected(UserConfig.getBoolean(baseName + "ChartsPopWhenMouseHovering", true));
            popItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(baseName + "ChartsPopWhenMouseHovering", popItem.isSelected());
                }
            });
            items.add(popItem);

            popEventMenu(event, items);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public List<MenuItem> functionsMenuItems(Event fevent) {
        return Data2DMenuTools.functionsMenus(this);
    }

    @Override
    public boolean controlAltN() {
        addRowsAction();
        return true;
    }

    @Override
    public boolean controlAltD() {
        deleteRowsAction();
        return true;
    }

    @Override
    public boolean controlAltL() {
        clearAction();
        return true;
    }

    /*
        action
     */
    @FXML
    @Override
    public synchronized void saveAction() {
        if (task != null && !task.isQuit()) {
            return;
        }
        if (!verifyData() || checkBeforeSave() < 0) {
            return;
        }
        if (manageController != null && manageController instanceof DataManufactureController) {
            DataManufactureSaveController.open(this);
            return;
        }
        if (data2D.isTable() && data2D.getSheet() == null) {
            Data2DTableCreateController.open(this);
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
                    data2D.savePageDataAs(targetData);
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
                dataSaved();
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

    public synchronized int checkBeforeSave() {
        if (!dataSizeLoaded) {
            popError(message("CountingTotalNumber"));
            return -1;
        }
        return 0;
    }

    public void dataSaved() {
        try {
            popInformation(message("Saved"));
            if (data2D.getFile() != null) {
                recordFileWritten(data2D.getFile());
            }
            notifySaved();
            readDefinition();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public synchronized void saveAsAction(Data2D targetData, SaveAsType saveAsType) {
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
                    data2D.savePageDataAs(targetData);
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

    @FXML
    @Override
    public void createAction() {
        try {
            if (!checkBeforeNextAction()) {
                return;
            }
            setData(Data2D.create(dataType));
            loadTmpData(null, data2D.tmpColumns(3), data2D.tmpData(3, 3));
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public synchronized void loadTmpData(String name, List<Data2DColumn> cols, List<List<String>> data) {
        try {
            if (!checkBeforeNextAction()) {
                return;
            }
            setData(Data2D.create(dataType));
            super.loadTmpData(name, cols, data);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void refreshAction() {
        goPage();
    }

    @FXML
    @Override
    public void recoverAction() {
        resetStatus();
        setData(data2D);
        if (data2D.isDataFile()) {
            data2D.initFile(data2D.getFile());
        }
        readDefinition();
    }

    @FXML
    @Override
    public void addAction() {
        if (!validateData()) {
            return;
        }
        addRowsAction();
    }

    @FXML
    @Override
    public void addRowsAction() {
        Data2DAddRowsController.open(this);
    }

    @FXML
    @Override
    public void editAction() {
        int index = selectedIndix();
        if (index < 0) {
            return;
        }
        Data2DRowEditController.open(this, index);
    }

    @FXML
    @Override
    public void deleteAction() {
        deleteRowsAction();
    }

    @FXML
    @Override
    public void clearAction() {
        if (data2D.isTmpData()) {
            deleteAllRows();
        } else {
            super.clearAction();
        }
    }

    @Override
    protected long clearData(FxTask currentTask) {
        return data2D.clearData();
    }

    public boolean verifyData() {
        if (!UserConfig.getBoolean("Data2DVerifyDataWhenSave", false)) {
            return true;
        }
        StringTable results = verifyResults();
        if (results.isEmpty()) {
            return true;
        }
        results.htmlTable();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(getMyStage().getTitle());
        alert.setHeaderText(getMyStage().getTitle());
        alert.setContentText(message("IgnoreInvalidAndSave"));
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        ButtonType buttonSave = new ButtonType(message("Save"));
        ButtonType buttonCancel = new ButtonType(message("Cancel"));
        alert.getButtonTypes().setAll(buttonSave, buttonCancel);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        stage.toFront();

        Optional<ButtonType> result = alert.showAndWait();
        return result != null && result.isPresent() && result.get() == buttonSave;
    }

    public void headerAction() {
        try {
            if (data2D == null || tableData.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            List<String> row = tableData.get(0);
            if (row == null || row.size() < 2) {
                popError(message("InvalidData"));
                return;
            }
            List<String> names = new ArrayList<>();
            for (int i = 1; i < row.size(); i++) {
                String name = row.get(i);
                if (name == null || name.isBlank()) {
                    name = message("Column") + i;
                }
                DerbyBase.checkIdentifier(names, name, true);
            }
//            dataController.columnsController.setNames(names); #########
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void showHelps(Event event) {
        List<MenuItem> items = Data2DMenuTools.helpMenus(this);

        items.add(new SeparatorMenuItem());

        CheckMenuItem hoverMenu = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
        hoverMenu.setSelected(UserConfig.getBoolean("Data2DHelpsPopWhenMouseHovering", false));
        hoverMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setBoolean("Data2DHelpsPopWhenMouseHovering", hoverMenu.isSelected());
            }
        });
        items.add(hoverMenu);

        popEventMenu(event, items);
    }

    @FXML
    public void popHelps(Event event) {
        if (UserConfig.getBoolean("Data2DHelpsPopWhenMouseHovering", false)) {
            showHelps(event);
        }
    }

}
