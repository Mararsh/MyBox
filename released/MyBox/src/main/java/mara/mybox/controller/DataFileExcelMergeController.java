package mara.mybox.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.DataDefinition;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.TableDataDefinition;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.MicrosoftDocumentTools;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @Author Mara
 * @CreateDate 2021-2-13
 * @License Apache License Version 2.0
 */
public class DataFileExcelMergeController extends FilesMergeController {

    protected XSSFWorkbook targetBook;
    protected Map<String, Integer> sheetsIndex;

    @FXML
    protected CheckBox sourceWithNamesCheck, targetWithNamesCheck;

    public DataFileExcelMergeController() {
        baseTitle = Languages.message("ExcelMerge");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Excel);
    }

    @Override
    public boolean matchType(File file) {
        String suffix = FileNameTools.getFileSuffix(file.getName());
        if (suffix == null) {
            return false;
        }
        suffix = suffix.trim().toLowerCase();
        return "xlsx".equals(suffix) || "xls".equals(suffix);
    }

    @Override
    protected boolean openWriter() {
        try {
            targetBook = new XSSFWorkbook();
            sheetsIndex = new HashMap<>();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String handleFile(File srcFile) {
        String result;
        try ( Workbook sourceBook = WorkbookFactory.create(srcFile)) {
            List<String> rowData = new ArrayList<>();
            for (int s = 0; s < sourceBook.getNumberOfSheets(); s++) {
                if (task == null || task.isCancelled()) {
                    return message("Cancelled");
                }
                Sheet sourceSheet = sourceBook.getSheetAt(s);
                String sheetName = sourceSheet.getSheetName();
                updateLogs(Languages.message("Reading") + " " + Languages.message("Sheet") + ":" + sheetName);
                Sheet targetSheet = targetBook.getSheet(sheetName);
                if (targetSheet == null) {
                    targetSheet = targetBook.createSheet(sheetName);
                }
                int sourceIndex = 0, targetIndex = 0;
                if (sheetsIndex.containsKey(sheetName)) {
                    targetIndex = sheetsIndex.get(sheetName);
                }
                for (Row sourceRow : sourceSheet) {
                    if (task == null || task.isCancelled()) {
                        return message("Cancelled");
                    }
                    if (sourceRow == null) {
                        continue;
                    }
                    for (int c = sourceRow.getFirstCellNum(); c < sourceRow.getLastCellNum(); c++) {
                        rowData.add(MicrosoftDocumentTools.cellString(sourceRow.getCell(c)));
                    }
                    if (targetIndex == 0 && targetWithNamesCheck.isSelected()) {
                        Row targetRow = targetSheet.createRow(targetIndex++);
                        for (int col = 0; col < rowData.size(); col++) {
                            Cell targetCell = targetRow.createCell(col, CellType.STRING);
                            if (sourceWithNamesCheck.isSelected()) {
                                targetCell.setCellValue(rowData.get(col));
                            } else {
                                targetCell.setCellValue(Languages.message("Field") + col);
                            }
                        }
                    }
                    if (sourceIndex > 0 || !sourceWithNamesCheck.isSelected()) {
                        Row targetRow = targetSheet.createRow(targetIndex++);
                        for (int col = 0; col < rowData.size(); col++) {
                            Cell targetCell = targetRow.createCell(col, CellType.STRING);
                            targetCell.setCellValue(rowData.get(col));
                        }
                    }
                    sourceIndex++;
                    rowData.clear();
                }
                sheetsIndex.put(sheetName, targetIndex);
            }
            result = Languages.message("Handled");
        } catch (Exception e) {
            result = e.toString();
        }
        return result;
    }

    @Override
    protected boolean closeWriter() {
        try {
            try ( FileOutputStream fileOut = new FileOutputStream(targetFile)) {
                targetBook.write(fileOut);
            }
            targetBook.close();
            try ( Connection conn = DerbyBase.getConnection()) {
                TableDataDefinition tableDataDefinition = new TableDataDefinition();
                tableDataDefinition.clear(conn, DataDefinition.DataType.DataFile, targetFile.getAbsolutePath());
                conn.commit();
                DataDefinition dataDefinition = DataDefinition.create()
                        .setDataName(targetFile.getAbsolutePath())
                        .setDataType(DataDefinition.DataType.DataFile)
                        .setHasHeader(targetWithNamesCheck.isSelected());
                tableDataDefinition.insertData(conn, dataDefinition);
                conn.commit();
            } catch (Exception e) {
                updateLogs(e.toString(), true, true);
                return false;
            }
            return true;
        } catch (Exception e) {
            updateLogs(e.toString(), true, true);
            return false;
        }
    }

}
