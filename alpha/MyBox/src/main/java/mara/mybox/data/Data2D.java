package mara.mybox.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.DataDefinition;
import mara.mybox.db.table.TableDataColumn;
import mara.mybox.db.table.TableDataDefinition;
import mara.mybox.fxml.SingletonTask;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public abstract class Data2D {

    protected Type type;
    protected TableDataDefinition tableDataDefinition;
    protected TableDataColumn tableDataColumn;
    protected DataDefinition definition;
    protected List<ColumnDefinition> columns;
    protected long dataNumber, pagesNumber, pageSize;
    protected long currentPage, startRowOfCurrentPage, endRowOfCurrentPage;   // 0-based, excluded end
    protected ObservableList<List<String>> pageData;
    protected SimpleBooleanProperty pageDataLoadedNotify, pageDataChangedNotify;
    protected boolean totalRead, pageDataChanged;

    public static enum Type {
        InternalTable, DataFileCSV, DataFileExcel, DataFileText, Matrix, UserTable, DataClipboard, Unknown
    }

    public Data2D() {
        initData();
        type = Type.DataFileCSV;
        definition = null;
        pageSize = 50;
        pageDataLoadedNotify = new SimpleBooleanProperty(false);
        pageDataChangedNotify = new SimpleBooleanProperty(false);
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
    public final void initData() {
        columns = null;
        dataNumber = 0;
        pagesNumber = 1;
        currentPage = startRowOfCurrentPage = endRowOfCurrentPage = 0;
        totalRead = false;
        pageData = FXCollections.observableArrayList();
        setPageDataChanged(false);
    }

    public void newData() {
        initData();
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

    public void loadPageData(List<List<String>> data, List<ColumnDefinition> dataColumns) {
        definition = new DataDefinition();
        columns = dataColumns;
        currentPage = startRowOfCurrentPage = endRowOfCurrentPage = 0;
        pageData = FXCollections.observableArrayList();
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
            ColumnDefinition column = column(col);
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

    public ColumnDefinition column(int col) {
        try {
            return columns.get(col);
        } catch (Exception e) {
            return null;
        }
    }

    public List<String> columnNames() {
        try {
            List<String> names = new ArrayList<>();
            for (ColumnDefinition column : columns) {
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
        File file = getFile();
        if (file != null) {
            return file.getAbsolutePath();
        } else if (definition != null) {
            return definition.getDataName();
        } else {
            return "";
        }
    }

    public String random(Random random, int col) {
        try {
            if (definition != null) {
                return column(col).random(random, definition.getMaxRandom(), definition.getScale());
            } else {
                return column(col).random(random, 10000, (short) 2);
            }
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isValid() {
        return definition != null && columns != null && !columns.isEmpty();
    }

    public boolean hasData() {
        return isValid() && (pageRowsNumber() > 0 || dataNumber > 0);
    }

    public File getFile() {
        if (definition == null) {
            return null;
        }
        return definition.getFile();
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
    public TableDataDefinition getTableDataDefinition() {
        return tableDataDefinition;
    }

    public void setTableDataDefinition(TableDataDefinition tableDataDefinition) {
        this.tableDataDefinition = tableDataDefinition;
    }

    public DataDefinition getDefinition() {
        return definition;
    }

    public void setDefinition(DataDefinition definition) {
        this.definition = definition;
    }

    public TableDataColumn getTableDataColumn() {
        return tableDataColumn;
    }

    public void setTableDataColumn(TableDataColumn tableDataColumn) {
        this.tableDataColumn = tableDataColumn;
    }

    public List<ColumnDefinition> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnDefinition> columns) {
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

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
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
