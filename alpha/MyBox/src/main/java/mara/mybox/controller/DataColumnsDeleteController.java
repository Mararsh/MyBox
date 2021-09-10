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
public class DataColumnsDeleteController extends BaseDataOperationController {

    @FXML
    @Override
    public void okAction() {
        try {
            if (!PopTools.askSure(message("Delete"), message("SureDelete"))) {
                return;
            }
            if (colCheckedRadio.isSelected()) {
                sheetController.deleteCols(sheetController.colsIndex(false));

            } else if (colAllRadio.isSelected()) {
                sheetController.deleteAllCols();

            } else if (colSelectRadio.isSelected()) {
                sheetController.deleteCols(selectedCols());
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
