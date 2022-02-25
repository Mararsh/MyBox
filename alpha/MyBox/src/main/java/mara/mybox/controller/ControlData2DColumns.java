package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import mara.mybox.data.StringTable;
import mara.mybox.data2d.Data2D;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.Data2DColumn;
import static mara.mybox.db.table.BaseTable.StringMaxLength;
import mara.mybox.db.table.TableColor;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.cell.TableAutoCommitCell;
import mara.mybox.fxml.cell.TableBooleanCell;
import mara.mybox.fxml.cell.TableCheckboxCell;
import mara.mybox.fxml.cell.TableColorCommitCell;
import mara.mybox.fxml.cell.TableComboBoxCell;
import mara.mybox.fxml.style.NodeStyleTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-10-16
 * @License Apache License Version 2.0
 */
public class ControlData2DColumns extends BaseTableViewController<Data2DColumn> {

    protected ControlData2D dataController;
    protected ControlData2DEditTable editController;
    protected TableData2DDefinition tableData2DDefinition;
    protected TableData2DColumn tableData2DColumn;
    protected Data2D data2D;
    protected Status status;
    protected Data2DConvertToDataBaseController convertController;

    public enum Status {
        Loaded, Modified, Applied
    }

    @FXML
    protected TableColumn<Data2DColumn, String> nameColumn, typeColumn;
    @FXML
    protected TableColumn<Data2DColumn, Boolean> editableColumn, notNullColumn, primaryColumn, autoColumn;
    @FXML
    protected TableColumn<Data2DColumn, Integer> indexColumn, lengthColumn, widthColumn;
    @FXML
    protected TableColumn<Data2DColumn, Color> colorColumn;
    @FXML
    protected FlowPane buttonsPane;
    @FXML
    protected Button renameColumnsButton, colorButton;

