package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.tools.FileTools;
import static mara.mybox.value.Languages.message;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

/**
 * @Author Mara
 * @CreateDate 2021-8-28
 * @License Apache License Version 2.0
 */
public abstract class ControlSheetFile_Operations extends ControlSheetFile_File {

    protected abstract File fileCopyCols(List<Integer> cols, boolean withNames);

    protected abstract File fileSetCols(List<Integer> cols, String value);

    protected abstract File fileSortCol(int col, boolean asc);

    protected abstract File fileAddCols(int col, boolean left, int number);

    protected abstract File fileDeleteAll(boolean keepCols);

    protected abstract File fileDeleteCols(List<Integer> cols);

    protected abstract File filePaste(ControlSheetCSV sourceController, int row, int col, boolean enlarge);

    protected abstract String fileText();

    protected abstract String fileHtml();

    @Override
    public void newSheet(int rows, int cols) {
        try {
            sourceFile = null;
            initCurrentPage();
            initFile();
            super.newSheet(rows, cols);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadData(String[][] data, List<ColumnDefinition> dataColumns) {
        try {
            sourceFile = null;
            initCurrentPage();
            initFile();
            makeSheet(data, dataColumns);;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void copyCols(List<Integer> cols, boolean withNames, boolean toSystemClipboard) {
        if (cols == null || cols.isEmpty()) {
            popError(message("NoData"));
            return;
        }
        if (sourceFile == null || pagesNumber <= 1) {
            copyRowsCols(rowsIndex(true), cols, withNames, toSystemClipboard);
            return;
        }
        synchronized (this) {
            SingletonTask copyTask = new SingletonTask<Void>(this) {

                private File file;

                @Override
                protected boolean handle() {
                    file = fileCopyCols(cols, withNames);
                    if (file == null || !file.exists()) {
                        return false;
                    }
                    if (toSystemClipboard) {
                        return true;
                    } else {
                        List<ColumnDefinition> dColumns = new ArrayList<>();
                        for (int c : cols) {
                            dColumns.add(columns.get(c));
                        }
//                        DataClipboard.create(tableDataDefinition, tableDataColumn, file, dColumns);
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (toSystemClipboard) {
                        TextClipboardTools.copyFileToSystemClipboard(parentController, file);
                    } else {
                        popSuccessful();
                        DataClipboardController.update();
                    }
                }

            };
            start(copyTask, false);
        }
    }

    @Override
    public void setCols(List<Integer> cols, String value) {
        if (cols == null || cols.isEmpty()) {
            popError(message("NoData"));
            return;
        }
        if (sourceFile == null || pagesNumber <= 1) {
            setRowsCols(rowsIndex(true), cols, value);
            return;
        }
        if (!checkBeforeNextAction()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                private File file;

                @Override
                protected boolean handle() {
                    file = fileSetCols(cols, value);
                    if (file == null || !file.exists()) {
                        return false;
                    }
                    backup();
                    return FileTools.rename(file, sourceFile);
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    loadPage(currentPage);
                }

            };
            start(task);
        }
    }

    @Override
    public void sort(int col, boolean asc) {
        if (sourceFile == null || pagesNumber <= 1) {
            sortRows(rowsIndex(true), col, asc);
            return;
        }
        if (columns == null || col < 0 || col >= columns.size()
                || !checkBeforeNextAction() || totalSize <= 0) {
            return;
        }
        if (!PopTools.askSure(baseTitle, colName(col) + " - "
                + (asc ? message("Ascending") : message("Descending")) + "\n"
                + message("DataFileOrderNotice"))) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                private File file;

                @Override
                protected boolean handle() {
                    file = fileSortCol(col, asc);
                    if (file == null || !file.exists()) {
                        return false;
                    }
                    backup();
                    return FileTools.rename(file, sourceFile);
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    loadPage(currentPage);
                }

            };
            start(task);
        }
    }

    @Override
    public void deleteAllRows() {
        if (sourceFile == null || pagesNumber <= 1) {
            deletePageRows();
            return;
        }
        deleteAll(true);
    }

    public void deleteAll(boolean keepCols) {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                private File file;

                @Override
                protected boolean handle() {
                    file = fileDeleteAll(keepCols);
                    if (file == null || !file.exists()) {
                        return false;
                    }
                    backup();
                    return FileTools.rename(file, sourceFile);
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    loadFile();
                }

            };
            start(task);
        }
    }

