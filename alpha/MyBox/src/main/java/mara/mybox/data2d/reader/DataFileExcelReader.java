package mara.mybox.data2d.reader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import mara.mybox.data2d.DataFileExcel;
import mara.mybox.tools.MicrosoftDocumentTools;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * @Author Mara
 * @CreateDate 2022-1-27
 * @License Apache License Version 2.0
 */
public class DataFileExcelReader extends Data2DReader {

    protected DataFileExcel readerExcel;
    protected Iterator<Row> iterator;

    public DataFileExcelReader(DataFileExcel data) {
        this.readerExcel = data;
        sourceData = data;
    }

    @Override
    public void scanData() {
        try (Workbook wb = WorkbookFactory.create(sourceFile)) {
            Sheet sourceSheet;
            String sheetName = sourceData.getSheet();
            if (sheetName != null) {
                sourceSheet = wb.getSheet(sheetName);
            } else {
                sourceSheet = wb.getSheetAt(0);
                sheetName = sourceSheet.getSheetName();
            }
            if (readerExcel != null) {
                int sheetsNumber = wb.getNumberOfSheets();
                List<String> sheetNames = new ArrayList<>();
                for (int i = 0; i < sheetsNumber; i++) {
                    sheetNames.add(wb.getSheetName(i));
                }
                readerExcel.setSheetNames(sheetNames);
                readerExcel.setSheet(sheetName);
            }
            iterator = sourceSheet.iterator();
            operate.handleData();
            wb.close();
        } catch (Exception e) {
            handleError(e.toString());
            setFailed();
        }
    }

    @Override
    public void readColumnNames() {
        if (iterator == null) {
            return;
        }
        while (iterator.hasNext() && !isStopped()) {
            readRecord();
            if (sourceRow == null || sourceRow.isEmpty()) {
                continue;
            }
            handleHeader();
            return;
        }
    }

    @Override
    public void readTotal() {
        if (iterator == null) {
            return;
        }
        rowIndex = 0;
        skipHeader();
        while (iterator.hasNext()) {
            if (isStopped()) {
                rowIndex = 0;
                return;
            }
            readRecord();
            if (sourceRow != null && !sourceRow.isEmpty()) {
                ++rowIndex;
            }
        }
    }

    public void skipHeader() {
        if (!readerHasHeader || iterator == null) {
            return;
        }
        while (iterator.hasNext() && (iterator.next() == null) && !isStopped()) {
        }
    }

    // rowIndex is 1-base while pageStartIndex and pageEndIndex are 0-based
    @Override
    public void readPage() {
        if (iterator == null) {
            return;
        }
        skipHeader();
        rowIndex = 0;
        while (iterator.hasNext() && !isStopped()) {
            readRecord();
            if (sourceRow == null || sourceRow.isEmpty()) {
                continue;
            }
            if (rowIndex++ < pageStartIndex) {
                continue;
            }
            if (rowIndex > pageEndIndex) {
                stop();
                break;
            }
            handlePageRow();
        }
    }

    @Override
    public void readRows() {
        if (iterator == null) {
            return;
        }
        skipHeader();
        rowIndex = 0;
        while (iterator.hasNext() && !isStopped()) {
            readRecord();
            if (sourceRow == null || sourceRow.isEmpty()) {
                continue;
            }
            ++rowIndex;
            handleRow();
        }
    }

    public void readRecord() {
        try {
            sourceRow = null;
            if (isStopped() || iterator == null) {
                return;
            }
            Row readerFileRow = iterator.next();
            if (readerFileRow == null) {
                return;
            }
            sourceRow = new ArrayList<>();
            for (int cellIndex = readerFileRow.getFirstCellNum(); cellIndex < readerFileRow.getLastCellNum(); cellIndex++) {
                String v = MicrosoftDocumentTools.cellString(readerFileRow.getCell(cellIndex));
                sourceRow.add(v);
            }
        } catch (Exception e) {
            handleError(e.toString());
        }
    }

}
