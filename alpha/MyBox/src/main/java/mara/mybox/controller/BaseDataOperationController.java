package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-9-4
 * @License Apache License Version 2.0
 */
public abstract class BaseDataOperationController extends BaseController {

    protected ControlSheet sheetController;

    @FXML
    protected ListView<Integer> rowsListView;
    @FXML
    protected ListView<String> colsListView;
    @FXML
    protected ToggleGroup colGroup, rowGroup;
    @FXML
    protected HBox rowBox, colBox;
    @FXML
    protected RadioButton rowCheckedRadio, rowCurrentPageRadio, rowAllRadio, rowSelectRadio,
            colCheckedRadio, colAllRadio, colSelectRadio, targetSysRadio, targetDataRadio;

    @Override
    public void setStageStatus() {
        setAsPopup(baseName);
    }

    public void setParameters(ControlSheet sheetController, int row, int col) {
        try {
            this.sheetController = sheetController;

            updateControls();
            if (rowsListView != null) {
                rowsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
                if (row >= 0) {
                    if (rowSelectRadio != null) {
                        rowSelectRadio.fire();
                    }
                    rowsListView.getSelectionModel().select(row);
                }
            }

            if (colsListView != null) {
                colsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
                if (col >= 0) {
                    if (colSelectRadio != null) {
                        colSelectRadio.fire();
                    }
                    colsListView.getSelectionModel().select(sheetController.columns.get(col).getName());
                }
            }

            if (rowGroup != null && rowsListView != null && rowSelectRadio != null) {
                rowGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                    @Override
                    public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                        rowsListView.setDisable(!rowSelectRadio.isSelected());
                    }
                });
                rowsListView.setDisable(!rowSelectRadio.isSelected());
            }

            if (colGroup != null && colsListView != null && colSelectRadio != null) {
                colGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                    @Override
                    public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                        colsListView.setDisable(!colSelectRadio.isSelected());
                    }
                });
                colsListView.setDisable(!colSelectRadio.isSelected());
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void updateControls() {
        try {
            if (rowGroup != null) {
                if (!sheetController.rowsSelected()) {
                    rowCheckedRadio.setDisable(true);
                    rowCurrentPageRadio.fire();
                } else {
                    rowCheckedRadio.setDisable(false);
                }
            }
            if (rowsListView != null) {
                List<Integer> rows = new ArrayList<>();
                for (long i = sheetController.pageStart(); i < sheetController.pageEnd(); i++) {
                    rows.add((int) i);
                }
                rowsListView.getItems().setAll(rows);
            }

            if (colGroup != null) {
                if (!sheetController.colsSelected()) {
                    colCheckedRadio.setDisable(true);
                    colAllRadio.fire();
                } else {
                    colCheckedRadio.setDisable(false);
                }
            }
            if (colsListView != null) {
                List<String> cols = new ArrayList<>();
                for (ColumnDefinition c : sheetController.columns) {
                    cols.add(c.getName());
                }
                colsListView.getItems().setAll(cols);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void cancelAction() {
        close();
    }

    @FXML
    public void refreshAction() {
        updateControls();
    }

    public List<Integer> selectedCols() {
        List<Integer> cols = new ArrayList<>();
        cols.addAll(colsListView.getSelectionModel().getSelectedIndices());// 0-based
        return cols;
    }

    public List<Integer> cols() {
        List<Integer> cols = null;
        if (colCheckedRadio.isSelected()) {
            cols = sheetController.colsIndex(false);
        } else if (colAllRadio.isSelected()) {
            cols = sheetController.colsIndex(true);
        } else if (colSelectRadio.isSelected()) {
            cols = selectedCols();
        }
        return cols;
    }

    public List<Integer> selectedRows() {
        List<Integer> rows = new ArrayList<>(); // 0-based
        rows.addAll(rowsListView.getSelectionModel().getSelectedIndices());// 0-based
        return rows;
    }

    public List<Integer> rows() {
        List<Integer> rows = null;
        if (rowCheckedRadio.isSelected()) {
            rows = sheetController.rowsIndex(false);

        } else if (rowCurrentPageRadio.isSelected()) {
            rows = sheetController.rowsIndex(true);

        } else if (rowAllRadio.isSelected()) {

        } else if (rowSelectRadio.isSelected()) {
            rows = selectedRows();
        }
        return rows;
    }

    @FXML
    @Override
    public void okAction() {
        try {
            List<Integer> cols = cols();

            List<Integer> rows = rows();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

}
