package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.util.Callback;
import mara.mybox.data.StringTable;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.tools.Data2DColumnTools;
import mara.mybox.data2d.tools.Data2DDefinitionTools;
import mara.mybox.data2d.tools.Data2DPageTools;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.InfoNode;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.cell.TableBooleanCell;
import mara.mybox.fxml.cell.TableCheckboxCell;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.style.StyleTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-10-16
 * @License Apache License Version 2.0
 */
public class ControlData2DColumns extends BaseData2DColumnsController {

    protected BaseData2DLoadController dataController;
    protected TableData2DDefinition tableData2DDefinition;

    @FXML
    protected Button headerButton;

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

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(headerButton, new Tooltip(message("FirstLineDefineNames")));
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    protected void setParameters(BaseData2DLoadController controller) {
        try {
            this.dataController = controller;

            loadValues();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void loadValues() {
        try {
            data2D = dataController.data2D;
            tableData2DDefinition = dataController.tableData2DDefinition;
            tableData2DColumn = dataController.tableData2DColumn;
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
            changed(false);
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

    @FXML
    @Override
    public void okAction() {
        try {
            StringTable validateTable = Data2DColumnTools.validate(tableData);
            if (validateTable != null && !validateTable.isEmpty()) {
                validateTable.htmlTable();
                return;
            }
            List<List<String>> newTableData = new ArrayList<>();
            if (!tableData.isEmpty()) {
                for (List<String> rowValues : dataController.tableData) {
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
            dataController.updatePage(newTableData);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void recoverAction() {
        loadValues();
    }

    @FXML
    @Override
    public void selectAction() {
        InfoTreeNodeSelectController controller = InfoTreeNodeSelectController.open(this, InfoNode.Data2DDefinition);
        controller.notify.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                InfoNode node = controller.selected();
                if (node == null) {
                    return;
                }
                addColumns(Data2DDefinitionTools.definitionFromXML(null, myController, node.getInfo()));
                controller.close();
            }
        });
    }

    @FXML
    public void headerAction() {
        try {
            if (dataController.data2D == null || dataController.tableData.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            List<String> row = dataController.tableData.get(0);
            if (row == null || row.size() < 2) {
                popError(message("InvalidData"));
                return;
            }
            List<String> names = new ArrayList<>();
            for (int i = 1; i < row.size(); i++) {
                String name = row.get(i);
                if (name == null || name.isBlank()) {
                    name = message("Column") + i;
                }
                DerbyBase.checkIdentifier(names, name, true);
            }
            setNames(names);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected List<MenuItem> exportMenu(Event mevent, Data2D currentData) {
        try {
            List<MenuItem> items = new ArrayList<>();

            MenuItem menu = new MenuItem(message("Save"), StyleTools.getIconImageView("iconSave.png"));
            menu.setOnAction((ActionEvent event) -> {
                saveCSV(currentData);
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            items.addAll(super.exportMenu(mevent, currentData));

            return items;

        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public void saveCSV(Data2D currentData) {
        if (currentData == null) {
            popError(message("NoData"));
            return;
        }
        Data2DDefinitionController.load(currentData);
    }

}
