package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.input.Clipboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;
import mara.mybox.data.Data2D;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.StyleTools;
import mara.mybox.fxml.cell.TableAutoCommitCell;
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

            paginationBox = dataController.paginationBox;
            pageSizeSelector = dataController.pageSizeSelector;
            pageSelector = dataController.pageSelector;
            pageLabel = dataController.pageLabel;
            dataSizeLabel = dataController.dataSizeLabel;
            pagePreviousButton = dataController.pagePreviousButton;
            pageNextButton = dataController.pageNextButton;
            pageFirstButton = dataController.pageFirstButton;
            pageLastButton = dataController.pageLastButton;

            if (data2D.isMatrix()) {
                dataController.thisPane.getChildren().remove(paginationBox);
            } else {
                initPagination();
            }

            data2D.setTableView(tableView);

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
                return;
            }
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
        if (data2D == null || dataSizeLoaded || data2D.isMatrix()) {
            return;
        }
        synchronized (this) {
            if (backgroundTask != null) {
                backgroundTask.cancel();
            }
            data2D.setDataSize(0);
            dataController.paginationBox.setVisible(false);
            backgroundTask = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    data2D.setBackgroundTask(backgroundTask);
                    return data2D.readTotal() >= 0;
                }

                @Override
                protected void whenSucceeded() {
                    dataSizeLoaded = true;
                    dataController.checkStatus();
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
                    refreshPagination();
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
            if (data2D == null || data2D.isMatrix()) {
                paginationBox.setVisible(false);
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
            data2D.setTableView(tableView);

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
    public void tableChanged(boolean changed) {
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

    @FXML
    @Override
    public void copyAction() {
        DataCopyController.open(this);
    }

    @FXML
    public void setValuesAction() {
        if (!checkData()) {
            return;
        }
        DataSetValuesController.open(this);
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
            DataPasteController.open(this, text, true);
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

    public void afterSaved() {
        try {
            isSettingValues = true;
            data2D.setEndRowOfCurrentPage(data2D.getStartRowOfCurrentPage() + tableData.size());
            long offset = startRowOfCurrentPage + 1;
            for (int i = 0; i < tableData.size(); i++) {
                List<String> row = tableData.get(i);
                row.set(0, (offset + i) + "");
            }
            for (int i = 0; i < data2D.columnsNumber(); i++) {
                Data2DColumn dataColumn = data2D.getColumns().get(i);
                TableColumn tableColumn = tableView.getColumns().get(i + 2);
                tableColumn.setUserData(dataColumn.getIndex());
            }
            tableView.refresh();
            isSettingValues = false;
            tableChanged(false);
            dataController.resetStatus();
            dataController.checkStatus();
            dataSizeLoaded = data2D.isMatrix();
            loadDataSize();
            dataController.notifySaved();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public synchronized void createTable() {
        data2D.resetData();

        List<Data2DColumn> columns = new ArrayList<>();
        for (int col = 1; col <= 3; col++) {
            Data2DColumn column = new Data2DColumn(data2D.colPrefix() + col, data2D.defaultColumnType());
            columns.add(column);
        }
        data2D.setColumns(columns);
        makeColumns();

        List<List<String>> rows = new ArrayList<>();
        for (int r = 1; r <= 3; r++) {
            List<String> row = new ArrayList<>();
            row.add("-1");
            for (int col = 0; col < 3; col++) {
                row.add(data2D.defaultColValue());
            }
            rows.add(row);
        }
        isSettingValues = true;
        tableData.setAll(rows);
        isSettingValues = false;

        dataSize = 3;
        pagesNumber = 1;
        currentPage = startRowOfCurrentPage = 0;
        data2D.setEndRowOfCurrentPage(dataSize);
        data2D.setDataSize(dataSize);
        tableChanged(false);

        dataController.attributesController.loadData();
        dataController.columnsController.loadData();
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

            menu = new MenuItem(message("Recover"), StyleTools.getIconImage("iconRecover.png"));
            menu.setOnAction((ActionEvent event) -> {
                dataController.parentController.recoverAction();
            });
            menu.setDisable(invalidData);
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(message("CreateData"), StyleTools.getIconImage("iconFileAdd.png"));
            menu.setOnAction((ActionEvent event) -> {
                dataController.parentController.createAction();
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("LoadContentInSystemClipboard"), StyleTools.getIconImage("iconImageSystem.png"));
            menu.setOnAction((ActionEvent event) -> {
                dataController.loadContentInSystemClipboard();
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("SaveAs"), StyleTools.getIconImage("iconSaveAs.png"));
            menu.setOnAction((ActionEvent event) -> {
                dataController.parentController.saveAsAction();
            });
            menu.setDisable(invalidData || data2D.isMatrix());
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("PopupClose"));
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

    @Override
    public void cleanPane() {
        try {
            dataController = null;
            data2D = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }
}
