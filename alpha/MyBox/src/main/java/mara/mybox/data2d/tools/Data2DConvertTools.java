package mara.mybox.data2d.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import mara.mybox.data.StringTable;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.Data2D_Attributes;
import mara.mybox.data2d.DataClipboard;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataFileExcel;
import mara.mybox.data2d.DataFileText;
import mara.mybox.data2d.DataTable;
import mara.mybox.data2d.TmpTable;
import mara.mybox.data2d.operate.Data2DOperate;
import mara.mybox.data2d.operate.Data2DSingleColumn;
import mara.mybox.data2d.writer.DataFileCSVWriter;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.CsvTools;
import mara.mybox.tools.FileCopyTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TextFileTools;
import static mara.mybox.value.Languages.message;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
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
public class Data2DConvertTools {

    public static File targetFile(String prefix, Data2D_Attributes.TargetType type) {
        if (type == null) {
            return null;
        }
        String ext;
        switch (type) {
            case Excel:
                ext = "xlsx";
                break;
            case Text:
            case Matrix:
                ext = "txt";
                break;
            default:
                ext = type.name().toLowerCase();
                break;
        }
        return FileTmpTools.generateFile(prefix, ext);
    }

    public static DataFileText toText(FxTask task, DataFileCSV csvData, String targetName, File targetFile) {
        if (csvData == null) {
            return null;
        }
        File csvFile = csvData.getFile();
        if (csvFile == null || !csvFile.exists() || csvFile.length() == 0) {
            return null;
        }
        File txtFile = targetFile != null ? targetFile : csvData.tmpFile(csvData.getName(), null, "txt");
        if (FileCopyTools.copyFile(csvFile, txtFile)) {
            DataFileText targetData = new DataFileText();
            targetData.copyAttributesFrom(csvData);
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
                csvData.loadColumns(conn);
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

    public static DataFileCSV toCSV(FxTask task, DataFileCSV csvData, String targetName, File targetFile) {
        if (csvData == null) {
            return null;
        }
        File csvFile = csvData.getFile();
        if (csvFile == null || !csvFile.exists() || csvFile.length() == 0) {
            return null;
        }
        File tcsvFile = targetFile != null ? targetFile : csvData.tmpFile(csvData.getName(), null, "csv");
        if (FileCopyTools.copyFile(csvFile, tcsvFile)) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.copyAttributesFrom(csvData);
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

    public static DataFileExcel toExcel(FxTask task, DataFileCSV csvData, String targetName, File targetFile) {
        if (task == null || csvData == null) {
            return null;
        }
        File csvFile = csvData.getFile();
        if (csvFile == null || !csvFile.exists() || csvFile.length() == 0) {
            return null;
        }
        File excelFile = csvData.tmpFile(csvData.getName(), null, "xlsx");
        boolean targetHasHeader = false;
        int tcolsNumber = 0;
        int trowsNumber = 0;
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
                    } catch (Exception e) {
                        // skip  bad lines
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
                    } catch (Exception e) {
                        // skip  bad lines
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
            targetData.copyAttributesFrom(csvData);
            if (targetFile != null) {
                if (!FileCopyTools.copyFile(excelFile, targetFile)) {
                    return null;
                }
            } else {
                targetData.setFile(excelFile);
            }
            targetData.setColumns(csvData.getColumns()).setSheet(targetSheetName).setHasHeader(targetHasHeader);
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

    public static DataFileCSV write(FxTask task, ResultSet results) {
        try {
            DataFileCSVWriter writer = new DataFileCSVWriter();
            writer.setPrintFile(FileTmpTools.getTempFile(".csv"));
            if (!Data2DTableTools.write(task, null, writer, results, null, 8, ColumnDefinition.InvalidAs.Empty)) {
                return null;
            }
            return (DataFileCSV) writer.getTargetData();
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return null;
        }
    }

    public static DataTable singleColumn(FxTask task, Data2D sourceData, List<Integer> cols) {
        if (sourceData == null) {
            return null;
        }
        try (Connection conn = DerbyBase.getConnection()) {
            List<Data2DColumn> columns = sourceData.getColumns();
            if (columns == null || columns.isEmpty()) {
                sourceData.loadColumns(conn);
            }
            if (columns == null || columns.isEmpty()) {
                return null;
            }
            List<Data2DColumn> referColumns = new ArrayList<>();
            referColumns.add(new Data2DColumn("data", ColumnDefinition.ColumnType.Double));
            DataTable dataTable = Data2DTableTools.createTable(task, conn,
                    TmpTable.tmpTableName(), referColumns, null, sourceData.getComments(), null, true);
            dataTable.setDataName(sourceData.getName());
            dataTable.copyDataAttributes(sourceData);
            if (cols == null || cols.isEmpty()) {
                cols = new ArrayList<>();
                for (int i = 0; i < columns.size(); i++) {
                    cols.add(i);
                }
            }
            Data2DOperate reader = Data2DSingleColumn.create(sourceData)
                    .setConn(conn).setWriterTable(dataTable)
                    .setCols(cols).setTask(task).start();
            if (reader != null && !reader.isFailed()) {
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

    public static String toHtml(FxTask task, DataFileCSV data) {
        StringTable table = new StringTable(data.getDataName());
        File file = FileTools.removeBOM(task, data.getFile());
        if (file == null) {
            return null;
        }
        try (CSVParser parser = CSVParser.parse(file, data.getCharset(),
                CsvTools.csvFormat(data.getDelimiter(), data.isHasHeader()))) {
            if (data.isHasHeader()) {
                List<String> names = parser.getHeaderNames();
                table.setNames(names);
            }
            Iterator<CSVRecord> iterator = parser.iterator();
            while (iterator.hasNext() && (task == null || task.isWorking())) {
                CSVRecord csvRecord = iterator.next();
                if (csvRecord == null) {
                    continue;
                }
                List<String> htmlRow = new ArrayList<>();
                for (String v : csvRecord) {
                    htmlRow.add(v != null ? "<PRE><CODE>" + v + "</PRE></CODE>" : null);
                }
                table.add(htmlRow);
            }
            return table.html();
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return null;
        }
    }

    public static File toHtmlFile(FxTask task, DataFileCSV data, File htmlFile) {
        String html = toHtml(task, data);
        if (html == null) {
            return null;
        }
        return TextFileTools.writeFile(htmlFile, html);
    }

}
