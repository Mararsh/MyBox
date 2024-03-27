package mara.mybox.data2d.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataClipboard;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataFileExcel;
import mara.mybox.data2d.DataFileText;
import mara.mybox.data2d.DataInternalTable;
import mara.mybox.data2d.DataMatrix;
import mara.mybox.data2d.DataTable;
import mara.mybox.data2d.TmpTable;
import mara.mybox.data2d.writer.Data2DWriter;
import mara.mybox.data2d.writer.DataTableWriter;
import mara.mybox.db.Database;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DRow;
import mara.mybox.db.table.TableData2D;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.CsvTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.TextFileTools;
import static mara.mybox.value.Languages.message;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @Author Mara
 * @CreateDate 2023-9-12
 * @License Apache License Version 2.0
 */
public class Data2DTableTools {

    public static DataTable makeTable(FxTask task, String tname,
            List<Data2DColumn> referColumns, List<String> keys, String idName) {
        try {
            if (referColumns == null || referColumns.isEmpty()) {
                return null;
            }
            if (tname == null || tname.isBlank()) {
                tname = TmpTable.tmpTableName();
            }
            DataTable dataTable = new DataTable();
            TableData2D tableData2D = dataTable.getTableData2D();
            String tableName = DerbyBase.fixedIdentifier(tname);
            tableData2D.setTableName(tableName);
            List<Data2DColumn> tableColumns = new ArrayList<>();
            List<String> validNames = new ArrayList<>();
            if (keys == null || keys.isEmpty()) {
                if (idName == null) {
                    idName = "id";
                }
                Data2DColumn idcolumn = new Data2DColumn(idName, ColumnDefinition.ColumnType.Long);
                idcolumn.setAuto(true).setIsPrimaryKey(true).setNotNull(true).setEditable(false);
                tableColumns.add(idcolumn);
                tableData2D.addColumn(idcolumn);
                validNames.add(idName);
            }
            for (Data2DColumn referColumn : referColumns) {
                Data2DColumn dataColumn = new Data2DColumn();
                dataColumn.cloneFrom(referColumn);
                dataColumn.setD2id(-1).setD2cid(-1).setAuto(false).setIsPrimaryKey(false);
                String referColumnName = referColumn.getColumnName();
                String columeName = DerbyBase.fixedIdentifier(referColumnName);
                columeName = DerbyBase.checkIdentifier(validNames, columeName, true);
                dataColumn.setColumnName(columeName);
                if (keys != null && !keys.isEmpty()) {
                    dataColumn.setIsPrimaryKey(keys.contains(referColumnName));
                }
                tableColumns.add(dataColumn);
                tableData2D.addColumn(dataColumn);
            }
            dataTable.setColumns(tableColumns).setTask(task).setSheet(tableName);
            return dataTable;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return null;
        }
    }

