package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.QueryCondition;
import mara.mybox.db.data.QueryCondition.DataOperation;
import mara.mybox.db.table.BaseTable;
import mara.mybox.db.table.ColumnDefinition;
import mara.mybox.db.table.TableQueryCondition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.tools.HtmlTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-5-2
 * @License Apache License Version 2.0
 */
public abstract class BaseDataManageController<P> extends BaseDataTableController<P> {

    protected BaseTable viewDefinition;
    protected String finalTitle, dataQueryString, pageQueryString,
            queryPrefix, sizePrefix, clearPrefix, queryOrder, orderTitle,
            dataQuerySQL, sizeQuerySQL, clearSQL, pageQuerySQL, tableDefinitionString;
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
    protected Button locationButton;
    @FXML
    protected ListView orderByList;
    @FXML
    protected ControlCSVEdit csvEditController;

    public BaseDataManageController() {
        prefixEditable = false;
        supportTop = false;
    }

    /*
        Methods need implementation/updates
     */
    @Override
    public void setTableDefinition() {
    }

    public void setTableValues() {
        queryPrefix = "SELECT * FROM " + tableName;
        sizePrefix = "SELECT count(" + idColumn + ") FROM " + tableName;
        clearPrefix = "DELETE FROM " + tableName;
        if (tableDefinition != null) {
            tableDefinitionString = tableDefinition.html();
            viewDefinition = tableDefinition;
        }
    }

    protected DataExportController dataExporter() {
        return (DataExportController) openStage(CommonValues.DataExportFxml);
    }

    protected String checkWhere() {
        if (geoController == null) {
            return null;
        }
        String where = geoController.check();
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
                .setDataName(tableName)
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
        if (currentPageStart < 1) {
            currentPageStart = 1;
        }
        if (currentPageSize < 1) {
            currentPageSize = pageSize > 0 ? pageSize : 50;
        }
        String dataFetch = "OFFSET " + (currentPageStart - 1) + " ROWS FETCH NEXT " + currentPageSize + " ROWS ONLY";
        pageQuerySQL = dataQuerySQL + " " + dataFetch;
        pageQueryString = pageQuerySQL;
        if (queryCondition != null) {
            queryCondition.setFetch(dataFetch);
            if (pagesNumber > 1) {
                finalTitle = queryCondition.getTitle() + " - " + message("Page") + currentPage;
            }
        }
    }

    @Override
    public List<P> readPageData() {
        setPageSQL();
        return tableDefinition.readData(pageQuerySQL);
    }

    @Override
    public void postLoadedTableData() {
        if (queryCondition == null) {
            return;
        }
        loadInfo();
    }

