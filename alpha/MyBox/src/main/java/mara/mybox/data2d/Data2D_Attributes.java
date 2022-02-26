package mara.mybox.data2d;

import java.util.List;
import java.util.Map;
import mara.mybox.controller.ControlData2DLoad;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public abstract class Data2D_Attributes extends Data2DDefinition {

    protected TableData2DDefinition tableData2DDefinition;
    protected TableData2DColumn tableData2DColumn;
    protected List<Data2DColumn> columns, savedColumns;
    protected Map<String, Object> options;
    protected int pageSize, newColumnIndex;
    protected long dataSize, pagesNumber;
    protected long currentPage, startRowOfCurrentPage, endRowOfCurrentPage;   // 0-based, excluded end
    protected ControlData2DLoad loadController;
    protected boolean tableChanged;
    protected SingletonTask task, backgroundTask;
    protected String error;

    public Data2D_Attributes() {
        tableData2DDefinition = new TableData2DDefinition();
        tableData2DColumn = new TableData2DColumn();
        pageSize = 50;
        initData();
    }

    private void initData() {
        resetDefinition();
        dataSize = 0;
        pagesNumber = 1;
        currentPage = startRowOfCurrentPage = endRowOfCurrentPage = 0;
        columns = null;
        savedColumns = null;
        newColumnIndex = -1;
        tableChanged = false;
        options = null;
        error = null;
    }

    public void resetData() {
        initData();
    }

    public void cloneAll(Data2D_Attributes d) {
        try {
            if (d == null) {
                return;
            }
            super.cloneAll(d);
            tableData2DDefinition = d.tableData2DDefinition;
            tableData2DColumn = d.tableData2DColumn;
            columns = d.columns;
            savedColumns = d.savedColumns;
            pageSize = d.pageSize;
            newColumnIndex = d.newColumnIndex;
            dataSize = d.dataSize;
            pagesNumber = d.pagesNumber;
            currentPage = d.currentPage;
            startRowOfCurrentPage = d.startRowOfCurrentPage;
            endRowOfCurrentPage = d.endRowOfCurrentPage;
            loadController = d.loadController;
            tableChanged = d.tableChanged;
            task = d.task;
            backgroundTask = d.backgroundTask;
            error = d.error;
            options = d.options;
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    /*
        get/set
     */
    public TableData2DDefinition getTableData2DDefinition() {
        return tableData2DDefinition;
    }

    public void setTableData2DDefinition(TableData2DDefinition tableData2DDefinition) {
        this.tableData2DDefinition = tableData2DDefinition;
    }

    public TableData2DColumn getTableData2DColumn() {
        return tableData2DColumn;
    }

    public void setTableData2DColumn(TableData2DColumn tableData2DColumn) {
        this.tableData2DColumn = tableData2DColumn;
    }

    public List<Data2DColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<Data2DColumn> columns) {
        this.columns = columns;
    }

    public long getDataSize() {
        return dataSize;
    }

    public void setDataSize(long dataSize) {
        this.dataSize = dataSize;
    }

    public long getPagesNumber() {
        return pagesNumber;
    }

    public void setPagesNumber(long pagesNumber) {
        this.pagesNumber = pagesNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getStartRowOfCurrentPage() {
        return startRowOfCurrentPage;
    }

    public void setStartRowOfCurrentPage(long startRowOfCurrentPage) {
        this.startRowOfCurrentPage = startRowOfCurrentPage;
    }

    public long getEndRowOfCurrentPage() {
        return endRowOfCurrentPage;
    }

    public void setEndRowOfCurrentPage(long endRowOfCurrentPage) {
        this.endRowOfCurrentPage = endRowOfCurrentPage;
    }

    public long getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(long currentPage) {
        this.currentPage = currentPage;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public boolean isTableChanged() {
        return tableChanged;
    }

    public ControlData2DLoad getLoadController() {
        return loadController;
    }

    public void setLoadController(ControlData2DLoad loadController) {
        this.loadController = loadController;
    }

    public Map<String, Object> getOptions() {
        return options;
    }

    public void setOptions(Map<String, Object> options) {
        this.options = options;
    }

    public List<Data2DColumn> getSavedColumns() {
        return savedColumns;
    }

    public void setSavedColumns(List<Data2DColumn> savedColumns) {
        this.savedColumns = savedColumns;
    }

    public SingletonTask getTask() {
        return task;
    }

    public Data2D_Attributes setTask(SingletonTask task) {
        this.task = task;
        return this;
    }

    public SingletonTask getBackgroundTask() {
        return backgroundTask;
    }

    public void setBackgroundTask(SingletonTask backgroundTask) {
        this.backgroundTask = backgroundTask;
    }

}
