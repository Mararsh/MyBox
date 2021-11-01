package mara.mybox.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import javafx.fxml.FXML;
import mara.mybox.data.DataClipboard;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.DataDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.tools.TextTools;
import mara.mybox.tools.TmpFileTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @Author Mara
 * @CreateDate 2021-8-24
 * @License Apache License Version 2.0
 */
public abstract class ControlSheet_Operations extends ControlSheet_Edit {

    protected char copyDelimiter = ',';

    protected abstract String[][] allRows(List<Integer> cols);

    public abstract void copyCols(List<Integer> cols, boolean withNames, boolean toSystemClipboard);

    public abstract void setCols(List<Integer> cols, String value);

    public abstract void sort(int col, boolean asc);

    public abstract void pasteFile(ControlSheetCSV sourceController, int row, int col, boolean enlarge);

    public abstract boolean exportCols(SheetExportController exportController, List<Integer> cols, boolean skip);

    public String[][] data(List<Integer> rows, List<Integer> cols) {
        if (rows == null || rows.isEmpty() || cols == null || cols.isEmpty() || sheetInputs == null || columns == null) {
            popError(message("NoData"));
            return null;
        }
        int rowSize = rows.size();
        int colSize = cols.size();
        String[][] data = new String[rowSize][colSize];
        for (int r = 0; r < rows.size(); ++r) {
            int row = rows.get(r);
            for (int c = 0; c < cols.size(); ++c) {
                data[r][c] = cellString(row, cols.get(c));
            }
        }
        return data;
    }

    public String[][] data(List<Integer> cols) {
        return data(rowsIndex(true), cols);
    }

    @FXML
    @Override
    public void copyToSystemClipboard() {
        SheetCopyToSystemClipboardController controller = (SheetCopyToSystemClipboardController) openChildStage(Fxmls.SheetCopyToSystemClipboardFxml, false);
        controller.setParameters((ControlSheet) this, -1, -1);
    }

    @FXML
    @Override
    public void copyToMyBoxClipboard() {
        SheetCopyToMyBoxClipboardController controller = (SheetCopyToMyBoxClipboardController) openChildStage(Fxmls.SheetCopyToMyBoxClipboardFxml, false);
        controller.setParameters((ControlSheet) this, -1, -1);
    }

    public void copyRowsCols(List<Integer> rows, List<Integer> cols, boolean withNames, boolean toSystemClipboard) {
        if (rows == null || rows.isEmpty() || cols == null || cols.isEmpty() || colsCheck == null) {
            popError(message("NoData"));
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                private String[][] data;

                @Override
                protected boolean handle() {
                    data = new String[rows.size()][cols.size()];
                    for (int r = 0; r < rows.size(); ++r) {
                        int row = rows.get(r);
                        for (int c = 0; c < cols.size(); c++) {
                            data[r][c] = cellString(row, cols.get(c));
                        }
                    }
                    if (toSystemClipboard) {
                        return data != null;
                    } else {
                        File dFile = DataClipboard.writeFile(data);
                        List<ColumnDefinition> dColumns = new ArrayList<>();
                        for (int c : cols) {
                            dColumns.add(columns.get(c));
                        }
                        DataDefinition def = DataClipboard.create(tableDataDefinition, tableDataColumn, dFile, dColumns);
                        return def != null;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    if (toSystemClipboard) {
                        List<String> colsNames = null;
                        if (withNames) {
                            colsNames = new ArrayList<>();
                            for (int c : cols) {
                                colsNames.add(colsCheck[c].getText());
                            }
                        }
                        TextClipboardTools.copyToSystemClipboard(myController,
                                TextTools.dataText(data, copyDelimiter + "", colsNames, null));
                    } else {
                        popSuccessful();
                        DataClipboardController.update();
                    }
                }

            };
            start(task);
        }
    }

    @FXML
    @Override
    public void myBoxClipBoard() {
        DataClipboardPopController.open((ControlSheet) this);
    }

