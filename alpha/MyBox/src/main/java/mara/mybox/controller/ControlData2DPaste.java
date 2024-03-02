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
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-11-27
 * @License Apache License Version 2.0
 */
public class ControlData2DPaste extends BaseController {

    protected BaseData2DSelectRowsController sourceController;
    protected Data2DManufactureController targetController;
    protected Data2D dataTarget;
    protected int row, col;
    protected ChangeListener<Boolean> targetStatusListener;

    @FXML
    protected HBox pasteBox, wayBox;
    @FXML
    protected ComboBox<String> rowSelector, colSelector;
    @FXML
    protected RadioButton replaceRadio, insertRadio, appendRadio;

    public ControlData2DPaste() {
        baseTitle = message("PasteContentInSystemClipboard");
    }

    public void setParameters(BaseData2DSelectRowsController source, Data2DManufactureController target) {
        try {
            sourceController = source;
            targetController = target;
            dataTarget = targetController.data2D;

            targetStatusListener = new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    makeControls(row, col);
                }
            };
            targetController.statusNotify.addListener(targetStatusListener);

            makeControls(0, 0);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void makeControls(int row, int col) {
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

            List<String> names = dataTarget.columnNames();
            if (names != null && !names.isEmpty()) {
                colSelector.getItems().setAll(names);
                colSelector.getSelectionModel().select(col);
            } else {
                colSelector.getItems().clear();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void okAction() {
        if (!sourceController.hasData()) {
            popError(message("NoData"));
            return;
        }
        if (!sourceController.checkSelections()) {
            popError(message("SelectToHanlde"));
            return;
        }
        row = rowSelector.getSelectionModel().getSelectedIndex();
        col = colSelector.getSelectionModel().getSelectedIndex();
        int rowsNumber = dataTarget.tableRowsNumber();
        int colsNumber = dataTarget.tableColsNumber();
        if (row < 0 || row >= rowsNumber || col < 0 || col >= colsNumber) {
            popError(message("InvalidParameters"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            List<List<String>> data;

            @Override
            protected boolean handle() {
                data = sourceController.selectedData(this);
                return data != null && !data.isEmpty();
            }

            @Override
            protected void whenSucceeded() {
                try {
                    targetController.isSettingValues = true;
                    if (replaceRadio.isSelected()) {
                        for (int r = row; r < Math.min(row + data.size(), rowsNumber); r++) {
                            List<String> tableRow = targetController.tableData.get(r);
                            List<String> dataRow = data.get(r - row);
                            for (int c = col; c < Math.min(col + dataRow.size(), colsNumber); c++) {
                                tableRow.set(c + 1, dataRow.get(c - col));
                            }
                            targetController.tableData.set(r, tableRow);
                        }
                    } else {
                        List<List<String>> newRows = new ArrayList<>();
                        for (int r = 0; r < data.size(); r++) {
                            List<String> newRow = targetController.data2D.newRow();
                            List<String> dataRow = data.get(r);
                            for (int c = col; c < Math.min(col + dataRow.size(), colsNumber); c++) {
                                newRow.set(c + 1, dataRow.get(c - col));
                            }
                            newRows.add(newRow);
                        }
                        targetController.tableData.addAll(insertRadio.isSelected() ? row : row + 1, newRows);
                    }
                    targetController.tableView.refresh();
                    targetController.isSettingValues = false;
                    targetController.tableChanged(true);
                    targetController.pageChanged();
                } catch (Exception e) {
                    MyBoxLog.error(e);
                }
            }

        };

        start(task);
    }

    @Override
    public void cleanPane() {
        try {
            targetController.statusNotify.removeListener(targetStatusListener);
            targetStatusListener = null;
            targetController = null;
            dataTarget = null;
            sourceController = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
