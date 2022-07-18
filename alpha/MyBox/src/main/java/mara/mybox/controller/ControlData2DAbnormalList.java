package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import mara.mybox.db.data.Data2DStyle;
import mara.mybox.db.table.TableData2DStyle;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-7-9
 * @License Apache License Version 2.0
 */
public class ControlData2DAbnormalList extends BaseSysTableController<Data2DStyle> {

    protected BaseData2DAbnormalController manageController;
    protected ControlData2DEditTable tableController;
    protected TableData2DStyle tableData2DStyle;

    @FXML
    protected TableColumn<Data2DStyle, Long> sidColumn, fromColumn, toColumn;
    @FXML
    protected TableColumn<Data2DStyle, String> titleColumn, columnsColumn, rowFilterColumn, columnFilterColumn;

    @Override
    protected void initColumns() {
        try {
            super.initColumns();
            sidColumn.setCellValueFactory(new PropertyValueFactory<>("d2sid"));
            titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
            fromColumn.setCellValueFactory(new PropertyValueFactory<>("from"));
            toColumn.setCellValueFactory(new PropertyValueFactory<>("to"));
            columnsColumn.setCellValueFactory(new PropertyValueFactory<>("columns"));
            rowFilterColumn.setCellValueFactory(new PropertyValueFactory<>("rowFilterString"));
            columnFilterColumn.setCellValueFactory(new PropertyValueFactory<>("columnFilterString"));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setParameters(BaseData2DAbnormalController manageController) {
        try {
            this.manageController = manageController;
            tableController = manageController.tableController;

            tableData2DStyle = new TableData2DStyle();
            tableDefinition = tableData2DStyle;
            tableName = tableDefinition.getTableName();
            idColumn = tableDefinition.getIdColumn();

            sourceChanged();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void sourceChanged() {
        try {
            if (tableController == null) {
                return;
            }
            queryConditions = "  d2id = " + tableController.data2D.getD2did() + " AND abnoramlValues = true";

            loadTableData();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void itemClicked() {
        editAction();
    }

    @Override
    protected void afterDeletion() {
        super.afterDeletion();
        if (manageController != null) {
            manageController.reloadDataPage();
        }
    }

    @Override
    protected void afterClear() {
        super.afterClear();
        if (manageController != null) {
            manageController.reloadDataPage();
        }
    }

    @FXML
    @Override
    public void editAction() {
        if (manageController == null) {
            return;
        }
        Data2DStyle selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        manageController.loadStyle(selected);
    }

}