    public void paste(ControlSheetCSV sourceController, int row, int col, boolean enlarge) {
        if (sourceController == null || sourceController.rowsTotal() == 0) {
            popError(message("NoData"));
            return;
        }
        if (sourceController.pagesNumber > 1 && !sourceController.checkBeforeNextAction()) {
            return;
        }
        String[][] data;
        pickData();
        sourceController.pickData();
        if (pageData == null) {
            if (enlarge) {
                if (sourceController.pagesNumber <= 1) {
                    data = sourceController.pageData;
                } else {
                    data = sourceController.readAll();
                }
                makeSheet(data);
            }
            return;
        }
        if (row < 0 || row > rowsNumber - 1) {
            row = rowsNumber - 1;
        }
        if (col < 0 || col > colsNumber - 1) {
            col = colsNumber - 1;
        }
        int sourceRowsSize = (int) sourceController.rowsTotal();
        int sourceColsSize = sourceController.colsNumber;
        if (pagesNumber <= 1
                || (row + sourceRowsSize <= rowsNumber && col + sourceColsSize <= colsNumber)) {
            pastePage(sourceController, row, col, enlarge);
        } else {
            pasteFile(sourceController, row, col, enlarge);
        }
    }

    public void pastePage(ControlSheetCSV sourceController, int row, int col, boolean enlarge) {
        String[][] source;
        if (sourceController.pagesNumber <= 1) {
            source = sourceController.pageData;
        } else {
            if (enlarge) {
                source = sourceController.readAll();
            } else {
                source = sourceController.read(rowsNumber - row, colsNumber - col);
            }
        }
        int sourceRowsSize = source.length, targetRowsSize = rowsNumber;
        int sourceColsSize = source[0].length, targetColsSize = colsNumber;
        if (enlarge && row + sourceRowsSize > rowsNumber) {
            targetRowsSize = row + sourceRowsSize;
        }
        if (enlarge && col + sourceColsSize > colsNumber) {
            targetColsSize = col + sourceColsSize;
        }
        String[][] values = new String[targetRowsSize][targetColsSize];
        for (int r = 0; r < targetRowsSize; r++) {
            for (int c = 0; c < targetColsSize; c++) {
                if (r >= row && r < row + sourceRowsSize && c >= col && c < col + sourceColsSize) {
                    values[r][c] = source[r - row][c - col];
                } else if (r < rowsNumber && c < colsNumber) {
                    values[r][c] = pageData[r][c];
                } else {
                    values[r][c] = defaultColValue;
                }
            }
        }
        makeSheet(values);
        popSuccessful();
    }

    @FXML
    public void setDataAction() {
        SheetEqualController controller = (SheetEqualController) openChildStage(Fxmls.SheetEqualFxml, false);
        controller.setParameters((ControlSheet) this, -1, -1);
    }

    public void setRowsCols(List<Integer> rows, List<Integer> cols, String value) {
        if (rows == null || rows.isEmpty() || cols == null || cols.isEmpty() || sheetInputs == null || columns == null) {
            popError(message("NoData"));
            return;
        }
        isSettingValues = true;
        Random random = null;
        for (int r = 0; r < rows.size(); ++r) {
            int row = rows.get(r);
            for (int c = 0; c < cols.size(); ++c) {
                int col = cols.get(c);
                String v = value;
                if (AppValues.MyBoxRandomFlag.equals(value)) {
                    if (random == null) {
                        random = new Random();
                    }
                    v = columns.get(col).random(random, maxRandom, scale);
                }
                sheetInputs[row][col].setText(v);
            }
        }
        isSettingValues = false;
        sheetChanged();
        popSuccessful();
    }

    @FXML
    public void sortDataAction() {
        SheetSortController controller = (SheetSortController) openChildStage(Fxmls.SheetSortFxml, false);
        controller.setParameters((ControlSheet) this, -1, -1);
    }

