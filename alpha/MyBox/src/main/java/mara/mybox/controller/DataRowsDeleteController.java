package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-9-4
 * @License Apache License Version 2.0
 */
public class DataRowsDeleteController extends BaseDataOperationController {

    @FXML
    @Override
    public void okAction() {
        try {
            if (!PopTools.askSure(message("Delete"), message("SureDelete"))) {
                return;
            }
            if (rowCheckedRadio.isSelected()) {
                sheetController.deleteRows(sheetController.rowsIndex(false));

            } else if (rowCurrentPageRadio.isSelected()) {
                sheetController.deletePageRows();

            } else if (rowAllRadio.isSelected()) {
                sheetController.deleteAllRows();

            } else if (rowSelectRadio.isSelected()) {
                sheetController.deleteRows(selectedRows());
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
