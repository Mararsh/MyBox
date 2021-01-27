package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import mara.mybox.db.table.ColumnDefinition;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.DateTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-12-25
 * @License Apache License Version 2.0
 */
public abstract class BaseDataFileController extends BaseSheetController {

    protected long totalSize, currentPageStart, currentPageEnd;   // 1-based
    protected int currentPage, pageSize, pagesNumber, currentPageSize;// 1-based
    protected boolean sourceWithNames, totalRead;
    protected List<ColumnDefinition> savedColumns;
    protected String loadError;

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

    public BaseDataFileController() {
        baseTitle = message("Field");
    }

    protected abstract boolean readDataDefinition(boolean pickOptions);

    protected abstract boolean readColumns();

    protected abstract boolean readTotal();

    protected abstract void setAllColValues(int col);

    protected abstract void copyAllColValues(int col);

    protected abstract void pasteAllColValues(int col);

    protected abstract void insertFileCol(int col, boolean left);

    protected abstract void DeleteFileCol(int col);

    protected abstract void copyAllSelectedCols();

    protected abstract void setAllSelectedCols();

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

            initPagination();

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
                        if (isSettingValues || newValue == null) {
                            return;
                        }
                        try {
                            int v = Integer.parseInt(newValue);
                            if (v < 0) {
                                pageSelector.getEditor().setStyle(badStyle);
                            } else {
                                pageSelector.getEditor().setStyle(null);
                                loadPage(v);
                            }
                        } catch (Exception e) {
                            pageSelector.getEditor().setStyle(badStyle);
                        }
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

    @Override
    public void checkRightPaneHide() {
    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();

        setControls(baseName);
        createAction();
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
        loadFile(false);
    }

    public void loadFile() {
        loadFile(false);
    }

    public void initFile() {
        dataName = null;
        columns = new ArrayList<>();
        totalSize = 0;
        currentPage = 1;
        currentPageStart = 1;
        currentPageEnd = pageSize;
        pagesNumber = 1;
        currentPageSize = pageSize;
        sourceWithNames = totalRead = false;
        loadError = null;
        savedColumns = null;
        paginationBox.setVisible(false);
        loadedLabel.setText("");
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
        clearDefButton.setDisable(true);
        recoverDefButton.setDisable(true);
        okDefButton.setDisable(true);
        dataChanged(false);
        updateStatus();
        clearSheet();
    }

