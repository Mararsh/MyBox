package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mara.mybox.db.table.ColumnDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-8-19
 * @License Apache License Version 2.0
 */
public abstract class BaseDataFileController_Sheet extends BaseSheetController {

    protected long totalSize, currentPageStart, currentPageEnd;   // 1-based
    protected int currentPage, pageSize, pagesNumber, copiedLines;// 1-based
    protected boolean sourceWithNames, totalRead;
    protected List<ColumnDefinition> savedColumns;
    protected String loadError;

    @FXML
    protected TitledPane filePane, saveAsPane, backupPane, formatPane;
    @FXML
    protected ComboBox<String> pageSizeSelector, pageSelector;
    @FXML
    protected Label pageLabel, loadedLabel;
    @FXML
    protected HBox paginationBox;
    @FXML
    protected VBox fileBox, formatBox;
    @FXML
    protected ControlFileBackup backupController;

    public abstract void loadPage(int pageNumber);

    protected abstract boolean readDataDefinition(boolean pickOptions);

    protected abstract boolean readColumns();

    protected abstract boolean readTotal();

    protected abstract boolean saveColumns();

    protected abstract File setAllColValues(int col, String value);

    protected abstract StringBuilder copyAllColValues(int col);

    protected abstract File pasteAllColValues(int col);

    protected abstract File insertFileCol(int col, boolean left, int number);

    protected abstract File DeleteFileCol(int col);

    protected abstract File deleteFileAllCols();

    protected abstract File deleteFileSelectedCols();

    protected abstract File orderFileCol(int col, boolean asc);

    protected abstract StringBuilder copyAllSelectedCols();

    protected abstract File setAllSelectedCols(String value);

    public void backup() {
        if (backupController != null && backupController.isBack()) {
            backupController.addBackup(sourceFile);
        }
    }

