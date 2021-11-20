package mara.mybox.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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

    protected TableData2DDefinition tableData2DDefinition;
    protected TableData2DColumn tableData2DColumn;
    protected List<Data2DColumn> columns;
    protected int pageSize;
    protected long dataSize, pagesNumber;
    protected long currentPage, startRowOfCurrentPage, endRowOfCurrentPage;   // 0-based, excluded end
    protected TableView<List<String>> tableView;
    protected final SimpleBooleanProperty pageLoadedNotify, tableChangedNotify;
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

    /*
        page data
     */
    public final void resetData() {
        resetDefinition();
        dataSize = 0;
        pagesNumber = 1;
        currentPage = startRowOfCurrentPage = endRowOfCurrentPage = 0;
        columns = null;
        totalRead = tableChanged = false;
        if (tableView != null) {
            tableView.getItems().clear();
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

    public String pageCell(List<String> tableRowValues, int pageCol) {
        try {
            int tableCol = index(pageCol);
            if (tableCol < 0) {
                return defaultColValue();
            } else {
                String value = tableRowValues.get(tableCol);
                if (value == null) {
                    return defaultColValue();
                } else {
                    return value;
                }
            }
        } catch (Exception e) {
            return defaultColValue();
        }
    }

    public int index(int pageCol) {
        try {
            int index = columns.get(pageCol).getIndex();
            for (int i = 1; i < tableView.getColumns().size(); i++) {
                TableColumn tableColumn = tableView.getColumns().get(i);
                if (tableColumn.getUserData() != null && index == (int) tableColumn.getUserData()) {
                    return i - 1;
                }
            }
        } catch (Exception e) {
        }
        return -1;
    }

    public List<Integer> indices() {
        try {
            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < columns.size(); i++) {
                indices.add(index(i));
            }
            return indices;
        } catch (Exception e) {
            return null;
        }
    }

    public List<String> pageRow(int row) {
        if (tableView == null || row < 0 || row > tableView.getItems().size() - 1) {
            return null;
        }
        try {
            List<String> tableRowValues = tableView.getItems().get(row);
            List<String> values = new ArrayList<>();
            for (int pageCol = 0; pageCol < columns.size(); pageCol++) {
                values.add(pageCell(tableRowValues, pageCol));
            }
            return values;
        } catch (Exception e) {
            return null;
        }
    }


    /*
        table
     */
    public void setTableChanged(boolean tableChanged) {
        this.tableChanged = tableChanged;
        notifyTableChanged();
    }

    public int tableRowsNumber() {
        return tableView == null ? 0 : tableView.getItems().size();
    }

    public int tableColsNumber() {
        return columns == null ? 0 : columns.size();
    }

    public List<String> tableRowRaw(int row) {
        if (tableView == null || row < 0 || row > tableView.getItems().size() - 1) {
            return null;
        }
        try {
            return tableView.getItems().get(row);
        } catch (Exception e) {
            return null;
        }
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
            return rowNames(tableView.getItems().size());
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
        return isValid() && tableView.getItems() != null && !tableView.getItems().isEmpty();
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

    public SimpleBooleanProperty getTableChangedNotify() {
        return tableChangedNotify;
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

    public TableView<List<String>> getTableView() {
        return tableView;
    }

    public void setTableView(TableView<List<String>> tableView) {
        this.tableView = tableView;
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
