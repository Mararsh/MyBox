package mara.mybox.data2d;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import mara.mybox.data2d.reader.Data2DReadDefinition;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileCopyTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.MicrosoftDocumentTools;
import static mara.mybox.value.Languages.message;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @Author Mara
 * @CreateDate 2021-10-17
 * @License Apache License Version 2.0
 */
public class DataFileExcel extends DataFile {

    protected List<String> sheetNames;
    protected boolean currentSheetOnly;

    public DataFileExcel() {
        type = Type.Excel;
    }

    public void cloneAll(DataFileExcel d) {
        try {
            if (d == null) {
                return;
            }
            super.cloneAll(d);
            sheetNames = d.sheetNames;
            currentSheetOnly = d.currentSheetOnly;
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void resetData() {
        super.resetData();
        sheetNames = null;
    }

    public void setOptions(boolean hasHeader) {
        options = new HashMap<>();
        options.put("hasHeader", hasHeader);
    }

    @Override
    public void applyOptions() {
        try {
            if (options == null) {
                return;
            }
            if (options.containsKey("hasHeader")) {
                hasHeader = (boolean) (options.get("hasHeader"));
            }
        } catch (Exception e) {
        }
    }

    @Override
    public Data2DDefinition queryDefinition(Connection conn) {
        if (conn == null || type == null || file == null || sheet == null) {
            return null;
        }
        return tableData2DDefinition.queryFileSheet(conn, type, file, sheet);
    }

    public void initFile(File file, String sheetName) {
        super.initFile(file);
        sheet = sheetName;
        sheetNames = null;
    }

    @Override
    public boolean checkForLoad() {
        if (sheet == null) {
            sheet = "sheet1";
        }
        return true;
    }

    @Override
    public boolean checkForSave() {
        if (sheet == null) {
            sheet = "sheet1";
        }
        if (dataName == null || dataName.isBlank()) {
            if (!isTmpData()) {
                dataName = file.getName();
            } else {
                dataName = DateTools.nowString();
            }
            dataName += " - " + sheet;
        }
        return true;
    }

    @Override
    public long readDataDefinition(Connection conn) {
        Data2DReadDefinition reader = Data2DReadDefinition.create(this);
        if (reader == null) {
            hasHeader = false;
            return -2;
        }
        reader.setTask(task).start();
        return super.readDataDefinition(conn);
    }

    @Override
    public boolean savePageData(Data2D targetData) {
        if (targetData == null || !(targetData instanceof DataFileExcel)) {
            return false;
        }
        DataFileExcel targetExcelFile = (DataFileExcel) targetData;
        File tmpFile = FileTmpTools.getTempFile();
        File tFile = targetExcelFile.getFile();
        if (tFile == null) {
            return false;
        }
        targetExcelFile.checkForLoad();
        boolean targetHasHeader = targetExcelFile.isHasHeader();
        String targetSheetName = targetExcelFile.getSheet();
        checkForLoad();
        if (file != null && file.exists() && file.length() > 0) {
            try (Workbook sourceBook = WorkbookFactory.create(file)) {
                Sheet sourceSheet;
                if (sheet != null) {
                    sourceSheet = sourceBook.getSheet(sheet);
                } else {
                    sourceSheet = sourceBook.getSheetAt(0);
                    sheet = sourceSheet.getSheetName();
                }
                if (targetSheetName == null) {
                    targetSheetName = sheet;
                }
                Workbook targetBook;
                Sheet targetSheet;
                File tmpDataFile = null;
                int sheetsNumber = sourceBook.getNumberOfSheets();
                if (sheetsNumber == 1
                        || (!file.equals(tFile) && targetExcelFile.isCurrentSheetOnly())) {
                    targetBook = new XSSFWorkbook();
                    targetSheet = targetBook.createSheet(targetSheetName);
                } else {
                    tmpDataFile = FileTmpTools.getTempFile();
                    FileCopyTools.copyFile(file, tmpDataFile);
                    targetBook = WorkbookFactory.create(tmpDataFile);
                    int index = targetBook.getSheetIndex(sheet);
                    targetBook.removeSheetAt(index);
                    targetSheet = targetBook.createSheet(targetSheetName);
                    targetBook.setSheetOrder(targetSheetName, index);
                }
                int targetRowIndex = 0;
                if (targetHasHeader) {
                    targetRowIndex = writeHeader(targetSheet, targetRowIndex);
                }
                Iterator<Row> iterator = sourceSheet.iterator();
                if (iterator != null && iterator.hasNext()) {
                    if (hasHeader) {
                        while (iterator.hasNext() && (iterator.next() == null) && task != null && !task.isCancelled()) {
                        }
                    }
                    int sourceRowIndex = -1;
                    while (iterator.hasNext() && task != null && !task.isCancelled()) {
                        Row sourceRow = iterator.next();
                        if (sourceRow == null) {
                            continue;
                        }
                        if (++sourceRowIndex < startRowOfCurrentPage || sourceRowIndex >= endRowOfCurrentPage) {
                            Row targetRow = targetSheet.createRow(targetRowIndex++);
                            writeFileRow(sourceRow, targetRow);
                        } else if (sourceRowIndex == startRowOfCurrentPage) {
                            targetRowIndex = writePageData(targetSheet, targetRowIndex);
                        }
                    }
                } else {
                    writePageData(targetSheet, targetRowIndex);
                }
                try (FileOutputStream fileOut = new FileOutputStream(tmpFile)) {
                    targetBook.write(fileOut);
                }
                targetBook.close();
                FileDeleteTools.delete(tmpDataFile);
            } catch (Exception e) {
                MyBoxLog.error(e);
                if (task != null) {
                    task.setError(e.toString());
                }
                return false;
            }

        } else {
            try (Workbook targetBook = new XSSFWorkbook()) {
                if (targetSheetName == null) {
                    targetSheetName = message("Sheet") + "1";
                }
                Sheet targetSheet = targetBook.createSheet(targetSheetName);
                int targetRowIndex = 0;
                if (targetHasHeader) {
                    targetRowIndex = writeHeader(targetSheet, targetRowIndex);
                }
                writePageData(targetSheet, targetRowIndex);
                try (FileOutputStream fileOut = new FileOutputStream(tmpFile)) {
                    targetBook.write(fileOut);
                }
                targetBook.close();
            } catch (Exception e) {
                MyBoxLog.error(e);
                if (task != null) {
                    task.setError(e.toString());
                }
                return false;
            }
        }
        return FileTools.rename(tmpFile, tFile, false);
    }

    public int writeHeader(Sheet targetSheet, int targetRowIndex) {
        if (!isColumnsValid()) {
            return targetRowIndex;
        }
        int index = targetRowIndex;
        Row targetRow = targetSheet.createRow(index++);
        for (int col = 0; col < columns.size(); col++) {
            Cell targetCell = targetRow.createCell(col, CellType.STRING);
            targetCell.setCellValue(columns.get(col).getColumnName());
        }
        return index;
    }

    protected int writePageData(Sheet targetSheet, int targetRowIndex) {
        int index = targetRowIndex;
        try {
            if (!isColumnsValid()) {
                return index;
            }
            for (int r = 0; r < tableRowsNumber(); r++) {
                if (task == null || task.isCancelled()) {
                    return index;
                }
                List<String> values = tableRow(r, false, false);
                Row targetRow = targetSheet.createRow(index++);
                for (int col = 0; col < values.size(); col++) {
                    Cell targetCell = targetRow.createCell(col, CellType.STRING);
                    targetCell.setCellValue(values.get(col));
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
        }
        return index;
    }

    public void writeFileRow(Row sourceRow, Row targetRow) {
        try {
            List<String> row = new ArrayList<>();
            for (int cellIndex = sourceRow.getFirstCellNum(); cellIndex < sourceRow.getLastCellNum(); cellIndex++) {
                String v = MicrosoftDocumentTools.cellString(sourceRow.getCell(cellIndex));
                row.add(v);
            }
            List<String> fileRow = fileRow(row);
            for (int col = 0; col < fileRow.size(); col++) {
                Cell targetCell = targetRow.createCell(col, CellType.STRING);
                targetCell.setCellValue(fileRow.get(col));
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public File tmpFile(List<String> cols, List<List<String>> data) {
        try {
            if (cols == null || cols.isEmpty()) {
                if (data == null || data.isEmpty()) {
                    return null;
                }
            }
            File tmpFile = FileTmpTools.generateFile("xlsx");
            Workbook targetBook = new XSSFWorkbook();
            Sheet targetSheet = targetBook.createSheet(message("Sheet") + "1");
            int targetRowIndex = 0;
            if (cols != null && !cols.isEmpty()) {
                Row targetRow = targetSheet.createRow(targetRowIndex++);
                for (int col = 0; col < cols.size(); col++) {
                    Cell targetCell = targetRow.createCell(col, CellType.STRING);
                    targetCell.setCellValue(cols.get(col));
                }
            }
            if (data != null) {
                for (int r = 0; r < data.size(); r++) {
                    if (task != null && task.isCancelled()) {
                        break;
                    }
                    List<String> values = data.get(r);
                    Row targetRow = targetSheet.createRow(targetRowIndex++);
                    for (int col = 0; col < values.size(); col++) {
                        Cell targetCell = targetRow.createCell(col, CellType.STRING);
                        targetCell.setCellValue(values.get(col));
                    }
                }
            }
            try (FileOutputStream fileOut = new FileOutputStream(tmpFile)) {
                targetBook.write(fileOut);
            }
            return tmpFile;
        } catch (Exception e) {
            MyBoxLog.console(e);
            if (task != null) {
                task.setError(e.toString());
            }
            return null;
        }
    }

    public boolean newSheet(String sheetName) {
        if (file == null || !file.exists() || file.length() == 0) {
            return false;
        }
        File tmpFile = FileTmpTools.getTempFile();
        File tmpDataFile = FileTmpTools.getTempFile();
        if (file.length() > 0) {
            FileCopyTools.copyFile(file, tmpDataFile);
        }
        try (Workbook targetBook = WorkbookFactory.create(tmpDataFile)) {
            Sheet targetSheet = targetBook.createSheet(sheetName);
            List<List<String>> data = tmpData(3, 3);
            for (int r = 0; r < data.size(); r++) {
                if (task == null || task.isCancelled()) {
                    break;
                }
                List<String> values = data.get(r);
                Row targetRow = targetSheet.createRow(r);
                for (int col = 0; col < values.size(); col++) {
                    Cell targetCell = targetRow.createCell(col, CellType.STRING);
                    targetCell.setCellValue(values.get(col));
                }
            }
            try (FileOutputStream fileOut = new FileOutputStream(tmpFile)) {
                targetBook.write(fileOut);
            }
        } catch (Exception e) {
            error = e.toString();
            return false;
        }
        try {
            FileDeleteTools.delete(tmpDataFile);
            if (tmpFile == null || !tmpFile.exists()) {
                return false;
            }
            if (FileTools.rename(tmpFile, file)) {
                initFile(file);
                hasHeader = false;
                sheet = sheetName;
                tableData2DDefinition.insertData(this);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            error = e.toString();
            return false;
        }
    }

    public boolean renameSheet(String newName) {
        if (file == null || !file.exists() || sheet == null) {
            return false;
        }
        String oldName = sheet;
        File tmpFile = FileTmpTools.getTempFile();
        File tmpDataFile = FileTmpTools.getTempFile();
        FileCopyTools.copyFile(file, tmpDataFile);
        try (Workbook book = WorkbookFactory.create(tmpDataFile)) {
            book.setSheetName(book.getSheetIndex(sheet), newName);
            try (FileOutputStream fileOut = new FileOutputStream(tmpFile)) {
                book.write(fileOut);
            }
        } catch (Exception e) {
            error = e.toString();
            return false;
        }
        try {
            FileDeleteTools.delete(tmpDataFile);
            if (tmpFile == null || !tmpFile.exists()) {
                return false;
            }
            if (FileTools.rename(tmpFile, file)) {
                sheet = newName;
                sheetNames.set(sheetNames.indexOf(oldName), sheet);
                tableData2DDefinition.updateData(this);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            error = e.toString();
            return false;
        }
    }

    public int deleteSheet(String name) {
        if (file == null || !file.exists()) {
            return -1;
        }
        File tmpFile = FileTmpTools.getTempFile();
        File tmpDataFile = FileTmpTools.getTempFile();
        FileCopyTools.copyFile(file, tmpDataFile);
        int index = -1;
        try (Workbook targetBook = WorkbookFactory.create(tmpDataFile)) {
            index = targetBook.getSheetIndex(name);
            if (index >= 0) {
                targetBook.removeSheetAt(index);
                try (FileOutputStream fileOut = new FileOutputStream(tmpFile)) {
                    targetBook.write(fileOut);
                }
            }
        } catch (Exception e) {
            error = e.toString();
            return -1;
        }
        try {
            FileDeleteTools.delete(tmpDataFile);
            if (tmpFile == null || !tmpFile.exists()) {
                return -1;
            }
            if (index < 0) {
                return -1;
            }
            if (FileTools.rename(tmpFile, file)) {
                return index;
            } else {
                return -1;
            }
        } catch (Exception e) {
            error = e.toString();
            return -1;
        }
    }

    /*
        get/set
     */
    public List<String> getSheetNames() {
        return sheetNames;
    }

    public void setSheetNames(List<String> sheetNames) {
        this.sheetNames = sheetNames;
    }

    public boolean isCurrentSheetOnly() {
        return currentSheetOnly;
    }

    public DataFileExcel setCurrentSheetOnly(boolean currentSheetOnly) {
        this.currentSheetOnly = currentSheetOnly;
        return this;
    }

}
