package mara.mybox.data2d.reader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.Data2D_Edit;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataFileExcel;
import mara.mybox.data2d.DataFileText;
import mara.mybox.data2d.DataFilter;
import mara.mybox.data2d.DataTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.tools.StringTools;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public abstract class Data2DReader {

    protected Data2D data2D;
    protected File sourceFile;
    protected Data2DOperator operator;
    protected long rowIndex; // 1-based 
    protected long rowsStart, rowsEnd; //  0-based
    protected List<String> sourceRow, names;
    protected List<List<String>> rows = new ArrayList<>();
    protected boolean failed;
    protected DataFilter filter;
    protected boolean readerHasHeader, readerStopped, needCheckTask;
    protected SingletonTask task;

    public abstract void scanData();

    public abstract void readColumnNames();

    public abstract void readTotal();

    public abstract void readPage();

    public abstract void readRows();

    public static Data2DReader create(Data2D_Edit data) {
        if (data == null) {
            return null;
        }
        if (data instanceof DataFileExcel) {
            return new DataFileExcelReader((DataFileExcel) data);
        } else if (data instanceof DataFileCSV) {
            return new DataFileCSVReader((DataFileCSV) data);
        } else if (data instanceof DataFileText) {
            return new DataFileTextReader((DataFileText) data);
        } else if (data instanceof DataTable) {
            return new DataTableReader((DataTable) data);
        }
        return null;
    }

    public void init(Data2D data) {
        this.data2D = data;
        task = data2D.getTask();
    }

    public boolean start() {
        if (data2D == null || !data2D.validData() || operator == null) {
            return false;
        }
        sourceFile = data2D.getFile();
        readerStopped = false;
        readerHasHeader = data2D.isHasHeader();
        needCheckTask = task != null;
        rowIndex = 0;  // 1-based
        rowsStart = data2D.getStartRowOfCurrentPage();
        rowsEnd = rowsStart + data2D.getPageSize();
        names = new ArrayList<>();
        rows = new ArrayList<>();
        sourceRow = new ArrayList<>();
        data2D.startFilter();
        scanData();
        afterScanned();
        operator.end();
        return true;
    }

    public void handleRow() {
        try {
            if (!data2D.filterDataRow(sourceRow, rowIndex)) {
                return;
            }
            if (data2D.filterReachMaxPassed()) {
                readerStopped = true;
                return;
            }
            operator.sourceRow = sourceRow;
            operator.rowIndex = rowIndex;
            operator.handleRow();
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
        }
    }

    public void handleHeader() {
        try {
            names = new ArrayList<>();
            if (readerHasHeader && StringTools.noDuplicated(sourceRow, true)) {
                names.addAll(sourceRow);
            } else {
                readerHasHeader = false;
                if (sourceRow != null) {
                    for (int i = 1; i <= sourceRow.size(); i++) {
                        names.add(data2D.colPrefix() + i);
                    }
                }
            }
            data2D.setHasHeader(readerHasHeader);
            readerStopped = true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
        }
    }

    public void handlePageRow() {
        List<String> row = new ArrayList<>();
        for (int i = 0; i < Math.min(sourceRow.size(), data2D.columnsNumber()); i++) {
            row.add(sourceRow.get(i));
        }
        for (int col = row.size(); col < data2D.columnsNumber(); col++) {
            row.add(data2D.defaultColValue());
        }
        row.add(0, "" + rowIndex);
        rows.add(row);
    }

    public void afterScanned() {
        try {
            data2D.stopFilter();
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
        }
    }

    public boolean readerStopped() {
        return readerStopped || (needCheckTask && (task == null || task.isCancelled()));
    }

    /*
        get/set
     */
    public boolean isFailed() {
        return failed;
    }

    public List<String> getNames() {
        return names;
    }

    public List<List<String>> getRows() {
        return rows;
    }

    public long getRowIndex() {
        return rowIndex;
    }

    public Data2DReader setReaderData(Data2D readerData) {
        this.data2D = readerData;
        return this;
    }

    public Data2DReader setReaderHasHeader(boolean readerHasHeader) {
        this.readerHasHeader = readerHasHeader;
        return this;
    }

    public Data2DReader setReaderCanceled(boolean readerStopped) {
        this.readerStopped = readerStopped;
        return this;
    }

    public Data2DReader setNeedCheckTask(boolean needCheckTask) {
        this.needCheckTask = needCheckTask;
        return this;
    }

    public Data2DReader setTask(SingletonTask task) {
        this.task = task;
        return this;
    }

    public Data2DReader setNames(List<String> names) {
        this.names = names;
        return this;
    }

}
