package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.tools.FileTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-8-28
 * @License Apache License Version 2.0
 */
public abstract class ControlSheetFile_Sheet extends ControlSheetFile_File {

    protected abstract File deleteFileSelectedColsDo();

    protected abstract File setFileSelectedColsDo(String value);

    protected abstract File setFileColValuesDo(int col, String value);

    protected abstract File fileSelectedCols(List<Integer> cols);

    protected abstract File pasteFileColValuesDo(int col);

    protected abstract File insertFileColDo(int col, boolean left, int number);

    protected abstract File DeleteFileColDo(int col);

    protected abstract File deleteFileAllCols();

    protected abstract File orderFileColDo(int col, boolean asc);

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

    // columns have been changed before call this
    @Override
    protected void deleteSelectedCols() {
        if (sourceFile == null || pagesNumber <= 1) {
            super.deleteSelectedCols();
            return;
        }
        if (!checkBeforeNextAction()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            if (!PopTools.askSure(baseTitle, message("DeleteSelectedCols") + "\n"
                    + message("NoticeAllChangeUnrecover"))) {
                return;
            }
            task = new BaseController_Attributes.SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    saveColumns();
                    File tmpFile = deleteFileSelectedColsDo();
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
            handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @Override
    public void copyFileSelectedCols() {
        if (colsCheck == null) {
            popError(message("NoData"));
            return;
        }
        if (sourceFile == null || pagesNumber <= 1) {
            copyPageSelectedColsToDataClipboard();
            return;
        }
        List<ColumnDefinition> selectedColumns = new ArrayList<>();
        for (int c = 0; c < colsCheck.length; c++) {
            if (colsCheck[c].isSelected()) {
                selectedColumns.add(columns.get(c));
            }
        }
        if (selectedColumns.isEmpty()) {
            popError(message("NoData"));
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
                    file = fileSelectedCols();
                    return file != null;
                }

                @Override
                protected void whenSucceeded() {
                    DataFileCSVController.open(file, true, ',');
                }

            };
            handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @Override
    protected void addColsNumber() {
        if (sourceFile == null || pagesNumber <= 1) {
            super.addColsNumber();
            return;
        }
        if (!checkBeforeNextAction()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            String value = askValue(message("NoticeAllChangeUnrecover"), message("AddColsNumber"), "1");
            int number;
            try {
                number = Integer.parseInt(value);
            } catch (Exception e) {
                popError(message("InvalidData"));
                return;
            }
            task = new SingletonTask<Void>() {
                StringBuilder s;

                @Override
                protected boolean handle() {
                    int col = colsCheck == null ? 0 : colsCheck.length;
                    makeColumns(col, number);
                    saveColumns();
                    File tmpFile = insertFileColDo(col, true, number);
                    if (tmpFile == null || !tmpFile.exists()) {
                        return false;
                    }
                    backup();
                    return FileTools.rename(tmpFile, sourceFile);
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    loadPage(currentPage);
                }

            };
            handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @Override
    protected void setFileSelectedCols() {
        if (sourceFile == null || pagesNumber <= 1) {
            setPageSelectedCols();
            return;
        }
        if (!checkBeforeNextAction() || totalSize <= 0) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            String value = askValue(message("NoticeAllChangeUnrecover"), message("SetFileSelectedColsValues"), defaultColValue);
            if (value == null) {
                return;
            }
            task = new SingletonTask<Void>() {
                StringBuilder s;

                @Override
                protected boolean handle() {
                    File tmpFile = setFileSelectedColsDo(value);
                    if (tmpFile == null || !tmpFile.exists()) {
                        return false;
                    }
                    backup();
                    return FileTools.rename(tmpFile, sourceFile);
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    loadPage(currentPage);
                }

            };
            handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @Override
    protected void setFileColValues(int col) {
        if (sourceFile == null || pagesNumber <= 1) {
            setPageColValues(col);
        }
        if (!checkBeforeNextAction() || totalSize <= 0) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            String value = askValue(colName(col) + "\n" + message("NoticeAllChangeUnrecover"),
                    message("SetAllColValues"), defaultColValue);
            if (!cellValid(col, value)) {
                popError(message("InvalidData"));
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    File tmpFile = setFileColValuesDo(col, value);
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
            handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @Override
    protected void copyFileColValues(int col) {
        if (col < 0 || col >= colsCheck.length) {
            return;
        }
        if (sourceFile == null || pagesNumber <= 1) {
            copyPageColValues(col);
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                File file;

                @Override
                protected boolean handle() {
//                    copiedCol = new ArrayList<>();
                    List<Integer> cols = new ArrayList<>();
                    cols.add(col);
                    file = fileSelectedCols(cols);
                    return file != null;
                }

                @Override
                protected void whenSucceeded() {
                    DataFileCSVController.open(file, true, ',');
                }

            };
            handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    protected File fileSelectedCols() {
        if (sourceFile == null) {
            return null;
        }
        List<Integer> cols = new ArrayList<>();
        for (int c = 0; c < colsCheck.length; ++c) {
            if (colsCheck[c].isSelected()) {
                cols.add(c);
            }
        }
        return fileSelectedCols(cols);
    }

    @Override
    protected void pasteFileColValues(int col) {
//        if (copiedCol == null || copiedCol.isEmpty()) {
//            popError(message("NoData"));
//            return;
//        }
        if (sourceFile == null || pagesNumber <= 1) {
            pastePageColValues(col);
            return;
        }
        if (!checkBeforeNextAction() || totalSize <= 0) {
            return;
        }
        if (!PopTools.askSure(baseTitle, colName(col) + " - " + message("PasteFileCol") + "\n"
                + message("NoticeAllChangeUnrecover"))) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    File tmpFile = pasteFileColValuesDo(col);
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
            handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @Override
    protected void insertFileCol(int col, boolean left, int number) {
        if (number < 1) {
            return;
        }
        if (sourceFile == null || pagesNumber <= 1) {
            insertPageCol(col, left, number);
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
                    int base = col + (left ? 0 : 1);
                    makeColumns(base, number);
                    saveColumns();
                    File tmpFile = insertFileColDo(col, left, number);
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
            handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @Override
    protected void deleteFileCol(int col) {
        if (sourceFile == null || pagesNumber <= 1) {
            deletePageCol(col);
            return;
        }
        if (columns.size() <= 1) {
            if (!PopTools.askSure(message("DeleteCol"), message("SureDeleteAll"))) {
                return;
            }
            deleteAllCols();
            return;
        }
        if (!checkBeforeNextAction()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            if (!PopTools.askSure(baseTitle, colName(col) + " - " + message("DeleteCol") + "\n"
                    + message("NoticeAllChangeUnrecover"))) {
                return;
            }
            task = new SingletonTask<Void>() {
                String[][] data;

                @Override
                protected boolean handle() {
                    columns.remove(col);
                    saveColumns();
                    File tmpFile = DeleteFileColDo(col);
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
            handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @Override
    protected void deleteAllCols() {
        if (sourceFile == null || pagesNumber <= 1) {
            super.deleteAllCols();
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
                    columns.clear();
                    saveColumns();
                    File tmpFile = deleteFileAllCols();
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
                    loadPage(1);
                }

            };
            handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @Override
    protected void orderFileCol(int col, boolean asc) {
        if (sourceFile == null || pagesNumber <= 1) {
            orderPageCol(col, asc);
            return;
        }
        if (columns == null || col < 0 || col >= columns.size()
                || !checkBeforeNextAction() || totalSize <= 0) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            if (!PopTools.askSure(baseTitle, colName(col) + " - "
                    + (asc ? message("Ascending") : message("Descending")) + "\n"
                    + message("DataFileOrderNotice"))) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    File tmpFile = orderFileColDo(col, asc);
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
            handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

}
