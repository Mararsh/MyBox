package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import mara.mybox.db.data.Data2DStyle;
import mara.mybox.db.table.TableData2DStyle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.cell.TableBooleanCell;

/**
 * @Author Mara
 * @CreateDate 2022-4-7
 * @License Apache License Version 2.0
 */
public class ControlData2DAbnormalList extends BaseSysTableController<Data2DStyle> {

    protected BaseData2DAbnormalController manageController;
    protected ControlData2DEditTable tableController;
    protected TableData2DStyle tableData2DStyle;
    protected String styleValue;
    protected Data2DStyle updatedStyle, orignialStyle;
    protected ChangeListener<Boolean> tableStatusListener;

    @FXML
    protected TableColumn<Data2DStyle, Long> sidColumn, fromColumn, toColumn;
    @FXML
    protected TableColumn<Data2DStyle, Integer> sequenceColumn;
    @FXML
    protected TableColumn<Data2DStyle, Boolean> abnormalColumn;
    @FXML
    protected TableColumn<Data2DStyle, String> columnsColumn, rowFilterColumn, columnFilterColumn,
            fontColorColumn, bgColorColumn, fontSizeColumn, boldColumn, moreColumn;

    @Override
    protected void initColumns() {
        try {
            super.initColumns();
            sidColumn.setCellValueFactory(new PropertyValueFactory<>("d2sid"));
            sequenceColumn.setCellValueFactory(new PropertyValueFactory<>("sequence"));
            abnormalColumn.setCellValueFactory(new PropertyValueFactory<>("abnoramlValues"));
            abnormalColumn.setCellFactory(new TableBooleanCell());
            fromColumn.setCellValueFactory(new PropertyValueFactory<>("from"));
            toColumn.setCellValueFactory(new PropertyValueFactory<>("to"));
            columnsColumn.setCellValueFactory(new PropertyValueFactory<>("columns"));
            rowFilterColumn.setCellValueFactory(new PropertyValueFactory<>("rowFilterString"));
            columnFilterColumn.setCellValueFactory(new PropertyValueFactory<>("columnFilterString"));
            fontColorColumn.setCellValueFactory(new PropertyValueFactory<>("fontColor"));
            bgColorColumn.setCellValueFactory(new PropertyValueFactory<>("bgColor"));
            fontSizeColumn.setCellValueFactory(new PropertyValueFactory<>("fontSize"));
            boldColumn.setCellValueFactory(new PropertyValueFactory<>("bold"));
            moreColumn.setCellValueFactory(new PropertyValueFactory<>("moreStyle"));

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
            queryConditions = "  d2id=" + tableController.data2D.getD2did() + "";

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

    @Override
    public void editNull() {
        if (manageController != null) {
            manageController.loadNull();
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
