package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.table.BaseTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;

/**
 * @param <P> Data
 * @Author Mara
 * @CreateDate 2019-12-18
 * @License Apache License Version 2.0
 */
public abstract class BaseDataTableController<P> extends BaseController {

    protected BaseTable tableDefinition;
    protected ObservableList<P> tableData;
    protected int currentPage, pageSize, pagesNumber, totalSize, currentPageSize, currentPageStart;  // 1-based
    protected String tableName, idColumn;
    protected boolean paginate;

    @FXML
    protected TableView<P> tableView;
    @FXML
    protected Button editButton, examplesButton, refreshButton, resetButton,
            importButton, exportButton, chartsButton, queryButton;
    @FXML
    protected ComboBox<String> pageSizeSelector, pageSelector;
    @FXML
    protected Label pageLabel, dataSizeLabel, selectedLabel;
    @FXML
    protected HBox paginationBox;
    @FXML
    protected CheckBox deleteConfirmCheck;

    public BaseDataTableController() {
        tableName = "";
        TipsLabelKey = "TableTips";

    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            setTableDefinition();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    // define tableDefinition here
    public void setTableDefinition() {
    }

    protected void initColumns() {
        try {

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            if (tableDefinition != null) {
                tableName = tableDefinition.getTableName();
                idColumn = tableDefinition.getIdColumn();
            }
            if (tableView == null) {
                return;
            }
            tableData = FXCollections.observableArrayList();

            initColumns();

            tableData.addListener((ListChangeListener.Change<? extends P> change) -> {
                if (isSettingValues) {
                    return;
                }
                tableChanged(change);
            });

            tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            tableView.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends P> ov, P t, P t1) -> {
                        checkSelected();
                    });

            tableView.setOnMouseClicked((MouseEvent event) -> {
                if (popMenu != null && popMenu.isShowing()) {
                    popMenu.hide();
                }
                if (event.getButton() == MouseButton.SECONDARY) {
                    popTableMenu(event);
                } else if (event.getClickCount() == 1) {
                    itemClicked();
                } else if (event.getClickCount() > 1) {
                    itemDoubleClicked();
                }
            });

            tableView.setItems(tableData);

            if (deleteConfirmCheck != null) {
                deleteConfirmCheck.selectedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                            AppVariables.setUserConfigValue(baseName + "ConfirmDelete", deleteConfirmCheck.isSelected());
                        });
                deleteConfirmCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "ConfirmDelete", true));
            }

            initButtons();
            initPagination();

            checkSelected();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void tableChanged(ListChangeListener.Change<? extends P> change) {
//        while (change.next()) {
//            if (change.wasPermutated()) {
//                for (int i = change.getFrom(); i < change.getTo(); ++i) {
//
//                }
//            } else if (change.wasUpdated()) {
//
//            } else {
//                for (P remitem : change.getRemoved()) {
//
//                }
//                for (P additem : change.getAddedSubList()) {
//                }
//            }
//        }
//        checkSelected();
    }

    protected void checkSelected() {
        if (isSettingValues) {
            return;
        }
        int selection = tableView.getSelectionModel().getSelectedIndices().size();
        if (deleteButton != null) {
            deleteButton.setDisable(selection == 0);
        }
        if (viewButton != null) {
            viewButton.setDisable(selection == 0);
        }
        if (editButton != null) {
            editButton.setDisable(selection == 0);
        }
        if (selectedLabel != null) {
            selectedLabel.setText(message("Selected") + ": " + selection);
        }
    }

    public void itemClicked() {
    }

    public void itemDoubleClicked() {
        editAction(null);
    }

    protected void popTableMenu(MouseEvent event) {
        if (isSettingValues) {
            return;
        }
        List<MenuItem> items = makeTableContextMenu();
        if (items == null || items.isEmpty()) {
            return;
        }
        items.add(new SeparatorMenuItem());
        MenuItem menu = new MenuItem(message("PopupClose"));
        menu.setStyle("-fx-text-fill: #2e598a;");
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = null;
        });
        items.add(menu);
        if (popMenu != null && popMenu.isShowing()) {
            popMenu.hide();
        }
        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(items);
        popMenu.show(tableView, event.getScreenX(), event.getScreenY());

    }

    protected List<MenuItem> makeTableContextMenu() {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            List<MenuItem> group = new ArrayList<>();
            if (viewButton != null && viewButton.isVisible() && !viewButton.isDisabled()) {
                menu = new MenuItem(message("View"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    viewAction();
                });
                group.add(menu);
            }

            if (editButton != null && editButton.isVisible() && !editButton.isDisabled()) {
                menu = new MenuItem(message("Edit"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    editAction(null);
                });
                group.add(menu);
            }

            if (copyButton != null && copyButton.isVisible() && !copyButton.isDisabled()) {
                menu = new MenuItem(message("Copy"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    copyAction();
                });
                group.add(menu);
            }

            if (deleteButton != null && deleteButton.isVisible() && !deleteButton.isDisabled()) {
                menu = new MenuItem(message("Delete"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    deleteAction();
                });
                group.add(menu);
            }

            if (clearButton != null && clearButton.isVisible() && !clearButton.isDisabled()) {
                menu = new MenuItem(message("Clear"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    clearAction();
                });
                group.add(menu);
            }

            if (!group.isEmpty()) {
                items.addAll(group);
                items.add(new SeparatorMenuItem());
            }

            if (pageNextButton != null && pageNextButton.isVisible() && !pageNextButton.isDisabled()) {
                menu = new MenuItem(message("NextPage"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    pageNextAction();
                });
                items.add(menu);
            }

            if (pagePreviousButton != null && pagePreviousButton.isVisible() && !pagePreviousButton.isDisabled()) {
                menu = new MenuItem(message("PreviousPage"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    pagePreviousAction();
                });
                items.add(menu);
            }

            if (refreshButton != null && refreshButton.isVisible() && !refreshButton.isDisabled()) {
                menu = new MenuItem(message("Refresh"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    refreshAction();
                });
                items.add(menu);
            }

            return items;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    protected void initButtons() {
        try {

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initPagination() {
        try {
            if (pageSelector == null) {
                return;
            }
            paginate = true;
            currentPage = 1;
            pageSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        checkCurrentPage();
                    });

            pageSize = 50;
            pageSizeSelector.getItems().addAll(Arrays.asList("50", "30", "100", "20", "60", "200", "300",
                    "500", "1000", "2000", "5000", "10000", "20000", "50000"));
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
                                AppVariables.setUserConfigValue(baseName + "PageSize", pageSize + "");
                                pageSizeSelector.getEditor().setStyle(null);
                                if (!isSettingValues) {
                                    loadTableData();
                                }
                            }
                        } catch (Exception e) {
                            pageSizeSelector.getEditor().setStyle(badStyle);
                        }
                    });
            isSettingValues = true;
            pageSizeSelector.getSelectionModel().select(AppVariables.getUserConfigValue(baseName + "PageSize", "50"));
            pageSizeSelector.getEditor().setStyle(null);
            isSettingValues = false;

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
                currentPage = v;
                pageSelector.getEditor().setStyle(null);
                loadTableData();
                return true;
            }
        } catch (Exception e) {
            pageSelector.getEditor().setStyle(badStyle);
            return false;
        }
    }

    public boolean preLoadingTableData() {
        return true;
    }

    public void loadTableData() {
        if (!preLoadingTableData()) {
            return;
        }
        tableData.clear();
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                private List<P> data;

                @Override
                protected boolean handle() {
                    if (paginate) {
                        totalSize = readDataSize();
                        if (totalSize <= pageSize) {
                            pagesNumber = 1;
                        } else {
                            pagesNumber = totalSize % pageSize == 0 ? totalSize / pageSize : totalSize / pageSize + 1;
                        }
                        if (currentPage <= 0) {
                            currentPage = 1;
                        }
                        if (currentPage > pagesNumber) {
                            currentPage = pagesNumber;
                        }
                        currentPageStart = pageSize * (currentPage - 1) + 1;
                        int currentPageEnd = Math.min(currentPageStart + pageSize, totalSize + 1); // 1-based, excluded
                        currentPageSize = currentPageEnd - currentPageStart;
                        data = readPageData();
                    } else {
                        pagesNumber = 1;
                        currentPage = 1;
                        data = readData();
                        if (data != null) {
                            totalSize = data.size();
                        } else {
                            totalSize = 0;
                        }
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (data != null && !data.isEmpty()) {
                        isSettingValues = true;
                        tableData.setAll(data);
                        isSettingValues = false;
                    }
                    tableView.refresh();
                    checkSelected();
                    setPagination();
                    postLoadedTableData();
                }
            };
            if (parentController != null) {
                parentController.openHandlingStage(task, Modality.WINDOW_MODAL, message("LoadingTableData"));
            } else {
                openHandlingStage(task, Modality.WINDOW_MODAL, message("LoadingTableData"));
            }
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void postLoadedTableData() {
    }

    protected void setPagination() {
        try {
            if (paginationBox != null) {
                paginationBox.setVisible(paginate);
            }
            if (pageSelector == null) {
                return;
            }
            isSettingValues = true;
            if (paginate) {
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
                dataSizeLabel.setText(message("Data") + ": " + tableData.size() + "/" + totalSize);
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
            } else {
                pageSelector.getItems().clear();
                pageSelector.setDisable(true);
                pageLabel.setText("");
                dataSizeLabel.setText("");
                pagePreviousButton.setDisable(true);
                pageFirstButton.setDisable(true);
                pageNextButton.setDisable(true);
                pageLastButton.setDisable(true);
            }
            pageSelector.getEditor().setStyle(null);
            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }

    }

    public int readDataSize() {
        if (tableDefinition != null) {
            return tableDefinition.size();
        } else {
            return 0;
        }
    }

    public List<P> readPageData() {
        if (tableDefinition != null) {
            return tableDefinition.query(currentPageStart - 1, currentPageSize);
        } else {
            return null;
        }
    }

    public List<P> readData() {
        if (tableDefinition != null) {
            return tableDefinition.query();
        } else {
            return null;
        }
    }

    @FXML
    public void editAction(ActionEvent event) {
        try {

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    @FXML
    public void viewAction() {
        try {

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    @FXML
    @Override
    public void copyAction() {
        try {

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void examplesAction() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    loadExamples();
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    refreshAction();
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void loadExamples() {

    }

    @FXML
    @Override
    public void deleteAction() {
        List<P> selected = tableView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        if (deleteConfirmCheck != null && deleteConfirmCheck.isSelected()) {
            if (!FxmlControl.askSure(getBaseTitle(), message("SureDelete"))) {
                return;
            }
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                private int deletedCount = 0;

                @Override
                protected boolean handle() {
                    deletedCount = deleteSelectedData();
                    return deletedCount >= 0;
                }

                @Override
                protected void whenSucceeded() {
                    popInformation(message("Deleted") + ":" + deletedCount);
                    if (deletedCount > 0) {
                        afterDeletion();
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

    protected int deleteSelectedData() {
        List<P> selected = tableView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return 0;
        }
        return deleteData(selected);
    }

    protected int deleteData(List<P> data) {
        if (data == null || data.isEmpty()) {
            return 0;
        }
        if (tableDefinition != null) {
            return tableDefinition.deleteData(data);
        }
        return 0;
    }

    protected void afterDeletion() {
        refreshAction();
    }

    @FXML
    @Override
    public void clearAction() {
        if (tableData == null || tableData.isEmpty()) {
            return;
        }
        if (!FxmlControl.askSure(getBaseTitle(), message("SureClear"))) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                int deletedCount = 0;

                @Override
                protected boolean handle() {
                    deletedCount = clearData();
                    return deletedCount >= 0;
                }

                @Override
                protected void whenSucceeded() {
                    popInformation(message("Deleted") + ":" + deletedCount);
                    if (deletedCount > 0) {
                        clearView();
                        afterDeletion();
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

    protected int clearData() {
        if (tableDefinition != null) {
            return tableDefinition.clearData();
        } else {
            return 0;
        }
    }

    public void clearView() {
        isSettingValues = true;
        tableData.clear();
        isSettingValues = false;
        tableView.refresh();
        totalSize = 0;
        pagesNumber = 0;
        setPagination();
        checkSelected();
    }

    @FXML
    public void refreshAction() {
        loadTableData();
    }

    @FXML
    public void goPage() {
        checkCurrentPage();
    }

    @FXML
    @Override
    public void pageNextAction() {
        currentPage++;
        loadTableData();
    }

    @FXML
    @Override
    public void pagePreviousAction() {
        currentPage--;
        loadTableData();
    }

    @FXML
    @Override
    public void pageFirstAction() {
        currentPage = 1;
        loadTableData();
    }

    @FXML
    @Override
    public void pageLastAction() {
        currentPage = Integer.MAX_VALUE;
        loadTableData();
    }

    @FXML
    protected void importAction() {
        File file = FxmlControl.selectFile(this);
        if (file == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    importData(file);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    refreshAction();
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    protected void importData(File file) {
        DerbyBase.importData(tableName, file.getAbsolutePath(), false);
    }

    @FXML
    protected void exportAction() {
        final File file = chooseSaveFile(AppVariables.getUserConfigPath(targetPathKey),
                message(tableName) + ".txt", CommonFxValues.AllExtensionFilter);
        if (file == null) {
            return;
        }
        recordFileWritten(file);
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    DerbyBase.exportData(tableName, file.getAbsolutePath());
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    FxmlStage.openTextEditer(null, file);
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    protected void analyseAction() {

    }

}
