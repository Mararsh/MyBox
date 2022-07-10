package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import mara.mybox.db.data.Data2DStyle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.cell.TableBooleanCell;

/**
 * @Author Mara
 * @CreateDate 2022-4-7
 * @License Apache License Version 2.0
 */
public class ControlData2DStyleList extends ControlData2DAbnormalList {

    @FXML
    protected TableColumn<Data2DStyle, Integer> sequenceColumn;
    @FXML
    protected TableColumn<Data2DStyle, Boolean> abnormalColumn;
    @FXML
    protected TableColumn<Data2DStyle, String> fontColorColumn, bgColorColumn,
            fontSizeColumn, boldColumn, moreColumn;

    @Override
    protected void initColumns() {
        try {
            super.initColumns();
            sequenceColumn.setCellValueFactory(new PropertyValueFactory<>("sequence"));
            abnormalColumn.setCellValueFactory(new PropertyValueFactory<>("abnoramlValues"));
            abnormalColumn.setCellFactory(new TableBooleanCell());
            fontColorColumn.setCellValueFactory(new PropertyValueFactory<>("fontColor"));
            bgColorColumn.setCellValueFactory(new PropertyValueFactory<>("bgColor"));
            fontSizeColumn.setCellValueFactory(new PropertyValueFactory<>("fontSize"));
            boldColumn.setCellValueFactory(new PropertyValueFactory<>("bold"));
            moreColumn.setCellValueFactory(new PropertyValueFactory<>("moreStyle"));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void sourceChanged() {
        try {
            if (tableController == null) {
                return;
            }
            queryConditions = "  d2id = " + tableController.data2D.getD2did();

            loadTableData();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
