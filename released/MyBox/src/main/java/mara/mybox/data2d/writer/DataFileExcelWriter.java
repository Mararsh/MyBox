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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @Author Mara
 * @CreateDate 2022-1-29
 * @License Apache License Version 2.0
 */
public class DataFileExcelWriter extends Data2DWriter {

    protected Workbook xssfBook;
    protected Sheet xssfSheet;
    protected String sheetName;
    protected boolean currentSheetOnly;
    protected File baseFile;
    protected int rowIndex;

    public DataFileExcelWriter() {
        fileSuffix = "xlsx";
        currentSheetOnly = false;
    }

    @Override
    public boolean openWriter() {
        try {
            if (!super.openWriter()) {
                return false;
            }
            if (printFile == null) {
                showInfo(message("InvalidParameter") + ": " + message("TargetFile"));
                return false;
            }
            showInfo(message("Writing") + " " + printFile.getAbsolutePath());
            tmpFile = FileTmpTools.getTempFile(".xlsx");
            if (sheetName == null) {
                sheetName = "sheet1";
            }
            if (!currentSheetOnly && baseFile != null && baseFile.exists()) {
                try (Workbook sourceBook = WorkbookFactory.create(baseFile)) {
                    int sheetsNumber = sourceBook.getNumberOfSheets();
                    if (sheetsNumber == 1) {
                        xssfBook = new XSSFWorkbook();
                        xssfSheet = xssfBook.createSheet(sheetName);
                    } else {
                        FileCopyTools.copyFile(baseFile, tmpFile);
                        xssfBook = WorkbookFactory.create(tmpFile);
                        int index = sourceBook.getSheetIndex(sheetName);
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
                xssfBook = new XSSFWorkbook();
                xssfSheet = xssfBook.createSheet(sheetName);
            }
            xssfSheet.setDefaultColumnWidth(20);
            rowIndex = 0;
            if (writeHeader && headerNames != null) {
                Row titleRow = xssfSheet.createRow(rowIndex++);
                CellStyle horizontalCenter = xssfBook.createCellStyle();
                horizontalCenter.setAlignment(HorizontalAlignment.CENTER);
                for (int i = 0; i < headerNames.size(); i++) {
                    Cell cell = titleRow.createCell(i, CellType.STRING);
                    cell.setCellValue(headerNames.get(i));
                    cell.setCellStyle(horizontalCenter);
                }
            }
            status = Status.Openned;
            return true;
        } catch (Exception e) {
            showError(e.toString());
            return false;
        }
    }

    @Override
    public void printTargetRow() {
        try {
            if (printRow == null) {
                return;
            }
            Row sheetRow = xssfSheet.createRow(rowIndex++);
            for (int i = 0; i < printRow.size(); i++) {
                Cell cell = sheetRow.createCell(i, CellType.STRING);
                cell.setCellValue(printRow.get(i));
            }
        } catch (Exception e) {
            showError(e.toString());
        }
    }

    @Override
    public void closeWriter() {
        try {
            if (xssfBook == null || xssfSheet == null || tmpFile == null) {
                showInfo(message("Failed") + ": " + printFile);
                status = Status.Failed;
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
            if (isFailed() || !tmpFile.exists()) {
                FileDeleteTools.delete(tmpFile);
                showInfo(message("Failed") + ": " + printFile);
                status = Status.Failed;
                return;
            }
            if (targetRowIndex == 0) {
                FileDeleteTools.delete(tmpFile);
                showInfo(message("NoData") + ": " + printFile);
                status = Status.NoData;
                return;
            }
            if (!FileTools.override(tmpFile, printFile, true)) {
                FileDeleteTools.delete(tmpFile);
                showInfo(message("Failed") + ": " + printFile);
                status = Status.Failed;
                return;
            }
            if (printFile == null || !printFile.exists()) {
                showInfo(message("Failed") + ": " + printFile);
                status = Status.Failed;
                return;
            }
            recordFileGenerated(printFile, VisitHistory.FileType.Excel);
            if (recordTargetData) {
                if (targetData == null) {
                    targetData = Data2D.create(Data2DDefinition.DataType.Excel);
                }
                targetData.setTask(task())
                        .setFile(printFile)
                        .setSheet(sheetName)
                        .setHasHeader(writeHeader)
                        .setDataName(dataName)
                        .setColsNumber(columns.size())
                        .setRowsNumber(targetRowIndex);
                Data2D.saveAttributes(conn(), targetData, columns);
            }
            status = Status.Created;
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

    public DataFileExcelWriter setCurrentSheetOnly(boolean currentSheetOnly) {
        this.currentSheetOnly = currentSheetOnly;
        return this;
    }

}
