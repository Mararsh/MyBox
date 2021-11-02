package mara.mybox.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.scene.control.TextField;
import mara.mybox.data.StringTable;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.DataDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.FileCopyTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.MicrosoftDocumentTools;
import static mara.mybox.tools.TextTools.delimiterValue;
import mara.mybox.tools.TmpFileTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
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
 * @CreateDate 2021-8-25
 * @License Apache License Version 2.0
 */
public abstract class ControlSheetExcel_File extends ControlSheetFile {

    protected String currentSheetName, targetSheetName;
    protected List<String> sheetNames;
    protected boolean targetWithNames, currentSheetOnly;

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

//            dataDefinition = tableDataDefinition.read(conn, dataType, dataName);
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
//                savedColumns = tableDataColumn.read(conn, dataDefinition.getDfid());
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
            if (columns != null && !columns.isEmpty()) {
                if (validateColumns(columns)) {
//                    tableDataColumn.save(dataDefinition.getDfid(), columns);
                    return true;
                } else {
                    return false;
                }
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
        if (startRowOfCurrentPage < 0) {
            startRowOfCurrentPage = 0;
        }
        long end = startRowOfCurrentPage + pageSize;
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
            int rowIndex = -1, maxCol = 0;
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
                    if (++rowIndex < startRowOfCurrentPage) {
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
                int colsSize = sourceWithNames ? columns.size() : maxCol;
                data = new String[rows.size()][colsSize];
                for (int row = 0; row < rows.size(); row++) {
                    List<String> rowData = rows.get(row);
                    for (int col = 0; col < Math.min(rowData.size(), colsSize); col++) {
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
            endRowOfCurrentPage = startRowOfCurrentPage;
        } else {
            endRowOfCurrentPage = startRowOfCurrentPage + data.length;
        }
        return data;
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
            task = new SingletonTask<Void>(this) {
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
            task = new SingletonTask<Void>(this) {
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
                    int sourceRowIndex = -1;
                    while (iterator.hasNext()) {
                        Row sourceRow = iterator.next();
                        if (sourceRow == null) {
                            continue;
                        }
                        if (++sourceRowIndex < startRowOfCurrentPage || sourceRowIndex >= endRowOfCurrentPage) {
                            Row targetRow = targetSheet.createRow(targetRowIndex++);
                            MicrosoftDocumentTools.copyRow(sourceRow, targetRow);
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
            saveDefinition(file, sheetNameSaved, otherSheetNames, withName);
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

//    @Override
//    protected boolean saveDefinition() {
////        return saveDefinition(sourceFile.getAbsolutePath() + "-" + currentSheetName, dataType,
////                Charset.defaultCharset(), ",", sourceWithNames, columns);
//    }
    protected void saveDefinition(File file, String currentSheetName, List<String> otherSheetNames, boolean withName) {
        try ( Connection conn = DerbyBase.getConnection()) {
            if (currentSheetName != null) {
//                saveDefinition(conn, file.getAbsolutePath() + "-" + currentSheetName,
//                        dataType, Charset.defaultCharset(), ",", withName, columns);
            }
            if (otherSheetNames != null) {
                for (String name : otherSheetNames) {
                    String sourceDataName = sourceFile.getAbsolutePath() + "-" + name;
//                    DataDefinition sourceDef = tableDataDefinition.read(conn, dataType, sourceDataName);
//                    if (sourceDef == null) {
//                        continue;
//                    }
//                    String targetDataName = file.getAbsolutePath() + "-" + name;
//                    DataDefinition targetDef = tableDataDefinition.read(conn, dataType, targetDataName);
//                    if (targetDef == null) {
//                        targetDef = DataDefinition.create()
//                                .setDataName(targetDataName).setDataType(dataType)
//                                .setHasHeader(sourceDef.isHasHeader());
//                        tableDataDefinition.insertData(conn, targetDef);
//                    } else {
//                        targetDef.setHasHeader(sourceDef.isHasHeader());
//                        tableDataDefinition.updateData(conn, targetDef);
//                        tableDataColumn.clear(conn, targetDef.getDfid());
//                    }
//                    conn.commit();
//                    List<ColumnDefinition> sourceColumns = tableDataColumn.read(conn, sourceDef.getDfid());
//                    if (sourceColumns == null || sourceColumns.isEmpty()) {
//                        continue;
//                    }
//                    for (ColumnDefinition column : sourceColumns) {
//                        column.setDataid(targetDef.getDfid());
//                    }
//                    tableDataColumn.save(conn, targetDef.getDfid(), sourceColumns);
//                    conn.commit();
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected String fileText() {
        if (sourceFile == null) {
            return null;
        }
        String delimiter = delimiterValue(displayDelimiterName);
        StringBuilder s = new StringBuilder();
        if (textTitleCheck.isSelected()) {
            s.append(titleName()).append("\n\n");
        }
        if (textColumnCheck.isSelected()) {
            rowText(s, -1, columnNames(), delimiter);
        }
        try ( Workbook sourceBook = WorkbookFactory.create(sourceFile)) {
            Sheet sourceSheet;
            if (currentSheetName != null) {
                sourceSheet = sourceBook.getSheet(currentSheetName);
            } else {
                sourceSheet = sourceBook.getSheetAt(0);
                currentSheetName = sourceSheet.getSheetName();
            }
            Iterator<Row> iterator = sourceSheet.iterator();
            int fileIndex = -1, dataIndex = 0;
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
                    if (++fileIndex < startRowOfCurrentPage || fileIndex >= endRowOfCurrentPage) {
                        List<String> values = new ArrayList<>();
                        for (int cellIndex = sourceRow.getFirstCellNum(); cellIndex < sourceRow.getLastCellNum(); cellIndex++) {
                            String d = MicrosoftDocumentTools.cellString(sourceRow.getCell(cellIndex));
                            d = d == null ? "" : d;
                            values.add(d);
                        }
                        rowText(s, dataIndex++, values, delimiter);
                    } else if (fileIndex == startRowOfCurrentPage) {
                        dataIndex = pageText(s, dataIndex, delimiter);
                    }
                }
            } else {
                pageText(s, dataIndex, delimiter);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        return s.toString();
    }

    @Override
    protected String fileHtml() {
        if (sourceFile == null) {
            return null;
        }
        StringBuilder s = new StringBuilder();
        List<String> names = null;
        if (htmlColumnCheck.isSelected()) {
            names = new ArrayList<>();
            if (htmlRowCheck.isSelected()) {
                names.add("");
            }
            names.addAll(columnNames());
        }
        String title = null;
        if (htmlTitleCheck.isSelected()) {
            title = titleName();
        }
        StringTable table = new StringTable(names, title);
        try ( Workbook sourceBook = WorkbookFactory.create(sourceFile)) {
            Sheet sourceSheet;
            if (currentSheetName != null) {
                sourceSheet = sourceBook.getSheet(currentSheetName);
            } else {
                sourceSheet = sourceBook.getSheetAt(0);
                currentSheetName = sourceSheet.getSheetName();
            }
            Iterator<Row> iterator = sourceSheet.iterator();
            int fileIndex = -1, dataIndex = 0;
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
                    if (++fileIndex < startRowOfCurrentPage || fileIndex >= endRowOfCurrentPage) {
                        List<String> values = new ArrayList<>();
                        if (htmlRowCheck.isSelected()) {
                            values.add(message("Row") + (dataIndex + 1));
                        }
                        for (int cellIndex = sourceRow.getFirstCellNum(); cellIndex < sourceRow.getLastCellNum(); cellIndex++) {
                            String d = MicrosoftDocumentTools.cellString(sourceRow.getCell(cellIndex));
                            d = d == null ? "" : d;
                            values.add(d);
                        }
                        table.add(values);
                        dataIndex++;
                    } else if (fileIndex == startRowOfCurrentPage) {
                        dataIndex = pageHtml(table, dataIndex);
                    }
                }
            } else {
                pageHtml(table, dataIndex);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        return table.html();
    }

}
