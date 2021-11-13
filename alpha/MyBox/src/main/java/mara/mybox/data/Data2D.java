package mara.mybox.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import mara.mybox.controller.ControlData2DEditTable;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.fxml.SingletonTask;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public abstract class Data2D extends Data2DDefinition {

    protected ControlData2DEditTable tableController;
    protected TableData2DDefinition tableData2DDefinition;
    protected TableData2DColumn tableData2DColumn;
    protected List<Data2DColumn> columns;
    protected int pageSize;
    protected long dataSize, pagesNumber;
    protected long currentPage, startRowOfCurrentPage, endRowOfCurrentPage;   // 0-based, excluded end
    protected ObservableList<List<String>> tableData;
    protected SimpleBooleanProperty pageLoadedNotify, tableChangedNotify;
    protected boolean totalRead, tableChanged;
    protected SingletonTask task, backgroundTask;
    protected String error;

    public Data2D() {
        pageSize = 50;
        pageLoadedNotify = new SimpleBooleanProperty(false);
        tableChangedNotify = new SimpleBooleanProperty(false);
        resetData();
    }

    /*
        abstract
     */
    public abstract long readDataDefinition();

    public abstract List<String> readColumns();

    public abstract long readTotal();

    public abstract List<List<String>> readPageData();

    public abstract boolean savePageData();

    /*
        page data
     */
    public int pageRowsNumber() {
        return tableData == null ? 0 : tableData.size();
    }

    public int pageColsNumber() {
        return tableData == null || tableData.isEmpty() ? 0 : tableData.get(0).size();
    }

    public final void resetData() {
        resetDefinition();
        columns = null;
        dataSize = 0;
        pagesNumber = 1;
        currentPage = startRowOfCurrentPage = endRowOfCurrentPage = 0;
        totalRead = tableChanged = false;
        if (tableData != null) {
            tableData.clear();
        }
    }

    public void newData() {
        resetData();
//        for (int r = 0; r < 3; r++) {
//            List<String> row = new HashMap<>();
//            for (int col = 0; col < 3; col++) {
//                row.add(defaultColValue());
//            }
//            tableData.add(row);
//        }
        dataSize = 3;
        pagesNumber = 1;
        currentPage = startRowOfCurrentPage = 0;
        endRowOfCurrentPage = 3;
        totalRead = false;
    }

    public boolean isMutiplePages() {
        return pagesNumber > 1;
    }

    public void notifyPageLoaded() {
        pageLoadedNotify.set(!pageLoadedNotify.get());
    }

    /*
        table
     */
    public void setTableChanged(boolean tableChanged) {
        this.tableChanged = tableChanged;
        notifyTableChanged();
    }

    public int tableRowsNumber() {
        return tableData == null ? 0 : tableData.size();
    }

    public int tableColsNumber() {
        return tableData == null || tableData.isEmpty() ? 0 : tableData.get(0).size() - 2;
    }

    public String cell(int row, int col) {
        String value = null;
        try {
            value = tableData.get(row).get(col + 1);
        } catch (Exception e) {
        }
        return value == null ? defaultColValue() : value;
    }

    public boolean isCellValid(int col, String value) {
        try {
            return column(col).validValue(value);
        } catch (Exception e) {
            return false;
        }
    }

    public List<String> row(int row) {
        if (tableData == null || row < 0 || row > tableData.size() - 1) {
            return null;
        }
        try {
            List<String> values = new ArrayList<>();
            for (int col = 0; col < columnsNumber(); col++) {
                values.add(cell(row, col));
            }
            return values;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean needSavePageData() {
        return isMutiplePages() && isTableChanged();
    }

    public void notifyTableChanged() {
        tableChangedNotify.set(!tableChangedNotify.get());
    }

    /*
        columns
     */
    public String defaultColValue() {
        return isMatrix() ? "0" : "";
    }

    public ColumnType defaultColumnType() {
        return isMatrix() ? ColumnType.Double : ColumnType.String;
    }

    public String colPrefix() {
        return "Column";
    }

    public boolean defaultColNotNull() {
        return isMatrix();
    }

    public Data2DColumn column(int col) {
        try {
            return columns.get(col);
        } catch (Exception e) {
            return null;
        }
    }

    public List<String> columnNames() {
        try {
            List<String> names = new ArrayList<>();
            for (Data2DColumn column : columns) {
                names.add(column.getName());
            }
            return names;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isColumnsValid() {
        return columns != null && !columns.isEmpty();
    }

    public int columnsNumber() {
        if (columns == null) {
            return 0;
        } else {
            return columns.size();
        }
    }

    /*
        attributes
     */
    public boolean isMatrix() {
        return type == Type.Matrix;
    }

    public boolean isFile() {
        return type == Type.DataFileCSV || type == Type.DataFileExcel || type == Type.DataFileText;
    }

    public String colName(int col) {
        try {
            return column(col).getName();
        } catch (Exception e) {
            return null;
        }
    }

    public String rowName(int row) {
        return message("Row") + (startRowOfCurrentPage + row + 1);
    }

    public List<String> rowNames() {
        try {
            return rowNames(tableData.size());
        } catch (Exception e) {
            return null;
        }
    }

    public List<String> rowNames(int end) {
        try {
            List<String> names = new ArrayList<>();
            for (int i = 0; i < end; i++) {
                names.add(rowName(i));
            }
            return names;
        } catch (Exception e) {
            return null;
        }
    }

    public String titleName() {
        if (file != null) {
            return file.getAbsolutePath();
        } else if (dataName != null) {
            return dataName;
        } else {
            return "";
        }
    }

    public String random(Random random, int col) {
        try {
            return column(col).random(random, maxRandom, scale);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean isValid() {
        return super.isValid() && columns != null && !columns.isEmpty();
    }

    public boolean hasData() {
        return isValid() && tableData != null && !tableData.isEmpty();
    }

    /*
        static
     */
    public static Data2D create(Type type) {
        if (type == null) {
            return null;
        }
        Data2D data;
        switch (type) {
            case DataFileCSV:
                data = new DataFileCSV();
                break;
            case DataFileExcel:
                data = new DataFileExcel();
                break;
            case DataFileText:
                data = new DataFileText();
                break;
            case Matrix:
                data = new DataFileCSV();
                break;
            case DataClipboard:
                data = new DataFileCSV();
                break;
            default:
                return null;
        }
        data.setType(type);
        return data;
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

    public SimpleBooleanProperty getPageLoadedNotify() {
        return pageLoadedNotify;
    }

    public void setPageLoadedNotify(SimpleBooleanProperty pageLoadedNotify) {
        this.pageLoadedNotify = pageLoadedNotify;
    }

    public ObservableList<List<String>> getTableData() {
        return tableData;
    }

    public void setTableData(ObservableList<List<String>> tableData) {
        this.tableData = tableData;
    }

    public SimpleBooleanProperty getTableChangedNotify() {
        return tableChangedNotify;
    }

    public void setTableChangedNotify(SimpleBooleanProperty tableChangedNotify) {
        this.tableChangedNotify = tableChangedNotify;
    }

    public boolean isTableChanged() {
        return tableChanged;
    }

    public boolean isTotalRead() {
        return totalRead;
    }

    public void setTotalRead(boolean totalRead) {
        this.totalRead = totalRead;
    }

    public ControlData2DEditTable getTableController() {
        return tableController;
    }

    public void setTableController(ControlData2DEditTable tableController) {
        this.tableController = tableController;
    }

    public SingletonTask getTask() {
        return task;
    }

    public void setTask(SingletonTask task) {
        this.task = task;
    }

    public SingletonTask getBackgroundTask() {
        return backgroundTask;
    }

    public void setBackgroundTask(SingletonTask backgroundTask) {
        this.backgroundTask = backgroundTask;
    }

}
