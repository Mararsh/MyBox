package mara.mybox.controller;

import java.sql.Connection;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.QueryCondition;
import mara.mybox.db.data.QueryCondition.DataOperation;
import mara.mybox.db.table.BaseTable;
import mara.mybox.db.table.TableQueryCondition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-5-2
 * @License Apache License Version 2.0
 */
public abstract class BaseDataManageController<P> extends BaseSysTableController<P> {

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
    protected ControlWebView infoViewController;
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
        sizePrefix = "SELECT count(*) FROM " + tableName;
        clearPrefix = "DELETE FROM " + tableName;
        if (tableDefinition != null) {
            tableDefinitionString = tableDefinition.html();
            viewDefinition = tableDefinition;
        }
    }

    protected DataExportController dataExporter() {
        return (DataExportController) openStage(Fxmls.DataExportFxml);
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
    public boolean checkBeforeLoadingTableData() {
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
        infoViewController.loadContents​(null);
        finalTitle = queryCondition.getTitle().replaceAll("\n", " ");
        setQuerySQL();
        return true;
    }

    protected void setPageSQL() {
        if (startRowOfCurrentPage < 0) {
            startRowOfCurrentPage = 0;
        }
        if (pageSize < 0) {
            pageSize = 50;
        }
        String dataFetch = "OFFSET " + startRowOfCurrentPage + " ROWS FETCH NEXT " + pageSize + " ROWS ONLY";
        pageQuerySQL = dataQuerySQL + " " + dataFetch;
        pageQueryString = pageQuerySQL;
        if (queryCondition != null) {
            queryCondition.setFetch(dataFetch);
            if (pagesNumber > 1) {
                finalTitle = queryCondition.getTitle() + " - " + message("Page") + (currentPage + 1);
            }
        }
    }

    @Override
    public List<P> readPageData(Connection conn) {
        setPageSQL();
        return tableDefinition.query(conn, pageQuerySQL);
    }

    @Override
    public void postLoadedTableData() {
        super.postLoadedTableData();
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
            infoViewController.setParent(this);
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
                String label = column.getColumnName();
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
                        String q = column.getColumnName() + " ASC";
                        queryOrder = queryOrder.isBlank() ? q : queryOrder + ", " + q;
                    }
                } else if (item.endsWith(" " + message("Descending"))) {
                    String name = item.substring(0, item.length() - message("Descending").length() - 1);
                    ColumnDefinition column = viewDefinition.columnByMessage(name);
                    if (column != null) {
                        String q = column.getColumnName() + " DESC";
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
            if (queryButton != null) {
                queryButton.requestFocus();
            }
            loadInfo();
            if (geoController != null) {
                geoController.setParent(this);
                geoController.loadTree();
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();

            if (clearButton != null) {
                NodeStyleTools.removeTooltip(clearButton);
            }
            if (deleteButton != null) {
                NodeStyleTools.setTooltip(deleteButton, message("Delete") + "\nDELETE / CTRL+d / ALT+d\n\n"
                        + message("DataDeletedComments"));
            }
            if (setButton != null) {
                NodeStyleTools.removeTooltip(setButton);
            }
            if (importButton != null) {
                NodeStyleTools.removeTooltip(importButton);
            }
            if (exportButton != null) {
                NodeStyleTools.removeTooltip(exportButton);
            }
            if (queryButton != null) {
                NodeStyleTools.removeTooltip(queryButton);
            }

            if (csvEditController != null) {
                NodeStyleTools.removeTooltip(csvEditController.inputButton);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
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
                    html += "<SPAN class=\"valueText\">" + dataSize + "</SPAN></BR></BR>";
                }
                if (queryCondition.getFetch() != null && !queryCondition.getFetch().isBlank()) {
                    html += "<SPAN class=\"boldText\">" + message("CurrentPage") + ": </SPAN></BR>";
                    html += "<SPAN class=\"valueText\">" + queryCondition.getFetch() + "</SPAN></BR>";
                    html += "<SPAN class=\"boldText\">" + message("DataNumber") + ": </SPAN>";
                    html += "<SPAN class=\"valueText\">" + tableData.size() + "</SPAN></BR></BR>";
                }
                html += loadMoreInfo();
            }
            html = HtmlWriteTools.html(null, html);
            infoViewController.loadContents​(html);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected String loadMoreInfo() {
        return "";
    }

    @Override
    protected void checkButtons() {
        if (isSettingValues) {
            return;
        }
        super.checkButtons();
        clearButton.setDisable(false);

        boolean isEmpty = tableData == null || tableData.isEmpty();
        boolean none = isEmpty || tableView.getSelectionModel().getSelectedItem() == null;
        if (setButton != null) {
            setButton.setDisable(none);
        }
        if (locationButton != null) {
            locationButton.setDisable(none);
        }
    }

    @Override
    public long readDataSize(Connection conn) {
//        MyBoxLog.debug(sizeQuerySQL);
        return DerbyBase.size(conn, sizeQuerySQL);
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
                DataQueryController controller = (DataQueryController) openStage(Fxmls.DataQueryFxml);
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
            menu = new MenuItem(message("PopupClose"), StyleTools.getIconImage("iconCancel.png"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent event) -> {
                popMenu.hide();
                popMenu = null;
            });
            popMenu.getItems().add(menu);

            LocateTools.locateBelow((Region) mouseEvent.getSource(), popMenu);

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
                DataQueryController controller = (DataQueryController) openStage(Fxmls.DataQueryFxml);
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
            menu = new MenuItem(message("PopupClose"), StyleTools.getIconImage("iconCancel.png"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent event) -> {
                popMenu.hide();
                popMenu = null;
            });
            popMenu.getItems().add(menu);

            LocateTools.locateBelow((Region) mouseEvent.getSource(), popMenu);

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
                    + "\n" + message("Page") + (currentPage + 1);
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

        if (!PopTools.askSure(this, getBaseTitle(), message("SureClearConditions")
                + "\n\n" + type + "\n" + title + "\n\n" + sql + "\n\n" + message("DataDeletedComments"))) {
            return;
        }

        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

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
            start(task);
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
        if (!PopTools.askSure(this, getBaseTitle(), message("SureClearConditions")
                + "\n\n" + clearCondition.getTitle().replaceAll("</br>", "\n")
                + "\n\n" + clearSQL + "\n\n" + message("DataDeletedComments"))) {
            return;
        }

        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {
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
            start(task);
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
            menu = new MenuItem(message("PopupClose"), StyleTools.getIconImage("iconCancel.png"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent event) -> {
                popMenu.hide();
                popMenu = null;
            });
            popMenu.getItems().add(menu);

            LocateTools.locateBelow((Region) mouseEvent.getSource(), popMenu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean keyF1() {
        return controlAltQ();
    }

    @Override
    public boolean keyF3() {
        return controlAltE();
    }

    @Override
    public boolean controlAltQ() {
        queryData();
        return true;
    }

    @Override
    public boolean controlAltE() {
        exportData();
        return true;
    }

    @Override
    public boolean controlAltR() {
        clearAction();
        return true;
    }

    @Override
    public void cleanPane() {
        try {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            if (geoController != null) {
                geoController.cleanPane();
            }
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
