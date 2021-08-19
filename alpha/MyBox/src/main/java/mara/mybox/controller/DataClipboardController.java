package mara.mybox.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import static mara.mybox.fxml.NodeStyleTools.badStyle;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.TextTools;
import mara.mybox.tools.TmpFileTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
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

    protected BaseSheetController sourceController;
    protected String sourceDelimiter;
    protected boolean isMatrix;

    @FXML
    protected HBox buttonBox;
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
        TipsLabelKey = "DataClipboardTips";
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            isMatrix = false;

            setSourceDelimiter(UserConfig.getString(baseName + "SourceDelimiter", "Blank"));
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
                            delimiterSourceInput.setStyle(NodeStyleTools.badStyle);
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
                    UserConfig.setString(baseName + "SourceDelimiter", sourceDelimiter);
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
                        delimiterSourceInput.setStyle(NodeStyleTools.badStyle);
                        return;
                    }
                    sourceDelimiter = newValue;
                    UserConfig.setString(baseName + "SourceDelimiter", sourceDelimiter);
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

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            if (isMatrix) {
                NodeStyleTools.setTooltip(tipsView, new Tooltip(message("MatrixInputComments")));
            } else {
                NodeStyleTools.setTooltip(tipsView, new Tooltip(message("DataInputComments")));
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void setSourceController(BaseSheetController sourceController) {
        try {
            this.sourceController = sourceController;
            this.sourceFile = sourceController.sourceFile;
            makeSheet(sourceController.sheet, sourceController.columns);
//            sheetDisplayController.defBottunsBox.setDisable(true);

            isMatrix = sourceController instanceof ControlMatrix;
            setControlsStyle();

            buttonBox.getChildren().addAll(cancelButton, okButton);
            if ((sourceController instanceof BaseDataFileController)
                    && ((BaseDataFileController) sourceController).pagesNumber > 1) {
                bottomLabel.setText(message("CanNotChangeColumnsNumber"));
            }
            NodeStyleTools.refreshStyle(buttonBox);
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
                        value = value.replaceAll(AppValues.DataIgnoreChars, "");
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
        try {
            this.dataChanged = dataChanged;
            if (dataChanged) {
                isSettingValues = true;
                inputArea.setText(TextTools.dataText(sheet, sourceDelimiter));
                isSettingValues = false;
            }
            okButton.setDisable(dataInvalid || inputArea.getText().isBlank());
            String info = message("RowsNumber") + ": " + rowsNumber + "  " + message("ColumnsNumber") + ": " + colsNumber;
            if (sourceController != null && sourceController.colsNumber != colsNumber) {
                info += "    " + message("CanNotChangeColumnsNumber");
                popError(message("CanNotChangeColumnsNumber"));
                okButton.setDisable(true);
                bottomLabel.setStyle(badStyle);
            } else {
                bottomLabel.setStyle(null);
            }
            bottomLabel.setText(info);
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    @Override
    protected void dataIsInvalid() {
        tabPane.getSelectionModel().select(sheetTab);
    }

    @Override
    public List<MenuItem> colModifyDefMenu(int col) {
        if (sourceController != null) {
            return null;
        } else {
            return super.colModifyDefMenu(col);
        }
    }

    @Override
    public List<MenuItem> makeSheetDeleteColsMenu() {
        if (sourceController != null) {
            return null;
        } else {
            return super.makeSheetDeleteColsMenu();
        }
    }

    @FXML
    @Override
    public boolean popAction() {
        if (inputArea.isFocused()) {
            TextPopController.openInput(this, inputArea);
            return true;
        }
        if (sheetDisplayController.popAction()) {
            return true;
        }
        if (tabPane.getSelectionModel().getSelectedItem() == textsTab) {
            TextPopController.openInput(this, inputArea);
            return true;
        }
        return false;
    }

    @FXML
    @Override
    public boolean menuAction() {
        if (inputArea.isFocused()) {
            Point2D localToScreen = inputArea.localToScreen(inputArea.getWidth() - 80, 80);
            MenuTextEditController.open(this, inputArea, localToScreen.getX(), localToScreen.getY());
            return true;
        }
        if (sheetDisplayController.menuAction()) {
            return true;
        }
        if (tabPane.getSelectionModel().getSelectedItem() == textsTab) {
            Point2D localToScreen = inputArea.localToScreen(inputArea.getWidth() - 80, 80);
            MenuTextEditController.open(this, inputArea, localToScreen.getX(), localToScreen.getY());
            return true;
        }
        return false;
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
        inputArea.setText(TextClipboardTools.getSystemClipboardString());
    }

    @FXML
    public void csvAction() {
        sheet = pickData();
        if (sheet == null || sheet.length < 1) {
            popError(message("NoData"));
            return;
        }
        File tmpFile = TmpFileTools.getTempFile(".csv");
        tmpFile = TextFileTools.writeFile(tmpFile, TextTools.dataText(sheet, ","));
        if (tmpFile == null || !tmpFile.exists()) {
            popFailed();
            return;
        }
        DataFileCSVController controller = (DataFileCSVController) openStage(Fxmls.DataFileCSVFxml);
        controller.setFile(tmpFile, false);
    }

    @FXML
    public void excelAction() {
        sheet = pickData();
        if (sheet == null || sheet.length < 1) {
            popError(message("NoData"));
            return;
        }
        File tmpFile = TmpFileTools.getTempFile(".xlsx");
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
        DataFileExcelController controller = (DataFileExcelController) openStage(Fxmls.DataFileExcelFxml);
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
        if (sourceController == null) {
            return;
        }
        sheet = pickData();
        if (sheet == null || sheet.length < 1) {
            popError(message("NoData"));
            return;
        }
        if (sheet[0].length != sourceController.colsCheck.length
                && (sourceController instanceof BaseDataFileController)) {
            if (((BaseDataFileController) sourceController).pagesNumber > 1) {
                popError(message("CanNotChangeColumnsNumber"));
                return;
            }
        }
        if (rowsNumber * colsNumber > 500) {
            if (!PopTools.askSure(sourceController.baseTitle, message("DataTooManyWhetherContinue"))) {
                return;
            }
        }
        sourceController.makeSheet(sheet, columns);
        sourceController.toFront();
        closeStage();
    }

    @Override
    public boolean checkBeforeNextAction() {
        return true;
    }

    @Override
    public void cleanPane() {
        try {
            sourceController = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
