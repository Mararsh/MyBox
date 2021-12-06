package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
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
public class DataClipboardPopController extends DataClipboardController {

    protected ControlData2D sourceController;
    protected ControlData2DEditTable sourceTableController, targetTableController;
    protected Data2D dataSource, dataTarget;

    @FXML
    protected ComboBox<String> rowSelector, colSelector;

    public DataClipboardPopController() {
        TipsLabelKey = "SelectSomeOrNone";
    }

    public void setParameters(ControlData2DEditTable target) {
        try {
            this.parentController = target;
            targetTableController = target;
            dataTarget = target.data2D;

            sourceController = clipboardController.dataController;
            sourceTableController = sourceController.tableController;
            dataSource = sourceController.data2D;

            sourceController.statusNotify.addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                        okButton.setDisable(!dataSource.hasData());
                    });

            makeControls(0, 0);

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void makeControls(int row, int col) {
        try {
            List<String> rows = new ArrayList<>();
            for (long i = 0; i < dataTarget.tableRowsNumber(); i++) {
                rows.add("" + (i + 1));
            }
            rowSelector.getItems().setAll(rows);
            rowSelector.getSelectionModel().select(row);

            colSelector.getItems().setAll(dataTarget.columnNames());
            colSelector.getSelectionModel().select(col);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void okAction() {
        if (!dataSource.hasData()) {
            popError(message("NoData"));
            return;
        }
        try {
            int row = rowSelector.getSelectionModel().getSelectedIndex();
            int col = colSelector.getSelectionModel().getSelectedIndex();
            int targetRowsNumber = dataTarget.tableRowsNumber();
            int targetColsNumber = dataTarget.tableColsNumber();
            if (row < 0 || row >= targetRowsNumber || col < 0 || col >= targetColsNumber) {
                popError(message("InvalidParameters"));
                return;
            }
            List<List<String>> data = sourceTableController.tableView.getSelectionModel().getSelectedItems();
            if (data == null || data.isEmpty()) {
                data = sourceTableController.tableData;
            }
            targetTableController.isSettingValues = true;
            for (int r = row; r < Math.min(row + data.size(), targetRowsNumber); r++) {
                List<String> targetTableRow = targetTableController.tableData.get(r);
                List<String> sourceDataRow = data.get(r - row);
                for (int c = col; c < Math.min(col + sourceDataRow.size(), targetColsNumber); c++) {
                    targetTableRow.set(c + 1, sourceDataRow.get(c - col + 1));
                }
                targetTableController.tableData.set(r, targetTableRow);
            }
            targetTableController.isSettingValues = false;
            targetTableController.tableChanged(true);

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
                if (object != null && object instanceof DataClipboardPopController) {
                    ((DataClipboardPopController) object).close();
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static DataClipboardPopController open(ControlData2DEditTable target) {
        try {
            if (target == null) {
                return null;
            }
            closeAll();
            DataClipboardPopController controller
                    = (DataClipboardPopController) WindowTools.openChildStage(target.getMyStage(), Fxmls.DataClipboardPopFxml, false);
            controller.setParameters(target);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
