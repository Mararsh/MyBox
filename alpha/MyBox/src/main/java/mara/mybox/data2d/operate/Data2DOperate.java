package mara.mybox.data2d.operate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.Data2DTools;
import mara.mybox.data2d.Data2D_Edit;
import mara.mybox.data2d.reader.Data2DReader;
import mara.mybox.data2d.writer.Data2DWriter;
import mara.mybox.data2d.writer.DataFileCSVWriter;
import mara.mybox.db.data.ColumnDefinition.InvalidAs;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public abstract class Data2DOperate {

    protected Data2DReader reader;
    protected List<Data2DWriter> writers;
    protected Data2D sourceData;
    protected File sourceFile;
    protected List<String> sourceRow, targetRow;
    protected FxTask task;
    protected List<Integer> cols, otherCols;
    protected int colsLen, scale = -1;
    protected boolean includeRowNumber, writeHeader, formatValues, passFilter, reachMax;
    protected boolean stopped, needCheckTask, errorContinue, failed;
    protected long sourceRowIndex; // 1-based 
    protected long count;
    protected InvalidAs invalidAs;

    /*
        init
     */
    public boolean setSourceData(Data2D_Edit data) {
        reader = Data2DReader.create(data);
        if (reader == null) {
            return false;
        }
        sourceData = reader.getSourceData();
        sourceFile = data.getFile();
        task = data.getTask();
        reader.setOperate(this);
        return true;
    }

    public Data2DOperate addWriter(Data2DWriter writer) {
        if (writers == null) {
            writers = new ArrayList<>();
        }
        writers.add(writer);
        return this;
    }

    /*
        run
     */
    public Data2DOperate start() {
        if (!checkParameters() || !go()) {
            failStop(null);
        }
        end();
        return this;
    }

    public boolean checkParameters() {
        count = 0;
        sourceRowIndex = 0;
        sourceRow = null;
        targetRow = null;
        stopped = false;
        failed = false;
        passFilter = false;
        reachMax = false;
        if (sourceData == null || !sourceData.validData()) {
            return false;
        }
        if (scale < 0) {
            scale = sourceData.getScale();
        }
        if (invalidAs == null) {
            invalidAs = InvalidAs.Skip;
        }
        if (writers == null || writers.isEmpty()) {
            makeTmpWriter();
        }
        return true;
    }

    public void makeTmpWriter() {
        if (!sourceData.isDataFile()) {
            return;
        }
        List<Data2DColumn> columns = sourceData.makeColumns(cols, includeRowNumber);
        DataFileCSVWriter writer = new DataFileCSVWriter();
        writer.setWriteHeader(true).setFormatValues(formatValues)
                .setRowNumber(includeRowNumber)
                .setColumns(columns)
                .setHeaderNames(Data2DTools.toNames(columns));
        writers = new ArrayList<>();
        writers.add(writer);
    }

    public boolean go() {
        return reader.start();
    }

    public void handleData() {
        reader.readRows();
    }

    public boolean handleRow() {
        return false;
    }

    public void handleRow(List<String> row, long index) {
        sourceRow = row;
        sourceRowIndex = index;
        targetRow = null;
        passFilter = sourceData.filterDataRow(sourceRow, sourceRowIndex);
        reachMax = sourceData.filterReachMaxPassed();
        if (reachMax) {
            stopped = true;
        } else if (passFilter) {
            if (handleRow()) {
                writeRow();
                count++;
            }
        }
    }

    public void writeRow() {
        if (writers == null || targetRow == null) {
            return;
        }
        for (Data2DWriter writer : writers) {
//            writer.writeRow(targetRow);
        }
    }

    public boolean end() {
        return true;
    }

    public void showInfo(String info) {
        if (task != null) {
            task.setInfo(info);
        }
    }

    public void showError(String error) {
        if (error == null || error.isBlank()) {
            return;
        }
        MyBoxLog.error(error);
        if (task != null) {
            task.setError(error);
        }
    }

    public void setStatus(boolean fail, boolean stop) {
        failed = fail;
        stopped = stop;
    }

    public void stop() {
        stopped = true;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void setFailed() {
        failed = true;
    }

    public void failStop(String error) {
        fail(error);
        stop();
    }

    public void fail(String error) {
        failed = true;
        showError(error);
    }

    public boolean isFailed() {
        return failed;
    }

    /*
        set
     */
    public Data2DOperate setTask(FxTask task) {
        this.task = task;
        return this;
    }

    public Data2DOperate setCols(List<Integer> cols) {
        this.cols = cols;
        if (cols == null) {
            colsLen = 0;
        } else {
            colsLen = cols.size();
        }
        return this;
    }

    public Data2DOperate setOtherCols(List<Integer> otherCols) {
        this.otherCols = otherCols;
        return this;
    }

    public Data2DOperate setReader(Data2DReader reader) {
        this.reader = reader;
        return this;
    }

    public Data2DOperate setData2D(Data2D data2D) {
        this.sourceData = data2D;
        return this;
    }

    public boolean isWriteHeader() {
        return writeHeader;
    }

    public Data2DOperate setWriteHeader(boolean writeHeader) {
        this.writeHeader = writeHeader;
        return this;
    }

    public Data2DOperate setIncludeRowNumber(boolean includeRowNumber) {
        this.includeRowNumber = includeRowNumber;
        return this;
    }

    public Data2DOperate setSourceRow(List<String> sourceRow) {
        this.sourceRow = sourceRow;
        return this;
    }

    public Data2DOperate setRowIndex(long rowIndex) {
        this.sourceRowIndex = rowIndex;
        return this;
    }

    public Data2DOperate setScale(int scale) {
        this.scale = scale;
        return this;
    }

    public Data2DOperate setInvalidAs(InvalidAs invalidAs) {
        this.invalidAs = invalidAs;
        return this;
    }

    public Data2DOperate setFormatValues(boolean formatValues) {
        this.formatValues = formatValues;
        return this;
    }

    public List<Data2DWriter> getWriters() {
        return writers;
    }

    public Data2DOperate setWriters(List<Data2DWriter> writers) {
        this.writers = writers;
        return this;
    }

    public Data2DOperate setErrorContinue(boolean errorContinue) {
        this.errorContinue = errorContinue;
        return this;
    }

    /*
        get
     */
    public FxTask getTask() {
        return task;
    }

    public long getSourceRowIndex() {
        return reader.getRowIndex();
    }

    public long getCount() {
        return count;
    }

    public InvalidAs getInvalidAs() {
        return invalidAs;
    }

}