    public void reloadChart() {

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
    public void initControls() {
        try {
            super.initControls();
            setTableValues();
            initOrder();
            if (csvEditController != null) {
                csvEditController.init(this, tableDefinition);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initOrder() {
        try {
            if (orderByList == null || tableDefinition == null) {
                return;
            }
            topNumber = 0;

            if (viewDefinition == null) {
                viewDefinition = tableDefinition;
            }
            orderByList.getItems().clear();
            orderByList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            for (Object o : viewDefinition.getColumns()) {
                ColumnDefinition column = (ColumnDefinition) o;
                String label = column.getLabel();
                orderByList.getItems().add(label + " " + message("Ascending"));
                orderByList.getItems().add(label + " " + message("Descending"));
            }
            orderByList.getSelectionModel().select(0);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void checkOrderBy() {
        try {
            if (orderByList == null || viewDefinition == null) {
                return;
            }
            queryOrder = "";
            orderTitle = "";
            List<String> selected = orderByList.getSelectionModel().getSelectedItems();
            for (String item : selected) {
                if (item.endsWith(" " + message("Ascending"))) {
                    String name = item.substring(0, item.length() - message("Ascending").length() - 1);
                    ColumnDefinition column = viewDefinition.columnByMessage(name);
                    if (column != null) {
                        String q = column.getName() + " ASC";
                        queryOrder = queryOrder.isBlank() ? q : queryOrder + ", " + q;
                    }
                } else if (item.endsWith(" " + message("Descending"))) {
                    String name = item.substring(0, item.length() - message("Descending").length() - 1);
                    ColumnDefinition column = viewDefinition.columnByMessage(name);
                    if (column != null) {
                        String q = column.getName() + " DESC";
                        queryOrder = queryOrder.isBlank() ? q : queryOrder + ", " + q;
                    }
                }
                orderTitle = orderTitle.isBlank() ? "\"" + item + "\"" : orderTitle + " \"" + item + "\"";
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();
            setButtons();
            loadInfo();
            if (geoController != null) {
                geoController.setParent(this);
                geoController.loadTree();
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void setButtons() {
        try {
            if (clearButton != null) {
                FxmlControl.removeTooltip(clearButton);
            }
            if (deleteButton != null) {
                FxmlControl.setTooltip(deleteButton, message("Delete") + "\nDELETE / CTRL+d / ALT+d\n\n"
                        + message("DataDeletedComments"));
            }
            if (setButton != null) {
                FxmlControl.removeTooltip(setButton);
            }
            if (importButton != null) {
                FxmlControl.removeTooltip(importButton);
            }
            if (exportButton != null) {
                FxmlControl.removeTooltip(exportButton);
            }
            if (queryButton != null) {
                FxmlControl.removeTooltip(queryButton);
                queryButton.requestFocus();
            }

            if (csvEditController != null) {
                FxmlControl.removeTooltip(csvEditController.inputButton);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    //  Call this when data are changed and need reload all
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
                html += "<SPAN class=\"boldText\">" + message("SetConditionsComments") + "</SPAN>";
            } else {
                html += "<SPAN class=\"boldText\">" + message("QueryConditionsName") + ":</SPAN> </BR>";
                html += "<SPAN class=\"valueText\">" + queryCondition.getTitle().replaceAll("\n", "</BR>") + "</SPAN></BR></BR>";

                html += "<SPAN class=\"boldText\">" + message("QueryConditions") + ":</SPAN></BR>";
                html += "<SPAN class=\"valueText\">" + queryCondition.getWhere() + "</SPAN></BR></BR>";

                if (dataQueryString != null && !dataQueryString.isBlank()) {
                    html += "<SPAN class=\"boldText\">" + message("DataQuery") + ": </SPAN></BR>";
                    html += "<SPAN class=\"valueText\">" + dataQueryString + "</SPAN></BR>";
                    html += "<SPAN class=\"boldText\">" + message("DataNumber") + ": </SPAN>";
                    html += "<SPAN class=\"valueText\">" + totalSize + "</SPAN></BR></BR>";
                }
                if (queryCondition.getFetch() != null && !queryCondition.getFetch().isBlank()) {
                    html += "<SPAN class=\"boldText\">" + message("CurrentPage") + ": </SPAN></BR>";
                    html += "<SPAN class=\"valueText\">" + queryCondition.getFetch() + "</SPAN></BR>";
                    html += "<SPAN class=\"boldText\">" + message("DataNumber") + ": </SPAN>";
                    html += "<SPAN class=\"valueText\">" + tableData.size() + "</SPAN></BR></BR>";
                }
                html += loadMoreInfo();
            }
            String htmlStyle = AppVariables.getUserConfigValue(baseName + "HtmlStyle", "Default");
            html = HtmlTools.html(null, htmlStyle, html);
            infoView.getEngine().loadContent​(html);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
        if (locationButton != null) {
            locationButton.setDisable(selection == 0);
        }
    }

    @Override
    public int readDataSize() {
//        MyBoxLog.debug(sizeQuerySQL);
        return DerbyBase.size(sizeQuerySQL);
    }

    @FXML
    @Override
    public void clearAction() {
        clear(message("ClearAsConditionTrees"));
    }

    protected void setClearSQL() {
        clearSQL = clearPrefix + (clearCondition.getWhere().isBlank() ? "" : " WHERE " + clearCondition.getWhere())
                + (clearPrefix.contains(" JOIN ") ? ")" : "");
    }

    public void loadAsConditions(QueryCondition condition) {
        queryCondition = condition;
        loadTableData();
    }

    @FXML
    public void upAction() {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(orderByList.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        List<Integer> newselected = new ArrayList<>();
        for (Integer index : selected) {
            if (index == 0 || newselected.contains(index - 1)) {
                newselected.add(index);
                continue;
            }
            String lang = (String) orderByList.getItems().get(index);
            orderByList.getItems().set(index, orderByList.getItems().get(index - 1));
            orderByList.getItems().set(index - 1, lang);
            newselected.add(index - 1);
        }
        orderByList.getSelectionModel().clearSelection();
        for (int index : newselected) {
            orderByList.getSelectionModel().select(index);
        }
        orderByList.refresh();
    }

    @FXML
    public void downAction() {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(orderByList.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        List<Integer> newselected = new ArrayList<>();
        for (int i = selected.size() - 1; i >= 0; --i) {
            int index = selected.get(i);
            if (index == orderByList.getItems().size() - 1
                    || newselected.contains(index + 1)) {
                newselected.add(index);
                continue;
            }
            String lang = (String) orderByList.getItems().get(index);
            orderByList.getItems().set(index, orderByList.getItems().get(index + 1));
            orderByList.getItems().set(index + 1, lang);
            newselected.add(index + 1);
        }
        orderByList.getSelectionModel().clearSelection();
        for (int index : newselected) {
            orderByList.getSelectionModel().select(index);
        }
        orderByList.refresh();

    }

    @FXML
    public void topAction() {
        List<Integer> selectedIndices = new ArrayList<>();
        selectedIndices.addAll(orderByList.getSelectionModel().getSelectedIndices());
        if (selectedIndices.isEmpty()) {
            return;
        }
        List<String> selected = new ArrayList<>();
        selected.addAll(orderByList.getSelectionModel().getSelectedItems());
        int size = selectedIndices.size();
        for (int i = size - 1; i >= 0; --i) {
            int index = selectedIndices.get(i);
            orderByList.getItems().remove(index);
        }
        orderByList.getSelectionModel().clearSelection();
        orderByList.getItems().addAll(0, selected);
        orderByList.getSelectionModel().selectRange(0, size);
        orderByList.refresh();
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
                        .setDataName(tableName)
                        .setDataOperation(DataOperation.QueryData)
                        .setPrefix(queryPrefix);
                DataQueryController controller = (DataQueryController) openStage(CommonValues.DataQueryFxml);
                controller.setValue(this, condition, tableDefinitionString, prefixEditable, supportTop);
            });
            popMenu.getItems().add(menu);

            List<QueryCondition> list = TableQueryCondition.readList(tableName,
                    DataOperation.QueryData,
                    AppVariables.fileRecentNumber > 0 ? AppVariables.fileRecentNumber : 15);
            if (list != null && !list.isEmpty()) {
                popMenu.getItems().add(new SeparatorMenuItem());
                menu = new MenuItem(message("RecentUsedConditions"));
                menu.setStyle("-fx-text-fill: #2e598a;");
                popMenu.getItems().add(menu);
                for (QueryCondition condition : list) {
                    menu = new MenuItem(condition.getTitle().replaceAll("</br>|\n", " "));
                    menu.setOnAction((ActionEvent event) -> {
                        queryCondition = condition;
                        loadTableData();
                    });
                    popMenu.getItems().add(menu);
                }
            }

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent event) -> {
                popMenu.hide();
                popMenu = null;
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateBelow((Region) mouseEvent.getSource(), popMenu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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

            MenuItem menu;

            menu = new MenuItem(message("ClearAsConditionTrees") + "\nCTRL+r / ALT+r");
            menu.setOnAction((ActionEvent event) -> {
                clear(message("ClearAsConditionTrees"));
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("ClearSelectedDataInPage"));
            menu.setOnAction((ActionEvent event) -> {
                clear(message("ClearSelectedDataInPage"));
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("ClearCurrentPage"));
            menu.setOnAction((ActionEvent event) -> {
                clear(message("ClearCurrentPage"));
            });
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(message("InputConditions"));
            menu.setOnAction((ActionEvent event) -> {
                QueryCondition condition = QueryCondition.create()
                        .setDataName(tableName)
                        .setDataOperation(DataOperation.ClearData)
                        .setPrefix(clearPrefix);
                DataQueryController controller = (DataQueryController) openStage(CommonValues.DataQueryFxml);
                controller.setValue(this, condition, tableDefinitionString, prefixEditable, supportTop);
            });
            popMenu.getItems().add(menu);

            List<QueryCondition> list = TableQueryCondition.readList(
                    tableName, DataOperation.ClearData,
                    AppVariables.fileRecentNumber > 0 ? AppVariables.fileRecentNumber : 15);
            if (list != null && !list.isEmpty()) {
                popMenu.getItems().add(new SeparatorMenuItem());

                menu = new MenuItem(message("RecentUsedConditions"));
                menu.setStyle("-fx-text-fill: #2e598a;");
                popMenu.getItems().add(menu);

                for (QueryCondition condition : list) {
                    menu = new MenuItem(condition.getTitle().replaceAll("</br>|\n", " "));
                    menu.setOnAction((ActionEvent event) -> {
                        clearCondition = condition;
                        clearAsConditions();
                    });
                    popMenu.getItems().add(menu);
                }
            }

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent event) -> {
                popMenu.hide();
                popMenu = null;
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateBelow((Region) mouseEvent.getSource(), popMenu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    public void clear(String type) {
        String title, sql;
        if (message("ClearSelectedDataInPage").equals(type)) {
            List<P> rows = tableView.getSelectionModel().getSelectedItems();
            if (rows == null || rows.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            title = clearCondition.getTitle().replaceAll("</br>", "\n")
                    + "\n" + message("Selected");
            sql = null;

        } else if (message("ClearCurrentPage").equals(type)) {
            if (tableData == null || tableData.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            title = finalTitle.replaceAll("</br>", "\n")
                    + "\n" + message("Page") + currentPage;
            sql = pageQuerySQL;

        } else if (message("ClearAsConditionTrees").equals(type)) {
            if (!checkClearCondition()) {
                return;
            }
            TableQueryCondition.write(clearCondition, true);
            title = clearCondition.getTitle().replaceAll("</br>", "\n");
            setClearSQL();
            sql = clearSQL;

        } else {
            return;
        }

        if (!FxmlControl.askSure(getBaseTitle(), message("SureClearConditions")
                + "\n\n" + type + "\n" + title + "\n\n" + sql + "\n\n" + message("DataDeletedComments"))) {
            return;
        }

        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                private int deletedCount = 0;

                @Override
                protected boolean handle() {
                    if (message("ClearSelectedDataInPage").equals(type)) {
                        deletedCount = deleteSelectedData();

                    } else if (message("ClearCurrentPage").equals(type)) {
                        deletedCount = deleteData(tableData);

                    } else if (message("ClearAsConditionTrees").equals(type)) {
                        deletedCount = DerbyBase.update(clearSQL);
                    }
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

    public void clearAsConditions(QueryCondition condition) {
        clearCondition = condition;
        clearAsConditions();
    }

    public void clearAsConditions() {
        if (clearCondition == null) {
            popError(message("SetConditionsComments"));
            return;
        }
        setClearSQL();
        if (!FxmlControl.askSure(getBaseTitle(), message("SureClearConditions")
                + "\n\n" + clearCondition.getTitle().replaceAll("</br>", "\n")
                + "\n\n" + clearSQL + "\n\n" + message("DataDeletedComments"))) {
            return;
        }

        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                private int count = 0;

                @Override
                protected boolean handle() {
                    count = DerbyBase.update(clearSQL);

                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    alertInformation(message("Deleted") + ": " + count);
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

    @FXML
    public void exportData() {
        if (!checkExportCondition()) {
            return;
        }
        TableQueryCondition.write(exportCondition, true);
        DataExportController controller = dataExporter();
        controller.setValues(this, exportCondition, tableDefinitionString, prefixEditable, supportTop);
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
                            .setDataName(tableName)
                            .setDataOperation(DataOperation.ExportData)
                            .setTitle(finalTitle)
                            .setPrefix(queryCondition.getPrefix())
                            .setWhere(queryCondition.getWhere())
                            .setOrder(queryCondition.getOrder())
                            .setFetch(queryCondition.getFetch())
                            .setTop(queryCondition.getTop());
                    DataExportController controller = dataExporter();
                    controller.currentPage(this, condition, tableDefinitionString, prefixEditable, supportTop);
                });
                popMenu.getItems().add(menu);
            }

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("InputConditions"));
            menu.setOnAction((ActionEvent event) -> {
                QueryCondition condition = QueryCondition.create()
                        .setDataName(tableName)
                        .setDataOperation(DataOperation.ExportData)
                        .setPrefix(queryPrefix);
                DataExportController controller = dataExporter();
                controller.setValues(this, condition, tableDefinitionString, prefixEditable, supportTop);
            });
            popMenu.getItems().add(menu);

            List<QueryCondition> list = TableQueryCondition.readList(
                    tableName, DataOperation.ExportData,
                    AppVariables.fileRecentNumber > 0 ? AppVariables.fileRecentNumber : 15);
            if (list != null && !list.isEmpty()) {
                popMenu.getItems().add(new SeparatorMenuItem());
                menu = new MenuItem(message("RecentUsedConditions"));
                menu.setStyle("-fx-text-fill: #2e598a;");
                popMenu.getItems().add(menu);
                for (QueryCondition condition : list) {
                    menu = new MenuItem(condition.getTitle().replaceAll("</br>|\n", " "));
                    menu.setOnAction((ActionEvent event) -> {
                        DataExportController controller = dataExporter();
                        controller.setValues(this, condition, tableDefinitionString, prefixEditable, supportTop);
                    });
                    popMenu.getItems().add(menu);
                }
            }

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent event) -> {
                popMenu.hide();
                popMenu = null;
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateBelow((Region) mouseEvent.getSource(), popMenu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void popLinksStyle(MouseEvent mouseEvent) {
        popMenu = FxmlControl.popHtmlStyle(mouseEvent, this, popMenu, infoView.getEngine());
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
                    return;
            }
        }
        super.keyHandler(event);
    }

    @Override
    public void controlAltHandler(KeyEvent event) {
        if (event.getCode() == null) {
            return;
        }
        switch (event.getCode()) {
            case Q:
                queryData();
                return;
            case E:
                exportData();
                return;
            case R:
                clearAction();
                return;
        }
        super.controlAltHandler(event);
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
