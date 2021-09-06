package mara.mybox.controller;

import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
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

    @Override
    public void setParameters(ControlSheet sheetController, int row, int col) {
        try {
            super.setParameters(sheetController, row, col);

            colsListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void okAction() {
        try {
            boolean asc = ascRadio.isSelected();

            List<Integer> cols = selectedCols();
            if (cols == null || cols.isEmpty()) {
                popError(message("InvalidParameters"));
                return;
            }
            int col = cols.get(0);
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
