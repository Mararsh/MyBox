package mara.mybox.controller;

import java.util.List;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;
import mara.mybox.data.Data2D;
import mara.mybox.data.DataClipboard;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.StyleTools;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.fxml.cell.TableAutoCommitCell;
import mara.mybox.tools.TextTools;
import static mara.mybox.value.Languages.message;

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

    @FXML
    protected TableColumn<List<String>, Integer> dataRowColumn;

    public ControlData2DEditTable() {
    }

    protected void setParameters(ControlData2DEdit editController) {
        try {
            dataController = editController.dataController;
            tableData2DDefinition = dataController.tableData2DDefinition;
            tableData2DColumn = dataController.tableData2DColumn;
            data2D = dataController.data2D;

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

            if (data2D.isMatrix()) {
                dataController.thisPane.getChildren().remove(paginationPane);
            } else {
                initPagination();
            }

            data2D.setTableController(this);

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

            checkData();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadData() {
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
        return dataController.checkBeforeNextAction();
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

    @Override
    public void loadDataSize() {
        if (data2D == null || dataSizeLoaded) {
            return;
        }
        if (data2D.isMatrix()) {
            dataSizeLoaded = true;
            return;
        }
        synchronized (this) {
            if (backgroundTask != null) {
                backgroundTask.cancel();
            }
            data2D.setDataSize(0);
            dataSizeLoaded = false;
            paginationPane.setVisible(false);
            saveButton.setDisable(true);
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
                    dataController.checkStatus();
                    refreshPagination();
                    saveButton.setDisable(false);
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
            if (data2D == null || data2D.isMatrix() || data2D.isNewData() || !dataSizeLoaded) {
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

        dataController.textController.loadData();
        dataController.viewController.loadData();
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
    public void popFunctionsMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            boolean invalidData = data2D == null || !data2D.isColumnsValid();
            boolean empty = invalidData || tableData.isEmpty();
            MenuItem menu;

            menu = new MenuItem(message("Save"), StyleTools.getIconImage("iconSave.png"));
            menu.setOnAction((ActionEvent event) -> {
                dataController.parentController.saveAction();
            });
            menu.setDisable(invalidData || !dataSizeLoaded);
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Recover"), StyleTools.getIconImage("iconRecover.png"));
            menu.setOnAction((ActionEvent event) -> {
                dataController.parentController.recoverAction();
            });
            menu.setDisable(invalidData || !dataSizeLoaded);
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(message("SetValues"), StyleTools.getIconImage("iconEqual.png"));
            menu.setOnAction((ActionEvent event) -> {
                setValuesAction();
            });
            menu.setDisable(empty);
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Copy"), StyleTools.getIconImage("iconCopy.png"));
            menu.setOnAction((ActionEvent event) -> {
                copyAction();
            });
            menu.setDisable(empty);
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("PasteContentInSystemClipboard"), StyleTools.getIconImage("iconPasteSystem.png"));
            menu.setOnAction((ActionEvent event) -> {
                pasteContentInSystemClipboard();
            });
            menu.setDisable(invalidData);
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("PasteContentInDataClipboard"), StyleTools.getIconImage("iconPaste.png"));
            menu.setOnAction((ActionEvent event) -> {
                pasteContentInMyboxClipboard();
            });
            menu.setDisable(invalidData);
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(message("Export"), StyleTools.getIconImage("iconExport.png"));
            menu.setOnAction((ActionEvent event) -> {
                export();
            });
            menu.setDisable(empty);
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Transpose"), StyleTools.getIconImage("iconRotateRight.png"));
            menu.setOnAction((ActionEvent event) -> {
                transpose();
            });
            menu.setDisable(empty);
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(message("CreateData"), StyleTools.getIconImage("iconCreateData.png"));
            menu.setOnAction((ActionEvent event) -> {
                dataController.parentController.createAction();
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("LoadContentInSystemClipboard"), StyleTools.getIconImage("iconImageSystem.png"));
            menu.setOnAction((ActionEvent event) -> {
                dataController.loadContentInSystemClipboard();
            });
            popMenu.getItems().add(menu);

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

            LocateTools.locateBelow((Region) mouseEvent.getSource(), popMenu);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
            SingletonTask copyTask = new SingletonTask<Void>(this) {

                private DataClipboard clip;

                @Override
                protected boolean handle() {
                    clip = DataClipboard.create(task, names, data);
                    return clip != null;
                }

                @Override
                protected void whenSucceeded() {
                    DataClipboardController controller = DataClipboardController.oneOpen();
                    controller.clipboardController.dataController.loadMatrix(clip);
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
        Data2DSetValuesController.open(this);
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
        if (!checkData() || !dataController.checkBeforeNextAction()) {
            return;
        }
        Data2DExportController.open(this);
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
