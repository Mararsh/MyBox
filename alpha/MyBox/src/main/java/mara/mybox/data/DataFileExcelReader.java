package mara.mybox.data;

import java.util.ArrayList;
import java.util.List;
import mara.mybox.dev.MyBoxLog;
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
public class DataFileExcelReader extends DataFileReader {

    protected DataFileExcel readerExcel;
    protected String readerSheet;

    public DataFileExcelReader(DataFileExcel data) {
        this.readerExcel = data;
        readerSheet = data.getSheet();
        init(data);
    }

    @Override
    public void scanFile() {
        try ( Workbook wb = WorkbookFactory.create(readerFile)) {
            Sheet sourceSheet;
            if (readerSheet != null) {
                sourceSheet = wb.getSheet(readerSheet);
            } else {
                sourceSheet = wb.getSheetAt(0);
                readerSheet = sourceSheet.getSheetName();
            }
            if (readerExcel != null) {
                int sheetsNumber = wb.getNumberOfSheets();
                List<String> sheetNames = new ArrayList<>();
                for (int i = 0; i < sheetsNumber; i++) {
                    sheetNames.add(wb.getSheetName(i));
                }
                readerExcel.setSheetNames(sheetNames);
                readerExcel.setSheet(readerSheet);
            }
            iterator = sourceSheet.iterator();
            if (operation == null) {
                readRecords();
            } else {
                switch (operation) {
                    case ReadDefnition:
                        readDefinition();
                        break;
                    case ReadColumns:
                        readColumns();
                        break;
                    case ReadTotal:
                        readTotal();
                        break;
                    case ReadPage:
                        readPage();
                        break;
                    default:
                        readRecords();
                        break;
                }
            }
            wb.close();
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (readerTask != null) {
                readerTask.setError(e.toString());
            }
        }
    }

    public void readColumns() {
        if (iterator == null) {
            return;
        }
        while (iterator.hasNext() && !readerStopped()) {
            record = readRecord();
            if (record == null || record.isEmpty()) {
                continue;
            }
            handleHeader();
            return;
        }
    }

    public void readTotal() {
        if (iterator == null) {
            return;
        }
        while (iterator.hasNext()) {
            if (readerStopped()) {
                rowIndex = 0;
                return;
            }
            ++rowIndex;
            iterator.next();
        }
    }

    public void skipHeader() {
        if (!readerHasHeader || iterator == null) {
            return;
        }
        while (iterator.hasNext() && (iterator.next() == null) && !readerStopped()) {
        }
    }

    public void readPage() {
        if (iterator == null) {
            return;
        }
        skipHeader();
        rowIndex = 0;
        while (iterator.hasNext() && !readerStopped()) {
            if (++rowIndex < rowsStart) {
                iterator.next();
                continue;
            }
            if (rowIndex >= rowsEnd) {
                readerStopped = true;
                break;
            }
            record = readRecord();
            if (record == null || record.isEmpty()) {
                continue;
            }
            handlePageRow();
        }
    }

    public void readRecords() {
        if (iterator == null) {
            return;
        }
        skipHeader();
        rowIndex = 0;
        while (iterator.hasNext() && !readerStopped()) {
            record = readRecord();
            if (record == null || record.isEmpty()) {
                continue;
            }
            ++rowIndex;
            handle(record);
        }
    }

    @Override
    public List<String> readRecord() {
        try {
            if (readerStopped() || iterator == null) {
                return null;
            }
            Row readerFileRow = iterator.next();
            if (readerFileRow == null) {
                return null;
            }
            record = new ArrayList<>();
            for (int cellIndex = readerFileRow.getFirstCellNum(); cellIndex < readerFileRow.getLastCellNum(); cellIndex++) {
                String v = MicrosoftDocumentTools.cellString(readerFileRow.getCell(cellIndex));
                record.add(v);
            }
            if (record.isEmpty()) {
                return null;
            }
            return record;
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (readerTask != null) {
                readerTask.setError(e.toString());
            }
            return null;
        }
    }

}
