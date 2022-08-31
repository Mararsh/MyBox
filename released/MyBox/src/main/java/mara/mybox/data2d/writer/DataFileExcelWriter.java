package mara.mybox.data2d.writer;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import mara.mybox.data2d.DataFileExcel;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileCopyTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.MicrosoftDocumentTools;
import mara.mybox.tools.TmpFileTools;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @Author Mara
 * @CreateDate 2022-1-27
 * @License Apache License Version 2.0
 */
public class DataFileExcelWriter extends Data2DWriter {

    protected DataFileExcel sourceExcel;
    protected String sheetName;
    protected Sheet sourceSheet, targetSheet;
    protected int targetRowIndex;
    protected Row sourceExcelRow;

    protected Iterator<Row> iterator;

    public DataFileExcelWriter(DataFileExcel data) {
        this.sourceExcel = data;
        sheetName = data.getSheet();
        init(data);
    }

    @Override
    public void scanData() {
        if (!FileTools.hasData(sourceFile)) {
            return;
        }
        File tmpFile = TmpFileTools.getTempFile();
        rowIndex = 0;
        count = 0;
        try ( Workbook sourceBook = WorkbookFactory.create(sourceFile)) {
            if (sheetName != null) {
                sourceSheet = sourceBook.getSheet(sheetName);
            } else {
                sourceSheet = sourceBook.getSheetAt(0);
                sheetName = sourceSheet.getSheetName();
            }
            if (sourceExcel != null) {
                int sheetsNumber = sourceBook.getNumberOfSheets();
                List<String> sheetNames = new ArrayList<>();
                for (int i = 0; i < sheetsNumber; i++) {
                    sheetNames.add(sourceBook.getSheetName(i));
                }
                sourceExcel.setSheetNames(sheetNames);
                sourceExcel.setSheet(sheetName);
            }

            File tmpDataFile = null;
            int sheetsNumber = sourceBook.getNumberOfSheets();
            Workbook targetBook;
            if (sheetsNumber == 1) {
                targetBook = new XSSFWorkbook();
                targetSheet = targetBook.createSheet(sheetName);
            } else {
                tmpDataFile = TmpFileTools.getTempFile();
                FileCopyTools.copyFile(sourceFile, tmpDataFile);
                targetBook = WorkbookFactory.create(tmpDataFile);
                int index = targetBook.getSheetIndex(sheetName);
                targetBook.removeSheetAt(index);
                targetSheet = targetBook.createSheet(sheetName);
                targetBook.setSheetOrder(sheetName, index);
            }
            targetRowIndex = 0;
            if (data2D.isHasHeader()) {
                targetRowIndex = sourceExcel.writeHeader(targetSheet, targetRowIndex);
            }
            iterator = sourceSheet.iterator();
            failed = !handleRows();
            sourceBook.close();
            if (failed) {
                FileDeleteTools.delete(tmpFile);
            } else {
                try ( FileOutputStream fileOut = new FileOutputStream(tmpFile)) {
                    targetBook.write(fileOut);
                }
                targetBook.close();
                failed = !FileTools.rename(tmpFile, sourceFile, false);
            }
            FileDeleteTools.delete(tmpDataFile);
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
            failed = true;
        }
        if (failed) {
            writerStopped = true;
        }
    }

    public boolean handleRows() {
        if (iterator == null) {
            return false;
        }
        try {
            if (data2D.isHasHeader()) {
                while (iterator.hasNext() && (iterator.next() == null) && !writerStopped()) {
                }
            }
            if (isClearData()) {
                count = data2D.getDataSize();
                return true;
            }
            while (iterator.hasNext() && !writerStopped()) {
                readRow();
                if (sourceRow == null || sourceRow.isEmpty()) {
                    continue;
                }
                ++rowIndex;
                handleRow();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
            return false;
        }
        return true;
    }

    public void readRow() {
        try {
            sourceRow = null;
            if (writerStopped() || iterator == null) {
                return;
            }
            sourceExcelRow = iterator.next();
            if (sourceExcelRow == null) {
                return;
            }
            sourceRow = new ArrayList<>();
            for (int c = sourceExcelRow.getFirstCellNum(); c < sourceExcelRow.getLastCellNum(); c++) {
                sourceRow.add(MicrosoftDocumentTools.cellString(sourceExcelRow.getCell(c)));
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
        }
    }

    @Override
    public void writeRow() {
        try {
            if (writerStopped() || sourceExcelRow == null || targetRow == null) {
                return;
            }
            Row targetExcelRow = targetSheet.createRow(targetRowIndex++);
            int i = 0;
            for (int c = sourceExcelRow.getFirstCellNum(); c < Math.min(sourceExcelRow.getLastCellNum(), targetRow.size()); c++) {
                Cell targetCell = targetExcelRow.createCell(c, CellType.STRING);
                targetCell.setCellValue(targetRow.get(i++));
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
        }
    }

}
