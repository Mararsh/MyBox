package mara.mybox.controller;

import java.util.List;
import javafx.fxml.FXML;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-9-2
 * @License Apache License Version 2.0
 */
public class SheetCopyToMyBoxClipboardController extends BaseDataOperationController {

    protected char delimiter;

    @FXML
    @Override
    public void okAction() {
        try {
            sheetController.copyDelimiter = delimiter;

            List<Integer> cols = cols();
            if (rowAllRadio.isSelected()) {
                sheetController.copyCols(cols, false, false);

            } else {
                sheetController.copyRowsCols(rows(), cols, false, false);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

}
