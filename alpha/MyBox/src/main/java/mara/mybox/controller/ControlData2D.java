package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import mara.mybox.data.Data2D;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.table.TableDataColumn;
import mara.mybox.db.table.TableDataDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 *
 */
public class ControlData2D extends BaseController {

    protected Data2D data2D;
    protected TableDataDefinition tableDataDefinition;
    protected TableDataColumn tableDataColumn;

    @FXML
    protected TabPane tabPane;
    @FXML
    protected Tab editTab, viewTab, defineTab;
    @FXML
    protected ControlData2DEdit editController;
    @FXML
    protected ControlData2DView viewController;
    @FXML
    protected ControlData2DDefine defineController;
    @FXML
    protected HBox paginationBox;
    @FXML
    protected ComboBox<String> pageSizeSelector, pageSelector;
    @FXML
    protected Label pageLabel, totalLabel;

    public void setDataType(Data2D.Type type) {
        try {
            data2D = Data2D.create(type);

            tableDataDefinition = new TableDataDefinition();
            tableDataColumn = new TableDataColumn();

            data2D.setTableDataDefinition(tableDataDefinition);
            data2D.setTableDataColumn(tableDataColumn);

            editController.setParameters(this);
            viewController.setParameters(this);
            defineController.setParameters(this);

            initPagination();

            data2D.getPageDataChangedNotify().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                        dataChanged();
                    });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void dataChanged() {

    }

    public void updateInterface() {
        editController.loadData();
        viewController.loadData();
        defineController.loadTableData();
    }

    public void loadData(List<List<String>> data, List<ColumnDefinition> dataColumns) {
        data2D.loadPageData(data, dataColumns);
        updateInterface();
    }

