package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-12-9
 * @License Apache License Version 2.0
 */
public class ControlData2DSelect extends BaseController {

    protected ControlData2DEditTable tableController;
    protected List<Integer> selectedColumnsIndices, selectedRowsIndices;

    @FXML
    protected HBox rowGroupBox;
    @FXML
    protected ToggleGroup rowGroup;
    @FXML
    protected RadioButton rowAllRadio, rowTableRadio;
    @FXML
    protected ControlListCheckBox rowsListController, colsListController;
    @FXML
    protected Button selectAllRowsButton, selectNoneRowsButton, selectAllColsButton, selectNoneColsButton;

    public void setParameters(ControlData2DEditTable tableController, boolean includeAll) {
        try {
            this.tableController = tableController;
            rowsListController.setParent(tableController);
            colsListController.setParent(tableController);

            refreshControls();
            rowsListController.checkAll();
            colsListController.checkAll();

            if (!includeAll) {
                rowTableRadio.fire();
                thisPane.getChildren().remove(rowGroupBox);
            } else {
                rowsListController.thisPane.disableProperty().bind(rowAllRadio.selectedProperty());
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void refreshControls() {
        try {
            List<String> selectedRows = rowsListController.checkedValues();
            List<String> rows = new ArrayList<>();
            for (long i = 0; i < tableController.tableData.size(); i++) {
                rows.add("" + (i + 1));
            }
            rowsListController.setValues(rows);
            rowsListController.checkValues(selectedRows);

            List<String> selectedCols = colsListController.checkedValues();
            colsListController.setValues(tableController.data2D.columnNames());
            colsListController.checkValues(selectedCols);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void selectAllRows() {
        rowsListController.checkAll();
    }

    @FXML
    public void selectNoneRows() {
        rowsListController.checkNone();
    }

    @FXML
    public void selectAllCols() {
        colsListController.checkAll();
    }

    @FXML
    public void selectNoneCols() {
        colsListController.checkNone();
    }

    public boolean isAllData() {
        return rowAllRadio.isSelected();
    }

    public List<Integer> selectedRowsIndices() {
        selectedRowsIndices = rowsListController.checkedIndices();
        return selectedRowsIndices;
    }

    public List<Integer> selectedColumnsIndices() {
        selectedColumnsIndices = colsListController.checkedIndices();
        return selectedColumnsIndices;
    }

    public List<String> selectedColumnsNames() {
        return colsListController.checkedValues();
    }

    public List<List<String>> selectedData() {
        try {
            selectedRowsIndices = rowsListController.checkedIndices();
            selectedColumnsIndices = colsListController.checkedIndices();
            if (selectedColumnsIndices == null || selectedColumnsIndices.isEmpty()
                    || selectedRowsIndices == null || selectedRowsIndices.isEmpty()) {
                return null;
            }
            List<List<String>> data = new ArrayList<>();
            for (int row : selectedRowsIndices) {
                List<String> tableRow = tableController.tableData.get(row);
                List<String> newRow = new ArrayList<>();
                for (int col : selectedColumnsIndices) {
                    newRow.add(tableRow.get(col + 1));
                }
                data.add(newRow);
            }
            return data;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(e.toString());
            return null;
        }
    }

}
