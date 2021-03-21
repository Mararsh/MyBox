package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.ColumnDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.TextTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-12-26
 * @License Apache License Version 2.0
 */
public class DataTextClipboardController extends BaseController {

    protected BaseSheetController sheetController;
    protected String[][] sheet;
    protected String sourceDelimiter;
    protected boolean isMatrix;

    @FXML
    protected TextArea inputArea;
    @FXML
    protected ControlClipboard clipboardController;
    @FXML
    protected ToggleGroup delimiterSourceGroup;
    @FXML
    protected RadioButton blankSourceRadio, blank4SourceRadio, blank8SourceRadio, tabSourceRadio, commaSourceRadio,
            lineSourceRadio, atSourceRadio, sharpSourceRadio, semicolonsSourceRadio, stringSourceRadio;
    @FXML
    protected TextField delimiterSourceInput;
    @FXML
    protected HBox buttonBox;
    @FXML
    protected CheckBox withNamesCheck;

    public DataTextClipboardController() {
        baseTitle = message("DataTextClipboard");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

    public void setSheet(BaseSheetController sheetController) {
        try {
            this.sheetController = sheetController;
            isSettingValues = true;
            setSourceDelimiter(sheetController.textController.delimiter);
            sheet = sheetController.data();
            inputArea.setText(sheetController.textController.textArea.getText());
            isSettingValues = false;

            validateData();

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

    public void setControls(String baseName) {
        try {
            this.baseName = baseName;
            isMatrix = sheetController != null && (sheetController instanceof BaseMatrixController);
            if (isMatrix) {
                FxmlControl.setTooltip(tipsView, new Tooltip(message("MatrixInputComments")));
            } else {
                FxmlControl.setTooltip(tipsView, new Tooltip(message("DataInputComments")));
            }
            clipboardController.setControls(baseName);

            setSourceDelimiter(AppVariables.getUserConfigValue(baseName + "SourceDelimiter", "Blank"));
            delimiterSourceGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    delimiterSourceInput.setStyle(null);
                    if (stringSourceRadio.isSelected()) {
                        String v = delimiterSourceInput.getText();
                        if (v == null || v.isBlank()) {
                            delimiterSourceInput.setStyle(badStyle);
                            return;
                        }
                        sourceDelimiter = v;
                        delimiterSourceInput.setStyle(null);
                    } else if (blankSourceRadio.isSelected()) {
                        sourceDelimiter = "Blank";
                    } else if (blank4SourceRadio.isSelected()) {
                        sourceDelimiter = "Blank4";
                    } else if (blank8SourceRadio.isSelected()) {
                        sourceDelimiter = "Blank8";
                    } else if (tabSourceRadio.isSelected()) {
                        sourceDelimiter = "Tab";
                    } else if (commaSourceRadio.isSelected()) {
                        sourceDelimiter = ",";
                    } else if (lineSourceRadio.isSelected()) {
                        sourceDelimiter = "|";
                    } else if (atSourceRadio.isSelected()) {
                        sourceDelimiter = "@";
                    } else if (sharpSourceRadio.isSelected()) {
                        sourceDelimiter = "#";
                    } else if (semicolonsSourceRadio.isSelected()) {
                        sourceDelimiter = ";";
                    }
                    AppVariables.setUserConfigValue(baseName + "SourceDelimiter", sourceDelimiter);
                    validateData();
                }
            });
            delimiterSourceInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (isSettingValues || !stringSourceRadio.isSelected()) {
                        return;
                    }
                    if (newValue == null || newValue.isBlank()) {
                        delimiterSourceInput.setStyle(badStyle);
                        return;
                    }
                    sourceDelimiter = newValue;
                    AppVariables.setUserConfigValue(baseName + "SourceDelimiter", sourceDelimiter);
                    delimiterSourceInput.setStyle(null);
                    validateData();
                }
            });

            inputArea.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    validateData();
                }
            });
            cancelButton.setVisible(false);
            okButton.setVisible(false);
            buttonBox.getChildren().removeAll(cancelButton, okButton);

        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    public void setSourceDelimiter(String sourceDelimiter) {
        try {
            this.sourceDelimiter = sourceDelimiter;
            switch (sourceDelimiter.toLowerCase()) {
                case "blank":
                    blankSourceRadio.fire();
                    break;
                case "blank4":
                    blank4SourceRadio.fire();
                    break;
                case "blank8":
                    blank8SourceRadio.fire();
                    break;
                case "tab":
                    tabSourceRadio.fire();
                    break;
                case ",":
                    commaSourceRadio.fire();
                    break;
                case "|":
                    lineSourceRadio.fire();
                    break;
                case "@":
                    atSourceRadio.fire();
                    break;
                case "#":
                    sharpSourceRadio.fire();
                    break;
                case ";":
                    semicolonsSourceRadio.fire();
                    break;
                default:
                    stringSourceRadio.fire();
                    delimiterSourceInput.setText(sourceDelimiter);
            }
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();
        setControls(baseName);
    }

    protected void validateData() {
        try {
            if (isSettingValues) {
                return;
            }
            String s = inputArea.getText();
            String[] lines = s.split("\n");
            int rowsNumber = 0, colsNumber = 0;
            List<List<String>> data = new ArrayList<>();
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] values;
                switch (sourceDelimiter.toLowerCase()) {
                    case "tab":
                        values = line.split("\t");
                        break;
                    case "blank":
                        values = line.split("\\s+");
                        break;
                    case "blank4":
                        values = line.split("\\s{4}");
                        break;
                    case "blank8":
                        values = line.split("\\s{8}");
                        break;
                    case "|":
                        values = line.split("\\|");
                        break;
                    default:
                        values = line.split(sourceDelimiter);
                        break;
                }
                if (values == null || values.length == 0) {
                    continue;
                }
                List<String> row = new ArrayList<>();
                for (String value : values) {
                    if (isMatrix) {
                        value = value.replaceAll(CommonValues.DataIgnoreChars, "");
                        try {
                            double d = Double.parseDouble(value);
                            row.add(d + "");
                        } catch (Exception e) {
                        }
                    } else {
                        row.add(value);
                    }
                }
                int size = row.size();
                if (size == 0) {
                    continue;
                }
                if (size > colsNumber) {
                    colsNumber = size;
                }
                data.add(row);
            }
            rowsNumber = data.size();
            if (rowsNumber == 0 || colsNumber == 0) {
                clipboardController.update(null);
                popError(message("InvalidData"));
                return;
            }
            sheet = new String[rowsNumber][colsNumber];
            for (int i = 0; i < rowsNumber; i++) {
                List<String> row = data.get(i);
                for (int j = 0; j < row.size(); j++) {
                    sheet[i][j] = row.get(j);
                }
            }
            clipboardController.update(sheet);
            bottomLabel.setText(message("RowsNumber") + ": " + rowsNumber + "  " + message("ColumnsNumber") + ": " + colsNumber);

        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    @Override
    public void sourceFileChanged(File file) {
        if (!checkBeforeNextAction()) {
            return;
        }
        sourceFile = file;
        inputArea.setText(TextTools.readText(file));
    }

    @FXML
    @Override
    public void clearAction() {
        isSettingValues = true;
        inputArea.clear();
        sheet = null;
        clipboardController.update(null);
        isSettingValues = false;
    }

    @FXML
    @Override
    public void pasteAction() {
        inputArea.setText(FxmlControl.getSystemClipboardString());
    }

    @FXML
    public void csvAction() {
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
            controller.makeSheet(sheet, null);
        }
    }

    @FXML
    public void excelAction() {
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
        if (sheetController == null || sheet == null) {
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
