package mara.mybox.data2d.operate;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.BaseLogsController;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.Data2D_Edit;
import mara.mybox.data2d.reader.Data2DReader;
import mara.mybox.data2d.writer.Data2DWriter;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition.InvalidAs;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public abstract class Data2DOperate {

    protected BaseController controller;
    protected Data2DReader reader;
    protected List<Data2DWriter> writers;
    protected Data2D sourceData;
    protected File sourceFile, targetPath, targetFile;
    protected List<String> sourceRow, targetRow;
    protected FxTask task;
    protected List<Integer> cols, otherCols;
    protected int colsLen, scale = -1;
    protected boolean includeRowNumber, writeHeader, rowPassFilter, reachMaxFiltered;
    protected boolean stopped, needCheckTask, failed, closeConn;
    protected long sourceRowIndex; // 1-based
    protected long handledCount;
    protected InvalidAs invalidAs;
    protected Connection conn;
    protected List<File> printedFiles;

    public Data2DOperate() {
        closeConn = true;
    }

    public String info() {
        String s = getClass().toString() + "\n";
        if (controller != null) {
            s += "controller: " + controller.getClass() + "\n";
        }
        if (reader != null) {
            s += "reader: " + reader.getClass() + "\n";
        }
        if (sourceData != null) {
            s += sourceData.info();
        }
        return s;
    }


    /*
        reader
     */
    public boolean setSourceData(Data2D_Edit data) {
        reader = Data2DReader.create(data);
        if (reader == null) {
            return false;
        }
        sourceData = reader.getSourceData();
        sourceFile = sourceData.getFile();
        task = sourceData.getTask();
        reader.setOperate(this);
        return true;
    }

    /*
        writers
     */
    public Data2DOperate addWriter(Data2DWriter writer) {
        if (writer == null) {
            return this;
        }
        if (writers == null) {
            writers = new ArrayList<>();
        }

        writers.add(writer.setOperate(this));
        return this;
    }

    public boolean openWriters() {
        if (writers == null) {
            return true;
        }
        for (Data2DWriter writer : writers) {
            setTargetFile(writer);
            if (!writer.openWriter()) {
                failStop(null);
                end();
                return false;
            }
        }
        return true;
    }

    public void setTargetFile(Data2DWriter writer) {
        if (writer != null && targetFile != null) {
            writer.setPrintFile(targetFile);
        }
    }

    public Data2DOperate addPrintedFile(File file) {
        if (file == null || !file.exists()) {
            return this;
        }
        if (printedFiles == null) {
            printedFiles = new ArrayList<>();
        }
        printedFiles.add(file);
        return this;
    }

    public boolean closeWriters() {
        if (writers == null) {
            return true;
        }
        for (Data2DWriter writer : writers) {
            writer.closeWriter();
        }
        return true;
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
        handledCount = 0;
        sourceRowIndex = 0;
        sourceRow = null;
        targetRow = null;
        stopped = false;
        failed = false;
        rowPassFilter = false;
        reachMaxFiltered = false;
        if (sourceData == null) {
            return false;
        }
        if (scale < 0) {
            scale = sourceData.getScale();
        }
        if (task != null) {
            controller = task.getController();
        }
        if (task == null) {
            controller = sourceData.getController();
        }
        return openWriters();
    }

    public boolean go() {
        return reader.start(false);
    }

    public void handleData() {
        reader.readRows();
    }

    public void handleRow(List<String> row, long index) {
        sourceRow = row;
        sourceRowIndex = index;
        targetRow = null;
        rowPassFilter = sourceData.filterDataRow(sourceRow, sourceRowIndex);
        reachMaxFiltered = sourceData.filterReachMaxPassed();
        if (reachMaxFiltered) {
            stopped = true;
        } else if (rowPassFilter) {
            if (handleRow()) {
                writeRow();
                handledCount++;
            }
        }
    }

    public boolean handleRow() {
        return false;
    }

    public void writeRow() {
        if (writers == null || targetRow == null) {
            return;
        }
        for (Data2DWriter writer : writers) {
            writer.writeRow(targetRow);
        }
    }

    public boolean end() {
        try {
            closeWriters();
            if (conn != null) {
                conn.commit();
                if (closeConn) {
                    conn.close();
                }
                conn = null;
            }
            return true;
        } catch (Exception e) {
            showError(e.toString());
            return false;
        }
    }

    public boolean openResults() {
        if (writers == null || writers.isEmpty()) {
            return false;
        }
        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                Platform.runLater(() -> {
                    for (Data2DWriter writer : writers) {
                        if (writer.showResult()) {
                            showInfo(writer.getFileSuffix() + " written.");
                        } else {
                            showInfo(writer.getFileSuffix() + " no data.");
                        }
                    }
                });
                Platform.requestNextPulse();
            }

        }, 200);
        return true;
    }

    /*
        status
     */
    public Connection conn() {
        try {
            if (conn == null || conn.isClosed()) {
                conn = DerbyBase.getConnection();
            }
            return conn;
        } catch (Exception e) {
            showError(e.toString());
            setFailed();
            return null;
        }
    }

    public FxTask getTask() {
        if (task == null && controller != null) {
            task = controller.getTask();
        }
        return task;
    }

    public void showInfo(String info) {
        if (info == null || info.isBlank()) {
            return;
        }
//        MyBoxLog.console(info);
        if (controller != null) {
            if (controller instanceof BaseLogsController) {
                ((BaseLogsController) controller).updateLogs(info);
            } else if (task != null) {
                task.setInfo(info);
            } else {
                controller.popInformation(info);
            }
        } else if (task != null) {
            task.setInfo(info);
        }
    }

    public void showError(String error) {
        if (error == null || error.isBlank()) {
            return;
        }
//        MyBoxLog.console(error);
        if (controller != null) {
            if (controller instanceof BaseLogsController) {
                ((BaseLogsController) controller).showLogs(error);
            } else if (task != null) {
                task.setError(error);
            } else {
                controller.displayError(error);
            }
        } else if (task != null) {
            task.setError(error);
        } else {
            MyBoxLog.error(error);
        }
    }

    public void stop() {
        stopped = true;
//        showInfo(message("Stopped"));
    }

    public boolean isStopped() {
        return stopped;
    }

    public void setFailed() {
        failed = true;
        showInfo(message("Failed"));
    }

    public void failStop(String error) {
        fail(error);
        stop();
    }

    public void fail(String error) {
        setFailed();
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

    public List<Data2DWriter> getWriters() {
        return writers;
    }

    public Data2DOperate setWriters(List<Data2DWriter> writers) {
        this.writers = writers;
        return this;
    }

    public Data2DOperate setController(BaseController controller) {
        this.controller = controller;
        return this;
    }

    /*
        get
     */
    public Data2DReader getReader() {
        return reader;
    }

    public Data2D getSourceData() {
        return sourceData;
    }

    public File getSourceFile() {
        return sourceFile;
    }

    public int getScale() {
        return scale;
    }

    public BaseController getController() {
        return controller;
    }

    public long getSourceRowIndex() {
        return reader.getSourceIndex();
    }

    public long getHandledCount() {
        return handledCount;
    }

    public InvalidAs getInvalidAs() {
        return invalidAs;
    }

    public List<File> getPrintedFiles() {
        return printedFiles;
    }

}