    protected void sortRows(List<Integer> rows, int col, boolean asc) {
        if (sheetInputs == null || columns == null
                || rows == null || rows.isEmpty() || col < 0 || col >= columns.size()) {
            popError(message("NoData"));
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                private String[][] sortedData;

                @Override
                protected boolean handle() {
                    Collections.sort(rows, new Comparator<Integer>() {
                        @Override
                        public int compare(Integer row1, Integer row2) {
                            if (row1 > row2) {
                                return 1;
                            } else if (row1 < row2) {
                                return -1;
                            } else {
                                return 0;
                            }
                        }
                    });

                    List<Integer> sortedRows = new ArrayList<>();
                    sortedRows.addAll(rows);
                    ColumnDefinition column = columns.get(col);
                    Collections.sort(sortedRows, new Comparator<Integer>() {
                        @Override
                        public int compare(Integer row1, Integer row2) {
                            int v = column.compare(cellString(row1, col), cellString(row2, col));
                            return asc ? v : -v;
                        }
                    });
                    int colsTotal = colsCheck.length;
                    int rowsSize = sortedRows.size();
                    sortedData = new String[rowsSize][colsTotal];
                    for (int r = 0; r < rowsSize; r++) {
                        int row = sortedRows.get(r);
                        for (int c = 0; c < colsTotal; c++) {
                            sortedData[r][c] = cellString(row, c);
                        }
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    isSettingValues = true;
                    int colsTotal = colsCheck.length;
                    for (int r = 0; r < rows.size(); r++) {
                        int row = rows.get(r);
                        for (int c = 0; c < colsTotal; c++) {
                            sheetInputs[row][c].setText(sortedData[r][c]);
                        }
                    }
                    isSettingValues = false;
                    sheetChanged();
                    popSuccessful();
                }

            };
            start(task);
        }
    }

    @FXML
    public void widthDataAction() {
        SheetWidthController controller = (SheetWidthController) openChildStage(Fxmls.SheetWidthFxml, false);
        controller.setParameters((ControlSheet) this, -1, -1);
    }

    public void widthCols(List<Integer> cols, int width) {
        if (cols == null || cols.isEmpty() || colsCheck == null || columns == null) {
            popError(message("NoData"));
            return;
        }
        for (int c = 0; c < cols.size(); ++c) {
            int col = cols.get(c);
            columns.get(col).setWidth(width);
            colsCheck[col].setPrefWidth(width);
            if (sheetInputs != null) {
                for (int r = 0; r < sheetInputs.length; ++r) {
                    sheetInputs[r][col].setPrefWidth(width);
                }
            }
        }
        columnsController.loadTableData();
        popSuccessful();
    }

    @FXML
    public void rowsAddAction() {
        SheetRowsAddController controller = (SheetRowsAddController) openChildStage(Fxmls.SheetRowsAddFxml, false);
        controller.setParameters((ControlSheet) this, -1, -1);
    }

