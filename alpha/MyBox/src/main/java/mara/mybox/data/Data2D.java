package mara.mybox.data;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import mara.mybox.controller.BaseController;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import org.apache.commons.csv.CSVRecord;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public abstract class Data2D extends Data2DDefinition {

    protected TableData2DDefinition tableData2DDefinition;
    protected TableData2DColumn tableData2DColumn;
    protected List<Data2DColumn> columns;
    protected List<Data2DColumn> savedColumns;
    protected int pageSize, newColumnIndex;
    protected long dataSize, pagesNumber;
    protected long currentPage, startRowOfCurrentPage, endRowOfCurrentPage;   // 0-based, excluded end
    protected TableView<List<String>> tableView;
    protected boolean userSavedDataDefinition, tableChanged;
    protected SingletonTask task, backgroundTask;
    protected String error;

    public Data2D() {
        tableData2DDefinition = new TableData2DDefinition();
        tableData2DColumn = new TableData2DColumn();
        pageSize = 50;
        userSavedDataDefinition = true;
        resetData();
    }

    public final void resetData() {
        resetDefinition();
        dataSize = 0;
        pagesNumber = 1;
        currentPage = startRowOfCurrentPage = endRowOfCurrentPage = 0;
        columns = null;
        savedColumns = null;
        newColumnIndex = -1;
        tableChanged = false;
    }

    public void initFile(File file) {
        resetData();
        this.file = file;
        if (isTmpFile()) {
            hasHeader = false;
            userSavedDataDefinition = false;
        }
    }

    public void load(Data2D d) {
        try {
            if (d == null) {
                return;
            }
            super.load(d);
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
            tableView = d.tableView;
            tableChanged = d.tableChanged;
            task = d.task;
            backgroundTask = d.backgroundTask;
            error = d.error;
        } catch (Exception e) {
        }
    }

    @Override
    public Data2D cloneAll() {
        try {
            Data2D newData = (Data2D) super.clone();
            newData.load(this);
            return newData;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }


    /*
        abstract
     */
    public abstract long readDataDefinition();

    public abstract List<String> readColumns();

    public abstract long readTotal();

    public abstract List<List<String>> readPageData();

    public abstract boolean savePageData(Data2D targetData);

    public abstract File tmpFile(List<String> columns, List<List<String>> data);

    /*
        page data
     */
    public boolean isMutiplePages() {
        return pagesNumber > 1;
    }

    public String pageCell(List<String> rowValues, int pageCol) {
        try {
            return rowValues.get(dataCol(columns.get(pageCol).getIndex()));
        } catch (Exception e) {
            return null;
        }
    }

    public int dataCol(int index) {
        try {
            for (int i = 2; i < tableView.getColumns().size(); i++) {
                TableColumn tableColumn = tableView.getColumns().get(i);
                if (tableColumn.getUserData() != null && index == (int) tableColumn.getUserData()) {
                    return i - 1;
                }
            }
        } catch (Exception e) {
        }
        return -1;
    }

    public List<String> pageRow(int row) {
        try {
            return pageRow(tableView.getItems().get(row));
        } catch (Exception e) {
            return pageRow(null);
        }
    }

    public List<String> pageRow(List<String> rowValues) {
        try {
            if (rowValues == null) {
                return null;
            }
            List<String> values = new ArrayList<>();
            for (int i = 0; i < columns.size(); i++) {
                values.add(pageCell(rowValues, i));
            }
            return values;
        } catch (Exception e) {
            return null;
        }
    }

    public List<String> recordRow(CSVRecord record) {
        try {
            if (record == null) {
                return null;
            }
            List<String> row = new ArrayList<>();
            int len = record.size();
            for (int i = 0; i < columns.size(); i++) {
                String value = null;
                int index = columns.get(i).getIndex();
                if (index >= 0 && index < len) {
                    value = record.get(index);
                }
                row.add(value);
            }
            return row;
        } catch (Exception e) {
            return null;
        }
    }

    public List<String> newRow() {
        try {
            List<String> newRow = new ArrayList<>();
            newRow.add("-1");
            for (int i = 0; i < columns.size(); i++) {
                newRow.add(defaultColValue());
            }
            return newRow;
        } catch (Exception e) {
            return pageRow(null);
        }
    }

    public List<String> copyRow(List<String> row) {
        if (row == null) {
            return null;
        }
        List<String> newRow = new ArrayList<>();
        newRow.addAll(row);
        newRow.set(0, "-1");
        return newRow;
    }

    /*
        table
     */
    public void setTableChanged(boolean tableChanged) {
        this.tableChanged = tableChanged;
    }

    public int tableRowsNumber() {
        return tableView == null ? 0 : tableView.getItems().size();
    }

    public int tableColsNumber() {
        return columns == null ? 0 : columns.size();
    }

    public List<String> tableRow(int row) {
        if (tableView == null || row < 0 || row > tableView.getItems().size() - 1) {
            return null;
        }
        try {
            return tableView.getItems().get(row);
        } catch (Exception e) {
            return null;
        }
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
        return message("Column");
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

    public int newColumnIndex() {
        return --newColumnIndex;
    }

    /*
        attributes
     */
    public Data2DDefinition queryDefinition(Connection conn) {
        return tableData2DDefinition.queryName(conn, type, dataName);
    }

    public boolean isMatrix() {
        return type == Type.Matrix;
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
        file
     */
    public boolean isFile() {
        return type == Type.DataFileCSV || type == Type.DataFileExcel
                || type == Type.DataFileText || type == Type.DataClipboard;
    }

    public boolean isExcel() {
        return type == Type.DataFileExcel;
    }

    public List<List<String>> tmpData() {
        Random random = new Random();
        List<List<String>> data = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            List<String> row = new ArrayList<>();
            for (int j = 0; j < 3; j++) {
                row.add((char) ('a' + random.nextInt(25)) + "");
            }
            data.add(row);
        }
        return data;
    }

    public File tmpFile() {
        return tmpFile(null, tmpData());
    }

    public boolean isTmpFile() {
        return file == null || file.getAbsolutePath().startsWith(AppVariables.MyBoxTempPath.getAbsolutePath());
    }

    public void open(File file) {
        if (file == null) {
            return;
        }
        String fxml;
        switch (type) {
            case DataFileCSV:
            case DataClipboard:
                fxml = Fxmls.DataFileCSVFxml;
                break;
            case DataFileExcel:
                fxml = Fxmls.DataFileExcelFxml;
                break;
            case DataFileText:
                fxml = Fxmls.TextEditorFxml;
                break;
            default:
                return;
        }
        BaseController controller = WindowTools.openStage(fxml);
        controller.sourceFileChanged(file);
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
                data = new DataClipboard();
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

    public boolean isTableChanged() {
        return tableChanged;
    }

    public TableView<List<String>> getTableView() {
        return tableView;
    }

    public void setTableView(TableView<List<String>> tableView) {
        this.tableView = tableView;
    }

    public boolean isUserSavedDataDefinition() {
        return userSavedDataDefinition;
    }

    public void setUserSavedDataDefinition(boolean userSavedDataDefinition) {
        this.userSavedDataDefinition = userSavedDataDefinition;
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
