package mara.mybox.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javafx.scene.control.TextField;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.DataDefinition;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.FileCopyTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.MicrosoftDocumentTools;
import mara.mybox.tools.TmpFileTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @Author Mara
 * @CreateDate 2021-8-25
 * @License Apache License Version 2.0
 */
public class ControlSheetExcel extends ControlSheetFile {

    protected String currentSheetName, targetSheetName;
    protected List<String> sheetNames;
    protected boolean targetWithNames, currentSheetOnly;

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Excel);
    }

    @Override
    public void initFile() {
        sheetNames = null;
        super.initFile();
    }

    @Override
    public void initCurrentPage() {
        currentSheetName = null;
        targetSheetName = null;
        super.initCurrentPage();
    }

    public void loadSheet(String sheetName) {
        if (sourceFile == null || !checkBeforeNextAction()) {
            return;
        }
        initCurrentPage();
        currentSheetName = sheetName;
        loadFile();
    }

    @Override
    protected boolean readDataDefinition() {
        try ( Connection conn = DerbyBase.getConnection();
                 Workbook wb = WorkbookFactory.create(sourceFile)) {
            int sheetsNumber = wb.getNumberOfSheets();
            sheetNames = new ArrayList<>();
            for (int i = 0; i < sheetsNumber; i++) {
                sheetNames.add(wb.getSheetName(i));
            }
            Sheet sheet;
            if (currentSheetName != null) {
                sheet = wb.getSheet(currentSheetName);
            } else {
                sheet = wb.getSheetAt(0);
                currentSheetName = sheet.getSheetName();
            }
            dataName = sourceFile.getAbsolutePath() + "-" + currentSheetName;

            dataDefinition = tableDataDefinition.read(conn, dataType, dataName);

            if (userSavedDataDefinition && dataDefinition != null) {
                sourceWithNames = dataDefinition.isHasHeader();
            }
            boolean changed = !userSavedDataDefinition;
            if (sourceWithNames) {
                Iterator<Row> iterator = sheet.iterator();
                Row firstRow = null;
                if (iterator != null) {
                    while (iterator.hasNext()) {
                        firstRow = iterator.next();
                        if (firstRow != null) {
                            break;
                        }
                    }
                }
                if (firstRow == null) {
                    sourceWithNames = false;
                    changed = true;
                }
            }
            if (dataDefinition == null) {
                dataDefinition = DataDefinition.create().setDataName(dataName).setDataType(dataType)
                        .setHasHeader(sourceWithNames);
                tableDataDefinition.insertData(conn, dataDefinition);
                conn.commit();
            } else {
                if (changed) {
                    dataDefinition.setHasHeader(sourceWithNames);
                    tableDataDefinition.updateData(conn, dataDefinition);
                    conn.commit();
                }
                savedColumns = tableDataColumn.read(conn, dataDefinition.getDfid());
            }
            userSavedDataDefinition = true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.console(e);
            return false;
        }
        return dataDefinition != null && dataDefinition.getDfid() >= 0;
    }

    @Override
    protected boolean readColumns() {
        columns = new ArrayList<>();
        if (!sourceWithNames) {
            return true;
        }
        try ( Workbook wb = WorkbookFactory.create(sourceFile)) {
            Sheet sheet;
            if (currentSheetName != null) {
                sheet = wb.getSheet(currentSheetName);
            } else {
                sheet = wb.getSheetAt(0);
                currentSheetName = sheet.getSheetName();
            }
            Iterator<Row> iterator = sheet.iterator();
            if (iterator == null) {
                sourceWithNames = false;
                return true;
            }
            Row firstRow = null;
            while (iterator.hasNext()) {
                firstRow = iterator.next();
                if (firstRow != null) {
                    break;
                }
            }
            if (firstRow == null) {
                sourceWithNames = false;
                return true;
            }
            for (int col = firstRow.getFirstCellNum(); col < firstRow.getLastCellNum(); col++) {
                String name = (message(colPrefix) + (col + 1));
                ColumnType type = ColumnType.String;
                Cell cell = firstRow.getCell(col);
                if (cell != null) {
                    switch (cell.getCellType()) {
                        case NUMERIC:
                            type = ColumnType.Double;
                            break;
                        case BOOLEAN:
                            type = ColumnType.Boolean;
                            break;
                    }
                    String v = MicrosoftDocumentTools.cellString(cell);
                    if (!v.isBlank()) {
                        name = v;
                    }
                }
                boolean found = false;
                if (savedColumns != null) {
                    for (ColumnDefinition def : savedColumns) {
                        if (def.getName().equals(name)) {
                            columns.add(def);
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    ColumnDefinition column = new ColumnDefinition(name, type);
                    columns.add(column);
                }
            }
            if (ColumnDefinition.valid(this, columns)) {
                tableDataColumn.save(dataDefinition.getDfid(), columns);
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.console(e);
            return false;
        }
        return true;
    }

    @Override
    protected boolean readTotal() {
        totalSize = 0;
        try ( Workbook wb = WorkbookFactory.create(sourceFile)) {
            Sheet sheet;
            if (currentSheetName != null) {
                sheet = wb.getSheet(currentSheetName);
            } else {
                sheet = wb.getSheetAt(0);
                currentSheetName = sheet.getSheetName();
            }
            Iterator<Row> iterator = sheet.iterator();
            if (iterator != null) {
                while (backgroundTask != null && !backgroundTask.isCancelled() && iterator.hasNext()) {
                    iterator.next();
                    totalSize++;
                }
                if (backgroundTask == null || backgroundTask.isCancelled()) {
                    totalSize = 0;
                } else if (sourceWithNames && totalSize > 0) {
                    totalSize--;
                }
            }
        } catch (Exception e) {
            if (backgroundTask != null) {
                backgroundTask.setError(e.toString());
            }
            MyBoxLog.console(e);
            return false;
        }
        return true;
    }

    @Override
    protected String[][] readPageData() {
        if (currentPageStart < 1) {
            currentPageStart = 1;
        }
        long end = currentPageStart + pageSize;
        String[][] data = null;
        try ( Workbook wb = WorkbookFactory.create(sourceFile)) {
            Sheet sheet;
            if (currentSheetName != null) {
                sheet = wb.getSheet(currentSheetName);
            } else {
                sheet = wb.getSheetAt(0);
                currentSheetName = sheet.getSheetName();
            }
            List<List<String>> rows = new ArrayList<>();
            Iterator<Row> iterator = sheet.iterator();
            int rowIndex = 0, maxCol = 0;
            if (iterator != null && iterator.hasNext()) {
                if (sourceWithNames) {
                    while (iterator.hasNext() && (iterator.next() == null)) {
                    }
                }
                while (iterator.hasNext()) {
                    Row fileRow = iterator.next();
                    if (fileRow == null) {
                        continue;
                    }
                    ++rowIndex;
                    if (rowIndex < currentPageStart) {
                        continue;
                    }
                    if (rowIndex >= end) {
                        break;
                    }
                    List<String> row = new ArrayList<>();
                    for (int cellIndex = fileRow.getFirstCellNum(); cellIndex < fileRow.getLastCellNum(); cellIndex++) {
                        String v = MicrosoftDocumentTools.cellString(fileRow.getCell(cellIndex));
                        row.add(v);
                    }
                    rows.add(row);
                    if (maxCol < row.size()) {
                        maxCol = row.size();
                    }
                }
            }
            if (!rows.isEmpty() && maxCol > 0) {
                data = new String[rows.size()][maxCol];
                for (int row = 0; row < rows.size(); row++) {
                    List<String> rowData = rows.get(row);
                    for (int col = 0; col < rowData.size(); col++) {
                        data[row][col] = rowData.get(col);
                    }
                }
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.console(e);
        }
        if (data == null) {
            currentPageEnd = currentPageStart;
        } else {
            currentPageEnd = currentPageStart + data.length;  // 1-based, excluded
        }
        return data;
    }

    @Override
    protected File fileCopyCols(List<Integer> cols, boolean withNames) {
        if (sourceFile == null || cols == null || cols.isEmpty()) {
            return null;
        }
        File tmpFile = TmpFileTools.getTempFile();
        try ( Workbook sourceBook = WorkbookFactory.create(sourceFile);
                 CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(tmpFile, Charset.forName("utf-8")), CSVFormat.DEFAULT)) {
            if (withNames) {
                List<String> names = new ArrayList<>();
                for (int c : cols) {
                    names.add(colsCheck[c].getText());
                }
                csvPrinter.printRecord(names);
            }
            Sheet sourceSheet;
            if (currentSheetName != null) {
                sourceSheet = sourceBook.getSheet(currentSheetName);
            } else {
                sourceSheet = sourceBook.getSheetAt(0);
                currentSheetName = sourceSheet.getSheetName();
            }
            Iterator<Row> iterator = sourceSheet.iterator();
            if (iterator != null && iterator.hasNext()) {
                if (sourceWithNames) {
                    while (iterator.hasNext() && (iterator.next() == null)) {
                    }
                }
                int sourceRowIndex = 0;
                while (iterator.hasNext()) {
                    Row sourceRow = iterator.next();
                    if (sourceRow == null) {
                        continue;
                    }
                    sourceRowIndex++;
                    if (sourceRowIndex < currentPageStart || sourceRowIndex >= currentPageEnd) {
                        List<String> values = new ArrayList<>();
                        for (int c : cols) {
                            int cellIndex = c + sourceRow.getFirstCellNum();
                            if (cellIndex >= sourceRow.getLastCellNum()) {
                                break;
                            }
                            String d = MicrosoftDocumentTools.cellString(sourceRow.getCell(cellIndex));
                            d = d == null ? "" : d;
                            values.add(d);
                        }
                        csvPrinter.printRecord(values);
                    } else if (sourceRowIndex == currentPageStart) {
                        copyPageData(csvPrinter, cols);
                    }
                }
            } else {
                copyPageData(csvPrinter, cols);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        return tmpFile;
    }

    @Override
    protected File fileSetCols(List<Integer> cols, String value) {
        if (sourceFile == null || cols == null || cols.isEmpty()) {
            return null;
        }
        File tmpTargetFile = TmpFileTools.getTempFile();
        File tmpDataFile = TmpFileTools.getTempFile();
        FileCopyTools.copyFile(sourceFile, tmpDataFile);
        try ( Workbook sourceBook = WorkbookFactory.create(sourceFile);
                 Workbook targetBook = WorkbookFactory.create(tmpDataFile)) {
            Sheet sourceSheet;
            if (currentSheetName != null) {
                sourceSheet = sourceBook.getSheet(currentSheetName);
            } else {
                sourceSheet = sourceBook.getSheetAt(0);
                currentSheetName = sourceSheet.getSheetName();
            }
            int index = targetBook.getSheetIndex(currentSheetName);
            targetBook.removeSheetAt(index);
            Sheet targetSheet = targetBook.createSheet(currentSheetName);
            targetBook.setSheetOrder(currentSheetName, index);
            Iterator<Row> iterator = sourceSheet.iterator();
            if (iterator != null && iterator.hasNext()) {
                int targetRowIndex = 0;
                if (sourceWithNames) {
                    Row targetRow = targetSheet.createRow(targetRowIndex++);
                    for (int i = 0; i < colsCheck.length; i++) {
                        Cell targetCell = targetRow.createCell(i);
                        targetCell.setCellValue(colsCheck[i].getText());
                    }
                    while (iterator.hasNext() && (iterator.next() == null)) {
                    }
                }
                Random random = null;
                while (iterator.hasNext()) {
                    Row sourceRow = iterator.next();
                    if (sourceRow == null) {
                        continue;
                    }
                    Row targetRow = targetSheet.createRow(targetRowIndex++);
                    int firstIndex = sourceRow.getFirstCellNum();
                    for (int cellIndex = firstIndex; cellIndex < sourceRow.getLastCellNum(); cellIndex++) {
                        CellType type = CellType.STRING;
                        Cell sourceCell = sourceRow.getCell(cellIndex);
                        if (sourceCell != null) {
                            type = sourceCell.getCellType();
                        }
                        Cell targetCell = targetRow.createCell(cellIndex, type);
                        int colIndex = cellIndex - firstIndex;
                        if (cols.contains(colIndex)) {
                            String v = value;
                            if (AppValues.MyBoxRandomFlag.equals(value)) {
                                if (random == null) {
                                    random = new Random();
                                }
                                v = columns.get(colIndex).random(random, maxRandom, scale);
                            }
                            MicrosoftDocumentTools.setCell(targetCell, type, v);
                        } else {
                            MicrosoftDocumentTools.copyCell(sourceCell, targetCell, type);
                        }
                    }
                }
            }
            try ( FileOutputStream fileOut = new FileOutputStream(tmpTargetFile)) {
                targetBook.write(fileOut);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        FileDeleteTools.delete(tmpDataFile);
        return tmpTargetFile;

    }

    @Override
    public File filePaste(ControlSheetCSV sourceController, int row, int col, boolean enlarge) {
        if (sourceController == null || sourceController.sourceFile == null || sourceFile == null || row < 0 || col < 0) {
            return null;
        }
        File tmpTargetFile = TmpFileTools.getTempFile();
        File tmpDataFile = TmpFileTools.getTempFile();
        FileCopyTools.copyFile(sourceFile, tmpDataFile);
        try ( CSVParser csvParser = CSVParser.parse(sourceController.sourceFile, sourceController.sourceCharset, sourceController.sourceCsvFormat);
                 Workbook sourceBook = WorkbookFactory.create(sourceFile);
                 Workbook targetBook = WorkbookFactory.create(tmpDataFile)) {
            Sheet sourceSheet;
            if (currentSheetName != null) {
                sourceSheet = sourceBook.getSheet(currentSheetName);
            } else {
                sourceSheet = sourceBook.getSheetAt(0);
                currentSheetName = sourceSheet.getSheetName();
            }
            int index = targetBook.getSheetIndex(currentSheetName);
            targetBook.removeSheetAt(index);
            Sheet targetSheet = targetBook.createSheet(currentSheetName);
            targetBook.setSheetOrder(currentSheetName, index);

            int colsSize;
            int targetColsSize = columns.size();
            if (enlarge && col + sourceController.colsNumber > targetColsSize) {
                colsSize = col + sourceController.colsNumber;
            } else {
                colsSize = targetColsSize;
            }
            long rowsIndex = 0, sourceRowsSize = sourceController.rowsTotal(), targetRowsSize = rowsTotal(), rowsSize;
            if (enlarge && row + sourceRowsSize > targetRowsSize) {
                rowsSize = row + sourceRowsSize;
            } else {
                rowsSize = targetRowsSize;
            }
            Iterator<CSVRecord> csvIterator = csvParser.iterator();

            Iterator<Row> sheetIterator = sourceSheet.iterator();
            int sheetRowIndex = 0;
            if (sheetIterator != null && sheetIterator.hasNext()) {
                if (sourceWithNames) {
                    Row targetRow = targetSheet.createRow(sheetRowIndex++);
                    for (int i = 0; i < colsCheck.length; i++) {
                        Cell targetCell = targetRow.createCell(i);
                        targetCell.setCellValue(colsCheck[i].getText());
                    }
                    while (sheetIterator.hasNext() && (sheetIterator.next() == null)) {
                    }
                }
            }
            while (rowsIndex < row && rowsIndex < rowsSize && sheetIterator != null && sheetIterator.hasNext()) {
                Row sourceRow = sheetIterator.next();
                if (sourceRow == null) {
                    continue;
                }
                Row targetRow = targetSheet.createRow(sheetRowIndex++);
                writeTarget(sourceRow, targetRow, colsSize);
                rowsIndex++;
            }

            while (rowsIndex >= row && rowsIndex < rowsSize && rowsIndex < row + sourceRowsSize && csvIterator.hasNext()) {
                Row targetRow = targetSheet.createRow(sheetRowIndex++);
                writeSource(csvIterator.next(),
                        sheetIterator != null && sheetIterator.hasNext() ? sheetIterator.next() : null,
                        targetRow, col, colsSize);
                rowsIndex++;
            }

            while (rowsIndex < rowsSize && sheetIterator != null && sheetIterator.hasNext()) {
                Row sourceRow = sheetIterator.next();
                if (sourceRow == null) {
                    continue;
                }
                Row targetRow = targetSheet.createRow(sheetRowIndex++);
                writeTarget(sourceRow, targetRow, colsSize);
                rowsIndex++;
            }

            try ( FileOutputStream fileOut = new FileOutputStream(tmpTargetFile)) {
                targetBook.write(fileOut);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        FileDeleteTools.delete(tmpDataFile);
        return tmpTargetFile;
    }

    protected void writeTarget(Row sourceRow, Row targetRow, int colsSize) {
        try {
            int base = sourceRow.getFirstCellNum(), cellIndex = base;
            for (; cellIndex < Math.min(base + colsSize, sourceRow.getLastCellNum()); cellIndex++) {
                CellType type = CellType.STRING;
                Cell sourceCell = sourceRow.getCell(cellIndex);
                if (sourceCell != null) {
                    type = sourceCell.getCellType();
                }
                Cell targetCell = targetRow.createCell(cellIndex, type);
                MicrosoftDocumentTools.copyCell(sourceCell, targetCell, type);
            }
            for (; cellIndex < base + colsSize; cellIndex++) {
                Cell targetCell = targetRow.createCell(cellIndex, CellType.STRING);
                MicrosoftDocumentTools.setCell(targetCell, CellType.STRING, defaultColValue);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void writeSource(CSVRecord sourceRecord, Row sourceRow, Row targetRow, int col, int colsSize) {
        try {
            int base = sourceRow == null ? 0 : sourceRow.getFirstCellNum(), cellIndex = base;
            while (cellIndex < base + col && cellIndex < base + colsSize) {
                if (sourceRow != null && cellIndex < sourceRow.getLastCellNum()) {
                    CellType type = CellType.STRING;
                    Cell sourceCell = sourceRow.getCell(cellIndex);
                    if (sourceCell != null) {
                        type = sourceCell.getCellType();
                    }
                    Cell targetCell = targetRow.createCell(cellIndex, type);
                    MicrosoftDocumentTools.copyCell(sourceCell, targetCell, type);
                } else {
                    Cell targetCell = targetRow.createCell(base + cellIndex, CellType.STRING);
                    MicrosoftDocumentTools.setCell(targetCell, CellType.STRING, defaultColValue);
                }
                cellIndex++;
            }
            while (cellIndex >= base + col && cellIndex < base + colsSize && cellIndex < col + sourceRecord.size()) {
                Cell targetCell = targetRow.createCell(cellIndex, CellType.STRING);
                MicrosoftDocumentTools.setCell(targetCell, CellType.STRING, sourceRecord.get(cellIndex - col));
                cellIndex++;
            }
            while (cellIndex < colsSize) {
                if (sourceRow != null && cellIndex < sourceRow.getLastCellNum()) {
                    CellType type = CellType.STRING;
                    Cell sourceCell = sourceRow.getCell(cellIndex);
                    if (sourceCell != null) {
                        type = sourceCell.getCellType();
                    }
                    Cell targetCell = targetRow.createCell(cellIndex, type);
                    MicrosoftDocumentTools.copyCell(sourceCell, targetCell, type);
                } else {
                    Cell targetCell = targetRow.createCell(base + cellIndex, CellType.STRING);
                    MicrosoftDocumentTools.setCell(targetCell, CellType.STRING, defaultColValue);
                }
                cellIndex++;
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected File fileSortCol(int col, boolean asc) {
        if (sourceFile == null || col < 0) {
            return null;
        }
        File tmpFile = TmpFileTools.getTempFile();
        File tmpDataFile = TmpFileTools.getTempFile();
        FileCopyTools.copyFile(sourceFile, tmpDataFile);
        try ( Workbook sourceBook = WorkbookFactory.create(sourceFile);
                 Workbook targetBook = WorkbookFactory.create(tmpDataFile)) {
            Sheet sourceSheet;
            if (currentSheetName != null) {
                sourceSheet = sourceBook.getSheet(currentSheetName);
            } else {
                sourceSheet = sourceBook.getSheetAt(0);
                currentSheetName = sourceSheet.getSheetName();
            }

            int index = targetBook.getSheetIndex(currentSheetName);
            targetBook.removeSheetAt(index);
            Sheet targetSheet = targetBook.createSheet(currentSheetName);
            targetBook.setSheetOrder(currentSheetName, index);

            List<Row> records = new ArrayList<>();
            Iterator<Row> iterator = sourceSheet.iterator();
            if (iterator != null && iterator.hasNext()) {
                if (sourceWithNames) {
                    while (iterator.hasNext() && (iterator.next() == null)) {
                    }
                }
                while (iterator.hasNext()) {
                    Row sourceRow = iterator.next();
                    if (sourceRow == null) {
                        continue;
                    }
                    records.add(sourceRow);
                }
            }
            if (records.isEmpty()) {
                return null;
            }
            Collections.sort(records, new Comparator<Row>() {
                @Override
                public int compare(Row row1, Row row2) {
                    try {
                        ColumnDefinition column = columns.get(col);
                        int v = column.compare(MicrosoftDocumentTools.cellString(row1.getCell(col)),
                                MicrosoftDocumentTools.cellString(row2.getCell(col)));
                        return asc ? v : -v;
                    } catch (Exception e) {
                        return 0;
                    }
                }
            });
            int targetRowIndex = 0;
            if (sourceWithNames) {
                Row targetRow = targetSheet.createRow(targetRowIndex++);
                for (int i = 0; i < columns.size(); i++) {
                    Cell targetCell = targetRow.createCell(i);
                    targetCell.setCellValue(columns.get(i).getName());
                }
            }
            for (Row sourceRow : records) {
                Row targetRow = targetSheet.createRow(targetRowIndex++);
                MicrosoftDocumentTools.copyRow(sourceRow, targetRow);
            }
            try ( FileOutputStream fileOut = new FileOutputStream(tmpFile)) {
                targetBook.write(fileOut);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        FileDeleteTools.delete(tmpDataFile);
        return tmpFile;
    }

    @Override
    protected File fileAddCols(int col, boolean left, int number) {
        if (sourceFile == null || col < 0 || number < 1) {
            return null;
        }
        File tmpFile = TmpFileTools.getTempFile();
        File tmpDataFile = TmpFileTools.getTempFile();
        FileCopyTools.copyFile(sourceFile, tmpDataFile);
        try ( Workbook sourceBook = WorkbookFactory.create(sourceFile);
                 Workbook targetBook = WorkbookFactory.create(tmpDataFile)) {
            Sheet sourceSheet;
            if (currentSheetName != null) {
                sourceSheet = sourceBook.getSheet(currentSheetName);
            } else {
                sourceSheet = sourceBook.getSheetAt(0);
                currentSheetName = sourceSheet.getSheetName();
            }
            int index = targetBook.getSheetIndex(currentSheetName);
            targetBook.removeSheetAt(index);
            Sheet targetSheet = targetBook.createSheet(currentSheetName);
            targetBook.setSheetOrder(currentSheetName, index);

            Iterator<Row> iterator = sourceSheet.iterator();
            if (iterator != null && iterator.hasNext()) {
                int targetRowIndex = 0;
                if (sourceWithNames) {
                    Row targetRow = targetSheet.createRow(targetRowIndex++);
                    for (int i = 0; i < columns.size(); i++) {
                        Cell targetCell = targetRow.createCell(i);
                        targetCell.setCellValue(columns.get(i).getName());
                    }
                    while (iterator.hasNext() && (iterator.next() == null)) {
                    }
                }
                while (iterator.hasNext()) {
                    Row sourceRow = iterator.next();
                    if (sourceRow == null) {
                        continue;
                    }
                    Row targetRow = targetSheet.createRow(targetRowIndex++);
                    int targetCol = 0;
                    Cell targetCell;
                    for (int cellIndex = sourceRow.getFirstCellNum(); cellIndex < sourceRow.getLastCellNum(); cellIndex++) {
                        if (col == cellIndex && left) {
                            for (int i = 0; i < number; i++) {
                                targetCell = targetRow.createCell(targetCol++, CellType.STRING);
                                MicrosoftDocumentTools.setCell(targetCell, CellType.STRING, defaultColValue);
                            }
                        }
                        CellType type = CellType.STRING;
                        Cell sourceCell = sourceRow.getCell(cellIndex);
                        if (sourceCell != null) {
                            type = sourceCell.getCellType();
                        }
                        targetCell = targetRow.createCell(targetCol++, type);
                        MicrosoftDocumentTools.copyCell(sourceCell, targetCell, type);
                        if (col == cellIndex && !left) {
                            for (int i = 0; i < number; i++) {
                                targetCell = targetRow.createCell(targetCol++, CellType.STRING);
                                MicrosoftDocumentTools.setCell(targetCell, CellType.STRING, defaultColValue);
                            }
                        }
                    }
                }
            }
            try ( FileOutputStream fileOut = new FileOutputStream(tmpFile)) {
                targetBook.write(fileOut);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        FileDeleteTools.delete(tmpDataFile);
        return tmpFile;
    }

    @Override
    protected File fileDeleteAll(boolean keepCols) {
        if (sourceFile == null) {
            return null;
        }
        File tmpFile = TmpFileTools.getTempFile();
        File tmpDataFile = TmpFileTools.getTempFile();
        FileCopyTools.copyFile(sourceFile, tmpDataFile);
        try ( Workbook sourceBook = WorkbookFactory.create(sourceFile);
                 Workbook targetBook = WorkbookFactory.create(tmpDataFile)) {
            Sheet sourceSheet;
            if (currentSheetName == null) {
                sourceSheet = sourceBook.getSheetAt(0);
                currentSheetName = sourceSheet.getSheetName();
            }
            int index = targetBook.getSheetIndex(currentSheetName);
            targetBook.removeSheetAt(index);
            Sheet targetSheet = targetBook.createSheet(currentSheetName);
            targetBook.setSheetOrder(currentSheetName, index);

            if (sourceWithNames && keepCols) {
                Row targetRow = targetSheet.createRow(0);
                for (int i = 0; i < columns.size(); i++) {
                    Cell targetCell = targetRow.createCell(i);
                    targetCell.setCellValue(columns.get(i).getName());
                }
            }

            try ( FileOutputStream fileOut = new FileOutputStream(tmpFile)) {
                targetBook.write(fileOut);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        FileDeleteTools.delete(tmpDataFile);
        return tmpFile;
    }

    @Override
    protected File fileDeleteCols(List<Integer> cols) {
        if (sourceFile == null || cols == null || cols.isEmpty()) {
            return null;
        }
        File tmpFile = TmpFileTools.getTempFile();
        File tmpDataFile = TmpFileTools.getTempFile();
        FileCopyTools.copyFile(sourceFile, tmpDataFile);
        try ( Workbook sourceBook = WorkbookFactory.create(sourceFile);
                 Workbook targetBook = WorkbookFactory.create(tmpDataFile)) {
            Sheet sourceSheet;
            if (currentSheetName != null) {
                sourceSheet = sourceBook.getSheet(currentSheetName);
            } else {
                sourceSheet = sourceBook.getSheetAt(0);
                currentSheetName = sourceSheet.getSheetName();
            }

            int index = targetBook.getSheetIndex(currentSheetName);
            targetBook.removeSheetAt(index);
            Sheet targetSheet = targetBook.createSheet(currentSheetName);
            targetBook.setSheetOrder(currentSheetName, index);

            Iterator<Row> iterator = sourceSheet.iterator();
            if (iterator != null && iterator.hasNext()) {
                int targetRowIndex = 0;
                if (sourceWithNames) {
                    List<String> colsNames = columnNames();
                    Row targetRow = targetSheet.createRow(targetRowIndex++);
                    for (int col = 0; col < colsNames.size(); col++) {
                        if (cols.contains(col)) {
                            continue;
                        }
                        Cell targetCell = targetRow.createCell(col);
                        targetCell.setCellValue(colsNames.get(col));
                    }
                    while (iterator.hasNext() && (iterator.next() == null)) {
                    }
                }
                while (iterator.hasNext()) {
                    Row sourceRow = iterator.next();
                    if (sourceRow == null) {
                        continue;
                    }
                    Row targetRow = targetSheet.createRow(targetRowIndex++);
                    int targetCol = 0;
                    Cell targetCell;
                    for (int cellIndex = sourceRow.getFirstCellNum(); cellIndex < sourceRow.getLastCellNum(); cellIndex++) {
                        if (cols.contains(cellIndex - sourceRow.getFirstCellNum())) {
                            continue;
                        }
                        CellType type = CellType.STRING;
                        Cell sourceCell = sourceRow.getCell(cellIndex);
                        if (sourceCell != null) {
                            type = sourceCell.getCellType();
                        }
                        targetCell = targetRow.createCell(targetCol++, type);
                        MicrosoftDocumentTools.copyCell(sourceCell, targetCell, type);
                    }
                }
            }
            try ( FileOutputStream fileOut = new FileOutputStream(tmpFile)) {
                targetBook.write(fileOut);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        FileDeleteTools.delete(tmpDataFile);
        return tmpFile;
    }

    @Override
    public void saveFile() {
        if (sourceFile == null) {
            saveAs();
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                String[][] data;

                @Override
                protected boolean handle() {
                    backup();
                    error = save(sourceFile, sourceWithNames);
                    return error == null;
                }

                @Override
                protected void whenSucceeded() {
                    popInformation(Languages.message("Saved"));
                    currentSheetName = targetSheetName;
                    dataChangedNotify.set(false);
                    loadFile();
                }

            };
            start(task);
        }
    }

    @Override
    public void saveAs() {
        File file = chooseSaveFile();
        if (file == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                String[][] data;

                @Override
                protected boolean handle() {
                    error = save(file, targetWithNames);
                    return error == null;
                }

                @Override
                protected void whenSucceeded() {
                    popInformation(Languages.message("Saved"));
                    recordFileWritten(file);
                    if (sourceFile == null || saveAsType == SaveAsType.Load) {
                        if (parentController != null) {
                            dataChangedNotify.set(false);
                            parentController.sourceFileChanged(file);
                            return;
                        }
                    }
                    DataFileExcelController controller = (DataFileExcelController) WindowTools.openStage(Fxmls.DataFileExcelFxml);
                    controller.sourceFileChanged(file);
                }
            };
            start(task);
        }
    }

    public String save(File file, boolean withName) {
        File tmpFile = TmpFileTools.getTempFile();
        if (withName && columns == null) {
            makeColumns(colsCheck.length);
        }
        List<String> otherSheetNames = new ArrayList<>();
        String sheetNameSaved;
        if (sourceFile != null) {
            try ( Workbook sourceBook = WorkbookFactory.create(sourceFile)) {
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
                sheetNameSaved = targetSheetName;
                Workbook targetBook;
                Sheet targetSheet;
                File tmpDataFile = null;
                int sheetsNumber = sourceBook.getNumberOfSheets();
                if (sheetsNumber == 1
                        || (!file.equals(sourceFile) && currentSheetOnly)) {
                    targetBook = new XSSFWorkbook();
                    targetSheet = targetBook.createSheet(targetSheetName);
                } else {
                    tmpDataFile = TmpFileTools.getTempFile();
                    FileCopyTools.copyFile(sourceFile, tmpDataFile);
                    targetBook = WorkbookFactory.create(tmpDataFile);
                    if (!currentSheetOnly) {
                        for (int i = 0; i < sheetsNumber; i++) {
                            otherSheetNames.add(targetBook.getSheetName(i));
                        }
                        otherSheetNames.remove(currentSheetName);
                    }
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
                    if (sourceWithNames) {
                        while (iterator.hasNext() && (iterator.next() == null)) {
                        }
                    }
                    int sourceRowIndex = 0;
                    while (iterator.hasNext()) {
                        Row sourceRow = iterator.next();
                        if (sourceRow == null) {
                            continue;
                        }
                        sourceRowIndex++;
                        if (sourceRowIndex < currentPageStart || sourceRowIndex >= currentPageEnd) {
                            Row targetRow = targetSheet.createRow(targetRowIndex++);
                            MicrosoftDocumentTools.copyRow(sourceRow, targetRow);
                        } else if (sourceRowIndex == currentPageStart) {
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
                return e.toString();
            }

        } else {
            try ( Workbook targetBook = new XSSFWorkbook()) {
                if (targetSheetName != null) {
                    sheetNameSaved = targetSheetName;
                } else {
                    sheetNameSaved = Languages.message("Sheet") + "1";
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
                return e.toString();
            }
        }
        if (FileTools.rename(tmpFile, file, false)) {
            saveColumns(file, sheetNameSaved, otherSheetNames, withName);
            return null;
        } else {
            return "Failed";
        }
    }

    protected int writeHeader(Sheet targetSheet, int targetRowIndex) {
        if (colsCheck == null) {
            return targetRowIndex;
        }
        int index = targetRowIndex;
        Row targetRow = targetSheet.createRow(index++);
        for (int col = 0; col < colsCheck.length; col++) {
            Cell targetCell = targetRow.createCell(col, CellType.STRING);
            targetCell.setCellValue(colsCheck[col].getText());
        }
        return index;
    }

    protected int writePageData(Sheet targetSheet, int targetRowIndex) {
        if (sheetInputs == null) {
            return targetRowIndex;
        }
        int index = targetRowIndex;
        for (TextField[] row : sheetInputs) {
            Row targetRow = targetSheet.createRow(index++);
            for (int col = 0; col < row.length; col++) {
                Cell targetCell = targetRow.createCell(col, CellType.STRING);
                targetCell.setCellValue(row[col].getText());
            }
        }
        return index;
    }

    @Override
    protected boolean saveDefinition() {
        return saveDefinition(sourceFile.getAbsolutePath() + "-" + currentSheetName, dataType,
                Charset.defaultCharset(), ",", sourceWithNames, columns);
    }

    protected void saveColumns(File file, String currentSheetName, List<String> otherSheetNames, boolean withName) {
        try ( Connection conn = DerbyBase.getConnection()) {
            if (currentSheetName != null) {
                saveDefinition(conn, file.getAbsolutePath() + "-" + currentSheetName,
                        dataType, Charset.defaultCharset(), ",", withName, columns);
            }
            if (otherSheetNames != null) {
                for (String name : otherSheetNames) {
                    String sourceDataName = sourceFile.getAbsolutePath() + "-" + name;
                    DataDefinition sourceDef = tableDataDefinition.read(conn, dataType, sourceDataName);
                    if (sourceDef == null) {
                        continue;
                    }
                    String targetDataName = file.getAbsolutePath() + "-" + name;
                    DataDefinition targetDef = tableDataDefinition.read(conn, dataType, targetDataName);
                    if (targetDef == null) {
                        targetDef = DataDefinition.create()
                                .setDataName(targetDataName).setDataType(dataType)
                                .setHasHeader(sourceDef.isHasHeader());
                        tableDataDefinition.insertData(conn, targetDef);
                    } else {
                        targetDef.setHasHeader(sourceDef.isHasHeader());
                        tableDataDefinition.updateData(conn, targetDef);
                        tableDataColumn.clear(conn, targetDef.getDfid());
                    }
                    conn.commit();
                    List<ColumnDefinition> sourceColumns = tableDataColumn.read(conn, sourceDef.getDfid());
                    if (sourceColumns == null || sourceColumns.isEmpty()) {
                        continue;
                    }
                    for (ColumnDefinition column : sourceColumns) {
                        column.setDataid(targetDef.getDfid());
                    }
                    tableDataColumn.save(conn, targetDef.getDfid(), sourceColumns);
                    conn.commit();
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
