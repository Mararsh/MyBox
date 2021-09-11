package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-9-5
 * @License Apache License Version 2.0
 */
public class DataSortController extends BaseDataOperationController {

    @FXML
    protected RadioButton ascRadio;

    @FXML
    @Override
    public void okAction() {
        try {
            int col = colSelector.getSelectionModel().getSelectedIndex();
            if (col < 0) {
                popError(message("NoSelection"));
                return;
            }
            boolean asc = ascRadio.isSelected();
            if (rowAllRadio.isSelected()) {
                sheetController.sort(col, asc);

            } else {
                sheetController.sortRows(rows(), col, asc);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
