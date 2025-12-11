package mara.mybox.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import mara.mybox.data.StringTable;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataFileExcel;
import mara.mybox.data2d.tools.Data2DColumnTools;
import mara.mybox.data2d.tools.Data2DDefinitionTools;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.VisitHistory;
import static mara.mybox.db.table.BaseTable.StringMaxLength;
import mara.mybox.db.table.TableColor;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.MenuTools;
import mara.mybox.fxml.cell.TableAutoCommitCell;
import mara.mybox.fxml.cell.TableBooleanCell;
import mara.mybox.fxml.cell.TableCheckboxCell;
import mara.mybox.fxml.cell.TableColorEditCell;
import mara.mybox.fxml.cell.TableDataColumnCell;
import mara.mybox.fxml.cell.TableTextAreaEditCell;
import mara.mybox.fxml.image.FxColorTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TextFileTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-10-16
 * @License Apache License Version 2.0
 */
public class ControlData2DColumns extends BaseTableViewController<Data2DColumn> {

    protected Data2DAttributesController attributesController;
    protected TableData2DDefinition tableData2DDefinition;
    protected TableData2DColumn tableData2DColumn;
    protected Data2D data2D;
    protected boolean changed;

    @FXML
    protected TableColumn<Data2DColumn, String> nameColumn, typeColumn, defaultColumn,
            labelColumn, descColumn, formatColumn;
    @FXML
    protected TableColumn<Data2DColumn, Boolean> editableColumn, notNullColumn, primaryColumn, autoColumn;
    @FXML
    protected TableColumn<Data2DColumn, Integer> indexColumn, lengthColumn, widthColumn;
    @FXML
    protected TableColumn<Data2DColumn, Color> colorColumn;
    @FXML
    protected FlowPane buttonsPane;

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.XML);
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
                        changed(true);
                    }
                }
            });
            nameColumn.setEditable(true);
            nameColumn.getStyleClass().add("editable-column");

            if (labelColumn != null) {
                labelColumn.setCellValueFactory(new PropertyValueFactory<>("label"));
                labelColumn.setCellFactory(TableAutoCommitCell.forStringColumn());
                labelColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Data2DColumn, String>>() {
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
                        if (!v.equals(column.getLabel())) {
                            column.setLabel(v);
                            changed(true);
                        }
                    }
                });
                labelColumn.setEditable(true);
                labelColumn.getStyleClass().add("editable-column");
            }

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
                                        changed(true);
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
                                        changed(true);
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
                        changed(true);
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
                        changed(true);
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
                            changed(true);
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
                        changed(true);
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
                    changed(true);
                }
            });
            descColumn.setEditable(true);
            descColumn.getStyleClass().add("editable-column");

            primaryColumn.setCellFactory(new TableBooleanCell());

            autoColumn.setCellFactory(new TableBooleanCell());

            checkButtons();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void setParameters(Data2DAttributesController controller) {
        try {
            attributesController = controller;

            loadValues();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void loadValues() {
        try {
            data2D = attributesController.data2D;
            tableData2DDefinition = attributesController.tableData2DDefinition;
            tableData2DColumn = attributesController.tableData2DColumn;
            setData2DColumns();
            loadColumns();
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
                                            changed(true);
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

    public void loadColumns() {
        try {
            if (isSettingValues) {
                return;
            }
            isSettingValues = true;
            tableData.clear();
            if (data2D != null && data2D.hasColumns()) {
                int colIndex = 0;
                for (Data2DColumn column : data2D.getColumns()) {
                    Data2DColumn c = column.cloneAll();
                    c.setIndex(colIndex++);
                    tableData.add(c);
                }
            }
            isSettingValues = false;
            tableChanged(false);
            changed(false);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void changed(boolean changed) {
        if (isSettingValues) {
            return;
        }
        this.changed = changed;
        if (attributesController != null) {
            attributesController.columnsChanged(changed);
        }
    }

    @Override
    public void tableChanged(boolean changed) {
        if (isSettingValues) {
            return;
        }
        if (changed) {
            changed(true);
        }
        checkButtons();
    }

    @Override
    public void checkButtons() {
        super.checkButtons();
        operationsButton.setDisable(data2D == null || tableData.isEmpty());
        addRowsButton.setDisable(data2D == null || data2D.isInternalTable());
        deleteRowsButton.setDisable(data2D == null || data2D.isInternalTable() || isNoneSelected());
    }

    public boolean isChanged() {
        return changed;
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

    public boolean isTable() {
        return data2D != null && data2D.isTable();
    }

    public boolean isMatrix() {
        return data2D != null && data2D.isMatrix();
    }

    public List<Data2DColumn> pickColumns() {
        try {
            StringTable validateTable = Data2DColumnTools.validate(tableData);
            if (validateTable != null && !validateTable.isEmpty()) {
                validateTable.htmlTable();
                return null;
            }
            List<Data2DColumn> columns = new ArrayList<>();
            for (int i = 0; i < tableData.size(); i++) {
                Data2DColumn column = tableData.get(i).cloneAll();
                columns.add(column);
            }
            return columns;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
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
    public Data2DColumn dataCopy(Data2DColumn col) {
        if (col == null) {
            return null;
        }
        Data2DColumn column = col.copy().setDataID(col.getDataID());
        column.setColumnName(col.getColumnName() + "_" + message("Copy"));
        column.setIndex(newColumnIndex());
        return column;
    }

    @FXML
    @Override
    public void deleteRowsAction() {
        List<Data2DColumn> selected = selectedItems();
        if (selected == null || selected.isEmpty()) {
            popError(message("SelectToHandle"));
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
    public void clearWithSure() {
        for (Data2DColumn column : tableData) {
            if (column.isIsPrimaryKey()) {
                popError(message("PrimaryKeysCanNotDeleted"));
                return;
            }
        }
        super.clearWithSure();
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
            changed(true);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void numberColumns() {
        try {
            String prefix = message(colPrefix());
            List<String> names = new ArrayList<>();
            List<Data2DColumn> selectedItems = selectedItems();
            for (int i = 0; i < tableData.size(); i++) {
                Data2DColumn col = tableData.get(i);
                if (selectedItems == null
                        || selectedItems.isEmpty()
                        || selectedItems.contains(col)) {
                    names.add(prefix + (i + 1));
                } else {
                    names.add(col.getColumnName());
                }
            }
            setNames(names);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void copyLabelsToNames() {
        try {
            List<String> names = new ArrayList<>();
            List<Data2DColumn> selectedItems = selectedItems();
            for (int i = 0; i < tableData.size(); i++) {
                Data2DColumn col = tableData.get(i);
                String label = col.getLabel();
                if (label != null && !label.isBlank()
                        && (selectedItems == null
                        || selectedItems.isEmpty()
                        || selectedItems.contains(col))) {
                    names.add(label);
                } else {
                    names.add(col.getColumnName());
                }
            }
            setNames(names);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void copyNamesToLabels() {
        try {
            List<Data2DColumn> selectedItems = selectedItems();
            isSettingValues = true;
            for (int i = 0; i < tableData.size(); i++) {
                Data2DColumn col = tableData.get(i);
                if (selectedItems == null
                        || selectedItems.isEmpty()
                        || selectedItems.contains(col)) {
                    col.setLabel(col.getColumnName());
                }
            }
            tableView.refresh();
            isSettingValues = false;
            changed(true);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void headerNames() {
        try {
            if (attributesController.data2D == null
                    || attributesController.tableData.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            List<String> row = attributesController.tableData.get(0);
            if (row == null || row.size() < 2) {
                popError(message("InvalidData"));
                return;
            }
            List<String> names = new ArrayList<>();
            List<Data2DColumn> selectedItems = selectedItems();
            for (int i = 0; i < tableData.size(); i++) {
                Data2DColumn col = tableData.get(i);
                if (selectedItems == null
                        || selectedItems.isEmpty()
                        || selectedItems.contains(col)) {
                    String value = row.get(i + 1);
                    if (value == null || value.isBlank()) {
                        value = message("Column") + i;
                    }
                    DerbyBase.checkIdentifier(names, value, true);
                } else {
                    names.add(col.getColumnName());
                }
            }
            setNames(names);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void randomColors() {
        try {
            isSettingValues = true;
            Random r = new Random();
            List<Data2DColumn> selectedItems = selectedItems();
            for (int i = 0; i < tableData.size(); i++) {
                Data2DColumn col = tableData.get(i);
                if (selectedItems == null
                        || selectedItems.isEmpty()
                        || selectedItems.contains(col)) {
                    col.setColor(FxColorTools.randomColor(r));
                }
            }
            tableView.refresh();
            isSettingValues = false;
            changed(true);
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
        changed(true);
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

    @FXML
    @Override
    public void selectAction() {
        DataSelectDataColumnController.open(this);
    }

    @FXML
    @Override
    public void recoverAction() {
        loadValues();
    }

    @Override
    public List<MenuItem> operationsMenuItems(Event fevent) {
        return Data2DColumnTools.operationsMenus(this);
    }


    /*
        import
     */
    @Override
    public void sourceFileChanged(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            protected DataFileCSV csv;

            @Override
            protected boolean handle() {
                try {
                    csv = Data2DDefinitionTools.fromXML(TextFileTools.readTexts(this, file));
                    return csv != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                addColumns(csv);
            }
        };
        start(task);
    }

    public void addColumns(Data2D def) {
        if (def == null) {
            return;
        }
        List<Data2DColumn> cols = def.getColumns();
        if (cols == null || cols.isEmpty()) {
            return;
        }
        tableData.addAll(cols);
    }

    public void addColumn(Data2DColumn column) {
        if (column == null) {
            return;
        }
        Data2DColumnTools.columnInfo(column);
        tableData.add(column);
    }

    /*
        export
     */
    @FXML
    public void popExportMenu(Event event) {
        if (MenuTools.isPopMenu("Data2DDefinitionExport")) {
            showExportMenu(event);
        }
    }

    @FXML
    protected void showExportMenu(Event mevent) {
        try {
            Data2D currentData = data2D != null ? data2D.cloneAll() : new DataFileCSV();
            currentData.setColumns(tableData);

            List<MenuItem> items = exportMenu(mevent, currentData);
            if (items == null || items.isEmpty()) {
                return;
            }

            popEventMenu(mevent, items);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected List<MenuItem> exportMenu(Event mevent, Data2D currentData) {
        try {
            List<MenuItem> items = new ArrayList<>();

            MenuItem menu = new MenuItem("CSV", StyleTools.getIconImageView("iconCSV.png"));
            menu.setOnAction((ActionEvent event) -> {
                exportCSV(currentData);
            });
            items.add(menu);

            menu = new MenuItem("JSON", StyleTools.getIconImageView("iconJSON.png"));
            menu.setOnAction((ActionEvent event) -> {
                exportJSON(currentData);
            });
            items.add(menu);

            menu = new MenuItem("XML", StyleTools.getIconImageView("iconXML.png"));
            menu.setOnAction((ActionEvent event) -> {
                exportXML(currentData);
            });
            items.add(menu);

            menu = new MenuItem("Excel", StyleTools.getIconImageView("iconExcel.png"));
            menu.setOnAction((ActionEvent event) -> {
                exportExcel(currentData);
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            CheckMenuItem attrMenu = new CheckMenuItem(message("ExportDataAttributes"),
                    StyleTools.getIconImageView("iconInfo.png"));
            attrMenu.setSelected(UserConfig.getBoolean("Data2DDefinitionExportAtributes", true));
            attrMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean("Data2DDefinitionExportAtributes", attrMenu.isSelected());
                }
            });
            items.add(attrMenu);

            items.add(MenuTools.popCheckMenu("Data2DDefinitionExport"));

            return items;

        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public void exportCSV(Data2D currentData) {
        if (currentData == null) {
            popError(message("NoData"));
            return;
        }
        File file = saveCurrentFile(VisitHistory.FileType.CSV,
                currentData.getName() + "-" + message("DataDefinition"));
        if (file == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            DataFileCSV csv;

            @Override
            protected boolean handle() {
                try {
                    csv = Data2DDefinitionTools.toCSVFile(currentData, file);
                    if (file != null && file.exists()) {
                        recordFileWritten(file, VisitHistory.FileType.CSV);
                        return true;
                    } else {
                        return false;
                    }
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                Data2DManufactureController.openDef(csv);
            }
        };
        start(task);
    }

    public void exportXML(Data2D currentData) {
        if (currentData == null) {
            popError(message("NoData"));
            return;
        }
        File file = saveCurrentFile(VisitHistory.FileType.XML,
                currentData.getName() + "-" + message("DataDefinition"));
        if (file == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            @Override
            protected boolean handle() {
                try {
                    String xml = Data2DDefinitionTools.toXML(currentData,
                            UserConfig.getBoolean("Data2DDefinitionExportAtributes", true),
                            "");
                    if (xml == null || xml.isBlank()) {
                        return false;
                    }
                    File tmpFile = FileTmpTools.getTempFile();
                    try (BufferedWriter xmlWriter = new BufferedWriter(new FileWriter(tmpFile, Charset.forName("UTF-8")))) {
                        xmlWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                        xmlWriter.write(xml);
                        xmlWriter.flush();
                        xmlWriter.close();
                    } catch (Exception e) {
                        MyBoxLog.error(e.toString());
                        return false;
                    }
                    if (FileTools.override(tmpFile, file, true)) {
                        recordFileWritten(file, VisitHistory.FileType.XML);
                        return true;
                    } else {
                        return false;
                    }
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                XmlEditorController.open(file);
            }
        };
        start(task);
    }

    public void exportJSON(Data2D currentData) {
        if (currentData == null) {
            popError(message("NoData"));
            return;
        }
        File file = saveCurrentFile(VisitHistory.FileType.JSON,
                currentData.getName() + "-" + message("DataDefinition"));
        if (file == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            @Override
            protected boolean handle() {
                try {
                    String json = Data2DDefinitionTools.toJSON(currentData,
                            UserConfig.getBoolean("Data2DDefinitionExportAtributes", true),
                            "");
                    if (json == null || json.isBlank()) {
                        return false;
                    }
                    File tmpFile = FileTmpTools.getTempFile();
                    try (BufferedWriter jsonWriter = new BufferedWriter(new FileWriter(tmpFile, Charset.forName("UTF-8")))) {
                        jsonWriter.write("{" + json + "}");
                        jsonWriter.flush();
                        jsonWriter.close();
                    }
                    if (FileTools.override(tmpFile, file, true)) {
                        recordFileWritten(file, VisitHistory.FileType.JSON);
                        return true;
                    } else {
                        return false;
                    }
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                JsonEditorController.open(file);
            }
        };
        start(task);
    }

    public void exportExcel(Data2D currentData) {
        if (currentData == null) {
            popError(message("NoData"));
            return;
        }
        File file = saveCurrentFile(VisitHistory.FileType.Excel,
                currentData.getName() + "-" + message("DataDefinition"));
        if (file == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            DataFileExcel excel;

            @Override
            protected boolean handle() {
                try {
                    excel = Data2DDefinitionTools.toExcelFile(currentData, file);
                    if (file != null && file.exists()) {
                        recordFileWritten(file, VisitHistory.FileType.CSV);
                        return true;
                    } else {
                        return false;
                    }
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                recordFileWritten(file, VisitHistory.FileType.Excel);
                Data2DManufactureController.openDef(excel);
            }
        };
        start(task);
    }

    public Data2D getData2D() {
        return data2D;
    }

}
