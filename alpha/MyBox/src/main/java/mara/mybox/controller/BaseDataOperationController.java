package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Window;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-9-4
 * @License Apache License Version 2.0
 */
public abstract class BaseDataOperationController extends BaseController {

    protected ControlSheet sheetController;

    @FXML
    protected Label targetLabel;
    @FXML
    protected ControlListCheckBox rowsListController, colsListController;
    @FXML
    protected ComboBox<String> rowSelector, colSelector;
    @FXML
    protected ToggleGroup colGroup, rowGroup;
    @FXML
    protected RadioButton rowCheckedRadio, rowCurrentPageRadio, rowAllRadio, rowSelectRadio,
            colCheckedRadio, colAllRadio, colSelectRadio;
    @FXML
    protected Button selectAllRowsButton, selectNoneRowsButton, selectAllColsButton, selectNoneColsButton;

    @Override
    public void setStageStatus() {
        setAsPopup(baseName);
    }

    public void setParameters(ControlSheet sheetController, int row, int col) {
        try {
            this.sheetController = sheetController;

            updateControls();

            if (rowsListController != null) {
                rowsListController.setParent(sheetController);
            }
            if (colsListController != null) {
                colsListController.setParent(sheetController);
            }

            if (row >= 0) {
                if (rowSelectRadio != null) {
                    rowSelectRadio.fire();
                }
                if (rowSelector != null) {
                    rowSelector.setValue(row + "");
                }
                if (rowsListController != null) {
                    rowsListController.setCheckIndices(Arrays.asList(row));
                }
            }

            if (col >= 0) {
                if (colSelectRadio != null) {
                    colSelectRadio.fire();
                }
                if (colSelector != null) {
                    colSelector.setValue(sheetController.columns.get(col).getName());
                }
                if (colsListController != null) {
                    colsListController.setCheckIndices(Arrays.asList(col));
                }
            }

            if (rowGroup != null && rowsListController != null && rowSelectRadio != null) {
                rowGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                    @Override
                    public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                        rowsListController.listView.setDisable(!rowSelectRadio.isSelected());
                        selectAllRowsButton.setDisable(!rowSelectRadio.isSelected());
                        selectNoneRowsButton.setDisable(!rowSelectRadio.isSelected());
                    }
                });
                rowsListController.listView.setDisable(!rowSelectRadio.isSelected());
                selectAllRowsButton.setDisable(!rowSelectRadio.isSelected());
                selectNoneRowsButton.setDisable(!rowSelectRadio.isSelected());
            }

            if (colGroup != null && colsListController != null && colSelectRadio != null) {
                colGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                    @Override
                    public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                        colsListController.listView.setDisable(!colSelectRadio.isSelected());
                        selectAllColsButton.setDisable(!colSelectRadio.isSelected());
                        selectNoneColsButton.setDisable(!colSelectRadio.isSelected());
                    }
                });
                colsListController.listView.setDisable(!colSelectRadio.isSelected());
                selectAllColsButton.setDisable(!colSelectRadio.isSelected());
                selectNoneColsButton.setDisable(!colSelectRadio.isSelected());
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void updateControls() {
        try {
            getMyStage().setTitle(sheetController.getBaseTitle());
            sourceFile = sheetController.sourceFile;

            if (rowGroup != null) {
                if (!sheetController.rowsSelected()) {
                    rowCheckedRadio.setDisable(true);
                    if (rowCheckedRadio.isSelected()) {
                        rowCurrentPageRadio.fire();
                    }
                } else {
                    rowCheckedRadio.setDisable(false);
                }
                rowAllRadio.setDisable(sheetController.pagesNumber <= 1 || sheetController.dataChangedNotify.get());
            }
            List<String> rows = new ArrayList<>();
            for (long i = sheetController.pageStart2(); i < sheetController.pageEnd2(); i++) {
                rows.add((i + 1) + "");
            }
            if (rowSelector != null) {
                String v = rowSelector.getValue();
                rowSelector.getItems().setAll(rows);
                if (v != null && rows.contains(v)) {
                    rowSelector.setValue(v);
                } else if (!rows.isEmpty()) {
                    rowSelector.getSelectionModel().select(0);
                }
            }
            if (rowsListController != null) {
                rowsListController.setValues(rows);
            }

            if (colGroup != null) {
                if (!sheetController.colsSelected()) {
                    colCheckedRadio.setDisable(true);
                    if (colCheckedRadio.isSelected()) {
                        colAllRadio.fire();
                    }
                } else {
                    colCheckedRadio.setDisable(false);
                }
            }
            List<String> cols = new ArrayList<>();
            if (sheetController.columns != null) {
                for (ColumnDefinition c : sheetController.columns) {
                    cols.add(c.getName());
                }
            }
            if (colSelector != null) {
                String v = colSelector.getValue();
                colSelector.getItems().setAll(cols);
                if (v != null && cols.contains(v)) {
                    colSelector.setValue(v);
                } else if (!cols.isEmpty()) {
                    colSelector.getSelectionModel().select(0);
                }
            }
            if (colsListController != null) {
                colsListController.setValues(cols);
            }

            if (sourceFile != null) {
                targetLabel.setText(message("File") + ": " + sourceFile.getAbsolutePath());
            } else {
                targetLabel.setText("");
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

    @FXML
    @Override
    public void cancelAction() {
        close();
    }

    @FXML
    public void refreshAction() {
        updateControls();
    }

    public List<Integer> selectedCols(ControlListCheckBox listController) {
        List<String> names = listController.checkedOrderedValues();
        if (names == null) {
            return null;
        }
        List<Integer> indices = new ArrayList<>();
        for (String name : names) {
            int index = sheetController.colIndex(name);
            if (index >= 0) {
                indices.add((Integer) index);
            }
        }
        return indices;
    }

    public List<Integer> selectedCols() {
        return selectedCols(colsListController);
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
        return rowsListController.checkedIndices();
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

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        static
     */
    public static void update(ControlSheet sheetController) {
        try {
            List<Window> windows = new ArrayList<>();
            windows.addAll(Window.getWindows());
            for (Window window : windows) {
                Object object = window.getUserData();
                if (object != null && object instanceof BaseDataOperationController) {
                    BaseDataOperationController controller = (BaseDataOperationController) object;
                    if (controller.sheetController == sheetController) {
                        controller.updateControls();
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static void closeAll(ControlSheet sheetController) {
        try {
            List<Window> windows = new ArrayList<>();
            windows.addAll(Window.getWindows());
            for (Window window : windows) {
                Object object = window.getUserData();
                if (object != null && object instanceof BaseDataOperationController) {
                    BaseDataOperationController controller = (BaseDataOperationController) object;
                    if (controller.sheetController == sheetController) {
                        controller.close();
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
