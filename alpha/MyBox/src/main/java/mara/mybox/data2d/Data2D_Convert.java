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
import java.util.Iterator;
import java.util.List;
import mara.mybox.data2d.reader.Data2DOperator;
import mara.mybox.data2d.reader.Data2DSingleColumn;
import mara.mybox.data2d.reader.Data2DWriteTable;
import mara.mybox.db.Database;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.InvalidAs;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DRow;
import mara.mybox.db.table.TableData2D;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.CsvTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.FileCopyTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TextFileTools;
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

    public static File targetFile(String prefix, TargetType type) {
        if (type == null) {
            return null;
        }
        return FileTmpTools.generateFile(prefix,
                type == TargetType.Excel ? "xlsx" : type.name().toLowerCase());
    }

    /*
        to/from database table
     */
    public DataTable toTable(FxTask task, String targetName) {
        if (targetName == null || targetName.isBlank()) {
            return null;
        }
        DataTable dataTable = null;
        try (Connection conn = DerbyBase.getConnection()) {
            dataTable = toTable(task, conn, targetName);
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
        }
        return dataTable;
    }

    public DataTable toTable(FxTask task, Connection conn, String targetName) {
        if (conn == null) {
            return null;
        }
        DataTable dataTable = null;
        try {
            dataTable = newTable(task, conn, targetName);
            importTable(task, conn, dataTable);
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
        }
        return dataTable;
    }

    public DataTable newTable(FxTask task, Connection conn, String targetName) {
        if (conn == null) {
            return null;
        }
        DataTable dataTable = null;
        try {
            String tableName = targetName;
            if (tableName == null || tableName.isBlank()) {
                tableName = "Data2D";
            }
            tableName = DerbyBase.fixedIdentifier(tableName);
            int index = 1;
            while (DerbyBase.exist(conn, tableName) > 0) {
                tableName = DerbyBase.appendIdentifier(tableName, ++index + "");
            }
            dataTable = createTable(task, conn, tableName, false);
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
        }
        return dataTable;
    }

    public DataTable createTable(FxTask task, Connection conn, String targetName, boolean dropExisted) {
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
            dataTable.cloneDataAttributes(this);
            dataTable.setDataName(targetName);
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

    public long importTable(FxTask task, Connection conn,
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
            }
            MyBoxLog.error(e);
            return -5;
        }
    }

    public long importTable(FxTask task, Connection conn, DataTable dataTable) {
        return importTable(task, conn, dataTable, null, false, InvalidAs.Blank);
    }

    public DataTable singleColumn(FxTask task, List<Integer> cols) {
        try (Connection conn = DerbyBase.getConnection()) {
            if (columns == null || columns.isEmpty()) {
                readColumns(conn);
            }
            if (columns == null || columns.isEmpty()) {
                return null;
            }
            List<Data2DColumn> referColumns = new ArrayList<>();
            referColumns.add(new Data2DColumn("data", ColumnDefinition.ColumnType.Double));
            DataTable dataTable = createTable(task, conn, TmpTable.tmpTableName(), referColumns, null, comments, null, true);
            dataTable.setDataName(dataName());
            dataTable.cloneDataAttributes(this);
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
                MyBoxLog.error(e);
            }
            return null;
        }
    }

    /* 
       static methods for table
     */
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
                dataColumn.setD2id(-1).setD2cid(-1)
                        .setAuto(false).setIsPrimaryKey(false);
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
            dataTable.setColumns(tableColumns)
                    .setTask(task).setSheet(tableName);
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

    public static DataTable createTable(FxTask task, Connection conn,
            String targetName, List<Data2DColumn> referColumns, List<String> keys, String comments,
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

    public static DataTable createTable(FxTask task, Connection conn, List<Data2DColumn> columns) {
        return createTable(task, conn, null, columns, null, null, null, true);
    }

    public static DataTable createTable(FxTask task, List<Data2DColumn> cols, List<List<String>> pageRows,
            String targetName, InvalidAs invalidAs) {
        if (cols == null || pageRows == null || pageRows.isEmpty()) {
            return null;
        }
        DataTable dataTable = new DataTable();
        dataTable.setColumns(cols);
        try (Connection conn = DerbyBase.getConnection()) {
            dataTable = dataTable.newTable(task, conn, targetName);
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
                    data2DRow.setColumnValue(column.getColumnName(),
                            column.fromString(row.get(i + 1), invalidAs));
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
            return null;
        }
        return dataTable;
    }

    public static DataFileCSV toCSV(FxTask task, DataTable dataTable, File file, boolean save) {
        if (task == null || dataTable == null || !dataTable.isValid() || file == null) {
            return null;
        }
        TableData2D tableData2D = dataTable.getTableData2D();
        List<Data2DColumn> dataColumns = dataTable.getColumns();
        int tcolsNumber = dataColumns.size(), trowsNumber = 0;
        String sql = tableData2D.queryAllStatement();
        if (task != null) {
            task.setInfo(sql);
        }
        try (Connection conn = DerbyBase.getConnection();
                PreparedStatement statement = conn.prepareStatement(sql);
                ResultSet results = statement.executeQuery();
                CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(file, Charset.forName("UTF-8")), CsvTools.csvFormat(",", true))) {
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
                } catch (Exception e) {  // skip  bad lines
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
            targetData.setColumns(dataTable.getColumns())
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

    public static DataFileCSV toCSV(FxTask task, DataTable dataTable) {
        File csvFile = dataTable.tmpFile(dataTable.dataName(), null, "csv");
        return toCSV(task, dataTable, csvFile, true);
    }

    public static DataFileText toText(FxTask task, DataTable dataTable) {
        if (task == null || dataTable == null || !dataTable.isValid()) {
            return null;
        }
        File txtFile = dataTable.tmpFile(dataTable.dataName(), null, "txt");
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

    public static DataFileExcel toExcel(FxTask task, DataTable dataTable) {
        if (task == null || dataTable == null || !dataTable.isValid()) {
            return null;
        }
        File excelFile = dataTable.tmpFile(dataTable.dataName(), null, "xlsx");
        String targetSheetName = message("Sheet") + "1";
        TableData2D tableData2D = dataTable.getTableData2D();
        List<Data2DColumn> dataColumns = dataTable.getColumns();
        int tcolsNumber = dataColumns.size(), trowsNumber = 0;
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
                } catch (Exception e) {  // skip  bad lines
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
            targetData.setColumns(dataTable.getColumns())
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

    public static DataMatrix toMatrix(FxTask task, DataTable dataTable) {
        if (task == null || dataTable == null || !dataTable.isValid()) {
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
                } catch (Exception e) {  // skip  bad lines
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
        matrix.cloneDataAttributes(dataTable);
        if (DataMatrix.save(task, matrix, dataColumns, rows)) {
            return matrix;
        } else {
            return null;
        }
    }

    public static DataClipboard toClip(FxTask task, DataTable dataTable) {
        if (task == null || dataTable == null || !dataTable.isValid()) {
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

    public static String toString(FxTask task, DataTable dataTable) {
        if (task == null || dataTable == null || !dataTable.isValid()) {
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

    public static DataFileCSV save(FxTask task, ResultSet results) {
        return save(null, null, task, results, null, 8, InvalidAs.Blank);
    }

    public static DataFileCSV save(DataTable dataTable, String dname, FxTask task,
            ResultSet results, String rowNumberName, int dscale, InvalidAs invalidAs) {
        try {
            if (results == null) {
                return null;
            }
            File csvFile = dataTable != null ? dataTable.tmpFile(dname, null, "csv")
                    : FileTmpTools.tmpFile(dname, "csv");
            long count = 0;
            int colsSize;
            List<Data2DColumn> db2Columns = new ArrayList<>();
            List<String> fileRow = new ArrayList<>();
            try (CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
                List<String> names = new ArrayList<>();
                if (rowNumberName != null) {
                    names.add(rowNumberName);
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
                        dc = new Data2DColumn(name,
                                ColumnDefinition.sqlColumnType(meta.getColumnType(col)),
                                meta.isNullable(col) == ResultSetMetaData.columnNoNulls);
                    }
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
                        Object v = column.value(results);
                        String s = column.toString(v);
                        if (column.needScale()) {
                            s = DoubleTools.scaleString(s, invalidAs, dscale);
                        }
                        fileRow.add(s);
                    }
                    csvPrinter.printRecord(fileRow);
                    fileRow.clear();
                }
            } catch (Exception e) {
                if (task != null) {
                    task.setError(e.toString());
                } else {
                    MyBoxLog.error(e);
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
                MyBoxLog.error(e);
            }
            return null;
        }
    }


    /*  
        to/from CSV
     */
    public static DataFileCSV toCSV(FxTask task, DataFileCSV csvData,
            String targetName, File targetFile) {
        if (csvData == null) {
            return null;
        }
        File csvFile = csvData.getFile();
        if (csvFile == null || !csvFile.exists() || csvFile.length() == 0) {
            return null;
        }
        File tcsvFile = targetFile != null ? targetFile
                : csvData.tmpFile(csvData.dataName(), null, "csv");
        if (FileCopyTools.copyFile(csvFile, tcsvFile)) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.cloneAttributes(csvData);
            targetData.setFile(tcsvFile);
            if (targetName != null) {
                targetData.setDataName(targetName);
            }
            targetData.saveAttributes();
            return targetData;
        } else {
            return null;
        }
    }

    public static DataFileExcel toExcel(FxTask task, DataFileCSV csvData,
            String targetName, File targetFile) {
        if (task == null || csvData == null) {
            return null;
        }
        File csvFile = csvData.getFile();
        if (csvFile == null || !csvFile.exists() || csvFile.length() == 0) {
            return null;
        }
        File excelFile = csvData.tmpFile(csvData.dataName(), null, "xlsx");
        boolean targetHasHeader = false;
        int tcolsNumber = 0, trowsNumber = 0;
        String targetSheetName = message("Sheet") + "1";
        File validFile = FileTools.removeBOM(task, csvFile);
        if (validFile == null || (task != null && !task.isWorking())) {
            return null;
        }
        try (CSVParser parser = CSVParser.parse(validFile, csvData.getCharset(), csvData.cvsFormat());
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
                try (FileOutputStream fileOut = new FileOutputStream(excelFile)) {
                    targetBook.write(fileOut);
                }
                targetBook.close();
            }
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
            targetData.cloneAttributes(csvData);
            if (targetFile != null) {
                if (!FileCopyTools.copyFile(excelFile, targetFile)) {
                    return null;
                }
            } else {
                targetData.setFile(excelFile);
            }
            targetData.setColumns(csvData.getColumns())
                    .setSheet(targetSheetName)
                    .setHasHeader(targetHasHeader);
            if (targetName != null) {
                targetData.setDataName(targetName);
            }
            targetData.setColsNumber(tcolsNumber).setRowsNumber(trowsNumber);
            targetData.saveAttributes();
            return targetData;
        } else {
            return null;
        }
    }

    public static DataFileText toText(FxTask task, DataFileCSV csvData,
            String targetName, File targetFile) {
        if (csvData == null) {
            return null;
        }
        File csvFile = csvData.getFile();
        if (csvFile == null || !csvFile.exists() || csvFile.length() == 0) {
            return null;
        }
        File txtFile = targetFile != null ? targetFile
                : csvData.tmpFile(csvData.dataName(), null, "txt");
        if (FileCopyTools.copyFile(csvFile, txtFile)) {
            DataFileText targetData = new DataFileText();
            targetData.cloneAttributes(csvData);
            targetData.setFile(txtFile);
            if (targetName != null) {
                targetData.setDataName(targetName);
            }
            targetData.saveAttributes();
            return targetData;
        } else {
            return null;
        }
    }

    public static DataMatrix toMatrix(FxTask task, Data2D sourceData, String targetName) {
        if (task == null || sourceData == null) {
            return null;
        }
        try (Connection conn = DerbyBase.getConnection()) {
            return toMatrix(task, conn, sourceData, targetName);
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return null;
        }
    }

    public static DataMatrix toMatrix(FxTask task, Connection conn,
            Data2D sourceData, String targetName) {
        if (conn == null || task == null || sourceData == null) {
            return null;
        }
        File csvFile = sourceData.getFile();
        if (csvFile == null || !csvFile.exists() || csvFile.length() == 0) {
            return null;
        }
        List<List<String>> data = sourceData.allRows(false);
        List<Data2DColumn> cols = sourceData.getColumns();
        if (cols == null || cols.isEmpty()) {
            try {
                sourceData.readColumns(conn);
                cols = sourceData.getColumns();
                if (cols == null || cols.isEmpty()) {
                    return null;
                }
            } catch (Exception e) {
                if (task != null) {
                    task.setError(e.toString());
                } else {
                    MyBoxLog.error(e);
                }
                return null;
            }
        }
        DataMatrix matrix = new DataMatrix();
        matrix.cloneAttributes(sourceData);
        if (targetName != null) {
            matrix.setDataName(targetName);
        }
        if (DataMatrix.save(task, conn, matrix, cols, data)) {
            return matrix;
        } else {
            return null;
        }
    }

    public static DataClipboard toClip(FxTask task, DataFileCSV csvData, String targetName) {
        if (task == null || csvData == null) {
            return null;
        }
        File csvFile = csvData.getFile();
        if (csvFile == null || !csvFile.exists() || csvFile.length() == 0) {
            return null;
        }
        List<Data2DColumn> cols = csvData.getColumns();
        if (cols == null || cols.isEmpty()) {
            try (Connection conn = DerbyBase.getConnection()) {
                csvData.readColumns(conn);
                cols = csvData.getColumns();
                if (cols == null || cols.isEmpty()) {
                    return null;
                }
            } catch (Exception e) {
                if (task != null) {
                    task.setError(e.toString());
                } else {
                    MyBoxLog.error(e);
                }
                return null;
            }
        }
        File clipFile = DataClipboard.newFile();
        if (FileCopyTools.copyFile(csvFile, clipFile, true, true)) {
            return DataClipboard.create(task, csvData, targetName, clipFile);
        } else {
            return null;
        }
    }

}
