package mara.mybox.controller;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;
import mara.mybox.data.StringTable;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataClipboard;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataFileExcel;
import mara.mybox.data2d.DataFileText;
import mara.mybox.data2d.DataMatrix;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.cell.TableAutoCommitCell;
import mara.mybox.tools.DoubleMatrixTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-12-17
 * @License Apache License Version 2.0
 */
public class ControlData2DLoad extends BaseTableViewController<List<String>> {

    protected ControlData2D dataController;
    protected Data2D data2D;
    protected TableData2DDefinition tableData2DDefinition;
    protected TableData2DColumn tableData2DColumn;
    protected char copyDelimiter = ',';
    protected boolean readOnly, notUpdateTitle;
    protected SimpleBooleanProperty statusNotify;
    protected Label dataLabel;

    @FXML
    protected TableColumn<List<String>, Integer> dataRowColumn;

    public ControlData2DLoad() {
        statusNotify = new SimpleBooleanProperty(false);
        readOnly = true;
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
            MyBoxLog.error(e.toString());
        }
    }

    public void setData(Data2D data) {
        try {
            data2D = data;
            tableData2DDefinition = data2D.getTableData2DDefinition();
            tableData2DColumn = data2D.getTableData2DColumn();

            if (paginationPane != null) {
                showPaginationPane(!data2D.isTmpData() && !data2D.isMatrix());
            }
            data2D.setLoadController(this);
            validateData();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        data
     */
    public void loadDef(Data2DDefinition def) {
        if (def == null) {
            loadNull();
            return;
        }
        if (data2D == null || data2D.getType() != def.getType()) {
            data2D = Data2D.create(def.getType());
            data2D.cloneAll(def);
        } else if (data2D != def) {
            data2D.resetData();
            data2D.cloneAll(def);
        }
        readDefinition();
    }

    public synchronized void readDefinition() {
        if (data2D == null) {
            loadNull();
            return;
        }
        if (!checkInvalidFile()) {
            return;
        }
        if (dataController != null) {
            dataController.resetStatus();
        } else {
            resetStatus();
        }
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try ( Connection conn = DerbyBase.getConnection()) {
                    data2D.setTask(task);
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
                data2D.setTask(null);
                task = null;
                resetView(false);
                if (dataController != null) {
                    dataController.loadData(data2D);   // Load data whatever
                } else {
                    loadData();
                }
            }

        };
        start(task);
    }

    public synchronized void loadData() {
        try {
            makeColumns();
            if (!validateData()) {
                dataSizeLoaded = true;
                tableChanged(false);
                notifyLoaded();
                return;
            }
            dataSizeLoaded = false;
            loadPage(data2D.getCurrentPage());
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean checkInvalidFile() {
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
                if (dataController != null) {
                    dataController.loadNull();
                } else {
                    if (data2D != null) {
                        data2D.resetData();
                    }
                    loadData();
                }
            }
        });
    }

    public synchronized void loadTmpData(List<Data2DColumn> cols, List<List<String>> data) {
        try {
            if (data2D == null) {
                return;
            }
            if (dataController != null) {
                dataController.resetStatus();
            } else {
                resetStatus();
            }
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
            StringTable validateTable = Data2DColumn.validate(columns);
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
            resetView(false);
            if (dataController != null) {
                dataController.setData(data2D);
            }
            displayTmpData(rows);
            if (dataController != null) {
                dataController.attributesController.loadData();
                dataController.columnsController.loadData();
            }
            if (validateTable != null && !validateTable.isEmpty()) {
                validateTable.htmlTable();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public synchronized boolean displayTmpData(List<List<String>> newData) {
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
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public void loadCSVFile(File csvFile) {
        if (csvFile == null || !csvFile.exists()) {
            return;
        }
        loadCSVData(new DataFileCSV(csvFile));
    }

    public void loadCSVData(DataFileCSV csvData) {
        if (csvData == null || csvData.getFile() == null || !csvData.getFile().exists()) {
            return;
        }
        task = new SingletonTask<Void>(this) {
            private Data2D fileData;

            @Override
            protected boolean handle() {
                try {
                    if (data2D.getType() == Data2D.Type.Texts) {
                        fileData = new DataFileText();
                        fileData.setFile(csvData.getFile())
                                .setDelimiter(csvData.getDelimiter())
                                .setCharset(csvData.getCharset());
                    } else {
                        switch (data2D.getType()) {
                            case CSV:
                                fileData = csvData;
                                break;
                            case Excel: {
                                DataFileExcel excelData = DataFileExcel.toExcel(task, csvData);
                                fileData = excelData;
                                break;
                            }
                            case DatabaseTable: {
                                DataTable dataTable = DataTable.toTable(task, csvData, true);
                                fileData = dataTable;
                                break;
                            }
                            case MyBoxClipboard: {
                                DataClipboard clip = DataClipboard.toClip(task, csvData);
                                fileData = clip;
                                break;
                            }
                            case Matrix: {
                                DataMatrix matrix = DataMatrix.toMatrix(task, csvData);
                                fileData = matrix;
                                break;
                            }
                        }
                    }
                    return fileData != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                loadDef(fileData);
                if (dataController != null && dataController.topController != null) {
                    dataController.topController.refreshAction();
                }
            }
        };
        start(task);
    }

    public void loadMatrix(double[][] matrix) {
        if (matrix == null || matrix.length == 0) {
            loadNull();
            return;
        }
        loadTmpData(data2D.tmpColumns(matrix[0].length), DoubleMatrixTools.toList(matrix));
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
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public boolean validateData() {
        boolean invalid = data2D == null || !data2D.isColumnsValid();
        thisPane.setDisable(invalid);
        return !invalid;
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
        statusNotify.set(!statusNotify.get());
        if (dataController != null) {
            dataController.notifyStatus();
        }
    }

    public void notifySaved() {
        notifyStatus();
        if (dataController != null) {
            dataController.notifySaved();
        }
    }

    @Override
    public void notifyLoaded() {
        notifyStatus();
        loadedNotify.set(!loadedNotify.get());
        if (data2D.getFile() != null) {
            recordFileOpened(data2D.getFile());
        }
        if (dataController != null) {
            dataController.notifyLoaded();
        }
    }

    @FXML
    public void renameAction(BaseTableViewController parent, int index, Data2DDefinition targetData) {
        String newName = PopTools.askValue(getTitle(), message("CurrentName") + ":" + targetData.getDataName(),
                message("NewName"), targetData.getDataName() + "m");
        if (newName == null || newName.isBlank()) {
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }
        synchronized (this) {
            task = new SingletonTask<Void>(this) {
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
                        if (dataController != null) {
                            dataController.attributesController.updateDataName();
                        }
                        if (parent != null) {
                            parent.updateStatus();
                        }
                        updateStatus();
                    }

                }

            };
            start(task);
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
            data2D.setLoadController(this);

            if (!validateData()) {
                return;
            }
            List<Data2DColumn> columns = data2D.getColumns();
            for (int i = 0; i < columns.size(); i++) {
                Data2DColumn dataColumn = columns.get(i);
                String name = dataColumn.getColumnName();
                TableColumn tableColumn = new TableColumn<List<String>, String>(name);
                tableColumn.setPrefWidth(dataColumn.getWidth());
                tableColumn.setEditable(!readOnly && dataColumn.isEditable() && !dataColumn.isId());
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

                if (tableColumn.isEditable()) {
                    tableColumn.setCellFactory(new Callback<TableColumn<List<String>, String>, TableCell<List<String>, String>>() {
                        @Override
                        public TableCell<List<String>, String> call(TableColumn<List<String>, String> param) {
                            try {
                                TableAutoCommitCell<List<String>, String> cell
                                        = new TableAutoCommitCell<List<String>, String>(new DefaultStringConverter()) {
                                    @Override
                                    public void updateItem(String item, boolean empty) {
                                        setStyle(null);
                                        try {
                                            setStyle(data2D.getStyle(getTableRow().getIndex(), name));
                                        } catch (Exception e) {
                                        }
                                        super.updateItem(item, empty);
                                        if (empty || item == null) {
                                            setText(null);
                                            setGraphic(null);
                                        }
                                    }

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
                                            if (row == null || row.size() <= col) {
                                                cancelEdit();
                                                return;
                                            }
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
                    tableColumn.getStyleClass().add("editable-column");
                } else {
                    tableColumn.setCellFactory(new Callback<TableColumn<List<String>, String>, TableCell<List<String>, String>>() {
                        @Override
                        public TableCell<List<String>, String> call(TableColumn<List<String>, String> param) {
                            try {
                                TableCell<List<String>, String> cell = new TableCell<List<String>, String>() {
                                    @Override
                                    public void updateItem(String item, boolean empty) {
                                        setStyle(null);
                                        try {
                                            setStyle(data2D.getStyle(getTableRow().getIndex(), name));
                                        } catch (Exception e) {
                                        }
                                        super.updateItem(item, empty);
                                        if (empty || item == null) {
                                            setText(null);
                                            setGraphic(null);
                                            return;
                                        }
                                        setText(item);
                                    }
                                };

                                return cell;
                            } catch (Exception e) {
                                return null;
                            }
                        }
                    });

                }

                if (dataController != null) {
                    tableColumn.widthProperty().addListener(new ChangeListener<Number>() {
                        @Override
                        public void changed(ObservableValue<? extends Number> o, Number ov, Number nv) {
                            int w = nv.intValue();
                            dataColumn.setWidth(w);
                            dataController.columnsController.setWidth(col - 1, w);
                        }
                    });
                }

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
    public List<List<String>> readPageData(Connection conn) {
        data2D.setTask(task);
        return data2D.readPageData(conn);
    }

    @Override
    protected void countPagination(Connection conn, long page) {
        if (data2D.isMatrix()) {
            pageSize = Integer.MAX_VALUE;
            dataSize = data2D.getDataSize();
            pagesNumber = 1;
            currentPage = 0;
            startRowOfCurrentPage = 0;
            dataSizeLoaded = true;
        } else {
            pageSize = UserConfig.getInt(baseName + "PageSize", 50);
            super.countPagination(conn, page);
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
            data2D.setTask(null);
        }
    }

    @Override
    public long readDataSize(Connection conn) {
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
        synchronized (this) {
            if (backgroundTask != null) {
                backgroundTask.cancel();
            }
            data2D.setDataSize(0);
            dataSizeLoaded = false;
            if (paginationPane != null) {
                paginationPane.setVisible(false);
            }
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
                    afterLoaded(true);
                }

            };
            start(backgroundTask, false);
        }
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
        countPagination(null, currentPage);
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
            MyBoxLog.debug(e.toString());
        }

    }

    protected void showPaginationPane(boolean show) {
        if (paginationPane == null) {
            return;
        }
        paginationPane.setVisible(show);
        if (dataController != null) {
            if (show) {
                if (!dataController.thisPane.getChildren().contains(paginationPane)) {
                    dataController.thisPane.getChildren().add(paginationPane);
                }
            } else {
                if (dataController.thisPane.getChildren().contains(paginationPane)) {
                    dataController.thisPane.getChildren().remove(paginationPane);
                }
            }
        } else {
            if (show) {
                if (!thisPane.getChildren().contains(paginationPane)) {
                    thisPane.getChildren().add(paginationPane);
                }
            } else {
                if (thisPane.getChildren().contains(paginationPane)) {
                    thisPane.getChildren().remove(paginationPane);
                }
            }
        }
    }

    @Override
    public void addRows(int index, int number) {
        data2D.moveDownStyles(index, number);
        super.addRows(index, number);
    }

    @FXML
    @Override
    public void deleteRowsAction() {
        List<Integer> selected = tableView.getSelectionModel().getSelectedIndices();
        if (selected == null || selected.isEmpty()) {
            data2D.getStyles().clear();
            return;
        }
        for (Integer index : selected) {
            data2D.moveUpStyles(index);
        }
        super.deleteRowsAction();
    }

    @Override
    protected void afterClear() {
        data2D.getStyles().clear();
        super.afterClear();
    }

    /*
        interface
     */
    @Override
    public void updateStatus() {
        super.updateStatus();
        myStage = getMyStage();
        if (data2D != null) {
            String name = data2D.displayName();
            if (dataLabel != null) {
                dataLabel.setText(name);
            }
            if (myStage != null && !notUpdateTitle) {
                String title = getRootBaseTitle() + " : " + name;
                if (data2D.isTableChanged()) {
                    title += " *";
                }
                myStage.setTitle(title);
            }
        } else {
            if (dataLabel != null) {
                dataLabel.setText("");
            }
            if (myStage != null && !notUpdateTitle) {
                myStage.setTitle(baseTitle);
            }
        }
        validateData();
    }

    @Override
    public void cleanPane() {
        try {
            statusNotify = null;
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

    public void setNotUpdateTitle(boolean notUpdateTitle) {
        this.notUpdateTitle = notUpdateTitle;
    }

}
