package mara.mybox.controller;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;
import mara.mybox.db.table.ColumnDefinition;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2021-3-11
 * @License Apache License Version 2.0
 */
public class DataSheetClipboardController extends BaseSheetController {

    protected BaseSheetController sheetController;

    @FXML
    protected HBox buttonBox;
    @FXML
    protected ControlClipboard clipboardController;
    @FXML
    protected CheckBox withNamesCheck;

    public DataSheetClipboardController() {
        baseTitle = message("DataSheetClipboard");
    }

    public void setSheet(BaseSheetController sheetController) {
        try {
            this.sheetController = sheetController;
            setControls(baseName);
            makeSheet(sheetController.data());

            okButton.disableProperty().bind(Bindings.isEmpty(clipboardController.textArea.textProperty()));
            cancelButton.setVisible(true);
            okButton.setVisible(true);
            buttonBox.getChildren().addAll(cancelButton, okButton);
            if ((sheetController instanceof BaseDataFileController)
                    && ((BaseDataFileController) sheetController).pagesNumber > 1) {
                bottomLabel.setText(message("CanNotChangeColumnsNumber"));
            }
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    @Override
    public void setControls(String baseName) {
        try {
            this.baseName = baseName;
            clipboardController.setControls(baseName);
            textController = clipboardController.textController;
            webView = clipboardController.webView;

            cancelButton.setVisible(false);
            okButton.setVisible(false);
            buttonBox.getChildren().removeAll(cancelButton, okButton);

            super.setControls(baseName);

        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();
        setControls(baseName);
        makeSheet(new String[3][3], false);
    }

    @FXML
    public void csvAction() {
        String[][] sheet = data();
        if (sheet == null || sheet.length < 1) {
            popError(message("NoData"));
            return;
        }
        if (withNamesCheck.isSelected()) {
            if (!ColumnDefinition.valid(sheet[0])) {
                popError(message("FirstLineAsNamesComments"));
                return;
            }
            DataFileCSVController controller = (DataFileCSVController) openStage(CommonValues.DataFileCSVFxml);
            controller.makeSheetWithName(sheet);
        } else {
            DataFileCSVController controller = (DataFileCSVController) openStage(CommonValues.DataFileCSVFxml);
            controller.makeSheet(data(), null);
        }
    }

    @FXML
    public void excelAction() {
        String[][] sheet = data();
        if (sheet == null || sheet.length < 1) {
            popError(message("NoData"));
            return;
        }
        if (withNamesCheck.isSelected()) {
            if (!ColumnDefinition.valid(sheet[0])) {
                popError(message("FirstLineAsNamesComments"));
                return;
            }
            DataFileExcelController controller = (DataFileExcelController) openStage(CommonValues.DataFileExcelFxml);
            controller.makeSheetWithName(sheet);
        } else {
            DataFileExcelController controller = (DataFileExcelController) openStage(CommonValues.DataFileExcelFxml);
            controller.makeSheet(sheet, null);
        }
    }

    @FXML
    @Override
    public void cancelAction() {
        closeStage();
    }

    @FXML
    @Override
    public void okAction() {
        if (sheetController == null) {
            return;
        }
        String[][] sheet = data();
        if (sheet == null) {
            return;
        }
        if (sheet[0].length != sheetController.colsCheck.length
                && (sheetController instanceof BaseDataFileController)) {
            if (((BaseDataFileController) sheetController).pagesNumber > 1) {
                popError(message("CanNotChangeColumnsNumber"));
                return;
            }
        }
        sheetController.makeSheet(sheet);
        sheetController.toFront();
        closeStage();
    }

}
