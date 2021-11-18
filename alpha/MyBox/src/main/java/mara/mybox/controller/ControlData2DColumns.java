package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import mara.mybox.data.Data2D;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.Data2DColumn;
import static mara.mybox.db.table.BaseTable.StringMaxLength;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.cell.TableAutoCommitCell;
import mara.mybox.fxml.cell.TableCheckboxCell;
import mara.mybox.fxml.cell.TableComboBoxCell;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-10-16
 * @License Apache License Version 2.0
 */
public class ControlData2DColumns extends BaseTableViewController<Data2DColumn> {

    protected ControlData2D dataController;
    protected TableData2DDefinition tableData2DDefinition;
    protected TableData2DColumn tableData2DColumn;
    protected Data2D data2D;
    protected boolean changed;

    @FXML
    protected TableColumn<Data2DColumn, String> nameColumn, typeColumn;
    @FXML
    protected TableColumn<Data2DColumn, Boolean> editableColumn, notNullColumn;
    @FXML
    protected TableColumn<Data2DColumn, Integer> indexColumn, lengthColumn, widthColumn;

    public ControlData2DColumns() {
    }

    @Override
    protected void initColumns() {
        try {
            super.initColumns();

            indexColumn.setCellValueFactory(new PropertyValueFactory<>("index"));
            indexColumn.setEditable(false);

            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            nameColumn.setCellFactory(TableAutoCommitCell.forStringColumn());
            nameColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Data2DColumn, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<Data2DColumn, String> t) {
                    if (t == null) {
                        return;
                    }
                    Data2DColumn column = t.getRowValue();
                    String v = t.getNewValue();
                    if (column == null || v == null) {
                        return;
                    }
                    if (!v.equals(column.getName())) {
                        column.setName(v);
                        changed(true);
                    }
                }
            });

            typeColumn.setCellValueFactory(new PropertyValueFactory<>("typeString"));
            ObservableList<String> types = FXCollections.observableArrayList();
            for (ColumnType type : Data2DColumn.editTypes()) {
                types.add(message(type.name()));
            }
            typeColumn.setCellFactory(TableComboBoxCell.forTableColumn(types));
            typeColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Data2DColumn, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<Data2DColumn, String> t) {
                    if (t == null) {
                        return;
                    }
                    Data2DColumn column = t.getRowValue();
                    String v = t.getNewValue();
                    if (column == null || v == null) {
                        return;
                    }
                    for (ColumnType type : Data2DColumn.editTypes()) {
                        if (type.name().equals(v) || message(type.name()).equals(v)) {
                            if (type != column.getType()) {
                                column.setType(type);
                                changed(true);
                            }
                            return;
                        }
                    }
                }
            });

            editableColumn.setCellFactory(new Callback<TableColumn<Data2DColumn, Boolean>, TableCell<Data2DColumn, Boolean>>() {
                @Override
                public TableCell<Data2DColumn, Boolean> call(TableColumn<Data2DColumn, Boolean> param) {
                    try {
                        TableCheckboxCell<Data2DColumn, Boolean> cell = new TableCheckboxCell<>() {
                            @Override
                            protected boolean getCellValue(int rowIndex) {
                                try {
                                    return tableData.get(rowIndex).isEditable();
                                } catch (Exception e) {
                                    return false;
                                }
                            }

                            @Override
                            protected void setCellValue(int rowIndex, boolean value) {
                                try {
                                    if (rowIndex < 0) {
                                        return;
                                    }
                                    Data2DColumn column = tableData.get(rowIndex);
                                    if (column == null) {
                                        return;
                                    }
                                    if (value != column.isEditable()) {
                                        column.setEditable(value);
                                        changed(true);
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

            notNullColumn.setCellFactory(new Callback<TableColumn<Data2DColumn, Boolean>, TableCell<Data2DColumn, Boolean>>() {
                @Override
                public TableCell<Data2DColumn, Boolean> call(TableColumn<Data2DColumn, Boolean> param) {
                    try {
                        TableCheckboxCell<Data2DColumn, Boolean> cell = new TableCheckboxCell<>() {
                            @Override
                            protected boolean getCellValue(int rowIndex) {
                                try {
                                    return tableData.get(rowIndex).isEditable();
                                } catch (Exception e) {
                                    return false;
                                }
                            }

                            @Override
                            protected void setCellValue(int rowIndex, boolean value) {
                                try {
                                    if (rowIndex < 0) {
                                        return;
                                    }
                                    Data2DColumn column = tableData.get(rowIndex);
                                    if (column == null) {
                                        return;
                                    }
                                    if (value != column.isEditable()) {
                                        column.setEditable(value);
                                        changed(true);
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

            lengthColumn.setCellValueFactory(new PropertyValueFactory<>("length"));
            lengthColumn.setCellFactory(TableAutoCommitCell.forIntegerColumn());
            lengthColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Data2DColumn, Integer>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<Data2DColumn, Integer> t) {
                    if (t == null) {
                        return;
                    }
                    Data2DColumn column = t.getRowValue();
                    Integer v = t.getNewValue();
                    if (column == null || v == null) {
                        return;
                    }
                    if (v != column.getLength()) {
                        if (v < 0 || v > StringMaxLength) {
                            v = StringMaxLength;
                        }
                        column.setLength(v);
                        changed(true);
                    }
                }
            });

            widthColumn.setCellValueFactory(new PropertyValueFactory<>("width"));
            widthColumn.setCellFactory(TableAutoCommitCell.forIntegerColumn());
            widthColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Data2DColumn, Integer>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<Data2DColumn, Integer> t) {
                    if (t == null) {
                        return;
                    }
                    Data2DColumn column = t.getRowValue();
                    Integer v = t.getNewValue();
                    if (column == null || v == null) {
                        return;
                    }
                    if (v != column.getWidth()) {
                        column.setWidth(v);
                        changed(true);
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void setParameters(ControlData2D dataController) {
        try {
            this.dataController = dataController;
            tableData2DDefinition = dataController.tableData2DDefinition;
            tableData2DColumn = dataController.tableData2DColumn;
            baseName = dataController.baseName;
            data2D = dataController.data2D;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void loadTableData() {
        try {
            tableData.clear();
            tableView.refresh();
            changed(false);
            if (data2D == null) {
                return;
            }
            if (data2D.isColumnsValid()) {
                for (Data2DColumn column : data2D.getColumns()) {
                    tableData.add(column.cloneBase());
                }
            }
            postLoadedTableData();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void changed(boolean changed) {
        this.changed = changed;
        dataController.columnsTab.setText(message("Columns") + (changed ? "*" : ""));
    }

    @FXML
    @Override
    public void copyAction() {
        List<Data2DColumn> selected = tableView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        for (Data2DColumn column : selected) {
            tableData.add(column.cloneBase());
        }
    }

    @FXML
    public void insertAction() {
        try {

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void trimAction() {
        try {
            String prefix = message(data2D.colPrefix());
            for (int i = 0; i < tableData.size(); i++) {
                tableData.get(i).setName(prefix + (i + 1));
            }
            tableView.refresh();
            changed(true);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public boolean save(SingletonTask saveTask, Connection conn) {
        if (data2D == null || conn == null) {
            return false;
        }
        try {
            long d2did = data2D.getD2did();
            if (d2did < 0) {
                return false;
            }
            StringTable validateTable = Data2DColumn.validate(tableData);
            if (validateTable != null && !validateTable.isEmpty()) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        validateTable.htmlTable();
                    }
                });
                return false;
            }
            List<Data2DColumn> columns = new ArrayList<>();
            for (int i = 0; i < tableData.size(); i++) {
                Data2DColumn column = tableData.get(i);
                columns.add(column);
            }
            tableData2DColumn.save(conn, d2did, columns);
            data2D.setColumns(columns);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (saveTask != null) {
                saveTask.setError(e.toString());
            }
            return false;
        }
    }

}
