package mara.mybox.controller;

import java.util.List;
import java.util.Optional;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mara.mybox.data.QueryCondition;
import mara.mybox.data.QueryCondition.DataOperation;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.TableQueryCondition;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.tools.HtmlTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-5-2
 * @License Apache License Version 2.0
 */
public class DataAnalysisController<P> extends TableManageController<P> {

    protected String finalTitle, dataQueryString, pageQueryString;
    protected String queryPrefix, sizePrefix, clearPrefix, queryOrder, orderTitle,
            dataQuerySQL, sizeQuerySQL, clearSQL, pageQuerySQL;
    protected String tableDefinition;
    protected DataOperation dataOperation;
    protected QueryCondition queryCondition, exportCondition, clearCondition;
    protected boolean prefixEditable, supportTop;
    protected int topNumber;

    @FXML
    protected TabPane tabsPane;
    @FXML
    protected Tab dataTab, infoTab, settingsTab;
    @FXML
    protected WebView infoView;
    @FXML
    protected GeographyCodeConditionTreeController geoController;
    @FXML
    protected Button dataImportButton, dataExportButton;
    @FXML
    protected CheckBox consoleCheck;

    public DataAnalysisController() {
        prefixEditable = false;
        supportTop = false;
    }

    /*
        Methods need implementation/updates
     */
    public void initSQL() {
//        queryPrefix = "SELECT * FROM " + dataName;
//        sizePrefix = "SELECT count(dataid) FROM " + dataName;
//        clearPrefix = "DELETE FROM " + dataName;
    }

    protected DerbyBase dataTable() {
        return null;
    }

    protected DataExportController dataExporter() {
        return null;
    }

    protected void checkOrderBy() {

    }

    protected String checkWhere() {
        if (geoController == null) {
            return null;
        }
        geoController.check();
        String where = geoController.getFinalConditions();
        if (where == null) {
            popError(message("SetConditionsComments"));
            return null;
        }
        return where;
    }

    protected String checkTitle() {
        if (geoController == null) {
            return null;
        }
        String title = geoController.getFinalTitle();
        if (title == null) {
            popError(message("SetConditionsComments"));
            return null;
        }
        return title;
    }

    protected QueryCondition checkCondition(boolean careOrder) {
        String where = checkWhere();
        if (where == null) {
            return null;
        }
        checkOrderBy();
        String title = checkTitle()
                + (!careOrder || orderTitle == null || orderTitle.isBlank() ? "" : "\n" + orderTitle)
                + (topNumber <= 0 ? "" : "\n" + message("NumberTopDataDaily") + ": " + topNumber);
        return QueryCondition.create()
                .setDataName(dataName)
                .setPrefix(queryPrefix)
                .setWhere(where)
                .setOrder(queryOrder)
                .setTop(topNumber)
                .setTitle(title);
    }

    protected boolean checkQueryCondition() {
        QueryCondition condition = checkCondition(true);
        if (condition == null) {
            popError(message("SetConditionsComments"));
            return false;
        }
        queryCondition = condition.setDataOperation(DataOperation.QueryData);
        return true;
    }

    protected boolean checkClearCondition() {
        QueryCondition condition = checkCondition(false);
        if (condition == null) {
            popError(message("SetConditionsComments"));
            return false;
        }
        clearCondition = condition.setDataOperation(DataOperation.ClearData);
        return true;
    }

    protected boolean checkExportCondition() {
        QueryCondition condition = checkCondition(true);
        if (condition == null) {
            popError(message("SetConditionsComments"));
            return false;
        }
        exportCondition = condition.setDataOperation(DataOperation.ExportData);
        return true;
    }

    protected void setQuerySQL() {
        if (queryCondition == null) {
            return;
        }
        if (queryCondition.getWhere() != null && !queryCondition.getWhere().isBlank()) {
            dataQuerySQL = queryCondition.getPrefix() + " WHERE " + queryCondition.getWhere();
            sizeQuerySQL = sizePrefix + " WHERE " + queryCondition.getWhere();
        } else {
            dataQuerySQL = queryCondition.getPrefix();
            sizeQuerySQL = sizePrefix;
        }
        dataQuerySQL += queryCondition.getOrder() == null || queryCondition.getOrder().isBlank()
                ? "" : " ORDER BY " + queryCondition.getOrder();
        pageQuerySQL = null;

        dataQueryString = dataQuerySQL
                + (queryCondition.getTop() <= 0 ? "" : "</br>" + message("NumberTopDataDaily") + ": " + topNumber);
        pageQueryString = pageQuerySQL;
    }

