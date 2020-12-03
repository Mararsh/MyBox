package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.TableBase;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;

/**
 * @Author Mara
 * @CreateDate 2019-12-18
 * @License Apache License Version 2.0
 */
public class TableManageController<P> extends BaseController {

    protected TableBase tableDefinition;
    protected ObservableList<P> tableData;
    protected int currentPage, pageSize, pagesNumber, totalSize, currentPageStart, currentPageSize;
    protected String tableName, idColumn;
    protected boolean paginate;

    @FXML
    protected TableView<P> tableView;
    @FXML
    protected Button editButton, examplesButton, refreshButton, resetButton,
            dataImportButton, dataExportButton, chartsButton, queryButton;
    @FXML
    protected ComboBox<String> pageSizeSelector, pageSelector;
    @FXML
    protected Label pageLabel, dataSizeLabel, selectedLabel;
    @FXML
    protected HBox paginationBox;
    @FXML
    protected CheckBox deleteConfirmCheck;

    public TableManageController() {
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
            checkSelected();

            tableView.setOnMouseClicked((MouseEvent event) -> {
                if (event.getClickCount() == 1) {
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
        checkSelected();
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
                        if (isSettingValues || newValue == null) {
                            return;
                        }
                        try {
                            int v = Integer.parseInt(newValue);
                            if (v < 0) {
                                pageSelector.getEditor().setStyle(badStyle);
                            } else {
                                currentPage = v;
                                pageSelector.getEditor().setStyle(null);
                                loadTableData();
                            }
                        } catch (Exception e) {
                            pageSelector.getEditor().setStyle(badStyle);
                        }
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
                        currentPageStart = pageSize * (currentPage - 1);
                        int currentPageEnd = Math.min(currentPageStart + pageSize - 1, totalSize);
                        currentPageSize = currentPageEnd - currentPageStart + 1;
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
                    previousButton.setDisable(false);
                    firstButton.setDisable(false);
                } else {
                    previousButton.setDisable(true);
                    firstButton.setDisable(true);
                }
                if (currentPage >= pagesNumber) {
                    nextButton.setDisable(true);
                    lastButton.setDisable(true);
                } else {
                    nextButton.setDisable(false);
                    lastButton.setDisable(false);
                }
            } else {
                pageSelector.getItems().clear();
                pageSelector.setDisable(true);
                pageLabel.setText("");
                dataSizeLabel.setText("");
                previousButton.setDisable(true);
                firstButton.setDisable(true);
                nextButton.setDisable(true);
                lastButton.setDisable(true);
            }
            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }

    }

    public int readDataSize() {
        return 0;
    }

    public List<P> readPageData() {
        return null;
    }

    public List<P> readData() {
        return null;
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
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getBaseTitle());
            alert.setContentText(AppVariables.message("SureDelete"));
            alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSure = new ButtonType(AppVariables.message("Sure"));
            ButtonType buttonCancel = new ButtonType(AppVariables.message("Cancel"));
            alert.getButtonTypes().setAll(buttonSure, buttonCancel);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() != buttonSure) {
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
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    popInformation(message("Deleted") + ":" + deletedCount);
                    if (deletedCount > 0) {
                        refreshAction();
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

    @FXML
    @Override
    public void clearAction() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(getBaseTitle());
        alert.setContentText(AppVariables.message("SureClear"));
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        ButtonType buttonSure = new ButtonType(AppVariables.message("Sure"));
        ButtonType buttonCancel = new ButtonType(AppVariables.message("Cancel"));
        alert.getButtonTypes().setAll(buttonSure, buttonCancel);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        stage.toFront();

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() != buttonSure) {
            return;
        }

        if (clearData()) {
            clearView();
            refreshAction();
        }
    }

    protected boolean clearData() {
        return true;
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
    @Override
    public void nextAction() {
        currentPage++;
        loadTableData();
    }

    @FXML
    @Override
    public void previousAction() {
        currentPage--;
        loadTableData();
    }

    @FXML
    @Override
    public void firstAction() {
        currentPage = 1;
        loadTableData();
    }

    @FXML
    @Override
    public void lastAction() {
        currentPage = Integer.MAX_VALUE;
        loadTableData();
    }

    @FXML
    protected void importAction() {
        final FileChooser fileChooser = new FileChooser();
        File path = AppVariables.getUserConfigPath(sourcePathKey);
        if (path.exists()) {
            fileChooser.setInitialDirectory(path);
        }
        fileChooser.getExtensionFilters().addAll(CommonFxValues.AllExtensionFilter);
        File file = fileChooser.showOpenDialog(getMyStage());
        if (file == null || !file.exists()) {
            return;
        }
        recordFileOpened(file);
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
