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
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public class ControlData2DSource extends ControlData2DLoad {

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
    protected FlowPane rowsPane, columnsPane;
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

            filterController.setParamters(this);
            tableView.requestFocus();

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
        if (noColumnSelection) {
            if (dataBox.getChildren().contains(columnsPane)) {
                dataBox.getChildren().remove(columnsPane);
            }
        } else {
            if (!dataBox.getChildren().contains(columnsPane)) {
                dataBox.getChildren().add(2, columnsPane);
            }
        }
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
            notifySelected();
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
            notifySelected();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void restoreSelections() {
        selectRows(checkedRowsIndices);
        selectCols(checkedColsIndices);
    }

    /*
        status
     */
    public boolean checkSelections() {
        if (data2D == null) {
            return false;
        }
        if (!checkRowFilter()) {
            return false;
        }
        checkedRows();
        checkColumns();
        return (allPagesRadio.isSelected() || (checkedRowsIndices != null && !checkedRowsIndices.isEmpty()))
                && (noColumnSelection || (checkedColsIndices != null && !checkedColsIndices.isEmpty()));
    }

    public boolean checkRowFilter() {
        if (data2D == null) {
            return false;
        }
        data2D.setError(null);
        return filterController.checkExpression();
    }

    public boolean isAllPages() {
        return allPagesRadio.isSelected();
    }

    public boolean notSelectColumn() {
        if (noColumnSelection) {
            return true;
        }
        for (int i = 2; i < tableView.getColumns().size(); i++) {
            TableColumn tableColumn = tableView.getColumns().get(i);
            CheckBox cb = (CheckBox) tableColumn.getGraphic();
            if (cb.isSelected()) {
                return false;
            }
        }
        return true;
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
                        notifySelected();
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
    private boolean checkColumns() {
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
            notifySelected();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    /*
        rows
     */
    // If none selected then select all
    private void checkedRows() {
        try {
            checkedRowsIndices = new ArrayList<>();
            if (allPagesRadio.isSelected()) {
                return;
            }
            List<Integer> selected = tableView.getSelectionModel().getSelectedIndices();
            if (currentPageRadio.isSelected() || selected == null || selected.isEmpty()) {
                for (int i = 0; i < tableData.size(); i++) {
                    if (!data2D.filter(tableData.get(i), i)) {
                        continue;
                    }
                    checkedRowsIndices.add(i);
                }
            } else {
                for (int i : selected) {
                    if (!data2D.filter(tableData.get(i), i)) {
                        continue;
                    }
                    checkedRowsIndices.add(i);
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    // If none selected then select all
    public List<List<String>> selectedData(boolean rowNumber) {
        return selectedData(checkedColsIndices, rowNumber);
    }

    public List<List<String>> selectedData(List<Integer> cols, boolean rowNumber) {
        try {
            if (checkedRowsIndices == null || checkedRowsIndices.isEmpty()
                    || cols == null || cols.isEmpty()) {
                return null;
            }
            List<List<String>> data = new ArrayList<>();
            int size = tableData.size();
            for (int row : checkedRowsIndices) {
                if (row < 0 || row >= size) {
                    continue;
                }
                List<String> tableRow = tableData.get(row);
                List<String> newRow = new ArrayList<>();
                if (rowNumber) {
                    newRow.add((row + 1) + "");
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