    protected void addRows(int row, boolean above, int number) {
        try {
            if (number < 1 || columns == null || columns.isEmpty()) {
                return;
            }
            String[][] current = pickData();
            String[][] values;
            int cNumber = columns.size();
            if (current == null) {
                values = new String[number][cNumber];
                for (int r = 0; r < number; ++r) {
                    for (int c = 0; c < cNumber; ++c) {
                        values[r][c] = defaultColValue;
                    }
                }
            } else {
                int rNumber = current.length;
                values = new String[rowsNumber + number][cNumber];
                int base;
                if (row < 0) {
                    base = 0;
                } else {
                    base = row + (above ? 0 : 1);
                }
                for (int r = 0; r < base; ++r) {
                    for (int c = 0; c < cNumber; ++c) {
                        values[r][c] = current[r][c];
                    }
                }
                for (int r = base; r < base + number; ++r) {
                    for (int c = 0; c < cNumber; ++c) {
                        values[r][c] = defaultColValue;
                    }
                }
                for (int r = base + number; r < rNumber + number; ++r) {
                    for (int c = 0; c < cNumber; ++c) {
                        values[r][c] = current[r - number][c];
                    }
                }
            }
            makeSheet(values);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    @FXML
    public void rowsDeleteAction() {
        SheetRowsDeleteController controller = (SheetRowsDeleteController) openChildStage(Fxmls.SheetRowsDeleteFxml, false);
        controller.setParameters((ControlSheet) this, -1, -1);
    }

    public void deleteRows(List<Integer> rows) {
        if (rows == null || rows.isEmpty()) {
            popError(message("SelectToHandle"));
            return;
        }
        if (rowsCheck == null || columns == null || columns.isEmpty()) {
            popError(message("NoData"));
            return;
        }
        String[][] values = null;
        values = new String[rowsCheck.length - rows.size()][columns.size()];
        int rowIndex = 0;
        for (int r = 0; r < rowsCheck.length; ++r) {
            if (rows.contains(r)) {
                continue;
            }
            for (int c = 0; c < columns.size(); ++c) {
                values[rowIndex][c] = cellString(r, c);
            }
            rowIndex++;
        }
        makeSheet(values);
        popSuccessful();
    }

    public void deletePageRows() {
        makeSheet(null);
        popSuccessful();
    }

    public void deleteAllRows() {
        deletePageRows();
    }

    @FXML
    public void columnsAddAction() {
        SheetColumnsAddController controller = (SheetColumnsAddController) openChildStage(Fxmls.SheetColumnsAddFxml, false);
        controller.setParameters((ControlSheet) this, -1, -1);
    }

    protected void addCols(int col, boolean left, int number) {
        if (number < 1) {
            return;
        }
        if (columns == null) {
            columns = new ArrayList<>();
        }
        int base;
        if (col < 0) {
            base = 0;
        } else {
            base = col + (left ? 0 : 1);
        }
        makeColumns(base, number);
        String[][] current = pickData();
        if (current == null) {
            makeSheet(null);
            return;
        }
        int rNumber = current.length;
        int cNumber = current[0].length + number;
        String[][] values = new String[rNumber][cNumber];
        for (int r = 0; r < rNumber; ++r) {
            for (int c = 0; c < base; ++c) {
                values[r][c] = current[r][c];
            }
            for (int c = base; c < base + number; ++c) {
                values[r][c] = defaultColValue;
            }
            for (int c = base + number; c < cNumber; ++c) {
                values[r][c] = current[r][c - number];
            }
        }
        makeSheet(values);
    }

    @FXML
    public void columnsDeleteAction() {
        SheetColumnsDeleteController controller = (SheetColumnsDeleteController) openChildStage(Fxmls.SheetColumnsDeleteFxml, false);
        controller.setParameters((ControlSheet) this, -1, -1);
    }

    public void deleteCols(List<Integer> cols) {
        if (cols == null || cols.isEmpty() || columns == null || columns.isEmpty()) {
            popError(message("SelectToHandle"));
            return;
        }
        List<ColumnDefinition> leftColumns = new ArrayList<>();
        List<Integer> leftColumnsIndex = new ArrayList<>();
        for (int c = 0; c < columns.size(); ++c) {
            if (!cols.contains(c)) {
                leftColumnsIndex.add(c);
                leftColumns.add(columns.get(c));
            }
        }
        String[][] values = null;
        if (rowsCheck != null) {
            values = new String[rowsCheck.length][leftColumns.size()];
            for (int r = 0; r < rowsCheck.length; ++r) {
                for (int c = 0; c < leftColumnsIndex.size(); ++c) {
                    values[r][c] = cellString(r, leftColumnsIndex.get(c));
                }
            }
        }
        makeSheet(values, leftColumns);
        popSuccessful();
    }

    public void deleteAllCols() {
        if (columns != null) {
            columns.clear();
        }
        makeSheet(null);
        popSuccessful();
    }

    @FXML
    public void calculateDataAction() {
        SheetCalculateController controller = (SheetCalculateController) openChildStage(Fxmls.SheetCalculateFxml, false);
        controller.setParameters((ControlSheet) this, -1, -1);
    }

    @FXML
    public void exportSheetAction() {
        SheetExportController controller = (SheetExportController) openChildStage(Fxmls.SheetExportFxml, false);
        controller.dataController.setParameters((ControlSheet) this, -1, -1);
    }

    public boolean exportRowsCols(SheetExportController exportController, List<Integer> rows, List<Integer> cols, boolean skip) {
        try {
            if (exportController == null) {
                return false;
            }
            if (rows == null || rows.isEmpty() || cols == null || cols.isEmpty()) {
                if (exportController.task != null) {
                    exportController.task.setError(message("NoData"));
                }
                return false;
            }
            List<String> names = new ArrayList<>();
            for (int c : cols) {
                names.add(columns.get(c).getName());
            }
            exportController.convertController.names = names;
            exportController.convertController.openWriters(exportController.filePrefix, skip);
            for (int r = 0; r < rows.size(); ++r) {
                if (exportController.task == null || exportController.task.isCancelled()) {
                    return false;
                }
                int row = rows.get(r);
                List<String> rowData = new ArrayList<>();
                for (int c = 0; c < cols.size(); c++) {
                    rowData.add(cellString(row, cols.get(c)));
                }
                exportController.convertController.writeRow(rowData);
            }
            exportController.convertController.closeWriters();
            return true;
        } catch (Exception e) {
            if (exportController.task != null) {
                exportController.task.setError(e.toString());
            }
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    @FXML
    public void csvAction() {
        if (sheetInputs == null || sheetInputs.length < 1) {
            popError(message("NoData"));
            return;
        }
        synchronized (this) {
            SingletonTask csvTask = new SingletonTask<Void>(this) {
                File tmpFile;

                @Override
                protected boolean handle() {
                    tmpFile = TmpFileTools.getTempFile(".csv");
                    CSVFormat csvFormat = CSVFormat.DEFAULT
                            .withDelimiter(',').withFirstRecordAsHeader()
                            .withIgnoreEmptyLines().withTrim().withNullString("");
                    try ( CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(tmpFile, Charset.forName("UTF-8")), csvFormat)) {
                        csvPrinter.printRecord(columnNames());
                        for (int r = 0; r < sheetInputs.length; r++) {
                            csvPrinter.printRecord(row(r));
                        }
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return tmpFile != null && tmpFile.exists();
                }

                @Override
                protected void whenSucceeded() {
                    DataFileCSVController.open(tmpFile, Charset.forName("UTF-8"), true, ',');
                }

            };
            start(csvTask, false);
        }
    }

    @FXML
    public void excelAction() {
        if (sheetInputs == null || sheetInputs.length < 1) {
            popError(message("NoData"));
            return;
        }
        synchronized (this) {
            SingletonTask excelTask = new SingletonTask<Void>(this) {
                File tmpFile;

                @Override
                protected boolean handle() {
                    tmpFile = TmpFileTools.getTempFile(".xlsx");
                    try ( Workbook targetBook = new XSSFWorkbook();
                             FileOutputStream fileOut = new FileOutputStream(tmpFile)) {
                        Sheet targetSheet = targetBook.createSheet();
                        int index = 0;
                        List<String> names = columnNames();
                        Row targetRow = targetSheet.createRow(index++);
                        for (int c = 0; c < names.size(); c++) {
                            Cell targetCell = targetRow.createCell(c, CellType.STRING);
                            targetCell.setCellValue(names.get(c));
                        }
                        for (int r = 0; r < sheetInputs.length; r++) {
                            targetRow = targetSheet.createRow(index++);
                            for (int c = 0; c < sheetInputs[r].length; c++) {
                                Cell targetCell = targetRow.createCell(c, CellType.STRING);
                                targetCell.setCellValue(cellString(r, c));
                            }
                        }
                        targetBook.write(fileOut);
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return tmpFile != null && tmpFile.exists();
                }

                @Override
                protected void whenSucceeded() {
                    DataFileExcelController.open(tmpFile, true);
                }

            };
            start(excelTask, false);
        }
    }

}
