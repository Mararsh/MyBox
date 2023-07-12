package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
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
import mara.mybox.fxml.cell.TableColorEditCell;
import mara.mybox.fxml.cell.TableDataColumnCell;
import mara.mybox.fxml.cell.TableTextAreaEditCell;
import mara.mybox.fxml.style.NodeStyleTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-10-16
 * @License Apache License Version 2.0
 */
public class ControlData2DColumns extends BaseTablePagesController<Data2DColumn> {

    protected ControlData2D dataController;
    protected ControlData2DLoad tableController;
    protected TableData2DDefinition tableData2DDefinition;
    protected TableData2DColumn tableData2DColumn;
    protected Data2D data2D;
    protected Status status;
    protected Data2DConvertToDataBaseController convertController;

    public enum Status {
        Loaded, Modified, Applied
    }

    @FXML
    protected TableColumn<Data2DColumn, String> nameColumn, typeColumn, defaultColumn, descColumn, formatColumn;
    @FXML
    protected TableColumn<Data2DColumn, Boolean> editableColumn, notNullColumn, primaryColumn, autoColumn;
    @FXML
    protected TableColumn<Data2DColumn, Integer> indexColumn, lengthColumn, widthColumn;
    @FXML
    protected TableColumn<Data2DColumn, Color> colorColumn;
    @FXML
    protected FlowPane buttonsPane;
    @FXML
    protected Button renameColumnsButton, randomColorsButton;

    public ControlData2DColumns() {
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(renameColumnsButton, new Tooltip(message("RenameAllColumns")));
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
            typeColumn.setCellFactory(TableDataColumnCell.create(this));
            typeColumn.setEditable(true);
            formatColumn.getStyleClass().add("editable-column");

            editableColumn.setCellValueFactory(new PropertyValueFactory<>("editable"));
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
                                    if (column == null || column.isAuto()) {
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

            formatColumn.setCellValueFactory(new PropertyValueFactory<>("formatDisplay"));
            formatColumn.setCellFactory(TableDataColumnCell.create(this));
            formatColumn.setEditable(true);
            formatColumn.getStyleClass().add("editable-column");

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
                    TableColorEditCell<Data2DColumn> cell = new TableColorEditCell<Data2DColumn>(myController, tableColor) {
                        @Override
                        public void colorChanged(int index, Color color) {
                            if (isSettingValues || color == null || index < 0 || index >= tableData.size()) {
                                return;
                            }
                            if (color.equals(tableData.get(index).getColor())) {
                                return;
                            }
                            tableData.get(index).setColor(color);
                            status(Status.Modified);
                        }
                    };
                    return cell;
                }
            });
            colorColumn.setEditable(true);
            colorColumn.getStyleClass().add("editable-column");

            defaultColumn.setCellValueFactory(new PropertyValueFactory<>("defaultValue"));
            defaultColumn.setCellFactory(TableAutoCommitCell.forStringColumn());
            defaultColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Data2DColumn, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<Data2DColumn, String> t) {
                    if (t == null) {
                        return;
                    }
                    Data2DColumn column = t.getRowValue();
                    if (column == null) {
                        return;
                    }
                    String v = t.getNewValue();
                    if ((v == null && column.getDefaultValue() != null)
                            || (v != null && !v.equals(column.getDefaultValue()))) {
                        column.setDefaultValue(v);
                        status(Status.Modified);
                    }
                }
            });

            descColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
            descColumn.setCellFactory(TableTextAreaEditCell.create(myController, null));
            descColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Data2DColumn, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<Data2DColumn, String> e) {
                    if (e == null) {
                        return;
                    }
                    int colIndex = e.getTablePosition().getRow();
                    if (colIndex < 0 || colIndex >= tableData.size()) {
                        return;
                    }
                    Data2DColumn column = tableData.get(colIndex);
                    column.setDescription(e.getNewValue());
                    tableData.set(colIndex, column);
                    status(Status.Modified);
                }
            });
            descColumn.setEditable(true);
            descColumn.getStyleClass().add("editable-column");

            checkButtons();

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
            setColumns();
        } catch (Exception e) {
            MyBoxLog.error(e);
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
    public void tableChanged(boolean changed) {
        if (isSettingValues) {
            return;
        }
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
        deleteRowsButton.setDisable(data2D == null || data2D.isInternalTable() || isNoneSelected());
        randomColorsButton.setDisable(tableData.isEmpty());
    }

    public void status(Status newStatus) {
        if (status == newStatus) {
            return;
        }
        status = newStatus;
        if (dataController != null) {
            dataController.checkStatus();
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
    public void deleteRowsAction() {
        List<Data2DColumn> selected = selectedItems();
        if (selected == null || selected.isEmpty()) {
            deleteAllRows();
            return;
        }
        for (Data2DColumn column : selected) {
            if (column.isIsPrimaryKey()) {
                popError(message("PrimaryKeysCanNotDeleted"));
                return;
            }
        }
        isSettingValues = true;
        tableData.removeAll(selected);
        isSettingValues = false;
        tableChanged(true);
    }

    @Override
    public void deleteAllRows() {
        for (Data2DColumn column : tableData) {
            if (column.isIsPrimaryKey()) {
                popError(message("PrimaryKeysCanNotDeleted"));
                return;
            }
        }
        super.deleteAllRows();
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
            MyBoxLog.error(e);
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
            MyBoxLog.error(e);
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

    public void setWidth(int index, int width) {
        if (index < 0 || index >= tableData.size()) {
            return;
        }
        Data2DColumn column = tableData.get(index);
        column.setWidth(width);
        isSettingValues = true;
        tableData.set(index, column);
        isSettingValues = false;
        if (status == null || status == Status.Loaded) {
            status(Status.Applied);
        }
    }

    public void setNames(List<String> names) {
        try {
            if (names == null || names.size() != tableData.size()) {
                return;
            }
            isSettingValues = true;
            for (int i = 0; i < tableData.size(); i++) {
                tableData.get(i).setColumnName(names.get(i));
            }
            tableView.refresh();
            isSettingValues = false;
            status = Status.Modified;
            okAction();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void addRowsAction() {
        Data2DColumnCreateController.open(this);
    }

    public void addRow(Data2DColumn row) {
        isSettingValues = true;
        tableData.add(row);
        tableView.scrollTo(row);
        isSettingValues = false;
        tableChanged(true);
    }

    @FXML
    @Override
    public void editAction() {
        int index = selectedIndix();
        if (index < 0) {
            return;
        }
        Data2DColumnEditController.open(this, index);
    }

}
