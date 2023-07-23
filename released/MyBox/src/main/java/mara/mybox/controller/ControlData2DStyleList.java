package mara.mybox.controller;

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
public class ControlData2DStyleList extends BaseSysTableController<Data2DStyle> {

    protected Data2DSetStylesController manageController;
    protected ControlData2DLoad tableController;
    protected TableData2DStyle tableData2DStyle;

    @FXML
    protected TableColumn<Data2DStyle, Long> sidColumn, fromColumn, toColumn;
    @FXML
    protected TableColumn<Data2DStyle, String> titleColumn, columnsColumn, filterColumn;
    @FXML
    protected TableColumn<Data2DStyle, Integer> sequenceColumn;
    @FXML
    protected TableColumn<Data2DStyle, Boolean> filterReversedColumn, abnormalColumn;
    @FXML
    protected TableColumn<Data2DStyle, String> fontColorColumn, bgColorColumn,
            fontSizeColumn, boldColumn, moreColumn;

    @Override
    protected void initColumns() {
        try {
            super.initColumns();

            sidColumn.setCellValueFactory(new PropertyValueFactory<>("d2sid"));
            titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
            fromColumn.setCellValueFactory(new PropertyValueFactory<>("from"));
            toColumn.setCellValueFactory(new PropertyValueFactory<>("to"));
            columnsColumn.setCellValueFactory(new PropertyValueFactory<>("columns"));
            filterColumn.setCellValueFactory(new PropertyValueFactory<>("filter"));
            filterReversedColumn.setCellValueFactory(new PropertyValueFactory<>("filterReversed"));

            sequenceColumn.setCellValueFactory(new PropertyValueFactory<>("sequence"));
            abnormalColumn.setCellValueFactory(new PropertyValueFactory<>("abnoramlValues"));
            abnormalColumn.setCellFactory(new TableBooleanCell());
            fontColorColumn.setCellValueFactory(new PropertyValueFactory<>("fontColor"));
            bgColorColumn.setCellValueFactory(new PropertyValueFactory<>("bgColor"));
            fontSizeColumn.setCellValueFactory(new PropertyValueFactory<>("fontSize"));
            boldColumn.setCellValueFactory(new PropertyValueFactory<>("bold"));
            moreColumn.setCellValueFactory(new PropertyValueFactory<>("moreStyle"));

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParameters(Data2DSetStylesController manageController) {
        try {
            this.manageController = manageController;
            tableController = manageController.tableController;

            tableData2DStyle = new TableData2DStyle();
            tableDefinition = tableData2DStyle;
            tableName = tableDefinition.getTableName();
            idColumnName = tableDefinition.getIdColumnName();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void sourceChanged() {
        try {
            if (tableController == null || tableController.data2D == null) {
                return;
            }
            queryConditions = "  d2id = " + tableController.data2D.getD2did();

            loadTableData();
        } catch (Exception e) {
            MyBoxLog.error(e);
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
        Data2DStyle selected = selectedItem();
        if (selected == null) {
            return;
        }
        manageController.loadStyle(selected);
    }

}
