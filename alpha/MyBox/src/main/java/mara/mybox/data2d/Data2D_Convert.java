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
import java.util.Random;
import mara.mybox.data2d.reader.Data2DOperator;
import mara.mybox.data2d.reader.Data2DSingleColumn;
import mara.mybox.data2d.reader.Data2DWriteTable;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.table.TableData2D;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.tools.CsvTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileCopyTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.TmpFileTools;
import static mara.mybox.tools.TmpFileTools.getPathTempFile;
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
        DataTable dataTable = null;
        try ( Connection conn = DerbyBase.getConnection()) {
            dataTable = createTable(task, conn, targetName, dropExisted);
            writeTableData(task, conn, dataTable);
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
        }
        return dataTable;
    }

    public DataTable createTable(SingletonTask task, Connection conn, String targetName, boolean dropExisted) {
        try {
            if (columns == null || columns.isEmpty()) {
                readColumns(conn);
            }
            if (columns == null || columns.isEmpty()) {
                return null;
            }
            DataTable dataTable = createTable(task, conn, targetName, columns, null, comments, null, dropExisted);
            if (dataTable == null) {
                return null;
            }
            dataTable.cloneDefinitionAttributes(this);
            dataTable.setDataName(targetName);
            return dataTable;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
            return null;
        }
    }

    public DataTable toTmpTable(SingletonTask task, List<Integer> cols,
            boolean includeRowNumber, boolean toNumbers, InvalidAs invalidAs) {
        try ( Connection conn = DerbyBase.getConnection()) {
            DataTable dataTable = createTmpTable(task, conn, tmpTableName(dataName()), cols, includeRowNumber, toNumbers);
            writeTableData(task, conn, dataTable, cols, includeRowNumber, invalidAs);
            return dataTable;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
            return null;
        }
    }

    public DataTable toTmpTable(SingletonTask task, List<Integer> cols, List<List<String>> rows,
            boolean includeRowNumber, boolean toNumbers, InvalidAs invalidAs) {
        try ( Connection conn = DerbyBase.getConnection()) {
            DataTable dataTable = createTmpTable(task, conn, tmpTableName(dataName()), cols, includeRowNumber, toNumbers);
            dataTable.save(task, conn, rows, invalidAs);
            return dataTable;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
            return null;
        }
    }

    public DataTable createTmpTable(SingletonTask task, Connection conn,
            String targetName, List<Integer> cols, boolean includeRowNumber, boolean toNumbers) {
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
                    if (toNumbers) {
                        column.setType(ColumnDefinition.ColumnType.Double);
                    }
                    sourceColumns.add(column);
                }
            }
            return createTable(task, conn, targetName, sourceColumns, null, comments, null, true);
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
            return null;
        }
    }

    public long writeTableData(SingletonTask task, Connection conn,
            DataTable dataTable, List<Integer> cols, boolean includeRowNumber, InvalidAs invalidAs) {
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
            Data2DWriteTable reader = Data2DWriteTable.create(this)
                    .setConn(conn).setWriterTable(dataTable);
            if (reader == null) {
                return -3;
            }
            reader.setIncludeRowNumber(includeRowNumber).setInvalidAs(invalidAs)
                    .setCols(cols).setTask(task).start();
            if (!reader.failed()) {
                conn.commit();
                return reader.getCount();
            } else {
                return -4;
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
            return -5;
        }
    }

    public long writeTableData(SingletonTask task, Connection conn, DataTable dataTable) {
        return writeTableData(task, conn, dataTable, null, false, InvalidAs.Blank);
    }

    public DataTable singleColumn(SingletonTask task, List<Integer> cols, boolean asDouble) {
        try ( Connection conn = DerbyBase.getConnection()) {
            if (columns == null || columns.isEmpty()) {
                readColumns(conn);
            }
            if (columns == null || columns.isEmpty()) {
                return null;
            }
            List<Data2DColumn> sourceColumns = new ArrayList<>();
            Data2DColumn column;
            if (asDouble) {
                column = new Data2DColumn("data", ColumnDefinition.ColumnType.Double);
            } else {
                column = new Data2DColumn("data", ColumnDefinition.ColumnType.String);
            }
            column.setD2cid(-1).setD2id(-1);
            sourceColumns.add(column);
            DataTable dataTable = createTable(task, conn, tmpTableName(), sourceColumns, null, comments, null, true);
            dataTable.cloneDefinitionAttributes(this);
            if (cols == null || cols.isEmpty()) {
                cols = new ArrayList<>();
                for (int i = 0; i < columns.size(); i++) {
                    cols.add(i);
                }
            }
            Data2DOperator reader = Data2DSingleColumn.create(this)
                    .setConn(conn).setWriterTable(dataTable)
                    .setCols(cols).setTask(task).start();
            if (reader != null && !reader.failed()) {
                conn.commit();
                return dataTable;
            } else {
                return null;
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
            return null;
        }
    }

    public static DataTable makeTable(SingletonTask task, String tname,
            List<Data2DColumn> sourceColumns, List<String> keys, String idName) {
        try {
            if (sourceColumns == null || sourceColumns.isEmpty()) {
                return null;
            }
            if (tname == null || tname.isBlank()) {
                tname = tmpTableName();
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
                validNames.add(idName.toUpperCase());
            }
            Map<String, String> columnsMap = new HashMap<>();
            Random random = new Random();
            for (Data2DColumn sourceColumn : sourceColumns) {
                Data2DColumn dataColumn = new Data2DColumn();
                dataColumn.cloneFrom(sourceColumn);
                dataColumn.setD2id(-1).setD2cid(-1)
                        .setAuto(false).setIsPrimaryKey(false);
                String sourceColumnName = sourceColumn.getColumnName();
                String columeName = DerbyBase.fixedIdentifier(sourceColumnName);
                while (validNames.contains(columeName.toUpperCase())) {
                    columeName += random.nextInt(10);
                }
                dataColumn.setColumnName(columeName);
                validNames.add(columeName);
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
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
            return null;
        }
    }

    public static DataTable createTable(SingletonTask task, Connection conn,
            String name, List<Data2DColumn> sourceColumns, List<String> keys, String comments,
            String idName, boolean dropExisted) {
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
            dataTable = makeTable(task, tableName, sourceColumns, keys, idName);
            if (dataTable == null) {
                return null;
            }
            tableData2D = dataTable.getTableData2D();
            if (conn.createStatement().executeUpdate(tableData2D.createTableStatement()) < 0) {
                return null;
            }
            conn.commit();
            dataTable.recordTable(conn, tableName, dataTable.getColumns(), comments);
            return dataTable;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
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
                 CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(file, Charset.forName("UTF-8")), CsvTools.csvFormat(",", true))) {
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
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
            return null;
        }
        if (file != null && file.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setColumns(dataTable.sourceColumns())
                    .setDataName(dataTable.dataName())
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
        File csvFile = TmpFileTools.getPathTempFile(AppPaths.getGeneratedPath(), dataTable.dataName(), ".csv");
        return toCSV(task, dataTable, csvFile, true);
    }

    public static DataFileText toText(SingletonTask task, DataTable dataTable) {
        if (task == null || dataTable == null || !dataTable.isValid()) {
            return null;
        }
        File txtFile = TmpFileTools.getPathTempFile(AppPaths.getGeneratedPath(), dataTable.dataName(), ".txt");
        DataFileCSV csvData = toCSV(task, dataTable, txtFile, false);
        if (csvData != null && txtFile != null && txtFile.exists()) {
            DataFileText targetData = new DataFileText();
            targetData.setColumns(csvData.getColumns())
                    .setFile(txtFile).setDataName(csvData.getDataName())
                    .setCharset(Charset.forName("UTF-8")).setDelimiter(",")
                    .setHasHeader(true)
                    .setColsNumber(csvData.getColsNumber())
                    .setRowsNumber(csvData.getRowsNumber());
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
        File excelFile = TmpFileTools.getPathTempFile(AppPaths.getGeneratedPath(), dataTable.dataName(), ".xlsx");
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
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
            return null;
        }
        if (excelFile != null && excelFile.exists()) {
            DataFileExcel targetData = new DataFileExcel();
            targetData.setColumns(dataTable.sourceColumns())
                    .setFile(excelFile).setSheet(targetSheetName)
                    .setDataName(dataTable.dataName())
                    .setHasHeader(true)
                    .setColsNumber(tcolsNumber).setRowsNumber(trowsNumber);
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
        int tcolsNumber = dataColumns.size();
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
                } catch (Exception e) {  // skip  bad lines
                    MyBoxLog.error(e);
                }
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
            return null;
        }
        DataMatrix matrix = new DataMatrix();
        matrix.cloneDefinitionAttributes(dataTable);
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
            return DataClipboard.create(task, csvData, clipFile);
        } else {
            return null;
        }
    }

    public static String toString(SingletonTask task, DataTable dataTable) {
        if (task == null || dataTable == null || !dataTable.isValid()) {
            return null;
        }
        File txtFile = TmpFileTools.getPathTempFile(AppPaths.getGeneratedPath(), dataTable.dataName(), ".txt");
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
    public static DataFileCSV save(String dname, SingletonTask task, ResultSet results, String rowNumberName) {
        try {
            if (results == null) {
                return null;
            }
            File csvFile = getPathTempFile(AppPaths.getGeneratedPath(), dname, ".csv");
            long count = 0;
            int colsSize;
            List<Data2DColumn> db2Columns = new ArrayList<>();
            List<String> fileRow = new ArrayList<>();
            try ( CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
                List<String> names = new ArrayList<>();
                if (rowNumberName != null) {
                    names.add(rowNumberName);
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

                while (results.next() && task != null && !task.isCancelled()) {
                    count++;
                    if (rowNumberName != null) {
                        fileRow.add(rowNumberName + count);
                    }
                    for (Data2DColumn column : db2Columns) {
                        Object v = results.getObject(column.getColumnName());
                        fileRow.add(v == null ? "" : column.toString(v));
                    }
                    csvPrinter.printRecord(fileRow);
                    fileRow.clear();
                }
            } catch (Exception e) {
                if (task != null) {
                    task.setError(e.toString());
                } else {
                    MyBoxLog.error(e.toString());
                }
                return null;
            }
            DataFileCSV targetData = new DataFileCSV();
            targetData.setFile(csvFile).setDataName(dname)
                    .setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(true)
                    .setColsNumber(colsSize).setRowsNumber(count);
            if (rowNumberName != null) {
                db2Columns.add(0, new Data2DColumn(rowNumberName, ColumnDefinition.ColumnType.String));
            }
            targetData.setColumns(db2Columns);
            return targetData;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
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
        File excelFile = getPathTempFile(AppPaths.getGeneratedPath(), csvData.dataName(), ".xlsx");
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
            } else {
                MyBoxLog.error(e.toString());
            }
            return null;
        }
        if (excelFile != null && excelFile.exists()) {
            DataFileExcel targetData = new DataFileExcel();
            targetData.setColumns(csvData.getColumns())
                    .setFile(excelFile).setSheet(targetSheetName)
                    .setHasHeader(targetHasHeader)
                    .cloneDefinitionAttributes(csvData);
            targetData.setColsNumber(tcolsNumber).setRowsNumber(trowsNumber);
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
        File txtFile = getPathTempFile(AppPaths.getGeneratedPath(), csvData.dataName(), ".txt");
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
                } else {
                    MyBoxLog.error(e.toString());
                }
                return null;
            }
        }
        DataMatrix matrix = new DataMatrix();
        matrix.cloneDefinitionAttributes(csvData);
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
                } else {
                    MyBoxLog.error(e.toString());
                }
                return null;
            }
        }
        File dFile = DataClipboard.newFile();
        if (FileCopyTools.copyFile(csvFile, dFile, true, true)) {
            return DataClipboard.create(task, csvData, dFile);
        } else {
            return null;
        }
    }

}
