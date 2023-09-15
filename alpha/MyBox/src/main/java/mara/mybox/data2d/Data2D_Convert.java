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
import mara.mybox.controller.BaseController;
import mara.mybox.controller.Data2DTargetExportController;
import mara.mybox.controller.DataFileCSVController;
import mara.mybox.controller.DataFileExcelController;
import mara.mybox.controller.DataFileTextController;
import mara.mybox.controller.DataInMyBoxClipboardController;
import mara.mybox.controller.DataTablesController;
import mara.mybox.controller.MatricesManageController;
import mara.mybox.data2d.reader.Data2DOperator;
import mara.mybox.data2d.reader.Data2DSingleColumn;
import mara.mybox.data2d.reader.Data2DWriteTable;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.InvalidAs;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.table.TableData2D;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.tools.CsvTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.FileCopyTools;
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

    /*
        to/from database table
     */
    public DataTable toTable(SingletonTask task, String targetName) {
        DataTable dataTable = null;
        try (Connection conn = DerbyBase.getConnection()) {
            String tableName = DerbyBase.fixedIdentifier(targetName);
            int index = 1;
            while (DerbyBase.exist(conn, tableName) > 0) {
                tableName = DerbyBase.appendIdentifier(tableName, ++index + "");
            }
            dataTable = createTable(task, conn, tableName, false);
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
                MyBoxLog.error(e);
            }
            return null;
        }
    }

    public long importTable(SingletonTask task, Connection conn,
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

    public long importTable(SingletonTask task, Connection conn, DataTable dataTable) {
        return importTable(task, conn, dataTable, null, false, InvalidAs.Blank);
    }

    public DataTable singleColumn(SingletonTask task, List<Integer> cols) {
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
                MyBoxLog.error(e);
            }
            return null;
        }
    }

    /* 
        static
     */
    public static DataTable makeTable(SingletonTask task, String tname,
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

    public static DataTable createTable(SingletonTask task, Connection conn,
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

    public static DataTable createTable(SingletonTask task, Connection conn, List<Data2DColumn> columns) {
        return createTable(task, conn, null, columns, null, null, null, true);
    }

    public static DataFileCSV toCSV(SingletonTask task, DataTable dataTable, File file, boolean save) {
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

    public static DataFileCSV toCSV(SingletonTask task, DataTable dataTable) {
        File csvFile = dataTable.tmpFile(dataTable.dataName(), null, "csv");
        return toCSV(task, dataTable, csvFile, true);
    }

    public static DataFileText toText(SingletonTask task, DataTable dataTable) {
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

    public static DataFileExcel toExcel(SingletonTask task, DataTable dataTable) {
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

    public static DataMatrix toMatrix(SingletonTask task, DataTable dataTable) {
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
        File txtFile = dataTable.tmpFile(dataTable.dataName(), null, "txt");
        DataFileCSV csvData = toCSV(task, dataTable, txtFile, false);
        if (csvData != null && txtFile != null && txtFile.exists()) {
            return TextFileTools.readTexts(txtFile);
        } else {
            return null;
        }
    }

    public static DataFileCSV save(SingletonTask task, ResultSet results) {
        return save(null, null, task, results, null, 8, InvalidAs.Blank);
    }

    public static DataFileCSV save(DataTable dataTable, String dname, SingletonTask task,
            ResultSet results, String rowNumberName, int dscale, InvalidAs invalidAs) {
        try {
            if (results == null) {
                return null;
            }
            File csvFile = dataTable.tmpFile(dname, null, "csv");
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

    public static void openDataTable(BaseController controller, DataTable dataTable, String target) {
        if (dataTable == null || target == null) {
            return;
        }
        if ("csv".equals(target)) {
            DataFileCSVController.loadTable(dataTable);
        } else if ("excel".equals(target)) {
            DataFileExcelController.loadTable(dataTable);
        } else if ("texts".equals(target)) {
            DataFileTextController.loadTable(dataTable);
        } else if ("matrix".equals(target)) {
            MatricesManageController.loadTable(dataTable);
        } else if ("systemClipboard".equals(target)) {
            TextClipboardTools.copyToSystemClipboard(controller, DataTable.toString(null, dataTable));
        } else if ("myBoxClipboard".equals(target)) {
            DataInMyBoxClipboardController.loadTable(dataTable);
        } else if ("table".equals(target)) {
            DataTablesController.loadTable(dataTable);
        }
    }

    /*  
        to/from CSV
     */
    public static DataFileExcel toExcel(SingletonTask task, DataFileCSV csvData) {
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
        File validFile = FileTools.removeBOM(csvFile);
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
        File txtFile = csvData.tmpFile(csvData.dataName(), null, "txt");
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
        File dFile = DataClipboard.newFile();
        if (FileCopyTools.copyFile(csvFile, dFile, true, true)) {
            return DataClipboard.create(task, csvData, dFile);
        } else {
            return null;
        }
    }

    public static void openCSV(BaseController controller, DataFileCSV csvFile, String target) {
        if (csvFile == null || target == null) {
            return;
        }
        if ("csv".equals(target)) {
            DataFileCSVController.loadCSV(csvFile);
        } else if ("excel".equals(target)) {
            DataFileExcelController.loadCSV(csvFile);
        } else if ("texts".equals(target)) {
            DataFileTextController.loadCSV(csvFile);
        } else if ("matrix".equals(target)) {
            MatricesManageController.loadCSV(csvFile);
        } else if ("systemClipboard".equals(target)) {
            TextClipboardTools.copyToSystemClipboard(controller, TextFileTools.readTexts(csvFile.getFile()));
        } else if ("myBoxClipboard".equals(target)) {
            DataInMyBoxClipboardController.loadCSV(csvFile);
        } else if ("table".equals(target)) {
            DataTablesController.loadCSV(csvFile);
        } else {
            Data2DTargetExportController.open(csvFile, target);
        }
    }

}
