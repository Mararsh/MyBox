package mara.mybox.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.Data2Column;
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
    protected List<Data2Column> columns;
    protected long dataNumber, pagesNumber, pageSize;
    protected long currentPage, startRowOfCurrentPage, endRowOfCurrentPage;   // 0-based, excluded end
    protected ObservableList<List<String>> pageData;
    protected SimpleBooleanProperty pageDataLoadedNotify, pageDataChangedNotify;
    protected boolean totalRead, pageDataChanged;

    public Data2D() {
        pageSize = 50;
        pageDataLoadedNotify = new SimpleBooleanProperty(false);
        pageDataChangedNotify = new SimpleBooleanProperty(false);
        resetData();
    }

    /*
        abstract
     */
    public abstract boolean readDataDefinition(SingletonTask<Void> task);

    public abstract boolean readColumns(SingletonTask<Void> task);

    public abstract boolean readTotal(SingletonTask<Void> task);

    public abstract boolean readPageData(SingletonTask<Void> task);

    /*
        page data
     */
    public final void resetData() {
        resetDefinition();
        columns = null;
        dataNumber = 0;
        pagesNumber = 1;
        currentPage = startRowOfCurrentPage = endRowOfCurrentPage = 0;
        totalRead = false;
        pageData = FXCollections.observableArrayList();
        setPageDataChanged(false);
    }

    public void newData() {
        resetData();
        for (int r = 0; r < 3; r++) {
            List<String> row = new ArrayList<>();
            for (int col = 0; col < 3; col++) {
                row.add(defaultColValue());
            }
            pageData.add(row);
        }
        dataNumber = 3;
        pagesNumber = 1;
        currentPage = startRowOfCurrentPage = 0;
        endRowOfCurrentPage = 3;
        totalRead = false;
        setPageDataChanged(false);
    }

    public void loadPageData(List<List<String>> rows, int colsNumber) {
        pageData = FXCollections.observableArrayList();
        List<List<String>> data = new ArrayList<>();
        if (!rows.isEmpty() && colsNumber > 0) {
            for (int row = 0; row < rows.size(); row++) {
                List<String> sourceRow = rows.get(row);
                List<String> pageRow = new ArrayList<>();
                int size = sourceRow.size();
                for (int col = 0; col < Math.min(size, colsNumber); col++) {
                    pageRow.add(sourceRow.get(col));
                }
                for (int col = pageRow.size(); col < colsNumber; col++) {
                    pageRow.add(defaultColValue());
                }
                data.add(pageRow);
            }
            pageData.addAll(data);
        }
        endRowOfCurrentPage = startRowOfCurrentPage + data.size();
        setPageDataChanged(true);
    }

    public void loadPageData(List<List<String>> data, List<Data2Column> dataColumns) {
        resetData();
        columns = dataColumns;
        if (data != null) {
            pageData.addAll(data);
            endRowOfCurrentPage = data.size();
        }
        setPageDataChanged(true);
    }

    public int pageRowsNumber() {
        return pageData == null ? 0 : pageData.size();
    }

    public int pageColsNumber() {
        return pageData == null || pageData.isEmpty() ? 0 : pageData.get(0).size();
    }

    public String cell(int row, int col) {
        String value = null;
        try {
            value = pageData.get(row).get(col);
            Data2Column column = column(col);
            if (value != null && column.isNumberType()) {
                value = value.replaceAll(",", "");
            }
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

    public List<String> rowList(int row) {
        if (pageData == null || row < 0 || row > pageData.size() - 1) {
            return null;
        }
        List<String> values = new ArrayList<>();
        try {
            for (int col = 0; col < pageData.get(row).size(); col++) {
                values.add(cell(row, col));
            }
        } catch (Exception e) {
        }
        return values;
    }

    public long pageEnd() {
        return startRowOfCurrentPage + (pageData == null ? 0 : pageData.size());
    }

    public void firstPage() {
        currentPage = startRowOfCurrentPage = endRowOfCurrentPage = 0;
    }

    public boolean isMutiplePages() {
        return pagesNumber > 1;
    }

    public boolean needSavePageData() {
        return isMutiplePages() && isPageDataChanged();
    }

    public boolean isNew() {
        return !totalRead;
    }

    public void setPageDataChanged(boolean pageDataChanged) {
        this.pageDataChanged = pageDataChanged;
        notifyPageDataLoaded();
    }

    public void notifyPageDataLoaded() {
        pageDataLoadedNotify.set(!pageDataLoadedNotify.get());
    }

    public void notifyPageDataChanged() {
        pageDataChangedNotify.set(!pageDataChangedNotify.get());
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

    public Data2Column column(int col) {
        try {
            return columns.get(col);
        } catch (Exception e) {
            return null;
        }
    }

    public List<String> columnNames() {
        try {
            List<String> names = new ArrayList<>();
            for (Data2Column column : columns) {
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
        return isValid() && (pageRowsNumber() > 0 || dataNumber > 0);
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

    public List<Data2Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Data2Column> columns) {
        this.columns = columns;
    }

    public long getDataNumber() {
        return dataNumber;
    }

    public void setDataNumber(long dataNumber) {
        this.dataNumber = dataNumber;
    }

    public long getPagesNumber() {
        return pagesNumber;
    }

    public void setPagesNumber(long pagesNumber) {
        this.pagesNumber = pagesNumber;
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(long pageSize) {
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

    public List<List<String>> getPageData() {
        return pageData;
    }

    public void setPageData(ObservableList<List<String>> pageData) {
        this.pageData = pageData;
    }

    public SimpleBooleanProperty getPageDataLoadedNotify() {
        return pageDataLoadedNotify;
    }

    public void setPageDataLoadedNotify(SimpleBooleanProperty pageDataLoadedNotify) {
        this.pageDataLoadedNotify = pageDataLoadedNotify;
    }

    public SimpleBooleanProperty getPageDataChangedNotify() {
        return pageDataChangedNotify;
    }

    public void setPageDataChangedNotify(SimpleBooleanProperty pageDataChangedNotify) {
        this.pageDataChangedNotify = pageDataChangedNotify;
    }

    public boolean isPageDataChanged() {
        return pageDataChanged;
    }

    public boolean isTotalRead() {
        return totalRead;
    }

    public void setTotalRead(boolean totalRead) {
        this.totalRead = totalRead;
    }

}
