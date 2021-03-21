package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import mara.mybox.db.table.ColumnDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-12-25
 * @License Apache License Version 2.0
 */
public abstract class BaseDataFileController extends BaseSheetController {

    protected long totalSize, currentPageStart, currentPageEnd;   // 1-based
    protected int currentPage, pageSize, pagesNumber, copiedLines;// 1-based
    protected boolean sourceWithNames, totalRead;
    protected List<ColumnDefinition> savedColumns;
    protected String loadError;

    @FXML
    protected TitledPane filePane, saveAsPane, backupPane;
    @FXML
    protected ComboBox<String> pageSizeSelector, pageSelector;
    @FXML
    protected Label pageLabel, loadedLabel;
    @FXML
    protected HBox paginationBox;
    @FXML
    protected Button clearDefButton, recoverDefButton, okDefButton;
    @FXML
    protected VBox fileBox, fileOptionsBox;
    @FXML
    protected ControlFileBackup backupController;

    public BaseDataFileController() {
    }

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

    @Override
    public void initValues() {
        try {
            super.initValues();
            currentPageStart = 1;
            currentPage = 1;
            pageSize = 50;
            sourceWithNames = totalRead = false;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            initBackupsTab();
            initSaveAsTab();
            initPagination();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initBackupsTab() {
        try {
            if (backupPane == null) {
                return;
            }
            backupPane.setExpanded(AppVariables.getUserConfigBoolean(baseName + "BackupPane", false));
            backupPane.expandedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                        AppVariables.setUserConfigValue(baseName + "BackupPane", backupPane.isExpanded());
                    });

            backupController.setControls(this, baseName);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initSaveAsTab() {
        try {
            if (saveAsPane == null) {
                return;
            }
            saveAsPane.setExpanded(AppVariables.getUserConfigBoolean(baseName + "SaveAsPane", true));
            saveAsPane.expandedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                        AppVariables.setUserConfigValue(baseName + "SaveAsPane", saveAsPane.isExpanded());
                    });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initPagination() {
        try {
            if (pageSelector == null) {
                return;
            }
            pageSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        checkCurrentPage();
                    });

            pageSize = AppVariables.getUserConfigInt(baseName + "PageSize", 50);
            pageSize = pageSize < 1 ? 50 : pageSize;
            pageSizeSelector.getItems().addAll(Arrays.asList("50", "30", "100", "20", "60", "200", "300",
                    "500", "1000", "2000", "5000", "10000"));
            pageSizeSelector.setValue(pageSize + "");
            pageSizeSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        if (newValue == null) {
                            return;
                        }
                        try {
                            int v = Integer.parseInt(newValue.trim());
                            if (v <= 0) {
                                pageSizeSelector.getEditor().setStyle(badStyle);
                            } else {
                                pageSize = v;
                                AppVariables.setUserConfigInt(baseName + "PageSize", pageSize);
                                pageSizeSelector.getEditor().setStyle(null);
                                if (!isSettingValues) {
                                    loadPage(currentPage);
                                }
                            }
                        } catch (Exception e) {
                            pageSizeSelector.getEditor().setStyle(badStyle);
                        }
                    });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected boolean checkCurrentPage() {
        if (isSettingValues || pageSelector == null) {
            return false;
        }
        String value = pageSelector.getEditor().getText();
        try {
            int v = Integer.parseInt(value);
            if (v < 0) {
                pageSelector.getEditor().setStyle(badStyle);
                return false;
            } else {
                pageSelector.getEditor().setStyle(null);
                loadPage(v);
                return true;
            }
        } catch (Exception e) {
            pageSelector.getEditor().setStyle(badStyle);
            return false;
        }
    }

    @Override
    public void checkRightPaneHide() {
    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();

        setControls(baseName);
    }

    @Override
    protected boolean checkValid() {
        if (inputs == null) {
            saveButton.setDisable(false);
            return true;
        }
        return super.checkValid();
    }

