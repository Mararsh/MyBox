package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.Data2DColumnTools;
import mara.mybox.data2d.Data2DExampleTools;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataFileExcel;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.VisitHistory;
import static mara.mybox.db.table.BaseTable.StringMaxLength;
import mara.mybox.db.table.TableColor;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.cell.TableAutoCommitCell;
import mara.mybox.fxml.cell.TableCheckboxCell;
import mara.mybox.fxml.cell.TableColorEditCell;
import mara.mybox.fxml.cell.TableDataColumnCell;
import mara.mybox.fxml.cell.TableTextAreaEditCell;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

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

    /*
        export
     */
    @FXML
    public void popExportMenu(Event event) {
        if (UserConfig.getBoolean("Data2DColumnsExportMenuPopWhenMouseHovering", true)) {
            showExportMenu(event);
        }
    }

    @FXML
    protected void showExportMenu(Event mevent) {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu = new MenuItem("CSV", StyleTools.getIconImageView("iconCSV.png"));
            menu.setOnAction((ActionEvent event) -> {
                exportCSV();
            });
            items.add(menu);

            menu = new MenuItem("JSON", StyleTools.getIconImageView("iconJSON.png"));
            menu.setOnAction((ActionEvent event) -> {
                exportJSON();
            });
            items.add(menu);

            menu = new MenuItem("XML", StyleTools.getIconImageView("iconXML.png"));
            menu.setOnAction((ActionEvent event) -> {
                exportXML();
            });
            items.add(menu);

            menu = new MenuItem("Excel", StyleTools.getIconImageView("iconExcel.png"));
            menu.setOnAction((ActionEvent event) -> {
                exportExcel();
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            CheckMenuItem hoverMenu = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
            hoverMenu.setSelected(UserConfig.getBoolean("Data2DColumnsExportMenuPopWhenMouseHovering", true));
            hoverMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean("Data2DColumnsExportMenuPopWhenMouseHovering", hoverMenu.isSelected());
                }
            });
            items.add(hoverMenu);

            popEventMenu(mevent, items);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void exportCSV() {
        File file = chooseSaveFile(VisitHistory.FileType.CSV);
        if (file == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

            DataFileCSV csv;

            @Override
            protected boolean handle() {
                try {
                    csv = Data2DColumnTools.toCSVFile(tableData, file);
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
                DataFileCSVController.loadCSV(csv);
            }
        };
        start(task);
    }

    public void exportXML() {
        File file = chooseSaveFile(VisitHistory.FileType.XML);
        if (file == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {
            @Override
            protected boolean handle() {
                try {
                    if (Data2DColumnTools.toXMLFile(tableData, file)
                            && file != null && file.exists()) {
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

    public void exportJSON() {
        File file = chooseSaveFile(VisitHistory.FileType.JSON);
        if (file == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {
            @Override
            protected boolean handle() {
                try {
                    if (Data2DColumnTools.toJSONFile(tableData, file)
                            && file != null && file.exists()) {
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

    public void exportExcel() {
        File file = chooseSaveFile(VisitHistory.FileType.Excel);
        if (file == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

            DataFileExcel excel;

            @Override
            protected boolean handle() {
                try {
                    excel = Data2DColumnTools.toExcelFile(tableData, file);
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
                DataFileExcelController.open(excel);
            }
        };
        start(task);
    }

    /*
        examples
     */
    @FXML
    protected void popExamplesMenu(Event event) {
        if (UserConfig.getBoolean("Data2DColumnsExamplesPopWhenMouseHovering", false)) {
            showExamplesMenu(event);
        }
    }

    @FXML
    protected void showExamplesMenu(Event mevent) {
        try {
            String lang = Languages.getLangName();
            List<MenuItem> items = new ArrayList<>();

            Menu myMenu = new Menu(message("MyData"), StyleTools.getIconImageView("iconCat.png"));
            items.add(myMenu);

            MenuItem menu = new MenuItem(message("Notes"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.Notes(lang);
                tableData.setAll(data.getColumns());
            });
            myMenu.getItems().add(menu);

            menu = new MenuItem(message("Contacts"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.Contacts(lang);
                tableData.setAll(data.getColumns());
            });
            myMenu.getItems().add(menu);

            menu = new MenuItem(message("CashFlow"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.CashFlow(lang);
                tableData.setAll(data.getColumns());
            });
            myMenu.getItems().add(menu);

            menu = new MenuItem(message("PrivateProperty"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.PrivateProperty(lang);
                tableData.setAll(data.getColumns());
            });
            myMenu.getItems().add(menu);

            myMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(message("Eyesight"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.Eyesight(lang);
                tableData.setAll(data.getColumns());
            });
            myMenu.getItems().add(menu);

            menu = new MenuItem(message("Weight"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.Weight(lang);
                tableData.setAll(data.getColumns());
            });
            myMenu.getItems().add(menu);

            menu = new MenuItem(message("Height"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.Height(lang);
                tableData.setAll(data.getColumns());
            });
            myMenu.getItems().add(menu);

            menu = new MenuItem(message("Menstruation"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.Menstruation(lang);
                tableData.setAll(data.getColumns());
            });
            myMenu.getItems().add(menu);

            Menu chinaMenu = new Menu(message("StatisticDataOfChina"), StyleTools.getIconImageView("iconChina.png"));
            items.add(chinaMenu);

            menu = new MenuItem(message("ChinaPopulation"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.ChinaPopulation(lang);
                tableData.setAll(data.getColumns());
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message("ChinaCensus"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.ChinaCensus(lang);
                tableData.setAll(data.getColumns());
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message("ChinaGDP"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.ChinaGDP(lang);
                tableData.setAll(data.getColumns());
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message("ChinaCPI"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.ChinaCPI(lang);
                tableData.setAll(data.getColumns());
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message("ChinaFoodConsumption"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.ChinaFoodConsumption(lang);
                tableData.setAll(data.getColumns());
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message("ChinaGraduates"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.ChinaGraduates(lang);
                tableData.setAll(data.getColumns());
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message("ChinaMuseums"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.ChinaMuseums(lang);
                tableData.setAll(data.getColumns());
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message("ChinaHealthPersonnel"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.ChinaHealthPersonnel(lang);
                tableData.setAll(data.getColumns());
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message("ChinaMarriage"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.ChinaMarriage(lang);
                tableData.setAll(data.getColumns());
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message("ChinaSportWorldChampions"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.ChinaSportWorldChampions(lang);
                tableData.setAll(data.getColumns());
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message("CrimesFiledByChinaPolice"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.CrimesFiledByChinaPolice(lang);
                tableData.setAll(data.getColumns());
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message("CrimesFiledByChinaProcuratorate"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.CrimesFiledByChinaProcuratorate(lang);
                tableData.setAll(data.getColumns());
            });
            chinaMenu.getItems().add(menu);

            chinaMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(message("ChinaNationalBureauOfStatistics"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent event) -> {
                myController.browse("https://data.stats.gov.cn/");
            });
            chinaMenu.getItems().add(menu);

            Menu regressionMenu = new Menu(message("RegressionData"), StyleTools.getIconImageView("iconLinearPgression.png"));
            items.add(regressionMenu);

            menu = new MenuItem(message("IncomeHappiness"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.IncomeHappiness(lang);
                tableData.setAll(data.getColumns());
            });
            regressionMenu.getItems().add(menu);

            menu = new MenuItem(message("ExperienceSalary"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.ExperienceSalary(lang);
                tableData.setAll(data.getColumns());
            });
            regressionMenu.getItems().add(menu);

            menu = new MenuItem(message("IrisSpecies"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.IrisSpecies(lang);
                tableData.setAll(data.getColumns());
            });
            regressionMenu.getItems().add(menu);

            menu = new MenuItem(message("DiabetesPrediction"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.DiabetesPrediction(lang);
                tableData.setAll(data.getColumns());
            });
            regressionMenu.getItems().add(menu);

            menu = new MenuItem(message("DiabetesPredictionStandardized"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.DiabetesPredictionStandardized(lang);
                tableData.setAll(data.getColumns());
            });
            regressionMenu.getItems().add(menu);

            menu = new MenuItem(message("HeartFailure"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.HeartFailure(lang);
                tableData.setAll(data.getColumns());
            });
            regressionMenu.getItems().add(menu);

            menu = new MenuItem(message("ConcreteCompressiveStrength"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.ConcreteCompressiveStrength(lang);
                tableData.setAll(data.getColumns());
            });
            regressionMenu.getItems().add(menu);

            menu = new MenuItem(message("DogRadiographsDataset"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.DogRadiographsDataset(lang);
                tableData.setAll(data.getColumns());
            });
            regressionMenu.getItems().add(menu);

            menu = new MenuItem(message("BaseballSalaries"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.BaseballSalaries(lang);
                tableData.setAll(data.getColumns());
            });
            regressionMenu.getItems().add(menu);

            menu = new MenuItem(message("SouthGermanCredit"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.SouthGermanCredit(lang);
                tableData.setAll(data.getColumns());
            });
            regressionMenu.getItems().add(menu);

            menu = new MenuItem(message("BostonHousingPrices"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.BostonHousingPrices(lang);
                tableData.setAll(data.getColumns());
            });
            regressionMenu.getItems().add(menu);

            regressionMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(message("AboutDataAnalysis"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent event) -> {
                myController.openHtml(HelpTools.aboutDataAnalysis());
            });
            regressionMenu.getItems().add(menu);

            Menu locationMenu = new Menu(message("LocationData"), StyleTools.getIconImageView("iconLocation.png"));
            items.add(locationMenu);

            menu = new MenuItem(message("ChineseHistoricalCapitals"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.ChineseHistoricalCapitals(lang);
                tableData.setAll(data.getColumns());
            });
            locationMenu.getItems().add(menu);

            menu = new MenuItem(message("AutumnMovementPatternsOfEuropeanGadwalls"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.AutumnMovementPatternsOfEuropeanGadwalls(lang);
                tableData.setAll(data.getColumns());
            });
            locationMenu.getItems().add(menu);

            menu = new MenuItem(message("SpermWhalesGulfOfMexico"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.SpermWhalesGulfOfMexico(lang);
                tableData.setAll(data.getColumns());
            });
            locationMenu.getItems().add(menu);

            menu = new MenuItem(message("EpidemicReportsCOVID19"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.EpidemicReportsCOVID19(lang);
                tableData.setAll(data.getColumns());
            });
            locationMenu.getItems().add(menu);

            boolean isChinese = "zh".equals(lang);

            Menu pmMenu = new Menu(message("ProjectManagement"), StyleTools.getIconImageView("iconCalculator.png"));
            items.add(pmMenu);

            menu = new MenuItem(isChinese ? "项目登记" : "Project register");
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.ProjectRegister(lang);
                tableData.setAll(data.getColumns());
            });
            pmMenu.getItems().add(menu);

            menu = new MenuItem(isChinese ? "项目状态" : "Project Status");
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.ProjectStatus(lang);
                tableData.setAll(data.getColumns());
            });
            pmMenu.getItems().add(menu);

            menu = new MenuItem(isChinese ? "任务登记" : "Task register");
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.TaskRegister(lang);
                tableData.setAll(data.getColumns());
            });
            pmMenu.getItems().add(menu);

            menu = new MenuItem(isChinese ? "任务状态" : "Task Status");
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.TaskStatus(lang);
                tableData.setAll(data.getColumns());
            });
            pmMenu.getItems().add(menu);

            menu = new MenuItem(isChinese ? "人员登记" : "Person register");
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.PersonRegister(lang);
                tableData.setAll(data.getColumns());
            });
            pmMenu.getItems().add(menu);

            menu = new MenuItem(isChinese ? "人员状态" : "Person Status");
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.PersonStatus(lang);
                tableData.setAll(data.getColumns());
            });
            pmMenu.getItems().add(menu);

            menu = new MenuItem(isChinese ? "资源登记" : "Resource register");
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.ResourceRegister(lang);
                tableData.setAll(data.getColumns());
            });
            pmMenu.getItems().add(menu);

            menu = new MenuItem(isChinese ? "资源状态" : "Resource Status");
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.ResourceStatus(lang);
                tableData.setAll(data.getColumns());
            });
            pmMenu.getItems().add(menu);

            menu = new MenuItem(isChinese ? "风险分析" : "Risk Analysis");
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.RiskAnalysis(lang);
                tableData.setAll(data.getColumns());
            });
            pmMenu.getItems().add(menu);

            menu = new MenuItem(isChinese ? "成本记录" : "Cost Record");
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.CostRecord(lang);
                tableData.setAll(data.getColumns());
            });
            pmMenu.getItems().add(menu);

            menu = new MenuItem(isChinese ? "检验记录" : "Verification Record");
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.VerificationRecord(lang);
                tableData.setAll(data.getColumns());
            });
            pmMenu.getItems().add(menu);

            items.add(new SeparatorMenuItem());

            CheckMenuItem pMenu = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
            pMenu.setSelected(UserConfig.getBoolean("Data2DColumnsExamplesPopWhenMouseHovering", false));
            pMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean("Data2DColumnsExamplesPopWhenMouseHovering", pMenu.isSelected());
                }
            });
            items.add(pMenu);

            popEventMenu(mevent, items);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
