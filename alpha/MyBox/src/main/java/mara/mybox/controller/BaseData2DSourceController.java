package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ExpressionCalculator;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public class BaseData2DSourceController extends ControlData2DLoad {

    protected ControlData2DEditTable tableController;
    protected List<Integer> checkedRowsIndices, checkedColsIndices;
    protected List<String> checkedColsNames;
    protected List<Data2DColumn> checkedColumns;
    protected boolean idExclude = false, noColumnSelection = false;
    protected ChangeListener<Boolean> tableLoadListener, tableStatusListener;

    @FXML
    protected ToggleGroup rowsGroup;
    @FXML
    protected RadioButton selectedRadio, allPagesRadio, currentPageRadio;
    @FXML
    protected VBox dataBox;
    @FXML
    protected ControlData2DRowFilter filterController;


    /*
        controls
     */
    @Override
    public void initControls() {
        try {
            super.initControls();

            String rowsType = UserConfig.getString(baseName + "RowsSelection", "CurrentPage");
            if (rowsType == null) {
                currentPageRadio.fire();
            } else if ("AllPages".equals(rowsType)) {
                allPagesRadio.fire();
            } else if ("Selected".equals(rowsType)) {
                selectedRadio.fire();
            }
            rowsGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    if (allPagesRadio.isSelected()) {
                        UserConfig.setString(baseName + "RowsSelection", "AllPages");
                    } else if (selectedRadio.isSelected()) {
                        UserConfig.setString(baseName + "RowsSelection", "Selected");
                    } else {
                        UserConfig.setString(baseName + "RowsSelection", "CurrentPage");
                    }
                    notifySelected();
                }
            });

            filterController.setParameters(this);

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

            expressionCalculator.setWebEngine(tableController.dataController.viewController.htmlController.webEngine);

            sourceChanged();

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
            makeColumns();
            isSettingValues = true;
            tableData.setAll(tableController.tableData);
            currentPage = tableController.currentPage;
            startRowOfCurrentPage = tableController.startRowOfCurrentPage;
            pageSize = tableController.pageSize;
            pagesNumber = tableController.pagesNumber;
            dataSize = tableController.dataSize;
            dataSizeLoaded = true;
            data2D.setExpressionCalculator(expressionCalculator);
            expressionCalculator.setData2D(data2D);
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
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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

    @Override
    public void postLoadedTableData() {
        super.postLoadedTableData();
        restoreSelections();
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

    public void restoreSelections() {
        selectRows(checkedRowsIndices);
        selectCols(checkedColsIndices);
    }

    /*
        status
     */
    public boolean checkSelections() {
        if (!checkRowFilter() || !checkedRows() || !checkColumns()) {
            return false;
        }
        if ((allPagesRadio.isSelected() || (checkedRowsIndices != null && !checkedRowsIndices.isEmpty()))
                && (noColumnSelection || (checkedColsIndices != null && !checkedColsIndices.isEmpty()))) {
            return true;
        } else {
            error = message("SelectToHandle");
            return false;
        }

    }

    public boolean checkRowFilter() {
        error = null;
        if (data2D == null || !data2D.hasData()) {
            error = message("InvalidData");
            return false;
        }
        if (!filterController.checkExpression(isAllPages())) {
            error = filterController.error;
            return false;
        } else {
            return true;
        }
    }

    public boolean isAllPages() {
        return allPagesRadio.isSelected();
    }

    public boolean isSquare() {
        return checkedRowsIndices != null && checkedColsIndices != null
                && !checkedRowsIndices.isEmpty()
                && checkedRowsIndices.size() == checkedColsIndices.size();
    }

    public boolean hasRowFilter() {
        String script = filterController.scriptInput.getText();
        return script != null && !script.isBlank();
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
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            error = e.toString();
            return false;
        }
    }

    public void selectCols(List<Integer> cols) {
        try {
            if (noColumnSelection) {
                return;
            }
            isSettingValues = true;
            if (cols != null && !cols.isEmpty() && cols.size() != tableView.getColumns().size() - 2) {
                for (int i = 2; i < tableView.getColumns().size(); i++) {
                    TableColumn tableColumn = tableView.getColumns().get(i);
                    CheckBox cb = (CheckBox) tableColumn.getGraphic();
                    int col = data2D.colOrder(cb.getText());
                    cb.setSelected(col >= 0 && cols.contains(col));
                }
            } else {
                selectNoneColumn();
            }
            isSettingValues = false;
            columnSelected();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    /*
        rows
     */
    // If none selected then select all
    private boolean checkedRows() {
        try {
            checkedRowsIndices = new ArrayList<>();
            if (allPagesRadio.isSelected()) {
                return true;
            }
            ExpressionCalculator calculator = data2D.getExpressionCalculator();
            calculator.filterPassedNumber = 0;
            List<Integer> selected = tableView.getSelectionModel().getSelectedIndices();
            if (currentPageRadio.isSelected() || selected == null || selected.isEmpty()) {
                for (int i = 0; i < tableData.size(); i++) {
                    if (!calculator.filterTableRow(tableData.get(i), i)) {
                        continue;
                    }
                    if (calculator.reachMaxFilterPassed()) {
                        break;
                    }
                    checkedRowsIndices.add(i);
                }
            } else {
                for (int i : selected) {
                    if (!calculator.filterTableRow(tableData.get(i), i)) {
                        continue;
                    }
                    if (calculator.reachMaxFilterPassed()) {
                        break;
                    }
                    checkedRowsIndices.add(i);
                }
            }
            return true;
        } catch (Exception e) {
            error = e.toString();
            MyBoxLog.debug(e);
            return false;
        }
    }

    // If none selected then select all
    public List<List<String>> selectedData(boolean showRowNumber) {
        return selectedData(checkedColsIndices, showRowNumber);
    }

    public List<List<String>> selectedData(List<Integer> cols, boolean showRowNumber) {
        return selectedData(checkedRowsIndices, checkedColsIndices, showRowNumber);
    }

    public List<List<String>> selectedData(List<Integer> rows, List<Integer> cols, boolean showRowNumber) {
        try {
            if (rows == null || rows.isEmpty()
                    || cols == null || cols.isEmpty()) {
                return null;
            }
            List<List<String>> data = new ArrayList<>();
            int size = tableData.size();
            for (int row : rows) {
                if (row < 0 || row >= size) {
                    continue;
                }
                List<String> tableRow = tableData.get(row);
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
            }
            return data;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public void selectRows(List<Integer> rows) {
        try {
            isSettingValues = true;
            if (rows != null && !rows.isEmpty() && rows.size() != tableData.size()) {
                for (int i = 0; i < tableData.size(); i++) {
                    if (rows.contains(i)) {
                        tableView.getSelectionModel().select(i);
                    } else {
                        tableView.getSelectionModel().clearSelection(i);
                    }
                }
            } else {
                tableView.getSelectionModel().clearSelection();
            }
            isSettingValues = false;
            notifySelected();
        } catch (Exception e) {
            MyBoxLog.debug(e);
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
