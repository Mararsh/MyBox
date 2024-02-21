package mara.mybox.controller;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.input.Clipboard;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import mara.mybox.data.StringTable;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.Data2DExampleTools;
import mara.mybox.data2d.Data2DTools;
import mara.mybox.data2d.DataClipboard;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataFileExcel;
import mara.mybox.data2d.DataFilter;
import mara.mybox.data2d.DataMatrix;
import mara.mybox.data2d.DataTable;
import mara.mybox.data2d.TmpTable;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import static mara.mybox.db.data.Data2DDefinition.Type.CSV;
import static mara.mybox.db.data.Data2DDefinition.Type.Excel;
import static mara.mybox.db.data.Data2DDefinition.Type.MyBoxClipboard;
import static mara.mybox.db.data.Data2DDefinition.Type.Texts;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.TableColor;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxBackgroundTask;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.fxml.cell.TableComboBoxCell;
import mara.mybox.fxml.cell.TableDataBooleanDisplayCell;
import mara.mybox.fxml.cell.TableDataBooleanEditCell;
import mara.mybox.fxml.cell.TableDataColorEditCell;
import mara.mybox.fxml.cell.TableDataCoordinateEditCell;
import mara.mybox.fxml.cell.TableDataDateEditCell;
import mara.mybox.fxml.cell.TableDataDisplayCell;
import mara.mybox.fxml.cell.TableDataEditCell;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.DoubleMatrixTools;
import mara.mybox.tools.TextTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-12-17
 * @License Apache License Version 2.0
 */
public class ControlData2DLoad extends BaseTablePagesController<List<String>> {

    protected BaseController manageController;
    protected Data2D data2D;
    protected Data2D.Type dataType;
    protected TableData2DDefinition tableData2DDefinition;
    protected TableData2DColumn tableData2DColumn;
    protected char copyDelimiter = ',';
    protected boolean readOnly, notUpdateTitle;
    protected SimpleBooleanProperty statusNotify;
    protected DataFilter styleFilter;

    @FXML
    protected TableColumn<List<String>, Integer> dataRowColumn;
    @FXML
    protected Label dataLabel;
    @FXML
    protected FlowPane buttonsPane;
    @FXML
    protected HBox buttonsBox;
    @FXML
    protected Button fileMenuButton;

