package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public class BaseData2DRowsColumnsController extends BaseData2DSourceRowsController {

    protected List<Integer> checkedColsIndices;
    protected List<String> checkedColsNames;
    protected List<Data2DColumn> checkedColumns;

    public void setParameters(BaseController parent) {
        try {
            if (parent == null || filterController == null) {
                return;
            }
            this.parentController = parent;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void updateStatus() {
        try {
            super.updateStatus();

            if (filterController != null) {
                filterController.setData2D(data2D);
            }
            refreshControls();
            if (toolbar != null) {
                if (data2D != null && data2D.isDataFile() && data2D.getFile() != null) {
                    if (!toolbar.getChildren().contains(fileMenuButton)) {
                        toolbar.getChildren().add(2, fileMenuButton);
                    }
                } else {
                    if (toolbar.getChildren().contains(fileMenuButton)) {
                        toolbar.getChildren().remove(fileMenuButton);
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void restoreSelections() {
        try {
            isSettingValues = true;
            if (selectedRowsIndices != null && !selectedRowsIndices.isEmpty()
                    && selectedRowsIndices.size() != tableData.size()) {
                for (int i = 0; i < tableData.size(); i++) {
                    if (selectedRowsIndices.contains(i)) {
                        tableView.getSelectionModel().select(i);
                    } else {
                        tableView.getSelectionModel().clearSelection(i);
                    }
                }
            } else {
                tableView.getSelectionModel().clearSelection();
            }

            if (checkedColsIndices != null && !checkedColsIndices.isEmpty()
                    && checkedColsIndices.size() != tableView.getColumns().size() - 2) {
                for (int i = 2; i < tableView.getColumns().size(); i++) {
                    TableColumn tableColumn = tableView.getColumns().get(i);
                    CheckBox cb = (CheckBox) tableColumn.getGraphic();
                    int col = data2D.colOrder(cb.getText());
                    cb.setSelected(col >= 0 && checkedColsIndices.contains(col));
                }
            } else {
                selectNoneColumn();
            }

            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }

    }

    /*
        columns
     */
    @Override
    public void makeColumns() {
        try {
            if (!isValidData()) {
                return;
            }
            super.makeColumns();

            for (int i = tableColumnStartIndex(); i < tableView.getColumns().size(); i++) {
                TableColumn tableColumn = tableView.getColumns().get(i);
                CheckBox cb = new CheckBox(tableColumn.getText());
                cb.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        columnSelected();
                    }
                });
                tableColumn.setGraphic(cb);
                tableColumn.setText(null);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    // If none selected then select all
    public boolean checkColumns() {
        try {
            if (data2D == null) {
                return false;
            }
            checkedColsIndices = new ArrayList<>();
            checkedColsNames = new ArrayList<>();
            checkedColumns = new ArrayList<>();
            List<Integer> allIndices = new ArrayList<>();
            List<String> allNames = new ArrayList<>();
            List<Data2DColumn> allCols = new ArrayList<>();
            for (int i = 2; i < tableView.getColumns().size(); i++) {
                TableColumn tableColumn = tableView.getColumns().get(i);
                CheckBox cb = (CheckBox) tableColumn.getGraphic();
                String name = cb.getText();
                int col = data2D.colOrder(name);
                if (col >= 0) {
                    allIndices.add(col);
                    allNames.add(name);
                    Data2DColumn dcol = data2D.getColumns().get(col).cloneAll();
                    allCols.add(dcol);
                    if (cb.isSelected()) {
                        checkedColsIndices.add(col);
                        checkedColsNames.add(name);
                        checkedColumns.add(dcol);
                    }
                }
            }
            if (checkedColsIndices.isEmpty()) {
                checkedColsIndices = allIndices;
                checkedColsNames = allNames;
                checkedColumns = allCols;
            }
            if (checkedColsIndices.isEmpty()) {
                popError(message("SelectToHandle") + ": " + message("Columns"));
                return false;
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @FXML
    public void selectAllColumns() {
        setColumnsSelected(true);
    }

    @FXML
    public void selectNoneColumn() {
        setColumnsSelected(false);
    }

    public void setColumnsSelected(boolean select) {
        try {
            isSettingValues = true;
            for (int i = 2; i < tableView.getColumns().size(); i++) {
                TableColumn tableColumn = tableView.getColumns().get(i);
                CheckBox cb = (CheckBox) tableColumn.getGraphic();
                cb.setSelected(select);
            }
            isSettingValues = false;
            columnSelected();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void columnSelected() {
    }

    /*
        status
     */
    public boolean checkSelections() {
        return checkRowsFilter() && checkedRows() && checkColumns();
    }

    public List<List<String>> tableFiltered(boolean showRowNumber) {
        return rowsFiltered(checkedColsIndices, showRowNumber);
    }

    public List<List<String>> selectedData(FxTask task, boolean hasHeaders) {
        return selectedData(task, checkedColsIndices, false, hasHeaders);
    }

    /*
        task
     */
    @Override
    public boolean checkOptions() {
        try {
            if (isSettingValues) {
                return true;
            }
            if (!hasData()) {
                popError(message("NoData"));
                return false;
            }
            if (!checkColumns() || !checkRowsFilter() || !checkedRows()) {
                return false;
            }
            if (!allPagesRadio.isSelected()
                    && selectedRowsIndices.isEmpty()) {
                popError(message("SelectToHandle") + ": " + message("Rows"));
                tabPane.getSelectionModel().select(dataTab);
                return false;
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

}