    @Override
    public List<MenuItem> colModifyMenu(int col) {
        List<MenuItem> items = new ArrayList<>();
        MenuItem menu;
        try {
            if (pagesNumber <= 1) {
                items.addAll(super.colModifyMenu(col));
            } else {
                menu = new MenuItem(Languages.message("SetPageColValues"));
                menu.setOnAction((ActionEvent event) -> {
                    SetPageColValues(col);
                });
                menu.setDisable(rowsCheck == null || rowsCheck.length == 0);
                items.add(menu);

                menu = new MenuItem(Languages.message("CopyPageCol"));
                menu.setOnAction((ActionEvent event) -> {
                    copyPageColValues(col);
                });
                menu.setDisable(rowsCheck == null || rowsCheck.length == 0);
                items.add(menu);

                menu = new MenuItem(Languages.message("PastePageCol"));
                menu.setOnAction((ActionEvent event) -> {
                    pastePageColValues(col);
                });
                menu.setDisable(copiedCol == null || copiedCol.isEmpty());
                items.add(menu);

                items.add(new SeparatorMenuItem());

                menu = new MenuItem(Languages.message("SetAllColValues"));
                menu.setOnAction((ActionEvent event) -> {
                    setAllColValuesTask(col);
                });
                menu.setDisable(dataChanged && sourceFile != null && pagesNumber > 1);
                items.add(menu);

                menu = new MenuItem(Languages.message("CopyAllCol"));
                menu.setOnAction((ActionEvent event) -> {
                    copyAllColValuesTask(col);
                });
                menu.setDisable(totalSize <= 0);
                items.add(menu);

                menu = new MenuItem(Languages.message("PasteAllCol"));
                menu.setOnAction((ActionEvent event) -> {
                    pasteAllColValuesTask(col);
                });
                menu.setDisable(copiedCol == null || copiedCol.isEmpty()
                        || (dataChanged && sourceFile != null && pagesNumber > 1));
                items.add(menu);

                items.add(new SeparatorMenuItem());

                menu = new MenuItem(Languages.message("InsertColLeft"));
                menu.setOnAction((ActionEvent event) -> {
                    insertFileColTask(col, true, 1);
                });
                menu.setDisable(dataChanged && sourceFile != null && pagesNumber > 1);
                items.add(menu);

                menu = new MenuItem(Languages.message("InsertColRight"));
                menu.setOnAction((ActionEvent event) -> {
                    insertFileColTask(col, false, 1);
                });
                menu.setDisable(dataChanged && sourceFile != null && pagesNumber > 1);
                items.add(menu);

                menu = new MenuItem(Languages.message("DeleteCol"));
                menu.setOnAction((ActionEvent event) -> {
                    DeleteFileColTask(col);
                });
                menu.setDisable(dataChanged && sourceFile != null && pagesNumber > 1);
                items.add(menu);

            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return items;
    }

    protected void setAllColValuesTask(int col) {
        if (sourceFile == null || pagesNumber <= 1) {
            SetPageColValues(col);
        }
        if (!checkBeforeNextAction() || totalSize <= 0) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            String value = askValue(colName(col) + "\n" + Languages.message("NoticeAllChangeUnrecover"),
                    Languages.message("SetAllColValues"), defaultColValue);
            if (!dataValid(col, value)) {
                popError(Languages.message("InvalidData"));
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    File tmpFile = setAllColValues(col, value);
                    if (tmpFile == null || !tmpFile.exists()) {
                        return false;
                    }
                    backup();
                    return FileTools.rename(tmpFile, sourceFile);
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    dataChanged = false;
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

    protected void copyAllColValuesTask(int col) {
        if (sourceFile == null || pagesNumber <= 1) {
            copyPageColValues(col);
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                StringBuilder s;

                @Override
                protected boolean handle() {
                    copiedCol = new ArrayList<>();
                    s = copyAllColValues(col);
                    return s != null;
                }

                @Override
                protected void whenSucceeded() {
                    TextClipboardTools.copyToSystemClipboard(myController, s.toString());
                }

            };
            handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    protected void copyPageCol(int col) {
        if (inputs == null) {
            return;
        }
        for (TextField[] input : inputs) {
            String v = input[col].getText();
            copiedCol.add(v == null ? "" : v);
        }
    }

    protected void pasteAllColValuesTask(int col) {
        if (copiedCol == null || copiedCol.isEmpty()) {
            popError(Languages.message("NoData"));
            return;
        }
        if (sourceFile == null || pagesNumber <= 1) {
            pastePageColValues(col);
            return;
        }
        if (!checkBeforeNextAction() || totalSize <= 0) {
            return;
        }
        if (!PopTools.askSure(baseTitle, colName(col) + " - " + Languages.message("PasteAllCol") + "\n"
                + Languages.message("NoticeAllChangeUnrecover"))) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    File tmpFile = pasteAllColValues(col);
                    if (tmpFile == null || !tmpFile.exists()) {
                        return false;
                    }
                    backup();
                    return FileTools.rename(tmpFile, sourceFile);
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    dataChanged = false;
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

    protected void insertFileColTask(int col, boolean left, int number) {
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
                    File tmpFile = insertFileCol(col, left, number);
                    if (tmpFile == null || !tmpFile.exists()) {
                        return false;
                    }
                    backup();
                    return FileTools.rename(tmpFile, sourceFile);
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    dataChanged = false;
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

    protected void DeleteFileColTask(int col) {
        if (sourceFile == null || pagesNumber <= 1) {
            deletePageCol(col);
            return;
        }
        if (columns.size() <= 1) {
            if (!PopTools.askSure(Languages.message("DeleteCol"), Languages.message("SureDeleteAll"))) {
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
            if (!PopTools.askSure(baseTitle, colName(col) + " - " + Languages.message("DeleteCol") + "\n"
                    + Languages.message("NoticeAllChangeUnrecover"))) {
                return;
            }
            task = new SingletonTask<Void>() {
                String[][] data;

                @Override
                protected boolean handle() {
                    columns.remove(col);
                    saveColumns();
                    File tmpFile = DeleteFileCol(col);
                    if (tmpFile == null || !tmpFile.exists()) {
                        return false;
                    }
                    backup();
                    return FileTools.rename(tmpFile, sourceFile);
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    dataChanged = false;
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
                    dataChanged = false;
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
    public List<MenuItem> colOrderMenu(int col) {
        try {
            List<MenuItem> items = super.colOrderMenu(col);

            MenuItem menu = new MenuItem(Languages.message("OrderFileRowsAscByThisCol"));
            menu.setOnAction((ActionEvent event) -> {
                orderFileRows(col, true);
            });
            menu.setDisable(rowsCheck == null || rowsCheck.length == 0);
            items.add(menu);

            menu = new MenuItem(Languages.message("OrderFileRowsDescByThisCol"));
            menu.setOnAction((ActionEvent event) -> {
                orderFileRows(col, false);
            });
            menu.setDisable(rowsCheck == null || rowsCheck.length == 0);
            items.add(menu);
            return items;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    protected void orderFileRows(int col, boolean asc) {
        if (sourceFile == null || pagesNumber <= 1) {
            super.orderPageRows(col, asc);
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
                    + (asc ? Languages.message("Ascending") : Languages.message("Descending")) + "\n"
                    + Languages.message("DataFileOrderNotice"))) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    File tmpFile = orderFileCol(col, asc);
                    if (tmpFile == null || !tmpFile.exists()) {
                        return false;
                    }
                    backup();
                    return FileTools.rename(tmpFile, sourceFile);
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    dataChanged = false;
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

    protected void copyAllSelectedColsTask() {
        if (colsCheck == null) {
            return;
        }
        if (sourceFile == null || pagesNumber <= 1) {
            copySelectedCols();
            return;
        }
        int cols = 0;
        for (CheckBox c : colsCheck) {
            if (c.isSelected()) {
                cols++;
            }
        }
        if (cols < 1) {
            popError(Languages.message("NoData"));
            return;
        }
        int selectedCols = cols;
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                private StringBuilder s;

                @Override
                protected boolean handle() {
                    copiedLines = 0;
                    s = copyAllSelectedCols();
                    return s != null;
                }

                @Override
                protected void whenSucceeded() {
                    TextClipboardTools.copyToSystemClipboard(myController, s.toString());
                }

            };
            handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    protected StringBuilder copyPageCols(StringBuilder s, String delimiterString) {
        if (inputs == null) {
            return null;
        }
        StringBuilder ps = s;
        for (TextField[] rowInputs : inputs) {
            String rowString = null;
            for (int i = 0; i < colsCheck.length; i++) {
                if (!colsCheck[i].isSelected()) {
                    continue;
                }
                String cellString = rowInputs[i].getText();
                cellString = cellString == null ? "" : cellString;
                if (rowString == null) {
                    rowString = cellString;
                } else {
                    rowString += delimiterString + cellString;
                }
            }
            rowString = rowString == null ? "" : rowString;
            if (ps == null) {
                ps = new StringBuilder();
                ps.append(rowString);
            } else {
                ps.append("\n").append(rowString);
            }
            copiedLines++;
        }
        return ps;
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
            if (!PopTools.askSure(baseTitle, Languages.message("DeleteSelectedCols") + "\n"
                    + Languages.message("NoticeAllChangeUnrecover"))) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    saveColumns();
                    File tmpFile = deleteFileSelectedCols();
                    if (tmpFile == null || !tmpFile.exists()) {
                        return false;
                    }
                    backup();
                    return FileTools.rename(tmpFile, sourceFile);
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    dataChanged = false;
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

    protected void setAllSelectedColsTask() {
        if (sourceFile == null || pagesNumber <= 1) {
            setSelectedCols();
            return;
        }
        if (!checkBeforeNextAction() || totalSize <= 0) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            String value = askValue(Languages.message("NoticeAllChangeUnrecover"), Languages.message("SetAllSelectedColsValues"), defaultColValue);
            if (value == null) {
                return;
            }
            task = new SingletonTask<Void>() {
                StringBuilder s;

                @Override
                protected boolean handle() {
                    File tmpFile = setAllSelectedCols(value);
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
            String value = askValue(Languages.message("NoticeAllChangeUnrecover"), Languages.message("AddColsNumber"), "1");
            int number;
            try {
                number = Integer.parseInt(value);
            } catch (Exception e) {
                popError(Languages.message("InvalidData"));
                return;
            }
            task = new SingletonTask<Void>() {
                StringBuilder s;

                @Override
                protected boolean handle() {
                    int col = colsCheck == null ? 0 : colsCheck.length;
                    makeColumns(col, number);
                    saveColumns();
                    File tmpFile = insertFileCol(col, true, number);
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
    public List<MenuItem> makeSheetCopyMenu() {
        List<MenuItem> items = new ArrayList<>();
        try {
            if (pagesNumber <= 1) {
                items.addAll(super.makeSheetCopyMenu());
            } else {
                MenuItem menu;

                rowsSelected = false;
                if (rowsCheck != null) {
                    for (int j = 0; j < rowsCheck.length; ++j) {
                        if (rowsCheck[j].isSelected()) {
                            rowsSelected = true;
                            break;
                        }
                    }
                }
                colsSelected = false;
                if (colsCheck != null) {
                    for (int j = 0; j < colsCheck.length; ++j) {
                        if (colsCheck[j].isSelected()) {
                            colsSelected = true;
                            break;
                        }
                    }
                }

                menu = new MenuItem(Languages.message("CopyPageAll"));
                menu.setOnAction((ActionEvent event) -> {
                    copyText();
                });
                menu.setDisable(inputs == null);
                items.add(menu);

                items.add(new SeparatorMenuItem());

                menu = new MenuItem(Languages.message("CopySelectedRows"));
                menu.setOnAction((ActionEvent event) -> {
                    if (!rowsSelected) {
                        popError(Languages.message("NoData"));
                        return;
                    }
                    copySelectedRows();
                });
                menu.setDisable(!rowsSelected);
                items.add(menu);

                menu = new MenuItem(Languages.message("CopyPageSelectedCol"));
                menu.setOnAction((ActionEvent event) -> {
                    if (!colsSelected) {
                        popError(Languages.message("NoData"));
                        return;
                    }
                    copySelectedCols();
                });
                menu.setDisable(!colsSelected);
                items.add(menu);

                menu = new MenuItem(Languages.message("CopyAllSelectedCol"));
                menu.setOnAction((ActionEvent event) -> {
                    if (!colsSelected) {
                        popError(Languages.message("NoData"));
                        return;
                    }
                    copyAllSelectedColsTask();
                });
                menu.setDisable(!colsSelected);
                items.add(menu);

                menu = new MenuItem(Languages.message("CopySelectedRowsCols"));
                menu.setOnAction((ActionEvent event) -> {
                    if (!colsSelected || !rowsSelected) {
                        popError(Languages.message("NoData"));
                        return;
                    }
                    copySelectedRowsCols();
                });
                menu.setDisable(!colsSelected || !rowsSelected);
                items.add(menu);

            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return items;
    }

    @Override
    public List<MenuItem> makeSheetEqualMenu() {
        List<MenuItem> items = new ArrayList<>();
        try {
            if (pagesNumber <= 1) {
                items.addAll(super.makeSheetEqualMenu());
            } else {
                MenuItem menu;

                rowsSelected = false;
                if (rowsCheck != null) {
                    for (int j = 0; j < rowsCheck.length; ++j) {
                        if (rowsCheck[j].isSelected()) {
                            rowsSelected = true;
                            break;
                        }
                    }
                }
                colsSelected = false;
                if (colsCheck != null) {
                    for (int j = 0; j < colsCheck.length; ++j) {
                        if (colsCheck[j].isSelected()) {
                            colsSelected = true;
                            break;
                        }
                    }
                }

                menu = new MenuItem(Languages.message("SetPageAll"));
                menu.setOnAction((ActionEvent event) -> {
                    setAllValues();
                });
                menu.setDisable(inputs == null);
                items.add(menu);

                menu = new MenuItem(Languages.message("SetSelectedRowsValues"));
                menu.setOnAction((ActionEvent event) -> {
                    if (!rowsSelected) {
                        popError(Languages.message("NoData"));
                        return;
                    }
                    setSelectedRows();
                });
                menu.setDisable(!rowsSelected);
                items.add(menu);

                menu = new MenuItem(Languages.message("SetPageSelectedColsValues"));
                menu.setOnAction((ActionEvent event) -> {
                    if (!colsSelected) {
                        popError(Languages.message("NoData"));
                        return;
                    }
                    setSelectedCols();
                });
                menu.setDisable(!colsSelected);
                items.add(menu);

                menu = new MenuItem(Languages.message("SetAllSelectedColsValues"));
                menu.setOnAction((ActionEvent event) -> {
                    if (!colsSelected) {
                        popError(Languages.message("NoData"));
                        return;
                    }
                    setAllSelectedColsTask();
                });
                menu.setDisable(!colsSelected
                        || (dataChanged && sourceFile != null && pagesNumber > 1));
                items.add(menu);

                menu = new MenuItem(Languages.message("SetSelectedRowsColsValues"));
                menu.setOnAction((ActionEvent event) -> {
                    if (!colsSelected || !rowsSelected) {
                        popError(Languages.message("NoData"));
                        return;
                    }
                    setSelectedRowsCols();
                });
                menu.setDisable(!colsSelected || !rowsSelected);
                items.add(menu);

            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return items;
    }

}
