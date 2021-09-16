package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.DataClipboard;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.tools.FileTools;
import static mara.mybox.value.Languages.message;
import org.apache.commons.csv.CSVPrinter;

/**
 * @Author Mara
 * @CreateDate 2021-8-28
 * @License Apache License Version 2.0
 */
public abstract class ControlSheetFile_Sheet extends ControlSheetFile_File {

    protected abstract File fileCopyCols(List<Integer> cols, boolean withNames);

    protected abstract File fileSetCols(List<Integer> cols, String value);

    protected abstract File fileSortCol(int col, boolean asc);

    protected abstract File fileAddCols(int col, boolean left, int number);

    protected abstract File fileDeleteAll(boolean keepCols);

    protected abstract File fileDeleteCols(List<Integer> cols);

    protected abstract File filePaste(ControlSheetCSV sourceController, int row, int col, boolean enlarge);

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
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

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
                        DataClipboard.create(tableDataDefinition, tableDataColumn, file, dColumns);
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
            start(task);
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
            task = new SingletonTask<Void>() {

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
            task = new SingletonTask<Void>() {

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
            task = new SingletonTask<Void>() {

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
            addCols(col, left, number);
            return;
        }
        if (!checkBeforeNextAction()) {
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
            popError(message("NoSelection"));
            return;
        }
        if (!checkBeforeNextAction()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

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
            task = new SingletonTask<Void>() {

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

    protected void copyPageData(CSVPrinter csvPrinter, List<Integer> cols) {
        try {
            if (csvPrinter == null || sheetInputs == null || cols == null || cols.isEmpty()) {
                return;
            }
            for (int r = 0; r < sheetInputs.length; r++) {
                List<String> values = new ArrayList<>();
                int colsSize = sheetInputs[r].length;
                for (int c : cols) {
                    if (c > colsSize) {
                        break;
                    }
                    values.add(cellString(r, c));
                }
                csvPrinter.printRecord(values);
            }
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
    }

}
