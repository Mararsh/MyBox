package mara.mybox.data2d;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import mara.mybox.data2d.scan.Data2DReader;
import mara.mybox.data2d.scan.Data2DReader.Operation;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.Data2DStyle;
import mara.mybox.db.table.TableData2DStyle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.RowFilter;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.tools.DateTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public abstract class Data2D_Edit extends Data2D_Data {

    public abstract Data2DDefinition queryDefinition(Connection conn);

    public abstract void applyOptions();

    public abstract List<String> readColumnNames();

    public abstract boolean savePageData(Data2D targetData);

    public abstract boolean setValue(List<Integer> cols, String value, boolean errorContinue);

    public abstract boolean delete(boolean errorContinue);

    public abstract long clearData();

    /*
        read
     */
    public boolean checkForLoad() {
        return true;
    }

    public long readDataDefinition(Connection conn) {
        if (isTmpData()) {
            checkForLoad();
            return -1;
        }
        try {
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
            MyBoxLog.debug(e);
            return -1;
        }
        return d2did;
    }

    public boolean readColumns(Connection conn) {
        try {
            columns = null;
            List<String> colNames = readColumnNames();
            if (colNames == null || colNames.isEmpty()) {
                hasHeader = false;
                columns = savedColumns;
            } else {
                List<String> validNames = new ArrayList<>();
                columns = new ArrayList<>();
                for (int i = 0; i < colNames.size(); i++) {
                    String name = colNames.get(i);
                    Data2DColumn column;
                    if (savedColumns != null && i < savedColumns.size()) {
                        column = savedColumns.get(i);
                        if (!hasHeader) {
                            name = column.getColumnName();
                        }
                    } else {
                        column = new Data2DColumn(name, defaultColumnType());
                    }
                    String vname = (name == null || name.isBlank()) ? message("Column") + (i + 1) : name;
                    while (validNames.contains(vname)) {
                        vname += "m";
                    }
                    validNames.add(vname);
                    column.setColumnName(vname);
                    columns.add(column);
                }
            }
            if (columns != null && !columns.isEmpty()) {
                Random random = new Random();
                List<String> names = new ArrayList<>();
                for (int i = 0; i < columns.size(); i++) {
                    Data2DColumn column = columns.get(i);
                    column.setD2id(d2did);
                    column.setIndex(i);
                    if (column.getColor() == null) {
                        column.setColor(FxColorTools.randomColor(random));
                    }
                    if (!isTable()) {
                        column.setAuto(false);
                        column.setIsPrimaryKey(false);
                    }
                    if (isMatrix()) {
                        column.setType(ColumnDefinition.ColumnType.Double);
                    }
                    names.add(column.getColumnName());
                }
                colsNumber = columns.size();
                if (d2did >= 0) {
                    tableData2DColumn.save(conn, d2did, columns);
                    tableData2DDefinition.updateData(conn, this);
                }
            } else {
                colsNumber = 0;
                if (d2did >= 0) {
                    tableData2DColumn.clear(conn, d2did);
                    tableData2DDefinition.updateData(conn, this);
                    tableData2DStyle.clear(conn, d2did);
                }
            }
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.debug(e);
            return false;
        }
    }

    public long readTotal() {
        dataSize = 0;
        Data2DReader reader = Data2DReader.create(this)
                .setReaderTask(backgroundTask).start(Operation.ReadTotal);
        if (reader != null) {
            dataSize = reader.getRowIndex();
        }
        rowsNumber = dataSize;
        try ( Connection conn = DerbyBase.getConnection()) {
            tableData2DDefinition.updateData(conn, this);
        } catch (Exception e) {
            if (backgroundTask != null) {
                backgroundTask.setError(e.toString());
            }
            MyBoxLog.error(e);
        }
        return dataSize;
    }

    public List<List<String>> readPageData(Connection conn) {
        if (!isColumnsValid()) {
            startRowOfCurrentPage = endRowOfCurrentPage = 0;
            return null;
        }
        if (startRowOfCurrentPage < 0) {
            startRowOfCurrentPage = 0;
        }
        endRowOfCurrentPage = startRowOfCurrentPage;
        Data2DReader reader = Data2DReader.create(this)
                .setConn(conn).setReaderTask(task)
                .start(Operation.ReadPage);
        if (reader == null) {
            startRowOfCurrentPage = endRowOfCurrentPage = 0;
            return null;
        }
        List<List<String>> rows = reader.getRows();
        if (rows != null) {
            endRowOfCurrentPage = startRowOfCurrentPage + rows.size();
        }
        readPageStyles(conn, rows);
        return rows;
    }

    public void readPageStyles(Connection conn, List<List<String>> rows) {
        styles = new ArrayList<>();
        if (d2did < 0 || startRowOfCurrentPage >= endRowOfCurrentPage) {
            return;
        }
        try ( PreparedStatement statement = conn.prepareStatement(TableData2DStyle.QueryStyles);) {
            statement.setLong(1, d2did);
            try ( ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    Data2DStyle style = tableData2DStyle.readData(results);
                    if (style != null) {
                        styles.add(style);
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void countSize() {
        try {
            rowsNumber = dataSize + (tableRowsNumber() - (endRowOfCurrentPage - startRowOfCurrentPage));
            colsNumber = tableColsNumber();
            if (colsNumber <= 0) {
                hasHeader = false;
            }
        } catch (Exception e) {
        }
    }

    /*
        write
     */
    public boolean checkForSave() {
        if (dataName == null || dataName.isBlank()) {
            if (file != null && !isTmpData()) {
                dataName = file.getName();
            } else {
                dataName = DateTools.nowString();
            }
        }
        return true;
    }

    public boolean saveAttributes() {
        try ( Connection conn = DerbyBase.getConnection()) {
            return saveAttributes(conn, (Data2D) this, columns);
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean saveAttributes(Data2D source, Data2D target) {
        try ( Connection conn = DerbyBase.getConnection()) {
            target.cloneAttributes(source);
            if (!saveAttributes(conn, target, source.getColumns())) {
                return false;
            }
            return target.getTableData2DStyle().copyStyles(conn, source.getD2did(), target.getD2did()) >= 0;
        } catch (Exception e) {
            if (source.getTask() != null) {
                source.getTask().setError(e.toString());
            }
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean saveAttributes(Data2D d, List<Data2DColumn> cols) {
        if (d == null) {
            return false;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            return saveAttributes(conn, d, cols);
        } catch (Exception e) {
            if (d.getTask() != null) {
                d.getTask().setError(e.toString());
            }
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean saveAttributes(Connection conn, Data2D d, List<Data2DColumn> inColumns) {
        if (d == null) {
            return false;
        }
        try {
            if (!d.checkForSave() || !d.checkForLoad()) {
                return false;
            }
            Data2DDefinition def;
            long did = d.getD2did();
            d.setModifyTime(new Date());
            d.setColsNumber(inColumns == null ? 0 : inColumns.size());
            if (did >= 0) {
                def = d.getTableData2DDefinition().updateData(conn, d);
            } else {
                def = d.queryDefinition(conn);
                if (def == null) {
                    def = d.getTableData2DDefinition().insertData(conn, d);
                } else {
                    d.setD2did(def.getD2did());
                    def = d.getTableData2DDefinition().updateData(conn, d);
                }
            }
            conn.commit();
            did = def.getD2did();
            if (did < 0) {
                return false;
            }
            d.cloneAll(def);
            if (inColumns != null && !inColumns.isEmpty()) {
                try {
                    List<Data2DColumn> savedColumns = new ArrayList<>();
                    for (int i = 0; i < inColumns.size(); i++) {
                        Data2DColumn column = inColumns.get(i).cloneAll();
                        if (column.getD2id() != did) {
                            column.setD2cid(-1);
                        }
                        column.setD2id(did);
                        column.setIndex(i);
                        if (!d.isTable()) {
                            column.setIsPrimaryKey(false);
                            column.setAuto(false);
                        }
                        if (d.isMatrix()) {
                            column.setType(ColumnDefinition.ColumnType.Double);
                        }
                        savedColumns.add(column);
                    }
                    d.getTableData2DColumn().save(conn, did, savedColumns);
                    d.setColumns(savedColumns);
                } catch (Exception e) {
                    if (d.getTask() != null) {
                        d.getTask().setError(e.toString());
                    }
                    MyBoxLog.error(e);
                }
            } else {
                d.getTableData2DColumn().clear(d);
                d.setColumns(null);
            }
            return true;
        } catch (Exception e) {
            if (d.getTask() != null) {
                d.getTask().setError(e.toString());
            }
            MyBoxLog.error(e);
            return false;
        }
    }

    /*
        filter
     */
    public boolean needFilter() {
        return rowFilter != null && rowFilter.needFilter();
    }

    public boolean calculateTableRowExpression(String script, List<String> tableRow, long tableRowNumber) {
        return rowFilter == null
                || rowFilter.calculateTableRowExpression(script, tableRow, tableRowNumber);
    }

    public boolean calculateDataRowExpression(String script, List<String> dataRow, long dataRowNumber) {
        error = null;
        if (rowFilter == null) {
            return true;
        }
        if (rowFilter.calculateDataRowExpression(script, dataRow, dataRowNumber)) {
            return true;
        } else {
            error = rowFilter.getError();
            return false;
        }
    }

    public boolean filterDataRow(List<String> dataRow, long dataRowIndex) {
        error = null;
        if (rowFilter == null) {
            return true;
        }
        if (rowFilter.filterDataRow(dataRow, dataRowIndex)) {
            return true;
        } else {
            error = rowFilter.getError();
            return false;
        }
    }

    public boolean filterPassed() {
        return rowFilter == null || rowFilter.passed;
    }

    public boolean filterReachMaxPassed() {
        return rowFilter != null && rowFilter.reachMaxPassed();
    }

    public String getExpressionResult() {
        return rowFilter == null ? null : rowFilter.expressionResult;
    }

    public void startFilterService(SingletonTask task) {
        if (needFilter()) {
            rowFilter.startService(task);
        }
    }

    public void startExpressionService(SingletonTask task) {
        if (rowFilter != null) {
            rowFilter.startService(task);
        }
    }

    public void stopFilterService() {
        if (rowFilter != null) {
            rowFilter.stopService();
        }
    }

    /*
        style
     */
    public String cellStyle(RowFilter calculator, int tableRowIndex, String colName) {
        try {
            if (calculator == null || styles == null || styles.isEmpty() || colName == null || colName.isBlank()) {
                return null;
            }
            List<String> tableRow = tableViewRow(tableRowIndex);
            if (tableRow == null || tableRow.size() < 1) {
                return null;
            }
            int colIndex = colOrder(colName);
            if (colIndex < 0) {
                return null;
            }
            String cellStyle = null;
            long dataRowIndex = Long.parseLong(tableRow.get(0)) - 1;
            for (Data2DStyle style : styles) {
                String names = style.getColumns();
                if (names != null && !names.isBlank()) {
                    String[] cols = names.split(Data2DStyle.ColumnSeparator);
                    if (cols != null && cols.length > 0) {
                        if (!(Arrays.asList(cols).contains(colName))) {
                            continue;
                        }
                    }
                }
                long rowStart = style.getRowStart();
                if (dataRowIndex < rowStart) {
                    continue;
                }
                if (rowStart >= 0) {
                    long rowEnd = style.getRowEnd();
                    if (rowEnd >= 0 && dataRowIndex >= rowEnd) {
                        continue;
                    }
                }
                calculator.reset().setData2D((Data2D) this);
                if (style.filterCell(calculator, tableRow, tableRowIndex, colIndex)) {
                    String styleValue = style.finalStyle();
                    if (styleValue == null || styleValue.isBlank()) {
                        cellStyle = null;
                    } else if (cellStyle == null) {
                        cellStyle = style.finalStyle();
                    } else {
                        if (!cellStyle.trim().endsWith(";")) {
                            cellStyle += ";";
                        }
                        cellStyle += style.finalStyle();
                    }
                }
            }
            return cellStyle;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
