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
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-11-27
 * @License Apache License Version 2.0
 */
public class Data2DPasteContentInSystemClipboardController extends BaseChildController {

    protected ControlData2DLoad targetTableController;
    protected Data2D dataTarget;
    protected int row, col;
    protected ChangeListener<Boolean> targetStatusListener;

    @FXML
    protected ControlData2DInput inputController;
    @FXML
    protected HBox pasteBox, wayBox;
    @FXML
    protected ComboBox<String> rowSelector, colSelector;
    @FXML
    protected RadioButton replaceRadio, insertRadio, appendRadio;

    public Data2DPasteContentInSystemClipboardController() {
        baseTitle = message("PasteContentInSystemClipboard");
    }

    public void setParameters(ControlData2DLoad target, String text) {
        try {
            this.parentController = target;
            targetTableController = target;
            dataTarget = targetTableController.data2D;

            inputController.load(text);

            targetStatusListener = new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    makeControls(row, col);
                }
            };
            targetTableController.statusNotify.addListener(targetStatusListener);

            inputController.statusNotify.addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                        okButton.setDisable(!inputController.hasData());
                    });

            makeControls(0, 0);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void okAction() {
        try {
            if (inputController.data == null || inputController.data.isEmpty()) {
                popError(message("NoData"));
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
            targetTableController.isSettingValues = true;
            if (replaceRadio.isSelected()) {
                for (int r = row; r < Math.min(row + inputController.data.size(), rowsNumber); r++) {
                    List<String> tableRow = targetTableController.tableData.get(r);
                    List<String> dataRow = inputController.data.get(r - row);
                    for (int c = col; c < Math.min(col + dataRow.size(), colsNumber); c++) {
                        tableRow.set(c + 1, dataRow.get(c - col));
                    }
                    targetTableController.tableData.set(r, tableRow);
                }
            } else {
                List<List<String>> newRows = new ArrayList<>();
                for (int r = 0; r < inputController.data.size(); r++) {
                    List<String> newRow = targetTableController.data2D.newRow();
                    List<String> dataRow = inputController.data.get(r);
                    for (int c = col; c < Math.min(col + dataRow.size(), colsNumber); c++) {
                        newRow.set(c + 1, dataRow.get(c - col));
                    }
                    newRows.add(newRow);
                }
                targetTableController.tableData.addAll(insertRadio.isSelected() ? row : row + 1, newRows);
            }
            targetTableController.tableView.refresh();
            targetTableController.isSettingValues = false;
            targetTableController.tableChanged(true);
            popDone();

            makeControls(row, col);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void cleanPane() {
        try {
            targetTableController.statusNotify.removeListener(targetStatusListener);
            targetStatusListener = null;
            targetTableController = null;
            dataTarget = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

    /*
        static
     */
    public static Data2DPasteContentInSystemClipboardController open(ControlData2DLoad parent, String text) {
        try {
            Data2DPasteContentInSystemClipboardController controller = (Data2DPasteContentInSystemClipboardController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.Data2DPasteContentInSystemClipboardFxml, false);
            controller.setParameters(parent, text);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
