package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.stage.Window;
import mara.mybox.data.Data2D;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-9-13
 * @License Apache License Version 2.0
 */
public class Data2DPasteContentInMyBoxClipboardController extends DataInMyBoxClipboardController {

    protected ControlData2DSource sourceController;
    protected ControlData2DLoad targetTableController;
    protected Data2D dataTarget;
    protected int row, col;

    @FXML
    protected ComboBox<String> rowSelector, colSelector;
    @FXML
    protected RadioButton replaceRadio, insertRadio, appendRadio;

    @Override
    public void setStageStatus() {
        setAsPop(baseName);
    }

    public void setParameters(ControlData2DLoad target) {
        try {
            sourceController = (ControlData2DSource) loadController;
            sourceController.showAllPages(false);

            this.parentController = target;
            targetTableController = target;
            dataTarget = target.data2D;

            targetTableController.statusNotify.addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                        makeControls(row, col);
                    });

            sourceController.loadedNotify.addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                        okButton.setDisable(!loadController.data2D.hasData());
                    });
            okButton.setDisable(true);

            makeControls(0, 0);

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
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
            if (!loadController.data2D.hasData()) {
                popError(message("NoData"));
                return;
            }
            row = rowSelector.getSelectionModel().getSelectedIndex();
            col = colSelector.getSelectionModel().getSelectedIndex();
            int targetRowsNumber = dataTarget.tableRowsNumber();
            int targetColsNumber = dataTarget.tableColsNumber();
            if (row < 0 || row >= targetRowsNumber || col < 0 || col >= targetColsNumber) {
                popError(message("InvalidParameters"));
                return;
            }
            List<List<String>> data = sourceController.selectedData(false);
            if (data == null || data.isEmpty()) {
                popError(message("SelectToHandle"));
                return;
            }
            targetTableController.isSettingValues = true;
            if (replaceRadio.isSelected()) {
                for (int r = row; r < Math.min(row + data.size(), targetRowsNumber); r++) {
                    List<String> tableRow = targetTableController.tableData.get(r);
                    List<String> dataRow = data.get(r - row);
                    for (int c = col; c < Math.min(col + dataRow.size(), targetColsNumber); c++) {
                        tableRow.set(c + 1, dataRow.get(c - col));
                    }
                    targetTableController.tableData.set(r, tableRow);
                }
            } else {
                List<List<String>> newRows = new ArrayList<>();
                for (int r = 0; r < data.size(); r++) {
                    List<String> newRow = targetTableController.data2D.newRow();
                    List<String> dataRow = data.get(r);
                    for (int c = col; c < Math.min(col + dataRow.size(), targetColsNumber); c++) {
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

    @FXML
    @Override
    public void cancelAction() {
        close();
    }

    @Override
    public boolean keyESC() {
        close();
        return false;
    }

    @Override
    public boolean keyF6() {
        close();
        return false;
    }


    /*
        static methods
     */
    public static void closeAll() {
        try {
            List<Window> windows = new ArrayList<>();
            windows.addAll(Window.getWindows());
            for (Window window : windows) {
                Object object = window.getUserData();
                if (object != null && object instanceof Data2DPasteContentInMyBoxClipboardController) {
                    ((Data2DPasteContentInMyBoxClipboardController) object).close();
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static Data2DPasteContentInMyBoxClipboardController open(ControlData2DLoad target) {
        try {
            if (target == null) {
                return null;
            }
            closeAll();
            Data2DPasteContentInMyBoxClipboardController controller
                    = (Data2DPasteContentInMyBoxClipboardController) WindowTools.openChildStage(target.getMyStage(),
                            Fxmls.Data2DPasteContentInMyBoxClipboardFxml, false);
            controller.setParameters(target);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
