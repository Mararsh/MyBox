package mara.mybox.data2d;

import java.io.File;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import mara.mybox.data.SetValue;
import mara.mybox.data2d.modify.Data2DClear;
import mara.mybox.data2d.modify.Data2DDelete;
import mara.mybox.data2d.modify.Data2DModify;
import mara.mybox.data2d.modify.Data2DSaveAttributes;
import mara.mybox.data2d.modify.Data2DSavePage;
import mara.mybox.data2d.modify.Data2DSetValue;
import mara.mybox.data2d.modify.DataTableClear;
import mara.mybox.data2d.modify.DataTableDelete;
import mara.mybox.data2d.modify.DataTableSetValue;
import mara.mybox.data2d.operate.Data2DOperate;
import mara.mybox.data2d.operate.Data2DReadPage;
import mara.mybox.data2d.operate.Data2DReadTotal;
import mara.mybox.data2d.writer.Data2DWriter;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition.InvalidAs;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.Data2DStyle;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.db.table.TableData2DStyle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.image.FxColorTools;
import mara.mybox.tools.CsvTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileDeleteTools;
import static mara.mybox.tools.FileTmpTools.getTempFile;
import mara.mybox.tools.TextFileTools;
import static mara.mybox.tools.TextTools.delimiterValue;
import static mara.mybox.value.Languages.message;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public abstract class Data2D_Edit extends Data2D_Filter {

    public abstract Data2DDefinition queryDefinition(Connection conn);

    public abstract List<String> readColumnNames();

    public abstract Data2DWriter selfWriter();


    /*
        read
     */
    public boolean checkForLoad() {
        return true;
    }

    public long loadDataDefinition(Connection conn) {
        if (isTmpData()) {
            checkForLoad();
            return -1;
        }
        try {
            Data2DDefinition definition = queryDefinition(conn);
            if (definition != null) {
                cloneFrom(definition);
            }
            checkForLoad();
            if (definition == null) {
                definition = tableData2DDefinition.insertData(conn, this);
                conn.commit();
                dataID = definition.getDataID();
            } else {
                tableData2DDefinition.updateData(conn, this);
                conn.commit();
                dataID = definition.getDataID();
                savedColumns = tableData2DColumn.read(conn, dataID);
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.debug(e);
            return -1;
        }
        return dataID;
    }

    public boolean deleteDataDefinition() {
        try (Connection conn = DerbyBase.getConnection()) {
            if (dataID < 0) {
                return false;
            }
            return tableData2DDefinition.deleteDefinition(conn, dataID) > 0;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.debug(e);
            return false;
        }
    }

    public boolean loadColumns(Connection conn) {
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
                    vname = DerbyBase.checkIdentifier(validNames, vname, true);
                    column.setColumnName(vname);
                    columns.add(column);
                }
            }
            if (columns != null && !columns.isEmpty()) {
                Random random = new Random();
                for (int i = 0; i < columns.size(); i++) {
                    Data2DColumn column = columns.get(i);
                    column.setDataID(dataID);
                    column.setIndex(i);
                    if (column.getColor() == null) {
                        column.setColor(FxColorTools.randomColor(random));
                    }
                    if (!isTable()) {
                        column.setAuto(false);
                        column.setIsPrimaryKey(false);
                    }
                    if (isMatrix()) {
                        column.setType(defaultColumnType());
                    }
                }
                colsNumber = columns.size();
                if (dataID >= 0 && conn != null) {
                    tableData2DColumn.save(conn, dataID, columns);
                    tableData2DDefinition.updateData(conn, this);
                }
            } else {
                colsNumber = 0;
                if (dataID >= 0 && conn != null) {
                    tableData2DColumn.clearColumns(conn, dataID);
                    tableData2DDefinition.updateData(conn, this);
                    tableData2DStyle.clear(conn, dataID);
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
        pagination.rowsNumber = -1;
        Data2DOperate opearte = Data2DReadTotal.create(this)
                .setTask(backgroundTask).start();
        if (opearte != null) {
            pagination.rowsNumber = opearte.getSourceRowIndex();
        }
        try (Connection conn = DerbyBase.getConnection()) {
            tableData2DDefinition.updateData(conn, this);
        } catch (Exception e) {
            if (backgroundTask != null) {
                backgroundTask.setError(e.toString());
            }
            MyBoxLog.error(e);
        }
        return pagination.rowsNumber;
    }

    public List<List<String>> loadPageData(Connection conn) {
        if (!hasColumns()) {
            pagination.startRowOfCurrentPage = pagination.endRowOfCurrentPage = 0;
            return null;
        }
        if (pagination.startRowOfCurrentPage < 0) {
            pagination.startRowOfCurrentPage = 0;
        }
        pagination.endRowOfCurrentPage = pagination.startRowOfCurrentPage;
        Data2DReadPage reader = Data2DReadPage.create(this).setConn(conn);
        if (reader == null) {
            pagination.startRowOfCurrentPage = pagination.endRowOfCurrentPage = 0;
            return null;
        }
        reader.setTask(task).start();
        List<List<String>> rows = reader.getRows();
        if (rows != null) {
            pagination.endRowOfCurrentPage = pagination.startRowOfCurrentPage + rows.size();
        }
        loadPageStyles(conn);
        return rows;
    }

    public void loadPageStyles(Connection conn) {
        styles = new ArrayList<>();
        if (dataID < 0 || pagination.startRowOfCurrentPage >= pagination.endRowOfCurrentPage) {
            return;
        }
        try (PreparedStatement statement = conn.prepareStatement(TableData2DStyle.QueryStyles)) {
            statement.setLong(1, dataID);
            conn.setAutoCommit(true);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    Data2DStyle style = tableData2DStyle.readData(results);
                    if (style != null) {
                        styles.add(style);
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
        }
        try {
            List<String> scripts = new ArrayList<>();
            for (int i = 0; i < styles.size(); i++) {
                Data2DStyle style = styles.get(i);
                scripts.add(style.getFilter());
            }
            scripts = calculateScriptsStatistic(scripts);
            for (int i = 0; i < styles.size(); i++) {
                Data2DStyle style = styles.get(i);
                style.setFilter(scripts.get(i));
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
        }
    }

    public void countPageSize() {
        try {
            pagination.rowsNumber = pagination.rowsNumber + (tableRowsNumber()
                    - (pagination.endRowOfCurrentPage - pagination.startRowOfCurrentPage));
            colsNumber = tableColsNumber();
            if (colsNumber <= 0) {
                hasHeader = false;
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        modify
     */
    public Data2DWriter initSelfWriter(Data2DWriter writer) {
        if (writer != null) {
            writer.setWriteHeader(hasHeader)
                    .setDataName(dataName)
                    .setPrintFile(file)
                    .setColumns(columns)
                    .setHeaderNames(columnNames())
                    .setTargetScale(scale)
                    .setTargetMaxRandom(maxRandom)
                    .setWriteComments(true)
                    .setTargetComments(comments)
                    .setRecordTargetFile(true)
                    .setRecordTargetData(true);
        }
        return writer;
    }

    public long savePageData(FxTask task) {
        try {
            Data2DModify operate = Data2DSavePage.save(this);
            if (operate == null) {
                return -2;
            }
            operate.setTask(task).start();
            if (operate.isFailed()) {
                return -3;
            }
            return operate.rowsCount();
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return -4;
        }
    }

    public long saveAttributes(FxTask task, Data2D attributes) {
        try {
            if (attributes == null) {
                return -1;
            }
            Data2DModify operate = Data2DSaveAttributes.create(this, attributes);
            if (operate == null) {
                return -2;
            }
            operate.setTask(task).start();
            if (operate.isFailed()) {
                return -3;
            }
            attributes.pagination.rowsNumber = operate.rowsCount();
            attributes.tableChanged = false;
            attributes.pagination.currentPage = pagination.currentPage;
            cloneDataFrom(attributes);
            return attributes.pagination.rowsNumber;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return -4;
        }
    }

    public long setValue(FxTask task, String colName, String value) {
        try {
            List<Integer> cols = new ArrayList<>();
            cols.add(colOrder(colName));
            SetValue setValue = new SetValue()
                    .setType(SetValue.ValueType.Value)
                    .setParameter(value);
            return setValue(task, cols, setValue, InvalidAs.Fail);
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return -4;
        }
    }

    public long setValue(FxTask task, List<Integer> cols,
            SetValue setValue, InvalidAs invalidAs) {
        try {
            if (!hasData() || cols == null || cols.isEmpty()) {
                return -1;
            }
            Data2DOperate operate = isTable()
                    ? new DataTableSetValue((DataTable) this, setValue)
                    : Data2DSetValue.create(this, setValue);
            if (operate == null) {
                return -2;
            }
            operate.setCols(cols).setInvalidAs(invalidAs)
                    .setTask(task).start();
            if (operate.isFailed()) {
                return -3;
            }
            tableChanged = false;
            return operate.getHandledCount();
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return -4;
        }
    }

    public long deleteRows(FxTask task) {
        try {
            if (!hasData()) {
                return -1;
            }
            Data2DOperate operate = isTable()
                    ? new DataTableDelete((DataTable) this)
                    : Data2DDelete.create(this);
            if (operate == null) {
                return -2;
            }
            operate.setTask(task).start();
            if (operate.isFailed()) {
                return -3;
            }
            return operate.getHandledCount();
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return -4;
        }
    }

    public long clearData(FxTask task) {
        try {
            if (!hasData()) {
                return -1;
            }
            Data2DOperate operate = isTable()
                    ? new DataTableClear((DataTable) this)
                    : Data2DClear.create(this);

            if (operate == null) {
                return -2;
            }
            operate.setTask(task).start();
            if (operate.isFailed()) {
                return -3;
            }
            tableChanged = false;
            return operate.getHandledCount();
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return -4;
        }
    }

    public String encodeCSV(FxTask task, String delimiterName,
            boolean displayRowNames, boolean displayColNames, boolean formatData) {
        if (!hasColumns()) {
            return "";
        }
        if (delimiterName == null) {
            delimiterName = ",";
        }
        try {
            File tmpFile = getTempFile(".csv");
            List<String> cols = null;
            if (displayColNames) {
                cols = new ArrayList<>();
                if (displayRowNames) {
                    cols.add(message("TableRowNumber"));
                    cols.add(message("DataRowNumber"));
                }
                cols.addAll(columnNames());
            }
            tmpFile = DataFileCSV.csvFile(task, tmpFile, delimiterValue(delimiterName),
                    cols, pageRows(displayRowNames, formatData));
            if (tmpFile == null || !tmpFile.exists()) {
                return "";
            }
            String page = TextFileTools.readTexts(task, tmpFile, Charset.forName("UTF-8"));
            FileDeleteTools.delete(tmpFile);
            return page;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return "";
        }
    }

    public List<List<String>> decodeCSV(FxTask task, String text, String delimiterName, boolean hasHeader) {
        if (text == null || delimiterName == null) {
            return null;
        }
        try {
            List<List<String>> data = new ArrayList<>();
            File tmpFile = getTempFile(".csv");
            TextFileTools.writeFile(tmpFile, text, Charset.forName("UTF-8"));
            if (tmpFile == null || !tmpFile.exists()) {
                return null;
            }
            try (CSVParser parser = CsvTools.csvParser(tmpFile, delimiterValue(delimiterName), hasHeader)) {
                if (hasHeader) {
                    data.add(parser.getHeaderNames());
                }
                for (CSVRecord record : parser) {
                    if (task != null && task.isCancelled()) {
                        return null;
                    }
                    data.add(record.toList());
                }
            } catch (Exception e) {
                if (task != null) {
                    task.setError(e.toString());
                } else {
                    MyBoxLog.error(e);
                }
                data = null;
            }
            FileDeleteTools.delete(tmpFile);
            return data;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return null;
        }
    }

    /*
        save
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
        try (Connection conn = DerbyBase.getConnection()) {
            return saveAttributes(conn);
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean saveAttributes(Connection conn) {
        return saveAttributes(conn, (Data2D) this, columns);
    }

    public static boolean saveAttributes(Data2D d, List<Data2DColumn> cols) {
        if (d == null) {
            return false;
        }
        try (Connection conn = DerbyBase.getConnection()) {
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
        if (conn == null) {
            return saveAttributes(d, inColumns);
        }
        try {
            if (!d.checkForSave() || !d.checkForLoad()) {
                return false;
            }
            Data2DDefinition def;
            long did = d.getDataID();
            d.setModifyTime(new Date());
            d.setColsNumber(inColumns == null ? 0 : inColumns.size());
            TableData2DDefinition tableData2DDefinition = d.getTableData2DDefinition();
            if (did >= 0) {
                def = tableData2DDefinition.updateData(conn, d);
            } else {
                def = d.queryDefinition(conn);
                if (def == null) {
                    def = tableData2DDefinition.insertData(conn, d);
                } else {
                    d.setDataID(def.getDataID());
                    def = tableData2DDefinition.updateData(conn, d);
                }
            }
            conn.commit();
            did = def.getDataID();
            if (did < 0) {
                return false;
            }
            d.cloneFrom(def);
            if (inColumns != null && !inColumns.isEmpty()) {
                try {
                    List<Data2DColumn> targetColumns = new ArrayList<>();
                    for (int i = 0; i < inColumns.size(); i++) {
                        Data2DColumn column = inColumns.get(i).cloneAll();
                        if (column.getDataID() != did) {
                            column.setColumnID(-1);
                        }
                        column.setDataID(did);
                        column.setIndex(i);
                        if (!d.isTable()) {
                            column.setIsPrimaryKey(false);
                            column.setAuto(false);
                        }
                        if (d.isMatrix()) {
                            column.setType(d.defaultColumnType());
                        }
                        targetColumns.add(column);
                    }
                    d.getTableData2DColumn().save(conn, did, targetColumns);
                    d.setColumns(targetColumns);
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

}
