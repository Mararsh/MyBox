package mara.mybox.controller;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-12-26
 * @License Apache License Version 2.0
 */
public class SheetPasteController extends BaseController {

    protected BaseSheetController sheetController;

    @FXML
    protected ControlDataTextController dataController;

    public SheetPasteController() {
        baseTitle = message("DataClipboard");
    }

    public void setSheet(BaseSheetController sheetController) {
        try {
            this.sheetController = sheetController;
            baseName = sheetController.baseName;
            dataController.delimiter = this.sheetController.delimiter;
            String[][] sheet = sheetController.data();
            dataController.colsNumber = sheet.length;
            dataController.rowsNumber = sheet[0].length;
            dataController.inputArea.setText(sheetController.textArea.getText());
            dataController.setControls(baseName, sheetController instanceof BaseMatrixController);
            dataController.validateData();

            okButton.disableProperty().bind(Bindings.isEmpty(dataController.dataArea.textProperty()));

        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    @FXML
    @Override
    public void okAction() {
        if (sheetController == null || dataController.sheet == null) {
            return;
        }
        sheetController.delimiter = dataController.delimiter;
        sheetController.makeSheet(dataController.sheet);
        sheetController.toFront();
        closeStage();
    }

}
