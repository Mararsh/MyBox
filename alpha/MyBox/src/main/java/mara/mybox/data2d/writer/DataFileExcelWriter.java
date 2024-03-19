package mara.mybox.data2d.writer;

import java.io.File;
import java.io.FileOutputStream;
import mara.mybox.data2d.Data2D;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.tools.FileCopyTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.FileTools;
import static mara.mybox.value.Languages.message;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
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
 * @CreateDate 2022-1-29
 * @License Apache License Version 2.0
 */
public class DataFileExcelWriter extends Data2DWriter {

    protected XSSFWorkbook xssfBook;
    protected XSSFSheet xssfSheet;
    protected String sheetName;
    protected File baseFile;
    protected int rowIndex;

    public DataFileExcelWriter() {
        fileSuffix = "xlsx";
        sheetName = "sheet1";
    }

    @Override
    public boolean openWriter() {
        try {
            if (!super.openWriter()) {
                return false;
            }
            if (targetFile == null) {
                showInfo(message("InvalidParameter") + ": " + message("TargetFile"));
                return false;
            }
            showInfo(message("Writing") + " " + targetFile.getAbsolutePath());
            tmpFile = FileTmpTools.getTempFile(".xlsx");
            rowIndex = 0;
            if (baseFile != null && baseFile.exists()) {
                try (Workbook sourceBook = WorkbookFactory.create(baseFile)) {
                    Sheet sourceSheet = sourceBook.getSheetAt(0);
                    sheetName = sourceSheet.getSheetName();
                    int sheetsNumber = sourceBook.getNumberOfSheets();
                    if (sheetsNumber == 1) {
                        xssfBook = new XSSFWorkbook(tmpFile);
                        xssfSheet = xssfBook.createSheet(sheetName);
                    } else {
                        FileCopyTools.copyFile(baseFile, tmpFile);
                        xssfBook = new XSSFWorkbook(tmpFile);
                        int index = xssfBook.getSheetIndex(sheetName);
                        if (index >= 0) {
                            xssfBook.removeSheetAt(index);
                            xssfSheet = xssfBook.createSheet(sheetName);
                            xssfBook.setSheetOrder(sheetName, index);
                        } else {
                            xssfSheet = xssfBook.createSheet(sheetName);
                        }
                    }

                } catch (Exception e) {
                    showError(e.toString());
                    return false;
                }
            } else {
                xssfBook = new XSSFWorkbook(tmpFile);
                xssfSheet = xssfBook.createSheet(sheetName);
            }
            xssfSheet.setDefaultColumnWidth(20);
            if (writeHeader) {
                XSSFRow titleRow = xssfSheet.createRow(rowIndex++);
                XSSFCellStyle horizontalCenter = xssfBook.createCellStyle();
                horizontalCenter.setAlignment(HorizontalAlignment.CENTER);
                for (int i = 0; i < headerNames.size(); i++) {
                    XSSFCell cell = titleRow.createCell(i);
                    cell.setCellValue(headerNames.get(i));
                    cell.setCellStyle(horizontalCenter);
                }
            }
            return true;
        } catch (Exception e) {
            showError(e.toString());
            return false;
        }
    }

    @Override
    public void printTargetRow() {
        try {
            if (targetRow == null) {
                return;
            }
            XSSFRow sheetRow = xssfSheet.createRow(rowIndex++);
            for (int i = 0; i < targetRow.size(); i++) {
                XSSFCell cell = sheetRow.createCell(i);
                cell.setCellValue(targetRow.get(i));
            }
        } catch (Exception e) {
            showError(e.toString());
        }
    }

    @Override
    public void closeWriter() {
        try {
            created = false;
            if (xssfBook == null || xssfSheet == null || tmpFile == null) {
                return;
            }
            if (isFailed() || !tmpFile.exists()) {
                xssfBook.close();
                FileDeleteTools.delete(tmpFile);
                return;
            }
            for (int i = 0; i < headerNames.size(); i++) {
                xssfSheet.autoSizeColumn(i);
            }
            try (FileOutputStream fileOut = new FileOutputStream(tmpFile)) {
                xssfBook.write(fileOut);
            }
            xssfBook.close();
            xssfBook = null;
            if (!FileTools.override(tmpFile, targetFile)) {
                FileDeleteTools.delete(tmpFile);
                return;
            }
            if (targetFile == null || !targetFile.exists()) {
                return;
            }
            recordFileGenerated(targetFile, VisitHistory.FileType.Excel);
            if (recordTargetData) {
                if (targetData == null) {
                    targetData = Data2D.create(Data2DDefinition.DataType.Excel);
                }
                targetData.setTask(task()).setFile(targetFile)
                        .setSheet(sheetName)
                        .setHasHeader(writeHeader)
                        .setDataName(dataName)
                        .setColsNumber(columns.size())
                        .setRowsNumber(targetRowIndex);
                Data2D.saveAttributes(conn(), targetData, columns);
            }
            created = true;
        } catch (Exception e) {
            showError(e.toString());
        }
    }

    /*
        get/set
     */
    public File getBaseFile() {
        return baseFile;
    }

    public DataFileExcelWriter setBaseFile(File baseFile) {
        this.baseFile = baseFile;
        return this;
    }

    public String getSheetName() {
        return sheetName;
    }

    public DataFileExcelWriter setSheetName(String sheetName) {
        this.sheetName = sheetName;
        return this;
    }

}
