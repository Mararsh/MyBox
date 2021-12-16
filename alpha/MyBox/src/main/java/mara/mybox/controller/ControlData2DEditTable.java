package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.Clipboard;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;
import mara.mybox.data.Data2D;
import mara.mybox.data.DataClipboard;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.fxml.cell.TableAutoCommitCell;
import mara.mybox.tools.TextTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public class ControlData2DEditTable extends BaseTableViewController<List<String>> {

    protected ControlData2D dataController;
    protected Data2D data2D;
    protected TableData2DDefinition tableData2DDefinition;
    protected TableData2DColumn tableData2DColumn;
    protected char copyDelimiter = ',';
    protected final SimpleBooleanProperty defSavedNotify;

    @FXML
    protected TableColumn<List<String>, Integer> dataRowColumn;

    public ControlData2DEditTable() {
        defSavedNotify = new SimpleBooleanProperty(false);
    }

    protected void setParameters(ControlData2DEdit editController) {
        try {
            dataController = editController.dataController;

            paginationPane = dataController.paginationPane;
            pageSizeSelector = dataController.pageSizeSelector;
            pageSelector = dataController.pageSelector;
            pageLabel = dataController.pageLabel;
            dataSizeLabel = dataController.dataSizeLabel;
            selectedLabel = dataController.selectedLabel;
            pagePreviousButton = dataController.pagePreviousButton;
            pageNextButton = dataController.pageNextButton;
            pageFirstButton = dataController.pageFirstButton;
            pageLastButton = dataController.pageLastButton;
            saveButton = dataController.saveButton;

            setControls();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setData(Data2D data) {
        try {
            data2D = data;
            tableData2DDefinition = dataController.tableData2DDefinition;
            tableData2DColumn = dataController.tableData2DColumn;

            if (data2D.isMatrix()) {
                dataController.thisPane.getChildren().remove(paginationPane);
            } else {
                initPagination();
            }

            data2D.setTableController(this);
            checkData();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void setControls() {
        try {
            dataRowColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<List<String>, Integer>, ObservableValue<Integer>>() {
                @Override
                public ObservableValue<Integer> call(TableColumn.CellDataFeatures<List<String>, Integer> param) {
                    try {
                        List<String> row = (List<String>) param.getValue();
                        Integer v = Integer.valueOf(row.get(0));
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
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        database
     */
    public synchronized void readDefinition() {
        if (data2D == null) {
            return;
        }
        if (!checkValidData()) {
            return;
        }
        if (dataController != null) {
            dataController.resetStatus();
        } else {
            resetStatus();
        }
        isSettingValues = true;
        resetView();
        isSettingValues = false;
        task = new SingletonTask<Void>(this) {

            private StringTable validateTable;

            @Override
            protected boolean handle() {
                try {
                    data2D.setTask(task);
                    data2D.setColumns(null);
                    long d2did = data2D.readDataDefinition();
                    List<String> dataCols = data2D.readColumns();
                    if (isCancelled()) {
                        return false;
                    }
                    if (dataCols == null || dataCols.isEmpty()) {
                        data2D.setHasHeader(false);
                        tableData2DColumn.clear(data2D);

                    } else {
                        List<Data2DColumn> columns = new ArrayList<>();
                        List<Data2DColumn> savedColumns = data2D.getSavedColumns();
                        for (int i = 0; i < dataCols.size(); i++) {
                            Data2DColumn column;
                            if (savedColumns != null && i < savedColumns.size()) {
                                column = savedColumns.get(i);
                                if (data2D.isHasHeader()) {
                                    column.setName(dataCols.get(i));
                                }
                            } else {
                                column = new Data2DColumn(dataCols.get(i), data2D.defaultColumnType());
                            }
                            column.setD2id(d2did);
                            column.setIndex(i);
                            columns.add(column);
                        }
                        data2D.setColumns(columns);
                        validateTable = Data2DColumn.validate(columns);
                        if (!data2D.isTmpData()) {
                            if (validateTable == null || validateTable.isEmpty()) {
                                tableData2DColumn.save(d2did, columns);
                            }
                        }
                    }
                    return true;
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
                data2D.setTask(null);
                task = null;
                if (dataController != null) {
                    dataController.loadData();   // Load data whatever
                    dataController.notifyLoaded();
                } else {
                    loadData();
                }
                if (validateTable != null && !validateTable.isEmpty()) {
                    validateTable.htmlTable();
                }
            }

        };
        start(task);
    }

    public boolean checkValidData() {
        if (data2D == null) {
            return false;
        }
        File file = data2D.getFile();
        if (file == null || file.exists()) {
            return true;
        }
        synchronized (this) {
            SingletonTask nullTask = new SingletonTask<Void>(this) {
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
                    loadNull();
                }
            };
            start(nullTask, false);
        }
        return false;
    }

    public void loadNull() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                data2D.resetData();
                if (dataController != null) {
                    loadData();
                    dataController.notifyLoaded();
                } else {
                    loadData();
                }
            }
        });
    }

    public synchronized void resetStatus() {
        if (task != null) {
            task.cancel();
        }
        if (backgroundTask != null) {
            backgroundTask.cancel();
        }
        data2D.setTableChanged(false);
    }

    public void notifyDefSaved() {
        defSavedNotify.set(!defSavedNotify.get());
    }

    /*
        table
     */
    public synchronized void loadData() {
        try {
            makeColumns();
            if (!checkData()) {
                dataSizeLoaded = true;
                return;
            }
            dataSizeLoaded = false;
            loadPage(currentPage);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean checkData() {
        super.updateStatus();
        boolean invalid = data2D == null || !data2D.isColumnsValid();
        thisPane.setDisable(invalid);
        return !invalid;
    }

    @Override
    public boolean checkBeforeLoadingTableData() {
        if (dataController != null) {
            return dataController.checkBeforeNextAction();
        } else {
            return true;
        }
    }

    @Override
    public List<List<String>> readPageData() {
        data2D.setTask(task);
        return data2D.readPageData();
    }

    @Override
    protected void countPagination(long page) {
        if (data2D.isMatrix()) {
            pageSize = Integer.MAX_VALUE;
            dataSize = data2D.getDataSize();
            pagesNumber = 1;
            currentPage = 0;
            startRowOfCurrentPage = 0;
        } else {
            pageSize = UserConfig.getInt(baseName + "PageSize", 50);
            super.countPagination(page);
        }
        data2D.setPageSize(pageSize);
        data2D.setPagesNumber(pagesNumber);
        data2D.setCurrentPage(currentPage);
        data2D.setStartRowOfCurrentPage(startRowOfCurrentPage);
        if (data2D.isMatrix()) {
            dataSizeLoaded = true;
        }
    }

    @Override
    public void postLoadedTableData() {
        super.postLoadedTableData();
        data2D.setTask(null);
    }

    @Override
    public long readDataSize() {
        return data2D.getDataSize();
    }

    public void correctDataSize() {
        if (data2D.isTmpData() || !data2D.isValid()) {
            return;
        }
        if (data2D.getColsNumber() != data2D.getColumns().size()
                || data2D.getRowsNumber() != data2D.getDataSize()) {
            synchronized (this) {
                SingletonTask updateTask = new SingletonTask<Void>(this) {

                    @Override
                    protected boolean handle() {
                        return data2D.saveDefinition();
                    }

                    @Override
                    protected void whenSucceeded() {
                        notifyDefSaved();
                    }

                    @Override
                    protected void whenFailed() {
                    }

                };
                start(updateTask, false);
            }
        }
    }

    @Override
    public void loadDataSize() {
        if (data2D == null) {
            return;
        }
        if (dataSizeLoaded || data2D.isTmpData() || data2D.isMatrix()) {
            dataSizeLoaded = true;
            correctDataSize();
            paginationPane.setVisible(false);
            if (saveButton != null) {
                saveButton.setDisable(false);
            }
            return;
        }
        synchronized (this) {
            if (backgroundTask != null) {
                backgroundTask.cancel();
            }
            data2D.setDataSize(0);
            dataSizeLoaded = false;
            paginationPane.setVisible(false);
            if (saveButton != null) {
                saveButton.setDisable(true);
            }
            backgroundTask = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    data2D.setBackgroundTask(backgroundTask);
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
                    data2D.setBackgroundTask(null);
                    backgroundTask = null;
                    dataSizeLoaded = true;
                    correctDataSize();
                    if (dataController != null) {
                        dataController.checkStatus();
                    }
                    refreshPagination();
                    if (saveButton != null) {
                        saveButton.setDisable(false);
                    }
                }

            };
            start(backgroundTask, false);
        }
    }

    protected void refreshPagination() {
        countPagination(currentPage);
        setPagination();
        updateStatus();
    }

    @Override
    protected void setPagination() {
        try {
            if (data2D == null || data2D.isMatrix() || data2D.isTmpData() || !dataSizeLoaded) {
                paginationPane.setVisible(false);
                return;
            }
            super.setPagination();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }

    }

    public void makeColumns() {
        try {
            isSettingValues = true;
            tableData.clear();
            tableView.getColumns().remove(2, tableView.getColumns().size());
            tableView.setItems(tableData);
            isSettingValues = false;
            data2D.setTableController(this);

            if (!checkData()) {
                return;
            }
            List<Data2DColumn> columns = data2D.getColumns();
            for (int i = 0; i < columns.size(); i++) {
                Data2DColumn dataColumn = columns.get(i);
                String name = dataColumn.getName();
                TableColumn tableColumn = new TableColumn<List<String>, String>(name);
                tableColumn.setPrefWidth(dataColumn.getWidth());
                tableColumn.setEditable(dataColumn.isEditable());
                tableColumn.setUserData(dataColumn.getIndex());
                int col = i + 1;

                tableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<List<String>, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<List<String>, String> param) {
                        try {
                            List<String> row = (List<String>) param.getValue();
                            String value = row.get(col);
                            if (value == null) {
                                return null;
                            }
                            return new SimpleStringProperty(value);
                        } catch (Exception e) {
                            return null;
                        }
                    }
                });

                tableColumn.setCellFactory(new Callback<TableColumn<List<String>, String>, TableCell<List<String>, String>>() {
                    @Override
                    public TableCell<List<String>, String> call(TableColumn<List<String>, String> param) {
                        try {
                            TableAutoCommitCell<List<String>, String> cell
                                    = new TableAutoCommitCell<List<String>, String>(new DefaultStringConverter()) {
                                @Override
                                public boolean valid(String value) {
                                    return dataColumn.validValue(value);
                                }

                                @Override
                                public void commitEdit(String value) {
                                    try {
                                        int rowIndex = rowIndex();
                                        if (rowIndex < 0 || !valid(value)) {
                                            cancelEdit();
                                            return;
                                        }
                                        List<String> row = tableData.get(rowIndex);
                                        String oldValue = row.get(col);
                                        if ((value == null && oldValue != null)
                                                || (value != null && !value.equals(oldValue))) {
                                            super.commitEdit(value);
                                            row.set(col, value);
                                            tableChanged(true);
                                        }
                                    } catch (Exception e) {
                                        MyBoxLog.debug(e);
                                    }
                                }

                            };
                            return cell;
                        } catch (Exception e) {
                            return null;
                        }
                    }
                });

                if (tableColumn.isEditable()) {
                    tableColumn.getStyleClass().add("editable-column");
                }

                tableView.getColumns().add(tableColumn);
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public synchronized void tableChanged(boolean changed) {
        if (isSettingValues) {
            return;
        }
        data2D.setTableChanged(changed);
        checkData();

        if (dataController != null) {
            dataController.textController.loadData();
            dataController.viewController.loadData();
        }
    }

    @Override
    public List<String> newData() {
        return data2D.newRow();
    }

    @Override
    public List<String> dataCopy(List<String> data) {
        return data2D.copyRow(data);
    }

    @FXML
    @Override
    public void addAction() {
        if (!checkData()) {
            return;
        }
        addRowsAction();
    }

    @FXML
    @Override
    public void deleteAction() {
        deleteRowsAction();
    }

    public boolean loadTmpData(List<List<String>> newData) {
        try {
            makeColumns();
            isSettingValues = true;
            tableData.setAll(newData);
            isSettingValues = false;
            dataSizeLoaded = true;
            setPagination();
            tableChanged(false);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public boolean loadData(List<List<String>> newData, boolean columnsChanged) {
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
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    @FXML
    @Override
    public void copyAction() {
        if (!checkData()) {
            return;
        }
        Data2DCopyController.open(this);
    }

    public void copyToSystemClipboard(List<String> names, List<List<String>> data) {
        try {
            if (data == null || data.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            String text = TextTools.dataText(data, ",", names, null);
            TextClipboardTools.copyToSystemClipboard(this, text);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void copyToMyBoxClipboard(List<String> names, List<List<String>> data) {
        try {
            if (data == null || data.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            copyToMyBoxClipboard2(data2D.toColumns(names), data);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void copyToMyBoxClipboard2(List<Data2DColumn> cols, List<List<String>> data) {
        try {
            if (data == null || data.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            SingletonTask copyTask = new SingletonTask<Void>(this) {

                private DataClipboard clip;

                @Override
                protected boolean handle() {
                    clip = DataClipboard.create(task, cols, data);
                    return clip != null;
                }

                @Override
                protected void whenSucceeded() {
                    DataClipboardController controller = DataClipboardController.oneOpen();
                    controller.load(clip);
                    popDone();
                }

            };
            start(copyTask, false);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void setValuesAction() {
        if (!checkData()) {
            return;
        }
        Data2DOperateController.open(this, "SetValues");
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
            Data2DPasteController.open(this, text, true);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void pasteContentInMyboxClipboard() {
        try {
            if (data2D == null) {
                return;
            }
            DataClipboardPopController.open(this);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void export() {
        if (!checkData()) {
            return;
        }
        Data2DExportController.open(this);
    }

    public void statistic() {
        if (!checkData()) {
            return;
        }
        Data2DOperateController.open(this, "Statistic");
    }

    public void percentage() {
        if (!checkData()) {
            return;
        }
        Data2DPercentageController.open(this);
    }

    @FXML
    public void transpose() {
        if (!checkData()) {
            return;
        }
        Data2DTransposeController.open(this);
    }

    @Override
    public void cleanPane() {
        try {
            dataController = null;
            data2D = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }


    /*
        get/set
     */
    public ObservableList<List<String>> getTableData() {
        return tableData;
    }

    public void setTableData(ObservableList<List<String>> tableData) {
        this.tableData = tableData;
    }

    public TableView<List<String>> getTableView() {
        return tableView;
    }

    public void setTableView(TableView<List<String>> tableView) {
        this.tableView = tableView;
    }

}
