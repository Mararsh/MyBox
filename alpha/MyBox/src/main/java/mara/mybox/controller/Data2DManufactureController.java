package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Toggle;
import javafx.scene.input.Clipboard;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import mara.mybox.data.StringTable;
import mara.mybox.data2d.tools.Data2DMenuTools;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.Data2DDefinition.DataType;
import mara.mybox.db.data.FileBackup;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public class Data2DManufactureController extends BaseData2DViewController {

    protected final SimpleBooleanProperty savedNotify;
    protected boolean isCSVModified, isCSVpicked, askedTmp;

    @FXML
    protected FlowPane opsPane;
    @FXML
    protected Button dataDefinitionButton, operationButton, dataMenuButton,
            verifyButton, chartsButton, calculateButton, trimDataButton;

    public Data2DManufactureController() {
        baseTitle = message("DataManufacture");
        readOnly = false;
        savedNotify = new SimpleBooleanProperty(false);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            csvArea.focusedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    if (isSettingValues || nv) {
                        return;
                    }
                    isCSVpicked = false;
                    if (isCSVModified) {
                        return;
                    }
                    isCSVModified = true;
                    tableChanged();
                }
            });
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }


    /*
        format
     */
    @Override
    public void beforeOpenFile() {
        htmlRadio.setSelected(true);  // In case that file  has too many columns
    }

    @Override
    public void checkFormat(Toggle ov) {
        if (isSettingValues) {
            return;
        }
        if (!isValidData() || csvRadio != ov || !isCSVModified || isCSVpicked) {
            switchFormat();
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private List<List<String>> rows;

            @Override
            protected boolean handle() {
                rows = pickCSV(this);
                return true;
            }

            @Override
            protected void whenSucceeded() {
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                if (ok && rows != null) {
                    isSettingValues = true;
                    tableData.setAll(rows);
                    isSettingValues = false;
                    data2D.setPageData(tableData);
                    switchFormat();
                } else {
                    isSettingValues = true;
                    csvRadio.setSelected(true);
                    isSettingValues = false;
                    popError(message("InvalidData"));
                }
            }

        };
        start(task);
    }

    @Override
    public void switchFormat() {
        super.switchFormat();
        isCSVModified = false;
        isCSVpicked = false;
    }

    @Override
    public void showHtmlButtons() {
        buttonsPane.getChildren().setAll(formCheck, titleCheck, columnCheck, rowCheck,
                dataDefinitionButton, addRowsButton, recoverButton, saveButton,
                editHtmlButton);
        isSettingValues = true;
        formCheck.setSelected(UserConfig.getBoolean(baseName + "HtmlShowForm", false));
        columnCheck.setSelected(UserConfig.getBoolean(baseName + "HtmlShowColumns", true));
        rowCheck.setSelected(UserConfig.getBoolean(baseName + "HtmlShowRowNumber", true));
        titleCheck.setSelected(UserConfig.getBoolean(baseName + "HtmlShowTitle", true));
        isSettingValues = false;
    }

    @Override
    public void showTextsButtons() {
        buttonsPane.getChildren().setAll(formCheck, titleCheck, columnCheck, rowCheck,
                wrapCheck, delimiterButton,
                dataDefinitionButton, addRowsButton, recoverButton, saveButton);
        isSettingValues = true;
        formCheck.setSelected(UserConfig.getBoolean(baseName + "TextsShowForm", false));
        columnCheck.setSelected(UserConfig.getBoolean(baseName + "TextsShowColumns", true));
        rowCheck.setSelected(UserConfig.getBoolean(baseName + "TextsShowRowNumber", true));
        titleCheck.setSelected(UserConfig.getBoolean(baseName + "TextsShowTitle", true));
        isSettingValues = false;
    }

    @Override
    public void showTableButtons() {
        buttonsPane.getChildren().setAll(lostFocusCommitCheck, dataDefinitionButton,
                clearButton, deleteRowsButton, moveUpButton, moveDownButton,
                editButton, addRowsButton, recoverButton, saveButton);
    }

    @Override
    public void showCsv() {
        try {
            if (!csvRadio.isSelected()) {
                return;
            }
            buttonsPane.getChildren().addAll(wrapCheck, delimiterButton,
                    dataDefinitionButton, addRowsButton, recoverButton, saveButton);
            pageBox.getChildren().add(csvBox);
            VBox.setVgrow(csvBox, Priority.ALWAYS);

            delimiterName = UserConfig.getString(baseName + "CsvDelimiter", ",");
            isSettingValues = true;
            wrapCheck.setSelected(UserConfig.getBoolean(baseName + "CsvWrap", true));
            csvArea.setWrapText(wrapCheck.isSelected());
            columnsLabel.setWrapText(wrapCheck.isSelected());
            isSettingValues = false;

            loadCsv();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void loadCsv() {
        if (!csvRadio.isSelected()) {
            return;
        }
        isCSVModified = false;
        isCSVpicked = false;
        if (data2D == null || !data2D.hasPageData()) {
            isSettingValues = true;
            csvArea.setText("");
            columnsLabel.setText("");
            isSettingValues = false;
            return;
        }
        if (loadTask != null) {
            loadTask.cancel();
        }
        loadTask = new FxSingletonTask<Void>(this) {
            private String text;

            @Override
            protected boolean handle() {
                text = data2D.encodeCSV(this, delimiterName, false, false);
                return text != null;
            }

            @Override
            protected void whenCanceled() {
            }

            @Override
            protected void whenFailed() {
            }

            @Override
            protected void whenSucceeded() {
                isSettingValues = true;
                csvArea.setText(text);
                String label = "";
                String delimiter = TextTools.delimiterValue(delimiterName);
                for (String name : data2D.columnNames()) {
                    if (!label.isEmpty()) {
                        label += delimiter;
                    }
                    label += name;
                }
                columnsLabel.setText(label);
                isSettingValues = false;

            }

        };
        start(loadTask, false);
    }

    public List<List<String>> pickCSV(FxTask task) {
        try {
            if (delimiterName == null) {
                delimiterName = ",";
            }
            List<List<String>> rows = new ArrayList<>();
            String text = csvArea.getText();
            if (text != null && !text.isBlank()) {
                int colsNumber = data2D.columnsNumber();
                List<List<String>> data = data2D.decodeCSV(task, text, delimiterName, false);
                if (data == null) {
                    return null;
                }
                long startindex = data2D.getStartRowOfCurrentPage();
                for (int i = 0; i < data.size(); i++) {
                    List<String> drow = data.get(i);
                    List<String> nrow = new ArrayList<>();
                    nrow.add((startindex + i) + "");
                    int len = drow.size();
                    if (len > colsNumber) {
                        nrow.addAll(drow.subList(0, colsNumber));
                    } else {
                        nrow.addAll(drow);
                        for (int c = len; c < colsNumber; c++) {
                            nrow.add(null);
                        }
                    }
                    rows.add(nrow);
                }
            }
            return rows;
        } catch (Exception e) {
            displayError(e.toString());
            return null;
        }
    }

    /*
        status
     */
    @Override
    public void updateStatus() {
        try {
            super.updateStatus();

            boolean invalidData = !isValidData();
            mainAreaBox.setDisable(invalidData);
            opsPane.setDisable(invalidData);
            recoverButton.setDisable(invalidData || !dataSizeLoaded
                    || data2D.isTmpData() || !data2D.isTableChanged());
            saveButton.setDisable(invalidData || !dataSizeLoaded);
            dataDefinitionButton.setDisable(invalidData);
            paginationPane.setVisible(dataSizeLoaded);
            if (data2D != null && data2D.isDataFile() && data2D.getFile() != null) {
                if (!toolbar.getChildren().contains(fileMenuButton)) {
                    toolbar.getChildren().add(0, fileMenuButton);
                }
            } else {
                if (toolbar.getChildren().contains(fileMenuButton)) {
                    toolbar.getChildren().remove(fileMenuButton);
                }
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void notifySaved() {
        savedNotify.set(!savedNotify.get());
    }

    @Override
    public boolean isValidPageData() {
        if (!super.isValidPageData()) {
            return false;
        }
        if (csvRadio.isSelected() && isCSVModified && !isCSVpicked) {
            List<List<String>> rows = pickCSV(null);
            if (rows == null) {
                return false;
            }
            super.updateTable(rows);
            isCSVpicked = true;
        }
        return true;
    }

    public boolean isTableMode() {
        return tableRadio.isSelected();
    }

    public boolean isDataChanged() {
        return data2D != null && data2D.isTableChanged();
    }

    /*
        table
     */
    @Override
    public boolean checkBeforeNextAction() {
        boolean goOn;
        if (!isDataChanged()) {
            goOn = true;
        } else {
            if (data2D != null && data2D.isTmpFile()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle(getTitle());
                alert.setHeaderText(getTitle());
                alert.setContentText(message("NeedSaveBeforeAction") + "\n"
                        + message("Data2DTmpFileNotice"));
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                ButtonType buttonSave = new ButtonType(message("Save"));
                ButtonType buttonSaveAs = new ButtonType(message("SaveAs"));
                ButtonType buttonNotSave = new ButtonType(message("NotSave"));
                ButtonType buttonCancel = new ButtonType(message("Cancel"));
                alert.getButtonTypes().setAll(buttonSaveAs, buttonSave, buttonNotSave, buttonCancel);
                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                stage.setAlwaysOnTop(true);
                stage.toFront();

                Optional<ButtonType> result = alert.showAndWait();
                if (result == null || !result.isPresent()) {
                    return false;
                }
                if (result.get() == buttonSaveAs) {
                    saveAsAction();
                    goOn = false;
                } else if (result.get() == buttonSave) {
                    saveAction();
                    goOn = false;
                } else {
                    goOn = result.get() == buttonNotSave;
                }

            } else {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle(getTitle());
                alert.setHeaderText(getTitle());
                alert.setContentText(message("NeedSaveBeforeAction"));
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                ButtonType buttonSave = new ButtonType(message("Save"));
                ButtonType buttonNotSave = new ButtonType(message("NotSave"));
                ButtonType buttonCancel = new ButtonType(message("Cancel"));
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

        }
        if (goOn) {
            resetStatus();
        }
        return goOn;
    }

    @Override
    public boolean leavingScene() {
        if (data2D != null && data2D.isTmpFile()
                && !isDataChanged()
                && !askedTmp
                && UserConfig.getBoolean("Data2DPromptTemporaryWhenClose", true)) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getTitle());
            alert.setHeaderText(getTitle());
            alert.setContentText(message("Data2DTmpFileNotice"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSaveAs = new ButtonType(message("SaveAs"));
            ButtonType buttonNotSave = new ButtonType(message("NotSave"));
            ButtonType buttonCancel = new ButtonType(message("Cancel"));
            alert.getButtonTypes().setAll(buttonSaveAs, buttonNotSave, buttonCancel);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            Optional<ButtonType> result = alert.showAndWait();
            if (result == null || !result.isPresent()) {
                return false;
            }
            if (result.get() == buttonSaveAs) {
                askedTmp = true;
                saveAsAction();
                return false;
            } else if (result.get() != buttonNotSave) {
                return false;
            }
        }
        askedTmp = super.leavingScene();
        return askedTmp;
    }

    @Override
    public List<String> newData() {
        if (!isValidData()) {
            return null;
        }
        return data2D.newRow();
    }

    @Override
    public List<String> dataCopy(List<String> data) {
        if (!isValidData()) {
            return null;
        }
        return data2D.copyRow(data);
    }

    /*
        menus
     */
    @Override
    public List<MenuItem> fileMenuItems(Event fevent) {
        return Data2DMenuTools.fileMenus(this);
    }

    @Override
    public List<MenuItem> dataMenuItems(Event fevent) {
        return Data2DMenuTools.dataMenus(this);
    }

    @FXML
    public void popVerifyMenu(Event event) {
        if (UserConfig.getBoolean(baseName + "VerifyPopWhenMouseHovering", true)) {
            showVerifyMenu(event);
        }
    }

    @FXML
    public void showVerifyMenu(Event mevent) {
        try {
            List<MenuItem> items = new ArrayList<>();
            items.addAll(Data2DMenuTools.verifyMenus(this));

            items.add(new SeparatorMenuItem());

            CheckMenuItem popItem = new CheckMenuItem(message("PopMenuWhenMouseHovering"),
                    StyleTools.getIconImageView("iconPop.png"));
            popItem.setSelected(UserConfig.getBoolean(baseName + "VerifyPopWhenMouseHovering", true));
            popItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(baseName + "VerifyPopWhenMouseHovering", popItem.isSelected());
                }
            });
            items.add(popItem);

            popEventMenu(mevent, items);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public List<MenuItem> operationsMenuItems(Event fevent) {
        return Data2DMenuTools.operationsMenus(this);
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
            List<MenuItem> items = Data2DMenuTools.trimMenus(this);

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
            List<MenuItem> items = Data2DMenuTools.calMenus(this);

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
            List<MenuItem> items = Data2DMenuTools.chartMenus(this);

            items.add(new SeparatorMenuItem());

            items.addAll(Data2DMenuTools.groupChartMenus(this));

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

    /*
        action
     */
    @FXML
    public void popCreateMenu(Event event) {
        if (UserConfig.getBoolean(baseName + "CreateMenuPopWhenMouseHovering", true)) {
            showCreateMenu(event);
        }
    }

    @FXML
    public void showCreateMenu(Event fevent) {
        try {
            List<MenuItem> items = Data2DMenuTools.createMenus(baseName);
            popEventMenu(fevent, items);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean createData(DataType type) {
        if (!super.createData(type)) {
            return false;
        }
        Data2DAttributesController.open(this);
        return true;
    }

    @Override
    public boolean controlAltN() {
        if (isValidData()) {
            addRowsAction();
            return true;
        }
        return false;
    }

    @Override
    public boolean controlAltD() {
        if (targetIsTextInput()) {
            return false;
        }
        if (isValidData() && isTableMode()) {
            deleteRowsAction();
            return true;
        }
        return false;
    }

    @Override
    public boolean controlAltL() {
        if (isValidData()) {
            clearAction();
        }
        return false;
    }

    @FXML
    public void definitonAction() {
        if (!isValidPageData()) {
            popError(message("InvalidData"));
            return;
        }
        Data2DAttributesController.open(this);
    }

    @FXML
    @Override
    public void saveAction() {
        if (!dataSizeLoaded) {
            popError(message("CountingTotalNumber"));
            return;
        }
        if (!isValidPageData()) {
            popError(message("InvalidData"));
            return;
        }
        if (!verifyData()) {
            return;
        }
        if (data2D.isTmpData()) {
            if (data2D.isTable()) {
                Data2DTableCreateController.open(this);
            } else {
                Data2DSaveAsController.save(this);
            }
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            private boolean needBackup = false;
            private FileBackup backup;

            @Override
            protected boolean handle() {
                try {
                    needBackup = data2D.needBackup();
                    if (needBackup) {
                        backup = addBackup(this, data2D.getFile());
                    }
                    data2D.startTask(this, null);
                    dataSize = data2D.savePageData(this);
                    return dataSize >= 0;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                popInformation(message("Saved"));
                notifySaved();
                loadPage(false);
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

    @FXML
    @Override
    public void refreshAction() {
        goPage();
    }

    @FXML
    @Override
    public void recoverAction() {
        if (!isValidData() || !data2D.isTableChanged()) {
            return;
        }
        if (data2D.isMutiplePages()) {
            loadPage(false);
        } else {
            loadDef(data2D, false);
        }
    }

    @FXML
    @Override
    public void addAction() {
        addRowsAction();
    }

    @FXML
    @Override
    public void addRowsAction() {
        if (!isValidData()) {
            popError(message("InvalidData"));
            return;
        }
        Data2DAddRowsController.open(this);
    }

    @Override
    public int addRows(int index, List<List<String>> list) {
        if (list == null || list.isEmpty()) {
            return -1;
        }
        if (index < 0) {
            index = tableData.size();
        }
        isSettingValues = true;
        tableData.addAll(index, list);
        isSettingValues = false;
        if (tableRadio.isSelected()) {
            tableView.scrollTo(index - 5);
        } else {
            switchFormat();
        }
        tableChanged(true);
        return list.size();
    }

    @FXML
    @Override
    public void editAction() {
        if (tableRadio.isSelected()) {
            int index = selectedIndix();
            if (index < 0) {
                return;
            }
            Data2DRowEditController.open(this, index);
        }
    }

    @FXML
    @Override
    public void deleteAction() {
        deleteRowsAction();
    }

    @FXML
    @Override
    public void clearAction() {
        if (!isValidData()) {
            popError(message("InvalidData"));
            return;
        }
        if (data2D.isTmpData()) {
            deleteAllRows();
        } else {
            super.clearAction();
        }
    }

    @Override
    protected long clearData(FxTask currentTask) {
        if (!isValidData()) {
            return 0;
        }
        return data2D.clearData(currentTask);
    }

    @FXML
    @Override
    public void pasteContentInSystemClipboard() {
        try {
            if (!isValidData()) {
                popError(message("InvalidData"));
                return;
            }
            String text = Clipboard.getSystemClipboard().getString();
            if (text == null || text.isBlank()) {
                popError(message("NoTextInClipboard"));
            }
            Data2DPasteContentInSystemClipboardController.open(this, text);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void pasteContentInMyboxClipboard() {
        try {
            if (!isValidData()) {
                popError(message("InvalidData"));
                return;
            }
            Data2DPasteContentInMyBoxClipboardController.open(this);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void myBoxClipBoard() {
        if (!isValidData()) {
            popError(message("InvalidData"));
            return;
        }
        Data2DPasteContentInMyBoxClipboardController.open(this);
    }

    public boolean verifyData() {
        if (!data2D.validateSave()) {
            return true;
        }
        StringTable results = verifyTableData();
        if (results == null) {
            return false;
        }
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

    @FXML
    @Override
    public void copyAction() {
        if (!isValidPageData()) {
            popError(message("InvalidData"));
            return;
        }
        Data2DCopyController.open(this);
    }

    @FXML
    public void exportAction() {
        if (!isValidPageData()) {
            popError(message("InvalidData"));
            return;
        }
        Data2DExportController.open(this);
    }

    @FXML
    public void setValue() {
        if (!isValidPageData()) {
            popError(message("InvalidData"));
            return;
        }
        Data2DSetValuesController.open(this);
    }

    @FXML
    public void delete() {
        if (!isValidPageData()) {
            popError(message("InvalidData"));
            return;
        }
        Data2DDeleteController.open(this);
    }

    @FXML
    public void setStyles() {
        if (!isValidPageData()) {
            popError(message("InvalidData"));
            return;
        }
        Data2DSetStylesController.open(this);
    }

    /*
        static
     */
    public static Data2DManufactureController open() {
        try {
            Data2DManufactureController controller
                    = (Data2DManufactureController) WindowTools.openStage(Fxmls.Data2DManufactureFxml);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static Data2DManufactureController openDef(Data2DDefinition def) {
        try {
            Data2DManufactureController controller = open();
            controller.loadDef(def);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static Data2DManufactureController openData(List<Data2DColumn> columns, List<List<String>> data) {
        return openData(null, columns, data);
    }

    public static Data2DManufactureController openData(String name, List<Data2DColumn> cols, List<List<String>> data) {
        Data2DManufactureController controller = open();
        controller.loadData(name, cols, data);
        controller.requestMouse();
        return controller;
    }

    public static Data2DManufactureController openType(DataType type,
            String name, List<Data2DColumn> cols, List<List<String>> data) {
        Data2DManufactureController controller = open();
        controller.loadData(name, cols, data);
        controller.requestMouse();
        return controller;
    }

    public static Data2DManufactureController loadTables(String prefix, List<StringTable> tables) {
        Data2DManufactureController controller = open();
        controller.loadTableData(prefix, tables);
        controller.requestMouse();
        return controller;
    }

    public static Data2DManufactureController openCSVFile(File file, Charset charset, boolean withNames, String delimiter) {
        try {
            Data2DManufactureController controller = open();
            controller.loadCSVFile(file, charset, withNames, delimiter);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static Data2DManufactureController openCSVFile(File file) {
        return openCSVFile(file, Charset.forName("UTF-8"), true, ",");
    }

    public static Data2DManufactureController openExcelFile(File file, String sheet, boolean withNames) {
        try {
            Data2DManufactureController controller = open();
            controller.loadExcelFile(file, sheet, withNames);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static Data2DManufactureController openTextFile(File file, Charset charset, boolean withNames, String delimiter) {
        try {
            Data2DManufactureController controller = open();
            controller.loadTextFile(file, charset, withNames, delimiter);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static Data2DManufactureController create(DataType type) {
        try {
            Data2DManufactureController controller = Data2DManufactureController.open();
            controller.createData(type);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
