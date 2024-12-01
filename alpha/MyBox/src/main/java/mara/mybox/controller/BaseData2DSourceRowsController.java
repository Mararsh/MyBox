package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataFilter;
import mara.mybox.data2d.tools.Data2DColumnTools;
import mara.mybox.data2d.writer.ListWriter;
import mara.mybox.db.data.ColumnDefinition;
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
public class BaseData2DSourceRowsController extends BaseData2DLoadController {

    protected BaseData2DLoadController dataController;
    protected List<Integer> selectedRowsIndices, filteredRowsIndices;
    protected boolean formatValues;

    @FXML
    protected Tab dataTab, filterTab;
    @FXML
    protected ToggleGroup rowsGroup;
    @FXML
    protected RadioButton selectedRadio, allPagesRadio, currentPageRadio;
    @FXML
    protected ControlData2DRowFilter filterController;

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();

            NodeStyleTools.setTooltip(selectedRadio, new Tooltip(message("SelectRowsComments")));
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void setParameters(BaseData2DTaskController taskController) {
        try {
            if (taskController == null) {
                return;
            }
            dataController = taskController.dataController;
            filterController = taskController.filterController;
            filterTab = taskController.filterTab;
            dataTab = taskController.sourceTab;
            if (tabPane == null) {
                tabPane = taskController.tabPane;
            }

            initParameters();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initParameters() {
        try {
            tableView.requestFocus();

            String rowsSelectionType = UserConfig.getString(baseName + "RowsSelectionType", "Selected");
            if ("AllPages".equals(rowsSelectionType)) {
                allPagesRadio.setSelected(true);
                setSelectable(false);
            } else if ("CurrentPage".equals(rowsSelectionType)) {
                currentPageRadio.setSelected(true);
                setSelectable(false);
            } else {
                selectedRadio.setSelected(true);
                setSelectable(true);
            }
            rowsGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    if (allPagesRadio.isSelected()) {
                        UserConfig.setString(baseName + "RowsSelectionType", "AllPages");
                        setSelectable(false);
                    } else if (selectedRadio.isSelected()) {
                        UserConfig.setString(baseName + "RowsSelectionType", "Selected");
                        setSelectable(true);
                    } else {
                        UserConfig.setString(baseName + "RowsSelectionType", "CurrentPage");
                        setSelectable(false);
                    }
                }
            });
            if (filterController != null) {
                filterController.setParameters(this);
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void sourceChanged(Data2D data) {
        try {
            data2D = data;
            makeColumns();
            updateTable(dataController.tableData);
            isSettingValues = true;
            currentPage = dataController.currentPage;
            startRowOfCurrentPage = dataController.startRowOfCurrentPage;
            pageSize = dataController.pageSize;
            pagesNumber = dataController.pagesNumber;
            dataSize = dataController.dataSize;
            dataSizeLoaded = true;
            data2D.setDataLoaded(true);
            isSettingValues = false;
            postLoadedTableData();
            refreshControls();
            notifyLoaded();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void refreshControls() {
        try {
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

            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }

    }

    public boolean isAllPages() {
        return allPagesRadio.isSelected();
    }

    @Override
    public void updateStatus() {
        super.updateStatus();
        if (dataBox != null) {
            dataBox.setDisable(data2D == null);
        }
    }

    // If none selected then select all
    public boolean checkedRows() {
        try {
            selectedRowsIndices = new ArrayList<>();
            DataFilter filter = data2D.filter;
            if (filter != null) {
                filter.start(null, data2D);
            }
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
            if (filter == null || !filter.needFilter()
                    || selectedRowsIndices == null || selectedRowsIndices.isEmpty()) {
                filteredRowsIndices = selectedRowsIndices;
            } else {
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
        return data2D != null && data2D.isValidDefinition() && !tableData.isEmpty();
    }

    /*
        filter
     */
    public boolean checkRowsFilter() {
        if (filterController != null
                && !filterController.checkExpression(isAllPages())) {
            String ferror = filterController.error;
            if (ferror != null && !ferror.isBlank()) {
                if (filterTab != null && tabPane != null) {
                    tabPane.getSelectionModel().select(filterTab);
                }
                alertError(ferror);
            }
            return false;
        } else {
            return true;
        }
    }

    // If none selected then select all
    public List<List<String>> rowsFiltered(List<Integer> cols, boolean showRowNumber) {
        return rowsFiltered(selectedRowsIndices, cols, showRowNumber);
    }

    public List<List<String>> rowsFiltered(List<Integer> rows, List<Integer> cols,
            boolean showRowNumber) {
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
                    if (v != null && formatValues) {
                        v = data2D.formatValue(col, v);
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

    public List<List<String>> selectedData(FxTask task, List<Integer> cols, boolean formatValues) {
        try {
            if (data2D == null || cols == null) {
                return null;
            }
            data2D.startFilter(filterController != null ? filterController.filter : null);
            if (!data2D.fillFilterStatistic()) {
                return null;
            }
            List<List<String>> data;
            if (isAllPages()) {
                ListWriter writer = new ListWriter();
                List<Data2DColumn> targetColumns = data2D.targetColumns(cols, false);
                writer.setColumns(targetColumns)
                        .setHeaderNames(Data2DColumnTools.toNames(targetColumns))
                        .setWriteHeader(true)
                        .setFormatValues(formatValues);
                data2D.copy(task, writer, cols,
                        false, ColumnDefinition.InvalidAs.Empty);
                data = writer.getRows();
            } else {
                data = rowsFiltered(cols, false);
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

}