    @Override
    protected void addCols(int col, boolean left, int number) {
        if (number < 1) {
            return;
        }
        if (sourceFile == null || pagesNumber <= 1) {
            super.addCols(col, left, number);
            return;
        }
        if (!checkBeforeNextAction()) {
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
                    int base;
                    if (col < 0) {
                        base = 0;
                    } else {
                        base = col + (left ? 0 : 1);
                    }
                    makeColumns(base, number);
                    saveDefinition();
                    File tmpFile = fileAddCols(col, left, number);
                    if (tmpFile == null || !tmpFile.exists()) {
                        return false;
                    }
                    backup();
                    return FileTools.rename(tmpFile, sourceFile);
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    loadFile();
                }

            };
            start(task);
        }
    }

    @Override
    public void deleteAllCols() {
        if (sourceFile == null || pagesNumber <= 1) {
            super.deleteAllCols();
            return;
        }
        deleteAll(false);
    }

    @Override
    public void deleteCols(List<Integer> cols) {
        if (sourceFile == null || pagesNumber <= 1) {
            super.deleteCols(cols);
            return;
        }
        if (cols == null || cols.isEmpty()) {
            popError(message("SelectToHandle"));
            return;
        }
        if (!checkBeforeNextAction()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    List<ColumnDefinition> leftColumns = new ArrayList<>();
                    for (int i = 0; i < columns.size(); ++i) {
                        if (!cols.contains(i)) {
                            leftColumns.add(columns.get(i));
                        }
                    }
                    columns = leftColumns;
                    saveDefinition();
                    File tmpFile = fileDeleteCols(cols);
                    if (tmpFile == null || !tmpFile.exists()) {
                        return false;
                    }
                    backup();
                    return FileTools.rename(tmpFile, sourceFile);
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    dataChangedNotify.set(false);
                    loadPage(currentPage);
                }

            };
            start(task);
        }
    }

    @Override
    public void pasteFile(ControlSheetCSV sourceController, int row, int col, boolean enlarge) {
        if (sourceController == null || sourceController.rowsTotal() == 0) {
            popError(message("NoData"));
            return;
        }
        if (sourceFile == null || (pagesNumber > 1 && !checkBeforeNextAction())) {
            return;
        }
        if (sourceController.sourceFile == null
                || (sourceController.pagesNumber > 1 && !sourceController.checkBeforeNextAction())) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    if (enlarge) {
                        int diff = col + sourceController.colsNumber - colsNumber;
                        if (diff > 0) {
                            makeColumns(col, diff);
                            saveDefinition();
                        }
                    }
                    File tmpFile = filePaste(sourceController, row, col, enlarge);
                    if (tmpFile == null || !tmpFile.exists()) {
                        return false;
                    }
                    backup();
                    return FileTools.rename(tmpFile, sourceFile);
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    dataChangedNotify.set(false);
                    loadPage(currentPage);
                }

            };
            start(task);
        }
    }

    @Override
    public boolean exportCols(SheetExportController exportController, List<Integer> cols, boolean skip) {
        if (exportController == null) {
            return false;
        }
        File file = fileCopyCols(cols, true);
        if (file == null || !file.exists()) {
            if (exportController.task != null) {
                exportController.task.setError(message("NoData"));
            }
            return false;
        }
        CSVFormat csvFormat = CSVFormat.DEFAULT.withFirstRecordAsHeader().withDelimiter(',')
                .withIgnoreEmptyLines().withTrim().withNullString("");
        try ( CSVParser parser = CSVParser.parse(file, Charset.forName("UTF-8"), csvFormat)) {
            List<String> names = parser.getHeaderNames();
            exportController.convertController.setParameters(exportController.filePrefix, skip);
            for (CSVRecord record : parser) {
                if (exportController.task == null || exportController.task.isCancelled()) {
                    return false;
                }
                List<String> rowData = new ArrayList<>();
                for (String name : names) {
                    rowData.add(record.get(name));
                }
                exportController.convertController.writeRow(rowData);
            }
            exportController.convertController.closeWriters();
        } catch (Exception e) {
            if (exportController.task != null) {
                exportController.task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return false;
        }
        return true;
    }

    @Override
    protected void displayAllText() {
        if (sourceFile == null || pagesNumber <= 1) {
            displayPageText();
            return;
        }
        synchronized (this) {
            SingletonTask textTask = new SingletonTask<Void>(this) {
                String text;

                @Override
                protected boolean handle() {
                    text = fileText();
                    return text != null;
                }

                @Override
                protected void whenSucceeded() {
                    textsDisplayArea.setText(text);
                }

            };
            start(textTask, false);
        }
    }

    @Override
    protected void displayAllHtml() {
        if (sourceFile == null || pagesNumber <= 1) {
            displayPageHtml();
            return;
        }
        synchronized (this) {
            SingletonTask textTask = new SingletonTask<Void>(this) {
                String html;

                @Override
                protected boolean handle() {
                    html = fileHtml();
                    return html != null;
                }

                @Override
                protected void whenSucceeded() {
                    htmlViewController.webEngine.loadContent(html);
                }

            };
            start(textTask, false);
        }
    }

    protected void copyPageData(CSVPrinter csvPrinter, List<Integer> cols) {
        try {
            if (csvPrinter == null || sheetInputs == null || cols == null || cols.isEmpty()) {
                return;
            }
            String d;
            for (int r = 0; r < sheetInputs.length; r++) {
                List<String> values = new ArrayList<>();
                int colsSize = sheetInputs[r].length;
                for (int c : cols) {
                    if (c > colsSize) {
                        d = defaultColValue;
                    } else {
                        d = cellString(r, c);
                    }
                    values.add(d);
                }
                csvPrinter.printRecord(values);
            }
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
    }

    protected void copyPageData(List<List<String>> rows, List<Integer> cols) {
        try {
            if (rows == null || sheetInputs == null || cols == null || cols.isEmpty()) {
                return;
            }
            for (int r = 0; r < sheetInputs.length; r++) {
                List<String> row = new ArrayList<>();
                int colsSize = sheetInputs[r].length;
                for (int c : cols) {
                    if (c > colsSize) {
                        row.add(defaultColValue);
                    } else {
                        row.add(cellString(r, c));
                    }
                }
                rows.add(row);
            }
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
    }

}
