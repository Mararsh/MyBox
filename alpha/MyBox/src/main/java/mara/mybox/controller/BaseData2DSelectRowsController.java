package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataFilter;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.style.NodeStyleTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public class BaseData2DSelectRowsController extends BaseData2DViewController {

    protected BaseData2DLoadController tableController;
    protected List<Integer> selectedRowsIndices, filteredRowsIndices, checkedColsIndices, otherColsIndices;
    protected List<String> checkedColsNames, otherColsNames;
    protected List<Data2DColumn> checkedColumns, otherColumns;
    protected boolean idExclude = false, selectColumnsInTable = false, noCheckedColumnsMeansAll = true;
    protected ChangeListener<Boolean> tableLoadListener, tableStatusListener;

    @FXML
    protected Tab dataTab, filterTab, optionsTab, groupTab;
    @FXML
    protected ToggleGroup rowsGroup;
    @FXML
    protected RadioButton selectedRadio, allPagesRadio, currentPageRadio;
    @FXML
    protected ControlData2DRowFilter filterController;
    @FXML
    protected FlowPane columnsPane, otherColumnsPane;
    @FXML
    protected CheckBox formatValuesCheck;

    /*
        controls
     */
    @Override
    public void initControls() {
        try {
            super.initControls();

            if (rowsGroup != null) {
                String rowsSelectionType = UserConfig.getString(baseName + "RowsSelectionType", "Selected");
                if ("AllPages".equals(rowsSelectionType)) {
                    allPagesRadio.setSelected(true);
                } else if ("CurrentPage".equals(rowsSelectionType)) {
                    currentPageRadio.setSelected(true);
                } else {
                    selectedRadio.setSelected(true);
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
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParameters(BaseController parent, BaseData2DLoadController controller) {
        try {
            if (controller == null || filterController == null) {
                return;
            }
            this.parentController = parent;
            this.tableController = controller;

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
            MyBoxLog.error(e);
        }
    }

    public void setParameters(BaseController parent) {
        try {
            if (parent == null || filterController == null) {
                return;
            }
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
            MyBoxLog.error(e);
        }
    }

    public void idExclude(boolean idExclude) {
        this.idExclude = idExclude;
    }

    public void selectColumnsInTable(boolean select) {
        selectColumnsInTable = select;
    }

    public void sourceLoaded() {

    }

    public void sourceChanged() {
        try {
            if (tableController == null) {
                return;
            }
            data2D = tableController.data2D.cloneAll();
            makeColumns();
            isSettingValues = true;
            tableData.setAll(tableController.tableData);
            data2D.setPageData(tableData);
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
            MyBoxLog.error(e);
        }
    }

    public void refreshControls() {
        try {
            updateTitle();
            if (data2D == null) {
                return;
            }
            isSettingValues = true;
            if (data2D.isMutiplePages()) {
                allPagesRadio.setDisable(false);
                showPaginationPane(true);
                setPagination();
            } else {
                if (allPagesRadio.isSelected()) {
                    currentPageRadio.setSelected(true);
                }
                allPagesRadio.setDisable(true);
                showPaginationPane(false);
            }
            if (columnsPane != null) {
                columnsPane.getChildren().clear();
                List<String> names = data2D.columnNames();
                if (names != null) {
                    for (String name : names) {
                        columnsPane.getChildren().add(new CheckBox(name));
                    }
                }
            }

            if (otherColumnsPane != null) {
                otherColumnsPane.getChildren().clear();
                List<String> names = data2D.columnNames();
                if (names != null) {
                    for (String name : names) {
                        otherColumnsPane.getChildren().add(new CheckBox(name));
                    }
                }
            }
            isSettingValues = false;
            restoreSelections();

        } catch (Exception e) {
            MyBoxLog.error(e);
            isSettingValues = false;
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

            if (checkedColsIndices != null && !checkedColsIndices.isEmpty()
                    && checkedColsIndices.size() != tableView.getColumns().size() - 2) {
                if (selectColumnsInTable) {
                    for (int i = 2; i < tableView.getColumns().size(); i++) {
                        TableColumn tableColumn = tableView.getColumns().get(i);
                        CheckBox cb = (CheckBox) tableColumn.getGraphic();
                        int col = data2D.colOrder(cb.getText());
                        cb.setSelected(col >= 0 && checkedColsIndices.contains(col));
                    }
                } else {
                    if (columnsPane != null) {
                        for (Node node : columnsPane.getChildren()) {
                            CheckBox cb = (CheckBox) node;
                            int col = data2D.colOrder(cb.getText());
                            cb.setSelected(col >= 0 && checkedColsIndices.contains(col));
                        }
                    }
                }
            } else {
                selectNoneColumn();
            }

            if (otherColumnsPane != null) {
                if (otherColsIndices != null && !otherColsIndices.isEmpty()
                        && otherColsIndices.size() != tableView.getColumns().size() - 2) {
                    for (Node node : otherColumnsPane.getChildren()) {
                        CheckBox cb = (CheckBox) node;
                        int col = data2D.colOrder(cb.getText());
                        cb.setSelected(col >= 0 && otherColsIndices.contains(col));
                    }
                } else {
                    selectNoneOtherColumn();
                }
            }

            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }

    }

    @Override
    protected void showPaginationPane(boolean show) {
        if (paginationPane == null) {
            return;
        }
        paginationPane.setVisible(show);
        if (dataBox == null) {
            return;
        }
        if (show) {
            if (!dataBox.getChildren().contains(paginationPane)) {
                dataBox.getChildren().add(paginationPane);
            }
        } else {
            if (dataBox.getChildren().contains(paginationPane)) {
                dataBox.getChildren().remove(paginationPane);
            }
        }
        NodeStyleTools.refreshStyle(dataBox);
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
            if (!selectColumnsInTable) {
                if (columnsPane != null) {
                    isSettingValues = true;
                    for (Node node : columnsPane.getChildren()) {
                        CheckBox cb = (CheckBox) node;
                        cb.setSelected(select);
                    }
                    isSettingValues = false;
                }
                return;
            }
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

    @FXML
    public void selectAllOtherColumns() {
        setOtherColumnsSelected(true);
    }

    @FXML
    public void selectNoneOtherColumn() {
        setOtherColumnsSelected(false);
    }

    public void setOtherColumnsSelected(boolean select) {
        try {
            if (otherColumnsPane == null) {
                return;
            }
            isSettingValues = true;
            for (Node node : otherColumnsPane.getChildren()) {
                CheckBox cb = (CheckBox) node;
                cb.setSelected(select);
            }
            isSettingValues = false;
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

    @Override
    public boolean validateData() {
        if (dataBox != null) {
            dataBox.setDisable(data2D == null);
        }
        if (editButton != null) {
            editButton.setDisable(data2D == null);
        }
        return data2D != null && data2D.isValid();
    }

    /*
        page
     */
    @Override
    public void loadPage() {
        super.loadPage();
        if (filterController != null) {
            filterController.setData2D(data2D);
        }
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
            if (!selectColumnsInTable) {
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
            otherColsIndices = new ArrayList<>();
            otherColsNames = new ArrayList<>();
            otherColumns = new ArrayList<>();
            List<Integer> allIndices = new ArrayList<>();
            List<String> allNames = new ArrayList<>();
            List<Data2DColumn> allCols = new ArrayList<>();
            boolean needSelection = false;
            if (selectColumnsInTable) {
                needSelection = true;
                int idOrder = -1;
                if (idExclude) {
                    idOrder = data2D.idOrder();
                }
                for (int i = 2; i < tableView.getColumns().size(); i++) {
                    TableColumn tableColumn = tableView.getColumns().get(i);
                    CheckBox cb = (CheckBox) tableColumn.getGraphic();
                    String name = cb.getText();
                    int col = data2D.colOrder(name);
                    if (col >= 0 && col != idOrder) {
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
            } else {
                if (columnsPane != null) {
                    needSelection = true;
                    for (Node node : columnsPane.getChildren()) {
                        CheckBox cb = (CheckBox) node;
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
                }
            }

            if (noCheckedColumnsMeansAll && checkedColsIndices.isEmpty()) {
                checkedColsIndices = allIndices;
                checkedColsNames = allNames;
                checkedColumns = allCols;
            }
            if (needSelection && checkedColsIndices.isEmpty()) {
                popError(message("SelectToHandle") + ": " + message("Columns"));
                return false;
            }
            if (otherColumnsPane != null) {
                for (Node node : otherColumnsPane.getChildren()) {
                    CheckBox cb = (CheckBox) node;
                    String name = cb.getText();
                    int col = data2D.colOrder(name);
                    if (col >= 0) {
                        Data2DColumn dcol = data2D.getColumns().get(col).cloneAll();
                        if (cb.isSelected()) {
                            otherColsIndices.add(col);
                            otherColsNames.add(name);
                            otherColumns.add(dcol);
                        }
                    }
                }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public void selectColumns(List<String> names) {
        try {
            selectNoneColumn();
            if (names == null || names.isEmpty()) {
                return;
            }
            if (selectColumnsInTable) {
                for (int i = 2; i < tableView.getColumns().size(); i++) {
                    TableColumn tableColumn = tableView.getColumns().get(i);
                    CheckBox cb = (CheckBox) tableColumn.getGraphic();
                    if (names.contains(cb.getText())) {
                        cb.setSelected(true);
                    }
                }
            } else {
                if (columnsPane != null) {
                    for (Node node : columnsPane.getChildren()) {
                        CheckBox cb = (CheckBox) node;
                        if (names.contains(cb.getText())) {
                            cb.setSelected(true);
                        }
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
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

    public boolean hasData() {
        return data2D != null && data2D.isValid() && !tableData.isEmpty();
    }

    /*
        filter
     */
    private boolean checkRowFilter() {
        if (filterController != null && !filterController.checkExpression(isAllPages())) {
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
    public List<List<String>> tableFiltered(boolean showRowNumber) {
        return tableFiltered(checkedColsIndices, showRowNumber);
    }

    public List<List<String>> tableFiltered(List<Integer> cols, boolean showRowNumber) {
        return tableFiltered(selectedRowsIndices, cols, showRowNumber);
    }

    public List<List<String>> tableFiltered(List<Integer> rows, List<Integer> cols, boolean showRowNumber) {
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
                if (!data2D.filterTableRow(tableRow, row)) {
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
                    String v = tableRow.get(index);
                    if (v != null && formatValuesCheck != null && formatValuesCheck.isSelected()) {
                        v = data2D.column(col).format(v);
                    }
                    newRow.add(v);
                }
                data.add(newRow);
                filteredRowsIndices.add(row);
            }
            return data;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<List<String>> selectedData(FxTask task) {
        try {
            if (data2D == null || checkedColsIndices == null) {
                return null;
            }
            data2D.startFilter(filterController != null ? filterController.filter : null);
            if (!data2D.fillFilterStatistic()) {
                return null;
            }
            List<List<String>> data;
            if (isAllPages()) {
                DataFileCSV csv = data2D.copy(null, checkedColsIndices,
                        false, true, formatValuesCheck != null && formatValuesCheck.isSelected());
                if (csv == null) {
                    return null;
                }
                data = csv.allRows(false);
            } else {
                data = tableFiltered(false);
            }
            data2D.stopTask();
            return data;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.console(e);
            return null;
        }
    }

    @Override
    public void cleanPane() {
        try {
            if (tableController != null) {
                tableController.loadedNotify.removeListener(tableLoadListener);
                tableLoadListener = null;
                tableController.statusNotify.removeListener(tableStatusListener);
                tableStatusListener = null;
                tableController = null;
            }
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
