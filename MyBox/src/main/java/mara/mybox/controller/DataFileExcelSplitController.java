package mara.mybox.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @Author Mara
 * @CreateDate 2021-2-1
 * @License Apache License Version 2.0
 */
public class DataFileExcelSplitController extends BaseBatchFileController {

    protected String filePrefix;
    protected List<String> headers;
    protected int maxLines, fileIndex, rowIndex, dataIndex;
    protected XSSFWorkbook xssfBook;
    protected XSSFSheet xssfSheet;
    protected String sheetName;

    @FXML
    protected CheckBox sourceWithNamesCheck, targetWithNamesCheck;
    @FXML
    protected ComboBox<String> maxLinesSelector;

    public DataFileExcelSplitController() {
        baseTitle = AppVariables.message("ExcelSplit");

        SourceFileType = VisitHistory.FileType.Excel;
        SourcePathType = VisitHistory.FileType.Excel;
        AddFileType = VisitHistory.FileType.Excel;
        AddPathType = VisitHistory.FileType.Excel;
        TargetPathType = VisitHistory.FileType.Excel;
        TargetFileType = VisitHistory.FileType.Excel;
        sourcePathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.Excel);
        targetPathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.Excel);
        sourceExtensionFilter = CommonFxValues.ExcelExtensionFilter;
        targetExtensionFilter = CommonFxValues.ExcelExtensionFilter;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(Bindings.isEmpty(tableView.getItems())
                    .or(Bindings.isEmpty(targetPathInput.textProperty()))
                    .or(maxLinesSelector.getEditor().styleProperty().isEqualTo(badStyle))
                    .or(targetPathInput.styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initOptionsSection() {
        try {

            maxLines = AppVariables.getUserConfigInt(baseName + "Lines", 1000);
            maxLines = maxLines < 1 ? 1000 : maxLines;
            maxLinesSelector.getItems().addAll(Arrays.asList(
                    "1000", "500", "200", "300", "800", "2000", "3000", "5000", "8000"
            ));
            maxLinesSelector.setValue(maxLines + "");
            maxLinesSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        try {
                            int v = Integer.valueOf(newValue);
                            if (v > 0) {
                                maxLines = v;
                                AppVariables.setUserConfigInt(baseName + "Lines", v);
                                FxmlControl.setEditorNormal(maxLinesSelector);
                            } else {
                                FxmlControl.setEditorBadStyle(maxLinesSelector);
                            }
                        } catch (Exception e) {
                            FxmlControl.setEditorBadStyle(maxLinesSelector);
                        }
                    });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    @Override
    public boolean makeMoreParameters() {
        filePrefix = null;
        targetExistType = TargetExistType.Replace;
        maxLines = maxLines < 1 ? 1000 : maxLines;
        return super.makeMoreParameters();
    }

    @Override
    public boolean matchType(File file) {
        String suffix = FileTools.getFileSuffix(file.getName());
        if (suffix == null) {
            return false;
        }
        suffix = suffix.trim().toLowerCase();
        return "xlsx".equals(suffix) || "xls".equals(suffix);
    }

    public String cellString(Cell cell) {
        if (cell == null) {
            return null;
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return cell.getNumericCellValue() + "";
            case BOOLEAN:
                return cell.getBooleanCellValue() + "";
        }
        return null;
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        countHandling(srcFile);
        filePrefix = FileTools.getFilePrefix(srcFile.getName());
        String result = null;
        try ( Workbook wb = WorkbookFactory.create(srcFile)) {
            List<String> rowData = new ArrayList<>();
            for (int s = 0; s < wb.getNumberOfSheets(); s++) {
                Sheet sheet = wb.getSheetAt(s);
                sheetName = sheet.getSheetName();
                fileIndex = rowIndex = dataIndex = 0;
                headers = null;
                xssfBook = null;
                updateLogs(message("Reading") + " " + message("Sheet") + ":" + sheetName);
                boolean write = true;
                for (Row row : sheet) {
                    if (row == null) {
                        continue;
                    }
                    for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
                        rowData.add(cellString(row.getCell(c)));
                    }
                    if (headers == null) {
                        headers = new ArrayList<>();
                        if (sourceWithNamesCheck.isSelected()) {
                            headers.addAll(rowData);
                            write = false;
                        } else {
                            for (int c = 1; c <= rowData.size(); c++) {
                                headers.add(message("Field") + c);
                            }
                        }
                    } else {
                        write = true;
                    }
                    result = checkTargetFile();
                    if (result != null) {
                        break;
                    }
                    if (write) {
                        XSSFRow sheetRow = xssfSheet.createRow(rowIndex++);
                        for (int c = 0; c < rowData.size(); c++) {
                            XSSFCell cell = sheetRow.createCell(c);
                            cell.setCellValue(rowData.get(c));
                        }
                        dataIndex++;
                    }
                    rowData.clear();
                }
                if (xssfBook != null) {
                    for (int i = 0; i < headers.size(); i++) {
                        xssfSheet.autoSizeColumn(i);
                    }
                    if (dataIndex < maxLines) {
                        int startRow = (fileIndex > 1 ? (fileIndex - 1) * maxLines : 0) + 1;
                        targetFile = makeTargetFile(filePrefix + "-" + sheetName + "_" + startRow + "-" + (startRow + dataIndex - 1), ".xlsx", targetPath);
                    }
                    try ( FileOutputStream fileOut = new FileOutputStream(targetFile)) {
                        xssfBook.write(fileOut);
                    }
                    xssfBook.close();
                    targetFileGenerated(targetFile);
                }
            }

            if (result == null) {
                result = message("Handled");
            }
        } catch (Exception e) {
            result = e.toString();
        }
        return result;
    }

    protected String checkTargetFile() {
        if (xssfBook != null && dataIndex < maxLines) {
            return null;
        }
        try {
            if (xssfBook != null) {
                for (int i = 0; i < headers.size(); i++) {
                    xssfSheet.autoSizeColumn(i);
                }
                try ( FileOutputStream fileOut = new FileOutputStream(targetFile)) {
                    xssfBook.write(fileOut);
                }
                xssfBook.close();
                targetFileGenerated(targetFile);
                rowIndex = dataIndex = 0;
            }
            int startRow = (fileIndex++) * maxLines + 1;
            targetFile = makeTargetFile(filePrefix + "-" + sheetName + "_" + startRow + "-" + (startRow + maxLines - 1), ".xlsx", targetPath);
            updateLogs(message("Writing") + " " + targetFile.getAbsolutePath());
            xssfBook = new XSSFWorkbook();
            xssfSheet = xssfBook.createSheet(sheetName);
            xssfSheet.setDefaultColumnWidth(20);
            if (headers != null && targetWithNamesCheck.isSelected()) {
                XSSFRow titleRow = xssfSheet.createRow(rowIndex++);
                XSSFCellStyle horizontalCenter = xssfBook.createCellStyle();
                horizontalCenter.setAlignment(HorizontalAlignment.CENTER);
                for (int i = 0; i < headers.size(); i++) {
                    XSSFCell cell = titleRow.createCell(i);
                    cell.setCellValue(headers.get(i));
                    cell.setCellStyle(horizontalCenter);
                }
            }
            return null;
        } catch (Exception e) {
            return e.toString();
        }
    }

}
