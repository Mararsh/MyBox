package mara.mybox.controller;

import java.util.ArrayList;
import javafx.beans.property.SimpleBooleanProperty;
import mara.mybox.db.data.ColumnDefinition;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-8-28
 * @License Apache License Version 2.0
 */
public abstract class ControlSheetFile_File extends ControlSheet {

    protected SimpleBooleanProperty fileLoadedNotify;
    protected boolean userSavedDataDefinition;

    protected abstract boolean readDataDefinition();

    protected abstract boolean readColumns();

    protected abstract boolean readTotal();

    protected abstract void saveFile();

    protected abstract void saveAs();

    public void initFile() {
        dataName = null;
        columns = new ArrayList<>();
        totalSize = 0;
        pagesNumber = 1;
        totalRead = false;
        savedColumns = null;
        paginationBox.setVisible(false);
        clearSheet();
        updateLabel();
        if (backupController != null) {
            backupController.loadBackups(sourceFile);
        }
        BaseDataOperationController.closeAll(this);
    }

    public void loadFile() {
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
                    if (!readDataDefinition() || isCancelled()) {
                        return false;
                    }
                    if (!readColumns() || isCancelled()) {
                        return false;
                    }
                    if (columns == null || columns.isEmpty()) {
                        columns = new ArrayList<>();
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
                    } else {
                        popFailed();
                    }
                    sheetBox.getChildren().clear();
                }

                @Override
                protected void finalAction() {
                    updateLabel();
                    fileLoadedNotify.set(!fileLoadedNotify.get());
                }

            };
            start(task);
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
                    countPagination((int) (startRowOfCurrentPage / pageSize));
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    totalRead = true;
                    paginationBox.setVisible(true);
                    setPagination();
                }

                @Override
                protected void whenFailed() {
                    if (isCancelled()) {
                        return;
                    }
                    if (error != null) {
                        popError(message(error));
                    } else {
                        popFailed();
                    }
                }

                @Override
                protected void finalAction() {
                    updateLabel();
                    fileLoadedNotify.set(!fileLoadedNotify.get());
                }

            };
            start(backgroundTask, false);
        }

    }

    public void backup() {
        if (backupController != null && backupController.isBack()) {
            backupController.addBackup(sourceFile);
        }
    }

    public void recover() {
        dataChangedNotify.set(false);
        loadFile();
    }

}
