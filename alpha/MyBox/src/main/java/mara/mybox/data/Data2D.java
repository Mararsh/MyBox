package mara.mybox.data;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.ControlData2DEditTable;
import mara.mybox.controller.ControlDataConvert;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.DateTools;
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
    protected ControlData2DEditTable tableController;
    protected boolean tableChanged;
    protected double[][] matrix;
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

    public abstract boolean export(ControlDataConvert convertController, List<Integer> colIndices);


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
        matrix = null;
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
            tableController = d.tableController;
            tableChanged = d.tableChanged;
            task = d.task;
            backgroundTask = d.backgroundTask;
            error = d.error;
            options = d.options;
            matrix = d.matrix;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
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

    public boolean isDataFile() {
        return type == Type.CSV || type == Type.Excel || type == Type.Text;
    }

    public boolean isTmpFile() {
        return file == null
                || file.getAbsolutePath().startsWith(AppVariables.MyBoxTempPath.getAbsolutePath());
    }

    public boolean export(ControlDataConvert convertController, List<Integer> colIndices, List<String> dataRow) {
        try {
            if (convertController == null || colIndices == null || colIndices.isEmpty()
                    || dataRow == null || dataRow.isEmpty()) {
                return false;
            }
            List<String> exportRow = new ArrayList<>();
            for (Integer col : colIndices) {
                String value = null;
                if (col >= 0 && col < dataRow.size()) {
                    value = dataRow.get(col);
                }
                exportRow.add(value);
            }
            convertController.writeRow(exportRow);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
            return false;
        }
    }

    public List<List<String>> allRows(List<Integer> cols) {
        return null;
    }

    public DoubleStatistic[] statisticData(List<Integer> cols) {
        return null;
    }

    public File percentage(List<String> names, List<Integer> cols, boolean withValues) {
        return null;
    }

    /*
        matrix
     */
    public void initMatrix(double[][] matrix) {
        resetData();
        this.matrix = matrix;
    }

    public boolean isMatrix() {
        return type == Type.Matrix;
    }

    public boolean isSquareMatrix() {
        return type == Type.Matrix && tableColsNumber() == tableRowsNumber();
    }

    /*
        clipboard
     */
    public boolean isClipboard() {
        return type == Type.Clipboard;
    }

    /*
        database
     */
    public long readDataDefinition() {
        if (isTmpData()) {
            checkForLoad();
            return -1;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            Data2DDefinition definition = queryDefinition(conn);
            if (definition != null) {
                cloneAll(definition);
            }
            applyOptions();
            checkForLoad();
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

    public void checkForLoad() {
    }

    public void checkForSave() {
        if (dataName == null || dataName.isBlank()) {
            if (file != null && !isTmpData()) {
                dataName = file.getName();
            } else {
                dataName = DateTools.nowString();
            }
        }
    }

    public void saveDefinition(Connection conn) {
        try {
            rowsNumber = dataSize + (tableRowsNumber() - (endRowOfCurrentPage - startRowOfCurrentPage));
            colsNumber = tableColsNumber();
            if (colsNumber <= 0) {
                hasHeader = false;
            }
            checkForSave();
            checkForLoad();
            Data2DDefinition def;
            if (d2did < 0) {
                def = queryDefinition(conn);
                if (def != null) {
                    d2did = def.getD2did();
                }
            }
            if (d2did >= 0) {
                def = tableData2DDefinition.updateData(conn, this);
            } else {
                def = tableData2DDefinition.insertData(conn, this);
            }
            conn.commit();
            cloneAll(def);
            for (int i = 0; i < columns.size(); i++) {
                Data2DColumn column = columns.get(i);
                column.setIndex(i);
            }
            tableData2DColumn.save(conn, d2did, columns);
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
        }
    }

    public boolean isTmpData() {
        if (isDataFile()) {
            return isTmpFile();
        } else {
            return d2did < 0;
        }
    }

    /*
        values
     */
    public String randomDouble(Random random) {
        return DoubleTools.format(DoubleTools.random(random, maxRandom), scale);
    }

    public String randomString(Random random) {
        return (char) ('a' + random.nextInt(25)) + "";
    }

    public double doubleValue(String v) {
        try {
            if (v == null || v.isBlank()) {
                return 0;
            }
            return Double.valueOf(v.replaceAll(",", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    /*
        table data
     */
    public List<List<String>> tableData() {
        return tableController == null ? null : tableController.getTableData();
    }

    public void setTableChanged(boolean tableChanged) {
        this.tableChanged = tableChanged;
    }

    public int tableRowsNumber() {
        return tableController == null ? 0 : tableData().size();
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

    public List<String> tableRowWithoutNumber(int row) {
        try {
            List<String> values = tableData().get(row);
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
        return isValid() && tableData() != null && !tableData().isEmpty();
    }

    public List<Data2DColumn> tmpColumns(int cols) {
        List<String> names = new ArrayList<>();
        for (int c = 1; c <= cols; c++) {
            names.add(colPrefix() + c);
        }
        return toColumns(names);
    }

    public List<List<String>> tmpData(int rows, int cols) {
        Random random = new Random();
        List<List<String>> data = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            List<String> row = new ArrayList<>();
            for (int j = 0; j < cols; j++) {
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

    /*
        table view
     */
    public List<String> tableViewRow(int row) {
        if (tableController == null || row < 0 || row > tableData().size() - 1) {
            return null;
        }
        try {
            return tableData().get(row);
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

    public int columnsNumber() {
        if (columns == null) {
            return 0;
        } else {
            return columns.size();
        }
    }

    public List<String> columnNames() {
        try {
            if (!isColumnsValid()) {
                return null;
            }
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

    public List<String> numberColumnNames() {
        if (columns == null) {
            return null;
        }
        List<String> names = new ArrayList<>();
        for (Data2DColumn col : columns) {
            if (col.isNumberType()) {
                names.add(col.getName());
            }
        }
        return names;
    }

    public List<Data2DColumn> toColumns(List<String> names) {
        try {
            if (names == null) {
                return null;
            }
            List<Data2DColumn> cols = new ArrayList<>();
            for (String c : names) {
                cols.add(new Data2DColumn(c, defaultColumnType()));
            }
            return cols;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }


    /*
        attributes
     */
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
            return rowNames(tableData().size());
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

    public ControlData2DEditTable getTableController() {
        return tableController;
    }

    public void setTableController(ControlData2DEditTable tableController) {
        this.tableController = tableController;
    }

    public double[][] getMatrix() {
        return matrix;
    }

    public void setMatrix(double[][] matrix) {
        this.matrix = matrix;
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
