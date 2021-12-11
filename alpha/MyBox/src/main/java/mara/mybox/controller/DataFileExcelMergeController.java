package mara.mybox.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.MicrosoftDocumentTools;
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
        baseTitle = message("ExcelMerge");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Excel);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(Bindings.isEmpty(tableData)
                    .or(targetFileController.valid.not())
            );

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
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
                updateLogs(message("Reading") + " " + message("Sheet") + ":" + sheetName);
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
                                targetCell.setCellValue(message("Column") + col);
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
            result = message("Handled");
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
            if (sheetsIndex.isEmpty()) {
                return true;
            }
            try ( Connection conn = DerbyBase.getConnection()) {
                TableData2DDefinition tableData2DDefinition = new TableData2DDefinition();
                for (String sheet : sheetsIndex.keySet()) {
                    Data2DDefinition def = tableData2DDefinition.queryFileSheet(conn, Data2DDefinition.Type.Excel, targetFile, sheet);
                    if (def == null) {
                        def = Data2DDefinition.create();
                    }
                    def.setType(Data2DDefinition.Type.Excel)
                            .setFile(targetFile)
                            .setDelimiter(sheet)
                            .setDataName(targetFile.getName())
                            .setHasHeader(targetWithNamesCheck.isSelected());
                    if (def.getD2did() < 0) {
                        tableData2DDefinition.insertData(conn, def);
                    } else {
                        tableData2DDefinition.updateData(conn, def);
                    }
                    conn.commit();
                }
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
