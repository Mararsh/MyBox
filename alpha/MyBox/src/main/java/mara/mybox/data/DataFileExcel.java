package mara.mybox.data;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileCopyTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.MicrosoftDocumentTools;
import mara.mybox.tools.TmpFileTools;
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

    protected String currentSheetName;
    protected List<String> sheetNames;
    protected boolean currentSheetOnly;

    public DataFileExcel() {
        type = Type.Excel;
    }

    public void setOptions(boolean hasHeader) {
        options = new HashMap<>();
        options.put("hasHeader", hasHeader);
    }

    public void setOptions(boolean hasHeader, String currentSheetName) {
        options = new HashMap<>();
        options.put("currentSheetName", currentSheetName);
        options.put("hasHeader", hasHeader);
    }

    public void setOptions(String currentSheetName) {
        options = new HashMap<>();
        options.put("currentSheetName", currentSheetName);
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
            if (options.containsKey("currentSheetName")) {
                currentSheetName = (String) (options.get("currentSheetName"));
            }
        } catch (Exception e) {
        }
    }

    @Override
    public Data2DDefinition queryDefinition(Connection conn) {
        return tableData2DDefinition.queryFileSheet(conn, type, file, currentSheetName);
    }

    @Override
    public void initFile(File file) {
        if (file == null || !file.equals(this.file)) {
            currentSheetName = null;
            sheetNames = null;
        }
        super.initFile(file);
    }

    @Override
    public void checkAttributes() {
        if (dataName == null || dataName.isBlank()) {
            if (!isTmpData()) {
                dataName = file.getName();
            } else {
                dataName = DateTools.nowString();
            }
            if (currentSheetName != null) {
                dataName += " - " + currentSheetName;
            }
        }
        delimiter = currentSheetName;  // use field "delimiter" as currentSheetName
        super.checkAttributes();
    }

    @Override
    public long readDataDefinition() {
        try ( Workbook wb = WorkbookFactory.create(file)) {
            int sheetsNumber = wb.getNumberOfSheets();
            sheetNames = new ArrayList<>();
            for (int i = 0; i < sheetsNumber; i++) {
                sheetNames.add(wb.getSheetName(i));
            }
            if (currentSheetName == null && sheetsNumber > 0) {
                currentSheetName = wb.getSheetAt(0).getSheetName();
            }
            wb.close();
        } catch (Exception e) {
        }
        return super.readDataDefinition();
    }

    @Override
    public List<String> readColumns() {
        List<String> names = null;
        try ( Workbook wb = WorkbookFactory.create(file)) {
            Sheet sheet;
            if (currentSheetName != null) {
                sheet = wb.getSheet(currentSheetName);
            } else {
                sheet = wb.getSheetAt(0);
                currentSheetName = sheet.getSheetName();
            }
            Iterator<Row> iterator = sheet.iterator();
            if (iterator == null) {
                return null;
            }
            Row firstRow = null;
            while (iterator.hasNext()) {
                firstRow = iterator.next();
                if (firstRow != null) {
                    break;
                }
            }
            if (firstRow == null) {
                return null;
            }
            names = new ArrayList<>();
            int colIndex = 1;
            for (int col = firstRow.getFirstCellNum(); col < firstRow.getLastCellNum(); col++) {
                String name = null;
                if (hasHeader) {
                    name = MicrosoftDocumentTools.cellString(firstRow.getCell(col));
                }
                if (name == null) {
                    name = (message(colPrefix()) + colIndex++);
                }
                names.add(name);
            }
            wb.close();
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.console(e);
        }
        return names;
    }

    @Override
    public long readTotal() {
        dataSize = 0;
        try ( Workbook wb = WorkbookFactory.create(file)) {
            Sheet sheet;
            if (currentSheetName != null) {
                sheet = wb.getSheet(currentSheetName);
            } else {
                sheet = wb.getSheetAt(0);
                currentSheetName = sheet.getSheetName();
            }
            Iterator<Row> iterator = sheet.iterator();
            if (iterator != null) {
                while (iterator.hasNext()) {
                    if (backgroundTask == null || backgroundTask.isCancelled()) {
                        dataSize = 0;
                        break;
                    }
                    try {
                        if (iterator.next() != null) {
                            dataSize++;
                        }
                    } catch (Exception e) {  // skip  bad lines
                    }
                }
            }
            wb.close();
        } catch (Exception e) {
            if (backgroundTask != null) {
                backgroundTask.setError(e.toString());
            }
            MyBoxLog.console(e);
            return -1;
        }
        if (hasHeader && dataSize > 0) {
            dataSize--;
        }
        return dataSize;
    }

    @Override
    public List<List<String>> readPageData() {
        if (startRowOfCurrentPage < 0) {
            startRowOfCurrentPage = 0;
        }
        endRowOfCurrentPage = startRowOfCurrentPage;
        List<List<String>> rows = new ArrayList<>();
        try ( Workbook wb = WorkbookFactory.create(file)) {
            Sheet sheet;
            if (currentSheetName != null) {
                sheet = wb.getSheet(currentSheetName);
            } else {
                sheet = wb.getSheetAt(0);
                currentSheetName = sheet.getSheetName();
            }
            Iterator<Row> iterator = sheet.iterator();
            if (iterator != null && iterator.hasNext()) {
                if (hasHeader) {
                    while (iterator.hasNext() && (iterator.next() == null) && task != null && !task.isCancelled()) {
                    }
                }
                long rowIndex = -1;
                int columnsNumber = columnsNumber();
                long rowsEnd = startRowOfCurrentPage + pageSize;
                while (iterator.hasNext() && task != null && !task.isCancelled()) {
                    try {
                        Row fileRow = iterator.next();
                        if (fileRow == null) {
                            continue;
                        }
                        if (++rowIndex < startRowOfCurrentPage) {
                            continue;
                        }
                        if (rowIndex >= rowsEnd) {
                            break;
                        }
                        List<String> row = new ArrayList<>();
                        for (int cellIndex = fileRow.getFirstCellNum(); cellIndex < fileRow.getLastCellNum(); cellIndex++) {
                            String v = MicrosoftDocumentTools.cellString(fileRow.getCell(cellIndex));
                            row.add(v);
                            if (row.size() >= columnsNumber) {
                                break;
                            }
                        }
                        for (int col = row.size(); col < columnsNumber; col++) {
                            row.add(null);
                        }
                        row.add(0, "" + (rowIndex + 1));
                        rows.add(row);
                    } catch (Exception e) {  // skip  bad lines
                    }
                }
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.console(e);
            return null;
        }
        endRowOfCurrentPage = startRowOfCurrentPage + rows.size();
        return rows;
    }

    @Override
    public boolean savePageData(Data2D targetData) {
        if (targetData == null || !(targetData instanceof DataFileExcel)) {
            return false;
        }
        DataFileExcel targetExcelFile = (DataFileExcel) targetData;
        File tmpFile = TmpFileTools.getTempFile();
        File tFile = targetExcelFile.getFile();
        if (tFile == null) {
            return false;
        }
        boolean withName = targetExcelFile.isHasHeader();
        String targetSheetName = targetExcelFile.getCurrentSheetName();
        String sheetNameSaved;
        if (file != null) {
            try ( Workbook sourceBook = WorkbookFactory.create(file)) {
                Sheet sourceSheet;
                if (currentSheetName != null) {
                    sourceSheet = sourceBook.getSheet(currentSheetName);
                } else {
                    sourceSheet = sourceBook.getSheetAt(0);
                    currentSheetName = sourceSheet.getSheetName();
                }
                if (targetSheetName == null) {
                    targetSheetName = currentSheetName;
                }
                Workbook targetBook;
                Sheet targetSheet;
                File tmpDataFile = null;
                int sheetsNumber = sourceBook.getNumberOfSheets();
                if (sheetsNumber == 1
                        || (!file.equals(tFile) && currentSheetOnly)) {
                    targetBook = new XSSFWorkbook();
                    targetSheet = targetBook.createSheet(targetSheetName);
                } else {
                    tmpDataFile = TmpFileTools.getTempFile();
                    FileCopyTools.copyFile(file, tmpDataFile);
                    targetBook = WorkbookFactory.create(tmpDataFile);
                    int index = targetBook.getSheetIndex(currentSheetName);
                    targetBook.removeSheetAt(index);
                    targetSheet = targetBook.createSheet(targetSheetName);
                    targetBook.setSheetOrder(targetSheetName, index);
                }
                int targetRowIndex = 0;
                if (withName) {
                    targetRowIndex = writeHeader(targetSheet, targetRowIndex);
                }
                Iterator<Row> iterator = sourceSheet.iterator();
                if (iterator != null && iterator.hasNext()) {
                    if (hasHeader) {
                        while (iterator.hasNext() && (iterator.next() == null)) {
                        }
                    }
                    int sourceRowIndex = -1;
                    while (iterator.hasNext()) {
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
                try ( FileOutputStream fileOut = new FileOutputStream(tmpFile)) {
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
            try ( Workbook targetBook = new XSSFWorkbook()) {
                if (targetSheetName != null) {
                    sheetNameSaved = targetSheetName;
                } else {
                    sheetNameSaved = message("Sheet") + "1";
                }
                Sheet targetSheet = targetBook.createSheet(sheetNameSaved);
                int targetRowIndex = 0;
                if (withName) {
                    targetRowIndex = writeHeader(targetSheet, targetRowIndex);
                }
                writePageData(targetSheet, targetRowIndex);
                try ( FileOutputStream fileOut = new FileOutputStream(tmpFile)) {
                    targetBook.write(fileOut);
                }
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

    protected int writeHeader(Sheet targetSheet, int targetRowIndex) {
        if (!isColumnsValid()) {
            return targetRowIndex;
        }
        int index = targetRowIndex;
        Row targetRow = targetSheet.createRow(index++);
        for (int col = 0; col < columns.size(); col++) {
            Cell targetCell = targetRow.createCell(col, CellType.STRING);
            targetCell.setCellValue(columns.get(col).getName());
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
                List<String> values = tableRow(r);
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

    @Override
    public File tmpFile(List<String> cols, List<List<String>> data) {
        try {
            if (cols == null || cols.isEmpty()) {
                if (data == null || data.isEmpty()) {
                    return null;
                }
            }
            File tmpFile = TmpFileTools.getTempFile(".xlsx");
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
            try ( FileOutputStream fileOut = new FileOutputStream(tmpFile)) {
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
        if (file == null) {
            return false;
        }
        File tmpFile = TmpFileTools.getTempFile();
        File tmpDataFile = TmpFileTools.getTempFile();
        FileCopyTools.copyFile(file, tmpDataFile);
        try ( Workbook targetBook = WorkbookFactory.create(tmpDataFile)) {
            Sheet targetSheet = targetBook.createSheet(sheetName);
            List<List<String>> data = tmpData();
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
            try ( FileOutputStream fileOut = new FileOutputStream(tmpFile)) {
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
                currentSheetName = sheetName;
                delimiter = currentSheetName;
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
        if (file == null || currentSheetName == null) {
            return false;
        }
        String oldName = currentSheetName;
        File tmpFile = TmpFileTools.getTempFile();
        File tmpDataFile = TmpFileTools.getTempFile();
        FileCopyTools.copyFile(file, tmpDataFile);
        try ( Workbook book = WorkbookFactory.create(tmpDataFile)) {
            book.setSheetName(book.getSheetIndex(currentSheetName), newName);
            try ( FileOutputStream fileOut = new FileOutputStream(tmpFile)) {
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
                currentSheetName = newName;
                delimiter = currentSheetName;
                sheetNames.set(sheetNames.indexOf(oldName), currentSheetName);
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
        if (file == null) {
            return -1;
        }
        File tmpFile = TmpFileTools.getTempFile();
        File tmpDataFile = TmpFileTools.getTempFile();
        FileCopyTools.copyFile(file, tmpDataFile);
        int index = -1;
        try ( Workbook targetBook = WorkbookFactory.create(tmpDataFile)) {
            index = targetBook.getSheetIndex(name);
            if (index >= 0) {
                targetBook.removeSheetAt(index);
                try ( FileOutputStream fileOut = new FileOutputStream(tmpFile)) {
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
    public String getCurrentSheetName() {
        return currentSheetName;
    }

    public void setCurrentSheetName(String currentSheetName) {
        this.currentSheetName = currentSheetName;
    }

    public List<String> getSheetNames() {
        return sheetNames;
    }

    public void setSheetNames(List<String> sheetNames) {
        this.sheetNames = sheetNames;
    }

    public boolean isCurrentSheetOnly() {
        return currentSheetOnly;
    }

    public void setCurrentSheetOnly(boolean currentSheetOnly) {
        this.currentSheetOnly = currentSheetOnly;
    }

}
