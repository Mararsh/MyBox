package mara.mybox.controller;

import java.util.List;
import java.util.Random;
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
import mara.mybox.data2d.Data2D;
import mara.mybox.db.data.Data2DColumn;
import static mara.mybox.db.table.BaseTable.StringMaxLength;
import mara.mybox.db.table.TableColor;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.cell.TableAutoCommitCell;
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
public abstract class BaseData2DColumnsController extends BaseTablePagesController<Data2DColumn> {

    protected TableData2DColumn tableData2DColumn;
    protected Status status;
    protected Data2D data2D;

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
    protected Button numberColumnsButton, randomColorsButton;

    public BaseData2DColumnsController() {
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(numberColumnsButton, new Tooltip(message("RenameAllColumns")));
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void initColumns() {
        try {
            super.initColumns();

            if (indexColumn != null) {
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
            }

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
            nameColumn.setEditable(true);
            nameColumn.getStyleClass().add("editable-column");

            typeColumn.setCellValueFactory(new PropertyValueFactory<>("typeString"));
            typeColumn.setCellFactory(TableDataColumnCell.create(this));
            typeColumn.setEditable(true);
            typeColumn.getStyleClass().add("editable-column");

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
                                    if (isChanging || rowIndex < 0) {
                                        return;
                                    }
                                    Data2DColumn column = tableData.get(rowIndex);
                                    if (column == null || column.isAuto()) {
                                        return;
                                    }
                                    if (value != column.isEditable()) {
                                        isChanging = true;
                                        column.setEditable(value);
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

            primaryColumn.setCellValueFactory(new PropertyValueFactory<>("isPrimaryKey"));

            autoColumn.setCellValueFactory(new PropertyValueFactory<>("auto"));

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
            lengthColumn.setEditable(true);
            lengthColumn.getStyleClass().add("editable-column");

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
            defaultColumn.setEditable(true);
            defaultColumn.getStyleClass().add("editable-column");

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

    public void status(ControlData2DColumns.Status newStatus) {
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
        randomColorsButton.setDisable(tableData.isEmpty());
        numberColumnsButton.setDisable(tableData.isEmpty());
        deleteRowsButton.setDisable(isNoneSelected());
    }

    public boolean isChanged() {
        return status == Status.Modified || status == Status.Applied;
    }

    public int newColumnIndex() {
        if (data2D != null) {
            return data2D.newColumnIndex();
        } else {
            int max = 0;
            for (Data2DColumn col : tableData) {
                if (col.getIndex() > max) {
                    max = col.getIndex();
                }
            }
            return max + 1;
        }
    }

    public String colPrefix() {
        if (data2D != null) {
            return data2D.colPrefix();
        } else {
            return "c";
        }
    }

    @Override
    public Data2DColumn newData() {
        Data2DColumn column = new Data2DColumn();
        int index = newColumnIndex();
        column.setIndex(index);
        column.setColumnName(colPrefix() + (-index));
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
        column.setIndex(newColumnIndex());
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
    public void numberColumns() {
        try {
            String prefix = message(colPrefix());
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
