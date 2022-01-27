package mara.mybox.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
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
public class DataFileExcelReader {

    protected DataFileExcel readerData;
    protected File readerFile;
    protected String readerSheet;
    protected boolean readerHasHeader, readerStopped, needCheckTask;
    protected Iterator<Row> iterator;
    protected Object returnedValue;
    protected SingletonTask readerTask;

    public DataFileExcelReader(DataFileExcel data) {
        this.readerData = data;
        if (data != null) {
            readerFile = data.getFile();
            readerSheet = data.getSheet();
            readerHasHeader = data.isHasHeader();
            readerTask = data.getTask();
            needCheckTask = readerTask != null;
        }
    }

    public DataFileExcelReader(File readerFile, String readerSheet) {
        this.readerFile = readerFile;
        this.readerSheet = readerSheet;
    }

    public boolean valid() {
        return true;
    }

    public Object start() {
        iterator = null;
        readerStopped = false;
        if (readerFile == null || !readerFile.exists() || readerFile.length() == 0 || !valid()) {
            return null;
        }
        try (Workbook wb = WorkbookFactory.create(readerFile)) {
            Sheet sourceSheet;
            if (readerSheet != null) {
                sourceSheet = wb.getSheet(readerSheet);
            } else {
                sourceSheet = wb.getSheetAt(0);
                readerSheet = sourceSheet.getSheetName();
            }
            if (readerData != null) {
                int sheetsNumber = wb.getNumberOfSheets();
                List<String> sheetNames = new ArrayList<>();
                for (int i = 0; i < sheetsNumber; i++) {
                    sheetNames.add(wb.getSheetName(i));
                }
                readerData.setSheetNames(sheetNames);
                readerData.setSheet(readerSheet);
            }
            iterator = sourceSheet.iterator();
            readIterator();
            wb.close();
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (readerTask != null) {
                readerTask.setError(e.toString());
            }
        }
        return returnValue();
    }

    public boolean readIterator() {
        try {
            if (iterator == null || !iterator.hasNext()) {
                return true;
            }
            if (readerHasHeader) {
                while (iterator.hasNext() && (iterator.next() == null) && !readerStopped()) {
                }
            }
            while (iterator.hasNext() && !readerStopped()) {
                readRow();
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (readerTask != null) {
                readerTask.setError(e.toString());
            }
            return false;
        }
    }

    public boolean readRow() {
        try {
            if (readerStopped() || iterator == null) {
                return false;
            }
            Row readerFileRow = iterator.next();
            if (readerFileRow == null) {
                return false;
            }
            List<String> record = new ArrayList<>();
            for (int cellIndex = readerFileRow.getFirstCellNum(); cellIndex < readerFileRow.getLastCellNum(); cellIndex++) {
                String v = MicrosoftDocumentTools.cellString(readerFileRow.getCell(cellIndex));
                record.add(v);
            }
            if (record.isEmpty()) {
                return false;
            }
            return handle(record);
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (readerTask != null) {
                readerTask.setError(e.toString());
            }
            return false;
        }
    }

    public boolean handle(List<String> record) {
        return true;
    }

    public Object returnValue() {
        return null;
    }

    public boolean readerStopped() {
        return readerStopped || (needCheckTask && (readerTask == null || readerTask.isCancelled()));
    }

    /*
        get/set
     */
    public DataFileExcel getReaderData() {
        return readerData;
    }

    public DataFileExcelReader setReaderData(DataFileExcel readerData) {
        this.readerData = readerData;
        return this;
    }

    public File getReaderFile() {
        return readerFile;
    }

    public DataFileExcelReader setReaderFile(File readerFile) {
        this.readerFile = readerFile;
        return this;
    }

    public String getReaderSheet() {
        return readerSheet;
    }

    public DataFileExcelReader setReaderSheet(String readerSheet) {
        this.readerSheet = readerSheet;
        return this;
    }

    public boolean isReaderHasHeader() {
        return readerHasHeader;
    }

    public DataFileExcelReader setReaderHasHeader(boolean readerHasHeader) {
        this.readerHasHeader = readerHasHeader;
        return this;
    }

    public boolean isReaderCanceled() {
        return readerStopped;
    }

    public DataFileExcelReader setReaderCanceled(boolean readerStopped) {
        this.readerStopped = readerStopped;
        return this;
    }

    public boolean isNeedCheckTask() {
        return needCheckTask;
    }

    public DataFileExcelReader setNeedCheckTask(boolean needCheckTask) {
        this.needCheckTask = needCheckTask;
        return this;
    }

    public Iterator<Row> getIterator() {
        return iterator;
    }

    public DataFileExcelReader setIterator(Iterator<Row> iterator) {
        this.iterator = iterator;
        return this;
    }

    public Object getReturnedValue() {
        return returnedValue;
    }

    public DataFileExcelReader setReturnedValue(Object returnedValue) {
        this.returnedValue = returnedValue;
        return this;
    }

    public SingletonTask getReaderTask() {
        return readerTask;
    }

    public DataFileExcelReader setReaderTask(SingletonTask readerTask) {
        this.readerTask = readerTask;
        return this;
    }

}