    @FXML
    public void queryData() {
        if (isSettingValues) {
            return;
        }
        if (!checkQueryCondition()) {
            return;
        }
        TableQueryCondition.write(queryCondition, true);
        loadTableData();
    }

    @Override
    public boolean preLoadingTableData() {
        if (isSettingValues) {
            return false;
        }
        if (queryCondition == null) {
            popError(message("SetConditionsComments"));
            return false;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        infoView.getEngine().loadContent​("");
        finalTitle = queryCondition.getTitle().replaceAll("\n", " ");
        setQuerySQL();
        return true;
    }

    protected void setPageSQL() {
        String dataFetch = "OFFSET " + currentPageStart + " ROWS FETCH NEXT " + currentPageSize + " ROWS ONLY";
        pageQuerySQL = dataQuerySQL + " " + dataFetch;
        pageQueryString = pageQuerySQL;
        if (queryCondition != null) {
            queryCondition.setFetch(dataFetch);
        }
        if (pagesNumber > 1) {
            finalTitle = queryCondition.getTitle() + " - " + message("Page") + currentPage;
        }
    }

    @Override
    public List<P> readPageData() {
        setPageSQL();
        return null;
    }

    @Override
    public void postLoadedTableData() {
        if (queryCondition == null) {
            return;
        }
        loadInfo();
    }

    @FXML
    protected void popImportMenu(MouseEvent mouseEvent) {

    }

    @FXML
    protected void popSetMenu(MouseEvent mouseEvent) {

    }


    /*
        Common methods
     */
    @Override
    public void initializeNext() {
        try {
            super.initializeNext();
            initSQL();
            consoleCheck.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                        loadInfo();
                    });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();
            setButtons();
            loadInfo();
            if (geoController != null) {
                geoController.setUserController(this);
                geoController.loadTree();
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void setButtons() {
        try {
            FxmlControl.removeTooltip(goButton);
            FxmlControl.removeTooltip(clearButton);
            FxmlControl.setTooltip(deleteButton, message("Delete") + "\nDELETE / CTRL+d / ALT+d\n\n"
                    + message("DataDeletedComments"));
            FxmlControl.removeTooltip(setButton);
            FxmlControl.removeTooltip(dataImportButton);
            FxmlControl.removeTooltip(dataExportButton);
            goButton.requestFocus();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void refreshAction() {
        queryData();
        if (geoController != null) {
            geoController.loadTree();
        }
    }

    public void loadInfo() {
        try {
            String html = "";
            if (queryCondition == null) {
                html += "<b>" + message("SetConditionsComments") + "</b> </br>";
            } else {
                html += "<b>" + message("QueryConditionsName") + ":</b> </br>";
                html += "<font color=\"#2e598a\">" + queryCondition.getTitle().replaceAll("\n", "</br>") + "</font></br></br>";

                html += "<b>" + message("QueryConditions") + ": </b></br>";
                html += "<font color=\"#2e598a\">" + queryCondition.getWhere() + "</font></br></br>";

                if (dataQueryString != null && !dataQueryString.isBlank()) {
                    html += "<b>" + message("DataQuery") + ": </b></br>";
                    html += "<font color=\"#2e598a\">" + dataQueryString + "</font></br>";
                    html += "<b>" + message("DataNumber") + ": </b>" + totalSize + "</br></br>";
                }
                if (queryCondition.getFetch() != null && !queryCondition.getFetch().isBlank()) {
                    html += "<b>" + message("CurrentPage") + ": </b></br>";
                    html += "<font color=\"#2e598a\">" + queryCondition.getFetch() + "</font></br>";
                    html += "<b>" + message("DataNumber") + ": </b>" + tableData.size() + "</br></br>";
                }
            }

            html += loadMoreInfo();

            html = HtmlTools.html(null,
                    consoleCheck.isSelected() ? HtmlTools.ConsoleStyle : HtmlTools.DefaultStyle,
                    html);
            infoView.getEngine().loadContent​(html);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected String loadMoreInfo() {
        return "";
    }

    @Override
    protected void checkSelected() {
        if (isSettingValues) {
            return;
        }
        super.checkSelected();
        int selection = tableView.getSelectionModel().getSelectedIndices().size();
        if (setButton != null) {
            setButton.setDisable(selection == 0);
        }
    }

    @FXML
    @Override
    public void clearAction() {
        if (!checkClearCondition()) {
            return;
        }
        TableQueryCondition.write(clearCondition, true);
        clear();
    }

    protected void setClearSQL() {
        clearSQL = clearPrefix + (clearCondition.getWhere().isBlank() ? "" : " WHERE " + clearCondition.getWhere());
    }

    public void clear() {
        if (clearCondition == null) {
            popError(message("SetConditionsComments"));
            return;
        }
        setClearSQL();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(getBaseTitle());
        alert.setContentText(AppVariables.message("SureClearConditions")
                + "\n\n" + clearCondition.getTitle().replaceAll("</br>", "\n")
                + "\n\n" + clearSQL
                + "\n\n" + message("DataDeletedComments")
        );
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
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {
                private int count = 0;

                @Override
                protected boolean handle() {
                    count = dataTable().update(clearSQL);

                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    alertInformation(message("Deleted") + ": " + count);
                    refreshAction();
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void loadAsConditions(QueryCondition condition) {
        queryCondition = condition;
        loadTableData();
    }

    protected String tableDefinition() {
        if (tableDefinition == null) {
            tableDefinition = dataTable().getCreate_Table_Statement();
        }
        return tableDefinition;
    }

    @FXML
    protected void popQueryMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menu = new MenuItem(message("QueryAsCondition") + "\nF1 / CTRL+q / ALT+Q");
            menu.setOnAction((ActionEvent event) -> {
                queryData();
            });
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("InputConditions"));
            menu.setOnAction((ActionEvent event) -> {
                QueryCondition condition = QueryCondition.create()
                        .setDataName(dataName)
                        .setDataOperation(DataOperation.QueryData)
                        .setPrefix(queryPrefix);
                DataQueryController controller = (DataQueryController) openStage(CommonValues.DataQueryFxml);
                controller.setValue(this, condition, tableDefinition(), prefixEditable, supportTop);
            });
            popMenu.getItems().add(menu);

            List<QueryCondition> list = TableQueryCondition.readList(dataName,
                    DataOperation.QueryData,
                    AppVariables.fileRecentNumber > 0 ? AppVariables.fileRecentNumber : 15);
            if (list != null && !list.isEmpty()) {
                popMenu.getItems().add(new SeparatorMenuItem());
                menu = new MenuItem(message("RecentUsedConditions"));
                menu.setStyle("-fx-text-fill: #2e598a;");
                popMenu.getItems().add(menu);
                for (QueryCondition condition : list) {
                    menu = new MenuItem(condition.getTitle().replaceAll("</br>", " ").replaceAll("\n", " "));
                    menu.setOnAction((ActionEvent event) -> {
                        queryCondition = condition;
                        loadTableData();
                    });
                    popMenu.getItems().add(menu);
                }
            }

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("MenuClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent event) -> {
                popMenu.hide();
                popMenu = null;
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateBelow((Region) mouseEvent.getSource(), popMenu);

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    protected void popClearMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menu = new MenuItem(message("ClearAsCondition") + "\nCTRL+r / ALT+r");
            menu.setOnAction((ActionEvent event) -> {
                clearAction();
            });
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(message("InputConditions"));
            menu.setOnAction((ActionEvent event) -> {
                QueryCondition condition = QueryCondition.create()
                        .setDataName(dataName)
                        .setDataOperation(DataOperation.ClearData)
                        .setPrefix(clearPrefix);
                DataQueryController controller = (DataQueryController) openStage(CommonValues.DataQueryFxml);
                controller.setValue(this, condition, tableDefinition(), prefixEditable, supportTop);
            });
            popMenu.getItems().add(menu);

            List<QueryCondition> list = TableQueryCondition.readList(
                    dataName, DataOperation.ClearData,
                    AppVariables.fileRecentNumber > 0 ? AppVariables.fileRecentNumber : 15);
            if (list != null && !list.isEmpty()) {
                popMenu.getItems().add(new SeparatorMenuItem());

                menu = new MenuItem(message("RecentUsedConditions"));
                menu.setStyle("-fx-text-fill: #2e598a;");
                popMenu.getItems().add(menu);

                for (QueryCondition condition : list) {
                    menu = new MenuItem(condition.getTitle().replaceAll("</br>", " ").replaceAll("\n", " "));
                    menu.setOnAction((ActionEvent event) -> {
                        clearCondition = condition;
                        clear();
                    });
                    popMenu.getItems().add(menu);
                }
            }

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("MenuClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent event) -> {
                popMenu.hide();
                popMenu = null;
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateBelow((Region) mouseEvent.getSource(), popMenu);

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    public void clearAsConditions(QueryCondition condition) {
        clearCondition = condition;
        clear();
    }

    @FXML
    public void exportData() {
        if (!checkExportCondition()) {
            return;
        }
        TableQueryCondition.write(exportCondition, true);
        DataExportController controller = dataExporter();
        controller.setValue(this, exportCondition, tableDefinition(), prefixEditable, supportTop);
    }

    @FXML
    protected void popExportMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menu;

            menu = new MenuItem(message("ExportAsCondition") + "\nF3 / CTRL+e / ALT+E");
            menu.setOnAction((ActionEvent event) -> {
                exportData();
            });
            popMenu.getItems().add(menu);

            if (queryCondition != null && tableData != null && !tableData.isEmpty()) {
                menu = new MenuItem(message("ExportCurrentPage"));
                menu.setOnAction((ActionEvent event) -> {
                    if (queryCondition == null || tableData == null || tableData.isEmpty()) {
                        popError(message("NoData"));
                        return;
                    }
                    QueryCondition condition = QueryCondition.create()
                            .setDataName(dataName)
                            .setDataOperation(DataOperation.ExportData)
                            .setTitle(finalTitle)
                            .setPrefix(queryCondition.getPrefix())
                            .setWhere(queryCondition.getWhere())
                            .setOrder(queryCondition.getOrder())
                            .setFetch(queryCondition.getFetch())
                            .setTop(queryCondition.getTop());
                    DataExportController controller = dataExporter();
                    controller.currentPage(this, condition, tableDefinition(), prefixEditable, supportTop);
                });
                popMenu.getItems().add(menu);
            }

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("InputConditions"));
            menu.setOnAction((ActionEvent event) -> {
                QueryCondition condition = QueryCondition.create()
                        .setDataName(dataName)
                        .setDataOperation(DataOperation.ExportData)
                        .setPrefix(queryPrefix);
                DataExportController controller = dataExporter();
                controller.setValue(this, condition, tableDefinition(), prefixEditable, supportTop);
            });
            popMenu.getItems().add(menu);

            List<QueryCondition> list = TableQueryCondition.readList(
                    dataName, DataOperation.ExportData,
                    AppVariables.fileRecentNumber > 0 ? AppVariables.fileRecentNumber : 15);
            if (list != null && !list.isEmpty()) {
                popMenu.getItems().add(new SeparatorMenuItem());
                menu = new MenuItem(message("RecentUsedConditions"));
                menu.setStyle("-fx-text-fill: #2e598a;");
                popMenu.getItems().add(menu);
                for (QueryCondition condition : list) {
                    menu = new MenuItem(condition.getTitle().replaceAll("</br>", " ").replaceAll("\n", " "));
                    menu.setOnAction((ActionEvent event) -> {
                        DataExportController controller = dataExporter();
                        controller.setValue(this, condition, tableDefinition(), prefixEditable, supportTop);
                    });
                    popMenu.getItems().add(menu);
                }
            }

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("MenuClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent event) -> {
                popMenu.hide();
                popMenu = null;
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateBelow((Region) mouseEvent.getSource(), popMenu);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void keyHandler(KeyEvent event) {
        KeyCode code = event.getCode();
        if (code != null) {
            switch (code) {
                case F1:
                    queryData();
                    return;
                case F3:
                    exportData();
            }
        }
        super.keyHandler(event);
    }

    @Override
    public void controlHandler(KeyEvent event) {
        if (!event.isControlDown()) {
            return;
        }
        String key = event.getText();
        if (key != null) {
            switch (key) {
                case "q":
                case "Q":
                    queryData();
                    return;
                case "e":
                case "E":
                    exportData();
                    return;
                case "r":
                case "R":
                    clearAction();
            }
        }
        super.controlHandler(event);
    }

    @Override
    public void altHandler(KeyEvent event) {
        if (!event.isAltDown()) {
            return;
        }
        String key = event.getText();
        if (key != null) {
            switch (key) {
                case "q":
                case "Q":
                    queryData();
                    return;
                case "e":
                case "E":
                    exportData();
                    return;
                case "r":
                case "R":
                    clearAction();
            }
        }
        super.altHandler(event);
    }

    @Override
    public boolean leavingScene() {
        try {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            if (geoController != null) {
                geoController.leavingScene();
            }
        } catch (Exception e) {
        }
        return super.leavingScene();
    }

}
