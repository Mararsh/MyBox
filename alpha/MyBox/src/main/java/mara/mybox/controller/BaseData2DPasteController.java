package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.HBox;
import mara.mybox.data2d.Data2D;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.cell.CellTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-9-13
 * @License Apache License Version 2.0
 */
public class BaseData2DPasteController extends ControlData2DSource {

    protected Data2DManufactureController targetController;
    protected Data2D dataTarget;
    protected int row, col;
    protected ChangeListener<Boolean> targetStatusListener;
    protected List<List<String>> data;

    @FXML
    protected HBox pasteBox, wayBox;
    @FXML
    protected ComboBox<String> rowSelector;
    @FXML
    protected ComboBox<Data2DColumn> colSelector;
    @FXML
    protected RadioButton replaceRadio, insertRadio, appendRadio;

    public BaseData2DPasteController() {
        baseTitle = message("PasteContentInMyBoxClipboard");
    }

    public void setParameters(Data2DManufactureController target) {
        try {
            targetController = target;
            dataTarget = targetController.data2D;

            initParameters();

            CellTools.makeColumnComboBox(colSelector);

            targetStatusListener = new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    updateTargetControls(row, col);
                }
            };
            targetController.statusNotify.addListener(targetStatusListener);

            updateTargetControls(0, 0);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void updateTargetControls(int row, int col) {
        try {
            if (dataTarget == null) {
                return;
            }
            List<String> rows = new ArrayList<>();
            for (long i = 0; i < dataTarget.tableRowsNumber(); i++) {
                rows.add("" + (i + 1));
            }
            if (!rows.isEmpty()) {
                rowSelector.getItems().setAll(rows);
                rowSelector.getSelectionModel().select(row);
            } else {
                rowSelector.getItems().clear();
            }

            if (dataTarget.getColumns() != null) {
                colSelector.getItems().setAll(dataTarget.getColumns());
                colSelector.getSelectionModel().select(col);
            } else {
                colSelector.getItems().clear();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean checkOptions() {
        try {
            if (!super.checkOptions()) {
                return false;
            }
            row = rowSelector.getSelectionModel().getSelectedIndex();
            col = colSelector.getSelectionModel().getSelectedIndex();
            if (row < 0 || row >= dataTarget.tableRowsNumber()
                    || col < 0 || col >= dataTarget.tableColsNumber()) {
                popError(message("InvalidParameters") + ": " + message("PasteLocation"));
                return false;
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public boolean doTask(FxTask currentTask) {
        data = selectedData(currentTask, true);
        return data != null && !data.isEmpty();
    }

    @Override
    public void afterSuccess() {
        try {
            int rowsNumber = dataTarget.tableRowsNumber();
            int colsNumber = dataTarget.tableColsNumber();
            targetController.isSettingValues = true;
            List<List<String>> rows = new ArrayList<>();
            rows.addAll(targetController.tableData);
            if (replaceRadio.isSelected()) {
                for (int r = row; r < Math.min(row + data.size(), rowsNumber);
                        r++) {
                    List<String> tableRow = targetController.data2D.pageRow(r, true);
                    List<String> dataRow = data.get(r - row);
                    for (int c = col;
                            c < Math.min(col + dataRow.size(), colsNumber); c++) {
                        tableRow.set(c + 1, dataRow.get(c - col));
                    }
                    rows.set(r, tableRow);
                }
            } else {
                List<List<String>> newRows = new ArrayList<>();
                for (int r = 0; r < data.size(); r++) {
                    List<String> newRow = targetController.data2D.newRow();
                    List<String> dataRow = data.get(r);
                    for (int c = col;
                            c < Math.min(col + dataRow.size(), colsNumber); c++) {
                        newRow.set(c + 1, dataRow.get(c - col));
                    }
                    newRows.add(newRow);
                }
                rows.addAll(insertRadio.isSelected() ? row : row + 1, newRows);
            }
            targetController.isSettingValues = false;
            targetController.updateTable(rows);
            targetController.tableChanged(true);
            targetController.popSuccessful();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void cleanPane() {
        try {
            if (targetController != null) {
                targetController.statusNotify.removeListener(targetStatusListener);
            }
            targetStatusListener = null;
            targetController = null;
            dataTarget = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
