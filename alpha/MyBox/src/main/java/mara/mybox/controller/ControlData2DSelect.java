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
    protected List<Integer> checkedRowsIndices, checkedColsIndices;
    protected List<String> checkedColumnsNames;
    protected boolean numberCols;

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

    public void setParameters(ControlData2DEditTable tableController, boolean allData, boolean numberCols) {
        try {
            this.tableController = tableController;
            rowsListController.setParent(tableController);
            colsListController.setParent(tableController);
            this.numberCols = numberCols;

            refreshControls();

            if (!allData) {
                rowTableRadio.fire();
                thisPane.getChildren().remove(rowGroupBox);
            } else {
                rowsListController.thisPane.disableProperty().bind(rowAllRadio.selectedProperty());
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public synchronized void refreshControls() {
        try {
            refreshRows();
            refreshCols();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void refreshRows() {
        try {
            List<String> selectedRows = rowsListController.checkedValues();
            List<String> rows = new ArrayList<>();
            for (long i = 0; i < tableController.tableData.size(); i++) {
                rows.add("" + (i + 1));
            }
            rowsListController.setValues(rows);
            if (selectedRows != null && !selectedRows.isEmpty()) {
                rowsListController.checkValues(selectedRows);
            } else {
                rowsListController.checkAll();
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void refreshCols() {
        try {
            List<String> selectedCols = colsListController.checkedValues();
            if (numberCols) {
                colsListController.setValues(tableController.data2D.numberColumnNames());
            } else {
                colsListController.setValues(tableController.data2D.columnNames());
            }
            if (selectedCols != null && !selectedCols.isEmpty()) {
                colsListController.checkValues(selectedCols);
            } else {
                colsListController.checkAll();
            }
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

    public List<Integer> checkedRowsIndices() {
        if (isAllData()) {
            checkedRowsIndices = new ArrayList<>();
            for (int i = 0; i < tableController.tableData.size(); i++) {
                checkedRowsIndices.add(i);
            }
        } else {
            checkedRowsIndices = new ArrayList<>();
            List<Integer> checked = rowsListController.checkedIndices();
            int size = tableController.tableData.size();
            for (int i : checked) {
                if (i < 0 || i >= size) {
                    continue;
                }
                checkedRowsIndices.add(i);
            }
        }
        return checkedRowsIndices;
    }

    public List<Integer> checkedColsIndices() {
        checkedColumnsNames = colsListController.checkedValues();
        if (checkedColumnsNames == null || checkedColumnsNames.isEmpty()) {
            return null;
        }
        checkedColsIndices = new ArrayList<>();
        for (String name : checkedColumnsNames) {
            int col = tableController.data2D.colOrder(name);
            if (col >= 0) {
                checkedColsIndices.add(col);
            }
        }
        return checkedColsIndices;
    }

    public List<String> checkedColumnsNames() {
        return colsListController.checkedValues();
    }

    public List<List<String>> selectedData() {
        try {
            checkedRowsIndices = checkedRowsIndices();
            checkedColsIndices = checkedColsIndices();
            if (checkedColsIndices == null || checkedColsIndices.isEmpty()
                    || checkedRowsIndices == null || checkedRowsIndices.isEmpty()) {
                return null;
            }
            List<List<String>> data = new ArrayList<>();
            for (int row : checkedRowsIndices) {
                if (row < 0 || row >= tableController.tableData.size()) {
                    continue;
                }
                List<String> tableRow = tableController.tableData.get(row);
                List<String> newRow = new ArrayList<>();
                for (int col : checkedColsIndices) {
                    int index = col + 1;
                    if (index < 0 || index >= tableRow.size()) {
                        continue;
                    }
                    newRow.add(tableRow.get(index));
                }
                data.add(newRow);
            }
            return data;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public List<List<String>> pageData() {
        try {
            checkedColsIndices = checkedColsIndices();
            if (checkedColsIndices == null || checkedColsIndices.isEmpty()) {
                return null;
            }
            List<List<String>> data = new ArrayList<>();
            for (int row = 0; row < tableController.tableData.size(); row++) {
                List<String> tableRow = tableController.tableData.get(row);
                List<String> newRow = new ArrayList<>();
                for (int col : checkedColsIndices) {
                    newRow.add(tableRow.get(col + 1));
                }
                data.add(newRow);
            }
            return data;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