    public static DataTable createTable(FxTask task, Connection conn, String targetName,
            List<Data2DColumn> referColumns, List<String> keys, String comments,
            String idName, boolean dropExisted) {
        try {
            if (conn == null || referColumns == null || referColumns.isEmpty()) {
                return null;
            }
            if (targetName == null || targetName.isBlank()) {
                targetName = TmpTable.tmpTableName();
            }
            DataTable dataTable = new DataTable();
            String tableName = DerbyBase.fixedIdentifier(targetName);
            if (DerbyBase.exist(conn, tableName) > 0) {
                if (!dropExisted) {
                    return null;
                }
                dataTable.drop(conn, tableName);
                conn.commit();
            }
            dataTable = makeTable(task, tableName, referColumns, keys, idName);
            if (dataTable == null) {
                return null;
            }
            TableData2D tableData2D = dataTable.getTableData2D();
            tableData2D.setTableName(dataTable.getSheet());
            String sql = tableData2D.createTableStatement();
            if (task != null) {
                task.setInfo(sql);
            }
            if (conn.createStatement().executeUpdate(sql) < 0) {
                return null;
            }
            conn.commit();
            dataTable.recordTable(conn, tableName, dataTable.getColumns(), comments);
            return dataTable;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
    }

    public static DataTable createTable(FxTask task, Connection conn,
            String targetName, List<Data2DColumn> columns) {
        return createTable(task, conn, targetName, columns, null, null, null, true);
    }

    public static boolean write(FxTask task, DataTable dataTable, Data2DWriter writer,
            ResultSet results, String rowNumberName, int dscale, ColumnDefinition.InvalidAs invalidAs) {
        try {
            if (writer == null || results == null) {
                return false;
            }
            List<Data2DColumn> db2Columns = new ArrayList<>();
            List<String> fileRow = new ArrayList<>();
            List<String> names = new ArrayList<>();
            List<Data2DColumn> targetColumns = new ArrayList<>();
            if (rowNumberName != null) {
                names.add(rowNumberName);
                targetColumns.add(0, new Data2DColumn(rowNumberName, ColumnDefinition.ColumnType.String));
            }
            ResultSetMetaData meta = results.getMetaData();
            for (int col = 1; col <= meta.getColumnCount(); col++) {
                String name = meta.getColumnName(col);
                names.add(name);
                Data2DColumn dc = null;
                if (dataTable != null) {
                    dc = dataTable.columnByName(name);
                    if (dc != null) {
                        dc = dc.cloneAll().setD2cid(-1).setD2id(-1);
                    }
                }
                if (dc == null) {
                    dc = new Data2DColumn(name, ColumnDefinition.sqlColumnType(meta.getColumnType(col)), meta.isNullable(col) == ResultSetMetaData.columnNoNulls);
                }
                db2Columns.add(dc);
                targetColumns.add(dc);
            }
            writer.setColumns(targetColumns).setHeaderNames(names);
            if (!writer.openWriter()) {
                return false;
            }
            long count = 0;
            while (results.next() && task != null && !task.isCancelled()) {
                count++;
                if (rowNumberName != null) {
                    fileRow.add(rowNumberName + count);
                }
                for (Data2DColumn column : db2Columns) {
                    Object v = column.value(results);
                    String s = column.toString(v);
                    if (column.needScale()) {
                        s = DoubleTools.scaleString(s, invalidAs, dscale);
                    }
                    fileRow.add(s);
                }
                writer.writeRow(fileRow);
                fileRow.clear();
            }
            writer.closeWriter();
            return writer.isCreated();
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return false;
        }
    }

    public static DataTable importTable(FxTask task, String targetName,
            List<Data2DColumn> cols, List<List<String>> pageRows, ColumnDefinition.InvalidAs invalidAs) {
        if (cols == null || pageRows == null || pageRows.isEmpty()) {
            return null;
        }
        DataTable dataTable = null;
        try (Connection conn = DerbyBase.getConnection()) {
            dataTable = createTable(task, conn, targetName, cols, null, null, null, false);
            if (dataTable == null) {
                return null;
            }
            TableData2D tableData2D = dataTable.getTableData2D();
            Data2DRow data2DRow = tableData2D.newRow();
            List<Data2DColumn> columns = dataTable.getColumns();
            conn.setAutoCommit(false);
            int count = 0;
            for (List<String> row : pageRows) {
                for (int i = 0; i < columns.size(); i++) {
                    Data2DColumn column = columns.get(i);
                    data2DRow.setColumnValue(column.getColumnName(), column.fromString(row.get(i + 1), invalidAs));
                }
                tableData2D.insertData(conn, data2DRow);
                if (++count % Database.BatchSize == 0) {
                    conn.commit();
                    if (task != null) {
                        task.setInfo(message("Imported") + ": " + count);
                    }
                }
            }
            if (count > 0) {
                dataTable.setRowsNumber(count);
                dataTable.getTableData2DDefinition().updateData(conn, dataTable);
                conn.commit();
                if (task != null) {
                    task.setInfo(message("Imported") + ": " + count);
                }
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
        }
        return dataTable;
    }

    public static long importTable(FxTask task, Connection conn,
            Data2D sourceData, DataTable targetTable, List<Integer> cols,
            boolean includeRowNumber, ColumnDefinition.InvalidAs invalidAs) {
        try {
            if (sourceData == null || conn == null || targetTable == null) {
                return -1;
            }
            List<Data2DColumn> srcColumns = sourceData.getColumns();
            if (srcColumns == null || srcColumns.isEmpty()) {
                sourceData.readColumns(conn);
            }
            if (srcColumns == null || srcColumns.isEmpty()) {
                return -2;
            }
            if (cols == null || cols.isEmpty()) {
                cols = new ArrayList<>();
                for (int i = 0; i < srcColumns.size(); i++) {
                    cols.add(i);
                }
            }
            DataTableWriter writer = new DataTableWriter()
                    .setTargetTable(targetTable);
            return sourceData.copy(task, writer, cols,
                    includeRowNumber, true, false, invalidAs);
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return -5;
        }
    }

    public static long importTable(FxTask task, Connection conn, Data2D sourceData, DataTable dataTable) {
        return Data2DTableTools.importTable(task, conn, sourceData, dataTable, null, false, ColumnDefinition.InvalidAs.Blank);
    }

    public static DataFileText toText(FxTask task, DataTable dataTable) {
        if (task == null || dataTable == null || !dataTable.isValidDefinition()) {
            return null;
        }
        File txtFile = dataTable.tmpFile(dataTable.dataName(), null, "txt");
        DataFileCSV csvData = toCSV(task, dataTable, txtFile, false);
        if (csvData != null && txtFile != null && txtFile.exists()) {
            DataFileText targetData = new DataFileText();
            targetData.setColumns(csvData.getColumns())
                    .setFile(txtFile)
                    .setDataName(csvData.getDataName())
                    .setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",")
                    .setHasHeader(true)
                    .setColsNumber(csvData.getColsNumber())
                    .setRowsNumber(csvData.getRowsNumber());
            targetData.saveAttributes();
            return targetData;
        } else {
            return null;
        }
    }

    public static String toString(FxTask task, DataTable dataTable) {
        if (task == null || dataTable == null || !dataTable.isValidDefinition()) {
            return null;
        }
        File txtFile = dataTable.tmpFile(dataTable.dataName(), null, "txt");
        DataFileCSV csvData = toCSV(task, dataTable, txtFile, false);
        if (csvData != null && txtFile != null && txtFile.exists()) {
            return TextFileTools.readTexts(task, txtFile);
        } else {
            return null;
        }
    }

    public static DataFileCSV toCSV(FxTask task, DataTable dataTable, File file, boolean save) {
        if (task == null || dataTable == null || !dataTable.isValidDefinition() || file == null) {
            return null;
        }
        TableData2D tableData2D = dataTable.getTableData2D();
        List<Data2DColumn> dataColumns = dataTable.getColumns();
        int tcolsNumber = dataColumns.size();
        int trowsNumber = 0;
        String sql = tableData2D.queryAllStatement();
        if (task != null) {
            task.setInfo(sql);
        }
        try (Connection conn = DerbyBase.getConnection();
                PreparedStatement statement = conn.prepareStatement(sql);
                ResultSet results = statement.executeQuery();
                CSVPrinter csvPrinter = new CSVPrinter(
                        new FileWriter(file, Charset.forName("UTF-8")), CsvTools.csvFormat(",", true))) {
            csvPrinter.printRecord(dataTable.columnNames());
            while (results.next() && task != null && !task.isCancelled()) {
                try {
                    List<String> row = new ArrayList<>();
                    for (int col = 0; col < tcolsNumber; col++) {
                        Data2DColumn column = dataColumns.get(col);
                        Object v = column.value(results);
                        row.add(column.toString(v));
                    }
                    csvPrinter.printRecord(row);
                    trowsNumber++;
                } catch (Exception e) {
                    // skip  bad lines
                    MyBoxLog.error(e);
                }
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return null;
        }
        if (file != null && file.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setColumns(dataTable.getColumns()).setDataName(dataTable.dataName()).setFile(file).setCharset(Charset.forName("UTF-8")).setDelimiter(",").setHasHeader(true).setColsNumber(tcolsNumber).setRowsNumber(trowsNumber);
            if (save) {
                targetData.saveAttributes();
            }
            return targetData;
        } else {
            return null;
        }
    }

    public static DataFileCSV toCSV(FxTask task, DataTable dataTable) {
        File csvFile = dataTable.tmpFile(dataTable.dataName(), null, "csv");
        return toCSV(task, dataTable, csvFile, true);
    }

    public static DataFileExcel toExcel(FxTask task, DataTable dataTable) {
        if (task == null || dataTable == null || !dataTable.isValidDefinition()) {
            return null;
        }
        File excelFile = dataTable.tmpFile(dataTable.dataName(), null, "xlsx");
        String targetSheetName = message("Sheet") + "1";
        TableData2D tableData2D = dataTable.getTableData2D();
        List<Data2DColumn> dataColumns = dataTable.getColumns();
        int tcolsNumber = dataColumns.size();
        int trowsNumber = 0;
        String sql = tableData2D.queryAllStatement();
        if (task != null) {
            task.setInfo(sql);
        }
        try (Connection conn = DerbyBase.getConnection();
                PreparedStatement statement = conn.prepareStatement(sql);
                ResultSet results = statement.executeQuery();
                Workbook targetBook = new XSSFWorkbook()) {
            Sheet targetSheet = targetBook.createSheet(targetSheetName);
            Row targetRow = targetSheet.createRow(0);
            for (int col = 0; col < tcolsNumber; col++) {
                Cell targetCell = targetRow.createCell(col, CellType.STRING);
                targetCell.setCellValue(dataColumns.get(col).getColumnName());
            }
            while (results.next() && task != null && !task.isCancelled()) {
                try {
                    targetRow = targetSheet.createRow(++trowsNumber);
                    for (int col = 0; col < tcolsNumber; col++) {
                        Data2DColumn column = dataColumns.get(col);
                        Object v = column.value(results);
                        Cell targetCell = targetRow.createCell(col, CellType.STRING);
                        targetCell.setCellValue(column.toString(v));
                    }
                } catch (Exception e) {
                    // skip  bad lines
                    MyBoxLog.error(e);
                }
            }
            try (FileOutputStream fileOut = new FileOutputStream(excelFile)) {
                targetBook.write(fileOut);
            }
            targetBook.close();
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return null;
        }
        if (excelFile != null && excelFile.exists()) {
            DataFileExcel targetData = new DataFileExcel();
            targetData.setColumns(dataTable.getColumns()).setFile(excelFile).setSheet(targetSheetName).setDataName(dataTable.dataName()).setHasHeader(true).setColsNumber(tcolsNumber).setRowsNumber(trowsNumber);
            targetData.saveAttributes();
            return targetData;
        } else {
            return null;
        }
    }

    public static DataClipboard toClip(FxTask task, DataTable dataTable) {
        if (task == null || dataTable == null || !dataTable.isValidDefinition()) {
            return null;
        }
        File clipFile = DataClipboard.newFile();
        DataFileCSV csvData = toCSV(task, dataTable, clipFile, false);
        if (csvData != null && clipFile != null && clipFile.exists()) {
            return DataClipboard.create(task, csvData, dataTable.getDataName(), clipFile);
        } else {
            return null;
        }
    }

    public static DataMatrix toMatrix(FxTask task, DataTable dataTable) {
        if (task == null || dataTable == null || !dataTable.isValidDefinition()) {
            return null;
        }
        List<List<String>> rows = new ArrayList<>();
        TableData2D tableData2D = dataTable.getTableData2D();
        List<Data2DColumn> dataColumns = dataTable.getColumns();
        int tcolsNumber = dataColumns.size();
        String sql = tableData2D.queryAllStatement();
        if (task != null) {
            task.setInfo(sql);
        }
        try (Connection conn = DerbyBase.getConnection();
                PreparedStatement statement = conn.prepareStatement(sql);
                ResultSet results = statement.executeQuery()) {
            while (results.next() && task != null && !task.isCancelled()) {
                try {
                    List<String> row = new ArrayList<>();
                    for (int col = 0; col < tcolsNumber; col++) {
                        Data2DColumn column = dataColumns.get(col);
                        Object v = column.value(results);
                        row.add(column.toString(v));
                    }
                    rows.add(row);
                } catch (Exception e) {
                    // skip  bad lines
                    MyBoxLog.error(e);
                }
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return null;
        }
        DataMatrix matrix = new DataMatrix();
        matrix.cloneValueAttributes(dataTable);
        if (DataMatrix.save(task, matrix, dataColumns, rows) >= 0) {
            return matrix;
        } else {
            return null;
        }
    }

    public static List<String> userTables() {
        List<String> userTables = new ArrayList<>();
        try (Connection conn = DerbyBase.getConnection()) {
            List<String> allTables = DerbyBase.allTables(conn);
            for (String name : allTables) {
                if (!DataInternalTable.InternalTables.contains(name.toUpperCase())) {
                    userTables.add(name);
                }
            }
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
        return userTables;
    }

}
