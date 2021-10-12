package mara.mybox.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileCopyTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.MicrosoftDocumentTools;
import mara.mybox.tools.TmpFileTools;
import mara.mybox.value.AppValues;
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

/**
 * @Author Mara
 * @CreateDate 2021-8-25
 * @License Apache License Version 2.0
 */
public abstract class ControlSheetExcel_Operations extends ControlSheetExcel_File {

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
                int sourceRowIndex = -1;
                while (iterator.hasNext()) {
                    Row sourceRow = iterator.next();
                    if (sourceRow == null) {
                        continue;
                    }
                    if (++sourceRowIndex < startRowOfCurrentPage || sourceRowIndex >= endRowOfCurrentPage) {
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
                    } else if (sourceRowIndex == startRowOfCurrentPage) {
                        copyPageData(csvPrinter, cols);
                    }
                }
                if (sourceRowIndex < 0) {
                    copyPageData(csvPrinter, cols);
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

}
