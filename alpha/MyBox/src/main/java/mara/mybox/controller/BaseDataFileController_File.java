package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import mara.mybox.db.table.ColumnDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.tools.DateTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-22
 * @License Apache License Version 2.0
 */
public abstract class BaseDataFileController_File extends BaseDataFileController_Size {

    protected abstract boolean readDataDefinition(boolean pickOptions);

    protected abstract boolean readColumns();

    protected abstract boolean readTotal();

    @Override
    protected long rowsTotal() {
        return totalSize;
    }

    @Override
    protected String rowName(int row) {
        return message("Row") + (currentPageStart + row);
    }

    protected void initBackupsTab() {
        try {
            if (backupPane == null) {
                return;
            }
            backupPane.setExpanded(UserConfig.getBoolean(baseName + "BackupPane", false));
            backupPane.expandedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                UserConfig.setBoolean(baseName + "BackupPane", backupPane.isExpanded());
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
            saveAsPane.setExpanded(UserConfig.getBoolean(baseName + "SaveAsPane", true));
            saveAsPane.expandedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                UserConfig.setBoolean(baseName + "SaveAsPane", saveAsPane.isExpanded());
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

            pageSize = UserConfig.getInt(baseName + "PageSize", 50);
            pageSize = pageSize < 1 ? 50 : pageSize;
            pageSizeSelector.getItems().addAll(Arrays.asList("50", "30", "100", "20", "60", "200", "300",
                    "500", "1000", "2000", "5000", "10000"));
            pageSizeSelector.setValue(pageSize + "");
            pageSizeSelector.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                if (newValue == null) {
                    return;
                }
                try {
                    int v = Integer.parseInt(newValue.trim());
                    if (v <= 0) {
                        pageSizeSelector.getEditor().setStyle(NodeStyleTools.badStyle);
                    } else {
                        pageSize = v;
                        UserConfig.setInt(baseName + "PageSize", pageSize);
                        pageSizeSelector.getEditor().setStyle(null);
                        if (!isSettingValues) {
                            loadPage(currentPage);
                        }
                    }
                } catch (Exception e) {
                    pageSizeSelector.getEditor().setStyle(NodeStyleTools.badStyle);
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void createAction() {
        try {
            if (!checkBeforeNextAction()) {
                return;
            }
            TableSizeController controller = (TableSizeController) openChildStage(Fxmls.TableSizeFxml, true);
            controller.setParameters(this);
            controller.notify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    newSheet(controller.rowsNumber, controller.colsNumber);
                    controller.closeStage();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void newSheet(int rows, int cols) {
        try {
            sourceFile = null;
            initFile();
            sheetBox.getChildren().clear();
            makeSheet(new String[rows][cols], false);
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
        formatBox.setDisable(sourceFile == null);
        sheetDisplayController.sourceFile = sourceFile;
        updateLabel();
        clearSheet();
        dataChanged(false);
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
                    updateLabel();
                    afterFileLoaded();
                }

            };
            task.setSelf(task);
            handling(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
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
                    updateLabel();
                }

            };
            backgroundTask.setSelf(backgroundTask);
            Thread thread = new Thread(backgroundTask);
            thread.setDaemon(false);
            thread.start();
        }

    }

    protected void afterFileLoaded() {
    }

    @Override
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
                    updateLabel();
                }

            };
            handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
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

    protected void updateLabel() {
        if (sourceFile == null) {
            loadedLabel.setText("");
        } else {
            loadedLabel.setText(message("DataSize") + ": " + totalSize + "\n"
                    + message("Load") + ": " + DateTools.nowString());
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
                pageSelector.getEditor().setStyle(NodeStyleTools.badStyle);
                return false;
            } else {
                pageSelector.getEditor().setStyle(null);
                loadPage(v);
                return true;
            }
        } catch (Exception e) {
            pageSelector.getEditor().setStyle(NodeStyleTools.badStyle);
            return false;
        }
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

}
