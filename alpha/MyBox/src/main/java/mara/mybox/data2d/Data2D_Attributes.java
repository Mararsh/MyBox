package mara.mybox.data2d;

import java.util.List;
import javafx.collections.ObservableList;
import mara.mybox.controller.BaseController;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.Data2DStyle;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.db.table.TableData2DStyle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;

/**
 * @Author Marai
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public abstract class Data2D_Attributes extends Data2DDefinition {

    public BaseController controller;
    public TableData2DDefinition tableData2DDefinition;
    public TableData2DColumn tableData2DColumn;
    public TableData2DStyle tableData2DStyle;
    public List<Data2DColumn> columns, savedColumns;
    public int newColumnIndex;
    public List<Data2DStyle> styles;
    public DataFilter filter;
    public ObservableList<List<String>> pageData;
    public boolean tableChanged, dataLoaded;
    public FxTask task, backgroundTask;
    public String error;

    public enum TargetType {
        CSV, Excel, Text, Matrix, DatabaseTable, SystemClipboard, MyBoxClipboard,
        JSON, XML, HTML, PDF, Replace, Insert, Append
    }

    public Data2D_Attributes() {
        tableData2DDefinition = new TableData2DDefinition();
        tableData2DColumn = new TableData2DColumn();
        tableData2DStyle = new TableData2DStyle();
        styles = null;
        initData();
    }

    private void initData() {
        resetDefinition();
        columns = null;
        savedColumns = null;
        newColumnIndex = -1;
        dataLoaded = true;
        tableChanged = false;
        styles = null;
        filter = null;
        error = null;
        pageData = null;
        task = null;
        backgroundTask = null;
    }

    public void resetData() {
        initData();
    }

    public void cloneDataFrom(Data2D_Attributes d) {
        try {
            dataID = d.getDataID();
            dataType = d.getType();
            copyAttributesFrom(d);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void copyAttributesFrom(Data2D_Attributes d) {
        try {
            super.copyAllAttributes(d);
            copyTaskAttributes(d);
            copyPageAttributes(d);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void copyTaskAttributes(Data2D_Attributes d) {
        try {
            if (d == null) {
                return;
            }
            task = d.task;
            backgroundTask = d.backgroundTask;
            error = d.error;
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void copyPageAttributes(Data2D_Attributes d) {
        try {
            if (d == null) {
                return;
            }
            copyDataAttributes(d);
            pageData = d.pageData;
            tableData2DDefinition = d.tableData2DDefinition;
            tableData2DColumn = d.tableData2DColumn;
            columns = d.columns;
            savedColumns = d.savedColumns;
            newColumnIndex = d.newColumnIndex;
            styles = d.styles;
            pagination.copyFrom(d.pagination);
            tableChanged = d.tableChanged;
            dataLoaded = d.dataLoaded;
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    /*
        get/set
     */
    public BaseController getController() {
        return controller;
    }

    public Data2D setController(BaseController controller) {
        this.controller = controller;
        return (Data2D) this;
    }

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

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public boolean isTableChanged() {
        return tableChanged;
    }

    public void setTableChanged(boolean tableChanged) {
        this.tableChanged = tableChanged;
    }

    public ObservableList<List<String>> getPageData() {
        return pageData;
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

    public FxTask getTask() {
        return task;
    }

    public Data2D_Attributes setTask(FxTask task) {
        this.task = task;
        return this;
    }

    public FxTask getBackgroundTask() {
        return backgroundTask;
    }

    public void setBackgroundTask(FxTask backgroundTask) {
        this.backgroundTask = backgroundTask;
    }

}
