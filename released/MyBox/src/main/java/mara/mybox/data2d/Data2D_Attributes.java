package mara.mybox.data2d;

import java.util.List;
import java.util.Map;
import mara.mybox.controller.ControlData2DLoad;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.Data2DStyle;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.db.table.TableData2DStyle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public abstract class Data2D_Attributes extends Data2DDefinition {

    public TableData2DDefinition tableData2DDefinition;
    public TableData2DColumn tableData2DColumn;
    public TableData2DStyle tableData2DStyle;
    public List<Data2DColumn> columns, savedColumns;
    public Map<String, Object> options;
    public int pageSize, newColumnIndex;
    public long dataSize, pagesNumber;
    public long currentPage, startRowOfCurrentPage, endRowOfCurrentPage;   // 0-based, excluded end
    public List<Data2DStyle> styles;
    public DataFilter filter;
    public ControlData2DLoad loadController;
    public boolean tableChanged;
    public SingletonTask task, backgroundTask;
    public String error;

    public Data2D_Attributes() {
        tableData2DDefinition = new TableData2DDefinition();
        tableData2DColumn = new TableData2DColumn();
        tableData2DStyle = new TableData2DStyle();
        pageSize = 50;
        styles = null;
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
        styles = null;
        filter = null;
        error = null;
        loadController = null;
        task = null;
        backgroundTask = null;
    }

    public void resetData() {
        initData();
    }

    public void cloneAll(Data2D_Attributes d) {
        try {
            cloneBase(d);
            cloneAttributes(d);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void cloneBase(Data2D_Attributes d) {
        try {
            if (d == null) {
                return;
            }
            super.cloneBase(d);
            task = d.task;
            backgroundTask = d.backgroundTask;
            error = d.error;
            options = d.options;
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void cloneAttributes(Data2D_Attributes d) {
        try {
            if (d == null) {
                return;
            }
            cloneDefinitionAttributes(d);
            loadController = d.loadController;
            tableData2DDefinition = d.tableData2DDefinition;
            tableData2DColumn = d.tableData2DColumn;
            columns = d.columns;
            savedColumns = d.savedColumns;
            newColumnIndex = d.newColumnIndex;
            styles = d.styles;
            dataSize = d.dataSize;
            pageSize = d.pageSize;
            pagesNumber = d.pagesNumber;
            currentPage = d.currentPage;
            startRowOfCurrentPage = d.startRowOfCurrentPage;
            endRowOfCurrentPage = d.endRowOfCurrentPage;
            tableChanged = d.tableChanged;
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

    public TableData2DStyle getTableData2DStyle() {
        return tableData2DStyle;
    }

    public void setTableData2DStyle(TableData2DStyle tableData2DStyle) {
        this.tableData2DStyle = tableData2DStyle;
    }

    public List<Data2DColumn> getColumns() {
        return columns;
    }

    public Data2D_Attributes setColumns(List<Data2DColumn> columns) {
        this.columns = columns;
        return this;
    }

    public long getDataSize() {
        return dataSize;
    }

    public Data2D_Attributes setDataSize(long dataSize) {
        this.dataSize = dataSize;
        return this;
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

    public List<Data2DStyle> getStyles() {
        return styles;
    }

    public DataFilter getFilter() {
        return filter;
    }

    public void setFilter(DataFilter filter) {
        this.filter = filter;
    }

    public Data2D_Attributes setStyles(List<Data2DStyle> styles) {
        this.styles = styles;
        return this;
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
