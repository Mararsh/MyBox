package mara.mybox.data;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javafx.scene.control.TableView;
import mara.mybox.controller.BaseController;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public abstract class Data2D extends Data2DDefinition {

    protected TableData2DDefinition tableData2DDefinition;
    protected TableData2DColumn tableData2DColumn;
    protected List<Data2DColumn> columns, savedColumns;
    protected Map<String, Object> options;
    protected int pageSize, newColumnIndex;
    protected long dataSize, pagesNumber;
    protected long currentPage, startRowOfCurrentPage, endRowOfCurrentPage;   // 0-based, excluded end
    protected TableView<List<String>> tableView;
    protected boolean tableChanged;
    protected SingletonTask task, backgroundTask;
    protected String error;

    /*
        abstract
     */
    public abstract Data2DDefinition queryDefinition(Connection conn);

    public abstract void applyOptions();

    public abstract List<String> readColumns();

    public abstract long readTotal();

    public abstract List<List<String>> readPageData();

    public abstract boolean savePageData(Data2D targetData);

    public abstract File tmpFile(List<String> columns, List<List<String>> data);

    /*
        class
     */
    public Data2D() {
        tableData2DDefinition = new TableData2DDefinition();
        tableData2DColumn = new TableData2DColumn();
        pageSize = 50;
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
        options = null;
    }

    @Override
    public Data2D cloneAll() {
        try {
            Data2D newData = (Data2D) super.clone();
            newData.cloneAll(this);
            return newData;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public void cloneAll(Data2D d) {
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
            tableView = d.tableView;
            tableChanged = d.tableChanged;
            task = d.task;
            backgroundTask = d.backgroundTask;
            error = d.error;
            options = d.options;
        } catch (Exception e) {
        }
    }

    @Override
    public boolean isValid() {
        return super.isValid() && columns != null && !columns.isEmpty();
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
            case CSV:
                data = new DataFileCSV();
                break;
            case Excel:
                data = new DataFileExcel();
                break;
            case Text:
                data = new DataFileText();
                break;
            case Matrix:
                data = new DataMatrix();
                break;
            case Clipboard:
                data = new DataClipboard();
                break;
            default:
                return null;
        }
        data.setType(type);
        return data;
    }


    /*
        file
     */
    public void initFile(File file) {
        resetData();
        this.file = file;
    }

    public List<List<String>> tmpData() {
        Random random = new Random();
        List<List<String>> data = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            List<String> row = new ArrayList<>();
            for (int j = 0; j < 3; j++) {
                if (type == Type.Matrix) {
                    row.add(randomDouble(random));
                } else {
                    row.add(randomString(random));
                }
            }
            data.add(row);
        }
        return data;
    }

    public File tmpFile() {
        List<String> cols = new ArrayList<>();
        for (int c = 1; c <= 3; c++) {
            cols.add(this.colPrefix() + c);
        }
        return tmpFile(cols, tmpData());
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
            case CSV:
            case Clipboard:
                fxml = Fxmls.DataFileCSVFxml;
                break;
            case Excel:
                fxml = Fxmls.DataFileExcelFxml;
                break;
            default:
                fxml = Fxmls.DataFileTextFxml;
        }
        BaseController controller = WindowTools.openStage(fxml);
        controller.sourceFileChanged(file);
    }

    public boolean isMutiplePages() {
        return pagesNumber > 1;
    }

    // file columns are not necessary in order of columns definition.
    // column's index remembers the order of columns
    // when index is less than 0, it is new column
    public List<String> fileRow(List<String> fileRow) {
        try {
            if (fileRow == null) {
                return null;
            }
            List<String> row = new ArrayList<>();
            int len = fileRow.size();
            for (int i = 0; i < columns.size(); i++) {
                String value = null;
                int index = columns.get(i).getIndex();
                if (index >= 0 && index < len) {
                    value = fileRow.get(index);
                }
                row.add(value);
            }
            return row;
        } catch (Exception e) {
            return null;
        }
    }

    /*
        database
     */
    public long readDataDefinition() {
        d2did = -1;
        if (isTmpFile()) {
            checkAttributes();
            return -1;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            Data2DDefinition definition = queryDefinition(conn);
            if (definition != null) {
                cloneAll(definition);
            }
            applyOptions();
            checkAttributes();
            if (definition == null) {
                definition = tableData2DDefinition.insertData(conn, this);
                conn.commit();
                d2did = definition.getD2did();
            } else {
                tableData2DDefinition.updateData(conn, this);
                conn.commit();
                d2did = definition.getD2did();
                savedColumns = tableData2DColumn.read(conn, d2did);
            }
            options = null;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.console(e);
            return -1;
        }
        return d2did;
    }

    public void checkAttributes() {
    }

    public String randomDouble(Random random) {
        return DoubleTools.format(DoubleTools.random(random, maxRandom), scale);
    }

    public String randomString(Random random) {
        return (char) ('a' + random.nextInt(25)) + "";
    }

    /*
        table data
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

    // Column's index, instead of column name or table index, is the key to determine the column.
    public int tableCol(int index) {
        try {
            for (int i = 0; i < columns.size(); i++) {
                if (index == columns.get(i).getIndex()) {
                    return i;
                }
            }
        } catch (Exception e) {
        }
        return -1;
    }

    public int tableCol(String name) {
        try {
            for (int i = 0; i < columns.size(); i++) {
                if (name.equals(columns.get(i).getName())) {
                    return i;
                }
            }
        } catch (Exception e) {
        }
        return -1;
    }

    public List<String> tableRow(int row) {
        try {
            List<String> values = tableView.getItems().get(row);
            return values.subList(1, values.size());
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
            return null;
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

    public boolean hasData() {
        return isValid() && tableView.getItems() != null && !tableView.getItems().isEmpty();
    }


    /*
        table view
     */
    public List<String> tableViewRow(int row) {
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

    public List<String> editableColumnNames() {
        if (columns == null) {
            return null;
        }
        List<String> names = new ArrayList<>();
        for (Data2DColumn col : columns) {
            if (col.isEditable()) {
                names.add(col.getName());
            }
        }
        return names;
    }

    /*
        attributes
     */
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