    @FXML
    @Override
    public void createAction() {
        try {
            if (!checkBeforeNextAction()) {
                return;
            }
            sourceFile = null;
            initFile();
            sheetBox.getChildren().clear();
            makeSheet(new String[3][3], false);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void refreshAction() {
        if (sourceFile == null || !checkBeforeNextAction()) {
            return;
        }
        loadFile(true);
    }

    @Override
    public void sourceFileChanged(File file) {
        if (!checkBeforeNextAction()) {
            return;
        }
        sourceFile = file;
        initCurrentPage();
        loadFile(false);
    }

    public void initCurrentPage() {
        currentPage = 1;
        currentPageStart = 1;
        currentPageEnd = 1;
    }

    public void loadFile() {
        loadFile(false);
    }

    public void initFile() {
        dataName = null;
        columns = new ArrayList<>();
        totalSize = 0;
        pagesNumber = 1;
        sourceWithNames = true;
        totalRead = false;
        loadError = null;
        savedColumns = null;
        paginationBox.setVisible(false);
        if (sourceFile == null) {
            if (fileBox.getChildren().contains(fileOptionsBox)) {
                fileBox.getChildren().remove(fileOptionsBox);
            }
        } else {
            if (!fileBox.getChildren().contains(fileOptionsBox)) {
                fileBox.getChildren().add(1, fileOptionsBox);
            }
        }
        defBox.getChildren().clear();
        saveButton.setDisable(true);
        clearDefButton.setDisable(true);
        recoverDefButton.setDisable(true);
        okDefButton.setDisable(true);
        dataChanged(false);
        updateStatus();
        clearSheet();
        if (backupController != null) {
            backupController.loadBackups(sourceFile);
        }
    }

    public void loadFile(boolean pickOptions) {
        if (sourceFile == null || !checkBeforeNextAction()) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                task.cancel();
            }
            initFile();
            task = new SingletonTask<Void>() {

                private String[][] data;

                @Override
                protected boolean handle() {
                    dataName = sourceFile.getAbsolutePath();
                    data = null;
                    if (!readDataDefinition(pickOptions) || isCancelled()) {
                        return false;
                    }
                    if (!readColumns() || isCancelled()) {
                        return false;
                    }
                    if (columns == null || columns.isEmpty()) {
                        if (savedColumns != null) {
                            columns = savedColumns;
                        } else {
                            columns = new ArrayList<>();
                        }
                    }
                    data = readPageData();
                    if (isCancelled()) {
                        return false;
                    }
                    if (data != null) {
                        if (columns.size() < data[0].length) {
                            for (int col = columns.size() + 1; col <= data[0].length; col++) {
                                ColumnDefinition column = new ColumnDefinition(message(colPrefix) + col, ColumnDefinition.ColumnType.String);
                                columns.add(column);
                            }
                            tableDataColumn.save(dataDefinition.getDfid(), columns);
                        }
                    }
                    if (isCancelled()) {
                        return false;
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    makeSheet(data, false);
                    loadTotal();
                }

                @Override
                protected void whenFailed() {
                    if (isCancelled()) {
                        return;
                    }
                    if (error != null) {
                        popError(message(error));
                    } else if (loadError != null) {
                        popError(message(loadError));
                    } else {
                        popFailed();
                    }
                    sheetBox.getChildren().clear();
                }

                @Override
                protected void finalAction() {
                    updateStatus();
                    afterFileLoaded();
                }

            };
            task.setSelf(task);
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void loadTotal() {
        if (sourceFile == null) {
            return;
        }
        synchronized (this) {
            if (backgroundTask != null) {
                backgroundTask.cancel();
            }
            totalSize = 0;
            totalRead = false;
            paginationBox.setVisible(false);
            backgroundTask = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    if (!readTotal() || isCancelled()) {
                        return false;
                    }
                    countPagination((int) ((currentPageStart - 1) / pageSize) + 1);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    totalRead = true;
                    paginationBox.setVisible(true);
                    setPagination();
                    saveButton.setDisable(false);
                    clearDefButton.setDisable(false);
                    recoverDefButton.setDisable(false);
                    okDefButton.setDisable(false);
                }

                @Override
                protected void whenFailed() {
                    if (isCancelled()) {
                        return;
                    }
                    if (error != null) {
                        popError(message(error));
                    } else if (loadError != null) {
                        popError(message(loadError));
                    } else {
                        popFailed();
                    }
                }

                @Override
                protected void finalAction() {
                    updateStatus();
                }

            };
            backgroundTask.setSelf(backgroundTask);
            Thread thread = new Thread(backgroundTask);
            thread.setDaemon(true);
            thread.start();
        }

    }

    protected void afterFileLoaded() {
    }

    @Override
    protected String rowName(int row) {
        return message("Row") + (currentPageStart + row);
    }

    public void loadPage(int pageNumber) {
        if (sourceFile == null || columns.isEmpty() || totalSize <= 0) {
            makeSheet(null);
            return;
        }
        if (!checkBeforeNextAction()) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                task.cancel();
            }
            task = new SingletonTask<Void>() {
                String[][] data;

                @Override
                protected boolean handle() {
                    countPagination(pageNumber);
                    data = readPageData();
                    return !isCancelled() && error == null && loadError == null;
                }

                @Override
                protected void whenSucceeded() {
                    makeSheet(data, false);
                    setPagination();
                }

                @Override
                protected void whenFailed() {
                    if (isCancelled()) {
                        return;
                    }
                    if (error != null) {
                        popError(message(error));
                    } else if (loadError != null) {
                        popError(message(loadError));
                    } else {
                        popFailed();
                    }
                }

                @Override
                protected void finalAction() {
                    updateStatus();
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    protected String[][] readPageData() {
        return null;
    }

    protected void countPagination(int pageNumber) {
        if (totalSize <= pageSize) {
            pagesNumber = 1;
        } else {
            pagesNumber = (int) (totalSize % pageSize == 0 ? totalSize / pageSize : (totalSize / pageSize + 1));
        }
        currentPage = pageNumber;
        if (currentPage <= 0) {   // 1-based
            currentPage = 1;
        }
        if (currentPage > pagesNumber) {
            currentPage = pagesNumber;
        }
        currentPageStart = pageSize * (currentPage - 1) + 1; // 1-based
    }

    protected void setPagination() {
        try {
            if (pageSelector == null || (paginationBox != null && !paginationBox.isVisible())) {
                return;
            }
            isSettingValues = true;
            pageSelector.setDisable(false);
            List<String> pages = new ArrayList<>();
            for (int i = Math.max(1, currentPage - 20);
                    i <= Math.min(pagesNumber, currentPage + 20); i++) {
                pages.add(i + "");
            }
            pageSelector.getItems().clear();
            pageSelector.getItems().addAll(pages);
            pageSelector.getSelectionModel().select(currentPage + "");

            pageLabel.setText("/" + pagesNumber);
            if (currentPage > 1) {
                pagePreviousButton.setDisable(false);
                pageFirstButton.setDisable(false);
            } else {
                pagePreviousButton.setDisable(true);
                pageFirstButton.setDisable(true);
            }
            if (currentPage >= pagesNumber) {
                pageNextButton.setDisable(true);
                pageLastButton.setDisable(true);
            } else {
                pageNextButton.setDisable(false);
                pageLastButton.setDisable(false);
            }
            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }

    }

    protected void updateStatus() {
        if (sourceFile == null) {
            loadedLabel.setText("");
        } else {
            loadedLabel.setText(message("DataSize") + ": " + totalSize + "\n"
                    + message("Load") + ": " + DateTools.nowString());
        }
    }

    @FXML
    @Override
    public void clearDefAction() {
        tableDataColumn.clear(dataType, dataName);
        loadFile();
    }

    @FXML
    public void goPage() {
        checkCurrentPage();
    }

    @FXML
    @Override
    public void pageNextAction() {
        loadPage(currentPage + inputs.length / pageSize);
    }

    @FXML
    @Override
    public void pagePreviousAction() {
        loadPage(currentPage - 1);
    }

    @FXML
    @Override
    public void pageFirstAction() {
        loadPage(1);
    }

    @FXML
    @Override
    public void pageLastAction() {
        loadPage(Integer.MAX_VALUE);
    }

    @Override
    public List<MenuItem> colModifyMenu(int col) {
        List<MenuItem> items = new ArrayList<>();
        MenuItem menu;
        try {
            if (pagesNumber <= 1) {
                items.addAll(super.colModifyMenu(col));
            } else {
                menu = new MenuItem(message("SetPageColValues"));
                menu.setOnAction((ActionEvent event) -> {
                    SetPageColValues(col);
                });
                menu.setDisable(rowsCheck == null || rowsCheck.length == 0);
                items.add(menu);

                menu = new MenuItem(message("CopyPageCol"));
                menu.setOnAction((ActionEvent event) -> {
                    copyPageColValues(col);
                });
                menu.setDisable(rowsCheck == null || rowsCheck.length == 0);
                items.add(menu);

                menu = new MenuItem(message("PastePageCol"));
                menu.setOnAction((ActionEvent event) -> {
                    pastePageColValues(col);
                });
                menu.setDisable(copiedCol == null || copiedCol.isEmpty());
                items.add(menu);

                items.add(new SeparatorMenuItem());

                menu = new MenuItem(message("SetAllColValues"));
                menu.setOnAction((ActionEvent event) -> {
                    setAllColValuesTask(col);
                });
                menu.setDisable(dataChanged && sourceFile != null && pagesNumber > 1);
                items.add(menu);

                menu = new MenuItem(message("CopyAllCol"));
                menu.setOnAction((ActionEvent event) -> {
                    copyAllColValuesTask(col);
                });
                menu.setDisable(totalSize <= 0);
                items.add(menu);

                menu = new MenuItem(message("PasteAllCol"));
                menu.setOnAction((ActionEvent event) -> {
                    pasteAllColValuesTask(col);
                });
                menu.setDisable(copiedCol == null || copiedCol.isEmpty()
                        || (dataChanged && sourceFile != null && pagesNumber > 1));
                items.add(menu);

                items.add(new SeparatorMenuItem());

                menu = new MenuItem(message("InsertColLeft"));
                menu.setOnAction((ActionEvent event) -> {
                    insertFileColTask(col, true, 1);
                });
                menu.setDisable(dataChanged && sourceFile != null && pagesNumber > 1);
                items.add(menu);

                menu = new MenuItem(message("InsertColRight"));
                menu.setOnAction((ActionEvent event) -> {
                    insertFileColTask(col, false, 1);
                });
                menu.setDisable(dataChanged && sourceFile != null && pagesNumber > 1);
                items.add(menu);

                menu = new MenuItem(message("DeleteCol"));
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
            String value = askValue(colName(col) + "\n" + message("NoticeAllChangeUnrecover"),
                    message("SetAllColValues"), defaultColValue);
            if (!dataValid(col, value)) {
                popError(message("InvalidData"));
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    File tmpFile = setAllColValues(col, value);
                    if (tmpFile == null || !tmpFile.exists()) {
                        return false;
                    }
                    return FileTools.rename(tmpFile, sourceFile);
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    dataChanged = false;
                    loadPage(currentPage);
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
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
                    if (FxmlControl.copyToSystemClipboard(s.toString())) {
                        popInformation(message("CopiedInSheet"));
                    } else {
                        popFailed();
                    }
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
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
            popError(message("NoData"));
            return;
        }
        if (sourceFile == null || pagesNumber <= 1) {
            pastePageColValues(col);
            return;
        }
        if (!checkBeforeNextAction() || totalSize <= 0) {
            return;
        }
        if (!FxmlControl.askSure(baseTitle, colName(col) + " - " + message("PasteAllCol") + "\n"
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
                    File tmpFile = pasteAllColValues(col);
                    if (tmpFile == null || !tmpFile.exists()) {
                        return false;
                    }
                    return FileTools.rename(tmpFile, sourceFile);
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    dataChanged = false;
                    loadPage(currentPage);
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
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
                    return FileTools.rename(tmpFile, sourceFile);
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    dataChanged = false;
                    loadPage(currentPage);
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    protected void DeleteFileColTask(int col) {
        if (sourceFile == null || pagesNumber <= 1) {
            deletePageCol(col);
            return;
        }
        if (columns.size() <= 1) {
            if (!FxmlControl.askSure(message("DeleteCol"), message("SureDeleteAll"))) {
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
            if (!FxmlControl.askSure(baseTitle, colName(col) + " - " + message("DeleteCol") + "\n"
                    + message("NoticeAllChangeUnrecover"))) {
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
                    return FileTools.rename(tmpFile, sourceFile);
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    dataChanged = false;
                    loadPage(currentPage);
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
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
                    return FileTools.rename(tmpFile, sourceFile);
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    dataChanged = false;
                    loadPage(1);
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @Override
    protected void orderCol(int col, boolean asc) {
        if (sourceFile == null || pagesNumber <= 1) {
            super.orderCol(col, asc);
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
            if (!FxmlControl.askSure(baseTitle, colName(col) + " - "
                    + (asc ? message("Ascending") : message("Descending")) + "\n"
                    + message("DataFileOrderNotice"))) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    File tmpFile = orderFileCol(col, asc);
                    if (tmpFile == null || !tmpFile.exists()) {
                        return false;
                    }
                    return FileTools.rename(tmpFile, sourceFile);
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    dataChanged = false;
                    loadPage(currentPage);
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
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
            popError(message("NoData"));
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
                    if (FxmlControl.copyToSystemClipboard(s.toString())) {
                        popInformation(message("CopiedToSystemClipboard") + "\n"
                                + message("RowsNumber") + ":" + copiedLines + "\n"
                                + message("ColumnsNumber") + ":" + selectedCols);
                    } else {
                        popFailed();
                    }
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
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
            if (!FxmlControl.askSure(baseTitle, message("DeleteSelectedCols") + "\n"
                    + message("NoticeAllChangeUnrecover"))) {
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
                    return FileTools.rename(tmpFile, sourceFile);
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    dataChanged = false;
                    loadPage(currentPage);
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
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
            String value = askValue(message("NoticeAllChangeUnrecover"), message("SetAllSelectedColsValues"), defaultColValue);
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
                    return FileTools.rename(tmpFile, sourceFile);
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    loadPage(currentPage);
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
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
                    File tmpFile = insertFileCol(col, true, number);
                    if (tmpFile == null || !tmpFile.exists()) {
                        return false;
                    }
                    return FileTools.rename(tmpFile, sourceFile);
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    loadPage(currentPage);
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
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

                menu = new MenuItem(message("CopyPageAll"));
                menu.setOnAction((ActionEvent event) -> {
                    copyTextAction();
                });
                menu.setDisable(inputs == null);
                items.add(menu);

                items.add(new SeparatorMenuItem());

                menu = new MenuItem(message("CopySelectedRows"));
                menu.setOnAction((ActionEvent event) -> {
                    if (!rowsSelected) {
                        popError(message("NoData"));
                        return;
                    }
                    copySelectedRows();
                });
                menu.setDisable(!rowsSelected);
                items.add(menu);

                menu = new MenuItem(message("CopyPageSelectedCol"));
                menu.setOnAction((ActionEvent event) -> {
                    if (!colsSelected) {
                        popError(message("NoData"));
                        return;
                    }
                    copySelectedCols();
                });
                menu.setDisable(!colsSelected);
                items.add(menu);

                menu = new MenuItem(message("CopyAllSelectedCol"));
                menu.setOnAction((ActionEvent event) -> {
                    if (!colsSelected) {
                        popError(message("NoData"));
                        return;
                    }
                    copyAllSelectedColsTask();
                });
                menu.setDisable(!colsSelected);
                items.add(menu);

                menu = new MenuItem(message("CopySelectedRowsCols"));
                menu.setOnAction((ActionEvent event) -> {
                    if (!colsSelected || !rowsSelected) {
                        popError(message("NoData"));
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

                menu = new MenuItem(message("SetPageAll"));
                menu.setOnAction((ActionEvent event) -> {
                    setAllValues();
                });
                menu.setDisable(inputs == null);
                items.add(menu);

                menu = new MenuItem(message("SetSelectedRowsValues"));
                menu.setOnAction((ActionEvent event) -> {
                    if (!rowsSelected) {
                        popError(message("NoData"));
                        return;
                    }
                    setSelectedRows();
                });
                menu.setDisable(!rowsSelected);
                items.add(menu);

                menu = new MenuItem(message("SetPageSelectedColsValues"));
                menu.setOnAction((ActionEvent event) -> {
                    if (!colsSelected) {
                        popError(message("NoData"));
                        return;
                    }
                    setSelectedCols();
                });
                menu.setDisable(!colsSelected);
                items.add(menu);

                menu = new MenuItem(message("SetAllSelectedColsValues"));
                menu.setOnAction((ActionEvent event) -> {
                    if (!colsSelected) {
                        popError(message("NoData"));
                        return;
                    }
                    setAllSelectedColsTask();
                });
                menu.setDisable(!colsSelected
                        || (dataChanged && sourceFile != null && pagesNumber > 1));
                items.add(menu);

                menu = new MenuItem(message("SetSelectedRowsColsValues"));
                menu.setOnAction((ActionEvent event) -> {
                    if (!colsSelected || !rowsSelected) {
                        popError(message("NoData"));
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