    public ControlData2DColumns() {
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(renameColumnsButton, new Tooltip(message("RenameAllColumns")));
            NodeStyleTools.setTooltip(colorButton, new Tooltip(message("RandomColors")));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void initColumns() {
        try {
            super.initColumns();

            indexColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Data2DColumn, Integer>, ObservableValue<Integer>>() {
                @Override
                public ObservableValue<Integer> call(TableColumn.CellDataFeatures<Data2DColumn, Integer> param) {
                    try {
                        Data2DColumn row = (Data2DColumn) param.getValue();
                        Integer v = row.getIndex();
                        if (v < 0) {
                            return null;
                        }
                        return new ReadOnlyObjectWrapper(v);
                    } catch (Exception e) {
                        return null;
                    }
                }
            });
            indexColumn.setEditable(false);

            nameColumn.setCellValueFactory(new PropertyValueFactory<>("columnName"));

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
                    if (!v.equals(column.getColumnName())) {
                        column.setColumnName(v);
                        status(Status.Modified);
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
                                status(Status.Modified);
                            }
                            return;
                        }
                    }
                }
            });

            editableColumn.setCellValueFactory(new PropertyValueFactory<>("editable"));

            notNullColumn.setCellValueFactory(new PropertyValueFactory<>("notNull"));
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
                                    if (rowIndex < 0) {
                                        return;
                                    }
                                    Data2DColumn column = tableData.get(rowIndex);
                                    if (column == null) {
                                        return;
                                    }
                                    if (value != column.isNotNull()) {
                                        column.setNotNull(value);
                                        status(Status.Modified);
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

            primaryColumn.setCellValueFactory(new PropertyValueFactory<>("isPrimaryKey"));
            primaryColumn.setCellFactory(new TableBooleanCell());

            autoColumn.setCellValueFactory(new PropertyValueFactory<>("auto"));
            autoColumn.setCellFactory(new TableBooleanCell());

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
                        status(Status.Modified);
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
                        status(Status.Modified);
                    }
                }
            });
            widthColumn.setEditable(true);
            widthColumn.getStyleClass().add("editable-column");

            TableColor tableColor = new TableColor();
            colorColumn.setCellValueFactory(new PropertyValueFactory<>("color"));
            colorColumn.setCellFactory(new Callback<TableColumn<Data2DColumn, Color>, TableCell<Data2DColumn, Color>>() {
                @Override
                public TableCell<Data2DColumn, Color> call(TableColumn<Data2DColumn, Color> param) {
                    TableColorCommitCell<Data2DColumn> cell = new TableColorCommitCell<Data2DColumn>(myController, tableColor) {
                        @Override
                        public void colorChanged(int index, Color color) {
                            if (isSettingValues || color == null || index < 0 || index >= tableData.size()) {
                                return;
                            }
                            tableData.get(index).setColor(color);
                            status(Status.Modified);
                        }
                    };
                    return cell;
                }
            });
            colorColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Data2DColumn, Color>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<Data2DColumn, Color> t) {
                    MyBoxLog.console("here");
                    if (isSettingValues || t == null) {
                        return;
                    }
                    Data2DColumn column = t.getRowValue();
                    Color v = t.getNewValue();
                    if (column == null || v == null || v.equals(column.getColor())) {
                        return;
                    }
                    MyBoxLog.console(v);
                    column.setColor(v);
                    status(Status.Modified);
                }
            });
            colorColumn.setEditable(true);
            colorColumn.getStyleClass().add("editable-column");

            checkButtons();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void setParameters(ControlData2D dataController) {
        try {
            this.dataController = dataController;
            editController = dataController.tableController;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void setParameters(Data2DConvertToDataBaseController convertController) {
        try {
            this.convertController = convertController;
            editController = convertController.editController;
            buttonsPane.getChildren().removeAll(cancelButton, okButton);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void setData(Data2D data) {
        try {
            data2D = data;
            tableData2DDefinition = editController.tableData2DDefinition;
            tableData2DColumn = editController.tableData2DColumn;
            setColumns();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void setColumns() {
        try {
            if (data2D == null) {
                return;
            }
            if (data2D.isTable() && data2D.getSheet() != null) {
                typeColumn.setEditable(false);
                typeColumn.getStyleClass().clear();

                nameColumn.setEditable(false);
                nameColumn.getStyleClass().clear();

                editableColumn.setEditable(false);
                editableColumn.setCellFactory(new TableBooleanCell());
                editableColumn.getStyleClass().clear();

                notNullColumn.setEditable(false);
                notNullColumn.setCellFactory(new TableBooleanCell());
                notNullColumn.getStyleClass().clear();

                lengthColumn.setEditable(false);
                lengthColumn.getStyleClass().clear();

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
                                            status(Status.Modified);
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
                editableColumn.setEditable(true);
                editableColumn.getStyleClass().add("editable-column");

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
                                        if (rowIndex < 0) {
                                            return;
                                        }
                                        Data2DColumn column = tableData.get(rowIndex);
                                        if (column == null) {
                                            return;
                                        }
                                        if (value != column.isNotNull()) {
                                            column.setNotNull(value);
                                            status(Status.Modified);
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
            }

            checkButtons();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
    public void tableChanged(boolean changed) {
        if (changed) {
            status(Status.Modified);
        } else {
            status(Status.Loaded);
        }
        checkButtons();
    }

    @Override
    public void checkButtons() {
        super.checkButtons();
        renameColumnsButton.setDisable(data2D == null || data2D.isTable() || tableData.isEmpty());
        addRowsButton.setDisable(data2D == null || data2D.isInternalTable());
        deleteButton.setDisable(data2D == null || data2D.isInternalTable() || tableData.isEmpty());
        colorButton.setDisable(tableData.isEmpty());
    }

    public void status(Status newStatus) {
        if (status == newStatus) {
            return;
        }
        status = newStatus;
        if (dataController != null) {
            dataController.checkStatus();
        }
        if (status == Status.Loaded || status == Status.Applied) {
            editController.notifyColumnChanged();
        }
    }

    public boolean isChanged() {
        return status == Status.Modified || status == Status.Applied;
    }

    @Override
    public Data2DColumn newData() {
        Data2DColumn column = new Data2DColumn();
        int index = data2D.newColumnIndex();
        column.setIndex(index);
        column.setColumnName(data2D.colPrefix() + (-index));
        column.setColor(FxColorTools.randomColor());
        return column;
    }

    @Override
    public Data2DColumn dataCopy(Data2DColumn data) {
        if (data == null) {
            return null;
        }
        Data2DColumn column = data.copy();
        column.setColumnName(data.getColumnName() + "_" + message("Copy"));
        column.setIndex(data2D.newColumnIndex());
        return column;
    }

    @FXML
    @Override
    public void addAction() {
        addRowsAction();
    }

    @FXML
    @Override
    public void deleteAction() {
        deleteRowsAction();
    }

    @FXML
    public void renameColumns() {
        try {
            String prefix = message(data2D.colPrefix());
            isSettingValues = true;
            for (int i = 0; i < tableData.size(); i++) {
                tableData.get(i).setColumnName(prefix + (i + 1));
            }
            tableView.refresh();
            isSettingValues = false;
            popDone();
            status(Status.Modified);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void randomColors() {
        try {
            isSettingValues = true;
            Random r = new Random();
            for (int i = 0; i < tableData.size(); i++) {
                tableData.get(i).setColor(FxColorTools.randomColor(r));
            }
            tableView.refresh();
            isSettingValues = false;
            popDone();
            status(Status.Modified);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
            if (convertController != null || editController == null) {
                return false;
            }
            StringTable validateTable = Data2DColumn.validate(tableData);
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
                for (List<String> rowValues : editController.tableData) {
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
            return editController.updateData(newTableData, true);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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

    public void setWidth(int index, int width) {
        if (index < 0 || index >= tableData.size()) {
            return;
        }
        Data2DColumn column = tableData.get(index);
        column.setWidth(width);
        tableData.set(index, column);
    }

    @FXML
    @Override
    public void addRowsAction() {
        if (data2D.isTable() && data2D.getSheet() != null) {
            Data2DColumnCreateController.open(this);
        } else {
            super.addRowsAction();
        }
    }

    public void addRow(Data2DColumn row) {
        isSettingValues = true;
        tableData.add(row);
        tableView.scrollTo(row);
        isSettingValues = false;
        tableChanged(true);
    }

}
