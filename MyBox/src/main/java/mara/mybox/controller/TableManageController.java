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
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mara.mybox.db.DerbyBase;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;

/**
 * @Author Mara
 * @CreateDate 2019-12-18
 * @License Apache License Version 2.0
 */
public class TableManageController<P> extends BaseController {

    protected ObservableList<P> tableData;
    protected int currentPage, pageSize;
    protected String dataName;

    @FXML
    protected TableView<P> tableView;
    @FXML
    protected Button editButton;
    @FXML
    protected ComboBox<String> pageSizeSelector, pageSelector;
    @FXML
    protected Label pageLabel, dataSizeLabel, selectedLabel;

    public TableManageController() {
        dataName = "Data";

        targetPathKey = "dataTargetPath";
        sourcePathKey = "dataSourcePath";
    }

    @Override
    public void initializeNext() {
        try {
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

            initButtons();

            initPagination();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initColumns() {
        try {

        } catch (Exception e) {
            logger.error(e.toString());
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

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();
        load();
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

    }

    protected void initButtons() {
        try {

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initPagination() {
        try {
            if (pageSelector == null) {
                return;
            }

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
                                load();
                            }
                        } catch (Exception e) {
                            pageSelector.getEditor().setStyle(badStyle);
                        }
                    });

            pageSize = 50;
            pageSizeSelector.getItems().addAll(Arrays.asList("50", "30", "100", "20", "60", "200", "300"));
            pageSizeSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        try {
                            int v = Integer.parseInt(newValue.trim());
                            if (v <= 0) {
                                pageSizeSelector.getEditor().setStyle(badStyle);
                            } else {
                                pageSize = v;
                                AppVariables.setUserConfigValue(baseName + "PageSize", pageSize + "");
                                pageSizeSelector.getEditor().setStyle(null);
                                load();
                            }
                        } catch (Exception e) {
                            pageSizeSelector.getEditor().setStyle(badStyle);
                        }
                    });
            pageSizeSelector.getSelectionModel().select(AppVariables.getUserConfigValue(baseName + "PageSize", "50"));
            pageSizeSelector.getEditor().setStyle(null);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void load() {
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {
                private int total, pagesNumber;
                private List<P> data;

                @Override
                protected boolean handle() {
                    total = readDataSize();
                    if (total <= pageSize) {
                        pagesNumber = 1;
                    } else {
                        pagesNumber = total % pageSize == 0 ? total / pageSize : total / pageSize + 1;
                    }
                    if (currentPage <= 0) {
                        currentPage = 1;
                    }
                    if (currentPage > pagesNumber) {
                        currentPage = pagesNumber;
                    }
                    int start = pageSize * (currentPage - 1);
                    int end = Math.min(start + pageSize - 1, total);
                    data = readData(start, end - start + 1);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    isSettingValues = true;
                    tableData.clear();
                    if (data != null && !data.isEmpty()) {
                        tableData.addAll(data);
                    }
                    isSettingValues = false;
                    tableView.refresh();
                    checkSelected();
                    setPagination(total, pagesNumber);
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    protected void setPagination(int total, int pagesNumber) {
        try {
            isSettingValues = true;
            List<String> pages = new ArrayList<>();
            for (int i = Math.max(1, currentPage - 20);
                    i <= Math.min(pagesNumber, currentPage + 20); i++) {
                pages.add(i + "");
            }
            pageSelector.getItems().clear();
            pageSelector.getItems().addAll(pages);
            pageSelector.getSelectionModel().select(currentPage + "");
            isSettingValues = false;

            pageLabel.setText("/" + pagesNumber);
            dataSizeLabel.setText(message("Data") + ": " + tableData.size() + "/" + total);
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
        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    public int readDataSize() {
        return 0;
    }

    public List<P> readData(int offset, int number) {
        return null;
    }

    @FXML
    @Override
    public void deleteAction() {
        if (deleteSelectedData()) {
            load();
        }
    }

    protected boolean deleteSelectedData() {
        List<P> selected = tableView.getSelectionModel().getSelectedItems();
        return true;
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
            tableData.clear();
            tableView.refresh();
            setPagination(0, 0);
            checkSelected();
        }
    }

    protected boolean clearData() {
        return true;
    }

    @FXML
    public void addAction() {
        try {

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    @Override
    public void nextAction() {
        currentPage++;
        load();
    }

    @FXML
    @Override
    public void previousAction() {
        currentPage--;
        load();
    }

    @FXML
    @Override
    public void firstAction() {
        currentPage = 1;
        load();
    }

    @FXML
    @Override
    public void lastAction() {
        currentPage = Integer.MAX_VALUE;
        load();
    }

    public void loadExamples() {

    }

    @FXML
    public void examplesAction() {
        synchronized (this) {
            if (task != null) {
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
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    public void refreshAction() {
        load();
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
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    DerbyBase.importData(dataName, file.getAbsolutePath(), false);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    refreshAction();
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    protected void exportAction() {
        final File file = chooseSaveFile(AppVariables.getUserConfigPath(targetPathKey),
                message(dataName) + ".del", CommonFxValues.AllExtensionFilter, false);
        if (file == null) {
            return;
        }
        recordFileWritten(file);
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    DerbyBase.exportData(dataName, file.getAbsolutePath());
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    FxmlStage.openTextEditer(null, file);
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

}
