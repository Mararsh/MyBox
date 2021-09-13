package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-9-2
 * @License Apache License Version 2.0
 */
public class DataPasteController extends BaseDataOperationController {

    protected ControlSheetFile sourceController;
    protected ControlSheet targetController;

    @FXML
    protected VBox rowsBox, colsBox;
    @FXML
    protected CheckBox enlargeCheck;

    public void setParameters(ControlSheetFile sourceController, ControlSheet targetController) {
        try {
            super.setParameters(targetController, -1, -1);

            this.sourceController = sourceController;
            this.targetController = targetController;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void okAction() {
        try {
            boolean enlarge = enlargeCheck.isSelected();
            int row = rowSelector.getSelectionModel().getSelectedIndex();
            if (row < 0) {
                row = rowSelector.getItems().size() - 1;
            }
            int col = colSelector.getSelectionModel().getSelectedIndex();
            if (col < 0) {
                col = colSelector.getItems().size() - 1;
            }

            targetController.paste(sourceController.pageData, row, col, enlarge);

//            List<Integer> cols = cols();
//            if (rowAllRadio.isSelected()) {
//                sheetController.copyCols(cols, withNames, toSystemClipboard);
//
//            } else {
//                sheetController.copyRowsCols(rows(), cols, withNames, toSystemClipboard);
//            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

}