    public void loadFile(boolean pickOptions) {
        if (sourceFile == null || !checkBeforeNextAction()) {
            return;
        }
        initFile();
        synchronized (this) {
            if (task != null) {
                task.cancel();
            }
            task = new SingletonTask<Void>() {

                private String[][] data;
                private boolean changed;

                @Override
                protected boolean handle() {
                    dataName = sourceFile.getAbsolutePath();
                    data = null;
                    if (!readDataDefinition(pickOptions)) {
                        error = loadError;
                        return false;
                    }
                    if (!readColumns()) {
                        error = loadError;
                        columns.clear();
                        data = new String[3][3];
                        changed = true;
                    } else {
                        data = readPageData();
                        if (data == null || data.length == 0) {
                            error = loadError;
                            data = new String[3][3];
                            columns.clear();
                            changed = true;
                        } else {
                            changed = false;
                        }
                    }
                    if (columns.size() < data[0].length) {
                        for (int col = columns.size() + 1; col <= data[0].length; col++) {
                            ColumnDefinition column = new ColumnDefinition(message("Field") + col, ColumnDefinition.ColumnType.String);
                            columns.add(column);
                        }
                        tableDataColumn.save(dataDefinition.getDfid(), columns);
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (error != null) {
                        popError(message(error));
                    }
                    makeSheet(data, changed);
                    loadTotal();
                }

                @Override
                protected void whenFailed() {
                    super.whenFailed();
                    sheetBox.getChildren().clear();
                }

                @Override
                protected void finalAction() {
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
            backgroundTask = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    totalSize = 0;
                    if (!readTotal()) {
                        error = loadError;
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
                    updateStatus();
                    clearDefButton.setDisable(false);
                    recoverDefButton.setDisable(false);
                    okDefButton.setDisable(false);
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
        if (sourceFile == null || columns == null || totalSize <= 0
                || !checkBeforeNextAction()) {
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
                    return data != null;
                }

                @Override
                protected void whenSucceeded() {
                    makeSheet(data, false);
                    setPagination();
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
            pagesNumber = (int) (totalSize % pageSize == 0 ? totalSize / pageSize : totalSize / pageSize + 1);
        }
        currentPage = pageNumber;
        if (currentPage <= 0) {
            currentPage = 1;
        }
        if (currentPage > pagesNumber) {
            currentPage = pagesNumber;
        }
        currentPageStart = pageSize * (currentPage - 1) + 1;// 1-based
        currentPageEnd = Math.min(currentPageStart + pageSize, totalSize + 1);  // 1-based, excluded
        currentPageSize = (int) (currentPageEnd - currentPageStart);
    }

    protected void setPagination() {
        try {
            if (pageSelector == null || inputs == null
                    || (paginationBox != null && !paginationBox.isVisible())) {
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
                items.add(menu);

                menu = new MenuItem(message("CopyPageCol"));
                menu.setOnAction((ActionEvent event) -> {
                    copyPageColValues(col);
                });
                items.add(menu);

                if (copiedCol != null && !copiedCol.isEmpty()) {
                    menu = new MenuItem(message("PastePageCol"));
                    menu.setOnAction((ActionEvent event) -> {
                        pastePageColValues(col);
                    });
                    items.add(menu);
                }

                items.add(new SeparatorMenuItem());

                menu = new MenuItem(message("SetAllColValues"));
                menu.setOnAction((ActionEvent event) -> {
                    setAllColValues(col);
                });
                items.add(menu);

                menu = new MenuItem(message("CopyAllCol"));
                menu.setOnAction((ActionEvent event) -> {
                    copyAllColValues(col);
                });
                items.add(menu);

                if (copiedCol != null && !copiedCol.isEmpty()) {
                    menu = new MenuItem(message("PasteAllCol"));
                    menu.setOnAction((ActionEvent event) -> {
                        pasteAllColValues(col);
                    });
                    items.add(menu);
                }

                items.add(new SeparatorMenuItem());

                menu = new MenuItem(message("InsertColLeft"));
                menu.setOnAction((ActionEvent event) -> {
                    insertFileCol(col, true);
                });
                items.add(menu);

                menu = new MenuItem(message("InsertColRight"));
                menu.setOnAction((ActionEvent event) -> {
                    insertFileCol(col, false);
                });
                items.add(menu);

                if (inputs[0].length > 1) {
                    menu = new MenuItem(message("DeleteCol"));
                    menu.setOnAction((ActionEvent event) -> {
                        DeleteFileCol(col);
                    });
                    items.add(menu);
                }

            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return items;
    }

    @Override
    public List<MenuItem> makeSheetCopyMenu() {
        List<MenuItem> items = new ArrayList<>();
        try {
            if (pagesNumber <= 1) {
                items.addAll(super.makeSheetCopyMenu());
            } else {
                MenuItem menu = new MenuItem(message("CopyPageAll"));
                menu.setOnAction((ActionEvent event) -> {
                    copyTextAction();
                });
                items.add(menu);
                items.add(new SeparatorMenuItem());

                rowsSelected = false;
                for (int j = 0; j < rowsCheck.length; ++j) {
                    if (rowsCheck[j].isSelected()) {
                        rowsSelected = true;
                        break;
                    }
                }
                colsSelected = false;
                for (int j = 0; j < colsCheck.length; ++j) {
                    if (colsCheck[j].isSelected()) {
                        colsSelected = true;
                        break;
                    }
                }
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
                    copyAllSelectedCols();
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
    public List<MenuItem> sheetSizeMoreMenu() {
        return null;
    }

    @Override
    public List<MenuItem> makeSheetEqualMenu() {
        List<MenuItem> items = new ArrayList<>();
        try {
            if (pagesNumber <= 1) {
                items.addAll(super.makeSheetEqualMenu());
            } else {
                MenuItem menu = new MenuItem(message("SetPageAll"));
                menu.setOnAction((ActionEvent event) -> {
                    setAllValues();
                });
                items.add(menu);

                rowsSelected = false;
                for (int j = 0; j < rowsCheck.length; ++j) {
                    if (rowsCheck[j].isSelected()) {
                        rowsSelected = true;
                        break;
                    }
                }
                colsSelected = false;
                for (int j = 0; j < colsCheck.length; ++j) {
                    if (colsCheck[j].isSelected()) {
                        colsSelected = true;
                        break;
                    }
                }

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
                    setAllSelectedCols();
                });
                menu.setDisable(!colsSelected);
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
