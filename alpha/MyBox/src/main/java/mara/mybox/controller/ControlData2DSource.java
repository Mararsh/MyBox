package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.HBox;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public class ControlData2DSource extends ControlData2DLoad {

    protected ControlData2DEditTable editController;
    protected List<Integer> checkedRowsIndices, checkedColsIndices;
    protected boolean idExclude = false;

    @FXML
    protected CheckBox columnsCheck, allPagesCheck;
    @FXML
    protected Label titleLabel;
    @FXML
    protected HBox buttonsBox;

    @Override
    public void initControls() {
        try {
            super.initControls();

            allPagesCheck.setSelected(UserConfig.getBoolean(baseName + "AllPages", false));
            allPagesCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "AllPages", allPagesCheck.isSelected());
                    notifySelect();
                }
            });

            columnsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    if (columnsCheck.isSelected()) {
                        selectAllCols();
                    } else {
                        selectNoneCols();
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void showAllPages(boolean show) {
        if (show) {
            if (!buttonsBox.getChildren().contains(allPagesCheck)) {
                buttonsBox.getChildren().add(allPagesCheck);
            }
        } else {
            if (buttonsBox.getChildren().contains(allPagesCheck)) {
                allPagesCheck.setSelected(false);
                buttonsBox.getChildren().remove(allPagesCheck);
            }
        }
    }

    public void idExclude(boolean idExclude) {
        this.idExclude = idExclude;
    }

    public void setParameters(BaseController parent, ControlData2DEditTable editController) {
        try {
            if (editController == null) {
                return;
            }
            this.parentController = parent;
            this.editController = editController;
            data2D = editController.data2D;

            updateData();
            editController.statusNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    updateData();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void updateData() {
        if (editController == null) {
            return;
        }
        data2D = editController.data2D.cloneAll();
        makeColumns();
        isSettingValues = true;
        tableData.setAll(editController.tableData);
        isSettingValues = false;
        notifyLoaded();
        checkChanged();
    }

    public void checkChanged() {
        if (!data2D.isMutiplePages() || data2D.isTableChanged()) {
            allPagesCheck.setSelected(false);
            allPagesCheck.setDisable(true);
        } else {
            allPagesCheck.setDisable(false);
        }
    }

    public boolean allPages() {
        checkChanged();
        return allPagesCheck.isSelected();
    }

    @Override
    public void makeColumns() {
        try {
            if (!validateData()) {
                return;
            }
            super.makeColumns();
            for (int i = 2; i < tableView.getColumns().size(); i++) {
                TableColumn tableColumn = tableView.getColumns().get(i);
                CheckBox cb = new CheckBox(tableColumn.getText());
                cb.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        notifySelect();
                    }
                });
                tableColumn.setGraphic(cb);
                tableColumn.setText(null);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void postLoadedTableData() {
        super.postLoadedTableData();
        restoreSelections();
    }

    // If none selected then select all
    public List<Integer> checkedRowsIndices() {
        try {
            checkedRowsIndices = new ArrayList<>();
            List<Integer> selected = tableView.getSelectionModel().getSelectedIndices();
            if (allPages() || selected == null || selected.isEmpty()) {
                for (int i = 0; i < tableData.size(); i++) {
                    checkedRowsIndices.add(i);
                }
            } else {
                for (int i : selected) {
                    checkedRowsIndices.add(i);
                }
            }
            return checkedRowsIndices;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public List<List<String>> selectedRows() {
        try {
            List<List<String>> data = new ArrayList<>();
            List<List<String>> selected = tableView.getSelectionModel().getSelectedItems();
            if (allPages() || selected == null || selected.isEmpty()) {
                for (int i = 0; i < tableData.size(); i++) {
                    data.add(tableData.get(i));
                }
            } else {
                for (List<String> d : selected) {
                    data.add(d);
                }
            }
            return data;
        } catch (Exception e) {
            MyBoxLog.debug(e);
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
            notifySelect();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    public void selectAllCols() {
        try {
            isSettingValues = true;
            for (int i = 2; i < tableView.getColumns().size(); i++) {
                TableColumn tableColumn = tableView.getColumns().get(i);
                CheckBox cb = (CheckBox) tableColumn.getGraphic();
                cb.setSelected(true);
            }
            isSettingValues = false;
            notifySelect();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    public void selectNoneCols() {
        try {
            isSettingValues = true;
            for (int i = 2; i < tableView.getColumns().size(); i++) {
                TableColumn tableColumn = tableView.getColumns().get(i);
                CheckBox cb = (CheckBox) tableColumn.getGraphic();
                cb.setSelected(false);
            }
            isSettingValues = false;
            notifySelect();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    // If none selected then select all
    public List<Integer> checkedColsIndices() {
        try {
            checkedColsIndices = new ArrayList<>();
            List<Integer> all = new ArrayList<>();
            int idOrder = -1;
            if (idExclude) {
                idOrder = data2D.idOrder();
            }
            for (int i = 2; i < tableView.getColumns().size(); i++) {
                TableColumn tableColumn = tableView.getColumns().get(i);
                CheckBox cb = (CheckBox) tableColumn.getGraphic();
                int col = data2D.colOrder(cb.getText());
                if (col >= 0 && col != idOrder) {
                    all.add(col);
                    if (cb.isSelected()) {
                        checkedColsIndices.add(col);
                    }
                }
            }
            if (checkedColsIndices.isEmpty()) {
                checkedColsIndices = all;
            }
            return checkedColsIndices;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    // If none selected then select all
    public List<String> checkedColsNames() {
        try {
            List<String> names = new ArrayList<>();
            List<String> all = new ArrayList<>();
            int idOrder = -1;
            if (idExclude) {
                idOrder = data2D.idOrder();
            }
            for (int i = 2; i < tableView.getColumns().size(); i++) {
                TableColumn tableColumn = tableView.getColumns().get(i);
                CheckBox cb = (CheckBox) tableColumn.getGraphic();
                int col = data2D.colOrder(cb.getText());
                if (col >= 0 && col != idOrder) {
                    all.add(cb.getText());
                    if (cb.isSelected()) {
                        names.add(cb.getText());
                    }
                }
            }
            if (names.isEmpty()) {
                names = all;
            }
            return names;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    // If none selected then select all
    public List<Data2DColumn> checkedCols() {
        try {
            List<Data2DColumn> cols = new ArrayList<>();
            List<Data2DColumn> all = new ArrayList<>();
            int idOrder = -1;
            if (idExclude) {
                idOrder = data2D.idOrder();
            }
            for (int i = 2; i < tableView.getColumns().size(); i++) {
                TableColumn tableColumn = tableView.getColumns().get(i);
                CheckBox cb = (CheckBox) tableColumn.getGraphic();
                int col = data2D.colOrder(cb.getText());
                if (col >= 0 && col != idOrder) {
                    Data2DColumn dcol = data2D.getColumns().get(col).cloneAll();
                    all.add(dcol);
                    if (cb.isSelected()) {
                        cols.add(dcol);
                    }
                }
            }
            if (cols.isEmpty()) {
                cols = all;
            }
            return cols;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    // If none selected then select all
    public List<List<String>> selectedData(boolean rowNumber) {
        try {
            if (!checkSelections()) {
                return null;
            }
            return selectedData(checkedRowsIndices, checkedColsIndices, rowNumber);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public List<List<String>> selectedData(List<Integer> rows, List<Integer> cols, boolean rowNumber) {
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

    public boolean checkSelections() {
        checkedRowsIndices = checkedRowsIndices();
        checkedColsIndices = checkedColsIndices();
        return checkedRowsIndices != null && !checkedRowsIndices.isEmpty()
                && checkedColsIndices != null && !checkedColsIndices.isEmpty();
    }

    public boolean isSquare() {
        checkedRowsIndices = checkedRowsIndices();
        checkedColsIndices = checkedColsIndices();
        return checkedRowsIndices != null && checkedColsIndices != null
                && !checkedRowsIndices.isEmpty()
                && checkedRowsIndices.size() == checkedColsIndices.size();
    }

    public void selectCols(List<Integer> cols) {
        try {
            isSettingValues = true;
            if (cols != null && !cols.isEmpty() && cols.size() != tableView.getColumns().size() - 2) {
                for (int i = 2; i < tableView.getColumns().size(); i++) {
                    TableColumn tableColumn = tableView.getColumns().get(i);
                    CheckBox cb = (CheckBox) tableColumn.getGraphic();
                    int col = data2D.colOrder(cb.getText());
                    cb.setSelected(col >= 0 && cols.contains(col));
                }
            } else {
                selectNoneCols();
            }
            isSettingValues = false;
            notifySelect();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void restoreSelections() {
        selectRows(checkedRowsIndices);
        selectCols(checkedColsIndices);
    }

    public void setLabel(String s) {
        titleLabel.setText(s);
    }

}
