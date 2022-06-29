package mara.mybox.data2d;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import mara.mybox.data2d.scan.Data2DReader;
import mara.mybox.data2d.scan.Data2DReader.Operation;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.table.TableData2D;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.tools.CsvTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileCopyTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.TmpFileTools;
import mara.mybox.value.AppPaths;
import static mara.mybox.value.Languages.message;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public abstract class Data2D_Convert extends Data2D_Edit {

    public static String TmpTablePrefix = "MYBOXTMP__";

    public static String tmpTableName() {
        return TmpTablePrefix + DateTools.nowString3();
    }

    public static String tmpTableName(String sourceName) {
        return TmpTablePrefix + sourceName + DateTools.nowString3();
    }

    /*
        to/from database table
     */
    public DataTable toTable(SingletonTask task, String targetName, boolean dropExisted) {
        try ( Connection conn = DerbyBase.getConnection()) {
            DataTable dataTable = createTable(task, conn, targetName, dropExisted);
            writeTableData(task, conn, dataTable);
            return dataTable;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
    }

    public DataTable createTable(SingletonTask task, Connection conn, String targetName, boolean dropExisted) {
        try {
            if (columns == null || columns.isEmpty()) {
                readColumns(conn);
            }
            if (columns == null || columns.isEmpty()) {
                return null;
            }
            return createTable(task, conn, targetName, columns, null, dropExisted);
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
    }

    public DataTable toTmpTable(SingletonTask task, List<Integer> cols, boolean includeRowNumber) {
        try ( Connection conn = DerbyBase.getConnection()) {
            DataTable dataTable = createTmpTable(task, conn, tmpTableName(shortName()), cols, includeRowNumber);
            writeTableData(task, conn, dataTable, cols, includeRowNumber);
            return dataTable;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
    }

    public DataTable createTmpTable(SingletonTask task, Connection conn,
            String targetName, List<Integer> cols, boolean includeRowNumber) {
        try {
            if (conn == null || cols == null || cols.isEmpty()) {
                return null;
            }
            if (columns == null || columns.isEmpty()) {
                readColumns(conn);
            }
            if (columns == null || columns.isEmpty()) {
                return null;
            }
            List<Data2DColumn> sourceColumns = new ArrayList<>();
            if (includeRowNumber) {
                sourceColumns.add(new Data2DColumn(message("SourceRowNumber"), ColumnDefinition.ColumnType.Long));
            }
            for (int i = 0; i < columns.size(); i++) {
                if (cols.contains(i)) {
                    Data2DColumn column = columns.get(i).cloneAll();
                    column.setD2cid(-1).setD2id(-1);
                    sourceColumns.add(column);
                }
            }
            return createTable(task, conn, targetName, sourceColumns, null, true);
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
    }

    public long writeTableData(SingletonTask task, Connection conn,
            DataTable dataTable, List<Integer> cols, boolean includeRowNumber) {
        try {
            if (conn == null || dataTable == null) {
                return -1;
            }
            if (columns == null || columns.isEmpty()) {
                readColumns(conn);
            }
            if (columns == null || columns.isEmpty()) {
                return -2;
            }
            if (cols == null || cols.isEmpty()) {
                cols = new ArrayList<>();
                for (int i = 0; i < columns.size(); i++) {
                    cols.add(i);
                }
            }
            Data2DReader reader = Data2DReader.create(this)
                    .setConn(conn).setDataTable(dataTable)
                    .setCols(cols).setIncludeRowNumber(includeRowNumber)
                    .setReaderTask(task).start(Operation.WriteTable);
            if (reader != null && !reader.isFailed()) {
                conn.commit();
                return reader.getCount();
            } else {
                return -3;
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return -4;
        }
    }

    public long writeTableData(SingletonTask task, Connection conn, DataTable dataTable) {
        return writeTableData(task, conn, dataTable, null, false);
    }

    public DataTable singleColumn(SingletonTask task, List<Integer> cols) {
        try ( Connection conn = DerbyBase.getConnection()) {
            if (columns == null || columns.isEmpty()) {
                readColumns(conn);
            }
            if (columns == null || columns.isEmpty()) {
                return null;
            }
            List<Data2DColumn> sourceColumns = new ArrayList<>();
            Data2DColumn column = new Data2DColumn("data", ColumnDefinition.ColumnType.String);
            column.setD2cid(-1).setD2id(-1);
            sourceColumns.add(column);
            DataTable dataTable = createTable(task, conn, tmpTableName(), sourceColumns, null, true);
            if (cols == null || cols.isEmpty()) {
                cols = new ArrayList<>();
                for (int i = 0; i < columns.size(); i++) {
                    cols.add(i);
                }
            }
            Data2DReader reader = Data2DReader.create(this)
                    .setConn(conn).setDataTable(dataTable).setCols(cols)
                    .setReaderTask(task).start(Operation.SingleColumn);
            if (reader != null && !reader.isFailed()) {
                conn.commit();
                return dataTable;
            } else {
                return null;
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
    }

    public static DataTable makeTable(String name, List<Data2DColumn> sourceColumns, List<String> keys) {
        try {
            if (sourceColumns == null || sourceColumns.isEmpty()) {
                return null;
            }
            if (name == null || name.isBlank()) {
                name = tmpTableName();
            }
            DataTable dataTable = new DataTable();
            TableData2D tableData2D = dataTable.getTableData2D();
            String tableName = DerbyBase.fixedIdentifier(name);
            tableData2D.setTableName(tableName);

            List<Data2DColumn> tableColumns = new ArrayList<>();
            String idname = null;
            if (keys == null || keys.isEmpty()) {
                idname = tableName.replace("\"", "") + "_id";
                Data2DColumn idcolumn = new Data2DColumn(idname, ColumnDefinition.ColumnType.Long);
                idcolumn.setAuto(true).setIsPrimaryKey(true).setNotNull(true).setEditable(false);
                tableColumns.add(idcolumn);
                tableData2D.addColumn(idcolumn);
            }
            Map<String, String> columnsMap = new HashMap<>();
            for (Data2DColumn sourceColumn : sourceColumns) {
                Data2DColumn dataColumn = new Data2DColumn();
                dataColumn.cloneFrom(sourceColumn);
                dataColumn.setD2id(-1).setD2cid(-1)
                        .setAuto(false).setIsPrimaryKey(false);
                String sourceColumnName = sourceColumn.getColumnName();
                String columeName = DerbyBase.fixedIdentifier(sourceColumnName);
                if (columeName.equalsIgnoreCase(idname)) {
                    columeName += "m";
                }
                dataColumn.setColumnName(columeName);
                if (keys != null && !keys.isEmpty()) {
                    dataColumn.setIsPrimaryKey(keys.contains(sourceColumnName));
                }
                tableColumns.add(dataColumn);
                tableData2D.addColumn(dataColumn);
                columnsMap.put(sourceColumnName, dataColumn.getColumnName());
            }
            dataTable.setSheet(tableName);
            dataTable.setColumns(tableColumns);
            dataTable.setColumnsMap(columnsMap);
            dataTable.setSourceColumns(sourceColumns);
            return dataTable;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static DataTable createTable(SingletonTask task, Connection conn,
            String name, List<Data2DColumn> sourceColumns, List<String> keys, boolean dropExisted) {
        try {
            if (conn == null || sourceColumns == null || sourceColumns.isEmpty()) {
                return null;
            }
            if (name == null || name.isBlank()) {
                name = tmpTableName();
            }
            DataTable dataTable = new DataTable();
            TableData2D tableData2D = dataTable.getTableData2D();
            String tableName = DerbyBase.fixedIdentifier(name);
            if (tableData2D.exist(conn, tableName)) {
                if (!dropExisted) {
                    return null;
                }
                dataTable.drop(conn, tableName);
                conn.commit();
            }
            dataTable = makeTable(tableName, sourceColumns, keys);
            if (dataTable == null) {
                return null;
            }
            tableData2D = dataTable.getTableData2D();
            if (conn.createStatement().executeUpdate(tableData2D.createTableStatement()) < 0) {
                return null;
            }
            conn.commit();
            dataTable.recordTable(conn, tableName, dataTable.getColumns());
            return dataTable;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
    }

    public static DataFileCSV toCSV(SingletonTask task, DataTable dataTable, File file, boolean save) {
        if (task == null || dataTable == null || !dataTable.isValid() || file == null) {
            return null;
        }
        TableData2D tableData2D = dataTable.getTableData2D();
        List<Data2DColumn> dataColumns = dataTable.getColumns();
        int tcolsNumber = dataColumns.size(), trowsNumber = 0;
        try ( Connection conn = DerbyBase.getConnection();
                 PreparedStatement statement = conn.prepareStatement(tableData2D.queryAllStatement());
                 ResultSet results = statement.executeQuery();
                 CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(file, Charset.forName("UTF-8")), CsvTools.csvFormat(',', true))) {
            csvPrinter.printRecord(dataTable.columnNames());
            while (results.next() && task != null && !task.isCancelled()) {
                try {
                    List<String> row = new ArrayList<>();
                    for (int col = 0; col < tcolsNumber; col++) {
                        Data2DColumn column = dataColumns.get(col);
                        Object v = results.getObject(column.getColumnName());
                        row.add(column.toString(v));
                    }
                    csvPrinter.printRecord(row);
                    trowsNumber++;
                } catch (Exception e) {  // skip  bad lines
                    MyBoxLog.error(e);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
            return null;
        }
        if (file != null && file.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setColumns(dataTable.sourceColumns())
                    .setFile(file).setCharset(Charset.forName("UTF-8")).setDelimiter(",")
                    .setHasHeader(true).setColsNumber(tcolsNumber).setRowsNumber(trowsNumber);
            if (save) {
                targetData.saveAttributes();
            }
            return targetData;
        } else {
            return null;
        }
    }

    public static DataFileCSV toCSV(SingletonTask task, DataTable dataTable) {
        File csvFile = TmpFileTools.getPathTempFile(AppPaths.getGeneratedPath(), dataTable.shortName(), ".csv");
        return toCSV(task, dataTable, csvFile, true);
    }

    public static DataFileText toText(SingletonTask task, DataTable dataTable) {
        if (task == null || dataTable == null || !dataTable.isValid()) {
            return null;
        }
        File txtFile = TmpFileTools.getPathTempFile(AppPaths.getGeneratedPath(), dataTable.shortName(), ".txt");
        DataFileCSV csvData = toCSV(task, dataTable, txtFile, false);
        if (csvData != null && txtFile != null && txtFile.exists()) {
            DataFileText targetData = new DataFileText();
            targetData.setColumns(csvData.getColumns())
                    .setFile(txtFile).setCharset(Charset.forName("UTF-8")).setDelimiter(",")
                    .setHasHeader(true).setColsNumber(csvData.getColsNumber()).setRowsNumber(csvData.getRowsNumber());
            targetData.saveAttributes();
            return targetData;
        } else {
            return null;
        }
    }

    public static DataFileExcel toExcel(SingletonTask task, DataTable dataTable) {
        if (task == null || dataTable == null || !dataTable.isValid()) {
            return null;
        }
        File excelFile = TmpFileTools.getPathTempFile(AppPaths.getGeneratedPath(), dataTable.shortName(), ".xlsx");
        String targetSheetName = message("Sheet") + "1";
        TableData2D tableData2D = dataTable.getTableData2D();
        List<Data2DColumn> dataColumns = dataTable.getColumns();
        int tcolsNumber = dataColumns.size(), trowsNumber = 0;
        try ( Connection conn = DerbyBase.getConnection();
                 PreparedStatement statement = conn.prepareStatement(tableData2D.queryAllStatement());
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
                        Object v = results.getObject(column.getColumnName());
                        Cell targetCell = targetRow.createCell(col, CellType.STRING);
                        targetCell.setCellValue(column.toString(v));
                    }
                } catch (Exception e) {  // skip  bad lines
                    MyBoxLog.error(e);
                }
            }
            try ( FileOutputStream fileOut = new FileOutputStream(excelFile)) {
                targetBook.write(fileOut);
            }
            targetBook.close();
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
            return null;
        }
        if (excelFile != null && excelFile.exists()) {
            DataFileExcel targetData = new DataFileExcel();
            targetData.setColumns(dataTable.sourceColumns())
                    .setFile(excelFile).setSheet(targetSheetName)
                    .setHasHeader(true).setColsNumber(tcolsNumber).setRowsNumber(trowsNumber);
            targetData.saveAttributes();
            return targetData;
        } else {
            return null;
        }
    }

    public static DataMatrix toMatrix(SingletonTask task, DataTable dataTable) {
        if (task == null || dataTable == null || !dataTable.isValid()) {
            return null;
        }
        List<List<String>> rows = new ArrayList<>();
        TableData2D tableData2D = dataTable.getTableData2D();
        List<Data2DColumn> dataColumns = dataTable.getColumns();
        int tcolsNumber = dataColumns.size(), trowsNumber = 0;
        try ( Connection conn = DerbyBase.getConnection();
                 PreparedStatement statement = conn.prepareStatement(tableData2D.queryAllStatement());
                 ResultSet results = statement.executeQuery()) {
            while (results.next() && task != null && !task.isCancelled()) {
                try {
                    List<String> row = new ArrayList<>();
                    for (int col = 0; col < tcolsNumber; col++) {
                        Data2DColumn column = dataColumns.get(col);
                        Object v = results.getObject(column.getColumnName());
                        row.add(column.toString(v));
                    }
                    rows.add(row);
                    trowsNumber++;
                } catch (Exception e) {  // skip  bad lines
                    MyBoxLog.error(e);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
            return null;
        }
        DataMatrix matrix = new DataMatrix();
        if (DataMatrix.save(task, matrix, dataColumns, rows)) {
            return matrix;
        } else {
            return null;
        }
    }

    public static DataClipboard toClip(SingletonTask task, DataTable dataTable) {
        if (task == null || dataTable == null || !dataTable.isValid()) {
            return null;
        }
        File clipFile = DataClipboard.newFile();
        DataFileCSV csvData = toCSV(task, dataTable, clipFile, false);
        if (csvData != null && clipFile != null && clipFile.exists()) {
            return DataClipboard.create(task, csvData.getColumns(), clipFile, csvData.getRowsNumber(), csvData.getColsNumber());
        } else {
            return null;
        }
    }

    public static String toString(SingletonTask task, DataTable dataTable) {
        if (task == null || dataTable == null || !dataTable.isValid()) {
            return null;
        }
        File txtFile = TmpFileTools.getPathTempFile(AppPaths.getGeneratedPath(), dataTable.shortName(), ".txt");
        DataFileCSV csvData = toCSV(task, dataTable, txtFile, false);
        if (csvData != null && txtFile != null && txtFile.exists()) {
            return TextFileTools.readTexts(txtFile);
        } else {
            return null;
        }
    }

    /*  
        to/from CSV
     */
    public static DataFileCSV save(SingletonTask task, ResultSet results, boolean showRowNumber) {
        try {
            if (results == null) {
                return null;
            }
            File csvFile = TmpFileTools.csvFile();
            long count = 0;
            int colsSize;
            List<Data2DColumn> db2Columns = new ArrayList<>();
            try ( CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
                List<String> names = new ArrayList<>();
                if (showRowNumber) {
                    names.add(message("SourceRowNumber"));
                }
                ResultSetMetaData meta = results.getMetaData();

                for (int col = 1; col <= meta.getColumnCount(); col++) {
                    String name = meta.getColumnName(col);
                    names.add(name);
                    Data2DColumn dc = new Data2DColumn(name,
                            ColumnDefinition.sqlColumnType(meta.getColumnType(col)),
                            meta.isNullable(col) == ResultSetMetaData.columnNoNulls);
                    db2Columns.add(dc);
                }
                csvPrinter.printRecord(names);
                colsSize = names.size();
                List<String> fileRow = new ArrayList<>();
                while (results.next() && task != null && !task.isCancelled()) {
                    count++;
                    if (showRowNumber) {
                        fileRow.add(count + "");
                    }
                    for (Data2DColumn column : db2Columns) {
                        Object v = results.getObject(column.getColumnName());
                        fileRow.add(v == null ? "" : column.toString(v));
                    }
                    csvPrinter.printRecord(fileRow);
                    fileRow.clear();
                }
            } catch (Exception e) {
                MyBoxLog.error(e.toString());
                return null;
            }
            DataFileCSV targetData = new DataFileCSV();
            targetData.setFile(csvFile).setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(true)
                    .setColsNumber(colsSize).setRowsNumber(count);
            if (showRowNumber) {
                db2Columns.add(0, new Data2DColumn(message("SourceRowNumber"), ColumnDefinition.ColumnType.Long));
            }
            targetData.setColumns(db2Columns);
            return targetData;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static DataFileExcel toExcel(SingletonTask task, DataFileCSV csvData) {
        if (task == null || csvData == null) {
            return null;
        }
        File csvFile = csvData.getFile();
        if (csvFile == null || !csvFile.exists() || csvFile.length() == 0) {
            return null;
        }
        File excelFile = new File(FileNameTools.replaceSuffix(csvFile.getAbsolutePath(), "xlsx"));
        boolean targetHasHeader = false;
        int tcolsNumber = 0, trowsNumber = 0;
        String targetSheetName = message("Sheet") + "1";
        File validFile = FileTools.removeBOM(csvFile);
        try ( CSVParser parser = CSVParser.parse(validFile, csvData.getCharset(), csvData.cvsFormat());
                 Workbook targetBook = new XSSFWorkbook()) {
            Sheet targetSheet = targetBook.createSheet(targetSheetName);
            int targetRowIndex = 0;
            Iterator<CSVRecord> iterator = parser.iterator();
            if (iterator != null) {
                if (csvData.isHasHeader()) {
                    try {
                        List<String> names = parser.getHeaderNames();
                        if (names != null) {
                            Row targetRow = targetSheet.createRow(targetRowIndex++);
                            for (int col = 0; col < names.size(); col++) {
                                Cell targetCell = targetRow.createCell(col, CellType.STRING);
                                targetCell.setCellValue(names.get(col));
                            }
                            tcolsNumber = names.size();
                            targetHasHeader = true;
                        }
                    } catch (Exception e) {  // skip  bad lines
                        MyBoxLog.error(e);
                    }
                }
                while (iterator.hasNext() && task != null && !task.isCancelled()) {
                    try {
                        CSVRecord record = iterator.next();
                        if (record != null) {
                            Row targetRow = targetSheet.createRow(targetRowIndex++);
                            for (int col = 0; col < record.size(); col++) {
                                Cell targetCell = targetRow.createCell(col, CellType.STRING);
                                targetCell.setCellValue(record.get(col));
                            }
                            trowsNumber++;
                        }
                    } catch (Exception e) {  // skip  bad lines
                        MyBoxLog.error(e);
                    }
                }
                try ( FileOutputStream fileOut = new FileOutputStream(excelFile)) {
                    targetBook.write(fileOut);
                }
                targetBook.close();
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (excelFile != null && excelFile.exists()) {
            DataFileExcel targetData = new DataFileExcel();
            targetData.setColumns(csvData.getColumns())
                    .setFile(excelFile).setSheet(targetSheetName)
                    .setHasHeader(targetHasHeader)
                    .setColsNumber(tcolsNumber).setRowsNumber(trowsNumber);
            targetData.saveAttributes();
            return targetData;
        } else {
            return null;
        }
    }

    public static DataFileText toText(DataFileCSV csvData) {
        if (csvData == null) {
            return null;
        }
        File csvFile = csvData.getFile();
        if (csvFile == null || !csvFile.exists() || csvFile.length() == 0) {
            return null;
        }
        File txtFile = new File(FileNameTools.replaceSuffix(csvFile.getAbsolutePath(), "txt"));
        if (FileCopyTools.copyFile(csvFile, txtFile)) {
            DataFileText targetData = new DataFileText();
            targetData.cloneAll(csvData);
            targetData.setType(Type.Texts).setFile(txtFile);
            targetData.saveAttributes();
            return targetData;
        } else {
            return null;
        }
    }

    public static DataMatrix toMatrix(SingletonTask task, DataFileCSV csvData) {
        if (task == null || csvData == null) {
            return null;
        }
        File csvFile = csvData.getFile();
        if (csvFile == null || !csvFile.exists() || csvFile.length() == 0) {
            return null;
        }
        List<List<String>> data = csvData.allRows(false);
        List<Data2DColumn> cols = csvData.getColumns();
        if (cols == null || cols.isEmpty()) {
            try ( Connection conn = DerbyBase.getConnection()) {
                csvData.readColumns(conn);
                cols = csvData.getColumns();
                if (cols == null || cols.isEmpty()) {
                    return null;
                }
            } catch (Exception e) {
                if (task != null) {
                    task.setError(e.toString());
                }
                MyBoxLog.error(e);
                return null;
            }
        }
        DataMatrix matrix = new DataMatrix();
        if (DataMatrix.save(task, matrix, cols, data)) {
            return matrix;
        } else {
            return null;
        }
    }

    public static DataClipboard toClip(SingletonTask task, DataFileCSV csvData) {
        if (task == null || csvData == null) {
            return null;
        }
        File csvFile = csvData.getFile();
        if (csvFile == null || !csvFile.exists() || csvFile.length() == 0) {
            return null;
        }
        List<Data2DColumn> cols = csvData.getColumns();
        if (cols == null || cols.isEmpty()) {
            try ( Connection conn = DerbyBase.getConnection()) {
                csvData.readColumns(conn);
                cols = csvData.getColumns();
                if (cols == null || cols.isEmpty()) {
                    return null;
                }
            } catch (Exception e) {
                if (task != null) {
                    task.setError(e.toString());
                }
                MyBoxLog.error(e);
                return null;
            }
        }
        File dFile = DataClipboard.newFile();
        if (FileCopyTools.copyFile(csvFile, dFile, true, true)) {
            return DataClipboard.create(task, cols, dFile, csvData.getRowsNumber(), cols.size());
        } else {
            MyBoxLog.error("Failed");
            return null;
        }
    }

}