    @FXML
    @Override
    public void createAction() {
        try {
            if (!checkBeforeNextAction()) {
                return;
            }
            data2D.initData();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        paigination
     */
    protected void initPagination() {
        try {
            data2D.firstPage();

            if (pageSelector == null) {
                return;
            }
            pageSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        checkCurrentPage();
                    });

            int pageSize = UserConfig.getInt(baseName + "PageSize", 50);
            pageSize = pageSize < 1 ? 50 : pageSize;
            data2D.setPageSize(pageSize);
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
                                pageSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                            } else {
                                UserConfig.setInt(baseName + "PageSize", v);
                                data2D.setPageSize(v);
                                pageSizeSelector.getEditor().setStyle(null);
                                if (!isSettingValues) {
                                    loadPage(data2D.getCurrentPage());
                                }
                            }
                        } catch (Exception e) {
                            pageSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    });

            paginationBox.setVisible(false);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadPage(long pageNumber) {
        if (data2D == null || !data2D.hasData()) {
            return;
        }
        if (!checkBeforeNextAction()) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                task.cancel();
            }
            task = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    countPagination(pageNumber);
                    data2D.readPageData(task);
                    return !isCancelled() && error == null;
                }

                @Override
                protected void whenSucceeded() {
                    data2D.setPageDataChanged(false);
                    editController.loadData();
                    setPagination();
                }

                @Override
                protected void finalAction() {
                    data2D.notifyPageDataLoaded();
                    updateLabel();
                    task = null;
                }

            };
            start(task);
        }
    }

    protected void countPagination(long pageNumber) {
        long dataNumber = data2D.getDataNumber(), pageSize = data2D.getPageSize();
        long pagesNumber;
        if (dataNumber <= pageSize) {
            pagesNumber = 1;
        } else {
            pagesNumber = dataNumber / pageSize;
            if (dataNumber % pageSize > 0) {
                pagesNumber++;
            }
        }
        data2D.setPagesNumber(pagesNumber);
        long currentPage = pageNumber;
        if (currentPage < 0) {
            currentPage = 0;
        }
        if (currentPage > pagesNumber - 1) {
            currentPage = pagesNumber - 1;
        }
        data2D.setStartRowOfCurrentPage(pageSize * currentPage);
        data2D.setCurrentPage(currentPage);
    }

    protected void setPagination() {
        try {
            if (paginationBox == null) {
                return;
            }
            if (data2D.isNew()) {
                paginationBox.setVisible(false);
                return;
            }
            paginationBox.setVisible(true);
            isSettingValues = true;
            pageSelector.setDisable(false);
            long currentPage = data2D.getCurrentPage(), pagesNumber = data2D.getPagesNumber();
            List<String> pages = new ArrayList<>();
            for (long i = Math.max(1, currentPage - 20);
                    i <= Math.min(pagesNumber, currentPage + 20); i++) {
                pages.add(i + "");
            }
            pageSelector.getItems().clear();
            pageSelector.getItems().addAll(pages);
            pageSelector.getSelectionModel().select((currentPage + 1) + "");

            pageLabel.setText("/" + pagesNumber);
            if (currentPage > 0) {
                pagePreviousButton.setDisable(false);
                pageFirstButton.setDisable(false);
            } else {
                pagePreviousButton.setDisable(true);
                pageFirstButton.setDisable(true);
            }
            if (currentPage >= pagesNumber - 1) {
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
        totalLabel.setText((data2D.getPagesNumber() <= 1 ? message("RowsNumber") : message("LinesNumberInFile"))
                + ":" + data2D.getDataNumber());
    }

    protected boolean checkCurrentPage() {
        if (isSettingValues || pageSelector == null) {
            return false;
        }
        String value = pageSelector.getEditor().getText();
        try {
            int v = Integer.parseInt(value);
            if (v < 1) {
                pageSelector.getEditor().setStyle(UserConfig.badStyle());
                return false;
            } else {
                pageSelector.getEditor().setStyle(null);
                loadPage(v - 1);
                return true;
            }
        } catch (Exception e) {
            pageSelector.getEditor().setStyle(UserConfig.badStyle());
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
        loadPage(data2D.getCurrentPage() + data2D.pageRowsNumber() / data2D.getPageSize());
    }

    @FXML
    @Override
    public void pagePreviousAction() {
        loadPage(data2D.getCurrentPage() - 1);
    }

    @FXML
    @Override
    public void pageFirstAction() {
        loadPage(0);
    }

    @FXML
    @Override
    public void pageLastAction() {
        loadPage(Integer.MAX_VALUE);
    }

    /*
        interface
     */
    @FXML
    @Override
    public boolean popAction() {
        try {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == viewTab) {
//                HtmlPopController.openWebView(this, webView);
                return true;

            } else if (tab == editTab) {
//                MarkdownPopController.open(this, markdownArea);
                return true;

            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    @FXML
    @Override
    public boolean synchronizeAction() {
        try {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == viewTab) {

                return true;

            } else if (tab == editTab) {

                return true;

            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    @FXML
    @Override
    public boolean menuAction() {
        try {
            closePopup();

            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == editTab) {
//                Point2D localToScreen = webView.localToScreen(webView.getWidth() - 80, 80);
//                MenuWebviewController.pop(webViewController, null, localToScreen.getX(), localToScreen.getY());
                return true;

            } else if (tab == viewTab) {
//                Point2D localToScreen = codesArea.localToScreen(codesArea.getWidth() - 80, 80);
//                MenuHtmlCodesController.open(this, codesArea, localToScreen.getX(), localToScreen.getY());
                return true;

            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    @Override
    public boolean checkBeforeNextAction() {
        boolean goOn;
        if (!data2D.isPageDataChanged()) {
            goOn = true;
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setHeaderText(getMyStage().getTitle());
            alert.setContentText(Languages.message("NeedSaveBeforeAction"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSave = new ButtonType(Languages.message("Save"));
            ButtonType buttonNotSave = new ButtonType(Languages.message("NotSave"));
            ButtonType buttonCancel = new ButtonType(Languages.message("Cancel"));
            alert.getButtonTypes().setAll(buttonSave, buttonNotSave, buttonCancel);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonSave) {
                saveAction();
                goOn = false;
            } else {
                goOn = result.get() == buttonNotSave;
            }
        }
        if (goOn) {
            if (task != null) {
                task.cancel();
            }
            if (backgroundTask != null) {
                backgroundTask.cancel();
            }
            data2D.setPageDataChanged(false);
        }
        return goOn;
    }

    @Override
    public void cleanPane() {
        try {
            tableDataDefinition = null;
            tableDataColumn = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
