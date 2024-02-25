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
import mara.mybox.data2d.reader.Data2DOperator;
import mara.mybox.data2d.reader.Data2DReadPage;
import mara.mybox.data2d.reader.Data2DReadTotal;
import mara.mybox.data2d.writer.Data2DWriter;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.Data2DStyle;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.db.table.TableData2DStyle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.FxTask;
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

    public abstract void applyOptions();

    public abstract List<String> readColumnNames();

    public abstract boolean savePageDataAs(Data2D targetData);

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
                    vname = DerbyBase.checkIdentifier(validNames, vname, true);
                    column.setColumnName(vname);
                    columns.add(column);
                }
            }
            if (columns != null && !columns.isEmpty()) {
                Random random = new Random();
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
                }
                colsNumber = columns.size();
                if (d2did >= 0 && conn != null) {
                    tableData2DColumn.save(conn, d2did, columns);
                    tableData2DDefinition.updateData(conn, this);
                }
            } else {
                colsNumber = 0;
                if (d2did >= 0 && conn != null) {
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

    public boolean loadColumns() {
        savedColumns = columns;
        return readColumns(null);
    }

    public long readTotal() {
        dataSize = 0;
        Data2DOperator reader = Data2DReadTotal.create(this)
                .setTask(backgroundTask).start();
        if (reader != null) {
            dataSize = reader.getRowIndex();
        }
        rowsNumber = dataSize;
        try (Connection conn = DerbyBase.getConnection()) {
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
        Data2DReadPage reader = Data2DReadPage.create(this).setConn(conn);
        if (reader == null) {
            startRowOfCurrentPage = endRowOfCurrentPage = 0;
            return null;
        }
        reader.setTask(task).start();
        pageData = reader.getRows();
        if (pageData != null) {
            endRowOfCurrentPage = startRowOfCurrentPage + pageData.size();
        }
        readPageStyles(conn);
        return pageData;
    }

    public void readPageStyles(Connection conn) {
        styles = new ArrayList<>();
        if (d2did < 0 || startRowOfCurrentPage >= endRowOfCurrentPage) {
            return;
        }
        try (PreparedStatement statement = conn.prepareStatement(TableData2DStyle.QueryStyles)) {
            statement.setLong(1, d2did);
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
        modify
     */
    public long setValue(List<Integer> cols, SetValue setValue, boolean errorContinue) {
        if (!validData() || cols == null || cols.isEmpty()) {
            return -1;
        }
        Data2DWriter writer = Data2DWriter.create(this)
                .setSetValue(setValue).setCols(cols)
                .setTask(task).start(Data2DWriter.Operation.SetValue);
        if (writer == null || writer.isFailed()) {
            return -2;
        }
        return writer.getCount();
    }

    public long deleteRows(boolean errorContinue) {
        if (!validData()) {
            return -1;
        }
        Data2DWriter writer = Data2DWriter.create(this)
                .setTask(task).start(Data2DWriter.Operation.Delete);
        if (writer == null || writer.isFailed()) {
            return -2;
        }
        return writer.getCount();
    }

    public long clearData() {
        if (!validData()) {
            return -1;
        }
        Data2DWriter writer = Data2DWriter.create(this)
                .setTask(task).start(Data2DWriter.Operation.ClearData);
        if (writer == null || writer.isFailed()) {
            return -2;
        }
        return dataSize;
    }

    public String encodeCSV(FxTask task, String delimiterName,
            boolean displayRowNames, boolean displayColNames, boolean formatValues) {
        if (!isColumnsValid()) {
            return "";
        }
        if (delimiterName == null) {
            delimiterName = ",";
        }
        try {
            File tmpFile = getTempFile(".csv");
            tmpFile = DataFileCSV.csvFile(task, tmpFile, delimiterValue(delimiterName),
                    displayColNames ? columnNames() : null, tableRows(displayRowNames, formatValues));
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
                }
                MyBoxLog.error(e);
            }
            FileDeleteTools.delete(tmpFile);
            return data;
        } catch (Exception e) {
            MyBoxLog.error(e);
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
        try (Connection conn = DerbyBase.getConnection()) {
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
        try {
            if (!d.checkForSave() || !d.checkForLoad()) {
                return false;
            }
            Data2DDefinition def;
            long did = d.getD2did();
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
                    d.setD2did(def.getD2did());
                    def = tableData2DDefinition.updateData(conn, d);
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
                    List<Data2DColumn> targetColumns = new ArrayList<>();
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
