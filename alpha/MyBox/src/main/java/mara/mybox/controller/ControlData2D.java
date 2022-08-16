package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataClipboard;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.fxml.style.StyleTools;
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
    protected ControlData2DEditText textController;
    protected final SimpleBooleanProperty statusNotify, loadedNotify, savedNotify;
    protected ControlFileBackup backupController;

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
            textController = editController.textController;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            StyleTools.setIconTooltips(functionsButton, "iconFunction.png", "");
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void setParameters(BaseData2DController topController) {
        try {
            this.manageController = topController;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
            MyBoxLog.error(e.toString());
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
            MyBoxLog.error(e.toString());
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
            MyBoxLog.error(e.toString());
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
        if (backupController != null) {
            if (data2D.isTmpData()) {
                backupController.loadBackups(null);
            } else {
                backupController.loadBackups(data2D.getFile());
            }
        }
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

    public synchronized int checkBeforeSave() {
        setData(tableController.data2D);
        if (!tableController.dataSizeLoaded) {
            popError(message("CountingTotalNumber"));
            return -1;
        }
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
            if (result == null || !result.isPresent()) {
                return -99;
            }
            if (result.get() == buttonApply) {
                if (textController.status == ControlData2DEditText.Status.Modified) {
                    textController.okAction();
                    if (textController.status != ControlData2DEditText.Status.Applied) {
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
        if (checkBeforeSave() < 0) {
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
                File file = chooseSaveFile();
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
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    if (backupController != null && backupController.isBack() && !data2D.isTmpData()) {
                        backupController.addBackup(task, data2D.getFile());
                    }
                    data2D.startTask(task, null);
                    data2D.savePageData(targetData);
                    data2D.startTask(task, null);
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
            }

            @Override
            protected void finalAction() {
                data2D.stopTask();
                task = null;

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
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    data2D.startTask(task, null);
                    data2D.savePageData(targetData);
                    data2D.startTask(task, null);
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
                data2D.stopTask();
                targetData.stopTask();
                task = null;
            }
        };
        start(task);
    }

    public void renameAction(BaseTableViewController parent, int index, Data2DDefinition targetData) {
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
            MyBoxLog.error(e.toString());
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
            MyBoxLog.error(e.toString());
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
            MyBoxLog.error(e.toString());
        }
    }

    public void loadCSVFile(File csvFile, List<ColumnType> columnTypes) {
        try {
            if (csvFile == null || !csvFile.exists()) {
                popError("Nonexistent");
                return;
            }
            if (!checkBeforeNextAction()) {
                return;
            }
            setData(Data2D.create(type));
            DataFileCSV csvData = new DataFileCSV(csvFile);
            csvData.setInitColumnTypes(columnTypes);
            tableController.loadCSVData(csvData);
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

    @FXML
    public void refreshAction() {
        goPage();
    }


    /*
        interface
     */
    @FXML
    public void popFunctionsMenu(MouseEvent mouseEvent) {
        if (UserConfig.getBoolean(interfaceName + "FunctionsPopWhenMouseHovering", true)) {
            functionsMenu(mouseEvent);
        }
    }

    @FXML
    public void showFunctionsMenu(ActionEvent event) {
        functionsMenu(event);
    }

    public void functionsMenu(Event menuEvent) {
        try {
            setData(tableController.data2D);

            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            boolean invalidData = data2D == null || !data2D.isValid();
            boolean empty = invalidData || tableController.tableData.isEmpty();
            MenuItem menu;

            popMenu.getItems().add(new SeparatorMenuItem());

            Menu modifyMenu = new Menu(message("Modify"), StyleTools.getIconImage("iconEdit.png"));
            popMenu.getItems().add(modifyMenu);

            menu = new MenuItem(message("SetValues"), StyleTools.getIconImage("iconEqual.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DSetValuesController.open(tableController);
            });
            menu.setDisable(empty);
            modifyMenu.getItems().add(menu);

            menu = new MenuItem(message("Delete"), StyleTools.getIconImage("iconDelete.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DDeleteController.open(tableController);
            });
            menu.setDisable(empty);
            modifyMenu.getItems().add(menu);

            menu = new MenuItem(message("SetStyles"), StyleTools.getIconImage("iconColor.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DSetStylesController.open(tableController);
            });
            menu.setDisable(empty || data2D.isTmpData());
            modifyMenu.getItems().add(menu);

            menu = new MenuItem(message("PasteContentInSystemClipboard"), StyleTools.getIconImage("iconPasteSystem.png"));
            menu.setOnAction((ActionEvent event) -> {
                tableController.pasteContentInSystemClipboard();
            });
            menu.setDisable(invalidData);
            modifyMenu.getItems().add(menu);

            menu = new MenuItem(message("PasteContentInMyBoxClipboard"), StyleTools.getIconImage("iconPaste.png"));
            menu.setOnAction((ActionEvent event) -> {
                tableController.pasteContentInMyboxClipboard();
            });
            menu.setDisable(invalidData);
            modifyMenu.getItems().add(menu);

            modifyMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(message("Save"), StyleTools.getIconImage("iconSave.png"));
            menu.setOnAction((ActionEvent event) -> {
                save();
            });
            menu.setDisable(invalidData || !tableController.dataSizeLoaded);
            modifyMenu.getItems().add(menu);

            menu = new MenuItem(message("Recover"), StyleTools.getIconImage("iconRecover.png"));
            menu.setOnAction((ActionEvent event) -> {
                recover();
            });
            menu.setDisable(invalidData || data2D.isTmpData());
            modifyMenu.getItems().add(menu);

            menu = new MenuItem(message("Refresh"), StyleTools.getIconImage("iconRefresh.png"));
            menu.setOnAction((ActionEvent event) -> {
                refreshAction();
            });
            menu.setDisable(invalidData || data2D.isTmpData());
            modifyMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());

            Menu trimMenu = new Menu(message("Trim"), StyleTools.getIconImage("iconClean.png"));
            popMenu.getItems().add(trimMenu);

            if (data2D.isTable()) {
                menu = new MenuItem(message("Query"), StyleTools.getIconImage("iconQuery.png"));
                menu.setOnAction((ActionEvent event) -> {
                    DataTableQueryController.open(tableController);
                });
                menu.setDisable(empty);
                trimMenu.getItems().add(menu);
            }

            menu = new MenuItem(message("CopyFilterQuery"), StyleTools.getIconImage("iconCopy.png"));
            menu.setOnAction((ActionEvent event) -> {
                tableController.copyAction();
            });
            menu.setDisable(empty);
            trimMenu.getItems().add(menu);

            menu = new MenuItem(message("Sort"), StyleTools.getIconImage("iconSort.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DSortController.open(tableController);
            });
            menu.setDisable(empty);
            trimMenu.getItems().add(menu);

            menu = new MenuItem(message("Transpose"), StyleTools.getIconImage("iconRotateRight.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DTransposeController.open(tableController);
            });
            menu.setDisable(empty);
            trimMenu.getItems().add(menu);

            menu = new MenuItem(message("Normalize"), StyleTools.getIconImage("iconBinary.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DNormalizeController.open(tableController);
            });
            menu.setDisable(empty);
            trimMenu.getItems().add(menu);

            Menu calMenu = new Menu(message("Calculation"), StyleTools.getIconImage("iconCalculator.png"));
            popMenu.getItems().add(calMenu);

            menu = new MenuItem(message("RowExpression"), StyleTools.getIconImage("iconCalculate.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DRowExpressionController.open(tableController);
            });
            menu.setDisable(empty);
            calMenu.getItems().add(menu);

            menu = new MenuItem(message("DescriptiveStatistics"), StyleTools.getIconImage("iconStatistic.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DStatisticController.open(tableController);
            });
            menu.setDisable(empty);
            calMenu.getItems().add(menu);

            menu = new MenuItem(message("GroupByValues"), StyleTools.getIconImage("iconAnalyse.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DGroupValuesController.open(tableController);
            });
            menu.setDisable(empty);
            calMenu.getItems().add(menu);

            menu = new MenuItem(message("SimpleLinearRegressionCombination"), StyleTools.getIconImage("iconLinearPgression.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DSimpleLinearRegressionCombinationController.open(tableController);
            });
            menu.setDisable(empty);
            calMenu.getItems().add(menu);

            menu = new MenuItem(message("SimpleLinearRegression"), StyleTools.getIconImage("iconLinearPgression.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DSimpleLinearRegressionController.open(tableController);
            });
            menu.setDisable(empty);
            calMenu.getItems().add(menu);

            menu = new MenuItem(message("MultipleLinearRegression"), StyleTools.getIconImage("iconLinearPgression.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DMultipleLinearRegressionController.open(tableController);
            });
            menu.setDisable(empty);
            calMenu.getItems().add(menu);

            menu = new MenuItem(message("FrequencyDistributions"), StyleTools.getIconImage("iconDistribution.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DFrequencyController.open(tableController);
            });
            menu.setDisable(empty);
            calMenu.getItems().add(menu);

            menu = new MenuItem(message("ValuePercentage"), StyleTools.getIconImage("iconPercentage.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DPercentageController.open(tableController);
            });
            menu.setDisable(empty);
            calMenu.getItems().add(menu);

            Menu chartMenu = new Menu(message("Charts"), StyleTools.getIconImage("iconGraph.png"));
            popMenu.getItems().add(chartMenu);

            menu = new MenuItem(message("XYChart"), StyleTools.getIconImage("iconXYChart.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DChartXYController.open(tableController);
            });
            menu.setDisable(empty);
            chartMenu.getItems().add(menu);

            menu = new MenuItem(message("PieChart"), StyleTools.getIconImage("iconPieChart.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DChartPieController.open(tableController);
            });
            menu.setDisable(empty);
            chartMenu.getItems().add(menu);

            menu = new MenuItem(message("BoxWhiskerChart"), StyleTools.getIconImage("iconBoxWhiskerChart.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DChartBoxWhiskerController.open(tableController);
            });
            menu.setDisable(empty);
            chartMenu.getItems().add(menu);

            menu = new MenuItem(message("SelfComparisonBarsChart"), StyleTools.getIconImage("iconBarChartH.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DChartSelfComparisonBarsController.open(tableController);
            });
            menu.setDisable(empty);
            chartMenu.getItems().add(menu);

            menu = new MenuItem(message("ComparisonBarsChart"), StyleTools.getIconImage("iconComparisonBarsChart.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DChartComparisonBarsController.open(tableController);
            });
            menu.setDisable(empty);
            chartMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(message("Export"), StyleTools.getIconImage("iconExport.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DExportController.open(tableController);
            });
            menu.setDisable(empty);
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("ConvertToDatabaseTable"), StyleTools.getIconImage("iconDatabase.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DConvertToDataBaseController.open(tableController);
            });
            menu.setDisable(invalidData);
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());

            if (data2D.isDataFile()) {
                menu = new MenuItem(message("Open"), StyleTools.getIconImage("iconOpen.png"));
                menu.setOnAction((ActionEvent event) -> {
                    selectSourceFile();
                });
                popMenu.getItems().add(menu);
            }

            menu = new MenuItem(message("CreateData"), StyleTools.getIconImage("iconAdd.png"));
            menu.setOnAction((ActionEvent event) -> {
                create();
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("LoadContentInSystemClipboard"), StyleTools.getIconImage("iconImageSystem.png"));
            menu.setOnAction((ActionEvent event) -> {
                loadContentInSystemClipboard();
            });
            popMenu.getItems().add(menu);

            if (data2D.isDataFile() || data2D.isUserTable() || data2D.isClipboard()) {
                Menu examplesMenu = new Menu(message("Examples"), StyleTools.getIconImage("iconExamples.png"));
                examplesMenu.getItems().addAll(examplesMenu());
                popMenu.getItems().add(examplesMenu);
            }

            CheckMenuItem passPop = new CheckMenuItem(message("PopWhenMouseHovering"));
            passPop.setSelected(UserConfig.getBoolean(interfaceName + "FunctionsPopWhenMouseHovering", true));
            passPop.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(interfaceName + "FunctionsPopWhenMouseHovering", passPop.isSelected());
                }
            });
            popMenu.getItems().add(passPop);

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("PopupClose"), StyleTools.getIconImage("iconCancel.png"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            LocateTools.locateBelow((Region) menuEvent.getSource(), popMenu);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public List<MenuItem> examplesMenu() {
        try {
            List<MenuItem> items = new ArrayList<>();

            MenuItem menu;
            String lang = Languages.isChinese() ? "zh" : "en";

            // https://data.stats.gov.cn/index.htm
            Menu chinaMenu = new Menu(message("StatisticDataOfChina"), StyleTools.getIconImage("iconChina.png"));
            items.add(chinaMenu);

            menu = new MenuItem(message("ChinaPopulation"));
            menu.setOnAction((ActionEvent event) -> {
                File file = FxFileTools.getInternalFile("/data/examples/ChinaPopulation_" + lang + ".csv",
                        "data", "ChinaPopulation_" + lang + ".csv", true);
                List<ColumnType> columnTypes = Arrays.asList(
                        ColumnType.Integer, ColumnType.Double, ColumnType.Double,
                        ColumnType.Double, ColumnType.Double, ColumnType.Double
                );
                loadCSVFile(file, columnTypes);
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message("ChinaCensus"));
            menu.setOnAction((ActionEvent event) -> {
                File file = FxFileTools.getInternalFile("/data/examples/ChinaCensus_" + lang + ".csv",
                        "data", "ChinaCensus_" + lang + ".csv", true);
                List<ColumnType> columnTypes = Arrays.asList(
                        ColumnType.Integer, ColumnType.Double, ColumnType.Double, ColumnType.Double,
                        ColumnType.Double, ColumnType.Double, ColumnType.Double, ColumnType.Double,
                        ColumnType.Double, ColumnType.Double, ColumnType.Double, ColumnType.Double,
                        ColumnType.Double, ColumnType.Double, ColumnType.Double, ColumnType.Double,
                        ColumnType.Double, ColumnType.Double, ColumnType.Double, ColumnType.Double,
                        ColumnType.Double, ColumnType.Double, ColumnType.Double, ColumnType.Double,
                        ColumnType.Double
                );
                loadCSVFile(file, columnTypes);
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message("ChinaGDP"));
            menu.setOnAction((ActionEvent event) -> {
                File file = FxFileTools.getInternalFile("/data/examples/ChinaGDP_" + lang + ".csv",
                        "data", "ChinaGDP_" + lang + ".csv", true);
                List<ColumnType> columnTypes = Arrays.asList(
                        ColumnType.Integer, ColumnType.Double, ColumnType.Double, ColumnType.Double,
                        ColumnType.Double, ColumnType.Double, ColumnType.Double
                );
                loadCSVFile(file, columnTypes);
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message("ChinaCPI"));
            menu.setOnAction((ActionEvent event) -> {
                File file = FxFileTools.getInternalFile("/data/examples/ChinaCPI_" + lang + ".csv",
                        "data", "ChinaCPI_" + lang + ".csv", true);
                List<ColumnType> columnTypes = Arrays.asList(
                        ColumnType.Integer, ColumnType.Double, ColumnType.Double, ColumnType.Double,
                        ColumnType.Double, ColumnType.Double, ColumnType.Double, ColumnType.Double
                );
                loadCSVFile(file, columnTypes);
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message("ChinaFoodConsumption"));
            menu.setOnAction((ActionEvent event) -> {
                File file = FxFileTools.getInternalFile("/data/examples/ChinaFoods_" + lang + ".csv",
                        "data", "ChinaFoods_" + lang + ".csv", true);
                List<ColumnType> columnTypes = Arrays.asList(
                        ColumnType.String, ColumnType.Double, ColumnType.Double, ColumnType.Double,
                        ColumnType.Double, ColumnType.Double, ColumnType.Double, ColumnType.Double,
                        ColumnType.Double
                );
                loadCSVFile(file, columnTypes);
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message("ChinaGraduates"));
            menu.setOnAction((ActionEvent event) -> {
                File file = FxFileTools.getInternalFile("/data/examples/ChinaGraduates_" + lang + ".csv",
                        "data", "ChinaGraduates_" + lang + ".csv", true);
                List<ColumnType> columnTypes = Arrays.asList(
                        ColumnType.Integer, ColumnType.Double, ColumnType.Double, ColumnType.Double,
                        ColumnType.Double, ColumnType.Double, ColumnType.Double, ColumnType.Double
                );
                loadCSVFile(file, columnTypes);
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message("ChinaMuseums"));
            menu.setOnAction((ActionEvent event) -> {
                File file = FxFileTools.getInternalFile("/data/examples/ChinaMuseums_" + lang + ".csv",
                        "data", "ChinaMuseums_" + lang + ".csv", true);
                List<ColumnType> columnTypes = Arrays.asList(
                        ColumnType.Integer, ColumnType.Long, ColumnType.Long, ColumnType.Long,
                        ColumnType.Long, ColumnType.Long, ColumnType.Long, ColumnType.Long,
                        ColumnType.Long, ColumnType.Double
                );
                loadCSVFile(file, columnTypes);
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message("ChinaHealthPersonnel"));
            menu.setOnAction((ActionEvent event) -> {
                File file = FxFileTools.getInternalFile("/data/examples/ChinaHealthPersonnel_" + lang + ".csv",
                        "data", "ChinaHealthPersonnel_" + lang + ".csv", true);
                List<ColumnType> columnTypes = Arrays.asList(
                        ColumnType.Integer, ColumnType.Double, ColumnType.Double, ColumnType.Double,
                        ColumnType.Double, ColumnType.Double, ColumnType.Double, ColumnType.Double,
                        ColumnType.Double, ColumnType.Double, ColumnType.Double
                );
                loadCSVFile(file, columnTypes);
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message("ChinaMarriage"));
            menu.setOnAction((ActionEvent event) -> {
                File file = FxFileTools.getInternalFile("/data/examples/ChinaMarriage_" + lang + ".csv",
                        "data", "ChinaMarriage_" + lang + ".csv", true);
                List<ColumnType> columnTypes = Arrays.asList(
                        ColumnType.Integer, ColumnType.Double, ColumnType.Double, ColumnType.Double,
                        ColumnType.Double, ColumnType.Double, ColumnType.Double, ColumnType.Double
                );
                loadCSVFile(file, columnTypes);
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message("ChinaSportWorldChampions"));
            menu.setOnAction((ActionEvent event) -> {
                File file = FxFileTools.getInternalFile("/data/examples/ChinaSportWorldChampions_" + lang + ".csv",
                        "data", "ChinaSportWorldChampions_" + lang + ".csv", true);
                List<ColumnType> columnTypes = Arrays.asList(
                        ColumnType.Integer, ColumnType.Integer, ColumnType.Integer, ColumnType.Integer,
                        ColumnType.Integer, ColumnType.Integer, ColumnType.Integer
                );
                loadCSVFile(file, columnTypes);
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message("CrimesFiledByChinaPolice"));
            menu.setOnAction((ActionEvent event) -> {
                File file = FxFileTools.getInternalFile("/data/examples/ChinaCrimesFiledByPolice_" + lang + ".csv",
                        "data", "ChinaCrimesFiledByPolice_" + lang + ".csv", true);
                List<ColumnType> columnTypes = Arrays.asList(
                        ColumnType.Integer, ColumnType.Integer, ColumnType.Integer, ColumnType.Integer,
                        ColumnType.Integer, ColumnType.Integer, ColumnType.Integer, ColumnType.Integer,
                        ColumnType.Integer, ColumnType.Integer, ColumnType.Integer, ColumnType.Integer
                );
                loadCSVFile(file, columnTypes);
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message("CrimesFiledByChinaProcuratorate"));
            menu.setOnAction((ActionEvent event) -> {
                File file = FxFileTools.getInternalFile("/data/examples/ChinaCrimesFiledByProcuratorate_" + lang + ".csv",
                        "data", "ChinaCrimesFiledByProcuratorate_" + lang + ".csv", true);
                List<ColumnType> columnTypes = Arrays.asList(
                        ColumnType.Integer, ColumnType.Integer, ColumnType.Integer, ColumnType.Integer,
                        ColumnType.Integer, ColumnType.Integer, ColumnType.Integer, ColumnType.Integer,
                        ColumnType.Integer, ColumnType.Integer, ColumnType.Integer, ColumnType.Integer,
                        ColumnType.Integer, ColumnType.Integer
                );
                loadCSVFile(file, columnTypes);
            });
            chinaMenu.getItems().add(menu);

            chinaMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(message("ChinaNationalBureauOfStatistics"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent event) -> {
                browse("https://data.stats.gov.cn/");
            });
            chinaMenu.getItems().add(menu);

            Menu regressionMenu = new Menu(message("Regression"), StyleTools.getIconImage("iconLinearPgression.png"));
            items.add(regressionMenu);

            // https://www.scribbr.com/statistics/simple-linear-regression/
            menu = new MenuItem(message("IncomeHappiness"));
            menu.setOnAction((ActionEvent event) -> {
                File file = FxFileTools.getInternalFile("/data/examples/IncomeHappiness_" + lang + ".csv",
                        "data", "IncomeHappiness_" + lang + ".csv", true);
                List<ColumnType> columnTypes = Arrays.asList(
                        ColumnType.Double, ColumnType.Double
                );
                loadCSVFile(file, columnTypes);
            });
            regressionMenu.getItems().add(menu);

            // https://github.com/krishnaik06/simple-Linear-Regression
            menu = new MenuItem(message("ExperienceSalary"));
            menu.setOnAction((ActionEvent event) -> {
                File file = FxFileTools.getInternalFile("/data/examples/ExperienceSalary_" + lang + ".csv",
                        "data", "ExperienceSalary_" + lang + ".csv", true);
                List<ColumnType> columnTypes = Arrays.asList(
                        ColumnType.Double, ColumnType.Double
                );
                loadCSVFile(file, columnTypes);
            });
            regressionMenu.getItems().add(menu);

            // http://archive.ics.uci.edu/ml/datasets/Iris
            menu = new MenuItem(message("IrisSpecies"));
            menu.setOnAction((ActionEvent event) -> {
                File file = FxFileTools.getInternalFile("/data/examples/IrisSpecies_" + lang + ".csv",
                        "data", "IrisSpecies_" + lang + ".csv", true);
                List<ColumnType> columnTypes = Arrays.asList(
                        ColumnType.Double, ColumnType.Double, ColumnType.Double, ColumnType.Double,
                        ColumnType.String
                );
                loadCSVFile(file, columnTypes);
            });
            regressionMenu.getItems().add(menu);

            // https://github.com/tomsharp/SVR/tree/master/data
            menu = new MenuItem(message("BostonHousingPrices"));
            menu.setOnAction((ActionEvent event) -> {
                File file = FxFileTools.getInternalFile("/data/examples/BostonHousingPrices_" + lang + ".csv",
                        "data", "BostonHousingPrices_" + lang + ".csv", true);
                List<ColumnType> columnTypes = Arrays.asList(
                        ColumnType.String, ColumnType.Double, ColumnType.Double, ColumnType.Double,
                        ColumnType.Double, ColumnType.Double, ColumnType.String, ColumnType.Double,
                        ColumnType.Double, ColumnType.Double, ColumnType.Double, ColumnType.Double,
                        ColumnType.Double, ColumnType.Double, ColumnType.Double, ColumnType.Double
                );
                loadCSVFile(file, columnTypes);
            });
            regressionMenu.getItems().add(menu);
            return items;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

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
    public boolean controlAltC() {
        if (targetIsTextInput()) {
            return false;
        }
        if (editTab.isSelected()) {
            if (editController.tableTab.isSelected()) {
                tableController.copyAction();

            } else if (editController.textTab.isSelected()) {
                TextClipboardTools.copyToMyBoxClipboard(myController, textController.textArea);

            }
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
            Data2DPasteContentInMyBoxClipboardController.open(tableController);
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

}