    public ControlData2DLoad() {
        statusNotify = new SimpleBooleanProperty(false);
        readOnly = true;
        styleFilter = new DataFilter();
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            if (dataRowColumn != null) {
                dataRowColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<List<String>, Integer>, ObservableValue<Integer>>() {
                    @Override
                    public ObservableValue<Integer> call(TableColumn.CellDataFeatures<List<String>, Integer> param) {
                        try {
                            List<String> row = (List<String>) param.getValue();
                            Integer v = Integer.parseInt(row.get(0));
                            if (v < 0) {
                                return null;
                            }
                            return new ReadOnlyObjectWrapper(v);
                        } catch (Exception e) {
                            return null;
                        }
                    }
                });
                dataRowColumn.setEditable(false);
                dataRowColumn.setCellFactory(new Callback<TableColumn<List<String>, Integer>, TableCell<List<String>, Integer>>() {
                    @Override
                    public TableCell<List<String>, Integer> call(TableColumn<List<String>, Integer> param) {
                        try {
                            TableCell<List<String>, Integer> cell = new TableCell<List<String>, Integer>() {
                                @Override
                                public void updateItem(Integer item, boolean empty) {
                                    super.updateItem(item, empty);
                                    setGraphic(null);
                                    if (empty || item == null) {
                                        setText(null);
                                        return;
                                    }
                                    setText(item + "");
                                }
                            };
                            cell.getStyleClass().add("row-number");
                            return cell;
                        } catch (Exception e) {
                            return null;
                        }
                    }
                });

                dataRowColumn.setPrefWidth(UserConfig.getInt("DataRowColumnWidth", 100));
                dataRowColumn.widthProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> o, Number ov, Number nv) {
                        UserConfig.setInt("DataRowColumnWidth", nv.intValue());
                    }
                });
            }

            updateStatus();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParameters(BaseController controller, Data2D.Type type) {
        try {
            manageController = controller;
            baseName = manageController.baseName;
            dataType = type;
            loadNull();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        data
     */
    public void setData(Data2D data) {
        try {
            if (data2D == null || data2D == data || data2D.getType() != data.getType()) {
                data2D = data;
            } else {
                data2D.resetData();
                data2D.cloneAll(data);
            }
            if (data2D != null) {
                tableData2DDefinition = data2D.getTableData2DDefinition();
                tableData2DColumn = data2D.getTableData2DColumn();

                switch (data2D.getType()) {
                    case CSV:
                    case MyBoxClipboard:
                        setFileType(VisitHistory.FileType.CSV);
                        break;
                    case Excel:
                        setFileType(VisitHistory.FileType.Excel);
                        break;
                    case Texts:
                        setFileType(VisitHistory.FileType.Text);
                        break;
                    default:
                        setFileType(VisitHistory.FileType.CSV);
                }

                if (paginationPane != null) {
                    showPaginationPane(!data2D.isTmpData() && !data2D.isMatrix());
                }
                data2D.setLoadController(this);

                if (selectFileButton != null && buttonsBox != null) {
                    if (data2D.isDataFile()) {
                        if (!buttonsBox.getChildren().contains(selectFileButton)) {
                            buttonsBox.getChildren().add(0, selectFileButton);
                        }
                    } else {
                        if (buttonsBox.getChildren().contains(selectFileButton)) {
                            buttonsBox.getChildren().remove(selectFileButton);
                        }
                    }
                }

                if (toolbar != null && fileMenuButton != null) {
                    if (data2D.isDataFile()) {
                        if (!toolbar.getChildren().contains(fileMenuButton)) {
                            toolbar.getChildren().add(0, fileMenuButton);
                        }
                    } else {
                        if (toolbar.getChildren().contains(fileMenuButton)) {
                            toolbar.getChildren().remove(fileMenuButton);
                        }
                    }
                }

            }

            validateData();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void resetData() {
        resetStatus();
        dataSizeLoaded = true;
        if (data2D != null) {
            data2D.resetData();
        }
        isSettingValues = true;
        tableData.clear();
        isSettingValues = false;
        notifyLoaded();
    }

    public void loadData(Data2D data) {
        if (data == null) {
            loadNull();
            return;
        }
        setData(data);
        readDefinition();
    }

    public void loadNull() {
        if (dataType != null) {
            loadData(Data2D.create(dataType));
        } else {
            resetData();
        }
    }

    public boolean loadDef(Data2DDefinition def) {
        if (!checkBeforeNextAction()) {
            return false;
        }
        resetStatus();
        if (def == null) {
            loadNull();
            return true;
        }
        if (data2D == null || data2D.getType() != def.getType()) {
            setData(Data2D.create(def.getType()));
        } else if (data2D != def) {
            data2D.resetData();
        }
        data2D.cloneAll(def);
        readDefinition();
        return true;
    }

    public synchronized void readDefinition() {
        if (data2D == null) {
            loadNull();
            return;
        }
        if (!checkInvalidFile()) {
            return;
        }
        resetStatus();
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    data2D.startTask(this, null);
                    data2D.readDataDefinition(conn);
                    if (isCancelled()) {
                        return false;
                    }
                    return data2D.readColumns(conn);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.stopTask();
                Data2D s = data2D;
                data2D = null;
                resetView(false);
                data2D = s;
                loadData();
            }

        };
        start(task, thisPane);
    }

    public boolean checkInvalidFile() {
        if (data2D == null) {
            return false;
        }
        File file = data2D.getFile();
        if (file == null || file.exists()) {
            return true;
        }
        FxTask nullTask = new FxTask<Void>(this) {
            @Override
            protected boolean handle() {
                try {
                    tableData2DDefinition.deleteData(data2D);
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                loadNull();
            }
        };
        start(nullTask, false);
        return false;
    }

    public void loadData() {
        try {
            makeColumns();
            if (!validateData()) {
                resetData();
                return;
            }
            dataSizeLoaded = false;
            loadPage(data2D.getCurrentPage());
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadTmpData(List<String> cols, List<List<String>> data) {
        loadTmpData(null, data2D.toColumns(cols), data);
    }

    public void loadTmpData(String name, List<Data2DColumn> cols, List<List<String>> data) {
        try {
            if (data2D == null) {
                return;
            }
            resetStatus();
            data2D.resetData();
            List<Data2DColumn> columns = new ArrayList<>();
            if (cols == null || cols.isEmpty()) {
                data2D.setHasHeader(false);
                if (data != null && !data.isEmpty()) {
                    for (int i = 0; i < data.get(0).size(); i++) {
                        Data2DColumn column = new Data2DColumn(data2D.colPrefix() + (i + 1), data2D.defaultColumnType());
                        columns.add(column);
                    }
                }
            } else {
                data2D.setHasHeader(true);
                for (Data2DColumn col : cols) {
                    columns.add(col.cloneAll());
                }
            }
            for (Data2DColumn column : columns) {
                column.setIndex(data2D.newColumnIndex());
            }
            data2D.setColumns(columns);
            StringTable validateTable = Data2DTools.validate(columns);
            List<List<String>> rows = new ArrayList<>();
            if (data != null) {
                for (int i = 0; i < data.size(); i++) {
                    List<String> row = new ArrayList<>();
                    row.add("-1");
                    row.addAll(data.get(i));
                    rows.add(row);
                }
            }
            data2D.checkForLoad();
            data2D.setDataName(name);
            resetView(false);
            setData(data2D);
            displayTmpData(rows);
            if (validateTable != null && !validateTable.isEmpty()) {
                validateTable.htmlTable();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean displayTmpData(List<List<String>> newData) {
        try {
            makeColumns();
            isSettingValues = true;
            tableData.setAll(newData);
            isSettingValues = false;
            dataSizeLoaded = true;
            setPagination();
            tableChanged(false);
            notifyLoaded();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public void loadCSVData(DataFileCSV csvData) {
        if (csvData == null || csvData.getFile() == null || !csvData.getFile().exists()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private Data2D targetData;

            @Override
            protected boolean handle() {
                try {
                    switch (data2D.getType()) {
                        case Texts:
                            targetData = csvData.cloneAll();
                            targetData.setType(Data2DDefinition.Type.Texts).setD2did(-1);
                            targetData.saveAttributes();
                            recordFileWritten(targetData.getFile(), VisitHistory.FileType.Text);
                            break;
                        case CSV:
                            targetData = csvData;
                            targetData.saveAttributes();
                            recordFileWritten(targetData.getFile(), VisitHistory.FileType.CSV);
                            break;
                        case Excel: {
                            DataFileExcel excelData = DataFileExcel.toExcel(this, csvData);
                            if (excelData != null) {
                                recordFileWritten(excelData.getFile(), VisitHistory.FileType.Excel);
                            }
                            targetData = excelData;
                            break;
                        }
                        case DatabaseTable: {
                            String name = csvData.dataName();
                            if (name.startsWith(TmpTable.TmpTablePrefix)
                                    || name.startsWith(TmpTable.TmpTablePrefix.toLowerCase())) {
                                name = name.substring(TmpTable.TmpTablePrefix.length());
                            }
                            DataTable dataTable = csvData.toTable(this, name);
                            targetData = dataTable;
                            break;
                        }
                        case MyBoxClipboard: {
                            DataClipboard clip = DataClipboard.toClip(this, csvData);
                            targetData = clip;
                            break;
                        }
                        case Matrix: {
                            DataMatrix matrix = DataMatrix.toMatrix(this, csvData);
                            targetData = matrix;
                            break;
                        }
                    }
                    return targetData != null;
                } catch (Exception e) {
                    error = e.toString();
                    MyBoxLog.console(error);
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                loadDef(targetData);
            }

        };
        start(task, thisPane);
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
            setData(Data2D.create(dataType));
            loadCSVData(csvData);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadTableData(DataTable dataTable) {
        if (dataTable == null) {
            return;
        }
        task = new FxSingletonTask<Void>(this) {
            private Data2D targetData;

            @Override
            protected boolean handle() {
                try {
                    switch (data2D.getType()) {
                        case Texts:
                            targetData = DataTable.toText(this, dataTable);
                            if (targetData != null) {
                                recordFileWritten(targetData.getFile(), VisitHistory.FileType.Text);
                            }
                            break;
                        case CSV:
                            targetData = DataTable.toCSV(this, dataTable);
                            if (targetData != null) {
                                recordFileWritten(targetData.getFile(), VisitHistory.FileType.CSV);
                            }
                            break;
                        case Excel: {
                            targetData = DataTable.toExcel(this, dataTable);
                            if (targetData != null) {
                                recordFileWritten(targetData.getFile(), VisitHistory.FileType.Excel);
                            }
                            break;
                        }
                        case DatabaseTable: {
                            targetData = dataTable;
                            break;
                        }
                        case MyBoxClipboard: {
                            targetData = DataTable.toClip(this, dataTable);
                            break;
                        }
                        case Matrix: {
                            targetData = DataTable.toMatrix(this, dataTable);
                            break;
                        }
                    }
                    return targetData != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                loadDef(targetData);
            }
        };
        start(task, thisPane);
    }

    public void loadMatrix(double[][] matrix) {
        if (matrix == null || matrix.length == 0) {
            loadNull();
            return;
        }
        loadTmpData(null, data2D.tmpColumns(matrix[0].length), DoubleMatrixTools.toList(matrix));
    }

    public synchronized boolean updateData(List<List<String>> newData, boolean columnsChanged) {
        try {
            if (columnsChanged) {
                makeColumns();
            }
            isSettingValues = true;
            tableData.setAll(newData);
            isSettingValues = false;
            tableChanged(true);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public void sourceFileChanged(File file) {
        try {
            if (!checkBeforeNextAction()) {
                return;
            }
            resetStatus();
            setData(Data2D.create(Data2DDefinition.type(file)));
            data2D.initFile(file);
            readDefinition();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        status
     */
    public boolean validateData() {
        boolean valid = data2D != null && data2D.isValid();
        if (mainAreaBox != null) {
            mainAreaBox.setDisable(!valid);
        }
        if (buttonsPane != null) {
            buttonsPane.setDisable(!valid);
        }
        return valid;
    }

    public synchronized void resetStatus() {
        if (task != null) {
            task.cancel();
        }
        if (backgroundTask != null) {
            backgroundTask.cancel();
        }
        if (data2D != null) {
            data2D.setTableChanged(false);
        }
    }

    public void notifyStatus() {
        updateStatus();
        if (statusNotify != null) {
            statusNotify.set(!statusNotify.get());
        }
    }

    public void notifySaved() {
        notifyStatus();
    }

    @Override
    public void notifyLoaded() {
        if (loadedNotify == null) {
            return;
        }
        notifyStatus();
        loadedNotify.set(!loadedNotify.get());
        if (data2D != null && data2D.getFile() != null) {
            recordFileOpened(data2D.getFile());
        }
    }

    /*
        action
     */
    @FXML
    @Override
    public void createAction() {
        try {
            if (!checkBeforeNextAction()) {
                return;
            }
            data2D = Data2D.create(dataType);
            loadTmpData(null, data2D.tmpColumns(3), data2D.tmpData(3, 3));
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void editAction() {
        Data2DDefinition.open(data2D);
    }

    @FXML
    @Override
    public void selectAction() {

    }

    @FXML
    public void renameAction(BaseTablePagesController parent, int index, Data2DDefinition targetData) {
        String newName = PopTools.askValue(getTitle(), message("CurrentName") + ":" + targetData.getDataName(),
                message("NewName"), targetData.getDataName() + "m");
        if (newName == null || newName.isBlank()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private Data2DDefinition def;

            @Override
            protected boolean handle() {
                targetData.setDataName(newName);
                def = tableData2DDefinition.updateData(targetData);
                return def != null;
            }

            @Override
            protected void whenSucceeded() {
                popSuccessful();
                if (parent != null) {
                    parent.tableData.set(index, def);
                }
                if (def.getD2did() == data2D.getD2did()) {
                    data2D.setDataName(newName);
                    if (parent != null) {
                        parent.updateStatus();
                    }
                    updateStatus();
                }

            }

        };
        start(task);
    }

    @FXML
    @Override
    public void copyAction() {
        if (!validateData()) {
            return;
        }
        Data2DCopyController.open(this);
    }

    @FXML
    @Override
    public void copyToSystemClipboard() {
        if (!validateData()) {
            return;
        }
        Data2DCopyController controller = Data2DCopyController.open(this);
        controller.targetController.systemClipboardRadio.setSelected(true);
    }

    public void copyToSystemClipboard(List<String> names, List<List<String>> data) {
        try {
            if (data == null || data.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            String text = TextTools.rowsText(null, data, ",", names, null);
            TextClipboardTools.copyToSystemClipboard(this, text);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void copyToMyBoxClipboard(List<String> names, List<List<String>> data) {
        try {
            if (data == null || data.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            toMyBoxClipboard(null, data2D.toColumns(names), data);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void toMyBoxClipboard(String name, List<Data2DColumn> cols, List<List<String>> data) {
        try {
            if (data == null || data.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            FxTask copyTask = new FxTask<Void>(this) {

                private DataClipboard clip;

                @Override
                protected boolean handle() {
                    clip = DataClipboard.create(task, name, cols, data);
                    return clip != null;
                }

                @Override
                protected void whenSucceeded() {
                    DataInMyBoxClipboardController controller = DataInMyBoxClipboardController.oneOpen();
                    controller.loadDef(clip);
                    popDone();
                }

            };
            start(copyTask, false);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void myBoxClipBoard() {
        Data2DPasteContentInMyBoxClipboardController.open(this);
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
            Data2DLoadContentInSystemClipboardController.open(this, text);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void pasteContentInSystemClipboard() {
        try {
            if (data2D == null) {
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
            if (data2D == null) {
                return;
            }
            Data2DPasteContentInMyBoxClipboardController.open(this);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void verifyAction() {
        StringTable results = verifyResults();
        if (results.isEmpty()) {
            popInformation(message("AllValuesValid"), 5000);
            return;
        }
        results.htmlTable();
    }

    public StringTable verifyResults() {
        try {
            List<String> names = new ArrayList<>();
            names.addAll(Arrays.asList(message("Row"), message("Column"), message("Invalid")));
            StringTable stringTable = new StringTable(names, data2D.displayName());
            for (int r = 0; r < tableData.size(); r++) {
                List<String> dataRow = tableData.get(r);
                for (int c = 0; c < data2D.columnsNumber(); c++) {
                    Data2DColumn column = data2D.column(c);
                    if (column.isAuto()) {
                        continue;
                    }
                    String value = dataRow.get(c + 1);
                    String item = null;
                    if (column.isNotNull() && (value == null || value.isBlank())) {
                        item = message("Null");
                    } else if (!column.validValue(value)) {
                        item = message(column.getType().name());
                    } else if (!data2D.validValue(value)) {
                        item = message("TextDataComments");
                    }
                    if (item == null) {
                        continue;
                    }
                    List<String> invalid = new ArrayList<>();
                    invalid.addAll(Arrays.asList((r + 1) + "", column.getColumnName(), item));
                    stringTable.add(invalid);
                }
            }
            return stringTable;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    /*
        table
     */
    @Override
    public void tableChanged(boolean changed) {
        if (isSettingValues || data2D == null) {
            return;
        }
        data2D.setTableChanged(changed);
        validateData();
    }

    public void makeColumns() {
        try {
            isSettingValues = true;
            tableData.clear();
            tableView.getColumns().remove(rowsSelectionColumn != null ? 2 : 1, tableView.getColumns().size());
            tableView.setItems(tableData);
            isSettingValues = false;
            if (data2D != null) {
                data2D.setLoadController(this);
            }

            if (!validateData()) {
                return;
            }
            List<Data2DColumn> columns = data2D.getColumns();

            ControlData2DLoad dataControl = this;
            TableColor tableColor = null;
            boolean includeCoordinate = data2D.includeCoordinate();
            for (int i = 0; i < columns.size(); i++) {
                Data2DColumn dataColumn = columns.get(i);
                String name = dataColumn.getColumnName();
                ColumnType type = dataColumn.getType();
                TableColumn tableColumn = new TableColumn<List<String>, String>(name);
                tableColumn.setPrefWidth(dataColumn.getWidth());
                tableColumn.setEditable(!readOnly && dataColumn.isEditable() && !dataColumn.isId());
                tableColumn.setUserData(dataColumn.getIndex());
                int colIndex = i + 1;

                tableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<List<String>, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<List<String>, String> param) {
                        try {
                            List<String> row = (List<String>) param.getValue();
                            return new SimpleStringProperty(row.get(colIndex));
                        } catch (Exception e) {
                            return null;
                        }
                    }
                });

                if (tableColumn.isEditable()) {

                    if (type == ColumnType.Enumeration) {
                        tableColumn.setCellFactory(TableComboBoxCell.create(dataColumn.enumValues(), 12));

                    } else if (type == ColumnType.Boolean) {
                        tableColumn.setCellFactory(TableDataBooleanEditCell.create(dataControl, dataColumn, colIndex));

                    } else if (type == ColumnType.Color) {
                        if (tableColor == null) {
                            tableColor = new TableColor();
                        }
                        tableColumn.setCellFactory(TableDataColorEditCell.create(dataControl, dataColumn, tableColor));

                    } else if (dataColumn.isTimeType()) {
                        tableColumn.setCellFactory(TableDataDateEditCell.create(dataControl, dataColumn));

                    } else if (includeCoordinate && (type == ColumnType.Longitude || type == ColumnType.Latitude)) {
                        tableColumn.setCellFactory(TableDataCoordinateEditCell.create(dataControl, dataColumn));

                    } else {
                        tableColumn.setCellFactory(TableDataEditCell.create(dataControl, dataColumn));
                    }
                    tableColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<List<String>, String>>() {
                        @Override
                        public void handle(TableColumn.CellEditEvent<List<String>, String> e) {
                            if (e == null) {
                                return;
                            }
                            int rowIndex = e.getTablePosition().getRow();
                            if (rowIndex < 0 || rowIndex >= tableData.size()) {
                                return;
                            }
                            List<String> row = tableData.get(rowIndex);
                            row.set(colIndex, e.getNewValue());
                            tableData.set(rowIndex, row);
                        }
                    });
                    tableColumn.getStyleClass().add("editable-column");
                } else {
                    if (type == ColumnType.Boolean) {
                        tableColumn.setCellFactory(TableDataBooleanDisplayCell.create(dataControl, dataColumn));
                    } else {
                        tableColumn.setCellFactory(TableDataDisplayCell.create(dataControl, dataColumn));
                    }
                }

                tableColumn.setComparator(new Comparator<String>() {
                    @Override
                    public int compare(String v1, String v2) {
                        return dataColumn.compare(v1, v2);
                    }
                });

                if (dataColumn.isAuto()) {
                    tableColumn.getStyleClass().clear();
                    tableColumn.getStyleClass().add("auto-column");
                } else if (dataColumn.isIsPrimaryKey()) {
                    tableColumn.getStyleClass().clear();
                    tableColumn.getStyleClass().add("primary-column");
                }

                tableView.getColumns().add(tableColumn);
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean checkBeforeLoadingTableData() {
        return data2D != null && data2D.isValid();
    }

    @Override
    public List<List<String>> readPageData(FxTask currentTask, Connection conn) {
        data2D.startFilter(null);
        return data2D.readPageData(conn);
    }

    @Override
    protected void countPagination(FxTask currentTask, Connection conn, long page) {
        if (data2D.isMatrix()) {
            pageSize = Integer.MAX_VALUE;
            dataSize = data2D.getDataSize();
            pagesNumber = 1;
            currentPage = 0;
            startRowOfCurrentPage = 0;
            dataSizeLoaded = true;
        } else {
            pageSize = UserConfig.getInt(baseName + "PageSize", 50);
            super.countPagination(currentTask, conn, page);
        }
        data2D.setPageSize(pageSize);
        data2D.setPagesNumber(pagesNumber);
        data2D.setCurrentPage(currentPage);
        data2D.setStartRowOfCurrentPage(startRowOfCurrentPage);
    }

    @Override
    public void postLoadedTableData() {
        super.postLoadedTableData();
        if (data2D != null) {
            data2D.stopTask();
        }
    }

    @Override
    public long readDataSize(FxTask currentTask, Connection conn) {
        return data2D.getDataSize();
    }

    public void correctDataSize() {
        if (data2D.isTmpData() || !data2D.isValid()) {
            return;
        }
        if (data2D.getColsNumber() != data2D.getColumns().size()
                || data2D.getRowsNumber() != data2D.getDataSize()) {
            FxTask updateTask = new FxTask<Void>(this) {

                @Override
                protected boolean handle() {
                    data2D.countSize();
                    return data2D.saveAttributes();
                }

                @Override
                protected void whenSucceeded() {
                    notifySaved();
                }

                @Override
                protected void whenFailed() {
                }

            };
            start(updateTask, false);
        }
    }

    @Override
    public void loadDataSize() {
        if (data2D == null) {
            return;
        }
        if (dataSizeLoaded || data2D.isTmpData() || data2D.isMatrix()) {
            afterLoaded(false);
            return;
        }
        if (backgroundTask != null) {
            backgroundTask.cancel();
            backgroundTask = null;
        }
        data2D.setDataSize(0);
        dataSizeLoaded = false;
        if (paginationPane != null) {
            paginationPane.setVisible(false);
        }
        if (saveButton != null) {
            saveButton.setDisable(true);
        }
        backgroundTask = new FxBackgroundTask<Void>(this) {

            @Override
            protected boolean handle() {
                data2D.setBackgroundTask(this);
                return data2D.readTotal() >= 0;
            }

            @Override
            protected void whenSucceeded() {
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
                super.finalAction();
                data2D.setBackgroundTask(null);
                afterLoaded(true);
            }

        };
        start(backgroundTask, false);
    }

    protected void afterLoaded(boolean paginate) {
        dataSizeLoaded = true;
        correctDataSize();
        if (paginationPane != null) {
            if (paginate) {
                showPaginationPane(true);
                refreshPagination();
            } else {
                showPaginationPane(false);
            }
        }
        if (saveButton != null) {
            saveButton.setDisable(false);
        }
        notifyLoaded();
    }

    protected void refreshPagination() {
        countPagination(null, null, currentPage);
        setPagination();
        updateStatus();
    }

    @Override
    protected void setPagination() {
        try {
            if (data2D == null || data2D.isMatrix() || data2D.isTmpData() || !dataSizeLoaded) {
                showPaginationPane(false);
                return;
            }
            super.setPagination();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }

    }

    protected void showPaginationPane(boolean show) {
        if (paginationPane == null) {
            return;
        }
        paginationPane.setVisible(show);
        if (show) {
            if (!thisPane.getChildren().contains(paginationPane)) {
                thisPane.getChildren().add(paginationPane);
                NodeStyleTools.refreshStyle(paginationPane);
            }
        } else {
            if (thisPane.getChildren().contains(paginationPane)) {
                thisPane.getChildren().remove(paginationPane);
            }
        }
    }

    /*
        interface
     */
    @Override
    public void updateStatus() {
        super.updateStatus();
        updateName();
        validateData();
    }

    public void updateName() {
        myStage = getMyStage();
        if (data2D != null) {
            String name = data2D.displayName();
            if (myStage != null && !notUpdateTitle) {
                String title = getRootBaseTitle() + " : " + name;
                if (data2D.isTableChanged()) {
                    title += " *";
                }
                myStage.setTitle(title);
            }
        } else {
            if (myStage != null && !notUpdateTitle) {
                myStage.setTitle(baseTitle);
            }
        }
    }

    public void setLabel(String s) {
        if (dataLabel != null) {
            dataLabel.setText(s);
        }
    }

    @Override
    public boolean controlAltC() {
        if (targetIsTextInput()) {
            return false;
        }
        copyToSystemClipboard();
        return true;
    }

    @Override
    public boolean controlAltV() {
        if (targetIsTextInput()) {
            return false;
        }
        pasteContentInSystemClipboard();
        return true;
    }

    @FXML
    protected void popExamplesMenu(Event event) {
        if (UserConfig.getBoolean("Data2DExamplesPopWhenMouseHovering", true)) {
            showExamplesMenu(event);
        }
    }

    @FXML
    protected void showExamplesMenu(Event event) {
        try {
            List<MenuItem> items = new ArrayList<>();
            items.addAll(Data2DExampleTools.examplesMenu(this));

            items.add(new SeparatorMenuItem());

            CheckMenuItem pMenu = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
            pMenu.setSelected(UserConfig.getBoolean("Data2DExamplesPopWhenMouseHovering", true));
            pMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean("Data2DExamplesPopWhenMouseHovering", pMenu.isSelected());
                }
            });
            items.add(pMenu);

            popEventMenu(event, items);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void cleanPane() {
        try {
            statusNotify = null;
            data2D = null;
            tableData2DDefinition = null;
            tableData2DColumn = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }


    /*
        set
     */
    public void setNotUpdateTitle(boolean notUpdateTitle) {
        this.notUpdateTitle = notUpdateTitle;
    }


    /*
        get
     */
    public Data2D getData2D() {
        return data2D;
    }

    public ObservableList<List<String>> getTableData() {
        return tableData;
    }

    public DataFilter getStyleFilter() {
        return styleFilter;
    }

}
