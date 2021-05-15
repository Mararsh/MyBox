package mara.mybox.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.ColumnDefinition.ColumnType;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @Author Mara
 * @CreateDate 2021-3-11
 * @License Apache License Version 2.0
 */
public class DataClipboardController extends BaseSheetController {

    protected BaseSheetController sheetController;
    protected String sourceDelimiter;
    protected boolean isMatrix;

    @FXML
    protected HBox buttonBox;
    @FXML
    protected ControlSheetData sheetDataController;
    @FXML
    protected TextArea inputArea;
    @FXML
    protected ToggleGroup delimiterSourceGroup;
    @FXML
    protected RadioButton blankSourceRadio, blank4SourceRadio, blank8SourceRadio, tabSourceRadio, commaSourceRadio,
            lineSourceRadio, atSourceRadio, sharpSourceRadio, semicolonsSourceRadio, stringSourceRadio;
    @FXML
    protected TextField delimiterSourceInput;

    public DataClipboardController() {
        baseTitle = message("DataClipboard");
        TipsLabelKey = "DataInputComments";
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

    @Override
    public void setControls(String baseName) {
        try {
            this.baseName = baseName;
            sheetDataController.setControls(baseName);
            textController = sheetDataController.textController;
            webView = sheetDataController.webView;
            isMatrix = false;

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
                    synchronizeChanged();
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
                    synchronizeChanged();
                }
            });

            inputArea.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    synchronizeChanged();
                }
            });

            buttonBox.getChildren().removeAll(cancelButton, okButton);

            super.setControls(baseName);

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

    public void setSheet(BaseSheetController sheetController) {
        try {
            this.sheetController = sheetController;
            sheet = sheetController.data();
            makeSheet(sheet);

            isMatrix = sheetController instanceof BaseMatrixController;
            if (isMatrix) {
                FxmlControl.setTooltip(tipsView, new Tooltip(message("MatrixInputComments")));
                defaultColumnType = ColumnType.String;
                defaultColNotNull = false;
            } else {
                FxmlControl.setTooltip(tipsView, new Tooltip(message("DataInputComments")));
            }

            buttonBox.getChildren().addAll(cancelButton, okButton);
            if ((sheetController instanceof BaseDataFileController)
                    && ((BaseDataFileController) sheetController).pagesNumber > 1) {
                bottomLabel.setText(message("CanNotChangeColumnsNumber"));
            }
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    public synchronized void synchronizeChanged() {
        try {
            if (isSettingValues) {
                return;
            }
            String s = inputArea.getText();
            String[] lines = s.split("\n");
            rowsNumber = colsNumber = 0;
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
                makeSheet(null);
                return;
            }
            sheet = new String[rowsNumber][colsNumber];
            for (int i = 0; i < rowsNumber; i++) {
                List<String> row = data.get(i);
                for (int j = 0; j < row.size(); j++) {
                    sheet[i][j] = row.get(j);
                }
            }
            makeSheet(sheet, false);
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    @Override
    public void sourceFileChanged(File file) {
        isSettingValues = true;
        inputArea.setText(TextTools.readText(file));
        isSettingValues = false;
        synchronizeChanged();
    }

    @Override
    protected void dataChanged(boolean dataChanged) {
        this.dataChanged = dataChanged;
        if (dataChanged) {
            isSettingValues = true;
            inputArea.setText(TextTools.dataText(sheet, sourceDelimiter));
            isSettingValues = false;
        }
        okButton.setDisable(inputArea.getText().isBlank());
        String info = message("RowsNumber") + ": " + rowsNumber + "  " + message("ColumnsNumber") + ": " + colsNumber;
        if (sheetController != null) {
            if (sheetController.colsNumber != colsNumber) {
                info += "    " + message("CanNotChangeColumnsNumber");
                okButton.setDisable(true);
            }
        }
        bottomLabel.setText(info);
    }

    @FXML
    @Override
    public void clearAction() {
        isSettingValues = true;
        inputArea.clear();
        isSettingValues = false;
        makeSheet(null);
    }

    @FXML
    @Override
    public void pasteAction() {
        inputArea.setText(FxmlControl.getSystemClipboardString());
    }

    @FXML
    public void csvAction() {
        sheet = data();
        if (sheet == null || sheet.length < 1) {
            popError(message("NoData"));
            return;
        }
        File tmpFile = FileTools.getTempFile(".csv");
        tmpFile = FileTools.writeFile(tmpFile, TextTools.dataText(sheet, ","));
        if (tmpFile == null || !tmpFile.exists()) {
            popFailed();
            return;
        }
        DataFileCSVController controller = (DataFileCSVController) openStage(CommonValues.DataFileCSVFxml);
        controller.setFile(tmpFile, false);
    }

    @FXML
    public void excelAction() {
        sheet = data();
        if (sheet == null || sheet.length < 1) {
            popError(message("NoData"));
            return;
        }
        File tmpFile = FileTools.getTempFile(".xlsx");
        try ( Workbook targetBook = new XSSFWorkbook();
                 FileOutputStream fileOut = new FileOutputStream(tmpFile)) {
            Sheet targetSheet = targetBook.createSheet();
            int index = 0;
            for (String[] row : sheet) {
                Row targetRow = targetSheet.createRow(index++);
                for (int col = 0; col < row.length; col++) {
                    Cell targetCell = targetRow.createCell(col, CellType.STRING);
                    targetCell.setCellValue(row[col]);
                }
            }
            targetBook.write(fileOut);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return;
        }
        if (!tmpFile.exists()) {
            popFailed();
            return;
        }
        DataFileExcelController controller = (DataFileExcelController) openStage(CommonValues.DataFileExcelFxml);
        controller.setFile(tmpFile, false);
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
        sheet = data();
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
        if (rowsNumber * colsNumber > 500) {
            if (!FxmlControl.askSure(sheetController.baseTitle, message("DataTooManyWhetherContinue"))) {
                return;
            }
        }
        sheetController.makeSheet(sheet);
        sheetController.toFront();
        closeStage();
    }

    @Override
    public boolean checkBeforeNextAction() {
        return true;
    }

}
