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
import mara.mybox.data.Data2D;
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
public abstract class ControlData2DEditTable_Operations extends BaseDataTableController<List<String>> {

    protected ControlData2D dataController;
    protected ControlData2DEdit editController;
    protected Data2D data2D;
    protected boolean changed;
    protected char copyDelimiter = ',';

    protected String cellString(int row, int col) {
        return null;
    }

    public List<Integer> rowsIndex(boolean isAll) {
        return null;
    }

    public boolean validateChange() {
        return true;
    }

//    protected abstract String[][] allRows(List<Integer> cols);
//    public abstract void copyCols(List<Integer> cols, boolean withNames, boolean toSystemClipboard);
//
//    public abstract void setCols(List<Integer> cols, String value);
//
//    public abstract void sort(int col, boolean asc);
//
//    public abstract void pasteFile(ControlSheetCSV sourceController, int row, int col, boolean enlarge);
//    public abstract boolean exportCols(SheetExportController exportController, List<Integer> cols, boolean skip);
    public String[][] data(List<Integer> rows, List<Integer> cols) {
        if (rows == null || rows.isEmpty() || cols == null || cols.isEmpty() || data2D.getColumns() == null) {
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
    public void validateData() {
        if (validateChange()) {
            popInformation(message("DataAreValid"));
        }
    }

    @FXML
    @Override
    public void copyToSystemClipboard() {
        SheetCopyToSystemClipboardController controller = (SheetCopyToSystemClipboardController) openChildStage(Fxmls.SheetCopyToSystemClipboardFxml, false);
//        controller.setParameters((ControlSheet) this, -1, -1);
    }

    @FXML
    @Override
    public void copyToMyBoxClipboard() {
        SheetCopyToMyBoxClipboardController controller = (SheetCopyToMyBoxClipboardController) openChildStage(Fxmls.SheetCopyToMyBoxClipboardFxml, false);
//        controller.setParameters((ControlSheet) this, -1, -1);
    }

    public void copyRowsCols(List<Integer> rows, List<Integer> cols, boolean withNames, boolean toSystemClipboard) {
        if (rows == null || rows.isEmpty() || cols == null || cols.isEmpty()) {
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
                            dColumns.add(data2D.column(c));
                        }
                        DataDefinition def
                                = DataClipboard.create(data2D.getTableDataDefinition(), data2D.getTableDataColumn(), dFile, dColumns);
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
                                colsNames.add(data2D.colName(c));
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
//        DataClipboardPopController.open((ControlSheet) this);
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
//        pickData();
        sourceController.pickData();
        if (data2D.getPageData() == null) {
            if (enlarge) {
                if (sourceController.pagesNumber <= 1) {
                    data = sourceController.pageData;
                } else {
                    data = sourceController.readAll();
                }
//                makeSheet(data); // ####
            }
            return;
        }
        if (row < 0 || row > data2D.pageRowsNumber() - 1) {
            row = data2D.pageRowsNumber() - 1;
        }
        if (col < 0 || col > data2D.pageColsNumber() - 1) {
            col = data2D.pageColsNumber() - 1;
        }
        int sourceRowsSize = (int) sourceController.rowsTotal();
        int sourceColsSize = sourceController.colsNumber;
        if (data2D.getPagesNumber() <= 1
                || (row + sourceRowsSize <= data2D.pageRowsNumber() && col + sourceColsSize <= data2D.pageColsNumber())) {
            pastePage(sourceController, row, col, enlarge);
        } else {
//            pasteFile(sourceController, row, col, enlarge);
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
                source = sourceController.read(data2D.pageRowsNumber() - row, data2D.pageColsNumber() - col);
            }
        }
        int sourceRowsSize = source.length, targetRowsSize = data2D.pageRowsNumber();
        int sourceColsSize = source[0].length, targetColsSize = data2D.pageColsNumber();
        if (enlarge && row + sourceRowsSize > data2D.pageRowsNumber()) {
            targetRowsSize = row + sourceRowsSize;
        }
        if (enlarge && col + sourceColsSize > data2D.pageColsNumber()) {
            targetColsSize = col + sourceColsSize;
        }
        String[][] values = new String[targetRowsSize][targetColsSize];
        for (int r = 0; r < targetRowsSize; r++) {
            for (int c = 0; c < targetColsSize; c++) {
                if (r >= row && r < row + sourceRowsSize && c >= col && c < col + sourceColsSize) {
                    values[r][c] = source[r - row][c - col];
                } else if (r < data2D.pageRowsNumber() && c < data2D.pageColsNumber()) {
                    values[r][c] = data2D.cell(r, c);
                } else {
                    values[r][c] = data2D.defaultColValue();
                }
            }
        }
//        makeSheet(values); // ####
        popSuccessful();
    }

    @FXML
    public void setDataAction() {
        SheetEqualController controller = (SheetEqualController) openChildStage(Fxmls.SheetEqualFxml, false);
//        controller.setParameters((ControlSheet) this, -1, -1);
    }

    public void setRowsCols(List<Integer> rows, List<Integer> cols, String value) {
        if (rows == null || rows.isEmpty() || cols == null || cols.isEmpty() || !data2D.isColumnsValid()) {
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
                    v = data2D.random(random, col);
                }
//                sheetInputs[row][col].setText(v); // #####
            }
        }
        isSettingValues = false;
//        sheetChanged();  // #####
        popSuccessful();
    }

    @FXML
    public void sortDataAction() {
        SheetSortController controller = (SheetSortController) openChildStage(Fxmls.SheetSortFxml, false);
//        controller.setParameters((ControlSheet) this, -1, -1);
    }

    protected void sortRows(List<Integer> rows, int col, boolean asc) {
        if (rows == null || rows.isEmpty() || col < 0 || col >= data2D.columnsNumber()) {
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
                    ColumnDefinition column = data2D.column(col);
                    Collections.sort(sortedRows, new Comparator<Integer>() {
                        @Override
                        public int compare(Integer row1, Integer row2) {
                            int v = column.compare(cellString(row1, col), cellString(row2, col));
                            return asc ? v : -v;
                        }
                    });
                    int colsTotal = data2D.columnsNumber();
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
                    int colsTotal = data2D.columnsNumber();
                    for (int r = 0; r < rows.size(); r++) {
                        int row = rows.get(r);
                        for (int c = 0; c < colsTotal; c++) {
//                            sheetInputs[row][c].setText(sortedData[r][c]);
                        }
                    }
                    isSettingValues = false;
//                    sheetChanged();
                    popSuccessful();
                }

            };
            start(task);
        }
    }

    @FXML
    public void widthDataAction() {
        SheetWidthController controller = (SheetWidthController) openChildStage(Fxmls.SheetWidthFxml, false);
//        controller.setParameters((ControlSheet) this, -1, -1);
    }

    public void widthCols(List<Integer> cols, int width) {
        if (cols == null || cols.isEmpty() || !data2D.isColumnsValid()) {
            popError(message("NoData"));
            return;
        }
        for (int c = 0; c < cols.size(); ++c) {
            int col = cols.get(c);
            data2D.column(col).setWidth(width);
//            colsCheck[col].setPrefWidth(width);
//            if (sheetInputs != null) {
//                for (int r = 0; r < sheetInputs.length; ++r) {
//                    sheetInputs[r][col].setPrefWidth(width);
//                }
//            }
        }
//        columnsController.loadTableData();
        popSuccessful();
    }

    @FXML
    public void rowsAddAction() {
        SheetRowsAddController controller = (SheetRowsAddController) openChildStage(Fxmls.SheetRowsAddFxml, false);
//        controller.setParameters((ControlSheet) this, -1, -1);
    }

    protected void addRows(int row, boolean above, int number) {
        try {
            if (number < 1 || !data2D.isColumnsValid()) {
                return;
            }
            List<List<String>> current = data2D.getPageData();
            String[][] values;
            int cNumber = data2D.columnsNumber();
            if (current == null) {
                values = new String[number][cNumber];
                for (int r = 0; r < number; ++r) {
                    for (int c = 0; c < cNumber; ++c) {
                        values[r][c] = data2D.defaultColValue();
                    }
                }
            } else {
                int rNumber = current.size();
                values = new String[data2D.pageRowsNumber() + number][cNumber];
                int base;
                if (row < 0) {
                    base = 0;
                } else {
                    base = row + (above ? 0 : 1);
                }
                for (int r = 0; r < base; ++r) {
                    for (int c = 0; c < cNumber; ++c) {
                        values[r][c] = data2D.cell(r, c);
                    }
                }
                for (int r = base; r < base + number; ++r) {
                    for (int c = 0; c < cNumber; ++c) {
                        values[r][c] = data2D.defaultColValue();
                    }
                }
                for (int r = base + number; r < rNumber + number; ++r) {
                    for (int c = 0; c < cNumber; ++c) {
                        values[r][c] = data2D.cell(r - number, c);
                    }
                }
            }
//            makeSheet(values);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    @FXML
    public void rowsDeleteAction() {
        SheetRowsDeleteController controller = (SheetRowsDeleteController) openChildStage(Fxmls.SheetRowsDeleteFxml, false);
//        controller.setParameters((ControlSheet) this, -1, -1);
    }

    public void deleteRows(List<Integer> rows) {
        if (rows == null || rows.isEmpty()) {
            popError(message("SelectToHandle"));
            return;
        }
        if (!data2D.hasData()) {
            popError(message("NoData"));
            return;
        }
        String[][] values = null;
        values = new String[data2D.pageRowsNumber() - rows.size()][data2D.columnsNumber()];
        int rowIndex = 0;
        for (int r = 0; r < data2D.pageRowsNumber(); ++r) {
            if (rows.contains(r)) {
                continue;
            }
            for (int c = 0; c < data2D.columnsNumber(); ++c) {
                values[rowIndex][c] = cellString(r, c);
            }
            rowIndex++;
        }
//        makeSheet(values);
        popSuccessful();
    }

    public void deletePageRows() {
//        makeSheet(null);
        popSuccessful();
    }

    public void deleteAllRows() {
        deletePageRows();
    }

    @FXML
    public void columnsAddAction() {
        SheetColumnsAddController controller = (SheetColumnsAddController) openChildStage(Fxmls.SheetColumnsAddFxml, false);
//        controller.setParameters((ControlSheet) this, -1, -1);
    }

    protected void addCols(int col, boolean left, int number) {
        if (number < 1) {
            return;
        }
        if (data2D.getColumns() == null) {
            data2D.setColumns(new ArrayList<>());
        }
        int base;
        if (col < 0) {
            base = 0;
        } else {
            base = col + (left ? 0 : 1);
        }
//        makeColumns(base, number);
        int rNumber = data2D.pageRowsNumber();
        int cNumber = data2D.pageColsNumber() + number;
        String[][] values = new String[rNumber][cNumber];
        for (int r = 0; r < rNumber; ++r) {
            for (int c = 0; c < base; ++c) {
                values[r][c] = data2D.cell(r, c);
            }
            for (int c = base; c < base + number; ++c) {
                values[r][c] = data2D.defaultColValue();
            }
            for (int c = base + number; c < cNumber; ++c) {
                values[r][c] = data2D.cell(r, c - number);
            }
        }
//        makeSheet(values);
    }

    @FXML
    public void columnsDeleteAction() {
        SheetColumnsDeleteController controller = (SheetColumnsDeleteController) openChildStage(Fxmls.SheetColumnsDeleteFxml, false);
//        controller.setParameters((ControlSheet) this, -1, -1);
    }

    public void deleteCols(List<Integer> cols) {
        if (cols == null || cols.isEmpty() || !data2D.isColumnsValid()) {
            popError(message("SelectToHandle"));
            return;
        }
        List<ColumnDefinition> leftColumns = new ArrayList<>();
        List<Integer> leftColumnsIndex = new ArrayList<>();
        for (int c = 0; c < data2D.columnsNumber(); ++c) {
            if (!cols.contains(c)) {
                leftColumnsIndex.add(c);
                leftColumns.add(data2D.column(c));
            }
        }
        String[][] values = null;
        int rowsNumber = data2D.pageRowsNumber();
        if (rowsNumber > 0) {
            values = new String[rowsNumber][leftColumns.size()];
            for (int r = 0; r < rowsNumber; ++r) {
                for (int c = 0; c < leftColumnsIndex.size(); ++c) {
                    values[r][c] = cellString(r, leftColumnsIndex.get(c));
                }
            }
        }
//        makeSheet(values, leftColumns);
        popSuccessful();
    }

    public void deleteAllCols() {
        if (data2D.getColumns() == null) {
            data2D.setColumns(new ArrayList<>());
        }
//        makeSheet(null);
        popSuccessful();
    }

    @FXML
    public void calculateDataAction() {
        SheetCalculateController controller = (SheetCalculateController) openChildStage(Fxmls.SheetCalculateFxml, false);
//        controller.setParameters((ControlSheet) this, -1, -1);
    }

    @FXML
    public void exportSheetAction() {
        SheetExportController controller = (SheetExportController) openChildStage(Fxmls.SheetExportFxml, false);
//        controller.dataController.setParameters((ControlSheet) this, -1, -1);
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
                names.add(data2D.colName(c));
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
        if (data2D.pageRowsNumber() <= 0) {
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
                        csvPrinter.printRecord(data2D.columnNames());
                        for (int r = 0; r < data2D.pageRowsNumber(); r++) {
                            csvPrinter.printRecord(data2D.rowList(r));
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
        if (data2D.pageRowsNumber() <= 0) {
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
                        List<String> names = data2D.columnNames();
                        Row targetRow = targetSheet.createRow(index++);
                        for (int c = 0; c < names.size(); c++) {
                            Cell targetCell = targetRow.createCell(c, CellType.STRING);
                            targetCell.setCellValue(names.get(c));
                        }
                        for (int r = 0; r < data2D.pageRowsNumber(); r++) {
                            targetRow = targetSheet.createRow(index++);
                            for (int c = 0; c < data2D.rowList(r).size(); c++) {
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
