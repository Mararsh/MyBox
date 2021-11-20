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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.cell.TableAutoCommitCell;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public class ControlData2DEditTable extends ControlData2DEditTable_Operations {

    protected ControlData2DView viewController;

    @FXML
    protected TableColumn<List<String>, Integer> dataRowColumn;

    public ControlData2DEditTable() {
    }

    protected void setParameters(ControlData2DEdit editController) {
        try {
            this.editController = editController;
            dataController = editController.dataController;
            viewController = dataController.viewController;
            tableData2DDefinition = dataController.tableData2DDefinition;
            tableData2DColumn = dataController.tableData2DColumn;
            this.data2D = dataController.data2D;
            this.baseName = dataController.baseName;

            paginationBox = dataController.paginationBox;
            pageSizeSelector = dataController.pageSizeSelector;
            pageSelector = dataController.pageSelector;
            pageLabel = dataController.pageLabel;
            dataSizeLabel = dataController.dataSizeLabel;
            pagePreviousButton = dataController.pagePreviousButton;
            pageNextButton = dataController.pageNextButton;
            pageFirstButton = dataController.pageFirstButton;
            pageLastButton = dataController.pageLastButton;

            initPagination();

            data2D.setTableView(tableView);

            dataRowColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<List<String>, Integer>, ObservableValue<Integer>>() {
                @Override
                public ObservableValue<Integer> call(TableColumn.CellDataFeatures<List<String>, Integer> param) {
                    try {
                        List<String> row = (List<String>) param.getValue();
                        String value = row.get(0);
                        if (value == null) {
                            return null;
                        }
                        return new ReadOnlyObjectWrapper(Integer.valueOf(value));
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

    public void loadData() {
        try {
            makeColumns();
            loadPage(currentPage);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean checkBeforeLoadingTableData() {
        return dataController.needSave();
    }

    @Override
    public long readDataSize() {
        return data2D.getDataSize();
    }

    @Override
    public List<List<String>> readPageData() {
        data2D.setTask(task);
        return data2D.readPageData();
    }

    @Override
    protected void countPagination(long page) {
        super.countPagination(page);
        data2D.setPageSize(pageSize);
        data2D.setPagesNumber(pagesNumber);
        data2D.setCurrentPage(currentPage);
        data2D.setStartRowOfCurrentPage(startRowOfCurrentPage);
    }

    @Override
    public void postLoadedTableData() {
        super.postLoadedTableData();
        data2D.setTask(null);
        data2D.notifyPageLoaded();
    }

    @Override
    protected void setPagination() {
        try {
            if (data2D == null) {
                return;
            }
            if (!data2D.isTotalRead()) {
                paginationBox.setVisible(false);
                return;
            }
            paginationBox.setVisible(true);
            super.setPagination();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }

    }

    protected void refreshPagination() {
        countPagination(currentPage);
        setPagination();
        updateSizeLabel();
    }

    public void makeColumns() {
        try {
            tableData.clear();
            tableView.getColumns().remove(2, tableView.getColumns().size());
            tableView.setItems(tableData);

            if (data2D == null || !data2D.isColumnsValid()) {
                return;
            }
            data2D.setTableView(tableView);

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

                tableColumn.getStyleClass().add("editable-column");

                tableView.getColumns().add(tableColumn);
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void tableChanged(boolean changed) {
        data2D.setTableChanged(changed);
        updateSizeLabel();
        dataController.checkStatus();
        viewController.loadData();
    }

    @FXML
    @Override
    public void deleteAction() {
        List<List<String>> selected = tableView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        isSettingValues = true;
        tableData.removeAll(selected);
        isSettingValues = false;
        tableChanged(true);
    }

    @FXML
    @Override
    public void copyAction() {

    }

    @FXML
    public void insertAction() {

    }

    public boolean applyColumns() {
        try {
            List<List<String>> newData = new ArrayList<>();
            if (data2D.columnsNumber() > 0) {
                for (List<String> rowValues : tableData) {
                    List<String> newRow = new ArrayList<>();
                    newRow.add(rowValues.get(0));
                    for (int pageCol = 0; pageCol < data2D.columnsNumber(); pageCol++) {
                        newRow.add(data2D.pageCell(rowValues, pageCol));
                    }
                    newData.add(newRow);
                }
            }
            makeColumns();
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

            MenuItem menu;

            menu = new MenuItem(message("Copy"));
            menu.setOnAction((ActionEvent event) -> {
                copyAction();
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Pop"));
            menu.setOnAction((ActionEvent event) -> {
                popAction();
            });
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
            editController = null;
            dataController = null;
            data2D = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }
}
