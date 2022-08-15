package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import mara.mybox.data2d.DataFilter;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public class BaseData2DSourceController extends ControlData2DLoad {

    protected ControlData2DEditTable tableController;
    protected List<Integer> selectedRowsIndices, filteredRowsIndices, checkedColsIndices;
    protected List<String> checkedColsNames;
    protected List<Data2DColumn> checkedColumns;
    protected boolean idExclude = false, noColumnSelection = false;
    protected ChangeListener<Boolean> tableLoadListener, tableStatusListener;

    @FXML
    protected Tab dataTab, filterTab, optionsTab;
    @FXML
    protected ToggleGroup rowsGroup;
    @FXML
    protected RadioButton selectedRadio, allPagesRadio, currentPageRadio;
    @FXML
    protected VBox dataBox;
    @FXML
    protected ControlData2DFilter filterController;


    /*
        controls
     */
    @Override
    public void initControls() {
        try {
            super.initControls();

            String rowsSelectionType = UserConfig.getString(baseName + "RowsSelectionType", "Selected");
            if ("AllPages".equals(rowsSelectionType)) {
                allPagesRadio.fire();
            } else if ("CurrentPage".equals(rowsSelectionType)) {
                currentPageRadio.fire();
            } else {
                selectedRadio.fire();
            }
            rowsGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    if (allPagesRadio.isSelected()) {
                        UserConfig.setString(baseName + "RowsSelectionType", "AllPages");
                    } else if (selectedRadio.isSelected()) {
                        UserConfig.setString(baseName + "RowsSelectionType", "Selected");
                    } else {
                        UserConfig.setString(baseName + "RowsSelectionType", "CurrentPage");
                    }
                    notifySelected();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setParameters(BaseController parent, ControlData2DEditTable tableController) {
        try {
            if (tableController == null) {
                return;
            }
            this.parentController = parent;
            this.tableController = tableController;

            filterController.setParameters(this);

            tableLoadListener = new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    sourceLoaded();
                }
            };
            tableController.loadedNotify.addListener(tableLoadListener);

            tableStatusListener = new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    sourceChanged();
                }
            };
            tableController.statusNotify.addListener(tableStatusListener);

            tableView.requestFocus();

            sourceChanged();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setParameters(BaseController parent) {
        try {
            this.parentController = parent;
            loadedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    filterController.setData2D(data2D);
                    refreshControls();
                }
            });

            filterController.setParameters(this);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void idExclude(boolean idExclude) {
        this.idExclude = idExclude;
    }

    public void noColumnSelection(boolean noColumnSelection) {
        this.noColumnSelection = noColumnSelection;
    }

    public void sourceLoaded() {

    }

    public void sourceChanged() {
        try {
            if (tableController == null) {
                return;
            }
            data2D = tableController.data2D.cloneAll();
            data2D.filter = new DataFilter();
            makeColumns();
            isSettingValues = true;
            tableData.setAll(tableController.tableData);
            currentPage = tableController.currentPage;
            startRowOfCurrentPage = tableController.startRowOfCurrentPage;
            pageSize = tableController.pageSize;
            pagesNumber = tableController.pagesNumber;
            dataSize = tableController.dataSize;
            dataSizeLoaded = true;
            filterController.setData2D(data2D);
            isSettingValues = false;
            refreshControls();
            notifyLoaded();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void refreshControls() {
        try {
            if (data2D.isMutiplePages()) {
                allPagesRadio.setDisable(false);
                showPaginationPane(true);
                setPagination();
            } else {
                if (allPagesRadio.isSelected()) {
                    currentPageRadio.fire();
                }
                allPagesRadio.setDisable(true);
                showPaginationPane(false);
            }
            restoreSelections();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

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

            if (!noColumnSelection) {
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
            }

            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }

    }

    @Override
    protected void showPaginationPane(boolean show) {
        paginationPane.setVisible(show);
        if (show) {
            if (!dataBox.getChildren().contains(paginationPane)) {
                dataBox.getChildren().add(paginationPane);
            }
        } else {
            if (dataBox.getChildren().contains(paginationPane)) {
                dataBox.getChildren().remove(paginationPane);
            }
        }
    }

    @FXML
    public void selectAllColumns() {
        try {
            if (noColumnSelection) {
                return;
            }
            isSettingValues = true;
            for (int i = 2; i < tableView.getColumns().size(); i++) {
                TableColumn tableColumn = tableView.getColumns().get(i);
                CheckBox cb = (CheckBox) tableColumn.getGraphic();
                cb.setSelected(true);
            }
            isSettingValues = false;
            columnSelected();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    public void selectNoneColumn() {
        try {
            if (noColumnSelection) {
                return;
            }
            isSettingValues = true;
            for (int i = 2; i < tableView.getColumns().size(); i++) {
                TableColumn tableColumn = tableView.getColumns().get(i);
                CheckBox cb = (CheckBox) tableColumn.getGraphic();
                cb.setSelected(false);
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
        return checkRowFilter() && checkedRows() && checkColumns();
    }

    public boolean isAllPages() {
        return allPagesRadio.isSelected();
    }

    public boolean isSquare() {
        return selectedRowsIndices != null && checkedColsIndices != null
                && !selectedRowsIndices.isEmpty()
                && selectedRowsIndices.size() == checkedColsIndices.size();
    }

    /*
        columns
     */
    @Override
    public void makeColumns() {
        try {
            if (!validateData()) {
                return;
            }
            super.makeColumns();
            if (noColumnSelection) {
                return;
            }
            for (int i = 2; i < tableView.getColumns().size(); i++) {
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
            checkedColsIndices = new ArrayList<>();
            checkedColsNames = new ArrayList<>();
            checkedColumns = new ArrayList<>();
            if (noColumnSelection) {
                return true;
            }
            List<Integer> allIndices = new ArrayList<>();
            List<String> allNames = new ArrayList<>();
            List<Data2DColumn> allCols = new ArrayList<>();
            int idOrder = -1;
            if (idExclude) {
                idOrder = data2D.idOrder();
            }
            for (int i = 2; i < tableView.getColumns().size(); i++) {
                TableColumn tableColumn = tableView.getColumns().get(i);
                CheckBox cb = (CheckBox) tableColumn.getGraphic();
                int col = data2D.colOrder(cb.getText());
                if (col >= 0 && col != idOrder) {
                    allIndices.add(col);
                    allNames.add(cb.getText());
                    Data2DColumn dcol = data2D.getColumns().get(col).cloneAll();
                    allCols.add(dcol);
                    if (cb.isSelected()) {
                        checkedColsIndices.add(col);
                        checkedColsNames.add(cb.getText());
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

    /*
        rows
     */
    // If none selected then select all
    private boolean checkedRows() {
        try {
            selectedRowsIndices = new ArrayList<>();
            DataFilter filter = data2D.filter;
            filter.start(null, data2D);
            List<Integer> selected = tableView.getSelectionModel().getSelectedIndices();
            if (allPagesRadio.isSelected() || currentPageRadio.isSelected()
                    || selected == null || selected.isEmpty()) {
                for (int i = 0; i < tableData.size(); i++) {
                    selectedRowsIndices.add(i);
                }
            } else {
                for (int i : selected) {
                    selectedRowsIndices.add(i);
                }
            }
            if (!allPagesRadio.isSelected() && selectedRowsIndices.isEmpty()) {
                popError(message("SelectToHandle") + ": " + message("Rows"));
                return false;
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public List<Integer> tableRows() {
        try {
            List<Integer> rows = new ArrayList<>();
            for (int i = 0; i < tableData.size(); i++) {
                rows.add(i);
            }
            return rows;
        } catch (Exception e) {
            error = e.toString();
            MyBoxLog.debug(e);
            return null;
        }
    }

    /*
        filter
     */
    private boolean checkRowFilter() {
        if (!filterController.checkExpression(isAllPages())) {
            String ferror = filterController.error;
            if (ferror != null && !ferror.isBlank()) {
                if (filterTab != null) {
                    tabPane.getSelectionModel().select(filterTab);
                }
                alertError(ferror);
            }
            return false;
        } else {
            return true;
        }
    }

    public List<Integer> filteredRowsIndices() {
        try {
            DataFilter filter = data2D.filter;
            if (filter == null || !filter.needFilter()
                    || selectedRowsIndices == null || selectedRowsIndices.isEmpty()) {
                return selectedRowsIndices;
            }
            filteredRowsIndices = new ArrayList<>();
            int size = tableData.size();
            for (int row : selectedRowsIndices) {
                if (row < 0 || row >= size
                        || !filter.filterTableRow(data2D, tableData.get(row), row)) {
                    continue;
                }
                if (filter.reachMaxPassed()) {
                    break;
                }
                filteredRowsIndices.add(row);
            }
            return filteredRowsIndices;
        } catch (Exception e) {
            error = e.toString();
            MyBoxLog.debug(e);
            return null;
        }
    }

    // If none selected then select all
    public List<List<String>> filtered(boolean showRowNumber) {
        return filtered(checkedColsIndices, showRowNumber);
    }

    public List<List<String>> filtered(List<Integer> cols, boolean showRowNumber) {
        return filtered(selectedRowsIndices, cols, showRowNumber);
    }

    public List<List<String>> filtered(List<Integer> rows, List<Integer> cols, boolean showRowNumber) {
        try {
            if (rows == null || rows.isEmpty()
                    || cols == null || cols.isEmpty()) {
                return null;
            }
            List<List<String>> data = new ArrayList<>();
            int size = tableData.size();
            filteredRowsIndices = new ArrayList<>();
            data2D.resetFilterNumber();
            for (int row : rows) {
                if (row < 0 || row >= size) {
                    continue;
                }
                List<String> tableRow = tableData.get(row);
                if (!data2D.filterTableRow(tableData.get(row), row)) {
                    continue;
                }
                if (data2D.filterReachMaxPassed()) {
                    break;
                }

                List<String> newRow = new ArrayList<>();
                if (showRowNumber) {
                    if (data2D.isTmpData()) {
                        newRow.add((row + 1) + "");
                    } else {
                        newRow.add(tableRow.get(0) + "");
                    }
                }
                for (int col : cols) {
                    int index = col + 1;
                    if (index < 0 || index >= tableRow.size()) {
                        continue;
                    }
                    newRow.add(tableRow.get(index));
                }
                data.add(newRow);
                filteredRowsIndices.add(row);
            }
            return data;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    @Override
    public void cleanPane() {
        try {
            tableController.loadedNotify.removeListener(tableLoadListener);
            tableLoadListener = null;
            tableController.statusNotify.removeListener(tableStatusListener);
            tableStatusListener = null;
            tableController = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
