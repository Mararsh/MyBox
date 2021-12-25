package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.HBox;
import mara.mybox.data.Data2D;
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

    protected ControlData2DLoad loadController;
    protected Data2D data2D;

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

    public void setParameters(ControlData2DLoad parent, String text) {
        try {
            loadController = parent;
            data2D = loadController.data2D;

            inputController.load(text);

            makeControls(0, 0);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void makeControls(int row, int col) {
        try {
            List<String> rows = new ArrayList<>();
            for (long i = 0; i < loadController.tableData.size(); i++) {
                rows.add("" + (i + 1));
            }
            rowSelector.getItems().setAll(rows);
            rowSelector.getSelectionModel().select(row);

            colSelector.getItems().setAll(data2D.columnNames());
            colSelector.getSelectionModel().select(col);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void okAction() {
        if (inputController.data == null || inputController.data.isEmpty()) {
            popError(message("NoData"));
            return;
        }
        pasteData();
        popDone();
    }

    public void pasteData() {
        try {
            int row = rowSelector.getSelectionModel().getSelectedIndex();
            int col = colSelector.getSelectionModel().getSelectedIndex();
            int rowsNumber = data2D.tableRowsNumber();
            int colsNumber = data2D.tableColsNumber();
            if (row < 0 || row >= rowsNumber || col < 0 || col >= colsNumber) {
                popError(message("InvalidParameters"));
                return;
            }
            loadController.isSettingValues = true;
            if (replaceRadio.isSelected()) {
                for (int r = row; r < Math.min(row + inputController.data.size(), rowsNumber); r++) {
                    List<String> tableRow = loadController.tableData.get(r);
                    List<String> dataRow = inputController.data.get(r - row);
                    for (int c = col; c < Math.min(col + dataRow.size(), colsNumber); c++) {
                        tableRow.set(c + 1, dataRow.get(c - col));
                    }
                    loadController.tableData.set(r, tableRow);
                }
            } else {
                List<List<String>> newRows = new ArrayList<>();
                for (int r = 0; r < inputController.data.size(); r++) {
                    List<String> newRow = loadController.data2D.newRow();
                    List<String> dataRow = inputController.data.get(r);
                    for (int c = col; c < Math.min(col + dataRow.size(), colsNumber); c++) {
                        newRow.set(c + 1, dataRow.get(c - col));
                    }
                    newRows.add(newRow);
                }
                loadController.tableData.addAll(insertRadio.isSelected() ? row : row + 1, newRows);
            }
            loadController.tableView.refresh();
            loadController.isSettingValues = false;
            loadController.tableChanged(true);

            makeControls(row, col);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        static
     */
    public static Data2DPasteContentInSystemClipboardController open(ControlData2DLoad parent, String text) {
        try {
            Data2DPasteContentInSystemClipboardController controller = (Data2DPasteContentInSystemClipboardController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.Data2DPasteContentInSystemClipboardFxml, false);
            controller.setParameters(parent, text);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
