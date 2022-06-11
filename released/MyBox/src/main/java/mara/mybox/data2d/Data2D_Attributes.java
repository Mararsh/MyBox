package mara.mybox.data2d;

import java.util.List;
import java.util.Map;
import javafx.scene.web.WebEngine;
import mara.mybox.controller.ControlData2DLoad;
import mara.mybox.data.FindReplaceString;
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

    protected TableData2DDefinition tableData2DDefinition;
    protected TableData2DColumn tableData2DColumn;
    protected TableData2DStyle tableData2DStyle;
    protected List<Data2DColumn> columns, savedColumns;
    protected Map<String, Object> options;
    protected int pageSize, newColumnIndex;
    protected long dataSize, pagesNumber;
    protected long currentPage, startRowOfCurrentPage, endRowOfCurrentPage;   // 0-based, excluded end
    protected List<Data2DStyle> styles;
    protected ControlData2DLoad loadController;
    protected boolean tableChanged, filterReversed, filterPassed;
    protected SingletonTask task, backgroundTask;
    protected String error, rowFilter;
    protected final Object lock = new Object();
    public WebEngine webEngine;
    public FindReplaceString findReplace;
    public String expressionResult;

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
        error = null;
        rowFilter = null;
        webEngine = null;
        findReplace = null;
        expressionResult = null;
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
            webEngine = d.webEngine;
            findReplace = d.findReplace;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void cloneAttributes(Data2D_Attributes d) {
        try {
            if (d == null) {
                return;
            }
            super.cloneAttributes(d);
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
            rowFilter = d.rowFilter;
            expressionResult = d.expressionResult;
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

    public List<Data2DStyle> getStyles() {
        return styles;
    }

    public void setStyles(List<Data2DStyle> styles) {
        this.styles = styles;
    }

    public String getRowFilter() {
        return rowFilter;
    }

    public void setRowFilter(String rowFilter) {
        this.rowFilter = rowFilter;
    }

    public boolean isFilterReversed() {
        return filterReversed;
    }

    public void setFilterReversed(boolean filterReversed) {
        this.filterReversed = filterReversed;
    }

    public WebEngine getWebEngine() {
        return webEngine;
    }

    public void setWebEngine(WebEngine webEngine) {
        this.webEngine = webEngine;
    }

    public FindReplaceString getFindReplace() {
        return findReplace;
    }

    public void setFindReplace(FindReplaceString findReplace) {
        this.findReplace = findReplace;
    }

    public String getExpressionResult() {
        return expressionResult;
    }

    public void setExpressionResult(String expressionResult) {
        this.expressionResult = expressionResult;
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
