package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import mara.mybox.data.StringTable;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.Data2DTools;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.cell.TableBooleanCell;
import mara.mybox.fxml.cell.TableCheckboxCell;

/**
 * @Author Mara
 * @CreateDate 2021-10-16
 * @License Apache License Version 2.0
 */
public class ControlData2DColumns extends BaseData2DColumnsController {

    protected ControlData2D dataController;
    protected ControlData2DLoad tableController;
    protected TableData2DDefinition tableData2DDefinition;
    protected Data2DConvertToDataBaseController convertController;

    public ControlData2DColumns() {
    }

    @Override
    public void initColumns() {
        try {
            super.initColumns();

            primaryColumn.setCellFactory(new TableBooleanCell());

            autoColumn.setCellFactory(new TableBooleanCell());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void setParameters(ControlData2D dataController) {
        try {
            this.dataController = dataController;
            tableController = dataController.tableController;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void setParameters(Data2DConvertToDataBaseController convertController) {
        try {
            this.convertController = convertController;
            tableController = convertController.tableController;
            buttonsPane.getChildren().removeAll(cancelButton, okButton);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void setData(Data2D data) {
        try {
            data2D = data;
            tableData2DDefinition = tableController.tableData2DDefinition;
            tableData2DColumn = tableController.tableData2DColumn;
            setData2DColumns();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void setData2DColumns() {
        try {
            if (data2D == null) {
                return;
            }
            if (data2D.isTable() && data2D.getSheet() != null) {
                typeColumn.setEditable(false);
                typeColumn.getStyleClass().clear();

                nameColumn.setEditable(false);
                nameColumn.getStyleClass().clear();

                notNullColumn.setEditable(false);
                notNullColumn.setCellFactory(new TableBooleanCell());
                notNullColumn.getStyleClass().clear();

                defaultColumn.setEditable(false);
                defaultColumn.getStyleClass().clear();

                lengthColumn.setEditable(false);
                lengthColumn.getStyleClass().clear();

                if (!tableView.getColumns().contains(primaryColumn)) {
                    tableView.getColumns().add(primaryColumn);
                    tableView.getColumns().add(autoColumn);
                }

            } else {
                if (data2D.isMatrix()) {
                    typeColumn.setEditable(false);
                    typeColumn.getStyleClass().clear();
                } else {
                    typeColumn.setEditable(true);
                    typeColumn.getStyleClass().add("editable-column");
                }

                nameColumn.setEditable(true);
                nameColumn.getStyleClass().add("editable-column");

                notNullColumn.setCellFactory(new Callback<TableColumn<Data2DColumn, Boolean>, TableCell<Data2DColumn, Boolean>>() {
                    @Override
                    public TableCell<Data2DColumn, Boolean> call(TableColumn<Data2DColumn, Boolean> param) {
                        try {
                            TableCheckboxCell<Data2DColumn, Boolean> cell = new TableCheckboxCell<>() {
                                @Override
                                protected boolean getCellValue(int rowIndex) {
                                    try {
                                        return tableData.get(rowIndex).isNotNull();
                                    } catch (Exception e) {
                                        return false;
                                    }
                                }

                                @Override
                                protected void setCellValue(int rowIndex, boolean value) {
                                    try {
                                        if (isChanging || rowIndex < 0) {
                                            return;
                                        }
                                        Data2DColumn column = tableData.get(rowIndex);
                                        if (column == null) {
                                            return;
                                        }
                                        if (value != column.isNotNull()) {
                                            isChanging = true;
                                            column.setNotNull(value);
                                            status(Status.Modified);
                                            isChanging = false;
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
                notNullColumn.setEditable(true);
                notNullColumn.getStyleClass().add("editable-column");

                lengthColumn.setEditable(true);
                lengthColumn.getStyleClass().add("editable-column");

                defaultColumn.setEditable(true);
                defaultColumn.getStyleClass().add("editable-column");

                if (tableView.getColumns().contains(primaryColumn)) {
                    tableView.getColumns().remove(primaryColumn);
                    tableView.getColumns().remove(autoColumn);
                }

            }

            checkButtons();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadData() {
        if (isSettingValues) {
            return;
        }
        status = null;
        loadColumns();
        status(Status.Loaded);
    }

    public void loadColumns() {
        try {
            isSettingValues = true;
            tableData.clear();
            tableView.refresh();
            isSettingValues = false;
            if (data2D == null) {
                return;
            }
            if (data2D.isColumnsValid()) {
                isSettingValues = true;
                for (Data2DColumn column : data2D.getColumns()) {
                    tableData.add(column.cloneAll());
                }
                isSettingValues = false;
            }
            postLoadedTableData();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void checkButtons() {
        super.checkButtons();
        numberColumnsButton.setDisable(data2D == null || data2D.isTable() || tableData.isEmpty());
        addRowsButton.setDisable(data2D == null || data2D.isInternalTable());
        deleteRowsButton.setDisable(data2D == null || data2D.isInternalTable() || isNoneSelected());
    }

    @Override
    public void status(Status newStatus) {
        if (status == newStatus) {
            return;
        }
        status = newStatus;
        if (dataController != null) {
            dataController.checkStatus();
        }
    }

    @FXML
    @Override
    public void okAction() {
        if (status != Status.Modified) {
            return;
        }
        if (pickValues()) {
            status(Status.Applied);
        }
    }

    public boolean pickValues() {
        try {
            if (convertController != null || tableController == null) {
                return false;
            }
            StringTable validateTable = Data2DTools.validate(tableData);
            if (validateTable != null && !validateTable.isEmpty()) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        dataController.tabPane.getSelectionModel().select(dataController.columnsTab);
                        validateTable.htmlTable();
                    }
                });
                return false;
            }
            List<List<String>> newTableData = new ArrayList<>();
            if (!tableData.isEmpty()) {
                for (List<String> rowValues : tableController.tableData) {
                    List<String> newRow = new ArrayList<>();
                    newRow.add(rowValues.get(0));
                    for (Data2DColumn row : tableData) {
                        int col = data2D.colOrder(row.getIndex()) + 1;
                        if (col <= 0 || col >= rowValues.size()) {
                            newRow.add(null);
                        } else {
                            newRow.add(rowValues.get(col));
                        }
                    }
                    newTableData.add(newRow);
                }
            }
            List<Data2DColumn> columns = new ArrayList<>();
            for (int i = 0; i < tableData.size(); i++) {
                columns.add(tableData.get(i).cloneAll());
            }
            data2D.setColumns(columns);
            return tableController.updateData(newTableData, true);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @FXML
    @Override
    public void cancelAction() {
        if (status == Status.Modified) {
            loadColumns();
            status(Status.Applied);
        }
    }

}
