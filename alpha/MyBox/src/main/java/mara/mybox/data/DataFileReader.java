package mara.mybox.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.tools.StringTools;
import org.apache.poi.ss.usermodel.Row;

/**
 * @Author Mara
 * @CreateDate 2022-1-28
 * @License Apache License Version 2.0
 */
public abstract class DataFileReader {

    protected DataFile dataFile;
    protected File readerFile;
    protected Operation operation;
    protected long rowIndex, rowsStart, rowsEnd;
    protected int columnsNumber;
    protected List<String> record, names;
    protected List<List<String>> rows = new ArrayList<>();
    protected boolean readerHasHeader, readerStopped, needCheckTask;
    protected Iterator<Row> iterator;
    protected SingletonTask readerTask;

    public static enum Operation {
        ReadDefnition, ReadTotal, ReadColumns, ReadPage
    }

    public abstract void scanFile();

    public static DataFileReader create2(DataFile data) {
        if (data == null) {
            return null;
        }
        if (data instanceof DataFileExcel) {
            return new DataFileExcelReader((DataFileExcel) data);
        }
        return null;
    }

    public void init(DataFile data) {
        this.dataFile = data;
        readerTask = dataFile.getTask();
    }

    public DataFileReader start(Operation operation) {
        if (dataFile == null) {
            return this;
        }
        this.operation = operation;
        return start();
    }

    public DataFileReader start() {
        iterator = null;
        readerStopped = false;
        readerFile = dataFile.getFile();
        if (readerFile == null || !readerFile.exists() || readerFile.length() == 0) {
            return this;
        }
        readerHasHeader = dataFile.isHasHeader();
        needCheckTask = readerTask != null;
        columnsNumber = dataFile.columnsNumber();
        rowIndex = -1;
        rowsStart = dataFile.startRowOfCurrentPage;
        rowsEnd = rowsStart + dataFile.pageSize;
        names = new ArrayList<>();
        rows = new ArrayList<>();
        scanFile();
        afterScanned();
        return this;
    }

    public boolean readDefinition() {
        dataFile.setHasHeader(readerHasHeader);
        return true;
    }

    public List<String> readRecord() {
        return null;
    }

    public boolean handleHeader() {
        try {
            names = new ArrayList<>();
            if (readerHasHeader && StringTools.noDuplicated(record, true)) {
                names.addAll(record);
            } else {
                readerHasHeader = false;
                for (int i = 1; i <= record.size(); i++) {
                    names.add(dataFile.colPrefix() + i);
                }
            }
            dataFile.setHasHeader(readerHasHeader);
            readerStopped = true;
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (readerTask != null) {
                readerTask.setError(e.toString());
            }
            return false;
        }
    }

    public boolean handlePageRow() {
        List<String> row = new ArrayList<>();
        for (int i = 0; i < Math.min(record.size(), columnsNumber); i++) {
            row.add(record.get(i));
        }
        for (int col = row.size(); col < columnsNumber; col++) {
            row.add(dataFile.defaultColValue());
        }
        row.add(0, "" + (rowIndex + 1));
        rows.add(row);
        return false;
    }

    public boolean handle(List<String> record) {
        return false;
    }

    public void afterScanned() {
        if (operation == Operation.ReadTotal) {
            if (readerHasHeader && rowIndex > 0) {
                rowIndex--;
            }
            dataFile.setDataSize(rowIndex);
        }
    }

    public boolean readerStopped() {
        return readerStopped || (needCheckTask && (readerTask == null || readerTask.isCancelled()));
    }

    /*
        get/set
     */
    public DataFile getReaderData() {
        return dataFile;
    }

    public DataFileReader setReaderData(DataFileExcel readerData) {
        this.dataFile = readerData;
        return this;
    }

    public File getReaderFile() {
        return readerFile;
    }

    public DataFileReader setReaderFile(File readerFile) {
        this.readerFile = readerFile;
        return this;
    }

    public boolean isReaderHasHeader() {
        return readerHasHeader;
    }

    public DataFileReader setReaderHasHeader(boolean readerHasHeader) {
        this.readerHasHeader = readerHasHeader;
        return this;
    }

    public boolean isReaderCanceled() {
        return readerStopped;
    }

    public DataFileReader setReaderCanceled(boolean readerStopped) {
        this.readerStopped = readerStopped;
        return this;
    }

    public boolean isNeedCheckTask() {
        return needCheckTask;
    }

    public DataFileReader setNeedCheckTask(boolean needCheckTask) {
        this.needCheckTask = needCheckTask;
        return this;
    }

    public Iterator<Row> getIterator() {
        return iterator;
    }

    public DataFileReader setIterator(Iterator<Row> iterator) {
        this.iterator = iterator;
        return this;
    }

    public SingletonTask getReaderTask() {
        return readerTask;
    }

    public DataFileReader setReaderTask(SingletonTask readerTask) {
        this.readerTask = readerTask;
        return this;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    public long getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(long rowIndex) {
        this.rowIndex = rowIndex;
    }

    public long getRowsStart() {
        return rowsStart;
    }

    public void setRowsStart(long rowsStart) {
        this.rowsStart = rowsStart;
    }

    public long getRowsEnd() {
        return rowsEnd;
    }

    public void setRowsEnd(long rowsEnd) {
        this.rowsEnd = rowsEnd;
    }

    public int getColumnsNumber() {
        return columnsNumber;
    }

    public void setColumnsNumber(int columnsNumber) {
        this.columnsNumber = columnsNumber;
    }

    public List<String> getRecord() {
        return record;
    }

    public void setRecord(List<String> record) {
        this.record = record;
    }

    public List<List<String>> getRows() {
        return rows;
    }

    public void setRows(List<List<String>> rows) {
        this.rows = rows;
    }

    public boolean isReaderStopped() {
        return readerStopped;
    }

    public void setReaderStopped(boolean readerStopped) {
        this.readerStopped = readerStopped;
    }

}
