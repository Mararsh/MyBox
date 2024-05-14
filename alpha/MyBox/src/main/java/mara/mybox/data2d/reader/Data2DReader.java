package mara.mybox.data2d.reader;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.Data2D_Edit;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataFileExcel;
import mara.mybox.data2d.DataFileText;
import mara.mybox.data2d.DataMatrix;
import mara.mybox.data2d.DataTable;
import mara.mybox.data2d.operate.Data2DOperate;
import mara.mybox.db.DerbyBase;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.StringTools;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public abstract class Data2DReader {

    protected Data2D sourceData;
    protected File sourceFile;
    protected Data2DOperate operate;
    protected long sourceIndex; // 1-based 
    protected long pageStartIndex, pageEndIndex; //  0-based
    protected List<String> sourceRow, names;
    protected List<List<String>> rows = new ArrayList<>();
    protected Connection conn;
    protected boolean readerHasHeader;

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
        } else if (data instanceof DataMatrix) {
            return new MatrixReader((DataMatrix) data);
        }
        return null;
    }

    public boolean start(boolean scanWholeFile) {
        if (sourceData == null || operate == null) {
            return false;
        }
        sourceFile = sourceData.getFile();
        readerHasHeader = sourceData.isHasHeader();
        sourceIndex = 0;  // 1-based
        pageStartIndex = sourceData.getStartRowOfCurrentPage();
        pageEndIndex = pageStartIndex + sourceData.getPageSize();
        names = new ArrayList<>();
        rows = new ArrayList<>();
        sourceRow = new ArrayList<>();
        sourceData.startFilter();
        if (sourceData.isTmpData()) {
            scanPage();
        } else if (scanWholeFile || !sourceData.hasPage()
                || sourceData.isMutiplePages() || !sourceData.isDataLoaded()) {
            scanFile();
        } else {
            scanPage();
        }
        afterScanned();
        return true;
    }

    public void scanFile() {
    }

    public void scanPage() {
        try {
            for (int r = 0; r < sourceData.tableRowsNumber(); r++) {
                if (isStopped()) {
                    return;
                }
                sourceRow = sourceData.tableRow(r);
                if (sourceRow == null || sourceRow.isEmpty()) {
                    continue;
                }
                ++sourceIndex;
                handleRow();
            }
        } catch (Exception e) {  // skip  bad lines
            showError(e.toString());
//            setFailed();
        }
    }

    public void handleRow() {
        try {
            operate.handleRow(sourceRow, sourceIndex);
        } catch (Exception e) {
            showError(e.toString());
        }
    }

    public void makeHeader() {
        try {
            names = new ArrayList<>();
            if (readerHasHeader && StringTools.noDuplicated(sourceRow, true)) {
                names.addAll(sourceRow);
            } else {
                readerHasHeader = false;
                if (sourceRow != null) {
                    for (int i = 1; i <= sourceRow.size(); i++) {
                        names.add(sourceData.colPrefix() + i);
                    }
                }
            }
            sourceData.setHasHeader(readerHasHeader);
            stop();
        } catch (Exception e) {
            showError(e.toString());
        }
    }

    public void makePageRow() {
        List<String> row = new ArrayList<>();
        for (int i = 0; i < Math.min(sourceRow.size(), sourceData.columnsNumber()); i++) {
            row.add(sourceRow.get(i));
        }
        for (int col = row.size(); col < sourceData.columnsNumber(); col++) {
            row.add(sourceData.defaultColValue());
        }
        row.add(0, "" + sourceIndex);
        rows.add(row);
    }

    public void afterScanned() {
        try {
            sourceData.stopFilter();
        } catch (Exception e) {
            showError(e.toString());
        }
    }

    /*
        status
     */
    public FxTask task() {
        if (operate != null) {
            return operate.getTask();
        } else {
            return null;
        }
    }

    public Connection conn() {
        if (operate != null) {
            return operate.conn();
        } else {
            return DerbyBase.getConnection();
        }
    }

    public void showInfo(String info) {
        if (operate != null) {
            operate.showInfo(info);
        }
    }

    public void showError(String error) {
        if (operate != null) {
            operate.showError(error);
        }
    }

    public void stop() {
        if (operate != null) {
            operate.stop();
        }
    }

    public void setFailed() {
        if (operate != null) {
            operate.setFailed();
        }
    }

    public boolean isStopped() {
        return operate == null || operate.isStopped();
    }

    /*
        get/set
     */
    public Data2D getSourceData() {
        return sourceData;
    }

    public Data2DOperate getOperate() {
        return operate;
    }

    public void setOperate(Data2DOperate operate) {
        this.operate = operate;
    }

    public List<String> getNames() {
        return names;
    }

    public List<List<String>> getRows() {
        return rows;
    }

    public long getSourceIndex() {
        return sourceIndex;
    }

    public void setSourceIndex(long sourceIndex) {
        this.sourceIndex = sourceIndex;
    }

    public Data2DReader setReaderData(Data2D readerData) {
        this.sourceData = readerData;
        return this;
    }

    public Data2DReader setReaderHasHeader(boolean readerHasHeader) {
        this.readerHasHeader = readerHasHeader;
        return this;
    }

    public Data2DReader setNames(List<String> names) {
        this.names = names;
        return this;
    }

}
